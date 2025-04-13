package com.balugaq.bukkitbackdoor.core.listeners;

import com.balugaq.bukkitbackdoor.api.code.BackdoorConstants;
import com.balugaq.bukkitbackdoor.api.code.Code;
import com.balugaq.bukkitbackdoor.api.code.CodeParser;
import com.balugaq.bukkitbackdoor.api.code.CustomLoaderDelegate;
import com.balugaq.bukkitbackdoor.api.code.Settings;
import com.balugaq.bukkitbackdoor.api.objects.Pair;
import com.balugaq.bukkitbackdoor.implementation.BukkitBackdoorPlugin;
import com.balugaq.bukkitbackdoor.utils.Logger;
import com.balugaq.bukkitbackdoor.utils.ReflectionUtils;
import com.balugaq.bukkitbackdoor.utils.Superhead;
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
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class DefaultConfig implements Listener {
    public static final Pattern ALIAS_PATTERN = Pattern.compile("([a-zA-Z0-9.,() +\\-*/;<>?:%\\n\\t])+=([a-zA-Z0-9.,() +\\-*/;<>?:%\\n\\t])+");
    public static final int MAX_REPLACEMENT_RETRY_TIMES = 10;
    @Getter
    private static final Map<String, String> replacements = new HashMap<>();
    @Getter
    private static final Set<String> imports = new HashSet<>();
    private static boolean firstLoad = true;

    @ParametersAreNonnullByDefault
    public static void addAlias(String s) {
        Matcher matcher = ALIAS_PATTERN.matcher(s);
        if (matcher.find()) {
            String a = s.split("=")[0];
            String b = s.split("=")[1];
            replacements.put(a, b);
        }
    }

    @ParametersAreNonnullByDefault
    public static void addImports(Class<?>... klasses) {
        for (Class<?> klass : klasses) {
            addImport(klass);
        }
    }

    @ParametersAreNonnullByDefault
    public static void addImport(Class<?> klass) {
        imports.add(klass.getName());
    }

    public static void loadAll() {
        Bukkit.getScheduler().runTaskLater(BukkitBackdoorPlugin.getInstance(), () -> {
            JShell jShell = (JShell) BackdoorConstants.getObject("jShell");
            for (String s : imports) {
                jShell.eval("import " + s);
            }
        }, 20L);
    }

    @ParametersAreNonnullByDefault
    public static String applyReplacements(String origin) {
        int retryTimes = 0;
        String before = origin;
        do {
            if (retryTimes >= MAX_REPLACEMENT_RETRY_TIMES) {
                break;
            }

            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                // 假设为 key = player, origin = player.getPlayer(player)
                String regex = entry.getKey() + "?([.,() +\\-*/;<>?:%\\n\\t])";
                // 将 player 替换为 value, 最后一个字符 ) 和 . 保留
                origin = origin.replaceFirst(regex, entry.getValue() + "$1");
            }
            retryTimes++;
        } while (!before.equals(origin));

        return origin;
    }

    @EventHandler
    @ParametersAreNonnullByDefault
    public void onLoad(PlayerJoinEvent event) {
        if (!firstLoad) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isOp()) {
            return;
        }

        firstLoad = false;

        Logger.log("DefaultConfig loading");
        addImports(
                Bukkit.class, Server.class, BackdoorConstants.class, Player.class,
                Block.class, Location.class, World.class, FluidCollisionMode.class,
                JShell.class, BukkitBackdoorPlugin.class, List.class, Map.class,
                Set.class, HashMap.class, HashSet.class, Pattern.class,
                Matcher.class, Collection.class, Collections.class, Preconditions.class,
                CodeParser.class, Code.class, CustomLoaderDelegate.class,
                Settings.class, ChatListener.class, DefaultConfig.class, Superhead.class,
                BukkitScheduler.class, PluginManager.class, ItemStack.class, Material.class,
                Recipe.class, YamlConfiguration.class, File.class, Files.class,
                Path.class, Paths.class, Class.class, Runnable.class,
                Thread.class, Consumer.class, Function.class, Supplier.class,
                Pair.class, ReflectionUtils.class, Field.class, Method.class,
                Constructor.class, MethodHandle.class, MethodHandles.class, VarHandle.class,
                MethodHandles.Lookup.class
        );
        /*
         * Simplified macro definitions
         * Variables definition:
         *     varName=varValue
         * ex:
         *     server=Bukkit.getServer()
         *
         * Function definition:
         *     funcName=funcQuote
         * ex:
         *     alias=DefaultConfig.addAlias
         */
        addAlias("server=Bukkit.getServer()");
        addAlias("BC=BackdoorConstants");
        addAlias("player=((Player) BackdoorConstants.getObject(\"player\"))");
        addAlias("loc=player.getLocation()");
        addAlias("block=loc.getBlock()");
        addAlias("world=loc.getWorld()");
        addAlias("lookingBlock=player.getTargetExact(16, FluidCollisionMode.NEVER)");
        addAlias("shell=((JShell) BackdoorConstants.getObject(\"jShell\"))");
        addAlias("plugin=BukkitBackdoor.getInstance()");
        addAlias("alias=DefaultConfig.addAlias");
        addAlias("async=Bukkit.getScheduler()");
        addAlias("pluginManager=Bukkit.getPluginManager()");
        addAlias("sout=System.out.println");
        addAlias("\\=/");

        loadAll();
    }
}
