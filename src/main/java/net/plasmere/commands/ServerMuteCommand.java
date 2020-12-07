package net.plasmere.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.plasmere.Utils;

import static net.minecraft.server.command.CommandManager.literal;

public class ServerMuteCommand {
    private static Boolean isServerMuted = false;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("servermute")
                .requires(scs -> Utils.hasPermissionFromSource(scs, "servermute"))
                .executes(ServerMuteCommand::setServerMute));

    }

    private static int setServerMute(CommandContext<ServerCommandSource> scs) {
        ServerMuteCommand.isServerMuted = !ServerMuteCommand.isServerMuted;
        Text messageText = Utils.codedCHText("&eServer mute is now set to: " + isServerMuted);
        try {
            scs.getSource().getPlayer().sendSystemMessage(messageText, Util.NIL_UUID);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 1;
    }
    public static Boolean isMuted() {
        return ServerMuteCommand.isServerMuted;
    }
}
