package com.un1ink.extension;

import com.un1ink.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
@Slf4j
public final class ExtensionLoader<T> {

    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADER_MAP = new ConcurrentHashMap<>(64);
    private static final Map<Class<?>, Object> extensionInstances = new ConcurrentHashMap<>(64);

    private final Class<?> type;
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     * 获取拓展加载器
     *
     * @param  type 服务类型
     * @return 拓展加载器
     * */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("ExtensionLoader: Type is null.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("ExtensionLoader: '" + type + "' is not an interface.");
        }
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("ExtensionLoader: '" + type + "' is not @SPI");
        }
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADER_MAP.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADER_MAP.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADER_MAP.get(type);
        }
        return extensionLoader;
    }

    /**
     * 根据服务名获取or创建拓展服务
     *
     * @param name 服务名
     * @return 服务对象
     * */
    public T getExtension(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // 双检锁创建服务单例对象
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 创建扩展服务
     *
     * @param name 服务名
     * */
    private T createExtension(String name) {
        log.info("load extension of name '" + name + "'.");
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }
        T instance = (T) extensionInstances.get(clazz);
        if (instance == null) {
            try {
                boolean flag = extensionInstances.containsKey(clazz);
                if(!flag) {
                    Object o = clazz.newInstance();
                    extensionInstances.putIfAbsent(clazz, clazz.newInstance());
                    instance = (T) extensionInstances.get(clazz);
                }

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    private Map<String, Class<?>> getExtensionClasses() {
        log.info("load extension classes.");
        Map<String, Class<?>> classes = cachedClasses.get();
        // 双检锁
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 加载服务目录，从配置文件中获取待加载服务
     *
     * @param extensionClasses 服务类集合
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        log.info("loading fileName: " + fileName);
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            } else {
                log.info("urls is null.");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 加载服务资源
     *
     * @param extensionClasses 服务类集合，待加载的服务类会被放入该集合
     * @param classLoader 类加载器，待加载的服务类会被该类加载器加载
     * @param resourceUrl 资源路径，配置文件路径
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // 去除注释
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        int ei = line.indexOf('=');
                        // ei左侧为服务名，ei右侧为服务类名
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        log.info("load extension of name '" + name + "' and class name '" + clazzName + "'.");
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
