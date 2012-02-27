/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;
import server.maps.MapleMapFactory;
import java.util.Map;
import java.util.HashMap;
import provider.MapleDataProviderFactory;
import java.io.File;
import tools.Pair;
import client.GameMode;
import client.MapleCharacter;
import net.channel.ChannelServer;
/**
 *
 * @author FateJiki
 */
public class GameModeManager {
    //hugeass variable haha
   /* private static Map<Pair<Integer, Integer>, Pair<GameMode, MapleMapFactory>> gameModes = new HashMap<Pair<Integer, Integer>, Pair<GameMode, MapleMapFactory>>();
    private int channel;
    private int maxSessions;
    
    public GameModeManager(int _channel, int _maxSessions){
        channel = _channel;
        maxSessions = _maxSessions;
    }
    //
    private MapleMapFactory registerGameModeSession(int destructionTime, int playercap, GameMode mode){
        MapleMapFactory GMFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")));
        GMFactory.setMode(mode);
        GMFactory.setChannel(getChannel());
        Pair p1 = new Pair(playercap, destructionTime);
        Pair p2 = new Pair(mode, GMFactory);
        gameModes.put(p1, p2);
        if(destructionTime > 0){
        TimerManager.getInstance().register(new Runnable(){
        @Override
        public void run(){
            
        }
    }, destructionTime * 60 * 1000); // minutes !
        }
        System.out.println("Initialized Game Mode Area : " + mode.name());
        return GMFactory;
    }
    
    // TODO: conditions, missions! :O whatev
    public void grantEntry(int charid, GameMode mode){
        MapleCharacter chr = ChannelServer.getCharacterFromAllServers(charid);
        chr.changeGMode(mode);
        getMapFactoryByMode(mode).getMap(chr.getMapId()); // tadaaa
        chr.dropMessage("You are now on mode : " + chr.getMap().getGMode().name());
    }
    
    public MapleMapFactory getMapFactoryByMode(GameMode mode){
        MapleMapFactory mmf = null;
        for(Pair p : gameModes.values()){
            MapleMapFactory factory = (MapleMapFactory)p.getLeft();
            if(factory.getMode().equals(mode)){
                return factory;
            }
        }
        // if all else fails, returns null, as in no map factory available..
        if(mmf == null){
           return registerGameModeSession(-1, Integer.MAX_VALUE, mode);
        } else {
            return null;
        }
    }
    
    public int getChannel(){
        return channel;
    }
    
    public int getMaxSessions(){
        return maxSessions;
    }
    */
}