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

public class FlyCommand {
    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            PlayerEntity self = context.getSource().getPlayer();

            self.getAbilities().allowFlying = !self.getAbilities().allowFlying;
            self.sendMessage(Utils.codedText("&eFlying is now " + (self.getAbilities().allowFlying ? "&aenabled" : "&cdisabled") + "&e."),false);

            if (!self.getAbilities().allowFlying)
                self.getAbilities().flying = false;

            self.sendAbilitiesUpdate();
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

            other.getAbilities().allowFlying = !other.getAbilities().allowFlying;
            self.sendMessage(Utils.codedText("&ePlayer &d" + args[1] + "&e's flight has been toggled to " + (other.getAbilities().allowFlying ? "&aenabled" : "&cdisabled") + "&e."), false);
            other.sendMessage(Utils.codedText("&eFlying is now " + (other.getAbilities().allowFlying ? "&aenabled" : "&cdisabled") + "&e."),false);

            if (!other.getAbilities().allowFlying)
                other.getAbilities().flying = false;

            other.sendAbilitiesUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("fly")
                .requires((context) -> TacoRevamped.getConfiguration().getPermissions().checkPermissions(context, "fly") || context.hasPermissionLevel(2))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(FlyCommand::runOther)
                        .requires((context) -> TacoRevamped.getConfiguration().getPermissions().checkPermissions(context, "fly.others") || context.hasPermissionLevel(2))
                )
                .executes(FlyCommand::run)
        );
    }
}
