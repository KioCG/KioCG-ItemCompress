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
    // 存储所有压缩物品配方的命名空间键
    public static final Set<NamespacedKey> namespacedKeys = new HashSet<>();
    // 存储覆盖解压的原版配方命名空间键
    // 如果一个物品在原版里能分解成其他物品，那么存入这里用来区分是应该解压还是分解
    public static final Set<NamespacedKey> decompressCovers = new HashSet<>();
    // 存储所有能够压缩与解压的物品
    public static final Set<Material> compressMaterial = EnumSet.noneOf(Material.class);

    // 存储压缩物品前缀
    private static final List<String> multiplePrefix = new ArrayList<>() {{
        add("§2①");
        add("§3②");
        add("§9③");
        add("§5④");
        add("§6⑤");
        add("§4⑥");
        add("§d⑦");
        add("§e⑧");
        add("§c⑨");
    }};

    // 返回是否为压缩物品
    public static boolean isCompressed(final int customModelData) {
        return customModelData > 1260 && customModelData < 1270;
    }

    // 返回压缩物品的显示名
    public static @NotNull Component getCompressedName(final int customModelData, final @NotNull ItemStack itemStack) {
        final int multiple = customModelData - 1260;
        //noinspection ImplicitNumericConversion,NumericCastThatLosesPrecision
        return Component.text(multiplePrefix.get(multiple - 1) + " ")
                        .append(Component.translatable(itemStack.translationKey()))
                        .append(Component.text("x" + (int) StrictMath.pow(9.0, multiple), NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false);
    }
}
