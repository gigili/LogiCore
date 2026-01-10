package dev.gacbl.logicore.api.cycles;

import dev.gacbl.logicore.core.IntegrationUtils;
import dev.gacbl.logicore.network.PacketHandler;
import dev.gacbl.logicore.network.payload.SyncPlayerCyclesPayload;
import dev.gacbl.logicore.network.payload.SyncPlayerKnowledgePayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CycleSavedData extends SavedData {
    private final Map<String, Long> storage = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> knowledge = new ConcurrentHashMap<>();

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

        if (tag.contains("knowledge")) {
            ListTag knowledgeList = tag.getList("knowledge", Tag.TAG_COMPOUND);
            for (Tag t : knowledgeList) {
                CompoundTag entry = (CompoundTag) t;
                String key = entry.getString("key");
                ListTag items = entry.getList("items", Tag.TAG_STRING);

                Set<String> itemSet = new HashSet<>();
                for (Tag itemTag : items) {
                    itemSet.add(itemTag.getAsString());
                }
                data.knowledge.put(key, itemSet);
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

        ListTag knowledgeList = new ListTag();
        knowledge.forEach((k, v) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("key", k);

            ListTag items = new ListTag();
            v.forEach(itemStr -> items.add(net.minecraft.nbt.StringTag.valueOf(itemStr)));

            entry.put("items", items);
            knowledgeList.add(entry);
        });
        tag.put("knowledge", knowledgeList);
        return tag;
    }

    public synchronized void unlockItem(ServerLevel level, String key, ResourceLocation itemPayload) {
        Set<String> unlocked = knowledge.computeIfAbsent(key, k -> new HashSet<>());
        String itemKey = itemPayload.toString();

        if (unlocked.add(itemKey)) {
            setDirty();
            syncKnowledgeClients(level, key, itemKey);
        }
    }

    private void syncKnowledgeClients(ServerLevel level, String storageKey, String itemKey) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (getKey(level, player.getUUID()).equals(storageKey)) {
                PacketHandler.sendToPlayer(player, new SyncPlayerKnowledgePayload(itemKey));
            }
        }
    }

    public boolean isUnlocked(String key, ResourceLocation item) {
        Set<String> unlocked = knowledge.get(key);
        return unlocked != null && unlocked.contains(item.toString());
    }

    public Set<String> getKnowledge(String playerKey) {
        return knowledge.get(playerKey) == null || knowledge.get(playerKey).isEmpty() ? Collections.emptySet() : knowledge.get(playerKey);
    }

    public void clearAllKnowledgeForOwner(String key) {
        knowledge.remove(key);
        setDirty();
    }

    public void clearAllKnowledge() {
        knowledge.clear();
        setDirty();
    }
}
