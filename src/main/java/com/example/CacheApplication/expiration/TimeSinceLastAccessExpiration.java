package com.example.CacheApplication.expiration;

import com.example.CacheApplication.core.CacheEntry;

public class TimeSinceLastAccessExpiration<K, V> extends BaseExpirationPolicy<K, V> {
    public TimeSinceLastAccessExpiration(long ttlMillis) {
        super(ttlMillis);
    }

    @Override
    public void updateAccessTimeForRead(CacheEntry<V> entry) {
        entry.updateLastAccessTime();
    }

    @Override
    public void updateAccessTimeForWrite(CacheEntry<V> entry) {
        entry.updateLastAccessTime();
    }
}