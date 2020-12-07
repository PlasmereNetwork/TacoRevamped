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

public class DiscordCommand {
    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            PlayerEntity self = context.getSource().getPlayer();

            self.sendMessage(Utils.codedText("&9DISCORD &8>> &bIf you wish to join the &9Discord &bfor the server, then click this link&8: &6https://discord.gg/Egt3bqb"), false);
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

            if (other == null){
                self.sendMessage(Utils.codedText("&cPlayer &d" + args[1] + " &cisn't online!"), false);
                return 0;
            }

            other.sendMessage(Utils.codedText("&3VOTE &8>> &bIf you wish to vote for the server, then click this link&8: &6http://vote.plasmere.net"), false);
            self.sendMessage(Utils.codedText("&eExecuted as " + Utils.getDisplayName(other) + " &esuccessfully!"), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = LiteralArgumentBuilder.literal("discord");
        literalArgumentBuilder.then(CommandManager.argument("player", EntityArgumentType.player())
                .requires((context) -> TacoRevamped.getConfiguration().getPermissions().checkPermissions(context, "discord.others") || context.hasPermissionLevel(2))
                .executes(DiscordCommand::runOther)
        );
        literalArgumentBuilder
//                .requires((context) -> {
//                    try {
//                        PlayerEntity playerEntity = context.getPlayer();
//                        return SettingsManager.canUseCommand(playerEntity, SettingsManager.Command.DISCORD);
//                    } catch (CommandSyntaxException e) {
//                        e.printStackTrace();
//                    }
//                    return false;
//                })
                .executes(DiscordCommand::run);
        dispatcher.register(literalArgumentBuilder);
    }
}
