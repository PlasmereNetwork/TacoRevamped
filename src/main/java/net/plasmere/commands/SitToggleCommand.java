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

public class SitToggleCommand {
    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            ServerPlayerEntity self = context.getSource().getPlayer();

            Utils.toggleSitToggled(self);

            self.sendMessage(Utils.codedText("&eToggled SitToggleCommand to " + (Utils.isSitToggled(self) ? "&aenabled" : "&cdisabled") + "&e!"), false);
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

            Utils.toggleSitToggled(other);

            self.sendMessage(Utils.codedText("&eToggled &d" + Utils.getDisplayName(other)
                    + "&e's SitToggleCommand to " + (Utils.isSitToggled(other) ? "&aenabled" : "&cdisabled") + "&e!"
            ), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = LiteralArgumentBuilder.literal("sittoggle");
        literalArgumentBuilder.then(CommandManager.argument("player", EntityArgumentType.player())
                .requires((context) -> TacoRevamped.getConfiguration().getPermissions().checkPermissions(context, "sittoggle.others") || context.hasPermissionLevel(2))
                .executes(SitToggleCommand::runOther)
        );
        literalArgumentBuilder
                .requires((context) -> TacoRevamped.getConfiguration().getPermissions().checkPermissions(context, "sittoggle") || context.hasPermissionLevel(2))
                .executes(SitToggleCommand::run);
        dispatcher.register(literalArgumentBuilder);
    }
}
