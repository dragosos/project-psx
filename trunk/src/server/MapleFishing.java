package server;

import client.MapleCharacter;
import tools.MaplePacketCreator;
import client.MapleCharacter;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/** 
 * 
 * @author FateJiki of RaGeZONE 
 */ 
public class MapleFishing { 
    private final static List<String> fishers = new ArrayList<String>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public static void removeFisher(String name){
        lock.writeLock().lock();
        try{
            if(containsFisher(name)){
                fishers.remove(name);
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public static void addFisher(String name){
        lock.writeLock().lock();
        try{
            if(!containsFisher(name)){
                fishers.add(name);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public static boolean containsFisher(String name){
        lock.readLock().lock();
        try{
            return fishers.contains(name);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public static List<String> getFishers(){
        List<String> fisherss = new ArrayList<String>();
        lock.readLock().lock();
        try{
            for(String n : fishers){
                fisherss.add(n);
            }
        } finally {
            lock.readLock().unlock();
        }
        return fisherss;
    }
    public static boolean started = false;
    public static void doFishing(MapleCharacter chr){ 
        int mesoMultiplier = 1; 
        int expMultiplier = 1; 
        switch(chr.getWorld()){ 
            case 0: 
                mesoMultiplier = constants.ServerConstants.MESO_RATE; 
                expMultiplier = constants.ServerConstants.EXP_RATE; 
                break; 
        }
        if(chr.haveItem(2270008)){
        chr.gainItem(2270008, (short)-1, false);
        } else {
            chr.announce(MaplePacketCreator.serverNotice(5, "You need fishing bait in order to fish!"));
            return;
        }
        int mesoAward = (int)(200.0 * Math.random() + 1201) * mesoMultiplier + (15 * chr.getLevel() / 5); 
        int expAward = (int)(645.0 * Math.random()) * expMultiplier + (15 * chr.getLevel() / 2) / 6; 
        if(chr.getLevel() >= 30 && chr.getMapId() == 251000100){ 
            int rand = (int)(4.0 * Math.random()); 
            switch(rand){ 
                case 0: 
                    chr.gainMeso(mesoAward, true, true, true); 
                    chr.getClient().getSession().write(MaplePacketCreator.catchMonster(9500336, 2000017, (byte)1)); 
                    chr.getMap().broadcastMessage(chr, MaplePacketCreator.catchMonster(9500336, 2000017, (byte)1), false); 
                    break; 
                case 1: 
                    chr.gainExp(expAward, true, true); 
                    chr.getClient().getSession().write(MaplePacketCreator.catchMonster(9500336, 2000017, (byte)1)); 
                    chr.getMap().broadcastMessage(chr, MaplePacketCreator.catchMonster(9500336, 2000017, (byte)1), false); 
                    break; 
                case 2: 
                    chr.gainItem(getRandomItem(), (short)1, true); 
                    chr.getClient().getSession().write(MaplePacketCreator.catchMonster(9500336, 2000017, (byte)1)); 
                    chr.getMap().broadcastMessage(chr, MaplePacketCreator.catchMonster(9500336, 2000017, (byte)1), false); 
                    break; 
                default:
                    chr.announce(MaplePacketCreator.serverNotice(5, "Oh no! The fish got away with your bait!"));
                    break;
            } 
           
        } else { 
            chr.dropMessage("You must be above level 30 to fish!"); 
        } 
    } 
    public static int getRandomItem(){ 
        int finalID = 0; 
        int rand = (int)(100.0 * Math.random()); 
        int[] commons = {1002851, 2002020, 2002020, 2000006, 2000018, 2002018, 2002024, 2002027, 2002027, 2000018, 2000018, 2000018 , 2000018, 2002030, 2002018, 2000016}; // filler' up 
        int[] uncommons = {1000025, 1002662, 1002812, 1002850, 1002881, 1002880, 1012072, 4020009, 2043220, 2043022, 2040543, 2044420, 2040943, 2043713, 2044220, 2044120, 2040429, 2043220, 2040943}; // filler' uptoo 
        int[] rares = {1002859, 1002553, 01002762, 01002763, 01002764, 01002765, 01002766, 01002663, 1002788, 1002949, 2049100, 2340000, 2040822,2040822,2040822,2040822,2040822,2040822,2040822,2040822}; // filler' uplast 
        if(rand >= 25){ 
            return commons[(int)(commons.length * Math.random())]; 
        } else if(rand <= 7 && rand >= 5){ 
            return uncommons[(int)(uncommons.length * Math.random())]; 
        } else if(rand <= 3){ 
            return rares[(int)(rares.length * Math.random())]; 
        } 

        return finalID; 
    } 
}