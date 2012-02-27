/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;
import client.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author FateJiki
 */
public class AntiDupe {
    private final static Set<Integer> allActivateItemUIDs = new HashSet<Integer>();
    private static int runningItemUID = 0;
    private static final ReentrantLock lock = new ReentrantLock();
    
    
    public static void AttributeUniqueID(IItem item){
        
    }
    
    private int getNextAvailableUID(){
        int highest = 1;
        int availableUid = 0;
        for(Integer i : allActivateItemUIDs){
            if(i.intValue() > highest){
                highest = i.intValue();
            }
        }
        availableUid = highest;
        availableUid++;
        while(isUIDUsed(availableUid)){
            availableUid++;
        }
        return availableUid;
    }
    
    public static boolean isUIDUsed(int uid){
        boolean yes = false;
        lock.lock();
        try{
            yes = allActivateItemUIDs.contains(uid);
        } finally {
            lock.unlock();
        }
        
        return yes;
    }
}
