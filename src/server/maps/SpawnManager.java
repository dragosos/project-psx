/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.maps;
import java.util.Map;
import java.util.HashMap;
import tools.Pair;
import server.life.MapleMonster;

/**
 *
 * @author FateJiki
 */
public class SpawnManager {
    boolean spawnStarted = false;
    private int mapid;
    private static Map<Pair<Integer, Integer>, MapleMonster> mobs = new HashMap<Pair<Integer, Integer>, MapleMonster>();
    
    public SpawnManager(){
        
    }
    
}
