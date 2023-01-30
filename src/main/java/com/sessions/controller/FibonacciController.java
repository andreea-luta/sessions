package com.sessions.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class FibonacciController {

    //Create a map in order to store the fibonacci sequence for each session
    private Map<String, List<Integer>> fibonacciSequenceMap;

    public FibonacciController (){
        this.fibonacciSequenceMap= new ConcurrentHashMap<>();
    }

    @PostMapping("/session/{sessionId}/{size}")
    public String createSession (@PathVariable String sessionId, @PathVariable int size){

        //Check if the session already exists in the fibonacciSequenceMap map
        if(fibonacciSequenceMap.containsKey(sessionId)) {
            return "The sessionId already exists";
        }
        else{
            fibonacciSequenceMap.put(sessionId, new ArrayList<>());
            fibonacciSequenceMap.get(sessionId).add(0);
            fibonacciSequenceMap.get(sessionId).add(1);
        }
        List<Integer> fibonacciSequence=fibonacciSequenceMap.get(sessionId);
        if(fibonacciSequence.size()<size){
            for(int i =fibonacciSequence.size(); i< size; i++) {
                int nextFibonacciNumber = fibonacciSequence.get(i - 1) + fibonacciSequence.get(i - 2);
                fibonacciSequence.add(nextFibonacciNumber);
            }
        }
        return "Session ID: " + sessionId + " and size of: " +size + " has been created.";
    }

    @GetMapping("/getNextGeneratedNumber/{sessionId}")
    public int nextFibonacciNumber(@PathVariable String sessionId){

        //Check weather the session we are looking for exists
        if(!fibonacciSequenceMap.containsKey(sessionId)){
            System.out.println("Session id: " + sessionId + "doesn't exist");
        }

        //Get the last number from the sequence
        int size = fibonacciSequenceMap.get(sessionId).size();
        int a= fibonacciSequenceMap.get(sessionId).get(size-2);
        int b= fibonacciSequenceMap.get(sessionId).get(size-3);
        int nextFibonacciNumber= a+b;
        //fibonacciSequenceMap.get(sessionId).add(nextFibonacciNumber);

        return nextFibonacciNumber;
    }

    @DeleteMapping("/deleteLastGeneratedNumber/{sessionId}")
    public String deleteLastFibonacciNumber(@PathVariable String sessionId){

        //Check weather the session we are looking for exists
        if(!fibonacciSequenceMap.containsKey(sessionId)){
            System.out.println("Session id: " + sessionId + "doesn't exist");
        }

        //Remove the last number from the sequence
        int lastFibonacciValue =fibonacciSequenceMap.get(sessionId).get(fibonacciSequenceMap.get(sessionId).size()-1);
        int lastFibonacciPosition= fibonacciSequenceMap.get(sessionId).size()-1;
        fibonacciSequenceMap.get(sessionId).remove(lastFibonacciPosition);
        return "Last fibonacci number from the sequence generated: " + lastFibonacciValue+ " for session id: " + sessionId + " has been deleted.";
    }

    @GetMapping("listAllNumbers/{sessionId}")
    public List<Integer> listAllFibonacciNumbers(@PathVariable String sessionId) {
        return fibonacciSequenceMap.get(sessionId);
    }
}
