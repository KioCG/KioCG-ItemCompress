package com.kiocg.ItemCompress.listeners;

import com.kiocg.ItemCompress.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class PrepareItemCraft implements Listener {
    @EventHandler
    public void onPrepareItemCraft(final @NotNull PrepareItemCraftEvent e) {
        final Recipe recipe = e.getRecipe();
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            // 压缩物品
            if (shapedRecipe.getKey().getKey().startsWith("compress_")) {
                final CraftingInventory craftingInventory = e.getInventory();

                int customModelData = 1260;
                final ItemStack[] matrix = craftingInventory.getMatrix();
                final ItemMeta itemMeta;
                if (matrix[0].hasItemMeta()) {
                    itemMeta = matrix[0].getItemMeta();
                    if (!itemMeta.hasCustomModelData() && itemMeta.hasDisplayName()) {
                        // 物品有自定义名，会在压缩中丢失
                        craftingInventory.setResult(null);
                        return;
                    } else if (itemMeta.hasCustomModelData()) {
                        customModelData = itemMeta.getCustomModelData();
                    }

                    if (customModelData < 1260 || customModelData >= 1269) {
                        // 不是压缩物品或超过最大压缩次数
                        craftingInventory.setResult(null);
                        return;
                    }

                    // 如果9个配方物品有不同
                    for (int i = 1; i < matrix.length; ++i) {
                        if (!matrix[i].hasItemMeta() || !Bukkit.getItemFactory().equals(itemMeta, matrix[i].getItemMeta())) {
                            craftingInventory.setResult(null);
                            return;
                        }
                    }
                } else {
                    // 如果9个配方物品有不同
                    for (int i = 1; i < matrix.length; ++i) {
                        if (matrix[i].hasItemMeta()) {
                            craftingInventory.setResult(null);
                            return;
                        }
                    }
                    itemMeta = matrix[0].getItemMeta();
                }

                if (!e.getViewers().get(0).hasPermission("kiocg.itemcompress.use")) {
                    craftingInventory.setResult(null);
                    return;
                }

                itemMeta.displayName(Utils.getCompressedName(customModelData + 1, matrix[0]));
                itemMeta.setCustomModelData(customModelData + 1);
                Objects.requireNonNull(craftingInventory.getResult()).setItemMeta(itemMeta);
            }
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            final NamespacedKey namespacedKey = shapelessRecipe.getKey();

            // 解压物品
            if (namespacedKey.getKey().startsWith("decompress_")) {
                final CraftingInventory craftingInventory = e.getInventory();
                //noinspection OptionalGetWithoutIsPresent
                final ItemStack itemStack = Arrays.stream(craftingInventory.getMatrix())
                                                  .filter(Objects::nonNull)
                                                  .findAny().get();

                if (!itemStack.hasItemMeta()) {
                    // 物品无法解压
                    craftingInventory.setResult(null);
                    return;
                }

                final ItemMeta itemMeta = itemStack.getItemMeta();
                if (!itemMeta.hasCustomModelData()) {
                    // 物品无法解压
                    craftingInventory.setResult(null);
                    return;
                }

                // 获取压缩次数
                final int customModelData = itemMeta.getCustomModelData();
                // 不是压缩物品
                if (!Utils.isCompressed(customModelData)) {
                    craftingInventory.setResult(null);
                    return;
                }

                if (!e.getViewers().get(0).hasPermission("kiocg.itemcompress.use")) {
                    craftingInventory.setResult(null);
                    return;
                }

                if (customModelData == 1261) {
                    // 解压成原版物品
                    itemMeta.displayName(null);
                    itemMeta.setCustomModelData(null);
                } else {
                    itemMeta.displayName(Utils.getCompressedName(customModelData - 1, itemStack));
                    itemMeta.setCustomModelData(customModelData - 1);
                }

                Objects.requireNonNull(craftingInventory.getResult()).setItemMeta(itemMeta);
                return;
            }

            // 覆盖原版的解压物品
            if (Utils.decompressCovers.contains(namespacedKey)) {
                final CraftingInventory craftingInventory = e.getInventory();
                //noinspection OptionalGetWithoutIsPresent
                final ItemStack itemStack = Arrays.stream(craftingInventory.getMatrix())
                                                  .filter(Objects::nonNull)
                                                  .findAny().get();

                if (!itemStack.hasItemMeta()) {
                    // 物品使用原版配方
                    return;
                }

                final ItemMeta itemMeta = itemStack.getItemMeta();
                if (!itemMeta.hasCustomModelData()) {
                    // 物品使用原版配方
                    return;
                }

                // 获取压缩次数
                final int customModelData = itemMeta.getCustomModelData();
                // 不是压缩物品
                if (!Utils.isCompressed(customModelData)) {
                    return;
                }

                if (!e.getViewers().get(0).hasPermission("kiocg.itemcompress.use")) {
                    craftingInventory.setResult(null);
                    return;
                }

                final ItemStack itemStackResult = itemStack.asQuantity(9);
                if (customModelData == 1261) {
                    // 解压成原版物品
                    itemMeta.displayName(null);
                    itemMeta.setCustomModelData(null);
                } else {
                    itemMeta.displayName(Utils.getCompressedName(customModelData - 1, itemStack));
                    itemMeta.setCustomModelData(customModelData - 1);
                }
                itemStackResult.setItemMeta(itemMeta);
                craftingInventory.setResult(itemStackResult);
            }
        }
    }
}
