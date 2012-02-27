/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.pqs;
import client.MapleCharacter;
import java.awt.Point;
import java.util.ArrayList;
import net.channel.ChannelServer;
import net.world.PlayerStorage;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import tools.MaplePacketCreator;


/**
 *
 * @author FateJiki
 * ht : 8810018
 * pos 52, 260
 */
public class HornedTailFight extends PartyQuest{
    
    public HornedTailFight(MapleCharacter leader, byte channel) {
        super(leader, channel, 3600, 910000000, 910000000, new int[] {240060200}, (byte) 6, (byte) 140, (short) 255, PartyQuestType.HORNED_TAIL_FIGHT);
        scheduleThread(new startSpawns(), 1000 * 60);
        modifyClock(60);
        PQMessage("Hurry up! Horned Tail will spawn in 60 seconds.");
    }
    private void initSpawns() {
        modifyClock(getTimeLeft());
        MapleMap map = ChannelServer.getInstance(channel).getMapFactory().getMap(240060200);
        map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8810026), new Point(52, 260));
    }
    
    @Override
    public void finish() {
        try {

            MapleMapFactory mf = ChannelServer.getInstance(channel).getMapFactory();
            ArrayList<MapleCharacter> chrs = new ArrayList<MapleCharacter>();
            for (int m : pqMaps) {
                if (!mf.getMap(m).getCharacters().isEmpty()) {
                    chrs.addAll(mf.getMap(m).getCharacters());
                }
            }

            scheduleThread(new scheduledDispose(), 1000 * 60);
            modifyClock(45);
            PQMessage("Congratulations! You have defeated Horntail. Please loot your bounty quickly, you will be warped out in 60 seconds.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public class scheduledDispose implements Runnable {
        @Override
        public void run(){
            dispose(false, false);
        }
    }
    
    public class startSpawns implements Runnable {
        @Override
        public void run(){
            initSpawns();
        }
    }
}
