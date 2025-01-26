package com.kiocg.ItemCompress;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.*;

public class CreateRecipe {
    public void create() {
        // 存储所有物品
        final Set<Material> materials = EnumSet.copyOf(Arrays.asList(Material.values()));

        materials.remove(Material.AIR);
        materials.remove(Material.BARRIER);

        // 应覆盖解压的原版配方
        final Set<Recipe> coverRecipe = new HashSet<>();
        final Iterator<Recipe> iterator = Bukkit.recipeIterator();
        run:
        while (iterator.hasNext()) {
            final Recipe recipe = iterator.next();

            if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                final List<RecipeChoice> choiceList = shapelessRecipe.getChoiceList();

                // 如果配方是由1个物品合成
                if (choiceList.size() == 1) {
                    coverRecipe.add(shapelessRecipe);
                    Utils.decompressCovers.add(shapelessRecipe.getKey());
                    continue;
                }

                // 如果配方是由9个物品合成
                if (choiceList.size() == 9) {
                    Material ingredientEquals = null;

                    for (final RecipeChoice recipeChoice : choiceList) {
                        // if (recipeChoice == null) {
                        //     continue run;
                        // }

                        //noinspection deprecation
                        final ItemStack itemStack = recipeChoice.getItemStack();
                        if (ingredientEquals == null) {
                            ingredientEquals = itemStack.getType();
                        } else if (ingredientEquals != itemStack.getType()) {
                            continue run;
                        }
                    }

                    // 如果这9个物品相同
                    materials.remove(ingredientEquals);
                }
            } else if (recipe instanceof ShapedRecipe shapedRecipe) {
                final Map<Character, RecipeChoice> choiceMap = shapedRecipe.getChoiceMap();

                // 如果配方是由9个物品合成
                if (choiceMap.size() == 9) {
                    Material ingredientEquals = null;

                    for (final RecipeChoice recipeChoice : choiceMap.values()) {
                        if (recipeChoice == null) {
                            continue run;
                        }

                        //noinspection deprecation
                        final ItemStack itemStack = recipeChoice.getItemStack();
                        if (ingredientEquals == null) {
                            ingredientEquals = itemStack.getType();
                        } else if (ingredientEquals != itemStack.getType()) {
                            continue run;
                        }
                    }

                    // 如果这9个物品相同
                    materials.remove(ingredientEquals);
                }
            }
        }

        // 创建压缩与解压的物品配方
        materials.stream()
                 .filter(material -> material.isItem() && material.getMaxStackSize() >= 9 && material.getCraftingRemainingItem() == null)
                 .forEach(material -> {
                     Utils.compressMaterial.add(material);

                     final NamespacedKey namespacedKey_Compress = new NamespacedKey(ItemCompress.getInstance(), "Compress_" + material);
                     Bukkit.addRecipe(new ShapedRecipe(namespacedKey_Compress, new ItemStack(material)).shape("aaa", "aaa", "aaa").setIngredient('a', material));
                     Utils.namespacedKeys.add(namespacedKey_Compress);

                     final NamespacedKey namespacedKey_Decompress = new NamespacedKey(ItemCompress.getInstance(), "Decompress_" + material);
                     Bukkit.addRecipe(new ShapelessRecipe(namespacedKey_Decompress, new ItemStack(material, 9)).addIngredient(1, material));
                     Utils.namespacedKeys.add(namespacedKey_Decompress);
                 });

        // 重新添加原版配方，以覆盖解压配方
        coverRecipe.forEach(recipe -> {
            Bukkit.removeRecipe(((Keyed) recipe).getKey());
            Bukkit.addRecipe(recipe);
        });
    }
}
