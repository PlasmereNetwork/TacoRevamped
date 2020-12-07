package net.plasmere.mixin;

import net.plasmere.save.sql.DbConn;
import net.plasmere.utils.LoggedEventType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BucketItem.class)
public class BucketItemMixin {
    @Inject(method = "placeFluid", at = @At("TAIL"))
    public void placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult blockHitResult, CallbackInfoReturnable c){
        if(player != null){

            HitResult result = player.raycast(5, 0.0F, true);
            if (result.getType().equals(HitResult.Type.BLOCK)) {
                DbConn.writeFluidInteraction(pos, world.getFluidState(pos).toString(), player, LoggedEventType.placed, true);
            }
            else{
                //DbConn.writeFluidInteraction(pos, world.getFluidState(pos).toString(), player, LoggedEventType.placed, true);
            }
        }
        else{
            DbConn.writeFluidInteraction(pos, world.getFluidState(pos).toString(), player, LoggedEventType.placed, false);
        }
    }
}
