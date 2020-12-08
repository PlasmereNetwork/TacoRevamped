package net.plasmere.mixin;

import net.minecraft.network.MessageType;
import net.minecraft.text.TranslatableText;
import net.plasmere.Utils;
import net.plasmere.commands.punishments.MuteCommand;
import net.plasmere.commands.punishments.ServerMuteCommand;
import net.plasmere.commands.StaffChatCommand;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {


    @Shadow
    public ServerPlayerEntity player;
    @Shadow
    private MinecraftServer server;

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    public void broadcastChatMessage(ChatMessageC2SPacket packet, CallbackInfo info) {
        if (MuteCommand.isMuted(player.getUuidAsString())&&!Utils.hasPermission(player, "mute")) {
            player.sendSystemMessage(Utils.codedCHText("&cYou are muted! You will be unmuted at: &6" + MuteCommand.whenUnmute(player.getUuidAsString())), Util.NIL_UUID);
            info.cancel();
        } 
        else if(ServerMuteCommand.isMuted()&&!Utils.hasPermission(player, "servermute")){
            String serverMuted = "&cThe server has been muted, please contact the moderators to unmute.";
            Text serverIsMuted = Utils.codedCHText(serverMuted);
            player.sendSystemMessage(serverIsMuted, Util.NIL_UUID);
            info.cancel();
        }
        else if (StaffChatCommand.isInStaffChat(player.getUuidAsString())&&!packet.getChatMessage().startsWith("/")) {
            String message = packet.getChatMessage();
            StaffChatCommand.sendToStaffChat(StaffChatCommand.generateStaffChatMessage(player, message), server);
            info.cancel();
        } else if (packet.getChatMessage().startsWith("#")){
            String message = packet.getChatMessage().substring(1);
            StaffChatCommand.sendToStaffChat(StaffChatCommand.generateStaffChatMessage(player, message), server);
            info.cancel();
        } else if (! packet.getChatMessage().startsWith("/")){
            String message = packet.getChatMessage();
            Text text = new TranslatableText("chat.type.text", this.player.getDisplayName(), Utils.codedCHText(message));
            this.server.getPlayerManager().broadcastChatMessage(text, MessageType.CHAT, this.player.getUuid());
            info.cancel();
        }
    }
}
