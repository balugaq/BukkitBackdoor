package com.balugaq.bukkitbackdoor.api.objects.enums;

import com.balugaq.bukkitbackdoor.core.listeners.DefaultConfig;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@Getter
public enum CommandEnum {
    KEYWORDS("kws", (player) -> {
        player.sendMessage(DefaultConfig.getReplacements().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).toList().toArray(new String[0]));
    }),
    VERSION("v", (player) -> {
        player.sendMessage("BukkitBackdoor v${project.version}");
    }),
    EXIT("exit", (player) -> {
        player.chat("!!jshell");
    });

    private final String keyword;
    private final Consumer<Player> action;

    CommandEnum(String keyword, Consumer<Player> action) {
        this.keyword = keyword;
        this.action = action;
    }

    public static boolean matches(String code, Player player) {
        for (CommandEnum commandEnum : values()) {
            if (code.startsWith("?" + commandEnum.keyword)) {
                commandEnum.action.accept(player);
                return true;
            }
        }
        return false;
    }
}
