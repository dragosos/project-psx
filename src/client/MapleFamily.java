/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import server.TimerManager;
import tools.MaplePacketCreator;

/**
 *
 * @author Rahul
 */
public class MapleFamily {

    public static void cacheList(int familyId, List<MapleFamilyCharacter> l) {

    }

    public static void cacheChar(int familyId, MapleFamilyCharacter fchr) {

    }

    public static List<MapleFamilyCharacter> getCharacterValues(int familyId) {
        return null;
    }

    public static void uncacheChar(int familyId, MapleFamilyCharacter toRemove) {

    }

    public static void recacheChar(int familyId, MapleFamilyCharacter toCache) {

    }

    public static boolean characterValuesExist(int familyId) {
        return false;
    }

    public static void cacheBuff(int cid, Runnable r, byte time) {

    }

    public static boolean buffExists(int cid) {
        return false;
    }

    public static void cancelBuff(MapleClient c, boolean alreadyExists) {
        
    }
}
