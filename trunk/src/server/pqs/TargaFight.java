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
 * @Scarlion ID : 9420547
 * @Furious : 9420549
 * @Equip : 1003025
 * @pos : 212, 215
 */
public class TargaFight extends PartyQuest {
    public TargaFight(MapleCharacter leader, byte channel) {
        super(leader, channel, (10 * 60), 910000000, 910000000, new int[] {180000001}, (byte) 2, (byte) 100, (short) 255, PartyQuestType.TARGA);
        scheduleThread(new startSpawns(), 1000 * 60);
        modifyClock(10);
        PQMessage("Hurry up! Targa will spawn in 10 seconds.");
    }
    private void initSpawns() {
        modifyClock(getTimeLeft());
        MapleMap map = ChannelServer.getInstance(channel).getMapFactory().getMap(180000001);
        map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9420547), new Point(212, 215));
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

            scheduleThread(new scheduledDispose(), 1000 * 15);
            modifyClock(15);
            PQMessage("Congratulations! You have defeated Targa. Please loot your bounty quickly, you will be warped out in 15 seconds.");
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
