package net.plasmere.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.plasmere.save.sql.DbConn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.plasmere.utils.IScreenHandlerMixin;
import net.plasmere.utils.LoggedEventType;

import java.util.List;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin implements IScreenHandlerMixin {
    @Final public List<Slot> slots;
    private BlockPos loggingPos;
    private boolean shouldLogUpdates = false;

    @Override
    public void setLoggingInfo(BlockPos loggingPos) {
        if (this.slots.size() == 0) { // Improvement: Check possible race condition where slots aren't yet initialized here
            System.out.println("[BL] Empty inventory opened for logging. Skipping.");
            return;
        }
        this.loggingPos = loggingPos;
        this.shouldLogUpdates = true;
    }
//
//    @Inject(method="method_30010", at=@At("HEAD"), remap=false)
//    public void slotClickInject(int i, int j, SlotActionType slotActionType, PlayerEntity playerEntity, CallbackInfoReturnable<ItemStack> cir) {
//        System.out.println(i + ", " + j + ", " + slotActionType.toString());
//    }

    @Redirect(method ="method_30010", at=@At(value="INVOKE", target="Lnet/minecraft/screen/slot/Slot;takeStack(I)Lnet/minecraft/item/ItemStack;", remap=true), remap = false)
    public ItemStack takeStackRedirect(Slot slot, int amount, int i, int j, SlotActionType slotActionType, PlayerEntity player) {
        ItemStack takenStack = slot.takeStack(amount);
        if (shouldLogUpdates && !(slot.inventory instanceof PlayerInventory)) { // could use an accessor mixin -> slot index -> constraint check instead
            logChange(player, takenStack, ItemStack.EMPTY);
        }
        return takenStack;
    }

    @Redirect(method ="method_30010", at=@At(value="INVOKE", target="Lnet/minecraft/screen/slot/Slot;setStack(Lnet/minecraft/item/ItemStack;)V", remap=true), remap = false)
    public void setStackRedirect(Slot slot, ItemStack stack, int i, int j, SlotActionType slotActionType, PlayerEntity player) {
        if (shouldLogUpdates && !(slot.inventory instanceof PlayerInventory)) {
            logChange(player, slot.getStack(), stack);
        }
        slot.setStack(stack);
    }

    @Redirect(method ="method_30010", at=@At(value="INVOKE", target="Lnet/minecraft/screen/ScreenHandler;transferSlot(Lnet/minecraft/entity/player/PlayerEntity;I)Lnet/minecraft/item/ItemStack;", remap=true), remap = false)
    public ItemStack transferSlotRedirect(ScreenHandler handler, PlayerEntity player, int slotIndex, int i, int j, SlotActionType slotActionType, PlayerEntity _player) {
        Slot sourceSlot = this.slots.get(slotIndex);
        if (sourceSlot == null) return handler.transferSlot(player, slotIndex);
        int origStackCount = sourceSlot.getStack().getCount();
        ItemStack result = handler.transferSlot(player, slotIndex);
        if (shouldLogUpdates) {
            int newStackCount = sourceSlot.getStack().getCount();
            ItemStack clonedResult = result.copy();
            boolean isAdd = sourceSlot.inventory instanceof PlayerInventory;
            clonedResult.setCount(origStackCount - newStackCount);
            logChange(player, isAdd ? ItemStack.EMPTY : clonedResult, isAdd ? clonedResult : ItemStack.EMPTY);
        }
        return result;
    }

    @Inject(method ="method_30010", at=@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;increment(I)V", remap=true, ordinal=0), remap = false, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void incrementInject(int i, int j, SlotActionType slotActionType, PlayerEntity playerEntity, CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack, PlayerInventory playerInventory, Slot slot4, ItemStack itemStack7, ItemStack itemStack8, int q) {
        if (shouldLogUpdates && !(slot4.inventory instanceof PlayerInventory)) {
            ItemStack newItemStack = itemStack7.copy();
            newItemStack.increment(q);
            logChange(playerEntity, itemStack7, newItemStack);
        }
    }

    private void logChange(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
        if (oldStack.isEmpty() && newStack.isEmpty()) return; // nothing to do

        if (!oldStack.isEmpty() && !newStack.isEmpty()) { // 2 non-empty stacks
            if (oldStack.getItem() == newStack.getItem()) { // add or remove to stack of same type
                int newCount = newStack.getCount();
                int oldCount = oldStack.getCount();
                if (newCount > oldCount) { // add items
                    logChange(player, ItemStack.EMPTY, new ItemStack(newStack.getItem(), newCount - oldCount));
                } else { // remove items
                    logChange(player, new ItemStack(newStack.getItem(), oldCount - newCount), ItemStack.EMPTY);
                }
            } else { // split up the actions
                logChange(player, oldStack, ItemStack.EMPTY); // log taking out the old stack
                logChange(player, ItemStack.EMPTY, newStack); // log putting in the new stack
            }
            return;
        }

        boolean oldEmpty = oldStack.isEmpty(); // we know only one is empty
        ItemStack significantStack = oldEmpty ? newStack : oldStack;
        LoggedEventType eventType = oldEmpty ? LoggedEventType.added : LoggedEventType.removed;
        // System.out.println(pos.toString() + ": " + player.getName().getString() + " " + eventType.toString() + " " + significantStack.toString());
        DbConn.writeContainerTransaction(loggingPos, significantStack, player, eventType);
    }
}
