package com.balugaq.bukkitbackdoor;

public class Logger {
    public static java.util.logging.Logger logger = BukkitBackdoor.getInstance().getLogger();
    public static void log(Object... message) {
        for (Object obj : message) {
            log(String.valueOf(obj));
        }
    }

    public static void log(String message) {
        logger.info(message);
    }

    public static void warn(Object... message) {
        for (Object obj : message) {
            warn(String.valueOf(obj));
        }
    }

    public static void warn(String message) {
        logger.warning(message);
    }

    public static void error(Object... message) {
        for (Object obj : message) {
            error(String.valueOf(obj));
        }
    }

    public static void error(String message) {
        logger.severe(message);
    }

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
