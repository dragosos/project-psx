/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;
import java.awt.Point;
import net.channel.ChannelServer;
import tools.MaplePacketCreator;
import server.maps.*;
/**
 *
 * @author FateJiki
 */
public class MapleDragon extends AbstractAnimatedMapleMapObject{
    private int ownerid;
    private int stance;
    private Point pos;
    private short ownerjobid;
    
    
    public MapleDragon(MapleCharacter owner){
        ownerid = owner.getId();
        stance = 0; // ?
        pos = owner.getPosition();
        ownerjobid = (short)owner.getJob().getId();
    }
    
    @Override
    public MapleMapObjectType getType(){
        return MapleMapObjectType.EVAN_DRAGON;
    }
    
    
    public int getOwnerId(){
        return ownerid;
    }
    
    
    public int getOwnerJobId(){
        return ownerjobid;
    }
    public MapleClient getClient(){
        return ChannelServer.getCharacterFromAllServers(ownerid).getClient();
    }
    
    @Override
    public void sendSpawnData(MapleClient c){
        c.announce(MaplePacketCreator.spawnDragon(this, c.getPlayer().getEvanDragon() == this));
    }

    public void setJobId(int delta){
        ownerjobid = (short)delta;
    }
    
    @Override
    public void sendDestroyData(MapleClient c){
        c.announce(MaplePacketCreator.removeDragon(getOwnerId()));
    }
}
