package net.plasmere.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.plasmere.utils.IScreenHandlerMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.OptionalInt;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ScreenHandlerListener {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method="openHandledScreen", at=@At(value="INVOKE", target="Lnet/minecraft/screen/ScreenHandler;addListener(Lnet/minecraft/screen/ScreenHandlerListener;)V"), locals= LocalCapture.CAPTURE_FAILEXCEPTION)
    public void handledScreenMixin(NamedScreenHandlerFactory screenHandlerFactory, CallbackInfoReturnable<OptionalInt> cir, ScreenHandler screenHandler) { // NamedScreenHandlerFactory screenHandler, CallbackInfoReturnable<OptionalInt> cir, ScreenHandler var2, ServerPlayNetworkHandler var4, OpenScreenS2CPacket var5
        if (screenHandlerFactory instanceof LockableContainerBlockEntity) {
            ((IScreenHandlerMixin)screenHandler).setLoggingInfo(((LockableContainerBlockEntity) screenHandlerFactory).getPos());
        }
    }
}
