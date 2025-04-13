package com.balugaq.bukkitbackdoor.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@UtilityClass
public class StringUtils {
    public static final String J_SHELL_PROMPT = color("JShell> ");
    public static final String ERROR_PREFIX = color("&cAn error occurred when using JShell: ");
    public static final String COMPILE_FAILED_PREFIX = color("Compile failed: ");
    public static final String OVER_IMPORT_PREFIX = color("Already imported: ");
    public static final String OVER_DEFINE_PREFIX = color("Already defined: ");
    public static final String STACK_TRACE_PREFIX = color("    &cat ");
    public static final String DEBUG_PREFIX = color("[Debug] ");
    public static final String J_SHELL_START_MESSAGE = color("&aJShell started!");
    public static final String J_SHELL_EXIT_MESSAGE = color("&aJShell exited!");
    public static final String HOOKED_PREFIX = color("Hooked: ");
    public static final String J_SHELL_READY = color("&aJShell Ready!");

    @ParametersAreNonnullByDefault
    @Nonnull
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
