/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.pqs.stagedpq;
import java.util.List;
import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import net.channel.ChannelServer;
import server.maps.MapleMap;
import client.*;
import server.pqs.PartyQuest;

/**
 *
 * @author FateJiki
 */
public abstract class StagedPartyQuest extends PartyQuest {
    /*
     * We choose LinkedList because it only allows unique values.
     */
    Map<Integer, PartyQuestStage> stages = new HashMap<Integer, PartyQuestStage>();
    Map<Integer, MapleMap> maps = new HashMap<Integer, MapleMap>();
    PartyQuestStageType PQType;
    byte nStages;
    int currStage;
    
    public StagedPartyQuest(MapleCharacter leader, PartyQuestStageType type){
        super(leader, type);
        nStages = (byte)type.getStageAmount();
        PQType = type;
        for(int i = 0; i < pqMaps.length; i++){
            maps.put(i, leader.getClient().getChannelServer().getMapFactory().getMap(pqMaps[i]));
        }
        currStage = 1;
    }
    
    //public List<MapleMap>
    
    public int getCurrentStage(){
        return currStage;
    }
    
    public Map<Integer, PartyQuestStage> getStages(){
        return stages;
    }
    
    public void WarpToNextStage(){
        currStage++;
        PartyQuestStage pqs = stages.get(currStage);
        for(MapleCharacter players : this.getAllMembers()){
          //  players.
        }
    }
    
    @Override
    public void dispose(boolean lackingMembers, boolean outOfTime){
        try {
            if(threads != null){
            for(ScheduledFuture sf : threads){
                sf.cancel(true);
            }
            threads.clear();
            threads = null;
            }
            for(PartyQuestStage pqs : stages.values()){
                pqs.dispose();
            }
            MapleCharacter chr = null;
            ChannelServer cserv = ChannelServer.getInstance(channel);
            if(members != null){
                
            for (String n : members) {
                chr = cserv.getPlayerStorage().getCharacterByName(n);
                if (chr != null) {
                    if (chr.getSPQ() != null) {
                        chr.setSPQ(null);
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
    
    public abstract void preloadStages();
    public abstract void completePQ();
    
}
