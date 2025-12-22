package dev.gacbl.logicore.api.cycles;

import dev.gacbl.logicore.core.IntegrationUtils;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncPlayerCyclesPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CycleSavedData extends SavedData {
    private final Map<String, Long> storage = new ConcurrentHashMap<>();

    public static CycleSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(new Factory<>(
                CycleSavedData::new,
                CycleSavedData::load,
                null
        ), "logicore_cloud_cycles");
    }

    public static String getKey(ServerLevel level, UUID uuid) {
        return IntegrationUtils.getStorageKey(level, uuid);
    }

    public synchronized void modifyCycles(ServerLevel level, String key, long change) {
        long current = storage.getOrDefault(key, 0L);
        long newValue = Math.max(0, current + change);

        if (current != newValue) {
            storage.put(key, newValue);
            setDirty();
            syncClients(level, key, newValue);
        }
    }

    private void syncClients(ServerLevel level, String storageKey, long value) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (getKey(level, player.getUUID()).equals(storageKey)) {
                PacketHandler.sendToPlayer(player, new SyncPlayerCyclesPayload(value));
            }
        }
    }

    public long getCyclesByKeyString(String key) {
        return storage.getOrDefault(key, 0L);
    }

    public static CycleSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        CycleSavedData data = new CycleSavedData();
        if (tag.contains("cycles")) {
            ListTag list = tag.getList("cycles", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag entry = (CompoundTag) t;
                data.storage.put(entry.getString("key"), entry.getLong("val"));
            }
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag list = new ListTag();
        storage.forEach((k, v) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("key", k);
            entry.putLong("val", v);
            list.add(entry);
        });
        tag.put("cycles", list);
        return tag;
    }
}
