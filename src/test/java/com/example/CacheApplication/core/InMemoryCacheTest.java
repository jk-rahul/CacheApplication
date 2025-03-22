package com.example.CacheApplication.core;

import com.example.CacheApplication.datastore.DataStore;
import com.example.CacheApplication.datastore.InMemoryDataStore;
import com.example.CacheApplication.expiration.ExpirationPolicy;
import com.example.CacheApplication.expiration.TimeSinceCreationExpiration;
import com.example.CacheApplication.refreshpolicy.FixedDurationRefreshPolicy;
import com.example.CacheApplication.refreshpolicy.RefreshPolicy;
import com.example.CacheApplication.util.ScheduledExecutorUtil;
import com.example.CacheApplication.writepolicy.WritePropagationPolicy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InMemoryCacheTest {
    private InMemoryCache<String, String> cache;
    private DataStore<String, String> spyDataStore;
    private ExpirationPolicy<String, String> spyExpirationPolicy;
    private WritePropagationPolicy<String, String> mockWritePolicy;
    private RefreshPolicy<String, String> spyRefreshPolicy;

    @BeforeEach
    void setUp() {
        mockWritePolicy = mock(WritePropagationPolicy.class);
        spyExpirationPolicy = spy(new TimeSinceCreationExpiration<>(500L));
        spyRefreshPolicy = spy(new FixedDurationRefreshPolicy<>(200)); // Refresh every 2 sec
        spyDataStore = spy(new InMemoryDataStore<>(new ConcurrentHashMap<>()));

        CacheConfiguration<String, String> config = CacheConfiguration.<String, String>builder()
                .maxCapacity(2)
                .expirationPolicy(spyExpirationPolicy)
                .writePropagationPolicy(mockWritePolicy)
                .refreshPolicy(spyRefreshPolicy)
                .asyncLoad(false)
                .build();

        cache = new InMemoryCache<>(config, spyDataStore);
    }

    @AfterAll
    static void tearDown() {
        ScheduledExecutorUtil.shutdown();
    }

    @Test
    void testPutAndGet() {
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));
    }

    @Test
    void testGetReturnsFromDataStoreWhenNotInCache() {
        when(spyDataStore.get("key1")).thenReturn("value1");
        assertEquals("value1", cache.get("key1"));
        verify(spyDataStore, times(1)).get("key1");
    }

    @Test
    void testRemove() {
        cache.put("key1", "value1");
        cache.remove("key1");
        assertNull(cache.get("key1"));
    }

    @Test
    void testExpirationPolicyUpdateOnRead() {
        cache.put("key1", "value1");
        cache.get("key1");
        verify(spyExpirationPolicy, times(1)).updateAccessTimeForRead(any());
    }

    @Test
    void testExpirationPolicyUpdateOnWrite() {
        cache.put("key1", "value1");
        verify(spyExpirationPolicy, times(1)).updateAccessTimeForWrite(any());
    }

    @Test
    void testWritePropagationPolicyOnPut() {
        cache.put("key1", "value1");
        verify(mockWritePolicy, times(1)).write(eq("key1"), eq("value1"), any());
    }

    @Test
    void testScheduledCleanupInvocation() throws InterruptedException {
        doAnswer(invocation -> {
            cache.remove("key1");
            return null;
        }).when(spyExpirationPolicy).scheduleAutoCleanup(any());

        cache.put("key1", "value1");
        // Allow time for expiration policy run
        TimeUnit.MILLISECONDS.sleep(1100);
        // Key should be expired
        assertNull(cache.get("key1"));
        // Ensure expiration policy is invoked
        verify(spyExpirationPolicy, times(1)).scheduleAutoCleanup(any());
    }

    @Test
    void testRefreshPolicyInvocation() throws InterruptedException {
        cache.put("key1", "value1");

        // Simulate a datastore update
        doReturn("updatedValue1").when(spyDataStore).get("key1");

        // Wait for refresh to trigger
        Thread.sleep(300);

        // Verify refresh policy executed
        verify(spyRefreshPolicy, atLeastOnce()).scheduleAutoRefresh(any());

        // Ensure cache got the updated value
        assertEquals("updatedValue1", cache.get("key1"));
    }

    @Test
    void testLRUEviction() {

        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // Access key1 to make key2 the least recently used
        cache.get("key1");
        // Ensure cache reached max capacity
        assertEquals(2, cache.getSize());

        // Add new key, this should do LRU eviction for key2
        cache.put("key3", "value3");
        // Key2 should be evicted
        assertNull(cache.get("key2"));
        // Key1 should remain
        assertEquals("value1", cache.get("key1"));
        // Key3 should remain
        assertEquals("value3", cache.get("key3"));
        // cache size should still be 2
        assertEquals(2, cache.getSize());
    }

    @Test
    void testConcurrentCacheAccess() throws InterruptedException {
        int numThreads = 10;
        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                int index = i;
                executor.submit(() -> {
                    try {
                        cache.put("key" + index, "value" + index);
                        assertNotNull(cache.get("key" + index));
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            executor.shutdown();
        }
    }

    @Test
    void testConcurrentEviction() throws InterruptedException {
        int numThreads = 5;
        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                int index = i;
                executor.submit(() -> {
                    try {
                        cache.put("key" + index, "value" + index);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            executor.shutdown();
        }
        assertTrue(cache.getSize() <= 2, "Cache size exceeded maxCapacity");
    }

    @Test
    void testConcurrentExpiration() throws InterruptedException {
        cache.put("key1", "value1");
        // Allow time for expiration
        Thread.sleep(600);

        int numThreads = 5;
        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        assertNull(cache.get("key1"));
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();
        }
    }

    @Test
    void testSyncLoadingDuringCacheCreation() {
        spyDataStore.put("keyA", "valueA");
        spyDataStore.put("keyB", "valueB");

        CacheConfiguration<String, String> config = CacheConfiguration.<String, String>builder()
                .maxCapacity(2)
                .expirationPolicy(spyExpirationPolicy)
                .writePropagationPolicy(mockWritePolicy)
                .refreshPolicy(spyRefreshPolicy)
                .asyncLoad(false)
                .build();

        InMemoryCache<String, String> newCache = new InMemoryCache<>(config, spyDataStore);

        assertEquals("valueA", newCache.get("keyA"));
        assertEquals("valueB", newCache.get("keyB"));
    }
}
