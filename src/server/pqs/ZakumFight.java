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
 * // zakum 1 = 8800000
 * // arms (loop from 8800003 to 8800010)
 * pos 65, -218
 */
public class ZakumFight extends PartyQuest{
    
    public ZakumFight(MapleCharacter leader, byte channel) {
        super(leader, channel, 3600, 910000000, 910000000, new int[] {280030000}, (byte) 4, (byte) 125, (short) 255, PartyQuestType.ZAKUM_FIGHT);
        scheduleThread(new startSpawns(), 1000 * 60);
        modifyClock(60);
        PQMessage("Hurry up! Zakum will spawn in 60 seconds.");
    }
    private void initSpawns() {
        modifyClock(getTimeLeft());
        MapleMap map = ChannelServer.getInstance(channel).getMapFactory().getMap(280030000);
        map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), new Point(65, -218));
        for (int i = 8800003; i < 8800010; i++) {// arms
            map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(i), new Point(65, -218));
        }
    }
    
    @Override
    public void finish() {
        try {
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.broadcastPacket(MaplePacketCreator.serverNotice(0, "Congratulations to the crew that have destroyed Zakum! You saved the Maple World!"));
            }

            MapleMapFactory mf = ChannelServer.getInstance(channel).getMapFactory();
            ArrayList<MapleCharacter> chrs = new ArrayList<MapleCharacter>();
            for (int m : pqMaps) {
                if (!mf.getMap(m).getCharacters().isEmpty()) {
                    chrs.addAll(mf.getMap(m).getCharacters());
                }
            }

            scheduleThread(new scheduledDispose(), 1000 * 45);
            modifyClock(45);
            PQMessage("Congratulations! You have defeated Zakum. Please loot your bounty quickly, you will be warped out in 45 seconds.");
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
