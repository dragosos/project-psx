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
import client.IEquip.ScrollResult;
import server.MapleInventoryManipulator;
import client.Equip;
import client.IItem;
import client.MapleInventoryType;
import server.MapleItemInformationProvider;

/**
 *
 * @author FateJiki
 *   5F 00 [49 DC 69 0E] [0A 00] [F5 FF] [01] 00 00
 * //60 00 4A 43 86 0E 16 00 FF FF 00
 */
public final class UseUpgradeScrollHandler extends AbstractMaplePacketHandler {
    
public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    slea.readInt(); // timestamp
    byte scrollUsed = (byte)slea.readShort();
    byte equipScrolled = (byte)slea.readShort();
    

    
    IItem item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(scrollUsed);
    int scrollid = item.getItemId();
    Equip eq = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(equipScrolled);
    if(item == null || eq == null){
        return;
    }
    byte oldLevel = eq.getLevel();
    c.getPlayer().dropMessage("miraculous shiyt " + slea);
    c.getPlayer().dropMessage("equipScrolled = " + equipScrolled);
    boolean isChaos = item.getItemId() == 2049116;
    if(isChaos){
        Equip scrolled = (Equip) MapleItemInformationProvider.getInstance().scrollEquipWithId(eq, item.getItemId(), false, c.getPlayer().isGM());
        System.out.println("lawl");
        ScrollResult result = ScrollResult.FAIL;
        if(scrolled.getLevel() == oldLevel + 1){
            result = ScrollResult.SUCCESS;
            c.getPlayer().equipChanged();
        }
        c.announce(MaplePacketCreator.scrolledItem(item, scrolled, false, false));
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getScrollEffect(c.getPlayer().getId(), result, false));
        c.announce(MaplePacketCreator.updateEquipSlot(scrolled));
        c.getPlayer().gainItem(scrollid, (short)-1, false);
    }
    
    
    }

    public boolean validateState(MapleClient c) {
        return true;
    }
}