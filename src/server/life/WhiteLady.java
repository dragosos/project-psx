/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.life;
import server.maps.PlayerNPCs;
import java.sql.*;
import client.MapleCharacter;
import client.MapleSkinColor;
import client.MapleStat;
import net.channel.ChannelServer;
import server.TimerManager;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 *
 * @author FateJiki
 */
public class WhiteLady extends PlayerNPCs{
    int searched_cid;
    public static final String white_lady = "SELECT * FROM playernpcs WHERE isGhost = 1 && ghostName = 'The White Lady'";
    
    public WhiteLady(ResultSet rs, int trigger_id){
        super(rs);
        searched_cid = trigger_id;
            name = "";
            hair = 31425;
            face = 21811;
            skin = 9;
            try{
         PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM playernpcs_equip WHERE NpcId = ?");
         ResultSet rs2 = ps.executeQuery();
         while(rs2.next()){
             System.out.println("Found white bitch clothes");
             equips.put(rs.getByte("equippos"), rs.getInt("equipid"));
         }
            } catch (SQLException e){
                
            }
    }
    
    public void appear(){
        MapleCharacter chr = ChannelServer.getCharacterFromAllServers(searched_cid);
        
        this.RX0 = chr.getPosition().x + 50;
        this.RX1 = chr.getPosition().x - 50;
        this.CY = chr.getPosition().y;
        this.FH = chr.getMap().getFootholds().findBelow(chr.getPosition()).getId();
        npcId = 9901002;
        equips.put((byte)-5, 1042003);
        equips.put((byte)-7, 1072001);
        equips.put((byte)-105, 1051076);
        equips.put((byte)-107, 1072010);
        equips.put((byte)-109, 1102218);
        equips.put((byte)-108, 1082079);
        equips.put((byte)-101, 1082079);
        equips.put((byte)-1, 1002419);
        setPosition(new java.awt.Point(RX1 + 50, CY));
        chr.getMap().addMapObject(this);
        chr.getMap().broadcastMessage(MaplePacketCreator.spawnPlayerNPC(this));
        chr.getMap().broadcastMessage(MaplePacketCreator.getPlayerNPC(this));
        
    }
    
    public void RemoveFromMap(){
        MapleCharacter chr = ChannelServer.getCharacterFromAllServers(searched_cid);
        chr.changeMap(chr.getMapId());
    }
    
    public static ResultSet getRS(){
        ResultSet rs = null;
        try{
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(white_lady);
            rs = ps.executeQuery();
            ps.close();
        } catch (SQLException e){
            
        } finally {
            return rs;
        }
    }
    
    public boolean isChosenOne(int cid){
        return cid == searched_cid;
    }
    
    public void _do(int result, int cid){
        MapleCharacter chr = ChannelServer.getCharacterFromAllServers(cid);
        switch(result){
            case 1: // killing curse
                chr.setHp(0, false);
                chr.updateSingleStat(MapleStat.HP, 0);
                break;
            case 2: // Giving a new pet beacon
                chr.gainItem(1032033, (short)1, true);
                break;
            case 3:
                int[] SkillIDs = {21121003, 21121008, 2311003, 3221002, 3321002, 4101004};
                for(int i = 0; i < SkillIDs.length; i++){
                    chr.giveMaxBuff(SkillIDs[i]);
                }
                SkillIDs = null;
                break;
            case 4:
                chr.gainMeso(chr.getLevel() * 1000000 / 2, true);
                break;
            case 5:
                chr.gainExp(chr.getLevel() * 500000, true, false);
                break;
            case 6:
                int gain = chr.getLevel() * (int)(100 * Math.random()) + 1;
                chr.getCashShop().gainCash(4, gain);
                chr.dropMessage("The White Lady has given you " + gain + " Prepaid NX Points.");
                break;
            case 7:
                MapleSkinColor color = (byte)(2.0 * Math.random()) == (byte)1 ? MapleSkinColor.BLUE : MapleSkinColor.GREEN;
                chr.setSkinColor(color);
                chr.announce(MaplePacketCreator.getWhisper("The White Lady", 1, "Enjoy your new skin color, it is now " + color.name() + "!"));
            default:
                chr.setMaxHp(chr.getMaxHp() + (int)(chr.getLevel() * 10 * Math.random()));
                chr.setMaxMp(chr.getMaxMp() + (int)(chr.getLevel() * 15 * Math.random()));
                chr.updateSingleStat(MapleStat.MAXHP, chr.getMaxHp());
                chr.updateSingleStat(MapleStat.MAXMP, chr.getMaxMp());
                chr.announce(MaplePacketCreator.getWhisper("The White Lady", 1, "You now have more HP and MP. Good day."));
                break;
        }
        
        //RemoveFromMap();
    }
    
}
