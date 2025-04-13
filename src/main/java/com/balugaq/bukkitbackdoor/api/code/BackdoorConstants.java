package com.balugaq.bukkitbackdoor.api.code;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
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
    public static Object getObject(String key) {
        return mapping.get(key);
    }

    @ParametersAreNonnullByDefault
    public static void setMapping(String key, Object value) {
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
}
