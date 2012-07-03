package client.command;

import client.MapleCharacter;
import client.MapleCharacter.FameStatus;
import client.MapleClient;
import client.MapleStat;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import constants.ServerConstants;
import java.net.InetAddress;
import java.rmi.RemoteException;
import net.MaplePacket;
import net.channel.ChannelServer;
import net.world.remote.WorldChannelInterface;
import scripting.npc.NPCScriptManager;
import server.MaplePortal;
import server.MapleTrade;
import server.maps.MapleMap;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class PlayerCommand {

    private static ResultSet rbRanking(boolean gm) {
        try {
            java.sql.Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (!gm) {
                ps = (PreparedStatement) con.prepareStatement("SELECT reborns, level, name, job FROM characters WHERE gm < 3 ORDER BY reborns desc LIMIT 10");
            } else {
                ps = (PreparedStatement) con.prepareStatement("SELECT name, gm FROM characters WHERE gm >= 3");
            }
            return ps.executeQuery();
        } catch (SQLException ex) {
            return null;
        }
    }

    private static ResultSet pvpRanking(boolean gm) {
        try {
            java.sql.Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (!gm) {
                ps = (PreparedStatement) con.prepareStatement("SELECT pvpkills, level, name, job FROM characters WHERE gm < 3 ORDER BY pvpkills desc LIMIT 10");
            } else {
                ps = (PreparedStatement) con.prepareStatement("SELECT name, gm FROM characters WHERE gm >= 3");
            }
            return ps.executeQuery();
        } catch (SQLException ex) {
            return null;
        }
    }
    private static int relationshipprompter = 0;
    public static int promptrelationship;

    public static void execute(MapleClient c, String[] splitted, char heading) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equalsIgnoreCase("server")) {
            player.dropMessage("" + ServerConstants.SERVERNAME + " Beta");
        } else if (splitted[0].equalsIgnoreCase("commands") || splitted[0].equalsIgnoreCase("help")) {
            player.dropMessage("Try @commands1 or @commands2");
        } else if (splitted[0].equalsIgnoreCase("commands1")) {
            player.dropMessage("===================================================");
            player.dropMessage("            " + ServerConstants.SERVERNAME + " Player Commands Page 1         ");
            player.dropMessage("===================================================");
            //player.dropMessage("@ranking             - Shows the Top 10 Players of the server.");
            player.dropMessage("@pvpranking          - Shows the Top 10 Users in PvP.");
            player.dropMessage("@sendmail            - Sends a letter to a user. (Perfect for offline Messaging!)");
            player.dropMessage("@checknewmail        - Checks your new mail that you havn't read before.");
            player.dropMessage("@checkallmail        - See your mail history/old mail.");
            // player.dropMessage("@presinfo            - Read the information of becoming president.");
            // player.dropMessage("@president           - Makes you your channel's president if you have 15 Reborns and 5,000 Leaves.");
            player.dropMessage("@gm                  - Sends a message to the server that you need a GM to help you. (Spam  =  Jail/Ban)");
            player.dropMessage("@dispose             - Unsticks you and allows you to talk to npcs again.");
            player.dropMessage("@expfix              - Sets your exp to 0.0%. Fixes negative exp.");
            player.dropMessage("@emo                 - Cuts your throat for you!");
            player.dropMessage("@checkstats          - Lets you view your current information.");
            player.dropMessage("@kin                 - Opens Male Stylist.");
            player.dropMessage("@nimakin             - Opens Female Stylist.");
            player.dropMessage("@cody                - Opens up Job Advancer Cody.");
            player.dropMessage("@maxskills           - Opens up Skill Maxer Duey.");
            player.dropMessage("@go [map]            - Warps you to practically every town/popular map.");
            player.dropMessage("@maps                - Shows you all the maps you can use with @go.");

        } else if (splitted[0].equalsIgnoreCase("commands2")) {
            player.dropMessage("===================================================");
            player.dropMessage("            " + ServerConstants.SERVERNAME + " Player Commands Page 2         ");
            player.dropMessage("===================================================");
            player.dropMessage("@fmnpc               - Opens up Shanks, the universal NPC.");
            player.dropMessage("@joinevent           - Joins any event that a GM opens up.");
            player.dropMessage("@hideout             - Warps you to the guild hideout. (If you have one)");
            if (player.getGuildRank() == 1) {
                player.dropMessage("@sethideout             - Sets your Guild's hideout to the map you are on.");
            }
            player.dropMessage("@save                - Saves your character to Database.");
            player.dropMessage("@str                 - Lets you manually raise your Strength.");
            player.dropMessage("@dex                 - Lets you manually raise your Dexterity.");
            player.dropMessage("@int                 - Lets you manually raise your Intelligence.");
            player.dropMessage("@luk                 - Lets you manually raise your Luck.");
            player.dropMessage("@connected           - Lets you see the Number of Users online.");

        } else if (splitted[0].equalsIgnoreCase("ranking")) {
            try {
                player.dropMessage("Top 10 players: ( Rebirths ): ");
                ResultSet rs = rbRanking(false);
                int i = 1;
                while (rs.next()) {
                    player.dropMessage(i + ". " + rs.getString("name") + ": Rebirths: " + rs.getInt("reborns"));
                    i++;
                }
            } catch (SQLException ex) {
                Logger.getLogger(PlayerCommand.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (splitted[0].equalsIgnoreCase("checkallmail")) {
            c.getPlayer().setSeenAllMail();
            ResultSet rs = getAllMail(c.getPlayer().getName());
            try {
                player.dropMessage("Your MailBox:");
                while (rs.next()) {
                    player.dropMessage(rs.getString("MailSender") + ": " + rs.getString("Message"));
                }
            } catch (Exception ex) {
            }

        } else if (splitted[0].equalsIgnoreCase("checknewmail")) {
            if (c.getPlayer().newMail() > 0) {
                ResultSet rs = getNewMail(c.getPlayer().getName());
                try {
                    player.dropMessage("Your New Messages:");
                    while (rs.next()) {
                        player.dropMessage(rs.getString("MailSender") + ": " + rs.getString("Message"));
                    }
                } catch (Exception ex) {
                }
                c.getPlayer().setSeenAllMail();
            } else {
                player.dropMessage("You have no new messages. Use @checkallmail to see your entire inbox.");
            }
        } else if (splitted[0].equalsIgnoreCase("sendmail")) {
            if (splitted.length <= 2) {
                player.dropMessage("Please use @sendmail <Mail Reciever> <Message>.");
            } else {
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                String Reciever = splitted[1];
                String message = StringUtil.joinStringFrom(splitted, 2);
                try {
                    c.getPlayer().sendMail(Reciever, message);
                } catch (SQLException ex) {
                    Logger.getLogger(PlayerCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
                player.dropMessage("You have sent " + Reciever + " this message: " + message);
                victim.dropMessage("[Notice]: " + c.getPlayer().getName() + " has just sent you a message! Use @checknewmail to see what it was.");
            }

        } else if (splitted[0].equalsIgnoreCase("accept")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(relationshipprompter);
            victim.setRelationship(player.getId());
            player.setRelationship(relationshipprompter);
            player.dropMessage("Done! " + victim.getName() + " is now your soul mate!");
            victim.dropMessage(player.getName() + " has accepted your request and is now your soul mate!");
            relationshipprompter = 0;

        } else if (splitted[0].equalsIgnoreCase("breakup")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(player.getRelationship());
            if (victim != null) {
                victim.dropMessage(player.getName() + " has broken up with you.");
                player.dropMessage("You have now broke up with " + MapleCharacter.getNameById(player.getRelationship()));
                player.setRelationship(0);
                victim.setRelationship(0);
            } else {
                player.dropMessage("The player is offline, use @leaverelationship to leave without notifying your partner.");
            }

        } else if (splitted[0].equalsIgnoreCase("leaverelationship")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(player.getRelationship());
            if (player.getRelationship() > 0) {
                try {
                    victim.sendNote(player.getRelationship(), player.getName() + " has left the relationship. You have not left your relationship yet since you were secretly told this by me. To leave your relationship use @leaverelationship");
                } catch (SQLException ex) {
                    Logger.getLogger(PlayerCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
                player.dropMessage("You have now left.");
                player.setRelationship(0);
            } else {
                player.dropMessage("You can't leave a relationship if you are not in one.");
            }

        } else if (splitted[0].equalsIgnoreCase("spousechat")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(player.getRelationship());
            if (victim == null) {
                if (player.getRelationship() < 1) {
                    player.dropMessage("You are not in a relationship.");
                } else {
                    player.dropMessage("Your partner is not online!");
                }
            } else {
                victim.dropMessage("[Spouse]" + StringUtil.joinStringFrom(splitted, 1));
                player.dropMessage("[Spouse]" + StringUtil.joinStringFrom(splitted, 1));
            }

        } else if (splitted[0].equalsIgnoreCase("getrelationship")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim == null || victim.getRelationship() == 0) {
                player.dropMessage("Syntax: -charname- or the character you are searhing for is not online. Or the character is not in a relationship.");
            } else {
                player.dropMessage(victim.getName() + "'s relationship partner is " + MapleCharacter.getNameById(victim.getRelationship()));
            }

        } else if (splitted[0].equalsIgnoreCase("decline")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterById(relationshipprompter);
            player.dropMessage("Done.");
            relationshipprompter = 0;
            victim.dropMessage(player.getName() + " has denied your request. Please try again later.");

        } else if (splitted[0].equalsIgnoreCase("askout")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if ((System.currentTimeMillis() / 60000) - player.getLastAskOutTime() >= 10) {
                if (victim != null) {
                    if (victim.getRelationship() == 0 || player.getRelationship() == 0) {
                        if (relationshipprompter == 0) {
                            if (splitted[1] != null) {
                                promptrelationship = 1;
                                relationshipprompter = player.getId();
                                victim.dropMessage(player.getName() + " wants to be your boyfriend/girlfriend. Use @accept to accept the request, use @decline to decline the request.");
                                player.dropMessage("Sent, the request is pending...");
                                player.setLastAskOutTime();
                            } else {
                                player.dropMessage("Syntax: relationship -Charname-");
                            }
                        } else {
                            player.dropMessage("A relationship request is already pending. Just use @decline to stop the request.");
                        }
                    } else {
                        player.dropMessage("The character is already in a relationship or you are already in a relationship!");
                    }
                } else {
                    player.dropMessage("Syntax: relationship -Charname- or the Character is offline.");
                }
            } else {
                player.dropMessage("You can only send a request every 10 minutes.");
            }

        } else if (splitted[0].equalsIgnoreCase("pvpranking")) {
            try {
                player.dropMessage("Top 10 PvP Players: ");
                ResultSet rs = pvpRanking(false);
                int i = 1;
                while (rs.next()) {
                    player.dropMessage(i + ". " + rs.getString("name") + ": PVP Kills: " + rs.getInt("pvpkills"));
                    i++;
                }
            } catch (SQLException ex) {
                Logger.getLogger(PlayerCommand.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (splitted[0].equalsIgnoreCase("gm")) {
            if ((System.currentTimeMillis() / 60000) - player.getLastGmCommandTime() >= 5) {
                for (MapleCharacter gms : player.getClient().getChannelServer().getPlayerStorage().getAllCharacters()) {
                    if (gms.gmLevel() > 0) {
                        gms.dropMessage("[Gm-Help] " + player.getName() + " asks: " + StringUtil.joinStringFrom(splitted, 1));
                    }
                }
                player.dropMessage(" Message sent. ");
            } else {
                player.dropMessage("You have to wait 5 minutes inbetween.");
            }

        } else if (splitted[0].equalsIgnoreCase("dispose") || splitted[0].equalsIgnoreCase("refresh")) {
            NPCScriptManager.getInstance().dispose(c);
            player.dropMessage("Unstucked.");
            c.getSession().write(MaplePacketCreator.enableActions());

        } else if (splitted[0].equalsIgnoreCase("emo") || splitted[0].equalsIgnoreCase("die")) {
            player.setHp(0);
            player.updateSingleStat(MapleStat.HP, 0);

        } else if (splitted[0].equalsIgnoreCase("expfix")) {
            player.setExp(0);
            player.updateSingleStat(MapleStat.EXP, Integer.valueOf(0));
            player.dropMessage("Your exp has been reset.");

        } else if (splitted[0].equalsIgnoreCase("cody")) {
           player.openNpc(9200000);

        } else if (splitted[0].equalsIgnoreCase("kin") || splitted[0].equalsIgnoreCase("nimakin") || splitted[0].equalsIgnoreCase("stylist")) {
            player.openNpc(9900000);

        } else if (splitted[0].equalsIgnoreCase("checkstats") || splitted[0].equalsIgnoreCase("checkme")) {
            player.dropMessage("Your stats are:");
            player.dropMessage("Str: " + player.getStr());
            player.dropMessage("Dex: " + player.getDex());
            player.dropMessage("Int: " + player.getInt());
            player.dropMessage("Luk: " + player.getLuk());
            player.dropMessage("Available AP: " + player.getRemainingAp());
            player.dropMessage("Rebirths: " + player.getReborns());
            player.dropMessage("PvP Kills: " + player.getPvpKills() + " PvP Deaths: " + player.getPvpDeaths());

        } else if (splitted[0].equalsIgnoreCase("goto") || splitted[0].equalsIgnoreCase("go") || splitted[0].equalsIgnoreCase("maps")) {
            HashMap<String, Integer> maps = new HashMap<String, Integer>();
            maps.put("henesys", 100000000);
            maps.put("ellinia", 101000000);
            maps.put("perion", 102000000);
            maps.put("kerning", 103000000);
            maps.put("lith", 104000000);
            maps.put("sleepywood", 105040300);
            maps.put("florina", 110000000);
            maps.put("orbis", 200000000);
            maps.put("happy", 209000000);
            maps.put("elnath", 211000000);
            maps.put("ereve", 130000000);
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
            maps.put("guild", 200000301);
            maps.put("fog", 105040306);
            if (splitted.length != 2) {
                StringBuilder builder = new StringBuilder("Syntax: @goto <mapname>");
                int i = 0;
                for (String mapss : maps.keySet()) {
                    if (1 % 10 == 0) {// 10 maps per line
                        player.dropMessage(builder.toString());
                    } else {
                        builder.append(mapss + ", ");
                    }
                }
                player.dropMessage(builder.toString());
            } else if (maps.containsKey(splitted[1])) {
                int map = maps.get(splitted[1]);
                if (map == 910000000) {
                    player.saveLocation("FREE_MARKET");
                }
                player.changeMap(map);
            } else {
                player.dropMessage("========================================================================");
                player.dropMessage("                       ...::: @go maps :::...                           ");
                player.dropMessage("========================================================================");
                player.dropMessage("| henesys | ellinia | perion | kerning | lith   | sleepywood | florina |");
                player.dropMessage("| orbis   | happy  | elnath  | ereve  | ludi       | omega   |");
                player.dropMessage("| korean  | aqua    | leafre | mulung  | herb   | nlc        | shrine  |");
                player.dropMessage("| showa  | fm      | guild  |");
            }
            maps.clear();

        } else if (splitted[0].equalsIgnoreCase("joinevent")) {
            if (c.getPlayer().getClient().getChannelServer().eventOn == false) {
                player.dropMessage("Sorry, there is no current event going on.");
            } else {
                MapleMap newMap = cserv.getMapFactory().getMap(c.getPlayer().getClient().getChannelServer().eventMap);
                MaplePortal newPortal = newMap.getPortal(0);
                player.changeMap(newMap, newPortal);
                player.dropMessage("You have been warped to the event!");
            }

        } else if (splitted[0].equalsIgnoreCase("rebirth") || splitted[0].equalsIgnoreCase("reborn")) {
            String messages[] = {" ..:: Rebirth Commands ::.. ", " @rebirthexp - Reborns you as a Explorer.", " @rebirthcyg - Reborns you as a Knight Of Cygnus.", " @rebirtharan - Reborns you as a Aran."};
            for (int i = 0; i < messages.length; i++) {
                player.dropMessage(messages[i]);
            }

        } else if (splitted[0].equalsIgnoreCase("rebirthexp")) {
            player.doReborn("Explorer");
        } else if (splitted[0].equalsIgnoreCase("rebirthcyg")) {
            player.doReborn("Cygnus");
        } else if (splitted[0].equalsIgnoreCase("rebirtharan")) {
            player.doReborn("Aran");

        } else if (splitted[0].equalsIgnoreCase("save")) {
            player.saveToDB(true);
            player.dropMessage("Saved.");

        } else if (splitted[0].equalsIgnoreCase("str") || splitted[0].equalsIgnoreCase("int") || splitted[0].equalsIgnoreCase("luk") || splitted[0].equalsIgnoreCase("dex")) {
            int amount = Integer.parseInt(splitted[1]);
            if (amount > 0 && amount <= player.getRemainingAp() && amount < 31997) {
                if (splitted[0].equalsIgnoreCase("str") && amount + player.getStr() < 32001) {
                    player.setStr(player.getStr() + amount);
                    player.updateSingleStat(MapleStat.STR, player.getStr());
                } else if (splitted[0].equalsIgnoreCase("int") && amount + player.getInt() < 32001) {
                    player.setInt(player.getInt() + amount);
                    player.updateSingleStat(MapleStat.INT, player.getInt());
                } else if (splitted[0].equalsIgnoreCase("luk") && amount + player.getLuk() < 32001) {
                    player.setLuk(player.getLuk() + amount);
                    player.updateSingleStat(MapleStat.LUK, player.getLuk());
                } else if (splitted[0].equalsIgnoreCase("dex") && amount + player.getDex() < 32001) {
                    player.setDex(player.getDex() + amount);
                    player.updateSingleStat(MapleStat.DEX, player.getDex());
                } else {
                    player.dropMessage("Please make sure the stat you are trying to raise is not over 32000.");
                }
                player.setRemainingAp(player.getRemainingAp() - amount);
                player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
            } else {
                player.dropMessage("Please make sure your AP is not over 32000 and you have enough to distribute.");
            }

        } else if (splitted[0].equalsIgnoreCase("togglesmega")) {
            player.changeViewingSmegas();
            player.dropMessage("Megaphones are now " + (player.wantsToViewSmega() ? "Visible" : "Invisible") + " to you.");

        } else if (splitted[0].equalsIgnoreCase("fmnpc")) {
           player.openNpc(9040011);

        } else if (splitted[0].equalsIgnoreCase("spinel")) {
            player.openNpc(9000020);

        } else if (splitted[0].equalsIgnoreCase("autorebirth")) {
            player.autoreborn = !player.autoreborn;
            player.dropMessage(player.autoreborn ? "Auto Reborn is on!" : "Auto reborn is off!");
        } else {
            player.dropMessage("Player Command " + heading + splitted[0] + " does not exist.");
        }
    }

    public static ResultSet getAllMail(String Reciever) {
        try {
            Connection con = (Connection) DatabaseConnection.getConnection();
            PreparedStatement ps = (PreparedStatement) con.prepareStatement("SELECT MailSender, Message FROM mail WHERE MailReciever = ? and Deleted = 0");
            ps.setString(1, Reciever);
            return ps.executeQuery();

        } catch (Exception ex) {
        }

        return null;
    }

    public static ResultSet getNewMail(String Reciever) {
        try {
            Connection con = (Connection) DatabaseConnection.getConnection();
            PreparedStatement ps = (PreparedStatement) con.prepareStatement("SELECT MailSender, Message FROM mail WHERE MailReciever = ? and Deleted = 0 and `Read` = 0");
            ps.setString(1, Reciever);
            return ps.executeQuery();

        } catch (Exception ex) {
        }

        return null;
    }
}