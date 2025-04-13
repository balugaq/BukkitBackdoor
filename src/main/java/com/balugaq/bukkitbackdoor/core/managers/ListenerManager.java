package com.balugaq.bukkitbackdoor.core.managers;

import com.balugaq.bukkitbackdoor.core.listeners.ChatListener;
import com.balugaq.bukkitbackdoor.core.listeners.DefaultConfig;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for managing the listeners of the plugin.
 *
 * @author balugaq
 * @since 1.0
 */
@Getter
public class ListenerManager {
    private final JavaPlugin plugin;

    @Nonnull
    List<Listener> listeners = new ArrayList<>();

    public ListenerManager(JavaPlugin plugin) {
        this.plugin = plugin;
        listeners.add(new ChatListener());
        listeners.add(new DefaultConfig());
    }

    public void registerListeners() {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    public void unregisterListeners() {
        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
    }
}
