package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.MaplePacket;
import net.SendOpcode;
import server.maps.MapleSummon;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author Expedia
 */
public class RemoveMechanicSummonHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int objectid = slea.readInt();
        for (MapleSummon summon : c.getPlayer().getSummons().values()) {
            if (summon.getObjectId() == objectid) {
                c.getPlayer().getMap().broadcastMessage(removeMechanicSummon(summon));
                break;
            }
        }
    }

    public static MaplePacket removeMechanicSummon(MapleSummon summon) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.REMOVE_SPECIAL_MAPOBJECT.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId());
        if (summon.isStationary()) {
            mplew.write(5);
        } else {
            mplew.write(10);
        }
        return mplew.getPacket();
    }
}