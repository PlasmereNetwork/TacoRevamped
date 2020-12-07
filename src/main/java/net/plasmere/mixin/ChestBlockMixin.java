package net.plasmere.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.plasmere.utils.IScreenHandlerMixin;

import java.util.function.Supplier;


@SuppressWarnings("public-target")
@Mixin(targets={"net/minecraft/block/ChestBlock$2$1"})
public class ChestBlockMixin {
    private ChestBlockEntity chestBlockEntity;
    private ChestBlockEntity chestBlockEntity2;

//    @Inject(method="<init>", at=@At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
//    public void constructorInject(@Coerce DoubleBlockProperties.PropertyRetriever arg, ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2, Inventory inventory, CallbackInfo ci) {
//        this.chestBlockEntity = chestBlockEntity;
//        this.chestBlockEntity2 = chestBlockEntity2;
//    }
//
//    @Redirect(method="createScreenHandlerFactory", at=@At(value="INVOKE", target="Lnet/minecraft/screen/GenericContainerScreenHandler;createGeneric9x6(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)Lnet/minecraft/screen/GenericContainerScreenHandler;"))
//    public GenericContainerScreenHandler createGeneric9x6Redirect(int syncId, PlayerInventory playerInventory, Inventory inventory) {
//        GenericContainerScreenHandler result = GenericContainerScreenHandler.createGeneric9x6(syncId, playerInventory, inventory);
//        ((IScreenHandlerMixin) result).setLoggingInfo(chestBlockEntity.getPos());
//        return result;
//    }
}
