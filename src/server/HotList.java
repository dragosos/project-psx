/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;
import tools.FileoutputUtil;
import net.channel.ChannelServer;
import client.MapleClient;
import java.sql.*;
import tools.DatabaseConnection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author FateJiki
 * @Use If anyone is suspected of hacking, they are put on this list for further review.
 */
public class HotList {
    private int cid;
    private int accid;
    private String c_name;
    private String acc_name;
    public boolean isNull;
    private Map<String, Object> evidence = new HashMap<String, Object>();
    
    public HotList(int charid){
        try{
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM hotlist WHERE playerid = ?");
        ps.setInt(1, charid);
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            cid = charid;
            accid = rs.getInt("accountid");
            acc_name = rs.getString("accountName");
            c_name = rs.getString("playername");
            isNull = false;
        } else {
            isNull = true; // meaning he is NOT on the hotlist
        }
        rs.close();
        ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public void addEvidence(String desc, Object value){
        evidence.put(desc, value);
    }
    
    public void initiateLogging(){
        List<String> strings = new ArrayList<String>();
        List<Object> off_values = new ArrayList<Object>();
        StringBuilder sb = new StringBuilder();
            sb.append("Here is the total log\n");
            sb.append("-----------------------\n");
            
        for(String str : evidence.keySet()){
            strings.add(str);
        }
        for(Object obj : evidence.values()){
            off_values.add(obj);
        }
        for(int i = 0; i < strings.size(); i++){
            sb.append("" + strings.get(i) + ":" + off_values.get(i) + "\n");
        }
      //  FileoutputUtil.logHotList(c_name);
        strings.clear();
        off_values.clear();
        sb = null;
        off_values = null;
    }
    
    public static void enterHotList(MapleClient chr){
        try{
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT into hotlist(playerid, playername, accountid, accountName VALUES(?, ?, ?, ?)");
            ps.setInt(1, chr.getPlayer().getId());
            ps.setString(2, chr.getPlayer().getName());
            ps.setInt(3, chr.getAccID());
            ps.setString(4, chr.getAccountName());
            ps.execute();
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
