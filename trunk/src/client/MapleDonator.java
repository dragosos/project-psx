/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;
import java.sql.*;
import net.channel.ChannelServer;
import tools.DatabaseConnection;
import java.util.Map;
import java.util.HashMap;
/**
 *
 * @author FateJiki
 */
public class MapleDonator {
    String donatorName; // acc id
    int accid;
    int donatorpoints;
    long expiredate;
    boolean active;
    
    
    public MapleDonator(String accountName, int _accid){
        donatorName = accountName;
        accid = _accid;
        Connection con = DatabaseConnection.getConnection();
        try{
            PreparedStatement ps = con.prepareStatement("SELECT * FROM mapledonator WHERE donatorname = ?");
            ps.setString(1, donatorName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                donatorpoints = rs.getInt("donatorpoints");
                expiredate = rs.getLong("expiredate");
                active = rs.getByte("isactive") == (byte)1;
            } else {
                donatorpoints = 0;
                expiredate = 0;
                active = false;
            }
            rs.close();
            ps.close();
            ps = null;
            con = null;
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    

   
    
    
    
    public boolean isActive(){
        return active;
    }
    public int getDP(){
        return donatorpoints;
    }
    public void useDP(int amount){
        donatorpoints -= amount;
        save();
    }
    
    public void reloadDonator(){
        save();
        Connection con = DatabaseConnection.getConnection();
        try{
            PreparedStatement ps = con.prepareStatement("SELECT * FROM mapledonator WHERE donatorname = ?");
            ps.setString(1, donatorName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                donatorpoints = rs.getInt("donatorpoints");
                expiredate = rs.getLong("expiredate");
                active = rs.getByte("isactive") == (byte)1;
            }
            rs.close();
            ps.close();
            ps = null;
            con = null;
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public boolean hasExpireDatePassed(){
        if(!active || active){
            return false;
        }
        if((System.currentTimeMillis() - expiredate) <= (1000 * 60 * 60)){ // one hour
            MapleCharacter chr = ChannelServer.getAccountFromAllServers(accid).getPlayer();
            chr.yellowMessage("Your donator status has now expired. All your donator perks have been disabled. To re-acquire your perks, go see the Donator NPC with your remaining Donator Points.");
            this.active = false;
            save();
            return true;
        } else {
            return false;
        }
    }
    
    public void save(){
        Connection con = DatabaseConnection.getConnection();
        try{
            PreparedStatement ps = con.prepareStatement("UPDATE mapledonator SET donatorpoints = ?, isactive = ?, expiredate = ? WHERE donatorname = ?");
            ps.setInt(1, this.donatorpoints);
            ps.setInt(2, active ? 1 : 0);
            ps.setLong(3, expiredate);
            ps.setString(4, donatorName);
            ps.executeUpdate();
            ps.close();
            /*ps = con.prepareStatement("DELETE from mapledonatorperks WHERE donatorname = ?");
            ps.setString(1, donatorName);
            ps.executeUpdate();
            ps.close();
            for(MapleDonatorPerkEntry mdpk : perks.values()){
                ps = con.prepareStatement("INSERT into mapledonatorperks(perkid, activated, donatorname) VALUES(?, ?, ?)");
                ps.setInt(1, mdpk.id);
                ps.setInt(2, 1);
                ps.setString(3, donatorName);
                ps.executeUpdate();
                ps.close();
            }*/
            ps = null;
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public MapleCharacter getPlayer(){
        return ChannelServer.getAccountFromAllServers(accid).getPlayer();
    }
    
    
    
    
    
    
    // STATICS
    public static void sendDonatorAd(MapleCharacter chr){
        if(!seenAd(chr.getClient().getAccountName())){
            chr.openNpc(9270047, "donator_intro");
            setSeenAd(chr.getClient().getAccountName());
        }
    }
    public static void setSeenAd(String accname){
        Connection con = DatabaseConnection.getConnection();
        try{
                PreparedStatement ps = con.prepareStatement("INSERT into mapledonator(donatorpoints, donatorname, expiredate, isactive) VALUES(?, ?, ?, ?)");
                ps.setInt(1, 0);
                ps.setString(2, accname);
                ps.setLong(3, (long)0);
                ps.setByte(4, (byte)0);
                ps.executeUpdate();
            ps.close();
            ps = null;
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static boolean seenAd(String accname){
        boolean seen = false;
        Connection con = DatabaseConnection.getConnection();
        try{
            PreparedStatement ps = con.prepareStatement("SELECT * FROM mapledonator WHERE donatorname = ?");
            ps.setString(1, accname);
            ResultSet rs = ps.executeQuery();
            seen = rs.next();
            rs.close();
            ps.close();
            ps = null;
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        return seen;
    }
    
    public static boolean isDonator(String accname){
        boolean exists = false;
        Connection con = DatabaseConnection.getConnection();
        try{
            PreparedStatement ps = con.prepareStatement("SELECT * FROM mapledonator WHERE donatorname = ?");
            ps.setString(1, accname);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                exists = rs.getByte("isactive") == (byte)1;
            }
            rs.close();
            ps.close();
            ps = null;
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        return exists;
    }
}
