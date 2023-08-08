package com.xiong.compress;

import com.xiong.extension.SPI;

@SPI
public interface Compress {
    byte[] compress(byte[] bytes);
    byte[] decompress(byte[] bytes);
}
