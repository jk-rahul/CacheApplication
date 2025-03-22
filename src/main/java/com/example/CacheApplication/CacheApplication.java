package com.example.CacheApplication;

import com.example.CacheApplication.core.CacheConfiguration;
import com.example.CacheApplication.core.InMemoryCache;
import com.example.CacheApplication.datastore.DataStore;
import com.example.CacheApplication.datastore.InMemoryDataStore;
import com.example.CacheApplication.expiration.TimeSinceCreationExpiration;
import com.example.CacheApplication.refreshpolicy.FixedDurationRefreshPolicy;
import com.example.CacheApplication.util.ScheduledExecutorUtil;
import com.example.CacheApplication.writepolicy.WriteThroughPropagationPolicy;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class CacheApplication {

	public static void main(String[] args) {

		final String KEY1 = "key1";
		final String KEY2 = "key2";
		final String KEY3 = "key3";
		CacheConfiguration<String, String> config = CacheConfiguration.<String, String>builder()
				.maxCapacity(2)
				.expirationPolicy(new TimeSinceCreationExpiration<>(1000L))
				.writePropagationPolicy(new WriteThroughPropagationPolicy<>())
				.refreshPolicy(new FixedDurationRefreshPolicy<>(2000L))
				.asyncLoad(false)
				.build();

		ConcurrentHashMap<String, String> datastoreMap = new ConcurrentHashMap<>();
		datastoreMap.put(KEY1, "value0");
		DataStore<String, String> dataStore = new InMemoryDataStore<>(datastoreMap);

		InMemoryCache<String, String> cache = new InMemoryCache<>(config, dataStore);

		// Given Test case 0 - basic
		System.out.println("Basic Get key1: " + cache.get(KEY1));

		// Given Test case 1 - add and retrieve
		cache.put(KEY1, "value1");
		System.out.println("And and retrieve key1: " + cache.get(KEY1));

		// Given Test case 2 - retrieve non-existent key
		System.out.println("Retrieve non-existent key2: " + cache.get(KEY2));

		// Given Test case 3 - update and retrieve existing key
		cache.put(KEY1, "value2");
		System.out.println("Update and retrieve key1: " + cache.get(KEY1));

		// Given Test case 4 - remove and retrieve existing key. also remove from datastore
		cache.remove(KEY1);
		dataStore.remove(KEY1);
		System.out.println("Remove and retrieve key1: " + cache.get(KEY1));

		// Given Test case 5 - test eviction policy. key1 would retrieved back from datastore.
		cache.put(KEY1, "value1");
		cache.put(KEY2, "value2");
		cache.put(KEY3, "value3");
		System.out.println("Get current size of cache after eviction: " + cache.getSize());

		// shutdown scheduled executor
		ScheduledExecutorUtil.shutdown();
	}

}
