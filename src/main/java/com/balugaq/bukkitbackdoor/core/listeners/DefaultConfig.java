package com.balugaq.bukkitbackdoor.core.listeners;

import com.balugaq.bukkitbackdoor.api.code.CodeParser;
import com.balugaq.bukkitbackdoor.api.code.CodeRunner;
import com.balugaq.bukkitbackdoor.implementation.BukkitBackdoorPlugin;
import com.balugaq.bukkitbackdoor.utils.Logger;
import com.balugaq.bukkitbackdoor.utils.StringUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class DefaultConfig implements Listener {
    public static final Pattern ALIAS_PATTERN = Pattern.compile("([a-zA-Z0-9.,() +\\-*/;<>?:%\\n\\t])+=([a-zA-Z0-9.,() +\\-*/;<>?:%\\n\\t])+");
    public static final int MAX_REPLACEMENT_RETRY_TIMES = BukkitBackdoorPlugin.getInstance().getConfigManager().getInt("max-replacement-retry-times", 10);
    @Getter
    private static final Map<String, String> replacements = new HashMap<>();
    @Getter
    private static final List<String> imports = new ArrayList<>();
    private static boolean firstLoad = true;

    @ParametersAreNonnullByDefault
    public static void addAlias(String s) {
        Matcher matcher = ALIAS_PATTERN.matcher(s);
        if (matcher.find()) {
            String a = s.split("=")[0];
            String b = s.split("=")[1];
            replacements.put(a, b);
        }
    }

    @ParametersAreNonnullByDefault
    public static void addImports(Class<?>... klasses) {
        for (Class<?> klass : klasses) {
            addImport(klass);
        }
    }

    @ParametersAreNonnullByDefault
    public static void addImport(Class<?> klass) {
        imports.add(klass.getName());
    }

    public static void loadAll() {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitBackdoorPlugin.getInstance(), () -> {
            for (String s : imports) {
                runCode("import " + s);
            }

            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.isOp()) {
                    player.sendMessage(StringUtils.J_SHELL_READY);
                }
            });

            List<String> commands = BukkitBackdoorPlugin.getInstance().getConfigManager().getStringList("commands");
            for (String s : commands) {
                runCode(s);
            }

            runCode("?exit");
        });
    }

    @ParametersAreNonnullByDefault
    private static void runCode(String message) {
        CodeRunner.runCode(CodeRunner.getJShell(), Bukkit.getConsoleSender(), CodeParser.parse(message));
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    public static String applyReplacements(String origin) {
        int retryTimes = 0;
        String before = origin;
        do {
            if (retryTimes >= MAX_REPLACEMENT_RETRY_TIMES) {
                break;
            }

            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                // 假设为 key = player, origin = player.getPlayer(player)
                String regex = entry.getKey() + "([.,() +\\-*/;<>?:%\\n\\t])?";
                // 将 player 替换为 value, 最后一个字符 ) 和 . 保留
                origin = origin.replaceFirst(regex, entry.getValue() + "$1");
            }
            retryTimes++;
        } while (!before.equals(origin));

        return origin;
    }

    @EventHandler
    @ParametersAreNonnullByDefault
    public void onLoad(ServerLoadEvent event) {
        if (!firstLoad) {
            return;
        }

        firstLoad = false;

        List<String> imports = BukkitBackdoorPlugin.getInstance().getConfigManager().getStringList("imports");
        for (String s : imports) {
            try {
                addImport(Class.forName(s));
                Logger.log("Loaded class: " + s);
            } catch (ClassNotFoundException e) {
                Logger.error("Unknown class: " + s);
                Logger.stackTrace(e);
            }
        }

        /*
         * Simplified macro definitions
         * Variables definition:
         *     varName=varValue
         * ex:
         *     server=Bukkit.getServer()
         *
         * Function definition:
         *     funcName=funcQuote
         * ex:
         *     alias=DefaultConfig.addAlias
         */

        List<String> aliases = BukkitBackdoorPlugin.getInstance().getConfigManager().getStringList("aliases");
        for (String s : aliases) {
            addAlias(s);
        }

        loadAll();
    }
}
