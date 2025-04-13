package com.balugaq.bukkitbackdoor.utils;

import com.balugaq.bukkitbackdoor.implementation.BukkitBackdoorPlugin;
import lombok.experimental.UtilityClass;

import javax.annotation.ParametersAreNonnullByDefault;

@UtilityClass
public class Logger {
    public static final java.util.logging.Logger logger = BukkitBackdoorPlugin.getInstance().getLogger();

    @ParametersAreNonnullByDefault
    public static void log(Object... message) {
        for (Object obj : message) {
            log(String.valueOf(obj));
        }
    }

    @ParametersAreNonnullByDefault
    public static void log(String message) {
        logger.info(message);
    }

    @ParametersAreNonnullByDefault
    public static void warn(Object... message) {
        for (Object obj : message) {
            warn(String.valueOf(obj));
        }
    }

    @ParametersAreNonnullByDefault
    public static void warn(String message) {
        logger.warning(message);
    }

    @ParametersAreNonnullByDefault
    public static void error(Object... message) {
        for (Object obj : message) {
            error(String.valueOf(obj));
        }
    }

    @ParametersAreNonnullByDefault
    public static void error(String message) {
        logger.severe(message);
    }

    @ParametersAreNonnullByDefault
    public static void debug(Object... message) {
        for (Object obj : message) {
            debug(String.valueOf(obj));
        }
    }

    @ParametersAreNonnullByDefault
    public static void debug(String message) {
        if (BukkitBackdoorPlugin.getInstance().getConfigManager().getBoolean("debug")) {
            logger.info(StringUtils.DEBUG_PREFIX + message);
        }
    }

    @ParametersAreNonnullByDefault
    public static void stackTrace(Throwable e) {
        e.printStackTrace();
    }

    public static void boomStackTrace() {
        try {
            throw new Exception();
        } catch (Exception e) {
            stackTrace(e);
        }
    }
}
