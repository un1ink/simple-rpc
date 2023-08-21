package com.un1ink.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 压缩类型 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
    GZIP((byte) 1, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
