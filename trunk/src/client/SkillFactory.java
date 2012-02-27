/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package client;

import constants.SkillConstants.*;
import constants.SkillConstants;
import java.io.File;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleStatEffect;
import java.util.*;
import server.life.Element;
import java.util.concurrent.locks.ReentrantReadWriteLock.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import tools.FileoutputUtil;

public class SkillFactory {
  //  private static Map<Integer, ISkill> skills = new HashMap<Integer, ISkill>();
    private static MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz"));
    private final static int[] bannedSkills = {8000, 8001, 8002, 8003, 10008000, 10008001, 10008002, 10008003, 20008000, 20008001, 20008002, 20008003, 20018000, 20018001, 20018002, 20018003};
    private final static List<Integer> bannedSkillList = new ArrayList<Integer>();
    private final static EnumMap<SkillType, HashMap<Integer, Skill>> skills = new EnumMap<SkillType, HashMap<Integer, Skill>>(SkillType.class);
    private final static ReentrantReadWriteLock locks = new ReentrantReadWriteLock();
    private final static ReadLock readLock = locks.readLock();
    private final static WriteLock writeLock = locks.writeLock();
    
    
    public static ISkill getSkill(int id) {
	if (!skills.isEmpty()) {
            readLock.lock();
            try{
                return skills.get(SkillType.getSkillTypeByID(id)).get(id);
            } finally {
                readLock.unlock();
            }
	}
        System.out.println("Loading Skills:::");
        
	final MapleDataDirectoryEntry root = datasource.getRoot();
        
            for(SkillType s : SkillType.values()){
                skills.put(s, new HashMap<Integer, Skill>());
            }
            
        int skillid;
	for (MapleDataFileEntry topDir : root.getFiles()) { // Loop thru jobs
	    if (topDir.getName().length() <= 8) {
		for (MapleData data : datasource.getData(topDir.getName())) { // Loop thru each jobs
		    if (data.getName().equals("skill")) {
			for (MapleData data2 : data) { // Loop thru each skill id
			    if (data2 != null) {
				skillid = Integer.parseInt(data2.getName());
                                    addSkill(loadFromData(skillid, data2));
			    }
			}
		    }
		}
	    }
	}
        return null;
    }
    
    private static void addSkill(Skill skill){
        writeLock.lock();
        try{
        SkillType type = SkillType.getSkillTypeBySkill(skill);
        skills.get(type).put(skill.getId(), skill);
        } finally {
            writeLock.unlock();
        }
    }
    
    
    public static Map<Integer, ISkill> getAllSkillsByType(SkillType type){
        HashMap<Integer, ISkill> localHM = new HashMap<Integer, ISkill>();
        readLock.lock();
        try{
        for(ISkill skill : skills.get(type).values()){
            localHM.put(skill.getId(), skill);
        }
        } finally {
            readLock.unlock();
        }
        return localHM;
    }

    public static Skill loadFromData(int id, MapleData data) {
        Skill ret = new Skill(id);
        boolean isBuff = false;
        int skillType = MapleDataTool.getInt("skillType", data, -1);
        String elem = MapleDataTool.getString("elemAttr", data, null);
        if (elem != null) {
            ret.element = Element.getFromChar(elem.charAt(0));
        } else {
            ret.element = Element.NEUTRAL;
        }
        MapleData effect = data.getChildByPath("effect");
        if (skillType != -1) {
            if (skillType == 2) {
                isBuff = true;
            }
        } else {
            MapleData action_ = data.getChildByPath("action");
            boolean action = false;
	    if (action_ == null) {
		if (data.getChildByPath("prepare/action") != null) {
		    action = true;
		} else {
		    switch (id) {
			case 5201001:
			case 5221009:
			    action = true;
			    break;
		    }
		}
	    } else {
		action = true;
	    }
	    ret.action = action;
            MapleData hit = data.getChildByPath("hit");
            MapleData ball = data.getChildByPath("ball");
            isBuff = effect != null && hit == null && ball == null;
            isBuff |= action_ != null && MapleDataTool.getString("0", action_, "").equals("alert2");
            switch (id) {
                case Hero.RUSH:
                case Paladin.RUSH:
                case DarkKnight.RUSH:
                case DragonKnight.SACRIFICE:
                case FPMage.EXPLOSION:
                case FPMage.POISON_MIST:
                case Cleric.HEAL:
                case Ranger.MORTAL_BLOW:
                case Sniper.MORTAL_BLOW:
                case Assassin.DRAIN:
                case Hermit.SHADOW_WEB:
                case Bandit.STEAL:
                case ChiefBandit.CHAKRA:
                case SuperGM.HEAL_PLUS_DISPEL:
                case Hero.MONSTER_MAGNET:
                case DarkKnight.MONSTER_MAGNET:
                case Gunslinger.RECOIL_SHOT:
                case Marauder.ENERGY_DRAIN:
                case BlazeWizard.FLAME_GEAR:
                case NightWalker.SHADOW_WEB:
                case NightWalker.POISON_BOMB:
                case NightWalker.VAMPIRE:
                    isBuff = false;
                    break;
                case Beginner.RECOVERY:
                case Beginner.MONSTER_RIDER:
                case Beginner.ECHO_OF_HERO:
                case Fighter.WEAPON_BOOSTER:
                case Fighter.POWER_GUARD:
                case Crusader.COMBO:
                case Hero.MAPLE_WARRIOR:
                case Hero.STANCE:
                case Page.WEAPON_BOOSTER:
                case Page.POWER_GUARD:
                case Page.THREATEN:
                case Paladin.SWORD_HOLY_CHARGE:
                case Paladin.MAPLE_WARRIOR:
                case Spearman.HYPER_BODY:
                case Spearman.SPEAR_BOOSTER:
                case DragonKnight.DRAGON_BLOOD:
                case DarkKnight.BEHOLDER:
                case DarkKnight.HEX_OF_BEHOLDER:
                case DarkKnight.MAPLE_WARRIOR:
                case Magician.MAGIC_GUARD:
                case FPWizard.SLOW:
                case FPMage.SEAL:
                case FPMage.SPELL_BOOSTER:
                case ILArchMage.ELQUINES:
                case FPArchMage.INFINITY:
                case FPArchMage.MANA_REFLECTION:
                case FPArchMage.MAPLE_WARRIOR:
                case ILMage.SEAL:
                case ILWizard.SLOW:
                case ILMage.SPELL_BOOSTER:
                case FPArchMage.IFRIT:
                case ILArchMage.INFINITY:
                case ILArchMage.MANA_REFLECTION:
                case ILArchMage.MAPLE_WARRIOR:
                case Cleric.INVINCIBLE:
                case Cleric.BLESS:
                case Priest.DOOM:
                case Priest.HOLY_SYMBOL:
                case Priest.SUMMON_DRAGON:
                case Bishop.BAHAMUT:
                case Bishop.HOLY_SHIELD:
                case Bishop.INFINITY:
                case Bishop.MANA_REFLECTION:
                case Bishop.MAPLE_WARRIOR:
                case Hunter.BOW_BOOSTER:
                case Ranger.PUPPET:
                case Ranger.SILVER_HAWK:
                case Bowmaster.CONCENTRATE:
                case Bowmaster.MAPLE_WARRIOR:
                case Bowmaster.PHOENIX:
                case Bowmaster.SHARP_EYES:
                case Crossbowman.CROSSBOW_BOOSTER:
                case Crossbowman.SOUL_ARROW:
                case Sniper.GOLDEN_EAGLE:
                case Sniper.PUPPET:
                case Marksman.BLIND:
                case Marksman.FROST_PREY:
                case Marksman.MAPLE_WARRIOR:
                case Marksman.SHARP_EYES:
                case Rogue.DARK_SIGHT:
                case Assassin.CLAW_BOOSTER:
                case Hermit.MESO_UP:
                case Hermit.SHADOW_PARTNER:
                case 4111007:
                case 4211007:
                case NightLord.MAPLE_WARRIOR:
                case NightLord.NINJA_AMBUSH:
                case NightLord.SHADOW_STARS:
                case Bandit.DAGGER_BOOSTER:
                case ChiefBandit.MESO_GUARD:
                case ChiefBandit.PICKPOCKET:
                case Shadower.MAPLE_WARRIOR:
                case Shadower.NINJA_AMBUSH:
                case Pirate.DASH:
                case Brawler.KNUCKLER_BOOSTER:
                case Brawler.OAK_BARREL:
                case Marauder.TRANSFORMATION:
                case Buccaneer.SUPER_TRANSFORMATION:
                case Buccaneer.SPEED_INFUSION:
                case Buccaneer.MAPLE_WARRIOR:
                case Outlaw.GAVIOTA:
                case Outlaw.OCTOPUS:
                case Corsair.BATTLE_SHIP:
                case Corsair.WRATH_OF_THE_OCTOPI:
                case GM.HIDE:
                case SuperGM.HOLY_SYMBOL:
                case SuperGM.HIDE:
                case SuperGM.HYPER_BODY:
                case Noblesse.ECHO_OF_HERO:
                case Noblesse.MONSTER_RIDER:
                case Noblesse.RECOVERY:
                case DawnWarrior.COMBO:
                case DawnWarrior.FINAL_ATTACK:
                case DawnWarrior.SOUL:
                case DawnWarrior.SOUL_CHARGE:
                case DawnWarrior.SWORD_BOOSTER:
                case BlazeWizard.ELEMENTAL_RESET:
                case BlazeWizard.FLAME:
                case BlazeWizard.IFRIT:
                case BlazeWizard.MAGIC_GUARD:
                case BlazeWizard.SLOW:
                case BlazeWizard.SPELL_BOOSTER:
                case WindArcher.BOW_BOOSTER:
                case WindArcher.FINAL_ATTACK:
                case WindArcher.PUPPET:
                case WindArcher.SOUL_ARROW:
                case WindArcher.STORM:
                case WindArcher.WIND_WALK:
                case WindArcher.EAGLE_EYE:
                case NightWalker.CLAW_BOOSTER:
                case NightWalker.DARKNESS:
                case NightWalker.DARK_SIGHT:
                case NightWalker.SHADOW_PARTNER:
                case ThunderBreaker.DASH:
                case ThunderBreaker.KNUCKLER_BOOSTER:
                case ThunderBreaker.LIGHTNING:
                case ThunderBreaker.LIGHTNING_CHARGE:
                case ThunderBreaker.SPARK:
                case ThunderBreaker.SPEED_INFUSION:
                case Aran.POLEARM_BOOSTER:
                case Aran.COMBO_DRAIN:
                case Aran.BODY_PRESSURE:
                case Aran.SNOW_CHARGE:
                case Aran.SMART_KNOCKBACK:
                case Aran.MAPLE_WARRIOR:
                case Aran.FREEZE_STANDING:
                case Aran.COMBO_BARRIER:
                case Outlaw.HOMING_BEACON:
                case Corsair.BULLSEYE:
                case Evan.NIMBLE_FEET:
                case Evan.RECOVER:
                case Evan.MONSTER_RIDER:
                case Evan.HEROS_ECHO:
                case Evan.DRAGON_FURY:
                case Evan.MAGIC_SHIELD:
                case Evan.MAGIC_BOOSTER:
                case Evan.KILLER_WINGS:
                case Evan.MAGIC_RESISTANCE:
                case Evan.MAPLE_WARRIOR:
                case Evan.ELEMENTAL_RESET:
                case Evan.BLESSING_OF_THE_ONYX:
                case Evan.SLOW:
                case 1034:
                case 10001034:
                case 20001034:
                case 20011034:
                case BattleMage.DARK_AURA:
                case BattleMage.YELLOW_AURA:
                case BattleMage.BLUE_AURA:
                case BattleMage.STAFF_BOOST:
                case BattleMage.BLOOD_DRAIN:
                case BattleMage.ADVANCED_BLUE_AURA:
                case BattleMage.BODY_BOOST:
                case BattleMage.CONVERSION:
                case BattleMage.SUMMON_REAPER_BUFF:
                case BattleMage.TELEPORT_MASTERY:
                case BattleMage.ADVANCED_DARK_AURA:
                case BattleMage.ADVANCED_YELLOW_AURA:
                case BattleMage.STANCE:
                case BattleMage.PARTY_SHIELD:
                case BattleMage.HEROS_WILL:
                case BattleMage.MAPLE_WARRIOR:
                case BattleMage.TWISTER_SPIN:
                case WildHunter.CROSSBOW_BOOSTER:
                case WildHunter.JAGUAR_RIDER:
                case WildHunter.SOUL_ARROW_CROSSBOW:
                case WildHunter.WILD_TRAP:
                case WildHunter.SILVER_HAWK:
                case WildHunter.FELINE_BERSERK:
                case WildHunter.MAPLE_WARRIOR:
                case WildHunter.SHARP_EYES:
                case WildHunter.BLIND:
                case Mechanic.PROTOTYPE:
                case Mechanic.SIEGE_MODE:
                case Mechanic.SIEGE_MODE2:
                case Mechanic.MAPLE_WARRIOR:
                case Mechanic.AMPLIFIER:
                case Mechanic.SATELLITE1:
                case Mechanic.SATELLITE2:
                case Mechanic.SATELLITE3:
                case Mechanic.ACCELERATION_BOT:
                case Mechanic.HEALING_ROBOT:
                case Mechanic.ROCK_N_SHOCK:
                case Mechanic.ROLL_OF_THE_DICE:
                case Mechanic.MECHANIC_RAGE:
                case Mechanic.PERFECT_ARMOR:
                case Mechanic.OPEN_PORTAL:
                case Mechanic.SATELLITE_SAFETY:
                case Mechanic.GIANT_ROBOT:
                case Mechanic.BOTS_N_TOTS:
                case BladeLord.MIRROR_IMAGE:
                    isBuff = true;
                    break;
            }
        }
        MapleData common = data.getChildByPath("common");
        if(common != null){
        int maxLevel = MapleDataTool.getIntConvert("maxLevel", common);
        MapleStatEffect mse = null;
        for (int i = 0; i < maxLevel; i++) {
            mse = MapleStatEffect.loadSkillEffectFromData(data.getChildByPath("common"), id, isBuff, i + 1);
            ret.effects.add(mse);
        }
        } else { // gm skills..
            ret.effects.add(MapleStatEffect.loadSkillEffectFromData(data.getChildByPath("level").getChildByPath("1"), id, isBuff, 1));
        }
        ret.animationTime = 0;
        if (effect != null) {
            for (MapleData effectEntry : effect) {
                if(effectEntry.getChildByPath("delay") != null){
                ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
                }
            }
        }
        return ret;
    }

    public static String getSkillName(int skillid) {
        MapleData data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img");
        StringBuilder skill = new StringBuilder();
        skill.append(String.valueOf(skillid));
        if (skill.length() == 4) {
            skill.delete(0, 4);
            skill.append("000").append(String.valueOf(skillid));
        }
        return MapleDataTool.getString("name" ,data.getChildByPath(skill.toString()));
    }
    
    public static void logAllHairsList(){
         MapleDataProvider hairsource = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Character.wz/Hair"));
         StringBuilder hairs = new StringBuilder();
         hairs.append("[");
         for(MapleDataFileEntry md : hairsource.getRoot().getFiles()){
             hairs.append(md.getName());
             hairs.append(", ");
         }
         hairs.append("]");
         System.out.println(hairs);
         FileoutputUtil.log("hair", hairs.toString(), null);
    }
}
