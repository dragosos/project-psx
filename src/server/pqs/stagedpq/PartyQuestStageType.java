/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.pqs.stagedpq;

/**
 *
 * @author FateJiki
 */
public enum PartyQuestStageType {
    // name(number of stages);
    LUDI_PQ(10),
    RESURRECTION_HOBLIN_KING(5),
    FIRST_TIME_TOGETHER(6), // incl. bonus
    
    ;
    final int stages;
    
    private PartyQuestStageType(final int stages_){
        stages = stages_;
    }
    
    public int getStageAmount(){
        return stages;
    }
}
