package com.example.CacheApplication.refreshpolicy;

import com.example.CacheApplication.core.RefreshableCache;
import com.example.CacheApplication.datastore.DataStore;

public interface RefreshPolicy<K, V> {
    void refresh(K key, DataStore<K, V> dataStore, RefreshableCache<K, V> cache);
    void scheduleAutoRefresh(RefreshableCache<K, V> cache, DataStore<K, V> dataStore);
}
