package com.example.CacheApplication.refreshpolicy;

import com.example.CacheApplication.core.RefreshableCache;
import com.example.CacheApplication.datastore.DataStore;
import com.example.CacheApplication.util.ScheduledExecutorUtil;

import java.util.concurrent.TimeUnit;

/**
 * Policy to schedule refreshing cache items at a fixed duration and
 * overwrite with update value from the backing {@link DataStore} datastore
 * @param <K>
 * @param <V>
 */
public class FixedDurationRefreshPolicy<K, V> implements RefreshPolicy<K, V> {
    private final long refreshPeriodMillis;

    public FixedDurationRefreshPolicy(long refreshPeriodMillis) {
        this.refreshPeriodMillis = refreshPeriodMillis;
    }

    @Override
    public void refresh(K key, DataStore<K, V> dataStore, RefreshableCache<K, V> cache) {
        cache.updateValue(key, dataStore.get(key));
    }

    @Override
    public void scheduleAutoRefresh(RefreshableCache<K, V> cache, DataStore<K, V> dataStore) {
        ScheduledExecutorUtil.scheduleAtFixedRate(cache.getAutoRefreshRunnable(),
                refreshPeriodMillis, refreshPeriodMillis, TimeUnit.MILLISECONDS);
    }
}