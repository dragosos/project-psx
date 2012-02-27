/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.achievement;

/**
 *
 * @author FateJiki
 */
public enum MapleAchievementLibrary {
    HELPING_OUT_THE_LOST_REFUGEES(1, "Good hearted Mapler", 1, 2000, 500000, 10000000), // done
    
    REACHING_LEVEL_10(2, "Beginner adventurer", 1, 500, 0, 50000), // done
    REACHING_LEVEL_30(3, "Intermediate Adventurer", 1, 1000, 0, 100000),// done
    REACHING_LEVEL_70(4, "Good Adventurer", 1, 1500, 0, 200000),// done
    REACHING_LEVEL_120(5, "Serious Adventurer", 1, 5000, 0, 3000000),// done
    REACHING_LEVEL_200(6, "Elite adventurer", 1, 10000, 0, 10000000),// done
    REACHING_HIGHEST_FAME(7, "Attention whore", 5, 8000, 100000000, 5000000),// done
    MAXING_HP(7, "Tolerating Player", 10, 2000, 1000000, 5000000),// done
    MAXING_MP(8, "Concentrated Fellow", 10, 2000, 1000000, 5000000),// done
    
    WALKED_AROUND_THE_COUNTRY(9, "Marathon runner", 10, 10000, 50000000, 0),// done
    WALKED_AROUND_THE_WORLD(10, "Crazy Walker", 100, 30000, 1000000000, 0),// done
    
    KILLED_ZAKUM(11, "Zakum Hunter", 5, 15000, 0, 0), // done
    KILLED_PIANUS(12, "Pianus Hunter", 5, 12000, 0, 0),// done
    KILLED_HORNTAIL(13, "Horntail Hunter", 10, 17500, 0, 0),// done
    KILLED_PINKBEAN(14, "Pink Bean Hunter", 15, 20000, 0, 0),// done
    KILLED_MANO(15, "Mano Hunter", 10, 5000, 0, 0),// done
    KILLED_STUMPY(16, "Woodchucker", 5, 2500, 0, 0), // done
    KILLED_10_BOSSES(17, "Boss Hunter", 5, 0, 0, 5000000),// done
    KILLED_100_BOSSES(18, "Boss Annihilator", 8, 3000, 5000000, 5000000),// done
    KILLED_1000_BOSSES(19, "Boss Exterminator", 10, 5000, 5000000, 5000000),// done
    FIND_THE_MYSTERIOUS_PAPER(20, "Note picker", 3, 2000, 50000, 100000),
    KILLED_100_MONSTERS(21, "Beginner monster hunter", 4, 300, 500, 10000), // done
    KILLED_1000_MONSTERS(22, "You're getting there..", 5, 500, 800, 15000),// done
    KILLED_10000_MONSTERS(23, "Serious monster hunter", 10, 10000, 9000000, 2000000),// done
    COMBO_100(24, "Combo-er", 2, 500, 9000, 100000), // done
    COMBO_1000(25, "Combo whore", 10, 5000, 10000, 0), // done
    HUNDRED_PLAYERS_IN_SCREEN(26, "Crowd-o-phile", 10, 1000, 50, 1005),// done
    SEEN_A_GM(27, "Kiss-ass GM Spotter", 10, 0, 500000),// done
    FARTED(28, "Passed-The-Gas", 10, 100, 50, 100000),// done
    HUNDRED_MONSTERS_ON_SCREEN(27, "Horde lover", 5, 1000, 500000, 0),// done
    ADD_FAMILY_CHARACTER(28, "Ex-Orphan", 5, 1000, 50000, 0),
    PURCHASED_A_PET(29, "Pet lover", 3, 1000, 60000, 100000), // done
    MADE_10_FRIENDS(30, "Social beast", 5, 1337, 30000, 50000), // done
    MADE_A_GUILD(31, "Guild-creator", 1, 800, 5000000, 10000),// done
    JOIN_A_GUILD(32, "Attention-seeker", 4, 500, 0, 0),//done
    CREATED_A_PARTY(33, "Born leader", 5, 800, 0, 0),//done
    ACCUMULATED_100_GP(34, "Team player", 5, 1000, 1000000, 10000),
    COMPLETED_BALROGPQ(35, "Balrog Slayer", 5, 1000, 1000000, 50000),
    COMPLETED_CHRISTMASPQ(36, "NooNoo Slayer", 5, 1000, 1000000, 50000),
    
    
    ;
    // Staying online for an amount of time
    
    
    
    
    
    
    
    final int id;
    final String name;
    final int rarety;
    int NXPrize = 0;
    int EXPPrize = 0;
    int MesoPrize = 0;
    int famePrize;
    int prizeId;
    
    private MapleAchievementLibrary(int _id, String _name, int _rarety, int _NXPrize, int _EXPPrize, int _MesoPrize){
        id = _id;
        name = _name;
        rarety = _rarety;
        NXPrize = _NXPrize;
        EXPPrize = _EXPPrize;
        MesoPrize = _MesoPrize;
    }
    
    private MapleAchievementLibrary(int _id, String _name, int _rarety, int _famePrize, int _prizeid){
        id = _id;
        name = _name;
        rarety = _rarety;
        famePrize = _famePrize;
        prizeId = _prizeid;
    }
    
    public String getAchievementName(){
        return name;
    }
    
    public int getId(){
        return id;
    }

    public int getRarety(){
        return rarety;
    }
    
    public int getNXPrize(){
        return NXPrize / 3;
    }
    
    public int getEXPPrize(){
        return EXPPrize;
    }
    
    public int getMesoPrize(){
        return MesoPrize;
    }
    
    public static MapleAchievementLibrary getById(int id){
        MapleAchievementLibrary mal = null;
        for(MapleAchievementLibrary mal2 : MapleAchievementLibrary.values()){
            if(mal2.getId() == id){
                mal = mal2; 
                break;
            }
        }
        
        return mal;
    }
}
