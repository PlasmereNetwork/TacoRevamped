package net.plasmere.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.plasmere.TacoRevamped;
import net.plasmere.Utils;
import net.plasmere.utils.UUIDFetcher;

import java.util.Objects;

public class GlowCommand {
    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            PlayerEntity self = context.getSource().getPlayer();

            self.setGlowing(!self.isGlowing());
            self.sendMessage(Utils.codedText("&eGlowing is now " + (self.isGlowing() ? "&aenabled" : "&cdisabled") + "&e."),false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static int runOther(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            String[] args = context.getInput().split(" ");

            PlayerEntity self = context.getSource().getPlayer();
            PlayerEntity other = Objects.requireNonNull(self.getServer()).getPlayerManager().getPlayer(UUIDFetcher.fetch(args[1]));

            if (other == null){
                self.sendMessage(Utils.codedText("&cPlayer &d" + args[1] + " &cisn't online!"), false);
                return 0;
            }

            other.setGlowing(!other.isGlowing());
            self.sendMessage(Utils.codedText("&ePlayer &d" + args[1] + "&e's flight has been toggled to " + (other.isGlowing() ? "&aenabled" : "&cdisabled") + "&e."), false);
            other.sendMessage(Utils.codedText("&eGlowing is now " + (other.isGlowing() ? "&aenabled" : "&cdisabled") + "&e."),false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("glow")
                .executes(GlowCommand::run)
                .requires((context) -> TacoRevamped.getConfiguration().getPermissions().checkPermissions(context, "glow") || context.hasPermissionLevel(2))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(GlowCommand::runOther)
                        .requires((context) -> TacoRevamped.getConfiguration().getPermissions().checkPermissions(context, "glow.others") || context.hasPermissionLevel(2))
                )
        );
    }
}
