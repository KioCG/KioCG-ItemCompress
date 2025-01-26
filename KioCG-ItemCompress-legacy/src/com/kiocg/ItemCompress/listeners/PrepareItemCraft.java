package com.kiocg.ItemCompress.listeners;

import com.kiocg.ItemCompress.Utils;
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

                Integer customModelDataEquals = null;

                for (final ItemStack itemStack : craftingInventory.getMatrix()) {
                    int customModelData = 1260;
                    if (Objects.requireNonNull(itemStack).hasItemMeta()) {
                        final ItemMeta itemMeta = itemStack.getItemMeta();
                        if (!itemMeta.hasCustomModelData()) {
                            // 是有数据值的物品
                            craftingInventory.setResult(null);
                            return;
                        }

                        customModelData = itemMeta.getCustomModelData();
                    }

                    if (customModelDataEquals == null) {
                        customModelDataEquals = customModelData;
                    } else if (customModelDataEquals != customModelData || customModelData < 1260 || customModelData >= 1269) {
                        // 如果9个配方物品有不同、不是压缩物品或超过最大压缩次数
                        craftingInventory.setResult(null);
                        return;
                    }
                }

                if (!e.getViewers().get(0).hasPermission("kiocg.itemcompress.use")) {
                    craftingInventory.setResult(null);
                    return;
                }

                //noinspection DataFlowIssue
                final int finalCustomModelData = customModelDataEquals + 1;
                final ItemStack itemStackResult = craftingInventory.getResult();
                Objects.requireNonNull(itemStackResult).editMeta(itemMetaResult -> {
                    itemMetaResult.displayName(Utils.getCompressedName(finalCustomModelData, itemStackResult));
                    itemMetaResult.setCustomModelData(finalCustomModelData);
                });
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
                // 不是压缩物品或没有权限
                if (!Utils.isCompressed(customModelData) || !e.getViewers().get(0).hasPermission("kiocg.itemcompress.use")) {
                    craftingInventory.setResult(null);
                    return;
                }

                // 解压成原版物品
                if (customModelData == 1261) {
                    return;
                }

                final int finalCustomModelData = customModelData - 1;
                final ItemStack itemStackResult = craftingInventory.getResult();
                Objects.requireNonNull(itemStackResult).editMeta(itemMetaResult -> {
                    itemMetaResult.displayName(Utils.getCompressedName(finalCustomModelData, itemStackResult));
                    itemMetaResult.setCustomModelData(finalCustomModelData);
                });
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
                // 不是压缩物品或没有权限
                if (!Utils.isCompressed(customModelData)) {
                    return;
                } else if (!e.getViewers().get(0).hasPermission("kiocg.itemcompress.use")) {
                    craftingInventory.setResult(null);
                    return;
                }

                // 获取结果物品, 不要使用 itemStack.asQuantity(9) 克隆元数据
                final ItemStack itemStackResult = new ItemStack(itemStack.getType(), 9);

                // 解压成原版物品
                if (customModelData == 1261) {
                    craftingInventory.setResult(itemStackResult);
                    return;
                }

                final int finalCustomModelData = customModelData - 1;
                Objects.requireNonNull(itemStackResult).editMeta(itemMetaResult -> {
                    itemMetaResult.displayName(Utils.getCompressedName(finalCustomModelData, itemStackResult));
                    itemMetaResult.setCustomModelData(finalCustomModelData);
                });

                craftingInventory.setResult(itemStackResult);
            }
        }
    }
}
