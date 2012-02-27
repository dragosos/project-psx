/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.pqs;

import client.MapleCharacter;
import java.awt.Point;
import java.util.ArrayList;
import net.channel.ChannelServer;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObjectType;
import server.life.MapleNPC;
import tools.MaplePacketCreator;
import server.life.MapleMonster;

/**
 *
 * @author FateJiki
 */
public class VonLeon extends PartyQuest {
    //8840010, then turns into von leon
    // ressurection tower : 211070110
    // spawn place.. 211070100
    // von leon pos : 25. -181
    MapleMonster vonLeon;
    public VonLeon(MapleCharacter leader, byte channel) {
        super(leader, channel, (60 * 50), 910000000, 910000000, new int[] {211070100, 211070110}, (byte) 4, (byte) 90, (short) 200, PartyQuestType.VON_LEON);
        PQMessage("Make your way to the throne! Von Leon is close to transformation.. HURRY!");
        modifyClock(40);
        scheduleThread(new scheduleInit(), 1000 * 40);
    }
    
    public void initSpawns(){
        modifyClock(getTimeLeft());
        MapleMap map = ChannelServer.getInstance(channel).getMapFactory().getMap(211070100);
        PQMessage("Lion King :Hahaha, and you thought you could win. Feast your eyes on the beginning of the end..");
        vonLeon = MapleLifeFactory.getMonster(8840010);
        map.spawnMonsterOnGroundBelow(vonLeon, new Point(25, -181));
        addThread(TimerManager.getInstance().register(new scheduleHealing(), 1000 * 60, 1000 * 10));
    }
    @Override
    public void finish() {
        try {
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.broadcastPacket(MaplePacketCreator.serverNotice(0, "Congratulations to the crew that has finally defeated the Lion King, Von Leon!!"));
            }
            MapleMapFactory mf = ChannelServer.getInstance(channel).getMapFactory();
            ArrayList<MapleCharacter> chrs = new ArrayList<MapleCharacter>();
            for (int m : pqMaps) {
                if (!mf.getMap(m).getCharacters().isEmpty()) {
                    chrs.addAll(mf.getMap(m).getCharacters());
                }
            }
            scheduleThread(new scheduledDispose(), 1000 * 30);
            modifyClock(30);
            PQMessage("Congratulations! You have defeated Von Leon even after his transformation! You will be warped out in 30 seconds.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public MapleMapObject getVonLeonObject(MapleMap map){
        MapleMapObject vonleon = null;
        for(MapleMapObject o : map.getMapObjects()){
            if(o.getType() == MapleMapObjectType.NPC){
                MapleNPC npc = (MapleNPC)o;
                if(npc.getId() == 2161000){ // von leon
                    return o;
                }
            }
        }
        return vonleon;
    }
    
    
    public class scheduledDispose implements Runnable {
        @Override
        public void run(){
            dispose(false, false);
        }
    }
    
    public class scheduleInit implements Runnable {
        @Override
        public void run(){
            initSpawns();
        }
    }
    
    public class scheduleHealing implements Runnable {
        @Override
        public void run(){
            for(MapleCharacter chr : getAllMembers()){
                if(vonLeon.isAlive()){
                    vonLeon.setHp(vonLeon.getHp() + 50000);
                    chr.announce(MaplePacketCreator.damageMonster(vonLeon.getObjectId(), -50000)); // healing
                    chr.announce(MaplePacketCreator.MapMessage("Regenerating.."));
                }
            }
        }
    }
}
