/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.life.farming;
import server.life.MapleMonster;
import server.life.MapleLifeFactory;
import client.MapleCharacter;

/**
 *
 * @author FateJiki
 */
public class MapleFarmMonster extends MapleMonster {
    int mobid;
    int farmid;
    int accountid;
    long birthDate;
    long deathDate; // randomly, but they do have a default amount of time to live.. <3
    byte gender;
    private MapleFarmHouse house;
    
    public MapleFarmMonster(int _farmid, int _mobid, int accid, long _birthDate, int _gender, MapleFarmHouse _house){ // for NEW farm
        super(MapleLifeFactory.getMonster(_mobid));
        mobid = _mobid;
        farmid = _farmid;
        accountid = accid;;
        birthDate = _birthDate;
        deathDate = FarmAnimalLibrary.getByMid(mobid).generateDeathDate(_mobid);
        gender = (byte)_gender;
        house = _house;
    }
    
    public MapleFarmMonster(int _farmid, int _mobid, int accid, long _deathDate, long _birthDate, int _gender, MapleFarmHouse _house){ // for OLD (ALREADY EXISTING ONES!) ANIMALS
        super(MapleLifeFactory.getMonster(_mobid));
        mobid = _mobid;
        farmid = _farmid;
        accountid = accid;;
        birthDate = _birthDate;
        deathDate = _deathDate;
        gender = (byte)_gender;
    }
    
    
    public int getAgeDays(){
        return (int)(((((System.currentTimeMillis() - birthDate) / 1000) / 60) / 60) / 24);
    }
    
    public byte getGender(){
        return gender;
    }
    
    public boolean isMale(){
        return gender == (byte)0;
    }
    
    public boolean isFemale(){
        return gender == (byte)1;
    }
    
}
