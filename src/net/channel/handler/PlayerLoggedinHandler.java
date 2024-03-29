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

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.SkillFactory;
import constants.SkillConstants;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.List;
import tools.DatabaseConnection;
import java.util.*;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.world.CharacterIdChannelPair;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.PlayerBuffValueHolder;
import net.world.guild.MapleAlliance;
import net.world.guild.MapleGuild;
import net.world.remote.WorldChannelInterface;
import server.clones.*;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class PlayerLoggedinHandler extends AbstractMaplePacketHandler {

    @Override
    public final boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int cid = slea.readInt();
        //MapleCharacter player = PlayerStorage.getInstance().removePlayer(cid);
        MapleCharacter player = null;
        if (player == null) {
            try {
                player = MapleCharacter.loadCharFromDB(cid, c, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            player.channelChanged(c);
        }
        if (!hasCharOnAccount(player.getName(), c.getAccountName())) {
            c.getPlayer().ban("Remote hacking", true);
            return;
        }
        c.setPlayer(player);
        c.setAccID(player.getAccountID());
        int state = c.getLoginState();
        boolean allowLogin = true;
        ChannelServer cserv = c.getChannelServer();
        MapleCharacter chr = c.getPlayer();
        chr.logintime = System.currentTimeMillis();
        synchronized (this) {
            try {
                WorldChannelInterface worldInterface = cserv.getWorldInterface();
                if (state == MapleClient.LOGIN_SERVER_TRANSITION) {
                    for (String charName : c.loadCharacterNames(c.getWorld())) {
                        if (worldInterface.isConnected(charName)) {
                            MapleCharacter to_dc = ChannelServer.getCharacterFromAllServers(charName);
                            to_dc.getClient().disconnect(); // disconnecting again
                            if (cserv.getPlayerStorage().getCharacterByName(charName) != null) {
                                c.getChannelServer().removePlayer(to_dc);
                            }
                            if (!worldInterface.isConnected(charName)) {
                                allowLogin = true;
                            } else {
                                allowLogin = false;
                            }
                            break;
                        }
                    }
                }
            } catch (RemoteException e) {
                cserv.reconnectWorld();
                allowLogin = false;
                e.printStackTrace();
                System.out.println("WORLD SERVER HAS CRASHED.");
            }

            if (state != MapleClient.LOGIN_SERVER_TRANSITION || !allowLogin) {
                c.setPlayer(null);
                c.getSession().close(true);
                return;
            }
            c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);
        }
        cserv.addPlayer(player);
        try {
            List<PlayerBuffValueHolder> buffs = cserv.getWorldInterface().getBuffsFromStorage(cid);
            if (buffs != null) {
                chr.silentGiveBuffs(buffs);
            }
        } catch (RemoteException e) {
            cserv.reconnectWorld();
        }
        // c.getSession().write(MaplePacketCreator.loadFamily(player));
//        c.getSession().write(MaplePacketCreator.getFamilyInfo(player));
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM cooldowns WHERE charid = ?");
            ps.setInt(1, chr.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final int skillid = rs.getInt("SkillID");
                final long length = rs.getLong("length"), startTime = rs.getLong("StartTime");
                if (length + startTime < System.currentTimeMillis()) {
                    continue;
                }
                chr.giveCoolDowns(skillid, startTime, length);
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM cooldowns WHERE charid = ?");
            ps.setInt(1, chr.getId());
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("SELECT Mesos FROM dueypackages WHERE RecieverId = ? and Checked = 1");
            ps.setInt(1, chr.getId());
            rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    PreparedStatement pss = DatabaseConnection.getConnection().prepareStatement("UPDATE dueypackages SET Checked = 0 where RecieverId = ?");
                    pss.setInt(1, chr.getId());
                    pss.executeUpdate();
                    pss.close();
                } catch (SQLException e) {
                }
                c.announce(MaplePacketCreator.sendDueyMSG((byte) 0x1B));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        c.announce(MaplePacketCreator.getCharInfo(player));
        // loading all skills
        Map<client.ISkill, MapleCharacter.SkillEntry> skills = chr.getSkills();
        for (Entry<client.ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
            chr.announce(MaplePacketCreator.updateSkill(skill.getKey().getId(), skill.getValue().skillevel, skill.getValue().masterlevel));
        }
        if (player.isGM() || player.isMWLB()) {
            player.toggleHide(true, true);
        }
        if (!player.isGM()) {
            player.toggleHide(false, false);
        }

        chr.sendMacros();
        /*if(player.getDonator().isActive()){
        for (int charge = 1211005; charge < 1211007; charge++) {
        player.changeSkillLevel(SkillFactory.getSkill(charge), (byte)SkillFactory.getSkill(charge).getMaxLevel(), SkillFactory.getSkill(charge).getMaxLevel(), -1);
        } 
        chr.changeSkillLevel(client.SkillFactory.getSkill(4111003), (byte)client.SkillFactory.getSkill(4111003).getMaxLevel(), client.SkillFactory.getSkill(4111003).getMaxLevel(), -1);
        chr.changeSkillLevel(client.SkillFactory.getSkill(4111003), (byte)client.SkillFactory.getSkill(4111003).getMaxLevel(), client.SkillFactory.getSkill(4111003).getMaxLevel(), -1);
        }*/
        player.sendKeymap();
        /*  if(chr.getAllPokemon().isEmpty()){
        int chance = (int)(2 * Math.random());
        chr.addPokemon(new MaplePokemon(MaplePokemonLibrary.getById(MaplePokemonLibrary.getRandomPokemonID()), chr, -1, 1, 0));
        chr.dropMessage(5, "<???> : Hi there.. I'm a Pokemon. Mind if I tag along?");
        }*/
        player.getMap().addPlayer(player);
        chr.announce(MaplePacketCreator.MapMessage("Welcome to Project PSX."));
        if (chr.isJaguar()) {
            chr.announce(MaplePacketCreator.updateJaguar());
        }
        chr.recalcLocalStats();
        try {
            int buddyIds[] = player.getBuddylist().getBuddyIds();
            cserv.getWorldInterface().loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : cserv.getWorldInterface().multiBuddyFind(player.getId(), buddyIds)) {
                BuddylistEntry ble = player.getBuddylist().get(onlineBuddy.getCharacterId());
                ble.setChannel(onlineBuddy.getChannel());
                player.getBuddylist().put(ble);
            }
            c.announce(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));
        } catch (RemoteException e) {
            cserv.reconnectWorld();
        }

        try {
            chr.showNote();
            if (player.getParty() != null) {
                MaplePartyCharacter pchar = player.getMPC();
                pchar.setChannel(c.getChannel());
                pchar.setMapId(chr.getMapId());
                pchar.setOnline(true);
                cserv.getWorldInterface().updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, pchar);
            }
            player.updatePartyMemberHP();
        } catch (RemoteException e) {
            cserv.reconnectWorld();
        }
        CharacterNameAndId pendingBuddyRequest = player.getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            player.getBuddylist().put(new BuddylistEntry(pendingBuddyRequest.getName(), "Default Group", pendingBuddyRequest.getId(), -1, false));
            c.announce(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), chr.getId(), pendingBuddyRequest.getName()));
        }
        if (player.getInventory(MapleInventoryType.EQUIPPED).findById(1122017) != null) {
            player.equipPendantOfSpirit();
        }
        c.announce(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));
        //   c.announce(MaplePacketCreator.updateGender(player));
        player.checkMessenger();
        //     c.announce(MaplePacketCreator.enableReport());
        //   player.changeSkillLevel(SkillFactory.getSkill(10000000 * player.getJobType() + 12), (byte) 20, 20, -1);
        player.checkBerserk();
        player.expirationTask();
        player.setRates();
        if (chr.isBeginnerJob()) {
            c.announce(MaplePacketCreator.getWhisper("The White Lady", 1, "You will get the normal amount of experience as soon as you job advance."));
        }
        player.getDonator().hasExpireDatePassed();
        if (player.isEvan()) {
            player.getEvanDragon().sendSpawnData(c);
        }
        if (player.getSkills().size() < 10) {
            player.maxAllSkillsByJobGroup();
        }

        if (player.getGuildId() > 0) {

            try {
                MapleGuild playerGuild = cserv.getWorldInterface().getGuild(player.getGuildId(), player.getMGC());
                if (playerGuild == null) {
                    player.deleteGuild(player.getGuildId());
                    player.resetMGC();
                    player.setGuildId(0);
                } else {
                    cserv.getWorldInterface().setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                    //    player.getMGC().setOnline(true);
                    c.announce(MaplePacketCreator.showGuildInfo(player));
                    int allianceId = player.getGuild().getAllianceId();
                    if (allianceId > 0) {
                        MapleAlliance newAlliance = cserv.getWorldInterface().getAlliance(allianceId);
                        if (newAlliance == null) {
                            newAlliance = MapleAlliance.loadAlliance(allianceId);
                            if (newAlliance != null) {
                                cserv.getWorldInterface().addAlliance(allianceId, newAlliance);
                            } else {
                                player.getGuild().setAllianceId(0);
                            }
                        }
                        if (newAlliance != null) {
                            //         c.announce(MaplePacketCreator.getAllianceInfo(newAlliance));
                            //        c.announce(MaplePacketCreator.getGuildAlliances(newAlliance, c));
                            //  cserv.getWorldInterface().allianceMessage(allianceId, MaplePacketCreator.allianceMemberOnline(player, true), player.getId(), -1);
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("Lost conection to the world server.. bringing it back up.");
                cserv.reconnectWorld();
            }


        }
        if (c.getPlayer().newMail() > 0) {
            player.dropMessage("[Notice]: You have " + c.getPlayer().newMail() + " unread messages.");
        }
    }

    public boolean hasCharOnAccount(String name, String accountName) {
        boolean hasChar = true;
        int accountid = 0;
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
            ps.setString(1, accountName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                accountid = rs.getInt("id");
                ps.close();
                ps = con.prepareStatement("SELECT * FROM characters where accountid = ? && name = ?");
                ps.setInt(1, accountid);
                ps.setString(2, name);
                rs = ps.executeQuery();
                if (rs.next()) {
                    return true;
                } else {
                    return false;
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return hasChar;
        }
    }
}
