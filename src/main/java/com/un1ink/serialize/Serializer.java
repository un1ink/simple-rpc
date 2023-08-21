package com.un1ink.serialize;

import com.un1ink.extension.SPI;

@SPI
public interface Serializer {
    byte[] serialize(Object object);
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
