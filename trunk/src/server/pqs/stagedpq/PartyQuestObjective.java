/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.pqs.stagedpq;

/**
 *
 * @author FateJiki
 */
public class PartyQuestObjective {
    private final int boss_ToEliminate;
    private final PartyQuestObjectiveType objective;
    
    
    public PartyQuestObjective(final PartyQuestObjectiveType objective_type, final int boss_eliminate_){
        boss_ToEliminate = boss_eliminate_;
        objective = objective_type;
    }
    
    public PartyQuestObjectiveType  getObjective(){
        return objective;
    }
    
    public int getBossToEliminate(){
        return boss_ToEliminate;
    }
    
    
    public enum PartyQuestObjectiveType {
        ELIMINATE_ALL_MONSTERS, // eliminate all monsters on the map
        ELIMINATE_BOSS, // defeating a simple boss :)
        ELIMINATE_OTHER_TEAM, // pvp
        GET_COUPONS,
        ;
    }
}
