package com.kiocg.ItemCompress;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class CreateRecipe {
    public void create() {
        // 存储所有物品
        final Set<Material> materials = EnumSet.copyOf(Lists.newArrayList(Registry.MATERIAL));

        // 应覆盖解压的原版配方
        final Set<Recipe> coverRecipe = new HashSet<>();
        final Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            final Recipe recipe = iterator.next();

            if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                if (!shapelessRecipe.getKey().getNamespace().equals(NamespacedKey.MINECRAFT)) {
                    continue;
                }

                final List<RecipeChoice> choiceList = shapelessRecipe.getChoiceList();

                // 如果配方是由1个物品合成
                if (choiceList.size() == 1) {
                    coverRecipe.add(shapelessRecipe);
                    Utils.decompressCovers.add(shapelessRecipe.getKey());
                    continue;
                }

                // 如果配方是由9个相同的物品合成
                //noinspection ObjectAllocationInLoop
                nineEqualsRecipeChoiceConsumer(choiceList, materials::remove);
            } else if (recipe instanceof ShapedRecipe shapedRecipe) {
                if (!shapedRecipe.getKey().getNamespace().equals(NamespacedKey.MINECRAFT)) {
                    continue;
                }

                final Collection<RecipeChoice> choiceList = shapedRecipe.getChoiceMap().values();

                // 如果配方是由9个相同的物品合成
                //noinspection ObjectAllocationInLoop
                nineEqualsRecipeChoiceConsumer(choiceList, materials::remove);
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

    private void nineEqualsRecipeChoiceConsumer(final @NotNull Collection<RecipeChoice> choiceList, final Consumer<Material> consumer) {
        if (choiceList.size() == 9 && choiceList.stream().distinct().count() == 1L) {
            final RecipeChoice recipeChoice = choiceList.iterator().next();
            if (recipeChoice instanceof RecipeChoice.MaterialChoice materialChoice) {
                materialChoice.getChoices().forEach(consumer);
            } else if (recipeChoice instanceof RecipeChoice.ExactChoice exactChoice) {
                exactChoice.getChoices().stream().map(ItemStack::getType).forEach(consumer);
            } else {
                throw new IllegalStateException("意料外的合成配方");
            }
        }
    }
}
