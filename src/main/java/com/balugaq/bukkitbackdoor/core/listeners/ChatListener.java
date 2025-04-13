package com.balugaq.bukkitbackdoor.core.listeners;

import com.balugaq.bukkitbackdoor.api.code.BackdoorConstants;
import com.balugaq.bukkitbackdoor.api.code.CodeParser;
import com.balugaq.bukkitbackdoor.api.code.CodeRunner;
import com.balugaq.bukkitbackdoor.api.code.CustomLoaderDelegate;
import com.balugaq.bukkitbackdoor.implementation.BukkitBackdoorPlugin;
import com.balugaq.bukkitbackdoor.utils.FileUtils;
import com.balugaq.bukkitbackdoor.utils.Logger;
import com.balugaq.bukkitbackdoor.utils.StringUtils;
import jdk.jshell.JShell;
import jdk.jshell.execution.LocalExecutionControl;
import jdk.jshell.execution.LocalExecutionControlProvider;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionEnv;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ChatListener implements Listener {

    private static final Set<UUID> jShellingPlayers = new HashSet<>();
    private static final Map<String, Object> replacements = new HashMap<>();
    private final JShell jShell;

    public ChatListener() {
        // Load all Bukkit classes by default
        JShell.Builder builder = JShell.builder()
                .executionEngine(new LocalExecutionControlProvider() {
                    @Override
                    public ExecutionControl createExecutionControl(ExecutionEnv env, Map<String, String> parameters) {
                        return new LocalExecutionControl(new CustomLoaderDelegate(this.getClass().getClassLoader()));
                    }
                }, Map.of());

        jShell = builder.build();
        FileUtils.forEachFiles(
                BukkitBackdoorPlugin.getInstance().getDataFolder().getAbsoluteFile().toPath().resolveSibling("..").resolveSibling("..").resolve("libraries").normalize().toFile(),
                ".jar",
                path -> {
                    try {
                        Logger.log(StringUtils.HOOKED_PREFIX + path.toString());
                        jShell.addToClasspath(path.toString());
                    } catch (Throwable e) {
                        Logger.stackTrace(e);
                    }
                });
        String th = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        th = URLDecoder.decode(th, StandardCharsets.UTF_8);
        th = th.substring(1);
        Logger.log(StringUtils.HOOKED_PREFIX + th);
        // todo: load all jars with config-controlled folders / files (include, exclude)
        jShell.addToClasspath(th);

        BackdoorConstants.setMapping("server", Bukkit.getServer());
        BackdoorConstants.setMapping("jShell", jShell);
        Logger.log(BackdoorConstants.keys());
    }

    @ParametersAreNonnullByDefault
    private static void loadReplacements(AsyncPlayerChatEvent event) {
        replacements.clear(); // Clear previous replacements to avoid stale data

        replacements.put("event", event);
        replacements.put("player", event.getPlayer());
        replacements.put("message", event.getMessage());
        replacements.put("name", event.getPlayer().getName());
        replacements.put("world", event.getPlayer().getLocation().getWorld());
        replacements.put("block", event.getPlayer().getLocation().getBlock());
        replacements.put("lookBlock", event.getPlayer().getTargetBlockExact(16, FluidCollisionMode.NEVER));

        int i = 0;
        for (ItemStack itemStack : event.getPlayer().getInventory()) {
            if (itemStack != null) { // Avoid adding null values
                replacements.put("slot" + i, itemStack);
            }
            i++;
        }

        // rewrite with a custom class.
        for (Map.Entry<String, Object> entry : replacements.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            BackdoorConstants.setMapping(key, value);
        }
    }

    @ParametersAreNonnullByDefault
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().isOp()) {
            return;
        }

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!jShellingPlayers.contains(playerId)) {
            if (event.getMessage().equals("jshell")) {
                jShellingPlayers.add(playerId);
                player.sendMessage(StringUtils.color(StringUtils.J_SHELL_START_MESSAGE));
                event.setCancelled(true);
                return;
            }
            // Pass the message if not in JShell mode
            return;
        }

        if (event.getMessage().equals("!!jshell")) {
            // exit jShell
            jShellingPlayers.remove(playerId);
            player.sendMessage(StringUtils.color(StringUtils.J_SHELL_EXIT_MESSAGE));
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        loadReplacements(event);

        String rawCode = event.getMessage();
        CodeRunner.runCode(jShell, player, CodeParser.parse(rawCode));
    }

}
