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
import client.MapleClient;
import server.TimerManager;
import server.movement.LifeMovementFragment;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import server.life.WhiteLady;
import server.clones.MaplePokemon;
import server.clones.MaplePokemonLibrary;

public final class MovePlayerHandler extends AbstractMovementPacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, final MapleClient c) {
        slea.skip(37);
        final List<LifeMovementFragment> res = parseMovement(slea);
        if (res != null) {
            if (c.getPlayer().isHidden()) {
                c.getPlayer().getMap().broadcastGMMessage(c.getPlayer(), MaplePacketCreator.movePlayer(c.getPlayer().getId(), res), false);
            } else {
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.movePlayer(c.getPlayer().getId(), res), false);
            } 
            updatePosition(res, c.getPlayer(), 0);
            c.getPlayer().getMap().movePlayer(c.getPlayer(), c.getPlayer().getPosition());
            c.getPlayer().getAchievementsProgress().addMetersWalked(1);
            c.getPlayer().getAutobanManager().lastMove = System.currentTimeMillis();
        /*    if(!c.getPlayer().isHidden()){
            TimerManager.getInstance().schedule(new Runnable(){
                @Override
                public void run(){
                    for(MaplePokemon p : c.getPlayer().getAllPokemon()){
                        p.move();
                        updatePosition(res, p, 0);
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.movePokemon(p.getObjectId(), p.getPosition(), res));
                    }
                }
                    }, 400);
            }*/
    }
    }
        
    public void initiateWhiteLady(int mid){
        WhiteLady wl = new WhiteLady(WhiteLady.getRS(), mid);
        wl.appear();
    }
    
    
    
}
