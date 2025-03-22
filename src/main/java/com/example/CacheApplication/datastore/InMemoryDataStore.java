package com.example.CacheApplication.datastore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryDataStore<K, V> implements DataStore<K, V> {
    private final ConcurrentHashMap<K, V> store;

    public InMemoryDataStore(ConcurrentHashMap<K, V> store) {
        this.store = store;
    }

    @Override
    public void put(K key, V value) {
        store.put(key, value);
    }

    @Override
    public V get(K key) {
        return store.get(key);
    }

    @Override
    public void remove(K key) {
        store.remove(key);
    }

    @Override
    public Iterable<K> getTopKeys(int limit) {
        return store.keySet().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
