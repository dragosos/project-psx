package client.command;

import client.IItem;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import client.MapleStat;
import constants.ItemConstants;
import java.util.*;
import java.net.*;
import java.io.*;
import java.rmi.RemoteException;
import java.util.Map.Entry;
import net.MaplePacket;
import net.channel.ChannelServer;
import net.world.remote.WorldLocation;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.MapleTrade;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;

public class GMCommand {

    public static boolean execute(MapleClient c, String[] splitted, char heading) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equalsIgnoreCase("ban")) {
            try {
                String originalReason = StringUtil.joinStringFrom(splitted, 2);
                String reason = player.getName() + " banned " + splitted[1] + ": " + originalReason;
                MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (target != null) {
                    if (target.gmLevel() < 3 || player.gmLevel() > 4) {
                        String readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
                        String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                        reason += " (IP: " + ip + ")";
                        target.ban(reason);
                        player.dropMessage("Banned " + readableTargetName + " ipban for " + ip + " reason: " + originalReason);
                        try {
                            ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(0, readableTargetName + " has been banned for " + originalReason + ".").getBytes());
                        } catch (RemoteException e) {
                            cserv.reconnectWorld();
                        }
                    } else {
                        player.dropMessage("You may not ban GMs.");
                    }
                } else {
                    if (MapleCharacter.ban(splitted[1], reason, false)) {
                        player.dropMessage("Offline Banned " + splitted[1]);
                    } else {
                        player.dropMessage("Failed to ban " + splitted[1]);
                    }
                }
            } catch (NullPointerException e) {
                player.dropMessage(splitted[1] + " could not be banned.");
            }
        } else if (splitted[0].equalsIgnoreCase("cancelbuffs")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                victim.cancelAllBuffs();
            }

        } else if (splitted[0].equalsIgnoreCase("seduce")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            int level = Integer.parseInt(splitted[2]);
            if (victim != null) {
                victim.setChair(0);
                victim.getClient().getSession().write(MaplePacketCreator.cancelChair(-1));
                victim.getMap().broadcastMessage(victim, MaplePacketCreator.showChair(victim.getId(), 0), false);
                victim.giveDebuff(MapleDisease.SEDUCE, MobSkillFactory.getMobSkill(128, level));
            } else {
                player.dropMessage("Player is not on.");
            }
        } else if (splitted[0].equalsIgnoreCase("stun")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            int level = Integer.parseInt(splitted[2]);
            if (victim != null) {
                victim.setChair(0);
                victim.getClient().getSession().write(MaplePacketCreator.cancelChair(-1));
                victim.getMap().broadcastMessage(victim, MaplePacketCreator.showChair(victim.getId(), 0), false);
                victim.giveDebuff(MapleDisease.STUN, MobSkillFactory.getMobSkill(123, level));
            } else {
                player.dropMessage("Player is not on.");
            }
        } else if (splitted[0].equalsIgnoreCase("seal")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            int level = Integer.parseInt(splitted[2]);
            if (victim != null) {
                victim.setChair(0);
                victim.getClient().getSession().write(MaplePacketCreator.cancelChair(-1));
                victim.getMap().broadcastMessage(victim, MaplePacketCreator.showChair(victim.getId(), 0), false);
                victim.giveDebuff(MapleDisease.SEAL, MobSkillFactory.getMobSkill(120, level));
            } else {
                player.dropMessage("Player is not on.");
            }
        } else if (splitted[0].equalsIgnoreCase("stand")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            int level = Integer.parseInt(splitted[2]);
            if (victim != null) {
                victim.setChair(0);
                victim.getClient().getSession().write(MaplePacketCreator.cancelChair(-1));
                victim.getMap().broadcastMessage(victim, MaplePacketCreator.showChair(victim.getId(), 0), false);
            } else {
                player.dropMessage("Player is not on.");
            }

        } else if (splitted[0].equalsIgnoreCase("cleardrops")) {
            MapleMap map = player.getMap();
            List<MapleMapObject> items = map.getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
            for (MapleMapObject i : items) {
                map.removeMapObject(i);
                map.broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, player.getId()));
            }
            player.dropMessage("You have destroyed " + items.size() + " items on the ground.");

        } else if (splitted[0].equalsIgnoreCase("clock")) {
            player.getMap().broadcastMessage(MaplePacketCreator.getClock(Integer.parseInt(splitted[1])));

        } else if (splitted[0].equalsIgnoreCase("dc")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.getClient().getSession().close();
            victim.getClient().disconnect();
            victim.saveToDB(true);
            cserv.removePlayer(victim);

        } else if (splitted[0].equalsIgnoreCase("event")) {
            if (player.getClient().getChannelServer().eventOn == false) {
                int mapid = StringUtil.getOptionalIntArg(splitted, 1, c.getPlayer().getMapId());
                player.getClient().getChannelServer().eventOn = true;
                player.getClient().getChannelServer().eventMap = mapid;
                try {
                    cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, c.getChannel(), "[Event] The event has started in Channel " + c.getChannel() + " in " + player.getMapId() + "!").getBytes());
                } catch (RemoteException e) {
                    cserv.reconnectWorld();
                }
            } else {
                player.getClient().getChannelServer().eventOn = false;
                try {
                    cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, c.getChannel(), "[Event] The event has ended. Thanks to all of those who participated.").getBytes());
                } catch (RemoteException e) {
                    cserv.reconnectWorld();
                }
            }

        } else if (splitted[0].equalsIgnoreCase("fakerelog")) {
            c.getSession().write(MaplePacketCreator.getCharInfo(player));
            player.getMap().removePlayer(player);
            player.getMap().addPlayer(player);

        } else if (splitted[0].equalsIgnoreCase("fame")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            int fame = Integer.parseInt(splitted[2]);
            victim.setFame(fame);
            victim.updateSingleStat(MapleStat.FAME, fame);

        } else if (splitted[0].equalsIgnoreCase("giftnx")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).getCashShop().gainCash(1, Integer.parseInt(splitted[2]));
            player.message("Done");

        } else if (splitted[0].equalsIgnoreCase("heal")) {
            player.setHp(player.getMaxHp());
            player.updateSingleStat(MapleStat.HP, player.getMaxHp());
            player.setMp(player.getMaxMp());
            player.updateSingleStat(MapleStat.MP, player.getMaxMp());

        } else if (splitted[0].equalsIgnoreCase("healmap")) {
            for (MapleCharacter mch : player.getMap().getCharacters()) {
                if (mch != null) {
                    mch.setHp(mch.getMaxHp());
                    mch.updateSingleStat(MapleStat.HP, mch.getMaxHp());
                    mch.setMp(mch.getMaxMp());
                    mch.updateSingleStat(MapleStat.MP, mch.getMaxMp());
                }
            }

        } else if (splitted[0].equalsIgnoreCase("oxfalse")) {
            if (player.getMapId() == 109020001) {
                for (MapleCharacter mch : c.getPlayer().getMap().getCharacters()) {
                    if (mch != null) {
                        if (mch.getPosition().x <= -143) {
                            mch.setHp(0);
                            mch.setMp(0);
                            mch.updateSingleStat(MapleStat.HP, 0);
                            mch.updateSingleStat(MapleStat.MP, 0);
                        }
                    }
                }
            } else {
                player.dropMessage("You are not in the OX Quiz map.");
            }
        } else if (splitted[0].equalsIgnoreCase("oxtrue")) {
            if (player.getMapId() == 109020001) {
                for (MapleCharacter mch : c.getPlayer().getMap().getCharacters()) {
                    if (mch != null) {
                        if (mch.getPosition().x >= -305) {
                            mch.setHp(0);
                            mch.setMp(0);
                            mch.updateSingleStat(MapleStat.HP, 0);
                            mch.updateSingleStat(MapleStat.MP, 0);
                        }
                    }
                }
            } else {
                player.dropMessage("You are not in the OX Quiz map.");
            }

        } else if (splitted[0].equalsIgnoreCase("oxmap")) {
            player.changeMap(109020001);

        } else if (splitted[0].equalsIgnoreCase("healperson")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setHp(victim.getMaxHp());
            victim.updateSingleStat(MapleStat.HP, victim.getMaxHp());
            victim.setMp(victim.getMaxMp());
            victim.updateSingleStat(MapleStat.MP, victim.getMaxMp());

        } else if (splitted[0].equalsIgnoreCase("id") || splitted[0].equalsIgnoreCase("search")) {
            try {
                URL url = new URL("http://www.mapletip.com/search_java.php?search_value=" + splitted[1] + "&check=true");
                URLConnection urlConn = url.openConnection();
                urlConn.setDoInput(true);
                urlConn.setUseCaches(false);
                BufferedReader dis = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String s;
                while ((s = dis.readLine()) != null) {
                    player.dropMessage(s);
                }
                dis.close();
            } catch (MalformedURLException mue) {
            } catch (IOException ioe) {
            }
        } else if (splitted[0].equalsIgnoreCase("job")) {
            player.changeJob(MapleJob.getById(Integer.parseInt(splitted[1])));

        } else if (splitted[0].equalsIgnoreCase("jobperson")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeJob(MapleJob.getById(Integer.parseInt(splitted[2])));

        } else if (splitted[0].equalsIgnoreCase("kill")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setHp(0);
            victim.updateSingleStat(MapleStat.HP, 0);
        } else if (splitted[0].equalsIgnoreCase("killall") || splitted[0].equalsIgnoreCase("monsterdebug")) {
            MapleMap map = player.getMap();
            double range = Double.POSITIVE_INFINITY;
            List<MapleMapObject> monsters = map.getMapObjectsInRange(player.getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));
            boolean kill = splitted[0].equalsIgnoreCase("killall");
            for (MapleMapObject monstermo : monsters) {
                MapleMonster monster = (MapleMonster) monstermo;
                if (kill) {
                    map.killMonster(monster, player, true);
                    monster.giveExpToCharacter(player, monster.getExp(), true, 1);
                } else {
                    player.dropMessage("Monster " + monster.toString());
                }
            }
            if (kill) {
                player.dropMessage("Killed " + monsters.size() + " monsters.");
            }
        } else if (splitted[0].equalsIgnoreCase("killeveryone")) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                mch.setHp(0);
                mch.updateSingleStat(MapleStat.HP, 0);
            }

        } else if (splitted[0].equalsIgnoreCase("killmap")) {
            for (MapleCharacter mch : player.getMap().getCharacters()) {
                mch.setHp(0);
                mch.updateSingleStat(MapleStat.HP, 0);
            }

        } else if (splitted[0].equalsIgnoreCase("levelperson")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setLevel(Integer.parseInt(splitted[2]));
            victim.gainExp(-victim.getExp(), false, false);
            victim.updateSingleStat(MapleStat.LEVEL, victim.getLevel());

        } else if (splitted[0].equalsIgnoreCase("mesoperson")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).gainMeso(Integer.parseInt(splitted[2]), true);

        } else if (splitted[0].equalsIgnoreCase("mesos")) {
            player.gainMeso(Integer.parseInt(splitted[1]), true);

        } else if (splitted[0].equalsIgnoreCase("saveall")) {
            for (ChannelServer chan : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : chan.getPlayerStorage().getAllCharacters()) {
                    chr.saveToDB(true);
                }
            }
            player.dropMessage("Save complete.");

        } else if (splitted[0].equals("spawn")) {
            if (splitted.length > 2) {
                for (int i = 0; i < Integer.parseInt(splitted[2]); i++) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(Integer.parseInt(splitted[1])), player.getPosition());
                }
            } else {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(Integer.parseInt(splitted[1])), player.getPosition());
            }
            
        } else if (splitted[0].equalsIgnoreCase("unjail")) {
            MapleMap target = cserv.getMapFactory().getMap(100000000);
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeMap(target, target.getPortal(0));

        } else if (splitted[0].equalsIgnoreCase("warp")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                if (splitted.length == 2) {
                    MapleMap target = victim.getMap();
                    player.changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
                } else {
                    MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(Integer.parseInt(splitted[2]));
                    victim.changeMap(target, target.getPortal(0));
                }
            } else {
                try {
                    victim = player;
                    WorldLocation loc = cserv.getWorldInterface().getLocation(splitted[1]);
                    if (loc != null) {
                        player.dropMessage("You will be cross-channel warped. This may take a few seconds.");
                        MapleMap target = cserv.getMapFactory().getMap(loc.map);
                        victim.cancelAllBuffs();
                        String ip = cserv.getIP(loc.channel);
                        victim.getMap().removePlayer(victim);
                        victim.setMap(target);
                        String[] socket = ip.split(":");
                        if (victim.getTrade() != null) {
                            MapleTrade.cancelTrade(player);
                        }
                        victim.saveToDB(true);
                        ChannelServer.getInstance(c.getChannel()).removePlayer(player);
                        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                        try {
                            MaplePacket packet = MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]));
                            c.getSession().write(packet);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        MapleMap target = cserv.getMapFactory().getMap(Integer.parseInt(splitted[1]));
                        player.changeMap(target, target.getPortal(0));
                    }
                } catch (Exception e) {
                }
            }

        } else if (splitted[0].equalsIgnoreCase("warpmap")) {
            for (MapleCharacter mch : player.getMap().getCharacters()) {
                if (mch != null) {
                    MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(Integer.parseInt(splitted[1]));
                    mch.changeMap(target, target.getPortal(0));
                }
            }

        } else if (splitted[0].equalsIgnoreCase("warphere")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeMap(player.getMap(), player.getMap().findClosestSpawnpoint(player.getPosition()));

        } else if (splitted[0].equalsIgnoreCase("whatmap")) {
            player.dropMessage("You are on map " + player.getMap().getId());

        } else if (splitted[0].equalsIgnoreCase("whosthere")) {
            StringBuilder builder = new StringBuilder("Players on Map: ");
            for (MapleCharacter chr : player.getMap().getCharacters()) {
                if (builder.length() > 150) {
                    builder.setLength(builder.length() - 2);
                    player.dropMessage(builder.toString());
                }
                builder.append(MapleCharacter.makeMapleReadable(chr.getName())).append(", ");
            }
            builder.setLength(builder.length() - 2);
            c.getSession().write(MaplePacketCreator.serverNotice(6, builder.toString()));

        } else if (splitted[0].equalsIgnoreCase("nx")) {
            for (int x = 0; x < 10; x++) {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400202), player.getPosition());
            }

        } else if (splitted[0].equalsIgnoreCase("str") || splitted[0].equalsIgnoreCase("dex") || splitted[0].equalsIgnoreCase("int") || splitted[0].equalsIgnoreCase("luk")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);//STAT COMMANDS
            int up = Integer.parseInt(splitted[2]);
            if (splitted[0].equalsIgnoreCase("str")) {
                victim.setStr(up);
                victim.updateSingleStat(MapleStat.STR, victim.getStr());
            } else if (splitted[0].equalsIgnoreCase("dex")) {
                victim.setDex(up);
                victim.updateSingleStat(MapleStat.DEX, victim.getDex());
            } else if (splitted[0].equalsIgnoreCase("luk")) {
                victim.setLuk(up);
                victim.updateSingleStat(MapleStat.LUK, victim.getLuk());
            } else {
                victim.setInt(up);
                victim.updateSingleStat(MapleStat.INT, victim.getInt());
            }

        } else if (splitted[0].equalsIgnoreCase("ap")) {
            player.setRemainingAp(Integer.parseInt(splitted[1]));
            player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());

        } else if (splitted[0].equalsIgnoreCase("level")) {
            player.setLevel(Integer.parseInt(splitted[1]));
            player.gainExp(-player.getExp(), false, false);
            player.updateSingleStat(MapleStat.LEVEL, player.getLevel());

        } else if (splitted[0].equalsIgnoreCase("maxall")) {
            player.setStr(32767);
            player.setDex(32767);
            player.setInt(32767);
            player.setLuk(32767);
            player.setLevel(255);
            player.setFame(13337);
            player.setMaxHp(30000);
            player.setMaxMp(30000);
            player.updateSingleStat(MapleStat.STR, 32767);
            player.updateSingleStat(MapleStat.DEX, 32767);
            player.updateSingleStat(MapleStat.INT, 32767);
            player.updateSingleStat(MapleStat.LUK, 32767);
            player.updateSingleStat(MapleStat.LEVEL, 255);
            player.updateSingleStat(MapleStat.FAME, 13337);
            player.updateSingleStat(MapleStat.MAXHP, 30000);
            player.updateSingleStat(MapleStat.MAXMP, 30000);

        } else if (splitted[0].equalsIgnoreCase("setall")) {
            int x = Integer.parseInt(splitted[1]);
            player.setStr(x);
            player.setDex(x);
            player.setInt(x);
            player.setLuk(x);
            player.updateSingleStat(MapleStat.STR, player.getStr());
            player.updateSingleStat(MapleStat.DEX, player.getStr());
            player.updateSingleStat(MapleStat.INT, player.getStr());
            player.updateSingleStat(MapleStat.LUK, player.getStr());

        } else if (splitted[0].equalsIgnoreCase("chattype")) {
            player.changeGMChat();
            player.dropMessage("You are now speaking in " + (player.getGMChat() ? "White" : "Black") + " chat.");

        } else if (splitted[0].equalsIgnoreCase("mute")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                if (splitted[2].equalsIgnoreCase("Perm") || splitted[2].equalsIgnoreCase("Permanent")) {
                    victim.setMuteLevel(2);
                    victim.dropMessage(" You have been Permanently muted. You can only be unmuted by a GM.");
                } else if (splitted[2].equalsIgnoreCase("Temp") || splitted[2].equalsIgnoreCase("Temperary")) {
                    victim.setMuteLevel(1);
                    victim.dropMessage(" You have been Temporary muted. You can be unmuted by doing @fmnpc.");
                } else if (splitted.length < 2) {
                    player.dropMessage(" ..:: Mute Commands ::..");
                    player.dropMessage(" !mute <name> <Perm/Temp>");
                    player.dropMessage(" Permanent means only a GM can unban. Temporary means they can uban themselves in @fmnpc");
                }
                player.dropMessage(" Success. ");
            } else {
                player.dropMessage(splitted[1] + " is either offline or doesn't exist.");
            }

        } else if (splitted[0].equalsIgnoreCase("unmute")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                victim.setMuteLevel(0);
                victim.dropMessage(" You have been unmuted.");
                player.dropMessage(" Success. ");
            } else {
                player.dropMessage(splitted[1] + " is either offline or doesn't exist.");
            }

        } else if (splitted[0].equalsIgnoreCase("mutemap")) {
            for (MapleCharacter players : player.getMap().getCharacters()) {
                players.setMuteLevel(1);
                players.dropMessage("You have been muted. Listen to the GM's instructions!");
            }

        } else if (splitted[0].equalsIgnoreCase("unmutemap")) {
            for (MapleCharacter players : player.getMap().getCharacters()) {
                players.setMuteLevel(0);
            }
            
        } else if (splitted[0].equalsIgnoreCase("map")) {
           player.changeMap(Integer.parseInt(splitted[1]), splitted.length > 2 ? Integer.parseInt(splitted[2]) : 0);

        } else if (splitted[0].equalsIgnoreCase("gmshop")) {
            MapleShopFactory.getInstance().getShop(1337).sendShop(player.getClient());

        } else if (splitted[0].equalsIgnoreCase("openshop")) {
            MapleShopFactory.getInstance().getShop(Integer.parseInt(splitted[1])).sendShop(c);

        } else if (splitted[0].equalsIgnoreCase("pos")) {
            player.dropMessage("X Coordinate: " + player.getPosition().getX() + " Y Coordinate: " + player.getPosition().getY() + " ( " + player.getPosition() + " )");

        } else if (splitted[0].equalsIgnoreCase("mutesmega") || splitted[0].equals("unmutesmega")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setBlockFromSmega(splitted[0].equalsIgnoreCase("mutesmega") ? 1 : 0);
            victim.dropMessage(splitted[0].equals("unmutesmega") ? " You can now use smegas. " : "You can no longer use smegas.");
            player.dropMessage(splitted[0].equals("unmutesmega") ? "The player can now use smegas." : "The player can no longer use smegas.");

        } else if (splitted[0].equalsIgnoreCase("say")) {
            try {
                ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "[" + player.getName() + "] " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
            } catch (RemoteException e) {
            }

        } else if (splitted[0].equalsIgnoreCase("gmmessage")) {
            for (MapleCharacter players : cserv.getPlayerStorage().getAllCharacters()) {
                if (players.gmLevel() >= 3) {
                    players.dropMessage("[Gm Message]: " + StringUtil.joinStringFrom(splitted, 1));
                }
            }

        } else if (splitted[0].equalsIgnoreCase("warpperson")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeMap(cserv.getPlayerStorage().getCharacterByName(splitted[2]).getMapId());
            
        } else if (splitted[0].equalsIgnoreCase("playerswithplayer")) {
            MapleCharacter otherplayer = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (otherplayer != null) {
                StringBuilder players = new StringBuilder("Players on Map: ");
                for (MapleCharacter chr : otherplayer.getMap().getCharacters()) {
                    players.append(MapleCharacter.makeMapleReadable(chr.getName())).append(", ");
                }
                //builder.setLength(builder.length() - 2);
                otherplayer.getClient().getSession().write(MaplePacketCreator.serverNotice(6, players.toString()));
            } else {
                player.dropMessage(" Can't find player: " + otherplayer.getName());
            }

        } else if (splitted[0].equalsIgnoreCase("closechalkboards")) {
            for (MapleCharacter map : player.getMap().getCharacters()) {
                map.setChalkboard(null);
                map.getClient().announce(MaplePacketCreator.useChalkboard(map, true));
            }

        } else if (splitted[0].equalsIgnoreCase("spawnmob")) {
            List<Pair<Integer, String>> mobpairs = new LinkedList<Pair<Integer, String>>();
            List<Pair<Integer, String>> monsters = new ArrayList<Pair<Integer, String>>();
            for (MapleData child : MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("string.wz")).getData("Mob.img").getChildren()) {
                player.dropMessage((String) child.getChildByPath("name").getData());
                monsters.add(new Pair(child.getName(), (String) child.getChildByPath("name").getData()));
            }
            for (Pair<Integer, String> allMobs : monsters) {
                if (allMobs.getRight().toLowerCase().equals(joinStringFrom(splitted, 2).toLowerCase())) {
                    mobpairs.add(new Pair(allMobs.getLeft(), allMobs.getRight()));
                }
            }
            if (mobpairs.isEmpty()) {
                player.dropMessage(" There is no such mob with the name:" + joinStringFrom(splitted, 2));
            } else {
                for (Pair<Integer, String> mobsToSummon : mobpairs) {
                    player.dropMessage(mobsToSummon.getRight());
                    //for (int i = 0; i < Integer.parseInt(splitted[1]); i++) {

                    player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobsToSummon.getLeft()), player.getPosition());
                    //}
                    player.dropMessage(" Have fun with your " + Integer.parseInt(splitted[1]) + mobsToSummon.getRight());
                }
            }

        } else if (splitted[0].equalsIgnoreCase("item") || splitted[0].equalsIgnoreCase("drop")) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if ((Object) splitted[1] instanceof Integer && (Object) splitted[2] instanceof Integer) {
                if (splitted[0].equalsIgnoreCase("item")) {
                    int itemId = Integer.parseInt(splitted[1]);
                    if (itemId >= 5000000 && itemId < 5000065) {
                        MaplePet.createPet(itemId);
                    } else {
                        MapleInventoryManipulator.addById(c, itemId, Short.parseShort(splitted[2]), player.getName(), -1);
                    }
                } else {
                    int itemId = Integer.parseInt(splitted[1]);
                    IItem toDrop;
                    if (ii.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                        toDrop = ii.getEquipById(itemId);
                    } else {
                        toDrop = new Item(itemId, (byte) 0, (short) StringUtil.getOptionalIntArg(splitted, 2, 1));
                    }
                    toDrop.setOwner(player.getName());
                    c.getPlayer().getMap().spawnItemDrop(player, player, toDrop, player.getPosition(), true, true);
                }
            } else {
                List<Pair<Integer, String>> itemPairs = new LinkedList<Pair<Integer, String>>();
                for (Pair<Integer, String> allitems : ii.getAllItems()) {
                    if (allitems.getRight().toLowerCase().equals(joinStringFrom(splitted, 2).toLowerCase())) {
                        itemPairs.add(new Pair(allitems.getLeft(), allitems.getRight()));
                    }
                }
                if (itemPairs.isEmpty()) {
                    player.dropMessage(" Sorry there is no item with the name: " + joinStringFrom(splitted, 2));
                } else {
                    String name = "Null Name";
                    Short quantity = 0;
                    for (Pair<Integer, String> itemsToAdd : itemPairs) {
                        int itemid = itemsToAdd.getLeft();
                        name = itemsToAdd.getRight();
                        quantity = (short) Integer.parseInt(splitted[1]);
                        if (splitted[0].equalsIgnoreCase("item")) {
                            if (itemid >= 5000000 && itemid < 5000065) {
                                MaplePet.createPet(itemid);
                            } else if (ii.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
                                int i = 1;
                                do {
                                    MapleInventoryManipulator.addById(c, itemid, (short) 1);
                                    i++;
                                } while (i <= quantity && player.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() != -1);
                            } else {
                                if (player.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() != -1) {
                                    MapleInventoryManipulator.addById(c, itemid, quantity);
                                } else {
                                    player.dropMessage(" No free space. ");
                                }
                                return false;
                            }
                        } else if (splitted[0].equalsIgnoreCase("drop")) {
                            if (ii.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
                                int i = 1;
                                do {
                                    player.getMap().spawnItemDrop(player, player, ii.getEquipById(itemid), player.getPosition(), true, true);
                                    i++;
                                } while (i <= quantity);
                            } else {
                                player.getMap().spawnItemDrop(player, player, new Item(itemid, (byte) 0, quantity), player.getPosition(), true, true);
                            }
                        }
                    }
                    player.dropMessage(" Have fun with your " + quantity + " new: " + name);
                }
            }

        } else if (splitted[0].equalsIgnoreCase("warpperson")) {
            player.getClient().getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]).changeMap(Integer.parseInt(splitted[2]));

        } else {
            if (player.gmLevel() == 3) {
                player.dropMessage("GM Command " + heading + splitted[0] + " does not exist");
            }
            return false;
        }
        return true;
    }

    public static String joinStringFrom(String arr[], int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i != arr.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}