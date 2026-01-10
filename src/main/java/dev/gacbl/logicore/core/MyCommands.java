package dev.gacbl.logicore.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.gacbl.logicore.LogiCore;
import dev.gacbl.logicore.api.cycles.CycleSavedData;
import dev.gacbl.logicore.blocks.datacable.cable_network.ComputationNetwork;
import dev.gacbl.logicore.blocks.datacable.cable_network.NetworkManager;
import dev.gacbl.logicore.client.ClientKnowledgeData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod(LogiCore.MOD_ID)
public class MyCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, net.minecraft.commands.Commands.CommandSelection selection) {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("logicore")
                .requires(source -> source.hasPermission(2));

        LiteralArgumentBuilder<CommandSourceStack> networksCommand = Commands.literal("networks");

        networksCommand.then(
                Commands.literal("list")
                        .executes(ctx -> listNetworks(ctx.getSource()))
        );

        networksCommand.then(
                Commands.literal("clear")
                        .executes(ctx -> clearNetworks(ctx.getSource()))
        );

        LiteralArgumentBuilder<CommandSourceStack> knowledgeCommand = Commands.literal("knowledge");

        knowledgeCommand.then(
                Commands.literal("list")
                        .executes(ctx -> listKnowledge(ctx.getSource()))
        );

        knowledgeCommand.then(
                Commands.literal("clear")
                        .executes(ctx -> clearKnowledge(ctx.getSource()))
        );

        rootCommand.then(networksCommand);
        rootCommand.then(knowledgeCommand);

        dispatcher.register(rootCommand);
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

    private static int listKnowledge(CommandSourceStack source) {
        ServerLevel serverLevel = source.getLevel();
        if (source.getPlayer() == null) return 0;

        String ownerKey = CycleSavedData.getKey(serverLevel, source.getPlayer().getUUID());
        CycleSavedData data = CycleSavedData.get(serverLevel);
        Set<String> knowledge = data.getKnowledge(ownerKey);
        Set<String> items = new HashSet<>();
        for (String kn : knowledge) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(kn)).asItem();
            ItemStack stack = new ItemStack(item);
            items.add(item.getName(stack).getString());
        }
        source.sendSuccess(() -> Component.literal("Knowledge list: " + items), false);
        return 1;
    }

    private static int clearKnowledge(CommandSourceStack source) {
        ServerLevel serverLevel = source.getLevel();
        if (source.getPlayer() == null) return 0;

        String ownerKey = CycleSavedData.getKey(serverLevel, source.getPlayer().getUUID());
        CycleSavedData data = CycleSavedData.get(serverLevel);
        data.clearAllKnowledgeForOwner(ownerKey);
        ClientKnowledgeData.clear();

        source.sendSuccess(() -> Component.literal("Knowledge database has been cleared"), false);
        return 1;
    }
}
