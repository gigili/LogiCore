package dev.gacbl.logicore.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.blocks.datacable.cable_network.ComputationNetwork;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.common.Mod;

@Mod(LogiCore.MOD_ID)
public class MyCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, net.minecraft.commands.Commands.CommandSelection selection) {
        LiteralArgumentBuilder<CommandSourceStack> networksCommand = Commands.literal("networks")
                .requires(source -> source.hasPermission(2));

        networksCommand.then(
                Commands.literal("list")
                        .executes(ctx -> listNetworks(ctx.getSource()))
        );

        networksCommand.then(
                Commands.literal("clear")
                        .executes(ctx -> clearNetworks(ctx.getSource()))
        );

        dispatcher.register(networksCommand);
    }

    private static int listNetworks(CommandSourceStack source) {
        ServerLevel serverLevel = source.getLevel();

        NetworkManager manager = NetworkManager.get(serverLevel);

        int networkCount = manager.getNetworks().size();
        source.sendSuccess(() -> Component.literal("Found " + networkCount + " computation networks."), false);
        for (ComputationNetwork network : manager.getNetworks().values()) {
            source.sendSuccess(() -> Component.literal("Network: " + network.getNetworkID()), false);
        }

        return 1;
    }

    private static int clearNetworks(CommandSourceStack source) {
        ServerLevel serverLevel = source.getLevel();

        NetworkManager manager = NetworkManager.get(serverLevel);
        manager.clearAll();
        source.sendSuccess(() -> Component.literal("All computation networks cleared."), true);

        return 1;
    }
}
