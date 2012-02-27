package net.channel.handler;

import client.MapleClient;
import client.IItem;
import net.AbstractMaplePacketHandler;
import scripting.item.ItemScriptManager;
import server.MapleItemInformationProvider;
import server.MapleItemInformationProvider.scriptedItem;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Jay Estrella
 */
public final class ScriptedItemHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        slea.readInt(); // trash stamp (thx rmzero)
        byte itemSlot = (byte) slea.readShort(); // item sl0t (thx rmzero)
        int itemId = slea.readInt(); // itemId
        IItem item = c.getPlayer().getInventory(ii.getInventoryType(itemId)).getItem(itemSlot);
        if (item == null || item.getItemId() != itemId || item.getQuantity() < 1) {
            System.out.println("Item is not scripted.." + itemId);
            return;
        }
        switch(itemId){
            case 2430112: // miracle cube fragments
                if(item.getQuantity() >= 10){
                    c.getPlayer().gainItem(2430112, (short)-10, false);
                    // removes 10 Miracle Cube fragments
                    c.getPlayer().gainItem(2049400, (short)1, false);
                    // gives one potential scroll
                } else {
                    c.getPlayer().dropMessage(5, "You don't have enough Miracle Cube Fragments. You need " + (10-item.getQuantity()) + " more..");
                }
                break;
            case 2430160: // strawberry icecream bar coupon
                c.getPlayer().gainItem(2430160, (short)-1, false);
                c.getPlayer().gainItem(2430160, (short)1, true);
                break;
            case 2430164: // halloween hat coupon
                c.getPlayer().gainItem(2430164, (short)-1, false);
                c.getPlayer().gainItem(1002699, (short)1, true);
                break;
            case 2430166: // explorer's cruely ring
                c.getPlayer().gainItem(2430166, (short)-1, false);
                c.getPlayer().gainItem(1112427, (short)1, true);
                break;
            case 2430168: // explorer's magical ring
                c.getPlayer().gainItem(2430168, (short)-1, false);
                c.getPlayer().gainItem(1112429, (short)1, true);
                break;
            case 2430098: // parallel equipment box
                c.getPlayer().dropMessage("Uncoded.");
                break;
            default:
                System.out.println("UNHANDLED SCRIPTED ITEM DETECTED!!" + itemId);
                c.getPlayer().dropMessage("This scripted item is not scripted. Please report it on the forums and don't forget to mention it's ID : " + itemId);
                break;
        }
        
        c.announce(MaplePacketCreator.enableActions());
    }
}
