package com.balugaq.bukkitbackdoor.api.code;

import com.balugaq.bukkitbackdoor.api.objects.enums.CommandEnum;
import com.balugaq.bukkitbackdoor.core.listeners.DefaultConfig;
import com.balugaq.bukkitbackdoor.utils.Logger;
import com.balugaq.bukkitbackdoor.utils.StringUtils;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@UtilityClass
public class CodeRunner {
    public static final Random RANDOM = new Random();

    @ParametersAreNonnullByDefault
    @Nonnull
    public static String handleSettings(Settings settings, String code) {
        if (settings.isTimeit()) {
            code = generateTimedCode(code);
        }
        if (settings.isSync()) {
            code = generateSynchronizedCode(code);
        }
        return code;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    public static String generateTimedCode(String code) {
        int identifier = RANDOM.nextInt(0, Integer.MAX_VALUE);
        return "long __timeit_start_" + identifier + " = System.currentTimeMillis(); " +
                code + "; " +
                "player.sendMessage(\"Time Taken: \" + (System.currentTimeMillis() - __timeit_start_" + identifier + ") + \"ms\");";
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    public static String generateSynchronizedCode(String code) {
        return "async.runTask(plugin, () -> {" + code + ";})";
    }

    @ParametersAreNonnullByDefault
    public static void runCode(JShell jShell, Player player, Code code) {
        String finalCode = code.getCode();
        player.sendMessage(StringUtils.color(StringUtils.J_SHELL_PROMPT + finalCode));
        if (CommandEnum.matches(finalCode, player)) {
            return;
        }

        Settings settings = code.getSettings();
        finalCode = handleSettings(settings, finalCode);
        finalCode = DefaultConfig.applyReplacements(finalCode);
        finalCode += ";";

        try {
            check(jShell.eval(finalCode));
        } catch (Throwable e) {
            Logger.stackTrace(e);
            player.sendMessage(StringUtils.color(StringUtils.ERROR_PREFIX + e.getMessage()));
            Arrays.stream(e.getStackTrace()).toList().forEach(element -> player.sendMessage(StringUtils.color(StringUtils.STACK_TRACE_PREFIX + element.toString())));
        }
    }

    @ParametersAreNonnullByDefault
    public static void check(List<SnippetEvent> events) {
        events.forEach(event -> {
            switch (event.status()) {
                case REJECTED -> {
                    // compile failed
                    Logger.error(StringUtils.COMPILE_FAILED_PREFIX + event);
                }
                case OVERWRITTEN -> {
                    switch (event.snippet().kind()) {
                        case IMPORT -> {
                            // over-import
                            Logger.warn(StringUtils.OVER_IMPORT_PREFIX + event);
                        }
                        case VAR -> {
                            // over-define
                            Logger.warn(StringUtils.OVER_DEFINE_PREFIX + event);
                        }
                    }
                }
            }
            if (event.exception() != null) {
                Logger.error(StringUtils.ERROR_PREFIX + event);
            }
        });
    }

}
