/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation version 3 as published by
the Free Software Foundation. You may not use, modify or distribute
this program under any other version of the GNU Affero General Public
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scripting.npc;

import client.Equip;
import client.IItem;
import client.ISkill;
import client.ItemFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import constants.ExpTable;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import client.MapleSkinColor;
import client.MapleStat;
import client.SkillFactory;
import tools.Randomizer;
import java.io.File;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import net.channel.ChannelServer;
import tools.DatabaseConnection;
import net.world.MapleParty;
import net.world.guild.MapleAlliance;
import net.world.guild.MapleGuild;
import net.world.remote.WorldChannelInterface;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import scripting.AbstractPlayerInteraction;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleSquad;
import server.MapleSquadType;
import server.MapleStatEffect;
import server.events.MapleEvent;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.pqs.*;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;

/**
 *
 * @author Matze
 */
public class NPCConversationManager extends AbstractPlayerInteraction {

    private int npc;
    private String getText;

    public NPCConversationManager(MapleClient c, int npc) {
        super(c);
        this.npc = npc;
    }

    public int getNpc() {
        return npc;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(this);
    }

    public void sendNext(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", (byte) 0));
    }

    public void sendPrev(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", (byte) 0));
    }

    public void sendNextPrev(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", (byte) 0));
    }

    public void sendOk(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", (byte) 0));
    }

    public void sendYesNo(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 2, text, "", (byte) 0));
    }

    public void sendAcceptDecline(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", (byte) 0));
    }

    public void sendSimple(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 5, text, "", (byte) 0));
    }

    public void sendNext(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", speaker));
    }

    public void sendPrev(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", speaker));
    }

    public void sendNextPrev(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", speaker));
    }

    public void sendOk(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", speaker));
    }

    public void sendYesNo(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, "", speaker));
    }

    public void sendAcceptDecline(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", speaker));
    }

    public void sendSimple(String text, byte speaker) {
        getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, "", speaker));
    }

    public void sendStyle(String text, int styles[]) {
        getClient().announce(MaplePacketCreator.getNPCTalkStyle(npc, text, styles));
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        getClient().announce(MaplePacketCreator.getNPCTalkNum(npc, text, def, min, max));
    }

    public void sendGetText(String text) {
        getClient().announce(MaplePacketCreator.getNPCTalkText(npc, text));
    }

    /*
     * 0 = ariant colliseum
     * 1 = Dojo
     * 2 = Carnival 1
     * 3 = Carnival 2
     * 4 = Ghost Ship PQ?
     * 5 = Pyramid PQ
     * 6 = Kerning Subway
     */
    public void sendDimensionalMirror(String text) {
        getClient().announce(MaplePacketCreator.getDimensionalMirror(text));
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public String getText() {
        return this.getText;
    }

    public int getJobId() {
        return getPlayer().getJob().getId();
    }

    public void startQuest(short id) {
        try {
            MapleQuest.getInstance(id).forceStart(getPlayer(), npc);
        } catch (NullPointerException ex) {
        }
    }

    public void completeQuest(short id) {
        try {
            MapleQuest.getInstance(id).forceComplete(getPlayer(), npc);
        } catch (NullPointerException ex) {
        }
    }

    public int getMeso() {
        return getPlayer().getMeso();
    }

    public void gainMeso(int gain) {
        getPlayer().gainMeso(gain, true, false, true);
    }

    public void gainExp(int gain) {
        getPlayer().gainExp(gain, true, true);
    }

    public int getLevel() {
        return getPlayer().getLevel();
    }

    public void showEffect(String effect) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(effect, 3));
    }

    public void playSound(String sound) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(sound, 4));
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        getPlayer().equipChanged();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.FACE, face);
        getPlayer().equipChanged();
    }

    public void setSkin(int color) {
        getPlayer().setSkinColor(MapleSkinColor.getById(color));
        getPlayer().updateSingleStat(MapleStat.SKIN, color);
        getPlayer().equipChanged();
    }

    public int itemQuantity(int itemid) {
        return getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemid)).countById(itemid);
    }

    public void displayGuildRanks() {
        MapleGuild.displayGuildRanks(getClient(), npc);
    }

    @Override
    public MapleParty getParty() {
        return getPlayer().getParty();
    }

    @Override
    public void resetMap(int mapid) {
        getClient().getChannelServer().getMapFactory(c.getPlayer().getGMode()).getMap(mapid).resetReactors();
    }

    public void environmentChange(String env, int mode) {
        getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(env, mode));
    }

    public void gainCloseness(int closeness) {
        for (MaplePet pet : getPlayer().getPets()) {
            if (pet.getCloseness() > 30000) {
                pet.setCloseness(30000);
                return;
            }
            pet.gainCloseness(closeness);
            while (pet.getCloseness() > ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
                pet.setLevel((byte) (pet.getLevel() + 1));
                byte index = getPlayer().getPetIndex(pet);
                getClient().announce(MaplePacketCreator.showOwnPetLevelUp(index));
                getPlayer().getMap().broadcastMessage(getPlayer(), MaplePacketCreator.showPetLevelUp(getPlayer(), index));
            }
            IItem petz = getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
            getPlayer().getClient().announce(MaplePacketCreator.updateSlot(petz));
        }
    }

    public String getName() {
        return getPlayer().getName();
    }

    public int getGender() {
        return getPlayer().getGender();
    }

    public void changeJobById(int a) {
        getPlayer().changeJob(MapleJob.getById(a));
    }

    public void addRandomItem(int id) {
        MapleItemInformationProvider i = MapleItemInformationProvider.getInstance();
        MapleInventoryManipulator.addFromDrop(getClient(), i.randomizeStats((Equip) i.getEquipById(id)), true);
    }

    public MapleJob getJobName(int id) {
        return MapleJob.getById(id);
    }

    public MapleStatEffect getItemEffect(int itemId) {
        return MapleItemInformationProvider.getInstance().getItemEffect(itemId);
    }

    public void resetStats() {
        getPlayer().resetStats();
    }

    public void maxMastery() {
        for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
            try {
                ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                if ((skill.getId() / 10000 % 10 == 2 || (getClient().getPlayer().isCygnus() && skill.getId() / 10000 % 10 == 1) || (getClient().getPlayer().isAran() && skill.getId() / 10000 % 10 == 2)) && getPlayer().getSkillLevel(skill) < 1) {
                    getPlayer().changeSkillLevel(skill, (byte) 0, skill.getMaxLevel(), -1);
                }
            } catch (NumberFormatException nfe) {
                break;
            } catch (NullPointerException npe) {
                continue;
            }
        }
    }

    public void processGachapon(int[] id, boolean remote) {
        int[] gacMap = {100000000, 101000000, 102000000, 103000000, 105040300, 800000000, 809000101, 809000201, 600000000, 120000000};
        int itemid = id[Randomizer.nextInt(id.length)];
        addRandomItem(itemid);
        if (!remote) {
            gainItem(5220000, (short) -1);
        }
        sendNext("You have obtained a #b#t" + itemid + "##k.");
        getClient().getChannelServer().broadcastPacket(MaplePacketCreator.gachaponMessage(getPlayer().getInventory(MapleInventoryType.getByType((byte) (itemid / 1000000))).findById(itemid), c.getChannelServer().getMapFactory(c.getPlayer().getGMode()).getMap(gacMap[(getNpc() != 9100117 && getNpc() != 9100109) ? (getNpc() - 9100100) : getNpc() == 9100109 ? 8 : 9]).getMapName(), getPlayer()));
    }
    public void startNpc(int id, String scriptName){
        NPCScriptManager.getInstance().start(c, id, scriptName, null);
    }

    public void disbandAlliance(MapleClient c, int allianceId) {
        PreparedStatement ps = null;
        try {
            ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM `alliance` WHERE id = ?");
            ps.setInt(1, allianceId);
            ps.executeUpdate();
            ps.close();
            c.getChannelServer().getWorldInterface().allianceMessage(c.getPlayer().getGuild().getAllianceId(), MaplePacketCreator.disbandAlliance(allianceId), -1, -1);
            c.getChannelServer().getWorldInterface().disbandAlliance(allianceId);
        } catch (RemoteException r) {
            c.getChannelServer().reconnectWorld();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        }
    }

    public boolean canBeUsedAllianceName(String name) {
        if (name.contains(" ") || name.length() > 12) {
            return false;
        }
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name FROM alliance WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ps.close();
                rs.close();
                return false;
            }
            ps.close();
            rs.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static MapleAlliance createAlliance(MapleCharacter chr1, MapleCharacter chr2, String name) {
        int id = 0;
        int guild1 = chr1.getGuildId();
        int guild2 = chr2.getGuildId();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO `alliance` (`name`, `guild1`, `guild2`) VALUES (?, ?, ?)",PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setInt(2, guild1);
            ps.setInt(3, guild2);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            id = rs.getInt(1);
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        MapleAlliance alliance = new MapleAlliance(name, id, guild1, guild2);
        try {
            WorldChannelInterface wci = chr1.getClient().getChannelServer().getWorldInterface();
            wci.setGuildAllianceId(guild1, id);
            wci.setGuildAllianceId(guild2, id);
            chr1.setAllianceRank(1);
            chr1.saveGuildStatus();
            chr2.setAllianceRank(2);
            chr2.saveGuildStatus();
            wci.addAlliance(id, alliance);
            wci.allianceMessage(id, MaplePacketCreator.makeNewAlliance(alliance, chr1.getClient()), -1, -1);
        } catch (RemoteException e) {
            chr1.getClient().getChannelServer().reconnectWorld();
            chr2.getClient().getChannelServer().reconnectWorld();
            return null;
        }
        return alliance;
    }

    public List<MapleCharacter> getPartyMembers() {
        if (getPlayer().getParty() == null) {
            return null;
        }
        List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
        for (ChannelServer channel : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : channel.getPartyMembers(getPlayer().getParty())) {
                if (chr != null) {
                    chars.add(chr);
                }
            }
        }
        return chars;
    }

    public void warpParty(int id) {
        for (MapleCharacter mc : getPartyMembers()) {
            if (id == 925020100) {
                mc.setDojoParty(true);
            }
            mc.changeMap(getWarpMap(id));
        }
    }

    public boolean hasMerchant() {
        return getPlayer().hasMerchant();
    }

    public boolean hasMerchantItems() {
        try {
            if (!ItemFactory.MERCHANT.loadItems(getPlayer().getId(), false).isEmpty()) {
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        if (getPlayer().getMerchantMeso() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void showFredrick() {
        c.announce(MaplePacketCreator.getFredrick(getPlayer()));
    }
    
    public List<tools.Pair<IItem, MapleInventoryType>> getSavedMerchantItems(){
        List<tools.Pair<IItem, MapleInventoryType>> items = new java.util.ArrayList<tools.Pair<IItem, MapleInventoryType>>();
        try{
        items = ItemFactory.MERCHANT.loadItems(getPlayer().getId(), false);
        } catch (SQLException e){
            e.printStackTrace();
        }
        return items;
    }
    
    public IItem getMerchantItemFromIndex(int index){
        return getSavedMerchantItems().get(index).getLeft();
    }
    
    public void retrieveMerchantItemFromStack(int index){
        boolean success = false;
        if(getSavedMerchantItems().get(index).getLeft().getQuantity() >= 1){
            success = true;
            IItem item = null;
            item = getSavedMerchantItems().get(index).getLeft();
            c.getPlayer().gainItem(getSavedMerchantItems().get(index).getLeft().getItemId(), item.getQuantity(), false);
        } else {
        success = MapleInventoryManipulator.addFromDrop(c, getSavedMerchantItems().get(index).getLeft(), true);
        }
        if(success){
        List<tools.Pair<IItem, MapleInventoryType>> newitems = new java.util.ArrayList<tools.Pair<IItem, MapleInventoryType>>();
        newitems.addAll(getSavedMerchantItems());
        newitems.remove(index);
        try{
        ItemFactory.MERCHANT.saveItems(newitems, getPlayer().getId());
        } catch (SQLException e){
            e.printStackTrace();
        }
        newitems = null;
        }
        
    }

    public int partyMembersInMap() {
        int inMap = 0;
        for (MapleCharacter char2 : getPlayer().getMap().getCharacters()) {
            if (char2.getParty() == getPlayer().getParty()) {
                inMap++;
            }
        }
        return inMap;
    }

    public MapleSquad createMapleSquad(MapleSquadType type) {
        MapleSquad squad = new MapleSquad(c.getChannel(), getPlayer());
        if (getSquadState(type) == 0) {
            c.getChannelServer().addMapleSquad(squad, type);
        } else {
            return null;
        }
        return squad;
    }

    public MapleCharacter getSquadMember(MapleSquadType type, int index) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        MapleCharacter ret = null;
        if (squad != null) {
            ret = squad.getMembers().get(index);
        }
        return ret;
    }

    public int getSquadState(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            return squad.getStatus();
        }
        return 0;
    }

    public void setSquadState(MapleSquadType type, int state) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.setStatus(state);
        }
    }

    public boolean checkSquadLeader(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            return squad.getLeader().getId() == getPlayer().getId();
        }
        return false;
    }

    public int numSquadMembers(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        int ret = 0;
        if (squad != null) {
            ret = squad.getSquadSize();
        }
        return ret;
    }

    public boolean isSquadMember(MapleSquadType type) {
        return c.getChannelServer().getMapleSquad(type).containsMember(getPlayer());
    }

    public void addSquadMember(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.addMember(getPlayer());
        }
    }

    public void removeSquadMember(MapleSquadType type, int index, boolean ban) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.banMember(squad.getMembers().get(index), ban);
        }
    }

    public boolean canAddSquadMember(MapleSquadType type) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            return !squad.isBanned(getPlayer());
        }
        return false;
    }

    public void warpSquadMembers(MapleSquadType type, int mapId) {
        MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        MapleMap map = c.getChannelServer().getMapFactory(c.getPlayer().getGMode()).getMap(mapId);
        if (squad != null) {
            if (checkSquadLeader(type)) {
                for (MapleCharacter chr : squad.getMembers()) {
                    chr.changeMap(map, map.getPortal(0));
                }
            }
        }
    }

    public MapleEvent getEvent() {
        return c.getChannelServer().getEvent();
    }

    public void divideTeams() {
        if (getEvent() != null) {
            getPlayer().setTeam(getEvent().getLimit() % 2); //muhaha :D
        }
    }
    
     public boolean isLeader() {
        return getParty().getLeader().equals(getPlayer().getMPC());
    }

    public byte meetsPQRequirements(String pqType) {
        if(getPlayer().isGM()){
            return 0; // passing automagically..;o
        }
        if (getPlayer().getParty() == null) {
            return 3;
        }
        if (!isLeader()) {
            return 4;
        }
        
        if (pqType.equals("BalrogPQ")) {
            if (getPlayer().getParty().getMembers().size() < 4) {
                return 2;
            }
            for (net.world.MaplePartyCharacter mpc : getPlayer().getParty().getMembers()) {
                if (mpc.getLevel() < 70) {
                    return 2;
                }
            }
            if (!ChannelServer.getInstance(getClient().getChannel()).getMapFactory().getMap(105100300).getCharacters().isEmpty()) {
                return 1;
            } else {
            return 0;
            }
        } else if(pqType.equals("ChristmasPQ")){
            if(getPlayer().getParty().getMembers().size() < 2) {
                return 2;
            } for(net.world.MaplePartyCharacter mpc : getPlayer().getParty().getMembers()){
                if(mpc.getLevel() < 20){
                    return 2;
                }
            }
            if(ChannelServer.getInstance(getClient().getChannel()).getMapFactory().getMap(209000011).getCharacters().isEmpty()){
                return 0;
            } else {
                return 1;
            }
        } else if(pqType.equals("VonLeon")){
            if(getPlayer().getParty().getMembers().size() < 2){
                return 2;
            } for(net.world.MaplePartyCharacter mpc : getPlayer().getParty().getMembers()){
                if(mpc.getLevel() < 90){
                    return 2;
                }
            }
            if(ChannelServer.getInstance(getClient().getChannel()).getMapFactory().getMap(211070110).getCharacters().isEmpty()){
                return 0;
            } else {
                return 1;
            }
        } else if(pqType.equals("ZakumFight")){
            if(getPlayer().getParty().getMembers().size() < 4){
                return 2;
            } for(net.world.MaplePartyCharacter mpc : getPlayer().getParty().getMembers()){
                if(mpc.getLevel() < 125){
                    return 2;
                }
            if(ChannelServer.getInstance(getClient().getChannel()).getMapFactory().getMap(280030000).getCharacters().isEmpty()){
                return 0;
            } else {
                return 1;
            }
            }
        
    } else if(pqType.equals("HornedTailFight")){
            if(getPlayer().getParty().getMembers().size() < 6){
                return 2;
            } for(net.world.MaplePartyCharacter mpc : getPlayer().getParty().getMembers()){
                if(mpc.getLevel() < 140){
                    return 2;
                }
            if(ChannelServer.getInstance(getClient().getChannel()).getMapFactory().getMap(240060200).getCharacters().isEmpty()){
                return 0;
            } else {
                return 1;
            }
            }
    } else if(pqType.equals("Scarlion")){
            if(getPlayer().getParty().getMembers().size() < 6){
                return 2;
            } for(net.world.MaplePartyCharacter mpc : getPlayer().getParty().getMembers()){
                if(mpc.getLevel() < 140){
                    return 2;
                }
            if(ChannelServer.getInstance(getClient().getChannel()).getMapFactory().getMap(240060200).getCharacters().isEmpty()){
                return 0;
            } else {
                return 1;
            }
            }
    }
        return 0;
    }

    public void createPQ(String pqType) {
        if (pqType.equals("BalrogPQ")) {
            new BalrogPQ(getPlayer(), (byte) getClient().getChannel());
        } else if(pqType.equals("ChristmasPQ")){
            new ChristmasPQ(getPlayer(), (byte) getClient().getChannel());
        } else if(pqType.equals("VonLeon")){
            new VonLeon(getPlayer(), (byte) getClient().getChannel());
        } else if(pqType.equals("ZakumFight")){
            new ZakumFight(getPlayer(), (byte)getClient().getChannel());
        } else if(pqType.equals("HornedTailFight")){
            new HornedTailFight(getPlayer(), (byte)getClient().getChannel());
        }
    }

    public boolean areMapsEmpty(int[] maps) {
        MapleMapFactory mf = getClient().getChannelServer().getMapFactory(getPlayer().getGMode());
        for (int i : maps) {
            if (!mf.getMap(i).getCharacters().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public void MakeGMItem (byte slot, MapleCharacter player) {
      client.MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
      Equip eu = (Equip) equip.getItem(slot);
      int item = equip.getItem(slot).getItemId();
      short hand = eu.getHands();
      byte level = eu.getLevel();
      short Watk = eu.getWatk();
      short Wdef = eu.getWdef();
      short Acc = eu.getAcc();
      short Avo = eu.getAvoid();
      short Jmp = eu.getJump();
      short Spd = eu.getSpeed();
      short Matk = eu.getMatk();
      short Mdef = eu.getMdef();
      short Hp = eu.getHp();
      short Mp = eu.getMp();
      Equip nItem = new Equip(item, equip.getNextFreeSlot());
      nItem.setStr((short) 32767); // STR
      nItem.setDex((short) 32767); // DEX
      nItem.setInt((short) 32767); // INT
      nItem.setLuk((short) 32767); // LUK
      nItem.setWatk(Watk);
      nItem.setWdef(Wdef);
      nItem.setAcc(Acc);
      nItem.setAvoid(Avo);
      nItem.setJump(Jmp);
      nItem.setSpeed(Spd);
      nItem.setMatk(Matk);
      nItem.setMdef(Mdef);
      nItem.setHp(Hp);
      nItem.setMp(Mp);
      nItem.setUpgradeSlots((byte) 0);
      nItem.setHands(hand);
      nItem.setLevel(level);
      nItem.setRingId(-1);
            player.getInventory(MapleInventoryType.EQUIP).addFromDB(nItem);
        }
    
    public String EquipList(MapleClient c) {
            StringBuilder str = new StringBuilder();
            client.MapleInventory equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP);
            List<String> stra = new LinkedList<String>();
            for (IItem item : equip.list()) {
                stra.add("#L" + item.getPosition() + "##v" + item.getItemId() + "##l");
            }
            for (String strb : stra) {
                str.append(strb);
            }
            return str.toString();
        }

}
