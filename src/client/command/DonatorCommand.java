package client.command;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import net.channel.ChannelServer;
import scripting.npc.NPCScriptManager;
import tools.MaplePacketCreator;
import tools.StringUtil;

/**
 * @author Moogra
 */
public class DonatorCommand {

    public static boolean execute(MapleClient c, String[] splitted, char heading) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equals("buffme")) {
            final int[] array = {9001000, 9101002, 9101003, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
            for (int i : array) {
                SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(player);
                
            }

        } else if (splitted[0].equalsIgnoreCase("dcommands")) {
            player.dropMessage("===================================================");
            player.dropMessage("             RoyalMS Donator Commands              ");
            player.dropMessage("===================================================");
            player.dropMessage("!buffme              - Sends an array of buffs your way!");
            player.dropMessage("!shout [Message]     - Sends a message to the whole server.");
            player.dropMessage("!goto [map]          - Warps you to any map. (More significant then @go)");
            player.dropMessage("!online              - Checks the players' names in your channel.");

        } else if (splitted[0].equalsIgnoreCase("shout")) {
            if ((System.currentTimeMillis() / 60000) - player.getLastShoutTime() >= 1) {
                String type = "[Donator]" + player.getName() + ": ";
                try {
                    ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, type + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                    player.setLastShoutTime();
                } catch (RemoteException e) {
                    cserv.reconnectWorld();
                }
            } else {
                player.dropMessage("You can only use this every 60 seconds. ");
            }
                        
         } else if (splitted[0].equalsIgnoreCase("goto")) {
            HashMap<String, Integer> maps = new HashMap<String, Integer>();
            maps.put("southperry", 60000);
            maps.put("amherst", 1010000);
            maps.put("henesys", 100000000);
            maps.put("ellinia", 101000000);
            maps.put("perion", 102000000);
            maps.put("kerning", 103000000);
            maps.put("lith", 104000000);
            maps.put("sleepywood", 105040300);
            maps.put("florina", 110000000);
            maps.put("gmmap", 180000000);
            maps.put("orbis", 200000000);
            maps.put("happy", 209000000);
            maps.put("elnath", 211000000);
            maps.put("ludi", 220000000);
            maps.put("omega", 221000000);
            maps.put("korean", 222000000);
            maps.put("aqua", 230000000);
            maps.put("leafre", 240000000);
            maps.put("mulung", 250000000);
            maps.put("herb", 251000000);
            maps.put("nlc", 600000000);
            maps.put("shrine", 800000000);
            maps.put("showa", 801000000);
            maps.put("fm", 910000000);
            if (maps.containsKey(splitted[1])) {
                player.changeMap(cserv.getMapFactory().getMap(maps.get(splitted[1])), cserv.getMapFactory().getMap(maps.get(splitted[1])).getPortal(0));
            }
            
        } else if (splitted[0].equalsIgnoreCase("online")) {
            StringBuilder builder = new StringBuilder("Characters online: ");
            for (MapleCharacter chr : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                builder.append(MapleCharacter.makeMapleReadable(chr.getName())).append(", ");
            }
            builder.setLength(builder.length() - 2);
            player.dropMessage(builder.toString());
        } else {
            if (c.getPlayer().gmLevel() == 1) {
                player.dropMessage("Donator Command " + heading + splitted[0] + " does not exist");
            }
            return false;
        }
        return true;
    }
}
