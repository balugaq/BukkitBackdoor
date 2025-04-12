package com.balugaq.bukkitbackdoor.code;

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
            "settings:\\[(([a-zA-Z0-9_]+=[a-zA-Z0-9_]),?)+\\]"
    );

    @ParametersAreNonnullByDefault
    @Nonnull
    public static Code parse(String rawCode) {
        Matcher matcher = SETTINGS_PATTERN.matcher(rawCode);
        Settings settings = new Settings();
        if (matcher.find()) {
            String string = matcher.group(0);
            parseSettings(string, settings);
            rawCode = rawCode.substring(matcher.end());
        }

        return new Code(rawCode, settings);
    }

    /**
     * 解析设置字符串
     *
     * @param string 设置字符串，格式为：settings:[key1=value1,key2=value2]
     */
    @ParametersAreNonnullByDefault
    public static void parseSettings(String string, Settings dest) {
        Map<String, String> map = new HashMap<>();
        string = string.substring(8, string.length() - 1);
        String[] pairs = string.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            map.put(keyValue[0], keyValue[1]);
        }

        for (Field field : Settings.class.getDeclaredFields()) {
            String key = field.getName();
            if (map.containsKey(key)) {
                try {
                    String rawValue = map.get(key);
                    try {
                        Object value = field.getType().cast(rawValue);
                        field.set(dest, value);
                    } catch (ClassCastException ignored) {
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
