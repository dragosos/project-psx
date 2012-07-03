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
package server;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import client.IItem;
import client.ISkill;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleMount;
import client.MapleStat;
import client.SkillFactory;
import client.autoban.AutobanFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.ItemConstants;
import net.channel.ChannelServer;
import provider.MapleData;
import provider.MapleDataTool;
import constants.SkillConstants.*;
import server.life.MapleMonster;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import net.world.PlayerCoolDownValueHolder;
import java.util.EnumMap;
import tools.MaplePacketCreator;
import tools.Pair;
import java.sql.*;
import net.MaplePacket;
import constants.SkillConstants;
import server.maps.FieldLimit;
import tools.DatabaseConnection;

/**
 * @author Matze
 * @author Frz
 */
public class MapleStatEffect implements Serializable {

    private static final long serialVersionUID = 3692756402846632237L;
    private short watk, matk, wdef, mdef, acc, avoid, speed, jump;
    private short hp, mp;
    private double hpR, mpR;
    private short mpCon, hpCon;
    private int duration;
    private boolean overTime;
    private int sourceid;
    private int moveTo;
    private boolean skill;
    private List<Pair<MapleBuffStat, Integer>> statups;
    private Map<MonsterStatus, Integer> monsterStatus;
    private int x, y;
    private double prop;
    private int itemCon, itemConNo;
    private int damage, attackCount, bulletCount, bulletConsume, fixdamage;
    private Point lt, rb;
    private int mobCount;
    private int moneyCon;
    private int cooldown;
    private int morphId = 0;
    private int ghost;
    private int fatigue;

    public static MapleStatEffect loadSkillEffectFromData(MapleData source, int skillid, boolean overtime, int level) {
        return loadFromData(source, skillid, true, overtime, level);
    }

    public static MapleStatEffect loadItemEffectFromData(MapleData source, int itemid, int level) {
        return loadFromData(source, itemid, false, false, level);
    }

    private static void addBuffStatPairToListIfNotZero(List<Pair<MapleBuffStat, Integer>> list, MapleBuffStat buffstat, Integer val) {
        if (val.intValue() != 0) {
            list.add(new Pair<MapleBuffStat, Integer>(buffstat, val));
        }
    }

    private static MapleStatEffect loadFromData(MapleData source, int sourceid, boolean skill, boolean overTime, int level) {
        MapleStatEffect ret = new MapleStatEffect();
        ret.duration = MapleDataTool.getInt("time", source, 0, level) * 3;
        // ret.duration = 100000000;
        if (ret.duration < 60) {
            ret.duration = 60 * 5; // 5 minutes
        }

        ret.hp = (short) MapleDataTool.getInt("hp", source, 0, level);
        ret.mp = (short) MapleDataTool.getInt("mp", source, 0, level);
        if (skill) {
            ret.hpR = MapleDataTool.getInt("x", source, 0, level) / 100.0;
            ret.mpR = MapleDataTool.getInt("y", source, 0, level) / 100.0;
        } else {
            ret.hpR = MapleDataTool.getInt("hpR", source, 0, 1);
            ret.mpR = MapleDataTool.getInt("mpR", source, 0, 1);
        }
        ret.mpCon = (short) (MapleDataTool.getInt("mpCon", source, 0, level) / 3);
        ret.hpCon = (short) (MapleDataTool.getInt("hpCon", source, 0, level) / 3);
        if (sourceid == BladeLord.MIRROR_IMAGE) {
            //  System.out.println("MPCON : " + ret.mpCon + " || hpCON : " + ret.hpCon + " DURATION : " + ret.duration);
        }
        int iprop = MapleDataTool.getInt("prop", source, 100, level);
        ret.prop = iprop / 100.0;
        ret.mobCount = MapleDataTool.getInt("mobCount", source, 1, level);
        ret.cooldown = 0;//MapleDataTool.getInt("cooltime", source, 0, level);
        ret.morphId = MapleDataTool.getInt("morph", source, 0, level);
        ret.ghost = MapleDataTool.getInt("ghost", source, 0, level);
        ret.fatigue = MapleDataTool.getInt("incFatigue", source, 0, level);
        ret.sourceid = sourceid;
        ret.skill = skill;
        if (!ret.skill && ret.duration > -1) {
            ret.overTime = true;
        } else {
            ret.duration *= 1000; // items have their times stored in ms, of course
            ret.overTime = overTime;
        }
        ArrayList<Pair<MapleBuffStat, Integer>> statups = new ArrayList<Pair<MapleBuffStat, Integer>>();

        ret.watk = (short) MapleDataTool.getInt("pad", source, 0, level);
        ret.wdef = (short) MapleDataTool.getInt("pdd", source, 0, level);
        ret.matk = (short) MapleDataTool.getInt("mad", source, 0, level);
        ret.mdef = (short) MapleDataTool.getInt("mdd", source, 0, level);
        ret.acc = (short) MapleDataTool.getIntConvert("acc", source, 0, level);
        ret.avoid = (short) MapleDataTool.getInt("eva", source, 0, level);
        ret.speed = (short) MapleDataTool.getInt("speed", source, 0, level);
        ret.jump = (short) MapleDataTool.getInt("jump", source, 0, level);
        if (ret.overTime && ret.getSummonMovementType() == null) {
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WATK, Integer.valueOf(ret.watk));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WDEF, Integer.valueOf(ret.wdef));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MATK, Integer.valueOf(ret.matk));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MDEF, Integer.valueOf(ret.mdef));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ACC, Integer.valueOf(ret.acc));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.AVOID, Integer.valueOf(ret.avoid));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.SPEED, Integer.valueOf(ret.speed));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.JUMP, Integer.valueOf(ret.jump));
        }
        MapleData ltd = source.getChildByPath("lt");
        if (ltd != null) {
            ret.lt = (Point) ltd.getData();
            ret.rb = (Point) source.getChildByPath("rb").getData();
        }
        int x = MapleDataTool.getInt("x", source, 0, level);
        ret.x = x;
        if (sourceid == BladeLord.MIRROR_IMAGE) {
            //  System.out.println("X : " + x);
        }
        ret.y = MapleDataTool.getInt("y", source, 0, level);
        ret.damage = MapleDataTool.getIntConvert("damage", source, 100, level);
        ret.fixdamage = MapleDataTool.getIntConvert("fixdamage", source, -1, level);
        ret.attackCount = MapleDataTool.getIntConvert("attackCount", source, 1, level);
        ret.bulletCount = MapleDataTool.getIntConvert("bulletCount", source, 1, level);
        ret.bulletConsume = MapleDataTool.getIntConvert("bulletConsume", source, 0, level);
        ret.moneyCon = MapleDataTool.getIntConvert("moneyCon", source, 0, level);
        ret.itemCon = MapleDataTool.getInt("itemCon", source, 0, level);
        ret.itemConNo = MapleDataTool.getInt("itemConNo", source, 0, level);
        ret.moveTo = MapleDataTool.getInt("moveTo", source, -1, level);
        EnumMap<MonsterStatus, Integer> monsterStatus = new EnumMap<MonsterStatus, Integer>(MonsterStatus.class);
        if (skill) {
            switch (sourceid) {
                // BEGINNER
                case SkillConstants.Beginner.RECOVERY:
                case Noblesse.RECOVERY:
                case Legend.RECOVERY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.RECOVERY, Integer.valueOf(x)));
                    break;
                case SkillConstants.Beginner.ECHO_OF_HERO:
                case Noblesse.ECHO_OF_HERO:
                case Legend.ECHO_OF_HERO:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ECHO_OF_HERO, Integer.valueOf(ret.x)));
                    break;
                case SkillConstants.Beginner.MONSTER_RIDER:
                case Noblesse.MONSTER_RIDER:
                case Legend.MONSTER_RIDER:
                case SkillConstants.Corsair.BATTLE_SHIP:
                case SkillConstants.Beginner.YETI_MOUNT1:
                case Noblesse.YETI_MOUNT1:
                case Legend.YETI_MOUNT1:
                case SkillConstants.Beginner.OSTRICH:
                case Noblesse.OSTRICH:
                case Legend.OSTRICH:
                case Citizen.OSTRICH:
                case SkillConstants.Beginner.CROCO:
                case Noblesse.CROCO:
                case Legend.CROCO:
                case Citizen.CROCO:
                case SkillConstants.Beginner.WITCH_BROOMSTICK:
                case Noblesse.WITCH_BROOMSTICK:
                case Legend.WITCH_BROOMSTICK:
                case Citizen.WITCH_BROOMSTICK:
                case SkillConstants.Beginner.BALROG_MOUNT:
                case Noblesse.BALROG_MOUNT:
                case Legend.BALROG_MOUNT:
                case Citizen.BALROG_MOUNT:
                case SkillConstants.Beginner.CHICKEN:
                case Legend.CHICKEN:
                case Noblesse.CHICKEN:
                case Citizen.CHICKEN:
                case SkillConstants.Beginner.OWL:
                case Legend.OWL:
                case Noblesse.OWL:
                case Citizen.OWL:
                case SkillConstants.Beginner.MOTHERSHIP:
                case Legend.MOTHERSHIP:
                case Noblesse.MOTHERSHIP:
                case Citizen.MOTHERSHIP:
                case SkillConstants.Beginner.OS3A_MACHINE:
                case Legend.OS3A_MACHINE:
                case Noblesse.OS3A_MACHINE:
                case Citizen.OS3A_MACHINE:
                case SkillConstants.Beginner.OS4_SHUTTLE:
                case Legend.OS4_SHUTTLE:
                case Noblesse.OS4_SHUTTLE:
                case Citizen.OS4_SHUTTLE:
                case SkillConstants.Beginner.ROGUE_RABBIT:
                case Legend.ROGUE_RABBIT:
                case Noblesse.ROGUE_RABBIT:
                case Citizen.ROGUE_RABBIT:
                case SkillConstants.Beginner.SMALL_RABBIT:
                case Legend.SMALL_RABBIT:
                case Noblesse.SMALL_RABBIT:
                case Citizen.SMALL_RABBIT:
                case SkillConstants.Beginner.LEONARDO_THE_LION:
                case Legend.LEONARDO_THE_LION:
                case Noblesse.LEONARDO_THE_LION:
                case Citizen.LEONARDO_THE_LION:
                case SkillConstants.Beginner.RACE_KART:
                case Legend.RACE_KART:
                case Noblesse.RACE_KART:
                case Citizen.RACE_KART:
                case SkillConstants.Beginner.ZD_TIGER:
                case Legend.ZD_TIGER:
                case Noblesse.ZD_TIGER:
                case Citizen.ZD_TIGER:
                case SkillConstants.Beginner.MIST_BALROG:
                case Legend.MIST_BALROG:
                case Noblesse.MIST_BALROG:
                case Citizen.MIST_BALROG:
                case SkillConstants.Beginner.SHINJO:
                case Legend.SHINJO:
                case Noblesse.SHINJO:
                case Citizen.SHINJO:
                case SkillConstants.Beginner.ORANGE_MUSHROOM:
                case Legend.ORANGE_MUSHROOM:
                case Noblesse.ORANGE_MUSHROOM:
                case Citizen.ORANGE_MUSHROOM:
                case SkillConstants.Beginner.NIGHTMARE:
                case Legend.NIGHTMARE:
                case Noblesse.NIGHTMARE:
                case Citizen.NIGHTMARE:
                case SkillConstants.Beginner.YETI:
                case Legend.YETI:
                case Noblesse.YETI:
                case Citizen.YETI:
                case SkillConstants.Beginner.PINK_BEAR_HOT_AIR_BALLOON:
                case Legend.PINK_BEAR_HOT_AIR_BALLOON:
                case Noblesse.PINK_BEAR_HOT_AIR_BALLOON:
                case Citizen.PINK_BEAR_HOT_AIR_BALLOON:
                case SkillConstants.Beginner.TRANSFORMATION_ROBOT:
                case Noblesse.TRANSFORMATION_ROBOT:
                case Legend.TRANSFORMATION_ROBOT:
                case Citizen.TRANSFORMATION_ROBOT:
                case SkillConstants.Beginner.MOTORCYCLE:
                case Legend.MOTORCYCLE:
                case Noblesse.MOTORCYCLE:
                case Citizen.MOTORCYCLE:
                case SkillConstants.Beginner.POWER_SUIT:
                case Legend.POWER_SUIT:
                case Noblesse.POWER_SUIT:
                case Citizen.POWER_SUIT:
                case SkillConstants.Beginner.NIMBUS_CLOUD:
                case Legend.NIMBUS_CLOUD:
                case Noblesse.NIMBUS_CLOUD:
                case Citizen.NIMBUS_CLOUD:
                case SkillConstants.Beginner.CHARGE_WOODEN_PONY:
                case Legend.CHARGE_WOODEN_PONY:
                case Noblesse.CHARGE_WOODEN_PONY:
                case Citizen.CHARGE_WOODEN_PONY:
                case SkillConstants.Beginner.BLACK_SCOOTER:
                case Legend.BLACK_SCOOTER:
                case Noblesse.BLACK_SCOOTER:
                case Citizen.BLACK_SCOOTER:
                case SkillConstants.Beginner.PINK_SCOOTER:
                case Legend.PINK_SCOOTER:
                case Noblesse.PINK_SCOOTER:
                case Citizen.PINK_SCOOTER:
                case SkillConstants.Beginner.GARGOYLE:
                case Legend.GARGOYLE:
                case Noblesse.GARGOYLE:
                case Citizen.GARGOYLE:
                case SkillConstants.Beginner.SPACESHIP:
                case Legend.SPACESHIP:
                case Noblesse.SPACESHIP:
                case Citizen.SPACESHIP:
                case SkillConstants.Beginner.UNICORN:
                case Legend.UNICORN:
                case Noblesse.UNICORN:
                case Citizen.UNICORN:
                case SkillConstants.Beginner.RED_TRUCK:
                case Legend.RED_TRUCK:
                case Noblesse.RED_TRUCK:
                case Citizen.RED_TRUCK:
                case SkillConstants.Beginner.LOW_RIDER:
                case Legend.LOW_RIDER:
                case Noblesse.LOW_RIDER:
                case Citizen.LOW_RIDER:
                case SkillConstants.Beginner.LION:
                case Legend.LION:
                case Noblesse.LION:
                case Citizen.LION:
                case SkillConstants.Beginner.YETI_MOUNT:
                case Legend.YETI_MOUNT:
                case Noblesse.YETI_MOUNT:
                case Citizen.YETI_MOUNT:
                case Noblesse.YETI_MOUNT2:
                case Legend.YETI_MOUNT2:

                case WildHunter.JAGUAR_RIDER:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, Integer.valueOf(sourceid)));
                    break;
                case SkillConstants.Beginner.BERSERK_FURY:
                case Noblesse.BERSERK_FURY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BERSERK_FURY, Integer.valueOf(1)));
                    break;
                case SkillConstants.Beginner.INVINCIBLE_BARRIER:
                case Noblesse.INVINCIBLE_BARRIER:
                case Legend.INVICIBLE_BARRIER:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DIVINE_BODY, Integer.valueOf(1)));
                    break;
                case Fighter.POWER_GUARD:
                case Page.POWER_GUARD:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.POWERGUARD, Integer.valueOf(x)));
                    break;
                case Spearman.HYPER_BODY:
                case GM.HYPER_BODY:
                case SuperGM.HYPER_BODY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HYPERBODYHP, Integer.valueOf(x)));
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HYPERBODYMP, Integer.valueOf(ret.y)));
                    break;
                case SkillConstants.Crusader.COMBO:
                case SkillConstants.DawnWarrior.COMBO:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, Integer.valueOf(1)));
                    break;
                case WhiteKnight.BW_FIRE_CHARGE:
                case WhiteKnight.BW_ICE_CHARGE:
                case WhiteKnight.BW_LIT_CHARGE:
                case WhiteKnight.SWORD_FIRE_CHARGE:
                case WhiteKnight.SWORD_ICE_CHARGE:
                case WhiteKnight.SWORD_LIT_CHARGE:
                case Paladin.BW_HOLY_CHARGE:
                case Paladin.SWORD_HOLY_CHARGE:
                case SkillConstants.DawnWarrior.SOUL_CHARGE:
                case ThunderBreaker.LIGHTNING_CHARGE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.WK_CHARGE, Integer.valueOf(x)));
                    break;
                case SkillConstants.DragonKnight.DRAGON_BLOOD:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DRAGONBLOOD, Integer.valueOf(ret.x)));
                    break;
                case SkillConstants.DragonKnight.DRAGON_ROAR:
                    ret.hpR = -x / 100.0;
                    break;
                case Hero.STANCE:
                case Paladin.STANCE:
                case SkillConstants.DarkKnight.STANCE:
                case SkillConstants.Aran.FREEZE_STANDING:
                case BattleMage.STANCE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.STANCE, Integer.valueOf(iprop)));
                    break;
                case SkillConstants.DawnWarrior.FINAL_ATTACK:
                case WindArcher.FINAL_ATTACK:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINALATTACK, Integer.valueOf(x)));
                    break;
                // MAGICIAN
                case Magician.MAGIC_GUARD:
                case SkillConstants.BlazeWizard.MAGIC_GUARD:
                case SkillConstants.Evan.MAGIC_GUARD:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_GUARD, Integer.valueOf(x)));
                    break;
                case SkillConstants.Cleric.INVINCIBLE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INVINCIBLE, Integer.valueOf(x)));
                    break;
                case Priest.HOLY_SYMBOL:
                case SuperGM.HOLY_SYMBOL:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SYMBOL, Integer.valueOf(x)));
                    break;
                case FPArchMage.INFINITY:
                case ILArchMage.INFINITY:
                case SkillConstants.Bishop.INFINITY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INFINITY, Integer.valueOf(x)));
                    break;
                case FPArchMage.MANA_REFLECTION:
                case ILArchMage.MANA_REFLECTION:
                case SkillConstants.Bishop.MANA_REFLECTION:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MANA_REFLECTION, Integer.valueOf(1)));
                    break;
                case SkillConstants.Bishop.HOLY_SHIELD:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SHIELD, Integer.valueOf(x)));
                    break;
                // BOWMAN
                case Priest.MYSTIC_DOOR:
                case Hunter.SOUL_ARROW:
                case SkillConstants.Crossbowman.SOUL_ARROW:
                case WindArcher.SOUL_ARROW:
                case WildHunter.SOUL_ARROW_CROSSBOW:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOULARROW, Integer.valueOf(x)));
                    break;
                case Ranger.PUPPET:
                case Sniper.PUPPET:
                case WindArcher.PUPPET:
                case Outlaw.OCTOPUS:
                case SkillConstants.Corsair.WRATH_OF_THE_OCTOPI:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PUPPET, Integer.valueOf(1)));
                    break;
                case SkillConstants.Bowmaster.CONCENTRATE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.CONCENTRATE, x));
                    break;
                case SkillConstants.Bowmaster.HAMSTRING:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HAMSTRING, Integer.valueOf(x)));
                    monsterStatus.put(MonsterStatus.SPEED, x);
                    break;
                case Marksman.BLIND:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLIND, Integer.valueOf(x)));
                    monsterStatus.put(MonsterStatus.ACC, x);
                    break;
                case SkillConstants.Bowmaster.SHARP_EYES:
                case Marksman.SHARP_EYES:
                case WildHunter.SHARP_EYES:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHARP_EYES, Integer.valueOf(ret.x << 8 | ret.y)));
                    break;
                // THIEF
                case Rogue.DARK_SIGHT:
                case WindArcher.WIND_WALK:
                case NightWalker.DARK_SIGHT:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, Integer.valueOf(x)));
                    break;
                case Hermit.MESO_UP:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOUP, Integer.valueOf(x)));
                    break;
                case Hermit.SHADOW_PARTNER:
                case NightWalker.SHADOW_PARTNER:
                case 4211008: // chief bandit's shadow partner
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, Integer.valueOf(x)));
                    break;
                case SkillConstants.ChiefBandit.MESO_GUARD:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOGUARD, Integer.valueOf(x)));
                    break;
                case SkillConstants.ChiefBandit.PICKPOCKET:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PICKPOCKET, Integer.valueOf(x)));
                    break;
                case NightLord.SHADOW_STARS:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOW_CLAW, Integer.valueOf(0)));
                    break;
                // PIRATE
                //case Pirate.DASH:
                ///  case ThunderBreaker.DASH:
                // case SkillConstants.Beginner.SPACE_DASH:
                /// case Noblesse.SPACE_DASH:
                ///    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH2, Integer.valueOf(ret.x)));
                //    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH, Integer.valueOf(ret.y)));
                //     break;
                case SkillConstants.Corsair.SPEED_INFUSION:
                case SkillConstants.Buccaneer.SPEED_INFUSION:
                case ThunderBreaker.SPEED_INFUSION:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPEED_INFUSION, Integer.valueOf(x)));
                    break;
                case Outlaw.HOMING_BEACON:
                case SkillConstants.Corsair.BULLSEYE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOMING_BEACON, Integer.valueOf(x)));
                    break;
                case ThunderBreaker.SPARK:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPARK, Integer.valueOf(x)));
                    break;
                // MULTIPLE
                case SkillConstants.Aran.POLEARM_BOOSTER:
                case Fighter.WEAPON_BOOSTER:
                case Page.WEAPON_BOOSTER:
                case Spearman.POLEARM_BOOSTER:
                case Spearman.SPEAR_BOOSTER:
                case Hunter.BOW_BOOSTER:
                case SkillConstants.Crossbowman.CROSSBOW_BOOSTER:
                case SkillConstants.Assassin.CLAW_BOOSTER:
                case SkillConstants.Bandit.DAGGER_BOOSTER:
                case FPMage.SPELL_BOOSTER:
                case ILMage.SPELL_BOOSTER:
                case SkillConstants.Brawler.KNUCKLER_BOOSTER:
                case Gunslinger.GUN_BOOSTER:
                case SkillConstants.DawnWarrior.SWORD_BOOSTER:
                case SkillConstants.BlazeWizard.SPELL_BOOSTER:
                case WindArcher.BOW_BOOSTER:
                case NightWalker.CLAW_BOOSTER:
                case ThunderBreaker.KNUCKLER_BOOSTER:
                case SkillConstants.Evan.MAGIC_BOOSTER:
                case SkillConstants.BladeRecruit.KATARA_BOOSTER:
                case BattleMage.STAFF_BOOST:
                case WildHunter.CROSSBOW_BOOSTER:
                case Mechanic.MECHANIC_RAGE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BOOSTER, -4));
                    break;
                case Hero.MAPLE_WARRIOR:
                case Paladin.MAPLE_WARRIOR:
                case SkillConstants.DarkKnight.MAPLE_WARRIOR:
                case FPArchMage.MAPLE_WARRIOR:
                case ILArchMage.MAPLE_WARRIOR:
                case SkillConstants.Bishop.MAPLE_WARRIOR:
                case SkillConstants.Bowmaster.MAPLE_WARRIOR:
                case Marksman.MAPLE_WARRIOR:
                case NightLord.MAPLE_WARRIOR:
                case Shadower.MAPLE_WARRIOR:
                case SkillConstants.Corsair.MAPLE_WARRIOR:
                case SkillConstants.Buccaneer.MAPLE_WARRIOR:
                case SkillConstants.Aran.MAPLE_WARRIOR:
                case SkillConstants.Evan.MAPLE_WARRIOR:
                case SkillConstants.BladeMaster.MAPLE_WARRIOR:
                case BattleMage.MAPLE_WARRIOR:
                case WildHunter.MAPLE_WARRIOR:
                case Mechanic.MAPLE_WARRIOR:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAPLE_WARRIOR, Integer.valueOf(ret.x)));
                    break;
                // SUMMON
                case Ranger.SILVER_HAWK:
                case Sniper.GOLDEN_EAGLE:
                case WildHunter.SILVER_HAWK:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case FPArchMage.IFRIT:
                case Marksman.FROST_PREY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    break;
                case Priest.SUMMON_DRAGON:
                case SkillConstants.Bowmaster.PHOENIX:
                case ILArchMage.ELQUINES:
                case SkillConstants.Bishop.BAHAMUT:
                case SkillConstants.DarkKnight.BEHOLDER:
                case Outlaw.GAVIOTA:
                case SkillConstants.DawnWarrior.SOUL:
                case SkillConstants.BlazeWizard.FLAME:
                case WindArcher.STORM:
                case NightWalker.DARKNESS:
                case ThunderBreaker.LIGHTNING:
                case SkillConstants.BlazeWizard.IFRIT:
                case BladeMaster.MIRRORED_TARGET:
                case Mechanic.HEALING_ROBOT:
                case Mechanic.SATELLITE1:
                case Mechanic.SATELLITE2:
                case Mechanic.SATELLITE3:
                case Mechanic.ACCELERATION_BOT:
                case Mechanic.AMPLIFIER:
                case WildHunter.WILD_TRAP:
                case Mechanic.BOTS_N_TOTS:
                case Mechanic.GIANT_ROBOT:
                case Hermit.DARK_FLARE:
                case ChiefBandit.DARK_FLARE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    break;
                // ----------------------------- MONSTER STATUS ---------------------------------- //
                case Rogue.DISORDER:
                    monsterStatus.put(MonsterStatus.WATK, Integer.valueOf(ret.x));
                    monsterStatus.put(MonsterStatus.WDEF, Integer.valueOf(ret.y));
                    break;
                case SkillConstants.Corsair.HYPNOTIZE:
                    monsterStatus.put(MonsterStatus.INERTMOB, 1);
                    break;
                case NightLord.NINJA_AMBUSH:
                case Shadower.NINJA_AMBUSH:
                    monsterStatus.put(MonsterStatus.NINJA_AMBUSH, Integer.valueOf(1));
                    break;
                case Page.THREATEN:
                    monsterStatus.put(MonsterStatus.WATK, Integer.valueOf(ret.x));
                    monsterStatus.put(MonsterStatus.WDEF, Integer.valueOf(ret.y));
                    break;
                case SkillConstants.Crusader.COMA:
                case SkillConstants.Crusader.SHOUT:
                case WhiteKnight.CHARGE_BLOW:
                case Hunter.ARROW_BOMB:
                case SkillConstants.ChiefBandit.ASSAULTER:
                case Shadower.BOOMERANG_STEP:
                case SkillConstants.Brawler.BACK_SPIN_BLOW:
                case SkillConstants.Brawler.DOUBLE_UPPERCUT:
                case SkillConstants.Buccaneer.DEMOLITION:
                case SkillConstants.Buccaneer.SNATCH:
                case SkillConstants.Buccaneer.BARRAGE:
                case Gunslinger.BLANK_SHOT:
                case SkillConstants.DawnWarrior.COMA:
                case SkillConstants.Aran.ROLLING_SPIN:
                case WildHunter.RICOCHET:
                case WildHunter.SONIC_ROAR:
                case Mechanic.ATOMIC_HAMMER:
                    monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case NightLord.TAUNT:
                case Shadower.TAUNT:
                    monsterStatus.put(MonsterStatus.SHOWDOWN, Integer.valueOf(1));
                    break;
                case ILWizard.COLD_BEAM:
                case ILMage.ICE_STRIKE:
                case ILArchMage.BLIZZARD:
                case ILMage.ELEMENT_COMPOSITION:
                case Sniper.BLIZZARD:
                case Outlaw.ICE_SPLITTER:
                case FPArchMage.PARALYZE:
                case SkillConstants.Aran.COMBO_TEMPEST:
                case SkillConstants.Evan.ICE_BREATH:
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    ret.duration *= 2; // freezing skills are a little strange
                    break;
                case FPWizard.SLOW:
                case ILWizard.SLOW:
                case SkillConstants.BlazeWizard.SLOW:
                case SkillConstants.Evan.SLOW:
                    monsterStatus.put(MonsterStatus.SPEED, Integer.valueOf(ret.x));
                    break;
                case FPWizard.POISON_BREATH:
                case FPMage.ELEMENT_COMPOSITION:
                    monsterStatus.put(MonsterStatus.POISON, Integer.valueOf(1));
                    break;
                case Priest.DOOM:
                    monsterStatus.put(MonsterStatus.DOOM, Integer.valueOf(1));
                    break;
                case ILMage.SEAL:
                case FPMage.SEAL:
                    monsterStatus.put(MonsterStatus.SEAL, Integer.valueOf(1));
                    break;
                case Hermit.SHADOW_WEB: // shadow web
                case NightWalker.SHADOW_WEB:
                    monsterStatus.put(MonsterStatus.SHADOW_WEB, 1);
                    break;
                case FPArchMage.FIRE_DEMON:
                case ILArchMage.ICE_DEMON:
                    monsterStatus.put(MonsterStatus.POISON, Integer.valueOf(1));
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    break;
                //ARAN
                case SkillConstants.Aran.COMBO_ABILITY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ARAN_COMBO, 100));
                    break;
                case SkillConstants.Aran.COMBO_BARRIER:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO_BARRIER, ret.x));
                    break;
                case SkillConstants.Aran.COMBO_DRAIN:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO_DRAIN, ret.x));
                    break;
                case SkillConstants.Aran.SMART_KNOCKBACK:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SMART_KNOCKBACK, ret.x));
                    break;
                case SkillConstants.Aran.BODY_PRESSURE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BODY_PRESSURE, ret.x));
                    break;
                case SkillConstants.Aran.SNOW_CHARGE:
                    monsterStatus.put(MonsterStatus.SPEED, ret.x);
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.WK_CHARGE, ret.y));
                    break;
                case SkillConstants.Evan.ELEMENTAL_RESET:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ELEMENTAL_RESET, ret.x));
                    break;
                case SkillConstants.Evan.MAGIC_SHIELD:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_SHIELD, ret.x));
                    break;
                case SkillConstants.Evan.KILLER_WINGS:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.WINGS, ret.x));
                    break;
                case SkillConstants.Evan.MAGIC_RESISTANCE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_RESISTANCE, ret.x));
                    break;
                case SkillConstants.Evan.RECOVERY_AURA:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.AURA_EFFECT, ret.x));
                    break;
                case SkillConstants.Evan.SOUL_STONE:
                    //statups.add(new Pair<MapleButtStat, Integer>(MapleBuffStat.))
                    break;
                case SkillConstants.BladeMaster.THORNS:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.THORNS, ret.x));
                    break;
                case SkillConstants.BladeLord.OWL_SPIRIT:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.OWL_SPIRIT, ret.x));
                    break;
                case SkillConstants.BladeSpecialist.TORNADO_SPIN:
                case SkillConstants.BladeSpecialist.TORNADO_SPIN_ATTACK:
                    //   statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.TWISTER, ret.x));
                    break;
                case SkillConstants.BladeMaster.FINAL_CUT:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINAL_CUT, ret.x));
                    break;
                case SkillConstants.BladeLord.MIRROR_IMAGE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, ret.x));
                    break;
                case BattleMage.SUMMON_REAPER_BUFF:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, ret.x));
                    break;
                case BattleMage.BLUE_AURA:
                    // statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLUE_AURA, ret.x));
                    // statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.AURA_EFFECT, ret.x));
                    break;
                case BattleMage.YELLOW_AURA:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.YELLOW_AURA, ret.x));
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.AURA_EFFECT, ret.x));
                    break;
                case BattleMage.DARK_AURA:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARK_AURA, ret.x));
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.AURA_EFFECT, ret.x));
                    break;
                case BattleMage.BLOOD_DRAIN:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLOOD_DRAIN, ret.x));
                    break;
                case BattleMage.CONVERSION:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.CONVERSION, ret.x));
                    break;
                case BattleMage.TWISTER_SPIN:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.TORNADO, ret.x));
                    break;
                case WildHunter.ITS_RAINING_MINES:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.RAINING_MINES, ret.x));
                    break;
                case WildHunter.BLIND:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLIND, ret.x));
                    break;
                case WildHunter.FELINE_BERSERK:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FELINE_BERSERK, ret.x));  // 20 = maxlevel? o.o
                    break;
                case Mechanic.PERFECT_ARMOR:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PERFECT_ARMOR, ret.x));
                    break;
                case Mechanic.ROLL_OF_THE_DICE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ROLL_OF_THE_DICE, ret.x));
                    break;
                case Citizen.INFILTRATE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, ret.x));
                    break;
                case WildHunter.JAGUAR_OSHI:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.JAGUAR_OSHI_BUFF, ret.y));
                    break;
                case Mechanic.PROTOTYPE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PROTOTYPE, ret.x));
                    break;
                case Mechanic.SIEGE_MODE:
                case Mechanic.SIEGE_MODE2:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SIEGE_MODE, ret.x));
                    break;
                case Mechanic.SATELLITE_SAFETY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SATELLITE_SAFETY, ret.x));
                    break;
                case Mechanic.EXTREME_MECH:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MECH_TRANSFORM, ret.x));
                    break;
                //  case BattleMage.TWISTER_SPIN:

            }
        }
        if (ret.isMorph()) {
            statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, Integer.valueOf(ret.getMorph())));
        }
        if (ret.ghost > 0 && !skill) {
            statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.GHOST_MORPH, Integer.valueOf(ret.ghost)));
        }
        ret.monsterStatus = monsterStatus;
        statups.trimToSize();
        ret.statups = statups;
        return ret;
    }

    /**
     * @param applyto
     * @param obj
     * @param attack damage done by the skill
     */
    public void applyPassive(MapleCharacter applyto, MapleMapObject obj, int attack) {
        if (makeChanceResult()) {
            switch (sourceid) { // MP eater
                case FPWizard.MP_EATER:
                case ILWizard.MP_EATER:
                case SkillConstants.Cleric.MP_EATER:
                    if (obj == null || obj.getType() != MapleMapObjectType.MONSTER) {
                        return;
                    }
                    MapleMonster mob = (MapleMonster) obj; // x is absorb percentage
                    if (!mob.isBoss()) {
                        int absorbMp = Math.min((int) (mob.getMaxMp() * (getX() / 100.0)), mob.getMp());
                        if (absorbMp > 0) {
                            mob.setMp(mob.getMp() - absorbMp);
                            applyto.addMP(absorbMp);
                            applyto.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 1));
                            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1), false);
                        }
                    }
                    break;
            }
        }
    }

    public boolean applyToPetBuff(MapleCharacter chr) {
        return applyTo(chr, chr, true, null, true);
    }

    public boolean applyTo(MapleCharacter chr) {
        return applyTo(chr, chr, true, null, false);
    }

    public boolean applyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, true, pos, false);
    }

    public boolean applyTo(MapleCharacter chr, Point pos, boolean primary) {
        return applyTo(chr, chr, primary, pos, false);
    }

    private boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, Point pos, boolean petBuff) {
        if (skill && (sourceid == GM.HIDE || sourceid == SuperGM.HIDE)) {
            applyto.toggleHide(false, applyto.isHidden() ? false : true);
            return true;
        }
        int hpchange = calcHPChange(applyfrom, primary);
        int mpchange = calcMPChange(applyfrom, primary);
        if (petBuff) {
            hpchange = 0;
            mpchange = 0;
        }
        if (primary) {
            if (itemConNo != 0) {
                MapleInventoryManipulator.removeById(applyto.getClient(), MapleItemInformationProvider.getInstance().getInventoryType(itemCon), itemCon, itemConNo, false, true);
            }
        }
        List<Pair<MapleStat, Integer>> hpmpupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
        if (!primary && isResurrection()) {
            hpchange = applyto.getMaxHp();
            applyto.setStance(0);
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.removePlayerFromMap(applyto.getId()), false);
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.spawnPlayerMapobject(applyto), false);
        }
        if (isDispel() && makeChanceResult()) {
            applyto.dispelDebuffs();
        } else if (isHeroWill()) {
            applyto.dispelSeduce();
        }
        if (isComboReset()) {
            applyto.setCombo(0);
        }
        if (applyfrom.getMp() < getMpCon()) {
            applyfrom.getAutobanManager().addPoint(AutobanFactory.MPCON, "mpCon hack for skill:" + sourceid + "; Player MP: " + applyto.getMp() + " MP Needed: " + getMpCon());
        }
        if (hpchange != 0) {
            if (hpchange < 0 && (-hpchange) > applyto.getHp()) {
                return false;
            }
            int newHp = applyto.getHp() + hpchange;
            if (newHp < 1) {
                newHp = 1;
            }
            applyto.setHp(newHp);
            hpmpupdate.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(applyto.getHp())));
        }
        int newMp = applyto.getMp() + mpchange;
        if (mpchange != 0 && !petBuff) {
            if (mpchange < 0 && -mpchange > applyto.getMp()) {
                return false;
            }

            applyto.setMp(newMp);
            hpmpupdate.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(applyto.getMp())));
        }
        applyto.getClient().getSession().write(MaplePacketCreator.updatePlayerStats(hpmpupdate, true));
        if (moveTo != -1) {
            if (applyto.getMap().getReturnMapId() != applyto.getMapId()) {
                MapleMap target;
                if (moveTo == 999999999) {
                    target = applyto.getMap().getReturnMap();
                } else {
                    target = ChannelServer.getInstance(applyto.getClient().getChannel()).getMapFactory(applyto.getGMode()).getMap(moveTo);
                    int targetid = target.getId() / 10000000;
                    if (targetid != 60 && applyto.getMapId() / 10000000 != 61 && targetid != applyto.getMapId() / 10000000 && targetid != 21 && targetid != 20) {
                        return false;
                    }
                }
                applyto.changeMap(target);
            } else {
                return false;
            }

        }
        if (isShadowClaw()) {
            int projectile = 0;
            MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
            for (int i = 0; i < 97; i++) { // impose order...
                IItem item = use.getItem((byte) i);
                if (item != null) {
                    if (ItemConstants.isThrowingStar(item.getItemId()) && item.getQuantity() >= 200) {
                        projectile = item.getItemId();
                        break;
                    }
                }
            }
            if (projectile == 0) {
                return false;
            } else {
                MapleInventoryManipulator.removeById(applyto.getClient(), MapleInventoryType.USE, projectile, 200, false, true);
            }

        }
        SummonMovementType summonMovementType = getSummonMovementType();
        if (overTime || isCygnusFA() || summonMovementType != null) {
            applyBuffEffect(applyfrom, applyto, primary);
        }

        if (primary && (overTime || isHeal())) {
            applyBuff(applyfrom);
        }

        if (primary && isMonsterBuff()) {
            applyMonsterBuff(applyfrom);
        }

        if (this.getFatigue() != 0) {
            applyto.getMount().setTiredness(applyto.getMount().getTiredness() + this.getFatigue());
        }

        if (summonMovementType != null) {
            int sid = sourceid == BladeLord.MIRROR_IMAGE ? applyfrom.getId() : sourceid;
            final MapleSummon tosummon = new MapleSummon(applyfrom, sid, applyto.getPosition(), summonMovementType);
            applyfrom.getMap().spawnSummon(tosummon);
            applyfrom.addSummon(sid, tosummon);
            tosummon.addHP(x);
            if (isBeholder()) {
                tosummon.addHP(1);
            } else if (sourceid == Mechanic.HEALING_ROBOT) {
                applyfrom.startRobotHealingSchedule(sourceid);
            }
        }
        if (isMagicDoor() && !FieldLimit.DOOR.check(applyto.getMap().getFieldLimit())) { // Magic Door
            Point doorPosition = new Point(applyto.getPosition());
            MapleDoor door = new MapleDoor(applyto.getId(), doorPosition);
            applyto.getMap().spawnDoor(door);
            applyto.addDoor(door);
            door = new MapleDoor(door);
            applyto.addDoor(door);
            door.getTown().spawnDoor(door);
            if (applyto.getParty() != null) {// update town doors
                applyto.silentPartyUpdate();
            }
            applyto.disableDoor();
        } else if (isMist()) {
            Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
            MapleMist mist = new MapleMist(bounds, applyfrom, this);
            applyfrom.getMap().spawnMist(mist, getDuration(), sourceid != Shadower.SMOKE_SCREEN, false);
        } else if (isTimeLeap()) { // Time Leap
            for (PlayerCoolDownValueHolder i : applyto.getAllCooldowns()) {
                if (i.skillId != SkillConstants.Buccaneer.TIME_LEAP) {
                    applyto.removeCooldown(i.skillId);
                }
            }
        }
        return true;
    }

    private void applyBuff(MapleCharacter applyfrom) {
        if (isPartyBuff() && (applyfrom.getParty() != null || isGmBuff())) {
            Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
            List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
            List<MapleCharacter> affectedp = new ArrayList<MapleCharacter>(affecteds.size());
            for (MapleMapObject affectedmo : affecteds) {
                MapleCharacter affected = (MapleCharacter) affectedmo;
                if (affected != applyfrom && (isGmBuff() || applyfrom.getParty().equals(affected.getParty()))) {
                    if ((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive())) {
                        affectedp.add(affected);
                    }
                    if (isTimeLeap()) {
                        for (PlayerCoolDownValueHolder i : affected.getAllCooldowns()) {
                            affected.removeCooldown(i.skillId);
                        }
                    }
                }
            }
            for (MapleCharacter affected : affectedp) {
                applyTo(applyfrom, affected, false, null, false);
                affected.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 2));
                affected.getMap().broadcastMessage(affected, MaplePacketCreator.showBuffeffect(affected.getId(), sourceid, 2), false);
            }
        }
    }

    private void applyMonsterBuff(MapleCharacter applyfrom) {
        Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
        List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
        ISkill skill_ = SkillFactory.getSkill(sourceid);
        int i = 0;
        for (MapleMapObject mo : affected) {
            MapleMonster monster = (MapleMonster) mo;
            if (makeChanceResult()) {
                monster.applyStatus(applyfrom, new MonsterStatusEffect(getMonsterStati(), skill_, null, false), isPoison(), getDuration());
            }
            i++;
            if (i >= mobCount) {
                break;
            }
        }
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        Point mylt;
        Point myrb;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(-lt.x + posFrom.x, rb.y + posFrom.y);
            mylt = new Point(-rb.x + posFrom.x, lt.y + posFrom.y);
        }
        Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    public void silentApplyBuff(MapleCharacter chr, long starttime) {
        int localDuration = duration;
        localDuration = alchemistModifyVal(chr, localDuration, false);
        CancelEffectAction cancelAction = new CancelEffectAction(chr, this, starttime);
        ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + localDuration) - System.currentTimeMillis()));
        chr.registerEffect(this, starttime, schedule);
        SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null) {
            final MapleSummon tosummon = new MapleSummon(chr, sourceid, chr.getPosition(), summonMovementType);
            if (!tosummon.isStationary()) {
                chr.addSummon(sourceid, tosummon);
                tosummon.addHP(x);
            }
        }
    }

    public final void applyComboBuff(final MapleCharacter applyto, int combo) {
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ARAN_COMBO, combo));
        applyto.getClient().getSession().write(MaplePacketCreator.giveBuff(sourceid, 99999, stat, false, sourceid));

        final long starttime = System.currentTimeMillis();
//	final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
//	final ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + 99999) - System.currentTimeMillis()));
        applyto.registerEffect(this, starttime, null);
    }

    private void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary) {
        if (!isMonsterRiding()) {
            applyto.cancelEffect(this, true, -1);
        }

        List<Pair<MapleBuffStat, Integer>> localstatups = statups;
        int localDuration = duration;
        int localsourceid = sourceid;
        int seconds = localDuration / 1000;
        MapleMount givemount = null;
        if (isMonsterRiding()) {
            int ridingLevel = 0;
            IItem mount = applyfrom.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
            if (mount != null) {
                ridingLevel = mount.getItemId();
            }
            if (sourceid == Corsair.BATTLE_SHIP) {
                ridingLevel = 1932000;
            } else if (sourceid == Beginner.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT1) {
                ridingLevel = 1932003;
            } else if (sourceid == Beginner.YETI_MOUNT || sourceid == Noblesse.YETI_MOUNT || sourceid == Legend.YETI_MOUNT || sourceid == Citizen.YETI_MOUNT) {
                ridingLevel = 1932004;
            } else if (sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Legend.WITCH_BROOMSTICK || sourceid == Citizen.WITCH_BROOMSTICK) {
                ridingLevel = 1932005;
            } else if (sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.BALROG_MOUNT || sourceid == Citizen.BALROG_MOUNT) {
                ridingLevel = 1932010;
            } else if (sourceid == Beginner.CHICKEN || sourceid == Noblesse.CHICKEN || sourceid == Legend.CHICKEN || sourceid == Citizen.CHICKEN) {
                ridingLevel = 1932029;
            } else if (sourceid == Beginner.OSTRICH || sourceid == Noblesse.OSTRICH || sourceid == Legend.OSTRICH || sourceid == Citizen.OSTRICH) {
                ridingLevel = 1932026;
            } else if (sourceid == Beginner.CROCO || sourceid == Noblesse.CROCO || sourceid == Legend.CROCO || sourceid == Citizen.CROCO) {
                ridingLevel = 1932007;
            } else if (sourceid == Beginner.OS4_SHUTTLE || sourceid == Noblesse.OS4_SHUTTLE || sourceid == Legend.OS4_SHUTTLE || sourceid == Citizen.OS4_SHUTTLE) {
                ridingLevel = 1932037;
            } else if (sourceid == Beginner.OWL || sourceid == Noblesse.OWL || sourceid == Legend.OWL || sourceid == Citizen.OWL) {
                ridingLevel = 1932038;
            } else if (sourceid == Beginner.MOTHERSHIP || sourceid == Noblesse.MOTHERSHIP || sourceid == Legend.MOTHERSHIP || sourceid == Citizen.MOTHERSHIP) {
                ridingLevel = 1932039;
            } else if (sourceid == Beginner.OS3A_MACHINE || sourceid == Noblesse.OS3A_MACHINE || sourceid == Legend.OS3A_MACHINE || sourceid == Citizen.OS3A_MACHINE) {
                ridingLevel = 1932040;
            } else if (sourceid == Beginner.SMALL_RABBIT || sourceid == Noblesse.SMALL_RABBIT || sourceid == Legend.SMALL_RABBIT || sourceid == Citizen.SMALL_RABBIT) {
                ridingLevel = 1932046;
            } else if (sourceid == Beginner.ROGUE_RABBIT || sourceid == Noblesse.ROGUE_RABBIT || sourceid == Legend.ROGUE_RABBIT || sourceid == Citizen.ROGUE_RABBIT) {
                ridingLevel = 1932045;
            } else if (sourceid == Beginner.LEONARDO_THE_LION || sourceid == Noblesse.LEONARDO_THE_LION || sourceid == Legend.LEONARDO_THE_LION || sourceid == Citizen.LEONARDO_THE_LION) {
                ridingLevel = 1932041;
            } else if (sourceid == Beginner.RACE_KART || sourceid == Noblesse.RACE_KART || sourceid == Legend.RACE_KART || sourceid == Citizen.RACE_KART) {
                ridingLevel = 1932013;
            } else if (sourceid == Beginner.ZD_TIGER || sourceid == Noblesse.ZD_TIGER || sourceid == Legend.ZD_TIGER || sourceid == Citizen.ZD_TIGER) {
                ridingLevel = 1932014;
            } else if (sourceid == Beginner.MIST_BALROG || sourceid == Noblesse.MIST_BALROG || sourceid == Legend.MIST_BALROG || sourceid == Citizen.MIST_BALROG) {
                ridingLevel = 1932012;
            } else if (sourceid == Beginner.SHINJO || sourceid == Noblesse.SHINJO || sourceid == Legend.SHINJO || sourceid == Citizen.SHINJO) {
                ridingLevel = 1932022;
            } else if (sourceid == Beginner.ORANGE_MUSHROOM || sourceid == Noblesse.ORANGE_MUSHROOM || sourceid == Legend.ORANGE_MUSHROOM || sourceid == Citizen.ORANGE_MUSHROOM) {
                ridingLevel = 1932023;
            } else if (sourceid == Beginner.NIGHTMARE || sourceid == Noblesse.NIGHTMARE || sourceid == Legend.NIGHTMARE || sourceid == Citizen.NIGHTMARE) {
                ridingLevel = 1932025;
            } else if (sourceid == Beginner.YETI || sourceid == Noblesse.YETI || sourceid == Legend.YETI || sourceid == Citizen.YETI) {
                ridingLevel = 1932004;
            } else if (sourceid == Beginner.PINK_BEAR_HOT_AIR_BALLOON || sourceid == Noblesse.PINK_BEAR_HOT_AIR_BALLOON || sourceid == Legend.PINK_BEAR_HOT_AIR_BALLOON || sourceid == Citizen.PINK_BEAR_HOT_AIR_BALLOON) {
                ridingLevel = 1932027;
            } else if (sourceid == Beginner.TRANSFORMATION_ROBOT || sourceid == Noblesse.TRANSFORMATION_ROBOT || sourceid == Legend.TRANSFORMATION_ROBOT || sourceid == Citizen.TRANSFORMATION_ROBOT) {
                ridingLevel = 1932028;
            } else if (sourceid == Beginner.MOTORCYCLE || sourceid == Noblesse.MOTORCYCLE || sourceid == Legend.MOTORCYCLE || sourceid == Citizen.MOTORCYCLE) {
                ridingLevel = 1932034;
            } else if (sourceid == Beginner.POWER_SUIT || sourceid == Noblesse.POWER_SUIT || sourceid == Legend.POWER_SUIT || sourceid == Citizen.POWER_SUIT) {
                ridingLevel = 1932035;
            } else if (sourceid == Beginner.NIMBUS_CLOUD || sourceid == Noblesse.NIMBUS_CLOUD || sourceid == Legend.NIMBUS_CLOUD || sourceid == Citizen.NIMBUS_CLOUD) {
                ridingLevel = 1932011;
            } else if (sourceid == Beginner.CHARGE_WOODEN_PONY || sourceid == Noblesse.CHARGE_WOODEN_PONY || sourceid == Legend.CHARGE_WOODEN_PONY || sourceid == Citizen.CHARGE_WOODEN_PONY) {
                ridingLevel = 1932006;
            } else if (sourceid == Beginner.BLACK_SCOOTER || sourceid == Noblesse.BLACK_SCOOTER || sourceid == Legend.BLACK_SCOOTER || sourceid == Citizen.BLACK_SCOOTER) {
                ridingLevel = 1932008;
            } else if (sourceid == Beginner.PINK_SCOOTER || sourceid == Noblesse.PINK_SCOOTER || sourceid == Legend.PINK_SCOOTER || sourceid == Citizen.PINK_SCOOTER) {
                ridingLevel = 1932009;
            } else if (sourceid == Beginner.GARGOYLE || sourceid == Noblesse.GARGOYLE || sourceid == Legend.GARGOYLE || sourceid == Citizen.GARGOYLE) {
                ridingLevel = 1932021;
            } else if (sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP || sourceid == Legend.SPACESHIP || sourceid == Citizen.SPACESHIP) {
                ridingLevel = 1932002;
            } else if (sourceid == Beginner.UNICORN || sourceid == Noblesse.UNICORN || sourceid == Legend.UNICORN || sourceid == Citizen.UNICORN) {
                ridingLevel = 1932018;
            } else if (sourceid == Beginner.RED_TRUCK || sourceid == Noblesse.RED_TRUCK || sourceid == Legend.RED_TRUCK || sourceid == Citizen.RED_TRUCK) {
                ridingLevel = 1932020;
            } else if (sourceid == Beginner.LOW_RIDER || sourceid == Noblesse.LOW_RIDER || sourceid == Legend.LOW_RIDER || sourceid == Citizen.LOW_RIDER) {
                ridingLevel = 1932019;
            } else if (sourceid == Beginner.LION || sourceid == Noblesse.LION || sourceid == Legend.LION || sourceid == Citizen.LION) {
                ridingLevel = 1932041;
            } else if (sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP) {
                ridingLevel = 1932000 + applyto.getSkillLevel(sourceid);
            } else if (sourceid == Beginner.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT1) {
                ridingLevel = 1932003;
                //} else if (sourceid == Beginner.YETI_MOUNT2 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Legend.YETI_MOUNT2) {
                // ridingLevel = 1932004;
            } else if (sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Legend.WITCH_BROOMSTICK) {
                ridingLevel = 1932005;
            } else if (sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.BALROG_MOUNT) {
                ridingLevel = 1932010;
            } else if (sourceid == WildHunter.JAGUAR_RIDER) {
                ridingLevel = 1932032;
            } else if (sourceid == Mechanic.PROTOTYPE || sourceid == Mechanic.SIEGE_MODE || sourceid == Mechanic.SIEGE_MODE2 || sourceid == Mechanic.EXTREME_MECH) {
                ridingLevel = 1932016;
            } else {
                if (applyto.getMount() == null) {
                    applyto.mount(ridingLevel, sourceid);
                }
                applyto.getMount().startSchedule();
            }
            if (sourceid == Corsair.BATTLE_SHIP) {
                givemount = new MapleMount(applyto, 1932000, sourceid);
            } else if (sourceid == Beginner.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT1) {
                givemount = new MapleMount(applyto, 1932003, sourceid);
            } else if (sourceid == Beginner.YETI_MOUNT || sourceid == Noblesse.YETI_MOUNT || sourceid == Legend.YETI_MOUNT || sourceid == Citizen.YETI_MOUNT) {
                givemount = new MapleMount(applyto, 1932004, sourceid);
            } else if (sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Legend.WITCH_BROOMSTICK || sourceid == Citizen.WITCH_BROOMSTICK) {
                givemount = new MapleMount(applyto, 1932005, sourceid);
            } else if (sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.BALROG_MOUNT || sourceid == Citizen.BALROG_MOUNT) {
                givemount = new MapleMount(applyto, 1932010, sourceid);
            } else if (sourceid == Beginner.CHICKEN || sourceid == Noblesse.CHICKEN || sourceid == Legend.CHICKEN || sourceid == Citizen.CHICKEN) {
                givemount = new MapleMount(applyto, 1932029, sourceid);
            } else if (sourceid == Beginner.OSTRICH || sourceid == Noblesse.OSTRICH || sourceid == Legend.OSTRICH || sourceid == Citizen.OSTRICH) {
                givemount = new MapleMount(applyto, 1932026, sourceid);
            } else if (sourceid == Beginner.CROCO || sourceid == Noblesse.CROCO || sourceid == Legend.CROCO || sourceid == Citizen.CROCO) {
                givemount = new MapleMount(applyto, 1932007, sourceid);
            } else if (sourceid == Beginner.OWL || sourceid == Noblesse.OWL || sourceid == Legend.OWL || sourceid == Citizen.OWL) {
                givemount = new MapleMount(applyto, 1932038, sourceid);
            } else if (sourceid == Beginner.MOTHERSHIP || sourceid == Noblesse.MOTHERSHIP || sourceid == Legend.MOTHERSHIP || sourceid == Citizen.MOTHERSHIP) {
                givemount = new MapleMount(applyto, 1932039, sourceid);
            } else if (sourceid == Beginner.OS4_SHUTTLE || sourceid == Noblesse.OS4_SHUTTLE || sourceid == Legend.OS4_SHUTTLE || sourceid == Citizen.OS4_SHUTTLE) {
                givemount = new MapleMount(applyto, 1932037, sourceid);
            } else if (sourceid == Beginner.OS3A_MACHINE || sourceid == Noblesse.OS3A_MACHINE || sourceid == Legend.OS3A_MACHINE || sourceid == Citizen.OS3A_MACHINE) {
                givemount = new MapleMount(applyto, 1932040, sourceid);
            } else if (sourceid == Beginner.SMALL_RABBIT || sourceid == Noblesse.SMALL_RABBIT || sourceid == Legend.SMALL_RABBIT || sourceid == Citizen.SMALL_RABBIT) {
                givemount = new MapleMount(applyto, 1932046, sourceid);
            } else if (sourceid == Beginner.ROGUE_RABBIT || sourceid == Noblesse.ROGUE_RABBIT || sourceid == Legend.ROGUE_RABBIT || sourceid == Citizen.ROGUE_RABBIT) {
                givemount = new MapleMount(applyto, 1932045, sourceid);
            } else if (sourceid == Beginner.LEONARDO_THE_LION || sourceid == Noblesse.LEONARDO_THE_LION || sourceid == Legend.LEONARDO_THE_LION || sourceid == Citizen.LEONARDO_THE_LION) {
                givemount = new MapleMount(applyto, 1932041, sourceid);
            } else if (sourceid == Beginner.RACE_KART || sourceid == Noblesse.RACE_KART || sourceid == Legend.RACE_KART || sourceid == Citizen.RACE_KART) {
                givemount = new MapleMount(applyto, 1932013, sourceid);
            } else if (sourceid == Beginner.ZD_TIGER || sourceid == Noblesse.ZD_TIGER || sourceid == Legend.ZD_TIGER || sourceid == Citizen.ZD_TIGER) {
                givemount = new MapleMount(applyto, 1932014, sourceid);
            } else if (sourceid == Beginner.MIST_BALROG || sourceid == Noblesse.MIST_BALROG || sourceid == Legend.MIST_BALROG || sourceid == Citizen.MIST_BALROG) {
                givemount = new MapleMount(applyto, 1932012, sourceid);
            } else if (sourceid == Beginner.SHINJO || sourceid == Noblesse.SHINJO || sourceid == Legend.SHINJO || sourceid == Citizen.SHINJO) {
                givemount = new MapleMount(applyto, 1932022, sourceid);
            } else if (sourceid == Beginner.ORANGE_MUSHROOM || sourceid == Noblesse.ORANGE_MUSHROOM || sourceid == Legend.ORANGE_MUSHROOM || sourceid == Citizen.ORANGE_MUSHROOM) {
                givemount = new MapleMount(applyto, 1932023, sourceid);
            } else if (sourceid == Beginner.NIGHTMARE || sourceid == Noblesse.NIGHTMARE || sourceid == Legend.NIGHTMARE || sourceid == Citizen.NIGHTMARE) {
                givemount = new MapleMount(applyto, 1932025, sourceid);
            } else if (sourceid == Beginner.YETI || sourceid == Noblesse.YETI || sourceid == Legend.YETI || sourceid == Citizen.YETI) {
                givemount = new MapleMount(applyto, 1932004, sourceid);
            } else if (sourceid == Beginner.PINK_BEAR_HOT_AIR_BALLOON || sourceid == Noblesse.PINK_BEAR_HOT_AIR_BALLOON || sourceid == Legend.PINK_BEAR_HOT_AIR_BALLOON || sourceid == Citizen.PINK_BEAR_HOT_AIR_BALLOON) {
                givemount = new MapleMount(applyto, 1932027, sourceid);
            } else if (sourceid == Beginner.TRANSFORMATION_ROBOT || sourceid == Noblesse.TRANSFORMATION_ROBOT || sourceid == Legend.TRANSFORMATION_ROBOT || sourceid == Citizen.TRANSFORMATION_ROBOT) {
                givemount = new MapleMount(applyto, 1932028, sourceid);
            } else if (sourceid == Beginner.MOTORCYCLE || sourceid == Noblesse.MOTORCYCLE || sourceid == Legend.MOTORCYCLE || sourceid == Citizen.MOTORCYCLE) {
                givemount = new MapleMount(applyto, 1932034, sourceid);
            } else if (sourceid == Beginner.POWER_SUIT || sourceid == Noblesse.POWER_SUIT || sourceid == Legend.POWER_SUIT || sourceid == Citizen.POWER_SUIT) {
                givemount = new MapleMount(applyto, 1932035, sourceid);
            } else if (sourceid == Beginner.NIMBUS_CLOUD || sourceid == Noblesse.NIMBUS_CLOUD || sourceid == Legend.NIMBUS_CLOUD || sourceid == Citizen.NIMBUS_CLOUD) {
                givemount = new MapleMount(applyto, 1932011, sourceid);
            } else if (sourceid == Beginner.CHARGE_WOODEN_PONY || sourceid == Noblesse.CHARGE_WOODEN_PONY || sourceid == Legend.CHARGE_WOODEN_PONY || sourceid == Citizen.CHARGE_WOODEN_PONY) {
                givemount = new MapleMount(applyto, 1932006, sourceid);
            } else if (sourceid == Beginner.BLACK_SCOOTER || sourceid == Noblesse.BLACK_SCOOTER || sourceid == Legend.BLACK_SCOOTER || sourceid == Citizen.BLACK_SCOOTER) {
                givemount = new MapleMount(applyto, 1932008, sourceid);
            } else if (sourceid == Beginner.PINK_SCOOTER || sourceid == Noblesse.PINK_SCOOTER || sourceid == Legend.PINK_SCOOTER || sourceid == Citizen.PINK_SCOOTER) {
                givemount = new MapleMount(applyto, 1932009, sourceid);
            } else if (sourceid == Beginner.GARGOYLE || sourceid == Noblesse.GARGOYLE || sourceid == Legend.GARGOYLE || sourceid == Citizen.GARGOYLE) {
                givemount = new MapleMount(applyto, 1932021, sourceid);
            } else if (sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP || sourceid == Legend.SPACESHIP || sourceid == Citizen.SPACESHIP) {
                givemount = new MapleMount(applyto, 1932002, sourceid);
            } else if (sourceid == Beginner.UNICORN || sourceid == Noblesse.UNICORN || sourceid == Legend.UNICORN || sourceid == Citizen.UNICORN) {
                givemount = new MapleMount(applyto, 1932018, sourceid);
            } else if (sourceid == Beginner.RED_TRUCK || sourceid == Noblesse.RED_TRUCK || sourceid == Legend.RED_TRUCK || sourceid == Citizen.RED_TRUCK) {
                givemount = new MapleMount(applyto, 1932020, sourceid);
            } else if (sourceid == Beginner.LOW_RIDER || sourceid == Noblesse.LOW_RIDER || sourceid == Legend.LOW_RIDER || sourceid == Citizen.LOW_RIDER) {
                givemount = new MapleMount(applyto, 1932019, sourceid);
            } else if (sourceid == Beginner.LION || sourceid == Noblesse.LION || sourceid == Legend.LION || sourceid == Citizen.LION) {
                givemount = new MapleMount(applyto, 1932041, sourceid);
            } else if (sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP) {
                givemount = new MapleMount(applyto, 1932000 + applyto.getSkillLevel(sourceid), sourceid);
            } else if (sourceid == Beginner.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT1) {
                givemount = new MapleMount(applyto, 1932003, sourceid);
                // } else if (sourceid == Beginner.YETI_MOUNT2 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Legend.YETI_MOUNT2) {
                //    givemount = new MapleMount(applyto, 1932004, sourceid);
            } else if (sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Legend.WITCH_BROOMSTICK) {
                givemount = new MapleMount(applyto, 1932005, sourceid);
            } else if (sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.BALROG_MOUNT) {
                givemount = new MapleMount(applyto, 1932010, sourceid);
            } else if (sourceid == WildHunter.JAGUAR_RIDER) {
                givemount = new MapleMount(applyto, 1932015, sourceid);
            } else if (sourceid == Mechanic.PROTOTYPE || sourceid == Mechanic.SIEGE_MODE || sourceid == Mechanic.SIEGE_MODE2 || sourceid == Mechanic.EXTREME_MECH) {
                givemount = new MapleMount(applyto, 1932016, sourceid);
            } else {
                givemount = applyto.getMount();
            }
            localDuration = sourceid;
            localsourceid = ridingLevel;
            localstatups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 0));
            applyto.setMount(givemount);
        } else if (isSkillMorph()) {
            localstatups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, getMorph(applyto)));
        }
        if (primary) {
            localDuration = alchemistModifyVal(applyfrom, localDuration, false);
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1, (byte) 3), false);
        }
        if (localstatups.size() > 0) {
            MaplePacket buff = null;
            MaplePacket mbuff = null;
            buff = MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), localDuration, localstatups, false, sourceid);
            if (isDash()) {
                buff = MaplePacketCreator.givePirateBuff(statups, sourceid, seconds);
                // mbuff = MaplePacketCreator.giveForeignDash(applyto.getId(), sourceid, seconds, localstatups);
            } else if (isInfusion()) {
                buff = MaplePacketCreator.givePirateBuff(statups, sourceid, seconds);
                mbuff = MaplePacketCreator.giveForeignInfusion(applyto.getId(), x, localDuration);
            } else if (isDs()) {
                List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, 0));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), dsstat);
            } else if (isCombo()) {
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), statups);
            } else if (isMonsterRiding()) {
                buff = MaplePacketCreator.giveBuff(localsourceid, localDuration, localstatups, true, sourceid);
                mbuff = MaplePacketCreator.showMonsterRiding(applyto.getId(), givemount);
                localDuration = duration;
            } else if (isShadowPartner()) {
                List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, 0));
                mbuff = MaplePacketCreator.enableActions();
            } else if (sourceid == BladeLord.MIRROR_IMAGE) {
                List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MIRROR_IMAGE, 0));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), statups);
            } else if (isSoulArrow()) {
                List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOULARROW, 0));
                mbuff = MaplePacketCreator.enableActions();
            } else if (isEnrage()) {
                applyto.handleOrbconsume();
            } else if (isMorph()) {
                List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, Integer.valueOf(getMorph(applyto))));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), stat);
            } else if (isTimeLeap()) {
                for (PlayerCoolDownValueHolder i : applyto.getAllCooldowns()) {
                    if (i.skillId != Buccaneer.TIME_LEAP) {
                        applyto.removeCooldown(i.skillId);
                    }
                }
            }
            long starttime = System.currentTimeMillis();
            CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
            ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, localDuration);
            applyto.registerEffect(this, starttime, schedule);
            if (buff != null) {
                applyto.getClient().getSession().write(buff);
            }
            if (mbuff != null) {
                applyto.getMap().broadcastMessage(applyto, mbuff, false);
            }
        }
    }

    private int calcHPChange(MapleCharacter applyfrom, boolean primary) {
        int hpchange = 0;
        if (hp != 0) {
            if (sourceid == BattleMage.CONVERSION) {
                hpchange += (applyfrom.getMaxHp() * (x / 100));
            }
            if (!skill) {
                if (primary) {
                    hpchange += alchemistModifyVal(applyfrom, hp, true);
                } else {
                    hpchange += hp;
                }
            } else {
                hpchange += makeHealHP(hp / 100.0, applyfrom.getTotalMagic(), 3, 5);
            }
        }
        if (hpR != 0) {
            hpchange += (int) (applyfrom.getCurrentMaxHp() * hpR);
            applyfrom.checkBerserk();
        }
        if (primary) {
            if (hpCon != 0) {
                hpchange -= hpCon;
            }
        }
        if (isChakra()) {
            hpchange += makeHealHP(getY() / 100.0, applyfrom.getTotalLuk(), 2.3, 3.5);
        }
        return hpchange;
    }

    private int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
        return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
    }

    private int calcMPChange(MapleCharacter applyfrom, boolean primary) {
        int mpchange = 0;
        if (mp != 0) {
            if (primary) {
                mpchange += alchemistModifyVal(applyfrom, mp, true);
            } else {
                mpchange += mp;
            }
        }
        if (mpR != 0) {
            mpchange += (int) (applyfrom.getCurrentMaxMp() * mpR);
        }
        if (primary) {
            if (mpCon != 0) {
                double mod = 1.0;
                boolean isAFpMage = applyfrom.getJob().isA(MapleJob.FP_MAGE);
                boolean isCygnus = applyfrom.getJob().isA(MapleJob.BLAZEWIZARD2);
                if (isAFpMage || isCygnus || applyfrom.getJob().isA(MapleJob.IL_MAGE)) {
                    ISkill amp = isAFpMage ? SkillFactory.getSkill(FPMage.ELEMENT_AMPLIFICATION) : (isCygnus ? SkillFactory.getSkill(BlazeWizard.ELEMENT_AMPLIFICATION) : SkillFactory.getSkill(ILMage.ELEMENT_AMPLIFICATION));
                    int ampLevel = applyfrom.getSkillLevel(amp);
                    if (ampLevel > 0) {
                        mod = amp.getEffect(ampLevel).getX() / 100.0;
                    }
                }
                mpchange -= mpCon * mod;
                if (applyfrom.getBuffedValue(MapleBuffStat.INFINITY) != null) {
                    mpchange = 0;
                } else if (applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE) != null) {
                    mpchange -= (int) (mpchange * (applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE).doubleValue() / 100));
                }
            }
        }
        return mpchange;
    }

    private int alchemistModifyVal(MapleCharacter chr, int val, boolean withX) {
        if (!skill && (chr.getJob().isA(MapleJob.HERMIT) || chr.getJob().isA(MapleJob.NIGHTWALKER3))) {
            MapleStatEffect alchemistEffect = getAlchemistEffect(chr);
            if (alchemistEffect != null) {
                return (int) (val * ((withX ? alchemistEffect.getX() : alchemistEffect.getY()) / 100.0));
            }
        }
        return val;
    }

    private MapleStatEffect getAlchemistEffect(MapleCharacter chr) {
        int id = Hermit.ALCHEMIST;
        if (chr.isCygnus()) {
            id = NightWalker.ALCHEMIST;
        }
        int alchemistLevel = chr.getSkillLevel(SkillFactory.getSkill(id));
        return alchemistLevel == 0 ? null : SkillFactory.getSkill(id).getEffect(alchemistLevel);
    }

    private boolean isGmBuff() {
        switch (sourceid) {
            case Beginner.ECHO_OF_HERO:
            case Noblesse.ECHO_OF_HERO:
            case Legend.ECHO_OF_HERO:
            case SuperGM.HEAL_PLUS_DISPEL:
            case SuperGM.HASTE:
            case SuperGM.HOLY_SYMBOL:
            case SuperGM.BLESS:
            case SuperGM.RESURRECTION:
            case SuperGM.HYPER_BODY:
                return true;
            default:
                return false;
        }
    }

    private boolean isMonsterBuff() {
        if (!skill) {
            return false;
        }
        switch (sourceid) {
            case Page.THREATEN:
            case FPWizard.SLOW:
            case ILWizard.SLOW:
            case FPMage.SEAL:
            case ILMage.SEAL:
            case Priest.DOOM:
            case Hermit.SHADOW_WEB:
            case NightLord.NINJA_AMBUSH:
            case Shadower.NINJA_AMBUSH:
            case BlazeWizard.SLOW:
            case BlazeWizard.SEAL:
            case NightWalker.SHADOW_WEB:
                return true;
        }
        return false;
    }

    private boolean isPartyBuff() {
        if (lt == null || rb == null) {
            return false;
        }
        if ((sourceid >= 1211003 && sourceid <= 1211008) || sourceid == Paladin.SWORD_HOLY_CHARGE || sourceid == Paladin.BW_HOLY_CHARGE || sourceid == DawnWarrior.SOUL_CHARGE) {// wk charges have lt and rb set but are neither player nor monster buffs
            return false;
        }
        return true;
    }

    private boolean isHeal() {
        return sourceid == Cleric.HEAL || sourceid == SuperGM.HEAL_PLUS_DISPEL;
    }

    private boolean isResurrection() {
        return sourceid == Bishop.RESURRECTION || sourceid == GM.RESURRECTION || sourceid == SuperGM.RESURRECTION;
    }

    private boolean isTimeLeap() {
        return sourceid == Buccaneer.TIME_LEAP;
    }

    public boolean isDragonBlood() {
        return skill && sourceid == DragonKnight.DRAGON_BLOOD;
    }

    public boolean isBerserk() {
        return skill && sourceid == DarkKnight.BERSERK;
    }

    private boolean isDs() {
        return skill && (sourceid == Rogue.DARK_SIGHT || sourceid == WindArcher.WIND_WALK || sourceid == NightWalker.DARK_SIGHT);
    }

    private boolean isCombo() {
        return skill && (sourceid == Crusader.COMBO || sourceid == DawnWarrior.COMBO);
    }

    private boolean isEnrage() {
        return skill && sourceid == Hero.ENRAGE;
    }

    public boolean isBeholder() {
        return skill && sourceid == DarkKnight.BEHOLDER;
    }

    private boolean isShadowPartner() {
        return skill && (sourceid == Hermit.SHADOW_PARTNER || sourceid == NightWalker.SHADOW_PARTNER || sourceid == 4211008);
    }

    private boolean isChakra() {
        return skill && sourceid == ChiefBandit.CHAKRA;
    }

    public boolean isMonsterRiding() {
        return skill && (sourceid % 10000000 == 1004 || sourceid == Corsair.BATTLE_SHIP || sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP || sourceid == Beginner.OSTRICH || sourceid == Beginner.CHICKEN || sourceid == Beginner.CROCO || sourceid == Beginner.OS3A_MACHINE || sourceid == Beginner.OWL || sourceid == Beginner.MOTHERSHIP || sourceid == Beginner.OS4_SHUTTLE
                || sourceid == Beginner.YETI_MOUNT1 || sourceid == Beginner.YETI_MOUNT || sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Beginner.BALROG_MOUNT || sourceid == Legend.OSTRICH || sourceid == Noblesse.CHICKEN || sourceid == Legend.CROCO || sourceid == Noblesse.OS3A_MACHINE || sourceid == Noblesse.OWL || sourceid == Noblesse.MOTHERSHIP || sourceid == Noblesse.OS4_SHUTTLE
                || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Noblesse.BALROG_MOUNT || sourceid == Noblesse.OSTRICH || sourceid == Legend.CHICKEN || sourceid == Noblesse.CROCO || sourceid == Legend.OS3A_MACHINE || sourceid == Legend.OWL || sourceid == Legend.MOTHERSHIP || sourceid == Legend.OS4_SHUTTLE
                || sourceid == Legend.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT || sourceid == Legend.WITCH_BROOMSTICK || sourceid == Legend.BALROG_MOUNT || sourceid == WildHunter.JAGUAR_RIDER || sourceid == Beginner.LEONARDO_THE_LION || sourceid == Legend.LEONARDO_THE_LION || sourceid == Noblesse.LEONARDO_THE_LION || sourceid == Beginner.SMALL_RABBIT || sourceid == Legend.SMALL_RABBIT || sourceid == Noblesse.SMALL_RABBIT || sourceid == Beginner.ROGUE_RABBIT || sourceid == Legend.ROGUE_RABBIT || sourceid == Noblesse.ROGUE_RABBIT
                || sourceid == Mechanic.PROTOTYPE || sourceid == Beginner.RACE_KART || sourceid == Noblesse.RACE_KART || sourceid == Legend.RACE_KART || sourceid == Beginner.ZD_TIGER || sourceid == Noblesse.ZD_TIGER || sourceid == Legend.ZD_TIGER || sourceid == Beginner.MIST_BALROG || sourceid == Noblesse.MIST_BALROG || sourceid == Legend.MIST_BALROG
                || sourceid == Beginner.SHINJO || sourceid == Noblesse.SHINJO || sourceid == Legend.SHINJO || sourceid == Beginner.ORANGE_MUSHROOM || sourceid == Noblesse.ORANGE_MUSHROOM || sourceid == Legend.ORANGE_MUSHROOM || sourceid == Beginner.NIGHTMARE || sourceid == Noblesse.NIGHTMARE || sourceid == Legend.NIGHTMARE
                || sourceid == Beginner.YETI || sourceid == Noblesse.YETI || sourceid == Legend.YETI || sourceid == Beginner.PINK_BEAR_HOT_AIR_BALLOON || sourceid == Noblesse.PINK_BEAR_HOT_AIR_BALLOON || sourceid == Legend.PINK_BEAR_HOT_AIR_BALLOON || sourceid == Beginner.TRANSFORMATION_ROBOT || sourceid == Noblesse.TRANSFORMATION_ROBOT || sourceid == Legend.TRANSFORMATION_ROBOT || sourceid == Beginner.MOTORCYCLE || sourceid == Noblesse.MOTORCYCLE || sourceid == Legend.MOTORCYCLE
                || sourceid == Beginner.POWER_SUIT || sourceid == Noblesse.POWER_SUIT || sourceid == Legend.POWER_SUIT || sourceid == Beginner.NIMBUS_CLOUD || sourceid == Noblesse.NIMBUS_CLOUD || sourceid == Legend.NIMBUS_CLOUD
                || sourceid == Beginner.BLACK_SCOOTER || sourceid == Noblesse.BLACK_SCOOTER || sourceid == Legend.BLACK_SCOOTER || sourceid == Beginner.CHARGE_WOODEN_PONY || sourceid == Noblesse.CHARGE_WOODEN_PONY || sourceid == Legend.CHARGE_WOODEN_PONY || sourceid == Beginner.PINK_SCOOTER || sourceid == Noblesse.PINK_SCOOTER || sourceid == Legend.PINK_SCOOTER || sourceid == Beginner.GARGOYLE || sourceid == Noblesse.GARGOYLE || sourceid == Legend.GARGOYLE || sourceid == Beginner.UNICORN || sourceid == Noblesse.UNICORN || sourceid == Legend.UNICORN
                || sourceid == Beginner.RED_TRUCK || sourceid == Noblesse.RED_TRUCK || sourceid == Legend.RED_TRUCK || sourceid == Beginner.LOW_RIDER || sourceid == Noblesse.LOW_RIDER || sourceid == Legend.LOW_RIDER || sourceid == Beginner.LION || sourceid == Noblesse.LION || sourceid == Legend.LION
                || sourceid == Citizen.YETI_MOUNT || sourceid == Citizen.CHICKEN || sourceid == Citizen.OSTRICH || sourceid == Citizen.CROCO || sourceid == Citizen.OS4_SHUTTLE || sourceid == Citizen.OWL || sourceid == Citizen.MOTHERSHIP || sourceid == Citizen.OS3A_MACHINE
                || sourceid == Citizen.LEONARDO_THE_LION || sourceid == Citizen.ROGUE_RABBIT || sourceid == Citizen.POWER_SUIT || sourceid == Citizen.PINK_BEAR_HOT_AIR_BALLOON || sourceid == Citizen.YETI || sourceid == Citizen.SHINJO || sourceid == Citizen.MIST_BALROG || sourceid == Citizen.ZD_TIGER || sourceid == Citizen.RACE_KART
                || sourceid == Citizen.NIMBUS_CLOUD || sourceid == Citizen.CHARGE_WOODEN_PONY || sourceid == Citizen.SMALL_RABBIT || sourceid == Citizen.MOTORCYCLE || sourceid == Citizen.TRANSFORMATION_ROBOT || sourceid == Citizen.NIGHTMARE || sourceid == Citizen.ORANGE_MUSHROOM
                || sourceid == Citizen.WITCH_BROOMSTICK || sourceid == Citizen.LION || sourceid == Citizen.LOW_RIDER || sourceid == Citizen.RED_TRUCK || sourceid == Citizen.UNICORN || sourceid == Citizen.SPACESHIP || sourceid == Citizen.GARGOYLE || sourceid == Citizen.PINK_SCOOTER || sourceid == Citizen.BLACK_SCOOTER || sourceid == Citizen.BALROG_MOUNT);
    }

    public boolean isMagicDoor() {
        return skill && sourceid == Priest.MYSTIC_DOOR;
    }

    public boolean isPoison() {
        return skill && (sourceid == FPMage.POISON_MIST || sourceid == FPWizard.POISON_BREATH || sourceid == FPMage.ELEMENT_COMPOSITION || sourceid == NightWalker.POISON_BOMB);
    }

    private boolean isMist() {
        return skill && (sourceid == FPMage.POISON_MIST || sourceid == Shadower.SMOKE_SCREEN || sourceid == BlazeWizard.FLAME_GEAR || sourceid == NightWalker.POISON_BOMB);
    }

    private boolean isSoulArrow() {
        return skill && (sourceid == Hunter.SOUL_ARROW || sourceid == Crossbowman.SOUL_ARROW || sourceid == WindArcher.SOUL_ARROW || sourceid == WildHunter.SOUL_ARROW_CROSSBOW);
    }

    private boolean isShadowClaw() {
        return skill && sourceid == NightLord.SHADOW_STARS;
    }

    private boolean isAmplifierRobot() {
        return skill && sourceid == Mechanic.AMPLIFIER;
    }

    private boolean isHealingRobot() {
        return skill && sourceid == Mechanic.HEALING_ROBOT;
    }

    private boolean isAccelerationRobot() {
        return skill && sourceid == Mechanic.ACCELERATION_BOT;
    }

    private boolean isDispel() {
        return skill && (sourceid == Priest.DISPEL || sourceid == SuperGM.HEAL_PLUS_DISPEL);
    }

    private boolean isHeroWill() {
        if (skill) {
            switch (sourceid) {
                case Hero.HEROS_WILL:
                case Paladin.HEROS_WILL:
                case DarkKnight.HEROS_WILL:
                case FPArchMage.HEROS_WILL:
                case ILArchMage.HEROS_WILL:
                case Bishop.HEROS_WILL:
                case Bowmaster.HEROS_WILL:
                case Marksman.HEROS_WILL:
                case NightLord.HEROS_WILL:
                case Shadower.HEROS_WILL:
                case Buccaneer.PIRATES_RAGE:
                case Aran.HEROS_WILL:
                case WildHunter.HEROS_WILL:
                case BattleMage.HEROS_WILL:
                case Mechanic.HEROS_WILL:
                case BladeMaster.HEROS_WILL:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    private boolean isDash() {
        return skill && (sourceid == Pirate.DASH || sourceid == ThunderBreaker.DASH || sourceid == Beginner.SPACE_DASH || sourceid == Noblesse.SPACE_DASH);
    }

    private boolean isSkillMorph() {
        return skill && (sourceid == Buccaneer.SUPER_TRANSFORMATION || sourceid == Marauder.TRANSFORMATION || sourceid == WindArcher.EAGLE_EYE || sourceid == ThunderBreaker.TRANSFORMATION);
    }

    private boolean isInfusion() {
        return skill && (sourceid == Buccaneer.SPEED_INFUSION || sourceid == Corsair.SPEED_INFUSION || sourceid == ThunderBreaker.SPEED_INFUSION);
    }

    private boolean isCygnusFA() {
        return skill && (sourceid == DawnWarrior.FINAL_ATTACK || sourceid == WindArcher.FINAL_ATTACK);
    }

    private boolean isMorph() {
        return morphId > 0;
    }

    private boolean isComboReset() {
        return sourceid == Aran.COMBO_BARRIER || sourceid == Aran.COMBO_DRAIN;
    }

    private int getFatigue() {
        return fatigue;
    }

    private int getMorph() {
        return morphId;
    }

    private int getMorph(MapleCharacter chr) {
        if (morphId % 10 == 0) {
            return morphId + chr.getGender();
        }
        return morphId + 100 * chr.getGender();
    }

    private SummonMovementType getSummonMovementType() {
        if (!skill) {
            return null;
        }
        switch (sourceid) {
            case Ranger.PUPPET:
            case Sniper.PUPPET:
            case WindArcher.PUPPET:
            case Outlaw.OCTOPUS:
            case Corsair.WRATH_OF_THE_OCTOPI:
            case BladeMaster.MIRRORED_TARGET:
            case WildHunter.WILD_TRAP:
            //     case Mechanic.ACCELERATION_BOT:
            //   case Mechanic.AMPLIFIER:
            case Mechanic.GIANT_ROBOT:
            case Mechanic.HEALING_ROBOT:
            case Mechanic.BOTS_N_TOTS:
            case Hermit.DARK_FLARE:
            case ChiefBandit.DARK_FLARE:
                return SummonMovementType.STATIONARY;
            case Ranger.SILVER_HAWK:
            case WildHunter.SILVER_HAWK:
            case Sniper.GOLDEN_EAGLE:
            case Priest.SUMMON_DRAGON:
            case Marksman.FROST_PREY:
            case Bowmaster.PHOENIX:
            case Outlaw.GAVIOTA:
            case BattleMage.SUMMON_REAPER_BUFF:
            //     return SummonMovementType.CIRCLE_FOLLOW;
            case DarkKnight.BEHOLDER:
            case FPArchMage.IFRIT:
            case ILArchMage.ELQUINES:
            case Bishop.BAHAMUT:
            case DawnWarrior.SOUL:
            // case BladeLord.MIRROR_IMAGE:
            case BlazeWizard.FLAME:
            case BlazeWizard.IFRIT:
            case WindArcher.STORM:
            case NightWalker.DARKNESS:
            case ThunderBreaker.LIGHTNING:
            case Mechanic.SATELLITE1:
            case Mechanic.SATELLITE2:
            case Mechanic.SATELLITE3:
                return SummonMovementType.FOLLOW;
        }
        return null;
    }

    public boolean isSkill() {
        return skill;
    }

    public int getSourceId() {
        return sourceid;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }

    private static class CancelEffectAction implements Runnable {

        private MapleStatEffect effect;
        private WeakReference<MapleCharacter> target;
        private long startTime;

        public CancelEffectAction(MapleCharacter target, MapleStatEffect effect, long startTime) {
            this.effect = effect;
            this.target = new WeakReference<MapleCharacter>(target);
            this.startTime = startTime;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.cancelEffect(effect, false, startTime);
            }
        }
    }

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }

    public short getHpCon() {
        return hpCon;
    }

    public short getMpCon() {
        return mpCon;
    }

    public short getMatk() {
        return matk;
    }

    public int getDuration() {
        return duration;
    }

    public List<Pair<MapleBuffStat, Integer>> getStatups() {
        return statups;
    }

    public boolean sameSource(MapleStatEffect effect) {
        return this.sourceid == effect.sourceid && this.skill == effect.skill;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public int getMobCount() {
        return mobCount;
    }

    public int getFixDamage() {
        return fixdamage;
    }

    public int getBulletCount() {
        return bulletCount;
    }

    public int getBulletConsume() {
        return bulletConsume;
    }

    public int getMoneyCon() {
        return moneyCon;
    }

    public int getCooldown() {
        return cooldown;
    }

    public Map<MonsterStatus, Integer> getMonsterStati() {
        return monsterStatus;
    }

    public double getMaxDistanceSq() {
        final int maxX = Math.max(Math.abs(lt == null ? 0 : lt.x), Math.abs(rb == null ? 0 : rb.x));
        final int maxY = Math.max(Math.abs(lt == null ? 0 : lt.y), Math.abs(rb == null ? 0 : rb.y));
        return (maxX * maxX) + (maxY * maxY);
    }
}