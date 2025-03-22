package com.example.CacheApplication.core;

import com.example.CacheApplication.datastore.DataStore;
import com.example.CacheApplication.expiration.ExpirationPolicy;
import com.example.CacheApplication.refreshpolicy.RefreshPolicy;
import com.example.CacheApplication.writepolicy.WritePropagationPolicy;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
@Builder
public class CacheConfiguration<K, V> {
    @Builder.Default
    private final int maxCapacity = 10;

    private final ExpirationPolicy<K, V> expirationPolicy;
    private final WritePropagationPolicy<K, V> writePropagationPolicy;
    private final RefreshPolicy<K, V> refreshPolicy;
    private final DataStore<K, V> dataStore;

    @Builder.Default
    private final boolean asyncLoad = false;

    private CacheConfiguration(int maxCapacity, ExpirationPolicy<K, V> expirationPolicy,
                               WritePropagationPolicy<K, V> writePropagationPolicy,
                               RefreshPolicy<K, V> refreshPolicy, DataStore<K, V> dataStore,
                               boolean asyncLoad) {
        if (maxCapacity > 0) {
            this.maxCapacity = maxCapacity;
        } else {
            throw new IllegalArgumentException("Max capacity must be greater than zero.");
        }
        this.expirationPolicy = Objects.requireNonNull(expirationPolicy, "Expiration policy cannot be null.");
        this.writePropagationPolicy = Objects.requireNonNull(writePropagationPolicy, "Write propagation policy cannot be null.");
        this.refreshPolicy = Objects.requireNonNull(refreshPolicy, "Refresh policy cannot be null.");
        this.dataStore = Objects.requireNonNull(dataStore, "Backing store cannot be null.");
        this.asyncLoad = asyncLoad;
    }
}
