package com.example.CacheApplication.expiration;

import com.example.CacheApplication.core.CacheEntry;

public class TimeSinceLastWriteExpiration<K, V> extends BaseExpirationPolicy<K, V> {
    public TimeSinceLastWriteExpiration(long ttlMillis) {
        super(ttlMillis);
    }

    @Override
    public void updateAccessTimeForWrite(CacheEntry<V> entry) {
        entry.updateLastAccessTime();
    }
}
