package com.example.CacheApplication.core;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;

@Getter
public class CacheEntry<V> {
    private final V value;
    private final long creationTime;
    private final AtomicLong lastAccessTime;

    public CacheEntry(V value) {
        this.value = value;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessTime = new AtomicLong(creationTime);
    }

    public void updateLastAccessTime() {
        lastAccessTime.set(System.currentTimeMillis());
    }

    public long getLastAccessTime() {
        return lastAccessTime.get();
    }
}
