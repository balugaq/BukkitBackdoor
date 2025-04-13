package com.balugaq.bukkitbackdoor.implementation;

import com.balugaq.bukkitbackdoor.core.managers.ConfigManager;
import com.balugaq.bukkitbackdoor.core.managers.ListenerManager;
import com.balugaq.bukkitbackdoor.utils.Superhead;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class BukkitBackdoorPlugin extends JavaPlugin {
    @Getter
    public static BukkitBackdoorPlugin instance;
    @Getter
    public ConfigManager configManager;
    @Getter
    public ListenerManager listenerManager;

    @Override
    public void onEnable() {
        instance = this;
        Superhead.show();
        configManager = new ConfigManager(this);
        listenerManager = new ListenerManager(this);
        listenerManager.registerListeners();
    }

    @Override
    public void onDisable() {
        instance = null;
        listenerManager.unregisterListeners();
    }
}
