/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.channel.ChannelServer;
import tools.DatabaseConnection;

/**
 *
 * @author Rahul
 */
public class MapleFamilyCharacter {
    private int cid, familyId, seniorId, currentRep, totalRep, todaysRep, junior1Id, junior2Id;
    private short jobId, level, pos;
    private byte dayUsed = -1;
    private String name, familyName, seniorName, notice, bonusesToday = "00000000000";
    private MapleCharacter chr;

    public MapleFamilyCharacter(MapleCharacter chr) {
        this.cid = chr.getId();
        this.chr = chr;
        this.name = chr.getName();
        this.jobId = (short) chr.getJob().getId();
        this.level = (short) chr.getLevel();
        loadFamilyInfo(chr.getId());
        if (this.seniorId == 0) {
            this.seniorName = "";
        }
        if (this.notice == null) {
            this.notice = "";
        }
    }

    public MapleFamilyCharacter(int cid) {//offline loading
        this.cid = cid;
        this.chr = null;
        loadCharFromDB(cid);
        loadFamilyInfo(cid);
        if (this.seniorId == 0) {
            this.seniorName = "";
        }
        if (this.notice == null) {
            this.notice = "";
        }
    }

    public final void loadCharFromDB(int cid) {//Pedigree
        PreparedStatement ps = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM characters WHERE id = ?");
            ps.setInt(1, cid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.name = rs.getString("name");
                this.jobId = rs.getShort("job");
                this.level = rs.getShort("level");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public final void loadFamilyInfo(int cid) {
        PreparedStatement ps = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM family_character WHERE cid = ?");
            ps.setInt(1, cid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.familyId = rs.getInt("familyId") == 0 ? cid : rs.getInt("familyId");
                this.seniorId = rs.getInt("seniorId");
                this.seniorName = rs.getString("seniorName");
                this.familyName = rs.getString("familyName");
                this.currentRep = rs.getInt("currentRep");
                this.totalRep = rs.getInt("totalRep");
                this.todaysRep = rs.getInt("todaysRep");
                this.junior1Id = rs.getInt("junior1Id");
                this.junior2Id = rs.getInt("junior2Id");
                this.notice = rs.getString("notice");
                this.pos = rs.getShort("pos");
                this.bonusesToday = rs.getString("usedBonuses");
                this.dayUsed = rs.getByte("dayUsed");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void saveToDB(boolean update) {
        PreparedStatement ps = null;
        try {
            if (update) {
                ps = DatabaseConnection.getConnection().prepareStatement("UPDATE family_character SET familyId = ?, familyName = ?, pos = ?, seniorId = ?, seniorName = ?, currentRep = ?, totalRep = ?, todaysRep = ?, junior1Id = ?, junior2Id = ?, notice = ?, usedBonuses = ?, dayUsed = ? WHERE cid = " + cid);
                ps.setInt(1, familyId);
                ps.setString(2, familyName);
                ps.setInt(3, pos);
                ps.setInt(4, seniorId);
                ps.setString(5, seniorName);
                ps.setInt(6, currentRep);
                ps.setInt(7, totalRep);
                ps.setInt(8, todaysRep);
                ps.setInt(9, junior1Id);
                ps.setInt(10, junior2Id);
                ps.setString(11, notice);
                ps.setString(12, bonusesToday);
                ps.setByte(13, dayUsed);
                ps.executeUpdate();
                ps.close();
            } else {
                ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO family_character (`cid`, `name`, `familyId`, `familyName`, pos, `seniorId`, `seniorName`, `currentRep`, `totalRep`, `todaysRep`, `junior1Id`, `junior2Id`, `notice`, `usedBonuses`, `dayUsed`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setInt(1, cid);
                ps.setString(2, name);
                ps.setInt(3, familyId);
                ps.setString(4, familyName);
                ps.setInt(5, pos);
                ps.setInt(6, seniorId);
                ps.setString(7, seniorName);
                ps.setInt(8, currentRep);
                ps.setInt(9, totalRep);
                ps.setInt(10, todaysRep);
                ps.setInt(11, junior1Id);
                ps.setInt(12, junior2Id);
                ps.setString(13, notice);
                ps.setString(14, bonusesToday);
                ps.setByte(15, dayUsed);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteCharFromDB(int id) {
         PreparedStatement ps = null;
         try {
             ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM family_character WHERE cid = " + id);
             ps.executeUpdate();
             ps.close();
         } catch (SQLException e) {
             System.out.println("Exception caught in deleting Family Character:");
             e.printStackTrace();
         }
     }

    public static int getTotalSize(int familyId) throws SQLException {
       return 0;
    }

    public static List<MapleFamilyCharacter> getAllMembers(int familyId) {
        if (MapleFamily.characterValuesExist(familyId)) {
            return MapleFamily.getCharacterValues(familyId);
        }
        List<MapleFamilyCharacter> ret = new LinkedList<MapleFamilyCharacter>();
        PreparedStatement ps = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM family_character WHERE familyId = ?");
            ps.setInt(1, familyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MapleFamilyCharacter toAdd = new MapleFamilyCharacter(rs.getInt("cid"));
                ret.add(toAdd);
            }
            rs.close();
            ps.close();
            MapleFamily.cacheList(familyId, ret);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static Map<MapleFamilyCharacter, Integer> getMembersByPos(int familyId, int pos) {
         Map ret = new HashMap<MapleFamilyCharacter, Integer>();//could've been list but cache messes things up :(
         for (MapleFamilyCharacter fchrs : getAllMembers(familyId)) {
             if (fchrs.getPos() == pos && !ret.containsValue(fchrs.getCid())) {//stupid check for cache..
                 ret.put(fchrs, pos);
             }
         }
         return ret;
     }

     public static MapleFamilyCharacter getMemberById(int familyId, int id) {
         for (MapleFamilyCharacter fchr : getAllMembers(familyId)) {
             if (fchr.getCid() == id) {
                 return fchr;
             }
         }
         return null;
     }

     public static int getMembersOnline(int familyId) {
        
         return 0;
     }

     public static MapleFamilyCharacter getSenior(int familyid, int juniorId) {
         for (MapleFamilyCharacter fchr : getAllMembers(familyid)) {
             if (fchr.getJunior1Id() == juniorId || fchr.getJunior2Id() == juniorId) {
                 return fchr;
             }
         }
         return null;
     }

    public int getCid() {
        return cid;
    }

    public MapleCharacter getPlayer() {
        return chr;
    }

    public String getName() {
        return name;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getSeniorName() {
        return seniorName;
    }

     public int getFamilyId() {
         return familyId;
     }

     public int getSeniorId() {
         return seniorId;
     }

     public int getCurrentRep() {
         return currentRep;
     }

     public int getTotalRep() {
         return totalRep;
     }

     public int getTodaysRep() {
         return todaysRep;
     }

     public int getJunior1Id() {
         return junior1Id;
     }

     public int getJunior2Id() {
         return junior2Id;
     }

     public int getJobId() {
         return jobId;
     }

     public int getLevel() {
         return level;
     }

     public void setCid(int s) {
         cid = s;
     }

     public void setFamilyId(int s) {
         familyId = s;
     }

     public void setSeniorId(int s) {
         seniorId = s;
     }

     public void setCurrentRep(int s) {
         currentRep = s;
     }

     public void setTotalRep(int s) {
         totalRep = s;
     }

     public void setTodaysRep(int s) {
         todaysRep = s;
     }

     public void setJunior1Id(int s) {
         junior1Id = s;
     }

     public void setJunior2Id(int s) {
         junior2Id = s;
     }

     public void addCurrentRep(int s) {
         currentRep += s;
     }

     public void addTotalRep(int s) {
         totalRep += s;
     }

     public void addTodaysRep(int s) {
         todaysRep += s;
     }

     public int getJuniorSize() {
         int i = 0;
         if (junior1Id > 0)  {
             i += 1;
         }
         if (junior2Id > 0) {
             i += 1;
         }
         return i;
     }

     public void setFamilyName(String s) {
         familyName = s;
     }

     public void setName(String s) {
         name = s;
     }

     public void setSeniorName(String s) {
         seniorName = s;
     }

     public String getFamilyNotice() {
         return notice;
     }

     public void setFamilyNotice(String to) {
         notice = to;
     }

     public int getPos() {
         return pos;
     }

     public void setPos(int newPos) {
         pos = (short)newPos;
     }

     public boolean hasNoFamily() {
         return seniorId == 0 && junior1Id == 0 && junior2Id == 0;
     }

     public boolean hasFamily() {
         return !hasNoFamily();
     }

     public boolean isBonusUsed(byte index) {
         if (bonusesToday.charAt(index) == '0') {
             return false;
         }
         return true;
     }

     public void setBonusesToday(byte index) {
        bonusesToday = bonusesToday.substring(0, index) + '1' + bonusesToday.substring(index+1);
    }

    public void resetBonusesUsed() {
        bonusesToday = "00000000000";
    }

    public boolean bonusesContains(byte index) {
        return bonusesToday.charAt(index) == '1';
    }

     public String getUsedBonuses() {
         return bonusesToday;
     }

     public void setDayUsed(byte day) {
         this.dayUsed = day;
     }

     public byte getDayUsed() {
         return dayUsed;
     }

     public void setLevel(int s) {
         level = (short)s;
     }

     public void setJob(int s) {
         jobId = (short)s;
     }

     @Override
     public boolean equals(Object o) {
         if (o.getClass() != MapleFamilyCharacter.class) {
             return false;
         }
         if (this.getCid() == ((MapleFamilyCharacter)o).getCid()) {
             return true;
         }
         return false;
     }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.cid;
        return hash;
    }
}
