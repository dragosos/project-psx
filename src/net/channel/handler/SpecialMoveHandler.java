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

import java.awt.Point;
import java.util.concurrent.ScheduledFuture;
import client.ISkill;
import client.MapleCharacter;
import client.MapleCharacter.CancelCooldownAction;
import client.MapleClient;
import client.MapleStat;
import client.SkillFactory;
import constants.SkillConstants.*;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SpecialMoveHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        slea.readInt();
        int skillid = slea.readInt();
        Point pos = null;
        int __skillLevel = slea.readByte();
        ISkill skill = SkillFactory.getSkill(skillid);
        int skillLevel = c.getPlayer().getSkillLevel(skill);
        if (skillid % 10000000 == 1010 || skillid % 10000000 == 1011) {
            skillLevel = 1;
            chr.setDojoEnergy(0);
            c.announce(MaplePacketCreator.getEnergy(0));
        }
        MapleStatEffect effect = skill.getEffect(__skillLevel);
        if (effect.getCooldown() > 0) {
            if (chr.skillisCooling(skillid)) {
                return;
            } else if (skillid != Corsair.BATTLE_SHIP) {
                c.announce(MaplePacketCreator.skillCooldown(skillid, effect.getCooldown()));
                ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(c.getPlayer(), skillid), effect.getCooldown() * 1000);
                chr.addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown() * 1000, timer);
            }
        }
        if (skillid == Hero.MONSTER_MAGNET || skillid == Paladin.MONSTER_MAGNET || skillid == DarkKnight.MONSTER_MAGNET) { // Monster Magnet
            int num = slea.readInt();
            int mobId;
            byte success;
            for (int i = 0; i < num; i++) {
                mobId = slea.readInt();
                success = slea.readByte();
                chr.getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showMagnet(mobId, success), false);
                MapleMonster monster = chr.getMap().getMonsterByOid(mobId);
                if (monster != null) {
                    monster.switchController(c.getPlayer(), monster.isControllerHasAggro());
                }
            }
            byte direction = slea.readByte();
            chr.getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showBuffeffect(chr.getId(), skillid, chr.getSkillLevel(skillid), direction), false);
            c.announce(MaplePacketCreator.enableActions());
            return;
        } else if (skillid == Buccaneer.TIME_LEAP) { // Timeleap
            MapleParty p = chr.getParty();
            if (p != null) {
                for (MaplePartyCharacter mpc : p.getMembers()) {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        if (cserv.getPlayerStorage().getCharacterById(mpc.getId()) != null) {
                            cserv.getPlayerStorage().getCharacterById(mpc.getId()).removeAllCooldownsExcept(5121010);
                        }
                    }
                }
            }
            chr.removeAllCooldownsExcept(Buccaneer.TIME_LEAP);
        } else if (skillid == Brawler.MP_RECOVERY) {// MP Recovery
            ISkill s = SkillFactory.getSkill(skillid);
            MapleStatEffect ef = s.getEffect(chr.getSkillLevel(s));
            int lose = chr.getMaxHp() / ef.getX();
            chr.setHp(chr.getHp() - lose);
            chr.updateSingleStat(MapleStat.HP, chr.getHp());
            int gain = lose * (ef.getY() / 100);
            chr.setMp(chr.getMp() + gain);
            chr.updateSingleStat(MapleStat.MP, chr.getMp());
        } else if (skillid % 10000000 == 1004) {
            slea.readShort();
        } else if(Cleric.HEAL == skillid){
            chr.addHP(chr.getHp());
            if(chr.getParty() != null){
            for(MaplePartyCharacter party : chr.getParty().getMembers()){
                if(party.getPlayer().getHp() != party.getPlayer().getMaxHp() && party.getPlayer().getId() != chr.getId()){
                int different = (party.getPlayer().getMaxMp() - (party.getPlayer().getHp())) % 20000;
                party.getPlayer().addHP(chr.getHp());
                party.getPlayer().updateSingleStat(MapleStat.HP, party.getPlayer().getHp());
                chr.gainExp(different, false, false);
                chr.announce(MaplePacketCreator.sendHealExp(different));
                }
            }
            }
        } 
        if (slea.available() == 5) {
            pos = new Point(slea.readShort(), slea.readShort());
        }
        if (chr.isAlive()) {
            if (skill.getId() != Priest.MYSTIC_DOOR || chr.canDoor()) {
                skill.getEffect(skillLevel).applyTo(c.getPlayer(), pos);
            } else {
                chr.message("Please wait 5 seconds before casting Mystic Door again");
                c.announce(MaplePacketCreator.enableActions());
            }
        } else {
            c.announce(MaplePacketCreator.enableActions());
        }
    }
}