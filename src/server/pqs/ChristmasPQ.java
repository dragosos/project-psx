/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.pqs;
import client.MapleCharacter;
import server.life.MapleMonster;
import java.util.List;
import java.util.ArrayList;
import net.channel.ChannelServer;
import server.life.MapleLifeFactory;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import java.awt.Point;
import server.TimerManager;
import tools.MaplePacketCreator;

/**
 *
 * @author FateJiki(Julien)
 * @Version v1.3
 */
public class ChristmasPQ extends PartyQuest {
    private final int mapid = 209000011; // hill of christmas
    private MapleMap map;
    private MapleMonster snowman;
    
    //MapleCharacter chr, byte channel, int time, int startMap, int endMap, final int[] maps, byte minSize, byte minLevel, short maxLevel, PartyQuestType _type) {
    public ChristmasPQ(MapleCharacter leader, byte channel){
        super(leader, channel, (60 * 25), 910000000, 910000000, new int[]{209000011}, (byte)2, (byte)20, (short)200, PartyQuestType.CHRISTMAS_PQ);
        scheduleThread(new scheduleInit(), 1000 * 10);
        PQMessage("Prepare yourselves! Noo Noo is on his way..");
        modifyClock(10);
    }
    public void initSpawns(){
        modifyClock(getTimeLeft());
        int[] monsterIDs = {9420015};
        map = ChannelServer.getInstance(this.channel).getMapFactory().getMap(mapid);
        for(int i = 0; i < monsterIDs.length; i++){
            MapleMonster snowman_ = MapleLifeFactory.getMonster(monsterIDs[i]);
            snowman = snowman_;
            manageDifficulty();
            map.spawnMonsterOnGroundBelow(snowman_, new Point(-76, 154));
        }
        // snowmen minions spawn every minute <3
        addThread(TimerManager.getInstance().register(new spawnSnowmen(), 1000 * 30));
    }
    
    
    
    public void manageDifficulty(){
        int highlv = getHighestLevelInParty();
        if(highlv >= 50){
            snowman.setHp(500000 * 15);
        } else if(highlv >= 70) {
            snowman.setHp(500000 * 25);
        } else if(highlv >= 90){
            snowman.setHp(500000 * 50);
        } else if(highlv >= 120){
            snowman.setHp(500000 * 60);
        }
        if(highlv >= 50){
            PQMessage("Since one or more member(s) has a high level for Noo Noo, Noo Noo's vitality has multiplied.");
        }
        
    }
    
    @Override
    public void finish(){
        for(ChannelServer cs : ChannelServer.getAllInstances()){
            cs.broadcastPacket(MaplePacketCreator.serverNotice(0, "Congratulations to the crew that successfully annihilated Noo Noo the Snowman!"));
        }
            MapleMapFactory mf = ChannelServer.getInstance(channel).getMapFactory();
            ArrayList<MapleCharacter> chrs = new ArrayList<MapleCharacter>();
            for (int m : pqMaps) {
                if (!mf.getMap(m).getCharacters().isEmpty()) {
                    chrs.addAll(mf.getMap(m).getCharacters());
                }
            }
        scheduleThread(new scheduleDispose(), 1000 * 10);
        modifyClock(10);
        PQMessage("Congratulations! You have successfully defeated Noo Noo the Snowman. You will automatically exit the PQ in 10 seconds.");
        snowman = null;
        map = null;
    }
    
    public class spawnSnowmen implements Runnable{
        @Override
        public void run(){
            Point pos = snowman.getPosition();
            for(int i = 0; i < 30; i++){
                map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9400712), pos);
            }
        }
    }
    
    public class scheduleInit implements Runnable {
        @Override
        public void run(){
            initSpawns();
        }
    }
    
    public class scheduleDispose implements Runnable {
        @Override
        public void run(){
            dispose(false, false);
        }
    }
    
    
    
}
