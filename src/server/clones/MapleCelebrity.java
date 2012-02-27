/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.clones;
import client.ItemFactory;
import client.MapleCharacter;
import client.MapleInventoryType;
import net.channel.ChannelServer;
import server.MapleInventoryManipulator;

/**
 *
 * @author FateJiki
 */
public class MapleCelebrity extends MapleCharacter{
    final int id;
    final byte skinColor;
    final int hairid;
    final int faceid;
    final byte gender; // 0 = male; 1 = female
    final int celeb_id;
    final int walkSpeed;
    final int overallid; // pos : -5 when equips
    final int weaponid;// pos : -11 when equip
    
    private MapleCelebrity(MapleCelebrityLibrary mcl, int cid){
        super();
        MapleCharacter celeb = ChannelServer.getCharacterFromAllServers(cid);
        celeb_id = cid;
        id = mcl.id;
        skinColor = mcl.skinColor;
        hairid = mcl.hairid;
        faceid = mcl.faceid;
        gender = mcl.gender;
        walkSpeed = mcl.walkSpeed;
        overallid = mcl.overallid;
        weaponid = mcl.weaponid;
        //MapleInventoryManipulator.equip(this, (byte)weaponid, getInventory(MapleInventoryType.EQUIPPED).getItem((byte)-11), true);
    }
    
}
