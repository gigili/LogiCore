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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CloudAe2Service implements IGridNodeService, IGridNodeListener<CloudAe2Service>, ICraftingProvider, IInWorldGridNodeHost, IActionHost, IStorageProvider {

    private final CloudInterfaceBlockEntity host;
    private final IManagedGridNode mainNode;
    private final VirtualCloudStorage virtualStorage;

    private static final Map<AEItemKey, IPatternDetails> PATTERNS_BY_OUTPUT = new ConcurrentHashMap<>();
    private static final List<IPatternDetails> PATTERN_LIST_VIEW = new CopyOnWriteArrayList<>();
    private static volatile boolean basePatternsInitialized = false;

    private long lastPatternRefreshTick = 0;
    private long lastSyncTick = 0;
    private long lastSyncedCycles = -1;
    private boolean hasRegistered = false;

    private final Queue<PendingCraft> pendingCrafts = new ArrayDeque<>();

    private static final Item CATALYST_ITEM = Items.STRUCTURE_VOID;
    private static final String NBT_PENDING_CRAFTS = "PendingCrafts";

    public CloudAe2Service(CloudInterfaceBlockEntity host) {
        this.host = host;

        this.mainNode = GridHelper.createManagedNode(this, this)
                .setVisualRepresentation(host.getBlockState().getBlock())
                .setInWorldNode(true)
                .setTagName("cloud_interface")
                .setIdlePowerUsage(5.0d)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(ICraftingProvider.class, this)
                .addService(IStorageProvider.class, this);

        this.virtualStorage = new VirtualCloudStorage();

        if (host.getLevel() instanceof ServerLevel) {
            mainNode.create(host.getLevel(), host.getBlockPos());
        }
    }

    @Override
    public void serverTick() {
        Level lvl = host.getLevel();
        if (lvl == null || lvl.isClientSide) return;
        if (!(lvl instanceof ServerLevel sl)) return;

        if (mainNode.getNode() == null) {
            mainNode.create(sl, host.getBlockPos());
        }

        if (!mainNode.isActive()) return;

        if (!basePatternsInitialized) {
            initBasePatterns(sl);
        }

        refreshVariantPatternsFromNetwork(sl.getGameTime());

        if (!hasRegistered) {
            var node = mainNode.getNode();
            if (node != null && node.getGrid() != null) {
                node.getGrid().getStorageService().refreshNodeStorageProvider(node);
                node.getGrid().getService(appeng.api.networking.crafting.ICraftingService.class).refreshNodeCraftingProvider(node);
                hasRegistered = true;
            }
        }

        if (host.getOwner() != null) {
            long now = sl.getGameTime();
            if (now - lastSyncTick >= 20) {
                lastSyncTick = now;

                String ownerKey = CycleSavedData.getKey(sl, host.getOwner());
                long currentCycles = CycleSavedData.get(sl).getCyclesByKeyString(ownerKey);

                if (currentCycles != lastSyncedCycles) {
                    lastSyncedCycles = currentCycles;

                    var node = mainNode.getNode();
                    if (node != null && node.getGrid() != null) {
                        node.getGrid().getStorageService().refreshNodeStorageProvider(node);
                    }
                }
            }
        }

        if (!pendingCrafts.isEmpty()) {
            processPendingCrafts();
        }
    }

    private void initBasePatterns(ServerLevel sl) {
        if (basePatternsInitialized) return;

        synchronized (PATTERNS_BY_OUTPUT) {
            if (basePatternsInitialized) return;

            PATTERNS_BY_OUTPUT.clear();
            PATTERN_LIST_VIEW.clear();

            AEItemKey catalystKey = AEItemKey.of(CATALYST_ITEM);

            for (Item item : BuiltInRegistries.ITEM) {
                if (item == Items.AIR || item == CATALYST_ITEM) continue;

                ItemStack stack = new ItemStack(item);
                long cost = CycleValueManager.getCycleValue(stack);
                if (cost <= 0) continue;

                AEItemKey outKey = AEItemKey.of(stack);
                IPatternDetails p = makeProcessingPattern(catalystKey, cost, outKey, sl);
                if (p != null) {
                    PATTERNS_BY_OUTPUT.put(outKey, p);
                }
            }

            PATTERN_LIST_VIEW.addAll(PATTERNS_BY_OUTPUT.values());
            basePatternsInitialized = true;
        }
    }

    @Nullable
    private IPatternDetails makeProcessingPattern(AEItemKey catalystKey, long cost, AEItemKey outKey, ServerLevel sl) {
        try {
            List<GenericStack> inputs = List.of(new GenericStack(catalystKey, cost));
            List<GenericStack> outputs = List.of(new GenericStack(outKey, 1));
            ItemStack patternStack = PatternDetailsHelper.encodeProcessingPattern(inputs, outputs);
            return PatternDetailsHelper.decodePattern(patternStack, sl);
        } catch (Exception e) {
            LogiCore.LOGGER.error("Failed to encode pattern for {}", outKey, e);
            return null;
        }
    }

    private void refreshVariantPatternsFromNetwork(long nowTick) {
        if (!mainNode.isActive()) return;
        if (nowTick - lastPatternRefreshTick < 20) return;
        lastPatternRefreshTick = nowTick;

        IGridNode node = mainNode.getNode();
        if (node == null || node.getGrid() == null) return;

        IStorageService storage = node.getGrid().getService(IStorageService.class);
        if (storage == null) return;

        KeyCounter cached = storage.getCachedInventory();
        if (cached == null) return;

        AEItemKey catalystKey = AEItemKey.of(CATALYST_ITEM);
        ServerLevel sl = (ServerLevel) host.getLevel();

        for (var entry : cached) {
            AEKey k = entry.getKey();
            if (!(k instanceof AEItemKey itemKey)) continue;

            Item item = itemKey.getItem();
            if (item == Items.AIR || item == CATALYST_ITEM) continue;
            if (PATTERNS_BY_OUTPUT.containsKey(itemKey)) continue;

            long cost = CycleValueManager.getCycleValue(itemKey.toStack());
            if (cost <= 0) continue;

            IPatternDetails p = makeProcessingPattern(catalystKey, cost, itemKey, sl);
            if (p != null) {
                PATTERNS_BY_OUTPUT.put(itemKey, p);
                PATTERN_LIST_VIEW.add(p);
            }
        }
    }

    private void processPendingCrafts() {
        IGridNode node = Objects.requireNonNull(mainNode.getNode());
        IStorageService storageService = node.getGrid().getService(IStorageService.class);
        MEStorage inventory = storageService.getInventory();
        IActionSource source = IActionSource.ofMachine(this);

        Iterator<PendingCraft> it = pendingCrafts.iterator();
        while (it.hasNext()) {
            PendingCraft craft = it.next();

            System.out.println("Processing craft " + craft.toString());

            long inserted = inventory.insert(craft.key, craft.amount, Actionable.SIMULATE, source);
            long remainder = craft.amount - inserted;

            if (remainder <= 0) {
                inventory.insert(craft.key, craft.amount, Actionable.MODULATE, source);
                it.remove();
                continue;
            }

            craft.ticksStuck++;
            if (craft.ticksStuck >= 100) {
                dropToWorld(craft, craft.amount);
                it.remove();
            }
        }
    }

    private void dropToWorld(PendingCraft craft, long amount) {
        System.out.println("Dropping craft " + craft.key + " to world " + amount);
        Level lvl = host.getLevel();
        if (!(lvl instanceof ServerLevel)) return;

        long remaining = amount;
        while (remaining > 0) {
            int toDrop = (int) Math.min(remaining, craft.key.getMaxStackSize());
            ItemStack stack = craft.key.toStack(toDrop);

            Vec3 pos = Vec3.atCenterOf(host.getBlockPos()).add(0, 0.7, 0);
            ItemEntity entity = new ItemEntity(lvl, pos.x, pos.y, pos.z, stack);
            entity.setDeltaMovement(0, 0.2, 0);
            lvl.addFreshEntity(entity);

            remaining -= toDrop;
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        Level lvl = host.getLevel();
        if (lvl instanceof ServerLevel sl && !basePatternsInitialized) {
            initBasePatterns(sl);
        }
        return PATTERN_LIST_VIEW;
    }

    @Override
    public int getPatternPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        System.out.println("Calling pushPattern");
        if (!(host.getLevel() instanceof ServerLevel)) return false;
        System.out.println("pushPattern host level check ok");
        if (!mainNode.isActive()) return false;
        System.out.println("pushPattern mainNode isActive check ok");

        GenericStack output = patternDetails.getPrimaryOutput();
        if (output == null) {
            System.out.println("pushPattern GenericStack output is null");
            return false;
        }
        System.out.println("pushPattern Checking GenericStack");
        if (!(getWhat(output) instanceof AEItemKey itemKey)) return false;

        System.out.println("pushPattern: " + patternDetails.getPrimaryOutput().what().wrapForDisplayOrFilter().getDisplayName().getString());

        pendingCrafts.add(new PendingCraft(itemKey, output.amount()));
        host.setChanged();
        return true;
    }

    private static AEKey getWhat(GenericStack output) {
        return output.what();
    }

    @Override
    public boolean isBusy() {
        return false;
    }

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
                craftTag.put("key", craft.key.toTagGeneric(registries));
                craftTag.putLong("amount", craft.amount);
                craftTag.putInt("ticksStuck", craft.ticksStuck);
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
                int ticksStuck = craftTag.getInt("ticksStuck");

                if (key instanceof AEItemKey itemKey) {
                    PendingCraft craft = new PendingCraft(itemKey, amount);
                    craft.ticksStuck = ticksStuck;
                    pendingCrafts.add(craft);
                }
            }
        }
    }

    @Override
    public void onSaveChanges(CloudAe2Service nodeOwner, IGridNode node) {
        host.setChanged();
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        mounts.mount(virtualStorage, Integer.MAX_VALUE);
    }

    @Override
    public AECableType getCableConnectionType(Direction direction) {
        return AECableType.SMART;
    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    @Override
    public @Nullable IGridNode getGridNode(@NotNull Direction dir) {
        return mainNode.getNode();
    }


    private static class PendingCraft {
        final AEItemKey key;
        long amount;
        int ticksStuck;

        PendingCraft(AEItemKey key, long amount) {
            this.key = key;
            this.amount = amount;
        }
    }

    private class VirtualCloudStorage implements MEStorage {

        private boolean isCatalyst(AEKey what) {
            return what instanceof AEItemKey k && k.getItem() == CATALYST_ITEM;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            if (!(host.getLevel() instanceof ServerLevel sl)) return;
            if (host.getOwner() == null) return;

            String ownerKey = CycleSavedData.getKey(sl, host.getOwner());
            long cycles = CycleSavedData.get(sl).getCyclesByKeyString(ownerKey);

            if (cycles > 0) {
                out.add(AEItemKey.of(CATALYST_ITEM), cycles);
            }
        }

        @Override
        public Component getDescription() {
            return Component.translatable("ui.tooltip.logicore.cycles_clean");
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            System.out.println("Inserting " + what.wrapForDisplayOrFilter().getDisplayName().getString() + " amount " + amount);
            if (!isCatalyst(what)) return 0;
            if (amount <= 0) return 0;
            if (mode != Actionable.MODULATE) return amount;

            if (!(host.getLevel() instanceof ServerLevel sl)) return 0;
            if (host.getOwner() == null) return 0;

            String ownerKey = CycleSavedData.getKey(sl, host.getOwner());
            CycleSavedData.get(sl).modifyCycles(sl, ownerKey, amount);
            return amount;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            System.out.println("Extracting " + what.wrapForDisplayOrFilter().getDisplayName().getString() + " amount " + amount);
            if (!isCatalyst(what)) return 0;
            if (amount <= 0) return 0;

            if (!(host.getLevel() instanceof ServerLevel sl)) return 0;
            if (host.getOwner() == null) return 0;

            String ownerKey = CycleSavedData.getKey(sl, host.getOwner());
            long cycles = CycleSavedData.get(sl).getCyclesByKeyString(ownerKey);

            long extracted = Math.min(amount, cycles);

            if (mode == Actionable.MODULATE && extracted > 0) {
                CycleSavedData.get(sl).modifyCycles(sl, ownerKey, -extracted);
            }

            return extracted;
        }
    }
}
