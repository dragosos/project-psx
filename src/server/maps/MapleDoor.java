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

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import java.lang.ref.WeakReference;
import net.channel.ChannelServer;
import server.MaplePortal;
import tools.MaplePacketCreator;

/**
 *
 * @author Matze
 */
public class MapleDoor extends AbstractMapleMapObject {
    private int ownerid;
    private WeakReference<MapleMap> town, target;
    private WeakReference<MaplePortal> townPortal;
    private Point targetPosition;

    public MapleDoor(int owner, Point targetPosition) {
        super();
        this.ownerid = owner;
        MapleCharacter c = ChannelServer.getCharacterFromAllServers(owner);
        this.target = new WeakReference<MapleMap>(c.getMap());
        this.targetPosition = targetPosition;
        setPosition(this.targetPosition);
        this.town = new WeakReference<MapleMap>(this.target.get().getReturnMap());
        this.townPortal = new WeakReference<MaplePortal>(getFreePortal());
    }

    public MapleDoor(MapleDoor origDoor) {
        super();
        this.ownerid = origDoor.ownerid;
        this.town = origDoor.town;
        this.townPortal = origDoor.townPortal;
        this.target = origDoor.target;
        this.targetPosition = origDoor.targetPosition;
        this.townPortal = origDoor.townPortal;
        setPosition(this.townPortal.get().getPosition());
    }

    private MaplePortal getFreePortal() {
        List<MaplePortal> freePortals = new ArrayList<MaplePortal>();
        for (MaplePortal port : town.get().getPortals()) {
            if (port.getType() == 6) {
                freePortals.add(port);
            }
        }
        Collections.sort(freePortals, new Comparator<MaplePortal>() {
            public int compare(MaplePortal o1, MaplePortal o2) {
                if (o1.getId() < o2.getId()) {
                    return -1;
                } else if (o1.getId() == o2.getId()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (MapleMapObject obj : town.get().getMapObjects()) {
            if (obj instanceof MapleDoor) {
                MapleDoor door = (MapleDoor) obj;
                if (door.getOwner().getParty() != null &&
                        getOwner().getParty().containsMembers(door.getOwner().getMPC())) {
                    freePortals.remove(door.getTownPortal());
                }
            }
        }
        return freePortals.iterator().next();
    }

    public void sendSpawnData(MapleClient client) {
        MapleCharacter owner = getOwner();
        MapleMap target = getTarget(), town = getTown();
        if (target.getId() == client.getPlayer().getMapId() || owner == client.getPlayer() && owner.getParty() == null) {
            client.getSession().write(MaplePacketCreator.spawnDoor(owner.getId(), town.getId() == client.getPlayer().getMapId() ? townPortal.get().getPosition() : targetPosition, true));
            if (owner.getParty() != null && (owner == client.getPlayer() || owner.getParty().containsMembers(client.getPlayer().getMPC()))) {
                client.getSession().write(MaplePacketCreator.partyPortal(town.getId(), target.getId(), targetPosition));
            }
            client.getSession().write(MaplePacketCreator.spawnPortal(town.getId(), target.getId(), targetPosition));
        }
    }

    public void sendDestroyData(MapleClient client) {
        MapleCharacter owner = getOwner();
        if (target.get().getId() == client.getPlayer().getMapId() || owner == client.getPlayer() || owner.getParty() != null && owner.getParty().containsMembers(client.getPlayer().getMPC())) {
            if (owner.getParty() != null && (owner == client.getPlayer() || owner.getParty().containsMembers(client.getPlayer().getMPC()))) {
                client.getSession().write(MaplePacketCreator.partyPortal(999999999, 999999999, new Point(-1, -1)));
            }
            client.getSession().write(MaplePacketCreator.removeDoor(owner.getId(), false));
            client.getSession().write(MaplePacketCreator.removeDoor(owner.getId(), true));
        }
    }

    public void warp(MapleCharacter chr, boolean toTown) {
        MapleCharacter owner = getOwner();
        if (chr == owner || owner.getParty() != null && owner.getParty().containsMembers(chr.getMPC())) {
            if (!toTown) {
                chr.changeMap(target.get(), targetPosition);
            } else {
                chr.changeMap(town.get(), townPortal.get());
            }
        } else {
            chr.getClient().getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public MapleCharacter getOwner() {
        return ChannelServer.getCharacterFromAllServers(ownerid);
    }

    public MapleMap getTown() {
        return town.get();
    }

    public MaplePortal getTownPortal() {
        return townPortal.get();
    }

    public MapleMap getTarget() {
        return target.get();
    }

    public Point getTargetPosition() {
        return targetPosition;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }
}
