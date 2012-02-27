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

import java.io.Serializable;
import net.LongValueHolder;

public enum MapleBuffStat implements LongValueHolder, Serializable {
          MORPH(0x2), // v95: very correct
       RECOVERY(0x4), // v95: very correct
  MAPLE_WARRIOR(0x8), // v95: very correct
     SHARP_EYES(0x20), // v95: very correct
         STANCE(0x10),// v95: very correct
MANA_REFLECTION(0x40), // v95: very correct
    SHADOW_CLAW(0x100),
       INFINITY(0x200), // v95: very correct
    HOLY_SHIELD(0x400), // v95: very correct
      HAMSTRING(0x800),
          BLIND(0x1000), // v95: very correct
    CONCENTRATE(0x2000), // v95: very correct
   ECHO_OF_HERO(0x8000), // v95: very correct
    GHOST_MORPH(0x20000),
           AURA(0x40000), 
        CONFUSE(0x80000),
   BERSERK_FURY(0x8000000),
    DIVINE_BODY(0x10000000),
          SPARK(0x20000000), // correct
    FINALATTACK(0x80000000L), // v95: very correct
           WATK(0x100000000L), // v95: very correct
           WDEF(0x200000000L), // v95: very correct
           MATK(0x400000000L), // v95: very ccorrect
           MDEF(0x800000000L), // v95: very correct
     BATTLESHIP(0xA00000040L), // v95: very correct
            ACC(0x1000000000L), // v95: very correct
          AVOID(0x2000000000L), // v95: very correct
          HANDS(0x4000000000L), // v95: very correct
          SPEED(0x8000000000L), // v95: very correct
           JUMP(0x10000000000L), // v95: very correct
     SELF_HASTE(0x18000000000L), //v95: very correcy
    MAGIC_GUARD(0x20000000000L), // v95: very correct
      DARKSIGHT(0x40000000000L), // v95: very correct
        BOOSTER(0x80000000000L), // v95: very correct
         SUMMON(0x100000000000L), // v95: very correct
     POWERGUARD(0x100000000000L), // v95: very correct
    HYPERBODYHP(0x200000000000L), // v95: very correct
    HYPERBODYMP(0x400000000000L), // v95: very correct
     INVINCIBLE(0x800000000000L), // v95: very correct
      SOULARROW(0x1000000000000L), // v95: very correct
          COMBO(0x20000000000000L), // v95: very correct
      WK_CHARGE(0x40000000000000L), // v97: very correct
    SOUL_CHARGE(0x40000400000000L),
    DRAGONBLOOD(0x80000000000000L), // 97: very correct
    HOLY_SYMBOL(0x100000000000000L), // v95: very correct
         MESOUP(0x200000000000000L), // v95: very correct
  SHADOWPARTNER(0x400000000000000L), // V95: very correct
     PICKPOCKET(0x800000000000000L), // v95: very correct
         PUPPET(0x800000000000000L), // v95: very correct
      MESOGUARD(0x1000000000000000L), // v95: very correct
      GIANT_ROBOT_MECH(100000000000L),
//00 00 00 00 00 00 00 04
      // 4000000000000000
   PERFECT_ARMOR(0x4, true),
SATELLITE_SAFETY(0x18, true), // v95: very correct
         TWISTER(0x20, true), // v95: very correct
      CONVERSION(0x400, true), // v95: very correct
   SUMMON_REAPER(0x80000000, true), // v95: very correct
  MECH_TRANSFORM(0x2000, true), // v95: very correct
     AURA_EFFECT(0x4000, true), // v95: very correct
       DARK_AURA(0x8000, true), // v95: very correct
       BLUE_AURA(0x10000, true), // v95: very correct
     YELLOW_AURA(0x20000, true), // v95: very correct
ROLL_OF_THE_DICE(0x100000, true), // v95: very correct
           DASH2(0x100000, true), // very correct (speed)
            DASH(0x200000, true), // very correct (jump)
TELEPORT_MASTERY(0x800000, true), // v95: very correct
  SPEED_INFUSION(0x800000, true), // v95: very correct
  MONSTER_RIDING(0x40000000, true), // v95: very correct
 ELEMENTAL_RESET(0x200000000L, true), // v95: very correct
     BLOOD_DRAIN(0x200000000L, true), // v95: very correct
      ARAN_COMBO(0x1000000000L, true), // v95: very correct
     COMBO_DRAIN(0x2000000000L, true), // v95: very correct
   COMBO_BARRIER(0x4000000000L, true), // v95: very correct
   BODY_PRESSURE(0x8000000000L, true), // v95: very correct
 SMART_KNOCKBACK(0x10000000000L, true), // correct
      PYRAMID_PQ(0x20000000000L, true), // correct
    MAGIC_SHIELD(0x800000000000L, true), // v95: very correct
MAGIC_RESISTANCE(0x1000000000000L, true), // v95: very correct
   ENERGY_CHARGE(0x4000000000000L, true), // correct
           WINGS(0x4000000000000L, true), // very correct
   PLAYER_FROZEN(0x8000000000000L, true), // v95: very correct
      OWL_SPIRIT(0x40000000000000L, true), //v95: very correct
       FINAL_CUT(0x100000000000000L, true), // v95: very correct
          THORNS(0x200000000000000L, true), // v95: very correct
   HOMING_BEACON(0x200000000000000L, true), // v95: needs testing
   JAGUAR_OSHI_BUFF(0x4000000000000000L, true), // v0.97
  FELINE_BERSERK(0x800000000080000L, true), // v95: very correct
    MIRROR_IMAGE(0x20000000000000L, true),
    TORNADO(0x2000000L, true),
    INFILTRATE(0x100000000L, true),
    SIEGE_MODE(0x200000000L, true),
    //
   RAINING_MINES(0x1000000000000000L, true), // v95: very correct
       PROTOTYPE(0xE000000040000003L, true); // v97: very correct

    static final long serialVersionUID = 0L;
    private final long i;
    private final boolean isFirst;

    private MapleBuffStat(long i, boolean isFirst) {
        this.i = i;
        this.isFirst = isFirst;
    }

    private MapleBuffStat(long i) {
        this.i = i;
        this.isFirst = false;
    }

    @Override
    public long getValue() {
        return i;
    }

    public boolean isFirst() {
        return isFirst;
    }
}

