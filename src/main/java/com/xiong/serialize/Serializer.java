package com.xiong.serialize;

import com.xiong.extension.SPI;

@SPI
public interface Serializer {
    byte[] serialize(Object object);
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
