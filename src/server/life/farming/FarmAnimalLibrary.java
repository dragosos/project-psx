/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.life.farming;

/**
 *
 * @author FateJiki
 */
public enum FarmAnimalLibrary {
    SNAIL(1, 100100, 14),
    PIG(2, 9500203, 30),
    SLIME(3, 210100, 25),
    STIRGE(4, 2300100, 21),
    JR_BALROG(5, 9500200, 60),
    KING_SLIME(6, 9500325, 75),
    RIBBON_PIG(7, 9500204, 35),
    YETI(8, 9500201, 80),
    WHITE_FANG(9, 9500202, 50);
    
    
    final int id;
    final int mid;
    final int averageLife; // avg life in days 
    private FarmAnimalLibrary(int _id, int actualmid, int _averageLife){ 
        id = _id;
        mid = actualmid;
        averageLife = _averageLife;
    }
    
    public static FarmAnimalLibrary getById(int fid){
        FarmAnimalLibrary fal1 = null;
        for(FarmAnimalLibrary fal : FarmAnimalLibrary.values()){
            if(fal.id == fid){
                fal1 = fal;
                break;
            }
        }
        return fal1;
    }
    public static FarmAnimalLibrary getByMid(int mobid){
        FarmAnimalLibrary fal1 = null;
        for(FarmAnimalLibrary fal : FarmAnimalLibrary.values()){
            if(fal.mid== mobid){
                fal1 = fal;
                break;
            }
        }
        return fal1;
    }
    
    public long generateDeathDate(int mid){
        FarmAnimalLibrary fal = FarmAnimalLibrary.getById(mid);
        long deathDate = fal.averageLife;
        byte multiplier = (byte)(0.40 * Math.random());
        byte operator = (byte)(10.0 * Math.random());
        if(operator >= (byte)6){
            // adding more life
           return deathDate / multiplier;
        } else {
            // subtracting life
            return deathDate * multiplier;
        }
    }
    
}
