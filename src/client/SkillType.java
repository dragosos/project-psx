/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author FateJiki
 */
public enum SkillType {
    MAGICIAN, BOWMAN, WARRIOR, THIEF, EVAN, ARAN, PIRATE, GM, MLWB, MISC, BEGINNER,
    DAWN_WARRIOR, BLAZE_WIZARD, WIND_ARCHER, NIGHT_WALKER, THUNDER_BREAKER, WILD_HUNTER,
    BATTLE_MAGE, MECHANIC, NONE
    
    ;
    
    public static SkillType getSkillTypeBySkill(Skill skill){
        return getSkillTypeByID(skill.getId());
    }
    public static SkillType getSkillTypeByID(int id){
        if(id >= 10000){
        return getSkillTypeByJobID(id / 10000);
        } else {
            return MISC;
        }
    }
    
    public static SkillType getSkillTypeByJobID(int id){
        SkillType ret = SkillType.MISC;
        if (id >= 0 && id < 100){
            ret = SkillType.BEGINNER;
        } else if(id >= 100 && id < 200){
            ret = SkillType.WARRIOR;
        } else if(id >= 200 && id < 300){
            ret = SkillType.MAGICIAN;
        } else if(id >= 300 && id < 400){
            ret = SkillType.BOWMAN;
        } else if(id >= 400 && id < 500){
            ret = SkillType.THIEF; // incl. DB
        } else if(id >= 500 && id < 600){
            ret = SkillType.PIRATE;
        } else if(id == 800){
            ret = SkillType.MLWB;
        } else if(id == 900 || id == 910){
            ret = SkillType.GM;
        } else if(id >= 1100 && id <= 1112){
            ret = SkillType.DAWN_WARRIOR;
        } else if(id >= 1200 && id <= 1212){
            ret = SkillType.BLAZE_WIZARD;
        } else if(id >= 1300 && id <= 1312){
            ret = SkillType.WIND_ARCHER;
        } else if(id >= 1400 && id <= 1412){
            ret = SkillType.NIGHT_WALKER;
        } else if(id >= 1500 && id <= 1512){
            ret = SkillType.THUNDER_BREAKER;
        } else if(id >= 2100 &&  id <= 2112){
            ret = SkillType.ARAN;
        } else if(id >= 2200 && id <= 2218){ // left out 1001 because of the beginner skills
            ret = SkillType.EVAN;
        } else if(id >= 3200 && id < 3300){
            ret = SkillType.BATTLE_MAGE;
        } else if(id >= 3300 && id < 3400){
            ret = SkillType.WILD_HUNTER;
        } else if(id >= 3500 && id < 3600){
            ret = SkillType.MECHANIC;
        } else if(id == 0 || id == 3000 || id == 2000 || id == 2001){
            ret = SkillType.NONE;
        }
        else {
            ret = SkillType.MISC;
        }
        return ret;
    }
}
