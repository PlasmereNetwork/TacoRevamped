package net.plasmere.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.plasmere.TacoRevamped;
import net.plasmere.Utils;
import net.plasmere.utils.UUIDFetcher;

import java.util.Objects;

public class WhoMeCommand {
    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            ServerPlayerEntity self = context.getSource().getPlayer();

            self.sendMessage(Utils.codedText("&e-- You are --" +
                    "\n&cDisplayName: " + self.getDisplayName() +
                    "\n&cName" + self.getName() +
                    "\n&cGetDisplayName: " + Utils.getDisplayName(self) +
                    "\n&cDisplayNameAsString: " + self.getDisplayName().asString() +
                    "\n&cNameAsString: " + self.getName().asString() +
                    "\n&cEntityName: " + self.getEntityName()
            ), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int runOther(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            String[] args = context.getInput().split(" ");

            ServerPlayerEntity self = context.getSource().getPlayer();
            ServerPlayerEntity other = Objects.requireNonNull(self.getServer()).getPlayerManager().getPlayer(UUIDFetcher.fetch(args[1]));

            if (other == null) {
                self.sendMessage(Utils.codedText("&cPlayer &d" + args[1] + " &cisn't online!"), false);
                return 0;
            }

            self.sendMessage(Utils.codedText("&e-- You are --" +
                    "\n&cDisplayName: " + other.getDisplayName() +
                    "\n&cName" + other.getName() +
                    "\n&cGetDisplayName: " + Utils.getDisplayName(other) +
                    "\n&cDisplayNameAsString: " + other.getDisplayName().asString() +
                    "\n&cNameAsString: " + other.getName().asString() +
                    "\n&cEntityName: " + other.getEntityName()
            ), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = LiteralArgumentBuilder.literal("whome");
        literalArgumentBuilder.then(CommandManager.argument("player", EntityArgumentType.player())
                .requires((context) -> TacoRevamped.getConfiguration().getPermissions().checkPermissions(context, "whome.other") || context.hasPermissionLevel(2))
                .executes(WhoMeCommand::runOther)
        );
        literalArgumentBuilder
                .requires((context) -> TacoRevamped.getConfiguration().getPermissions().checkPermissions(context, "whome") || context.hasPermissionLevel(2))
                .executes(WhoMeCommand::run);
        dispatcher.register(literalArgumentBuilder);
    }
}
