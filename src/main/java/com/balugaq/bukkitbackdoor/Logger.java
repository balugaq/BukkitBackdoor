package com.balugaq.bukkitbackdoor;

import lombok.experimental.UtilityClass;

import javax.annotation.ParametersAreNonnullByDefault;

@UtilityClass
public class Logger {
    public static final java.util.logging.Logger logger = BukkitBackdoor.getInstance().getLogger();

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
