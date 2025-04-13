package com.balugaq.bukkitbackdoor.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@UtilityClass
public class StringUtils {
    public static final String J_SHELL_PROMPT = "&ajshell> ";
    public static final String ERROR_PREFIX = "&cAn error occurred when using JShell: ";
    public static final String COMPILE_FAILED_PREFIX = "&cCompile failed: ";
    public static final String OVER_IMPORT_PREFIX = "&cAlready imported: ";
    public static final String OVER_DEFINE_PREFIX = "&cAlready defined: ";
    public static final String STACK_TRACE_PREFIX = "    &cat ";
    public static final String J_SHELL_START_MESSAGE = "&aJShell started!";
    public static final String HOOKED_MESSAGE = "Hooked: ";

    @ParametersAreNonnullByDefault
    @Nonnull
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
