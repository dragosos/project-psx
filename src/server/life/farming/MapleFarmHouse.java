/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.life.farming;
import server.maps.MapleMapFactory;
import provider.MapleDataProviderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import server.maps.MapleMap;
import tools.FileoutputUtil;
import tools.DatabaseConnection;

/**
 *
 * @author FateJiki
 */
public class MapleFarmHouse extends MapleMap {
    // TODO: more upgrades for farms
    private double grabbingDistance = 35.0; // default :35.0  :)
    private int maxMonsters = 10;
    private List<MapleFarmMonster> farmMonsters = new ArrayList<MapleFarmMonster>();
    private MapleMapFactory instance = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")));
    
    
    private MapleFarmHouse(int accid, int channel){
        //public MapleMap(int mapid, int channel, int returnMapId, float monsterRate) {
        super(100000203, channel, 100000203, 1); // inheriting MapleMap <3
        Connection con = DatabaseConnection.getConnection();
        try{
            boolean hasFarm = false;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM farmhouses WHERE accid = ?");
            ps.setInt(1, accid);
            ResultSet rs = ps.executeQuery();
            // TODO: Support for multi-farms :) (Maybe? :o)
            if(rs.next()){ // Not a loop because you're supposed to only have ONE farm! May change in the future..
                hasFarm = true;
                grabbingDistance = rs.getDouble("grabbingDistance");
                maxMonsters = rs.getInt("maxMonsters");
                // Now loading monsters/animals
                ps = con.prepareStatement("SELECT * FROM farmmonsters WHERE farmid = ?");
                ps.setInt(1, accid); // one per account , yeah.
                rs = ps.executeQuery();
                if(hasFarm){
                MapleFarmMonster mfm = null;
                while(rs.next()){         
                    int farmid = rs.getInt("farmid");
                    long birthDate = Long.parseLong(rs.getString("birthdate"));
                    long deathDate = Long.parseLong(rs.getString("deathdate"));
                    int gender = rs.getInt("gender");
                    int mobid = rs.getInt("mobid");
                    mfm = new MapleFarmMonster(farmid, mobid, accid, deathDate, birthDate, gender, this);
                    farmMonsters.add(mfm);
                    mfm = null; // disposing
                }
                rs.close();
                ps.close();
                } 
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
         // blah
        }
    }
    
    
    public static MapleFarmHouse getFarm(int farmid, int accid, int channel){
        MapleFarmHouse ret;
        Connection con = DatabaseConnection.getConnection();
        try{
        PreparedStatement ps = con.prepareStatement("SELECT * FROM farmhouses where farmid = ? && accountid = ?");
        ps.setInt(1, farmid);
        ps.setInt(2, accid);
        ResultSet rs = ps.executeQuery();
        if(rs.next()){ // meaning there is indeed a farm
            rs.close();
            return new MapleFarmHouse(accid, channel);
        }

        } catch (SQLException e){
            
        } finally {
            return null;
        }
    }
    
    public static int getFarmID(int accid){
        Connection con = DatabaseConnection.getConnection();
        try{
            PreparedStatement ps = con.prepareStatement("SELECT * FROM farmhouses where accid = ?");
            ps.setInt(1, accid);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                int farmid = rs.getInt("id");
                rs.close();
                ps.close();
                return farmid;
            } else {
                return -1;
            }
        } catch (SQLException e){
            
        } finally {
            return -1;
        }
    }
    
    
    /**
     * FARM CREATION <BR>
     * returns true if farm has been created <br>
     * return false if farm has not been created (Either from an SQLException or b/c a farm already exists<br>
     * @param
     * @return
     */
    public static boolean createFarm(int accid){
        if(hasMapleFarm(accid)){ // TODO: Think about multi-farms support
            return false;
        } else {
            try{
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT into farmhouses (accid, grabbingDistance, maxMonsters) VALUES(?, ?, ?)");
            ps.setInt(1, accid);
            ps.setDouble(2, 10.0);
            ps.setInt(3, 8);
            ps.executeUpdate();
            ps.close();
            return true;
            } catch(SQLException e){
                FileoutputUtil.log(FileoutputUtil.System_Error, "Unable to create a farm for accid :" + accid + ".\n\n Error : \n" + e.getMessage(), null);
                return false;
            }
        }
        
    }
    
    public static boolean hasMapleFarm(int accid){
      try{
        boolean hasFarm;
        Connection con = DatabaseConnection.getConnection();
        
        PreparedStatement ps = con.prepareStatement("SELECT * FROM farmhouses WHERE accid = ?");
        ps.setInt(1, accid);
        hasFarm = ps.executeQuery().next();
        ps.close(); // disposing
        
        return hasFarm;
        } catch(SQLException e){
        } finally {
            return false;
        }
    }
    
    private String loadOwnersNames(){
        return "undone";
    }
    
}
