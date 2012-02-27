/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.channel.handler;
import client.MapleClient;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import java.awt.Point;
import server.movement.LifeMovementFragment;
import java.util.List;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleDragon;

/**
 *
 * @author FateJiki
 */
public class MoveDragonHandler extends AbstractMovementPacketHandler {
    
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        List<LifeMovementFragment> res = null;
        if(c.getPlayer() == null || c.getPlayer().getEvanDragon() == null){
            return;
        }
        MapleDragon dragon = c.getPlayer().getEvanDragon();
        
        Point startingPos = slea.readPos();
        slea.skip(4);
        res = parseMovement(slea);
        
        if(res != null){
            updatePosition(res, dragon, -1);
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.moveDragon(dragon.getOwnerId(), startingPos, res));
            c.getPlayer().getMap().moveDragon(dragon, dragon.getPosition());
        }
    }
}
