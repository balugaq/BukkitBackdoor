package com.balugaq.bukkitbackdoor.utils;

import com.balugaq.bukkitbackdoor.implementation.BukkitBackdoorPlugin;

import java.io.File;

public class Constants {
    public static final File PACKED_FILE = new File(BukkitBackdoorPlugin.getInstance().getDataFolder(), BukkitBackdoorPlugin.getInstance().getConfigManager().getString("packed-file"));
}
