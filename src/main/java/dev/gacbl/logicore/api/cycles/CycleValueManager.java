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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LogiCore.MOD_ID)
public class CycleValueManager {
    private static final Map<Item, Integer> CYCLE_VALUES = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CACHE_FILENAME = "logicore_cycles.json";

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
        reload(server.getRecipeManager(), server.registryAccess(), server, false);
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                reload(server.getRecipeManager(), server.registryAccess(), server, true);
            }
        }
    }

    public static void reload(RecipeManager recipeManager, RegistryAccess registryAccess, MinecraftServer server, Boolean forceReload) {
        CYCLE_VALUES.clear();

        Path configPath = server != null ? server.getWorldPath(LevelResource.PLAYER_DATA_DIR) : null;
        File cacheFile = configPath != null ? configPath.resolve(CACHE_FILENAME).toFile() : null;

        if (cacheFile != null && cacheFile.exists() && !forceReload) {
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
                server.sendSystemMessage(Component.literal(String.format("Loaded %s cycle values from cache.", CYCLE_VALUES.size())));
                return;
            } catch (Exception e) {
                server.sendSystemMessage(Component.literal("Failed to load cycle cache, recalculating..."));
                LogiCore.LOGGER.error("Failed to load cycle cache, recalculating...", e);
            }
        }

        LogiCore.LOGGER.info("Starting cycle value calculation...");
        long start = System.currentTimeMillis();
        calculateValues(recipeManager, registryAccess);
        long time = System.currentTimeMillis() - start;
        LogiCore.LOGGER.info("Calculated cycle values for {} items in {}ms", CYCLE_VALUES.size(), time);

        if (cacheFile != null) {
            try (FileWriter writer = new FileWriter(cacheFile)) {
                JsonObject json = new JsonObject();
                CYCLE_VALUES.entrySet().stream()
                        .sorted(Comparator.comparing(e -> BuiltInRegistries.ITEM.getKey(e.getKey())))
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

    private static void calculateValues(RecipeManager recipeManager, RegistryAccess registryAccess) {
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

        boolean changed = true;
        int maxIterations = 50;
        int currentIteration = 0;

        while (changed && maxIterations > 0) {
            changed = false;
            maxIterations--;
            currentIteration++;

            if (currentIteration % 10 == 0) {
                LogiCore.LOGGER.debug("Cycle Calculation Iteration: {}", currentIteration);
            }

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
                    LogiCore.LOGGER.warn("Failed to calculate cycles for recipe: {}", holder.id(), e);
                }
            }
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
}
