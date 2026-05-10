package dev.gacbl.logicore.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.gacbl.logicore.LogiCore;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Utils {
    public static String formatValues(long value) {
        long v = Math.abs(value);
        if (v >= 1_000_000_000_000L) {
            return String.format("%.2fT", value / 1_000_000_000_000.0);
        } else if (v >= 1_000_000_000L) {
            return String.format("%.2fB", value / 1_000_000_000.0);
        } else if (v >= 1_000_000L) {
            return String.format("%.2fM", value / 1_000_000.0);
        } else if (v >= 1_000L) {
            return String.format("%.2fK", value / 1_000.0);
        }
        return String.valueOf(value);
    }

    public static String getItemKey(ItemStack stack) {
        if (stack.isEmpty()) return "";
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        DataComponentPatch patch = stack.getComponentsPatch();
        if (patch.isEmpty()) return itemId;
        DataResult<JsonElement> result = DataComponentPatch.CODEC.encodeStart(JsonOps.INSTANCE, patch);
        return result.map(json -> {
            String jsonStr = json.toString();
            return itemId + "#" + Base64.getEncoder().encodeToString(jsonStr.getBytes(StandardCharsets.UTF_8));
        }).result().orElse(itemId);
    }

    @Nullable
    public static ItemStack getItemStackFromKey(String key) {
        if (key.isEmpty()) return null;
        int sep = key.indexOf('#');
        String itemId = sep == -1 ? key : key.substring(0, sep);
        var optItem = BuiltInRegistries.ITEM.get(Identifier.tryParse(itemId));
        if (optItem.isEmpty()) return null;
        Item item = optItem.get().value();
        if (item == net.minecraft.world.item.Items.AIR) return null;
        ItemStack stack = new ItemStack(item);
        if (sep == -1) return stack;
        try {
            String encoded = key.substring(sep + 1);
            String jsonStr = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            JsonElement json = JsonParser.parseString(jsonStr);
            DataResult<DataComponentPatch> result = DataComponentPatch.CODEC.parse(JsonOps.INSTANCE, json);
            result.ifSuccess(stack::applyComponents);
        } catch (Exception ignored) {
        }
        return stack.isEmpty() ? null : stack;
    }

    public static ResourceKey<Item> createID(String name) {
        return ResourceKey.create(Registries.ITEM, LogiCore.identifier(name));
    }
}
