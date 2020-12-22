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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TempBanIPCommand {
    public static final Pattern PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private static int checkIp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Matcher matcher = PATTERN.matcher(StringArgumentType.getString(context, "target"));
        if (matcher.matches()) {
            return runIP(context);
        } else {
            return run(context);
        }
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        try {
            String[] args = context.getInput().split(" ");

            ServerPlayerEntity self = context.getSource().getPlayer();
            ServerPlayerEntity other = UUIDFetcher.getServerPlayerEntity(StringArgumentType.getString(context, "target"));

            if (other == null){
                self.sendMessage(Utils.codedText("&cCould not use &d" + args[1] + "&c!"), false);
                return 0;
            }

            if (self.equals(other)){
                self.sendMessage(Utils.codedText("&cCannot use this command on yourself!"), false);
                return 0;
            }

            int seconds = 0;

            if (args[1].endsWith("s")) {
                seconds = Integer.parseInt(args[1].substring(0, args[1].indexOf('s')));
            } else if (args[1].endsWith("m")) {
                seconds = Integer.parseInt(args[1].substring(0, args[1].indexOf('m'))) * 60;
            } else if (args[1].endsWith("h")) {
                seconds = Integer.parseInt(args[1].substring(0, args[1].indexOf('h'))) * 60 * 60;
            } else if (args[1].endsWith("d")) {
                seconds = Integer.parseInt(args[1].substring(0, args[1].indexOf('d'))) * 60 * 60 * 24;
            } else if (args[1].endsWith("w")) {
                seconds = Integer.parseInt(args[1].substring(0, args[1].indexOf('d'))) * 60 * 60 * 24 * 7;
            } else if (args[1].endsWith("y")) {
                seconds = Integer.parseInt(args[1].substring(0, args[1].indexOf('d'))) * 60 * 60 * 24 * 365;
            }

            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.SECOND, seconds);

            List<String> reasons = new ArrayList<>();
            for (int i = 0; i < args.length; i++){
                if (! (i <= 1)){
                    reasons.add(args[i]);
                }
            }
            String reason = Utils.normalize(reasons);

            BannedIpList list = Objects.requireNonNull(self.getServer()).getPlayerManager().getIpBanList();
            BannedIpEntry entry = new BannedIpEntry(other.getIp(), new Date(), self.getName().asString(), c.getTime(), reason);
            list.add(entry);
            List<ServerPlayerEntity> playerEntities = self.getServer().getPlayerManager().getPlayersByIp(other.getIp());
            for (ServerPlayerEntity p : playerEntities){
                p.networkHandler.disconnect(Utils.newText("You have been temporarily IP banned!"));
            }
            self.sendMessage(Utils.codedText("&eBanned &d" + args[1] + " &ewith IP " + other.getIp() + " &euntil &c" + c.getTime() + " &eand for reason: " + reason), false);
            for (ServerPlayerEntity p : self.getServer().getPlayerManager().getPlayerList()){
                if (Utils.hasPermission(p, "bans.see")){
                    p.sendMessage(Utils.codedText("&d" + Utils.getDisplayName(self) + " &8>> &cBanned &d" + args[1] + " &cfor &6" + args[2] + " &cfor reason: &3" + reason), false);
                }
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int runIP(CommandContext<ServerCommandSource> context) {
        try {
            String[] args = context.getInput().split(" ");

            ServerPlayerEntity self = context.getSource().getPlayer();

            if (self.getIp().equals(args[1])){
                self.sendMessage(Utils.codedText("&cCannot use this command on yourself!"), false);
                return 0;
            }

            int seconds = 0;

            if (args[2].endsWith("s")) {
                seconds = Integer.parseInt(args[2].substring(0, args[2].indexOf('s')));
            } else if (args[2].endsWith("m")) {
                seconds = Integer.parseInt(args[2].substring(0, args[2].indexOf('m'))) * 60;
            } else if (args[2].endsWith("h")) {
                seconds = Integer.parseInt(args[2].substring(0, args[2].indexOf('h'))) * 60 * 60;
            } else if (args[2].endsWith("d")) {
                seconds = Integer.parseInt(args[2].substring(0, args[2].indexOf('d'))) * 60 * 60 * 24;
            } else if (args[2].endsWith("w")) {
                seconds = Integer.parseInt(args[2].substring(0, args[2].indexOf('d'))) * 60 * 60 * 24 * 7;
            } else if (args[2].endsWith("y")) {
                seconds = Integer.parseInt(args[2].substring(0, args[2].indexOf('d'))) * 60 * 60 * 24 * 365;
            }

            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.SECOND, seconds);

            List<String> reasons = new ArrayList<>();
            for (int i = 0; i < args.length; i++){
                if (! (i <= 1)){
                    reasons.add(args[i]);
                }
            }
            String reason = Utils.normalize(reasons);

            BannedIpList list = Objects.requireNonNull(self.getServer()).getPlayerManager().getIpBanList();
            BannedIpEntry entry = new BannedIpEntry(args[1], new Date(), self.getName().asString(), c.getTime(), reason);
            list.add(entry);
            List<ServerPlayerEntity> playerEntities = self.getServer().getPlayerManager().getPlayersByIp(args[1]);
            for (ServerPlayerEntity p : playerEntities){
                p.networkHandler.disconnect(Utils.newText("You have been banned!"));
            }
            self.sendMessage(Utils.codedText("&eBanned &d" + args[1] + " &euntil &c" + c.getTime() + " &eand for reason: " + reason), false);
            for (ServerPlayerEntity p : self.getServer().getPlayerManager().getPlayerList()){
                if (Utils.hasPermission(p, "bans.see")){
                    p.sendMessage(Utils.codedText("&d" + Utils.getDisplayName(self) + " &8>> &cBanned &d" + args[1] + " &cfor &6" + args[2] + " &cfor reason: &3" + reason), false);
                }
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("tempbanip")
                .then(CommandManager.argument("target", StringArgumentType.word())
                        .then(CommandManager.argument("duration", StringArgumentType.string())
                                .then(CommandManager.argument("reason", MessageArgumentType.message())
                                        .requires((commandSource) ->
                                                TacoRevamped.getConfiguration().getPermissions().checkPermissions(commandSource, "tempbanip")
                                                        || commandSource.hasPermissionLevel(2))
                                        .executes(TempBanIPCommand::checkIp)
                                )
                        )
                );
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(literalArgumentBuilder);

        dispatcher.register(CommandManager.literal("taco:tempbanip").redirect(node));
    }
}
