package com.balugaq.bukkitbackdoor;

import com.balugaq.bukkitbackdoor.code.BackdoorConstants;
import com.balugaq.bukkitbackdoor.code.Code;
import com.balugaq.bukkitbackdoor.code.CodeParser;
import com.balugaq.bukkitbackdoor.code.CustomLoaderDelegate;
import com.balugaq.bukkitbackdoor.code.Settings;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import jdk.jshell.execution.LocalExecutionControl;
import jdk.jshell.execution.LocalExecutionControlProvider;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionEnv;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatListener implements Listener {
    private static final String J_SHELL_START_MESSAGE = "&aJShell started!";
    private static final String J_SHELL_PROMPT = "&cjshell> ";
    private static final String ERROR_PREFIX = "&cAn error occurred when using JShell: ";
    private static final String STACK_TRACE_PREFIX = "    &cat ";

    private final Set<UUID> jShellingPlayers = new HashSet<>();
    private final Map<String, Object> replacements = new HashMap<>();
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
        getAllFiles(
                BukkitBackdoor.getInstance().getDataFolder().getAbsoluteFile().toPath().resolveSibling("..").resolveSibling("..").resolve("libraries").normalize().toFile(),
                ".jar",
                path -> {
                    try {
                        Logger.log("Path: " + path.toString());
                        jShell.addToClasspath(path.toString());
                    } catch (Throwable e) {
                        Logger.stackTrace(e);
                    }
                });
        String th = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        th = URLDecoder.decode(th, StandardCharsets.UTF_8);
        th = th.substring(1);
        Logger.log("Path: " + th);
        jShell.addToClasspath(th);

        compile(jShell.eval("import org.bukkit.*;"));
        compile(jShell.eval("import org.bukkit.inventory.*;"));
        compile(jShell.eval("import org.bukkit.entity.*;"));
        BackdoorConstants.setObject("server", Bukkit.getServer());
        compile(jShell.eval("import com.balugaq.bukkitbackdoor.code.BackdoorConstants"));
        Logger.log(BackdoorConstants.keys());
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    private static String handleSettings(Settings settings, String code) {
        if (settings.isTimeit()) {
            return generateTimedCode(code);
        }
        return code;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    private static String generateTimedCode(String code) {
        return "long __timeit_start = System.currentTimeMillis(); " +
                code + "; " +
                "player.sendMessage(\"Time Taken: \" + (System.currentTimeMillis() - __timeit_start) + \"ms\");";
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    private static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
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
                player.sendMessage(color(J_SHELL_START_MESSAGE));
                event.setCancelled(true);
                return;
            }
            // Pass the message if not in JShell mode
            return;
        }

        if (event.getMessage().equals("!!jshell")) {
            // exit jShell
            jShellingPlayers.remove(playerId);
            player.sendMessage(color("&aJShell exited!"));
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        loadReplacements(event);

        String rawCode = event.getMessage();
        runCode(player, CodeParser.parse(rawCode));
    }

    @ParametersAreNonnullByDefault
    private void loadReplacements(AsyncPlayerChatEvent event) {
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
            BackdoorConstants.setObject(key, value);
        }
    }

    @ParametersAreNonnullByDefault
    private void runCode(Player player, Code code) {
        String finalCode = code.getCode();
        player.sendMessage(color(J_SHELL_PROMPT + finalCode));
        Settings settings = code.getSettings();
        finalCode = handleSettings(settings, finalCode);
        finalCode += ";";

        try {
            compile(jShell.eval(finalCode));
        } catch (Throwable e) {
            Logger.stackTrace(e);
            player.sendMessage(color(ERROR_PREFIX + e.getMessage()));
            Arrays.stream(e.getStackTrace()).toList().forEach(element -> player.sendMessage(color(STACK_TRACE_PREFIX + element.toString())));
        }
    }

    public void compile(List<SnippetEvent> events) {
        events.forEach(event -> Logger.log("Event: " + event));
    }

    public static void getAllFiles(File dir, String suffix, Consumer<Path> consumer) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    getAllFiles(file, suffix, consumer);
                } else {
                    if (file.getName().endsWith(suffix)) {
                        consumer.accept(file.toPath());
                    }
                }
            }
        } else {
            if (dir.getName().endsWith(suffix)) {
                consumer.accept(dir.toPath());
            }
        }
    }
}
