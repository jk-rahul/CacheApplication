package com.example.CacheApplication.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ScheduledExecutorUtilTest {

    @AfterAll
    static void tearDown() {
        ScheduledExecutorUtil.shutdown();
    }

    @Test
    void testExecuteAsyncRunsTask() throws InterruptedException {
        Runnable task = mock(Runnable.class);

        ScheduledExecutorUtil.executeAsync(task);
        // Allow some time for async execution
        Thread.sleep(100);
        // Verify that the task was executed
        verify(task, times(1)).run();
    }

    @Test
    void testScheduleAtFixedRate() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        Runnable task = latch::countDown;

        ScheduledExecutorUtil.scheduleAtFixedRate(task, 0, 100, TimeUnit.MILLISECONDS);
        // Ensure 3 executions happened
        assertTrue(latch.await(350, TimeUnit.MILLISECONDS));
    }

    @Test
    void testScheduleWithFixedDelay() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        Runnable task = latch::countDown;

        ScheduledExecutorUtil.scheduleWithFixedDelay(task, 0, 100, TimeUnit.MILLISECONDS);
        // Ensure 3 executions happened
        assertTrue(latch.await(350, TimeUnit.MILLISECONDS));
    }

    @Test
    void testMultipleTasksRunConcurrently() throws InterruptedException {
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);

        Runnable task1 = counter1::incrementAndGet;
        Runnable task2 = counter2::incrementAndGet;

        ScheduledExecutorUtil.scheduleAtFixedRate(task1, 0, 100, TimeUnit.MILLISECONDS);
        ScheduledExecutorUtil.scheduleAtFixedRate(task2, 0, 100, TimeUnit.MILLISECONDS);

        Thread.sleep(350); // Wait for a few executions

        assertTrue(counter1.get() >= 3);
        assertTrue(counter2.get() >= 3);
    }
}
