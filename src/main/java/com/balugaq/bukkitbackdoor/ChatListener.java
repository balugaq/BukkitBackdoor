package com.balugaq.bukkitbackdoor;

import com.balugaq.bukkitbackdoor.code.Code;
import com.balugaq.bukkitbackdoor.code.CodeParser;
import com.balugaq.bukkitbackdoor.code.Settings;
import jdk.jshell.JShell;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
    private final JShell jShell = JShell.create();

    public ChatListener() {
        // Load all Bukkit classes by default
        jShell.eval("import org.bukkit.*;");
    }

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

        loadReplacements(event);

        String rawCode = event.getMessage();
        runCode(player, CodeParser.parse(rawCode), event);
    }

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

        for (Map.Entry<String, Object> entry : replacements.entrySet()) {
            try {
                jShell.eval("var " + entry.getKey() + " = " + entry.getValue().toString());
            } catch (Exception e) {
                Logger.stackTrace(e);
                event.getPlayer().sendMessage(color("&cError loading replacement variable: " + entry.getKey()));
            }
        }
    }

    private void runCode(Player player, Code code, AsyncPlayerChatEvent event) {
        String finalCode = code.getCode();
        player.sendMessage(color(J_SHELL_PROMPT + finalCode));
        Settings settings = code.getSettings();
        finalCode = handleSettings(settings, finalCode);

        try {
            jShell.eval(finalCode);
        } catch (Throwable e) {
            Logger.stackTrace(e);
            player.sendMessage(color(ERROR_PREFIX + e.getMessage()));
            Arrays.stream(e.getStackTrace()).toList().forEach(element -> {
                player.sendMessage(color(STACK_TRACE_PREFIX + element.toString()));
            });
        }
    }

    private String handleSettings(Settings settings, String code) {
        if (settings.isTimeit()) {
            return generateTimedCode(code);
        }
        return code;
    }

    private String generateTimedCode(String code) {
        return "long __timeit_start = System.currentTimeMillis(); " +
                code + "; " +
                "player.sendMessage(\"Time Taken: \" + (System.currentTimeMillis() - __timeit_start) + \"ms\");";
    }

    private String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
