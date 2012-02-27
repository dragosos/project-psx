/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.handler;
import client.MapleClient;
import net.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.FileoutputUtil;

/**
 *
 * @author FateJiki
 */
public class ClientErrorHandler implements MaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
     System.out.println("PACKET ERROR : " + slea);
    if(slea.available() >= 6){
        slea.skip(6);
        short badPacketSize = slea.readShort();
        // skipping cause i don't know what the rest means... slea.skip(2) is always equal to slea.skip(4) ..
        slea.skip(4); // skips all the way to the broken packet
        short badpacket = slea.readShort();
        slea.skip(badPacketSize);
        FileoutputUtil.log("packeterrors/" + badpacket + "_" + badPacketSize, "NFO :" + slea.toString(), null);
        } else {
            System.err.println("ERROR : Detected packet error but unable to handle!!");
        }
    }

    public boolean validateState(MapleClient c) {
        return true;
    }
}  