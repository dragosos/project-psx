/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program unader any cother version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client;

import client.autoban.AutobanFactory;
import server.achievement.MapleAchievement;
import constants.ExpTable;
import constants.ServerConstants;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import net.MaplePacket;
import net.channel.ChannelServer;
import net.world.MapleMessenger;
import net.world.MapleMessengerCharacter;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.PlayerBuffValueHolder;
import net.world.PlayerCoolDownValueHolder;
import net.world.guild.MapleGuild;
import net.world.guild.MapleGuildCharacter;
import server.clones.*;
import net.world.remote.WorldChannelInterface;
import constants.SkillConstants.*;
import scripting.event.EventInstanceManager;
import client.autoban.AutobanManager;
import constants.ItemConstants;
import server.CashShop;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.MaplePortal;
import server.MapleShop;
import server.MapleStatEffect;
import server.MapleStorage;
import server.MapleTrade;
import server.TimerManager;
import server.events.MapleEvents;
import server.events.MapleEvents.*;
import server.events.MapleFitness;
import server.events.MapleOla;
import server.events.MonsterCarnival;
import server.events.MonsterCarnivalParty;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.maps.AbstractAnimatedMapleMapObject;
import server.maps.HiredMerchant;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapEffect;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.PlayerNPCs;
import server.maps.SavedLocation;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import server.GameModeManager;
import java.util.HashSet;
import scripting.npc.NPCScriptManager;
import server.achievement.MapleAchievementLibrary;
import server.achievement.MapleAchievementProgress;
import server.pqs.PartyQuest;
import server.life.farming.MapleFarmHouse;
import tools.FileoutputUtil;
import java.util.EnumMap;
import server.achievement.MapleAchievementLibrary;
import server.HotList;
import server.pqs.stagedpq.*;

public class MapleCharacter extends AbstractAnimatedMapleMapObject {

    private int world;
    private int accountid;
    private int rank;
    private int rankMove;
    private int jobRank;
    private int jobRankMove;
    private int id;
    private int level;
    private int str;
    private int dex;
    private int luk;
    private int int_;
    private int hp;
    private int maxhp;
    private int realhp;
    private int mp;
    private int maxmp;
    private int realmp;
    private int hpMpApUsed;
    private int hair;
    private int face;
    private int remainingAp;
    private int remainingSp;
    private int fame;
    private int initialSpawnPoint;
    public int mapid;
    private int gender;
    private int currentPage;
    private int currentType = 0;
    private int currentTab = 1;
    private int chair;
    private int itemEffect;
    private int guildid;
    private int guildrank;
    private int allianceRank;
    private int messengerposition = 4;
    private int slots = 0;
    private int energybar;
    private int gmLevel;
    private int ci = 0;
    private MapleFamily family;
    private int familyId;
    private int bookCover;
    private int markedMonster = 0;
    private int battleshipHp = 0;
    private int mesosTraded = 0;
    private int possibleReports = 10;
    private int dojoPoints;
    private int vanquisherStage;
    private int dojoStage;
    private int dojoEnergy;
    private int vanquisherKills;
    private int warpToId;
    private int expRate = 1;
    private int mesoRate = 1;
    private int dropRate = 1;
    private int omokwins = 0;
    private int omokties = 0;
    private int omoklosses = 0;
    private int matchcardwins = 0;
    private int matchcardties = 0;
    private int matchcardlosses = 0;
    private int married = 0;
    private long dojoFinish = 0;
    private long lastfametime = 0;
    private long lastUsedCashItem = 0;
    private long lastHealed = 0;
    private transient int localmaxhp = 50;
    private transient int localmaxmp = 50;
    private transient int localstr = 4;
    private transient int localdex = 4;
    private transient int localluk = 4;
    private transient int localint_ = 4;
    private transient int magic = 0;
    private transient int watk = 0;
    private boolean hidden = false;
    private boolean canDoor = true;
    private boolean Berserk = false;
    private boolean hasMerchant = false;
    private int linkedLevel = 0;
    private String linkedName = null;
    private boolean finishedDojoTutorial = false;
    private boolean dojoParty = false;
    private String name;
    private String chalktext;
    private String search = null;
    private AtomicInteger exp = new AtomicInteger();
    private AtomicInteger gachaexp = new AtomicInteger();
    private AtomicInteger meso = new AtomicInteger();
    private int merchantmeso;
    private BuddyList buddylist;
    private EventInstanceManager eventInstance = null;
    private HiredMerchant hiredMerchant = null;
    private MapleClient client;
    private MapleGuildCharacter mgc = null;
    private MaplePartyCharacter mpc = null;
    private MapleInventory[] inventory;
    private MapleJob job = MapleJob.BEGINNER;
    public MapleMap map;
    private MapleMap dojoMap;
    private MapleMessenger messenger = null;
    private MapleMiniGame miniGame;
    private MapleMount maplemount;
    private MapleParty party;
    private MaplePet[] pets = new MaplePet[3];
    private MaplePlayerShop playerShop = null;
    private MapleShop shop = null;
    private MapleSkinColor skinColor = MapleSkinColor.NORMAL;
    private MapleStorage storage = null;
    private MapleTrade trade = null;
    private java.util.EnumMap<SavedLocationType, SavedLocation> savedLocations = new java.util.EnumMap<SavedLocationType, SavedLocation>(SavedLocationType.class);
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private List<Integer> lastmonthfameids;
    private Map<MapleQuest, MapleQuestStatus> quests;
    private Set<MapleMonster> controlled = new LinkedHashSet<MapleMonster>();
    private Map<Integer, String> entered = new LinkedHashMap<Integer, String>();
    private Set<MapleMapObject> visibleMapObjects = new LinkedHashSet<MapleMapObject>();
    private Map<ISkill, SkillEntry> skills = new LinkedHashMap<ISkill, SkillEntry>();
    private Map<MapleBuffStat, MapleBuffStatValueHolder> effects = Collections.synchronizedMap(new LinkedHashMap<MapleBuffStat, MapleBuffStatValueHolder>());
    private Map<Integer, MapleKeyBinding> keymap = new LinkedHashMap<Integer, MapleKeyBinding>();
    private Map<Integer, MapleSummon> summons = new LinkedHashMap<Integer, MapleSummon>();
    private Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<Integer, MapleCoolDownValueHolder>();
    private List<MapleDisease> diseases = new ArrayList<MapleDisease>();
    private List<MapleDoor> doors = new ArrayList<MapleDoor>();
    private ScheduledFuture<?> dragonBloodSchedule;
    private ScheduledFuture<?> mapTimeLimitTask = null;
    private ScheduledFuture<?> fullnessSchedule;
    private ScheduledFuture<?> fullnessSchedule_1;
    private ScheduledFuture<?> fullnessSchedule_2;
    private ScheduledFuture<?> hpDecreaseTask;
    private ScheduledFuture<?> beholderHealingSchedule;
    private ScheduledFuture<?> beholderBuffSchedule;
    private ScheduledFuture<?> BerserkSchedule;
    private ScheduledFuture<?> expiretask;
    private ScheduledFuture<?> mech_robotTask;
    private List<ScheduledFuture<?>> timers = new ArrayList<ScheduledFuture<?>>();
    private NumberFormat nf = new DecimalFormat("#,###,###,###");
    private ArrayList<String> commands = new ArrayList<String>();
    private ArrayList<Integer> excluded = new ArrayList<Integer>();
    private MonsterBook monsterbook;
    private List<MapleRing> crushRings = new ArrayList<MapleRing>();
    private List<MapleRing> friendshipRings = new ArrayList<MapleRing>();
    private MapleRing marriageRing;
    private static String[] ariantroomleader = null;
    private static int[] ariantroomslot = null;
    private CashShop cashshop;
    private long portaldelay = 0, lastattack = 0;
    private int combocounter = 0, lastmobcount = 0;
    private List<String> blockedPortals = new ArrayList<String>();
    public ArrayList<String> area_data = new ArrayList<String>();
    private AutobanManager autoban;
    private boolean isbanned = false;
    private ScheduledFuture<?> pendantOfSpirit = null; //1122017
    private int pendantExp = 0; //Actually should just be equipExp
    private int[] trockmaps = new int[5];
    private int[] viptrockmaps = new int[10];
    private MapleEvents events;
    private static GameMode gMode;
    private EnumMap<MapleAchievementLibrary, MapleAchievement> achievements = new EnumMap<MapleAchievementLibrary, MapleAchievement>(MapleAchievementLibrary.class);
    private int pvpmonsteroid = -1;
    private int pvpkills = 0, pvpdeaths = 0;
    private boolean hasPvpEnabled = false;
    private MapleAchievementProgress achievementProgress;
    private MapleFamilyCharacter familyChar = null;
    private MapleFamilyBonus familyBonus = null;
    private short campaignProgress = (short) 0;
    public boolean isFishing = false;
    public boolean shutDownLock = false;
    private PartyQuest pq = null;
    private MapleDonator md;
    private int votepoints = 0;
    private ExtendedSPTable table;
    public volatile boolean npctalk = false;
    private MapleDragon evanDragon;
    public boolean isDB = false;
    public int prty_invitationID = -1;
    public AutobanManager suspectManager = null;
    private boolean isMWLB = false;
    private List<MaplePokemon> m_pets = new ArrayList<MaplePokemon>();
    public long logintime = 0;
    public ReentrantLock NPCLock = new ReentrantLock();
    public long lastNPCChat = 0;
    private byte every_ThirdMonsterBonus = 0;
    public boolean ismapChanging = false;
    private StagedPartyQuest spq = null;
    private boolean gmchat = true;
    private int reborns;
    private int relationship;
    private int mute;
    private float jailtime;
    private boolean wantstoviewsmega = true;
    public boolean autoreborn = false;

    public MapleCharacter() {
        setStance(0);
        inventory = new MapleInventory[MapleInventoryType.values().length];

        for (MapleInventoryType type : MapleInventoryType.values()) {
            byte b = 24;
            if (type == MapleInventoryType.CASH) {
                b = 96;
            }
            inventory[type.ordinal()] = new MapleInventory(type, (byte) b);
        }

        quests = new LinkedHashMap<MapleQuest, MapleQuestStatus>();
        setPosition(new Point(0, 0));
    }

    public String ListSuspectInfractions() {
        String ret = "No infractions.";
        if (suspectManager != null) {
            ret = "";
            for (int i = 0; i < suspectManager.points.size(); i++) {
                ret += "'#e" + suspectManager.points.keySet().iterator().next().name() + "#n'(Count :#e" + suspectManager.points.get(AutobanFactory.values()[i]) + "#n)\r\n";
            }
        }

        return ret;
    }

    public boolean addEveryThirdMonsterBonus() {
        if (every_ThirdMonsterBonus < 2) {
            every_ThirdMonsterBonus++;
            return false;
        } else { // resetting, reached max, which is 3.
            every_ThirdMonsterBonus = 0;
            return true;
        }
    }

    public MaplePokemon getPokemon(int index) {
        return m_pets.get(index);
    }

    public MaplePokemon getPokemonFromList(MaplePokemon p) {
        MaplePokemon ret = null;
        for (MaplePokemon mp : getAllPokemon()) {
            if (mp == p) {
                return mp;
            }
        }

        return ret;
    }

    public void addPokemon(MaplePokemon p) {
        m_pets.add(p);
    }

    public void setPokemonToNull(int i) {
        m_pets.remove(i);
    }

    public void removePokemon(MaplePokemon p) {
        m_pets.remove(p);
    }

    public List<MaplePokemon> getAllPokemon() {
        return m_pets;
    }

    public void setMount(MapleMount newm) {
        maplemount = newm;
    }

    public static MapleCharacter getDefault(MapleClient c) {
        MapleCharacter ret = new MapleCharacter();
        ret.client = c;
        ret.gmLevel = c.gmLevel();
        ret.hp = 50;
        ret.maxhp = 50;
        ret.mp = 5;
        ret.maxmp = 5;
        ret.str = 12;
        ret.dex = 5;
        ret.int_ = 4;
        ret.luk = 4;
        ret.map = null;
        ret.job = MapleJob.BEGINNER;
        ret.level = 1;
        ret.accountid = c.getAccID();
        ret.buddylist = new BuddyList(20);
        ret.maplemount = null;
        ret.pvpkills = 0;
        ret.pvpdeaths = 0;
        ret.campaignProgress = 0;
        ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.USE).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(24);
        ret.getInventory(MapleInventoryType.ETC).setSlotLimit(24);
        int[] key = {18, 65, 2, 23, 3, 4, 5, 6, 16, 17, 19, 25, 26, 27, 31, 34, 35, 37, 38, 40, 43, 44, 45, 46, 50, 56, 59, 60, 61, 62, 63, 64, 57, 48, 29, 7, 24, 33, 41};
        int[] type = {4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 5, 6, 6, 6, 6, 6, 6, 5, 4, 5, 4, 4, 4, 4};
        int[] action = {0, 106, 10, 1, 12, 13, 18, 24, 8, 5, 4, 19, 14, 15, 2, 17, 11, 3, 20, 16, 9, 50, 51, 6, 7, 53, 100, 101, 102, 103, 104, 105, 54, 22, 52, 21, 25, 26, 23};
        for (int i = 0; i < key.length; i++) {
            ret.keymap.put(key[i], new MapleKeyBinding(type[i], action[i]));
        }
        //to fix the map 0 lol
        for (int i = 0; i < 5; i++) {
            ret.trockmaps[i] = 999999999;
        }
        for (int i = 0; i < 10; i++) {
            ret.viptrockmaps[i] = 999999999;
        }

        if (ret.isGM()) {
            ret.job = MapleJob.GM;
            ret.level = 200;
            //int[] gmskills = {9001000, 9001001, 9001000, 9101000, 9101001, 9101002, 9101003, 9101004, 9101005, 9101006, 9101007, 9101008};
        }
        return ret;
    }

    public void addCooldown(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
        //  if (this.coolDowns.containsKey(Integer.valueOf(skillId))) {
        //      this.coolDowns.remove(skillId);
        //  }
        //  this.coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length, timer));
    }

    public void addCommandToList(String command) {
        commands.add(command);
    }

    public void addCrushRing(MapleRing r) {
        crushRings.add(r);
    }

    public MapleRing getRingById(int id) {
        for (MapleRing ring : getCrushRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }
        for (MapleRing ring : getFriendshipRings()) {
            if (ring.getRingId() == id) {
                return ring;
            }
        }
        if (getMarriageRing().getRingId() == id) {
            return getMarriageRing();
        }

        return null;
    }

    public void changeGMode(GameMode newMode) {
        gMode = newMode;
        // getClient().getChannelServer().getGameModeManager().grantEntry(this.getId(), gMode);
    }

    public GameMode getGMode() {
        return gMode;
    }

    public int addDojoPointsByMap() {
        int pts = 0;
        if (dojoPoints < 17000) {
            pts = 1 + ((getMap().getId() - 1) / 100 % 100) / 6;
            if (!dojoParty) {
                pts++;
            }
            this.dojoPoints += pts;
        }
        return pts;
    }

    public void addDoor(MapleDoor door) {
        doors.add(door);
    }

    public void addExcluded(int x) {
        excluded.add(x);
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void addFriendshipRing(MapleRing r) {
        friendshipRings.add(r);
    }

    public void addHP(int delta) {
        setHp(hp + delta);
        updateSingleStat(MapleStat.HP, hp);
    }

    public void addMesosTraded(int gain) {
        this.mesosTraded += gain;
    }

    public void addMP(int delta) {
        setMp(mp + delta);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        setHp(hp + hpDiff);
        setMp(mp + mpDiff);
        updateSingleStat(MapleStat.HP, getHp());
        updateSingleStat(MapleStat.MP, getMp());
    }

    public void addPet(MaplePet pet) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                pets[i] = pet;
                return;
            }
        }
    }

    public void addStat(int type, int up) {
        if (type == 1) {
            this.str += up;
            updateSingleStat(MapleStat.STR, str);
        } else if (type == 2) {
            this.dex += up;
            updateSingleStat(MapleStat.DEX, dex);
        } else if (type == 3) {
            this.int_ += up;
            updateSingleStat(MapleStat.INT, int_);
        } else if (type == 4) {
            this.luk += up;
            updateSingleStat(MapleStat.LUK, luk);
        }
    }

    public int addHP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleJob jobtype = player.getJob();
        int MaxHP = player.getMaxHp();
        if (player.getHpMpApUsed() > 9999 || MaxHP >= 30000) {
            return MaxHP;
        }
        if (jobtype.isA(MapleJob.BEGINNER)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.WARRIOR) || jobtype.isA(MapleJob.DAWNWARRIOR1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(10000000) : SkillFactory.getSkill(1000001)) > 0) {
                MaxHP += 20;
            } else {
                MaxHP += 8;
            }
        } else if (jobtype.isA(MapleJob.MAGICIAN) || jobtype.isA(MapleJob.BLAZEWIZARD1)) {
            MaxHP += 6;
        } else if (jobtype.isA(MapleJob.BOWMAN) || jobtype.isA(MapleJob.WINDARCHER1)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.THIEF) || jobtype.isA(MapleJob.NIGHTWALKER1)) {
            MaxHP += 8;
        } else if (jobtype.isA(MapleJob.PIRATE) || jobtype.isA(MapleJob.THUNDERBREAKER1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(15100000) : SkillFactory.getSkill(5100000)) > 0) {
                MaxHP += 18;
            } else {
                MaxHP += 8;
            }
        }
        return MaxHP;
    }

    public int addMP(MapleClient c) {
        MapleCharacter player = c.getPlayer();
        int MaxMP = player.getMaxMp();
        if (player.getHpMpApUsed() > 9999 || player.getMaxMp() >= 30000) {
            return MaxMP;
        }
        if (player.getJob().isA(MapleJob.BEGINNER) || player.getJob().isA(MapleJob.NOBLESSE) || player.getJob().isA(MapleJob.LEGEND)) {
            MaxMP += 6;
        } else if (player.getJob().isA(MapleJob.WARRIOR) || player.getJob().isA(MapleJob.DAWNWARRIOR1) || player.getJob().isA(MapleJob.ARAN1)) {
            MaxMP += 2;
        } else if (player.getJob().isA(MapleJob.MAGICIAN) || player.getJob().isA(MapleJob.BLAZEWIZARD1)) {
            if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(12000000) : SkillFactory.getSkill(2000001)) > 0) {
                MaxMP += 18;
            } else {
                MaxMP += 14;
            }

        } else if (player.getJob().isA(MapleJob.BOWMAN) || player.getJob().isA(MapleJob.THIEF)) {
            MaxMP += 10;
        } else if (player.getJob().isA(MapleJob.PIRATE)) {
            MaxMP += 14;
        }

        return MaxMP;
    }

    public void addSummon(int id, MapleSummon summon) {
        summons.put(id, summon);
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        synchronized (visibleMapObjects) {
            visibleMapObjects.add(mo);
        }
    }

    public void ban(String reason, boolean dc) {
        getClient().banMacs();
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?");
            ps.setString(1, reason);
            ps.setInt(2, accountid);
            ps.executeUpdate();
            ps.close();
            this.isbanned = true;
        } catch (Exception e) {
        }
        if (dc) {
            getClient().disconnect(); // trolololo
        }
    }

    public static boolean ban(String id, String reason, boolean accountId) {
        PreparedStatement ps = null;
        try {
            Connection con = DatabaseConnection.getConnection();
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                return true;
            }
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }

            boolean ret = false;
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PreparedStatement psb = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?");
                psb.setString(1, reason);
                psb.setInt(2, rs.getInt(1));
                psb.executeUpdate();
                psb.close();
                ret = true;
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException ex) {
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException e) {
            }
        }
        return false;
    }

    public int calculateMaxBaseDamage(int watk) {
        int maxbasedamage;
        if (watk == 0) {
            maxbasedamage = 1;
        } else {
            IItem weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
            if (weapon_item != null) {
                MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
                int mainstat;
                int secondarystat;
                if (weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW) {
                    mainstat = localdex;
                    secondarystat = localstr;
                } else if ((getJob().isA(MapleJob.THIEF) || getJob().isA(MapleJob.NIGHTWALKER1)) && (weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER)) {
                    mainstat = localluk;
                    secondarystat = localdex + localstr;
                } else {
                    mainstat = localstr;
                    secondarystat = localdex;
                }
                maxbasedamage = (int) (((weapon.getMaxDamageMultiplier() * mainstat + secondarystat) / 100.0) * watk) + 10;
            } else {
                maxbasedamage = 0;
            }
        }
        return maxbasedamage;
    }

    public boolean isDamageHacking(int damage) {
        int plevel = getLevel();
        if (this.getReborns() < 1 && !isGM()) {
            return plevel <= 10 && damage >= 2500 || plevel <= 20 && damage >= 5000 || plevel <= 30 && damage >= 10000 || plevel <= 65 && damage >= 100000 || damage >= 500000 && plevel <= 80 || damage == 999999 && plevel <= 100;
        } else {
            return false;
        }
    }

    public void cancelAllBuffs() {
        if (effects != null) {
            for (MapleBuffStatValueHolder mbsvh : new ArrayList<MapleBuffStatValueHolder>(effects.values())) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void cancelBuffStats(MapleBuffStat stat) {
        List<MapleBuffStat> buffStatList = Arrays.asList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList);
    }

    public void setCombo(int count) {
        if (combocounter > 30000) {
            combocounter = 30000;
            return;
        }
        combocounter = count;

    }

    public void setLastAttack(long time) {
        lastattack = time;
    }

    public int getCombo() {
        return combocounter;
    }

    public long getLastAttack() {
        return lastattack;
    }

    public int getLastMobCount() { //Used for skills that have mobCount at 1. (a/b)
        return lastmobcount;
    }

    public void setLastMobCount(int count) {
        lastmobcount = count;
    }

    public void channelChanged(MapleClient c) {
        this.client = c;
        MaplePortal portal = map.findClosestSpawnpoint(getPosition());
        if (portal == null) {
            portal = map.getPortal(0);
        }
        this.setPosition(portal.getPosition());
        this.initialSpawnPoint = portal.getId();
        this.map = c.getChannelServer().getMapFactory(gMode).getMap(getMapId());
    }

    public boolean getGMChat() {
        return gmchat;
    }

    public static class CancelCooldownAction implements Runnable {

        private int skillId;
        private WeakReference<MapleCharacter> target;

        public CancelCooldownAction(MapleCharacter target, int skillId) {
            this.target = new WeakReference<MapleCharacter>(target);
            this.skillId = skillId;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.removeCooldown(skillId);
                realTarget.client.announce(MaplePacketCreator.skillCooldown(skillId, 0));
            }
        }
    }

    public void cancelEffect(int itemId) {
        cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(itemId), false, -1);
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        List<MapleBuffStat> buffstats;
        if (!overwrite) {
            buffstats = getBuffStats(effect, startTime);
        } else {
            List<Pair<MapleBuffStat, Integer>> statups = effect.getStatups();
            buffstats = new ArrayList<MapleBuffStat>(statups.size());
            for (Pair<MapleBuffStat, Integer> statup : statups) {
                buffstats.add(statup.getLeft());
            }
        }
        deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
            if (!getDoors().isEmpty()) {
                MapleDoor door = getDoors().iterator().next();
                for (MapleCharacter chr : door.getTarget().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleCharacter chr : door.getTown().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleDoor destroyDoor : getDoors()) {
                    door.getTarget().removeMapObject(destroyDoor);
                    door.getTown().removeMapObject(destroyDoor);
                }
                clearDoors();
                silentPartyUpdate();
            }
        }
        if (effect.getSourceId() == Spearman.HYPER_BODY || effect.getSourceId() == GM.HYPER_BODY || effect.getSourceId() == SuperGM.HYPER_BODY || effect.getSourceId() == BattleMage.CONVERSION) {
            List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(4);
            statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, Math.min(hp, maxhp)));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, Math.min(mp, maxmp)));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, maxhp));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, maxmp));
            getClient().announce(MaplePacketCreator.updatePlayerStats(getClient(), statup));
        }
        if (effect.isMonsterRiding()) {
            if (effect.getSourceId() != Corsair.BATTLE_SHIP) {
                this.getMount().cancelSchedule();
                this.getMount().setActive(false);
            }
        }
        if (!overwrite) {
            cancelPlayerBuffs(buffstats);
        }
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        cancelEffect(effects.get(stat).effect, false, -1);
    }

    public void toggleHide(boolean hide) {
        if (isGM()) {
            if (!hide) {
                this.hidden = false;
                getMap().broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                updatePartyMemberHP();
                dropMessage("You are now out of hide mode. Everybody can see you now.");
            } else {
                this.hidden = true;
                getMap().broadcastNONGMMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
                dropMessage("You are now in hide mode.");
            }
            announce(MaplePacketCreator.enableActions());
        }
    }

    public void toggleHide(boolean login, boolean yes) {
        if (isGM()) {
            if (!yes) {
                this.hidden = false;
                getMap().broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                updatePartyMemberHP();
                dropMessage("You are now out of hide mode. Everybody can see you now.");
            } else {
                this.hidden = true;
                if (!login) {
                    getMap().broadcastNONGMMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
                }
                dropMessage("You are now in hide mode.");
            }
            announce(MaplePacketCreator.enableActions());
        }
    }

    private void cancelFullnessSchedule(int petSlot) {
        switch (petSlot) {
            case 0:
                if (fullnessSchedule != null) {
                    fullnessSchedule.cancel(false);
                }
                break;
            case 1:
                if (fullnessSchedule_1 != null) {
                    fullnessSchedule_1.cancel(false);
                }
                break;
            case 2:
                if (fullnessSchedule_2 != null) {
                    fullnessSchedule_2.cancel(false);
                }
                break;
        }
    }

    public void cancelMagicDoor() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<MapleBuffStatValueHolder>(effects.values())) {
            if (mbsvh.effect.isMagicDoor()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void cancelMapTimeLimitTask() {
        if (mapTimeLimitTask != null) {
            mapTimeLimitTask.cancel(false);
        }
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        if (getClient().getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            recalcLocalStats();
            enforceMaxHpMp();
            client.announce(MaplePacketCreator.cancelBuff(buffstats));
            if (buffstats.size() > 0) {
                if (!buffstats.contains(MapleBuffStat.SHADOWPARTNER)) {
                    getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
                }
            }
        }
    }

    public static boolean canCreateChar(String name) {
        if (name.length() < 4 || name.length() > 12) {
            return false;
        }

        if (isInUse(name)) {
            return false;
        }

        return getIdByName(name) < 0 && !name.toLowerCase().contains("gm") && Pattern.compile("[a-zA-Z0-9_-]{3,12}").matcher(name).matches();
    }

    public boolean canDoor() {
        return canDoor;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (gmLevel > 0) {
            return FameStatus.OK;
        } else if (lastfametime >= System.currentTimeMillis() - 3600000 * 24) {
            return FameStatus.NOT_TODAY;
        } else if (lastmonthfameids.contains(Integer.valueOf(from.getId()))) {
            return FameStatus.NOT_THIS_MONTH;
        } else {
            return FameStatus.OK;
        }
    }

    public void changeCI(int type) {
        this.ci = type;
    }

    public void changeJob(MapleJob newJob) {
        this.setRates(); // reinitializing the rates!
        if (job.getId() == 0) {
            restoreAPStats();
        }
        if (newJob != null) {
            this.job = newJob;
        } else {
            return;
        }
        this.remainingSp++;
        if (newJob.getId() % 10 == 2) {
            this.remainingSp += 2;
        }
        if (newJob.getId() % 10 > 1) {
            this.remainingAp += 5;
        }
        int job_ = job.getId() % 1000; // lame temp "fix"
        if (job_ == 100) {
            maxhp += Randomizer.rand(200, 250);
        } else if (job_ == 200) {
            maxmp += Randomizer.rand(100, 150);
        } else if (job_ % 100 == 0) {
            maxhp += Randomizer.rand(100, 150);
            maxhp += Randomizer.rand(25, 50);
        } else if (job_ > 0 && job_ < 200) {
            maxhp += Randomizer.rand(300, 350);
        } else if (job_ < 300) {
            maxmp += Randomizer.rand(450, 500);
        } //handle KoC here (undone)
        else if (job_ > 0 && job_ != 1000) {
            maxhp += Randomizer.rand(300, 350);
            maxmp += Randomizer.rand(150, 200);
        }
        if (maxhp >= 30000) {
            maxhp = 30000;
        }
        if (maxmp >= 30000) {
            maxmp = 30000;
        }
        if (!isGM()) {
            for (byte i = 1; i < 5; i++) {
                //  gainSlots(i, 4, true);
            }
        }
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(5);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, Integer.valueOf(localmaxhp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, Integer.valueOf(localmaxmp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP, remainingSp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.JOB, Integer.valueOf(job.getId())));
        recalcLocalStats();
        getClient().announce(MaplePacketCreator.updatePlayerStats(getClient(), statup));
        silentPartyUpdate();
        if (this.guildid > 0) {
            getGuild().broadcast(MaplePacketCreator.jobMessage(0, job.getId(), name), this.getId());
        }
        guildUpdate();
        if (getFamilyChar().hasFamily()) {
            getFamilyChar().setJob(getJob().getId());
            MapleFamily.recacheChar(familyChar.getFamilyId(), familyChar);
        }
        //   getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 8), false);
        if (isEvan()) {
            getEvanDragon().setJobId(getJob().getId());
            announce(MaplePacketCreator.spawnDragon(evanDragon, true));
            MapleDragon dragon = (MapleDragon) getMap().getMapObject(getEvanDragon().getObjectId());
            if (dragon != null) {
                getMap().removeMapObject(dragon);
                getMap().addMapObject(getEvanDragon());
            }

        }

        maxAllSkillsByJobGroup();
        if (isJaguar()) {
            announce(MaplePacketCreator.updateJaguar());
        }

    }

    public void changeKeybinding(int key, MapleKeyBinding keybinding) {
        if (keybinding.getType() != 0) {
            keymap.put(Integer.valueOf(key), keybinding);
        } else {
            keymap.remove(Integer.valueOf(key));
        }
    }

    public MapleMap mapSubstitution(MapleMap to) {
        /*
         * GameModeManager gmm =
         * this.getClient().getChannelServer().getGameModeManager();
         * if(getGMode().getType() > 0 && gmm.getMapFactoryByMode(gMode) !=
         * null){ // meaning it's anything but normal mode return
         * gmm.getMapFactoryByMode(gMode).getMap(to.getId()); } else { return
         * to;
        }
         */
        return map;
    }

    public void changeMap(int map) {
        changeMap(map, 0);
    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap = getClient().getChannelServer().getMapFactory(gMode).getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, String portal) {
        MapleMap warpMap = getClient().getChannelServer().getMapFactory(gMode).getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, MaplePortal portal) {
        MapleMap warpMap = getClient().getChannelServer().getMapFactory(gMode).getMap(map);
        changeMap(warpMap, portal);
    }

    public void changeMap(MapleMap to) {
        changeMap(to, to.getPortal(0));
    }

    public void changeMap(final MapleMap to, final MaplePortal pto) {
        if (to == null) {
            dropMessage("This map is currently unavailable. Please try another map.");
            return;
        }
        if (to.getId() == 100000200 || to.getId() == 211000100 || to.getId() == 220000300) {
            changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId() - 2, this));
        } else {
            changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this));
        }
    }

    public void changeMap(final MapleMap to, final Point pos) {
        changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to, 0x80, this));
    }

    public void changeMapBanish(int mapid, String portal, String msg) {
        dropMessage(5, msg);
        MapleMap map_ = ChannelServer.getInstance(client.getChannel()).getMapFactory(gMode).getMap(mapid);
        changeMap(map_, map_.getPortal(portal));
    }

    private void changeMapInternal(final MapleMap to, final Point pos, MaplePacket warpPacket) {
        if (getTrade() != null) {
            if (getTrade() == getTrade().getPartner()) {
                return;
            } else {
                // tryin' to dupee
                getClient().disconnect();
                return;
            }
        }
        warpPacket.setOnSend(new Runnable() {

            @Override
            public void run() {
                if (map.getCharacters().contains(MapleCharacter.this)) {
                    map.removePlayer(MapleCharacter.this);
                }
                if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
                    setMap(to);
                    setPosition(pos);
                    map.addPlayer(MapleCharacter.this);
                    if (party != null) {
                        mpc.setMapId(to.getId());
                        silentPartyUpdate();
                        client.announce(MaplePacketCreator.updateParty(client.getChannel(), party, PartyOperation.SILENT_UPDATE, null));
                        updatePartyMemberHP();
                    }
                    if (getMap().getHPDec() > 0) {
                        hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {

                            @Override
                            public void run() {
                                doHurtHp();
                            }
                        }, 10000);
                    }
                }
            }
        });
        client.announce(warpPacket);
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void changeSkillLevel(ISkill skill, byte newLevel, int newMasterlevel, long expiration) {
        if (newLevel > -1) {
            skills.put(skill, new SkillEntry(newLevel, newMasterlevel, -1));
            this.client.announce(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel));
        } else {
            skills.remove(skill);
            this.client.announce(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel)); //Shouldn't use expiration anymore :)
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM skills WHERE skillid = ? AND characterid = ?");
                ps.setInt(1, skill.getId());
                ps.setInt(2, id);
                ps.execute();
                ps.close();
            } catch (SQLException ex) {
                System.out.print("Error deleting skill: " + ex);
            }
        }
    }

    public void changeTab(int tab) {
        this.currentTab = tab;
    }

    public void changeType(int type) {
        this.currentType = type;
    }

    public void checkBerserk() {
        if (BerserkSchedule != null) {
            BerserkSchedule.cancel(false);
        }
        final MapleCharacter chr = this;
        if (job.equals(MapleJob.DARKKNIGHT)) {
            ISkill BerserkX = SkillFactory.getSkill(DarkKnight.BERSERK);
            final int skilllevel = getSkillLevel(BerserkX);
            if (skilllevel > 0) {
                Berserk = chr.getHp() * 100 / chr.getMaxHp() < BerserkX.getEffect(skilllevel).getX();
                BerserkSchedule = TimerManager.getInstance().register(new Runnable() {

                    @Override
                    public void run() {
                        client.announce(MaplePacketCreator.showOwnBerserk(skilllevel, Berserk));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBerserk(getId(), skilllevel, Berserk), false);
                    }
                }, 5000, 3000);
            }
        }
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4 && messengerposition > -1) {
            try {
                WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
                wci.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(this, messengerposition), messengerposition);
                wci.updateMessenger(getMessenger().getId(), name, client.getChannel());
            } catch (Exception e) {
                client.getChannelServer().reconnectWorld();
            }
        }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (!monster.isControllerHasAggro()) {
            if (monster.getController() == this) {
                monster.setControllerHasAggro(true);
            } else {
                monster.switchController(this, true);
            }
        }
    }

    public void clearDoors() {
        doors.clear();
    }

    public void clearSavedLocation(SavedLocationType type) {
        if (savedLocations.containsKey(type)) {
            savedLocations.remove(type);
        }
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        if (monster == getPvpMonster()) {
            return;
        }
        monster.setController(this);
        if (controlled != null) {
            controlled.add(monster);
            client.announce(MaplePacketCreator.controlMonster(monster, false, aggro));
        }
    }

    public int countItem(int itemid) {
        return inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
    }

    public void decreaseBattleshipHp(int decrease) {
        this.battleshipHp -= decrease;
        if (battleshipHp <= 0) {
            this.battleshipHp = 0;
            ISkill battleship = SkillFactory.getSkill(Corsair.BATTLE_SHIP);
            int cooldown = battleship.getEffect(getSkillLevel(battleship)).getCooldown();
            getClient().announce(MaplePacketCreator.skillCooldown(Corsair.BATTLE_SHIP, cooldown));
            addCooldown(Corsair.BATTLE_SHIP, System.currentTimeMillis(), cooldown * 1000, TimerManager.getInstance().schedule(new CancelCooldownAction(this, Corsair.BATTLE_SHIP), cooldown * 1000));
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            resetBattleshipHp();
        }
    }

    public void decreaseReports() {
        this.possibleReports--;
    }

    public void deleteGuild(int guildId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?");
            ps.setInt(1, guildId);
            ps.execute();
            ps.close();
            ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            System.out.print("Error deleting guild: " + ex);
        }
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public void giveBuff(int skillid) {
        SkillFactory.getSkill(skillid).getEffect(1).applyTo(this);
        this.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(SkillFactory.getSkill(skillid).getEffect(1).getSourceId(), 2));
        this.getMap().broadcastMessage(this, MaplePacketCreator.showBuffeffect(getId(), SkillFactory.getSkill(skillid).getEffect(1).getSourceId(), 2), false);

    }

    public void giveBuff(int skillid, int level) {
        SkillFactory.getSkill(skillid).getEffect(level).applyToPetBuff(this);
        this.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(SkillFactory.getSkill(skillid).getEffect(level).getSourceId(), 2));
        this.getMap().broadcastMessage(this, MaplePacketCreator.showBuffeffect(getId(), SkillFactory.getSkill(skillid).getEffect(level).getSourceId(), 2), false);

    }

    public void giveMaxBuff(int skillid) {
        int level = SkillFactory.getSkill(skillid).getMaxLevel();
        SkillFactory.getSkill(skillid).getEffect(level).applyToPetBuff(this);
        this.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(SkillFactory.getSkill(skillid).getEffect(level).getSourceId(), 2));
        this.getMap().broadcastMessage(this, MaplePacketCreator.showBuffeffect(getId(), SkillFactory.getSkill(skillid).getEffect(level).getSourceId(), 2), false);

    }

    private void deregisterBuffStats(List<MapleBuffStat> stats) {
        synchronized (stats) {
            List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<MapleBuffStatValueHolder>(stats.size());
            for (MapleBuffStat stat : stats) {
                MapleBuffStatValueHolder mbsvh = effects.get(stat);
                if (mbsvh != null) {
                    effects.remove(stat);
                    boolean addMbsvh = true;
                    for (MapleBuffStatValueHolder contained : effectsToCancel) {
                        if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect) {
                            addMbsvh = false;
                        }
                    }
                    if (addMbsvh) {
                        effectsToCancel.add(mbsvh);
                    }
                    if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET) {
                        int summonId = mbsvh.effect.getSourceId();
                        MapleSummon summon = summons.get(summonId);
                        if (summon != null) {
                            getMap().broadcastMessage(MaplePacketCreator.removeSummon(summon, true), summon.getPosition());
                            getMap().removeMapObject(summon);
                            removeVisibleMapObject(summon);
                            summons.remove(summonId);
                        }
                        if (summon.getSkill() == DarkKnight.BEHOLDER) {
                            if (beholderHealingSchedule != null) {
                                beholderHealingSchedule.cancel(false);
                                beholderHealingSchedule = null;
                            }
                            if (beholderBuffSchedule != null) {
                                beholderBuffSchedule.cancel(false);
                                beholderBuffSchedule = null;
                            }
                        }
                    } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                        dragonBloodSchedule.cancel(false);
                        dragonBloodSchedule = null;
                    }
                }
            }
            for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
                if (getBuffStats(cancelEffectCancelTasks.effect, cancelEffectCancelTasks.startTime).isEmpty()) {
                    cancelEffectCancelTasks.schedule.cancel(false);
                }
            }
        }
    }

    public void disableDoor() {
        canDoor = false;
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                canDoor = true;
            }
        }, 5000);
    }

    public void disbandGuild() {
        if (guildid < 1 || guildrank != 1) {
            return;
        }
        try {
            client.getChannelServer().getWorldInterface().disbandGuild(guildid);
        } catch (Exception e) {
        }
    }

    public void dispel() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<MapleBuffStatValueHolder>(effects.values())) {
            if (mbsvh.effect.isSkill()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void dispelDebuffs() {
        List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
        for (MapleDisease disease : diseases) {
            if (disease == MapleDisease.WEAKEN || disease != MapleDisease.DARKNESS || disease != MapleDisease.SEAL || disease != MapleDisease.POISON) {
                disease_.add(disease);
                client.announce(MaplePacketCreator.cancelDebuff(disease_));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
                disease_.clear();
            } else {
                return;
            }
        }
        this.diseases.clear();
    }

    public void dispelSeduce() {
        List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
        for (MapleDisease disease : diseases) {
            if (disease == MapleDisease.SEDUCE) {
                disease_.add(disease);
                client.announce(MaplePacketCreator.cancelDebuff(disease_));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
                disease_.clear();
            }
        }
        this.diseases.clear();
    }

    public void dispelSkill(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (skillid == 0) {
                if (mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() % 10000000 == 1004 || dispelSkills(mbsvh.effect.getSourceId()))) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            } else if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    private boolean dispelSkills(int skillid) {
        switch (skillid) {
            case DarkKnight.BEHOLDER:
            // case FPArchMage.
            // case ILArchMage.IFRIT:
            case Priest.SUMMON_DRAGON:
            case Bishop.BAHAMUT:
            case Ranger.PUPPET:
            case Ranger.SILVER_HAWK:
            case Sniper.PUPPET:
            case Sniper.GOLDEN_EAGLE:
            case Hermit.SHADOW_PARTNER:
                return true;
            default:
                return false;
        }
    }

    public void dispelSeal() {
        List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
        for (MapleDisease disease : diseases) {
            if (disease == MapleDisease.SEAL) {
                disease_.add(disease);
                client.announce(MaplePacketCreator.cancelDebuff(disease_));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
                disease_.clear();
            }
        }
        this.diseases.clear();
    }

    public void doHurtHp() {
        if (this.getInventory(MapleInventoryType.EQUIPPED).findById(getMap().getHPDecProtect()) != null) {
            return;
        }
        addHP(-getMap().getHPDec());
        hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                doHurtHp();
            }
        }, 10000);
    }

    public void dropMessage(String message) {
        dropMessage(0, message);
    }

    public void dropMessage(int type, String message) {
        client.announce(MaplePacketCreator.serverNotice(type, message));
    }

    public String emblemCost() {
        return nf.format(MapleGuild.CHANGE_EMBLEM_COST);
    }

    public List<ScheduledFuture<?>> getTimers() {
        return timers;
    }

    private void enforceMaxHpMp() {
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>(2);
        if (getMp() > getCurrentMaxMp()) {
            setMp(getMp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(getMp())));
        }
        if (getHp() > getCurrentMaxHp()) {
            setHp(getHp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(getHp())));
        }
        if (stats.size() > 0) {
            client.announce(MaplePacketCreator.updatePlayerStats(getClient(), stats));
        }
    }

    public void enteredScript(String script, int mapid) {
        if (!entered.containsKey(mapid)) {
            entered.put(mapid, script);
        }
    }

    public void equipChanged() {
        getMap().broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        recalcLocalStats();
        enforceMaxHpMp();
        //saveToDB(true);
        if (getMessenger() != null) {
            WorldChannelInterface wci = client.getChannelServer().getWorldInterface();
            try {
                wci.updateMessenger(getMessenger().getId(), getName(), client.getChannel());
            } catch (Exception e) {
                client.getChannelServer().reconnectWorld();
            }
        }
    }

    public ScheduledFuture<?> getExpirationTask() {
        return expiretask;
    }

    public void expirationTask() {
        expiretask = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                long expiration, currenttime = System.currentTimeMillis();
                Set<ISkill> keys = getSkills().keySet();
                for (Iterator<ISkill> i = keys.iterator(); i.hasNext();) {
                    ISkill key = i.next();
                    SkillEntry skill = getSkills().get(key);
                    if (skill.expiration != -1 && skill.expiration < currenttime) {
                        changeSkillLevel(key, (byte) -1, 0, -1);
                    }
                }

                List<IItem> toberemove = new ArrayList<IItem>();
                for (MapleInventory inv : inventory) {
                    for (IItem item : inv.list()) {
                        expiration = item.getExpiration();
                        if (expiration != -1 && (expiration < currenttime) && ((item.getFlag() & ItemConstants.LOCK) == ItemConstants.LOCK)) {
                            byte aids = item.getFlag();
                            aids &= ~(ItemConstants.LOCK);
                            item.setFlag(aids); //Probably need a check, else people can make expiring items into permanent items...
                            item.setExpiration(-1);
                            forceUpdateItem(inv.getType(), item);   //TEST :3
                        } else if (expiration != -1 && expiration < currenttime) {
                            client.announce(MaplePacketCreator.itemExpired(item.getItemId()));
                            toberemove.add(item);
                        }
                    }
                    for (IItem item : toberemove) {
                        MapleInventoryManipulator.removeFromSlot(client, inv.getType(), item.getPosition(), item.getQuantity(), true);
                    }
                    toberemove.clear();
                }
                //saveToDB(true);
            }
        }, 60000);
    }

    public enum FameStatus {

        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public void forceUpdateItem(MapleInventoryType type, IItem item) {
        client.announce(MaplePacketCreator.clearInventoryItem(type, item.getPosition(), false));
        client.announce(MaplePacketCreator.addInventorySlot(type, item, false));
    }

    public void gainGachaExp() {
        int expgain = 0;
        int currentgexp = gachaexp.get();
        if ((currentgexp + exp.get()) >= ExpTable.getExpNeededForLevel(level)) {
            expgain += ExpTable.getExpNeededForLevel(level) - exp.get();
            int nextneed = ExpTable.getExpNeededForLevel(level + 1);
            if ((currentgexp - expgain) >= nextneed) {
                expgain += nextneed;
            }
            this.gachaexp.set(currentgexp - expgain);
        } else {
            expgain = this.gachaexp.getAndSet(0);
        }
        gainExp(expgain, false, false);
        updateSingleStat(MapleStat.GACHAEXP, this.gachaexp.get());
    }

    public void gainGachaExp(int gain) {
        updateSingleStat(MapleStat.GACHAEXP, gachaexp.addAndGet(gain));
    }

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, show, inChat, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat, boolean white) {
        int equip = (gain / 10) * pendantExp;
        int total = gain + equip;
        if (level < getMaxLevel()) {
            if ((long) this.exp.get() + (long) total > (long) Integer.MAX_VALUE) {
                int gainFirst = ExpTable.getExpNeededForLevel(level) - this.exp.get();
                total -= gainFirst + 1;
                this.gainExp(gainFirst + 1, false, inChat, white);
            }
            if (show && gain > 0) {
                boolean addBonus = this.addEveryThirdMonsterBonus();
                if (addBonus) {
                    total += (int) (total * 1.50);
                    client.announce(MaplePacketCreator.getBonus3rdMoExp(gain));
                } else {
                    client.announce(MaplePacketCreator.getShowExpGain(gain, equip, inChat, white));
                }
            }
            exp.addAndGet(total);
            updateSingleStat(MapleStat.EXP, this.exp.get());
            if (exp.get() >= ExpTable.getExpNeededForLevel(level)) {
                levelUp(true);
                int need = ExpTable.getExpNeededForLevel(level);
                if (exp.get() >= need) {
                    setExp(need - 1);
                    updateSingleStat(MapleStat.EXP, need);
                }
            }
        }
        for (MaplePokemon p : getAllPokemon()) {
            //   p.giveExpToPokemon(gain / 2);
        }
    }

    public void gainFame(int delta) {
        this.addFame(delta);
        this.updateSingleStat(MapleStat.FAME, this.fame);
    }

    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
        if (meso.get() + gain < 0) {
            client.announce(MaplePacketCreator.enableActions());
            return;
        }
        updateSingleStat(MapleStat.MESO, meso.addAndGet(gain), enableActions);
        if (show) {
            client.announce(MaplePacketCreator.getShowMesoGain(gain, inChat));
        }
    }

    public void setMeso(int delta) {
        this.meso.set(delta);
    }

    public void genericGuildMessage(int code) {
        this.client.announce(MaplePacketCreator.genericGuildMessage((byte) code));
    }

    public int getAccountID() {
        return accountid;
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        List<PlayerBuffValueHolder> ret = new ArrayList<PlayerBuffValueHolder>();
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect));
        }
        return ret;
    }

    public List<PlayerCoolDownValueHolder> getAllCooldowns() {
        List<PlayerCoolDownValueHolder> ret = new ArrayList<PlayerCoolDownValueHolder>();
        for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
            ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
        }
        return ret;
    }

    public int getAllianceRank() {
        return this.allianceRank;
    }

    public int getAllowWarpToId() {
        return warpToId;
    }

    public static String getAriantRoomLeaderName(int room) {
        return ariantroomleader[room];
    }

    public static int getAriantSlotsRoom(int room) {
        return ariantroomslot[room];
    }

    public int getBattleshipHp() {
        return battleshipHp;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    public static Map<String, String> getCharacterFromDatabase(String name) {
        Map<String, String> character = new LinkedHashMap<String, String>();

        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `id`, `accountid`, `name` FROM `characters` WHERE `name` = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }

            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                character.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
            }

            rs.close();
            ps.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return character;
    }

    public static boolean isInUse(String name) {
        return getCharacterFromDatabase(name) != null;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Long.valueOf(mbsvh.startTime);
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Integer.valueOf(mbsvh.value);
    }

    public int getBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return -1;
        }
        return mbsvh.effect.getSourceId();
    }

    private List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
        List<MapleBuffStat> stats = new ArrayList<MapleBuffStat>();
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet()) {
            if (stateffect.getValue().effect.sameSource(effect) && (startTime == -1 || startTime == stateffect.getValue().startTime)) {
                stats.add(stateffect.getKey());
            }
        }
        return stats;
    }

    public int getChair() {
        return chair;
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public MapleClient getClient() {
        return client;
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public Collection<MapleMonster> getControlledMonsters() {
        return Collections.unmodifiableCollection(controlled);
    }

    public List<MapleRing> getCrushRings() {
        Collections.sort(crushRings);
        return crushRings;
    }

    public int getCurrentCI() {
        return ci;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getCurrentMaxHp() {
        return localmaxhp;
    }

    public int getCurrentMaxMp() {
        return localmaxmp;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getCurrentType() {
        return currentType;
    }

    public int getDex() {
        return dex;
    }

    public List<MapleDisease> getDiseases() {
        synchronized (diseases) {
            return Collections.unmodifiableList(diseases);
        }
    }

    public int getDojoEnergy() {
        return dojoEnergy;
    }

    public boolean getDojoParty() {
        return dojoParty;
    }

    public int getDojoPoints() {
        return dojoPoints;
    }

    public int getDojoStage() {
        return dojoStage;
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList<MapleDoor>(doors);
    }

    public int getDropRate() {
        return dropRate;
    }

    public int getEnergyBar() {
        return energybar;
    }

    public EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public ArrayList<Integer> getExcluded() {
        return excluded;
    }

    public int getExp() {
        return exp.get();
    }

    public int getGachaExp() {
        return gachaexp.get();
    }

    public int getExpRate() {
        return expRate;
    }

    public int getFace() {
        return face;
    }

    public int getFame() {
        return fame;
    }

    public MapleFamily getFamily() {
        return family;
    }

    public void setFamily(MapleFamily f) {
        this.family = f;
    }

    public int getFamilyId() {
        return familyId;
    }

    public boolean getFinishedDojoTutorial() {
        return finishedDojoTutorial;
    }

    public List<MapleRing> getFriendshipRings() {
        Collections.sort(friendshipRings);
        return friendshipRings;
    }

    public int getGender() {
        return gender;
    }

    public boolean isMale() {
        return getGender() == 0;
    }

    public MapleGuild getGuild() {
        try {
            return client.getChannelServer().getWorldInterface().getGuild(getGuildId(), this.getMGC());
        } catch (Exception ex) {
            return null;
        }
    }

    public int getGuildId() {
        return guildid;
    }

    public int getGuildRank() {
        return guildrank;
    }

    public int getHair() {
        return hair;
    }

    public HiredMerchant getHiredMerchant() {
        return hiredMerchant;
    }

    public int getHp() {
        return hp;
    }

    public int getHpMpApUsed() {
        return hpMpApUsed;
    }

    public int getId() {
        return id;
    }

    public static int getIdByName(String name) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT id FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int id = rs.getInt("id");
            rs.close();
            ps.close();
            return id;
        } catch (Exception e) {
        }
        return -1;
    }

    public static String getNameById(int id) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name FROM characters WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            String name = rs.getString("name");
            rs.close();
            ps.close();
            return name;
        } catch (Exception e) {
        }
        return null;
    }

    public int getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public int getInt() {
        return int_;
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }

    public MapleJob getJob() {
        return job;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public int getJobType() {
        return job.getId() / 1000;
    }

    public Map<Integer, MapleKeyBinding> getKeymap() {
        return keymap;
    }

    public long getLastHealed() {
        return lastHealed;
    }

    public long getLastUsedCashItem() {
        return lastUsedCashItem;
    }

    public int getLevel() {
        return level;
    }

    public int getLuk() {
        return luk;
    }

    public int getFh() {
        if (getMap().getFootholds().findBelow(this.getPosition()) == null) {
            return 0;
        } else {
            return getMap().getFootholds().findBelow(this.getPosition()).getId();
        }
    }

    public MapleMap getMap() {
        return map;
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }

    public int getMarkedMonster() {
        return markedMonster;
    }

    public MapleRing getMarriageRing() {
        return marriageRing;
    }

    public int getMarried() {
        return married;
    }

    public int getMasterLevel(ISkill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).masterlevel;
    }

    public int getMaxHp() {
        return maxhp;
    }
    
    public int getRealHp(){
        return realhp;
    }

    public int getMaxLevel() {
        return isCygnus() ? 120 : 200;
    }

    public int getMaxMp() {
        return maxmp;
    }
    
    public int getRealMp() {
        return realmp;
    }

    public int getMeso() {
        return meso.get();
    }

    public int getMerchantMeso() {
        return merchantmeso;
    }

    public int getMesoRate() {
        return mesoRate;
    }

    public int getMesosTraded() {
        return mesosTraded;
    }

    public int getMessengerPosition() {
        return messengerposition;
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public MaplePartyCharacter getMPC() {
        //if (mpc == null) mpc = new MaplePartyCharacter(this);
        return mpc;
    }

    public void setMPC(MaplePartyCharacter mpc) {
        this.mpc = mpc;
    }

    public MapleMiniGame getMiniGame() {
        return miniGame;
    }

    public int getMiniGamePoints(String type, boolean omok) {
        if (omok) {
            if (type.equals("wins")) {
                return omokwins;
            } else if (type.equals("losses")) {
                return omoklosses;
            } else {
                return omokties;
            }
        } else {
            if (type.equals("wins")) {
                return matchcardwins;
            } else if (type.equals("losses")) {
                return matchcardlosses;
            } else {
                return matchcardties;
            }
        }
    }

    public MonsterBook getMonsterBook() {
        return monsterbook;
    }

    public int getMonsterBookCover() {
        return bookCover;
    }

    public MapleMount getMount() {
        return maplemount;
    }

    public int getMp() {
        return mp;
    }

    public MapleMessenger getMessenger() {
        return messenger;
    }

    public String getName() {
        return name;
    }

    public int getNextEmptyPetIndex() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                return i;
            }
        }
        return 3;
    }

    public int getNoPets() {
        int ret = 0;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                ret++;
            }
        }
        return ret;
    }

    public int getNumControlledMonsters() {
        return controlled.size();
    }

    public MapleParty getParty() {
        return party;
    }

    public int getPartyId() {
        return (party != null ? party.getId() : -1);
    }

    public MaplePlayerShop getPlayerShop() {
        return playerShop;
    }

    public MaplePet[] getPets() {
        return pets;
    }

    public MaplePet getPet(int index) {
        return pets[index];
    }

    public byte getPetIndex(int petId) {
        for (byte i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == petId) {
                    return i;
                }
            }
        }
        return -1;
    }

    public byte getPetIndex(MaplePet pet) {
        for (byte i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getPossibleReports() {
        return possibleReports;
    }

    public final byte getQuestStatus(final int quest) {
        for (final MapleQuestStatus q : quests.values()) {
            if (q.getQuest().getId() == quest) {
                return (byte) q.getStatus().getId();
            }
        }
        return 0;
    }

    public MapleQuestStatus getQuest(MapleQuest quest) {
        if (!quests.containsKey(quest)) {
            return new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
        }
        return quests.get(quest);
    }

    public void completeAchievement(int achievementid) {
        MapleAchievementLibrary lib = MapleAchievementLibrary.getById(achievementid);
        MapleAchievement achievement = new MapleAchievement(0, 0, 0, 0);
        achievement = new MapleAchievement(getId(), lib);
        achievement.completeMapleAchievement(achievement, lib.getAchievementName());
    }

    public boolean needQuestItem(int questid, int itemid) {
        if (questid <= 0) {
            return true; //For non quest items :3
        }
        MapleQuest quest = MapleQuest.getInstance(questid);
        return getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid) <= quest.getItemAmountNeeded(itemid);
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public int getRemainingAp() {
        return remainingAp;
    }

    public int getRemainingSp() {
        return remainingSp;
    }

    public int getSavedLocation(String type) {
        SavedLocation sl = savedLocations.get(SavedLocationType.fromString(type));
        if (sl == null) {
            return 102000000;
        }
        int m = sl.getMapId();
        if (!SavedLocationType.fromString(type).equals(SavedLocationType.WORLDTOUR)) {
            clearSavedLocation(SavedLocationType.fromString(type));
        }
        return m;
    }

    public void reloadSavedLocation() {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM savedlocations WHERE characterid = ?");
            ps.setInt(1, getId());
            System.out.println("charid : " + getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SavedLocation sl = new SavedLocation(rs.getInt("map"), rs.getInt("portal"));
            }
        } catch (SQLException e) {
        }
    }

    public String getSearch() {
        return search;
    }

    public MapleShop getShop() {
        return shop;
    }

    public Map<ISkill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public int getSkillLevel(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return 0;
        }
        return ret.skillevel;
    }

    public byte getSkillLevel(ISkill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).skillevel;
    }

    public long getSkillExpiration(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return -1;
        }
        return ret.expiration;
    }

    public long getSkillExpiration(ISkill skill) {
        if (skills.get(skill) == null) {
            return -1;
        }
        return skills.get(skill).expiration;
    }

    public MapleSkinColor getSkinColor() {
        return skinColor;
    }

    public int getSlot() {
        return slots;
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public final int getStartedQuestsSize() {
        int i = 0;
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                if (q.getQuest().getInfoNumber() > 0) {
                    i++;
                }
                i++;
            }
        }
        return i;
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect;
    }

    public MapleStorage getStorage() {
        return storage;
    }

    public int getStr() {
        return str;
    }

    public Map<Integer, MapleSummon> getSummons() {
        return Collections.synchronizedMap(summons);
    }

    public int getTotalLuk() {
        return localluk;
    }

    public int getTotalMagic() {
        return magic;
    }

    public int getTotalWatk() {
        return watk;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public int getVanquisherKills() {
        return vanquisherKills;
    }

    public int getVanquisherStage() {
        return vanquisherStage;
    }

    public Collection<MapleMapObject> getVisibleMapObjects() {
        return Collections.unmodifiableCollection(visibleMapObjects);
    }

    public int getWorld() {
        return world;
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        //  int time = (int) ((length + starttime) - System.currentTimeMillis());
        //addCooldown(skillid, System.currentTimeMillis(), time, TimerManager.getInstance().schedule(new CancelCooldownAction(this, skillid), time));
    }

    public void giveDebuff(MapleDisease disease, MobSkill skill) {
        if (this.isAlive() && diseases.size() < 2) {
            List<Pair<MapleDisease, Integer>> disease_ = new ArrayList<Pair<MapleDisease, Integer>>();
            disease_.add(new Pair<MapleDisease, Integer>(disease, Integer.valueOf(skill.getX())));
            this.diseases.add(disease);
            client.announce(MaplePacketCreator.giveDebuff(disease_, skill));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(this.id, disease_, skill), false);
        }
    }

    public int gmLevel() {
        return gmLevel;
    }

    public String guildCost() {
        return nf.format(MapleGuild.CREATE_GUILD_COST);
    }

    private void guildUpdate() {
        if (this.guildid < 1) {
            return;
        }
        mgc.setLevel(level);
        mgc.setJobId(job.getId());
        try {
            this.client.getChannelServer().getWorldInterface().memberLevelJobUpdate(this.mgc);
            int allianceId = getGuild().getAllianceId();
            if (allianceId > 0) {
                client.getChannelServer().getWorldInterface().allianceMessage(allianceId, MaplePacketCreator.updateAllianceJobLevel(this), getId(), -1);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void handleEnergyChargeGain() { // to get here energychargelevel has to be > 0
        ISkill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
        MapleStatEffect ceffect = null;
        ceffect = energycharge.getEffect(getSkillLevel(energycharge));
        TimerManager tMan = TimerManager.getInstance();
        if (energybar < 10000) {
            energybar += 102;
            if (energybar > 10000) {
                energybar = 10000;
            }
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ENERGY_CHARGE, energybar));
            setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
            client.announce(MaplePacketCreator.giveBuff(energybar, 0, stat, false));
            client.announce(MaplePacketCreator.showOwnBuffEffect(energycharge.getId(), 2));
            getMap().broadcastMessage(this, MaplePacketCreator.showBuffeffect(id, energycharge.getId(), 2));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(energybar, stat));
        }
        if (energybar >= 10000 && energybar < 11000) {
            energybar = 15000;
            final MapleCharacter chr = this;
            tMan.schedule(new Runnable() {

                @Override
                public void run() {
                    energybar = 0;
                    List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ENERGY_CHARGE, energybar));
                    setBuffedValue(MapleBuffStat.ENERGY_CHARGE, energybar);
                    getMap().broadcastMessage(chr, MaplePacketCreator.giveForeignBuff(id, stat));
                }
            }, ceffect.getDuration());
        }
    }

    public void handleOrbconsume() {
        int skillid = isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
        ISkill combo = SkillFactory.getSkill(skillid);
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        client.announce(MaplePacketCreator.giveBuff(skillid, combo.getEffect(getSkillLevel(combo)).getDuration() + (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis())), stat, false));
        getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat), false);
    }

    public boolean hasEntered(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEntered(String script, int mapId) {
        if (entered.containsKey(mapId)) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(Integer.valueOf(to.getId()));
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public boolean hasMerchant() {
        return hasMerchant;
    }

    public boolean haveItem(int itemid) {
        return getItemQuantity(itemid, false) > 0;
    }

    public void increaseGuildCapacity() { //hopefully nothing is null
        if (getMeso() < getGuild().getIncreaseGuildCost(getGuild().getCapacity())) {
            dropMessage(1, "You don't have enough mesos.");
            return;
        }
        try {
            client.getChannelServer().getWorldInterface().increaseGuildCapacity(guildid);
        } catch (RemoteException e) {
            client.getChannelServer().reconnectWorld();
            return;
        }
        gainMeso(-getGuild().getIncreaseGuildCost(getGuild().getCapacity()), true, false, false);
    }

    public boolean isActiveBuffedValue(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                return true;
            }
        }
        return false;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public boolean isBuffFrom(MapleBuffStat stat, ISkill skill) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return false;
        }
        return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
    }

    public boolean isCygnus() {
        return getJob().getId() >= MapleJob.NOBLESSE.getId() && getJob().getId() < 1600;
    }

    public boolean isAran() {
        return getJob().getId() >= 2000 && getJob().getId() <= 2112;
    }

    public boolean isBeginnerJob() {
        return (getJob().getId() == 0 || getJob().getId() == 1000 || getJob().getId() == 2000) && getLevel() < 11;
    }

    public boolean isGM() {
        return gmLevel > 0;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        return visibleMapObjects.contains(mo);
    }

    public boolean isPartyLeader() {
        return party.getLeader() == party.getMemberById(getId());
    }

    public void leaveMap() {
        if (controlled != null && visibleMapObjects != null) {
            controlled.clear();
            visibleMapObjects.clear();
            if (chair != 0) {
                chair = 0;
            }
            if (hpDecreaseTask != null) {
                hpDecreaseTask.cancel(false);
            }
        }
    }

    public void levelUp(boolean takeexp) {
        ISkill improvingMaxHP = null;
        ISkill improvingMaxMP = null;
        int improvingMaxHPLevel = 0;
        int improvingMaxMPLevel = 0;

        if (isBeginnerJob()) {
            remainingAp = 0;
            if (getLevel() < 6) {
                str += 5;
            } else {
                str += 4;
                dex += 1;
            }
        } else {
            remainingAp += 5;
            if (isCygnus() && level < 70) {
                remainingAp++;
            }
        }
        if (job == MapleJob.BEGINNER || job == MapleJob.NOBLESSE || job == MapleJob.LEGEND) {
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(10, 12);
        } else if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(DawnWarrior.MAX_HP_INCREASE) : SkillFactory.getSkill(Swordsman.IMPROVED_MAX_HP_INCREASE);
            if (job.isA(MapleJob.CRUSADER)) {
                improvingMaxMP = SkillFactory.getSkill(1210000);
            } else if (job.isA(MapleJob.DAWNWARRIOR2)) {
                improvingMaxMP = SkillFactory.getSkill(11110000);
            }
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(24, 28);
            maxmp += Randomizer.rand(4, 6);
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            improvingMaxMP = isCygnus() ? SkillFactory.getSkill(BlazeWizard.INCREASING_MAX_MP) : SkillFactory.getSkill(Magician.IMPROVED_MAX_MP_INCREASE);
            improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
            maxhp += Randomizer.rand(10, 14);
            maxmp += Randomizer.rand(22, 24);
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF) || (job.getId() > 1299 && job.getId() < 1500)) {
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(14, 16);
        } else if (job.isA(MapleJob.GM)) {
            maxhp = 99999;
            maxmp = 99999;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.IMPROVE_MAX_HP) : SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(22, 28);
            maxmp += Randomizer.rand(18, 23);
        } else if (job.isA(MapleJob.ARAN1)) {
            maxhp += Randomizer.rand(44, 48);
            int aids = Randomizer.rand(4, 8);
            maxmp += aids + Math.floor(aids * 0.1);
        } else if (job.isAnEvan()) {
            maxhp += Randomizer.rand(20, 30);
            maxmp += Randomizer.rand(40, 50);
        } else if (this.isDualBlade()) {
            maxhp += Randomizer.rand(10, 40);
            maxmp += Randomizer.rand(20, 40);
        } else if (this.getJob().getId() >= MapleJob.BATTLEMAGE1.getId() && getJob().getId() <= MapleJob.BATTLEMAGE4.getId()) {
            maxhp += Randomizer.rand(20, 50);
            maxmp += Randomizer.rand(30, 60);
        } else if (this.isJaguar()) {
            maxhp += Randomizer.rand(40, 90);
            maxmp += Randomizer.rand(20, 37);
        } else if (isMechanic()) {
            maxhp += Randomizer.rand(40, 90);
            maxmp += Randomizer.rand(10, 30);
        }
        // since you can now have 3x more hp..
        if (improvingMaxHPLevel > 0 && (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.DAWNWARRIOR1))) {
            maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
        }
        if (improvingMaxMPLevel > 0 && (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.CRUSADER) || job.isA(MapleJob.BLAZEWIZARD1))) {
            maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
        }
        maxmp += localint_ / 10;
        if (takeexp) {
            exp.addAndGet(-ExpTable.getExpNeededForLevel(level));
            if (exp.get() < 0) {
                exp.set(0);
            }
        }
        level++;
        if (level >= getMaxLevel()) {
            exp.set(0);
        }
        maxhp = Math.min(30000, maxhp);
        maxmp = Math.min(30000, maxmp);
        if (level == 200) {
            exp.set(0);
        }
        hp = maxhp;
        mp = maxmp;
        recalcLocalStats();
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(10);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, localmaxhp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, localmaxmp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.EXP, exp.get()));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.LEVEL, level));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, localmaxhp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, localmaxmp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.STR, str));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.DEX, dex));
        if (job.getId() % 1000 > 0) {
            remainingSp += 3;
            statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP, remainingSp));
        }
        client.announce(MaplePacketCreator.updatePlayerStats(getClient(), statup));
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0), false);
        recalcLocalStats();
        silentPartyUpdate();

        switch (level) {
            case 10:
                this.completeAchievement(MapleAchievementLibrary.REACHING_LEVEL_10);
                break;
            case 30:
                completeAchievement(MapleAchievementLibrary.REACHING_LEVEL_30);
                break;
            case 70:
                completeAchievement(MapleAchievementLibrary.REACHING_LEVEL_70);
                break;
            case 120:
                completeAchievement(MapleAchievementLibrary.REACHING_LEVEL_120);
                break;
            case 200:
                completeAchievement(MapleAchievementLibrary.REACHING_LEVEL_200);
                break;
        }
        autoJob();
        if (this.guildid > 0) {
            getGuild().broadcast(MaplePacketCreator.levelUpMessage(2, level, name), this.getId());
        }
        if (MapleInventoryManipulator.checkSpace(client, 4310000, (short) 1, "")) {
            MapleInventoryManipulator.addById(client, 4310000, (short) 1);
        }
        guildUpdate();
        if (getLevel() >= 200 && autoreborn) {
            if (getJobId() >= 0 && getJobId() <= 522) {
                doReborn("Explorer");
            } else if (getJobId() >= 1000 && getJobId() <= 1512) {
                doReborn("Cygnus");
            } else if (getJobId() >= 2000) {
                doReborn("Aran");
            }
        }
    }

    public int getJobId() {
        return getJob().getId();
    }

    public MapleDonator getDonator() {
        return md;
    }

    public void setBuddyOfflineByName(String name) {
        for (BuddylistEntry ble : this.getBuddylist().getBuddies()) {
            if (ble.getName().equals(name)) {
                ble.setOffline();
                break;
            }
        }
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException {
        MapleCharacter ret = new MapleCharacter();
        ret.client = client;
        ret.id = charid;
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
        ps.setInt(1, charid);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            rs.close();
            ps.close();
            throw new RuntimeException("Loading char failed (not found)");
        }
        try {
            ret.achievementProgress = new MapleAchievementProgress(charid); // this will autoload from db :3
            ret.achievements = MapleAchievement.loadAllAchievements(charid);
            ret.name = rs.getString("name");
            ret.level = rs.getInt("level");
            ret.fame = rs.getInt("fame");
            ret.str = rs.getInt("str");
            ret.dex = rs.getInt("dex");
            ret.int_ = rs.getInt("int");
            ret.luk = rs.getInt("luk");
            ret.exp.set(rs.getInt("exp"));
            ret.gachaexp.set(rs.getInt("gachaexp"));
            ret.hp = rs.getInt("hp");
            ret.maxhp = rs.getInt("maxhp");
            ret.mp = rs.getInt("mp");
            ret.maxmp = rs.getInt("maxmp");
            ret.hpMpApUsed = rs.getInt("hpMpUsed");
            ret.hasMerchant = rs.getInt("HasMerchant") == 1;
            ret.remainingSp = rs.getInt("sp");
            ret.isDB = rs.getInt("isDB") == 1;
            int mountexp = rs.getInt("mountexp");
            int mountlevel = rs.getInt("mountlevel");
            int mounttiredness = rs.getInt("mounttiredness");
            ret.remainingAp = rs.getInt("ap");
            ret.meso.set(rs.getInt("meso"));
            // ret.merchantmeso = rs.getInt("MerchantMesos");
            ret.gmLevel = rs.getInt("gm");
            ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
            ret.gender = rs.getInt("gender");
            ret.job = MapleJob.getById(rs.getInt("job"));
            ret.table = new ExtendedSPTable(ret.job.getId());
            if (ret.isEvan()) {
                ret.evanDragon = new MapleDragon(ret);
            }
            ret.finishedDojoTutorial = rs.getInt("finishedDojoTutorial") == 1;
            ret.vanquisherKills = rs.getInt("vanquisherKills");
            ret.omokwins = rs.getInt("omokwins");
            ret.omoklosses = rs.getInt("omoklosses");
            ret.omokties = rs.getInt("omokties");
            ret.matchcardwins = rs.getInt("matchcardwins");
            ret.matchcardlosses = rs.getInt("matchcardlosses");
            ret.matchcardties = rs.getInt("matchcardties");
            ret.hair = rs.getInt("hair");
            ret.face = rs.getInt("face");
            ret.accountid = rs.getInt("accountid");
            ret.mapid = rs.getInt("map");
            ret.initialSpawnPoint = rs.getInt("spawnpoint");
            ret.world = rs.getInt("world");
            // ret.rank = rs.getInt("rank");
            // ret.rankMove = rs.getInt("rankMove");
            //ret.jobRank = rs.getInt("jobRank");
            //ret.jobRankMove = rs.getInt("jobRankMove");
            //ret.guildid = rs.getInt("guildid");
            //ret.guildrank = rs.getInt("guildrank");
            //ret.allianceRank = rs.getInt("allianceRank");
            ret.familyId = rs.getInt("familyId");
            ret.bookCover = rs.getInt("monsterbookcover");
            ret.monsterbook = new MonsterBook();
            ret.monsterbook.loadCards(charid);
            ret.vanquisherStage = rs.getInt("vanquisherStage");
            ret.dojoPoints = rs.getInt("dojoPoints");
            ret.dojoStage = rs.getInt("lastDojoStage");
            ret.gMode = GameMode.getById(rs.getInt("GameMode"));
            ret.pvpkills = rs.getInt("pvpkills");
            ret.pvpdeaths = rs.getInt("pvpdeaths");
            ret.campaignProgress = rs.getShort("campaignProgress");
            ret.familyId = charid;
            // ret.isMWLB = rs.getByte("mwlb") == (byte)1;
            ret.reborns = rs.getInt("reborns");
            ret.mute = rs.getInt("mute");
            if (ret.guildid > 0) {
                ret.mgc = new MapleGuildCharacter(ret);
            }
            int buddyCapacity = rs.getInt("buddyCapacity");
            ret.buddylist = new BuddyList(buddyCapacity);
            ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(96);
            ret.getInventory(MapleInventoryType.USE).setSlotLimit(96);
            ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(96);
            ret.getInventory(MapleInventoryType.ETC).setSlotLimit(96);
            for (Pair<IItem, MapleInventoryType> item : ItemFactory.INVENTORY.loadItems(ret.id, !channelserver)) {
                ret.getInventory(item.getRight()).addFromDB(item.getLeft());
                IItem itemz = item.getLeft();
                if (itemz.getPetId() > -1) {
                    MaplePet pet = itemz.getPet();
                    if (pet.isSummoned()) {
                        ret.addPet(pet);
                    }

                    continue;
                }
                if (item.getRight().equals(MapleInventoryType.EQUIP) || item.getRight().equals(MapleInventoryType.EQUIPPED)) {
                    IEquip equip = (IEquip) item.getLeft();
                    if (equip.getRingId() > -1) {
                        MapleRing ring = MapleRing.loadFromDb(equip.getRingId());
                        if (item.getRight().equals(MapleInventoryType.EQUIPPED)) {
                            ring.equip();
                        }
                        if (ring.getItemId() > 1112012) {
                            ret.addFriendshipRing(ring);
                        } else {
                            ret.addCrushRing(ring);
                        }
                    }
                }
            }
            if (channelserver) {
                MapleMapFactory mapFactory = client.getChannelServer().getMapFactory(gMode);
                ret.map = mapFactory.getMap(ret.mapid);
                if (ret.map == null) {
                    ret.map = mapFactory.getMap(100000000);
                }
                MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
                if (portal == null) {
                    portal = ret.map.getPortal(0);
                    ret.initialSpawnPoint = 0;
                }
                ret.setPosition(portal.getPosition());
                int partyid = rs.getInt("party");
                try {
                    MapleParty party = client.getChannelServer().getWorldInterface().getParty(partyid);
                    if (party != null) {
                        ret.mpc = party.getMemberById(ret.id);
                        if (ret.mpc != null) {
                            ret.party = party;
                        }
                    }
                } catch (RemoteException ex) {
                    client.getChannelServer().reconnectWorld();
                }
                int messengerid = rs.getInt("messengerid");
                int position = rs.getInt("messengerposition");
                if (messengerid > 0 && position < 4 && position > -1) {
                    try {
                        WorldChannelInterface wci = client.getChannelServer().getWorldInterface();
                        MapleMessenger messenger = wci.getMessenger(messengerid);
                        if (messenger != null) {
                            ret.messenger = messenger;
                            ret.messengerposition = position;
                        }
                    } catch (RemoteException ez) {
                        client.getChannelServer().reconnectWorld();
                    }
                }
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT mapid,vip FROM trocklocations WHERE characterid = ? LIMIT 15");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            byte v = 0;
            byte r = 0;
            while (rs.next()) {
                if (rs.getInt("vip") == 1) {
                    ret.viptrockmaps[v] = rs.getInt("mapid");
                    v++;
                } else {
                    ret.trockmaps[r] = rs.getInt("mapid");
                    r++;
                }
            }
            while (v < 10) {
                ret.viptrockmaps[v] = 999999999;
                v++;
            }
            while (r < 5) {
                ret.trockmaps[r] = 999999999;
                r++;
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT name FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            rs = ps.executeQuery();
            if (rs.next()) {
                ret.getClient().setAccountName(rs.getString("name"));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM eventstats WHERE characterid = ?");
            ps.setInt(1, ret.id);
            rs = ps.executeQuery();
            if (rs.next()) {
                ret.events = new MapleEvents(new RescueGaga(rs.getInt("rescuegaga")), new ArtifactHunt(rs.getInt("artifacthunt")));
            }
            rs.close();
            ps.close();
            ret.cashshop = new CashShop(ret.accountid, ret.id, ret.getJobType());
            ret.autoban = new AutobanManager(ret);
            ret.marriageRing = null; //for now
            ps = con.prepareStatement("SELECT name, level FROM characters WHERE accountid = ? AND id != ? ORDER BY level DESC limit 1");
            ps.setInt(1, ret.accountid);
            ps.setInt(2, charid);
            rs = ps.executeQuery();
            if (rs.next()) {
                ret.linkedName = rs.getString("name");
                ret.linkedLevel = rs.getInt("level");
            }
            rs.close();
            ps.close();
            if (channelserver) {
                ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                PreparedStatement pse = con.prepareStatement("SELECT * FROM questprogress WHERE queststatusid = ?");
                PreparedStatement psf = con.prepareStatement("SELECT mapid FROM medalmaps WHERE queststatusid = ?");
                while (rs.next()) {
                    MapleQuest q = MapleQuest.getInstance(rs.getShort("quest"));
                    MapleQuestStatus status = new MapleQuestStatus(q, MapleQuestStatus.Status.getById(rs.getInt("status")));
                    long cTime = rs.getLong("time");
                    if (cTime > -1) {
                        status.setCompletionTime(cTime * 1000);
                    }
                    status.setForfeited(rs.getInt("forfeited"));
                    ret.quests.put(q, status);
                    pse.setInt(1, rs.getInt("queststatusid"));
                    ResultSet rsProgress = pse.executeQuery();
                    while (rsProgress.next()) {
                        status.setProgress(rsProgress.getInt("progressid"), rsProgress.getString("progress"));
                    }
                    rsProgress.close();
                    psf.setInt(1, rs.getInt("queststatusid"));
                    ResultSet medalmaps = psf.executeQuery();
                    while (medalmaps.next()) {
                        status.addMedalMap(medalmaps.getInt("mapid"));
                    }
                    medalmaps.close();
                }
                rs.close();
                ps.close();
                pse.close();
                psf.close();

                ps = con.prepareStatement("SELECT skillid,skilllevel,masterlevel,expiration FROM skills WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.skills.put(SkillFactory.getSkill(rs.getInt("skillid")), new SkillEntry(rs.getByte("skilllevel"), rs.getInt("masterlevel"), rs.getLong("expiration")));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int position = rs.getInt("position");
                    SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                    ret.skillMacros[position] = macro;
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int key = rs.getInt("key");
                    int type = rs.getInt("type");
                    int action = rs.getInt("action");
                    ret.keymap.put(Integer.valueOf(key), new MapleKeyBinding(type, action));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `locationtype`,`map`,`portal` FROM savedlocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.savedLocations.put(SavedLocationType.fromString(rs.getString("locationtype")), new SavedLocation(rs.getInt("map"), rs.getInt("portal")));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                ret.lastfametime = 0;
                ret.lastmonthfameids = new ArrayList<Integer>(31);
                while (rs.next()) {
                    ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                    ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
                }
                rs.close();
                ps.close();
                ret.buddylist.loadFromDb(charid);
                ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid);
                ret.recalcLocalStats();
                ret.resetBattleshipHp();
                ret.silentEnforceMaxHpMp();
            }
            int mountid = ret.getJobType() * 10000000 + 1004;
            if (ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
                ret.maplemount = new MapleMount(ret, ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId(), mountid);
            } else {
                ret.maplemount = new MapleMount(ret, 0, mountid);
            }
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);
            ret.md = new MapleDonator(ret.getClient().getAccountName(), client.getAccID());
            ps = con.prepareStatement("SELECT * FROM maplepokemon WHERE masterName = ?");
            ps.setString(1, ret.getName());
            rs = ps.executeQuery();
            while (rs.next()) {
                int level = rs.getInt("level");
                int pokeId = rs.getInt("pid");
                int retexp = rs.getInt("exp");
                int uniqueID = rs.getInt("uniqueid");
                MaplePokemon newp = new MaplePokemon(MaplePokemonLibrary.getById(pokeId), ret, uniqueID, level, retexp);
                ret.addPokemon(newp);
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");
        return i;
    }

    private static class MapleBuffStatValueHolder {

        public MapleStatEffect effect;
        public long startTime;
        public int value;
        public ScheduledFuture<?> schedule;

        public MapleBuffStatValueHolder(MapleStatEffect effect, long startTime, ScheduledFuture<?> schedule, int value) {
            super();
            this.effect = effect;
            this.startTime = startTime;
            this.schedule = schedule;
            this.value = value;
        }
    }

    public static class MapleCoolDownValueHolder {

        public int skillId;
        public long startTime, length;
        public ScheduledFuture<?> timer;

        public MapleCoolDownValueHolder(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
            this.timer = timer;
        }
    }

    public void message(String m) {
        dropMessage(5, m);
    }

    public void yellowMessage(String m) {
        announce(MaplePacketCreator.sendYellowTip(m));
    }

    public Map<MapleQuest, MapleQuestStatus> getAllQuests() {
        return Collections.unmodifiableMap(quests);
    }

    public void mobKilled(int id) {
        for (MapleQuestStatus q : getAllQuests().values()) {
            if (q.getStatus() == MapleQuestStatus.Status.COMPLETED || q.getQuest().canComplete(this, null)) {
                continue;
            }
            if (q.progress(id)) {
                client.announce(MaplePacketCreator.updateQuest(q.getQuest().getId(), q.getProgress(id)));
                if (q.getQuest().canComplete(this, null)) {
                    client.getSession().write(MaplePacketCreator.getShowQuestCompletion(q.getQuest().getId()));
                }
                break;
            }
        }
    }

    public void mount(int id, int skillid) {
        maplemount = new MapleMount(this, id, skillid);
    }

    public void playerNPC(MapleCharacter v, int scriptId) {
        int npcId;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM playernpcs WHERE ScriptId = ?");
            ps.setInt(1, scriptId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps = con.prepareStatement("INSERT INTO playernpcs (name, hair, face, skin, x, cy, map, ScriptId, Foothold, rx0, rx1) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, v.getName());
                ps.setInt(2, v.getHair());
                ps.setInt(3, v.getFace());
                ps.setInt(4, v.getSkinColor().getId());
                ps.setInt(5, getPosition().x);
                ps.setInt(6, getPosition().y);
                ps.setInt(7, getMapId());
                ps.setInt(8, scriptId);
                ps.setInt(9, getMap().getFootholds().findBelow(getPosition()).getId());
                ps.setInt(10, getPosition().x + 50);
                ps.setInt(11, getPosition().x - 50);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                rs.next();
                npcId = rs.getInt(1);
                ps.close();
                ps = con.prepareStatement("INSERT INTO playernpcs_equip (NpcId, equipid, equippos) VALUES (?, ?, ?)");
                ps.setInt(1, npcId);
                for (IItem equip : getInventory(MapleInventoryType.EQUIPPED)) {
                    int position = Math.abs(equip.getPosition());
                    if ((position < 12 && position > 0) || (position > 100 && position < 112)) {
                        ps.setInt(2, equip.getItemId());
                        ps.setInt(3, equip.getPosition());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
                ps.close();
                rs.close();
                ps = con.prepareStatement("SELECT * FROM playernpcs WHERE ScriptId = ?");
                ps.setInt(1, scriptId);
                rs = ps.executeQuery();
                rs.next();
                PlayerNPCs pn = new PlayerNPCs(rs);
                for (ChannelServer channel : ChannelServer.getAllInstances()) {
                    MapleMap m = channel.getMapFactory(gMode).getMap(getMapId());
                    m.broadcastMessage(MaplePacketCreator.spawnPlayerNPC(pn));
                    m.broadcastMessage(MaplePacketCreator.getPlayerNPC(pn));
                    m.addMapObject(pn);
                }
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void playerDead(MapleCharacter... pvpkiller) {
        if (getDonator().isActive() && (int) (100.0 * Math.random()) >= 50) {
            dropMessage(5, "As a donator, there is a 50% chance that you will be saved from death.. You have been saved.");
            setHp(getMaxHp());
            updateSingleStat(MapleStat.HP, getMaxHp());
            return;
        }
        cancelAllBuffs();
        dispelDebuffs();
        if (getEventInstance() != null) {
            getEventInstance().playerKilled(this);
        }
        int[] charmID = {5130000, 4031283, 4140903};
        int possesed = 0;
        int i;
        for (i = 0; i < charmID.length; i++) {
            int quantity = getItemQuantity(charmID[i], false);
            if (possesed == 0 && quantity > 0) {
                possesed = quantity;
                break;
            }
        }
        if (possesed > 0) {
            message("You have used a safety charm, so your EXP points have not been decreased.");
            MapleInventoryManipulator.removeById(client, MapleItemInformationProvider.getInstance().getInventoryType(charmID[i]), charmID[i], 1, true, false);
        } else if (mapid > 925020000 && mapid < 925030000) {
            this.dojoStage = 0;
        } else if (mapid > 980000100 && mapid < 980000700) {
            getMap().broadcastMessage(this, MaplePacketCreator.CPQDied(this));
        } else if (getJob() != MapleJob.BEGINNER) { //Hmm...
            int XPdummy = ExpTable.getExpNeededForLevel(getLevel());
            if (getMap().isTown()) {
                XPdummy /= 100;
            }
            if (XPdummy == ExpTable.getExpNeededForLevel(getLevel())) {
                if (getLuk() <= 100 && getLuk() > 8) {
                    XPdummy *= (200 - getLuk()) / 2000;
                } else if (getLuk() < 8) {
                    XPdummy /= 10;
                } else {
                    XPdummy /= 20;
                }
            }
            if (getExp() > XPdummy) {
                gainExp(-XPdummy, false, false);
            } else {
                gainExp(-getExp(), false, false);
            }
        }
        if (getBuffedValue(MapleBuffStat.MORPH) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        }

        if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        }

        if (getChair() == -1) {
            setChair(0);
            client.announce(MaplePacketCreator.cancelChair(-1));
            getMap().broadcastMessage(this, MaplePacketCreator.showChair(getId(), 0), false);
        }
        client.announce(MaplePacketCreator.enableActions());
    }

    private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(false);
        }
        dragonBloodSchedule = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                addHP(-bloodEffect.getX());
                client.announce(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
                getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), bloodEffect.getSourceId(), 5), false);
                checkBerserk();
            }
        }, 4000, 4000);
    }

    public void recalcLocalStats() {
        int oldmaxhp = localmaxhp;
        localmaxhp = getMaxHp();
        localmaxmp = getMaxMp();
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        int speed = 100, jump = 100;
        magic = localint_;
        watk = 0;
        for (IItem item : getInventory(MapleInventoryType.EQUIPPED)) {
            IEquip equip = (IEquip) item;
            localmaxhp += equip.getHp();
            localmaxmp += equip.getMp();
            localdex += equip.getDex();
            localint_ += equip.getInt();
            localstr += equip.getStr();
            localluk += equip.getLuk();
            magic += equip.getMatk() + equip.getInt();
            watk += equip.getWatk();
            speed += equip.getSpeed();
            jump += equip.getJump();
        }
        magic = Math.min(magic, 2000);
        Integer hbhp = getBuffedValue(MapleBuffStat.HYPERBODYHP);
        if (hbhp != null) {
            localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
        }
        Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
        if (hbmp != null) {
            localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
        }
        Integer conversionhp = getBuffedValue(MapleBuffStat.CONVERSION);
        if (conversionhp != null) {
            localmaxhp += (conversionhp.doubleValue() / 100) * localmaxhp;
        }
        Pair p = getDecentHBHPMPBonus();
        if (Integer.parseInt(p.getLeft().toString()) != 0 && Integer.parseInt(p.getRight().toString()) != 0) {
            int hpUp = Integer.parseInt(p.getLeft().toString());
            int mpUp = Integer.parseInt(p.getRight().toString());
            localmaxhp += hpUp;
            localmaxmp += mpUp;
        }
        localmaxhp = Math.min(30000, localmaxhp);
        localmaxmp = Math.min(30000, localmaxmp);
        Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
        if (watkbuff != null) {
            watk += watkbuff.intValue();
        }
        if (job.isA(MapleJob.BOWMAN)) {
            ISkill expert = null;
            if (job.isA(MapleJob.MARKSMAN)) {
                expert = SkillFactory.getSkill(3220004);
            } else if (job.isA(MapleJob.BOWMASTER)) {
                expert = SkillFactory.getSkill(3120005);
            }
            if (expert != null) {
                int boostLevel = getSkillLevel(expert);
                if (boostLevel > 0) {
                    watk += expert.getEffect(boostLevel).getX();
                }
            }
        }
        Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
        if (matkbuff != null) {
            magic += matkbuff.intValue();
        }
        Integer speedbuff = getBuffedValue(MapleBuffStat.SPEED);
        if (speedbuff != null) {
            speed += speedbuff.intValue();
        }
        Integer jumpbuff = getBuffedValue(MapleBuffStat.JUMP);
        if (jumpbuff != null) {
            jump += jumpbuff.intValue();
        }
        if (speed > 140) {
            speed = 140;
        }
        if (jump > 123) {
            jump = 123;
        }
        if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
            updatePartyMemberHP();
        }
    }

    public void receivePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapId() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        client.announce(MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getHp(), other.getCurrentMaxHp()));
                    }
                }
            }
        }
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule) {
        if (effect.isDragonBlood()) {
            prepareDragonBlood(effect);
        } else if (effect.isBerserk()) {
            checkBerserk();
        } else if (effect.isBeholder()) {
            final int beholder = DarkKnight.BEHOLDER;
            if (beholderHealingSchedule != null) {
                beholderHealingSchedule.cancel(false);
            }
            if (beholderBuffSchedule != null) {
                beholderBuffSchedule.cancel(false);
            }
            ISkill bHealing = SkillFactory.getSkill(DarkKnight.AURA_OF_BEHOLDER);
            int bHealingLvl = getSkillLevel(bHealing);
            if (bHealingLvl > 0) {
                final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                int healInterval = healEffect.getX() * 1000;
                beholderHealingSchedule = TimerManager.getInstance().register(new Runnable() {

                    @Override
                    public void run() {
                        addHP(healEffect.getHp());
                        client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, 5), true);
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showOwnBuffEffect(beholder, 2), false);
                    }
                }, healInterval, healInterval);
            }
            ISkill bBuff = SkillFactory.getSkill(DarkKnight.HEX_OF_BEHOLDER);
            if (getSkillLevel(bBuff) > 0) {
                final MapleStatEffect buffEffect = bBuff.getEffect(getSkillLevel(bBuff));
                int buffInterval = buffEffect.getX() * 1000;
                beholderBuffSchedule = TimerManager.getInstance().register(new Runnable() {

                    @Override
                    public void run() {
                        buffEffect.applyTo(MapleCharacter.this);
                        client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, (int) (Math.random() * 3) + 6), true);
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), beholder, 2), false);
                    }
                }, buffInterval, buffInterval);
            }
        }
        for (Pair<MapleBuffStat, Integer> statup : effect.getStatups()) {
            effects.put(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, schedule, statup.getRight().intValue()));
        }
        recalcLocalStats();
    }

    public void removeAllCooldownsExcept(int id) {
        for (MapleCoolDownValueHolder mcvh : coolDowns.values()) {
            if (mcvh.skillId != id) {
                coolDowns.remove(mcvh.skillId);
            }
        }
    }

    public static void removeAriantRoom(int room) {
        ariantroomleader[room] = "";
        ariantroomslot[room] = 0;
    }

    public void removeCooldown(int skillId) {
        if (this.coolDowns.containsKey(skillId)) {
            this.coolDowns.remove(skillId);
        }
    }

    public void removeDisease(MapleDisease disease) {
        synchronized (diseases) {
            if (diseases.contains(disease)) {
                diseases.remove(disease);
            }
        }
    }

    public void removeDiseases() {
        diseases.clear();
    }

    public void removePet(MaplePet pet, boolean shift_left) {
        int slot = -1;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    pets[i] = null;
                    slot = i;
                    break;
                }
            }
        }
        if (shift_left) {
            if (slot > -1) {
                for (int i = slot; i < 3; i++) {
                    if (i != 2) {
                        pets[i] = pets[i + 1];
                    } else {
                        pets[i] = null;
                    }
                }
            }
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.remove(mo);
    }

    public void restoreBasicAPStats() {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(5);
        int ap_toGive = (getLevel() - 1) * 5;
        dex = 4;
        int_ = 4;
        str = 4;
        luk = 4;
        remainingAp = ap_toGive;
        if (this.getHpMpApUsed() > 0) {
            remainingAp -= getHpMpApUsed();
        }
        this.updateSingleStat(MapleStat.AVAILABLEAP, remainingAp);
        updateSingleStat(MapleStat.STR, str);
        updateSingleStat(MapleStat.DEX, dex);
        updateSingleStat(MapleStat.INT, int_);
        updateSingleStat(MapleStat.LUK, luk);
    }

//    public boolean hasAbusedAPGlitch(){
    //    if((dex + int_ + str + dex + availableAp) / 5 )
    //   }
    public void restoreAPStats() {
        if (dex == 4 && int_ == 4 && str == 4 && luk == 4) {
            return;
        }
        int ap_toGive = (dex - 4) + (int_ - 4) + (str - 4) + (luk - 4);
        dex = 4;
        int_ = 4;
        str = 4;
        luk = 4;
        remainingAp += ap_toGive;
        this.updateSingleStat(MapleStat.AVAILABLEAP, remainingAp);
        updateSingleStat(MapleStat.STR, str);
        updateSingleStat(MapleStat.DEX, dex);
        updateSingleStat(MapleStat.INT, int_);
        updateSingleStat(MapleStat.LUK, luk);

    }

    public void resetStats() {
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(5);
        int tap = 0, tsp = 1;
        int tstr = 4, tdex = 4, tint = 4, tluk = 4;
        int levelap = (isCygnus() ? 6 : 5);
        switch (job.getId()) {
            case 100:
            case 1100:
            case 2100://?
                tstr = 35;
                tap = ((getLevel() - 10) * levelap) + 14;
                tsp += ((getLevel() - 10) * 3);
                break;
            case 200:
            case 1200:
                tint = 20;
                tap = ((getLevel() - 8) * levelap) + 29;
                tsp += ((getLevel() - 8) * 3);
                break;
            case 300:
            case 1300:
            case 400:
            case 1400:
                tdex = 25;
                tap = ((getLevel() - 10) * levelap) + 24;
                tsp += ((getLevel() - 10) * 3);
                break;
            case 500:
            case 1500:
                tdex = 20;
                tap = ((getLevel() - 10) * levelap) + 29;
                tsp += ((getLevel() - 10) * 3);
                break;
        }
        this.remainingAp = tap;
        this.remainingSp = tsp;
        this.dex = tdex;
        this.int_ = tint;
        this.str = tstr;
        this.luk = tluk;
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, tap));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP, tsp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.STR, tstr));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.DEX, tdex));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.INT, tint));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.LUK, tluk));
        announce(MaplePacketCreator.updatePlayerStats(getClient(), statup));
    }

    public void resetBattleshipHp() {
        this.battleshipHp = 4000 * getSkillLevel(SkillFactory.getSkill(Corsair.BATTLE_SHIP)) + ((getLevel() - 120) * 2000);
    }

    public void resetEnteredScript() {
        if (entered.containsKey(map.getId())) {
            entered.remove(map.getId());
        }
    }

    public void resetEnteredScript(int mapId) {
        if (entered.containsKey(mapId)) {
            entered.remove(mapId);
        }
    }

    public void resetEnteredScript(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                entered.remove(mapId);
            }
        }
    }

    public void resetMGC() {
        this.mgc = null;
    }

    public void saveCooldowns() {
        if (getAllCooldowns().size() > 0) {
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)");
                ps.setInt(1, getId());
                for (PlayerCoolDownValueHolder cooling : getAllCooldowns()) {
                    ps.setInt(2, cooling.skillId);
                    ps.setLong(3, cooling.startTime);
                    ps.setLong(4, cooling.length);
                    ps.addBatch();
                }
                ps.executeBatch();
                ps.close();
            } catch (SQLException se) {
            }
        }
    }

    public void saveGuildStatus() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, allianceRank = ? WHERE id = ?");
            ps.setInt(1, guildid);
            ps.setInt(2, guildrank);
            ps.setInt(3, allianceRank);
            ps.setInt(4, id);
            ps.execute();
            ps.close();
        } catch (SQLException se) {
        }
    }

    public void saveLocation(String type) {
        MaplePortal closest = map.findClosestPortal(getPosition());
        savedLocations.put(SavedLocationType.fromString(type), new SavedLocation(getMapId(), closest != null ? closest.getId() : 0));
    }

    public void saveToDB(boolean update) {
        Connection con = DatabaseConnection.getConnection();
        try {
            if (update && this.achievementProgress != null) {
                this.getAchievementsProgress().saveAchievementProgress();
            }
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);
            PreparedStatement ps;
            if (update) {
                ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, gachaexp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpMpUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, mountlevel = ?, mountexp = ?, mounttiredness= ?, equipslots = ?, useslots = ?, setupslots = ?, etcslots = ?,  monsterbookcover = ?, vanquisherStage = ?, dojoPoints = ?, lastDojoStage = ?, finishedDojoTutorial = ?, vanquisherKills = ?, matchcardwins = ?, matchcardlosses = ?, matchcardties = ?, omokwins = ?, omoklosses = ?, omokties = ?, pvpkills = ?, pvpdeaths = ?, GameMode = ?, campaignProgress = ?, familyId = ?, reborns = ?, mute = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
            } else {
                ps = con.prepareStatement("INSERT INTO characters (level, fame, str, dex, luk, `int`, exp, gachaexp, hp, mp, maxhp, maxmp, sp, ap, gm, skincolor, gender, job, hair, face, map, meso, hpMpUsed, spawnpoint, party, buddyCapacity, messengerid, messengerposition, mountlevel, mountexp, mounttiredness, equipslots, useslots, setupslots, etcslots, monsterbookcover, vanquisherStage, dojopoints, lastDojoStage, finishedDojoTutorial, vanquisherKills, matchcardwins, matchcardlosses, matchcardties, omokwins, omoklosses, omokties, pvpkills, pvpdeaths, GameMode, campaignProgress, familyId, reborns, mute, accountid, name, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            }
            if (gmLevel < 1 && level > 199) {
                ps.setInt(1, isCygnus() ? 120 : 200);
            } else {
                ps.setInt(1, level);
            }
            ps.setInt(2, fame);
            ps.setInt(3, str);
            ps.setInt(4, dex);
            ps.setInt(5, luk);
            ps.setInt(6, int_);
            ps.setInt(7, Math.abs(exp.get()));
            ps.setInt(8, Math.abs(gachaexp.get()));
            ps.setInt(9, hp);
            ps.setInt(10, mp);
            ps.setInt(11, maxhp);
            ps.setInt(12, maxmp);
            ps.setInt(13, remainingSp);
            ps.setInt(14, remainingAp);
            ps.setInt(15, gmLevel);
            ps.setInt(16, skinColor.getId());
            ps.setInt(17, gender);
            ps.setInt(18, job.getId());
            ps.setInt(19, hair);
            ps.setInt(20, face);
            if (map == null) {
                if (getJob() == MapleJob.BEGINNER) {
                    ps.setInt(21, 109090204);
                } else if (getJob() == MapleJob.NOBLESSE) {
                    ps.setInt(21, 109090204);
                } else if (getJob() == MapleJob.LEGEND) {
                    ps.setInt(21, 109090204);
                } else if (getJob() == MapleJob.GM || getJob() == MapleJob.SUPERGM) {
                    ps.setInt(21, 109090204);
                } else if (isEvan() || isResistance() || isDualBlade()) {
                    ps.setInt(21, 109090204);
                }
            } else {
                if (map.getForcedReturnId() != 999999999 && map.getId() != 109090204) {
                    ps.setInt(21, map.getForcedReturnId());
                } else {
                    ps.setInt(21, map.getId());
                }
            }
            ps.setInt(22, meso.get());
            ps.setInt(23, hpMpApUsed);
            if (map == null || map.getId() == 610020000 || map.getId() == 610020001) {
                ps.setInt(24, 0);
            } else {
                MaplePortal closest = map.findClosestSpawnpoint(getPosition());
                if (closest != null) {
                    ps.setInt(24, closest.getId());
                } else {
                    ps.setInt(24, 0);
                }
            }
            if (party != null) {
                ps.setInt(25, party.getId());
            } else {
                ps.setInt(25, -1);
            }
            if (buddylist != null) {
                ps.setInt(26, buddylist.getCapacity());
            }
            if (messenger != null) {
                ps.setInt(27, messenger.getId());
                ps.setInt(28, messengerposition);
            } else {
                ps.setInt(27, 0);
                ps.setInt(28, 4);
            }
            if (maplemount != null) {
                ps.setInt(29, maplemount.getLevel());
                ps.setInt(30, maplemount.getExp());
                ps.setInt(31, maplemount.getTiredness());
            } else {
                ps.setInt(29, 1);
                ps.setInt(30, 0);
                ps.setInt(31, 0);
            }
            for (int i = 1; i < 5; i++) {
                ps.setInt(i + 31, getSlots(i));
            }

            //  if (update) {
            //      monsterbook.saveCards(getId());
            //  }
            ps.setInt(36, bookCover);
            ps.setInt(37, vanquisherStage);
            ps.setInt(38, dojoPoints);
            ps.setInt(39, dojoStage);
            ps.setInt(40, finishedDojoTutorial ? 1 : 0);
            ps.setInt(41, vanquisherKills);
            ps.setInt(42, matchcardwins);
            ps.setInt(43, matchcardlosses);
            ps.setInt(44, matchcardties);
            ps.setInt(45, omokwins);
            ps.setInt(46, omoklosses);
            ps.setInt(47, omokties);
            ps.setInt(48, pvpkills);
            ps.setInt(49, pvpdeaths);
            ps.setInt(50, 0);
            ps.setShort(51, campaignProgress);
            ps.setInt(52, familyId);
            ps.setInt(53, reborns);
            ps.setInt(54, mute);
            if (update) {
                ps.setInt(55, id);
            } else {
                ps.setInt(55, accountid);
                ps.setString(56, name);
                ps.setInt(57, world);
            }
            int updateRows = ps.executeUpdate();
            if (!update) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                } else {
                    throw new RuntimeException("Inserting char failed.");
                }
            } else if (updateRows < 1) {
                throw new RuntimeException("Character not in database (" + id + ")");
            }
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    pets[i].saveToDb();
                }
            }
            deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<Integer, MapleKeyBinding> keybinding : keymap.entrySet()) {
                ps.setInt(2, keybinding.getKey().intValue());
                ps.setInt(3, keybinding.getValue().getType());
                ps.setInt(4, keybinding.getValue().getAction());
                ps.addBatch();
            }
            ps.executeBatch();
            List<Pair<IItem, MapleInventoryType>> itemsWithType = new ArrayList<Pair<IItem, MapleInventoryType>>();

            for (MapleInventory iv : inventory) {
                for (IItem item : iv.list()) {
                    itemsWithType.add(new Pair<IItem, MapleInventoryType>(item, iv.getType()));
                }
            }

            ItemFactory.INVENTORY.saveItems(itemsWithType, id);
            deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
                ps.setInt(2, skill.getKey().getId());
                ps.setInt(3, skill.getValue().skillevel);
                ps.setInt(4, skill.getValue().masterlevel);
                ps.setLong(5, skill.getValue().expiration);
                ps.addBatch();
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`, `portal`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                if (savedLocations.get(savedLocationType) != null) {
                    ps.setString(2, savedLocationType.name());
                    ps.setInt(3, savedLocations.get(savedLocationType).getMapId());
                    ps.setInt(4, savedLocations.get(savedLocationType).getPortal());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid, vip) VALUES (?, ?, 0)");
            for (int i = 0; i < getTrockSize(); i++) {
                if (trockmaps[i] != 999999999) {
                    ps.setInt(1, getId());
                    ps.setInt(2, trockmaps[i]);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            /*
             * ps = con.prepareStatement("INSERT INTO
             * trocklocations(characterid, mapid, vip) VALUES (?, ?, 1)"); for
             * (int i = 0; i < getVipTrockSize(); i++) { if (viptrockmaps[i] !=
             * 999999999) { ps.setInt(1, getId()); ps.setInt(2,
             * viptrockmaps[i]); ps.addBatch(); } }
            ps.executeBatch();
             */
            deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
            ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`, `group`) VALUES (?, ?, 0, ?)");
            ps.setInt(1, id);
            for (BuddylistEntry entry : buddylist.getBuddies()) {
                if (entry.isVisible()) {
                    ps.setInt(2, entry.getCharacterId());
                    ps.setString(3, entry.getGroup());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            deleteWhereCharacterId(con, "DELETE FROM eventstats WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO eventstats (characterid, `rescuegaga`, `artifacthunt`) VALUES (?, ?, 0)");
            ps.setInt(1, id);
            ps.setInt(2, events == null ? 0 : events.getGagaRescue().getCompleted());
            ps.executeUpdate();
            deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`) VALUES (DEFAULT, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement pse = con.prepareStatement("INSERT INTO questprogress VALUES (DEFAULT, ?, ?, ?)");
            PreparedStatement psf = con.prepareStatement("INSERT INTO medalmaps VALUES (DEFAULT, ?, ?)");
            ps.setInt(1, id);
            for (MapleQuestStatus q : quests.values()) {
                ps.setInt(2, q.getQuest().getId());
                ps.setInt(3, q.getStatus().getId());
                ps.setInt(4, (int) (q.getCompletionTime() / 1000));
                ps.setInt(5, q.getForfeited());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                for (int mob : q.getProgress().keySet()) {
                    pse.setInt(1, rs.getInt(1));
                    pse.setInt(2, mob);
                    pse.setString(3, q.getProgress(mob));
                    pse.addBatch();
                }
                for (int i = 0; i < q.getMedalMaps().size(); i++) {
                    psf.setInt(1, rs.getInt(1));
                    psf.setInt(2, q.getMedalMaps().get(i));
                    psf.addBatch();
                }
                pse.executeBatch();
                psf.executeBatch();
                rs.close();
            }
            pse.close();
            psf.close();
            ps = con.prepareStatement("UPDATE accounts SET gm = ? WHERE id = ?");
            ps.setInt(1, gmLevel);
            ps.setInt(2, client.getAccID());
            ps.executeUpdate();
            ps = con.prepareStatement("UPDATE characters SET isDB = ? WHERE id = ?");
            ps.setInt(1, isDB ? 1 : 0);
            ps.setInt(2, getId());
            ps.executeUpdate();

            if (cashshop != null) {
                cashshop.save();
            }
            if (storage != null) {
                storage.saveToDB();
            }
            if (getDonator() != null && !update) {
                getDonator().save();
            }
            if (gmLevel > 0) {
                ps = con.prepareStatement("INSERT INTO gmlog (`cid`, `command`) VALUES (?, ?)");
                ps.setInt(1, id);
                for (String com : commands) {
                    ps.setString(2, com);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            ps.close();
            ps = con.prepareStatement("DELETE FROM maplepokemon where masterName = ?");
            ps.setString(1, getName());
            ps.execute();
            ps.close();
            if (getAllPokemon().size() > 0) {
                for (MaplePokemon p : getAllPokemon()) {
                    ps = con.prepareStatement("INSERT INTO maplepokemon (level, pid, exp, masterName) VALUES (?, ?, ?, ?)");
                    ps.setInt(1, p.getLevel());
                    ps.setInt(2, p.pokemonid);
                    ps.setInt(3, p.getExp());
                    ps.setString(4, getName());
                    ps.execute();
                    ps.close();
                }
            }
            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                con.rollback();
                getClient().disconnect();
            } catch (Exception se) {
                FileoutputUtil.log("saving_bug.rtf", "Player has had an issue while saving...\n\n" + se, null);
                se.printStackTrace();
            }
        } finally {
            try {
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } catch (Exception e) {
            }

        }
    }

    public void sendPolice(int greason, String reason, int duration) {
        announce(MaplePacketCreator.sendPolice(greason, reason, duration));
        this.isbanned = true;
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                // client.disconnect(); //FAGGOTS
            }
        }, duration);
    }

    public void sendPolice(String text) {
        announce(MaplePacketCreator.sendPolice(text));
        this.isbanned = true;
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                //  client.disconnect(); //FAGGOTS
            }
        }, 6000);
    }

    public void sendKeymap() {
        client.announce(MaplePacketCreator.getKeymap(keymap));
    }

    public void sendMacros() {
        boolean macros = false;
        for (int i = 0; i < 5; i++) {
            if (skillMacros[i] != null) {
                macros = true;
            }
        }
        if (macros) {
            client.announce(MaplePacketCreator.getMacros(skillMacros));
        }
    }

    public void sendNote(String to, String msg, byte fame) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`, `fame`) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, to);
            ps.setString(2, this.getName());
            ps.setString(3, msg);
            ps.setLong(4, System.currentTimeMillis());
            ps.setByte(5, fame);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAllianceRank(int rank) {
        allianceRank = rank;
        if (mgc != null) {
            mgc.setAllianceRank(rank);
        }
    }

    public void setAllowWarpToId(int id) {
        this.warpToId = id;
    }

    public static void setAriantRoomLeader(int room, String charname) {
        ariantroomleader[room] = charname;
    }

    public static void setAriantSlotRoom(int room, int slot) {
        ariantroomslot[room] = slot;
    }

    public void setBattleshipHp(int battleshipHp) {
        this.battleshipHp = battleshipHp;
    }

    public void setBuddyCapacity(int capacity) {
        buddylist.setCapacity(capacity);
        client.announce(MaplePacketCreator.updateBuddyCapacity(capacity));
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }

    public void setChair(int chair) {
        this.chair = chair;
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
    }

    public void setDex(int dex) {
        this.dex = dex;
        recalcLocalStats();
    }

    public void setDojoEnergy(int x) {
        this.dojoEnergy = x;
    }

    public void setDojoParty(boolean b) {
        this.dojoParty = b;
    }

    public void setDojoPoints(int x) {
        this.dojoPoints = x;
    }

    public void setDojoStage(int x) {
        this.dojoStage = x;
    }

    public void setDojoStart() {
        this.dojoMap = map;
        int stage = (map.getId() / 100) % 100;
        this.dojoFinish = System.currentTimeMillis() + (stage > 36 ? 15 : stage / 6 + 5) * 60000;
    }

    public void setRates() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        int hr = cal.get(Calendar.HOUR_OF_DAY);
        if ((haveItem(5360001) && hr > 6 && hr < 12) || (haveItem(5360002) && hr > 9 && hr < 15) || (haveItem(536000) && hr > 12 && hr < 18) || (haveItem(5360004) && hr > 15 && hr < 21) || (haveItem(536000) && hr > 18) || (haveItem(5360006) && hr < 5) || (haveItem(5360007) && hr > 2 && hr < 6) || (haveItem(5360008) && hr >= 6 && hr < 11)) {
            this.dropRate = 2 * ServerConstants.DROP_RATE;
            this.mesoRate = 2 * ServerConstants.MESO_RATE;
        } else {
            this.dropRate = ServerConstants.DROP_RATE;
            this.mesoRate = ServerConstants.MESO_RATE;
        }
        if ((haveItem(5211000) && hr > 17 && hr < 21) || (haveItem(5211014) && hr > 6 && hr < 12) || (haveItem(5211015) && hr > 9 && hr < 15) || (haveItem(5211016) && hr > 12 && hr < 18) || (haveItem(5211017) && hr > 15 && hr < 21) || (haveItem(5211018) && hr > 14) || (haveItem(5211039) && hr < 5) || (haveItem(5211042) && hr > 2 && hr < 8) || (haveItem(5211045) && hr > 5 && hr < 11) || haveItem(5211048)) {
            if (isBeginnerJob()) {
                this.expRate = 50;
            } else {
                this.expRate = 2 * ServerConstants.EXP_RATE;
            }
        } else {
            if (isBeginnerJob()) {
                this.expRate = 25;
            } else {
                this.expRate = ServerConstants.EXP_RATE;
                dropRate = ServerConstants.DROP_RATE;
            }
        }

        if (getDonator().isActive()) {
            expRate *= 2;
            dropRate *= 2;
            mesoRate *= 2;
        }

        // expRate *= 2; // christmas event lolololo
        // tis done.
    }

    public void setEnergyBar(int set) {
        energybar = set;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void setExp(int amount) {
        this.exp.set(amount);
    }

    public void setGachaExp(int amount) {
        this.gachaexp.set(amount);
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public void setFinishedDojoTutorial() {
        this.finishedDojoTutorial = true;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGM(int level) {
        this.gmLevel = level;
    }

    public void setGuildId(int _id) {
        guildid = _id;
        if (guildid > 0) {
            if (mgc == null) {
                mgc = new MapleGuildCharacter(this);
            } else {
                mgc.setGuildId(guildid);
            }
        } else {
            mgc = null;
        }
    }

    public void setGuildRank(int _rank) {
        guildrank = _rank;
        if (mgc != null) {
            mgc.setGuildRank(_rank);
        }
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setHasMerchant(boolean set) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = ?");
            ps.setInt(1, set ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        hasMerchant = set;
    }

    public void setMerchantMeso(int set) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?");
            ps.setInt(1, set);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            return;
        }
        merchantmeso = set;
    }

    public void setHiredMerchant(HiredMerchant merchant) {
        this.hiredMerchant = merchant;
    }

    public void setHp(int newhp) {
        setHp(newhp, false);
    }

    public void setHp(int newhp, boolean silent) {
        int oldHp = hp;
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        this.hp = thp;
        if (!silent) {
            updatePartyMemberHP();
        }
        if (oldHp > hp && !isAlive()) {
            playerDead();
        }
    }

    public void setHpMpApUsed(int mpApUsed) {
        this.hpMpApUsed = mpApUsed;
    }

    public void setHpMp(int x) {
        setHp(x);
        setMp(x);
        updateSingleStat(MapleStat.HP, hp);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void setInt(int int_) {
        this.int_ = int_;
        recalcLocalStats();
    }

    public void setInventory(MapleInventoryType type, MapleInventory inv) {
        inventory[type.ordinal()] = inv;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public void setJob(MapleJob job) {
        this.job = job;
    }

    public void setLastHealed(long time) {
        this.lastHealed = time;
    }

    public void setLastUsedCashItem(long time) {
        this.lastUsedCashItem = time;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setLuk(int luk) {
        this.luk = luk;
        recalcLocalStats();
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public void setMarkedMonster(int markedMonster) {
        this.markedMonster = markedMonster;
    }

    public void setMaxHp(int hp) {
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxHpCreation(int hp) {
        this.maxhp = hp;
    }

    public void setMaxHp(int hp, boolean ap) {
        hp = Math.min(30000, hp);
        if (ap) {
            setHpMpApUsed(getHpMpApUsed() + 1);
        }
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp) {
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setMaxMpCreation(int mp) {
        this.maxmp = mp;
    }

    public void setMaxMp(int mp, boolean ap) {
        mp = Math.min(30000, mp);
        if (ap) {
            setHpMpApUsed(getHpMpApUsed() + 1);
        }
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void setMessengerPosition(int position) {
        this.messengerposition = position;
    }

    public void setMiniGame(MapleMiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public void setMiniGamePoints(MapleCharacter visitor, int winnerslot, boolean omok) {
        if (omok) {
            if (winnerslot == 1) {
                this.omokwins++;
                visitor.omoklosses++;
            } else if (winnerslot == 2) {
                visitor.omokwins++;
                this.omoklosses++;
            } else {
                this.omokties++;
                visitor.omokties++;
            }
        } else {
            if (winnerslot == 1) {
                this.matchcardwins++;
                visitor.matchcardlosses++;
            } else if (winnerslot == 2) {
                visitor.matchcardwins++;
                this.matchcardlosses++;
            } else {
                this.matchcardties++;
                visitor.matchcardties++;
            }
        }
    }

    public void setMonsterBookCover(int bookCover) {
        this.bookCover = bookCover;
    }

    public void setMp(int newmp) {
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > localmaxmp) {
            tmp = localmaxmp;
        }
        this.mp = tmp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParty(MapleParty party) {
        if (party == null) {
            this.mpc = null;
        }
        this.party = party;
    }

    public void setPlayerShop(MaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp = remainingSp;
    }

    public void setSearch(String find) {
        search = find;
    }

    public void setSkinColor(MapleSkinColor skinColor) {
        this.skinColor = skinColor;
    }

    public byte getSlots(int type) {
        return type == MapleInventoryType.CASH.getType() ? 96 : inventory[type].getSlotLimit();
    }

    public boolean gainSlots(int type, int slots) {
        return gainSlots(type, slots, true);
    }

    public boolean gainSlots(int type, int slots, boolean update) {
        slots += inventory[type].getSlotLimit();
        if (slots <= 96) {
            inventory[type].setSlotLimit(slots);

            saveToDB(true);
            if (update) {
                client.announce(MaplePacketCreator.updateInventorySlotLimit(type, slots));
            }

            return true;
        }

        return false;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public void setSlot(int slotid) {
        slots = slotid;
    }

    public void setStr(int str) {
        this.str = str;
        recalcLocalStats();
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public void setVanquisherKills(int x) {
        this.vanquisherKills = x;
    }

    public void setVanquisherStage(int x) {
        this.vanquisherStage = x;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void shiftPetsRight() {
        if (pets[2] == null) {
            pets[2] = pets[1];
            pets[1] = pets[0];
            pets[0] = null;
        }
    }

    public void showDojoClock() {
        int stage = (map.getId() / 100) % 100;
        long time;
        if (stage % 6 == 1) {
            time = (stage > 36 ? 15 : stage / 6 + 5) * 60;
        } else {
            time = (dojoFinish - System.currentTimeMillis()) / 1000;
        }
        if (stage % 6 > 0) {
            client.announce(MaplePacketCreator.getClock((int) time));
        }
        boolean rightmap = true;
        int clockid = (dojoMap.getId() / 100) % 100;
        if (map.getId() > clockid / 6 * 6 + 6 || map.getId() < clockid / 6 * 6) {
            rightmap = false;
        }
        final boolean rightMap = rightmap; // lol
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (rightMap) {
                    client.getPlayer().changeMap(client.getChannelServer().getMapFactory(gMode).getMap(925020000));
                }
            }
        }, time * 1000 + 3000); // let the TIMES UP display for 3 seconds, then warp
    }

    public void showNote() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM notes WHERE `to`=? AND `deleted` = 0", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setString(1, this.getName());
            ResultSet rs = ps.executeQuery();
            rs.last();
            int count = rs.getRow();
            rs.first();
            client.announce(MaplePacketCreator.showNotes(rs, count));
            rs.close();
            ps.close();
            ps = DatabaseConnection.getConnection().prepareStatement("UPDATE notes SET deleted = 1 WHERE to = ?");
            ps.setString(1, getName());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public void resetNotes() {
    }

    private void silentEnforceMaxHpMp() {
        setMp(getMp());
        setHp(getHp(), true);
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
        }
    }

    public void silentPartyUpdate() {
        if (party != null) {
            try {
                client.getChannelServer().getWorldInterface().updateParty(party.getId(), PartyOperation.SILENT_UPDATE, getMPC());
            } catch (RemoteException e) {
                e.printStackTrace();
                client.getChannelServer().reconnectWorld();
            }
        }
    }

    public static class SkillEntry {

        public int masterlevel;
        public byte skillevel;
        public long expiration;

        public SkillEntry(byte skillevel, int masterlevel, long expiration) {
            this.skillevel = skillevel;
            this.masterlevel = masterlevel;
            this.expiration = expiration;
        }

        @Override
        public String toString() {
            return skillevel + ":" + masterlevel;
        }

        public boolean hasMastery() {
            if (masterlevel > 0) {
                return true;
            }
            final int jobId = skillevel / 10000;
            if (jobId == 0 || jobId == 2000 || jobId == 2001) {
                return false;
            } else if (jobId == 434) {
                return true;
            } else if (masteryBookList(skillevel)) {
                return true;
            }
            return false;
        }

        public boolean masteryBookList(int id) {
            switch (id) {
                case 1120004: // achilles
                case 1121006: // rush
                case 1121008: // intrepid slash
                case 1121010: // enrage
                case 1121011: // hero's will
                case 1220005: // achilles
                case 1220006: // guardian
                case 1220010: // advanced charge
                case 1221000: // maple warrior
                case 1221002: // power stance
                case 1221004: // divine charge
                case 1221007: // rush
                case 1221009: // blast
                case 1221011: // heaven's hammer
                case 1221012: // hero's will
                case 1320005: // achilles
                case 1320006: // berserk
                case 1320008: // aura of the beholder
                case 1320009: // hex of the beholder
                case 1321000: // maple warrior
                case 1321001: // monster magnet
                case 1321002: // power stance
                case 1321003: // rush
                case 1321007: // beholder
                case 1321010: // hero's will
                case 2121000: // maple warrior
                case 2121001: // big bang
                case 2121002: // mana reflection
                case 2121003: // fire demon
                case 2121004: // infinity
                case 2121005: // ifrit
                case 2121006: // paralyze
                case 2121007: // meteor shower
                case 2121008: // heros will
                case 2221000: // maple warrior
                case 2221001: // big bang
                case 2221002: // mana refelection
                case 2221003: // ice demon
                case 2221004: // infinity
                case 2221005: // elquines
                case 2221006: // chain lightning
                case 2221007: // blizzard
                case 2221008: // hero's will
                case 2321000: // maple warrior
                case 2321001: // big bang
                case 2321002: // mana reflection
                case 2321003: // bahamut
                case 2321004: // infinity
                case 2321005: // holy shield
                case 2321006: // resurrection
                case 2321007: // angel ray
                case 2321008: // genesis
                case 2321009: // hero's will
                case 3120005: // bow expert
                case 3121000: // maple warrior
                case 3121002: // sharp eyes
                case 3121003: // dragons breathe
                case 3121004: // hurricane
                case 3121006: // phoenix
                case 3121007: // hamstring
                case 3121008: // concentrate
                case 3121009: // hero's will
                case 3220004: // marksman boost
                case 3221000: // maple warrior
                case 3221001: // piercing arrow
                case 3221002: // sharp eyes
                case 3221003: // dragons breathe
                case 3221005: // frostprey
                case 3221006: // blind
                case 3221007: // snipe
                case 3221008: // hero's will
                case 4121000: // maple warrior
                case 4120002: // shadow shifter
                case 4121003: // taunt
                case 4121004: // ninja ambush
                case 4120005: // venomous star
                case 4121006: // shadow stars
                case 4121007: // triple throw
                case 4121008: // ninja storm
                case 4121009: // hero's will
                case 4220002: // shadow shifter
                case 4220005: // venomous stab
                case 4221000: // maple warrior
                case 4221001: // assassinate
                case 4221003: // taunt
                case 4221004: // ninja ambush
                case 4221006: // smokescreen
                case 4221007: // boomerange step
                case 4221008: // hero's will
                case 4311003: // slash storm
                case 4321000: // tornado spin
                case 4331002: // mirror image
                case 4331005: // flying assaulter

                // xxx blade master already taken care of above.

                case 5121000: // maple warrior
                case 5121001: // dragon strike
                case 5121002: // energy orb
                case 5121003: // super transformation
                case 5121004: // demolition
                case 5121005: // snatch
                case 5121007: // barrage
                case 5121008: // pirate's rage
                case 5121009: // speed infusion
                case 5121010: // time leap
                case 5220001: // elemental boost
                case 5220002: // wrath of the octopi
                case 5220011: // bullseye
                case 5221000: // maple warrior
                case 5221003: // air strike
                case 5221004: // rapid fire
                case 5221006: // battleship
                case 5221007: // battleship cannon
                case 5221008: // battleship torpedo
                case 5221009: // hypnotise
                case 5221010: // speed infusion

                // xxx dawn warrior - no mastery books.
                // xxx blaze wizard - no mastery books.
                // xxx wind archer - no mastery books.
                // xxx nightwalker - no mastery books.
                // xxx thunderbreaker - no mastery books.

                case 21120001: // high mastery
                case 21120002: // over swing
                case 21120005: // final blow
                case 21120006: // combo tempest
                case 21120007: // combo barrier
                case 21121000: // maple warrior
                case 21121003: // freeze standing
                case 21121008: // hero's will
                case 22170001: // magic mastery
                case 22171000: // maple warrior
                case 22171002: // illusion
                case 22171003: // flame wheel
                case 22171004: // echos will
                case 22111001: // magic guard
                case 22141002: // magic boost
                case 22140000: // critical magic
                case 22181000: // blessing of the oynx
                case 22181001: // blaze
                case 22181002: // dark fog
                case 22181003: // soul stone
                case 32120000: // advanced dark aura
                case 32120001: // advanced yellow aura
                case 32121002: // finishing blow
                case 32121003: // twister spin
                case 32121004: // dark genesis
                case 32121005: // stance
                case 32121006: // party shield
                case 32121007: // maple warrior
                case 32121008: // hero's will
                case 33120000: // crossbow expert
                case 33121001: // exploding arrows
                case 33121002: // sonic roar
                case 33121004: // sharp eyes
                case 33121005: // stink bomb shot
                case 33121006: // feline berserk
                case 33121007: // maple warrior
                case 33121008: // hero's will
                case 33121009: // wild arrow blast
                case 35120000: // extreme mech
                case 35120001: // robot mastery
                case 35121003: // giant robot sg-88
                case 35121005: // missile tank
                case 35121006: // sateillite safety
                case 35121007: // maple warrior
                case 35121008: // hero's will
                case 35121009: // bots 'n tots
                case 35121010: // amplifier af-11
                case 35121012: // laser blast
                case 35121013: // siege mode
                    return true;
                default:
                    return false;
            }
        }
    }

    public boolean skillisCooling(int skillId) {
        return coolDowns.containsKey(Integer.valueOf(skillId));
    }

    public void startFullnessSchedule(final int decrease, final MaplePet pet, int petSlot) {
        /*
         * ScheduledFuture<?> schedule = TimerManager.getInstance().register(new
         * Runnable() {
         *
         * @Override public void run() { int newFullness = pet.getFullness() -
         * decrease; if (newFullness <= 5) { pet.setFullness(15);
         * pet.saveToDb(); unequipPet(pet, true); } else {
         * pet.setFullness(newFullness); pet.saveToDb(); IItem petz =
         * getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
         * client.announce(MaplePacketCreator.updateSlot(petz)); } } }, 180000,
         * 18000); switch (petSlot) { case 0: fullnessSchedule = schedule;
         * break; case 1: fullnessSchedule_1 = schedule; break; case 2:
         * fullnessSchedule_2 = schedule; break;
        }
         */
    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, 30000);
    }

    public void startMapEffect(String msg, int itemId, int duration) {
        final MapleMapEffect mapEffect = new MapleMapEffect(msg, itemId);
        getClient().announce(mapEffect.makeStartData());
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                getClient().announce(mapEffect.makeDestroyData());
            }
        }, duration);
    }

    public void stopControllingMonster(MapleMonster monster) {
        if (controlled != null) {
            synchronized (controlled) {
                controlled.remove(monster);
            }
        }
    }

    public void unequipAllPets() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                unequipPet(pets[i], true);
            }
        }
    }

    public void unequipPet(MaplePet pet, boolean shift_left) {
        unequipPet(pet, shift_left, false);
    }

    public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger) {
        if (this.getPet(this.getPetIndex(pet)) != null) {
            this.getPet(this.getPetIndex(pet)).setSummoned(false);
            this.getPet(this.getPetIndex(pet)).saveToDb();
        }
        cancelFullnessSchedule(getPetIndex(pet));
        getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pet, true, hunger), true);
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
        //   stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(0)));
        //   client.getSession().write(MaplePacketCreator.petStatUpdate(this));
        client.getSession().write(MaplePacketCreator.enableActions());
        removePet(pet, shift_left);
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
    }

    public void updatePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapId() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        other.client.announce(MaplePacketCreator.updatePartyMemberHP(getId(), this.hp, maxhp));
                    }
                }
            }
        }
    }
//byte mode, short quest, String info) {

    public void updateQuest(MapleQuestStatus quest) {
        quests.put(quest.getQuest(), quest);
        if (quest.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
            announce(MaplePacketCreator.questProgress((short) quest.getQuest().getId(), quest.getProgress(0)));
            if (quest.getQuest().getInfoNumber() > 0) {
                announce(MaplePacketCreator.questProgress(quest.getQuest().getInfoNumber(), Integer.toString(quest.getMedalProgress())));
            }
            announce(MaplePacketCreator.updateQuestInfo(quest.getQuest().getId(), quest.getNpc()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
            announce(MaplePacketCreator.completeQuest((short) quest.getQuest().getId(), quest.getCompletionTime()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)) {
            announce(MaplePacketCreator.forfeitQuest((short) quest.getQuest().getId()));
        }
    }

    public void questTimeLimit(final MapleQuest quest, int time) {
        ScheduledFuture<?> sf = TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                announce(MaplePacketCreator.questExpire(quest.getId()));
                MapleQuestStatus newStatus = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
                newStatus.setForfeited(getQuest(quest).getForfeited() + 1);
                updateQuest(newStatus);
            }
        }, time);
        announce(MaplePacketCreator.addQuestTimeLimit(quest.getId(), time));
        timers.add(sf);
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    private void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
        announce(MaplePacketCreator.updatePlayerStats(Collections.singletonList(new Pair<MapleStat, Integer>(stat, Integer.valueOf(newval))), itemReaction));
    }

    public void announce(MaplePacket packet) {
        getClient().announce(packet);
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    public void sendDestroyData(MapleClient client) {
        getClient().announce(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (!this.isHidden() || client.getPlayer().gmLevel() > 0) {
            getClient().announce(MaplePacketCreator.spawnPlayerMapobject(this));
        }
    }

    @Override
    public void setObjectId(int id) {
        return;
    }

    @Override
    public String toString() {
        return name;
    }
    private int givenRiceCakes;
    private boolean gottenRiceHat;

    public int getGivenRiceCakes() {
        return givenRiceCakes;
    }

    public void increaseGivenRiceCakes(int amount) {
        this.givenRiceCakes += amount;
    }

    public boolean getGottenRiceHat() {
        return gottenRiceHat;
    }

    public void setGottenRiceHat(boolean b) {
        this.gottenRiceHat = b;
    }

    public int getLinkedLevel() {
        return linkedLevel;
    }

    public String getLinkedName() {
        return linkedName;
    }

    public CashShop getCashShop() {
        return cashshop;
    }

    public void portalDelay(long delay) {
        this.portaldelay = System.currentTimeMillis() + delay;
    }

    public long portalDelay() {
        return portaldelay;
    }

    public void blockPortal(String scriptName) {
        if (!blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.add(scriptName);
            client.announce(MaplePacketCreator.enableActions());
        }
    }

    public void unblockPortal(String scriptName) {
        if (blockedPortals.contains(scriptName) && scriptName != null) {
            blockedPortals.remove(scriptName);
        }
    }

    public List<String> getBlockedPortals() {
        return blockedPortals;
    }

    public boolean getAranIntroState(String mode) {
        if (area_data.contains(mode)) {
            return true;
        }
        return false;
    }

    public void addAreaData(int quest, String data) {
        if (!this.area_data.contains(data)) {
            this.area_data.add(data);
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO char_area_info VALUES (DEFAULT, ?, ?, ?)");
                ps.setInt(1, getId());
                ps.setInt(2, quest);
                ps.setString(3, data);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                System.out.println("[AREA DATA] An error has occured.");
                ex.printStackTrace();
            }
        }
    }

    public void removeAreaData() {
        this.area_data.clear();
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM char_area_info WHERE charid = ?");
            ps.setInt(1, getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("[AREA DATA] An error has occured.");
            ex.printStackTrace();
        }
    }

    public void autoban(String reason, int greason) {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + (int) (2.0 * Math.random()) + 1, cal.get(Calendar.DAY_OF_MONTH) + (int) (30.0 * Math.random()) + 1, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        Timestamp TS = new Timestamp(cal.getTimeInMillis());
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banreason = ?, tempban = ?, greason = ? WHERE id = ?");
            ps.setString(1, reason);
            ps.setTimestamp(2, TS);
            ps.setInt(3, greason);
            ps.setInt(4, accountid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
        getClient().getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(0, "[Maple-Leaf WATCH]" + getName() + " has been blocked. He is now pending investigation."));
    }

    public void suspendCheater_MWLB(int days, int player_accid) {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + days, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        Timestamp TS = new Timestamp(cal.getTimeInMillis());
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banreason = ?, tempban = ?, greason = ? WHERE id = ?");
            ps.setString(1, "Blocked by MWLB member " + getName() + " for hacking.");
            ps.setTimestamp(2, TS);
            ps.setInt(3, 1);
            ps.setInt(4, player_accid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public int getSuspectAccId_MWLB() {
        return suspectManager.chr == null ? -1 : suspectManager.chr.getClient().getAccID();
    }

    public boolean isBanned() {
        return isbanned;
    }

    public int[] getTrockMaps() {
        return trockmaps;
    }

    public int[] getVipTrockMaps() {
        return viptrockmaps;
    }

    public int getTrockSize() {
        int ret = 0;
        for (int i = 0; i < 5; i++) {
            if (trockmaps[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromTrocks(int map) {
        for (int i = 0; i < 5; i++) {
            if (trockmaps[i] == map) {
                trockmaps[i] = 999999999;
                break;
            }
        }
    }

    public void addTrockMap() {
        if (getTrockSize() >= 5) {
            return;
        }
        trockmaps[getTrockSize()] = getMapId();
    }

    public boolean isTrockMap(int id) {
        for (int i = 0; i < 5; i++) {
            if (trockmaps[i] == id) {
                return true;
            }
        }
        return false;
    }

    public int getVipTrockSize() {
        int ret = 0;
        for (int i = 0; i < 10; i++) {
            if (viptrockmaps[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromVipTrocks(int map) {
        for (int i = 0; i < 10; i++) {
            if (viptrockmaps[i] == map) {
                viptrockmaps[i] = 999999999;
                break;
            }
        }
    }

    public void addVipTrockMap() {
        if (getVipTrockSize() >= 10) {
            return;
        }

        viptrockmaps[getVipTrockSize()] = getMapId();
    }

    public boolean isVipTrockMap(int id) {
        for (int i = 0; i < 10; i++) {
            if (viptrockmaps[i] == id) {
                return true;
            }
        }
        return false;
    }
    //EVENTS
    private byte team = 0;
    private MapleFitness fitness;
    private MapleOla ola;
    private long snowballattack;

    public byte getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = (byte) team;
    }

    public MapleOla getOla() {
        return ola;
    }

    public void setOla(MapleOla ola) {
        this.ola = ola;
    }

    public MapleFitness getFitness() {
        return fitness;
    }

    public void setFitness(MapleFitness fit) {
        this.fitness = fit;
    }

    public long getLastSnowballAttack() {
        return snowballattack;
    }

    public void setLastSnowballAttack(long time) {
        this.snowballattack = time;
    }
    //Monster Carnival
    private int cp = 0;
    private int obtainedcp = 0;
    private MonsterCarnivalParty carnivalparty;
    private MonsterCarnival carnival;

    public MonsterCarnivalParty getCarnivalParty() {
        return carnivalparty;
    }

    public void setCarnivalParty(MonsterCarnivalParty party) {
        this.carnivalparty = party;
    }

    public MonsterCarnival getCarnival() {
        return carnival;
    }

    public void setCarnival(MonsterCarnival car) {
        this.carnival = car;
    }

    public int getCP() {
        return cp;
    }

    public int getObtainedCP() {
        return obtainedcp;
    }

    public void addCP(int cp) {
        this.cp += cp;
        this.obtainedcp += cp;
    }

    public void useCP(int cp) {
        this.cp -= cp;
    }

    public void setObtainedCP(int cp) {
        this.obtainedcp = cp;
    }

    public int getAndRemoveCP() {
        int rCP = 10;
        if (cp < 9) {
            rCP = cp;
            cp = 0;
        } else {
            cp -= 10;
        }

        return rCP;
    }

    public AutobanManager getAutobanManager() {
        return autoban;
    }

    public void equipPendantOfSpirit() {
        if (pendantOfSpirit == null) {
            pendantOfSpirit = TimerManager.getInstance().register(new Runnable() {

                @Override
                public void run() {
                    if (pendantExp < 3) {
                        pendantExp++;
                        message("Pendant of the Spirit has been equipped for " + pendantExp + " hour(s), you will now receive " + pendantExp + "0% bonus exp.");
                    }
                }
            }, 3600000); //1 hour
        }
    }

    public void unequipPendantOfSpirit() {
        if (pendantOfSpirit != null) {
            pendantOfSpirit.cancel(false);
            pendantOfSpirit = null;
        }
        pendantExp = 0;
    }

    public void increaseEquipExp(int mobexp) {
        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        for (IItem item : getInventory(MapleInventoryType.EQUIPPED).list()) {
            Equip nEquip = (Equip) item;
            String itemName = mii.getName(nEquip.getItemId());
            if (itemName == null) {
                continue;
            }

            if ((itemName.contains("Reverse") && nEquip.getItemLevel() < 4) || itemName.contains("Timeless") && nEquip.getItemLevel() < 6) {
                nEquip.gainItemExp(client, mobexp, itemName.contains("Timeless"));
            }
        }
    }

    public MapleEvents getEvents() {
        return events;
    }

    public boolean hasPvpMonster() {
        return map.getMapObject(MapleMapObjectType.MONSTER, pvpmonsteroid) != null;
    }

    public MapleMonster getPvpMonster() {
        return hasPvpMonster() ? (MapleMonster) map.getMapObject(MapleMapObjectType.MONSTER, pvpmonsteroid) : null;
    }

    public void setPvpMonster(boolean b) {
        if (b) {
            MapleMonster m = MapleLifeFactory.getMonster(9400713);
            m.setHp(this.maxhp);
            m.setPvpOwner(this.id);
            getMap().broadcastMessage(this, MaplePacketCreator.spawnMonster(m, true), false);
            pvpmonsteroid = m.getObjectId();
        } else {
            getMap().killMonster(getPvpMonster(), null, false);
            pvpmonsteroid = -1;
        }
    }

    public int getPvpKills() {
        return pvpkills;
    }

    public void addPvpKill() {
        pvpkills++;
        dropMessage("Congratulations! You have " + pvpkills + " PvP Kills!");
    }

    public int getPvpDeaths() {
        return pvpdeaths;
    }

    public void addPvpDeath() {
        pvpdeaths++;
        dropMessage("Boooo! You now have " + pvpdeaths + " PvP Deaths!");
    }

    public boolean hasPvpEnabled() {
        return hasPvpEnabled;
    }

    public void enablePvp() {
        hasPvpEnabled = true;
        setPvpMonster(true);
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
        int[] s = {str, dex, int_, luk};
        MapleStat[] st = {MapleStat.STR, MapleStat.DEX, MapleStat.INT, MapleStat.LUK};
        for (int i = 0; i < s.length; i++) {
            if (s[i] > 1000) {
                s[i] = 1000;
            }
            s[i] += (getReborns() * 1.5);
            stats.add(new Pair(st[i], s[i]));
        }
        getClient().getSession().write(MaplePacketCreator.temporaryStats(stats));
    }

    public void disablePvp() {
        hasPvpEnabled = false;
        setPvpMonster(false);
        getClient().getSession().write(MaplePacketCreator.resetTemporaryStats());
    }

    public int getReborns() {
        return reborns; // just for now, to get rid of the error calculating pvp damage
    }

    public PartyQuest getPQ() {
        return pq;
    }

    public void setPQ(PartyQuest _pq) {
        pq = _pq;
    }

    public StagedPartyQuest getSPQ() {
        return spq;
    }

    public void setSPQ(StagedPartyQuest newSpq) {
        spq = newSpq;
    }

    public EnumMap<MapleAchievementLibrary, MapleAchievement> getAchievements() {
        return achievements;
    }

    public void addAchievementToStack(MapleAchievement toAdd) {
        achievements.put(MapleAchievementLibrary.getById(toAdd.id), toAdd);
    }

    public boolean achievementDone(int id) {
        return MapleAchievement.isAchievementAccomplished(getId(), id);
    }

    public MapleAchievementProgress getAchievementsProgress() {
        return achievementProgress;
    }

    public void completeAchievement(MapleAchievementLibrary mal) {
        completeAchievement(mal.getId());
    }

    public MapleFamilyCharacter getFamilyChar() {
        if (familyChar == null) {
            familyChar = new MapleFamilyCharacter(this);
            return familyChar;
        } else {
            return familyChar;
        }
    }

    public void setFamilyChar(MapleFamilyCharacter chr) {
        this.familyChar = chr;
    }

    public MapleFamilyBonus getFamilyBonus() {
        return familyBonus;
    }

    public void setFamilyBonus(MapleFamilyBonus s) {
        familyBonus = s;
    }

    public short getCampaignProgress() {
        return campaignProgress;
    }

    public void setCampaignProgress(short progress) {
        campaignProgress = progress;
    }

    public void warpToFarm() {
        MapleFarmHouse mfh = MapleFarmHouse.getFarm(MapleFarmHouse.getFarmID(getClient().getAccID()), getClient().getAccID(), client.getChannel());
        this.changeMap(mfh);
    }

    public void gainItem(int id, short quantity, boolean randomStats) {
        gainItem(id, quantity, randomStats, false);
    }

    public void gainPotentialItem(int id, boolean randomStats) {
        gainItem(id, (short) 1, true, true);
    }

    public void gainItem(int id, short quantity, boolean randomStats, boolean givePotential) {
        MapleClient c = client;
        if (id >= 5000000 && id <= 5000100) {
            MapleInventoryManipulator.addById(c, id, (short) 1, null, MaplePet.createPet(id), -1);
            return; // NO PETS..
        }
        if (quantity >= 0) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            IItem item = ii.getEquipById(id);
            if (givePotential) {
                Equip eq = (Equip) item;
                eq.potential = 1;
            }
            if (!MapleInventoryManipulator.checkSpace(c, id, quantity, "")) {
                c.getPlayer().dropMessage(1, "Your inventory is full. Please remove an item from your " + ii.getInventoryType(id).name() + " inventory.");
                return;
            }
            if (ii.getInventoryType(id).equals(MapleInventoryType.EQUIP) && !ItemConstants.isRechargable(item.getItemId())) {
                if (randomStats) {
                    MapleInventoryManipulator.addFromDrop(c, ii.randomizeStats((Equip) item), false);
                } else {
                    MapleInventoryManipulator.addFromDrop(c, (Equip) item, false);
                }
            } else {
                MapleInventoryManipulator.addById(c, id, quantity);
            }
        } else {
            MapleInventoryManipulator.removeById(c, MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, false);
        }
        c.announce(MaplePacketCreator.getShowItemGain(id, quantity, true));
    }

    public void openNpc(int id) {
        NPCScriptManager.getInstance().dispose(client);
        NPCScriptManager.getInstance().start(client, id, null, null);
    }

    public void openNpc(int id, String scriptName) {
        NPCScriptManager.getInstance().dispose(client);
        NPCScriptManager.getInstance().start(client, id, scriptName, null);
    }

    public void initiateStarterMapRaid() {
        Point pos = new Point(345, 155);
        for (int i = 0; i < 9; i++) {
            getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(100100), pos);
        }
        this.shutDownLock = true;
    }

    public void autoJob() {
        int job_ = getJob().getId();
        if (isDualBlade()) {
            switch (getLevel()) {
                case 20:
                    changeJob(MapleJob.BLADE_RECRUIT);
                    break;
                case 30:
                    changeJob(MapleJob.BLADE_ACOLYTE);
                    break;
                case 55:
                    changeJob(MapleJob.BLADE_SPECIALIST);
                    break;
                case 70:
                    changeJob(MapleJob.BLADE_LORD);
                    break;
                case 120:
                    changeJob(MapleJob.BLADE_MASTER);
                    break;
            }
        } else if (getJob().getId() >= MapleJob.BATTLEMAGE1.getId() && getJob().getId() <= MapleJob.BATTLEMAGE4.getId()) {
            switch (getLevel()) {
                case 30:
                    changeJob(MapleJob.BATTLEMAGE2);
                    break;
                case 70:
                    changeJob(MapleJob.BATTLEMAGE3);
                    break;
                case 120:
                    changeJob(MapleJob.BATTLEMAGE4);
                    break;
            }
        } else if (isEvan()) {
            switch (getLevel()) {
                case 10:
                    changeJob(MapleJob.EVAN1);
                    break;
                case 20:
                    changeJob(MapleJob.EVAN2);
                    break;
                case 30:
                    changeJob(MapleJob.EVAN3);
                    break;
                case 40:
                    changeJob(MapleJob.EVAN4);
                    break;
                case 50:
                    changeJob(MapleJob.EVAN5);
                    break;
                case 60:
                    changeJob(MapleJob.EVAN6);
                    break;
                case 80:
                    changeJob(MapleJob.EVAN7);
                    break;
                case 100:
                    changeJob(MapleJob.EVAN8);
                    break;
                case 120:
                    changeJob(MapleJob.EVAN9);
                    break;
                case 160:
                    changeJob(MapleJob.EVAN10);
                    break;
                default:
                    break;
            }
        } else if (getJob().getId() >= MapleJob.WILDHUNTER1.getId() && getJob().getId() <= MapleJob.WILDHUNTER4.getId()) {
            switch (getLevel()) {
                case 30:
                    changeJob(MapleJob.WILDHUNTER2);
                    break;
                case 70:
                    changeJob(MapleJob.WILDHUNTER3);
                    break;
                case 120:
                    changeJob(MapleJob.WILDHUNTER4);
                    break;
            }
        } else if (isMechanic()) {
            switch (getLevel()) {
                case 30:
                    changeJob(MapleJob.MECHANIC2);
                    break;
                case 70:
                    changeJob(MapleJob.MECHANIC3);
                    break;
                case 120:
                    changeJob(MapleJob.MECHANIC4);
                    break;
            }
        } else {
            if (!isAran() && level == 9 || !isAran() && !isCygnus() && level == 29) {
                NPCScriptManager.getInstance().start(getClient(), 2003, null, null);
            } else if (isAran() && level == 9) {
                changeJob(MapleJob.getById(2100));
            } else if (isAran() && level == 29) {
                changeJob(MapleJob.getById(2110));
            } else if (isCygnus() && level == 29) {
                changeJob(MapleJob.getById(job_ + 10));
            } else if (isCygnus() && level == 69) {
                changeJob(MapleJob.getById(job_ + 1));
            } else if (isAran() && level == 69) {
                changeJob(MapleJob.getById(2111));
            } else if (isAran() && level == 119) {
                changeJob(MapleJob.getById(2112));
            }
            if (!isAran() && !isCygnus() && level == 69) {
                if (is2ndJob()) {
                    changeJob(MapleJob.getById(job_ + 1));
                } else {
                    NPCScriptManager.getInstance().start(getClient(), 2003, null, null);
                }
            } else if (!isCygnus() && !isAran() && level == 119) {
                if (is3rdJob()) {
                    changeJob(MapleJob.getById(job_ + 1));
                } else if (is2ndJob()) {
                    changeJob(MapleJob.getById(job_ + 1));
                } else {
                    NPCScriptManager.getInstance().start(getClient(), 2003, null, null);
                }
            }
        }
    }

    public boolean is2ndJob() {
        return getJob().getId() % 10 == 0 && getJob().getId() % 100 != 0;
    }

    public boolean is3rdJob() {
        return getJob().getId() % 10 == 1;
    }

    public void maxSkillsByCurrJob() {
        for (provider.MapleData skill_ : provider.MapleDataProviderFactory.getDataProvider(new java.io.File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
            try {
                ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                if ((skill.getId() / 10000) == getJob().getId() && !SkillFactory.getSkillName(skill.getId()).toLowerCase().contains("final attack")) {
                    changeSkillLevel(skill, (byte) skill.getMaxLevel(), skill.getMaxLevel(), -1);
                }
            } catch (NumberFormatException nfe) {
                break;
            } catch (NullPointerException npe) {
                continue;
            }
        }
    }

    public void removeFinalAttack() {
        boolean skillChanged = false;
        for (ISkill skill : getSkills().keySet()) {
            if (SkillFactory.getSkillName(skill.getId()).toLowerCase().contains("final attack")) {
                this.changeSkillLevel(skill, (byte) 0, 0, -1);
                skillChanged = true;
            }
        }
        if (skillChanged) {
            dropMessage(5, "'Final Attack' is a bugged skill - so it was removed from your skills.");
        }
    }

    public boolean isDonator() {
        return MapleDonator.isDonator(getClient().getAccountName());
    }

    public int getSummonerID() {
        int job = getJob().getId();
        if (job >= 200 && job < 300) {
            return 2321003;
        } else {
            return 3121006;
        }
    }

    public void dispose() {
        if (client.getChannelServer().getPlayerStorage().getCharacterByName(getName()) != null) {
            client.getChannelServer().removePlayer(this);
        }
        if (getMount() != null) {
            this.getMount().cancelSchedule();
        }
        if (BerserkSchedule != null) {
            this.BerserkSchedule.cancel(true);
            this.BerserkSchedule = null;
        }
        this.achievementProgress = null;
        this.achievements = null;
        this.autoban = null;
        if (beholderBuffSchedule != null) {
            this.beholderBuffSchedule.cancel(true);
            this.beholderBuffSchedule = null;
        }
        if (beholderHealingSchedule != null) {
            this.beholderHealingSchedule.cancel(true);
            this.beholderHealingSchedule = null;
        }
        this.area_data = null;
        this.blockedPortals = null;
        this.buddylist = null;
        this.carnival = null;
        this.cashshop = null;
        this.commands = null;
        this.controlled = null;
        this.coolDowns = null;
        this.crushRings = null;
        this.diseases = null;
        this.dojoMap = null;
        if (dragonBloodSchedule != null) {
            this.dragonBloodSchedule.cancel(true);
            this.dragonBloodSchedule = null;
        }
        this.doors = null;
        this.effects = null;
        this.entered = null;
        this.eventInstance = null;
        this.events = null;
        this.excluded = null;
        if (this.expiretask != null) {
            this.expiretask.cancel(true);
            this.expiretask = null;
        }
        this.family = null;
        this.familyBonus = null;
        this.familyChar = null;
        this.fitness = null;
        this.friendshipRings = null;
        if (fullnessSchedule != null) {
            this.fullnessSchedule.cancel(true);
            this.fullnessSchedule = null;
        }
        if (fullnessSchedule_1 != null) {
            fullnessSchedule_1.cancel(true);
            fullnessSchedule_1 = null;
        }
        if (fullnessSchedule_2 != null) {
            fullnessSchedule_2.cancel(true);
            fullnessSchedule_2 = null;
        }
        if (hpDecreaseTask != null) {
            this.hpDecreaseTask.cancel(true);
            this.hpDecreaseTask = null;
        }
        this.inventory = null;
        if (keymap != null) {
            this.keymap.clear();
            this.keymap = null;
        }
        this.lastmonthfameids.clear();
        this.lastmonthfameids = null;
        if (mapTimeLimitTask != null) {
            this.mapTimeLimitTask.cancel(true);
            this.mapTimeLimitTask = null;
        }
        this.maplemount = null;
        this.marriageRing = null;
        this.md = null;
        this.mgc = null;
        this.monsterbook = null;
        this.mpc = null;
        this.ola = null;
        this.map = null;
        this.pets = null;
        this.party = null;
        if (pendantOfSpirit != null) {
            this.pendantOfSpirit.cancel(true);
            this.pendantOfSpirit = null;
        }
        this.pq = null;
        if (quests != null) {
            this.quests.clear();
            this.quests = null;
        }
        this.savedLocations = null;
        this.shop = null;
        this.skillMacros = null;
        this.skills.clear();
        this.skills = null;
        this.storage = null;
        this.summons.clear();
        this.summons = null;
        this.visibleMapObjects.clear();
        this.visibleMapObjects = null;
        this.evanDragon = null;
        this.suspectManager = null;
        this.table = null;
        this.NPCLock = null;
    }

    public boolean isResistance() {
        return getJob().getId() >= MapleJob.CITIZEN.getId() && getJob().getId() <= MapleJob.MECHANIC4.getId();
    }

    public boolean isEvan() {
        return getJob().getId() == MapleJob.FARMER.getId() || getJob().getId() >= MapleJob.EVAN1.getId() && getJob().getId() <= MapleJob.EVAN10.getId();
    }

    public ExtendedSPTable getSPTable() {
        return table;
    }

    public boolean isExtendedSPType() {
        if (table != null) {
            return table.getSPFromJobID(getJob().getId()) != -1;
        } else {
            table = new ExtendedSPTable(job.getId());
            return table.getSPFromJobID(getJob().getId()) != -1;
        }
    }

    public boolean isDualBlade() {
        return getJob().getId() >= MapleJob.BLADE_RECRUIT.getId() && getJob().getId() <= MapleJob.BLADE_MASTER.getId() || isDB;
    }

    public MapleDragon getEvanDragon() {
        return isEvan() ? evanDragon : null;
    }

    public void setEvanDragon(MapleDragon delta) {
        evanDragon = delta;
    }

    public Set<MapleBuffStat> getBuffEffects() {
        return this.effects.keySet();
    }

    public void maxAllSkillsByJobGroup() {
        if (SkillType.getSkillTypeByJobID(getJob().getId()) == SkillType.NONE) {
            return;
        } else {
            for (ISkill skill : SkillFactory.getAllSkillsByType(SkillType.getSkillTypeByJobID(getJob().getId())).values()) {
                this.changeSkillLevel(skill, (byte) skill.getMaxLevel(), skill.getMaxLevel() + 1, -1);
            }
        }
    }

    public boolean isMWLB() {
        return isMWLB;
    }

    public Pair<Integer, Integer> getDecentHBHPMPBonus() {
        int hpUp = 0;
        int mpUp = 0;
        // DECENT
        boolean hasDecentHB = false;
        int[] decentHB = {10008003, 20008003, 20018003, 8003, 30008003};
        for (int i = 0; i < decentHB.length; i++) {
            if (this.getSkillLevel(decentHB[i]) > 0) {
                hpUp += (int) (localmaxhp * 0.40);
                mpUp += (int) (localmaxhp * 0.40);
            }
        }

        return new Pair(hpUp, mpUp);
    }

    public boolean isJaguar() {
        return getJob().getId() >= MapleJob.WILDHUNTER1.getId() && getJob().getId() <= MapleJob.WILDHUNTER4.getId();
    }

    public boolean isMechanic() {
        return getJob().getId() >= MapleJob.MECHANIC1.getId() && getJob().getId() <= MapleJob.MECHANIC4.getId();
    }

    public ScheduledFuture<?> getRobotSchedule() {
        return this.mech_robotTask;
    }

    public void startRobotHealingSchedule(final int skill) {
        if (mech_robotTask != null) {
            mech_robotTask.cancel(true);
        }

        mech_robotTask = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                if (getParty() == null) {
                    for (MaplePartyCharacter mpc : getParty().getMembers()) {
                        MapleCharacter chr = mpc.getPlayer();
                        double distance = chr.getPosition().distanceSq(getSummons().get(skill).getPosition());
                        if (distance <= 600000.0) {
                            if ((int) ((double) (chr.getHp() / chr.getMaxHp()) * 100) < 60) {
                                int hpToHeal = (int) (chr.getMaxHp() * .25);
                                chr.addHP(hpToHeal);
                            }
                        }
                    }
                } else {
                    double distance = getPosition().distanceSq(getSummons().get(skill).getPosition());
                    if (distance <= 600000.0) {
                        if ((int) ((double) (getHp() / getMaxHp()) * 100) < 60) {
                            int hpToHeal = (int) (getMaxHp() * .25);
                            addHP(hpToHeal);
                        }
                    }
                }
            }
        }, 1000 * 3);
    }

    public MapleSummon getDarkFlare() {
        for (MapleSummon ms : getSummons().values()) {
            if (ms.getSkill() == Hermit.DARK_FLARE || ms.getSkill() == ChiefBandit.DARK_FLARE) {
                return ms;
            }
        }

        return null;
    }

    public void doReborn(String name) {
        if (getLevel() >= 200) {
            setReborns(getReborns() + 1);
            List<Pair<MapleStat, Integer>> reborn = new ArrayList<Pair<MapleStat, Integer>>(4);
            setLevel(1);
            setExp(0);
            setJob(name.equals("Explorer") ? MapleJob.BEGINNER : name.equals("Cygnus") ? MapleJob.NOBLESSE : MapleJob.LEGEND);
            updateSingleStat(MapleStat.LEVEL, 1);
            updateSingleStat(MapleStat.JOB, name.equals("Explorer") ? 0 : name.equals("Cygnus") ? 1000 : 2000);
            updateSingleStat(MapleStat.EXP, 0);
        }
    }

    public void setReborns(int amt) {
        this.reborns = amt;
    }

    public boolean inBlockedMap() {
        for (int i = 0; i < ServerConstants.BLOCKED_COMMANDS_MAPS.length; i++) {
            if (this.getMapId() == ServerConstants.BLOCKED_COMMANDS_MAPS[i]) {
                return true;
            }
        }
        return false;
    }
    private long askouttime, gmcommandtime, shouttime;

    public long getLastAskOutTime() {
        return askouttime;
    }

    public long setLastAskOutTime() {
        return askouttime = System.currentTimeMillis() / 60000;
    }

    public long getLastGmCommandTime() {
        return gmcommandtime;
    }

    public long setLastGmCommandTime() {
        return gmcommandtime = System.currentTimeMillis() / 60000;
    }

    public long getLastShoutTime() {
        return shouttime;
    }

    public long setLastShoutTime() {
        return shouttime = System.currentTimeMillis() / 60000;
    }

    public void setSeenAllMail() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE mail SET `read` = 1 WHERE MailReciever = ?");
            ps.setString(1, getName());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            return;
        }
    }

    public int newMail() {
        int mail = 0;
        PreparedStatement ps = null;
        Connection con = DatabaseConnection.getConnection();
        try {
            ps = con.prepareStatement("SELECT COUNT(*) as c FROM mail where mailreciever = ? and `read` = 0 and deleted = 0");
            ps.setString(1, getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                mail = rs.getInt("c");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        }
        return mail;
    }

    public void sendMail(String Reciever, String message) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO mail (`MailSender`, `MailReciever`, `Message`) VALUES (?, ?, ?)");
        ps.setString(1, getName());
        ps.setString(2, Reciever);
        ps.setString(3, message);
        ps.executeUpdate();
        ps.close();
    }

    public void setRelationship(int level) {
        this.relationship = level;
    }

    public int getRelationship() {
        return this.relationship;
    }

    public void sendNote(int to, String msg) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
        ps.setInt(1, to);
        ps.setString(2, this.getName());
        ps.setString(3, msg);
        ps.setLong(4, System.currentTimeMillis());
        ps.executeUpdate();
        ps.close();
    }

    public void changeViewingSmegas() {
        wantstoviewsmega = !wantstoviewsmega;
    }

    public boolean wantsToViewSmega() {
        return wantstoviewsmega;
    }
    private int blockedfromsemga;

    public void setBlockFromSmega(int amt) {
        this.blockedfromsemga = amt;
    }

    public boolean isBlockedFromSmega() {
        return blockedfromsemga == 1 ? true : false;
    }

    public void reloadChar() {
        getClient().getSession().write(MaplePacketCreator.getCharInfo(this));
        getMap().removePlayer(this);
        getMap().addPlayer(this);
    }

    public void setName(String name, boolean changeName) {
        if (!changeName) {
            this.name = name;
        } else {
            Connection con = DatabaseConnection.getConnection();
            try {
                con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                con.setAutoCommit(false);
                PreparedStatement sn = con.prepareStatement("UPDATE characters SET name = ? WHERE id = ?");
                sn.setString(1, name);
                sn.setInt(2, id);
                sn.execute();
                con.commit();
                sn.close();
                this.name = name;
            } catch (SQLException e) {
            }
        }
    }

    public void ban(String reason) {
        try {
            getClient().banMacs();
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?");
            ps.setInt(1, 1);
            ps.setString(2, reason);
            ps.setInt(3, accountid);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
            String[] ipSplit = client.getSession().getRemoteAddress().toString().split(":");
            ps.setString(1, ipSplit[0]);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("Error while banning " + ex);
        }
        client.getSession().write(MaplePacketCreator.sendPolice("You are banned."));
        TimerManager.getInstance().schedule(new Runnable() {

            public void run() {
                client.getSession().close();
            }
        }, 10000);
    }

    public void changeGMChat() {
        gmchat = !gmchat;
    }

    public int getMuted() {
        return mute;
    }

    public boolean isPermMuted() {
        return mute == 2 ? true : false;
    }

    public boolean isMuted() {
        return mute > 0 ? true : false;
    }

    public void setMuteLevel(int newlevel) {
        this.mute = newlevel;
    }

    public boolean isMarried() {
        return getMarried() == 1 ? true : false;
    }

    public static boolean unban(String id, boolean accountId) {
        boolean ret = false;
        try {
            PreparedStatement ps;
            Connection con = DatabaseConnection.getConnection();
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PreparedStatement psb = con.prepareStatement("UPDATE accounts SET banned = -1 WHERE id = ?");
                psb.setInt(1, rs.getInt(1));
                psb.executeUpdate();
                psb.close();
                ret = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("Error with unbanning :" + ex);
        }
        return ret;
    }

    public static boolean unbanIP(String id) {
        String banString = "";
        PreparedStatement ps;
        Connection con = DatabaseConnection.getConnection();
        boolean ret = false;
        try {
            ps = con.prepareStatement("SELECT banreason FROM accounts WHERE name = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ps.close();
            }
            banString = rs.getString("banreason");
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error with unbanning IP :" + e);
        }
        if (banString.indexOf("IP: /") != -1) {
            String ip = banString.substring(banString.indexOf("IP: /") + 5, banString.length() - 1);
            try {
                ps = con.prepareStatement("DELETE FROM ipbans WHERE ip = ?");
                ps.setString(1, ip);
                ps.executeUpdate();
                ps.close();
                ret = true;
            } catch (SQLException exe) {
                System.out.println("Error with unbanning IP :" + exe);
            }
        }
        return ret;
    }
}