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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

@UtilityClass
public class CodeRunner {
    public static final Random RANDOM = new Random();
    public static final List<String> PRE_COMMITS = BukkitBackdoorPlugin.getInstance().getConfigManager().getStringList("pre-commit");
    public static final List<String> POST_COMMITS = BukkitBackdoorPlugin.getInstance().getConfigManager().getStringList("post-commit");
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

        List<String> extendPaths = BukkitBackdoorPlugin.getInstance().getConfigManager().getStringList("classpath.jars.folders");
        for (String extendPath : extendPaths) {
            boolean deep = false;
            if (extendPath.startsWith("deep:")) {
                deep = true;
                extendPath = extendPath.substring(5);
            }

            FileUtils.forEachFiles(
                    getPath(BukkitBackdoorPlugin.getInstance().getDataFolder().getAbsoluteFile().toPath(), extendPath).normalize().toFile(),
                    ".jar",
                    path -> {
                        try {
                            loadClasspath(jShell, path.toString());
                        } catch (Throwable e) {
                            Logger.stackTrace(e);
                        }
                    }, deep);
        }

        List<String> singleJars = BukkitBackdoorPlugin.getInstance().getConfigManager().getStringList("classpath.jars.singles");
        for (String jarPath : singleJars) {
            if ("this".equals(jarPath)) {
                String th = BukkitBackdoorPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                th = URLDecoder.decode(th, StandardCharsets.UTF_8);
                th = th.substring(1);
                loadClasspath(jShell, th);
                continue;
            }

            loadClasspath(jShell, getPath(BukkitBackdoorPlugin.getInstance().getDataFolder().getAbsoluteFile().toPath(), jarPath).normalize().toString());
        }

        List<String> customs = BukkitBackdoorPlugin.getInstance().getConfigManager().getStringList("classpath.customs");
        for (String custom : customs) {
            loadClasspath(jShell, custom);
        }

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
        } else {
            code = generateAsynchronizedCode(code);
        }
        return code;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    public static String multilineSupport(String code) {
        return "((Runnable) (() -> {" + code + ";})).run()";
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    public static String generateTimedCode(String code) {
        int identifier = RANDOM.nextInt(0, Integer.MAX_VALUE);
        return "long __timeit_start_" + identifier + " = System.nanoTime(); " +
                code + "; " +
                "Logger.log(\"Time Taken: \" + (System.nanoTime() - __timeit_start_" + identifier + ") / 1_000_000D + \"ms\");";
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    public static String generateSynchronizedCode(String code) {
        return "async.runTask(plugin, () -> {" + code + ";})";
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    public static String generateAsynchronizedCode(String code) {
        return "async.runTaskAsynchronously(plugin, () -> {" + code + ";})";
    }

    @ParametersAreNonnullByDefault
    public static void runCode(JShell jShell, CommandSender sender, Code code) {
        String finalCode = code.getCode();
        String displayCode = finalCode;
        if (CommandEnum.matches(finalCode, sender)) {
            return;
        }

        if (!finalCode.startsWith("import ")) { // Avoid replace imports
            Settings settings = code.getSettings();
            finalCode = multilineSupport(finalCode);
            finalCode = handleSettings(settings, finalCode);
            finalCode = DefaultConfig.applyReplacements(finalCode);
        }
        finalCode += ";";

        warpedCheck(jShell, PRE_COMMITS, sender, displayCode);
        warpedCheck(jShell, finalCode, sender, displayCode);
        warpedCheck(jShell, POST_COMMITS, sender, displayCode);
    }

    public static void warpedCheck(JShell jShell, List<String> codes, CommandSender sender, String displayCode) {
        for (String code : codes) {
            warpedCheck(jShell, code, sender, displayCode);
        }
    }

    public static void warpedCheck(JShell jShell, String finalCode, CommandSender sender, String displayCode) {
        Logger.debug(sender.getName() + " committed: " + displayCode);
        Logger.debug("Formatted as: " + finalCode);
        try {
            check(jShell.eval(finalCode), sender, displayCode);
        } catch (Throwable e) {
            Logger.stackTrace(e);
            sender.sendMessage(StringUtils.color(StringUtils.ERROR_PREFIX + e.getMessage()));
            Arrays.stream(e.getStackTrace()).toList().forEach(element -> sender.sendMessage(StringUtils.color(StringUtils.STACK_TRACE_PREFIX + element.toString())));
        }
    }

    @ParametersAreNonnullByDefault
    public static void check(List<SnippetEvent> events, CommandSender sender, String displayCode) {
        events.forEach(event -> {
            boolean sent = false;
            switch (event.status()) {
                case REJECTED -> {
                    // compile failed
                    Logger.error(StringUtils.COMPILE_FAILED_PREFIX + event);
                    sender.sendMessage(StringUtils.color("&c" + StringUtils.J_SHELL_PROMPT + StringUtils.COMPILE_FAILED_PREFIX + displayCode));
                    sent = true;
                }
                case OVERWRITTEN -> {
                    switch (event.snippet().kind()) {
                        case IMPORT -> {
                            // over-import
                            Logger.warn(StringUtils.OVER_IMPORT_PREFIX + event);
                            sender.sendMessage(StringUtils.color("&e" + StringUtils.J_SHELL_PROMPT + StringUtils.OVER_IMPORT_PREFIX + displayCode));
                            sent = true;
                        }
                        case VAR -> {
                            // over-define
                            Logger.warn(StringUtils.OVER_DEFINE_PREFIX + event);
                            sender.sendMessage(StringUtils.color("&e" + StringUtils.J_SHELL_PROMPT + StringUtils.OVER_DEFINE_PREFIX + displayCode));
                            sent = true;
                        }
                    }
                }
            }
            if (event.exception() != null) {
                Logger.error(StringUtils.ERROR_PREFIX + event);
                sender.sendMessage(StringUtils.color("&c" + StringUtils.J_SHELL_PROMPT + StringUtils.ERROR_PREFIX + displayCode));
                sender.sendMessage(event.exception().getMessage());
                sent = true;
            }

            if (!sent) {
                sender.sendMessage(StringUtils.color("&a" + StringUtils.J_SHELL_PROMPT + displayCode));
            }
        });
    }

    public static void loadClasspath(JShell jShell, String classpath) {
        try {
            jShell.addToClasspath(classpath);
            Logger.log(StringUtils.HOOKED_PREFIX + classpath);
        } catch (Throwable e) {
            Logger.stackTrace(e);
        }
    }

    public static Path getPath(Path original, String extendPath) {
        String[] parts = extendPath.split("/");
        for (String part : parts) {
            original = original.resolve(part);
        }
        return original;
    }

}
