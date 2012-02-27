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
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Jay Estrella
 */
public final class FamilyAddHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        //AB 00 0C 00 53 6E 69 66 66 69 6E 67 50 61 77 6B
        String toAdd = slea.readMapleAsciiString();
        MapleCharacter addChr = c.getChannelServer().getPlayerStorage().getCharacterByName(toAdd);
        if (addChr != null) {
            if (addChr.getFamilyChar().hasFamily()) {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (c.getPlayer().getLevel() <= addChr.getLevel()) {
                c.getPlayer().dropMessage(5, "You must be a higher level.");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            } else if (c.getPlayer().getLevel() > addChr.getLevel() + 20) {
                c.getPlayer().dropMessage(5, "Your junior must be less than 20 levels lower than you.");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            addChr.getClient().getSession().write(MaplePacketCreator.sendFamilyInvite(c.getPlayer().getId(), c.getPlayer().getName()));
            c.getPlayer().dropMessage(5, "The invite has been sent.");
        } else {
            c.getPlayer().dropMessage(5, "The player cannot be found!");
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}

