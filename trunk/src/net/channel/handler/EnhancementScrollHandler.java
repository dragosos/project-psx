/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.channel.handler;
import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;
import client.autoban.*;
import java.util.EnumMap;
import client.MapleCharacter;
import client.MapleJob;
import net.channel.ChannelServer;
import tools.MaplePacketCreator;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import client.Equip;
import client.IItem;
import client.MapleInventoryType;
import server.MapleItemInformationProvider;

/**
 *
 * @author FateJiki
 * 5F 00 49 DC 69 0E 0A 00 F5 FF 01 00 00
 * //60 00 4A 43 86 0E 16 00 FF FF 00
 */
public final class EnhancementScrollHandler extends AbstractMaplePacketHandler {
    
public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    slea.readInt(); // timestamp
    short slot = slea.readShort();
    slea.skip(3); // unknown
    Equip eq = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte)slot);
    c.getPlayer().dropMessage(1, "Disabled until I figure out how this works. :)");
    c.announce(MaplePacketCreator.enableActions());
    return;
    }

    public boolean validateState(MapleClient c) {
        return true;
    }
}