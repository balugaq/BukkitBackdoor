package com.balugaq.bukkitbackdoor.api.code;

import jdk.jshell.execution.LoaderDelegate;
import jdk.jshell.spi.ExecutionControl.ClassBytecodes;
import jdk.jshell.spi.ExecutionControl.ClassInstallException;
import jdk.jshell.spi.ExecutionControl.InternalException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.CodeSource;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomLoaderDelegate implements LoaderDelegate {

    private final RemoteClassLoader loader;
    private final Map<String, Class<?>> klasses = new HashMap<>();

    @ParametersAreNonnullByDefault
    public CustomLoaderDelegate(ClassLoader parent) {
        this.loader = new RemoteClassLoader(parent);
        Thread.currentThread().setContextClassLoader(loader);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void load(ClassBytecodes[] cbcs)
            throws ClassInstallException {
        boolean[] loaded = new boolean[cbcs.length];
        try {
            for (ClassBytecodes cbc : cbcs) {
                loader.declare(cbc.name(), cbc.bytecodes());
            }
            for (int i = 0; i < cbcs.length; ++i) {
                ClassBytecodes cbc = cbcs[i];
                Class<?> klass = loader.loadClass(cbc.name());
                klasses.put(cbc.name(), klass);
                loaded[i] = true;

                klass.getDeclaredMethods();
            }
        } catch (Throwable ex) {
            throw new ClassInstallException("load: " + ex.getMessage(), loaded);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void classesRedefined(ClassBytecodes[] cbcs) {
        for (ClassBytecodes cbc : cbcs) {
            loader.declare(cbc.name(), cbc.bytecodes());
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void addToClasspath(String cp) throws InternalException {
        try {
            for (String path : cp.split(File.pathSeparator)) {
                loader.addURL(new File(path).toURI().toURL());
            }
        } catch (Exception ex) {
            throw new InternalException(ex.toString());
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    @Nullable
    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> klass = klasses.get(name);
        if (klass == null) {
            throw new ClassNotFoundException(name + " not found");
        } else {
            return klass;
        }
    }

    private static class RemoteClassLoader extends URLClassLoader {

        private final Map<String, ClassFile> classFiles = new HashMap<>();

        @ParametersAreNonnullByDefault
        RemoteClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
        }

        @ParametersAreNonnullByDefault
        void declare(String name, byte[] bytes) {
            classFiles.put(toResourceString(name), new ClassFile(bytes, System.currentTimeMillis()));
        }

        @Override
        @ParametersAreNonnullByDefault
        @Nullable
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            ClassFile file = classFiles.get(toResourceString(name));
            if (file == null) {
                return super.findClass(name);
            }
            return super.defineClass(name, file.data, 0, file.data.length, (CodeSource) null);
        }

        @Override
        @ParametersAreNonnullByDefault
        @Nullable
        public URL findResource(String name) {
            URL u = doFindResource(name);
            return u != null ? u : super.findResource(name);
        }

        @Override
        @ParametersAreNonnullByDefault
        @Nullable
        public Enumeration<URL> findResources(String name) throws IOException {
            URL u = doFindResource(name);
            Enumeration<URL> sup = super.findResources(name);

            if (u == null) {
                return sup;
            }

            List<URL> result = new ArrayList<>();

            while (sup.hasMoreElements()) {
                result.add(sup.nextElement());
            }

            result.add(u);

            return Collections.enumeration(result);
        }

        @ParametersAreNonnullByDefault
        @Nullable
        private URL doFindResource(String name) {
            if (classFiles.containsKey(name)) {
                try {
                    return URL.of(new URI("jshell", null, "/" + name, null),
                            new ResourceURLStreamHandler(name));
                } catch (MalformedURLException | URISyntaxException ex) {
                    throw new InternalError(ex);
                }
            }

            return null;
        }

        @ParametersAreNonnullByDefault
        @Nonnull
        private String toResourceString(String className) {
            return className.replace('.', '/') + ".class";
        }

        @ParametersAreNonnullByDefault
        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }

        private record ClassFile(byte[] data, long timestamp) {
        }

        private class ResourceURLStreamHandler extends URLStreamHandler {

            private final String name;

            @ParametersAreNonnullByDefault
            ResourceURLStreamHandler(String name) {
                this.name = name;
            }

            @Override
            @ParametersAreNonnullByDefault
            protected URLConnection openConnection(URL u) {
                return new URLConnection(u) {
                    private InputStream in;
                    private Map<String, List<String>> fields;
                    private List<String> fieldNames;

                    @Override
                    public void connect() {
                        if (connected) {
                            return;
                        }
                        connected = true;
                        ClassFile file = classFiles.get(name);
                        in = new ByteArrayInputStream(file.data);
                        fields = new LinkedHashMap<>();
                        fields.put("content-length", List.of(Integer.toString(file.data.length)));
                        Instant instant = new Date(file.timestamp).toInstant();
                        ZonedDateTime time = ZonedDateTime.ofInstant(instant, ZoneId.of("GMT"));
                        String timeStamp = DateTimeFormatter.RFC_1123_DATE_TIME.format(time);
                        fields.put("date", List.of(timeStamp));
                        fields.put("last-modified", List.of(timeStamp));
                        fieldNames = new ArrayList<>(fields.keySet());
                    }

                    @Override
                    @Nonnull
                    public InputStream getInputStream() {
                        connect();
                        return in;
                    }

                    @Override
                    @ParametersAreNonnullByDefault
                    @Nullable
                    public String getHeaderField(String name) {
                        connect();
                        return fields.getOrDefault(name, List.of())
                                .stream()
                                .findFirst()
                                .orElse(null);
                    }

                    @Override
                    @Nonnull
                    public Map<String, List<String>> getHeaderFields() {
                        connect();
                        return fields;
                    }

                    @Override
                    @Nullable
                    public String getHeaderFieldKey(int n) {
                        return n < fieldNames.size() ? fieldNames.get(n) : null;
                    }

                    @Override
                    @Nullable
                    public String getHeaderField(int n) {
                        String name = getHeaderFieldKey(n);

                        return name != null ? getHeaderField(name) : null;
                    }

                };
            }
        }
    }

}
