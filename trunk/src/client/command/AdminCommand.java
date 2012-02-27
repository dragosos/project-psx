package client.command;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import constants.ServerConstants;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import net.channel.ChannelServer;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class AdminCommand {

    public static boolean execute(MapleClient c, String[] splitted, char heading) {
        ChannelServer cserv = c.getChannelServer();
        MapleCharacter player = c.getPlayer();
        if (splitted[0].equalsIgnoreCase("popup")) {
            try {
                ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(1, StringUtil.joinStringFrom(splitted, 1)).getBytes());
            } catch (RemoteException noob) {
                cserv.reconnectWorld();
            }

        } else if (splitted[0].equalsIgnoreCase("pnpc")) {
            int npcId = Integer.parseInt(splitted[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId);
            int xpos = player.getPosition().x;
            int ypos = player.getPosition().y;
            int fh = player.getMap().getFootholds().findBelow(player.getPosition()).getId();
            if (npc != null && !npc.getName().equalsIgnoreCase("MISSINGNO")) {
                npc.setPosition(player.getPosition());
                npc.setCy(ypos);
                npc.setRx0(xpos + 50);
                npc.setRx1(xpos - 50);
                npc.setFh(fh);
                npc.setCustom(true);
                try {
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                    ps.setInt(1, npcId);
                    ps.setInt(2, 0);
                    ps.setInt(3, fh);
                    ps.setInt(4, ypos);
                    ps.setInt(5, xpos + 50);
                    ps.setInt(6, xpos - 50);
                    ps.setString(7, "n");
                    ps.setInt(8, xpos);
                    ps.setInt(9, ypos);
                    ps.setInt(10, player.getMapId());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    player.dropMessage("Failed to save NPC to the database");
                }
                player.getMap().addMapObject(npc);
                player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
            } else {
                player.dropMessage("You have entered an invalid Npc-Id");
            }

        } else if (splitted[0].equalsIgnoreCase("setreborns")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).setReborns(Integer.parseInt(splitted[2]));

        } else if (splitted[0].equalsIgnoreCase("changegender")) {
            if (splitted.length == 3) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    cserv.getPlayerStorage().getCharacterByName(splitted[1]).setGender(Integer.parseInt(splitted[2]));
                } else {
                    player.dropMessage(splitted[1] + " Not found.");
                }
                if (victim.getGender() == 0) {
                    victim.dropMessage(" You are now a male!");
                    player.dropMessage(splitted[1] + " Is now a Male!");
                } else if (victim.getGender() == 1) {
                    victim.dropMessage(" You are now a female!");
                    player.dropMessage(splitted[1] + " Is now a Female!");
                } else {
                    victim.dropMessage(" You are now a Transvestite! Have fun!");
                    player.dropMessage(splitted[1] + " Is now a Transvestite! Haha. What a fail :]");
                }
                victim.saveToDB(true);
            } else {
                player.dropMessage(" Incorrect Syntax. Use: !tranny (name) (type)  where type = 0, 1, or 2.");
            }

        } else if (splitted[0].equalsIgnoreCase("killfm")) {
            if (player.getMapId() == 910000000) {
                for (MapleCharacter mch : c.getPlayer().getMap().getCharacters()) {
                    if (mch != null) {
                        if (mch.getPosition().x <= 1626) {
                            mch.setHp(0);
                            mch.setMp(0);
                            mch.updateSingleStat(MapleStat.HP, 0);
                            mch.updateSingleStat(MapleStat.MP, 0);
                        }
                    }
                }
            } else {
                player.dropMessage("You are not in the FM ");
            }

        } else if (splitted[0].equals("mesorate")) {
            ServerConstants.MESO_RATE = (byte) (Integer.parseInt(splitted[1]) % 128);
            cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "Meso Rate has been changed to " + Integer.parseInt(splitted[1]) + "x."));

        } else if (splitted[0].equals("exprate")) {
            ServerConstants.EXP_RATE = (byte) (Integer.parseInt(splitted[1]) % 350);
            cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "Exp Rate has been changed to " + Integer.parseInt(splitted[1]) + "x."));
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                for (MapleCharacter mc : cs.getPlayerStorage().getAllCharacters()) {
                    mc.setRates();
                }
            }

        } else if (splitted[0].equals("speakall")) {
            String text = StringUtil.joinStringFrom(splitted, 1);
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                mch.getMap().broadcastMessage(MaplePacketCreator.getChatText(mch.getId(), text, false, 0));
            }

        } else if (splitted[0].equals("pmob")) {
            int npcId = Integer.parseInt(splitted[1]);
            int mobTime = Integer.parseInt(splitted[2]);
            int xpos = player.getPosition().x;
            int ypos = player.getPosition().y;
            int fh = player.getMap().getFootholds().findBelow(player.getPosition()).getId();
            if (splitted[2] == null) {
                mobTime = 0;
            }
            MapleMonster mob = MapleLifeFactory.getMonster(npcId);
            if (mob != null && !mob.getName().equals("MISSINGNO")) {
                mob.setPosition(player.getPosition());
                mob.setCy(ypos);
                mob.setRx0(xpos + 50);
                mob.setRx1(xpos - 50);
                mob.setFh(fh);
                MapleMonster monster = mob;
                try {
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid, mobtime ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                    ps.setInt(1, npcId);
                    ps.setInt(2, 0);
                    ps.setInt(3, fh);
                    ps.setInt(4, ypos);
                    ps.setInt(5, xpos + 50);
                    ps.setInt(6, xpos - 50);
                    ps.setString(7, "m");
                    ps.setInt(8, xpos);
                    ps.setInt(9, ypos);
                    ps.setInt(10, player.getMapId());
                    ps.setInt(11, mobTime);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    player.dropMessage("Failed to save MOB to the database");
                }
                player.getMap().addMonsterSpawn(monster.getId(), monster.getPosition(), mobTime, 0, (short) monster.getFh(), (short) monster.getRx0(), (short) monster.getRx1(), (short) monster.getCy(), (short) monster.getF());

            } else {
                player.dropMessage("You have entered an invalid Mob-Id");
            }

        } else if (splitted[0].equals("playernpc")) {
            player.playerNPC(c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]), Integer.parseInt(splitted[2]));

        } else if (splitted[0].equalsIgnoreCase("gmperson")) {
            final MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setGM(Integer.parseInt(splitted[2]));
            victim.dropMessage(" You are now a level " + Integer.parseInt(splitted[2]) + " GM. You will be disconnected in 10 seconds to apply the GM.");
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    victim.getClient().disconnect();
                }
            }, 10000);

        } else if (splitted[0].equalsIgnoreCase("notice")) {
            try {
                ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "[Notice]: " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
            } catch (RemoteException e) {
            }

        } else {
            player.dropMessage("Command " + heading + splitted[0] + " does not exist");
            return false;
        }
        return true;
    }
}