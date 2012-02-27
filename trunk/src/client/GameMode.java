/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author FateJiki
 */
public enum GameMode {
    //TODO: Integrate this with Achievements!
    
        NORMAL(0), // Gleich
        CASUAL(1), // Beil
        HARDCORE(2), // Stark
        SURVIVAL(3);// Leben
        
        
        final int type;
    private GameMode(int _type){
        type = _type;
    }
    
    public int getType(){
        return type;
    }
    public static GameMode getById(int _type){
        GameMode gm = null;
        for(GameMode gm_srch : GameMode.values()){
            if(gm_srch.getType() == _type){
                gm = gm_srch;
            }
        }
        
        return gm;
    }
    
    public boolean isA(GameMode basemode){
        return getType() == basemode.getType();
    }
    
    public enum GameModeRequirements{
        ALL,
        NORMAL_ONLY,
        CASUAL_ONLY,
        CASUAL_HARDCORE,
        CASUAL_SURVIVAL,
        HARDCORE_SURVIVAL,
        HARDCORE_ONLY,
        SURVIVAL_ONLY,
    }
}
