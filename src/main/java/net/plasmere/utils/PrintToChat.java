package net.plasmere.utils;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.entity.player.PlayerEntity;
import net.plasmere.save.sql.DbConn;

import java.util.HashMap;

public class PrintToChat {
    public static void print(PlayerEntity player, String message, Formatting formatting) {
        LiteralText textMessage = new LiteralText(message);
        textMessage.formatted(formatting);
        player.sendMessage(textMessage, false);
    }

    public static void prepareInteractionsPrint(HashMap<String, String> params, ServerPlayerEntity sender) {
        //Prepare the block's state to make the messages shorter and more readable
        String trimmed = params.get("state");
        int index = trimmed.indexOf("[");
        if(index > 0){
            trimmed =  trimmed.substring(0, index);
        }
        String message = trimmed+" was "+params.get("type") +" by "+ DbConn.getDisplayName(DbConn.getUuid(params.get("player")))+" at "+params.get("time")/*+" "+valuesArray[8]*/;
        //Print message
        PrintToChat.print(sender, message, Formatting.DARK_AQUA);
    }
}
