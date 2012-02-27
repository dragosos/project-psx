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
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import client.command.CommandProcessor;
import java.util.LinkedList;
import java.util.List;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.Pair;

public final class GeneralchatHandler extends net.AbstractMaplePacketHandler {

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int tickcount = slea.readInt();
        String s = slea.readMapleAsciiString();
        MapleCharacter chr = c.getPlayer();
        byte remaining = slea.readByte();
        if (!CommandProcessor.processCommand(c, s)) {
            if (chr.getMapId() == 910000000) {
                if (s.startsWith("I want ")) {
                    if ((chr.getPosition().getX() >= 333 && chr.getPosition().getX() <= 368) && (chr.getPosition().getY() == -17)) {
                        List<Pair<Integer, String>> itemPairs = new LinkedList<Pair<Integer, String>>();
                        for (Pair<Integer, String> allitems : MapleItemInformationProvider.getInstance().getAllItems()) {
                            if (allitems.getRight().toLowerCase().equals(s.substring(7).toLowerCase())) {
                                itemPairs.add(new Pair(allitems.getLeft(), allitems.getRight()));
                            }
                        }
                        if (itemPairs.isEmpty()) {
                            chr.dropMessage(" Sorry there is no Equipment with the name: " + s.substring(7).toString());
                        } else {
                            for (Pair<Integer, String> itemstoadd : itemPairs) {
                                MapleInventoryManipulator.addById(c, itemstoadd.getLeft(), (short) 1);
                                chr.dropMessage(" Have fun with your new: " + itemstoadd.getRight());
                            }
                        }
                    } else {
                        chr.dropMessage(" Sorry you are not standing on the barrel. Please move onto the barrrel in order to get the Equip you would like. ");
                    }
                }
            }
            if (chr.getMuted() != 0) {
                chr.dropMessage(chr.isPermMuted() ? "You have been perm muted. You can only be unmuted by a GM." : "You have been temp-muted. Wait for the map to be unmuted, or talk to the FMNPC in the FM to get unmuted.");
            } else {
                if (chr.gmLevel() == 1) {
                    chr.getMap().broadcastMessage(MaplePacketCreator.sendYellowTip("[Donor] " + chr.getName() + ": " + s));
                    chr.getMap().broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), s, chr.getGMChat(), 0));
                } else {
                    if (!chr.isHidden()) {
                        c.getPlayer().getMap().broadcastChatText(chr, remaining, s, false);
                    } else {
                        c.getPlayer().getMap().broadcastChatText(chr, remaining, s, true);
                    }
                }
            }
        }
    }
}

