package com.balugaq.bukkitbackdoor;

import com.balugaq.bukkitbackdoor.code.Code;
import com.balugaq.bukkitbackdoor.code.CodeParser;
import com.balugaq.bukkitbackdoor.code.Settings;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.VarSnippet;
import jdk.jshell.execution.FailOverExecutionControlProvider;
import jdk.jshell.execution.JdiExecutionControlProvider;
import jdk.jshell.execution.LocalExecutionControlProvider;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
                .executionEngine(new JdiExecutionControlProvider(), Map.of());

        jShell = builder.build();
        try {
            Class<?> clazz = Class.forName("org.bukkit.Bukkit");
            String jarPath = clazz.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();
            jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
            if (jarPath.startsWith("/")) {
                jarPath = jarPath.substring(1);
            }
            Logger.log("jarPath: " + jarPath);
            jShell.addToClasspath(jarPath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        compile(jShell.eval("import org.bukkit.*;"));
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

        // Can't affect at all, not passed an Object but a String
        for (Map.Entry<String, Object> entry : replacements.entrySet()) {
            try {
                jShell.varValue()
                compile(jShell.eval("var " + entry.getKey() + " = " + entry.getValue() + ";"));
            } catch (Exception e) {
                Logger.stackTrace(e);
                event.getPlayer().sendMessage(color("&cError loading replacement variable: " + entry.getKey()));
            }
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
            Arrays.stream(e.getStackTrace()).toList().forEach(element -> {
                player.sendMessage(color(STACK_TRACE_PREFIX + element.toString()));
            });
        }
    }

    public void compile(List<SnippetEvent> events) {
        events.forEach(event -> {
            Logger.log("Event: " + event);
        });
    }
}
