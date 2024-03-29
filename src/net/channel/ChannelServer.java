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
package net.channel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MalformedObjectNameException;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import client.MapleCharacter;
import client.SkillFactory;
import constants.ServerConstants;
import java.util.ArrayList;
import java.util.Map.Entry;
import net.world.MapleParty;
import tools.DatabaseConnection;
import net.MaplePacket;
import net.MapleServerHandler;
import net.PacketProcessor;
import net.ServerMode;
import net.ServerMode.Mode;
import net.mina.MapleCodecFactory;
import net.world.MaplePartyCharacter;
import net.world.guild.MapleGuild;
import net.world.guild.MapleGuildCharacter;
import net.world.guild.MapleGuildSummary;
import net.world.remote.WorldChannelInterface;
import net.world.remote.WorldRegistry;
import provider.MapleDataProviderFactory;
import scripting.event.EventScriptManager;
import server.ShutdownServer;
import server.TimerManager;
import server.maps.MapleMapFactory;
import tools.MaplePacketCreator;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import server.MapleSquad;
import server.MapleSquadType;
import server.events.MapleEvent;
import server.maps.HiredMerchant;
import server.maps.MapleMap;
import client.GameMode;
import server.GameModeManager;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import tools.FileoutputUtil;
import client.MapleClient;
import net.world.WorldServer;
import server.calendar.CalendarEventsManager;
import server.life.MapleLifeFactory;

public class ChannelServer implements Runnable {

    private int port = 7575;
    private static WorldRegistry worldRegistry;
    private PlayerStorage players = new PlayerStorage();
    private int channel;
    private String key;
    private ChannelWorldInterface cwi;
    private WorldChannelInterface wci;
    private IoAcceptor acceptor;
    private String ip;
    private boolean shutdown = false;
    private boolean finishedShutdown = false;
    private MapleMapFactory mapFactory;
    private EventScriptManager eventSM;
    private static Map<Integer, ChannelServer> instances = new HashMap<Integer, ChannelServer>();
    private static Map<String, ChannelServer> pendingInstances = new HashMap<String, ChannelServer>();
    private Map<Integer, MapleGuildSummary> gsStore = new HashMap<Integer, MapleGuildSummary>();
    private Map<Integer, HiredMerchant> hiredMerchants = new HashMap<Integer, HiredMerchant>();
    private Boolean worldReady = true;
    private int instanceId = 0;
    private MapleEvent event;
    private Map<MapleSquadType, MapleSquad> mapleSquads = new HashMap<MapleSquadType, MapleSquad>();
    private ReentrantReadWriteLock mapLocks = new ReentrantReadWriteLock();
    private CalendarEventsManager cal_events = new CalendarEventsManager((byte) channel);
    public boolean eventOn = false;
    public int eventMap = 0;

    private ChannelServer(String key) {
        mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")));
        this.key = key;
    }

    public static WorldRegistry getWorldRegistry() {
        return worldRegistry;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void addInstanceId() {
        instanceId++;
    }

    public GameModeManager getGameModeManager() {
        return null;
    }

    public void reconnectWorld() {
        try {
            if (wci != null) {
                wci.isAvailable();
            } else {
                System.out.println("WCI IS NULL");
            }
        } catch (RemoteException ex) {
            synchronized (worldReady) {
                System.out.println("Worldready is false.");
                worldReady = false;
            }
            synchronized (worldReady) {
                if (worldReady) {
                    return;
                }
            }
            synchronized (cwi) {
                System.out.println("Reconnecting to world server");
                synchronized (wci) {
                    try {
                        Registry registry = LocateRegistry.getRegistry(ServerConstants.HOST, 1099, new SslRMIClientSocketFactory());
                        worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
                        cwi = new ChannelWorldInterfaceImpl(this);
                        wci = worldRegistry.registerChannelServer(key, cwi);
                        Properties dbProp = new Properties();
                        FileReader fr = new FileReader("db.properties");
                        dbProp.load(fr);
                        fr.close();
                        DatabaseConnection.initialize(dbProp);
                        DatabaseConnection.getConnection();
                        wci.serverReady();
                    } catch (Exception e) {
                    }
                    worldReady = true;
                }
            }
            synchronized (worldReady) {
                worldReady.notifyAll();
            }
        }
    }

    @Override
    public void run() {
        try {
            cwi = new ChannelWorldInterfaceImpl(this);
            try {
                wci = worldRegistry.registerChannelServer(key, cwi);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            eventSM = new EventScriptManager(this, ServerConstants.EVENTS.split(" "));
            Properties dbProp = new Properties();
            FileReader fileReader = new FileReader("db.properties");
            dbProp.load(fileReader);
            fileReader.close();
            DatabaseConnection.initialize(dbProp);
            Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement("UPDATE accounts SET loggedin = 0");
            ps.executeUpdate();
            ps.close();
            ps = c.prepareStatement("UPDATE characters SET HasMerchant = 0");
            ps.executeUpdate();
            ps.close();
            port = 7575 + this.channel - 1;
            ip = ServerConstants.HOST + ":" + port;
            IoBuffer.setUseDirectBuffer(false);
            IoBuffer.setAllocator(new SimpleBufferAllocator());
            acceptor = new NioSocketAcceptor();
            TimerManager tMan = TimerManager.getInstance();
            tMan.start();
            tMan.register(new respawnMaps(), 10000);
            //  tMan.register(new doFishing(), 30000);
            acceptor.setHandler(new MapleServerHandler(PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER), channel));
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
            acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
            acceptor.bind(new InetSocketAddress(port));
            ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
            SkillFactory.getSkill(9999999);
            worldReady = true;
            wci.serverReady();
            getCalendarManager().startTimer();
            eventSM.init();
            MapleLifeFactory.loadQuestCounts();
            FileoutputUtil.log(FileoutputUtil.runtime_log, "Channel server(" + getChannel() + ") is online on port " + port, null);

            //    MapleLifeFactory.getAllNoTouchMonsters(); // lawl
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CalendarEventsManager getCalendarManager() {
        return cal_events;
    }

    public void shutdown() {
        shutdown = true;
        boolean error = true;
        while (error) {
            try {
                for (HiredMerchant hm : getHiredMerchants().values()) {
                    hm.saveItems(true);
                }
                for (MapleCharacter chr : players.getAllCharacters()) {
                    synchronized (chr) {
                        chr.getClient().disconnect();
                    }
                    error = false;
                }
            } catch (Exception e) {
                error = true;
            }
        }
        finishedShutdown = true;
        wci = null;
        cwi = null;
    }

    public void unbind() {
        acceptor.unbind();
    }

    public boolean hasFinishedShutdown() {
        return finishedShutdown;
    }

    public MapleMapFactory getMapFactory(GameMode gMode) {
        return mapFactory;
    }

    public MapleMapFactory getMapFactory(int type) {
        return mapFactory;
    }

    public MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    private static ChannelServer newInstance(String key, int channel) throws InstanceAlreadyExistsException, MalformedObjectNameException {
        ChannelServer instance = new ChannelServer(key);
        pendingInstances.put(key, instance);
        return instance;
    }

    public static ChannelServer getInstance(int channel) {
        return instances.get(channel);
    }

    public void addPlayer(MapleCharacter chr) {
        players.registerPlayer(chr);
        chr.announce(MaplePacketCreator.serverMessage(ServerConstants.SERVER_MESSAGE));
    }

    public IPlayerStorage getPlayerStorage() {
        return players;
    }

    public void removePlayer(MapleCharacter chr) {
        players.deregisterPlayer(chr);
    }

    public int getConnectedClients() {
        return players.getAllCharacters().size();
    }

    public void setServerMessage(String newMessage) {
        ServerConstants.SERVER_MESSAGE = newMessage;
        broadcastPacket(MaplePacketCreator.serverMessage(ServerConstants.SERVER_MESSAGE));
    }

    public void broadcastPacket(MaplePacket data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            chr.announce(data);
        }
    }

    public void broadcastMegaphones(MaplePacket data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            if (chr.wantsToViewSmega()) {
                chr.announce(data);
            }
        }
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        if (pendingInstances.containsKey(key)) {
            pendingInstances.remove(key);
        }
        if (instances.containsKey(channel)) {
            instances.remove(channel);
        }
        instances.put(channel, this);
        this.channel = channel;
        mapFactory.setChannel(channel);
    }

    public static Collection<ChannelServer> getAllInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public String getIP() {
        return ip;
    }

    public String getIP(int channel) {
        try {
            return getWorldInterface().getIP(channel);
        } catch (RemoteException e) {
            System.out.println("Lost connection to world server " + e);
            throw new RuntimeException("Lost connection to world server");
        }
    }

    public WorldChannelInterface getWorldInterface() {
        synchronized (worldReady) {
            while (!worldReady) {
                try {
                    worldReady.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return wci;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void shutdown(int time) {
        TimerManager.getInstance().schedule(new ShutdownServer(getChannel()), time);
    }

    public MapleEvent getEvent() {
        return event;
    }

    public void setEvent(MapleEvent event) {
        this.event = event;
    }

    public EventScriptManager getEventSM() {
        return eventSM;
    }

    public MapleGuild getGuild(MapleGuildCharacter mgc) {
        int gid = mgc.getGuildId();
        MapleGuild g = null;
        try {
            g = this.getWorldInterface().getGuild(gid, mgc);
        } catch (RemoteException re) {
            System.out.println("RemoteException while fetching MapleGuild. " + re);
            return null;
        }
        if (gsStore.get(gid) == null) {
            gsStore.put(gid, new MapleGuildSummary(g));
        }
        return g;
    }

    public MapleGuildSummary getGuildSummary(int gid) {
        if (gsStore.containsKey(gid)) {
            return gsStore.get(gid);
        } else {
            try {
                MapleGuild g = this.getWorldInterface().getGuild(gid, null);
                if (g != null) {
                    gsStore.put(gid, new MapleGuildSummary(g));
                }
                return gsStore.get(gid);
            } catch (RemoteException re) {
                System.out.println("RemoteException while fetching GuildSummary. " + re);
                return null;
            }
        }
    }

    public void updateGuildSummary(int gid, MapleGuildSummary mgs) {
        gsStore.put(gid, mgs);
    }

    public void reloadGuildSummary() {
        try {
            MapleGuild g;
            for (int i : gsStore.keySet()) {
                g = this.getWorldInterface().getGuild(i, null);
                if (g != null) {
                    gsStore.put(i, new MapleGuildSummary(g));
                } else {
                    gsStore.remove(i);
                }
            }
        } catch (RemoteException re) {
            System.out.println("RemoteException while reloading GuildSummary." + re);
        }
    }

    public static void main(String args[]) throws FileNotFoundException, IOException, NotBoundException, InstanceAlreadyExistsException, MalformedObjectNameException {
        ServerMode.setServerMode(Mode.CHANNEL);
        Registry registry = LocateRegistry.getRegistry("localhost", 1099, new SslRMIClientSocketFactory());
        worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
        for (int i = 0; i < ServerConstants.CHANNEL_NUMBER; i++) {
            newInstance("release" + (i + 1), i + 1).run();
        }
        DatabaseConnection.getConnection();
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                for (ChannelServer channel : getAllInstances()) {
                    for (MapleCharacter mc : channel.getPlayerStorage().getAllCharacters()) {
                        mc.saveToDB(true);
                        if (mc.getHiredMerchant() != null) {
                            if (mc.getHiredMerchant().isOpen()) {
                                try {
                                    mc.getHiredMerchant().saveItems(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void broadcastGMPacket(MaplePacket data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            if (chr.isGM()) {
                chr.announce(data);
            }
        }
    }

    public void yellowWorldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
            mc.announce(MaplePacketCreator.sendYellowTip(msg));
        }
    }

    public void worldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
            mc.dropMessage(msg);
        }
    }

    public List<MapleCharacter> getPartyMembers(MapleParty party) {
        List<MapleCharacter> partym = new ArrayList<MapleCharacter>(8);
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (partychar.getChannel() == getChannel()) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
                if (chr != null) {
                    partym.add(chr);
                }
            }
        }
        return partym;
    }

    public class respawnMaps implements Runnable {

        @Override
        public void run() {
            for (Entry<Integer, MapleMap> map : mapFactory.getMaps().entrySet()) {
                map.getValue().respawn();
            }
        }
    }

    /*    public class doFishing implements Runnable {
    @Override 
    public void run(){ 
    for(String fisher : MapleFishing.getFishers()){ 
    MapleCharacter chr = getCharacterFromAllServers(fisher);
    if(chr != null && chr.getMapId() == 251000100){ 
    server.MapleFishing.doFishing(chr); 
    } 
    }
    }
    }*/
    public static MapleCharacter getCharacterFromAllServers(int id) {
        for (ChannelServer cserv_ : ChannelServer.getAllInstances()) {
            MapleCharacter ret = cserv_.getPlayerStorage().getCharacterById(id);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    public static MapleCharacter getCharacterFromAllServers(String name) {
        for (ChannelServer cserv_ : ChannelServer.getAllInstances()) {
            MapleCharacter ret = cserv_.getPlayerStorage().getCharacterByName(name);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    public static MapleClient getAccountFromAllServers(int id) {
        for (ChannelServer cserv_ : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv_.getPlayerStorage().getAllCharacters()) {
                if (chr.getClient().getAccID() == id) {
                    return chr.getClient();
                }
            }
        }
        return null;
    }

    public MapleSquad getMapleSquad(MapleSquadType type) {
        return mapleSquads.get(type);
    }

    public boolean addMapleSquad(MapleSquad squad, MapleSquadType type) {
        if (mapleSquads.get(type) == null) {
            mapleSquads.remove(type);
            mapleSquads.put(type, squad);
            return true;
        } else {
            return false;
        }
    }

    public Map<Integer, HiredMerchant> getHiredMerchants() {
        return hiredMerchants;
    }

    public void addHiredMerchant(int chrid, HiredMerchant hm) {
        hiredMerchants.put(chrid, hm);
    }

    public void removeHiredMerchant(int chrid) {
        hiredMerchants.remove(chrid);
    }
}