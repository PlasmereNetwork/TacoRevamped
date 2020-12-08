package net.plasmere.commands.punishments;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.TacoRevamped;
import net.plasmere.Utils;
import net.plasmere.utils.UUIDFetcher;

import java.util.*;

public class BanCommand {
    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            String[] args = context.getInput().split(" ");

            ServerPlayerEntity self = context.getSource().getPlayer();

            ServerPlayerEntity other = UUIDFetcher.getServerPlayerEntity(args[1]);

            if (other == null){
                self.sendMessage(Utils.codedText("&cCould not use &d" + args[1] + "&c!"), false);
                return 0;
            }

            if (self.equals(other)){
                self.sendMessage(Utils.codedText("&cCannot use this command on yourself!"), false);
                return 0;
            }

            List<String> reasons = new ArrayList<>();
            for (int i = 0; i < args.length; i++){
                if (! (i <= 1)){
                    reasons.add(args[i]);
                }
            }
            String reason = Utils.normalize(reasons);

            BannedPlayerList list = Objects.requireNonNull(self.getServer()).getPlayerManager().getUserBanList();
            BannedPlayerEntry entry = new BannedPlayerEntry(other.getGameProfile(), new Date(), self.getName().asString(), null, reason);
            list.add(entry);
            if (self.server.getPlayerManager().getPlayerList().contains(other)) {
                other.networkHandler.disconnect(Utils.newText("You have been banned!"));
            }
            self.sendMessage(Utils.codedText("&eBanned &d" + args[1] + " &efor reason: " + reason), false);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("ban")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .then(CommandManager.argument("reason", MessageArgumentType.message())
                                .requires((commandSource) ->
                                        TacoRevamped.getConfiguration().getPermissions().checkPermissions(commandSource, "ban")
                                                || commandSource.hasPermissionLevel(2))
                        .executes(BanCommand::run))
                );
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(literalArgumentBuilder);

        dispatcher.register(CommandManager.literal("taco:ban").redirect(node));
    }
}
