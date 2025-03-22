package com.example.CacheApplication.refreshpolicy;

import com.example.CacheApplication.core.RefreshableCache;

public interface RefreshPolicy<K, V> {
    void scheduleAutoRefresh(RefreshableCache<K, V> cache);
}
