
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
package server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import constants.SkillConstants.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Calendar;
import client.Equip;
import client.IItem;
import client.Item;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MaplePet;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.ItemConstants;
import tools.Randomizer;
import constants.ServerConstants;
import net.MaplePacket;
import net.channel.ChannelServer;
import scripting.map.MapScriptManager;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.SpawnPoint;
import tools.MaplePacketCreator;
import server.events.MapleCoconut;
import server.events.MapleFitness;
import server.events.MapleOla;
import server.events.MapleOxQuiz;
import server.events.MapleSnowball;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import tools.Pair;
import java.util.concurrent.locks.ReentrantReadWriteLock.*;
import server.calendar.CalendarEventsLibrary;
import server.life.*;

public class MapleMap {

    private static final List<MapleMapObjectType> rangedMapobjectTypes = Arrays.asList(MapleMapObjectType.SHOP, MapleMapObjectType.ITEM, MapleMapObjectType.NPC, MapleMapObjectType.MONSTER, MapleMapObjectType.DOOR, MapleMapObjectType.SUMMON, MapleMapObjectType.REACTOR, MapleMapObjectType.PLAYER_NPC, MapleMapObjectType.EVAN_DRAGON, MapleMapObjectType.POKEMON);
    private final Map<Integer, MapleMapObject> mapobjects = new LinkedHashMap<Integer, MapleMapObject>();
    private Collection<SpawnPoint> monsterSpawn = new LinkedList<SpawnPoint>();
    private final AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private final Collection<MapleNPC> npcs = new ArrayList<MapleNPC>();
    private final Collection<MapleCharacter> characters = new ArrayList<MapleCharacter>();
    private Map<Integer, MaplePortal> portals = new HashMap<Integer, MaplePortal>();
    private List<Rectangle> areas = new ArrayList<Rectangle>();
    private MapleFootholdTree footholds = null;
    private static MapleNPCStats stats;
    private int mapid;
    private int runningOid = 100000;
    private int returnMapId;
    private int channel;
    private float monsterRate;
    private boolean clock;
    private boolean boat;
    private boolean docked;
    private String mapName;
    private String streetName;
    private MapleMapEffect mapEffect = null;
    private boolean everlast = false;
    private int forcedReturnMap = 999999999;
    private long timeLimit;
    private int decHP = 0;
    private int protectItem = 0;
    private boolean town;
    private MapleOxQuiz ox;
    private boolean isOxQuiz = false;
    private boolean dropsOn = true;
    private String onFirstUserEnter;
    private String onUserEnter;
    private int fieldType;
    private int fieldLimit = 0;
    private ScheduledFuture<?> mapMonitor = null;
    //   private ScheduledFuture<?> respawnWorker;
    private Pair<Integer, String> timeMob = null;
    // HPQ
    private int riceCakeNum = 0; // bad place to put this (why is it in here then)
    private boolean allowHPQSummon = false; // bad place to put this
    // events
    private boolean eventstarted = false;
    private MapleSnowball snowball0 = null;
    private MapleSnowball snowball1 = null;
    private MapleCoconut coconut;
    private boolean spawnStarter = false;

    public MapleMap(int mapid, int channel, int returnMapId, float monsterRate) {
        this.mapid = mapid;
        this.channel = (short) channel;
        this.returnMapId = returnMapId;
        this.monsterRate = monsterRate;
        //   initializeSpawn();
    }

    public void broadcastMessage(MapleCharacter source, MaplePacket packet) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source) {
                    chr.getClient().announce(packet);
                }
            }
        }
    }

    public int getSpawnedMOnMap() {
        return this.spawnedMonstersOnMap.get();
    }

    /*public void initializeRespawnWorker(){
    respawnWorker = TimerManager.getInstance().register(new Runnable(){
    @Override
    public void run(){
    ChannelServer.getInstance(channel).getMapFactory().getMap(mapid).respawn();
    }
    }, (int)(10000 * monsterRate));
    }*/
    public void broadcastGMMessage(MapleCharacter source, MaplePacket packet) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && (chr.gmLevel() > source.gmLevel())) {
                    chr.getClient().announce(packet);
                }
            }
        }
    }

    public void toggleDrops() {
        this.dropsOn = !dropsOn;
    }

    public List<MapleMapObject> getMapObjectsInRect(Rectangle box, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (box.contains(l.getPosition())) {
                        ret.add(l);
                    }
                }
            }
        }
        return ret;
    }

    public int getId() {
        return mapid;
    }

    public MapleMap getReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(returnMapId);
    }

    public int getReturnMapId() {
        return returnMapId;
    }

    public void setReactorState() {
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) o).getState() < 1) {
                        ((MapleReactor) o).setState((byte) 1);
                        broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 1));
                    }
                }
            }
        }
    }

    public int getForcedReturnId() {
        return forcedReturnMap;
    }

    public MapleMap getForcedReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(forcedReturnMap);
    }

    public void setForcedReturnMap(int map) {
        this.forcedReturnMap = map;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getTimeLeft() {
        return (int) ((timeLimit - System.currentTimeMillis()) / 1000);
    }

    public int getCurrentPartyId() {
        for (MapleCharacter chr : this.getCharacters()) {
            if (chr.getPartyId() != -1) {
                return chr.getPartyId();
            }
        }
        return -1;
    }

    public void addMapObject(MapleMapObject mapobject) {
        synchronized (this.mapobjects) {
            mapobject.setObjectId(runningOid);
            this.mapobjects.put(Integer.valueOf(runningOid), mapobject);
            incrementRunningOid();
        }
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery) {
        spawnAndAddRangedMapObject(mapobject, packetbakery, null);
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery, SpawnCondition condition) {
        this.addMapObject(mapobject);
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (condition == null || condition.canSpawn(chr)) {
                    if (chr.getPosition().distanceSq(mapobject.getPosition()) <= 722500) {
                        packetbakery.sendPackets(chr.getClient());
                        chr.addVisibleMapObject(mapobject);
                    }
                }
            }
        }
    }

    private void incrementRunningOid() {
        runningOid++;
        /*   if (!this.mapobjects.containsKey(Integer.valueOf(runningOid))) {
        return;
        }
        for (int numIncrements = 1; numIncrements < 30000; numIncrements++) {
        if (runningOid > 30000) {
        runningOid = 100;
        }
        if (this.mapobjects.containsKey(Integer.valueOf(runningOid))) {
        runningOid++;
        } else {
        return;
        }
        }
        
        throw new RuntimeException("Out of OIDs on map " + mapid + " (channel: " + channel + ")");*/
        //unneeded and costly
    }

    public void removeMapObject(int num) {
        synchronized (this.mapobjects) {
            this.mapobjects.remove(Integer.valueOf(num));
        }
    }

    public void removeMapObject(MapleMapObject obj) {
        removeMapObject(obj.getObjectId());
    }

    public void removeMapObject(MapleMapObjectType type, MapleMapObject obj) {
        removeMapObject(obj.getObjectId());
    }

    private Point calcPointBelow(Point initial) {
        MapleFoothold fh = footholds.findBelow(initial);
        if (fh == null) {
            fh = footholds.findBelow(new Point(0, 0));
        }
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            double s5 = Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2)));
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) s5;
            } else {
                dropY = fh.getY1() + (int) s5;
            }
        }
        return new Point(initial.x, dropY);
    }

    private Point calcDropPos(Point initial, Point fallback) {
        Point ret = calcPointBelow(new Point(initial.x, initial.y - 50));
        if (ret == null) {
            return fallback;
        }
        return ret;
    }

    private void dropFromMonster(final MapleCharacter chr, final MapleMonster mob) {
        if (mob.dropsDisabled() || !dropsOn) {
            chr.dropMessage("Either the drops are disabled or the mob doesn't drop anything..");
            return;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final byte droptype = (byte) (mob.getStats().isExplosiveReward() ? 3 : mob.getStats().isFfaLoot() ? 2 : chr.getParty() != null ? 1 : 0);
        final int mobpos = mob.getPosition().x, chServerrate = ServerConstants.DROP_RATE;
        IItem idrop;
        byte d = 1;
        Point pos = new Point(0, mob.getPosition().y);
        final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
        if (ii == null || mi == null) {
            chr.dropMessage("Either the itemprovider died or the MapleMonster one did.");
        }
        final List<MonsterDropEntry> dropEntry = new ArrayList<MonsterDropEntry>(mi.retrieveDrop(mob.getId()));
        Collections.shuffle(dropEntry);
        int mesos = (2 * (Math.max(10, mob.getStats().getLevel())));
        mesos += (int) (mesos * Math.random());
        if (chr.getBuffedValue(MapleBuffStat.MESOUP) != null) {
            mesos = (int) (mesos * chr.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
        }
        /* //Gain NX on monster kill if not donator
        if (!chr.getDonator().isActive()) {
            spawnMesoDrop((mesos * ServerConstants.MESO_RATE) / 2, calcDropPos(pos, mob.getPosition()), mob, chr, false, droptype);
            chr.gainMeso((mesos * ServerConstants.MESO_RATE) / 2, true);
            int i = (int) (100.0 * Math.random());
            if (i < 20) {
                chr.announce(MaplePacketCreator.MapMessage("You have gained 50NX!"));
                chr.getCashShop().gainCash(4, 50);
            }
        } else {
        * //Gain NX on monster kill if donator
            int i = (int) (100.0 * Math.random());
            if (i < 20) {
                chr.announce(MaplePacketCreator.MapMessage("You have gained 120 NX Cash Points!"));
                chr.getCashShop().gainCash(4, 120);
            }
        }
        */
        for (final MonsterDropEntry de : dropEntry) {
            if (Randomizer.nextInt(999999) < de.chance * chServerrate) {
                if (droptype == 3) {
                    pos.x = (int) (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
                } else {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                }
                if (de.itemId != 0) {
                    if (ItemConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                        int potential_chance = (int) (100.0 * Math.random());
                        if (potential_chance < 20) {
                            Equip eq = (Equip) idrop;
                            eq.potential = (byte) 1; // is a potential
                        }
                    } else {
                        idrop = new Item(de.itemId, (byte) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum : 1));
                    }
                    Point dropPos = calcDropPos(pos, mob.getPosition());
                    //Move the drops to the donator
                   // dropPos = chr.getDonator().isActive() ? chr.getPosition() : dropPos;
                    spawnDrop(idrop, dropPos, mob, chr, droptype, de.questid);
                    spawnMesoDrop((mesos * ServerConstants.MESO_RATE) / 2, dropPos, mob, chr, false, droptype);
                }
                d++;
            }
        }
        final List<MonsterGlobalDropEntry> globalEntry = mi.getGlobalDrop();
        // Global Drops
        for (final MonsterGlobalDropEntry de : globalEntry) {
            if (Randomizer.nextInt(999999) < de.chance) {
                if (droptype == 3) {
                    pos.x = (int) (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
                } else {
                    pos.x = (int) (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                }
                if (de.itemId == 0) {
                    //chr.getCashShop().gainCash(1, 80);
                } else {
                    if (ItemConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                    } else {
                        idrop = new Item(de.itemId, (byte) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum : 1));
                    }
                    spawnDrop(idrop, calcDropPos(pos, mob.getPosition()), mob, chr, droptype, de.questid);
                    d++;
                }
            }
        }
    }

    private void spawnDrop(final IItem idrop, final Point dropPos, final MapleMonster mob, final MapleCharacter chr, final byte droptype, final short questid) {
        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, mob, chr, droptype, false, questid);
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                if (questid <= 0 || (c.getPlayer().getQuestStatus(questid) == 1 && c.getPlayer().needQuestItem(questid, idrop.getItemId()))) {
                    c.getSession().write(MaplePacketCreator.dropItemFromMapObject(mdrop, mob.getPosition(), dropPos, (byte) 1));
                }
            }
        }, null);

        TimerManager.getInstance().schedule(new ExpireMapItemJob(mdrop), 180000);
        activateItemReactors(mdrop, chr.getClient());
    }

    public final void spawnMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype) {
        final Point droppos = calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, droptype, playerDrop);

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(MaplePacketCreator.dropItemFromMapObjectMeso(mdrop, dropper.getPosition(), droppos, (byte) 1));
            }
        }, null);

        TimerManager.getInstance().schedule(new ExpireMapItemJob(mdrop), 180000);
    }

    public final void disappearingItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, final Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 1, false);
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 3), drop.getPosition());
    }

    public MapleMonster getMonsterById(int id) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.MONSTER) {
                    if (((MapleMonster) obj).getId() == id) {
                        return (MapleMonster) obj;
                    }
                }
            }
        }
        return null;
    }

    public int countMonster(int id) {
        int count = 0;
        for (MapleMapObject m : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster mob = (MapleMonster) m;
            if (mob.getId() == id) {
                count++;
            }
        }
        return count;
    }

    public boolean damageMonster(final MapleCharacter chr, final MapleMonster monster, int damage) {
        if (monster.getId() == 8800000) {
            for (MapleMapObject object : chr.getMap().getMapObjects()) {
                MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                if (mons != null) {
                    if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                        return true;
                    }
                }
            }
        }
        if (monster.isAlive() && monster != null) {
            synchronized (monster) {
                if (!monster.isAlive()) {//lol?
                    return false;
                }
                if (damage > 0) {
                    monster.damage(chr, damage, true);
                    if (!monster.isAlive()) {  // monster just died
                        killMonster(monster, chr, true);
                    }
                } else if (monster.getId() >= 8810002 && monster.getId() <= 8810009) {
                    for (MapleMapObject object : chr.getMap().getMapObjects()) {
                        MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                        if (mons != null) {
                            if (mons.getId() == 8810018) {
                                damageMonster(chr, mons, damage);
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops) {
        try {
            killMonster(monster, chr, withDrops, false, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean secondTime, int animation) throws Exception {
        MapleCharacter dropOwner = null;
        try {
            if (chr == null) {
                spawnedMonstersOnMap.decrementAndGet();
                broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), animation), monster.getPosition());
                removeMapObject(monster);
                return;
            } else {
                try {
                    spawnedMonstersOnMap.decrementAndGet();
                    removeMapObject(monster);
                    broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), animation), monster.getPosition());
                    dropOwner = monster.killBy(chr);
                } finally {
                    if (withDrops) {
                        if (dropOwner == null) {
                            dropOwner = chr;
                        }
                        this.dropFromMonster(dropOwner, monster);
                    }
                }
            }
        } finally {
            /*MapleCharacter player = chr;
            if(player.getDonator().isActive()){
            int chance = (int)(100.0 * Math.random());
            if(chance >= 99){
            client.ISkill blind;
            if(player.getDonator().isActive() && (int)(100.0 * Math.random()) > 70){
            //2311005
            blind = client.SkillFactory.getSkill(2311005);
            monster.applyStatus(player, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.DOOM, 1), client.SkillFactory.getSkill(2311005), null, false), false, 15000);
            chr.changeSkillLevel(blind, (byte)blind.getMaxLevel(), blind.getMaxLevel(), -1);
            } if(player.getDonator().isActive() && (int)(100.0 * Math.random()) > 60){
            // 3101005
            blind = client.SkillFactory.getSkill(4111003);
            monster.applyStatus(player, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SHADOW_WEB, 1), client.SkillFactory.getSkill(4111003), null, false), false,  15000);
            chr.changeSkillLevel(blind, (byte)blind.getMaxLevel(), blind.getMaxLevel(), -1);
            }
            }
            }*/
            /* if (chr.getQuest(MapleQuest.getInstance(29400)).getStatus().equals(MapleQuestStatus.Status.STARTED)) {
            if (chr.getLevel() >= 120 && monster.getStats().getLevel() >= 120) {
            //FIX MEDAL SHET
            } else if (monster.getStats().getLevel() >= chr.getLevel()) {
            
            }
            }*/
            if (chr.getPQ() != null) {
                if (monster.getId() == 8830000) {
                    if (chr.getPQ().getType() == server.pqs.PartyQuestType.BALROGPQ) {
                        server.pqs.BalrogPQ pq = (server.pqs.BalrogPQ) chr.getPQ();
                        pq.finish();
                    }
                }
                if (monster.getId() == 9420015) {
                    if (chr.getPQ().getType() == server.pqs.PartyQuestType.CHRISTMAS_PQ) {
                        server.pqs.ChristmasPQ pq = (server.pqs.ChristmasPQ) chr.getPQ();
                        pq.finish();
                    }
                }
                if (monster.getId() == 8840000) {
                    if (chr.getPQ().getType() == server.pqs.PartyQuestType.VON_LEON) {
                        server.pqs.VonLeon pq = (server.pqs.VonLeon) chr.getPQ();
                        pq.finish();
                    }
                }
                if (monster.getId() == 8800002) {
                    if (chr.getPQ().getType() == server.pqs.PartyQuestType.ZAKUM_FIGHT) {
                        server.pqs.ZakumFight pq = (server.pqs.ZakumFight) chr.getPQ();
                        pq.finish();
                    }
                } else if (monster.getId() == 9420549) {
                    if (chr.getPQ().getType() == server.pqs.PartyQuestType.SCARLION) {
                        server.pqs.ScarlionFight pq = (server.pqs.ScarlionFight) chr.getPQ();
                        pq.finish();
                    }
                }
            }
            chr.increaseEquipExp(monster.getExp());
            /* int buff = monster.getBuffToGive();
            if (buff > -1) {
            for (MapleMapObject mmo : this.getAllPlayer()) {
            MapleCharacter character = (MapleCharacter) mmo;
            if (character.isAlive()) {
            MapleStatEffect statEffect = mii.getItemEffect(buff);
            character.getClient().announce(MaplePacketCreator.showOwnBuffEffect(buff, 1));
            character.getMap().broadcastMessage(character, MaplePacketCreator.showBuffeffect(character.getId(), buff, 1), false);
            statEffect.applyTo(character);
            }
            }
            }*/
            //   monster.setHp(0);

            if (monster.getId() >= 8800003 && monster.getId() <= 8800010) {
                boolean makeZakReal = true;
                Collection<MapleMapObject> objects = getMapObjects();
                for (MapleMapObject object : objects) {
                    MapleMonster mons = getMonsterByOid(object.getObjectId());
                    if (mons != null) {
                        if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                            makeZakReal = false;
                            break;
                        }
                    }
                }
                if (makeZakReal) {
                    for (MapleMapObject object : objects) {
                        MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
                        if (mons != null) {
                            if (mons.getId() == 8800000) {
                                makeMonsterReal(mons);
                                updateMonsterController(mons);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public void killMonster(int monsId) {
        for (MapleMapObject mmo : getMapObjects()) {
            if (mmo instanceof MapleMonster) {
                if (((MapleMonster) mmo).getId() == monsId) {
                    this.killMonster((MapleMonster) mmo, (MapleCharacter) getAllPlayer().get(0), false);
                }
            }
        }
    }

    public void killAllMonsters() {
        for (MapleMapObject monstermo : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monstermo;
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
            removeMapObject(monster);
        }
    }

    public List<MapleMapObject> getAllPlayer() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
    }

    public void destroyReactor(int oid) {
        final MapleReactor reactor = getReactorByOid(oid);
        TimerManager tMan = TimerManager.getInstance();
        broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
        reactor.setAlive(false);
        removeMapObject(reactor);
        reactor.setTimerActive(false);
        if (reactor.getDelay() > 0) {
            tMan.schedule(new Runnable() {

                @Override
                public void run() {
                    respawnReactor(reactor);
                }
            }, reactor.getDelay());
        }
    }

    public void resetReactors() {
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setState((byte) 0);
                    ((MapleReactor) o).setTimerActive(false);
                    broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 0));
                }
            }
        }
    }

    public void shuffleReactors() {
        List<Point> points = new ArrayList<Point>();
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    points.add(((MapleReactor) o).getPosition());
                }
            }
            Collections.shuffle(points);
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setPosition(points.remove(points.size() - 1));
                }
            }
        }
    }

    public MapleReactor getReactorById(int Id) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) obj).getId() == Id) {
                        return (MapleReactor) obj;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Automagically finds a new controller for the given monster from the chars on the map...
     *
     * @param monster
     */
    public void updateMonsterController(MapleMonster monster) {
        synchronized (monster) {
            if (!monster.isAlive()) {
                return;
            }
            if (monster.getController() != null) {
                if (monster.getController().getMap() != this) {
                    monster.getController().stopControllingMonster(monster);
                } else {
                    return;
                }
            }
            int mincontrolled = -1;
            MapleCharacter newController = null;
            synchronized (characters) {
                for (MapleCharacter chr : characters) {
                    if (!chr.isHidden() && (chr.getControlledMonsters().size() < mincontrolled || mincontrolled == -1)) {
                        mincontrolled = chr.getControlledMonsters().size();
                        newController = chr;
                    }
                }
            }
            if (newController != null) {// was a new controller found? (if not no one is on the map)
                if (monster.isFirstAttack()) {
                    newController.controlMonster(monster, true);
                    monster.setControllerHasAggro(true);
                    monster.setControllerKnowsAboutAggro(true);
                } else {
                    newController.controlMonster(monster, false);
                }
            }
        }
    }

    public Collection<MapleMapObject> getMapObjects() {
        HashMap<Integer, MapleMapObject> mo = new HashMap<Integer, MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject mos : mapobjects.values()) {
                mo.put(mos.getObjectId(), mos);
            }
        }

        return mo.values();
    }

    public boolean containsNPC(int npcid) {
        if (npcid == 9000066) {
            return true;
        }
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.NPC) {
                    if (((MapleNPC) obj).getId() == npcid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public MapleMapObject getMapObject(MapleMapObjectType type){
        return mapobjects.get(MapleMapObjectType.NPC);
    }

    public MapleMapObject getMapObject(int oid) {
        return mapobjects.get(oid);
    }

    public MapleMapObject getMapObject(MapleMapObjectType type, int oid) {
        return mapobjects.get(oid);
    }

    /**
     * returns a monster with the given oid, if no such monster exists returns null
     *
     * @param oid
     * @return
     */
    public MapleMonster getMonsterByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid);
        if (mmo == null) {
            return null;
        }
        if (mmo.getType() == MapleMapObjectType.MONSTER) {
            return (MapleMonster) mmo;
        }
        return null;
    }

    public MapleReactor getReactorByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid);
        if (mmo == null) {
            return null;
        }
        return mmo.getType() == MapleMapObjectType.REACTOR ? (MapleReactor) mmo : null;
    }

    public MapleReactor getReactorByName(String name) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) obj).getName().equals(name)) {
                        return (MapleReactor) obj;
                    }
                }
            }
        }
        return null;
    }

    public void spawnMonsterOnGroudBelow(MapleMonster mob, Point pos) {
        spawnMonsterOnGroundBelow(mob, pos);
    }

    public void spawnMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y--;
        mob.setPosition(spos);
        spawnMonster(mob);
    }

    public void spawnCPQMonster(MapleMonster mob, Point pos, int team) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y--;
        mob.setPosition(spos);
        mob.setTeam(team);
        spawnMonster(mob);
    }

    private void monsterItemDrop(final MapleMonster m, final IItem item, long delay) {
        final ScheduledFuture<?> monsterItemDrop = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                if (MapleMap.this.getMonsterById(m.getId()) != null) {
                    if (item.getItemId() == 4001101) {
                        MapleMap.this.broadcastMessage(MaplePacketCreator.serverNotice(6, "The Moon Bunny made rice cake number " + (MapleMap.this.riceCakeNum + 1)));
                    }
                    spawnItemDrop(m, null, item, m.getPosition(), true, true);
                }
            }
        }, delay, delay);
        if (getMonsterById(m.getId()) == null) {
            monsterItemDrop.cancel(true);
        }
    }

    public void spawnFakeMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = getGroundBelow(pos);
        mob.setPosition(spos);
        spawnFakeMonster(mob);
    }

    public Point getGroundBelow(Point pos) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y--;
        return spos;
    }

    public void spawnRevives(final MapleMonster monster) {
        monster.setMap(this);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.spawnMonster(monster, false));
            }
        });
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnMonster(final MapleMonster monster) {
        monster.setMap(this);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.spawnMonster(monster, true));
            }
        }, null);
        updateMonsterController(monster);
        if (monster.getDropPeriodTime() > 0) { //9300102 - Watchhog, 9300061 - Moon Bunny (HPQ)
            if (monster.getId() == 9300102) {
                monsterItemDrop(monster, new Item(4031507, (byte) 0, (short) 1), monster.getDropPeriodTime());
            } else if (monster.getId() == 9300061) {
                monsterItemDrop(monster, new Item(4001101, (byte) 0, (short) 1), monster.getDropPeriodTime() / 3);
            } else {
                System.out.println("UNCODED TIMED MOB DETECTED: " + monster.getId());
            }
        }
        spawnedMonstersOnMap.incrementAndGet();
        if (monster.getStats().removeAfter() > 0) {
            TimerManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    killMonster(monster, (MapleCharacter) getAllPlayer().get(0), false);
                }
            }, monster.getStats().removeAfter() * 1000);
        }
        if (mapid == 910110000 && !this.allowHPQSummon) { // HPQ make monsters invisible
            this.broadcastMessage(MaplePacketCreator.makeMonsterInvisible(monster));
        }
    }

    public void spawnDojoMonster(final MapleMonster monster) {
        Point[] pts = {new Point(140, 0), new Point(190, 7), new Point(187, 7)};
        spawnMonsterWithEffect(monster, 15, pts[Randomizer.nextInt(3)]);
    }

    public void spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        monster.setMap(this);
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y--;
        monster.setPosition(spos);
        if (mapid < 925020000 || mapid > 925030000) {
            monster.disableDrops();
        }
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.spawnMonster(monster, true, effect));
            }
        });
        if (monster.hasBossHPBar()) {
            broadcastMessage(monster.makeBossHPBarPacket(), monster.getPosition());
        }
        updateMonsterController(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void spawnFakeMonster(final MapleMonster monster) {
        monster.setMap(this);
        monster.setFake(true);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.spawnFakeMonster(monster, 0));
            }
        });
        spawnedMonstersOnMap.incrementAndGet();
    }

    public void makeMonsterReal(final MapleMonster monster) {
        monster.setFake(false);
        broadcastMessage(MaplePacketCreator.makeMonsterReal(monster));
        updateMonsterController(monster);
    }

    public void spawnReactor(final MapleReactor reactor) {
        reactor.setMap(this);
        spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.announce(reactor.makeSpawnData());
            }
        });
    }

    private void respawnReactor(final MapleReactor reactor) {
        reactor.setState((byte) 0);
        reactor.setAlive(true);
        spawnReactor(reactor);
    }

    public void spawnDoor(final MapleDoor door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {

            public void sendPackets(MapleClient c) {
                c.announce(MaplePacketCreator.spawnDoor(door.getOwner().getId(), door.getTargetPosition(), false));
                if (door.getOwner().getParty() != null && (door.getOwner() == c.getPlayer() || door.getOwner().getParty().containsMembers(c.getPlayer().getMPC()))) {
                    c.announce(MaplePacketCreator.partyPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
                }
                c.announce(MaplePacketCreator.spawnPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
                c.announce(MaplePacketCreator.enableActions());
            }
        }, new SpawnCondition() {

            public boolean canSpawn(MapleCharacter chr) {
                return chr.getMapId() == door.getTarget().getId() || chr == door.getOwner() && chr.getParty() == null;
            }
        });
    }

    public List<MapleCharacter> getPlayersInRange(Rectangle box, List<MapleCharacter> chr) {
        List<MapleCharacter> character = new LinkedList<MapleCharacter>();
        synchronized (characters) {
            for (MapleCharacter a : characters) {
                if (chr.contains(a.getClient().getPlayer())) {
                    if (box.contains(a.getPosition())) {
                        character.add(a);
                    }
                }
            }
            return character;
        }
    }

    public void spawnSummon(final MapleSummon summon) {
        spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                //   System.out.println("summon checking null maybe.");
                if (summon != null) {
                    //       System.out.println("Summon spawned..");
                    c.getSession().write(MaplePacketCreator.spawnSummon(summon, true));
                } else {
                    //       System.out.println("sorry, summon is null.");
                }
            }
        }, null);
    }

    public List<MapleMonster> getMapMonstersInRange(Point from, double rangeSq, MapleMapObjectType type) {
        synchronized (mapobjects) {
            List<MapleMonster> ret = new LinkedList<MapleMonster>();
            for (MapleMapObject l : mapobjects.values()) {
                if (from.distanceSq(l.getPosition()) <= rangeSq && l.getType() == type) {
                    ret.add((MapleMonster) l);
                }
            }

            return ret;
        }
    }

    public void spawnMist(final MapleMist mist, final int duration, boolean poison, boolean fake) {
        addMapObject(mist);
        broadcastMessage(fake ? mist.makeFakeSpawnData(30) : mist.makeSpawnData());
        TimerManager tMan = TimerManager.getInstance();
        final ScheduledFuture<?> poisonSchedule;
        if (poison) {
            Runnable poisonTask = new Runnable() {

                @Override
                public void run() {
                    List<MapleMapObject> affectedMonsters = getMapObjectsInBox(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER));
                    for (MapleMapObject mo : affectedMonsters) {
                        if (mist.makeChanceResult()) {
                            MonsterStatusEffect poisonEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), mist.getSourceSkill(), null, false);
                            ((MapleMonster) mo).applyStatus(mist.getOwner(), poisonEffect, true, duration);
                        }
                    }
                }
            };
            poisonSchedule = tMan.register(poisonTask, 2000, 2500);
        } else {
            poisonSchedule = null;
        }
        tMan.schedule(new Runnable() {

            @Override
            public void run() {
                removeMapObject(mist);
                if (poisonSchedule != null) {
                    poisonSchedule.cancel(false);
                }
                broadcastMessage(mist.makeDestroyData());
            }
        }, duration);
    }

    public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, Point pos, final boolean ffaDrop, final boolean playerDrop) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 0, playerDrop);

        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 1));
            }
        }, null);
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 0));

        if (!everlast) {
            TimerManager.getInstance().schedule(new ExpireMapItemJob(drop), 180000);
            activateItemReactors(drop, owner.getClient());
        }
    }

    private void activateItemReactors(final MapleMapItem drop, final MapleClient c) {
        final IItem item = drop.getItem();

        for (final MapleMapObject o : getAllReactor()) {
            final MapleReactor react = (MapleReactor) o;

            if (react.getReactorType() == 100) {
                if (react.getReactItem().getLeft() == item.getItemId() && react.getReactItem().getRight() == item.getQuantity()) {

                    if (react.getArea().contains(drop.getPosition())) {
                        if (!react.isTimerActive()) {
                            TimerManager.getInstance().schedule(new ActivateItemReactor(drop, react, c), 5000);
                            react.setTimerActive(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    public final List<MapleMapObject> getAllReactor() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, 30000);
    }

    public void startMapEffect(String msg, int itemId, long time) {
        if (mapEffect != null) {
            return;
        }
        mapEffect = new MapleMapEffect(msg, itemId);
        broadcastMessage(mapEffect.makeStartData());
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                broadcastMessage(mapEffect.makeDestroyData());
                mapEffect = null;
            }
        }, time);
    }

    public void addPlayer(final MapleCharacter chr) {
        synchronized (characters) {
            this.characters.add(chr);
        }
        if (characters.size() > 1 && chr.isMWLB()) {
            String text = "Players on map : ";
            for (MapleCharacter players : getCharacters()) {
                if (players.getId() != chr.getId()) {
                    text += "'" + players.getName() + "',";
                }

            }
            chr.yellowMessage(text);
        }
        if (!this.spawnStarter) {
            try {
                //   initializeSpawn();
            } finally {
                //   spawnStarter = true;
            }
        }
        synchronized (this.mapobjects) {
            this.mapobjects.put(Integer.valueOf(chr.getObjectId()), chr);
        }
        if (onFirstUserEnter.length() != 0 && !chr.hasEntered(onFirstUserEnter, mapid) && MapScriptManager.getInstance().scriptExists(onFirstUserEnter, true)) {
            if (getAllPlayer().size() <= 1) {
                chr.enteredScript(onFirstUserEnter, mapid);
                MapScriptManager.getInstance().getMapScript(chr.getClient(), onFirstUserEnter, true);
            }
        }
        if (onUserEnter.length() != 0) {
            if (onUserEnter.equals("cygnusTest") && (mapid < 913040000 || mapid > 913040006)) {
                chr.saveLocation("INTRO");
            }
            MapScriptManager.getInstance().getMapScript(chr.getClient(), onUserEnter, false);
        }
        if (FieldLimit.CANNOTUSEMOUNTS.check(fieldLimit) && chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            chr.cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
        }
        if (mapid == 923010000 && getMonsterById(9300102) == null) { // Kenta's Mount Quest
            spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300102), new Point(77, 426));
        } else if (mapid == 910110000) { // Henesys Party Quest
            chr.getClient().announce(MaplePacketCreator.getClock(15 * 60));
            TimerManager.getInstance().register(new Runnable() {

                @Override
                public void run() {
                    if (mapid == 910110000) {
                        chr.getClient().getPlayer().changeMap(chr.getClient().getChannelServer().getMapFactory().getMap(925020000));
                    }
                }
            }, 15 * 60 * 1000 + 3000);
        }
        String name = "You are in " + getMapName();
        //Announces map name on screen...
        //chr.announce(MaplePacketCreator.MapMessage(name));
        MaplePet[] pets = chr.getPets();
        for (int i = 0; i < chr.getPets().length; i++) {
            if (pets[i] != null) {
                pets[i].setPos(getGroundBelow(chr.getPosition()));
                chr.announce(MaplePacketCreator.showPet(chr, pets[i], false, false));
            } else {
                break;
            }
        }
        /*      if(!chr.isHidden()){
        for(final MaplePokemon p : chr.getAllPokemon()){
        spawnAndAddRangedMapObject(p, new DelayedPacketCreation() {
        
        @Override
        public void sendPackets(MapleClient c) {
        c.announce(MaplePacketCreator.spawnMonster(p, true));
        }
        }, null);
        }
        }*/
        if (chr.isHidden() || chr.isMWLB()) {
            broadcastGMMessage(chr, MaplePacketCreator.spawnPlayerMapobject(chr), false);
            // chr.announce(MaplePacketCreator.getGMEffect(0x10, (byte) 1));
        } else {
            broadcastMessage(chr, MaplePacketCreator.spawnPlayerMapobject(chr));
        }
        if (chr.getEvanDragon() != null && chr.isEvan()) {
            this.addMapObject(chr.getEvanDragon());
            chr.getEvanDragon().sendSpawnData(chr.getClient());
            // broadcastMessage(chr, MaplePacketCreator.spawnDragon(chr.getEvanDragon()));
        }
        
        //Calendar Events
        //chr.announce(MaplePacketCreator.startMapEffect("", ChannelServer.getInstance(channel).getCalendarManager().retrieveMapEffectID(), true));
        //sendObjectPlacement(chr.getClient());
        if (isStartingEventMap() && !eventStarted()) {
            chr.getMap().getPortal("join00").setPortalStatus(false);
        }
        if (hasForcedEquip()) {
            chr.getClient().announce(MaplePacketCreator.showForcedEquip(-1));
        }
        if (specialEquip()) {
            chr.getClient().announce(MaplePacketCreator.coconutScore(0, 0));
            chr.getClient().announce(MaplePacketCreator.showForcedEquip(chr.getTeam()));
        }
        // if (chr.getPlayerShop() != null) {
        //     addMapObject(chr.getPlayerShop());
        // }
        MapleStatEffect summonStat = chr.getStatForBuff(MapleBuffStat.SUMMON);
        if (summonStat != null) {
            MapleSummon summon = chr.getSummons().get(summonStat.getSourceId());
            if (summon != null) {
                summon.setPosition(chr.getPosition());
                chr.getMap().spawnSummon(summon);
                updateMapObjectVisibility(chr, summon);
            }
        }
        if (mapEffect != null) {
            mapEffect.sendStartData(chr.getClient());
        }

        if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted()) {
            chr.getClient().announce(MaplePacketCreator.getClock((int) (chr.getEventInstance().getTimeLeft() / 1000)));
        }
        /*    if (chr.getFitness() != null && chr.getFitness().isTimerStarted()) 
        chr.getClient().announce(MaplePacketCreator.getClock((int) (chr.getFitness().getTimeLeft() / 1000)));
        
        if (chr.getOla() != null && chr.getOla().isTimerStarted()) 
        chr.getClient().announce(MaplePacketCreator.getClock((int) (chr.getOla().getTimeLeft() / 1000)));
        
        if (mapid == 109060000)
        chr.announce(MaplePacketCreator.rollSnowBall(true, 0, null, null));
         *//*
        MonsterCarnival carnival = chr.getCarnival();
        MonsterCarnivalParty cparty = chr.getCarnivalParty();
        if (carnival != null && cparty != null && (mapid == 980000101 || mapid == 980000201 || mapid == 980000301 || mapid == 980000401 || mapid == 980000501 || mapid == 980000601)) {
        chr.getClient().announce(MaplePacketCreator.getClock((int) (carnival.getTimeLeft() / 1000)));
        chr.getClient().announce(MaplePacketCreator.startCPQ(chr, carnival.oppositeTeam(cparty)));
        }*/
        if (hasClock()) {
            Calendar cal = Calendar.getInstance();
            chr.getClient().announce((MaplePacketCreator.getClockTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))));
        }
        // if (hasBoat() == 2) {
        //     chr.getClient().announce((MaplePacketCreator.boatPacket(true)));
        // } else if (hasBoat() == 1 && (chr.getMapId() != 200090000 || chr.getMapId() != 200090010)) {
        ////     chr.getClient().announce(MaplePacketCreator.boatPacket(false));
        // }
        chr.receivePartyMemberHP();
    }

    public MaplePortal findClosestPortal(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (distance < shortestDistance) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public MaplePortal getRandomSpawnpoint() {
        List<MaplePortal> spawnPoints = new ArrayList<MaplePortal>();
        for (MaplePortal portal : portals.values()) {
            if (portal.getType() >= 0 && portal.getType() <= 2) {
                spawnPoints.add(portal);
            }
        }
        MaplePortal portal = spawnPoints.get(new Random().nextInt(spawnPoints.size()));
        return portal != null ? portal : getPortal(0);
    }

    public void removePlayer(MapleCharacter chr) {
        synchronized (characters) {
            characters.remove(chr);
        }
        removeMapObject(Integer.valueOf(chr.getObjectId()));
        if (chr.getEvanDragon() != null) {
            removeMapObject(chr.getEvanDragon());
            broadcastMessage(MaplePacketCreator.removeDragon(chr.getId()));
        }/* for(MaplePokemon p : chr.getAllPokemon()){
        synchronized(mapobjects){
        if(mapobjects.containsKey(p.getObjectId())){
        mapobjects.remove(p.getObjectId());
        }
        }
        p.sendDestroyData(chr.getClient());
        }*/
        if (!chr.isHidden()) {
            broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        } else {
            broadcastGMMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        }

        for (MapleMonster monster : chr.getControlledMonsters()) {
            monster.setController(null);
            monster.setControllerHasAggro(false);
            monster.setControllerKnowsAboutAggro(false);
            updateMonsterController(monster);
        }
        chr.leaveMap();
        chr.cancelMapTimeLimitTask();
        for (MapleSummon summon : chr.getSummons().values()) {
            if (summon.isStationary()) {
                chr.cancelBuffStats(MapleBuffStat.PUPPET);
            } else {
                removeMapObject(summon);
            }
        }
    }

    public void broadcastMessage(MaplePacket packet) {
        broadcastMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    public void broadcastGMMessage(MaplePacket packet) {
        broadcastGMMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Nonranged. Repeat to source according to parameter.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    /**
     * Ranged and repeat according to parameters.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     * @param ranged
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource, boolean ranged) {
        broadcastMessage(repeatToSource ? null : source, packet, 722500, source.getPosition());
    }

    /**
     * Always ranged from Point.
     *
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(MaplePacket packet, Point rangedFrom) {
        broadcastMessage(null, packet, 722500, rangedFrom);
    }

    /**
     * Always ranged from point. Does not repeat to source.
     *
     * @param source
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, Point rangedFrom) {
        broadcastMessage(source, packet, 722500, rangedFrom);
    }

    private void broadcastMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().announce(packet);
                        }
                    } else {
                        chr.getClient().announce(packet);
                    }
                }
            }
        }
    }

    private boolean isNonRangedType(MapleMapObjectType type) {
        switch (type) {
            case NPC:
            case HIRED_MERCHANT:
            case PLAYER_NPC:
            case MIST:
                return true;
        }
        return false;
    }

    private void sendObjectPlacement(MapleClient mapleClient) {
        final MapleCharacter chr = mapleClient.getPlayer();
        synchronized (characters) {
            for (MapleCharacter p : characters) {
                if (p != chr) {
                    if (p.isHidden()) {
                        if (chr.isGM() || chr.isMWLB()) {
                            chr.announce(MaplePacketCreator.spawnPlayerMapobject(p));
                        }
                    } else {
                        chr.announce(MaplePacketCreator.spawnPlayerMapobject(p));
                    }
                }
            }
        }
        for (MapleMapObject o : getMapObjects()) {
            if (o.getType() == MapleMapObjectType.SUMMON) {
                MapleSummon summon = (MapleSummon) o;
                if (summon.getOwner() == chr) {
                    if (chr.getSummons().isEmpty() || !chr.getSummons().containsValue(summon)) {
                        mapobjects.remove(o.getObjectId());
                        continue;
                    }
                }
            }
            if (isNonRangedType(o.getType())) {
                o.sendSpawnData(mapleClient);
            } else if (o.getType() == MapleMapObjectType.MONSTER) {
                updateMonsterController((MapleMonster) o);
            }
        }
        if (chr != null) {
            for (MapleMapObject o : getMapObjectsInRange(chr.getPosition(), 722500, rangedMapobjectTypes)) {
                if (o != null) {
                    if (o.getType() == MapleMapObjectType.REACTOR) {
                        if (((MapleReactor) o).isAlive()) {
                            o.sendSpawnData(chr.getClient());
                            if (o != null) {
                                chr.addVisibleMapObject(o);
                            }
                        }
                    } else {
                        o.sendSpawnData(chr.getClient());
                        chr.addVisibleMapObject(o);
                    }
                }
            }
        }
    }

    public List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if ((l.getPosition() == null)) {
                        mapobjects.remove(l.getObjectId());
                        l = null;
                        continue;
                    }
                    if (from.distanceSq(l.getPosition()) <= rangeSq) {//todo: bug (NPE) here; fixme
                        ret.add(l);
                    }
                }
            }
        }
        return ret;
    }

    public List<MapleMapObject> getMapObjectsInBox(Rectangle box, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (box.contains(l.getPosition())) {
                        ret.add(l);
                    }
                }
            }
        }
        return ret;
    }

    public void addPortal(MaplePortal myPortal) {
        portals.put(myPortal.getId(), myPortal);
    }

    public MaplePortal getPortal(String portalname) {
        for (MaplePortal port : portals.values()) {
            if (port.getName().equals(portalname)) {
                return port;
            }
        }
        return null;
    }

    public MaplePortal getPortal(int portalid) {
        return portals.get(portalid);
    }

    public void addMapleArea(Rectangle rec) {
        areas.add(rec);
    }

    public List<Rectangle> getAreas() {
        return new ArrayList<Rectangle>(areas);
    }

    public Rectangle getArea(int index) {
        return areas.get(index);
    }

    public void setFootholds(MapleFootholdTree footholds) {
        this.footholds = footholds;
    }

    public MapleFootholdTree getFootholds() {
        return footholds;
    }

    /**
     * not threadsafe, please synchronize yourself
     *
     * @param monster
     * @param mobTime
     */
    public void addMonsterSpawn(int monster, Point pos, int mobTime, int team, short fh, short rx0, short rx1, short cy, short f) {
        Point newpos = calcPointBelow(pos);
        if (newpos == null) {
            return;//nothing
        }
        newpos.y -= 1;
        SpawnPoint sp = new SpawnPoint(monster, newpos, mobTime, team, fh, rx0, rx1, cy, f);
        monsterSpawn.add(sp);
        if (sp.shouldSpawn() || mobTime == -1) {// -1 does not respawn and should not either but force ONE spawn
            sp.spawnMonster(this);
        }
    }

    public float getMonsterRate() {
        return monsterRate;
    }

    public Collection<MapleCharacter> getCharacters() {
        return Collections.synchronizedCollection(characters);
    }
    
     public static MapleMapObjectType getNPCs() {
        return MapleMapObjectType.NPC;
    }

    public MapleCharacter getCharacterById(int id) {
        for (MapleCharacter c : this.getCharacters()) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    private void updateMapObjectVisibility(MapleCharacter chr, MapleMapObject mo) {
        if (!chr.isMapObjectVisible(mo)) { // monster entered view range
            if (mo.getType() == MapleMapObjectType.SUMMON || mo.getPosition().distanceSq(chr.getPosition()) <= 900000 * 2) {
                chr.addVisibleMapObject(mo);
                mo.sendSpawnData(chr.getClient());
            }
        } else if (mo.getType() != MapleMapObjectType.SUMMON && mo.getPosition().distanceSq(chr.getPosition()) > 900000 * 2) {
            chr.removeVisibleMapObject(mo);
            mo.sendDestroyData(chr.getClient());
        }
    }

    public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                updateMapObjectVisibility(chr, monster);
            }
        }
    }

    public void moveDragon(client.MapleDragon dragon, Point reportedPos) {
        dragon.setPosition(reportedPos);
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                updateMapObjectVisibility(chr, dragon);
            }
        }
    }

    public void movePlayer(MapleCharacter player, Point newPosition) {
        player.setPosition(newPosition);
        Collection<MapleMapObject> visibleObjects = player.getVisibleMapObjects();
        MapleMapObject[] visibleObjectsNow = visibleObjects.toArray(new MapleMapObject[visibleObjects.size()]);
        try {
            for (MapleMapObject mo : visibleObjectsNow) {
                synchronized (mapobjects) {
                    if (mo != null) {
                        if (mapobjects.get(mo.getObjectId()) == mo) {
                            updateMapObjectVisibility(player, mo);
                        } else {
                            player.removeVisibleMapObject(mo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (MapleMapObject mo : getMapObjectsInRange(player.getPosition(), 722500, rangedMapobjectTypes)) {
            if (!player.isMapObjectVisible(mo)) {
                mo.sendSpawnData(player.getClient());
                player.addVisibleMapObject(mo);
            }
        }
    }

    public MaplePortal findClosestSpawnpoint(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (portal.getType() >= 0 && portal.getType() <= 2 && distance < shortestDistance && portal.getTargetMapId() == 999999999) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection(portals.values());
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setClock(boolean hasClock) {
        this.clock = hasClock;
    }

    public boolean hasClock() {
        return clock;
    }

    public void setTown(boolean isTown) {
        this.town = isTown;
    }

    public boolean isTown() {
        return town;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setEverlast(boolean everlast) {
        this.everlast = everlast;
    }

    public boolean getEverlast() {
        return everlast;
    }

    public int getSpawnedMonstersOnMap() {
        return spawnedMonstersOnMap.get();
    }

    private class ExpireMapItemJob implements Runnable {

        private MapleMapItem mapitem;

        public ExpireMapItemJob(MapleMapItem mapitem) {
            this.mapitem = mapitem;
        }

        @Override
        public void run() {
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                synchronized (mapitem) {
                    if (mapitem.isPickedUp()) {
                        return;
                    }
                    MapleMap.this.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0), mapitem.getPosition());
                    MapleMap.this.removeMapObject(mapitem);
                    mapitem.setPickedUp(true);
                }
            }
        }
    }

    private class ActivateItemReactor implements Runnable {

        private MapleMapItem mapitem;
        private MapleReactor reactor;
        private MapleClient c;

        public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
            this.mapitem = mapitem;
            this.reactor = reactor;
            this.c = c;
        }

        @Override
        public void run() {
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                synchronized (mapitem) {
                    TimerManager tMan = TimerManager.getInstance();
                    if (mapitem.isPickedUp()) {
                        return;
                    }
                    MapleMap.this.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0), mapitem.getPosition());
                    synchronized (mapobjects) {
                        MapleMap.this.mapobjects.remove(mapitem.getObjectId());
                    }
                    reactor.hitReactor(c);
                    reactor.setTimerActive(false);
                    if (reactor.getDelay() > 0) {
                        tMan.schedule(new Runnable() {

                            @Override
                            public void run() {
                                reactor.setState((byte) 0);
                                broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
                            }
                        }, reactor.getDelay());
                    }
                }
            }
        }
    }

    public void respawn() {
        if (characters.isEmpty()) {
            return;
        }

        int numShouldSpawn = (monsterSpawn.size() - spawnedMonstersOnMap.get()) * Math.round(monsterRate);
        if (numShouldSpawn > 0) {
            List<SpawnPoint> randomSpawn = new ArrayList<SpawnPoint>(monsterSpawn);
            Collections.shuffle(randomSpawn);
            int spawned = 0;
            for (SpawnPoint spawnPoint : randomSpawn) {
                if (spawnPoint.shouldSpawn()) {
                    spawnPoint.spawnMonster(MapleMap.this);
                    spawned++;
                }
                if (spawned >= numShouldSpawn) {
                    break;
                }
            }
        }
    }

    private static interface DelayedPacketCreation {

        void sendPackets(MapleClient c);
    }

    private static interface SpawnCondition {

        boolean canSpawn(MapleCharacter chr);
    }

    public int getHPDec() {
        return decHP;
    }

    public void setHPDec(int delta) {
        decHP = delta;
    }

    public int getHPDecProtect() {
        return protectItem;
    }

    public void setHPDecProtect(int delta) {
        this.protectItem = delta;
    }

    private int hasBoat() {
        return docked ? 2 : (boat ? 1 : 0);
    }

    public void setBoat(boolean hasBoat) {
        this.boat = hasBoat;
    }

    public void setDocked(boolean isDocked) {
        this.docked = isDocked;
    }

    public void broadcastGMMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        broadcastGMMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    private void broadcastGMMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && chr.isGM()) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().announce(packet);
                        }
                    } else {
                        chr.getClient().announce(packet);
                    }
                }
            }
        }
    }

    public void broadcastNONGMMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isGM()) {
                    chr.getClient().announce(packet);
                }
            }
        }
    }

    public void broadcastChatText(MapleCharacter source, byte remainingBytes, String msg, boolean gmsonly) {
        MaplePacket packet = MaplePacketCreator.getChatText(source.getId(), msg, source.isGM() ? source.getGMChat() : false, remainingBytes);
        synchronized (characters) {
            for (MapleCharacter players : characters) {
                if (gmsonly) {
                    if (players.gmLevel() > 1) {
                        players.getClient().announce(packet);
                    }
                } else {
                    players.getClient().announce(packet);
                }
            }
        }
    }

    public MapleOxQuiz getOx() {
        return ox;
    }

    public void setOx(MapleOxQuiz set) {
        this.ox = set;
    }

    public void setOxQuiz(boolean b) {
        this.isOxQuiz = b;
    }

    public boolean isOxQuiz() {
        return isOxQuiz;
    }

    public void setOnUserEnter(String onUserEnter) {
        this.onUserEnter = onUserEnter;
    }

    public String getOnUserEnter() {
        return onUserEnter;
    }

    public void setOnFirstUserEnter(String onFirstUserEnter) {
        this.onFirstUserEnter = onFirstUserEnter;
    }

    public String getOnFirstUserEnter() {
        return onFirstUserEnter;
    }

    private boolean hasForcedEquip() {
        return fieldType == 81 || fieldType == 82;
    }

    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    public void clearDrops(MapleCharacter player) {
        List<MapleMapObject> items = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
        for (MapleMapObject i : items) {
            player.getMap().removeMapObject(i);
            player.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, player.getId()));
        }
    }

    public void clearDrops() {
        for (MapleMapObject i : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM))) {
            removeMapObject(i);
        }
    }

    public void addMapTimer(int time) {
        timeLimit = System.currentTimeMillis() + (time * 1000);
        broadcastMessage(MaplePacketCreator.getClock(time));
        mapMonitor = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                if (timeLimit != 0 && timeLimit < System.currentTimeMillis()) {
                    warpEveryone(getForcedReturnId());
                }
                if (getCharacters().isEmpty()) {
                    resetReactors();
                    killAllMonsters();
                    clearDrops();
                    timeLimit = 0;
                    if (mapid >= 922240100 && mapid <= 922240119) {
                        toggleHiddenNPC(9001108);
                    }
                    mapMonitor.cancel(true);
                    mapMonitor = null;
                }
            }
        }, 1000);
    }

    public void setFieldLimit(int fieldLimit) {
        this.fieldLimit = fieldLimit;
    }

    public int getFieldLimit() {
        return fieldLimit;
    }

    public void resetRiceCakes() {
        this.riceCakeNum = 0;
    }

    public void setAllowHPQSummon(boolean b) {
        this.allowHPQSummon = b;
    }

    /*public void showAllMonsters() {
    }*/
    public void warpEveryone(int to) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                chr.changeMap(to);
            }
        }
    }

    // BEGIN EVENTS
    public void setSnowball(int team, MapleSnowball ball) {
        switch (team) {
            case 0:
                this.snowball0 = ball;
                break;
            case 1:
                this.snowball1 = ball;
                break;
            default:
                break;
        }
    }

    public MapleSnowball getSnowball(int team) {
        switch (team) {
            case 0:
                return snowball0;
            case 1:
                return snowball1;
            default:
                return null;
        }
    }

    private boolean specialEquip() {//Maybe I shouldn't use fieldType :\
        return fieldType == 4 || fieldType == 19;
    }

    public void setCoconut(MapleCoconut nut) {
        this.coconut = nut;
    }

    public MapleCoconut getCoconut() {
        return coconut;
    }

    public void warpOutByTeam(int team, int mapid) {
        for (MapleCharacter chr : getCharacters()) {
            if (chr != null) {
                if (chr.getTeam() == team) {
                    chr.changeMap(mapid);
                }
            }
        }
    }

    public void startEvent(final MapleCharacter chr) {
        if (this.mapid == 109080000) {
            setCoconut(new MapleCoconut(this));
            coconut.startEvent();

        } else if (this.mapid == 109040000) {
            chr.setFitness(new MapleFitness(chr));
            chr.getFitness().startFitness();

        } else if (this.mapid == 109030001 || this.mapid == 109030101) {
            chr.setOla(new MapleOla(chr));
            chr.getOla().startOla();

        } else if (this.mapid == 109020001 && getOx() == null) {
            setOx(new MapleOxQuiz(this));
            getOx().sendQuestion();
            setOxQuiz(true);

        } else if (this.mapid == 109060000 && getSnowball(chr.getTeam()) == null) {
            setSnowball(0, new MapleSnowball(0, this));
            setSnowball(1, new MapleSnowball(1, this));
            getSnowball(chr.getTeam()).startEvent();
        }
    }

    public boolean eventStarted() {
        return eventstarted;
    }

    public void startEvent() {
        this.eventstarted = true;
    }

    public void setEventStarted(boolean event) {
        this.eventstarted = event;
    }

    public String getEventNPC() {
        StringBuilder sb = new StringBuilder();
        sb.append("Talk to ");
        if (mapid == 60000) {
            sb.append("Paul!");
        } else if (mapid == 104000000) {
            sb.append("Jean!");
        } else if (mapid == 200000000) {
            sb.append("Martin!");
        } else if (mapid == 220000000) {
            sb.append("Tony!");
        } else {
            return null;
        }
        return sb.toString();
    }

    public boolean hasEventNPC() {
        return this.mapid == 60000 || this.mapid == 104000000 || this.mapid == 200000000 || this.mapid == 220000000;
    }

    public boolean isStartingEventMap() {
        return this.mapid == 109040000 || this.mapid == 109020001 || this.mapid == 109010000 || this.mapid == 109030001 || this.mapid == 109030101;
    }

    public boolean isEventMap() {
        return this.mapid >= 109010000 && this.mapid < 109050000 || this.mapid > 109050001 && this.mapid <= 109090000;
    }

    public void timeMob(int id, String msg) {
        timeMob = new Pair<Integer, String>(id, msg);
    }

    public Pair<Integer, String> getTimeMob() {
        return timeMob;
    }

    public void toggleHiddenNPC(int id) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.NPC) {
                    MapleNPC npc = (MapleNPC) obj;
                    if (npc.getId() == id) {
                        npc.setHide(!npc.isHidden());
                        if (!npc.isHidden()) //Should only be hidden upon changing maps
                        {
                            broadcastMessage(MaplePacketCreator.spawnNPC(npc));
                        }
                    }
                }
            }
        }
    }
}