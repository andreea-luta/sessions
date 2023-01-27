package com.sessions.controller;

import org.junit.jupiter.api.Test;

import java.util.List;

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
}
