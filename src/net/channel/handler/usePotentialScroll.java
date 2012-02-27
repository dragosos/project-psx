/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.channel.handler;
import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleInventory;
import tools.MaplePacketCreator;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import client.Equip;
import client.IEquip.ScrollResult;
import client.IItem;
import client.MapleInventoryType;
import server.MapleItemInformationProvider;

/**
 *
 * @author FateJiki
 * 5F 00 49 DC 69 0E 0A 00 F5 FF 01 00 00
 * //60 00 4A 43 86 0E 16 00 FF FF 00
 * // 61 00 F3 A8 C1 2C 01 00 F5 FF 00
 */
public final class usePotentialScroll extends AbstractMaplePacketHandler {
    private static short itemOption1;
    private static short itemOption2;
    private static short itemOption3 = 0;
    private static byte itemStat;
public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    slea.readInt(); // timestamp
        byte slot = (byte) slea.readShort();
        byte applyto = (byte) slea.readShort();
        boolean Equipment = false;
        MapleInventory invItem = c.getPlayer().getInventory(MapleInventoryType.USE);
        IItem pot_scroll = invItem.getItem(slot);
        final Equip myEquip = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(applyto);
        final Equip myEquipped = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(applyto);
        
        ScrollResult rs = ScrollResult.SUCCESS;
        if (pot_scroll.getQuantity() < 1 || pot_scroll.getItemId() != 2049400 && pot_scroll.getItemId() != 2049401) {
            c.getPlayer().ban("Packet editing : Using an item that isn't a potential scroll", true);
            return;
        }

        int itemPercentPlus[] = {/*+*/10001,10002,10003,10004,10005,10006,10007,10008,10009,10010,10011,10012,10013,10014,
                                 /*3%*/10041,10042,10043,10044,10045,10046,10047,10048,10051,10052,10053,10054,10055,10070,10081,
                                 /*6%*/20041,20042,20043,20044,20045,20046,20047,20048,20051,20052,20053,20054,20055,20070,20086,
                                 /*9%*/30041,30042,30043,30044,30045,30046,30047,30048,30051,30052,30053,30054,30055,30070,30086};
        int randStat1 = (int)(100.0 * Math.random());
        int randStat2 = (int)(200.0 * Math.random());
        if(Math.ceil(Math.random() * 100) < 15){ //Rare = 70 , Unique = 20 , Epic = 10
            itemOption1 = (short) itemPercentPlus[(int)(58 * Math.random() +1)];
            if(randStat1 > 60){
            itemOption2 = (short) itemPercentPlus[(int)(58 * Math.random()+1)];
            }
            if(Math.ceil(Math.random() * 100) <= 25 && randStat2 > 50){
                itemOption3 = (short) itemPercentPlus[(int)(42 * Math.random()+1)];
            }
            itemStat = 7;
        } else if (Math.ceil(Math.random() * 100) < 25){
            itemOption1 = (short) itemPercentPlus[(int)(42 * Math.random()+1)];
            if(randStat1 > 60){
            itemOption2 = (short) itemPercentPlus[(int)(42 * Math.random()+1)];
            }
            if(Math.ceil(Math.random() * 100) <= 15 && randStat2 > 50){
                itemOption3 = (short) itemPercentPlus[(int)(28 * Math.random()+1)];
            }
            itemStat = 6;
        } else {
            itemOption1 = (short) itemPercentPlus[(int)(28 * Math.random()+1)];
            if(randStat1 > 60){
            itemOption2 = (short) itemPercentPlus[(int)(28 * Math.random()+1)];
            }
            if(Math.ceil(Math.random() * 100) <= 15 && randStat2 > 50){
                itemOption3 = (short) itemPercentPlus[(int)(14 * Math.random()+1)];
            }
            itemStat = 5;
        }
        if (applyto >= 0) {
            myEquip.potential = itemStat;
            myEquip.potential1 = itemOption1;
            myEquip.potential2 = itemOption2;
            myEquip.potential3 = itemOption3;
            Equipment = true;
        } else {
            myEquipped.potential = itemStat;
            myEquipped.potential1 = itemOption1;
            myEquipped.potential2 = itemOption2;
            myEquipped.potential3 = itemOption3;
        }
        
         
         c.getSession().write(MaplePacketCreator.updateEquipSlot(Equipment ? myEquip : myEquipped));
         c.getPlayer().announce(MaplePacketCreator.getScrollEffect(c.getPlayer().getId(), rs, false));
         c.getPlayer().gainItem(pot_scroll.getItemId(), (short)-1, false);
         c.getPlayer().equipChanged();
    }

    public boolean validateState(MapleClient c) {
        return true;
    }
}