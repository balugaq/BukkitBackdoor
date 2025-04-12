package com.balugaq.bukkitbackdoor;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class BukkitBackdoor extends JavaPlugin {
    @Getter
    public static BukkitBackdoor instance;
    public ChatListener chatListener;
    public DefaultConfig defaultConfig;

    @Override
    public void onEnable() {
        instance = this;
        Superhead.show();
        chatListener = new ChatListener();
        defaultConfig = new DefaultConfig();
        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(defaultConfig, this);
    }

    @Override
    public void onDisable() {
        instance = null;
        HandlerList.unregisterAll(chatListener);

    }
}
