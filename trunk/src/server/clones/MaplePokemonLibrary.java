/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.clones;

/**
 *
 * @author FateJiki
 */
public enum MaplePokemonLibrary {
    // total 300 levels..
    PIG(2, 9500203, 0.23, 0.4, 1),
    MEDIUM_SNOWMAN(3, 9500322, .75, 0.6, 5),
    // lv 100
    JR_BALROG(5, 9500200, 0.60, 0.4, 6),
    RIBBON_PIG(7, 9500204, 0.27, 0.25, 2),
    YETI(8, 9500201, 0.40, 0.36, 4),
    WHITE_FANG(9, 9500202, 0.30, 0.15, 3),
    // lv300
    PINK_BEAN(10, 8820000, 5, 0.8, 15),
    RED_EARED_MONSTER(11, 9500315, 4, 0.9, 10),
    KING_YETI(12, 9500316, 7, 0.4, 7),
    MULTI_TAILED_FOX(13, 9500312, 5, 1.2, 6),
    // Lv400
    ALLIGATOR(14, 9500311, 6, 0.5, 10),
    CUCKOO_CLOCK(15, 9500310, 8, 0.7, 12),
    KING_CLANG(16, 9500309, 9, 0.7, 10),
    UNDEAD_MONKEY(17, 9500308, 2, 2.0, 14),
    BIG_TREE(18, 9500307, 0.5, 3.0, 8),
    MANO(19, 9500306, 2, 0.4, 10),
    ;
    
    final int id;
    final int modid;
    final double damageMod_ = 1;
    final double atkRate_; // based on a 1000ms atk interval
    final int attackNum;
    private MaplePokemonLibrary(int _id, int _mobid, double _damageMod, double atkRate, int zattackNum){ 
        id = _id;
        modid = _mobid;
        _damageMod = damageMod_;
        atkRate_ = atkRate;
        attackNum = zattackNum;
    }
    
    public static MaplePokemonLibrary getById(int id){
        for(MaplePokemonLibrary mpf : MaplePokemonLibrary.values()){
            if(id == mpf.id){
                return mpf;
            }
        }
        
        return null;
    }
    
    public static int getRandomPokemonID(){
        int id = 1;
        id = MaplePokemonLibrary.values()[(int)(MaplePokemonLibrary.values().length * Math.random())].id;
        
        return id;
    }
    
}
    