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
import net.world.MaplePartyCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.autoban.AutobanFactory;
import constants.SkillConstants.*;
import java.awt.Point;
import net.AbstractMaplePacketHandler;
import scripting.item.ItemScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleItemInformationProvider.scriptedItem;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class ItemPickupHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        slea.skip(9);
        int oid = slea.readInt();
        MapleCharacter chr = c.getPlayer();
        MapleMapObject ob = chr.getMap().getMapObject(oid);
        c.announce(MaplePacketCreator.enableActions());
        Point cpos = ob.getPosition();
        if (chr.getInventory(MapleItemInformationProvider.getInstance().getInventoryType(ob.getObjectId())).getNextFreeSlot() > -1) {
            if (chr.getMapId() > 209000000 && chr.getMapId() < 209000016 && chr.getPQ() == null) {//happyville trees
                MapleMapItem mapitem = (MapleMapItem) ob;
                if (mapitem.getDropper().getObjectId() == c.getPlayer().getObjectId()) {
                    if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), false)) {
                        chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                        chr.getMap().removeMapObject(ob);
                    } else {
                        return;
                    }
                    mapitem.setPickedUp(true);
                } else {
                    c.announce(MaplePacketCreator.getInventoryFull());
                    c.announce(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                return;
            }
            if (ob == null) {
                c.announce(MaplePacketCreator.getInventoryFull());
                c.announce(MaplePacketCreator.getShowInventoryFull());
                return;
            }
            if (ob instanceof MapleMapItem) {
                MapleMapItem mapitem = (MapleMapItem) ob;
                synchronized (mapitem) {
                    if (mapitem.getQuest() > 0 && !chr.needQuestItem(mapitem.getQuest(), mapitem.getItemId())) {
                        c.announce(MaplePacketCreator.showItemUnavailable());
                        c.announce(MaplePacketCreator.enableActions());
                        return;
                    }
                    if (mapitem.isPickedUp()) {
                        c.announce(MaplePacketCreator.getInventoryFull());
                        c.announce(MaplePacketCreator.getShowInventoryFull());
                        return;
                    }
                    final double Distance = cpos.distanceSq(mapitem.getPosition());
                    if (Distance > 2500) {
                        AutobanFactory.SHORT_ITEM_VAC.autoban(chr, cpos.toString() + Distance);
                    } else if (chr.getPosition().distanceSq(mapitem.getPosition()) > 90000.0) {
                        AutobanFactory.ITEM_VAC.autoban(chr, cpos.toString() + Distance);
                    }
                    if (mapitem.getMeso() > 0) {
                        if (chr.getParty() != null) {
                            int mesosamm = mapitem.getMeso();
                            if (mesosamm > 50000 * chr.getMesoRate()) {
                                return;
                            }
                            int partynum = 0;
                            for (MaplePartyCharacter partymem : chr.getParty().getMembers()) {
                                if (partymem.isOnline() && partymem.getMapId() == chr.getMap().getId() && partymem.getChannel() == c.getChannel()) {
                                    partynum++;
                                }
                            }
                            for (MaplePartyCharacter partymem : chr.getParty().getMembers()) {
                                if (partymem.isOnline() && partymem.getMapId() == chr.getMap().getId()) {
                                    MapleCharacter somecharacter = c.getChannelServer().getPlayerStorage().getCharacterById(partymem.getId());
                                    if (somecharacter != null) {
                                        somecharacter.gainMeso(mesosamm / partynum, true, true, false);
                                    }
                                }
                            }
                        } else {
                            chr.gainMeso(mapitem.getMeso(), true, true, false);
                        }
                        chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                        chr.getMap().removeMapObject(ob);
                    } else if (mapitem.getItem().getItemId() / 10000 == 243) {
                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        scriptedItem info = ii.getScriptedItemInfo(mapitem.getItem().getItemId());
                        if (info.runOnPickup()) {
                            ItemScriptManager ism = ItemScriptManager.getInstance();
                            String scriptName = info.getScript();
                            if (ism.scriptExists(scriptName))
                                ism.getItemScript(c, scriptName);
                            
                        } else {
                            MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true);
                        }
                        chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                        chr.getMap().removeMapObject(ob);
                    } else if (useItem(c, mapitem.getItem().getItemId())) {
                        if (mapitem.getItem().getItemId() / 10000 == 238) {
                          //  chr.getMonsterBook().addCard(c, mapitem.getItem().getItemId());
                        }
                        mapitem.setPickedUp(true);
                        chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                        chr.getMap().removeMapObject(ob);
                    } else if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true)) {
                        chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
                        chr.getMap().removeMapObject(ob);
                    } else if (mapitem.getItem().getItemId() == 4031868) {
                        chr.getMap().broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chr.getName(), chr.getItemQuantity(4031868, false), false));
                        chr.getMap().removeMapObject(ob);
                    } else {
                        return;
                    }
                    mapitem.setPickedUp(true);
                }
            }            
        }
        c.announce(MaplePacketCreator.enableActions());
    }

    static boolean useItem(final MapleClient c, final int id) {
     if(id == 2022166){ // stunner!
         client.ISkill blind = client.SkillFactory.getSkill(4111003);
         c.getPlayer().changeSkillLevel(blind, (byte)blind.getMaxLevel(), blind.getMaxLevel(), -1);
            for(MapleMapObject o : c.getPlayer().getMap().getMapObjects()) {
                if(o.getType() == MapleMapObjectType.MONSTER && o.getPosition().distanceSq(c.getPlayer().getPosition()) <= 200000.0){
                server.life.MapleMonster monster = (server.life.MapleMonster) o;
                monster.applyStatus(c.getPlayer(), new client.status.MonsterStatusEffect(java.util.Collections.singletonMap(client.status.MonsterStatus.SHADOW_WEB, 1), client.SkillFactory.getSkill(4111003), null, false), false,  (long)(10000.0 * Math.random() + 5000));
            }
            }
            return true;
        } if(id == 2022165){
            client.ISkill blind = client.SkillFactory.getSkill(Priest.DOOM);
            c.getPlayer().changeSkillLevel(blind, (byte)blind.getMaxLevel(), blind.getMaxLevel(), -1);
            for(MapleMapObject o : c.getPlayer().getMap().getMapObjects()){
                if(o.getType() == MapleMapObjectType.MONSTER && o.getPosition().distanceSq(c.getPlayer().getPosition()) <= 200000.0){
                server.life.MapleMonster monster = (server.life.MapleMonster) o;
                monster.applyStatus(c.getPlayer(), new client.status.MonsterStatusEffect(java.util.Collections.singletonMap(client.status.MonsterStatus.DOOM, 1), client.SkillFactory.getSkill(blind.getId()), null, false), false,  (long)(10000.0 * Math.random() + 5000));
            }
            }
            return true;
        }
        if (id / 1000000 == 2) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (ii.isConsumeOnPickup(id)) {
                if (id > 2022430 && id < 2022434) {
                    for (MapleCharacter mc : c.getPlayer().getMap().getCharacters()) {
                        if (mc.getParty() == c.getPlayer().getParty()) {
                            ii.getItemEffect(id).applyTo(mc);
                        }
                    }
                } else {
                    ii.getItemEffect(id).applyTo(c.getPlayer());
                }
                return true;
            }
        } if(id == 4031530 || id == 4031531){
            c.getPlayer().getCashShop().gainCash(4, id == 4031530 ? 100 : 250);
            c.announce(MaplePacketCreator.MapMessage("You have gained " + (id == 4031530 ? 100 : 250) + "NX Cash Points!"));
           // c.announce(MaplePacketCreator.serverNotice(5, "You have gained " + (id == 4031530 ? 100 : 250) + "NX Cash Points!"));
            return true;
        }
        return false;
    }
}
