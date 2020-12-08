package net.plasmere;

import net.minecraft.server.MinecraftServer;
import net.plasmere.commands.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.effect.StatusEffects;
import net.plasmere.commands.messaging.MsgCommand;
import net.plasmere.commands.messaging.ReplyCommand;
import net.plasmere.commands.punishments.*;
import net.plasmere.save.sql.DbConn;

import java.io.IOException;

public class TacoRevamped implements ModInitializer {

    private static MinecraftServer server;
    private static Configuration configuration;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onLogicalServerStarting);

        TacoRevamped.configuration = Configuration.load();
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            PermissionCommand.register(dispatcher);
            PlayerActionCommand.register(dispatcher, VanishCommand.class);
            StaffChatCommand.register(dispatcher);
            FlyCommand.register(dispatcher);
            DiscordCommand.register(dispatcher);
            GlowCommand.register(dispatcher);
            SitToggleCommand.register(dispatcher);
            VoteCommand.register(dispatcher);
            WhoMeCommand.register(dispatcher);
            SeekInventoryCommand.register(dispatcher);

            // Messaging
            MsgCommand.register(dispatcher);
            ReplyCommand.register(dispatcher);

            // Punishments
            BanCommand.register(dispatcher);
            BanIPCommand.register(dispatcher);
            MuteCommand.register(dispatcher);
            ServerMuteCommand.register(dispatcher);
            TempBanCommand.register(dispatcher);
            TempBanIPCommand.register(dispatcher);

            Commands commands = new Commands();
            commands.register(dispatcher);
        });

        // Connect to database when server is started
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            DbConn.connect(server);
            DbConn.server = server;
        });
        // Close DB connection when server is closed
        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            DbConn.close();
        });

        //Setup inspect mode
        InspectModeHandler.init();

        // When completed
        System.out.println("[BL] Initialization completed");

        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            TacoRevamped.shutdown();
        });
    }

    public static void shutdown() {
        try {
            configuration.save();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Server Simplified: couldn't save configuration!");
        }

        VanishCommand.getVanished().forEach(entity -> entity.removeStatusEffect(StatusEffects.INVISIBILITY));
    }

    private void onLogicalServerStarting(MinecraftServer minecraftServer){ server = minecraftServer; }

    public static Configuration getConfiguration() {
        return configuration;
    }
    public static MinecraftServer getServer() { return server; }
}