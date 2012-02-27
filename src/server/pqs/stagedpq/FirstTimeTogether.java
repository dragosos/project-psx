/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.pqs.stagedpq;
import client.MapleCharacter;
import server.maps.MapleMap;
import server.pqs.stagedpq.PartyQuestObjective.PartyQuestObjectiveType;

/**
 *
 * @author FateJiki(Julien)
 * @version v1.1
 * @param Kerning City's Party Quest
 * @param "First Time Together"
 */
public class FirstTimeTogether extends StagedPartyQuest {
    
    public FirstTimeTogether(MapleCharacter leader){
        super(leader, PartyQuestStageType.FIRST_TIME_TOGETHER);
    }
    
    @Override
    public void completePQ(){
        
    }

    @Override
    public void preloadStages(){
        stages.put(1, new stage1(getPQLeader()));
    }
    
    @Override
    public void finish(){
        
    }
    
    
    public class stage1 extends PartyQuestStage {
        MapleMap stagedMap;
        public stage1(MapleCharacter leader){
            // Retrieves the correct StagedPartyQuest object from the PQ Leader.
            super(leader.getSPQ());
            registerNewObjective(PartyQuestObjectiveType.GET_COUPONS, leader.getParty().getMembers().size());
        }
        @Override
        public void completeStage(){
            
            dispose(); // disposes the stage, memory-related reasons
        }
        
        @Override
        public void completeObjective(PartyQuestObjective obj){
            if(obj.getObjective() == PartyQuestObjectiveType.GET_COUPONS){
                gainExp(200 * this.getSPQReference().currStage);
            }
            // stage 1 objective is to one coupon per player
        }
    }
    
}
