package com.example.CacheApplication.datastore;

public interface DataStore<K, V> {
    void put(K key, V value);
    V get(K key);
    void remove(K key);
    Iterable<K> getTopKeys(int limit);
}
