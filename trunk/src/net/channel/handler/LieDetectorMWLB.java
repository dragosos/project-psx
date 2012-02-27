/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.channel.handler;
import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;
import client.autoban.*;
import java.util.Map;
import client.MapleCharacter;
import client.MapleJob;
import net.channel.ChannelServer;
import tools.MaplePacketCreator;
import net.AbstractMaplePacketHandler;
/**
 *
 * @author FateJiki
 * @use This is the Maple Leaf Brigadier shiz.
 */
public final class LieDetectorMWLB extends AbstractMaplePacketHandler {
    
public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    String charname = slea.readMapleAsciiString();
    
    MapleCharacter suspect = ChannelServer.getCharacterFromAllServers(charname);
    if(suspect != null){
        Map<AutobanFactory, Integer> cheaterlog = suspect.getAutobanManager().points;
        if(!suspect.isGM() && !suspect.getJob().isA(MapleJob.MAPLELEAF_BRIGADIER)){
            if(cheaterlog.isEmpty()){
                c.getPlayer().dropMessage(1, "'" + charname + "' is not currently suspected of cheating. If you are so sure of his guilt, please go to a GM. If this is an emergency, please suspend his account and take a screenshot priot to doing so.");
                c.getPlayer().announce(MaplePacketCreator.enableActions());
                return;
            } else {
                c.getPlayer().suspectManager = suspect.getAutobanManager();
                c.getPlayer().openNpc(9010000, "LieDetector");
            }
        } else {
            c.getPlayer().dropMessage(1, "You cannot make a GM or another Maple Leaf Brigadier take the lie detector test.");
            c.getPlayer().announce(MaplePacketCreator.enableActions());
            return;
        }
    }
    }

    public boolean validateState(MapleClient c) {
        return true;
    }
}
