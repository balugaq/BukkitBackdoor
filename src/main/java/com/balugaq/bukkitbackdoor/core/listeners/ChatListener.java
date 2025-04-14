package com.balugaq.bukkitbackdoor.core.listeners;

import com.balugaq.bukkitbackdoor.api.code.BackdoorConstants;
import com.balugaq.bukkitbackdoor.api.code.CodeParser;
import com.balugaq.bukkitbackdoor.api.code.CodeRunner;
import com.balugaq.bukkitbackdoor.utils.StringUtils;
import lombok.Getter;
import org.bukkit.FluidCollisionMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class ChatListener implements Listener {
    @Getter
    private static final Set<UUID> jShellingPlayers = new HashSet<>();

    @ParametersAreNonnullByDefault
    private static void loadReplacements(AsyncPlayerChatEvent event) {
        BackdoorConstants.setMapping("currentms", System.currentTimeMillis());
        BackdoorConstants.setMapping("currentns", System.nanoTime());
        BackdoorConstants.setMapping("event", event);
        BackdoorConstants.setMapping("player", event.getPlayer());
        BackdoorConstants.setMapping("message", event.getMessage());
        BackdoorConstants.setMapping("name", event.getPlayer().getName());
        BackdoorConstants.setMapping("world", event.getPlayer().getLocation().getWorld());
        BackdoorConstants.setMapping("block", event.getPlayer().getLocation().getBlock());
        BackdoorConstants.setMapping("lookBlock", event.getPlayer().getTargetBlockExact(16, FluidCollisionMode.NEVER));

        int i = 0;
        for (ItemStack itemStack : event.getPlayer().getInventory().getContents()) {
            BackdoorConstants.setMapping("slot" + i, itemStack);
            i++;
        }
    }

    public static void runCode(@Nullable Cancellable event, CommandSender sender, UUID uuid, String message) {
        if (!jShellingPlayers.contains(uuid)) {
            if (message.equals("jshell")) {
                jShellingPlayers.add(uuid);
                sender.sendMessage(StringUtils.J_SHELL_START_MESSAGE);
                if (event != null) {
                    event.setCancelled(true);
                }
                return;
            }
            // Pass the message if not in JShell mode
            return;
        }

        if (message.equals("!!jshell")) {
            // exit jShell
            jShellingPlayers.remove(uuid);
            sender.sendMessage(StringUtils.J_SHELL_EXIT_MESSAGE);
            if (event != null) {
                event.setCancelled(true);
            }
            return;
        }

        if (event != null) {
            event.setCancelled(true);
        }

        if (event instanceof AsyncPlayerChatEvent apce) {
            loadReplacements(apce);
        }

        CodeRunner.runCode(CodeRunner.getJShell(), sender, CodeParser.parse(message));
    }

    @ParametersAreNonnullByDefault
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().isOp()) {
            return;
        }

        Player player = event.getPlayer();
        runCode(event, player, player.getUniqueId(), event.getMessage());
    }

}
