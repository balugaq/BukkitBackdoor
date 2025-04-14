package com.balugaq.bukkitbackdoor.api.objects.enums;

import com.balugaq.bukkitbackdoor.api.code.CodeParser;
import com.balugaq.bukkitbackdoor.api.code.CodeRunner;
import com.balugaq.bukkitbackdoor.core.listeners.DefaultConfig;
import com.balugaq.bukkitbackdoor.implementation.BukkitBackdoorPlugin;
import com.balugaq.bukkitbackdoor.utils.Constants;
import com.balugaq.bukkitbackdoor.utils.FileUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.function.Consumer;

@Getter
public enum CommandEnum {
    KEYWORDS("kws", (sender) -> {
        sender.sendMessage(DefaultConfig.getReplacements().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).toList().toArray(new String[0]));
    }),
    VERSION("v", (sender) -> {
        sender.sendMessage("BukkitBackdoor v" + BukkitBackdoorPlugin.getInstance().getDescription().getVersion());
    }),
    EXIT("exit", (sender) -> {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitBackdoorPlugin.getInstance(), () -> {
            if (sender instanceof Player player) {
                Bukkit.getScheduler().runTaskAsynchronously(BukkitBackdoorPlugin.getInstance(), () -> {
                    player.chat("!!jshell");
                });
            }
        });
    }),
    RUN("run", (sender) -> {
        String message = FileUtils.readFile(Constants.PACKED_FILE.toPath());
        CodeRunner.runCode(CodeRunner.getJShell(), sender, CodeParser.parse(message));
    });

    private final String keyword;
    private final Consumer<CommandSender> action;

    CommandEnum(String keyword, Consumer<CommandSender> action) {
        this.keyword = keyword;
        this.action = action;
    }

    public static boolean matches(String code, CommandSender sender) {
        for (CommandEnum commandEnum : values()) {
            if (code.startsWith("?" + commandEnum.keyword)) {
                commandEnum.action.accept(sender);
                return true;
            }
        }
        return false;
    }
}
