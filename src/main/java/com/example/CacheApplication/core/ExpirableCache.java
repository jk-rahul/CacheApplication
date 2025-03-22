package com.example.CacheApplication.core;

public interface ExpirableCache {
    Runnable getAutoExpirationRunnable();
}
