package com.kiocg.ItemCompress.listeners;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class CraftItem implements Listener {
    // 在合成时, 内部代码对配方的遍历次数太多, 造成严重的卡顿
    // 此处将所有批量压缩优化成单次遍历
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftItem(final @NotNull CraftItemEvent e) {
        final ClickType clickType = e.getClick();
        if (clickType != ClickType.SHIFT_LEFT && clickType != ClickType.SHIFT_RIGHT) {
            return;
        }

        final CraftingInventory craftingInventory = e.getInventory();
        final ItemStack result = craftingInventory.getResult();
        if (result == null) {
            return;
        }

        // 合成物品

        final ItemStack[] matrix = craftingInventory.getMatrix();
        int craftAmount = 64;
        boolean isEmpty = true;

        for (final ItemStack itemStack : matrix) {
            if (itemStack == null) {
                continue;
            } else if (itemStack.getType().getCraftingRemainingItem() != null) {
                // 配方有剩余物, 交给原版处理
                return;
            }

            isEmpty = false;

            final int amountTemp = itemStack.getAmount();
            if (craftAmount > amountTemp) {
                craftAmount = amountTemp;
            }
        }

        if (isEmpty) {
            return;
        }

        // 需要取消事件, 否则物品会被覆盖
        e.setCancelled(true);

        final Player player = (Player) e.getWhoClicked();
        final int resultAmount = result.getAmount();
        final ItemStack leftover = betterAddItem(player, result.clone(), craftAmount * resultAmount);
        if (leftover != null) {
            final int leftoverAmount = leftover.getAmount();

            final int amount = leftoverAmount / resultAmount;
            if (amount != 0) {
                for (final ItemStack itemStack : matrix) {
                    if (itemStack != null) {
                        itemStack.setAmount(itemStack.getAmount() - craftAmount + amount);
                    }
                }
            } else {
                craftingInventory.setResult(null);
                for (final ItemStack itemStack : matrix) {
                    if (itemStack != null) {
                        itemStack.setAmount(itemStack.getAmount() - craftAmount);
                    }
                }
            }

            final int remainder = leftoverAmount % resultAmount;
            if (remainder != 0) {
                final Item item = player.getWorld().dropItem(player.getLocation(), leftover.asQuantity(remainder));

                // 兼容其他插件
                final PlayerDropItemEvent playerDropItemEvent = new PlayerDropItemEvent(player, item);
                if (!playerDropItemEvent.callEvent()) {
                    item.remove();
                    return;
                }

                player.playSound(player, Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.PLAYERS, 0.5F, 2.0F);
            }
        } else {
            craftingInventory.setResult(null);
            for (final ItemStack itemStack : matrix) {
                if (itemStack != null) {
                    itemStack.setAmount(itemStack.getAmount() - craftAmount);
                }
            }
        }
    }

    // 正确的往玩家背包添加大数量物品 (例如防止鸡蛋堆叠成64个)
    private static @Nullable ItemStack betterAddItem(final @NotNull Player player, final @NotNull ItemStack itemStack, int amount) {
        final int maxStackSize = itemStack.getMaxStackSize();
        itemStack.setAmount(maxStackSize);

        final int remainder = amount % maxStackSize;
        amount /= maxStackSize;

        while (amount-- > 0) {
            final HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(itemStack.clone());
            if (!leftovers.isEmpty()) {
                final ItemStack leftover = leftovers.get(0);
                leftover.setAmount(leftover.getAmount() + amount * maxStackSize + remainder);
                return leftover;
            }
        }

        if (remainder != 0) {
            final HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(itemStack.asQuantity(remainder));
            if (!leftovers.isEmpty()) {
                return leftovers.get(0);
            }
        }

        return null;
    }
}
