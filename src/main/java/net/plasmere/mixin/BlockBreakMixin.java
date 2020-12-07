package net.plasmere.mixin;

import net.plasmere.save.sql.DbConn;
import net.plasmere.utils.LoggedEventType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(Block.class)
public abstract class BlockBreakMixin implements ItemConvertible {
  @Inject(at = @At("HEAD"), method = "onBreak")
  public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
    DbConn.writeInteractions(pos, state, player, LoggedEventType.broken, true);
  }
}