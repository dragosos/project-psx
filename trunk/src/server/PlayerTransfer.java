/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;
import client.GameMode;
import java.util.HashMap;
import java.util.Map;
import client.MapleCharacter;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import net.channel.ChannelServer;
import java.sql.SQLException;
import client.MapleClient;
import server.maps.MapleMapFactory;

/**
 *
 * @author FateJiki
 * @Use This is to reduce stress on the SQL Server. Instead of unloading and reloading.
 */
public class PlayerTransfer {
    /*private static Map<Integer, MapleCharacter> queueList = new HashMap<Integer, MapleCharacter>();
    final static ReentrantReadWriteLock QueueLock = new ReentrantReadWriteLock();
    final static WriteLock writeLock = QueueLock.writeLock();
    final static ReadLock readLock = QueueLock.readLock();
    
    public static void storeCharacter(MapleCharacter chr){
        try{
        writeLock.lock();
        if(containsCharacter(chr.getId())){
            writeLock.lock();
            queueList.put(chr.getId(), chr);
        }
        } finally {
            writeLock.unlock();
        }
    }
    
    public static MapleCharacter retrieveCharacter(int chrid, MapleClient c){
        long now = System.currentTimeMillis();
       MapleCharacter retr_Char = null;
        try {
            if(containsCharacter(chrid)){
                readLock.lock();
                retr_Char = queueList.get(chrid);
                removeCharacter(chrid);
            } else {
                // If it cannot be found in the stack, then it will be loaded DIRECTLY from MySQL.
                try{
                retr_Char = MapleCharacter.loadCharFromDB(chrid, ChannelServer.getCharacterFromAllServers(chrid).getClient(), true);
                } catch (SQLException e){
                    e.printStackTrace();
                }
            }
        } finally {
            readLock.unlock();
        }
            System.out.println("Retrieving character from stack took : " + (System.currentTimeMillis() - now) + "ms!");
            return retr_Char;
    }
    
    private static boolean containsCharacter(int chrid){
        readLock.lock();
        boolean exists = false;
        try{
            exists = queueList.containsKey(chrid);
        } finally {
            readLock.unlock();
            return exists;
        }
    }
    
    private static void removeCharacter(int chrid){
        writeLock.lock();
        try{
            queueList.remove(chrid);
        } finally {
            writeLock.unlock();
        }
    }
        private synchronized static void updateCServer(MapleCharacter ret, MapleClient c){ 
        c.setPlayer(ret);
        System.out.println("client is set");
        MapleMapFactory mapFactory = c.getChannelServer().getMapFactory(c.getPlayer().getGMode());
        System.out.println("now setting map");
                c.getPlayer().setMap(mapFactory.getMap(ret.getMapId()));
                if (c.getPlayer().getMap() == null) { 
                    c.getPlayer().setMap(mapFactory.getMap(100000000)); 
                } 
    } 
}*/
}
