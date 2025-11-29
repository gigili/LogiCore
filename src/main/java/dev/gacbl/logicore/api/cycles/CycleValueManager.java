package dev.gacbl.logicore.api.cycles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.core.ModDataMaps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LogiCore.MOD_ID)
public class CycleValueManager {
    private static final Map<Item, Integer> CYCLE_VALUES = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CACHE_FILENAME = "cycle_cache.json";
    private static final String CUSTOM_FILENAME = "custom_cycles.json";

    public static int getCycleValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return CYCLE_VALUES.getOrDefault(stack.getItem(), 0) * stack.getCount();
    }

    public static boolean hasCycleValue(ItemStack stack) {
        return CYCLE_VALUES.containsKey(stack.getItem());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        reload(server.getRecipeManager(), server.registryAccess(), false);
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                reload(server.getRecipeManager(), server.registryAccess(), true);
            }
        }
    }

    public static void reload(RecipeManager recipeManager, RegistryAccess registryAccess, boolean forceReload) {
        CYCLE_VALUES.clear();

        Path configDir = FMLPaths.CONFIGDIR.get().resolve(LogiCore.MOD_ID);
        Path cacheDir = configDir.resolve("cache");

        try {
            if (!Files.exists(configDir)) Files.createDirectories(configDir);
            if (!Files.exists(cacheDir)) Files.createDirectories(cacheDir);
        } catch (IOException e) {
            LogiCore.LOGGER.error("Failed to create config directories", e);
        }

        File cacheFile = cacheDir.resolve(CACHE_FILENAME).toFile();
        File customFile = configDir.resolve(CUSTOM_FILENAME).toFile();

        ensureCustomFileExists(customFile);

        boolean cacheValid = cacheFile.exists();
        if (cacheValid && customFile.exists() && customFile.lastModified() > cacheFile.lastModified()) {
            LogiCore.LOGGER.info("Custom cycles file modified. Invalidating cache and recalculating...");
            cacheValid = false;
        }

        if (cacheValid && !forceReload) {
            if (loadFromCache(cacheFile)) {
                return;
            }
        }

        LogiCore.LOGGER.info("Starting cycle value calculation...");
        long start = System.currentTimeMillis();

        calculateValues(recipeManager, registryAccess, customFile);

        long time = System.currentTimeMillis() - start;
        LogiCore.LOGGER.info("Calculated cycle values for {} items in {}ms", CYCLE_VALUES.size(), time);

        saveToCache(cacheFile);
    }

    private static void calculateValues(RecipeManager recipeManager, RegistryAccess registryAccess, File customFile) {
        try {
            registryAccess.registryOrThrow(Registries.ITEM).holders().forEach(holder -> {
                Integer value = holder.getData(ModDataMaps.ITEM_CYCLES);
                if (value != null) {
                    CYCLE_VALUES.put(holder.value(), value);
                }
            });
        } catch (Exception e) {
            LogiCore.LOGGER.error("Failed to load base cycle values from DataMap", e);
        }

        if (customFile.exists()) {
            loadCustomValues(customFile, registryAccess);
        }

        boolean changed = true;
        int maxIterations = 50;

        while (changed && maxIterations > 0) {
            changed = false;
            maxIterations--;

            for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
                try {
                    Recipe<?> recipe = holder.value();
                    ItemStack result = recipe.getResultItem(registryAccess);

                    if (result.isEmpty() || CYCLE_VALUES.containsKey(result.getItem())) continue;

                    int calculatedCost = calculateRecipeCost(recipe);

                    if (calculatedCost > 0) {
                        int costPerItem = calculatedCost / Math.max(1, result.getCount());
                        if (costPerItem > 0) {
                            CYCLE_VALUES.put(result.getItem(), costPerItem);
                            changed = true;
                        }
                    }
                } catch (Exception e) {
                    LogiCore.LOGGER.warn("Failed to load cycle values from DataMap", e);
                }
            }
        }
    }

    private static void loadCustomValues(File file, RegistryAccess registryAccess) {
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json.has("values")) {
                JsonObject values = json.getAsJsonObject("values");
                for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
                    String key = entry.getKey();
                    int value = entry.getValue().getAsInt();

                    if (key.startsWith("#")) {
                        try {
                            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, ResourceLocation.parse(key.substring(1)));
                            registryAccess.registryOrThrow(Registries.ITEM).getTag(tagKey).ifPresent(tag -> {
                                tag.stream().forEach(holder -> CYCLE_VALUES.put(holder.value(), value));
                            });
                        } catch (Exception e) {
                            LogiCore.LOGGER.warn("Invalid tag in custom_cycles.json: {}", key);
                        }
                    } else {
                        ResourceLocation id = ResourceLocation.tryParse(key);
                        if (id != null) {
                            Item item = BuiltInRegistries.ITEM.get(id);
                            if (item != net.minecraft.world.item.Items.AIR || id.getPath().equals("air")) {
                                CYCLE_VALUES.put(item, value);
                            }
                        }
                    }
                }
                LogiCore.LOGGER.info("Applied custom cycle overrides.");
            }
        } catch (Exception e) {
            LogiCore.LOGGER.error("Failed to load custom_cycles.json", e);
        }
    }

    private static int calculateRecipeCost(Recipe<?> recipe) {
        int totalCost = 0;
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;
            int minIngredientCost = Integer.MAX_VALUE;
            boolean foundValue = false;
            for (ItemStack stack : ingredient.getItems()) {
                if (CYCLE_VALUES.containsKey(stack.getItem())) {
                    int val = CYCLE_VALUES.get(stack.getItem());
                    if (val < minIngredientCost) {
                        minIngredientCost = val;
                        foundValue = true;
                    }
                }
            }
            if (!foundValue) return 0;
            totalCost += minIngredientCost;
        }
        return totalCost;
    }

    private static void ensureCustomFileExists(File file) {
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                JsonObject json = new JsonObject();
                json.addProperty("_comment", "Define custom cycle values here. Keys can be Item IDs (minecraft:cobblestone) or Tags (#minecraft:logs).");
                JsonObject values = new JsonObject();
                values.addProperty("minecraft:dirt", 1);
                json.add("values", values);
                GSON.toJson(json, writer);
            } catch (IOException e) {
                LogiCore.LOGGER.error("Failed to create default custom_cycles.json", e);
            }
        }
    }

    private static boolean loadFromCache(File cacheFile) {
        try (FileReader reader = new FileReader(cacheFile)) {
            LogiCore.LOGGER.info("Loading cycle values from cache: {}", cacheFile.getAbsolutePath());
            JsonObject json = GSON.fromJson(reader, JsonObject.class);

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
                if (id != null) {
                    Item item = BuiltInRegistries.ITEM.get(id);
                    if (item != net.minecraft.world.item.Items.AIR || id.getPath().equals("air")) {
                        CYCLE_VALUES.put(item, entry.getValue().getAsInt());
                    }
                }
            }
            LogiCore.LOGGER.info("Loaded {} cycle values from cache.", CYCLE_VALUES.size());
            return true;
        } catch (Exception e) {
            LogiCore.LOGGER.error("Failed to load cycle cache", e);
            return false;
        }
    }

    private static void saveToCache(File cacheFile) {
        try (FileWriter writer = new FileWriter(cacheFile)) {
            JsonObject json = new JsonObject();
            CYCLE_VALUES.entrySet().stream()
                    .sorted((e1, e2) -> BuiltInRegistries.ITEM.getKey(e1.getKey()).compareTo(BuiltInRegistries.ITEM.getKey(e2.getKey())))
                    .forEach(entry -> {
                        ResourceLocation key = BuiltInRegistries.ITEM.getKey(entry.getKey());
                        json.addProperty(key.toString(), entry.getValue());
                    });

            GSON.toJson(json, writer);
            LogiCore.LOGGER.info("Saved cycle values to cache: {}", cacheFile.getAbsolutePath());
        } catch (IOException e) {
            LogiCore.LOGGER.error("Failed to save cycle cache", e);
        }
    }
}
