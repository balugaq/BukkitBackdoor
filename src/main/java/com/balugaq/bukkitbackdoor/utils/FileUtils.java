package com.balugaq.bukkitbackdoor.utils;

import lombok.experimental.UtilityClass;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

@UtilityClass
public class FileUtils {
    @ParametersAreNonnullByDefault
    public static void forEachFiles(File dir, String suffix, Consumer<Path> consumer, boolean deep) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }

            for (File file : files) {
                if (deep && file.isDirectory()) {
                    forEachFiles(file, suffix, consumer, deep);
                } else {
                    if (file.getName().endsWith(suffix)) {
                        consumer.accept(file.toPath());
                    }
                }
            }
        } else {
            if (dir.getName().endsWith(suffix)) {
                consumer.accept(dir.toPath());
            }
        }
    }
}
