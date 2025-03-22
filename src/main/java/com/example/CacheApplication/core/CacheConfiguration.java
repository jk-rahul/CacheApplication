package com.example.CacheApplication.core;

import com.example.CacheApplication.expiration.ExpirationPolicy;
import com.example.CacheApplication.refreshpolicy.RefreshPolicy;
import com.example.CacheApplication.writepolicy.WritePropagationPolicy;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CacheConfiguration<K, V> {
    private final int maxCapacity;
    private final ExpirationPolicy<K, V> expirationPolicy;
    private final WritePropagationPolicy<K, V> writePropagationPolicy;
    private final RefreshPolicy<K, V> refreshPolicy;
    private final boolean asyncLoad;
}
