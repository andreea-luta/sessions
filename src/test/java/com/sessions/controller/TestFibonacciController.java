package com.sessions.controller;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFibonacciController {
    @Test
    public void testCreateSession(){
        String sessionId ="12345";
        int size = 20;
        FibonacciController fibonacciController = new FibonacciController();
        String result = fibonacciController.createSession(sessionId,size);

        assertEquals("Session ID: 12345 and size of: 20 has been created.",result);
    }

    @Test
    public void testNextFibonacciNumber(){
        String sessionId ="12345";
        int size = 5;
        FibonacciController fibonacciController = new FibonacciController();
        fibonacciController.createSession(sessionId,size);
        int result = fibonacciController.nextFibonacciNumber(sessionId);

        assertEquals(3,result);
    }

    @Test
    public void testDeleteLastGeneratedNumber(){
        String sessionId ="12345";
        int size = 12;
        FibonacciController fibonacciController = new FibonacciController();
        fibonacciController.createSession(sessionId,size);
        String result = fibonacciController.deleteLastFibonacciNumber(sessionId);

        assertEquals("Last fibonacci number from the sequence generated: " + 89 + " for session id: " + 12345 + " has been deleted.", result);
    }

    @Test
    public void testListAllNumbers(){
        String sessionId ="12345";
        int size = 7;
        FibonacciController fibonacciController = new FibonacciController();
        fibonacciController.createSession(sessionId,size);
        List <Integer> result = fibonacciController.listAllFibonacciNumbers(sessionId);

        assertEquals(7, result.size());
        assertEquals(0, result.get(0));
        assertEquals(1, result.get(1));
        assertEquals(5, result.get(5));
        assertEquals(8, result.get(6));
    }

    @Test
    public void testListAllNumbers_works_in_parallel() {
        final FibonacciController fibonacciController = new FibonacciController();
        final CountDownLatch startWorkLatch = new CountDownLatch(1);
        final AtomicInteger sessionIdSeed = new AtomicInteger(0);
        final List<Integer> createdSessionIds = new CopyOnWriteArrayList<>();
        final int workerThreadCount = 10;
        final int workerThreadTargetCallCount = 1000;
        final CountDownLatch workDoneLatch = new CountDownLatch(workerThreadCount * workerThreadTargetCallCount);
        final SessionCreator sessionCreator = new SessionCreator(
                fibonacciController, createdSessionIds, sessionIdSeed, workDoneLatch
        );
        final Random randomNumberGenerator = new Random();
        final NumbersReader numbersReader = new NumbersReader(
                fibonacciController, createdSessionIds, randomNumberGenerator, workDoneLatch
        );
        final ErrorDetector errorDetector = new ErrorDetector();
        IntStream.range(0, workerThreadCount).forEach(threadIndex -> new Thread(errorDetector.wrap(() -> {
            awaitThreadStartWorkNotice(startWorkLatch);
            IntStream.range(0, workerThreadTargetCallCount).forEach(sessionIndex -> sessionCreator.create());
        })).start());
        IntStream.range(0, workerThreadCount).forEach(threadIndex -> new Thread(errorDetector.wrap(() -> {
            awaitThreadStartWorkNotice(startWorkLatch);
            IntStream.range(0, workerThreadTargetCallCount).forEach(sessionIndex -> numbersReader.read());
        })).start());

        startWorkLatch.countDown();
        assertDoesNotThrow(() -> workDoneLatch.await());
        assertEquals(0, errorDetector.errorCount.get());
    }

    private static class ErrorDetector {
        private final AtomicInteger errorCount = new AtomicInteger();

        public Runnable wrap(Runnable runnable) {
            return () -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            };
        }
    }

    @AllArgsConstructor
    private static class SessionCreator {
        private final FibonacciController fibonacciController;
        private final List<Integer> createdSessionIds;
        private final AtomicInteger sessionIdSeed;
        private final CountDownLatch workDoneLatch;

        public void create() {
            int sessionId = sessionIdSeed.incrementAndGet();
            fibonacciController.createSession(String.valueOf(sessionId), 10);
            createdSessionIds.add(sessionId);
            workDoneLatch.countDown();
        }
    }

    @AllArgsConstructor
    private static class NumbersReader {
        private final FibonacciController fibonacciController;
        private final List<Integer> createdSessionIds;
        private final Random randomNumberGenerator;
        private final CountDownLatch workDoneLatch;

        public void read() {
            while (createdSessionIds.size() == 0) { sleepUnsafe(); }
            final int currentlyCreatedSessionsCount = createdSessionIds.size();
            final int sessionIdIndex = randomNumberGenerator.nextInt(currentlyCreatedSessionsCount);
            final Integer sessionId = createdSessionIds.get(sessionIdIndex);
            fibonacciController.listAllFibonacciNumbers(String.valueOf(sessionId));
            workDoneLatch.countDown();
        }
    }

    private static void sleepUnsafe() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void awaitThreadStartWorkNotice(final CountDownLatch countDownLatch) {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
