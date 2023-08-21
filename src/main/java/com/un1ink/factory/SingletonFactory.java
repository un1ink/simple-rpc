package com.un1ink.factory;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 单例工厂，用于获取客户端或服务端等单例对象
 * */
public class SingletonFactory {
    /** 单例对象缓存池 */
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private SingletonFactory(){}

    /**
     * 通过反射获取实例对象
     * @param clazz 目标类
     * @return 实例对象
     * */
    public static <T> T getInstance(Class<T> clazz){
        if(clazz == null){
            throw new IllegalArgumentException();
        }
        String key = clazz.toString();
        if(OBJECT_MAP.containsKey(key)){
            return clazz.cast(OBJECT_MAP.get(key));
        } else {
            return clazz.cast(OBJECT_MAP.computeIfAbsent(key, k->{
                try {
                    clazz.newInstance();
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
    }
}
