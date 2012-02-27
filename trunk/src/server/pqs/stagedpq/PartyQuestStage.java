/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.pqs.stagedpq;
import java.util.EnumMap;
import server.pqs.stagedpq.PartyQuestObjective.PartyQuestObjectiveType;
import server.pqs.PartyQuest;
import client.MapleCharacter;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 *
 * @author FateJiki
 */
public abstract class PartyQuestStage {
    private final PartyQuestStageType PQType;
    private final EnumMap<PartyQuestObjectiveType, PartyQuestObjective> objectives;
    private StagedPartyQuest spq_ref;
    
    public PartyQuestStage(final StagedPartyQuest spq){
        spq_ref = spq;
        PQType = spq.PQType;
        objectives = new EnumMap<PartyQuestObjectiveType, PartyQuestObjective>(PartyQuestObjectiveType.class);
    }
    
    public PartyQuestStageType getType(){
        return PQType;
    }
    
    public StagedPartyQuest getSPQReference(){
        return spq_ref;
    }
    
    public MapleMap getPQMap(){
        return null;
       // return getSPQReference().get
    }
    
    public void registerNewObjective(PartyQuestObjectiveType type, int value){
        if(!objectives.containsKey(type)){
            objectives.put(type, new PartyQuestObjective(type, value));
        } else {
            // objective already exists lololo
        }
    }
    
    public void completeStage(){
        for(MapleCharacter member : getSPQReference().getAllMembers()){
            // sends the "CLEAR" packet
            member.announce(MaplePacketCreator.mapEffect("party/clear"));
        }
    }
    
    public void dispose(){
        if(spq_ref != null){
        spq_ref = null;
        }
        objectives.clear();
    }
    
    public abstract void completeObjective(PartyQuestObjective obj);
    
    
}
