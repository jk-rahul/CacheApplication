package com.example.CacheApplication.expiration;

import com.example.CacheApplication.core.CacheEntry;

public class TimeSinceCreationExpiration<K, V> extends BaseExpirationPolicy<K, V> {
    public TimeSinceCreationExpiration(long ttlMillis) {
        super(ttlMillis);
    }

    @Override
    public boolean isExpired(CacheEntry<V> entry) {
        return System.currentTimeMillis() - entry.getCreationTime() > ttlMillis;
    }
}