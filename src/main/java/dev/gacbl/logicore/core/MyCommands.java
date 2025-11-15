package dev.gacbl.logicore.core;

import com.mojang.brigadier.CommandDispatcher;
import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.datacable.network.NetworkManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;

@Mod(LogiCore.MOD_ID)
public class MyCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, net.minecraft.commands.Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("clear_network")
                .executes(ctx -> new MyCommands().clearNetwork(ctx.getSource().getPlayerOrException()))
        );
    }

    private int clearNetwork(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        NetworkManager manager = NetworkManager.get(level);

        manager.clearAll();

        player.sendSystemMessage(Component.literal("ALL NETWORKS CLEARED!"));

        return 1;
    }
}
