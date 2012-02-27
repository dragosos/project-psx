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
 */
public class BalrogPQ extends PartyQuest {

    public BalrogPQ(MapleCharacter leader, byte channel) {
        super(leader, channel, 3600, 910000000, 910000000, new int[] {105100300}, (byte) 4, (byte) 70, (short) 200, PartyQuestType.BALROGPQ);
        initSpawns();
    }

    private void initSpawns() {
        MapleMap map = ChannelServer.getInstance(channel).getMapFactory().getMap(105100300);
        int[] balrogSpawns = {8830000, 8830001, 8830002};
        for (int i : balrogSpawns) {
            map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(i), new Point(412, 258));
        }
        addThread(TimerManager.getInstance().register(new spawnBabies(), 1000 * 60 * 5));
    }

    @Override
    public void finish() {
        try {
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.broadcastPacket(MaplePacketCreator.serverNotice(0, "Congratulations to the crew that have obliterated Lord Balrog!"));
            }

            MapleMapFactory mf = ChannelServer.getInstance(channel).getMapFactory();
            ArrayList<MapleCharacter> chrs = new ArrayList<MapleCharacter>();
            for (int m : pqMaps) {
                if (!mf.getMap(m).getCharacters().isEmpty()) {
                    chrs.addAll(mf.getMap(m).getCharacters());
                }
            }

            scheduleThread(new scheduledDispose(), 1000 * 10);
            PQMessage("Congratulation! You have defeated Balrog. Please loot your bounty quickly, you will be warped out in 10 seconds.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public class spawnBabies implements Runnable{
        @Override
        public void run(){
            MapleCharacter leader = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(leaderName);
            for(int i = 0; i < 10; i++){
            leader.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(6400007), new java.awt.Point(412, 258));
            }
        }
    }
    
    public class scheduledDispose implements Runnable {
        @Override
        public void run(){
            dispose(false, false);
        }
    }
}
