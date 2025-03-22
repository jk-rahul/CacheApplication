package com.example.CacheApplication.expiration;

import com.example.CacheApplication.core.CacheEntry;
import com.example.CacheApplication.core.ExpirableCache;

public interface ExpirationPolicy<K, V> {
    void scheduleAutoCleanup(ExpirableCache cache);

    default void updateAccessTimeForRead(CacheEntry<V> entry) {}

    default void updateAccessTimeForWrite(CacheEntry<V> entry) {}

    boolean isExpired(CacheEntry<V> entry);
}