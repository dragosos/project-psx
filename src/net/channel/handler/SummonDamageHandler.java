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

import java.util.ArrayList;
import java.util.List;
import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.autoban.AutobanFactory;
import client.status.MonsterStatusEffect;
import net.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.MapleMonster;
import server.maps.MapleSummon;
import tools.*;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SummonDamageHandler extends AbstractMaplePacketHandler {
    public final class SummonAttackEntry {

        private int monsterOid;
        private int damage;

        public SummonAttackEntry(int monsterOid, int damage) {
            this.monsterOid = monsterOid;
            this.damage = damage;
        }

        public int getMonsterOid() {
            return monsterOid;
        }

        public int getDamage() {
            return damage;
        }
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        slea.skip(29);
        long delay = (System.currentTimeMillis() - c.getPlayer().getAutobanManager().lastSummonAttack);
        double distance;
        final MapleCharacter player = c.getPlayer();
        if (!player.isAlive()) {
            return;
        }
        MapleSummon summon = null;
        for (MapleSummon sum : player.getSummons().values()) {
            if (sum.getObjectId() == oid) {
                summon = sum;
            }
        }

        ISkill summonSkill = SkillFactory.getSkill(summon.getSkill());
        MapleStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
        List<SummonAttackEntry> allDamage = new ArrayList<SummonAttackEntry>();
        int numAttacked = slea.readByte();
        
        slea.skip(8); 
        slea.skip(4); // v0.97+
        MapleMonster attacked = null;
        for (int x = 0; x < numAttacked; x++) {
            int monsterOid = slea.readInt(); // attacked oid
            slea.skip(4); // id of something
            slea.skip(14);
            int damage = slea.readInt();
            allDamage.add(new SummonAttackEntry(monsterOid, damage));
            attacked = c.getPlayer().getMap().getMonsterByOid(monsterOid);
        }
        if(delay < 200 && !summon.isCustom){
            c.getPlayer().getAutobanManager().addPoint(AutobanFactory.SUMMON_FAST_ATTACK, "Summon-attacking too often :( " + delay);
        } else {
            if(!summon.isCustom){
                c.getPlayer().getAutobanManager().lastSummonAttack = System.currentTimeMillis();
            }
        }
        player.getMap().broadcastMessage(player, MaplePacketCreator.summonAttack(player.getId(), summon.getSkill(), (byte)4, allDamage), summon.getPosition());
        for (SummonAttackEntry attackEntry : allDamage) {
            int damage = attackEntry.getDamage();
            MapleMonster target = player.getMap().getMonsterByOid(attackEntry.getMonsterOid());
            if (target != null) {
                if (damage > 0 && summonEffect.getMonsterStati().size() > 0) {
                    if (summonEffect.makeChanceResult()) {
                        target.applyStatus(player, new MonsterStatusEffect(summonEffect.getMonsterStati(), summonSkill, null, false), summonEffect.isPoison(), 4000);
                    }
                }
                player.getMap().damageMonster(player, target, damage);
            }
        }
        final MapleSummon fsummon = summon;
        TimerManager.getInstance().schedule(new Runnable(){
            @Override
            public void run(){
            if(fsummon.getSkill() == constants.SkillConstants.Mechanic.GIANT_ROBOT){
                player.cancelEffect(SkillFactory.getSkill(constants.SkillConstants.Mechanic.GIANT_ROBOT).getEffect(1), false, -1);
            }
            }
        }, 1000 * 3);
    }
}