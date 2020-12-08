package net.plasmere.commands.punishments;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.TacoRevamped;
import net.plasmere.Utils;
import net.plasmere.utils.UUIDFetcher;

import java.util.*;

public class BanIPCommand {
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

            BannedIpList list = Objects.requireNonNull(self.getServer()).getPlayerManager().getIpBanList();
            BannedIpEntry entry = new BannedIpEntry(other.getIp(), new Date(), self.getName().asString(), null, reason);
            list.add(entry);
            List<ServerPlayerEntity> playerEntities = self.getServer().getPlayerManager().getPlayersByIp(other.getIp());
            for (ServerPlayerEntity p : playerEntities){
                p.networkHandler.disconnect(Utils.newText("You have been IP banned!"));
            }
            self.sendMessage(Utils.codedText("&eBanned &d" + args[1] + " &ewith IP " + other.getIp() + " &efor reason: " + reason), false);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int runIP(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            String[] args = context.getInput().split(" ");

            ServerPlayerEntity self = context.getSource().getPlayer();

            if (self.getIp().equals(args[1])){
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

            BannedIpList list = Objects.requireNonNull(self.getServer()).getPlayerManager().getIpBanList();
            BannedIpEntry entry = new BannedIpEntry(args[1], new Date(), self.getName().asString(), null, reason);
            list.add(entry);
            List<ServerPlayerEntity> playerEntities = self.getServer().getPlayerManager().getPlayersByIp(args[1]);
            for (ServerPlayerEntity p : playerEntities){
                p.networkHandler.disconnect(Utils.newText("You have been banned!"));
            }
            self.sendMessage(Utils.codedText("&eBanned &d" + args[1] + " &efor reason: " + reason), false);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("banip")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .then(CommandManager.argument("reason", MessageArgumentType.message())
                                .requires((commandSource) ->
                                        TacoRevamped.getConfiguration().getPermissions().checkPermissions(commandSource, "banip")
                                                || commandSource.hasPermissionLevel(2))
                                .executes(BanIPCommand::run)
                        )
                )
                .then(CommandManager.argument("ip", StringArgumentType.string())
                        .then(CommandManager.argument("reason", MessageArgumentType.message())
                                .requires((commandSource) ->
                                        TacoRevamped.getConfiguration().getPermissions().checkPermissions(commandSource, "banip")
                                                || commandSource.hasPermissionLevel(2))
                                .executes(BanIPCommand::runIP)
                        )
                );
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(literalArgumentBuilder);

        dispatcher.register(CommandManager.literal("taco:banip").redirect(node));;
    }
}
