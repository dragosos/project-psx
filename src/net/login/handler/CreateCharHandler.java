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
package net.login.handler;

import client.Equip;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleSkinColor;
import client.MapleJob;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import java.util.List;
import java.util.ArrayList;
import server.MapleItemInformationProvider;

public final class CreateCharHandler extends AbstractMaplePacketHandler {

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            MapleCharacter newchar = MapleCharacter.getDefault(c);
            boolean isDB = false;
            boolean cheating = false;
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            List<Integer> equips = new ArrayList<Integer>();
            newchar.setWorld(c.getWorld()); // TODO: World online checking x)
            String name = slea.readMapleAsciiString();
            int job = slea.readInt();
            int DBShort = slea.readShort();
            int face = slea.readInt();
            int hair = slea.readInt() + slea.readInt();
            int skinColor = slea.readInt();
            int top = slea.readInt();
            int bottom = slea.readInt();
            int shoes = slea.readInt();
            int weapon = slea.readInt();
            int gender = slea.readByte();
            equips.add(top);
            equips.add(bottom);
            equips.add(shoes);
            equips.add(weapon);
            /*
            for (Integer i : equips) {
            int itemid = i.intValue();
            if (ii.getEquipStats(itemid).get("reqLevel") > 10) {
            cheating = true;
            break;
            //} else if (ii.getEquipStats(itemid).get("incSTR") > 0) {
            //cheating = true;
            }
            }
            if (cheating) {
            c.banMacs();
            c.disconnect();
            }
             */
            if (!MapleCharacter.canCreateChar(name)) {
                return;
            }
            if (job == 1 && DBShort == 1) {
                isDB = true;
            }
            if (job == 0) { // Resistance
                newchar.setLevel(10);
                newchar.setMaxHpCreation(400);
                newchar.setMaxMpCreation(800);
                newchar.setRemainingSp(3);
                newchar.setRemainingAp(9 * 5);
                newchar.setJob(MapleJob.CITIZEN);
                newchar.setMap(130000000);
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161047, (byte) 0, (short) 1));
            } else if (job == 1) { // Adventurer
                newchar.setJob(MapleJob.BEGINNER);
                newchar.setMap(1020000);
                newchar.setLevel(10);
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1));
            } else if (job == 2) { // Knights of Cygnus
                newchar.setJob(MapleJob.NOBLESSE);
                newchar.setMap(130000000); // aran 140000000
                newchar.setLevel(10);
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161047, (byte) 0, (short) 1));
            } else if (job == 3) { // aran
                newchar.setJob(MapleJob.LEGEND);
                newchar.setMap(140000000); // aran 140000000
                newchar.setLevel(10);
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161048, (byte) 0, (short) 1));
            } else if (job == 4) { // Evan
                newchar.setJob(MapleJob.EVAN1);
                newchar.setLevel(10);
                newchar.setMap(1000000);
                newchar.setMaxHpCreation(200);
                newchar.setMaxMpCreation(400);
                newchar.setRemainingSp(1);
                newchar.setRemainingAp(9 * 5);
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161048, (byte) 0, (short) 1));
            } else {
                System.out.println("[CHAR CREATION] A new job ID has been found: " + job);
            }
            newchar.isDB = isDB;
            if (newchar.isDB) {
                newchar.setJob(MapleJob.BLADE_RECRUIT);
                newchar.setLevel(20);
                newchar.setMaxMpCreation(400);
                newchar.setMaxHpCreation(500);
                newchar.setRemainingSp(1);
                newchar.setRemainingAp(19 * 5);
            }
            newchar.setMeso(10000000);
            newchar.setGender(gender);
            newchar.setName(name);
            newchar.setFace(face);
            newchar.setHair(hair);
            newchar.setSkinColor(MapleSkinColor.getById(skinColor));
            MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
            if (job == 0) { // Resistance has Overalls 
                Equip eq_top = new Equip(top, (byte) -5, -1);
                eq_top.setWdef((short) 3);
                eq_top.setUpgradeSlots(7);
                equip.addFromDB(eq_top.copy());
                slea.skip(4);
            } else {
                Equip eq_top = new Equip(top, (byte) -5, -1);
                eq_top.setWdef((short) 3);
                eq_top.setUpgradeSlots(7);
                equip.addFromDB(eq_top.copy());
                Equip eq_bottom = new Equip(bottom, (byte) -6, -1);
                eq_bottom.setWdef((short) 2);
                eq_bottom.setUpgradeSlots(7);
                equip.addFromDB(eq_bottom.copy());
            }
            Equip eq_shoes = new Equip(shoes, (byte) -7, -1);
            eq_shoes.setWdef((short) 2); //rite? o_O
            eq_shoes.setUpgradeSlots(7);
            equip.addFromDB(eq_shoes.copy());
            Equip eq_weapon = new Equip(weapon, (byte) -11, -1);
            eq_weapon.setWatk((short) 15);
            eq_weapon.setUpgradeSlots(7);
            equip.addFromDB(eq_weapon.copy());
            newchar.saveToDB(false);
            //c.getSession().write(MaplePacketCreator.getLoginFailed(6));
            c.getSession().write(MaplePacketCreator.addNewCharEntry(newchar));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
