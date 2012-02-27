/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.clones;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import client.MapleCharacter;
import constants.ExpTable;
import server.clones.MaplePokemonLibrary;
import java.util.concurrent.ScheduledFuture;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import java.util.*;
import server.movement.LifeMovementFragment;
import server.TimerManager;

/**
 *
 * @author FateJiki
 */
public class MaplePokemon extends MapleMonster{
    public MapleCharacter master;
    public int pokemonid;
    public long lastAttack;
    MaplePokemonLibrary mpl;
    private int level;
    private int uniqueid;
    private int exp;
    
    
    public MaplePokemon(MaplePokemonLibrary mpf, MapleCharacter m, int uniqueid_, int level_, int exp_){
        super(MapleLifeFactory.getMonster(mpf.modid));
        monsterPet = true;
        master = m;
        pokemonid = mpf.id;
        mpl = mpf;
        level = level_;
        uniqueid = uniqueid_;
        exp = exp_;
    }
    
    @Override
    public MapleMapObjectType getType(){
        return MapleMapObjectType.POKEMON;
    }
    
    public int getLevel(){
        return level;
    }
    
    public void talk(){
        // impossible? :o for now methinks
    }
    
    @Override
    public int getExp(){
        return exp;
    }
    
    
    
    public void giveExpToPokemon(int addexp){
        long newexp = addexp + exp;
        if(newexp >= getExpNeededForLevel()){
            master.announce(MaplePacketCreator.showEquipmentLevelUp());
            level++;
            exp = 0;
            if(PokemonEvolutionTable.doesEvolve(level)){
                evolve();
                master.dropMessage(5, "Your pokemon has evolved!");
            } else {
                 master.dropMessage(5, "Your pokemon has leveled up! He will now deal more damage than ever before!");
            }
        } else {
            exp = (int)newexp;
        }
    }
    
    public int getExpNeededForLevel(){
        int retexp = 1;
        int normalExpNeeded = ExpTable.getExpNeededForLevel(Math.min(level, 200));
        exp = normalExpNeeded / 5;
        return retexp;
    }
    
    public void evolve(){
        int[] firstEvo = {5, 7, 8, 9};
        int[] secondEvo = {10, 11, 12, 13};
        int[] thirdEvo = {14, 15, 16, 17, 18, 19};
        int evolveTo;
        if(getLevel() == 25){
            evolveTo = firstEvo[(int)(firstEvo.length * Math.random())];
        } else if(getLevel() == 130){
            evolveTo = secondEvo[(int)(secondEvo.length * Math.random())];
        } else if(getLevel() == 250){
            evolveTo = thirdEvo[(int)(thirdEvo.length * Math.random())];
        } else {
            evolveTo = -1; // wtf
        } if(evolveTo != -1){
            mpl = MaplePokemonLibrary.getById(evolveTo);
            master.getMap().broadcastMessage(MaplePacketCreator.killMonster(getObjectId(), false));
            master.getMap().broadcastMessage(MaplePacketCreator.spawnMonster(this, true));
        }
    }
    
    public void move(){
        boolean canAttack = true;
        boolean attacked = false;
       if((System.currentTimeMillis() - master.getAutobanManager().lastAttack) > 10000){
           canAttack = false;
    } if((System.currentTimeMillis() - lastAttack) < (int)(2500 * mpl.atkRate_)){
        canAttack = false;
    }
    if(canAttack){
        ArrayList<MapleMonster> surroundingMonsters = new ArrayList<MapleMonster>();
        for(MapleMonster m : master.getMap().getMapMonstersInRange(getPosition(), 14999.0, MapleMapObjectType.MONSTER)){
            if(surroundingMonsters.size() < mpl.attackNum && !m.monsterPet){
                surroundingMonsters.add(m);
        }
        }
            
            for(MapleMonster e : surroundingMonsters){
                int damage = (int)(40 * Math.random() + 1) * (int)(level * mpl.damageMod_);
                damage += (int)(100.0 * Math.random());
                master.getMap().broadcastMessage(MaplePacketCreator.damageMonster(e.getObjectId(), damage));
                master.announce(MaplePacketCreator.showMonsterHP(e.getObjectId(), e.getHp() / e.getMaxHp()));
                master.getMap().damageMonster(master, e, damage);
                attacked = false;
            }
    }
}
}
