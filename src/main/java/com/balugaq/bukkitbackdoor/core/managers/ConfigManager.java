package com.balugaq.bukkitbackdoor.core.managers;

import com.balugaq.bukkitbackdoor.utils.Logger;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for managing the configuration of the plugin.
 *
 * @author balugaq
 */
public class ConfigManager {
    private final @Nonnull JavaPlugin plugin;

    public ConfigManager(@Nonnull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setupDefaultConfig() {
        // config.yml
        final InputStream inputStream = plugin.getResource("config.yml");
        final File existingFile = new File(plugin.getDataFolder(), "config.yml");

        if (inputStream == null) {
            return;
        }

        if (!existingFile.exists()) {
            try {
                if (!existingFile.getParentFile().exists()) {
                    existingFile.getParentFile().mkdirs();
                }
                existingFile.createNewFile();
            } catch (IOException e) {
                Logger.stackTrace(e);
                return;
            }
        }

        final Reader reader = new InputStreamReader(inputStream);
        final FileConfiguration resourceConfig = YamlConfiguration.loadConfiguration(reader);
        final FileConfiguration existingConfig = YamlConfiguration.loadConfiguration(existingFile);

        for (String key : resourceConfig.getKeys(false)) {
            checkKey(existingConfig, resourceConfig, key);
        }

        try {
            existingConfig.save(existingFile);
        } catch (IOException e) {
            Logger.stackTrace(e);
        }
    }

    @ParametersAreNonnullByDefault
    private void checkKey(FileConfiguration existingConfig, FileConfiguration resourceConfig, String key) {
        final Object currentValue = existingConfig.get(key);
        final Object newValue = resourceConfig.get(key);
        if (newValue instanceof ConfigurationSection section) {
            for (String sectionKey : section.getKeys(false)) {
                checkKey(existingConfig, resourceConfig, key + "." + sectionKey);
            }
        } else if (currentValue == null) {
            existingConfig.set(key, newValue);
        }
    }

    public FileConfiguration getConfiguration() {
        return plugin.getConfig();
    }

    public List<String> getStringList(String key) {
        return getConfiguration().getStringList(key);
    }

    public boolean getBoolean(String key) {
        return getConfiguration().getBoolean(key);
    }

    public boolean getBoolean(String key, boolean def) {
        return getConfiguration().getBoolean(key, def);
    }

    public int getInt(String key) {
        return getConfiguration().getInt(key);
    }

    public int getInt(String key, int def) {
        return getConfiguration().getInt(key, def);
    }

    public String getString(String key) {
        return getConfiguration().getString(key);
    }

    public String getString(String key, String def) {
        return getConfiguration().getString(key, def);
    }

    public List<Integer> getIntegerList(String key) {
        return getConfiguration().getIntegerList(key);
    }

    public List<Boolean> getBooleanList(String key) {
        return getConfiguration().getBooleanList(key);
    }

    public List<String> getStringList(String key, List<String> def) {
        return getConfiguration().getStringList(key);
    }

    public List<Double> getDoubleList(String key) {
        return getConfiguration().getDoubleList(key);
    }

    public List<Float> getFloatList(String key) {
        return getConfiguration().getFloatList(key);
    }

    public List<Long> getLongList(String key) {
        return getConfiguration().getLongList(key);
    }

    public List<Byte> getByteList(String key) {
        return getConfiguration().getByteList(key);
    }

    public List<Character> getCharacterList(String key) {
        return getConfiguration().getCharacterList(key);
    }

    public List<Short> getShortList(String key) {
        return getConfiguration().getShortList(key);
    }

    public List<Map<?, ?>> getMapList(String key) {
        return getConfiguration().getMapList(key);
    }

    public void set(String key, Object value) {
        getConfiguration().set(key, value);
    }

    public double getDouble(String key) {
        return getConfiguration().getDouble(key);
    }

    public double getDouble(String key, double def) {
        return getConfiguration().getDouble(key, def);
    }

    public Color getColor(String key) {
        return getConfiguration().getColor(key);
    }

    public Color getColor(String key, Color def) {
        return getConfiguration().getColor(key, def);
    }

    public Location getLocation(String key) {
        return getConfiguration().getLocation(key);
    }

    public Location getLocation(String key, Location def) {
        return getConfiguration().getLocation(key, def);
    }

    public OfflinePlayer getOfflinePlayer(String key) {
        return getConfiguration().getOfflinePlayer(key);
    }

    public OfflinePlayer getOfflinePlayer(String key, OfflinePlayer def) {
        return getConfiguration().getOfflinePlayer(key, def);
    }

    public ItemStack getItemStack(String key) {
        return getConfiguration().getItemStack(key);
    }

    public ItemStack getItemStack(String key, ItemStack def) {
        return getConfiguration().getItemStack(key, def);
    }

    public Object getObject(String key, Class<?> clazz) {
        return getConfiguration().getObject(key, clazz);
    }

    public <T> T getObject(String key, Class<T> clazz, T def) {
        return getConfiguration().getObject(key, clazz, def);
    }

    public <T extends ConfigurationSerializable> T getMap(String key, Class<T> clazz) {
        return getConfiguration().getSerializable(key, clazz);
    }
}
