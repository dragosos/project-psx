/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.pqs;

import client.MapleCharacter;
import constants.ServerConstants;
import java.util.ArrayList;
import java.util.List;
import net.channel.ChannelServer;
import net.world.MaplePartyCharacter;
import server.TimerManager;
import server.maps.MapleMap;
import java.util.concurrent.ScheduledFuture;
import tools.MaplePacketCreator;
import server.pqs.stagedpq.*;

/**
 *
 * @author Sharky
 */
public abstract class PartyQuest {
    public int time, lobby, rewardMap;
    public String leaderName;
    public int[] pqMaps;
    public byte minSize, minLevel, channel;
    public short maxLevel;
    public long timeStarted;
    public List<String> members = new ArrayList<String>();
    public PartyQuestType type;
    public List<ScheduledFuture> threads = new ArrayList<ScheduledFuture>();
    
    public PartyQuest(MapleCharacter chr, byte channel, int time, int startMap, int endMap, final int[] maps, byte minSize, byte minLevel, short maxLevel, PartyQuestType _type) {
        try {
            this.leaderName = chr.getName();
            this.channel = channel;
            this.minSize = minSize;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            type = _type;

            
            this.timeStarted = System.currentTimeMillis();
            this.time = time;
            this.lobby = startMap;
            this.rewardMap = endMap;
            this.pqMaps = maps;
            initiation(chr, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    // stagedpq shit
    public PartyQuest(MapleCharacter leader, PartyQuestStageType type){
        PartyQuestStageInfo info = PartyQuestStageInfo.getByType(type);
        if(info == null){
           System.out.println("Invalid type.");
           return;
        }
        
        this.leaderName = leader.getName();
        this.channel = (byte)leader.getClient().getChannel();
        this.minSize = info.minimumPlayerSize();
        this.minLevel = info.minimumPlayerLevel();
        this.maxLevel = info.maxPlayerLevel();
        this.type = PartyQuestType.STAGED_PQ_UNRELATED;
        
        this.timeStarted = System.currentTimeMillis();
        this.time = info.getDuration();
        this.lobby = info.getStartMap();
        this.rewardMap = info.getEndMap();
        this.pqMaps = info.getMaps();
        initiation(leader, true);
    }

    private void initiation(MapleCharacter leader, boolean stagedpq){
            MapleMap map = null;
            for(MaplePartyCharacter p : leader.getParty().getMembers()){
                if(leader.getMapId() != p.getMapId()){
                    PQMessage("The request to Party Quest has failed because not all of your party members are in the same map.");
                    dispose(false, true);
                    return;
                }
            }
            for (int m : pqMaps) {
                map = ChannelServer.getInstance(channel).getMapFactory().getMap(m);
                map.killAllMonsters();
                map.clearDrops();
                if (map.getMonsterRate() > 0) {
                    map.respawn();
                }
            }
            for (MaplePartyCharacter p : leader.getParty().getMembers()) {
                members.add(p.getName());
                if(stagedpq){
                    p.getPlayer().setSPQ((StagedPartyQuest) this);
                } else {
                p.getPlayer().setPQ(this);
                }
                p.getPlayer().changeMap(pqMaps[0]);
                p.getPlayer().announce(MaplePacketCreator.getClock(time));
            }

            scheduleThread(new Runnable() {
                @Override
                public void run() {
                    dispose(false, true);
                }
            }, time * 1000);
    }

    public int getTimeLeft() {
        long current = System.currentTimeMillis();
        return (int) (time - (current - timeStarted) / 1000);
    }

    public void removeMember(MapleCharacter chr) {
        members.remove(chr.getName());
        if (members.size() < minSize) {
            dispose(true, false);
        }
    }
    
    public void addThread(ScheduledFuture sf){
        threads.add(sf);
    }
    
    public void scheduleThread(Runnable thread, long delay){
        threads.add(TimerManager.getInstance().schedule(thread, delay));
    }
    
    public PartyQuestType getType(){
        return type;
    }
    
    
    public void PQMessage(String msg){
        for(String player : members){
            getCServ().getPlayerStorage().getCharacterByName(player).announce(MaplePacketCreator.MapMessage(msg));
        }
    }
    
    public void gainExp(int delta){
        for(MapleCharacter players : getAllMembers()){
            delta *= ServerConstants.QUEST_EXP_RATE;
            players.gainExp(delta, true, true);
        }
    }
    
    public ArrayList<MapleCharacter> getAllMembers(){
        ArrayList<MapleCharacter> member = new ArrayList<MapleCharacter>();
        for(String player : members){
            member.add(getCServ().getPlayerStorage().getCharacterByName(player));
        }
        
        return member;
    }
    
    public MapleCharacter getPQLeader(){
        return ChannelServer.getCharacterFromAllServers(leaderName);
    }
    
    public void modifyClock(int seconds){
        for(String player : members){
            getCServ().getPlayerStorage().getCharacterByName(player).announce(MaplePacketCreator.getClock(seconds));
        }
    }
    
    public int getHighestLevelInParty(){
        MapleCharacter chr = null;
        int highestLevel = 1;
        for(String c : members){
            chr = getCServ().getPlayerStorage().getCharacterByName(c);
            if(chr.getLevel() > highestLevel){
                highestLevel = chr.getLevel();
            }
        }
        return highestLevel;
    }
    
    public ChannelServer getCServ(){
        return ChannelServer.getInstance(channel);
    }
    
    public void dispose(boolean lackingMembers, boolean outOfTime) {
        try {
            if(threads != null){
            for(ScheduledFuture sf : threads){
                sf.cancel(true);
            }
            threads.clear();
            threads = null;
            }
            MapleCharacter chr = null;
            ChannelServer cserv = ChannelServer.getInstance(channel);
            if(members != null){
                
            for (String n : members) {
                chr = cserv.getPlayerStorage().getCharacterByName(n);
                if (chr != null) {
                    if (chr.getPQ() != null) {
                        chr.setPQ(null);
                    }
                    for (int m : pqMaps) {
                        if (chr.getMapId() == m) {
                            chr.changeMap(lobby);
                        }
                    }
                    if (lackingMembers) {
                        chr.dropMessage("Your Party Quest has ended since your party is lacking members.");
                    } else if (outOfTime) {
                        chr.dropMessage("Your Party Quest has ended because you did not finish in time.");
                    } else {
                        chr.dropMessage("Congratulations on completing the Quest!");
                    }
                }
            }
            } else {
                return; 
            }

            MapleMap map = null;
            for (int m : pqMaps) {
                map = cserv.getMapFactory().getMap(m);
                map.killAllMonsters();
                map.clearDrops();
                map.resetReactors();
            }
            pqMaps = null;
            members = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            
        }
    }

    public abstract void finish();
   
}
