package net.login.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import client.MapleClient;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.login.LoginServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Randomizer;

public class ViewAllCharSelectedWithPicHandler extends AbstractMaplePacketHandler {
    private static Logger log = LoggerFactory.getLogger(ViewAllCharSelectedWithPicHandler.class);
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

    String pic = slea.readMapleAsciiString();
    int charId = slea.readInt();
    slea.readInt();//world
                int channel = Randomizer.rand(0, ServerConstants.CHANNEL_NUMBER - 1);
		String macs = slea.readMapleAsciiString();
		c.updateMacs(macs);

		if (c.hasBannedMac()) {
                    c.getSession().close(true);
                    return;
		}
		if (c.checkPic(pic)) {
                    try {
			if (c.getIdleTask() != null) {
				c.getIdleTask().cancel(true);
			}
			c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);

			String channelServerIP = MapleClient.getChannelServerIPFromSubnet(c.getSession().getRemoteAddress().toString().replace("/", "").split(":")[0], channel);
			if(channelServerIP.equals("0.0.0.0")) {
				String[] socket = LoginServer.getInstance().getIP(channel).split(":");

				c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
			} else {
				String[] socket = LoginServer.getInstance().getIP(channel).split(":");
				c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(channelServerIP), Integer.parseInt(socket[1]), charId));
			}
                    } catch (UnknownHostException e) {
			log.error("Host not found", e);
                    }

                } else {

            c.announce(MaplePacketCreator.wrongPic());
            }
        }

}
