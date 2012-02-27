/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package server.life;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.*;

import server.CashShop;
import server.MapleItemInformationProvider;
import tools.DatabaseConnection;
import java.util.concurrent.ConcurrentHashMap;

public class MapleMonsterInformationProvider {
// Author : LightPepsi
    private static MapleMonsterInformationProvider instance = new MapleMonsterInformationProvider();
    private final Map<Integer, List<MonsterDropEntry>> drops = new HashMap<Integer, List<MonsterDropEntry>>();
    private final List<MonsterGlobalDropEntry> globaldrops = new ArrayList<MonsterGlobalDropEntry>();

    protected MapleMonsterInformationProvider() {
        retrieveGlobal();
    }

    public static final MapleMonsterInformationProvider getInstance() {
        return instance;
    }

    public final List<MonsterGlobalDropEntry> getGlobalDrop() {
        return globaldrops;
    }
    
    public static void reInitializeMMIP(){
        instance = new MapleMonsterInformationProvider();
    }
    
    private void addCustomList(){
        List<tools.Pair<Integer, String>> allItems = MapleItemInformationProvider.getInstance().getAllItems();
        for(tools.Pair<Integer, String> itemsPair : allItems){
            String name = itemsPair.getRight();
            boolean globaldropsDec = false;
            int chance = 100;
            if(name.toLowerCase().contains("vip") || name.toLowerCase().contains("katara")){
                globaldropsDec = true;
                chance = 4;
            } else if(name.toLowerCase().contains("chair")){
                globaldropsDec = true;
                chance = 48;
            } else if(name.toLowerCase().contains("cushion")){
                globaldropsDec = true;
                chance = 48;
            } else if(name.toLowerCase().contains("maple")){ // maple weapons
                globaldropsDec = true;
                chance = 23;
            } else if(name.toLowerCase().contains("tube")){
                globaldropsDec = true;
                chance = 20;
            } else if(name.toLowerCase().contains("timeless")){
                globaldropsDec = true;
                chance = 1;
            } else if(name.toLowerCase().contains("reverse")){
                globaldropsDec = true;
                chance = 2;
            } else if(name.toLowerCase().contains("mask")){
                globaldropsDec = true;
                chance = 20;
            } else if(name.toLowerCase().contains("unwelcome guest")){
                globaldropsDec = true;
                chance = 5;
            } else if(name.toLowerCase().contains("elemental")){
                globaldropsDec = true;
                chance = 6;
            } else if(name.toLowerCase().contains("miracle cube")){
                globaldropsDec = true;
                chance = 400;
            } else if(name.toLowerCase().contains("potential") && !name.toLowerCase().contains("coupon")){
                globaldropsDec = true;
                chance = 1234;
            }
            
                if(globaldropsDec && CashShop.CashItemFactory.getItem(itemsPair.getLeft()) == null && !name.toLowerCase().contains("coupon")){
                   // System.out.println("Adding " + Integer.parseInt(itemsPair.getLeft().toString()) + " with chance of " + chance);
                    globaldrops.add(
                    new MonsterGlobalDropEntry(
                            Integer.parseInt(itemsPair.getLeft().toString()),
                            chance,
                            -1,
                            (byte)0,
                            1,
                            1,
                            (short)0
                            )
                    );
                } else {

                }
        }
    }
    
    private void addBossDrops(List<MonsterDropEntry> ret, BossType type){
        List<tools.Pair<Integer, String>> allItems = MapleItemInformationProvider.getInstance().getAllItems();
        for(tools.Pair<Integer, String> pairs : allItems){
            String name = pairs.getRight();
            int itemid = Integer.parseInt(pairs.getLeft().toString());
            int chance = 1000;
            boolean add = false;
            if(name.toLowerCase().contains("von leon") && type.equals(BossType.VONLEON)){
                add = true;
                chance = 19999;
            } else if(name.toLowerCase().contains("balrog's") && type.equals(BossType.BALROG) && !name.toLowerCase().contains("scroll")){ // lolbalrog :)
                add = true;
                chance = 12499;
            } else if(name.toLowerCase().contains("scarlion") && type.equals(BossType.SCARLION)){
                add = true;
                chance = 199999;
            } else if(name.toLowerCase().contains("targa") && type.equals(BossType.TARGA)){
                add = true;
                chance = 199999;
            }
            /*else if(name.toLowerCase().contains("marbas")){
                add = true;
                chance = 199999;
            } else if(name.toLowerCase().contains("valefor")){
                add = true;
                chance = 199999;
            }*/
            if(add){
            ret.add(new MonsterDropEntry(itemid, chance, 1, 1, (short)0));
            }
        }
    }

    private void retrieveGlobal() {
        if(globaldrops.isEmpty()){
            addCustomList();
        }
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM drop_data_global WHERE chance > 0");
            rs = ps.executeQuery();

            while (rs.next()) {
            globaldrops.add(
                new MonsterGlobalDropEntry(
                rs.getInt("itemid"),
                rs.getInt("chance"),
                rs.getInt("continent"),
                rs.getByte("dropType"),
                rs.getInt("minimum_quantity"),
                rs.getInt("maximum_quantity"),
                rs.getShort("questid")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving drop" + e);
        } finally {
            try {
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
            } catch (SQLException ignore) {
            }
        }
    }

    public final List<MonsterDropEntry> retrieveDrop(final int monsterId) {
    if (drops.containsKey(monsterId)) {
        return drops.get(monsterId);
    }
    final List<MonsterDropEntry> ret = new LinkedList<MonsterDropEntry>();
    switch(monsterId){
        case 8840000:
            addBossDrops(ret, BossType.VONLEON);
            break;
        case 8830000:
            addBossDrops(ret, BossType.BALROG);
            break;
        case 9420549:
            addBossDrops(ret, BossType.SCARLION);
            break;
        case 9420544:
            addBossDrops(ret, BossType.TARGA);
            break;
        default:
            // nothin' :)
            break;
    }
    
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
        ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM drop_data WHERE dropperid = ?");
        ps.setInt(1, monsterId);
        rs = ps.executeQuery();

        while (rs.next()) {
        ret.add(
            new MonsterDropEntry(
            rs.getInt("itemid"),
            rs.getInt("chance"),
            rs.getInt("minimum_quantity"),
            rs.getInt("maximum_quantity"),
            rs.getShort("questid")));
        }
    } catch (SQLException e) {
        return ret;
    } finally {
        try {
        if (ps != null) {
            ps.close();
        }
        if (rs != null) {
            rs.close();
        }
        } catch (SQLException ignore) {
        return ret;
        }
    }
    drops.put(monsterId, ret);
    return ret;
    }

    public final void clearDrops() {
    drops.clear();
    globaldrops.clear();
    retrieveGlobal();
    }
}