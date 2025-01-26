package com.kiocg.ItemCompress;

import com.kiocg.ItemCompress.listeners.CancelListeners;
import com.kiocg.ItemCompress.listeners.PrepareItemCraft;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ItemCompress extends JavaPlugin implements Listener {
    private static ItemCompress instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    public static ItemCompress getInstance() {
        //noinspection StaticVariableUsedBeforeInitialization
        return instance;
    }

    @Override
    public void onEnable() {
        new CreateRecipe().create();

        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PrepareItemCraft(), this);
        pluginManager.registerEvents(new CancelListeners(), this);

        // 兼容数据包重载
        pluginManager.registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        if (!Bukkit.isStopping()) {
            Utils.namespacedKeys.forEach(Bukkit::removeRecipe);
        }
    }

    @EventHandler
    public void onServerResourcesReloaded(final @NotNull ServerResourcesReloadedEvent e) {
        Utils.compressMaterial.clear();
        Utils.namespacedKeys.clear();
        Utils.decompressCovers.clear();
        Bukkit.getScheduler().runTask(this, () -> new CreateRecipe().create());
    }
}
