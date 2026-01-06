package dev.gacbl.logicore.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientKnowledgeData {
    private static final Set<String> UNLOCKED_ITEMS = new HashSet<>();

    public static void add(String itemKey) {
        UNLOCKED_ITEMS.add(itemKey);
    }

    public static boolean isUnlocked(String itemKey) {
        return UNLOCKED_ITEMS.contains(itemKey);
    }

    public static void clear() {
        UNLOCKED_ITEMS.clear();
    }

    public static void addAll(List<String> itemKeys) {
        UNLOCKED_ITEMS.addAll(itemKeys);
    }
}
