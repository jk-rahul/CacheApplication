package com.example.CacheApplication.core;

public interface RefreshableCache<K, V> {
    Runnable getAutoRefreshRunnable();
    void updateValue(K key, V newValue);
}
