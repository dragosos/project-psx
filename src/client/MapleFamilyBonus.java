/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

/**
 *
 * @author Rahul
 */
public class MapleFamilyBonus {

    private long timeStarted;
    private double expBonus;
    private double dropBonus;
    private int timeInMins;
    private boolean partyBuff;

    public MapleFamilyBonus(long timeS, int tIM, double expB, double dropB, boolean partyB) {
        timeStarted = timeS;
        this.timeInMins = tIM;
        expBonus = expB;
        dropBonus = dropB;
        partyBuff = partyB;
    }

    public long getTimeStarted() {
        return timeStarted;
    }

    public double getExpBonus() {
        return expBonus;
    }

    public double getDropBonus() {
        return dropBonus;
    }

    public boolean isPartyBuff() {
        return partyBuff;
    }

    public void setTimeStarted(long s) {
        timeStarted = s;
    }

    public void setExpBonus(double s) {
        expBonus = s;
    }

    public void setDropBonus(double s) {
        dropBonus = s;
    }

    public void setPartyBuff(boolean party) {
        partyBuff = party;
    }

    public boolean shouldExpire(long time) {
        int timeStartd = (int)(timeStarted/60000);//convert to mins
        int timeMins = (int)(time/60000);//convert time to mins
        if (timeMins >= timeStartd+timeInMins) {
            return true;
        }
        return false;
    }

    public boolean shouldExpire() {
        return shouldExpire(System.currentTimeMillis());
    }

    public int getDuration() {
        return timeInMins;
    }
}
