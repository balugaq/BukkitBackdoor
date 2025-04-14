package com.balugaq.bukkitbackdoor.api.code;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@UtilityClass
public class BackdoorConstants {
    private static final Map<String, Object> mapping = new HashMap<>();

    @ParametersAreNonnullByDefault
    @Nonnull
    public static Object getObject0(String key) {
        return mapping.get(key);
    }

    public static void setMapping(@Nonnull String key, @Nullable Object value) {
        mapping.put(key, value);
    }

    @Nonnull
    public static Map<String, Object> getMapping() {
        return mapping;
    }

    @Nonnull
    public static Set<String> keys() {
        return mapping.keySet();
    }

    @ParametersAreNonnullByDefault
    @Nullable
    public static <T> T getObject(@Nonnull String key) {
        try {
            return (T) getObject0(key);
        } catch (ClassCastException e) {
            return null;
        }
    }
}
