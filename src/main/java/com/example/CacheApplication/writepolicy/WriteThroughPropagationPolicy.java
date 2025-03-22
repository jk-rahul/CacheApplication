package com.example.CacheApplication.writepolicy;

import com.example.CacheApplication.datastore.DataStore;

public class WriteThroughPropagationPolicy<K, V> implements WritePropagationPolicy<K, V> {
    @Override
    public void write(K key, V value, DataStore<K, V> dataStore) {
        dataStore.put(key, value);
    }
}
