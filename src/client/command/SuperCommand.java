package client.command;

import client.Equip;
import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.SkillFactory;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import net.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class SuperCommand {
    private static String splitt;
    private static int type;

    public static boolean execute(MapleClient c, String[] splitted, char heading) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equalsIgnoreCase("npc")) {
            MapleNPC npc = MapleLifeFactory.getNPC(Integer.parseInt(splitted[1]));
            if (npc != null && !npc.getName().equalsIgnoreCase("MISSINGNO")) {
                npc.setPosition(player.getPosition());
                npc.setCy(player.getPosition().y);
                npc.setRx0(player.getPosition().x + 50);
                npc.setRx1(player.getPosition().x - 50);
                npc.setFh(player.getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                npc.setCustom(true);
                player.getMap().addMapObject(npc);
                player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
            } else {
                player.dropMessage("You entered a invalid ID.");
            }
            
        } else if (splitted[0].equalsIgnoreCase("removenpcs")) {
            List<MapleMapObject> npcs = player.getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC));
            for (MapleMapObject npcmo : npcs) {
                MapleNPC npc = (MapleNPC) npcmo;
                if (npc.isCustom()) {
                    player.getMap().removeMapObject(npc.getObjectId());
                }
            }

          } else if (splitted[0].equalsIgnoreCase("spy")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    player.dropMessage("Players stats are:");
                    player.dropMessage("Level: " + victim.getLevel() + "  ||  Rebirthed: " + victim.getReborns());
                    player.dropMessage("Fame: " + victim.getFame());
                    player.dropMessage("Str: " + victim.getStr() + "  ||  Dex: " + victim.getDex() + "  ||  Int: " + victim.getInt() + "  ||  Luk: " + victim.getLuk());
                    player.dropMessage("Player has " + victim.getMeso() + " mesos.");
                    player.dropMessage("Hp: " + victim.getHp() + "/" + victim.getCurrentMaxHp() + "  ||  Mp: " + victim.getMp() + "/" + victim.getCurrentMaxMp());
                    player.dropMessage("NX Cash: " + victim.getCashShop().getCash(0));
                    player.dropMessage("GM Level: " + victim.gmLevel());
                    player.dropMessage("" + (victim.getGender() == 0 ? " Girlfriend: " : "Girlfriend: ") + "" + MapleCharacter.getNameById(victim.getRelationship()));
                } else {
                    player.dropMessage("Player not found.");
                }

        } else if (splitted[0].equalsIgnoreCase("unban")) {
            MapleCharacter.unban(splitted[1], false);
            MapleCharacter.unbanIP(splitted[1]);
            player.dropMessage("Unbanned " + splitted[1]);
            
        } else if (splitted[0].equalsIgnoreCase("zakum")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8800000), player.getPosition());
            for (int x = 8800003; x <= 8800010; x++) {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(x), player.getPosition());
            }

        } else if (splitted[0].equalsIgnoreCase("unbuffmap")) {
            for (MapleCharacter map : player.getMap().getCharacters()) {
                if (map != null && map != player) {
                    map.cancelAllBuffs();
                }
            }
            
        } else if (splitted[0].equalsIgnoreCase("buffmap")) {
            for (MapleCharacter map : player.getMap().getCharacters()) {
                if (map != null && map != player) {
                    int[] skills = new int[]{2001002, 1201007, 1301007, 2201001, 2321004, 2321004, 3121002, 3121000, 2311003, 1101006, 4101004, 4111001, 1111002, 2321005, 5101006, 1121002};
                    for (int i = 0; i < skills.length; i++) {
                        SkillFactory.getSkill(skills[i]).getEffect(SkillFactory.getSkill(skills[i]).getMaxLevel()).applyTo(map);
                    }
                }
            }
 
        } else if (splitted[0].equalsIgnoreCase("servermessage")) {
            for (int i = 1; i <= ChannelServer.getAllInstances().size(); i++) {
                ChannelServer.getInstance(i).setServerMessage(StringUtil.joinStringFrom(splitted, 1));
            }
            
        } else if (splitted[0].equalsIgnoreCase("warpallhere")) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                if (mch.getMapId() != player.getMapId()) {
                    mch.changeMap(player.getMap(), player.getPosition());
                }
            }           
        } else {
            if (c.getPlayer().gmLevel() == 4) {
                player.dropMessage("SuperGM Command " + heading + splitted[0] + " does not exist");
            }
            return false;
        }
        return true;
    }

}