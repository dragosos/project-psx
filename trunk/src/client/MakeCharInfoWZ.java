/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;
import provider.MapleData;
import java.util.List;
import java.util.ArrayList;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author FateJiki
 */
public class MakeCharInfoWZ {
    private static MapleData data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz")).getData("MakeCharInfo.img");
    private static Map<Byte, Integer> authorizedClothes = new HashMap<Byte, Integer>();
    private static ReentrantReadWriteLock locks = new ReentrantReadWriteLock();
    
    public static void buildAllCharInfoWZ(){
        if(authorizedClothes != null){
            return;
        }
            // two loops ; one for female, one for male
        // starting with male!
            for(MapleData maleData_set : data.getChildByPath("Info/CharMale").getChildren()){
                for(MapleData maleData_values : maleData_set.getChildren()){
                    authorizedClothes.put((byte)0, MapleDataTool.getInt(data.getChildByPath("Info/CharMale" + maleData_values.getName())));
                    System.out.println("Added " + MapleDataTool.getInt(data.getChildByPath("Info/CharMale" + maleData_values.getName())));
                }
            }
            
            for(MapleData femaleData_set : data.getChildByPath("Info/CharFemale")){
                for(MapleData femaleData_values : femaleData_set.getChildren()){
                    authorizedClothes.put((byte)1, MapleDataTool.getInt(data.getChildByPath("Info/CharFemale" + femaleData_values.getName())));
                    System.out.println("Added " + MapleDataTool.getInt(data.getChildByPath("Info/CharMale" + femaleData_values.getName())));
                }
            }
    }
    
    public static boolean containsClothes(List<Integer> clothes){
        locks.readLock().lock();
        try{
        for (int i : clothes){
            if (!authorizedClothes.values().contains(i)){
                return false;
            }
        } } catch (Exception e){
        } finally {
        locks.readLock().unlock();
        }
        return true; // Means O.K.
    }
    
    private static int[] clothes_Ar = {20000, 20001, 20002, 30030, 30020, 30000, 0, 7, 3, 2, 0, 1, 2, 3, 1040002, 1040006, 1040010, 1060002, 1060006, 1072001, 1072005, 1072037, 1072038, 1302000, 1322005, 1312004, 21000, 21001, 21002, 31000, 31040, 31050, 1041002, 1041006, 1041010, 1041011, 1061002, 1061008, 1072001, 1072005, 1072037, 1072038, 1302000, 1322005, 1312004, 30007, 30003, 30002, 30037, 30033, 30032, 30027, 30023, 30022, 31007, 31003, 31002, 31047, 31043, 31042, 31057, 31053, 31052};
    public static boolean containsClothesThruArray(List<Integer> clothes){
        List<Integer> compared = new ArrayList<Integer>();
        for(int i = 0; i < clothes_Ar.length; i++){
            compared.add(clothes_Ar[i]);
        }
        for (int i = 0; i < clothes.size(); i++){
            if(compared.contains(clothes.get(i))){
                continue;
            } else {
                int underEval = clothes.get(i);
                if(30030 - underEval <= 30){
                    // lets him pass:
                    continue;
                } else {
                    return false;
                }
            }
        }
        return true; // Means O.K.
    }
}
