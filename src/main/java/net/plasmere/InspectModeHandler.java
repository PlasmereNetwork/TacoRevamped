package net.plasmere;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.plasmere.save.sql.DbConn;
import net.plasmere.utils.PlayerUtils;
import net.plasmere.utils.PrintToChat;


import java.util.HashSet;

public class InspectModeHandler {
    private static HashSet<ServerPlayerEntity> inspectingPlayers = new HashSet<>();

    public static void init() {
        AttackBlockCallback.EVENT.register(((playerEntity, world, hand, blockPos, direction) -> {
            if (inspectingPlayers.contains(playerEntity)) {
                String dim = PlayerUtils.getPlayerDimension(playerEntity);

                DbConn.readEvents(blockPos, dim, null, (ServerPlayerEntity) playerEntity);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        }));
    }

    public static void toggleMode(ServerPlayerEntity playerEntity) {
        if (inspectingPlayers.contains(playerEntity)) {
            inspectingPlayers.remove(playerEntity);
        } else {
            inspectingPlayers.add(playerEntity);
        }

        PrintToChat.print(playerEntity, "Toggled Inspect Mode", Formatting.GOLD);
    }
}
