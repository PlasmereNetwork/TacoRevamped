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
            for (int i = 0; i < args.length; i++){
                if (! (i <= 1)){
                    a.add(args[i]);
                }
            }
            String msg = Utils.normalize(a);

            other.sendMessage(Utils.codedText("&8[ &d" + Utils.getDisplayName(self) + " &9>> &6YOU &8] &e" + msg), false);
            self.sendMessage(Utils.codedText("&8[ &6YOU &9>> &d" + Utils.getDisplayName(other) + " &8] &e" + msg), false);

            Utils.putMsges(self, other);
            Utils.putMsges(other, self);

            for (ServerPlayerEntity p : self.getServer().getPlayerManager().getPlayerList()){
                if (TacoRevamped.getConfiguration().getPermissions().checkPermissions(p.getCommandSource(), "socialspy") || p.hasPermissionLevel(2)){
                    p.sendMessage(Utils.codedText("&2SSPY &9>> &d" + Utils.getDisplayName(self) + " &8>> &d" + Utils.getDisplayName(other) + " &8: &e" + msg), false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> node = regOther(dispatcher);
        dispatcher.register(CommandManager.literal("tell").redirect(node));
        dispatcher.register(CommandManager.literal("t").redirect(node));
        dispatcher.register(CommandManager.literal("w").redirect(node));
        dispatcher.register(CommandManager.literal("taco:msg").redirect(node));
    }

    public static LiteralCommandNode<ServerCommandSource> registerMain(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = LiteralArgumentBuilder.literal("msg");
        literalArgumentBuilder
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("msg", MessageArgumentType.message())
                                .executes(MsgCommand::run)
                        )
                );
        return dispatcher.register(literalArgumentBuilder);
    }

    public static LiteralCommandNode<ServerCommandSource> regOther(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("msg")
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("message", MessageArgumentType.message())
                                .executes(MsgCommand::run)
                        )
                );
        return dispatcher.register(literalArgumentBuilder);
    }
}
