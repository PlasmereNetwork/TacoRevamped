package net.plasmere.mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
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
@Mixin(ChestBlockEntity.class)
public class ChestBlockMixin extends BlockEntity {
    private final BlockPos blockPos;

    public ChestBlockMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.blockPos = blockPos;
    }

    // target="Lnet/minecraft/screen/GenericContainerScreenHandler;createGeneric9x6(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)Lnet/minecraft/screen/GenericContainerScreenHandler;"
    @Redirect(method="createScreenHandler", at=@At(value="INVOKE", target = "Lnet/minecraft/screen/GenericContainerScreenHandler;createGeneric9x6(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)Lnet/minecraft/screen/GenericContainerScreenHandler;"))
    public GenericContainerScreenHandler createGeneric9x6Redirect(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        GenericContainerScreenHandler result = GenericContainerScreenHandler.createGeneric9x6(syncId, playerInventory, inventory);
        ((IScreenHandlerMixin) result).setLoggingInfo(blockPos);
        return result;
    }
}
