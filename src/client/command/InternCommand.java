package client.command;

import client.MapleCharacter;
import client.MapleClient;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import net.channel.ChannelServer;
import server.MapleShopFactory;
import server.maps.MapleMap;

public class InternCommand {

    public static boolean execute(MapleClient c, String[] splitted, char heading) {
        MapleCharacter player = new MapleCharacter();
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equalsIgnoreCase("glimmer")) {
            MapleShopFactory.getInstance().getShop(1338).sendShop(c);
        } else {
            if (c.getPlayer().gmLevel() == 2) {
                player.dropMessage("Intern Command " + heading + splitted[0] + " does not exist");
            }
            return false;
        }
        return true;
    }
}