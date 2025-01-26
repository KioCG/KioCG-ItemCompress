package com.kiocg.ItemCompress.listeners;

import com.destroystokyo.paper.MaterialSetTag;
import com.kiocg.ItemCompress.Utils;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CancelListeners implements Listener {
    // 防止使用压缩物品
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(final @NotNull PlayerInteractEvent e) {
        final Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) {
            return;
        }

        final ItemStack itemStack = e.getItem();
        if (itemStack == null || itemStack.getMaxStackSize() == 1 || !itemStack.hasItemMeta()) {
            return;
        }

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasCustomModelData() && Utils.isCompressed(itemMeta.getCustomModelData())) {
            e.setUseItemInHand(Event.Result.DENY);

            // 防止染色告示牌
            if (action == Action.RIGHT_CLICK_BLOCK && MaterialSetTag.ALL_SIGNS.isTagged(Objects.requireNonNull(e.getClickedBlock()).getType())) {
                e.setUseInteractedBlock(Event.Result.DENY);
            }
        }
    }

    // 防止压缩物品与生物交互
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void cancelPlayerInteractEntity(final @NotNull PlayerInteractEntityEvent e) {
        final ItemStack itemStack = e.getPlayer().getInventory().getItem(e.getHand());
        if (itemStack.getMaxStackSize() == 1 || !itemStack.hasItemMeta()) {
            return;
        }

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasCustomModelData() && Utils.isCompressed(itemMeta.getCustomModelData())) {
            e.setCancelled(true);
        }
    }
}
