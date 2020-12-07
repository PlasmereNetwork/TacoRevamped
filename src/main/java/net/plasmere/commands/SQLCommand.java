package net.plasmere.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.plasmere.save.sql.DbConn;

public class SQLCommand {
    public static void register(LiteralCommandNode root) {
        LiteralCommandNode<ServerCommandSource> inspectNode = CommandManager.literal("sql").requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.argument("sql", MessageArgumentType.message())
                        .executes(context -> query(context.getSource(), MessageArgumentType.getMessage(context, "sql"))))
                .build();

        root.addChild(inspectNode);
    }

    public static int query(ServerCommandSource scs, Text sql) throws CommandSyntaxException {
        DbConn.cusQuery(sql.asString(), scs.getPlayer());
        return 1;
    }
}
