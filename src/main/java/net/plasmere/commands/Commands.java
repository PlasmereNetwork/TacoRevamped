package net.plasmere.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class Commands {
        public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
                LiteralCommandNode<ServerCommandSource> blockLoggerNode = CommandManager
                        .literal("bl")
                        .requires(source -> source.hasPermissionLevel(3))
                        .build();

                dispatcher.getRoot().addChild(blockLoggerNode);

                InspectCommand.register(blockLoggerNode);
                SearchCommand.register(blockLoggerNode);
                SQLCommand.register(blockLoggerNode);
        }
}
