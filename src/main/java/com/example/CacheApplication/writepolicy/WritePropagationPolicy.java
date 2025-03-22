package com.example.CacheApplication.writepolicy;

import com.example.CacheApplication.datastore.DataStore;

public interface WritePropagationPolicy<K, V>  {
    void write(K key, V value, DataStore<K, V> dataStore);
    default void scheduleWrite() {}
}
