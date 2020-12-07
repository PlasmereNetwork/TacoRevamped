package net.plasmere.mixin;

import net.plasmere.save.sql.DbConn;
import net.plasmere.utils.LoggedEventType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;

@Mixin(BlockItem.class)
public abstract class BlockPlaceMixin extends Item {
    public BlockPlaceMixin(Settings settings) {
    super(settings);
  }

  @Inject(at = @At(value = "INVOKE",target = "Lnet/minecraft/item/ItemPlacementContext;getBlockPos()Lnet/minecraft/util/math/BlockPos;"),method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;")
  public void place(ItemPlacementContext c, CallbackInfoReturnable<Boolean> info) {
    if(c.getPlayer()!=null){
      DbConn.writeInteractions(c.getBlockPos(), c.getWorld().getBlockState(c.getBlockPos()), c.getPlayer(), LoggedEventType.placed, true);
    }
    else{
      DbConn.writeInteractions(c.getBlockPos(), c.getWorld().getBlockState(c.getBlockPos()), c.getPlayer(), LoggedEventType.placed, false);
    }
  }
}
