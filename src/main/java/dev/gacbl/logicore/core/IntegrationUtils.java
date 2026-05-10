package dev.gacbl.logicore.core;

import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class IntegrationUtils {
    public static void registerEvents() {
        // FTB Teams integration disabled during 26.1 port.
        // To re-enable: uncomment the FTBTeams block below and
        // ensure FTB Teams is available on the classpath.
        // if (ModList.get().isLoaded("ftb_teams") || ModList.get().isLoaded("ftbteams")) {
        //     FTBTeamsHandler.register();
        // }
    }

    public static String getStorageKey(ServerLevel level, UUID playerUUID) {
        // FTB Teams integration disabled during 26.1 port.
        // To re-enable: uncomment the block below.
        // if (ModList.get().isLoaded("ftb_teams") || ModList.get().isLoaded("ftbteams")) {
        //     String ftbKey = FTBTeamsHandler.getTeamId(playerUUID);
        //     if (ftbKey != null) return "ftb:" + ftbKey;
        // }

        // Profile cache integration disabled during 26.1 port
        // To re-enable: use level.getServer().getProfileRepository() or similar
/*
        var profile = Objects.requireNonNull(level.getServer().getProfileCache()).get(playerUUID);
        if (profile.isPresent()) {
            PlayerTeam team = level.getScoreboard().getPlayersTeam(profile.get().getName());
            if (team != null) {
                return "team_vanilla:" + team.getName();
            }
        }
*/

        return playerUUID.toString();
    }

    // ============================================================
    // FTB Teams Handler - DISABLED during 26.1 port
    // To re-enable: uncomment the class below and ensure FTB Teams
    // is available on the classpath.
    // ============================================================
    /*
    public static class FTBTeamsHandler {
        static void register() {
            TeamEvent.PLAYER_JOINED_PARTY.register(event -> {
                ServerPlayer player = event.getPlayer();
                syncPlayer(player);
            });

            TeamEvent.PLAYER_LEFT_PARTY.register(event -> {
                ServerPlayer player = event.getPlayer();
                syncPlayer(player);
            });
        }

        private static void syncPlayer(ServerPlayer player) {
            if (player == null) return;

            ServerLevel level = player.serverLevel();
            CycleSavedData data = CycleSavedData.get(level);

            String newKey = IntegrationUtils.getStorageKey(level, player.getUUID());
            long balance = data.getCyclesByKeyString(newKey);

            PacketHandler.sendToPlayer(player, new SyncPlayerCyclesPayload(balance));
        }

        public static String getTeamId(UUID playerUUID) {
            try {
                Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID);
                if (team.isPresent()) {
                    return team.get().getId().toString();
                }
            } catch (Exception ignored) {
                // Fail silently if API mismatch or error
            }
            return null;
        }
    }
    */
}
