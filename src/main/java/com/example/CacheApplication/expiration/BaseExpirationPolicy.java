package com.example.CacheApplication.expiration;

import com.example.CacheApplication.core.CacheEntry;
import com.example.CacheApplication.core.ExpirableCache;
import com.example.CacheApplication.util.ScheduledExecutorUtil;

import java.util.concurrent.TimeUnit;

/**
 * Abstract BaseExpirationPolicy implementing the common
 * scheduleCleanup() and isExpired({@link CacheEntry} entry) methods.
 *
 * @param <K>
 * @param <V>
 */
abstract class BaseExpirationPolicy<K, V> implements ExpirationPolicy<K, V> {
    protected final long ttlMillis;

    BaseExpirationPolicy(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    @Override
    public void scheduleAutoCleanup(ExpirableCache cache) {
        ScheduledExecutorUtil.scheduleWithFixedDelay(cache.getAutoExpirationRunnable(),
                ttlMillis, ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isExpired(CacheEntry<V> entry) {
        return System.currentTimeMillis() - entry.getLastAccessTime() > ttlMillis;
    }
}
