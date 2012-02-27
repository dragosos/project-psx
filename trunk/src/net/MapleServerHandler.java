/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net;

import client.MapleClient;
import constants.ServerConstants;
import net.channel.ChannelServer;
import tools.MapleAESOFB;
import tools.MaplePacketCreator;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import tools.FileoutputUtil;
import java.io.IOException;

public class MapleServerHandler extends IoHandlerAdapter {
    private PacketProcessor processor;
    private int channel = -1;

    public MapleServerHandler(PacketProcessor processor) {
        this.processor = processor;
    }

    public MapleServerHandler(PacketProcessor processor, int channel) {
        this.processor = processor;
        this.channel = channel;
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        Runnable r = ((MaplePacket) message).getOnSend();
        if (r != null) {
            r.run();
        }
  //      System.out.println("SENT : " + message);
        super.messageSent(session, message);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        MapleClient c = ((MapleClient) session.getAttribute(MapleClient.CLIENT_KEY));
        if (c.getAccountName() != null) {
            if(cause instanceof IOException ){
                System.out.println(c.getAccountName() + " has closed the game.");
            } else {
            System.out.println(c.getAccountName() + " caught an exception: ");
            cause.printStackTrace();
            }
        }
        if(cause instanceof java.util.ConcurrentModificationException){
           FileoutputUtil.log("ConcurrencyErrors", "exception was caught.." + cause.getMessage() , null); 
        } else {
        FileoutputUtil.log(FileoutputUtil.exceptions, "exception was caught.." + cause, null);
        }
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        System.out.println("IoSession with " + session.getRemoteAddress() + " opened.");
        if (channel > -1) {
            if (ChannelServer.getInstance(channel).isShutdown()) {
                session.close(true);
                return;
            }
        }
        byte key[] = {0x13, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, (byte) 0xB4, 0x00, 0x00, 0x00, 0x1B, 0x00, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x33, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00};
        byte ivRecv[] = {70, 114, 122, 82};
        byte ivSend[] = {82, 48, 120, 115};
        ivRecv[3] = (byte) (Math.random() * 255);
        ivSend[3] = (byte) (Math.random() * 255);
        MapleAESOFB sendCypher = new MapleAESOFB(key, ivSend, (short) (0xFFFF - ServerConstants.VERSION));
        MapleAESOFB recvCypher = new MapleAESOFB(key, ivRecv, (short) ServerConstants.VERSION);
        MapleClient client = new MapleClient(sendCypher, recvCypher, session);
        client.setChannel(channel);
        session.write(MaplePacketCreator.getHello(ServerConstants.VERSION, ivSend, ivRecv));
        session.setAttribute(MapleClient.CLIENT_KEY, client);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        synchronized (session) {
            MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
            if (client != null) {
                client.disconnect();
                session.removeAttribute(MapleClient.CLIENT_KEY);
            }
        }
        super.sessionClosed(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        byte[] content = (byte[]) message;
        SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(content));
        short packetId = slea.readShort();
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        MaplePacketHandler packetHandler = processor.getHandler(packetId);
        if (packetHandler != null && packetHandler.validateState(client)) {
            try {
                try{
                packetHandler.handlePacket(slea, client);
                } finally {
                    // so nobody bugs up :3
                  //  if(packetId != RecvOpcode.MOVE_PLAYER.getValue() && packetId != RecvOpcode.MOVE_DRAGON.getValue() && packetId != RecvOpcode.MOVE_LIFE.getValue()&& packetId != RecvOpcode.MOVE_PET.getValue() && client.getPlayer() != null){
                  ///  client.announce(MaplePacketCreator.enableActions());
                   // }
                }
             //   System.out.println("used " + packetHandler.toString());
            } catch (Throwable t) {
            }
            
        } else {
            if(packetId != 0xDF){
         //  System.out.println("unhandled : " + slea); 
            }
        }
            if(packetId == RecvOpcode.CLIENT_ERROR.getValue()){
                System.out.println("Got client error.. " + slea);
                
            } else {
               // System.out.println("RECV : " + slea);
         //   }
    }
            
                    if(packetId != RecvOpcode.MOVE_PLAYER.getValue() && packetId != RecvOpcode.MOVE_DRAGON.getValue() && packetId != RecvOpcode.MOVE_LIFE.getValue()&& packetId != RecvOpcode.MOVE_PET.getValue() && client.getPlayer() != null){
                  //  System.out.println("mehh.." + slea);
                    }
    }

    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
         //   client.sendPing();
        }
        super.sessionIdle(session, status);
    }
}
