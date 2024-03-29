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

import client.MapleBuffStat;
import client.MapleCharacter;
import java.net.InetAddress;
import java.rmi.RemoteException;
import client.MapleClient;
import client.MapleInventoryType;
import java.io.IOException;
import net.AbstractMaplePacketHandler;
import net.world.MapleMessengerCharacter;
import net.world.PlayerStorage;
import server.MapleTrade;
import server.maps.FieldLimit;
import server.maps.HiredMerchant;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import server.PlayerTransfer;

/**
 *
 * @author Matze
 */
public final class ChangeChannelHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int channel = slea.readByte() + 1;
        MapleCharacter chr = c.getPlayer();
        if (chr.isBanned()) {
            c.disconnect();
            return;
        }
        try{
        if (!chr.isAlive() || FieldLimit.CHANGECHANNEL.check(chr.getMap().getFieldLimit()) || chr.shutDownLock) {
            c.announce(MaplePacketCreator.enableActions());
            c.announce(MaplePacketCreator.serverNotice(1, "You have a job to do! Please complete the current objective before proceeding."));
            return;
        }
        if (chr.getTrade() != null) {
            MapleTrade.cancelTrade(c.getPlayer());
        }
        HiredMerchant merchant = chr.getHiredMerchant();
        if (merchant != null) {
            if (merchant.isOwner(c.getPlayer())) {
                merchant.setOpen(true);
            } else {
                merchant.removeVisitor(c.getPlayer());
            }
        }
        try {
            c.getChannelServer().getWorldInterface().addBuffsToStorage(chr.getId(), chr.getAllBuffs());
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
        if (chr.getMessenger() != null) {
            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
            try {
                c.getChannelServer().getWorldInterface().silentLeaveMessenger(chr.getMessenger().getId(), messengerplayer);
            } catch (RemoteException e) {
                c.getChannelServer().reconnectWorld();
            }
        }
        chr.cancelMagicDoor();
        chr.saveCooldowns();
        chr.getExpirationTask().cancel(false);
        if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        }
        if (chr.getBuffedValue(MapleBuffStat.PUPPET) != null) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        }
        if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.COMBO);
        }
        chr.getInventory(MapleInventoryType.EQUIPPED).checked(false); //test
        } finally {
        chr.getMap().removePlayer(chr);
        chr.getClient().getChannelServer().removePlayer(chr);
        chr.saveToDB(true);
        chr.getClient().updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
        try {
            String[] socket = c.getChannelServer().getIP(channel).split(":");
            c.announce(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
        } catch (IOException e) {
        }
        }
        //PlayerTransfer.addCharacter(c.getPlayer());  
     //   PlayerTransfer.storeCharacter(chr);
      //  PlayerStorage.getInstance().addPlayer(chr);
    }
}