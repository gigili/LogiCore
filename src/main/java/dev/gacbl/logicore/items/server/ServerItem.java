package dev.gacbl.logicore.items.server;

import dev.gacbl.logicore.items.processorunit.ProcessorUnitItem;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitModule;
import dev.gacbl.logicore.items.processorunit.ProcessorUnitTier;
import dev.gacbl.logicore.items.server.ui.ServerMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class ServerItem extends Item {
    public ServerItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .component(DataComponents.CONTAINER, ItemContainerContents.fromItems(NonNullList.withSize(9, ItemStack.EMPTY)))
        );
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                    (windowId, playerInventory, playerEntity) -> new ServerMenu(windowId, playerInventory, stack),
                    Component.translatable("item.logicore.server")
            ));
        }
        return InteractionResultHolder.success(stack);
    }

    public static ShapedRecipeBuilder getRecipe() {
        return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ServerModule.SERVER.get())
                .pattern("IRI")
                .pattern("QPQ")
                .pattern("GRG")
                .define('Q', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/quartz")))
                .define('R', ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/redstone")))
                .define('G', Items.GOLD_INGOT)
                .define('I', Items.IRON_INGOT)
                .define('P', ProcessorUnitModule.PROCESSOR_UNIT_BASIC.get());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        HashMap<String, Integer> cpuCount = new HashMap<>();
        for (int i = 0; i < contents.getSlots(); i++) {
            Item it = contents.getStackInSlot(i).getItem();
            if (it instanceof ProcessorUnitItem item) {
                cpuCount.put(item.tier.name, cpuCount.getOrDefault(item.tier.name, 0) + 1);
            }
        }

        if (cpuCount.containsKey(ProcessorUnitTier.BASIC.name)) {
            tooltipComponents.add(Component.translatable("cpu.tooltip.logicore.basic_tier_count", cpuCount.get(ProcessorUnitTier.BASIC.name)));
        }

        if (cpuCount.containsKey(ProcessorUnitTier.ADVANCED.name)) {
            tooltipComponents.add(Component.translatable("cpu.tooltip.logicore.advance_tier_count", cpuCount.get(ProcessorUnitTier.ADVANCED.name)));
        }

        if (cpuCount.containsKey(ProcessorUnitTier.ULTIMATE.name)) {
            tooltipComponents.add(Component.translatable("cpu.tooltip.logicore.ultimate_tier_count", cpuCount.get(ProcessorUnitTier.ULTIMATE.name)));
        }
    }
}
