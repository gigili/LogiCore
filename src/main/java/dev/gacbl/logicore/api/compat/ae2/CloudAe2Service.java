package dev.gacbl.logicore.api.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.*;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CloudAe2Service implements IGridNodeService, IGridNodeListener<CloudAe2Service>, IInWorldGridNodeHost, IActionHost, IStorageProvider {

    private final CloudInterfaceBlockEntity host;
    private final IManagedGridNode mainNode;
    private final VirtualCloudStorage virtualStorage;
    private boolean hasRegistered = false;

    private long lastSyncTick = 0;
    private long lastSyncedCycles = -1;

    private static final List<CachedEntry> CACHED_ENTRIES = new ArrayList<>();
    private static boolean cacheInitialized = false;

    private record CachedEntry(AEItemKey key, int cost) {
    }

    public CloudAe2Service(CloudInterfaceBlockEntity host) {
        this.host = host;
        this.mainNode = GridHelper.createManagedNode(this, this)
                .setVisualRepresentation(host.getBlockState().getBlock())
                .setInWorldNode(true)
                .setTagName("cloud_interface")
                .setIdlePowerUsage(5.0d)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IStorageProvider.class, this);

        this.virtualStorage = new VirtualCloudStorage();

        if (host.getLevel() instanceof ServerLevel) {
            mainNode.create(host.getLevel(), host.getBlockPos());
        }
    }

    public static void rebuildCache() {
        synchronized (CACHED_ENTRIES) {
            CACHED_ENTRIES.clear();
            LogiCore.LOGGER.info("Building Cloud AE2 Storage Cache...");

            for (Map.Entry<Item, Integer> entry : CycleValueManager.CYCLE_VALUES.entrySet()) {
                Item item = entry.getKey();
                int cost = entry.getValue();

                if (item == Items.AIR || cost <= 0) continue;

                try {
                    AEItemKey key = AEItemKey.of(item);
                    CACHED_ENTRIES.add(new CachedEntry(key, cost));
                } catch (Exception e) {
                    LogiCore.LOGGER.warn("Failed to create AE2 key for item: {}", item, e);
                }
            }
            cacheInitialized = true;
            LogiCore.LOGGER.info("Cloud Cache built with {} items.", CACHED_ENTRIES.size());
        }
    }

    /**
     * Calculates a safe sync interval based on the number of registered items.
     * Prevents "Death by Update Loop" in large modpacks.
     */
    private int getDynamicSyncInterval() {
        int count;
        synchronized (CACHED_ENTRIES) {
            count = CACHED_ENTRIES.size();
        }
        if (count < 500) return 20;       // Small pack: 1 second
        if (count < 2000) return 100;     // Medium pack: 5 seconds
        if (count < 10000) return 400;    // Large pack: 20 seconds
        return 1200;                      // Kitchen Sink (ATM, etc): 60 seconds
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

        if (!cacheInitialized) {
            rebuildCache();
        }

        if (!hasRegistered) {
            var node = mainNode.getNode();
            if (node != null && node.getGrid() != null) {
                IStorageService service = node.getGrid().getService(IStorageService.class);
                if (service != null) {
                    service.refreshNodeStorageProvider(node);
                    hasRegistered = true;
                }
            }
        }

        if (host.getOwner() != null) {
            long now = sl.getGameTime();
            int syncInterval = getDynamicSyncInterval();
            long timeDiff = now - lastSyncTick;

            if (timeDiff >= syncInterval) {
                String ownerKey = CycleSavedData.getKey(sl, host.getOwner());
                long currentCycles = CycleSavedData.get(sl).getCyclesByKeyString(ownerKey);

                if (currentCycles != lastSyncedCycles) {
                    long delta = Math.abs(currentCycles - lastSyncedCycles);

                    boolean criticalStateChange = (lastSyncedCycles <= 0 && currentCycles > 0) || (lastSyncedCycles > 0 && currentCycles == 0);
                    boolean isSignificant = lastSyncedCycles > 0 && ((double) delta / lastSyncedCycles > 0.05);

                    if (lastSyncedCycles == -1 || criticalStateChange || isSignificant) {
                        lastSyncTick = now;
                        lastSyncedCycles = currentCycles;

                        var node = mainNode.getNode();
                        if (node != null && node.getGrid() != null) {
                            IStorageService service = node.getGrid().getService(IStorageService.class);
                            if (service != null) {
                                service.refreshNodeStorageProvider(node);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRemove() {
        mainNode.destroy();
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        mainNode.saveToNBT(tag);
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        mainNode.loadFromNBT(tag);
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

    private class VirtualCloudStorage implements MEStorage {

        @Override
        public void getAvailableStacks(KeyCounter out) {
            if (host.isRemoved()) return;
            if (!(host.getLevel() instanceof ServerLevel sl)) return;
            if (host.getOwner() == null) return;

            if (!cacheInitialized) return;

            String ownerKey = CycleSavedData.getKey(sl, host.getOwner());
            long totalCycles = CycleSavedData.get(sl).getCyclesByKeyString(ownerKey);

            if (totalCycles <= 0) return;

            synchronized (CACHED_ENTRIES) {
                for (CachedEntry entry : CACHED_ENTRIES) {
                    long count = totalCycles / entry.cost;
                    if (count > 0) {
                        out.add(entry.key, count);
                    }
                }
            }
        }

        @Override
        public Component getDescription() {
            return Component.translatable("ui.tooltip.logicore.cycles_clean");
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof AEItemKey itemKey)) return 0;
            if (amount <= 0) return 0;
            if (!(host.getLevel() instanceof ServerLevel sl)) return 0;
            if (host.getOwner() == null) return 0;

            long costPerItem = CycleValueManager.getCycleValue(itemKey.toStack());
            if (costPerItem <= 0) return 0;

            long totalValueToAdd = costPerItem * amount;

            if (mode == Actionable.MODULATE) {
                String ownerKey = CycleSavedData.getKey(sl, host.getOwner());
                CycleSavedData.get(sl).modifyCycles(sl, ownerKey, totalValueToAdd);
            }
            return amount;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!(what instanceof AEItemKey itemKey)) return 0;
            if (amount <= 0) return 0;
            if (!(host.getLevel() instanceof ServerLevel sl)) return 0;
            if (host.getOwner() == null) return 0;

            long costPerItem = CycleValueManager.getCycleValue(itemKey.toStack());
            if (costPerItem <= 0) return 0;

            String ownerKey = CycleSavedData.getKey(sl, host.getOwner());

            long totalCycles = CycleSavedData.get(sl).getCyclesByKeyString(ownerKey);

            long maxAffordable = totalCycles / costPerItem;
            long toExtract = Math.min(amount, maxAffordable);

            if (toExtract <= 0) return 0;

            if (mode == Actionable.MODULATE) {
                long totalCost = toExtract * costPerItem;
                CycleSavedData.get(sl).modifyCycles(sl, ownerKey, -totalCost);
            }
            return toExtract;
        }
    }
}
