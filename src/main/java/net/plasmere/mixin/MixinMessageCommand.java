package net.plasmere.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(MessageCommand.class)
public class MixinMessageCommand {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Text message, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        for (ServerPlayerEntity p : targets) {
            source.getMinecraftServer().getCommandManager().execute(source, "taco:msg " + p.getName().asString() + " " + message.asString());
        }
        cir.setReturnValue(1);
    }
}
