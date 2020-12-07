package net.plasmere.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.TacoRevamped;
import net.plasmere.Utils;
import net.plasmere.utils.UUIDFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MsgCommand {
    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            String[] args = context.getInput().split(" ");

            ServerPlayerEntity self = context.getSource().getPlayer();
            ServerPlayerEntity other = Objects.requireNonNull(self.getServer()).getPlayerManager().getPlayer(UUIDFetcher.fetch(args[1]));

            if (other == null){
                self.sendMessage(Utils.codedText("&cPlayer &d" + args[1] + " &cisn't online!"), false);
                return 0;
            }

            List<String> a = new ArrayList<>();
            for (String arg : args){
                if (!arg.equals(args[0]) && !arg.equals(args[1])){
                    a.add(arg);
                }
            }
            String msg = Utils.concat(a);

            other.sendMessage(Utils.codedText("&8[ &d" + Utils.getDisplayName(self) + " &9>> &6YOU &8] &e" + msg), false);
            self.sendMessage(Utils.codedText("&8[ &6YOU &9>> &d" + Utils.getDisplayName(other) + " &8] &e" + msg), false);

            Utils.putMsges(self, other);
            Utils.putMsges(other, self);

            for (ServerPlayerEntity p : self.getServer().getPlayerManager().getPlayerList()){
                if (TacoRevamped.getConfiguration().getPermissions().checkPermissions(p.getCommandSource(), "socialspy") || p.hasPermissionLevel(2) && ! self.equals(p) && ! other.equals(p)){
                    p.sendMessage(Utils.codedText("&2SSPY &9>> &d" + Utils.getDisplayName(self) + " &8>> " + Utils.getDisplayName(other) + " &8: &e" + msg), false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode node = registerMain(dispatcher);
        dispatcher.register(CommandManager.literal("tell").redirect(node));
        dispatcher.register(CommandManager.literal("t").redirect(node));
        dispatcher.register(CommandManager.literal("w").redirect(node));
        dispatcher.register(CommandManager.literal("taco:msg").redirect(node));
    }

    public static LiteralCommandNode registerMain(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = LiteralArgumentBuilder.literal("msg");
        literalArgumentBuilder
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("msg", MessageArgumentType.message())
                                .executes(MsgCommand::run)));
        return dispatcher.register(literalArgumentBuilder);
    }
}