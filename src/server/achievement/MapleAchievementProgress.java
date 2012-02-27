/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.achievement;
import net.channel.ChannelServer;
import client.MapleCharacter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import tools.DatabaseConnection;
import java.sql.ResultSet;

/**
 *
 * @author FateJiki
 */
public class MapleAchievementProgress {
    private final int cid;
    private int metersWalked;
    private int bossesKilled;
    private int monstersKilled;
    
    
    public MapleAchievementProgress(int _cid){
        cid = _cid;
        Connection con = DatabaseConnection.getConnection();
        try {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM MapleAchievementProgress where cid = ?");
        ps.setInt(1, cid);
        ResultSet rs = ps.executeQuery();
        if(rs.next()){ // meaning it exists :O
            metersWalked = rs.getInt("metersWalked");
            bossesKilled = rs.getInt("bossesKilled");
            monstersKilled = rs.getInt("monstersKilled");
            
        } else { // going to create a row for the new character f3.
            ps = con.prepareStatement("INSERT INTO MapleAchievementProgress (cid) VALUES (?)");
            ps.setInt(1, cid);
            ps.executeUpdate();
        }
        rs.close();
        ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            // dunno
        }
    }
    
    public void saveAchievementProgress(){
        Connection con = DatabaseConnection.getConnection();
        try{
            PreparedStatement ps = con.prepareStatement("UPDATE MapleAchievementProgress SET metersWalked = ?, bossesKilled = ?, monstersKilled = ? WHERE cid = ?");
            ps.setInt(1, metersWalked);
            ps.setInt(2, bossesKilled);
            ps.setInt(3, monstersKilled);
            ps.setInt(4, cid);
            ps.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public int getMetersWalked(){
        return metersWalked;
    }
    
    public int getBossesKilled(){
        return bossesKilled;
    }
    
    public int getMonstersKilled(){
        return monstersKilled;
    }
    
    public int getCharacterId(){
        return cid;
    }
    
    public void addMetersWalked(int add){
        metersWalked += add;
        switch(metersWalked){
            case 12874752 :// 8000 miles XD
                    getPlayer().completeAchievement(MapleAchievementLibrary.WALKED_AROUND_THE_WORLD.getId());
                    break;
            case 4634910: //
                getPlayer().completeAchievement(MapleAchievementLibrary.WALKED_AROUND_THE_COUNTRY.getId());
                break;
        }
    }
    
    public void addBossesKilled(int add){
        bossesKilled += add;
        switch(monstersKilled){
            case 100:
                getPlayer().completeAchievement(MapleAchievementLibrary.KILLED_100_BOSSES.getId());
                break;
            case 1000:
                getPlayer().completeAchievement(MapleAchievementLibrary.KILLED_1000_BOSSES.getId());
                break;
        }
    }
    
    public void addMonstersKilled(int add){
        monstersKilled += add;
        switch(monstersKilled){
            case 100:
                getPlayer().completeAchievement(MapleAchievementLibrary.KILLED_100_MONSTERS.getId());
                break;
            case 1000:
                getPlayer().completeAchievement(MapleAchievementLibrary.KILLED_1000_MONSTERS.getId());
                break;
            case 10000:
                getPlayer().completeAchievement(MapleAchievementLibrary.KILLED_10000_MONSTERS.getId());
                break;
        }
    }
    
    public MapleCharacter getPlayer(){
        return ChannelServer.getCharacterFromAllServers(cid);
    }
    
    
}
