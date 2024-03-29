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

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import net.AbstractMaplePacketHandler;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import server.achievement.MapleAchievementLibrary;
import tools.data.input.SeekableLittleEndianAccessor;

public final class GiveFameHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter target = (MapleCharacter) c.getPlayer().getMap().getMapObject(MapleMapObjectType.PLAYER, slea.readInt());
        int mode = slea.readByte();
        if(mode > 1 && mode < -1){
            return; // fame exploiting :3
        }
        int famechange = 2 * mode - 1;
        if(famechange != 1 && famechange != -1){
            return;
        }
        MapleCharacter player = c.getPlayer();
        if ((target == player || player.getLevel() < 15)) {
            player.dropMessage(5, "Players that are below Lv.15 may not fame others.");
            return;
        }
        switch (player.canGiveFame(target)) {
            case OK:
                if (Math.abs(target.getFame() + famechange) < 30001) {
                    target.addFame(famechange);
                    target.updateSingleStat(MapleStat.FAME, target.getFame());
                }
                if (!player.isGM()) {
                    player.hasGivenFame(target);
                }
                c.announce(MaplePacketCreator.giveFameResponse(mode, target.getName(), target.getFame()));
                target.getClient().announce(MaplePacketCreator.receiveFame(mode, player.getName()));
                if(target.getFame() == 30000){
                    target.completeAchievement(MapleAchievementLibrary.REACHING_HIGHEST_FAME);
                }
                break;
            case NOT_TODAY:
                c.announce(MaplePacketCreator.giveFameErrorResponse(3));
                break;
            case NOT_THIS_MONTH:
                c.announce(MaplePacketCreator.giveFameErrorResponse(4));
                break;
        }
    }
}
