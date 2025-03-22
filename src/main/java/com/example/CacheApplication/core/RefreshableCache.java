package com.example.CacheApplication.core;

public interface RefreshableCache<K, V> {
    Runnable getAutoRefreshRunnable();
    void updateCacheValue(K key);
}
