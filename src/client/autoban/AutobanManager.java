/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.autoban;

import client.MapleCharacter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kevintjuh93
 */
public class AutobanManager {
    public MapleCharacter chr;
    public Map<AutobanFactory, Integer> points = new HashMap<AutobanFactory, Integer>();
    private Map<AutobanFactory, Long> lastTime = new HashMap<AutobanFactory, Long>();
    private int misses = 0;
    private int lastmisses = 0;
    private int samemisscount = 0;
    private long spam[] = new long[20];
    private int timestamp[] = new int[20];
    private byte timestampcounter[] = new byte[20];
    public long lastAttack = 0;
    public long lastMove = 0;
    public long lastPetBuff = 0;
    public long lastSummonAttack = 0;
    public short unlimitedAttackCounter = 0;
    public long lastSave = 0;
    public boolean isBanned = false;
    public long lastBambooRain = 0;
    
    


    public AutobanManager(MapleCharacter chr) {
        this.chr = chr;
    }

    public void addPoint(AutobanFactory fac, String reason) {
        if (!isGM()) {
        if (lastTime.containsKey(fac)) {
            if (lastTime.get(fac) < (System.currentTimeMillis() - fac.getExpire())) {
                points.put(fac, points.get(fac) / 2); //So the points are not completely gone.
            }
        }
        if (fac.getExpire() != -1) {
            lastTime.put(fac, System.currentTimeMillis());
        }
        
        //foolproof this one
        if (fac.equals(AutobanFactory.DAMAGE_HACKING)) {
            isBanned = true;
            chr.autoban("Autobanned for " + fac.name() + " ;" + reason, 1);
            chr.sendPolice("You have been temporarily blocked by #bThe Maple-Leaf Watch Brigade#k for the #eHACK reason#k. \r\n\r\n @eYour account is currently pending investigation by a GM.");
        }
        }
    }
    
    public int calculateNumPoints(){
        int pts = 0;
        for(AutobanFactory fac : points.keySet()){
            pts += points.get(fac);
        }
        
        return pts;
    }
    
    public boolean isGM() {
        return chr.isGM();
    }
    
    public void permBan(String reason, boolean mac){
        
    }
    public boolean checkSave(){
        return (System.currentTimeMillis() - lastSave) > (1000 * 60 * 25);
    }
    public void addMiss() {
        this.misses++;
    }

    public void resetMisses() {
        if (lastmisses == misses && misses > 6) {
            samemisscount++;
        }
        if (samemisscount > 4)
            chr.autoban("Autobanned for : " + misses + " Miss godmode", 1);
        else if (samemisscount > 0)

        this.lastmisses = misses;
        this.misses = 0;
    }
    
    //Don't use the same type for more than 1 thing
    public void spam(int type) {
        this.spam[type] = System.currentTimeMillis();
    }

    public long getLastSpam(int type) {
        return spam[type];
    }

    /**
     * Timestamp checker
     *
     *  <code>type</code>:<br>
     * 0: HealOverTime<br>
     * 1: Pet Food<br>
     * 2: ItemSort<br>
     * 3: ItemIdSort<br>
     * 4: SpecialMove<br>
     * 5: UseCatchItem<br>
     *
     * @param type type
     * @return Timestamp checker
     */
    public void setTimestamp(int type, int time) {
        if (this.timestamp[type] == time) {  
            this.timestampcounter[type]++;
            if (this.timestampcounter[type] >= 3) {
           //     chr.getClient().disconnect();
                //System.out.println("Same timestamp for type: " + type + "; Character: " + chr);
            }
            return;
        }
        this.timestamp[type] = time;
    }
}
