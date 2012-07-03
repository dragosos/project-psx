package net.login.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import client.MapleClient;
import constants.ServerConstants;
import java.util.logging.Level;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.login.LoginServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import server.authentication.RegisteredAuthenticates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharSelectedWithPicHandler extends AbstractMaplePacketHandler {

    private String channelServerIP;

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        System.out.println("Using pic");
        String pic = slea.readMapleAsciiString();
        int charId = slea.readInt();
        String macs = slea.readMapleAsciiString();
        c.updateMacs(macs);
        String mac = macs;
        if (macs.contains(", ")) {
            mac = mac.split(", ")[0];
        }
       /* if (c.gmLevel() > 0 && RegisteredAuthenticates.getByMac(mac, c).equals(RegisteredAuthenticates.UNAUTHORIZED)) {
            c.sendUnauthorizedLogin();
            return;
        }*/
        if (c.hasBannedMac()) {
            c.getSession().close(true);
            return;
        }
        if (ServerConstants.ENABLE_PIC && !c.checkPic(pic)) {
            c.announce(MaplePacketCreator.wrongPic());
        }
        try {
            if (c.getIdleTask() != null) {
                c.getIdleTask().cancel(true);
            }
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            String channelServerIP = MapleClient.getChannelServerIPFromSubnet(c.getSession().getRemoteAddress().toString().replace("/", "").split(":")[0], c.getChannel());
            if (channelServerIP.equals("0.0.0.0")) {
                String[] socket = LoginServer.getInstance().getIP(c.getChannel()).split(":");
                if (c.getWorld() != 1) {
                    c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
                } else {
                    c.announce(MaplePacketCreator.getTestServerIP(Integer.parseInt(socket[1]), charId));
                }
            } else {
                if (c.getWorld() != 1) {
                    c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(channelServerIP), Integer.parseInt(LoginServer.getInstance().getIP(c.getChannel()).split(":")[1]), charId));
                } else {
                    c.announce(MaplePacketCreator.getTestServerIP(Integer.parseInt(LoginServer.getInstance().getIP(c.getChannel()).split(":")[1]), charId));
                }
            }
        } catch (UnknownHostException e) {
            try {
                c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(channelServerIP), Integer.parseInt(LoginServer.getInstance().getIP(c.getChannel()).split(":")[1]), charId));
            } catch (UnknownHostException ex) {
                java.util.logging.Logger.getLogger(CharSelectedWithPicHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}