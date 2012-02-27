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

import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import server.movement.LifeMovementFragment;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class MovePetHandler extends AbstractMovementPacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int petId = slea.readInt();
        slea.readLong();
        List<LifeMovementFragment> res = parseMovement(slea);
        if (res.isEmpty()) {
            return;
        }
        MapleCharacter player = c.getPlayer();
        byte slot = player.getPetIndex(petId);
        if (slot == -1) {
            return;
        }
        
        player.getPet(slot).updatePosition(res);
        player.getMap().broadcastMessage(player, MaplePacketCreator.movePet(player.getId(), petId, slot, res), false);
        if(c.getPlayer().getInventory(client.MapleInventoryType.EQUIPPED).findById(1002419) != null || c.getPlayer().getDonator().isActive()){ // has mark of the beta equipped!
        conductPetAttacking(c.getPlayer(), c.getPlayer().getDonator().isActive());
        } if(c.getPlayer().getInventory(client.MapleInventoryType.EQUIPPED).findById(1032033) != null || c.getPlayer().getDonator().isActive()) { // Protector Rock!
            conductPetProtection(c.getPlayer(), c.getPlayer().getDonator().isActive());
        } 
    }
    
    public void conductPetProtection(MapleCharacter chr, boolean perk){
        if(System.currentTimeMillis() - chr.getAutobanManager().lastPetBuff <= 1000 * 60 * 2){
            return;
        } else {
        chr.getAutobanManager().lastPetBuff = System.currentTimeMillis();
        String[] dialog = {"Here Master, Stand up straight!", "Expulsum everto!", "Stay Sharp!" , "Dieme Reflecto! ", "GO! MOVE!"};
        int[] SkillIDs = {21121003, 21121008, 2311003, 3221002, 3321002, 4101004};
        for(int i = 0; i < chr.getPets().length; i++){
            if((int)10.0 * Math.random() > 7){
            chr.getMap().broadcastMessage(MaplePacketCreator.petChat(chr.getId(), (byte)i, 1, dialog[i]));
            }
            if(perk){
      for(int e = 0; e < SkillIDs.length; e++){
            chr.giveMaxBuff(SkillIDs[e]);
            if(chr.getParty() != null){
                for(net.world.MaplePartyCharacter mpc : chr.getParty().getMembers()){
                    client.ISkill s = SkillFactory.getSkill(SkillIDs[i]);
                    MapleCharacter c = mpc.getPlayer();
                    c.giveBuff(SkillIDs[e], 5);
                    c = null;
                }
            }
                }
            } else {
            chr.giveMaxBuff(SkillIDs[i]);
            if(chr.getParty() != null){
                for(net.world.MaplePartyCharacter mpc : chr.getParty().getMembers()){
                    MapleCharacter c = mpc.getPlayer();
                    c.giveBuff(SkillIDs[i], 5);
                    c = null;
                }
            }
            }
        }
        }
    }

    public void conductPetAttacking(MapleCharacter chr, boolean perk){
        if(chr.getPets() != null){
            String[] monsterDialog = {"How dare you attack my master! Take that!", "Attacking monsters!", "RAWWWR", "Die.", "Muffin pancakes <3"};
            
            for(int i = 0; i < chr.getPets().length; i++){
                List<server.life.MapleMonster> moInRange = chr.getMap().getMapMonstersInRange(chr.getPet(i).getPos(), 15000.0, server.maps.MapleMapObjectType.MONSTER);
                int damage = (int)(((chr.getPet(i).getLevel() * 5)* (int)(2.0 * Math.random() + 2)));
                int level = chr.getLevel();
                int chance = (int)(100.0 * Math.random());
                int attackAmount = (int)(100.0 * Math.random());
                if(level >= 10 && level < 30){
                    damage *= 5;
                } else if(level >= 30 && level < 70){
                    damage *= 12;
                } else if(level >= 70 && level < 120){
                    damage *= 17;
                } else if(level >= 120 && level < 150){
                    damage *= 27;
                } else if(level >= 120){
                    damage *= 40;
                }
                if(attackAmount >= 95){
                    attackAmount = (int)(3.0 * Math.random()) + 1;
                } else {
                    attackAmount = 1;
                } if(perk){
                    attackAmount *= 2;
                    damage *= 20;
                }
                
                if((System.currentTimeMillis() - chr.getPet(i).lastAttack) <= 1250 || (System.currentTimeMillis() - chr.getAutobanManager().lastAttack) >= 12000 || (System.currentTimeMillis() - chr.getAutobanManager().lastMove) >= 6000 || moInRange.isEmpty()){
                    moInRange = null;
                    return;
                    }
                if(chr.getPet(i).getCloseness() >= constants.ExpTable.getClosenessNeededForLevel(chr.getPet(i).getLevel())){
                    chr.announce(MaplePacketCreator.showOwnPetLevelUp((byte)i));
                    chr.getMap().broadcastMessage(chr, MaplePacketCreator.showPetLevelUp(chr, (byte)i), false);
                    chr.getPet(i).setLevel((byte)(chr.getPet(i).getLevel() + 1));
                } if(chance >= 70 && attackAmount == 1){
                    chr.getMap().broadcastMessage(MaplePacketCreator.petChat(chr.getId(), (byte)i, 1, monsterDialog[(int)(monsterDialog.length * Math.random())]));
                } if(attackAmount > 1){
                    chr.getMap().broadcastMessage(MaplePacketCreator.petChat(chr.getId(), (byte)i, 1, "M-U-L-T-I B-L-O-W! Take that!"));
                } for(int e = 0; e < attackAmount; e++){
                    if(moInRange.get(1) != null){
                server.life.MapleMonster locked_on = moInRange.get(1);
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.damageMonster(locked_on.getObjectId(), damage), true);
                locked_on.getMap().damageMonster(chr, locked_on, damage);
                chr.getPet(i).lastAttack = System.currentTimeMillis();
                    }
                }
                chr.getPet(i).gainCloseness(1);
                moInRange = null; // dispose
        }
    }
}
    
    
}
