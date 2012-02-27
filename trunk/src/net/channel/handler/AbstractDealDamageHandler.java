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
package net.channel.handler;

import client.Equip;
import constants.SkillConstants.*;
import client.IItem;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import client.ISkill;
import client.Item;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleInventoryType;
import client.MapleStat;
import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.ItemConstants;
import server.life.MonsterDropEntry;
import tools.Randomizer;
import net.AbstractMaplePacketHandler;
import client.autoban.AutobanFactory;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.Element;
import server.life.ElementalEffectiveness;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.LittleEndianAccessor;

public abstract class AbstractDealDamageHandler extends AbstractMaplePacketHandler {

    public static class AttackInfo {

        public int numAttacked, numDamage, numAttackedAndDamage, skill, skilllevel, stance, direction, rangedirection, charge, display;
        public List<Pair<Integer, List<Integer>>> allDamage;
        public boolean isHH = false;
        public int speed = 4;
        public int weapon;

        public MapleStatEffect getAttackEffect(MapleCharacter chr, ISkill theSkill) {
            ISkill mySkill = theSkill;
            if (mySkill == null) {
                mySkill = SkillFactory.getSkill(skill);
            }
            int skillLevel = chr.getSkillLevel(mySkill);
            if (skillLevel == 0) {
                return null;
            }
            if (display > 80) { //Hmm
              //  return null;
            }
            return mySkill.getEffect(skillLevel);
        }
    }

    protected void applyAttack(AttackInfo attack, final MapleCharacter player, int attackCount) {
        ISkill theSkill = null;
        MapleStatEffect attackEffect = null;
        player.getAutobanManager().lastAttack = System.currentTimeMillis();
        if (player.isBanned()) return;
        if (attack.skill != 0) {
            theSkill = SkillFactory.getSkill(attack.skill);
            attackEffect = attack.getAttackEffect(player, theSkill);
            if (attackEffect == null && !player.isMechanic()) {
                player.getClient().announce(MaplePacketCreator.enableActions());
                return;
            }
         //   player.dropMessage("SKillID : " + attack.skill);
            if(attack.skill == Mechanic.GIANT_ROBOT){
                player.cancelEffect(SkillFactory.getSkill(Mechanic.GIANT_ROBOT).getEffect(1), false, -1);
            }

           // if (player.getMp() < attackEffect.getMpCon())
            //    AutobanFactory.MPCON.addPoint(player.getAutobanManager(), "Skill: " + attack.skill + "; Player MP: " + player.getMp() + "; MP Needed: " + attackEffect.getMpCon());

            if (attack.skill != Cleric.HEAL) {
                if (player.isAlive()) {
                    attackEffect.applyTo(player);
                } else {
                    player.getClient().announce(MaplePacketCreator.enableActions());
                }
            }
            int mobCount = attackEffect.getMobCount();
                if (attack.skill == DawnWarrior.FINAL_ATTACK
                          || attack.skill == Spearman.FINAL_ATTACK_SPEAR || attack.skill == Spearman.FINAL_ATTACK_POLEARM || attack.skill == WindArcher.FINAL_ATTACK
                          || attack.skill == DawnWarrior.FINAL_ATTACK || attack.skill == Hunter.FINAL_ATTACK || attack.skill == Crossbowman.FINAL_ATTACK) {
                          mobCount = 15;//:(
                }
         // if (attack.numAttacked > mobCount) {
                  //  AutobanFactory.MOB_COUNT.autoban(player, "Skill: " + attack.skill + "; Count: " + attack.numAttacked + " Max: " + attackEffect.getMobCount());
          //          return;
           // }
        }
        if (!player.isAlive()) {
            return;
        }

        //WTF IS THIS F3,1
       // if (attackCount != attack.numDamage && attack.skill != ChiefBandit.MESO_EXPLOSION && attack.skill != NightWalker.VAMPIRE && attack.skill != WindArcher.WIND_SHOT && attack.skill != Aran.COMBO_SMASH && attack.skill != Aran.COMBO_PENRIL && attack.skill != Aran.COMBO_TEMPEST && attack.skill != NightLord.NINJA_AMBUSH && attack.skill != Shadower.NINJA_AMBUSH && !player.isMechanic()) {
       //     return;
        //}
        int totDamage = 0;
        final MapleMap map = player.getMap();

        if (attack.skill == ChiefBandit.MESO_EXPLOSION) {
            int delay = 0;
            for (Pair<Integer, List<Integer>> oned : attack.allDamage) {
                MapleMapObject mapobject = map.getMapObject(oned.getLeft().intValue());
                if (mapobject != null && mapobject.getType() == MapleMapObjectType.ITEM) {
                    final MapleMapItem mapitem = (MapleMapItem) mapobject;
                    final Point position = mapobject.getPosition();
                    final int objId = mapobject.getObjectId();

                    if (mapitem.getMeso() > 9) {
                        synchronized(mapitem){
                            if (mapitem.isPickedUp()) {
                                return;
                            }
                            TimerManager.getInstance().schedule(new Runnable() {

                                public void run() {
                                    map.removeMapObject(mapitem);
                                    map.broadcastMessage(MaplePacketCreator.removeItemFromMap(objId, 4, 0), position);
                                    mapitem.setPickedUp(true);
                                }
                            }, delay);
                            delay += 100;
                        }
                    } else if (mapitem.getMeso() == 0) {
                  //      player.getCheatTracker().registerOffense(CheatingOffense.ETC_EXPLOSION);
                        return;
                    }
                } else if (mapobject != null && mapobject.getType() != MapleMapObjectType.MONSTER) {
                  //  player.getCheatTracker().registerOffense(CheatingOffense.EXPLODING_NONEXISTANT);
                    return; // etc explosion, exploding nonexistant things, etc.
                }
            }
        }
        for (Pair<Integer, List<Integer>> oned : attack.allDamage) {
            final MapleMonster monster = map.getMonsterByOid(oned.getLeft().intValue());
            if (monster != null) {
                int totDamageToOneMonster = 0;
                double distance = player.getPosition().distanceSq(monster.getPosition());
                for (Integer eachd : oned.getRight()) {
                    totDamageToOneMonster += eachd.intValue();
                }
                totDamage += totDamageToOneMonster;
                player.checkMonsterAggro(monster);
                if(player.isDamageHacking(totDamageToOneMonster)){
                    player.getAutobanManager().addPoint(AutobanFactory.DAMAGE_HACKING, "Dealing too much damage :" + totDamageToOneMonster + " at Lv." + player.getLevel());
                } if(distance >= 850000.0){
                    player.getAutobanManager().addPoint(AutobanFactory.LONG_RANGE_ATTACK, "Long range damage");
                }
                if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null && (attack.skill == 0 || attack.skill == Rogue.DOUBLE_STAB || attack.skill == Bandit.SAVAGE_BLOW || attack.skill == ChiefBandit.ASSAULTER || attack.skill == ChiefBandit.BAND_OF_THIEVES || attack.skill == Shadower.ASSASSINATE || attack.skill == Shadower.TAUNT || attack.skill == Shadower.BOOMERANG_STEP)) {
                    ISkill pickpocket = SkillFactory.getSkill(ChiefBandit.PICKPOCKET);
                    int delay = 0;
                    final int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET).intValue();
                    for (final Integer eachd : oned.getRight()) {
                        if (pickpocket.getEffect(player.getSkillLevel(pickpocket)).makeChanceResult()) {
                            TimerManager.getInstance().schedule(new Runnable() {

                                public void run() {
                                    player.getMap().spawnMesoDrop(Math.min((int) Math.max(((double) eachd / (double) 20000) * (double) maxmeso, (double) 1), maxmeso), new Point((int) (monster.getPosition().getX() + Randomizer.nextInt(100) - 50), (int) (monster.getPosition().getY())), monster, player, true, (byte) 0);
                                }
                            }, delay);
                            delay += 100;
                        }
                    }
                } else if(attack.skill == WildHunter.SWIPE){
                    int hp = (int)(totDamageToOneMonster * ((double)(SkillFactory.getSkill(WildHunter.SWIPE).getEffect(player.getSkillLevel(WildHunter.SWIPE)).getX() / 100)));
                    int monsterhp = (int)(monster.getMaxHp() * 0.15);
                    if(hp >= monsterhp){
                        hp = monsterhp;
                    }
                    player.addHP(hp);
                } else if(player.getBuffedValue(MapleBuffStat.BLOOD_DRAIN) != null){
                    int hp = (int)(totDamageToOneMonster * 0.05);
                    int maxGainHP = (int)(player.getMaxHp() * 0.10);
                    if(hp > maxGainHP){
                        hp = maxGainHP;
                    }
                    
                    player.addHP(hp);
                }
                else if (attack.skill == Marksman.SNIPE) {
                    totDamageToOneMonster = 195000 + Randomizer.nextInt(5000);
                } else if (attack.skill == Marauder.ENERGY_DRAIN || attack.skill == ThunderBreaker.ENERGY_DRAIN || attack.skill == NightWalker.VAMPIRE || attack.skill == Assassin.DRAIN) {
                    player.addHP(Math.min(monster.getMaxHp(), Math.min((int) ((double) totDamage * (double) SkillFactory.getSkill(attack.skill).getEffect(player.getSkillLevel(SkillFactory.getSkill(attack.skill))).getX() / 100.0), player.getMaxHp() / 2)));
                } else if (attack.skill == Bandit.STEAL && !monster.isBoss()) {
                    ISkill steal = SkillFactory.getSkill(Bandit.STEAL);
                    if (Math.random() < 0.3 && steal.getEffect(player.getSkillLevel(steal)).makeChanceResult()) { //Else it drops too many cool stuff :(
                        List<MonsterDropEntry> toSteals = MapleMonsterInformationProvider.getInstance().retrieveDrop(monster.getId());
                        Collections.shuffle(toSteals);
                        int toSteal = toSteals.get(rand(0, (toSteals.size() - 1))).itemId;
                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        IItem item = null;
                        if (ItemConstants.getInventoryType(toSteal).equals(MapleInventoryType.EQUIP)) {
                            item = ii.randomizeStats((Equip) ii.getEquipById(toSteal));
                        } else {
                            item = new Item(toSteal, (byte) 0, (short) 1, -1);
                        }
                        player.getMap().spawnItemDrop(monster, player, item, monster.getPosition(), false, false);
                        monster.addStolen(toSteal);
                    }
                } else if (attack.skill == FPArchMage.FIRE_DEMON) {
                    monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(FPArchMage.FIRE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(FPArchMage.FIRE_DEMON))).getDuration() * 1000);
                } else if (attack.skill == ILArchMage.ICE_DEMON) {
                    monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(ILArchMage.ICE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(ILArchMage.ICE_DEMON))).getDuration() * 1000);
                } else if (attack.skill == Outlaw.HOMING_BEACON || attack.skill == Corsair.BULLSEYE) {
                    player.setMarkedMonster(monster.getObjectId());
                    player.announce(MaplePacketCreator.giveBuff(1, attack.skill, Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOMING_BEACON, monster.getObjectId())), false));
                }
                if (player.getBuffedValue(MapleBuffStat.HAMSTRING) != null) {
                    ISkill hamstring = SkillFactory.getSkill(Bowmaster.HAMSTRING);
                    if (hamstring.getEffect(player.getSkillLevel(hamstring)).makeChanceResult()) {
                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, hamstring.getEffect(player.getSkillLevel(hamstring)).getX()), hamstring, null, false);
                        monster.applyStatus(player, monsterStatusEffect, false, hamstring.getEffect(player.getSkillLevel(hamstring)).getY() * 1000);
                    }
                }
                if (player.getBuffedValue(MapleBuffStat.BLIND) != null) {
                    ISkill blind = SkillFactory.getSkill(Marksman.BLIND);
                    if (blind.getEffect(player.getSkillLevel(blind)).makeChanceResult()) {
                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.ACC, blind.getEffect(player.getSkillLevel(blind)).getX()), blind, null, false);
                        monster.applyStatus(player, monsterStatusEffect, false, blind.getEffect(player.getSkillLevel(blind)).getY() * 1000);
                    }
                }
                
                final int id = player.getJob().getId();
                if (id == 121 || id == 122) {
                    for (int charge = 1211005; charge < 1211007; charge++) {
                        ISkill chargeSkill = SkillFactory.getSkill(charge);
                        if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, chargeSkill)) {
                            final ElementalEffectiveness iceEffectiveness = monster.getEffectiveness(Element.ICE);
                            if (totDamageToOneMonster > 0 && iceEffectiveness == ElementalEffectiveness.NORMAL || iceEffectiveness == ElementalEffectiveness.WEAK) {
                                monster.applyStatus(player, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.FREEZE, 1), chargeSkill, null, false), false, 5000 * 2000);
                            }
                            break;
                        }
                    }
                }else if (player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null) {
                    final ISkill skill = SkillFactory.getSkill(21101003);
                    final MapleStatEffect eff = skill.getEffect(player.getSkillLevel(skill));

                    if (eff.makeChanceResult()) {
                        monster.applyStatus(player, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.NEUTRALISE, 1), skill, null, false), false, eff.getX() * 1000, false);
                    }
                } else if (player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
                    final ISkill skill = SkillFactory.getSkill(21100005);
                    player.setHp(player.getHp() + ((totDamage * skill.getEffect(player.getSkillLevel(skill)).getX()) / 100), true);
                    player.updateSingleStat(MapleStat.HP, player.getHp());
                } else if (id == 412 || id == 422 || id == 1411) {
                    ISkill type = SkillFactory.getSkill(player.getJob().getId() == 412 ? 4120005 : (player.getJob().getId() == 1411 ? 14110004 : 4220005));
                    if (player.getSkillLevel(type) > 0) {
                        MapleStatEffect venomEffect = type.getEffect(player.getSkillLevel(type));
                        for (int i = 0; i < attackCount; i++) {
                            if (venomEffect.makeChanceResult()) {
                                if (monster.getVenomMulti() < 3) {
                                    monster.setVenomMulti((monster.getVenomMulti() + 1));
                                    MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), type, null, false);
                                    monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
                                }
                            }
                        }
                    }
                }
                if (attack.skill != 0) {
                    if (attackEffect.getFixDamage() != -1 && monster.getStats().getFixedDamage() != -1) {
                        if (totDamageToOneMonster != attackEffect.getFixDamage() && totDamageToOneMonster != 0)
                            AutobanFactory.FIX_DAMAGE.autoban(player, String.valueOf(totDamageToOneMonster) + " damage");
                    }
                }
                if (totDamageToOneMonster > 0 && attackEffect != null && attackEffect.getMonsterStati().size() > 0) {
                    if (attackEffect.makeChanceResult()) {
                        monster.applyStatus(player, new MonsterStatusEffect(attackEffect.getMonsterStati(), theSkill, null, false), attackEffect.isPoison(), attackEffect.getDuration());
                    }
                }
                if (attack.isHH && !monster.isBoss()) {
                    map.damageMonster(player, monster, monster.getHp() - 1);
                } else if (attack.isHH) {
                    int HHDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Paladin.HEAVENS_HAMMER).getEffect(player.getSkillLevel(SkillFactory.getSkill(Paladin.HEAVENS_HAMMER))).getDamage() / 100));
                    map.damageMonster(player, monster, (int) (Math.floor(Math.random() * (HHDmg / 5) + HHDmg * .8)));
                } else {
                    map.damageMonster(player, monster, totDamageToOneMonster);
                }
            }
        }
    }

protected AttackInfo parseDamage(LittleEndianAccessor lea, MapleCharacter player, boolean ranged, boolean magic) {
        AttackInfo ret = new AttackInfo();
        if(player.isMechanic()){
    //        FileoutputUtil.log("mechanic_attacks.rtf", "lolo " + lea, null);
        }
        lea.skip(1);
        lea.skip(8); // 0.88
        ret.numAttackedAndDamage = lea.readByte();
        ret.numAttacked = (ret.numAttackedAndDamage >>> 4) & 0xF;
        ret.numDamage = ret.numAttackedAndDamage & 0xF;
        ret.allDamage = new ArrayList<Pair<Integer, List<Integer>>>();
        lea.skip(8); // 0.88
        ret.skill = substituteSIDForCheck(lea.readInt());
        if (ret.skill > 0) {
            ret.skilllevel = player.getSkillLevel(SkillFactory.getSkill(ret.skill));
        }
        lea.skip(1); // 0.94
        lea.skip(4); // 0.74
        lea.skip(4); // 0.74
        lea.skip(8); // 0.88
        if (magic) {
            lea.skip(24);
        }
        if (ranged) {
            lea.skip(1);
        }
        switch (ret.skill) {
            case Bishop.BIG_BANG:
            case BladeMaster.FINAL_CUT:
            case Brawler.CORKSCREW_BLOW:
            case Evan.FIRE_BREATH:
            case Evan.ICE_BREATH:
            case FPArchMage.BIG_BANG:
            case Gunslinger.GRENADE:
            case ILArchMage.BIG_BANG:
            case NightWalker.POISON_BOMB:
            case ThunderBreaker.CORKSCREW_BLOW:
                ret.charge = lea.readInt();
            default:
               ret.charge = -1;
        }
        ret.display = lea.readByte();
        ret.direction = lea.readByte(); // 0.80+
        ret.stance = lea.readByte();
        lea.skip(4); // 0.94
        ret.weapon = lea.readByte();
        ret.speed = lea.readByte();
        lea.skip(4);
        lea.skip(4); // 0.88
        if (ranged) {
            lea.skip(2); // Star slot
            lea.skip(2); // Cash Star Slot
            lea.skip(1);
            switch (ret.skill) {
                case 3121004: // hurricane
                case 3221001: // piercing arrow
                case 5221004: // rapid fire
                case 13111002: // hurricane
                case 33121009: // wild arrow blast
                case 35001001: // flame thrower
                case 35101009: // enhanced flame thrower
                    lea.skip(4);
                    break;
                default:
                    break;
            }
        }
        if(ret.skill == ChiefBandit.MESO_EXPLOSION){
            int oid = lea.readInt();
            lea.skip(4); // timestamp prolly
            lea.skip(8); // x/y
            ret.numDamage = lea.readByte();
            for(int a = 0; a < ret.numDamage; a++){
                int damage = lea.readInt();
            }
            lea.skip(8);
        }
        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = lea.readInt();
            lea.skip(4); // ?
            lea.skip(8); // X & Y Position
            lea.skip(2);
            List<Integer> allDamageNumbers = new ArrayList<Integer>();
            for (int j = 0; j < ret.numDamage; j++) {
                int damage = lea.readInt();
            //    System.out.println("damage : damage" + damage);
                if (ret.skill == Marksman.SNIPE) {
                    damage += 0x80000000;
                }
                allDamageNumbers.add(Integer.valueOf(damage));
            }
            lea.skip(4); // 0.94
            ret.allDamage.add(new Pair<Integer, List<Integer>>(Integer.valueOf(oid), allDamageNumbers));
        }
        return ret;
    }

    private static int rand(int l, int u) {
        return (int) ((Math.random() * (u - l + 1)) + l);
    }
    
     private static int substituteSIDForCheck(int skillID) {
        int newID = 0;
        switch (skillID) {
            case 21110007:
            case 21110008:
                newID = 21110002;
                break;
            case 21120009:
            case 21120010:
                newID = 21120002;
                break;
            default:
                newID = skillID;
        }
        return newID;
    }

    
}
