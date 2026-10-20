package com.breakinblocks.justenoughcraftingtables.menu;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class WindowSlot extends Slot {
    private final StationMenu menu;
    private final int position;

    public WindowSlot(StationMenu menu, WindowContainer container, int position, int x, int y) {
        super(container, position, x, y);
        this.menu = menu;
        this.position = position;
    }

    public int position() {
        return position;
    }

    @Nullable
    public IItemHandler handler() {
        return menu.windowHandler();
    }

    public int realIndex() {
        return menu.windowIndex(position);
    }

    public void pickup(Player player, int button) {
        menu.windowPickup(this, button, player);
    }

    @Override
    public boolean isActive() {
        return menu.isWindowSlotActive(position);
    }

    @Override
    public ItemStack getItem() {
        IItemHandler handler = handler();
        int real = realIndex();
        if (handler == null || real < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = handler.getStackInSlot(real);
        return menu.isClientSide() ? stack : stack.copy();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        IItemHandler handler = handler();
        int real = realIndex();
        if (stack.isEmpty() || handler == null || real < 0 || !menu.allowInsert()) {
            return false;
        }
        ItemStack current = handler.getStackInSlot(real);
        if (!current.isEmpty() && !ItemStack.isSameItemSameTags(current, stack)) {
            return false;
        }
        return handler.isItemValid(real, stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        IItemHandler handler = handler();
        int real = realIndex();
        if (handler == null || real < 0 || handler.getStackInSlot(real).isEmpty()) {
            return false;
        }
        return !handler.extractItem(real, 1, true).isEmpty();
    }

    @Override
    public boolean allowModification(Player player) {
        return mayPickup(player);
    }

    @Override
    public int getMaxStackSize() {
        IItemHandler handler = handler();
        int real = realIndex();
        return handler == null || real < 0 ? 0 : handler.getSlotLimit(real);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        IItemHandler handler = handler();
        int real = realIndex();
        if (handler == null || real < 0 || stack.isEmpty()) {
            return 0;
        }
        ItemStack current = handler.getStackInSlot(real);
        int max = stack.getMaxStackSize();
        if (!current.isEmpty() && !ItemStack.isSameItemSameTags(current, stack)) {
            return Math.min(max, current.getCount());
        }
        ItemStack remainder = handler.insertItem(real, stack.copyWithCount(max), true);
        int accepted = max - remainder.getCount();
        return Math.min(max, current.getCount() + accepted);
    }

    @Override
    public ItemStack remove(int amount) {
        IItemHandler handler = handler();
        int real = realIndex();
        if (handler == null || real < 0 || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack current = handler.getStackInSlot(real);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = handler.extractItem(real, Math.min(amount, current.getMaxStackSize()), false);
        if (!extracted.isEmpty()) {
            menu.onWindowModified();
        }
        return extracted;
    }

    @Override
    public void set(ItemStack stack) {
        menu.setWindowStack(this, stack);
    }

    @Override
    public void setChanged() {
        menu.onWindowModified();
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int increment) {
        if (stack.isEmpty() || !mayPlace(stack)) {
            return stack;
        }
        IItemHandler handler = handler();
        int real = realIndex();
        if (handler == null || real < 0) {
            return stack;
        }
        int offered = Math.min(increment, stack.getCount());
        if (offered <= 0) {
            return stack;
        }
        ItemStack remainder = handler.insertItem(real, stack.copyWithCount(offered), false);
        int accepted = offered - remainder.getCount();
        if (accepted > 0) {
            stack.shrink(accepted);
            menu.onWindowModified();
        }
        return stack;
    }

    @Override
    public Optional<ItemStack> tryRemove(int count, int decrement, Player player) {
        if (!mayPickup(player)) {
            return Optional.empty();
        }
        IItemHandler handler = handler();
        int real = realIndex();
        if (handler == null || real < 0) {
            return Optional.empty();
        }
        ItemStack current = handler.getStackInSlot(real);
        int amount = Math.min(Math.min(count, decrement), current.getMaxStackSize());
        if (amount <= 0) {
            return Optional.empty();
        }
        ItemStack extracted = handler.extractItem(real, amount, false);
        if (extracted.isEmpty()) {
            return Optional.empty();
        }
        menu.onWindowModified();
        return Optional.of(extracted);
    }
}
