package net.plasmere;

import net.plasmere.commands.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.effect.StatusEffects;
import net.plasmere.commands.messaging.MsgCommand;
import net.plasmere.commands.messaging.ReplyCommand;
import net.plasmere.save.sql.DbConn;

import java.io.IOException;

public class TacoRevamped implements ModInitializer {

    private static Configuration configuration;

    @Override
    public void onInitialize() {
        TacoRevamped.configuration = Configuration.load();
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            MuteCommand.register(dispatcher);
            PermissionCommand.register(dispatcher);
            PlayerActionCommand.register(dispatcher, VanishCommand.class);
            ServerMuteCommand.register(dispatcher);
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
        });

//        // Register commands
//        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
//            Commands commands = new Commands();
//            commands.register(dispatcher);
//        });
//        // Connect to database when server is started
//        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
//            DbConn.connect(server);
//            DbConn.server = server;
//        });
//        // Close DB connection when server is closed
//        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
//            DbConn.close();
//        });
//
//        //Setup inspect mode
//        InspectModeHandler.init();
//
//        // When completed
//        System.out.println("[BL] Initialisation completed");

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

    public static Configuration getConfiguration() {
        return configuration;
    }

}