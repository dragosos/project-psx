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
package server.maps;

import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import java.awt.Point;
import java.awt.Rectangle;
import net.MaplePacket;
import net.channel.ChannelServer;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.life.MobSkill;
import tools.MaplePacketCreator;
import server.maps.MapleMap;

/**
 *
 * @author LaiLaiNoob
 */
public class MapleMist extends AbstractMapleMapObject {
    private Rectangle mistPosition;
    private int ownerid = -1;
    private int mo_oid = -1;
    private int mo_mapid = -1;
    private MapleStatEffect source;
    private MobSkill skill;
    private boolean isMobMist, isPoisonMist;
    private int skillDelay;
    private int channel;

    public MapleMist(Rectangle mistPosition, MapleMonster mob, MobSkill _skill) {
        this.mistPosition = mistPosition;
        this.mo_oid = mob.getObjectId();
        mo_mapid = mob.getMap().getId();
        channel = mob.getMap().getCharacters().iterator().next().getClient().getChannel();
        this.skill = _skill;
        isMobMist = true;
        isPoisonMist = true;
        skillDelay = 0;
    }

    public MapleMist(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source) {
        this.mistPosition = mistPosition;
        this.ownerid = owner.getId();
        this.source = source;
        this.skillDelay = 8;
        this.isMobMist = false;
        switch (source.getSourceId()) {
            case 4221006: // Smoke Screen
                isPoisonMist = false;
                break;
            case 2111003: // FP mist
            case 12111005: // Flame Gear
            case 14111006: // Poison Bomb
                isPoisonMist = true;
                break;
        }
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MIST;
    }

    @Override
    public Point getPosition() {
        return mistPosition.getLocation();
    }

    public ISkill getSourceSkill() {
        return SkillFactory.getSkill(source.getSourceId());
    }

    public boolean isMobMist() {
        return isMobMist;
    }

    public boolean isPoisonMist() {
        return isPoisonMist;
    }

    public int getSkillDelay() {
        return skillDelay;
    }

    public MapleMonster getMobOwner() {
        ChannelServer cs = ChannelServer.getInstance(channel);
        MapleMap map = cs.getMapFactory().getMap(mo_mapid);
        return map.getMonsterByOid(mo_oid);
    }

    public MapleCharacter getOwner() {
        return ChannelServer.getCharacterFromAllServers(ownerid);
    }

    public Rectangle getBox() {
        return mistPosition;
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    public MaplePacket makeDestroyData() {
        return MaplePacketCreator.removeMist(getObjectId());
    }

    public MaplePacket makeSpawnData() {
        if (ownerid != -1) {
            MapleCharacter owner = getOwner();
            return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), owner.getSkillLevel(SkillFactory.getSkill(source.getSourceId())), this);
        } else {
            MapleMonster mob = getMobOwner();
        return MaplePacketCreator.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
        }
    }

    public MaplePacket makeFakeSpawnData(int level) {
        if (ownerid != -1) {
            MapleCharacter owner = getOwner();
            return MaplePacketCreator.spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), level, this);
        } else {
            MapleMonster mob = getMobOwner();
        return MaplePacketCreator.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(makeSpawnData());
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(makeDestroyData());
    }

    public boolean makeChanceResult() {
        return source.makeChanceResult();
    }
}
