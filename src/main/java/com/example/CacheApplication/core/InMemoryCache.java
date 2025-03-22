package com.example.CacheApplication.core;

import com.example.CacheApplication.refreshpolicy.RefreshPolicy;
import com.example.CacheApplication.util.ScheduledExecutorUtil;
import com.example.CacheApplication.writepolicy.WritePropagationPolicy;
import com.example.CacheApplication.datastore.DataStore;
import com.example.CacheApplication.expiration.ExpirationPolicy;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.Map;

/**
 * {@link InMemoryCache} capable of storing and retrieving key-value pairs.
 * Enabled with features and configurations including -
 *  maxCapacity limit with LRU Eviction from the cache
 *  {@link ExpirationPolicy} to automatically remove expired items from cache
 *  {@link DataStore} backing the cache for cache miss, load during creation and refresh
 *  {@link WritePropagationPolicy} to propagate updates in cached data to the backing datastore
 *  {@link RefreshPolicy} to auto refresh cache item after a duration from the datastore
 * See Unit Tests for InMemoryCache
 * @param <K>
 * @param <V>
 */
public class InMemoryCache<K, V> implements Cache<K, V>, RefreshableCache<K, V>, ExpirableCache  {
    private final Map<K, CacheEntry<V>> cache;
    private final DataStore<K, V> dataStore;
    private final ExpirationPolicy<K,V> expirationPolicy;
    private final WritePropagationPolicy<K, V> writePropagationPolicy;
    private final RefreshPolicy<K, V> refreshPolicy;

    /**
     * Constructor for InMemoryCache with config and backingDataStore as params.
     * Build the cache using config properties and datastore.
     * Setup scheduled tasks and load the cache from datastore as defined.
     * @param config
     * @param backingDataStore
     */
    public InMemoryCache(CacheConfiguration<K, V> config, DataStore<K, V> backingDataStore) {
        // get config properties
        this.expirationPolicy = config.getExpirationPolicy();
        this.writePropagationPolicy = config.getWritePropagationPolicy();
        this.refreshPolicy = config.getRefreshPolicy();

        this.dataStore = backingDataStore;
        // build cache object
        this.cache = new ConcurrentLinkedHashMap.Builder<K, CacheEntry<V>>()
                .maximumWeightedCapacity(config.getMaxCapacity())
                .build();

        // setup scheduled policy tasks - expiration, writePropagation, refresh
        expirationPolicy.scheduleAutoCleanup(this);
        writePropagationPolicy.scheduleWrite();
        refreshPolicy.scheduleAutoRefresh(this, dataStore);

        // load cache from data store
        if (config.isAsyncLoad()) {
            ScheduledExecutorUtil.executeAsync(() -> this.loadCacheFromDataStore(config.getMaxCapacity()));
        } else {
            loadCacheFromDataStore(config.getMaxCapacity());
        }
    }

    /**
     * Method to load cache from datastore. Datastore returns topKeys limit maxCapacity.
     * @param maxCapacity
     */
    private void loadCacheFromDataStore(int maxCapacity) {
        for (K key : dataStore.getTopKeys(maxCapacity)) {
            V value = dataStore.get(key);
            if (value != null) {
                put(key, value);
            }
        }
    }

    /**
     * Write entry to cache, propagate policy to datastore
     * and invoke expirationPolicy for updating access time.
     * @param key
     * @param value
     */
    @Override
    public void put(K key, V value) {
        writePropagationPolicy.write(key, value, dataStore);
        cache.compute(key, (k, existingEntry) -> {
            CacheEntry<V> newEntry = new CacheEntry<>(value);
            expirationPolicy.updateAccessTimeForWrite(newEntry);
            return newEntry;
        });
    }

    /**
     * Get value for key from cache. Retrieve from datastore if not present in cache
     * @param key
     * @return <V>
     */
    @Override
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            V value = dataStore.get(key);
            if (value != null) {
                put(key, value);
            }
            return value;
        }
        expirationPolicy.updateAccessTimeForRead(entry);
        return entry.getValue();
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    /**
     * Expire cache items runnable as per the {@link ExpirationPolicy} implementation
     * @return Runnable
     */
    @Override
    public Runnable getAutoExpirationRunnable() {
        return () -> cache.entrySet().removeIf(
                entry -> expirationPolicy.isExpired(entry.getValue()));
    }

    /**
     * Refresh cache items runnable as per the {@link RefreshPolicy} implementation
     * @return Runnable
     */
    @Override
    public Runnable getAutoRefreshRunnable() {
        return () -> cache.keySet().forEach(key -> refreshPolicy.refresh(key, dataStore, this));
    }

    /**
     * Update cache item with newValue retrieved as per the {@link RefreshPolicy} implementation
     * @param key
     * @param newValue
     */
    @Override
    public void updateValue(K key, V newValue) {
        if (newValue != null) {
            cache.compute(key, (k, existingEntry) -> new CacheEntry<>(newValue));
        }
    }

    public int getSize() {
        return cache.size();
    }
}
