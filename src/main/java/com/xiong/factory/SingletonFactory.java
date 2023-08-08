package com.xiong.factory;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {
//    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private SingletonFactory(){
    }

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
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
    }
}
