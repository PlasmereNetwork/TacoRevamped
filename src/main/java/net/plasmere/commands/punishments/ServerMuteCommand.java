package net.plasmere.commands.punishments;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.plasmere.Utils;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.literal;

public class ServerMuteCommand {
    private static Boolean isServerMuted = false;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("servermute")
                .requires(scs -> Utils.hasPermissionFromSource(scs, "servermute"))
                .executes(ServerMuteCommand::setServerMute));

    }

    private static int setServerMute(CommandContext<ServerCommandSource> scs) throws CommandSyntaxException {
        ServerMuteCommand.isServerMuted = !ServerMuteCommand.isServerMuted;
        ServerPlayerEntity self = scs.getSource().getPlayer();
        Text messageText = Utils.codedCHText("&eServer mute is now set to: " + isServerMuted);
        try {
            self.sendSystemMessage(messageText, Util.NIL_UUID);
            for (ServerPlayerEntity p : Objects.requireNonNull(scs.getSource().getPlayer().getServer()).getPlayerManager().getPlayerList()){
                if (Utils.hasPermission(p, "mutes.see")){
                    p.sendMessage(Utils.codedText("&d" + Utils.getDisplayName(self) + " &8>> &3Set mute for serverwide chat to " +
                            (isServerMuted ? "&cmuted" : "&aunmuted") + "&3."), false);
                }
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static Boolean isMuted() {
        return ServerMuteCommand.isServerMuted;
    }
}
