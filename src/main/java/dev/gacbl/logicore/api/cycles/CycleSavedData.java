package dev.gacbl.logicore.api.cycles;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CycleSavedData extends SavedData {
    // Key = UUID string (for solo) or Team Name (for parties)
    private final Map<String, Long> storage = new HashMap<>();

    public static CycleSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(
                CycleSavedData::new,
                CycleSavedData::load,
                null
        ), "logicore_cloud_cycles");
    }

    public long getCycles(Player player) {
        return storage.getOrDefault(getStorageKey(player), 0L);
    }

    public void addCycles(Player player, long amount) {
        String key = getStorageKey(player);
        storage.put(key, storage.getOrDefault(key, 0L) + amount);
        setDirty();
    }

    public long extractCycles(Player player, long maxExtract, boolean simulate) {
        String key = getStorageKey(player);
        long current = storage.getOrDefault(key, 0L);
        long extracted = Math.min(current, maxExtract);

        if (!simulate) {
            storage.put(key, current - extracted);
            setDirty();
        }
        return extracted;
    }

    // Helper to interact via UUID (e.g., from BlockEntity when player is offline)
    // Note: detailed team logic for offline players requires more complex Scoreboard lookup
    // This simple version assumes the BE stored the 'Key' or just uses UUID.
    public void modifyCyclesByKey(String key, long change) {
        long current = storage.getOrDefault(key, 0L);
        long newValue = current + change;
        if (newValue < 0) newValue = 0;
        storage.put(key, newValue);
        setDirty();
    }

    public long getCyclesByKey(String key) {
        return storage.getOrDefault(key, 0L);
    }

    // Determines if we use the Player's UUID or their Team Name
    public static String getStorageKey(Player player) {
        if (player != null && player.getTeam() != null) {
            return player.getTeam().getName();
        }
        return "";
    }

    public static CycleSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        CycleSavedData data = new CycleSavedData();
        ListTag list = tag.getList("cycles", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag entry = (CompoundTag) t;
            data.storage.put(entry.getString("key"), entry.getLong("val"));
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
