/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.clones;

/**
 *
 * @author FateJiki
 */
public enum MapleCelebrityLibrary {
    
    JOHN(1, (byte)10, 30816, 20305, (byte)1, 305, 1050100,1302024);
  //  SELENA(2, (byte)10, )
    
    final int id;
    final byte skinColor;
    final int hairid;
    final int faceid;
    final byte gender; // 0 = male; 1 = female
    final int walkSpeed;
    final int overallid; // pos : -5 when equips
    final int weaponid;// pos : -11 when equip
    private MapleCelebrityLibrary(int _id, byte _skinColor, int _hairid, int _faceid, byte _gender, int _walkspeed, int overallid_, int weaponid_){
        id = _id;
        skinColor = _skinColor;
        hairid = _hairid;
        faceid = _faceid;
        gender = _gender;
        walkSpeed = _walkspeed;
        overallid = overallid_;
        weaponid = weaponid_;
    }
}
