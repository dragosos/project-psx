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
import client.IItem;
import client.MapleInventoryType;
import server.MapleItemInformationProvider;
/**
 *
 * @author FateJiki
 */
public final class MagnifyingGlassHandler extends AbstractMaplePacketHandler {
	    private static short itemOption1;
    private static short itemOption2;
    private static short itemOption3 = 0;
    private static byte itemStat;
public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt(); // Timestamp
        byte slot = (byte) slea.readShort();
        byte applyto = (byte) slea.readShort();
        boolean Equipment = false;
        MapleInventory invItem = c.getPlayer().getInventory(MapleInventoryType.USE);
        IItem magnifyGlass = invItem.getItem(slot);
        final Equip myEquip = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(applyto);
        final Equip myEquipped = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(applyto);
        int itemid = magnifyGlass.getItemId();
        if (magnifyGlass.getQuantity() < 1) {
            return;
        } if(itemid != 2460000 && itemid != 2460001 && itemid != 2460002 && itemid != 2460003){
            c.getPlayer().ban("Packet editing a magnifying glass..", true);
            return;
        } if(myEquipped.getType() != 1){
            c.getPlayer().ban("Packet editing : Not using an equip to magnify glass.", true);
            return;
        } if(myEquipped.isPotentialByte() < (byte)1){
            c.getPlayer().ban("Packet editing : Trying to use a magnifying glass on item that has no potential.", true);
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
        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, magnifyGlass.getItemId(), 1, true, false);
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showMagnifyingEffect(c.getPlayer().getId(), applyto));
         c.getSession().write(MaplePacketCreator.updateEquipSlot(Equipment ? myEquip : myEquipped));
       //c.getPlayer().equipChanged();
    }

private short generatePotentialStat(byte index){
    short stat = 0;
    boolean secondStat = (int)(100.0 * Math.random()) >= 65;
    boolean thirdStat = (int)(100.0 * Math.random()) >= 85;
    if(index == (byte)1){
        stat = (short)MapleItemInformationProvider.getInstance().getRandomPotentialStat();
        return stat;
    } else {
        if(index > (byte)1){
        if(secondStat && index == (byte)2 || thirdStat && index == (byte)3){
           return stat = (short)MapleItemInformationProvider.getInstance().getRandomPotentialStat();
        } else {
            return 0;
        }
    }
    }
    return stat;
}

private byte generateRank(Equip eq){
    byte rank = 4;
    if(eq.potential1 != 0){
        rank++; // rare
    } if(eq.potential2 != 0){
        rank++; // epic
    } if(eq.potential3 != 0){
        rank++; //unique
    }
    
    return rank;
}


    public boolean validateState(MapleClient c) {
        return true;
    }
}
