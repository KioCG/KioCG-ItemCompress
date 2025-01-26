package com.kiocg.ItemCompress.listeners;

import com.destroystokyo.paper.MaterialSetTag;
import com.kiocg.ItemCompress.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EntityChangeBlock implements Listener {
    @EventHandler
    public void onEntityChangeBlock(final @NotNull EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock fallingBlock)
            || !MaterialSetTag.ANVIL.isTagged(fallingBlock.getBlockData().getMaterial())) {
            return;
        }

        final Block block = e.getBlock();
        if (block.getType() != Material.AIR) {
            return;
        }

        final Location location = block.getLocation();
        if (!MaterialSetTag.ANVIL.isTagged(location.clone().subtract(0.0, 1.0, 0.0).getBlock().getType())) {
            return;
        }

        final List<Item> items = (ArrayList<Item>) location.add(0.5, 0.0, 0.5).getNearbyEntitiesByType(Item.class, 0.5);
        if (items.isEmpty()) {
            return;
        }

        // 铁砧砸铁砧, 并尝试压缩物品

        final ItemStack itemStack = items.get(0).getItemStack();

        // 物品无法压缩
        final Material material = itemStack.getType();
        if (!Utils.compressMaterial.contains(material)) {
            return;
        }

        // 超过最大压缩次数
        final ItemMeta itemMeta = itemStack.getItemMeta();
        final int customModelData = itemMeta.hasCustomModelData() ? itemMeta.getCustomModelData() : 1260;
        if (customModelData >= 1269) {
            return;
        }

        int totalAmount = itemStack.getAmount();
        for (final Item item : items.subList(1, items.size())) {
            final ItemStack itemStackTemp = item.getItemStack();
            if (!itemStackTemp.isSimilar(itemStack)) {
                // 物品里含有杂质
                return;
            }

            totalAmount += itemStackTemp.getAmount();
        }
        if (totalAmount < 9) {
            // 数量不足以压缩
            return;
        }

        // 扣除物品
        int remainder = totalAmount % 9;
        for (final Item item : items) {
            if (remainder <= 0) {
                item.remove();
            }

            final ItemStack itemStackTemp = item.getItemStack();
            final int amountTemp = itemStackTemp.getAmount();
            if (remainder > amountTemp) {
                remainder -= amountTemp;
            } else {
                itemStackTemp.setAmount(remainder);
                remainder = 0;
            }
        }

        // 生成压缩物品
        final ItemStack itemStackDrop = new ItemStack(material);

        itemStackDrop.editMeta(itemMetaDrop -> {
            itemMetaDrop.displayName(Utils.getCompressedName(customModelData + 1, itemStackDrop));
            itemMetaDrop.setCustomModelData(customModelData + 1);
        });

        final int maxStackSize = itemStackDrop.getMaxStackSize();
        itemStackDrop.setAmount(maxStackSize);

        totalAmount /= 9;
        while (true) {
            if (totalAmount > maxStackSize) {
                totalAmount -= maxStackSize;
                block.getWorld().dropItem(location, itemStackDrop.clone());
            } else {
                block.getWorld().dropItem(location, itemStackDrop.asQuantity(totalAmount));
                return;
            }
        }
    }
}
