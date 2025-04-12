package com.balugaq.bukkitbackdoor;

import com.balugaq.bukkitbackdoor.code.BackdoorConstants;
import com.balugaq.bukkitbackdoor.code.Code;
import com.balugaq.bukkitbackdoor.code.CodeParser;
import com.balugaq.bukkitbackdoor.code.CustomLoaderDelegate;
import com.balugaq.bukkitbackdoor.code.Settings;
import com.google.common.base.Preconditions;
import jdk.jshell.JShell;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class DefaultConfig implements Listener {
    public static final Pattern ALIAS_PATTERN = Pattern.compile("[a-zA-Z0-9]+=\\w+");
    public static final Map<String, String> replacements = new HashMap<>();
    public static final Set<String> imports = new HashSet<>();

    @EventHandler
    @ParametersAreNonnullByDefault
    public void onLoad(ServerLoadEvent event) {
        addImports(
                Bukkit.class, Server.class, BackdoorConstants.class, Player.class,
                Block.class, Location.class, World.class, FluidCollisionMode.class,
                JShell.class, BukkitBackdoor.class, List.class, Map.class,
                Set.class, HashMap.class, HashSet.class, Pattern.class,
                Matcher.class, Collection.class, Collections.class, Preconditions.class,
                Getter.class, Setter.class, Data.class, NoArgsConstructor.class,
                AllArgsConstructor.class, Nonnull.class, Nullable.class, ParametersAreNonnullByDefault.class,
                ParametersAreNullableByDefault.class, CodeParser.class, Code.class, CustomLoaderDelegate.class,
                Settings.class, ChatListener.class, DefaultConfig.class, Superhead.class,
                BukkitScheduler.class, PluginManager.class);
        // Simplified macro definitions
        addAlias("server=Bukkit.getServer()");
        addAlias("player=(Player) BackdoorConstants.getObject(\"player\")");
        addAlias("loc=player.getLocation()");
        addAlias("block=loc.getBlock()");
        addAlias("world=loc.getWorld()");
        addAlias("lookingBlock=player.getTargetExact(16, FluidCollisionMode.NEVER)");
        addAlias("shell=(JShell) JShell.getObject(\"jShell\")");
        addAlias("plugin=BukkitBackdoor.getInstance()");
        addAlias("alias=DefaultConfig.addAlias");
        addAlias("task=Bukkit.getScheduler()");
        addAlias("pluginManager=Bukkit.getPluginManager()");

        loadAll();
    }

    @ParametersAreNonnullByDefault
    public void addAlias(String s) {
        Matcher matcher = ALIAS_PATTERN.matcher(s);
        if (matcher.find()) {
            String a = s.split("=")[0];
            String b = s.split("=")[1];
            replacements.put(a, b);
        }
    }

    @ParametersAreNonnullByDefault
    public void addImports(Class<?>... klasses) {
        for (Class<?> klass : klasses) {
            addImport(klass);
        }
    }

    @ParametersAreNonnullByDefault
    public void addImport(Class<?> klass) {
        imports.add(klass.getName());
    }

    public void loadAll() {
        JShell jShell = (JShell) BackdoorConstants.getObject("jShell");
        for (String s : imports) {
            jShell.eval("import " + s);
        }
    }

    @ParametersAreNonnullByDefault
    public static String applyReplacements(String origin) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            origin = origin.replace(entry.getKey(), entry.getValue());
        }

        return origin;
    }
}
