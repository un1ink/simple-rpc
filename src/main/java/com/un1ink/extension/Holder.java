package com.un1ink.extension;

/**
 * 用volatile修饰value，保证Holder的可见性
 *
 * @param <T>
 */
public class Holder <T>{
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
