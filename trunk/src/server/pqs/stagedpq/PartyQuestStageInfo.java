/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.pqs.stagedpq;

/**
 *
 * @author FateJiki
 */
public enum PartyQuestStageInfo {
    //    //MapleCharacter chr, byte channel, int time, int startMap, int endMap, final int[] maps, byte minSize, byte minLevel, short maxLevel, PartyQuestStageType stagedType, PartyQuestStage[] stages) {
    HOBLIN_KING(new int[]{}, 999999, 999999, (byte)3, (byte)75, (short)200, PartyQuestStageType.RESURRECTION_HOBLIN_KING, (60 * 60)),
    FIRST_TIME_TOGETHER(new int[]{}, 103000000, 103000000, (byte)4, (byte)21, (short)200, PartyQuestStageType.FIRST_TIME_TOGETHER, 60 * 25),
    
    
    ;
    
    final private int[] maps;
    final private int startmap;
    final private int endmap;
    final private byte minSize;
    final private byte minLevel;
    final private short maxLevel;
    final private PartyQuestStageType type;
    final private int time;
    
    
    private PartyQuestStageInfo(int[] maps_, int startMap_, int endmap_, byte minSize_, byte minLevel_, short maxLevel_, PartyQuestStageType type_, int time_){
        maps = maps_;
        startmap = startMap_;
        endmap = endmap_;
        minSize = minSize_;
        minLevel = minLevel_;
        maxLevel = maxLevel_;
        type = type_;
        time = time_;
    }
    
    public static PartyQuestStageInfo getByType(PartyQuestStageType type){
        PartyQuestStageInfo info = null;
        for(PartyQuestStageInfo pqStages : PartyQuestStageInfo.values()){
            if(pqStages.getPQSType().equals(type)){
                return info;
            }
        }
        
        return null;
    }
    
    public int[] getMaps(){
        return maps;
    }
    
    public int getStartMap(){
        return startmap;
    }
    
    public int getEndMap(){
        return endmap;
    }
    
    public byte minimumPlayerSize(){
        return minSize;
    }
    
    public byte minimumPlayerLevel(){
        return minLevel;
    }
    
    public short maxPlayerLevel(){
        return maxLevel;
    }
    
    public PartyQuestStageType getPQSType(){
        return type;
    }
    
    public int getDuration(){
        return time;
    }
}
