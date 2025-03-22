package com.example.CacheApplication.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Util class to support executing various async and scheduled tasks for the InMemoryCache system
 */
public class ScheduledExecutorUtil {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period,
                                                         TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay,
                                                         TimeUnit unit) {
        return scheduler.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    public static void executeAsync(Runnable task) {
        scheduler.submit(task);
    }

    public static void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
