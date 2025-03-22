package com.example.CacheApplication.writepolicy;

import com.example.CacheApplication.datastore.DataStore;
import com.example.CacheApplication.util.ScheduledExecutorUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class WriteBackPropagationPolicy<K, V> implements WritePropagationPolicy<K, V> {
    private final long flushInterval;
    private final Map<K, V> writeBuffer = new ConcurrentHashMap<>();
    private final DataStore<K, V> dataStore;

    public WriteBackPropagationPolicy(long flushInterval, DataStore<K, V> dataStore) {
        this.flushInterval = flushInterval;
        this.dataStore = dataStore;
    }

    @Override
    public void scheduleWrite() {
        ScheduledExecutorUtil.scheduleAtFixedRate(this::flushWrites, flushInterval, flushInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void write(K key, V value, DataStore<K, V> dataStore) {
        writeBuffer.put(key, value);
    }

    private void flushWrites() {
        writeBuffer.forEach((key, value) -> {
            dataStore.put(key, value);
            writeBuffer.remove(key);
        });
    }
}
