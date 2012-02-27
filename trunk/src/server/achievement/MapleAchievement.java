/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.achievement;
import client.MapleCharacter;
import net.channel.ChannelServer;
import java.sql.*;
import tools.DatabaseConnection;
import java.util.EnumMap;
import server.achievement.MapleAchievementLibrary;

/**
 *
 * @author FateJiki
 */
public class MapleAchievement {
    public int id;
    public int cid;
    public int rarety = 0;
    public int prizeid = 0;
    public int NXPrize = 0;
    public int EXPPrize = 0;
    public int MesoPrize = 0;
    public String achievementName;
    
    
    public MapleAchievement(int _id, int _cid, int _rarety, int _prizeid){
        id = _id;
        cid = _cid;
        rarety = _rarety;
        prizeid = _prizeid;
      //  gameModeReq = GameModeRequirements.ALL;
    }
    
    public MapleAchievement(int _id, int _cid, int _rarety, int _NXPrize, int _EXPPrize, int _MesoPrize, String _name){
        id = _id;
        cid = _cid;
        rarety = _rarety;
        NXPrize = _NXPrize;
        EXPPrize = _EXPPrize;
        MesoPrize = _MesoPrize;
        // if null, it'll be automatically set to ALL
      //  gameModeReq = _required == null ? GameModeRequirements.ALL : _required;
        achievementName = _name;
    }
    
    public MapleAchievement(int _cid, MapleAchievementLibrary preset){
        cid = _cid;
        id = preset.getId();
        rarety = preset.getRarety();
        NXPrize = preset.getNXPrize();
        EXPPrize = preset.getEXPPrize();
        MesoPrize = preset.getMesoPrize();
    }
    
    
    public String getName(){
        return achievementName;
    }
    public int getId(){
        return id;
    }
    
    public int getCharacterID(){
        return cid;
    }
    
    public int getRarety(){
        return rarety;
    }
    
    public int getPrizeID(){
        return prizeid;
    }
    
    public int getNXPrize(){
        return NXPrize;
    }
    
    public int getEXPPrize(){
        return EXPPrize;
    }
    
    public int getMesoPrize(){
        return MesoPrize;
    }
    
    public MapleCharacter getCharacter(){
        return ChannelServer.getCharacterFromAllServers(cid);
    }
    
    public static EnumMap<MapleAchievementLibrary, MapleAchievement> loadAllAchievements(int cid){
        EnumMap<MapleAchievementLibrary, MapleAchievement> achievements = new EnumMap<MapleAchievementLibrary, MapleAchievement>(MapleAchievementLibrary.class);
        Connection con = DatabaseConnection.getConnection();
        try{
        PreparedStatement ps = con.prepareStatement("SELECT * FROM MapleAchievements WHERE cid = ?");
        ps.setInt(1, cid);
        ResultSet rs = ps.executeQuery();
        MapleAchievement mapleAchievement = new MapleAchievement(0, 0, 0, 0);
        while(rs.next()){
            mapleAchievement = new MapleAchievement(rs.getInt("aid"), rs.getInt("cid"), rs.getInt("rarety"), rs.getInt("prizeid"));
            achievements.put(MapleAchievementLibrary.getById(mapleAchievement.id), mapleAchievement);
            mapleAchievement = null; // dispose :o
        }
        rs.close();
        ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
           return achievements;
        }
    }
    
    private void saveAchievement(MapleAchievement mA){
        Connection con = DatabaseConnection.getConnection();
        try{
        PreparedStatement ps = con.prepareStatement("INSERT into MapleAchievements (aid, cid, rarety, prizeid) VALUES(?, ?, ?, ?)");
        ps.setInt(1, mA.getId());
        ps.setInt(2, mA.getCharacterID());
        ps.setInt(3, mA.getRarety());
        ps.setInt(4, mA.getPrizeID());
        ps.executeUpdate();
        mA.getCharacter().addAchievementToStack(mA);
        ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public static boolean isAchievementAccomplished(int aid, int cid){
        boolean accomplished = false;
        Connection con = DatabaseConnection.getConnection();
        try{
            PreparedStatement ps = con.prepareStatement("SELECT * FROM MapleAchievements WHERE cid = ? && aid = ?");
            ps.setInt(1, cid);
            ps.setInt(2, aid);
            ResultSet rs = ps.executeQuery();
            accomplished = rs.next();
            rs.close();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        } finally {
            return accomplished;
        }
    }
    
    public void completeMapleAchievement(MapleAchievement completed, String nameOfAchievement){
        if(!isAchievementAccomplished(completed.getId(), completed.getCharacterID())){
            MapleCharacter chr = ChannelServer.getCharacterFromAllServers(completed.getCharacterID());
            int _EXPPrize = completed.getEXPPrize();
            int _NXCash = completed.getNXPrize();
            int _mesoPrize = completed.getMesoPrize();
            String message = "You have completed an achievement! - '" + "" + nameOfAchievement + "" + "'. \r\n\r\n As a result, you have gained ";
            if(_EXPPrize > 0){
                message += "\t\n" + _EXPPrize + "EXP Points";
                chr.gainExp(_EXPPrize, true, false);
            } if(_NXCash > 0){
                message += "\r\n," + _NXCash + " NX Cash";
                chr.getCashShop().gainCash(2, _NXCash);
            } if(_mesoPrize > 0){
                message += ",\r\n" + _mesoPrize + " mesos";
                chr.gainMeso(_mesoPrize, true, true, true);
            }
            saveAchievement(completed);
          //  chr.sendNote(chr.getName(), message, (byte)0);
           // chr.showNote();pll
            chr.dropMessage(5, message);
        
        } else {
            // bleh bleh
        }
    }
    
}
    
    
    

