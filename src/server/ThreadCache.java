/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.*;

/**
 *
 * @author FateJiki
 */
public class ThreadCache {
    private static Map<Long, ScheduledFuture<?>> threadCache = new HashMap<Long, ScheduledFuture<?>>();
    ReentrantReadWriteLock threadsLock;
    private static WriteLock wl;
    private static ReadLock rl;
    private ScheduledFuture expirationWorker;
    private boolean started = false;
    
    public ThreadCache(){
        threadsLock = new ReentrantReadWriteLock();
        wl = threadsLock.writeLock();
        rl = threadsLock.readLock();
    }
    
    public void addThread(long repeatTime, ScheduledFuture sf){
        if(!started){
           start(); 
        } else {
        wl.lock();
        try{
        threadCache.put(System.currentTimeMillis() + repeatTime, sf);
        } finally {
            wl.unlock();
        }
        }
    }
    
    public void start(){
        started = true;
        expirationWorker = TimerManager.getInstance().register(new expirationJob(), 1000 * 45);
    }
    
    public void dispose(){
        expirationWorker.cancel(true);
        expirationWorker = null;
        for(ScheduledFuture sf : threadCache.values()){
            sf.cancel(true);
            sf = null;
        }
        threadCache.clear();
        threadCache = null;
    }
    
    public boolean hasStarted(){
        return started;
    }
  
    public class expirationJob implements Runnable {
        @Override
        public void run(){
            long now = System.currentTimeMillis();
            int num = 0;
            int running = 0;
            rl.lock();
            try{
            for(Long n : threadCache.keySet()){
                if(now >= n){
                    ScheduledFuture sf = threadCache.get(n);
                    if(sf != null){
                        sf.cancel(true);
                        threadCache.remove(n);
                        sf = null;
                        num++;
                    }
                    running++;
                }
            }
            System.out.println("[THREAD-CACHE] Out of " + running + " threads, " + num + " have been disposed of.");
            } finally {
                rl.unlock();
            }
        }
    }
}
