package com.kiocg.ItemCompress;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Utils {
    // 存储所有能够压缩与解压的物品
    public static final Set<Material> compressMaterial = EnumSet.noneOf(Material.class);
    // 存储所有压缩物品配方的命名空间键
    public static final Set<NamespacedKey> namespacedKeys = new HashSet<>();
    // 存储覆盖解压的原版配方命名空间键
    // 如果一个物品在原版里能分解成其他物品，那么存入这里用来区分是应该解压还是分解
    public static final Set<NamespacedKey> decompressCovers = new HashSet<>();

    // 存储压缩物品前缀
    private static final List<Component> multiplePrefix = new ArrayList<>() {{
        add(Component.text("① ", NamedTextColor.DARK_GREEN));
        add(Component.text("② ", NamedTextColor.DARK_AQUA));
        add(Component.text("③ ", NamedTextColor.BLUE));
        add(Component.text("④ ", NamedTextColor.DARK_PURPLE));
        add(Component.text("⑤ ", NamedTextColor.GOLD));
        add(Component.text("⑥ ", NamedTextColor.DARK_RED));
        add(Component.text("⑦ ", NamedTextColor.LIGHT_PURPLE));
        add(Component.text("⑧ ", NamedTextColor.YELLOW));
        add(Component.text("⑨ ", NamedTextColor.RED));
    }};

    // 返回是否为压缩物品
    public static boolean isCompressed(final int customModelData) {
        return customModelData > 1260 && customModelData < 1270;
    }

    // 返回压缩物品的显示名
    public static @NotNull Component getCompressedName(final int customModelData, final @NotNull ItemStack itemStack) {
        final int multiple = customModelData - 1260;
        // noinspection NumericCastThatLosesPrecision
        return Component.empty().append(multiplePrefix.get(multiple - 1))
                        .append(Component.translatable(itemStack.translationKey()))
                        .append(Component.text("x" + (int) Math.pow(9, multiple), NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false);
    }
}
