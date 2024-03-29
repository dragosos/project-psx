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

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import net.world.guild.*;
import java.util.Iterator;
import tools.MaplePacketCreator;
import client.MapleCharacter;
import server.achievement.MapleAchievementLibrary;

public final class GuildOperationHandler extends AbstractMaplePacketHandler {
    private boolean isGuildNameAcceptable(String name) {
        if (name.length() < 3 || name.length() > 12) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLowerCase(name.charAt(i)) && !Character.isUpperCase(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void respawnPlayer(MapleCharacter mc) {
        mc.getMap().broadcastMessage(mc, MaplePacketCreator.removePlayerFromMap(mc.getId()), false);
        mc.getMap().broadcastMessage(mc, MaplePacketCreator.spawnPlayerMapobject(mc), false);
    }

    private class Invited {
        public String name;
        public int gid;
        public long expiration;

        public Invited(String n, int id) {
            name = n.toLowerCase();
            gid = id;
            expiration = System.currentTimeMillis() + 60 * 60 * 1000;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Invited)) {
                return false;
            }
            Invited oth = (Invited) other;
            return (gid == oth.gid && name.equals(oth));
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 83 * hash + this.gid;
            return hash;
        }
    }
    private java.util.List<Invited> invited = new java.util.LinkedList<Invited>();
    private long nextPruneTime = System.currentTimeMillis() + 20 * 60 * 1000;

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        System.out.println("GUILD : " + slea);
        if (System.currentTimeMillis() >= nextPruneTime) {
            Iterator<Invited> itr = invited.iterator();
            Invited inv;
            while (itr.hasNext()) {
                inv = itr.next();
                if (System.currentTimeMillis() >= inv.expiration) {
                    itr.remove();
                }
            }
            nextPruneTime = System.currentTimeMillis() + 20 * 60 * 1000;
        }
        MapleCharacter mc = c.getPlayer();
        byte type = slea.readByte();
        switch (type) {
            case 0x00:
                //c.announce(MaplePacketCreator.showGuildInfo(mc));
                break;
            case 0x02:
                if (mc.getGuildId() > 0 || mc.getMapId() != 200000301) {
                    c.getPlayer().dropMessage(1, "You cannot create a new Guild while in one.");
                    return;
                }
                if (mc.getMeso() < MapleGuild.CREATE_GUILD_COST) {
                    c.getPlayer().dropMessage(1, "You do not have enough mesos to create a Guild.");
                    return;
                }
                String guildName = slea.readMapleAsciiString();
                if (!isGuildNameAcceptable(guildName)) {
                    c.getPlayer().dropMessage(1, "The Guild name you have chosen is not accepted.");
                    return;
                }
                int gid;
                try {
                    gid = c.getChannelServer().getWorldInterface().createGuild(mc.getId(), guildName);
                } catch (java.rmi.RemoteException re) {
                    System.out.println("RemoteException occurred " + re);
                    c.getPlayer().dropMessage(5, "Unable to connect to the World Server. Please try again later.");
                    return;
                }
                if (gid == 0) {
                    c.announce(MaplePacketCreator.genericGuildMessage((byte) 0x1c));
                    return;
                }
                mc.gainMeso(-MapleGuild.CREATE_GUILD_COST, true, false, true);
                mc.setGuildId(gid);
                mc.setGuildRank(1);
                mc.saveGuildStatus();
                c.announce(MaplePacketCreator.showGuildInfo(mc));
                c.getPlayer().dropMessage(1, "You have successfully created a Guild.");
                c.getPlayer().completeAchievement(MapleAchievementLibrary.MADE_A_GUILD);
                respawnPlayer(mc);
                break;
            case 0x05:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() > 2) {
                    return;
                }
                String name = slea.readMapleAsciiString();
                MapleGuildResponse mgr = MapleGuild.sendInvite(c, name);
                if (mgr != null) {
                    c.announce(mgr.getPacket());
                } else {
                    Invited inv = new Invited(name, mc.getGuildId());
                    if (!invited.contains(inv)) {
                        invited.add(inv);
                    }
                }
                break;
            case 0x06:
                if (mc.getGuildId() > 0) {
                    System.out.println("[hax] " + mc.getName() + " attempted to join a guild when s/he is already in one.");
                    return;
                }
                gid = slea.readInt();
                int cid = slea.readInt();
                if (cid != mc.getId()) {
                    System.out.println("[hax] " + mc.getName() + " attempted to join a guild with a different character id.");
                    return;
                }
                name = mc.getName().toLowerCase();
                Iterator<Invited> itr = invited.iterator();
                boolean bOnList = false;
                while (itr.hasNext()) {
                    Invited inv = itr.next();
                    if (gid == inv.gid && name.equals(inv.name)) {
                        bOnList = true;
                        itr.remove();
                        break;
                    }
                }
                if (!bOnList) {
                    System.out.println("[hax] " + mc.getName() + " is trying to join a guild that never invited him/her (or that the invitation has expired)");
                    return;
                }
                mc.setGuildId(gid); // joins the guild
                mc.setGuildRank(5); // start at lowest rank
                int s;
                try {
                    s = c.getChannelServer().getWorldInterface().addGuildMember(mc.getMGC());
                } catch (java.rmi.RemoteException e) {
                    System.out.println("RemoteException occurred while attempting to add character to guild " + e);
                    c.getPlayer().dropMessage(5, "Unable to connect to the World Server. Please try again later.");
                    mc.setGuildId(0);
                    return;
                }
                if (s == 0) {
                    c.getPlayer().dropMessage(1, "The Guild you are trying to join is already full.");
                    mc.setGuildId(0);
                    return;
                }
                c.announce(MaplePacketCreator.showGuildInfo(mc));
                mc.saveGuildStatus(); // update database
                c.getPlayer().completeAchievement(MapleAchievementLibrary.JOIN_A_GUILD);
                respawnPlayer(mc);
                break;
            case 0x07:
                cid = slea.readInt();
                name = slea.readMapleAsciiString();
                if (cid != mc.getId() || !name.equals(mc.getName()) || mc.getGuildId() <= 0) {
                    System.out.println("[hax] " + mc.getName() + " tried to quit guild under the name \"" + name + "\" and current guild id of " + mc.getGuildId() + ".");
                    return;
                }
                try {
                    c.getChannelServer().getWorldInterface().leaveGuild(mc.getMGC());
                } catch (java.rmi.RemoteException re) {
                    re.printStackTrace();
                    System.out.println("RemoteException occurred while attempting to leave guild " + re);
                    c.getPlayer().dropMessage(5, "Unable to connect to the World Server. Please try again later.");
                    return;
                }
                c.announce(MaplePacketCreator.showGuildInfo(null));
                mc.setGuildId(0);
                mc.saveGuildStatus();
                respawnPlayer(mc);
                break;
            case 0x08:
                cid = slea.readInt();
                name = slea.readMapleAsciiString();
                if (mc.getGuildRank() > 2 || mc.getGuildId() <= 0) {
                    System.out.println("[hax] " + mc.getName() + " is trying to expel without rank 1 or 2.");
                    return;
                }
                try {
                    c.getChannelServer().getWorldInterface().expelMember(mc.getMGC(), name, cid);
                } catch (java.rmi.RemoteException re) {
                    re.printStackTrace();
                    System.out.println("RemoteException occurred while attempting to change rank " + re);
                    c.getPlayer().dropMessage(5, "Unable to connect to the World Server. Please try again later.");
                    return;
                }
                break;
            case 0x0E:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() != 1) {
                    System.out.println("[hax] " + mc.getName() + " tried to change guild rank titles when s/he does not have permission.");
                    return;
                }
                String ranks[] = new String[5];
                for (int i = 0; i < 5; i++) {
                    ranks[i] = slea.readMapleAsciiString();
                }
                try {
                    c.getChannelServer().getWorldInterface().changeRankTitle(mc.getGuildId(), ranks);
                } catch (java.rmi.RemoteException re) {
                    re.printStackTrace();
                    System.out.println("RemoteException occurred " + re);
                    c.announce(MaplePacketCreator.serverNotice(5, "Unable to connect to the World Server. Please try again later."));
                    return;
                }
                break;
            case 0x0F:
                cid = slea.readInt();
                byte newRank = slea.readByte();
                if (mc.getGuildRank() > 2 || (newRank <= 2 && mc.getGuildRank() != 1) || mc.getGuildId() <= 0) {
                    System.out.println("[hax] " + mc.getName() + " is trying to change rank outside of his/her permissions.");
                    return;
                }
                if (newRank <= 1 || newRank > 5) {
                    return;
                }
                try {
                    c.getChannelServer().getWorldInterface().changeRank(mc.getGuildId(), cid, newRank);
                } catch (java.rmi.RemoteException re) {
                    re.printStackTrace();
                    System.out.println("RemoteException occurred while attempting to change rank " + re);
                    c.announce(MaplePacketCreator.serverNotice(5, "Unable to connect to the World Server. Please try again later."));
                    return;
                }
                break;
            case 0x10:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() != 1 || mc.getMapId() != 200000301) {
                    System.out.println("[hax] " + mc.getName() + " tried to change guild emblem without being the guild leader.");
                    return;
                }
                if (mc.getMeso() < MapleGuild.CHANGE_EMBLEM_COST) {
                    c.announce(MaplePacketCreator.serverNotice(1, "You do not have enough mesos to create a Guild or make an emblem."));
                    return;
                }
                short bg = slea.readShort();
                byte bgcolor = slea.readByte();
                short logo = slea.readShort();
                byte logocolor = slea.readByte();
                try {
                    c.getChannelServer().getWorldInterface().setGuildEmblem(mc.getGuildId(), bg, bgcolor, logo, logocolor);
                } catch (java.rmi.RemoteException re) {
                    System.out.println("RemoteException occurred " + re);
                    c.announce(MaplePacketCreator.serverNotice(5, "Unable to connect to the World Server. Please try again later."));
                    return;
                }
                mc.gainMeso(-MapleGuild.CHANGE_EMBLEM_COST, true, false, true);
                respawnPlayer(mc);
                break;
            case 0x11:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() > 2) {
                    System.out.println("[hax] " + mc.getName() + " tried to change guild notice while not in a guild.");
                    return;
                }
                String notice = slea.readMapleAsciiString();
                if (notice.length() > 100) {
                    return;
                }
                try {
                    c.getChannelServer().getWorldInterface().setGuildNotice(mc.getGuildId(), notice);
                } catch (java.rmi.RemoteException re) {
                    System.out.println("RemoteException occurred " + re);
                    c.announce(MaplePacketCreator.serverNotice(5, "Unable to connect to the World Server. Please try again later."));
                    return;
                }
                break;
            default:
                System.out.println("Unhandled GUILD_OPERATION packet: \n" + slea.toString());
        }
    }
}
