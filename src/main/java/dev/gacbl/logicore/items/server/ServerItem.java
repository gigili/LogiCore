package dev.gacbl.logicore.items.server;

import dev.gacbl.logicore.items.server.ui.ServerMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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
}
