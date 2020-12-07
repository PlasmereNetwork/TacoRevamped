package net.plasmere.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.plasmere.save.sql.DbConn;
import net.plasmere.utils.LoggedEventType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerInteractionManager.class)
public class BlockUseMixin {
    @Inject(method="interactBlock", at=@At(value="INVOKE", target="Lnet/minecraft/advancement/criterion/ItemUsedOnBlockCriterion;test(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V", ordinal = 0), locals= LocalCapture.CAPTURE_FAILEXCEPTION)
    public void interactInject(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir, BlockPos blockPos, BlockState blockState) {
        DbConn.writeInteractions(blockPos, blockState, player, LoggedEventType.used, true);
    }
}
