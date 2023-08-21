package com.un1ink.compress;

import com.un1ink.extension.SPI;

/** 压缩工具类接口 */
@SPI
public interface Compress {
    byte[] compress(byte[] bytes);
    byte[] decompress(byte[] bytes);
}
