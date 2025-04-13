package com.balugaq.bukkitbackdoor.api.code;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class CodeParser {
    public static final Pattern SETTINGS_PATTERN = Pattern.compile(
            "settings:\\[([a-zA-Z0-9_]+=[a-zA-Z0-9_]+,?)+\\]"
    );

    @ParametersAreNonnullByDefault
    @Nonnull
    public static Code parse(String rawCode) {
        Settings settings = new Settings();
        if (rawCode.startsWith("settings:[")) {
            Matcher matcher = SETTINGS_PATTERN.matcher(rawCode);
            if (matcher.find()) {
                String string = matcher.group(0);
                parseSettings(string, settings);
                rawCode = rawCode.substring(matcher.end());
            }
        }

        return new Code(rawCode, settings);
    }

    /**
     * 解析设置字符串
     *
     * @param string 设置字符串，格式为：settings:[key1=value1,key2=value2]
     */
    public static void parseSettings(String string, Settings dest) {
        Map<String, String> map = new HashMap<>();
        string = string.substring(10, string.length() - 1);
        String[] pairs = string.split(",");
        for (String pair : pairs) {
            if (pair.isEmpty()) continue;
            String[] keyValue = pair.split("=", 2); // 限制分割次数，防止值中含等号
            if (keyValue.length != 2) continue;
            map.put(keyValue[0], keyValue[1]);
        }

        for (Field field : Settings.class.getDeclaredFields()) {
            String key = field.getName();
            if (map.containsKey(key)) {
                try {
                    String rawValue = map.get(key);
                    Class<?> type = field.getType();
                    Object value = parseValue(type, rawValue);
                    if (value == null) continue;
                    field.setAccessible(true);
                    field.set(dest, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Object parseValue(Class<?> type, String rawValue) {
        try {
            if (type == String.class) return rawValue;
            else if (type == int.class || type == Integer.class) return Integer.parseInt(rawValue);
            else if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(rawValue);
            else if (type == long.class || type == Long.class) return Long.parseLong(rawValue);
            else if (type == double.class || type == Double.class) return Double.parseDouble(rawValue);
            else if (type.isEnum()) return Enum.valueOf(type.asSubclass(Enum.class), rawValue);
            else return type.cast(rawValue);
        } catch (IllegalArgumentException | ClassCastException e) {
            // 转换失败，返回null
        }
        return null;
    }
}
