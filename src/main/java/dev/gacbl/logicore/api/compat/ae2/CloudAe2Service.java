package dev.gacbl.logicore.api.compat.ae2;

// ============================================================
// AE2 Cloud Service - DISABLED during 26.1 port
// To re-enable: Remove the /* and */ comment markers below,
// ensure AE2 (Applied Energistics 2) is available on the
// classpath with the correct API for Minecraft 26.1.
// ============================================================
/*
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
import dev.gacbl.logicore.core.Utils;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.stream.Collectors;

public class CloudAe2Service {
    private final CloudInterfaceBlockEntity cloudInterface;
    private IStorageService storageService;
    private GridConnectionManager gridManager;

    public CloudAe2Service(CloudInterfaceBlockEntity cloudInterface) {
        this.cloudInterface = cloudInterface;
        this.gridManager = new GridConnectionManager(cloudInterface);
    }

    public void serverTick() {
        if (cloudInterface.getLevel() == null || cloudInterface.getLevel().isClientSide()) return;
        if (!(cloudInterface.getLevel() instanceof ServerLevel serverLevel)) return;

        // Check if AE2 is available and we have a valid grid node
        var gridNode = gridManager.getGridNode();
        if (gridNode == null) return;

        // Get the storage service from the grid
        storageService = gridNode.getGrid().getStorageService();
        if (storageService == null) return;

        // Process upload/download operations
        processOperations();
    }

    private void processOperations() {
        // Upload items from buffer to AE2 network
        if (cloudInterface.hasItem()) {
            ItemStack stack = cloudInterface.extract();
            if (!stack.isEmpty()) {
                var insertResult = storageService.getInventory().insert(
                        AEItemKey.of(stack),
                        stack.getCount(),
                        Actionable.SIMULATE,
                        createActionSource()
                );
                // Check if we can insert all items
                if (insertResult == 0) {
                    storageService.getInventory().insert(
                            AEItemKey.of(stack),
                            stack.getCount(),
                            Actionable.MODULATE,
                            createActionSource()
                    );
                }
            }
        }

        // Download items from AE2 network when cycles are available
        long availableCycles = getAvailableCyclesForDownload();
        if (availableCycles > 0) {
            downloadItems(availableCycles);
        }
    }

    private long getAvailableCyclesForDownload() {
        if (cloudInterface.getLevel() == null) return 0;
        UUID owner = cloudInterface.getOwner();
        if (owner == null) return 0;

        String key = CycleSavedData.getKey((ServerLevel) cloudInterface.getLevel(), owner);
        return CycleSavedData.get((ServerLevel) cloudInterface.getLevel()).getCyclesByKeyString(key);
    }

    private void downloadItems(long maxCycles) {
        // Scan AE2 network for items with cycle values
        var availableItems = storageService.getInventory().getAvailableStacks();
        if (availableItems == null) return;

        List<Map.Entry<AEKey, Long>> cycleItems = new ArrayList<>();
        availableItems.forEach((key, count) -> {
            if (key instanceof AEItemKey itemKey) {
                ItemStack stack = itemKey.toStack((int) Math.min(count, Integer.MAX_VALUE));
                if (CycleValueManager.hasCycleValue(stack)) {
                    cycleItems.add(Map.entry(key, count));
                }
            }
        });

        // Extract items and convert to cycles
        for (var entry : cycleItems) {
            if (maxCycles <= 0) break;

            AEKey key = entry.getKey();
            long count = entry.getValue();

            if (key instanceof AEItemKey itemKey) {
                long extracted = storageService.getInventory().extract(
                        key,
                        count,
                        Actionable.SIMULATE,
                        createActionSource()
                );

                if (extracted > 0) {
                    ItemStack extractedStack = itemKey.toStack((int) Math.min(extracted, Integer.MAX_VALUE));
                    int cycleValue = CycleValueManager.getCycleValue(extractedStack);
                    long totalCycles = (long) cycleValue * extracted;

                    if (totalCycles <= maxCycles) {
                        storageService.getInventory().extract(
                                key,
                                extracted,
                                Actionable.MODULATE,
                                createActionSource()
                        );

                        // Credit cycles to player
                        if (cloudInterface.getOwner() != null) {
                            String ownerKey = CycleSavedData.getKey(
                                    (ServerLevel) cloudInterface.getLevel(),
                                    cloudInterface.getOwner()
                            );
                            CycleSavedData.get((ServerLevel) cloudInterface.getLevel())
                                    .modifyCycles((ServerLevel) cloudInterface.getLevel(), ownerKey, totalCycles);
                        }
                        maxCycles -= totalCycles;
                    }
                }
            }
        }
    }

    private IActionSource createActionSource() {
        return IActionSource.ofMachine(gridManager.getGridNode());
    }

    public void onRemove() {
        if (gridManager != null) {
            gridManager.destroy();
        }
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        gridManager.save(tag);
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        gridManager.load(tag);
    }
}
*/
