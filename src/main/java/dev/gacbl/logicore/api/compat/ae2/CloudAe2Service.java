package dev.gacbl.logicore.api.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.*;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.api.util.AECableType;
import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.api.cycles.CycleValueManager;
import dev.gacbl.logicore.blocks.cloud_interface.CloudInterfaceBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CloudAe2Service implements IGridNodeService, IGridNodeListener<CloudAe2Service>, ICraftingProvider, IInWorldGridNodeHost, IActionHost, IStorageProvider {

    private final CloudInterfaceBlockEntity host;
    private final IManagedGridNode mainNode;
    private final VirtualCloudStorage virtualStorage;
    private final List<IPatternDetails> cachedPatterns = new ArrayList<>();
    private boolean patternsInitialized = false;

    public static final Item CATALYST_ITEM = Items.STRUCTURE_VOID;
    private final Queue<PendingCraft> pendingCrafts = new ArrayDeque<>();
    private static final String NBT_PENDING_CRAFTS = "PendingCrafts";

    private long lastSyncedCycles = -1;
    private long lastSyncTick = 0;

    public CloudAe2Service(CloudInterfaceBlockEntity host) {
        this.host = host;
        this.virtualStorage = new VirtualCloudStorage();
        this.mainNode = GridHelper.createManagedNode(this, this)
                .setVisualRepresentation(host.getBlockState().getBlock())
                .setInWorldNode(true)
                .setTagName("cloud_interface")
                .setIdlePowerUsage(5.0d)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(ICraftingProvider.class, this)
                .addService(IStorageProvider.class, this);
    }

    private void initPatterns() {
        if (patternsInitialized) return;
        cachedPatterns.clear();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR || item == CATALYST_ITEM) continue;

            ItemStack stack = new ItemStack(item);
            long cost = CycleValueManager.getCycleValue(stack);
            AEItemKey catalystKey = AEItemKey.of(CATALYST_ITEM);

            if (cost > 0) {

                AEItemKey outputKey = AEItemKey.of(stack);

                List<GenericStack> inputs = List.of(new GenericStack(catalystKey, cost));
                List<GenericStack> outputs = List.of(new GenericStack(outputKey, 1));

                try {
                    ItemStack patternStack = PatternDetailsHelper.encodeProcessingPattern(inputs, outputs);
                    IPatternDetails pattern = PatternDetailsHelper.decodePattern(patternStack, host.getLevel());
                    if (pattern != null) {
                        cachedPatterns.add(pattern);
                    }
                } catch (Exception e) {
                    LogiCore.LOGGER.error("Failed to encode pattern for {}", item, e);
                }
            }
        }
        patternsInitialized = true;
    }

    @Override
    public Object getGridNode() {
        return mainNode.getNode();
    }

    @Override
    public void serverTick() {
        if (!mainNode.isReady()) {
            mainNode.create(host.getLevel(), host.getBlockPos());
        }
        if (!patternsInitialized && mainNode.isActive()) {
            initPatterns();
            if (mainNode.getNode() != null) {
                mainNode.getNode().getGrid().getService(appeng.api.networking.crafting.ICraftingService.class)
                        .refreshNodeCraftingProvider(mainNode.getNode());
                mainNode.getNode().getGrid().getStorageService().refreshNodeStorageProvider(mainNode.getNode());
            }
        }

        if (!pendingCrafts.isEmpty() && mainNode.isActive()) {
            processPendingCrafts();
        }

        if (mainNode.isActive() && host.getLevel() != null && host.getOwner() != null) {
            long now = host.getLevel().getGameTime();

            if (now - lastSyncTick >= 20) {
                ServerLevel serverLevel = Objects.requireNonNull(host.getLevel().getServer()).overworld();
                String ownerKey = CycleSavedData.getKey(serverLevel, host.getOwner());
                long currentCycles = CycleSavedData.get(serverLevel).getCyclesByKeyString(ownerKey);

                if (currentCycles != lastSyncedCycles) {
                    lastSyncedCycles = currentCycles;
                    lastSyncTick = now;

                    if (mainNode.getNode() != null) {
                        mainNode.getNode().getGrid().getStorageService().refreshNodeStorageProvider(mainNode.getNode());
                    }
                }
            }
        }
    }

    private void processPendingCrafts() {
        IStorageService storageService = Objects.requireNonNull(mainNode.getNode()).getGrid().getService(IStorageService.class);
        MEStorage inventory = storageService.getInventory();
        IActionSource source = IActionSource.ofMachine(this);

        Iterator<PendingCraft> it = pendingCrafts.iterator();
        while (it.hasNext()) {
            PendingCraft craft = it.next();
            long inserted = inventory.insert(craft.key, craft.amount, Actionable.MODULATE, source);

            craft.amount -= inserted;
            if (craft.amount <= 0) {
                it.remove();
            }
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        if (!patternsInitialized) initPatterns();
        return cachedPatterns;
    }

    @Override
    public int getPatternPriority() {
        return 1000;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (host.getLevel() == null || !mainNode.isActive()) return false;
        UUID owner = host.getOwner();
        if (owner == null) return false;

        GenericStack output = patternDetails.getPrimaryOutput();
        if (output == null || !(output.what() instanceof AEItemKey itemKey)) return false;

        long costPerItem = CycleValueManager.getCycleValue(itemKey.toStack());
        if (costPerItem <= 0) return false;

        long quantity = output.amount();
        long totalCost = costPerItem * quantity;

        ServerLevel serverLevel = Objects.requireNonNull(host.getLevel().getServer()).overworld();
        String ownerKey = CycleSavedData.getKey(serverLevel, owner);
        CycleSavedData data = CycleSavedData.get(serverLevel);
        long currentCycles = data.getCyclesByKeyString(ownerKey);

        if (currentCycles >= totalCost) {
            data.modifyCycles(serverLevel, ownerKey, -totalCost);

            pendingCrafts.add(new PendingCraft(itemKey, quantity));
            host.setChanged();

            return true;
        }

        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    // --- Boilerplate ---

    @Override
    public void onRemove() {
        mainNode.destroy();
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        mainNode.saveToNBT(tag);
        if (!pendingCrafts.isEmpty()) {
            ListTag list = new ListTag();
            for (PendingCraft craft : pendingCrafts) {
                CompoundTag craftTag = new CompoundTag();
                craftTag.put("key", craft.key.toTag(registries));
                craftTag.putLong("amount", craft.amount);
                list.add(craftTag);
            }
            tag.put(NBT_PENDING_CRAFTS, list);
        }
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        mainNode.loadFromNBT(tag);

        pendingCrafts.clear();
        if (tag.contains(NBT_PENDING_CRAFTS)) {
            ListTag list = tag.getList(NBT_PENDING_CRAFTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag craftTag = list.getCompound(i);

                AEKey key = AEKey.fromTagGeneric(registries, craftTag.getCompound("key"));
                long amount = craftTag.getLong("amount");

                if (key instanceof AEItemKey itemKey) {
                    pendingCrafts.add(new PendingCraft(itemKey, amount));
                }
            }
        }
    }

    @Override
    public void onSaveChanges(CloudAe2Service nodeOwner, IGridNode node) {
        host.setChanged();
    }

    @Override
    public IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    @Override
    public @Nullable IGridNode getGridNode(Direction direction) {
        return mainNode.getNode();
    }

    @Override
    public AECableType getCableConnectionType(Direction direction) {
        return AECableType.SMART;
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storageMounts.mount(virtualStorage);
    }

    private static class PendingCraft {
        final AEItemKey key;
        long amount;

        PendingCraft(AEItemKey key, long amount) {
            this.key = key;
            this.amount = amount;
        }
    }

    private class VirtualCloudStorage implements MEStorage {
        private final AEItemKey CATALYST_KEY = AEItemKey.of(CATALYST_ITEM);

        @Override
        public void getAvailableStacks(KeyCounter out) {
            if (host.getLevel() == null || host.getOwner() == null) return;

            ServerLevel serverLevel = Objects.requireNonNull(host.getLevel().getServer()).overworld();
            String ownerKey = CycleSavedData.getKey(serverLevel, host.getOwner());
            long cycles = CycleSavedData.get(serverLevel).getCyclesByKeyString(ownerKey);

            if (cycles > 0) {
                out.add(CATALYST_KEY, cycles);
            }
        }

        @Override
        public Component getDescription() {
            return Component.literal("Cloud Cycles");
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            assert host.getLevel() != null;
            ServerLevel serverLevel = Objects.requireNonNull(host.getLevel().getServer()).overworld();
            String ownerKey = CycleSavedData.getKey(serverLevel, host.getOwner());
            CycleSavedData data = CycleSavedData.get(serverLevel);
            if (what.equals(CATALYST_KEY)) {
                data.modifyCycles(serverLevel, ownerKey, amount);
                return amount;
            }

            return 0;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (what.equals(CATALYST_KEY)) {
                if (mode == Actionable.SIMULATE) {
                    if (host.getLevel() != null && host.getOwner() != null) {
                        ServerLevel serverLevel = Objects.requireNonNull(host.getLevel().getServer()).overworld();
                        String ownerKey = CycleSavedData.getKey(serverLevel, host.getOwner());
                        long cycles = CycleSavedData.get(serverLevel).getCyclesByKeyString(ownerKey);
                        return Math.min(amount, cycles);
                    }
                    return 0;
                }
                return amount;
            }
            return 0;
        }
    }
}
