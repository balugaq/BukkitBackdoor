package com.balugaq.bukkitbackdoor.api.code;

import com.balugaq.bukkitbackdoor.api.objects.enums.CommandEnum;
import com.balugaq.bukkitbackdoor.core.listeners.DefaultConfig;
import com.balugaq.bukkitbackdoor.implementation.BukkitBackdoorPlugin;
import com.balugaq.bukkitbackdoor.utils.FileUtils;
import com.balugaq.bukkitbackdoor.utils.Logger;
import com.balugaq.bukkitbackdoor.utils.StringUtils;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import jdk.jshell.execution.LocalExecutionControl;
import jdk.jshell.execution.LocalExecutionControlProvider;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionEnv;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

@UtilityClass
public class CodeRunner {
    public static final Random RANDOM = new Random();
    @Getter
    public static final JShell jShell;

    static {
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
        String th = BukkitBackdoorPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        th = URLDecoder.decode(th, StandardCharsets.UTF_8);
        th = th.substring(1);
        Logger.log(StringUtils.HOOKED_PREFIX + th);
        // todo: load all jars with config-controlled folders / files (include, exclude)
        jShell.addToClasspath(th);

        BackdoorConstants.setMapping("server", Bukkit.getServer());
        BackdoorConstants.setMapping("jshell", jShell);
    }

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
    public static void runCode(JShell jShell, CommandSender sender, Code code) {
        String finalCode = code.getCode();
        sender.sendMessage(StringUtils.color(StringUtils.J_SHELL_PROMPT + finalCode));
        if (CommandEnum.matches(finalCode, sender)) {
            return;
        }

        if (!finalCode.startsWith("import ")) { // Avoid replace imports
            Settings settings = code.getSettings();
            finalCode = handleSettings(settings, finalCode);
            finalCode = DefaultConfig.applyReplacements(finalCode);
        }
        finalCode += ";";

        try {
            check(jShell.eval(finalCode));
        } catch (Throwable e) {
            Logger.stackTrace(e);
            sender.sendMessage(StringUtils.color(StringUtils.ERROR_PREFIX + e.getMessage()));
            Arrays.stream(e.getStackTrace()).toList().forEach(element -> sender.sendMessage(StringUtils.color(StringUtils.STACK_TRACE_PREFIX + element.toString())));
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
