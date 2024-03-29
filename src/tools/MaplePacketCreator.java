
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
package tools;

import java.awt.Point;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import constants.SkillConstants.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import client.BuddylistEntry;
import client.IEquip;
import client.IEquip.ScrollResult;
import client.IItem;
import client.ISkill;
import client.Item;
import client.ItemFactory;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.MapleFamilyEntry;
import net.channel.handler.AbstractDealDamageHandler.AttackInfo;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleKeyBinding;
import client.MapleMount;
import client.MaplePet;
import client.MapleQuestStatus;
import client.MapleRing;
import client.MapleStat;
import client.SkillMacro;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.ItemConstants;
import constants.ServerConstants;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import net.LongValueHolder;
import net.MaplePacket;
import net.SendOpcode;
import net.channel.handler.PlayerInteractionHandler;
import net.channel.handler.SummonDamageHandler.SummonAttackEntry;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import client.MapleDragon;
import client.MapleJob;
import net.channel.ChannelServer;
import net.world.PartyOperation;
import net.world.PlayerCoolDownValueHolder;
import net.world.guild.MapleAlliance;
import net.world.guild.MapleGuild;
import net.world.guild.MapleGuildCharacter;
import net.world.guild.MapleGuildSummary;
import server.CashShop;
import server.CashShop.CashItem;
import server.DueyPackages;
import server.MTSItemInfo;
import server.MapleItemInformationProvider;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.MaplePlayerShopItem;
import server.MapleShopItem;
import server.MapleTrade;
import server.events.MapleSnowball;
import server.events.MonsterCarnivalParty;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.MobSkill;
import server.maps.HiredMerchant;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMist;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import server.maps.PlayerNPCs;
import server.movement.LifeMovementFragment;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author Frz
 */
public class MaplePacketCreator {

    private final static byte[] CHAR_INFO_MAGIC = new byte[]{(byte) 0xff, (byte) 0xc9, (byte) 0x9a, 0x3b};
    public static final List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();
    private final static byte[] ITEM_MAGIC = new byte[]{(byte) 0x80, 0x05};
    private final static int ITEM_YEAR2000 = -1085019342;
    private final static long REAL_YEAR2000 = 946681229830L;
    private final static byte[] WARP_PACKET_MAGIC = HexTool.getByteArrayFromHexString("02 00 01 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00");
    private final static byte[] NON_EXPIRE = {0x00, (byte) 0x80, 0x05, (byte) 0xBB, 0x46, (byte) 0xE6, 0x17, 0x02};
    private final static byte[] NON_EXPIRE2 = {(byte) 0xBB, 0x46, (byte) 0xE6, 0x17, 0x02};

    public static int getItemTimestamp(long realTimestamp) {
        int time = (int) ((realTimestamp - REAL_YEAR2000) / 1000 / 60); // convert to minutes
        return (int) (time * 35.762787) + ITEM_YEAR2000;
    }

    private static int getQuestTimestamp(long realTimestamp) {
        return (int) (((int) (realTimestamp / 1000 / 60)) * 0.1396987) + 27111908;
    }

    private static long getKoreanTimestamp(long realTimestamp) {
        return realTimestamp * 10000 + 116444592000000000L;
    }

    private static long getTime(long realTimestamp) {
        return realTimestamp * 10000 + 116444592000000000L;
    }
    /* explorer
     * [19 78 00 00] [44 61 79 75 6D 73 75 6E 00 00 00 00 00] [00] [01] [21 4E 00 00] [6D 76 00 00] [00 00 00 00 00 00 00 00 ]
     * [00 00 00 00 00 00 00 00] [00 00 00 00 00 00 00 00] [01] [00 00] [0C 00] [05 00] [04 00] [04 00] [32 00 00 00] [32 00 00 00] [05 00 00 00] 
     * [05 00 00 00] [00 00] [00 00] [00 00 00 00] [00 00] [00 00 00 00] [10 27 00 00] [02] [00 00 00 00] 01 00 00 01 21 4E 00 00 01 6D 76 00 
     * 00 05 82 DE 0F 00 06 2D 2D 10 00 07 81 5B 10 00 0B 15 2C 14 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
     * 00 01 01 00 00 00 00 00 00 00 D5 00 00 00 2C FF FF FF 01 05 00 00 00 00 00 00 00
     */

    private static void addCharStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getId()); // character id
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getName(), '\0', 13));
        mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
        mplew.write(chr.getSkinColor().getId()); // skin color
        mplew.writeInt(chr.getFace()); // face
        mplew.writeInt(chr.getHair()); // hair

        for (int i = 0; i < 3; i++) {
            if (chr.getPet(i) != null) //Checked GMS.. and your pets stay when going into the cash shop.
            {
                mplew.writeLong(chr.getPet(i).getUniqueId());
            } else {
                mplew.writeLong(0);
            }
        }

        if (!chr.isMWLB()) {
            mplew.write(chr.getLevel()); // level
            mplew.writeShort(chr.getJob().getId()); // job
            mplew.writeShort(chr.getStr()); // str
            mplew.writeShort(chr.getDex()); // dex
            mplew.writeShort(chr.getInt()); // int
            mplew.writeShort(chr.getLuk()); // luk
            mplew.writeInt(chr.getHp()); // hp (?)
            mplew.writeInt(chr.getMaxHp()); // maxhp
            mplew.writeInt(chr.getMp()); // mp (?)
            mplew.writeInt(chr.getMaxMp()); // maxmp
        } else {
            mplew.write(200); // level
            mplew.writeShort(800); // job
            mplew.writeShort(30000); // str
            mplew.writeShort(30000); // dex
            mplew.writeShort(30000); // int
            mplew.writeShort(30000); // luk
            mplew.writeInt(chr.getHp()); // hp (?)
            mplew.writeInt(99999); // maxhp
            mplew.writeInt(chr.getMp()); // mp (?)
            mplew.writeInt(99999); // maxmp
        }
        mplew.writeShort(chr.getRemainingAp()); // remaining ap
        if (chr.isExtendedSPType()) {
            if (chr.getSPTable() == null) {
                mplew.write(0); // 0 sp
            } else {
                // size gets written..
                chr.getSPTable().addSPData(mplew);
            }
        } else {
            mplew.writeShort(chr.getRemainingSp()); // remaining sp
        }
        mplew.writeInt(chr.getExp()); // current exp
        mplew.writeShort(chr.getFame()); // fame
        mplew.writeInt(chr.getGachaExp()); //Gacha Exp
        mplew.writeInt(chr.getMapId()); // current map id
        mplew.write(chr.getInitialSpawnpoint()); // spawnpoint
        mplew.writeInt(0);
        mplew.writeShort(chr.isDualBlade() ? 1 : 0); // is dual blade?
    }
    /* explorer
     *  [01] [00] 00 01 [21 4E 00 00] [01] [6D 76 00 
     * 00]
     */

    private static void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega) {
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor().getId()); // skin color
        mplew.writeInt(chr.getFace()); // face
        mplew.write(mega ? 0 : 1);
        mplew.writeInt(chr.getHair()); // hair
        addCharEquips(mplew, chr);
    }

    private static void addCharacterInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeLong(-1L);
        mplew.write(0);
        mplew.write(0); // 0.94
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddylist().getCapacity()); // buddylist capacity
        mplew.write(0);
        mplew.writeShort(0); // 0.96
        addInventoryInfo(mplew, chr);
        addSkillInfo(mplew, chr);
        addQuestInfo(mplew, chr);
        //    mplew.writeShort(0); // Mini Games
        mplew.writeLong(0);
        for (int i = 0; i < 28; i++) { // v97 has 28 of these suckers
            mplew.write(CHAR_INFO_MAGIC);
        }
        /* Monsterbook Data */
        mplew.write(0);
        mplew.write(0);
        //  /* Monsterbook Data */

        /* AddAreaData */
        mplew.writeShort(0);
        mplew.writeShort(0);

        mplew.writeShort(0);
        // for (int i : chr.getQuestData().keySet()) {
        //     mplew.writeShort(i);
        //     mplew.writeMapleAsciiString(chr.getQuestData().get(i));
        // }
        /* AddAreaData */

        if (chr.isJaguar()) {
            addJaguarInfo(mplew); // 0.95
        }
    }

    private static void addCharacterData(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeLong(-1L);
        mplew.write(0);
        mplew.write(0); // 0.94
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddylist().getCapacity()); // buddylist capacity
        mplew.write(0);
        mplew.writeShort(0); // 0.96
        addInventoryInfo(mplew, chr);
        addSkillInfo(mplew, chr);
        mplew.writeInt(0); // quests
        //    mplew.writeShort(0); // Mini Games
        mplew.writeLong(0);
        for (int i = 0; i < 28; i++) { // v97 has 28 of these suckers
            mplew.write(CHAR_INFO_MAGIC);
        }
        /* Monsterbook Data */
        mplew.write(0);
        mplew.write(0);
        //  /* Monsterbook Data */

        /* AddAreaData */
        mplew.writeShort(0);
        mplew.writeShort(0);

        mplew.writeShort(0);
        // for (int i : chr.getQuestData().keySet()) {
        //     mplew.writeShort(i);
        //     mplew.writeMapleAsciiString(chr.getQuestData().get(i));
        // }
        /* AddAreaData */

        if (chr.isJaguar()) {
            addJaguarInfo(mplew); // 0.95
        }
    }

    private static void addTeleportInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final int[] tele = chr.getTrockMaps();
        final int[] viptele = chr.getVipTrockMaps();
        for (int i = 0; i < 5; i++) {
            mplew.writeInt(tele[i]);
        }
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(viptele[i]);
        }
    }
    /*
     *  [05] [82 DE 0F 00] [06] [2D 2D 10 00] [07] [81 5B 10 00] [0B] [15 2C 14 00] [FF] [FF] [00 00 00 00] [00 00 00 00] [00 00 00 00] 
     * [00 00 00 00 ]
     * 00 01 01 00 00 00 00 00 00 00 D5 00 00 00 2C FF FF FF 01 05 00 00 00 00 00 00 00
     */

    private static void addCharEquips(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<IItem> ii = MapleItemInformationProvider.getInstance().canWearEquipment(chr, equip.list());
        Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
        for (IItem item : ii) {
            byte pos = (byte) (item.getPosition() * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, item.getItemId());
            } else if (pos > 100 && pos != 111) { // don't ask. o.o
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getItemId());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getItemId());
            }
        }
        for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }

        mplew.write(0xFF);
        for (Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        IItem cWeapon = equip.getItem((byte) -111);
        mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
        for (int i = 0; i < 3; i++) {
            if (chr.getPet(i) != null) {
                mplew.writeInt(chr.getPet(i).getItemId());
            } else {
                mplew.writeInt(0);
            }
        }
    }
    /*
     * 0E 00 00 19 78 00 00 44 61 79 75 6D 73 75 6E 00 00 00 00 00 00 01 21 4E 00 00 6D 76 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
     * 00 00 00 00 00 00 00 00 00 00 00 01 00 00 0C 00 05 00 04 00 04 00 32 00 00 00 32 00 00 00 05 00 00 00 05 00 00 00 00 00 00 00 00 00
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 01 21 4E 00 00 01 6D 76 00 00 05 82 DE 0F 00 06 2D 2D 10 00 07 81 5B 10
     * 00 0B 15 2C 14 
     * 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
     * 
     * normal char :
     *      * [00] [01] [01 00 00 00] [00 00 00 00] [D5 00 00 00] 2C FF FF FF 01 05 00 00 00 00 00 00 00
     */

    private static void addCharEntry(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean viewall) {
        addCharStats(mplew, chr);
        addCharLook(mplew, chr, false);
        if (!viewall) {
            mplew.write(0);
        }
        if (chr.isGM()) {
            mplew.write(0);
            return;
        }
        mplew.write(1); // world rank enabled (next 4 ints are not sent if disabled) Short??
        mplew.writeInt(chr.getRank()); // world rank
        mplew.writeInt(chr.getRankMove()); // move (negative is downwards)
        mplew.writeInt(chr.getJobRank()); // job rank
        mplew.writeInt(chr.getJobRankMove()); // move (negative is downwards)*/
    }
////[00 00 00 00] [02 00] [DE 28 01 00] [30] [E0 28 01 00] [30] 00 00 00 00 00 00 00 00 00 00 

    private static void addQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(chr.getStartedQuestsSize());
        for (MapleQuestStatus q : chr.getStartedQuests()) {
            mplew.writeShort(q.getQuest().getId());
            mplew.writeMapleAsciiString(q.getQuestData());
            if (q.getQuest().getInfoNumber() > 0) {
                mplew.writeShort(q.getQuest().getInfoNumber());
                mplew.writeMapleAsciiString(Integer.toString(q.getMedalProgress()));
            }
        }
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        mplew.writeShort(completed.size());
        for (MapleQuestStatus q : completed) {
            mplew.writeShort(q.getQuest().getId());
            int time = getQuestTimestamp(q.getCompletionTime());
            mplew.writeInt(time);
            mplew.writeInt(time);
        }
    }
// addItemInfo(mplew, item, true);

    private static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item) {
        addItemInfo(mplew, item, false, false);
    }

    private static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item, boolean zeroPosition) {
        addItemInfo(mplew, item, zeroPosition, false, false, false, false);
    }

    private static void addItemInfoFromDrop(MaplePacketLittleEndianWriter mplew, IItem item, boolean hiddenpot) {
        addItemInfoFromDrop(mplew, item, true, false, false, false, false, hiddenpot);
    }

    private static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time, boolean showexpirationtime) {
        mplew.write(0);
        mplew.writeShort(0x0580); // 80 05
        if (time != -1) {
            mplew.writeInt(getItemTimestamp(time));
            mplew.write(1);
        } else {
            mplew.writeInt(0x17E646BB); // BB 46 E6 17 02
            mplew.write(2);
        }
    }

    private static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long y) {
        addExpirationTime(mplew, y, true);
    }

    private static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item, boolean zeroPosition, boolean leaveOut) {
        addItemInfo(mplew, item, zeroPosition, leaveOut, false, false, false);
    }
//02 99 AB 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 02 00 00 00 00 00[]

    private static void addItemInfoFromDrop(MaplePacketLittleEndianWriter mplew, IItem item, boolean zeroPosition, boolean leaveOut, boolean gachapon, boolean inTrade, boolean fromDrop, boolean hiddenpot) {
        boolean ring = false;
        IEquip equip = null;
        if (item.getType() == IItem.EQUIP) {
            equip = (IEquip) item;
            if (equip.getRingId() > -1) {
                ring = true;
            }
        }
        byte pos = item.getPosition();
        boolean masking = false;
        if (!gachapon) {
            if (zeroPosition) {
                if (!leaveOut) {
                    //    mplew.write(0);
                }
            } else if (pos <= (byte) -1) { // equiipped
                pos *= -1;
                if (pos > 100 || ring) {
                    masking = true;
                    mplew.writeShort(pos - 100);
                } else {
                    if ((item.getType() == IItem.EQUIP) && (!inTrade)) {
                        mplew.writeShort(pos);
                    } else {
                        mplew.write(pos);
                    }
                }
            } else {
                if (!fromDrop) {
                    if ((item.getType() == IItem.EQUIP) && (!inTrade)) {
                        mplew.writeShort(item.getPosition());
                    } else {
                        mplew.write(item.getPosition());
                    }
                }
            }
        }
        if (item.getPetId() > -1) {
            mplew.write(3);
        } else {
            mplew.write(item.getType());
        }
        mplew.writeInt(item.getItemId());
        if (ring) {
            mplew.write(1);
            mplew.writeInt(equip.getRingId());
            mplew.writeInt(0);
        }
        //  mplew.writeInt(-1); Some Durability Shiz Here
        if (item.getPetId() > -1) {
            MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getPosition(), item.getPetId());
            String petname = pet.getName();
            mplew.write(1);
            mplew.writeInt(item.getPetId());
            mplew.writeInt(0);
            mplew.write(0);
            mplew.write(ITEM_MAGIC);
            mplew.write(HexTool.getByteArrayFromHexString("BB 46 E6 17 02"));
            if (petname.length() > 13) {
                petname = petname.substring(0, 13);
            }
            mplew.writeAsciiString(petname);
            for (int i = petname.length(); i < 13; i++) {
                mplew.write(0);
            }
            mplew.write(pet.getLevel());
            mplew.writeShort(pet.getCloseness());
            mplew.write(pet.getFullness());
            mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
            mplew.writeInt(0);
            mplew.write(HexTool.getByteArrayFromHexString("50 46 00 00 00 00")); //wonder what this is - WAS 50 46 00 00
            return;
        }
        if (masking && !ring) {
            mplew.write(HexTool.getByteArrayFromHexString("01 6D 9F 8A 00 00 00 00 00 E0 33 9E"));
            addExpirationTime(mplew, 0, false);
        } else if (ring) {
            mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
        } else {

            mplew.writeShort(0);
            mplew.write(ITEM_MAGIC);
            mplew.write(NON_EXPIRE2);
            //addExpirationTime(mplew, 0, false);

        }
        mplew.writeInt(-1);
        if (item.getType() == IItem.EQUIP) {

            mplew.write(equip.getUpgradeSlots());
            mplew.write(equip.getLevel());
            mplew.writeShort(equip.getStr());
            mplew.writeShort(equip.getDex());
            mplew.writeShort(equip.getInt());
            mplew.writeShort(equip.getLuk());
            mplew.writeShort(equip.getHp());
            mplew.writeShort(equip.getMp());
            mplew.writeShort(equip.getWatk());
            mplew.writeShort(equip.getMatk());
            mplew.writeShort(equip.getWdef());
            mplew.writeShort(equip.getMdef());
            mplew.writeShort(equip.getAcc());
            mplew.writeShort(equip.getAvoid());
            mplew.writeShort(equip.getHands());
            mplew.writeShort(equip.getSpeed());
            mplew.writeShort(equip.getJump());
            mplew.writeMapleAsciiString(equip.getOwner());
            mplew.writeShort(equip.getFlag());
            if (!masking) {
                mplew.write(0);
                mplew.write(equip.getItemLevel());
                mplew.writeShort(0);
                mplew.writeShort(0); // Item Exp
                mplew.writeInt(-1);
                mplew.writeShort(equip.getVicious());
                mplew.writeShort(0);
                mplew.write(!hiddenpot ? equip.isPotentialByte() : 1); // pot zero
                mplew.write(equip.getStarsByte()); // starz
                mplew.writeShort(equip.getPotentialStats(1)); // pot 1
                mplew.writeShort(equip.getPotentialStats(2));// pot 2
                mplew.writeShort(equip.getPotentialStats(3));// pot 3
                mplew.writeInt(0);
                mplew.writeLong(-1);

            } else {
                mplew.write(HexTool.getByteArrayFromHexString("00 00 00 40 E0 FD FF FF FF FF"));
                mplew.writeLong(0);
                mplew.writeLong(0);
            }

            mplew.write(HexTool.getByteArrayFromHexString("00 40 E0 FD 3B 37 4F 01"));
            mplew.writeInt(-1);

        } else {
            mplew.writeShort(item.getQuantity());
            mplew.writeMapleAsciiString(item.getOwner());
            mplew.writeShort(item.getFlag());
            if (ItemConstants.isRechargable(item.getItemId())) {
                mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 54 00 00 34"));
            }
        }
    }

    private static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item, boolean zeroPosition, boolean leaveOut, boolean gachapon, boolean inTrade, boolean fromDrop) {
        boolean ring = false;
        IEquip equip = null;
        if (item.getType() == IItem.EQUIP) {
            equip = (IEquip) item;
            if (equip.getRingId() > -1) {
                ring = true;
            }
        }
        byte pos = item.getPosition();
        boolean masking = false;
        if (!zeroPosition) {
            if (pos < 0) {
                pos *= -1;
                if (pos >= 1000) {
                    mplew.writeShort(pos);
                } else if (pos > 100) {
                    mplew.write(pos - 100);
                } else {
                    mplew.write(pos);
                }
            } else {
                mplew.write(pos);
            }
            if (pos < 1000 && item.getItemId() / 1000000 == 1 && !inTrade) { // this is fine here as trade slot isn't negative
                mplew.write(0);
            }
        }
        if (item.getPetId() > -1) {
            mplew.write(3);
        } else {
            mplew.write(item.getType());
        }
        mplew.writeInt(item.getItemId());
        if (ring) {
            mplew.write(1);
            mplew.writeInt(equip.getRingId());
            mplew.writeInt(0);
        }

        if (item.getPetId() > -1) {
            MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getPosition(), item.getPetId());
            addPetInfo(mplew, pet, true);
            return;
        }
        mplew.write(masking || ring ? 1 : 0);
        if (masking && !ring) {
            mplew.writeLong(1);//unique id?
        } else if (ring) {
            mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
        } else {
            mplew.write(0);
        }
        mplew.write(ITEM_MAGIC);
        mplew.write(NON_EXPIRE2);
        mplew.writeInt(-1);
        if (item.getType() == IItem.EQUIP) {
            mplew.write(equip.getUpgradeSlots());
            mplew.write(equip.getLevel());
            mplew.writeShort(equip.getStr());
            mplew.writeShort(equip.getDex());
            mplew.writeShort(equip.getInt());
            mplew.writeShort(equip.getLuk());
            mplew.writeShort(equip.getHp());
            mplew.writeShort(equip.getMp());
            mplew.writeShort(equip.getWatk());
            mplew.writeShort(equip.getMatk());
            mplew.writeShort(equip.getWdef());
            mplew.writeShort(equip.getMdef());
            mplew.writeShort(equip.getAcc());
            mplew.writeShort(equip.getAvoid());
            mplew.writeShort(equip.getHands());
            mplew.writeShort(equip.getSpeed());
            mplew.writeShort(equip.getJump());
            mplew.writeMapleAsciiString(equip.getOwner());
            mplew.writeShort(equip.getFlag());
            mplew.write(0);
            if (!masking) {
                mplew.write(equip.getItemLevel());
                mplew.writeShort(0);
                mplew.writeShort(0); // Item Exp
                mplew.writeInt(-1);
                mplew.writeInt(equip.getVicious());
                mplew.write(equip.isPotentialByte()); // pot zero // 1 = is potential
                mplew.write(equip.getStarsByte()); // starz
                mplew.writeShort(equip.getPotentialStats(1)); // pot 1
                mplew.writeShort(equip.getPotentialStats(2));// pot 2
                mplew.writeShort(equip.getPotentialStats(3));// pot 3
                mplew.writeShort(0);//hp ratio
                mplew.writeShort(0);//mp ratio
                mplew.writeLong(-1);

            } else {
                mplew.write(HexTool.getByteArrayFromHexString("00 00 40 E0 FD"));
                mplew.writeInt(-1); // durability
                mplew.writeInt(0); // vicious
                mplew.writeLong(0);
                mplew.writeInt(0);
                mplew.writeShort(0); // 
            }

            mplew.write(HexTool.getByteArrayFromHexString("00 40 E0 FD 3B 37 4F 01"));
            mplew.writeInt(-1);

        } else {
            mplew.writeShort(item.getQuantity());
            mplew.writeMapleAsciiString(item.getOwner());
            mplew.writeShort(item.getFlag());
            if (ItemConstants.isRechargable(item.getItemId())) {
                mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 54 00 00 34"));
            }
        }
    }

    private static void addInventoryInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getMeso());
        for (byte i = 1; i <= 5; i++) {
            mplew.write(chr.getInventory(MapleInventoryType.getByType(i)).getSlotLimit());
        }
        mplew.write(HexTool.getByteArrayFromHexString("00 40 E0 FD 3B 37 4F 01"));
        //     mplew.write(0);
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<IItem> equippedC = iv.list();
        List<Item> equipped = new ArrayList<Item>(equippedC.size());
        List<Item> equippedCash = new ArrayList<Item>(equippedC.size());
        for (IItem item : equippedC) {
            if (item.getPosition() <= -100) {
                equippedCash.add((Item) item);
            } else {
                equipped.add((Item) item);
            }
        }
        Collections.sort(equipped);
        for (Item item : equipped) {
            addItemInfo(mplew, item);
        }
        mplew.writeShort(0);
        for (Item item : equippedCash) {
            addItemInfo(mplew, item);
        }
        mplew.writeShort(0);
        for (IItem item : chr.getInventory(MapleInventoryType.EQUIP).list()) {
            addItemInfo(mplew, item);
        }
        mplew.writeShort(0);
        mplew.writeShort(0); // 0.88 - Dragon Equip
        mplew.writeShort(0); // 0.94 - Mechanic Equip
        for (IItem item : chr.getInventory(MapleInventoryType.USE).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (IItem item : chr.getInventory(MapleInventoryType.SETUP).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (IItem item : chr.getInventory(MapleInventoryType.ETC).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (IItem item : chr.getInventory(MapleInventoryType.CASH).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        mplew.writeInt(-1); // 0.96
    }

//[00 00 00 00] [02 00] [DE 28 01 00] [30] [E0 28 01 00] [30] 00 00 00 00 00 00 00 00 00 00 
    private static void addSkillInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Map<ISkill, MapleCharacter.SkillEntry> skills = chr.getSkills();
        mplew.writeShort(0);
        /*  for (Entry<ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
        mplew.writeInt(skill.getKey().getId());
        mplew.writeInt(skill.getValue().skillevel);
        
        mplew.write(0);
        mplew.write(ITEM_MAGIC);
        mplew.write(NON_EXPIRE);
        
        if (skill.getValue().hasMastery()) //love you moogra
        {
        mplew.writeInt(skill.getValue().masterlevel);
        }*/
        mplew.writeShort(0); // cooldowns
            /*  if (skill.getKey().isFourthJob() && !chr.getJob().isA(MapleJob.EVAN1)) {
        mplew.writeInt(skill.getValue().masterlevel);
        }*/
    }

    private static void addMonsterBookInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getMonsterBookCover()); // cover
        mplew.write(0);
        Map<Integer, Integer> cards = chr.getMonsterBook().getCards();
        mplew.writeShort(cards.size());
        for (Entry<Integer, Integer> all : cards.entrySet()) {
            mplew.writeShort(all.getKey() % 10000); // Id
            mplew.write(all.getValue()); // Level
        }
    }

    public static MaplePacket sendGuestTOS() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SEND_LINK.getValue());
        mplew.writeShort(0x100);
        mplew.writeInt(Randomizer.nextInt(999999));
        mplew.writeLong(0);
        mplew.write(new byte[]{(byte) 0x40, (byte) 0xE0, (byte) 0xFD, (byte) 0x3B, (byte) 0x37, (byte) 0x4F, 1});
        mplew.writeLong(getKoreanTimestamp(System.currentTimeMillis()));
        mplew.writeInt(0);
        mplew.writeMapleAsciiString("http://maplefags.com");
        return mplew.getPacket();
    }

    /**
     * Sends a hello packet.
     *
     * @param mapleVersion The maple client version.
     * @param sendIv the IV used by the server for sending
     * @param recvIv the IV used by the server for receiving
     * @return
     */
    public static MaplePacket getHello(short mapleVersion, byte[] sendIv, byte[] recvIv) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(0x0E);
        mplew.writeShort(97);
        mplew.writeMapleAsciiString("3"); // patch version
        mplew.write(recvIv);
        mplew.write(sendIv);
        mplew.write(8);
        return mplew.getPacket();
    }

    /**
     * Sends a ping packet.
     *
     * @return The packet.
     */
    public static MaplePacket getPing() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendOpcode.PING.getValue());
        return mplew.getPacket();
    }

    /**
     * Gets a login failed packet.
     *
     * Possible values for <code>reason</code>:<br>
     * 3: ID deleted or blocked<br>
     * 4: Incorrect password<br>
     * 5: Not a registered id<br>
     * 6: System error<br>
     * 7: Already logged in<br>
     * 8: System error<br>
     * 9: System error<br>
     * 10: Cannot process so many connections<br>
     * 11: Only users older than 20 can use this channel<br>
     * 13: Unable to log on as master at this ip<br>
     * 14: Wrong gateway or personal info and weird korean button<br>
     * 15: Processing request with that korean button!<br>
     * 16: Please verify your account through email...<br>
     * 17: Wrong gateway or personal info<br>
     * 21: Please verify your account through email...<br>
     * 23: License agreement<br>
     * 25: Maple Europe notice =[<br>
     * 27: Some weird full client notice, probably for trial versions<br>
     *
     * @param reason The reason logging in failed.
     * @return The login failed packet.
     */
    public static MaplePacket getLoginFailed(int reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        mplew.writeInt(reason);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket sendPolice(int reason, String reasoning, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GM_POLICE.getValue());
        mplew.writeInt(duration);
        mplew.write(4); //Hmmm
        mplew.write(reason);
        mplew.writeMapleAsciiString(reasoning);
        return mplew.getPacket();
    }

    public static MaplePacket sendPolice(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GM_POLICE.getValue());
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static MaplePacket getPermBan(byte reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        mplew.writeShort(2); // Account is banned
        mplew.write(0);
        mplew.write(reason);
        mplew.write(new byte[]{1, 1, 1, 1, 0});

        return mplew.getPacket();
    }

    public static MaplePacket getTempBan(long timestampTill, byte reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);
        mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        mplew.write(2);
        mplew.write0(5);
        mplew.write(reason);
        mplew.writeLong(timestampTill); // Tempban date is handled as a 64-bit long, number of 100NS intervals since 1/1/1601. Lulz.

        return mplew.getPacket();
    }

    /**
     * Gets a successful authentication and PIN Request packet.
     *
     * @param c
     * @param account The account name.
     * @return The PIN request packet.
     */
    /*
     * [00 00] [00 00 00 00][ 00 00] [5E 01 00 00] [00] [00] [00] [00] 00 [08 00] [66 61 74 65 6A 69 6B 69] [00] [00] 
     * [00 00 00 00 00 00 00 00] [00 00 00 00 00 00 00 00] [01] [00 00 00 00 00 00 00 00] 00 00 00 00 00
     */
    public static MaplePacket getAuthSuccess(MapleClient c, String account) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(c.getAccID()); //user id
        mplew.write(c.getGender());
        mplew.write(c.gmLevel() > 0 ? 0 : 0); //admin byte //2nd 0 == 1
        mplew.write(0);
        mplew.write(0);
        mplew.write(0); // v97
        mplew.writeMapleAsciiString(account);
        mplew.write(0);
        mplew.write(0); //isquietbanned
        mplew.writeLong(0);
        mplew.writeLong(0); //creation time
        mplew.writeInt(0);
        //  if (ServerConstants.ENABLE_PIN) {
        //        mplew.writeShort(c.getPic() == null || c.getPic().length() == 0 ? 0 : 1); //for sure when the pic is empty
        //  } else {
        mplew.write(2);
        mplew.write(2);
        //   }
        mplew.writeLong(0); // v97
        mplew.writeInt(0);// v97
        mplew.write(0);// v97
        return mplew.getPacket();
    }

    /**
     * Gets a packet detailing a PIN operation.
     *
     * Possible values for <code>mode</code>:<br>
     * 0 - PIN was accepted<br>
     * 1 - Register a new PIN<br>
     * 2 - Invalid pin / Reenter<br>
     * 3 - Connection failed due to system error<br>
     * 4 - Enter the pin
     *
     * @param mode The mode.
     * @return
     */
    private static MaplePacket pinOperation(byte mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PIN_OPERATION.getValue());
        mplew.write(mode);
        return mplew.getPacket();
    }

    public static MaplePacket pinRegistered() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PIN_ASSIGNED.getValue());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket requestPin() {
        return pinOperation((byte) 4);
    }

    public static MaplePacket requestPinAfterFailure() {
        return pinOperation((byte) 2);
    }

    public static MaplePacket registerPin() {
        return pinOperation((byte) 1);
    }

    public static MaplePacket pinAccepted() {
        return pinOperation((byte) 0);
    }

    public static MaplePacket wrongPic() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        // mplew.writeShort(SendOpcode.WRONG.getValue());
        // mplew.write(20);
        return mplew.getPacket();
    }

    /**
     * Gets a packet detailing a server and its channels.
     *
     * @param serverId
     * @param serverName The name of the server.
     * @param channelLoad Load of the channel - 1200 seems to be max.
     * @return The server info packet.
     */
    public static MaplePacket getServerList(int serverId, String serverName, Map<Integer, Integer> channelLoad) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERLIST.getValue());
        mplew.write(serverId);
        mplew.writeMapleAsciiString(serverName);
        mplew.write(ServerConstants.FLAG);
        mplew.writeMapleAsciiString(ServerConstants.EVENT_MESSAGE);
        mplew.write(0x64); // rate modifier, don't ask O.O!
        mplew.write(0x0); // event xp * 2.6 O.O!
        mplew.write(0x64); // rate modifier, don't ask O.O!
        mplew.write(0x0); // drop rate * 2.6
        mplew.write(0x0);
        int lastChannel = 1;
        Set<Integer> channels = channelLoad.keySet();
        for (int i = 30; i > 0; i--) {
            if (channels.contains(i)) {
                lastChannel = i;
                break;
            }
        }
        mplew.write(lastChannel);
        int load;
        for (int i = 1; i <= lastChannel; i++) {
            if (channels.contains(i)) {
                load = channelLoad.get(i) * 1200 / ServerConstants.CHANNEL_LOAD; // try this
            } else {
                load = ServerConstants.CHANNEL_LOAD;
            }
            mplew.writeMapleAsciiString(serverName + "-" + i);
            mplew.writeInt(load);
            mplew.write(1);
            mplew.writeShort(i - 1);
        }
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket getTestServerList(Map<Integer, Integer> channelLoad) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERLIST.getValue());
        mplew.write(1);
        mplew.writeMapleAsciiString("hi");
        mplew.write(ServerConstants.FLAG);
        mplew.writeMapleAsciiString("This is a TEST server. It is not opened to the general public!");
        mplew.write(0x64); // rate modifier, don't ask O.O!
        mplew.write(0x0); // event xp * 2.6 O.O!
        mplew.write(0x64); // rate modifier, don't ask O.O!
        mplew.write(0x0); // drop rate * 2.6
        mplew.write(0x0);
        int lastChannel = 1;
        Set<Integer> channels = channelLoad.keySet();
        for (int i = 30; i > 0; i--) {
            if (channels.contains(i)) {
                lastChannel = i;
                break;
            }
        }
        mplew.write(5); // 5 channels
        int load;
        for (int i = 1; i <= lastChannel; i++) {
            if (channels.contains(i)) {
                load = channelLoad.get(i) * 1200 / ServerConstants.CHANNEL_LOAD; // try this
            } else {
                load = ServerConstants.CHANNEL_LOAD;
            }
            mplew.writeMapleAsciiString("TESTSERVER");// servername;
            mplew.writeInt(load);
            mplew.write(1);
            mplew.writeShort(i - 1);
        }
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet saying that the server list is over.
     *
     * @return The end of server list packet.
     */
    public static MaplePacket getEndOfServerList() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.SERVERLIST.getValue());
        mplew.write(0xFF);
        return mplew.getPacket();
    }

    /**
     * Gets a packet detailing a server status message.
     *
     * Possible values for <code>status</code>:<br>
     * 0 - Normal<br>
     * 1 - Highly populated<br>
     * 2 - Full
     *
     * @param status The server status.
     * @return The server status packet.
     */
    public static MaplePacket getServerStatus(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.SERVERSTATUS.getValue());
        mplew.writeShort(status);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client the IP of the channel server.
     *
     * @param inetAddr The InetAddress of the requested channel server.
     * @param port The port the channel is on.
     * @param clientId The ID of the client.
     * @return The server IP packet.
     */
    /*
     * [0C 00] [00 00] [D9 17 01 67] [98 1D] [19 78 00 00] 00 00 00 00 00
     */
    public static MaplePacket getServerIP(InetAddress inetAddr, int port, int clientId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        byte[] addr = inetAddr.getAddress();
        mplew.write(addr);
        mplew.writeShort(port);
        mplew.writeInt(clientId);
        mplew.write(new byte[]{0, 0, 0, 0, 0});
        return mplew.getPacket();
    }

    public static MaplePacket getTestServerIP(int port, int clientId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        byte[] addr = {(byte) 192, (byte) 168, (byte) 2, (byte) 20};
        mplew.write(addr);
        mplew.writeShort(port);
        mplew.writeInt(clientId);
        mplew.write(new byte[]{0, 0, 0, 0, 0});
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client the IP of the new channel.
     *
     * @param inetAddr The InetAddress of the requested channel server.
     * @param port The port the channel is on.
     * @return The server IP packet.
     */
    ////[11 00] [01] [C7 10 E9 28] 86 21 00
    // [10 00] [01] [D9 17 01 67] [99 1D]
    public static MaplePacket getChannelChange(InetAddress inetAddr, int port) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        byte[] addr = inetAddr.getAddress();
        mplew.write(addr);
        mplew.writeShort(port);

        return mplew.getPacket();
    }

    /**
     * Gets a packet with a list of characters.
     *
     * @param c The MapleClient to load characters of.
     * @param serverId The ID of the server requested.
     * @return The character list packet.
     */
    /* no chars
     * [0B 00] [00] [00] [00] [05 00 00 00] 00 00 00 00
     * resistance :
     * 0B 00 00 01 11 78 00 00 69 52 65 73 69 73 74 00 00 00 00 00 00 00 02 2A 4E 00 00 A8 75 00 00 00 00 00 00 00 00 
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 B8 0B 0C 00 05 00 04 00 04 00 32 00 00 00 32 00 00 00 05
     * 00 00 00 05 00 00 00 00 00 01 01 00 00 00 00 00 00 00 00 00 00 00 C0 EE 7D 37 00 00 00 00 00 00 00 00 02 2A 4E 00
     * 00 01 A8 75 00 00 05 45 06 10 00 07 A6 5B 10 00 0B 8C DE 13 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
     * 00 00 00 01 01 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00
     * 
     * explorer :
     * [0B 00] [00] [01] 19 78 00 00 44 61 79 75 6D 73 75 6E 00 00 00 00 00 00 01 21 4E 00 00 6D 76 00 00 00 00 00 00 00 00 00 00 
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 0C 00 05 00 04 00 04 00 32 00 00 00 32 00 00 00 05 00 00 00 
     * 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 27 00 00 02 00 00 00 00 01 00 00 01 21 4E 00 00 01 6D 76 00 
     * 00 05 82 DE 0F 00 06 2D 2D 10 00 07 81 5B 10 00 0B 15 2C 14 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
     * 00 01 01 00 00 00 00 00 00 00 D5 00 00 00 2C FF FF FF 01 05 00 00 00 00 00 00 00
     *      * normal char :
     *      * [00] [01] [01 00 00 00] [00 00 00 00] [D5 00 00 00] 2C FF FF FF 
     */
    public static MaplePacket getCharList(MapleClient c, int serverId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHARLIST.getValue());
        mplew.write(0);
        List<MapleCharacter> chars = c.loadCharacters(serverId);
        mplew.write((byte) chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr, false);
        }
        if (ServerConstants.ENABLE_PIC) {
            mplew.write(c.getPic() == null || c.getPic().length() == 0 ? 0 : 1);
        } else {
            mplew.write(1);
        }

        mplew.writeLong(c.getCharacterSlots());
        return mplew.getPacket();
    }

    public static MaplePacket enableTV() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.ENABLE_TV.getValue());
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * Removes TV
     * 
     * @return The Remove TV Packet
     */
    public static MaplePacket removeTV() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendOpcode.REMOVE_TV.getValue());
        return mplew.getPacket();
    }

    /**
     * Sends MapleTV
     *
     * @param chr The character shown in TV
     * @param messages The message sent with the TV
     * @param type The type of TV
     * @param partner The partner shown with chr
     * @return the SEND_TV packet
     */
    public static MaplePacket sendTV(MapleCharacter chr, List<String> messages, int type, MapleCharacter partner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SEND_TV.getValue());
        mplew.write(partner != null ? 3 : 1);
        mplew.write(type); //Heart = 2  Star = 1  Normal = 0
        addCharLook(mplew, chr, false);
        mplew.writeMapleAsciiString(chr.getName());
        if (partner != null) {
            mplew.writeMapleAsciiString(partner.getName());
        } else {
            mplew.writeShort(0);
        }
        for (int i = 0; i < messages.size(); i++) {
            if (i == 4 && messages.get(4).length() > 15) {
                mplew.writeMapleAsciiString(messages.get(4).substring(0, 15));
            } else {
                mplew.writeMapleAsciiString(messages.get(i));
            }
        }
        mplew.writeInt(1337); // time limit shit lol 'Your thing still start in blah blah seconds'
        if (partner != null) {
            addCharLook(mplew, partner, false);
        }
        return mplew.getPacket();
    }

    /**
     * Gets character info for a character.
     *
     * @param chr The character to get info about.
     * @return The character info packet.
     */
    /*
     * 8F 00 02 00 01 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 01 00 00 D5 70 BB
     * 20 61 63 79 F8 64 67 E1 AB FF FF FF FF FF FF FF FF 00 00 19 78 00 00 44 61 79 75 6D 73 75 6E 00 00 00 00 00 00 01 21 
     * 4E 00 00 6D 76 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 0C 00 05 00 04 
     * 00 04 00 32 00 00 00 32 00 00 00 05 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
     * 00 00 00 01 00 14 00 00 00 19 34 00 00 18 18 18 18 60 00 40 E0 FD 3B 37 4F 01 05 00 01 82 DE 0F 00 00 00 80 05 BB 46 
     * E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 00 00 00 00 00 00 00 00 00 00 
     * 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF 00
     * 40 E0 FD 3B 37 4F 01 FF FF FF FF 06 00 01 2D 2D 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00
     * 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 07 00 01 81
     * 5B 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
     * FF FF FF FF FF FF FF FF 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0B 00 01 15 2C 14 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 
     * 07 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
     * 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     * 00 00 00 00 00 00 00 00 00 00 01 02 82 84 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 64 00 00 00 00 00 02 02 86 84 1E 00
     * 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 64 00 00 00 00 00 00 00 01 02 E9 7D 3F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01
     * 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9
     * 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A
     * 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B
     * FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
     * 8D 55 BC 31 AC CC 01 64 00 00 008F 00 02 00 01 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 
     * 00 00 00 01 00 00 D5 70 BB 20 61 63 79 F8 64 67 E1 AB FF FF FF FF FF FF FF FF 00 00 19 78 00 00 44 61 79 75 6D 73 75 6E 00
     * 00 00 00 00 00 01 21 4E 00 00 6D 76 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00
     * 0C 00 05 00 04 00 04 00 32 00 00 00 32 00 00 00 05 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
     * 00 00 00 00 00 00 01 00 14 00 00 00 19 34 00 00 18 18 18 18 60 00 40 E0 FD 3B 37 4F 01 05 00 01 82 DE 0F 00 00 00 80 05 BB 
     * 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 
     * 00 00 00 00 00 00 00 00 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 
     * 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 06 00 01 2D 2D 10 00 00 00 80 
     * 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 
     * 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 40 
     * E0 FD 3B 37 4F 01 FF FF FF FF 07 00 01 81 5B 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 00 00 00 00 00 00 00 00 00 
     * 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 
     * 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0B 00 01 15 2C 14 00 00 00 80 05 
     * BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
     * 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 40 E0 
     * FD 3B 37 4F 01 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 01 02 82 84 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 64 00 00 
     * 00 00 00 02 02 86 84 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 64 00 00 00 00 00 00 00 01 02 E9 7D 3F 00 00 00 80 05 BB 
     * 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF C9 9A 3B FF 
     * C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 
     * 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 
     * 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B 00 00 00 00 00 
     * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8D 55 BC 31 AC CC 01 64 00 00 00
     */
    public static MaplePacket getCharInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WARP_TO_MAP.getValue());
        addChannelInfo(mplew, chr.getClient().getChannel());
        mplew.write(1);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.writeShort(0);
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(Randomizer.nextInt());
        }
        addCharacterInfo(mplew, chr);
        mplew.writeInt(0);
        mplew.writeInt(0); // Lucky reward after long unactivity; Received/do not show = 1; not received/show = 0
        mplew.writeInt(30100003); // SN of cash item #1
        mplew.writeInt(30100003); // SN #2
        mplew.writeInt(30100003); // SN #3
        mplew.write(0);
        mplew.writeLong(getTime(System.currentTimeMillis()));
        mplew.writeInt(100); // random stuff*/
        return mplew.getPacket();
    }

    public static void addChannelInfo(MaplePacketLittleEndianWriter mplew, int channel) {
        mplew.write(WARP_PACKET_MAGIC);
        mplew.writeInt(channel - 1);
        mplew.writeInt(0);
    }

    /**
     * Gets an empty stat update.
     *
     * @return The empy stat update packet.
     */
    public static MaplePacket enableActions() {
        return updatePlayerStats(null, EMPTY_STATUPDATE, true);
    }

    /**
     * Gets an update for specified stats.
     *
     * @param stats The stats to update.
     * @return The stat update packet.
     */
    public static MaplePacket updatePlayerStats(MapleClient c, List<Pair<MapleStat, Integer>> stats) {
        return updatePlayerStats(c, stats, false);
    }

    public static MaplePacket updatePlayerStats(List<Pair<MapleStat, Integer>> stats, boolean itemreaction) {
        return updatePlayerStats(null, stats, itemreaction);
    }

    /**
     * Gets an update for specified stats.
     *
     * @param stats The list of stats to update.
     * @param itemReaction Result of an item reaction(?)
     * @return The stat update packet.
     */
    public static MaplePacket updatePlayerStats(MapleClient c, List<Pair<MapleStat, Integer>> stats, boolean itemReaction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_STATS.getValue());
        mplew.write(itemReaction ? 1 : 0);
        int updateMask = 0;
        for (Pair<MapleStat, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<MapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {

                @Override
                public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) {
                    int val1 = o1.getLeft().getValue();
                    int val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                }
            });
        }
        mplew.writeInt(updateMask);
        for (Pair<MapleStat, Integer> statupdate : mystats) {
            if (statupdate.getLeft().getValue() >= 1) {
                if (statupdate.getLeft().getValue() == 0x1) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() <= 0x4) {
                    mplew.writeInt(statupdate.getRight());
                } else if (statupdate.getLeft().getValue() < 0x20) {
                    mplew.write(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() == 0x8000) {
                    boolean hasSp = c.getPlayer().getRemainingSp() > 0;
                    if (client.ExtendedSPType.getFromJobID(c.getPlayer().getJob().getId()) != null) {
                        mplew.write(hasSp ? 1 : 0);
                        if (hasSp) {
                            mplew.write(1); // TODO: SP Table Index
                            mplew.write(statupdate.getRight().byteValue()); // remaining sp
                        }
                    } else {
                        mplew.writeShort(statupdate.getRight().byteValue()); // remaining sp
                    }
                } else if (statupdate.getLeft().getValue() >= 0x400 && statupdate.getLeft().getValue() <= 0x2000) {
                    mplew.writeInt(statupdate.getRight().intValue());
                } else if (statupdate.getLeft().getValue() < 0xFFFF) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else {
                    mplew.writeInt(statupdate.getRight().intValue());
                }
            }
        }
        mplew.write(0); // 0.88
        mplew.write(0); // 0.94
        return mplew.getPacket();
    }

    public static MaplePacket updatePlayerStat(MapleClient c, Pair<MapleStat, Integer> stats, boolean itemReaction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_STATS.getValue());
        mplew.write(itemReaction ? 1 : 0);
        int updateMask = 0;
        updateMask = stats.getLeft().getValue();
        mplew.writeInt(updateMask);
        Pair<MapleStat, Integer> statupdate = stats;
        if (statupdate.getLeft().getValue() >= 1) {
            if (statupdate.getLeft().getValue() == 0x1) {
                mplew.writeShort(statupdate.getRight().shortValue());
            } else if (statupdate.getLeft().getValue() <= 0x4) {
                mplew.writeInt(statupdate.getRight());
            } else if (statupdate.getLeft().getValue() < 0x20) {
                mplew.write(statupdate.getRight().shortValue());
            } else if (statupdate.getLeft().getValue() == 0x8000) {
                boolean hasSp = c.getPlayer().getRemainingSp() > 0;
                if (client.ExtendedSPType.getFromJobID(c.getPlayer().getJob().getId()) != null) {
                    mplew.write(hasSp ? 1 : 0);
                    if (hasSp) {
                        mplew.write(1); // TODO: SP Table Index
                        mplew.write(statupdate.getRight().byteValue()); // remaining sp
                    }
                } else {
                    mplew.writeShort(statupdate.getRight().byteValue()); // remaining sp
                }
            } else if (statupdate.getLeft().getValue() >= 0x400 && statupdate.getLeft().getValue() <= 0x2000) {
                mplew.writeInt(statupdate.getRight().intValue());
            } else if (statupdate.getLeft().getValue() < 0xFFFF) {
                mplew.writeShort(statupdate.getRight().shortValue());
            } else {
                mplew.writeInt(statupdate.getRight().intValue());
            }
        }
        mplew.write(0); // 0.88
        mplew.write(0); // 0.94
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to change maps.
     *
     * @param to The <code>MapleMap</code> to warp to.
     * @param spawnPoint The spawn portal number to spawn at.
     * @param chr The character warping to <code>to</code>
     * @return The map change packet.
     */
    public static MaplePacket getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WARP_TO_MAP.getValue());
        mplew.write(WARP_PACKET_MAGIC);
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.writeInt(0); // v96
        mplew.writeLong(0); // Count
        mplew.write(0);
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeInt(chr.getHp());
        mplew.write(0);
        mplew.writeLong(getTime(System.currentTimeMillis()));
        mplew.writeInt(100);
        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a portal.
     *
     * @param townId The ID of the town the portal goes to.
     * @param targetId The ID of the target.
     * @param pos Where to put the portal.
     * @return The portal spawn packet.
     */
    public static MaplePacket spawnPortal(int townId, int targetId, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(14);
        mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        if (pos != null) {
            mplew.writePos(pos);
        }
        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a door.
     *
     * @param oid The door's object ID.
     * @param pos The position of the door.
     * @param town
     * @return The remove door packet.
     */
    public static MaplePacket spawnDoor(int oid, Point pos, boolean town) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendOpcode.SPAWN_DOOR.getValue());
        mplew.write(town ? 1 : 0);
        mplew.writeInt(oid);
        mplew.writePos(pos);
        return mplew.getPacket();
    }

    /**
     * Gets a packet to remove a door.
     *
     * @param oid The door's ID.
     * @param town
     * @return The remove door packet.
     */
    public static MaplePacket removeDoor(int oid, boolean town) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
        if (town) {
            mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
            mplew.writeInt(999999999);
            mplew.writeInt(999999999);
        } else {
            mplew.writeShort(SendOpcode.REMOVE_DOOR.getValue());
            mplew.write(0);
            mplew.writeInt(oid);
        }
        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a special map object.
     *
     * @param summon
     * @param skillLevel The level of the skill used.
     * @param animated Animated spawn?
     * @return The spawn packet for the map object.
     */
    public static MaplePacket spawnSummon(MapleSummon summon, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
        mplew.writeShort(SendOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(summon.getSkill());
        mplew.write(summon.getOwner().getLevel());
        mplew.write(1); //skilllevel < 1 -> error -5
        mplew.writeShort(summon.getPosition().x);
        mplew.writeShort(summon.getPosition().y);
        mplew.write(summon.getSkill() == BattleMage.SUMMON_REAPER_BUFF ? 5 : 4); // test - I think, this one is Stance.
        server.maps.MapleFoothold foothold = summon.getOwner().getMap().getFootholds().findBelow(summon.getPosition());
        mplew.writeShort(foothold == null ? 0 : foothold.getId()); //Pretty sure this is foothold, it only matters for walking summons of course
        mplew.write(summon.getMovementType().getValue()); // 0 = don't move, 1 = follow (4th mage summons?), 2/4 = only tele follow, 3 = bird follow
        //mplew.write(mirroredTarget ? 0 : 1); // 0 and the summon can't attack - but puppets don't attack with 1 either ^.- - Beholder = 2, Buff Summon ?
        switch (summon.getSkill()) {
            case Mechanic.GIANT_ROBOT:
                mplew.write(5);
                break;
            case 1085: // ARCANGEL
            case 10001085:
            case 20001085:
            case 20011085:
            case 30001085:
                mplew.write(2);
                break;
            case BladeLord.MIRROR_IMAGE:
                mplew.write(0);
                break;
            default:
                mplew.write(1);
        }
        mplew.write((animated ? 1 : 0));
        if (summon.getSkill() != BladeMaster.MIRRORED_TARGET) {
            mplew.writeShort(0);
        } else {
            mplew.write(1);
            addCharLook(mplew, summon.getOwner(), true);
            mplew.writeLong(0);
        }
        return mplew.getPacket();
    }

    /**
     * Gets a packet to remove a special map object.
     *
     * @param summon
     * @param animated Animated removal?
     * @return The packet removing the object.
     */
    public static MaplePacket removeSummon(MapleSummon summon, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendOpcode.REMOVE_SPECIAL_MAPOBJECT.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId());
        mplew.write(animated ? 4 : 1); // ?
        return mplew.getPacket();
    }

    /**
     * Gets the response to a relog request.
     *
     * @return The relog response packet.
     */
    public static MaplePacket getRelogResponse() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.RELOG_RESPONSE.getValue());
        mplew.write(1);
        return mplew.getPacket();
    }

    /**
     * Gets a server message packet.
     *
     * @param message The message to convey.
     * @return The server message packet.
     */
    public static MaplePacket serverMessage(String message) {
        return serverMessage(4, 0, message, true, false);
    }

    /**
     * Gets a server notice packet.
     *
     * Possible values for <code>type</code>:<br>
     * 0: [Notice]<br>
     * 1: Popup<br>
     * 2: Megaphone<br>
     * 3: Super Megaphone<br>
     * 4: Scrolling message at top<br>
     * 5: Pink Text<br>
     * 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param message The message to convey.
     * @return The server notice packet.
     */
    public static MaplePacket serverNotice(int type, String message) {
        return serverMessage(type, 0, message, false, false);
    }

    /**
     * Gets a server notice packet.
     *
     * Possible values for <code>type</code>:<br>
     * 0: [Notice]<br>
     * 1: Popup<br>
     * 2: Megaphone<br>
     * 3: Super Megaphone<br>
     * 4: Scrolling message at top<br>
     * 5: Pink Text<br>
     * 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param channel The channel this notice was sent on.
     * @param message The message to convey.
     * @return The server notice packet.
     */
    public static MaplePacket serverNotice(int type, int channel, String message) {
        return serverMessage(type, channel, message, false, false);
    }

    public static MaplePacket serverNotice(int type, int channel, String message, boolean smegaEar) {
        return serverMessage(type, channel, message, false, smegaEar);
    }

    /**
     * Gets a server message packet.
     *
     * Possible values for <code>type</code>:<br>
     * 0: [Notice]<br>
     * 1: Popup<br>
     * 2: Megaphone<br>
     * 3: Super Megaphone<br>
     * 4: Scrolling message at top<br>
     * 5: Pink Text<br>
     * 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param channel The channel this notice was sent on.
     * @param message The message to convey.
     * @param servermessage Is this a scrolling ticker?
     * @return The server notice packet.
     */
    private static MaplePacket serverMessage(int type, int channel, String message, boolean servermessage, boolean megaEar) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (servermessage) {
            mplew.write(1);
        }
        mplew.writeMapleAsciiString(message);
        if (type == 3) {
            mplew.write(channel - 1); // channel
            mplew.write(megaEar ? 1 : 0);
        } else if (type == 6) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    /**
     * Sends a Avatar Super Megaphone packet.
     *
     * @param chr The character name.
     * @param medal The medal text.
     * @param channel Which channel.
     * @param itemId Which item used.
     * @param message The message sent.
     * @param ear Whether or not the ear is shown for whisper.
     * @return
     */
    public static MaplePacket getAvatarMega(MapleCharacter chr, String medal, int channel, int itemId, List<String> message, boolean ear) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.AVATAR_MEGA.getValue());
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(medal + chr.getName());
        for (String s : message) {
            mplew.writeMapleAsciiString(s);
        }
        mplew.writeInt(channel - 1); // channel
        mplew.write(ear ? 1 : 0);
        addCharLook(mplew, chr, true);
        return mplew.getPacket();
    }

    /**
     * Sends the Gachapon green message when a user uses a gachapon ticket.
     * @param item
     * @param town
     * @param player
     * @return
     */
    public static MaplePacket gachaponMessage(IItem item, String town, MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(0x0B);
        mplew.writeMapleAsciiString(player.getName() + " : got a(n)");
        mplew.writeInt(0); //random?
        mplew.writeMapleAsciiString(town);
        addItemInfo(mplew, item, true);
        return mplew.getPacket();
    }

    public static MaplePacket spawnNPC(MapleNPC life) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(24);
        mplew.writeShort(SendOpcode.SPAWN_NPC.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        if (life.getF() == 1) {
            mplew.write(0);
        } else {
            mplew.write(1);
        }
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(23);
        mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        if (life.getF() == 1) {
            mplew.write(0);
        } else {
            mplew.write(1);
        }
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(MiniMap ? 1 : 0);
        return mplew.getPacket();
    }

    /**
     * Gets a spawn monster packet.
     *
     * @param life The monster to spawn.
     * @param newSpawn Is it a new spawn?
     * @return The spawn monster packet.
     */
    public static MaplePacket spawnMonster(MapleMonster life, boolean newSpawn) {
        return spawnMonsterInternal(life, false, newSpawn, false, 0, false);
    }

    /**
     * Gets a spawn monster packet.
     *
     * @param life The monster to spawn.
     * @param newSpawn Is it a new spawn?
     * @param effect The spawn effect.
     * @return The spawn monster packet.
     */
    public static MaplePacket spawnMonster(MapleMonster life, boolean newSpawn, int effect) {
        return spawnMonsterInternal(life, false, newSpawn, false, effect, false);
    }

    /**
     * Gets a control monster packet.
     *
     * @param life The monster to give control to.
     * @param newSpawn Is it a new spawn?
     * @param aggro Aggressive monster?
     * @return The monster control packet.
     */
    public static MaplePacket controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
        return spawnMonsterInternal(life, true, newSpawn, aggro, 0, false);
    }

    /**
     * Makes a monster invisible for Ariant PQ.
     * @param life
     * @return
     */
    public static MaplePacket makeMonsterInvisible(MapleMonster life) {
        return spawnMonsterInternal(life, true, false, false, 0, true);
    }

    /**
     * Internal function to handler monster spawning and controlling.
     *
     * @param life The mob to perform operations with.
     * @param requestController Requesting control of mob?
     * @param newSpawn New spawn (fade in?)
     * @param aggro Aggressive mob?
     * @param effect The spawn effect to use.
     * @return The spawn/control packet.
     */
    /*
     * [29 01] [01] [F6 93 04 00] [01 32 E8 8D 00 00 00 00] [00 00 00 00] [00 00] [00 00 00 00] [00]
     * [00 00 00 00] 00 88 00 00 00 00 00 00 C9 01 9A 00 03 00 00 13 00 FF FF 00 00 00 00 00 00 00 00 FF
     */
    private static MaplePacket spawnMonsterInternal(MapleMonster life, boolean requestController, boolean newSpawn, boolean aggro, int effect, boolean makeInvis) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (makeInvis) {
            mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
            mplew.write(0);
            mplew.writeInt(life.getObjectId());
            return mplew.getPacket();
        }
        if (requestController) {
            mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
            if (aggro) {
                mplew.write(2);
            } else {
                mplew.write(1);
            }
        } else {
            mplew.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
        }
        mplew.writeInt(life.getObjectId());
        mplew.write(life.getController() == null ? 5 : 1); // ????!? either 5 or 1?  5 if has no controller, 1 if so
        //mplew.write(5);
        mplew.writeInt(life.getId());
//[00] [88] [00 00] [00 00 00 00] [C9 01] [9A 00] [03] [00 00] [13 00] [FF FF] [00 00 00 00] [00 00 00 00 FF
        mplew.writeLong(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0x88);
        mplew.writeShort(0);
        mplew.writeInt(0); // v97
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);
        mplew.write(life.getStance());
        mplew.writeShort(life.getStartFh());
        mplew.writeShort(life.getFh());
        if (effect > 0) {
            mplew.write(effect);
            mplew.write(0);
            mplew.writeShort(0);
            if (effect == 15) {//15 seems to add a byte... (Dojo spawn effect)
                mplew.write(0);
            }
        }
        if (newSpawn) {
            mplew.writeShort(-2);
        } else {
            mplew.writeShort(-1);
        }
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(-1);
        return mplew.getPacket();
    }

    /**
     * Handles monsters not being targettable, such as Zakum's first body.
     * @param life The mob to spawn as non-targettable.
     * @param effect The effect to show when spawning.
     * @return The packet to spawn the mob as non-targettable.
     */
    public static MaplePacket spawnFakeMonster(MapleMonster life, int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(38);
        mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.write(life.getController() == null ? 5 : 1); // ????!? either 5 or 1?  5 if has no controller, 1 if so
        mplew.write(5); // v98
        mplew.writeInt(life.getId());
        mplew.writeLong(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0x88);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);
        mplew.write(life.getStance());
        mplew.writeShort(life.getStartFh());
        mplew.writeShort(life.getFh());
        if (effect > 0) {
            mplew.write(effect);
            mplew.write(0);
            mplew.writeShort(0);
        }
        mplew.writeShort(-2);
        mplew.writeLong(0); // v98 , now a long instead of two ints
        mplew.write(-1);
        return mplew.getPacket();
    }

    /**
     * Makes a monster previously spawned as non-targettable, targettable.
     * @param life The mob to make targettable.
     * @return The packet to make the mob targettable.
     */
    //spawnAction(mplew, life, 0, false, false, false, true);
    // spawn false,
    // newspawn false
    // fake false
    // fr = true
    public static MaplePacket makeMonsterReal(MapleMonster life) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(30);
        mplew.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.write(5);
        mplew.writeInt(life.getId());
        mplew.writeLong(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0x88);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);
        mplew.write(life.getStance());
        mplew.writeShort(life.getStartFh());
        mplew.writeShort(life.getFh());
        mplew.writeShort(-1);
        mplew.writeLong(0);
        mplew.writeInt(-1);
        return mplew.getPacket();
    }

    /**
     * Gets a stop control monster packet.
     *
     * @param oid The ObjectID of the monster to stop controlling.
     * @return The stop control monster packet.
     */
    public static MaplePacket stopControllingMonster(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(0);
        mplew.writeInt(oid);
        return mplew.getPacket();
    }

    /**
     * Gets a response to a move monster packet.
     *
     * @param objectid The ObjectID of the monster being moved.
     * @param moveid The movement ID.
     * @param currentMp The current MP of the monster.
     * @param useSkills Can the monster use skills?
     * @return The move response packet.
     */
    public static MaplePacket moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills) {
        return moveMonsterResponse(objectid, moveid, currentMp, useSkills, 0, 0);
    }

    /**
     * Gets a response to a move monster packet.
     *
     * @param objectid The ObjectID of the monster being moved.
     * @param moveid The movement ID.
     * @param currentMp The current MP of the monster.
     * @param useSkills Can the monster use skills?
     * @param skillId The skill ID for the monster to use.
     * @param skillLevel The level of the skill to use.
     * @return The move response packet.
     */
    //[2B 01] [E7 93 04 00] [01 00] [00] [0A 00] [00] [00] [00 00 00 00]
    public static MaplePacket moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);
        mplew.writeShort(SendOpcode.MOVE_MONSTER_RESPONSE.getValue());
        mplew.writeInt(objectid);
        mplew.writeShort(moveid);
        mplew.write(useSkills ? 1 : 0);
        mplew.writeShort(currentMp);
        mplew.write(skillId);
        mplew.write(skillLevel);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * Gets a general chat packet.
     *
     * @param cidfrom The character ID who sent the chat.
     * @param text The text of the chat.
     * @param whiteBG
     * @param show
     * @return The general chat packet.
     */
    public static MaplePacket getChatText(int cidfrom, String text, boolean gm, int show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHATTEXT.getValue());
        mplew.writeInt(cidfrom);
        mplew.write(gm ? 1 : 0);
        mplew.writeMapleAsciiString(text);
        mplew.write(show);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show an EXP increase.
     *
     * @param gain The amount of EXP gained.
     * @param inChat In the chat box?
     * @param white White text or yellow?
     * @return The exp gained packet.
     */
    public static MaplePacket showExpType(int gain, int equip, boolean inChat, boolean white, int type) {
        int bonusEventExp = 0;
        int bonusExp_3rdMo = 0;
        int weddingBOnus = 0;
        int partyBonus = 0;
        int equipItemBonus = 0;
        int internetCafeBonus = 0;
        int rainbowWeekBonus = 0;
        int partyRingBonus = 0;
        int cakeVSPie = 0;
        int normalexp = 0;
        switch (type) {
            case 1:
                bonusEventExp = gain;
                break;
            case 2:
                bonusExp_3rdMo = 50;
                break;
            case 3:
                weddingBOnus = gain;
                break;
            case 4:
                equipItemBonus = gain;
                break;
            case 5:
                internetCafeBonus = gain;
                break;
            case 6:
                rainbowWeekBonus = gain;
                break;
            case 7:
                partyRingBonus = gain;
                break;
            case 8:
                cakeVSPie = gain;
                break;
            default:
                normalexp = gain;
                break;
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        if (inChat) {
            mplew.write(4);
        } else {
            mplew.write(3); // (3,exp),(4,fame), (5,mesos),(6,guildpoints)
        }
        mplew.write(white ? 1 : 0);
        mplew.writeInt(gain);
        mplew.write(inChat ? 1 : 0);
        mplew.writeInt(bonusEventExp); // Bonus Event
        mplew.write(bonusExp_3rdMo); // "A bonus EXP (amount)% is awarded for every 3rd monster defeated".
        mplew.write(0);
        mplew.writeInt(weddingBOnus); // Wedding Bonus
        mplew.write(0);
        mplew.writeInt(partyBonus); // Party Bonus
        mplew.writeInt(equip); // Equip Item Bonus
        mplew.writeInt(internetCafeBonus); // Internet Cafe Bonus
        mplew.writeInt(rainbowWeekBonus); // Rainbow Week Bonus
        if (inChat) {
            mplew.write(0);
        }
        mplew.writeInt(partyRingBonus); // Party Ring Bonus
        mplew.writeInt(cakeVSPie); // Cake vs Pie Bonus
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getShowExpGain(int gain, int equip, boolean inChat, boolean white) {
        return showExpType(gain, equip, inChat, white, 0);
    }

    public static MaplePacket getBonus3rdMoExp(int gain) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3); // (3,exp),(4,fame), (5,mesos),(6,guildpoints)
        mplew.write(1); // 1 = white, 0 = yellow..
        mplew.writeInt(gain);
        mplew.write(0);
        mplew.writeInt((int) (gain * .50)); // Bonus Event
        mplew.write(50); // "A bonus EXP (amount)% is awarded for every 3rd monster defeated".
        mplew.write(0);
        mplew.writeInt(0); // Wedding Bonus
        mplew.write(0);
        mplew.writeInt(0); // Party Bonus
        mplew.writeInt(0); // Equip Item Bonus
        mplew.writeInt(0); // Internet Cafe Bonus
        mplew.writeInt(0); // Rainbow Week Bonus
        mplew.writeInt(0); // Party Ring Bonus
        mplew.writeInt(0); // Cake vs Pie Bonus
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getLionKingBonus(int exp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3); // (3,exp),(4,fame), (5,mesos),(6,guildpoints)
        mplew.write(0); // 1 = white, 0 = yellow..
        mplew.writeInt(exp);
        mplew.write(0);
        mplew.writeInt(exp / 2); // Bonus Event
        mplew.write(50); // "A bonus EXP (amount)% is awarded for every 3rd monster defeated".
        mplew.write(0);
        mplew.writeInt(0); // Wedding Bonus
        mplew.write(0);
        mplew.writeInt(0); // Party Bonus
        mplew.writeInt(0); // Equip Item Bonus
        mplew.writeInt(0); // Internet Cafe Bonus
        mplew.writeInt(0); // Rainbow Week Bonus
        mplew.writeInt(0); // Party Ring Bonus
        mplew.writeInt(0); // Cake vs Pie Bonus
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket sendHealExp(int gain) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3); // (3,exp),(4,fame), (5,mesos),(6,guildpoints)
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(gain); // Bonus Event
        mplew.write(0); // "A bonus EXP (amount)% is awarded for every 3rd monster defeated".
        mplew.write(0);
        mplew.writeInt(0); // Wedding Bonus
        mplew.write(0);
        mplew.writeInt(0); // Party Bonus
        mplew.writeInt(0); // Equip Item Bonus
        mplew.writeInt(0); // Internet Cafe Bonus
        mplew.writeInt(0); // Rainbow Week Bonus
        mplew.writeInt(0); // Party Ring Bonus
        mplew.writeInt(0); // Cake vs Pie Bonus
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a fame gain.
     *
     * @param gain How many fame gained.
     * @return The meso gain packet.
     */
    public static MaplePacket getShowFameGain(int gain) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(4);
        mplew.writeInt(gain);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @return The meso gain packet.
     */
    public static MaplePacket getShowMesoGain(int gain) {
        return getShowMesoGain(gain, false);
    }

    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @param inChat Show in the chat window?
     * @return The meso gain packet.
     */
    public static MaplePacket getShowMesoGain(int gain, boolean inChat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            mplew.write(0);
            mplew.writeShort(1); //v83
        } else {
            mplew.write(5);
        }
        mplew.writeInt(gain);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a item gain.
     *
     * @param itemId The ID of the item gained.
     * @param quantity How many items gained.
     * @return The item gain packet.
     */
    public static MaplePacket getShowItemGain(int itemId, short quantity) {
        return getShowItemGain(itemId, quantity, false);
    }

    /**
     * Gets a packet telling the client to show an item gain.
     *
     * @param itemId The ID of the item gained.
     * @param quantity The number of items gained.
     * @param inChat Show in the chat window?
     * @return The item gain packet.
     */
    public static MaplePacket getShowItemGain(int itemId, short quantity, boolean inChat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (inChat) {
            mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(3);
            mplew.write(1);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
        } else {
            mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
            mplew.writeShort(0);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket killMonster(int oid, boolean animation) {
        return killMonster(oid, animation ? 1 : 0);
    }

    /**
     * Gets a packet telling the client that a monster was killed.
     *
     * @param oid The objectID of the killed monster.
     * @param animation 0 = dissapear, 1 = fade out, 2+ = special
     * @return The kill monster packet.
     */
    public static MaplePacket killMonster(int oid, int animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.KILL_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(animation);
        mplew.write(animation); // Not a boolean, really an int type
        return mplew.getPacket();
    }

    //broadcastMessage(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 0));
    public static MaplePacket dropItemFromMapObject(MapleMapItem drop, Point pos, Point dropPos, byte mod) {
        return dropItemFromMapObjectInternal(drop.getItemId(), drop.getObjectId(), drop.getDropper().getObjectId(), drop.getOwner(), pos, dropPos, mod, false, (byte) drop.getDropType());
    }

    public static MaplePacket dropItemFromMapObjectMeso(MapleMapItem drop, Point pos, Point dropPos, byte mod) {
        return dropItemFromMapObjectInternal(drop.getItemId(), drop.getObjectId(), drop.getDropper().getObjectId(), drop.getOwner(), pos, dropPos, mod, true, (byte) drop.getDropType());
    }

    public static MaplePacket dropItemFromMapObjectInternal(int itemid, int itemoid, int dropperoid, int ownerid, Point dropfrom, Point dropto, byte mod, boolean mesos, byte type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        mplew.write(mod);
        mplew.writeInt(itemoid);
        mplew.write(mesos ? 1 : 0); // 1 = mesos, 0 =item
        mplew.writeInt(itemid);
        mplew.writeInt(ownerid); // owner charid
        mplew.write(mesos ? 4 : type); //<-----  0 = timeout for anyone who isn't the owner (like what happens with normal drops), 1 = timeout for owner's party, 2 = FFA (Free for all), 3 = explosive/FFA
        mplew.writeShort(dropto.x);
        mplew.writeShort(dropto.y);
        mplew.writeInt(ownerid); // drop owner, 0 for bosses
        if (mod != 2) {
            mplew.writeShort(dropfrom.x);
            mplew.writeShort(dropfrom.y);
            mplew.writeShort(0);
        }
        if (!mesos) {
            mplew.write(0);
            mplew.write(ITEM_MAGIC);
            addExpirationTime(mplew, System.currentTimeMillis(), false);
        }
        mplew.write(1); //pet EQP pickup
        mplew.write(1); //pet EQP pickup enable
        return mplew.getPacket();
    }

    private static void addExpirationTimeMeso(MaplePacketLittleEndianWriter mplew, long time, boolean addzero) {
        if (addzero) {
            mplew.write(0);
        }
        mplew.write(ITEM_MAGIC);
        if (time == -1) {
            mplew.writeInt(400967355);
            mplew.write(2);
        } else {
            mplew.writeInt(getItemTimestamp(time));
            mplew.write(1);
        }
    }

    /**
     * Gets a packet spawning a player as a mapobject to other clients.
     *
     * @param chr The character to spawn to other clients.
     * @return The spawn player packet.
     */
    public static MaplePacket spawnPlayerMapobject(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_PLAYER.getValue());

        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel()); //v83
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeShort(0);
        if (chr.getGuildId() < 1) { // for now
            mplew.writeMapleAsciiString("");
            mplew.write(new byte[6]);
        } else {
            MapleGuildSummary gs = chr.getClient().getChannelServer().getGuildSummary(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
                mplew.writeShort(gs.getLogoBG());
                mplew.write(gs.getLogoBGColor());
                mplew.writeShort(gs.getLogo());
                mplew.write(gs.getLogoColor());
            } else {
                mplew.writeMapleAsciiString("");
                mplew.write(new byte[6]);
            }
        }
        mplew.write(3); // v97
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0xF8);

        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            mplew.writeInt(2);
        } else {
            mplew.writeInt(0);
        }
        long buffmask = 0;
        Integer buffvalue = null;
        if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null || chr.isHidden()) {
            buffmask |= MapleBuffStat.DARKSIGHT.getValue();
        }
        //    if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
        //      buffmask |= MapleBuffStat.COMBO.getValue();
        //    buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue());
        //}
        if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
            buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
            buffmask |= MapleBuffStat.SOULARROW.getValue();
        }
        /*   if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
        buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MORPH).intValue());
        }
        if (chr.getBuffedValue(MapleBuffStat.ENERGY_CHARGE) != null) {
        buffmask |= MapleBuffStat.ENERGY_CHARGE.getValue();
        buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.ENERGY_CHARGE).intValue());
        }//FIX THIS^*/
        mplew.writeInt((int) ((buffmask >> 32) & 0xffffffffL));
        if (buffvalue != null) {
            //    if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) { //TEST
            //      mplew.writeShort(buffvalue);
            //    } else {
            mplew.write(buffvalue.byteValue());
            //    }
        }
        mplew.writeInt((int) (buffmask & 0xffffffffL));
        int CHAR_MAGIC_SPAWN = Randomizer.nextInt(5000000);
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);//v74
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0);
        mplew.write(0);
        if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null && chr.getMount() != null) {
            mplew.writeInt(chr.getMount().getItemId());
            mplew.writeInt(chr.getMount().getSkillId()); // TODO: mechanic support :o
        } else {
            mplew.writeLong(0);
        }
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeShort(chr.getJob().getId());
        addCharLook(mplew, chr, false); // 31 old; 28 b4 writePos
        mplew.writeLong(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0); // item effect
        mplew.writeInt(chr.getChair()); // chair
        mplew.writePos(chr.getPosition());
        mplew.write(chr.getStance());
        mplew.writeShort(0); // fh?
        mplew.writeShort(0);
        if (chr.getMount() == null) {
            mplew.writeInt(1); // mob level
            mplew.writeLong(0); // mob exp + tiredness
        } else {
            mplew.writeInt(chr.getMount().getLevel());
            mplew.writeInt(chr.getMount().getExp());
            mplew.writeInt(chr.getMount().getTiredness());
        }
        mplew.write(0); // announce box, our method is hackish
        if (chr.getChalkboard() != null) {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getChalkboard());
        } else {
            mplew.write(0);
        }
        addRingLook(mplew, chr, true);
        addRingLook(mplew, chr, false);
        addMarriageRingLook(mplew, chr);
        mplew.writeShort(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    private static void addRemainingSpData(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        boolean hasSp = chr.getRemainingSp() > 0;
        int jobid = chr.getJob().getId();
        if (jobid / 1000 != 3 && jobid / 100 != 22 && jobid != 2001) {
            mplew.writeShort(chr.getRemainingSp());
        } else {
            mplew.write(hasSp ? 1 : 0);
            if (hasSp) {
                mplew.write(1); // TODO: Attach advancement id w/ jobid. (SP Table)
                mplew.write(chr.getRemainingSp());
            }
        }
    }

    private static void addRingLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean crush) {
        List<MapleRing> rings;
        if (crush) {
            rings = chr.getCrushRings();
        } else {
            rings = chr.getFriendshipRings();
        }
        boolean yes = false;
        for (MapleRing ring : rings) {
            if (ring.equipped()) {
                if (yes == false) {
                    yes = true;
                    mplew.write(1);
                }
                mplew.writeInt(ring.getRingId());
                mplew.writeInt(0);
                mplew.writeInt(ring.getPartnerRingId());
                mplew.writeInt(0);
                mplew.writeInt(ring.getItemId());
            }
        }
        if (yes == false) {
            mplew.write(0);
        }
    }

    private static void addMarriageRingLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        if (chr.getMarriageRing() != null && !chr.getMarriageRing().equipped()) {
            mplew.write(0);
            return;
        }
        mplew.write(chr.getMarriageRing() != null ? 1 : 0);
        if (chr.getMarriageRing() != null) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(chr.getMarriageRing().getPartnerChrId());
            mplew.writeInt(chr.getMarriageRing().getRingId());
        }
    }

    /**
     * Adds a announcement box to an existing MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWriter to add an announcement box to.
     * @param shop The shop to announce.
     */
    private static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MaplePlayerShop shop, int availability) {
        mplew.write(4);
        mplew.writeInt(shop.getObjectId());
        mplew.writeMapleAsciiString(shop.getDescription());
        mplew.write(0);
        mplew.write(0);
        mplew.write(1);
        mplew.write(availability);
        mplew.write(0);
    }

    private static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MapleMiniGame game, int gametype, int type, int ammount, int joinable) {
        mplew.write(gametype);
        mplew.writeInt(game.getObjectId()); // gameid/shopid
        mplew.writeMapleAsciiString(game.getDescription()); // desc
        mplew.write(0);
        mplew.write(type);
        mplew.write(ammount);
        mplew.write(2);
        mplew.write(joinable);
    }

    public static MaplePacket facialExpression(MapleCharacter from, int expression) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
        mplew.writeShort(SendOpcode.FACIAL_EXPRESSION.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(expression);
        mplew.writeInt(-1); // v88
        mplew.write(0); // v88
        return mplew.getPacket();
    }

    private static void serializeMovementList(LittleEndianWriter lew, List<LifeMovementFragment> moves) {
        lew.write(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(lew);
        }
    }

    public static MaplePacket movePlayer(int cid, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MOVE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(0);
        mplew.writeInt(0);
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MOVE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.writePos(startPos);
        mplew.writeInt(0);
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket moveMonster(int useskill, int skill, int skill_1, int skill_2, int skill_3, int oid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (startPos.x == 0 && startPos.y == 0) {
            return enableActions();
        }
        mplew.writeShort(SendOpcode.MOVE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.writeShort(0);
        mplew.write(useskill);
        if (useskill > 0) {
            mplew.write(skill);
            mplew.write(skill_1);
            mplew.write(skill_2);
            mplew.write(skill_3);
        } else {
            mplew.writeInt(0);
        }
        mplew.write(0);
        mplew.writeLong(0);
        mplew.writeShort(startPos.x);
        mplew.writeShort(startPos.y);
        mplew.writeInt(0);
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket movePokemon(int oid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (startPos.x == 0 && startPos.y == 0) {
            return enableActions();
        }
        mplew.writeShort(SendOpcode.MOVE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.writeShort(0);
        mplew.write(0); // using skill
        mplew.write(0); // skill1
        mplew.write(0); // skill2
        mplew.write(0); // skill3
        mplew.write(0); //skill4
        mplew.write(0);
        mplew.writeLong(0);
        mplew.writeShort(startPos.x);
        mplew.writeShort(startPos.y);
        mplew.writeInt(0);
        // blocking all movement packets that a monster CANNOT use.
        for (LifeMovementFragment move : moves) {
            if (move.getType() != 0 && move.getType() != 5 && move.getType() != 14 && move.getType() != 17 && move.getType() != 4) {
                moves.remove(move);
            }
        }
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket pokemonAttack(int oid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (startPos.x == 0 && startPos.y == 0) {
            return enableActions();
        }
        mplew.writeShort(SendOpcode.MOVE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.writeShort(0);
        mplew.write(1); // using skill
        mplew.write(0); // skill1
        mplew.write(0); // skill2
        mplew.write(0); // skill3
        mplew.write(0); //skill4
        mplew.write(0);
        mplew.writeLong(0);
        mplew.writeShort(startPos.x);
        mplew.writeShort(startPos.y);
        mplew.writeInt(0);
        // blocking all movement packets that a monster CANNOT use.
        for (LifeMovementFragment move : moves) {
            if (move.getType() != 0 && move.getType() != 5 && move.getType() != 14 && move.getType() != 17 && move.getType() != 20 && move.getType() != 4) {
                moves.remove(move);
            }
        }
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket summonAttack(int cid, int summonSkillId, byte direction, List<SummonAttackEntry> allDamage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SUMMON_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(direction);
        mplew.write(4);
        mplew.write(allDamage.size());
        for (SummonAttackEntry attackEntry : allDamage) {
            mplew.writeInt(attackEntry.getMonsterOid()); // oid
            mplew.write(7); // who knows
            mplew.writeInt(attackEntry.getDamage()); // damage
        }
        // mplew.write(0); // v97
        return mplew.getPacket();
    }

    public static MaplePacket moveDragon(int cid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MOVE_DRAGON.getValue());
        mplew.writeInt(cid);
        mplew.writeShort(startPos.x);
        mplew.writeShort(startPos.y);
        mplew.writeInt(0);
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket closeRangeAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, List<Pair<Integer, List<Integer>>> damage, int speed, int direction, int display) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CLOSE_RANGE_ATTACK.getValue());
        addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, damage, speed, direction, display);
        return mplew.getPacket();
    }

    public static MaplePacket rangedAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, int projectile, List<Pair<Integer, List<Integer>>> damage, int speed, int direction, int display) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.RANGED_ATTACK.getValue());
        addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, projectile, damage, speed, direction, display);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket magicAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, List<Pair<Integer, List<Integer>>> damage, int charge, int speed, int direction, int display) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAGIC_ATTACK.getValue());
        addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, damage, speed, direction, display);
        if (charge != -1) {
            mplew.writeInt(charge);
        }
        mplew.writeLong(0);
        return mplew.getPacket();
    }

    private static void addAttackBody(LittleEndianWriter lew, MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, int projectile, List<Pair<Integer, List<Integer>>> damage, int speed, int direction, int display) {
        lew.writeInt(chr.getId());
        lew.write(numAttackedAndDamage);
        lew.write(10); // level..
        if (skill > 0) {
            lew.write(skilllevel);
            lew.writeInt(skill);
        } else {
            lew.write(0);
        }
        lew.write(display);
        lew.write(direction);
        lew.write(stance);
        lew.write(speed);
        lew.write(0x32);
        lew.writeInt(projectile);
        for (Pair<Integer, List<Integer>> oned : damage) {
            if (oned.getRight() != null) {
                lew.writeInt(oned.getLeft().intValue());
                lew.write(0xFF);
                if (skill == 4211006) {
                    lew.write(oned.getRight().size());
                }
                for (Integer eachd : oned.getRight()) {
                    if (skill != 4211006) {
                        lew.write(0);
                    }
                    lew.writeInt(eachd.intValue());
                }
            }
        }
    }

    private static int doubleToShortBits(double d) {
        return (int) (Double.doubleToLongBits(d) >> 48);
    }
//

    public static MaplePacket getNPCShop(MapleClient c, int sid, List<MapleShopItem> items) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.OPEN_NPC_SHOP.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(items.size() + 1 + 1); // item count
        for (MapleShopItem item : items) {
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getPrice()); //Mesos
            mplew.writeInt(item.getPrice() == 0 ? item.getPitch() : 0); // perf pitch :)
            mplew.write0(8); //Item flags / expire time or something
            mplew.writeInt(0); // v88 int added here
            mplew.write(0); // v87 1 byte added here
            if (!ItemConstants.isRechargable(item.getItemId())) {
                mplew.writeShort(1); // stacksize o.o
                mplew.writeShort(item.getBuyable() * 2);
            } else {
                mplew.writeShort(0);
                mplew.writeInt(0);
                mplew.writeShort(doubleToShortBits(ii.getPrice(item.getItemId())));
                mplew.writeShort(ii.getSlotMax(c, item.getItemId()));
            }
        }
        // adding magnifying glass to all shops..
        mplew.writeInt(2460003); // premium
        mplew.writeLong(1000 * 50); // price
        mplew.writeLong(0); // unknown
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeShort(1);
        mplew.writeShort(100);
        // end magnifying..
        // adding potential items
        mplew.writeInt(2049400);
        mplew.writeLong(1000 * 1000 * 10); // 10m
        mplew.writeLong(0); // item flags/expiration
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeShort(1);
        mplew.writeShort(1);
        // end potential scroll
        // starting miracle cube? (nahh..)
        return mplew.getPacket();
    }

    /* 00 = /
     * 01 = You don't have enough in stock
     * 02 = You do not have enough mesos
     * 03 = Please check if your inventory is full or not
     * 05 = You don't have enough in stock
     * 06 = Due to an error, the trade did not happen
     * 07 = Due to an error, the trade did not happen
     * 08 = /
     * 0D = You need more items
     * 0E = CRASH; LENGTH NEEDS TO BE LONGER :O
     */
    public static MaplePacket shopTransaction(byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        mplew.write(code);
        return mplew.getPacket();
    }

    public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item, boolean hasPot) {
        return addInventorySlot(type, item, false, hasPot);
    }
//00 01 01 02 02 00 53 00

    public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop, boolean hasPot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.writeShort(1); // add mode
        mplew.write(type.equals(MapleInventoryType.EQUIPPED) ? 1 : type.getType()); // iv type
        mplew.writeShort(item.getPosition()); // slot id
        addItemInfo(mplew, item, true);
        return mplew.getPacket();
    }

    public static MaplePacket updateInventorySlot(MapleInventoryType type, IItem item) {
        return updateInventorySlot(type, item, false);
    }
    //00 01 01 02 02 00 53 00

    public static MaplePacket updateEquipSlot(IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0);
        mplew.write(2);
        mplew.write(3);
        mplew.write(1);
        mplew.writeShort(item.getPosition());
        mplew.write(0);
        mplew.write(item.getType());
        mplew.writeShort(item.getPosition());
        addItemInfo(mplew, item, true);
        mplew.writeMapleAsciiString("");
        return mplew.getPacket();
    }

//[00] [01 01] [02] [02] 0[] [53 00]
    public static MaplePacket updateInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        if (fromDrop) {
            mplew.write(1);
        } else {
            mplew.write(0);
        }
        mplew.write(HexTool.getByteArrayFromHexString("01 01")); // update
        mplew.write(type.getType()); // iv type
        mplew.write(item.getPosition()); // slot id
        mplew.write(0); // ?
        mplew.writeShort(item.getQuantity());
        return mplew.getPacket();
    }

    public static MaplePacket updateInventorySlotLimit(int type, int newLimit) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_INVENTORY_SLOTS.getValue());
        mplew.write(type);
        mplew.write(newLimit);
        return mplew.getPacket();
    }

    public static MaplePacket moveInventoryItem(MapleInventoryType type, byte src, byte dst) {
        return moveInventoryItem(type, src, dst, (byte) -1);
    }
//00 01 01 02 02 00 53 00

    public static MaplePacket moveInventoryItem(MapleInventoryType type, byte src, byte dst, byte equipIndicator) {
        // 1D 00 01 01 02 00 F5 FF 01 00 01
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(new byte[]{1, 1, 2});
        mplew.write(type.getType()); // iv type
        mplew.writeShort(src);
        mplew.writeShort(dst);
        if (equipIndicator != -1) {
            mplew.write(equipIndicator);
        }
        return mplew.getPacket();
    }

    public static MaplePacket moveAndMergeInventoryItem(MapleInventoryType type, byte src, byte dst, short total) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(new byte[]{1, 2, 3});
        mplew.write(type.getType()); // iv type
        mplew.writeShort(src);
        mplew.write(1); // merge mode?
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(total);
        return mplew.getPacket();
    }

    public static MaplePacket moveAndMergeWithRestInventoryItem(MapleInventoryType type, byte src, byte dst, short srcQ, short dstQ) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(new byte[]{1, 2, 1});
        mplew.write(type.getType()); // iv type
        mplew.writeShort(src);
        mplew.writeShort(srcQ);
        mplew.write(1);
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(dstQ);
        return mplew.getPacket();
    }

    public static MaplePacket clearInventoryItem(MapleInventoryType type, byte slot, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(new byte[]{1, 3});
        mplew.write(type.equals(MapleInventoryType.EQUIPPED) ? 1 : type.getType()); // iv type
        mplew.writeShort(slot);
        if (!fromDrop) {
            mplew.write(2);
        }
        return mplew.getPacket();
    }

    public static MaplePacket scrolledItem(IItem scroll, IItem item, boolean destroyed, boolean potential) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1); // fromdrop always true
        mplew.write(destroyed ? 2 : 3);
        mplew.write(scroll.getQuantity() > 0 && !potential ? 1 : 3);
        mplew.write(MapleInventoryType.USE.getType());
        mplew.writeShort(scroll.getPosition());
        if (scroll.getQuantity() > 0 && !potential) {
            mplew.writeShort(scroll.getQuantity());
        }
        mplew.write(3);
        if (!destroyed) {
            mplew.write(MapleInventoryType.EQUIP.getType());
            mplew.writeShort(item.getPosition());
            mplew.write(0);
        }
        mplew.write(MapleInventoryType.EQUIP.getType());
        mplew.writeShort(item.getPosition());
        if (!destroyed) {
            addItemInfo(mplew, item, true);
        }
        mplew.write(potential ? 4 : 1);
        return mplew.getPacket();
    }

    public static MaplePacket getScrollEffect(int chr, ScrollResult scrollSuccess, boolean legendarySpirit) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_SCROLL_EFFECT.getValue());
        mplew.writeInt(chr);
        switch (scrollSuccess) {
            case SUCCESS:
                mplew.writeShort(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            case FAIL:
                mplew.writeShort(0);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            case CURSE:
                mplew.write(0);
                mplew.write(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
        }
        mplew.writeInt(1); // big bang
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket removePlayerFromMap(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        mplew.writeInt(cid);
        return mplew.getPacket();
    }

    /**
     * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/>
     * 4 - explode<br/> cid is ignored for 0 and 1
     *
     * @param oid
     * @param animation
     * @param cid
     * @return
     */
    public static MaplePacket removeItemFromMap(int oid, int animation, int cid) {
        return removeItemFromMap(oid, animation, cid, false, 0);
    }

    /**
     * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/>
     * 4 - explode<br/> cid is ignored for 0 and 1.<br /><br />Flagging pet
     * as true will make a pet pick up the item.
     *
     * @param oid
     * @param animation
     * @param cid
     * @param pet
     * @param slot
     * @return
     */
    public static MaplePacket removeItemFromMap(int oid, int animation, int cid, boolean pet, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(animation); // expire
        mplew.writeInt(oid);
        if (animation >= 2) {
            mplew.writeInt(cid);
            if (pet) {
                mplew.write(slot);
                mplew.writeLong(0); //safety pending proper cap
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateContribution(MapleCharacter mc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x48);
        mplew.writeInt(mc.getGuildId());
        mplew.writeInt(mc.getId());
        //mplew.writeInt(mc.getContribution()); // might need to add this in yourself
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket updateCharLook(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        addCharLook(mplew, chr, false);
        addRingLook(mplew, chr, true);
        addRingLook(mplew, chr, false);
        addMarriageRingLook(mplew, chr);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

//[1C 00] [01 01 03] [02] [03 00]
    public static MaplePacket dropInventoryItem(MapleInventoryType type, short src) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(new byte[]{1, 1, 3});
        mplew.write(type.getType());
        mplew.writeShort(src);
        if (src < 0) {
            mplew.write(1);
        }
        return mplew.getPacket();
    }

    public static MaplePacket dropInventoryItemUpdate(MapleInventoryType type, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(new byte[]{1, 1, 1});
        mplew.write(type.getType());
        mplew.writeShort(item.getPosition());
        mplew.writeShort(item.getQuantity());
        return mplew.getPacket();
    }

    public static MaplePacket damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, int direction, boolean pgmr, int pgmr_1, boolean is_pg, int oid, int pos_x, int pos_y) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.write(skill);
        mplew.writeInt(damage);
        mplew.writeInt(monsteridfrom);
        mplew.write(direction);
        if (pgmr) {
            mplew.write(0x32);//new
            mplew.write(pgmr_1);
            mplew.write(is_pg ? 1 : 0);
            mplew.writeInt(oid);
            mplew.write(7);
            mplew.writeShort(pos_x);
            mplew.writeShort(pos_y);
        } else {
            mplew.writeShort(0);
        }
        mplew.write(0); // stance :O
        mplew.writeInt(damage);
        if (fake > 0) {
            mplew.writeInt(fake);
        }
        return mplew.getPacket();
    }

    public static MaplePacket charNameResponse(String charname, boolean nameUsed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHAR_NAME_RESPONSE.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(nameUsed ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket addNewCharEntry(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(0);
        addCharEntry(mplew, chr, false);
        return mplew.getPacket();
    }

    /**
     * state 0 = del ok state 12 = invalid bday state 14 = incorrect pic
     *
     * @param cid
     * @param state
     * @return
     */
    public static MaplePacket deleteCharResponse(int cid, int state) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DELETE_CHAR_RESPONSE.getValue());
        mplew.writeInt(cid);
        mplew.write(state);
        return mplew.getPacket();
    }

    public static MaplePacket enableRecommended(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ENABLE_RECOMMENDED.getValue());
        mplew.writeInt(enable ? 3 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket sendRecommended(int world, String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SEND_RECOMMENDED.getValue());
        mplew.write(world);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }

    /**
     * 
     * @param chr
     * @param isSelf
     * @return
     */
    public static MaplePacket charInfo(MapleCharacter chr) {
        //3D 00 0A 43 01 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHAR_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob().getId());
        mplew.writeShort(chr.getFame());
        mplew.write(chr.getMarriageRing() != null ? 1 : 0);
        String guildName = "";
        String allianceName = "";
        MapleGuildSummary gs = chr.getClient().getChannelServer().getGuildSummary(chr.getGuildId());
        if (chr.getGuildId() > 0 && gs != null) {
            guildName = gs.getName();
            try {
                MapleAlliance alliance = chr.getClient().getChannelServer().getWorldInterface().getAlliance(gs.getAllianceId());
                if (alliance != null) {
                    allianceName = alliance.getName();
                }
            } catch (RemoteException re) {
                chr.getClient().getChannelServer().reconnectWorld();
            }
        }
        mplew.writeMapleAsciiString(guildName);
        mplew.writeMapleAsciiString(allianceName);
        mplew.write(0);
        MaplePet[] pets = chr.getPets();
        IItem inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -114);
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                mplew.write(pets[i].getUniqueId());
                mplew.writeInt(pets[i].getItemId()); // petid
                mplew.writeMapleAsciiString(pets[i].getName());
                mplew.write(pets[i].getLevel()); // pet level
                mplew.writeShort(pets[i].getCloseness()); // pet closeness
                mplew.write(pets[i].getFullness()); // pet fullness
                mplew.writeShort(0);
                mplew.writeInt(inv != null ? inv.getItemId() : 0);
            }
        }
        mplew.write(0); //end of pets
        if (chr.getMount() != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
            mplew.write(chr.getMount().getId()); //mount
            mplew.writeInt(chr.getMount().getLevel()); //level
            mplew.writeInt(chr.getMount().getExp()); //exp
            mplew.writeInt(chr.getMount().getTiredness()); //tiredness
        } else {
            mplew.write(0);
        }
        mplew.write(chr.getCashShop().getWishList().size());
        for (int sn : chr.getCashShop().getWishList()) {
            mplew.writeInt(sn);
        }
        mplew.writeInt(chr.getMonsterBook().getBookLevel());
        mplew.writeInt(chr.getMonsterBook().getNormalCard());
        mplew.writeInt(chr.getMonsterBook().getSpecialCard());
        mplew.writeInt(chr.getMonsterBook().getTotalCards());
        mplew.writeInt(chr.getMonsterBookCover() > 0 ? MapleItemInformationProvider.getInstance().getCardMobId(chr.getMonsterBookCover()) : 0);
        IItem medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -49);
        if (medal != null) {
            // mplew.writeInt(medal.getItemId());
            mplew.writeInt(0); // no medals for now 
        } else {
            mplew.writeInt(0);
        }
        ArrayList<Short> medalQuests = new ArrayList<Short>();
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        for (MapleQuestStatus q : completed) {
            if (q.getQuest().getId() >= 29000) { // && q.getQuest().getId() <= 29923
                medalQuests.add(q.getQuest().getId());
            }
        }

        Collections.sort(medalQuests);
        mplew.writeShort(medalQuests.size());
        for (Short s : medalQuests) {
            mplew.writeShort(s);
        }
        return mplew.getPacket();
    }

    /**
     * It is important that statups is in the correct order (see decleration
     * order in MapleBuffStat) since this method doesn't do automagical
     * reordering.
     *
     * @param buffid
     * @param bufflength
     * @param statups
     * @return
     */
    //[00 00 00 00] [00 00 00 00 00 00 00 04] [00 00 00 00 00 00 00 00] [0E 00] [CE 14 F9 01] [50 C3 00 00] [00] 00 32 00 00
    public static MaplePacket giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups, boolean mount, int sourceid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
        boolean special = false;
        writeLongMask(mplew, statups);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            if (statup.getLeft().equals(MapleBuffStat.MONSTER_RIDING)
                    || statup.getLeft().equals(MapleBuffStat.HOMING_BEACON)) {
                special = true;
            }
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeInt(buffid);
            if (statup.getLeft().equals(MapleBuffStat.PROTOTYPE)) {
                mplew.write(HexTool.getByteArrayFromHexString("FF FF FF 7F F4 01 AA 12 16 02 FF FF FF 7F 14 00 AA 12 16 02 FF FF FF 7F 90 01 AA 12 16 02 FF FF FF 7F 90 01 AA 12 16 02 FF FF FF 7F 00 00 F0 7A 1D 00 AA 12 16 02 00 00 00"));
            } else {
                mplew.writeInt(bufflength);
            }
        }
        mplew.write(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        //   mplew.writeInt(statups.get(0).getRight()); //Homing beacon ...
        if (special) {
            mplew.write0(3);
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups, boolean mount) {
        return giveBuff(buffid, bufflength, statups, mount, buffid);
    }

    /**
     * 
     * @param cid
     * @param statups
     * @param mount
     * @return
     */
    public static MaplePacket showMonsterRiding(int cid, MapleMount mount) { //Gtfo with this, this is just giveForeignBuff
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(0);
        if (mount.getSkillId() == Mechanic.PROTOTYPE) {
            mplew.write(HexTool.getByteArrayFromHexString("03 00 00 40 00 00 00 E0"));
        } else {
            mplew.writeLong(MapleBuffStat.MONSTER_RIDING.getValue());
        }
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(mount.getItemId());
        mplew.writeInt(mount.getSkillId());
        mplew.writeLong(0);
        return mplew.getPacket();
    }

    //public static MaplePacket showLocalMount()
    /*        mplew.writeInt(cid);
    writeLongMask(mplew, statups);
    for (Pair<MapleBuffStat, Integer> statup : statups) {
    if (morph) {
    mplew.writeInt(statup.getRight().intValue());
    } else {
    mplew.writeShort(statup.getRight().shortValue());
    }
    }
    mplew.writeShort(0);
    mplew.write(0);*/
    /**
     *
     * @param c
     * @param quest
     * @return
     */
    public static MaplePacket forfeitQuest(short quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @return
     */
    public static MaplePacket completeQuest(short quest, long time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.write(HexTool.getByteArrayFromHexString("02 A0 67 B9 DA 69 3A C8 01"));
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @param npc
     * @param progress
     * @return
     */
    public static MaplePacket updateQuestInfo(short quest, int npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(8); //0x0A in v95
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket addQuestTimeLimit(short quest, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(6);
        mplew.writeShort(1);//Size but meh, when will there be 2 at the same time? And it won't even replace the old one :)
        mplew.writeShort(quest);
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static MaplePacket removeQuestTimeLimit(short quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(7);
        mplew.writeShort(1);//Position
        mplew.writeShort(quest);
        return mplew.getPacket();
    }

    public static MaplePacket updateQuest(short quest, String status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.write(1);
        mplew.writeMapleAsciiString(status);
        return mplew.getPacket();
    }

    private static <E extends LongValueHolder> long getLongMask(List<Pair<E, Integer>> statups) {
        long mask = 0;
        for (Pair<E, Integer> statup : statups) {
            mask |= statup.getLeft().getValue();
        }
        return mask;
    }

    private static <E extends LongValueHolder> long getLongMaskFromList(List<E> statups) {
        long mask = 0;
        for (E statup : statups) {
            mask |= statup.getValue();
        }
        return mask;
    }

    private static <E extends LongValueHolder> long getLongMaskD(List<Pair<MapleDisease, Integer>> statups) {
        long mask = 0;
        for (Pair<MapleDisease, Integer> statup : statups) {
            mask |= statup.getLeft().getValue();
        }
        return mask;
    }

    private static <E extends LongValueHolder> long getLongMaskFromListD(List<MapleDisease> statups) {
        long mask = 0;
        for (MapleDisease statup : statups) {
            mask |= statup.getValue();
        }
        return mask;
    }

    public static MaplePacket giveDebuff(List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /* mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
        long mask = getLongMaskD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (Pair<MapleDisease, Integer> statup : statups) {
        mplew.writeShort(statup.getRight().shortValue());
        mplew.writeShort(skill.getSkillId());
        mplew.writeShort(skill.getSkillLevel());
        mplew.writeInt((int) skill.getDuration());
        }
        mplew.writeShort(0); // ??? wk charges have 600 here o.o
        mplew.writeShort(900);//Delay
        mplew.write(1);*/
        return mplew.getPacket();
    }

    public static MaplePacket giveForeignDebuff(int cid, List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /*  mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMaskD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (int i = 0; i < statups.size(); i++) {
        mplew.writeShort(skill.getSkillId());
        mplew.writeShort(skill.getSkillLevel());
        }
        mplew.writeShort(0); // same as give_buff
        mplew.writeShort(900);//Delay*/
        return mplew.getPacket();
    }
// 

    public static MaplePacket cancelForeignDebuff(int cid, List<MapleDisease> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /*  mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMaskFromListD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        mplew.write(3);*/
        return mplew.getPacket();
    }

    private static boolean isFirstLong(List<MapleBuffStat> statups) {
        for (MapleBuffStat stat : statups) {
            if (stat.isFirst()) {
                return true;
            }
        }
        return false;
    }

    public static MaplePacket giveForeignBuff(int cid, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeLongMask(mplew, statups);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
        }
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeIntMaskFromList(mplew, statups);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    private static void writeIntMaskFromList(MaplePacketLittleEndianWriter mplew, List<MapleBuffStat> statups) {
        int[] buffs = {0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < statups.size(); i++) {
            buffs[i] |= statups.get(i).getValue();
        }
        for (int i : buffs) {
            mplew.writeInt(i);
        }
    }
    /*
     * [00 00 00 00] [00 C0 00 00 00 00 00 00] [00 00 00 00 00 00 00 00] 03
     */

    public static MaplePacket cancelBuff(List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CANCEL_BUFF.getValue());
        writeLongMaskFromList(mplew, statups);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    private static void writeLongMask(MaplePacketLittleEndianWriter mplew, List<Pair<MapleBuffStat, Integer>> statups) {
        long firstmask = 0;
        long secondmask = 0;
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            if (statup.getLeft().isFirst()) {
                firstmask |= statup.getLeft().getValue();
            } else {
                secondmask |= statup.getLeft().getValue();
            }
        }
        mplew.writeInt(0); // 0.96
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }

    /*  private static void writeLongMaskFromList(MaplePacketLittleEndianWriter mplew, List<MapleBuffStat> statups) {
    long firstmask = 0;
    long secondmask = 0;
    for (MapleBuffStat statup : statups) {
    if (statup.isFirst()) {
    firstmask |= statup.getValue();
    } else {
    secondmask |= statup.getValue();
    }
    }
    mplew.writeLong(firstmask);
    mplew.writeLong(secondmask);
    }
     */
    private static void writeLongMaskFromList(MaplePacketLittleEndianWriter mplew, List<MapleBuffStat> statups) {
        long firstmask = 0;
        long secondmask = 0;
        for (MapleBuffStat statup : statups) {
            if (statup.isFirst()) {
                firstmask |= statup.getValue();
            } else {
                secondmask |= statup.getValue();
            }
        }
        mplew.writeInt(0); // 0.96
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }

    public static MaplePacket cancelDebuff(List<MapleDisease> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(19);
        /*mplew.writeShort(SendOpcode.CANCEL_BUFF.getValue());
        mplew.writeLong(0);
        mplew.writeLong(getLongMaskFromListD(statups));
        mplew.write(0);*/
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopChat(MapleCharacter c, String chat, boolean owner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(owner ? 0 : 1);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopNewVisitor(MapleCharacter c, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopRemoveVisitor(int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        if (slot > 0) {
            mplew.write(slot);
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradePartnerAdd(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(1);
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeShort(c.getJob().getId());
        return mplew.getPacket();
    }

    public static MaplePacket getTradeInvite(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.INVITE.getCode());
        mplew.write(3);
        mplew.writeMapleAsciiString(c.getName());
        mplew.write(new byte[]{(byte) 0xB7, (byte) 0x50, 0, 0});
        return mplew.getPacket();
    }

    public static MaplePacket getTradeMesoSet(byte number, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SET_MESO.getCode());
        mplew.write(number);
        mplew.writeInt(meso);
        return mplew.getPacket();
    }

    public static MaplePacket getTradeItemAdd(byte number, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SET_ITEMS.getCode());
        mplew.write(number);
        mplew.write(item.getPosition());
        addItemInfo(mplew, item, true);
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopItemUpdate(MaplePlayerShop shop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
        mplew.write(shop.getItems().size());
        for (MaplePlayerShopItem item : shop.getItems()) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            addItemInfo(mplew, item.getItem(), true);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param shop
     * @param owner
     * @return
     */
    public static MaplePacket getPlayerShop(MapleClient c, MaplePlayerShop shop, boolean owner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(4);
        mplew.write(4);
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, shop.getOwner(), false);
        mplew.writeMapleAsciiString(shop.getOwner().getName());
        mplew.write(1);
        addCharLook(mplew, shop.getOwner(), false);
        mplew.writeMapleAsciiString(shop.getOwner().getName());
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(shop.getDescription());
        List<MaplePlayerShopItem> items = shop.getItems();
        mplew.write(0x10);
        mplew.write(items.size());
        for (MaplePlayerShopItem item : items) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            addItemInfo(mplew, item.getItem(), true);
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradeStart(MapleClient c, MapleTrade trade, byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(3);
        mplew.write(2);
        mplew.write(number);
        if (number == 1) {
            mplew.write(0);
            addCharLook(mplew, trade.getPartner().getChr(), true);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
            mplew.writeShort(c.getPlayer().getJob().getId());
        }
        mplew.write(number);
        addCharLook(mplew, c.getPlayer(), true);
        mplew.writeMapleAsciiString(c.getPlayer().getName());
        mplew.writeShort(c.getPlayer().getJob().getId());
        mplew.write(0xFF);
        return mplew.getPacket();
    }

    public static MaplePacket getTradeConfirmation() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CONFIRM.getCode());
        return mplew.getPacket();
    }

    public static MaplePacket getTradeCompletion(byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(number);
        mplew.write(7);
        return mplew.getPacket();
    }

    public static MaplePacket getTradeCancel(byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(number);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static MaplePacket addCharBox(MapleCharacter c, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getPlayerShop(), type);
        return mplew.getPacket();
    }

    public static MaplePacket removeCharBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * Possible values for <code>speaker</code>:<br>
     * 0: Npc talking  (left)<br>
     * 1: Npc talking (right)<br>
     * 2: Player talking (left)<br>
     * 3: Player talking (left)<br>
     *
     * @param npc Npcid
     * @param msgType
     * @param talk
     * @param endBytes
     * @param speaker
     * @return
     */
    public static MaplePacket getNPCTalk(int npc, byte msgType, String talk, String endBytes, int speaker) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(msgType);
        mplew.write(speaker); // 1, 0
        if (speaker == 5) {
            mplew.writeInt(0); // npcid
        }
        mplew.writeMapleAsciiString(talk);
        mplew.write(HexTool.getByteArrayFromHexString(endBytes));
        return mplew.getPacket();
    }

    public static MaplePacket getDimensionalMirror(String talk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(9010022);
        mplew.write(0x0E);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString(talk);
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkStyle(int npc, String talk, int styles[]) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(8); // 8 in v88, 9 in v99
        mplew.write(0);
        mplew.writeMapleAsciiString(talk);
        mplew.write(styles.length);
        for (int i = 0; i < styles.length; i++) {
            mplew.writeInt(styles[i]);
        }
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkNum(int npc, String talk, int def, int min, int max) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.writeShort(4); // +1 v88
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(def);
        mplew.writeInt(min);
        mplew.writeInt(max);
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkText(int npc, String talk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.writeShort(3); // let's asume +1 in v88
        mplew.writeMapleAsciiString(talk);
        mplew.write0(12);
        return mplew.getPacket();
    }

    public static MaplePacket showBuffeffect(int cid, int skillid, int effectid) {
        return showBuffeffect(cid, skillid, effectid, (byte) 3);
    }

    public static MaplePacket showBuffeffect(int cid, int skillid, int effectid, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effectid); //buff level
        mplew.writeInt(skillid);
        mplew.write(200); // not sure but seems like it
        mplew.write(1); // skillelvel
        mplew.write0(4);
        return mplew.getPacket();
    }

    public static MaplePacket showOwnBuffEffect(int skillid, int effectid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(0xA9);
        mplew.writeShort(1); // duration?
        return mplew.getPacket();
    }

    public static MaplePacket showOwnBerserk(int skilllevel, boolean Berserk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(1);
        mplew.writeInt(1320006);
        mplew.write(0xA9);
        mplew.write(skilllevel);
        mplew.write(Berserk ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket showBerserk(int cid, int skilllevel, boolean Berserk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(1);
        mplew.writeInt(1320006);
        mplew.write(0xA9);
        mplew.write(skilllevel);
        mplew.write(Berserk ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket updateSkill(int skillid, int level, int masterlevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_SKILLS.getValue());
        mplew.write(1);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        mplew.write(0);//*
        mplew.write(ITEM_MAGIC);//*
        addExpirationTime(mplew, 0, false);//*
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket getShowQuestCompletion(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_QUEST_COMPLETION.getValue());
        mplew.writeShort(id);
        return mplew.getPacket();
    }

    public static MaplePacket getKeymap(Map<Integer, MapleKeyBinding> keybindings) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.KEYMAP.getValue());
        mplew.write(0);
        for (int x = 0; x < 90; x++) {
            MapleKeyBinding binding = keybindings.get(Integer.valueOf(x));
            if (binding != null) {
                mplew.write(binding.getType());
                mplew.writeInt(binding.getAction());
            } else {
                mplew.write(0);
                mplew.writeInt(0);
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket getWhisper(String sender, int channel, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WHISPER.getValue());
        mplew.write(0x12);
        mplew.writeMapleAsciiString(sender);
        mplew.writeShort(channel - 1); // I guess this is the channel
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    /**
     *
     * @param target name of the target character
     * @param reply error code: 0x0 = cannot find char, 0x1 = success
     * @return the MaplePacket
     */
    public static MaplePacket getWhisperReply(String target, byte reply) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WHISPER.getValue());
        mplew.write(0x0A); // whisper?
        mplew.writeMapleAsciiString(target);
        mplew.write(reply);
        return mplew.getPacket();
    }

    public static MaplePacket getInventoryFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getShowInventoryFull() {
        return getShowInventoryStatus(0xff);
    }

    public static MaplePacket showItemUnavailable() {
        return getShowInventoryStatus(0xfe);
    }
// [26 00] [00] [00] 99 AB 1E 00 02 00 00 00 00 00 00 00 00 00 00 00

    public static MaplePacket getShowInventoryStatus(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(mode);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket getStorage(int npcId, byte slots, Collection<IItem> items, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x16);
        mplew.writeInt(npcId);
        mplew.write(slots);
        mplew.writeShort(0x7E);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        mplew.writeShort(0);
        mplew.write((byte) items.size());
        for (IItem item : items) {
            addItemInfo(mplew, item, true);
        }
        mplew.writeShort(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getStorageFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x11);
        return mplew.getPacket();
    }

    public static MaplePacket mesoStorage(byte slots, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x13);
        mplew.write(slots);
        mplew.writeShort(2);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        return mplew.getPacket();
    }

    public static MaplePacket storeStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.OPEN_STORAGE.getValue());
        mplew.write(0xD);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (IItem item : items) {
            addItemInfo(mplew, item, true);
        }
        return mplew.getPacket();
    }

    public static MaplePacket takeOutStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x9);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (IItem item : items) {
            addItemInfo(mplew, item, true);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param oid
     * @param remhppercentage
     * @return
     */
    public static MaplePacket showMonsterHP(int oid, int remhppercentage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_MONSTER_HP.getValue());
        mplew.writeInt(oid);
        mplew.write(remhppercentage);
        return mplew.getPacket();
    }

    public static MaplePacket showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BOSS_ENV.getValue());
        mplew.write(5);
        mplew.writeInt(oid);
        mplew.writeInt(currHP);
        mplew.writeInt(maxHP);
        mplew.write(tagColor);
        mplew.write(tagBgColor);
        // mplew.writeInt(0); // v0.97
        return mplew.getPacket();
    }

    public static MaplePacket giveFameResponse(int mode, String charname, int newfame) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(charname);
        mplew.write(mode);
        mplew.writeShort(newfame);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     * status can be: <br>
     * 0: ok, use giveFameResponse<br>
     * 1: the username is incorrectly entered<br>
     * 2: users under level 15 are unable to toggle with fame.<br>
     * 3: can't raise or drop fame anymore today.<br>
     * 4: can't raise or drop fame for this character for this month anymore.<br>
     * 5: received fame, use receiveFame()<br>
     * 6: level of fame neither has been raised nor dropped due to an unexpected
     * error
     *
     * @param status
     * @return
     */
    public static MaplePacket giveFameErrorResponse(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
        mplew.write(status);
        return mplew.getPacket();
    }

    public static MaplePacket receiveFame(int mode, String charnameFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
        mplew.write(5);
        mplew.writeMapleAsciiString(charnameFrom);
        mplew.write(mode);
        return mplew.getPacket();
    }
////[08] [46 06] [00 00] [FF C9 9A 3B] [FF C9 9A 3B] [00 00 00 00] [00 00 00 00]

    public static MaplePacket partyCreated(MapleParty party) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(8);
        mplew.writeShort(party.getId()); // partyid
        mplew.writeShort(0);
        mplew.write(CHAR_INFO_MAGIC);
        mplew.write(CHAR_INFO_MAGIC);
        mplew.writeInt(0);
        mplew.writeInt(0); // v88
        return mplew.getPacket();
    }

    public static MaplePacket partyInvite(MapleCharacter from) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(from.getParty().getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.writeInt(from.getLevel());
        mplew.writeInt(from.getJob().getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * 10: A beginner can't create a party.
     * 1/11/14/19: Your request for a party didn't work due to an unexpected error.
     * 13: You have yet to join a party.
     * 16: Already have joined a party.
     * 17: The party you're trying to join is already in full capacity.
     * 19: Unable to find the requested character in this channel.
     *
     * @param message
     * @return
     */
    public static MaplePacket partyStatusMessage(int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        return mplew.getPacket();
    }

    /**
     * 23: 'Char' have denied request to the party.
     *
     * @param message
     * @param charname
     * @return
     */
    public static MaplePacket partyStatusMessage(int message, String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        mplew.writeMapleAsciiString(charname);
        return mplew.getPacket();
    }

    private static void addPartyStatus(int forchannel, MapleParty party, LittleEndianWriter lew, boolean leaving) {
        List<MaplePartyCharacter> partymembers = new ArrayList<MaplePartyCharacter>(party.getMembers());
        while (partymembers.size() < 6) {
            partymembers.add(new MaplePartyCharacter());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeAsciiString(StringUtil.getRightPaddedStr(partychar.getName(), '\0', 13));
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getJobId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getLevel());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                lew.writeInt(partychar.getChannel() - 1);
            } else {
                lew.writeInt(-1);
            }
        }
        lew.writeInt(party.getLeader().getId());

        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel) {
                lew.writeInt(partychar.getMapId());
            } else {
                lew.writeInt(999999999);
            }
        }
        for (MaplePartyCharacter partyChar : partymembers) {
            lew.writeLong(0); // v0.97
        }
        lew.writeInt(0); // v0.97
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel && !leaving) {
                lew.writeInt(partychar.getDoorTown());
                lew.writeInt(partychar.getDoorTarget());
                lew.writeInt(partychar.getDoorPosition().x);
                lew.writeInt(partychar.getDoorPosition().y);
                lew.writeInt(0); // v0.97
            } else {
                lew.writeInt(999999999);
                lew.writeInt(999999999);
                lew.writeInt(0);
                lew.writeLong(-1);
            }
        }
        lew.writeInt(0); // v83+
        for (MaplePartyCharacter mpc : partymembers) {
            lew.writeInt(255);
        }
        for (int i = 0; i < 4; i++) {
            lew.writeLong(0);
        }
    }
//[0C] [E9 0F 00 00] [BF 7C 00 00] [00] [E9 0F 00 00]

    public static MaplePacket updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        switch (op) {
            case DISBAND:
            case EXPEL:
            case LEAVE:
                mplew.write(0xC);
                mplew.writeInt(party.getId());
                mplew.writeInt(target.getId());
                if (op == PartyOperation.DISBAND) {
                    mplew.write(0);
                    mplew.writeInt(party.getId());
                } else {
                    mplew.write(1);
                    mplew.write(op.equals(PartyOperation.EXPEL) ? 1 : 0);
                    mplew.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, mplew, false);
                }
                break;
            case JOIN:
                mplew.write(0xF);
                mplew.writeInt(party.getId());
                mplew.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, mplew, false);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                mplew.write(0x7);
                mplew.writeInt(party == null ? 0 : party.getId()); // we probably want to throw exception though if it is null
                addPartyStatus(forChannel, party, mplew, false);
                break;
            case CHANGE_LEADER:
                mplew.write(0x1A);
                mplew.writeInt(target.getId());
                mplew.write(1);
                break;
        }
        return mplew.getPacket();
    }

    public static MaplePacket partyPortal(int townId, int targetId, Point position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
        mplew.writeShort(0x23);
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        mplew.writePos(position);
        return mplew.getPacket();
    }
//[E5 7A 00 00] [2F 12 00 00] [2F 12 00 00]

    public static MaplePacket updatePartyMemberHP(int cid, int curhp, int maxhp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_PARTYMEMBER_HP.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(curhp);
        mplew.writeInt(maxhp);
        return mplew.getPacket();
    }

    /**
     * mode: 0 buddychat; 1 partychat; 2 guildchat
     *
     * @param name
     * @param chattext
     * @param mode
     * @return
     */
    public static MaplePacket multiChat(String name, String chattext, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MULTICHAT.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(chattext);
        return mplew.getPacket();
    }

    private static void writeIntMask(MaplePacketLittleEndianWriter mplew, Map<MonsterStatus, Integer> stats) {
        int firstmask = 0;
        int secondmask = 0;
        for (MonsterStatus stat : stats.keySet()) {
            if (stat.isFirst()) {
                firstmask |= stat.getValue();
            } else {
                secondmask |= stat.getValue();
            }
        }
        mplew.writeInt(firstmask);
        mplew.writeInt(secondmask);
    }

    private static void writeIntMask(MaplePacketLittleEndianWriter mplew, List<Pair<MapleBuffStat, Integer>> statups) {
        int firstmask = 0;
        int secondmask = 0;
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            if (statup.getLeft().isFirst()) {
                firstmask |= statup.getLeft().getValue();
            } else {
                secondmask |= statup.getLeft().getValue();
            }
        }
        mplew.writeInt(0);
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }

    public static MaplePacket applyMonsterStatus(final int oid, final MonsterStatusEffect mse) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(0);
        mplew.writeLong(0);
        writeIntMask(mplew, mse.getStati());
        for (Map.Entry<MonsterStatus, Integer> stat : mse.getStati().entrySet()) {
            mplew.writeShort(stat.getValue());
            if (mse.isMonsterSkill()) {
                mplew.writeShort(mse.getMobSkill().getSkillId());
                mplew.writeShort(mse.getMobSkill().getSkillLevel());
            } else {
                mplew.writeInt(mse.getSkill().getId());
            }
            mplew.writeShort(-1); // might actually be the buffTime but it's not displayed anywhere
        }
        mplew.writeShort(0);
        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket cancelMonsterStatus(int oid, Map<MonsterStatus, Integer> stats) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CANCEL_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        for (int i = 0; i < 6; i++) {
            mplew.writeInt(0);
        }
        int mask = 0;
        for (MonsterStatus stat : stats.keySet()) {
            mask |= stat.getValue();
        }
        mplew.writeInt(mask);
        mplew.write(1); // amount to cancel
        mplew.write(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket getClock(int time) { // time in seconds
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CLOCK.getValue());
        mplew.write(2); // clock type. if you send 3 here you have to send another byte (which does not matter at all) before the timestamp
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static MaplePacket getClockTime(int hour, int min, int sec) { // Current Time
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CLOCK.getValue());
        mplew.write(1); //Clock-Type
        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);
        return mplew.getPacket();
    }

    public static MaplePacket spawnMist(int oid, int ownerCid, int skill, int level, MapleMist mist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_MIST.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(mist.isMobMist() ? 0 : mist.isPoisonMist() ? 1 : 2);
        mplew.writeInt(ownerCid);
        mplew.writeInt(skill);
        mplew.write(level);
        mplew.writeShort(mist.getSkillDelay()); // Skill delay
        mplew.writeInt(mist.getBox().x);
        mplew.writeInt(mist.getBox().y);
        mplew.writeInt(mist.getBox().x + mist.getBox().width);
        mplew.writeInt(mist.getBox().y + mist.getBox().height);
        mplew.writeInt(0);
        mplew.writeInt(0); // v0.97 o-o
        return mplew.getPacket();
    }

    public static MaplePacket removeMist(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.REMOVE_MIST.getValue());
        mplew.writeInt(oid);
        return mplew.getPacket();
    }

    public static MaplePacket damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DAMAGE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(unkByte);
        mplew.writeInt(damage);
        mplew.writeInt(monsterIdFrom);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket damageMonster(int oid, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.writeInt(damage);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket healMonster(int oid, int heal) {
        return damageMonster(oid, -heal);
    }
    /*
     * [41 00] [07] [01] [9F 78 00 00] [52 61 7A 65 65
    6E 00 00 00 00 00 00 00] [00] [FF FF FF FF]
    [47 72 6F 75 70 20 55 6E 6B 6E 6F 77 6E]
    [00 00 00 00] [00 00 00 00]
     */

    public static MaplePacket updateBuddylist(Collection<BuddylistEntry> buddylist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(7);
        mplew.write(buddylist.size());
        for (BuddylistEntry buddy : buddylist) {
            mplew.writeInt(buddy.getCharacterId()); // cid
            mplew.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getName(), '\0', 13));
            if (buddy.isOnline() && buddy.isVisible()) {
                mplew.write(0);
                mplew.writeInt(buddy.getChannel() - 1); // offline = -1
            } else if (!buddy.isOnline()) {
                mplew.write(0);
                mplew.writeInt(-1);
            } else {
                mplew.write(2);
                mplew.writeInt(0x5B1C0000);
            }
            mplew.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getGroup(), '\0', 13));
            mplew.writeInt(0); //AccountID - BuddyListID (One UniqueID per account of buddylist)
        }
        for (int x = 0; x < buddylist.size(); x++) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket buddylistMessage(byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(message);
        return mplew.getPacket();
    }

    public static MaplePacket requestBuddylistAdd(int cidFrom, int cid, String nameFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        MapleCharacter chr = ChannelServer.getCharacterFromAllServers(cidFrom);
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(9);
        mplew.writeInt(cidFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(chr.getLevel());
        mplew.writeInt(chr.getJob().getId());
        mplew.writeInt(cidFrom);
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(nameFrom, '\0', 11));
        mplew.write(0x00); // v88
        mplew.write(0x82); // v88
        mplew.write(0x01);
        mplew.writeInt(0x10); // v88
        mplew.writeNullTerminatedAsciiString("Group Unknown");
        mplew.writeInt(cid);
        return mplew.getPacket();
    }

    public static MaplePacket updateBuddyChannel(int characterid, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(0x14);
        mplew.writeInt(characterid);
        mplew.write(0);
        mplew.writeInt(channel);
        return mplew.getPacket();
    }

    public static MaplePacket itemEffect(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_EFFECT.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static MaplePacket updateBuddyCapacity(int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
        mplew.write(0x15);
        mplew.write(capacity);
        return mplew.getPacket();
    }

    public static MaplePacket showChair(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_CHAIR.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static MaplePacket cancelChair(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CANCEL_CHAIR.getValue());
        if (id == -1) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeShort(id);
        }
        return mplew.getPacket();
    }

    // is there a way to spawn reactors non-animated?
    public static MaplePacket spawnReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();
        mplew.writeShort(SendOpcode.REACTOR_SPAWN.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.writeInt(reactor.getId());
        mplew.write(reactor.getState());
        mplew.writePos(pos);
        mplew.writeShort(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket triggerReactor(MapleReactor reactor, int stance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();
        mplew.writeShort(SendOpcode.REACTOR_HIT.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(pos);
        mplew.writeShort(stance);
        mplew.write(0);
        mplew.write(5); // frame delay, set to 5 since there doesn't appear to be a fixed formula for it
        return mplew.getPacket();
    }

    public static MaplePacket destroyReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();
        mplew.writeShort(SendOpcode.REACTOR_DESTROY.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(pos);
        return mplew.getPacket();
    }

    public static MaplePacket musicChange(String song) {
        return environmentChange(song, 6);
    }

    public static MaplePacket showEffect(String effect) {
        return environmentChange(effect, 3);
    }

    public static MaplePacket playSound(String sound) {
        return environmentChange(sound, 4);
    }

    public static MaplePacket environmentChange(String env, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BOSS_ENV.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(env);
        return mplew.getPacket();
    }

    public static MaplePacket startMapEffect(String msg, int itemid, boolean active) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAP_EFFECT.getValue());
        mplew.write(active ? 0 : 1);
        mplew.writeInt(itemid);
        if (active) {
            mplew.writeMapleAsciiString(msg);
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeMapEffect() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAP_EFFECT.getValue());
        mplew.write(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket mapEffect(String path) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAP_EFFECT.getValue());
        mplew.write(3);
        mplew.writeMapleAsciiString("BasicEff/");
        return mplew.getPacket();
    }

    public static MaplePacket mapSound(String path) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAP_EFFECT.getValue());
        mplew.write(4);
        mplew.writeMapleAsciiString(path);
        return mplew.getPacket();
    }

    public static MaplePacket showGuildInfo(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x20); //signature for showing guild info
        if (c == null) { //show empty guild (used for leaving, expelled)
            mplew.write(0);
            return mplew.getPacket();
        }
        MapleGuild g = c.getClient().getChannelServer().getGuild(c.getMGC());
        if (g == null) { //failed to read from DB - don't show a guild
            mplew.write(0);
            return mplew.getPacket();
        } else {
            c.setGuildRank(c.getGuildRank());
        }
        mplew.write(1); //bInGuild
        mplew.writeInt(g.getId());
        mplew.writeMapleAsciiString(g.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(g.getRankTitle(i));
        }
        Collection<MapleGuildCharacter> members = g.getMembers();
        mplew.write(members.size()); //then it is the size of all the members
        for (MapleGuildCharacter mgc : members) {//and each of their character ids o_O
            mplew.writeInt(mgc.getId());
        }
        for (MapleGuildCharacter mgc : members) {
            mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
            mplew.writeInt(mgc.getJobId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getGuildRank());
            mplew.writeInt(mgc.isOnline() ? 1 : 0);
            mplew.writeInt(mgc.getAllianceRank());
            mplew.writeInt(0); // guild contribution
        }
        mplew.writeInt(g.getCapacity());
        mplew.writeShort(g.getLogoBG());
        mplew.write(g.getLogoBGColor());
        mplew.writeShort(g.getLogo());
        mplew.write(g.getLogoColor());
        mplew.writeMapleAsciiString(g.getNotice());
        mplew.writeInt(g.getGP());
        mplew.writeInt(g.getGP());
        mplew.writeInt(g.getAllianceId());
        mplew.write(1); // guild level
        mplew.writeShort(0);
        mplew.writeShort(0); // guild skills size
        return mplew.getPacket();
    }

    public static MaplePacket guildMemberOnline(int gid, int cid, boolean bOnline) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x43);
        mplew.writeInt(gid);
        mplew.writeInt(cid);
        mplew.write(bOnline ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket guildInvite(int gid, int level, int jobid, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x05);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(name);
        mplew.writeInt(level);
        mplew.writeInt(jobid);
        return mplew.getPacket();
    }

    /**
     * 'Char' has denied your guild invitation.
     *
     * @param charname
     * @return
     */
    public static MaplePacket denyGuildInvitation(String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x37);
        mplew.writeMapleAsciiString(charname);
        return mplew.getPacket();
    }

    public static MaplePacket genericGuildMessage(byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(code);
        return mplew.getPacket();
    }

    public static MaplePacket newGuildMember(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x2D);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
        mplew.writeInt(mgc.getJobId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getGuildRank()); //should be always 5 but whatevs
        mplew.writeInt(mgc.isOnline() ? 1 : 0); //should always be 1 too
        mplew.writeInt(1); //? could be guild signature, but doesn't seem to matter
        mplew.writeInt(3); // contribution..
        return mplew.getPacket();
    }

    //someone leaving, mode == 0x2c for leaving, 0x2f for expelled
    public static MaplePacket memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(bExpelled ? 0x35 : 0x32);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeMapleAsciiString(mgc.getName());
        return mplew.getPacket();
    }

    //rank change
    public static MaplePacket changeRank(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x46);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.write(mgc.getGuildRank());
        return mplew.getPacket();
    }

    public static MaplePacket guildNotice(int gid, String notice) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x4B);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(notice);
        return mplew.getPacket();
    }

    public static MaplePacket guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x42);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());
        return mplew.getPacket();
    }

    public static MaplePacket rankTitleChange(int gid, String[] ranks) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x44);
        mplew.writeInt(gid);
        for (int i = 0; i < 5; i++) {
            mplew.writeMapleAsciiString(ranks[i]);
        }
        return mplew.getPacket();
    }

    public static MaplePacket guildDisband(int gid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x34);
        mplew.writeInt(gid);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x49);
        mplew.writeInt(gid);
        mplew.writeShort(bg);
        mplew.write(bgcolor);
        mplew.writeShort(logo);
        mplew.write(logocolor);
        return mplew.getPacket();
    }

    public static MaplePacket guildCapacityChange(int gid, int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x40);
        mplew.writeInt(gid);
        mplew.write(capacity);
        return mplew.getPacket();
    }

    public static MaplePacket guildSkillPointGain(int gid, int charid, int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(0x43);
        mplew.write(0x48);
        mplew.writeInt(gid);
        mplew.writeInt(charid);
        mplew.writeInt(amount);
        return mplew.getPacket();
    }

    public static MaplePacket guildSkill(MapleGuildCharacter mgc, int gid, int gskillid, short gskilllevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x43);
        mplew.write(0x55);
        mplew.writeInt(gid);
        writeGuildSkillInfo(mplew, mgc, gskillid, gskilllevel);
        return mplew.getPacket();
    }

    public static void writeGuildSkillInfo(MaplePacketLittleEndianWriter mplew, MapleGuildCharacter mgc, int gskillid, short gskilllevel) {
        mplew.writeInt(gskillid);
        mplew.writeShort(gskilllevel);
        mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
        mplew.writeMapleAsciiString(mgc.getName());
        mplew.writeShort(0);
    }

    public static void addThread(MaplePacketLittleEndianWriter mplew, ResultSet rs) throws SQLException {
        mplew.writeInt(rs.getInt("localthreadid"));
        mplew.writeInt(rs.getInt("postercid"));
        mplew.writeMapleAsciiString(rs.getString("name"));
        mplew.writeLong(getKoreanTimestamp(rs.getLong("timestamp")));
        mplew.writeInt(rs.getInt("icon"));
        mplew.writeInt(rs.getInt("replycount"));
    }

    public static MaplePacket BBSThreadList(ResultSet rs, int start) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BBS_OPERATION.getValue());
        mplew.write(0x06);
        if (!rs.last()) {
            mplew.write(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        int threadCount = rs.getRow();
        if (rs.getInt("localthreadid") == 0) { //has a notice
            mplew.write(1);
            addThread(mplew, rs);
            threadCount--; //one thread didn't count (because it's a notice)
        } else {
            mplew.write(0);
        }
        if (!rs.absolute(start + 1)) { //seek to the thread before where we start
            rs.first(); //uh, we're trying to start at a place past possible
            start = 0;
        }
        mplew.writeInt(threadCount);
        mplew.writeInt(Math.min(10, threadCount - start));
        for (int i = 0; i < Math.min(10, threadCount - start); i++) {
            addThread(mplew, rs);
            rs.next();
        }
        return mplew.getPacket();
    }

    public static MaplePacket showThread(int localthreadid, ResultSet threadRS, ResultSet repliesRS) throws SQLException, RuntimeException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BBS_OPERATION.getValue());
        mplew.write(0x07);
        mplew.writeInt(localthreadid);
        mplew.writeInt(threadRS.getInt("postercid"));
        mplew.writeLong(getKoreanTimestamp(threadRS.getLong("timestamp")));
        mplew.writeMapleAsciiString(threadRS.getString("name"));
        mplew.writeMapleAsciiString(threadRS.getString("startpost"));
        mplew.writeInt(threadRS.getInt("icon"));
        if (repliesRS != null) {
            int replyCount = threadRS.getInt("replycount");
            mplew.writeInt(replyCount);
            int i;
            for (i = 0; i < replyCount && repliesRS.next(); i++) {
                mplew.writeInt(repliesRS.getInt("replyid"));
                mplew.writeInt(repliesRS.getInt("postercid"));
                mplew.writeLong(getKoreanTimestamp(repliesRS.getLong("timestamp")));
                mplew.writeMapleAsciiString(repliesRS.getString("content"));
            }
            if (i != replyCount || repliesRS.next()) {
                throw new RuntimeException(String.valueOf(threadRS.getInt("threadid")));
            }
        } else {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket showGuildRanks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x4B);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("GP"));
            mplew.writeInt(rs.getInt("logo"));
            mplew.writeInt(rs.getInt("logoColor"));
            mplew.writeInt(rs.getInt("logoBG"));
            mplew.writeInt(rs.getInt("logoBGColor"));
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateGP(int gid, int GP) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x48);
        mplew.writeInt(gid);
        mplew.writeInt(GP);
        return mplew.getPacket();
    }

    public static MaplePacket skillEffect(MapleCharacter from, int skillId, int level, byte flags, int speed, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        mplew.write(level);
        mplew.write(flags);
        mplew.write(speed);
        mplew.write(direction); //Mmmk
        return mplew.getPacket();
    }

    public static MaplePacket skillCancel(MapleCharacter from, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CANCEL_SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        mplew.writeLong(0);
        return mplew.getPacket();
    }

    public static MaplePacket showMagnet(int mobid, byte success) { // Monster Magnet
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_MAGNET.getValue());
        mplew.writeInt(mobid);
        mplew.write(success);
        mplew.write0(10); //Mmmk
        return mplew.getPacket();
    }

    /**
     * Sends a player hint.
     *
     * @param hint The hint it's going to send.
     * @param width How tall the box is going to be.
     * @param height How long the box is going to be.
     * @return The player hint packet.
     */
    public static MaplePacket sendHint(String hint, int width, int height) {
        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_HINT.getValue());
        mplew.writeMapleAsciiString(hint);
        mplew.writeShort(width);
        mplew.writeShort(height);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket messengerInvite(String from, int messengerid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x03);
        mplew.writeMapleAsciiString(from);
        mplew.write(0);
        mplew.writeInt(messengerid);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket sendSpouseChat(MapleCharacter wife, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
        mplew.writeMapleAsciiString(wife.getName());
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static MaplePacket sendDonatorChat(String name, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static MaplePacket addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x00);
        mplew.write(position);
        addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(0x00);
        return mplew.getPacket();
    }

    public static MaplePacket removeMessengerPlayer(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x02);
        mplew.write(position);
        return mplew.getPacket();
    }

    public static MaplePacket updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x07);
        mplew.write(position);
        addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(0x00);
        return mplew.getPacket();
    }

    public static MaplePacket joinMessenger(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x01);
        mplew.write(position);
        return mplew.getPacket();
    }

    public static MaplePacket messengerChat(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(0x06);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static MaplePacket messengerNote(String text, int mode, int mode2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESSENGER.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);
        return mplew.getPacket();
    }

    public static void addPetInfo(MaplePacketLittleEndianWriter mplew, MaplePet pet, boolean showpet) {
        mplew.write(1);
        if (showpet) {
            mplew.write(1);
        }
        mplew.writeInt(pet.getItemId());
        mplew.writeMapleAsciiString(pet.getName());
        mplew.writeInt(pet.getUniqueId());
        mplew.writeInt(0);
        mplew.writePos(pet.getPos());
        mplew.write(pet.getStance());
        mplew.writeInt(pet.getFh());
    }

    public static MaplePacket showPet(MapleCharacter chr, MaplePet pet, boolean remove, boolean hunger) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_PET.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getPetIndex(pet));
        if (remove) {
            mplew.write(0);
            mplew.write(hunger ? 1 : 0);
        } else {
            addPetInfo(mplew, pet, true);
        }
        return mplew.getPacket();
    }

    public static MaplePacket movePet(int cid, int pid, byte slot, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MOVE_PET.getValue());
        mplew.writeInt(cid);
        mplew.write(slot);
        mplew.writeInt(pid);
        mplew.writeInt(0); // position?
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket petChat(int cid, byte index, int act, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PET_CHAT.getValue());
        mplew.writeInt(cid);
        mplew.write(index);
        mplew.write(0);
        mplew.write(act);
        mplew.writeMapleAsciiString(text);
        mplew.write(0); // quote ring
        return mplew.getPacket();
    }

    public static MaplePacket commandResponse(int cid, byte index, int animation, boolean success) {
        //[BF 7C 00 00] [00] [00] [00] [00 00]
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PET_COMMAND.getValue());
        mplew.writeInt(cid);
        mplew.write(index);
        mplew.write((animation == 1 && success) ? 1 : 0);
        mplew.write(animation);
        mplew.writeShort(animation != 1 && success ? 1 : 0);
        //    mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket showOwnPetLevelUp(byte index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(6);
        mplew.write(0);
        mplew.write(index); // Pet Index
        return mplew.getPacket();
    }

    public static MaplePacket showPetLevelUp(MapleCharacter chr, byte index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(6);
        mplew.write(0);
        mplew.write(index);
        return mplew.getPacket();
    }

    public static MaplePacket changePetName(MapleCharacter chr, String newname, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PET_NAMECHANGE.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(0);
        mplew.writeMapleAsciiString(newname);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket petStatUpdate(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_STATS.getValue());
        int mask = 0;
        mask |= MapleStat.PET.getValue();
        mplew.write(0);
        mplew.writeInt(mask);
        MaplePet[] pets = chr.getPets();
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                mplew.writeInt(pets[i].getUniqueId());
                mplew.writeInt(0);
            } else {
                mplew.writeLong(0);
            }
        }
        mplew.write(1);
        mplew.write(3);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket showForcedEquip(int team) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FORCED_MAP_EQUIP.getValue());
        if (team > -1) {
            mplew.write(team);   // 00 = red, 01 = blue
        }
        return mplew.getPacket();
    }

    public static MaplePacket summonSkill(int cid, int summonSkillId, int newStance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SUMMON_SKILL.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);
        return mplew.getPacket();
    }

    public static MaplePacket skillCooldown(int sid, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.COOLDOWN.getValue());
        mplew.writeInt(sid);
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static MaplePacket skillBookSuccess(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.USE_SKILL_BOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(maxlevel);
        mplew.write(canuse ? 1 : 0);
        mplew.write(success ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket getMacros(SkillMacro[] macros) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SKILL_MACRO.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        mplew.write(count);
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                mplew.writeMapleAsciiString(macro.getName());
                mplew.write(macro.getShout());
                mplew.writeInt(macro.getSkill1());
                mplew.writeInt(macro.getSkill2());
                mplew.writeInt(macro.getSkill3());
            } else {
                mplew.write0(15);
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerNPC(PlayerNPCs npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_NPC.getValue());
        mplew.write(0x01);
        mplew.writeInt(npc.getId());
        mplew.writeMapleAsciiString(npc.getName());
        mplew.write(0); // direction
        mplew.write(npc.getSkin());
        mplew.writeInt(npc.getFace());
        mplew.write(0);
        mplew.writeInt(npc.getHair());
        Map<Byte, Integer> equip = npc.getEquips();
        Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        for (byte position : equip.keySet()) {
            byte pos = (byte) (position * -1);
            if (pos > 100) {
                pos -= 100;
                myEquip.put(pos, equip.get(position));
            } else {
                if (myEquip.get(pos) == null) {
                    myEquip.put(pos, equip.get(position));
                }
            }
        }
        for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.writeShort(-1);
        Integer cWeapon = equip.get((byte) -111);
        if (cWeapon != null) {
            mplew.writeInt(cWeapon);
        } else {
            mplew.writeInt(0);
        }
        for (int i = 0; i < 12; i++) {
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateAriantPQRanking(String name, int score, boolean empty) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ARIANT_SCORE.getValue());
        mplew.write(empty ? 0 : 1);
        if (!empty) {
            mplew.writeMapleAsciiString(name);
            mplew.writeInt(score);
        }
        return mplew.getPacket();
    }

    public static MaplePacket catchMonster(int monsobid, int itemid, byte success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CATCH_MONSTER.getValue());
        mplew.writeInt(monsobid);
        mplew.writeInt(itemid);
        mplew.write(success);
        return mplew.getPacket();
    }

    public static MaplePacket catchMessage(int message) { // not done, I guess
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CATCH_MESSAGE.getValue());
        mplew.write(message); // 1 = too strong, 2 = Elemental Rock
        mplew.writeInt(0);//Maybe itemid?
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket showAllCharacter(int chars, int unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendOpcode.ALL_CHARLIST.getValue());
        mplew.write(1);
        mplew.writeInt(chars);
        mplew.writeInt(unk);
        return mplew.getPacket();
    }

    public static MaplePacket showAllCharacterInfo(int worldid, List<MapleCharacter> chars) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALL_CHARLIST.getValue());
        mplew.write(0);
        mplew.write(worldid);
        mplew.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr, true);
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateMount(int charid, MapleMount mount, boolean levelup) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(charid);
        mplew.writeInt(mount.getLevel());
        mplew.writeInt(mount.getExp());
        mplew.writeInt(mount.getTiredness());
        mplew.write(levelup ? (byte) 1 : (byte) 0);
        return mplew.getPacket();
    }

    public static MaplePacket boatPacket(boolean type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BOAT_EFFECT.getValue());
        mplew.writeShort(type ? 1 : 2);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGame(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(1);
        mplew.write(0);
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, minigame.getOwner(), false);
        mplew.writeMapleAsciiString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            addCharLook(mplew, visitor, false);
            mplew.writeMapleAsciiString(visitor.getName());
        }
        mplew.write(0xFF);
        mplew.write(0);
        mplew.writeInt(1);
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("wins", true));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("ties", true));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("losses", true));
        mplew.writeInt(2000);
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            mplew.writeInt(1);
            mplew.writeInt(visitor.getMiniGamePoints("wins", true));
            mplew.writeInt(visitor.getMiniGamePoints("ties", true));
            mplew.writeInt(visitor.getMiniGamePoints("losses", true));
            mplew.writeInt(2000);
        }
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.write(piece);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameReady(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.READY.getCode());
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameUnReady(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.UN_READY.getCode());
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameStart(MapleMiniGame game, int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.START.getCode());
        mplew.write(loser);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameSkipOwner(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SKIP.getCode());
        mplew.write(0x01);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameRequestTie(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.REQUEST_TIE.getCode());
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameDenyTie(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ANSWER_TIE.getCode());
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(0);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameSkipVisitor(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.writeShort(PlayerInteractionHandler.Action.SKIP.getCode());
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameMoveOmok(MapleMiniGame game, int move1, int move2, int move3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.MOVE_OMOK.getCode());
        mplew.writeInt(move1);
        mplew.writeInt(move2);
        mplew.write(move3);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameNewVisitor(MapleCharacter c, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1);
        mplew.writeInt(c.getMiniGamePoints("wins", true));
        mplew.writeInt(c.getMiniGamePoints("ties", true));
        mplew.writeInt(c.getMiniGamePoints("losses", true));
        mplew.writeInt(2000);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameRemoveVisitor() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(1);
        return mplew.getPacket();
    }

    private static MaplePacket getMiniGameResult(MapleMiniGame game, int win, int lose, int tie, int result, int forfeit, boolean omok) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.GET_RESULT.getCode());
        if (tie == 0 && forfeit != 1) {
            mplew.write(0);
        } else if (tie == 1) {
            mplew.write(1);
        } else if (forfeit == 1) {
            mplew.write(2);
        }
        mplew.write(0); // owner
        mplew.writeInt(1); // unknown
        mplew.writeInt(game.getOwner().getMiniGamePoints("wins", omok) + win); // wins
        mplew.writeInt(game.getOwner().getMiniGamePoints("ties", omok) + tie); // ties
        mplew.writeInt(game.getOwner().getMiniGamePoints("losses", omok) + lose); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getVisitor().getMiniGamePoints("wins", omok) + lose); // wins
        mplew.writeInt(game.getVisitor().getMiniGamePoints("ties", omok) + tie); // ties
        mplew.writeInt(game.getVisitor().getMiniGamePoints("losses", omok) + win); // losses
        mplew.writeInt(2000); // points
        game.getOwner().setMiniGamePoints(game.getVisitor(), result, omok);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameOwnerWin(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 1, 0, true);
    }

    public static MaplePacket getMiniGameVisitorWin(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 2, 0, true);
    }

    public static MaplePacket getMiniGameTie(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 0, 1, 3, 0, true);
    }

    public static MaplePacket getMiniGameOwnerForfeit(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 2, 1, true);
    }

    public static MaplePacket getMiniGameVisitorForfeit(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 1, 1, true);
    }

    public static MaplePacket getMiniGameClose() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(1);
        mplew.write(3);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCard(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(2);
        mplew.write(2);
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, minigame.getOwner(), false);
        mplew.writeMapleAsciiString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            addCharLook(mplew, visitor, false);
            mplew.writeMapleAsciiString(visitor.getName());
        }
        mplew.write(0xFF);
        mplew.write(0);
        mplew.writeInt(2);
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("wins", false));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("ties", false));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("losses", false));
        mplew.writeInt(2000);
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            mplew.writeInt(2);
            mplew.writeInt(visitor.getMiniGamePoints("wins", false));
            mplew.writeInt(visitor.getMiniGamePoints("ties", false));
            mplew.writeInt(visitor.getMiniGamePoints("losses", false));
            mplew.writeInt(2000);
        }
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.write(piece);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardStart(MapleMiniGame game, int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.START.getCode());
        mplew.write(loser);
        mplew.write(0x0C);
        int last = 13;
        if (game.getMatchesToWin() > 10) {
            last = 31;
        } else if (game.getMatchesToWin() > 6) {
            last = 21;
        }
        for (int i = 1; i < last; i++) {
            mplew.writeInt(game.getCardId(i));
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardNewVisitor(MapleCharacter c, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1);
        mplew.writeInt(c.getMiniGamePoints("wins", false));
        mplew.writeInt(c.getMiniGamePoints("ties", false));
        mplew.writeInt(c.getMiniGamePoints("losses", false));
        mplew.writeInt(2000);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardSelect(MapleMiniGame game, int turn, int slot, int firstslot, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.SELECT_CARD.getCode());
        mplew.write(turn);
        if (turn == 1) {
            mplew.write(slot);
        } else if (turn == 0) {
            mplew.write(slot);
            mplew.write(firstslot);
            mplew.write(type);
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardOwnerWin(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 1, 0, false);
    }

    public static MaplePacket getMatchCardVisitorWin(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 2, 0, false);
    }

    public static MaplePacket getMatchCardTie(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 0, 1, 3, 0, false);
    }

    public static MaplePacket fredrickMessage(byte operation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //    mplew.writeShort(SendOpcode.FREDRICK_MESSAGE.getValue());
        //       mplew.write(operation);
        return mplew.getPacket();
    }

    public static MaplePacket getFredrick(byte op) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FREDRICK_OPERATION.getValue());
        mplew.write(op);

        switch (op) {
            case 0x24:
                mplew.write0(8);
                break;
            default:
                mplew.write(0);
                break;
        }
        return mplew.getPacket();
    }

    public static MaplePacket getFredrick(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.FREDRICK_OPERATION.getValue());
        mplew.write(0x23);
        mplew.writeInt(9030000); // Fredrick
        mplew.writeInt(32272); //id
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(chr.getMerchantMeso());
        mplew.write(0);
        try {
            List<Pair<IItem, MapleInventoryType>> items = ItemFactory.MERCHANT.loadItems(chr.getId(), false);
            mplew.write(items.size());

            for (int i = 0; i < items.size(); i++) {
                addItemInfo(mplew, items.get(i).getLeft(), true);
            }
        } catch (SQLException e) {
        }
        mplew.writeShort(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket addOmokBox(MapleCharacter c, int ammount, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getMiniGame(), 1, 0, ammount, type);
        return mplew.getPacket();
    }

    public static MaplePacket removeOmokBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket addMatchCardBox(MapleCharacter c, int ammount, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getMiniGame(), 2, 0, ammount, type);
        return mplew.getPacket();
    }

    public static MaplePacket removeMatchcardBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopChat(MapleCharacter c, String chat, byte slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(slot);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    public static MaplePacket getTradeChat(MapleCharacter c, String chat, boolean owner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(owner ? 0 : 1);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantBox() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SEND_TITLE_BOX.getValue()); // header.
        mplew.write(0x07);
        return mplew.getPacket();
    }

    public static MaplePacket owlOfMinerva(MapleClient c, int itemid, List<HiredMerchant> hms, List<MaplePlayerShopItem> items) { //Thanks moongra, you save me some time :)
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /*    mplew.writeShort(SendOpcode.OWL_OF_MINERVA.getValue()); // header.
        mplew.write(6);
        mplew.writeInt(0);
        mplew.writeInt(itemid);
        mplew.writeInt(hms.size());
        for (HiredMerchant hm  : hms) {
        for (MaplePlayerShopItem item : items) {
        mplew.writeMapleAsciiString(hm.getOwner());
        mplew.writeInt(hm.getMapId());
        mplew.writeMapleAsciiString(hm.getDescription());
        mplew.writeInt(item.getItem().getQuantity());
        mplew.writeInt(item.getBundles());
        mplew.writeInt(item.getPrice());
        mplew.writeInt(hm.getOwnerId());
        mplew.write(hm.getFreeSlot() == -1 ? 1 : 0);
        MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(hm.getOwnerId());
        if ((chr != null) && (c.getChannel() == hm.getChannel()))
        mplew.write(1);
        else
        mplew.write(2);
        
        if (item.getItem().getItemId() / 1000000 == 1) {
        addItemInfo(mplew, item.getItem(), true);
        }
        }
        }*/
        return mplew.getPacket();
    }

    public static MaplePacket retrieveFirstMessage() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SEND_TITLE_BOX.getValue()); // header.
        mplew.write(0x09);
        return mplew.getPacket();
    }

    public static MaplePacket remoteChannelChange(byte ch) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SEND_TITLE_BOX.getValue()); // header.
        mplew.write(0x10);
        mplew.writeInt(0);//No idea yet
        mplew.write(ch);
        return mplew.getPacket();
    }
    /*
     * Possible things for SEND_TITLE_BOX
     * 0x0E = 00 = Renaming Failed - Can't find the merchant, 01 = Renaming succesful
     * 0x10 = Changes channel to the store (Store is open at Channel 1, do you want to change channels?)
     * 0x11 = You cannot sell any items when managing.. blabla
     * 0x12 = FKING POPUP LOL
     */

    public static MaplePacket getHiredMerchant(MapleCharacter chr, HiredMerchant hm, boolean firstTime) {//Thanks Dustin
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(5);
        mplew.write(5);
        mplew.write(4);
        mplew.write(hm.isOwner(chr) ? 0 : hm.getVisitorSlot(chr) + 1);
        mplew.write(0);
        mplew.writeInt(hm.getItemId());
        mplew.writeMapleAsciiString("Hired Merchant");
        for (int i = 0; i < 3; i++) {
            if (hm.getVisitors()[i] != null) {
                mplew.write(i + 1);
                addCharLook(mplew, hm.getVisitors()[i], true);
                mplew.writeMapleAsciiString(hm.getVisitors()[i].getName());
                mplew.writeShort(hm.getVisitors()[i].getJob().getId());
            }
        }
        mplew.write(0xFF);
        if (hm.isOwner(chr)) {
            mplew.writeShort(hm.getMessages().size());
            for (Pair<String, Byte> chats : hm.getMessages()) {
                mplew.writeMapleAsciiString(chats.getLeft());
                mplew.write(chats.getRight());
            }
        } else {
            mplew.writeShort(0); // number of chats there structure: for (int i = 0; i < num; i++) masciistring, byte slot
        }
        mplew.writeMapleAsciiString(hm.getOwner());
        if (hm.isOwner(chr)) {
            mplew.writeInt(0); // time left probably
            mplew.writeInt(firstTime ? 1 : 0);
            mplew.write(0);
            mplew.write(0); // sold items
            mplew.writeInt((int) Math.min(chr.getMerchantMeso(), Integer.MAX_VALUE));
        }
        mplew.writeMapleAsciiString(hm.getDescription());
        mplew.write(0x10); // slot max
        mplew.writeInt(hm.isOwner(chr) ? (int) Math.min(chr.getMerchantMeso(), Integer.MAX_VALUE) : chr.getMeso());
        mplew.write(hm.getItems().size());
        if (hm.getItems().isEmpty()) {
            mplew.write(0); // not on GMS
        } else {
            for (MaplePlayerShopItem item : hm.getItems()) {
                mplew.writeShort(item.getBundles());
                mplew.writeShort(item.getItem().getQuantity());
                mplew.writeInt(item.getPrice());
                addItemInfo(mplew, item.getItem(), true);
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateHiredMerchant(HiredMerchant hm, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
        mplew.writeInt(chr.getMeso());
        mplew.write(hm.getItems().size());
        for (MaplePlayerShopItem item : hm.getItems()) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            addItemInfo(mplew, item.getItem(), true);
        }
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantChat(String message, byte slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
        mplew.write(slot);
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantVisitorLeave(int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        if (slot != 0) {
            mplew.write(slot);
        }
        mplew.writeInt(0); // v0.97
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantOwnerLeave() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.REAL_CLOSE_MERCHANT.getCode());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket leaveHiredMerchant(int slot, int status2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(slot);
        mplew.write(status2);
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantVisitorAdd(MapleCharacter chr, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        addCharLook(mplew, chr, false);
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket spawnHiredMerchant(HiredMerchant hm) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_HIRED_MERCHANT.getValue());
        mplew.writeInt(hm.getOwnerId());
        mplew.writeInt(hm.getItemId());
        mplew.writeShort((short) hm.getPosition().getX());
        mplew.writeShort((short) hm.getPosition().getY());
        mplew.writeShort(0);
        mplew.writeMapleAsciiString(hm.getOwner());
        mplew.write(0x05);
        mplew.writeInt(hm.getObjectId());
        mplew.writeMapleAsciiString(hm.getDescription());
        mplew.write(hm.getItemId() % 10);
        mplew.write(new byte[]{1, 4});
        return mplew.getPacket();
    }

    public static MaplePacket destroyHiredMerchant(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DESTROY_HIRED_MERCHANT.getValue());
        mplew.writeInt(id);
        return mplew.getPacket();
    }

    public static MaplePacket spawnPlayerNPC(PlayerNPCs npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(npc.getObjectId());
        mplew.writeInt(npc.getId());
        mplew.writeShort(npc.getPosition().x);
        mplew.writeShort(npc.getCY());
        mplew.write(1);
        mplew.writeShort(npc.getFH());
        mplew.writeShort(npc.getRX0());
        mplew.writeShort(npc.getRX1());
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket sendYellowTip(String tip) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.YELLOW_TIP.getValue());
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(tip);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket giveInfusion(int buffid, int bufflength, int speed) {//This ain't correct
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(MapleBuffStat.SPEED_INFUSION.getValue());
        mplew.write0(24);
        mplew.write0(3);
        mplew.writeInt(speed);
        mplew.writeInt(buffid);
        mplew.write0(10);
        mplew.writeShort(bufflength);
        mplew.writeShort(600); // probably 58 02 again
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket givePirateBuff(List<Pair<MapleBuffStat, Integer>> statups, int buffid, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
        writeLongMask(mplew, statups);
        mplew.writeShort(0);
        mplew.write(0);
        for (Pair<MapleBuffStat, Integer> stat : statups) {
            mplew.writeInt(stat.getRight().shortValue());
            mplew.writeInt(buffid);
            mplew.write0(5);
            mplew.writeShort(duration);
        }
        mplew.writeShort(0);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static MaplePacket giveForeignDash(int cid, int buffid, int time, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeLongMask(mplew, statups);
        mplew.writeShort(0);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeInt(statup.getRight().shortValue());
            mplew.writeInt(buffid);
            mplew.write0(5);
            mplew.writeShort(time);
        }
        mplew.writeShort(0);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static MaplePacket giveForeignInfusion(int cid, int speed, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(MapleBuffStat.SPEED_INFUSION.getValue());
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(speed);
        mplew.writeInt(5121009);
        mplew.writeLong(0);
        mplew.writeInt(duration);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket sendMTS(List<MTSItemInfo> items, int tab, int type, int page, int pages) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x15); //operation
        mplew.writeInt(pages * 16); //testing, change to 10 if fails
        mplew.writeInt(items.size()); //number of items
        mplew.writeInt(tab);
        mplew.writeInt(type);
        mplew.writeInt(page);
        mplew.write(1);
        mplew.write(1);
        for (int i = 0; i < items.size(); i++) {
            MTSItemInfo item = items.get(i);
            addItemInfo(mplew, item.getItem(), true);
            mplew.writeInt(item.getID()); //id
            mplew.writeInt(item.getTaxes()); //this + below = price
            mplew.writeInt(item.getPrice()); //price
            mplew.writeLong(0);
            mplew.writeInt(getQuestTimestamp(item.getEndingDate()));
            mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
            mplew.writeMapleAsciiString(item.getSeller()); //char name
            for (int j = 0; j < 28; j++) {
                mplew.write(0);
            }
        }
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket noteSendMsg() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.SHOW_NOTES.getValue());
        mplew.write(4);
        return mplew.getPacket();
    }

    /*
     *  0 = Player online, use whisper
     *  1 = Check player's name
     *  2 = Receiver inbox full
     */
    public static MaplePacket noteError(byte error) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.SHOW_NOTES.getValue());
        mplew.write(5);
        mplew.write(error);
        return mplew.getPacket();
    }

    public static MaplePacket showNotes(ResultSet notes, int count) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_NOTES.getValue());
        mplew.write(3);
        mplew.write(count);
        for (int i = 0; i < count; i++) {
            mplew.writeInt(notes.getInt("id"));
            mplew.writeMapleAsciiString(notes.getString("from") + " ");//Stupid nexon forgot space lol
            mplew.writeMapleAsciiString(notes.getString("message"));
            mplew.writeLong(getKoreanTimestamp(notes.getLong("timestamp")));
            mplew.write(notes.getByte("fame"));//FAME :D
            notes.next();
        }
        return mplew.getPacket();
    }

    public static MaplePacket useChalkboard(MapleCharacter chr, boolean close) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CHALKBOARD.getValue());
        mplew.writeInt(chr.getId());
        if (close) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getChalkboard());
        }
        return mplew.getPacket();
    }

    public static MaplePacket trockRefreshMapList(MapleCharacter chr, boolean delete, boolean vip) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.TROCK_LOCATIONS.getValue());
        mplew.write(delete ? 2 : 3);
        if (vip) {
            mplew.write(1);
            int[] map = chr.getVipTrockMaps();
            for (int i = 0; i < 10; i++) {
                mplew.writeInt(map[i]);
            }
        } else {
            mplew.write(0);
            int[] map = chr.getTrockMaps();
            for (int i = 0; i < 5; i++) {
                mplew.writeInt(map[i]);
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket showMTSCash(MapleCharacter p) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION2.getValue());
        mplew.writeInt(p.getCashShop().getCash(4));
        mplew.writeInt(p.getCashShop().getCash(2));
        return mplew.getPacket();
    }

    public static MaplePacket MTSWantedListingOver(int nx, int items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x3D);
        mplew.writeInt(nx);
        mplew.writeInt(items);
        return mplew.getPacket();
    }

    public static MaplePacket MTSConfirmSell() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x1D);
        return mplew.getPacket();
    }

    public static MaplePacket MTSConfirmBuy() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x33);
        return mplew.getPacket();
    }

    public static MaplePacket MTSFailBuy() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x34);
        mplew.write(0x42);
        return mplew.getPacket();
    }

    public static MaplePacket MTSConfirmTransfer(int quantity, int pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x27);
        mplew.writeInt(quantity);
        mplew.writeInt(pos);
        return mplew.getPacket();
    }

    public static MaplePacket notYetSoldInv(List<MTSItemInfo> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x23);
        mplew.writeInt(items.size());
        if (!items.isEmpty()) {
            for (MTSItemInfo item : items) {
                addItemInfo(mplew, item.getItem(), true);
                mplew.writeInt(item.getID()); //id
                mplew.writeInt(item.getTaxes()); //this + below = price
                mplew.writeInt(item.getPrice()); //price
                mplew.writeLong(0);
                mplew.writeInt(getQuestTimestamp(item.getEndingDate()));
                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                mplew.writeMapleAsciiString(item.getSeller()); //char name
                for (int i = 0; i < 28; i++) {
                    mplew.write(0);
                }
            }
        } else {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket transferInventory(List<MTSItemInfo> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x21);
        mplew.writeInt(items.size());
        if (!items.isEmpty()) {
            for (MTSItemInfo item : items) {
                addItemInfo(mplew, item.getItem(), true);
                mplew.writeInt(item.getID()); //id
                mplew.writeInt(item.getTaxes()); //taxes
                mplew.writeInt(item.getPrice()); //price
                mplew.writeLong(0);
                mplew.writeInt(getQuestTimestamp(item.getEndingDate()));
                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                mplew.writeMapleAsciiString(item.getSeller()); //char name
                for (int i = 0; i < 28; i++) {
                    mplew.write(0);
                }
            }
        }
        mplew.write(0xD0 + items.size());
        mplew.write(new byte[]{-1, -1, -1, 0});
        return mplew.getPacket();
    }

    public static MaplePacket showCouponRedeemedItem(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());
        mplew.writeShort(0x49); //v72
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeShort(1);
        mplew.writeShort(0x1A);
        mplew.writeInt(itemid);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket showCash(MapleCharacter mc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_UPDATE.getValue());

        mplew.writeInt(mc.getCashShop().getCash(1));
        mplew.writeInt(mc.getCashShop().getCash(2));
        mplew.writeInt(mc.getCashShop().getCash(4));

        return mplew.getPacket();
    }

    public static MaplePacket enableCSUse() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(0x12);
        mplew.write0(6);
        return mplew.getPacket();
    }

    /**
     * 
     * @param target
     * @param mapid
     * @param MTSmapCSchannel
     * 0: MTS
     * 1: Map
     * 2: CS
     * 3: Different Channel
     * @return
     */
    public static MaplePacket getFindReply(String target, int mapid, int MTSmapCSchannel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.WHISPER.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(target);
        mplew.write(MTSmapCSchannel); // 0: mts 1: map 2: cs 3: wrong ch 4: unable to find
        mplew.writeInt(MTSmapCSchannel == 3 ? mapid - 1 : mapid); // -1 if mts, cs, 0 if wrong channel
        if (MTSmapCSchannel == 1) {
            mplew.writeInt(0); // something with coord?
            mplew.writeInt(0); // something
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendAutoHpPot(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.AUTO_HP_POT.getValue());
        mplew.writeInt(itemId);
        return mplew.getPacket();
    }

    public static MaplePacket sendAutoMpPot(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.AUTO_MP_POT.getValue());
        mplew.writeInt(itemId);
        return mplew.getPacket();
    }

    public static MaplePacket showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.OX_QUIZ.getValue());
        mplew.write(askQuestion ? 1 : 0);
        mplew.write(questionSet);
        mplew.writeShort(questionId);
        return mplew.getPacket();
    }

    public static MaplePacket updateGender(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        //      mplew.writeShort(SendOpcode.GENDER.getValue());
        //     mplew.write(chr.getGender());
        return mplew.getPacket();
    }

    public static MaplePacket enableReport() { // by snow
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendOpcode.ENABLE_REPORT.getValue());
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket giveFinalAttack(int skillid, int time) {//packets found by lailainoob
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.write(0);//some 80 and 0 bs DIRECTION
        mplew.write(0x80);//let's just do 80, then 0
        mplew.writeInt(0);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(time);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket loadFamily(MapleCharacter player) {
        //    String[] title = {"Family Reunion", "Summon Family", "My Drop Rate 1.5x (15 min)", "My EXP 1.5x (15 min)", "Family Bonding (30 min)", "My Drop Rate 2x (15 min)", "My EXP 2x (15 min)", "My Drop Rate 2x (30 min)", "My EXP 2x (30 min)", "My Party Drop Rate 2x (30 min)", "My Party EXP 2x (30 min)"};
        //    String[] description = {"[Target] Me\n[Effect] Teleport directly to the Family member of your choice.", "[Target] 1 Family member\n[Effect] Summon a Family member of choice to the map you're in.", "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c1.5x#.\n*  If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] EXP earned from hunting will be increased #c1.5x#.\n* If the EXP event is in progress, this will be nullified.", "[Target] At least 6 Family members online that are below me in the Pedigree\n[Time] 30 min.\n[Effect] Monster drop rate and EXP earned will be increased #c2x#. \n* If the EXP event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] EXP earned from hunting will be increased #c2x#.\n* If the EXP event is in progress, this will be nullified.", "[Target] Me\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 30 min.\n[Effect] EXP earned from hunting will be increased #c2x#. \n* If the EXP event is in progress, this will be nullified.", "[Target] My party\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] My party\n[Time] 30 min.\n[Effect] EXP earned from hunting will be increased #c2x#.\n* If the EXP event is in progress, this will be nullified."};
        //    int[] repCost = {3, 5, 7, 8, 10, 12, 15, 20, 25, 40, 50};
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /*   mplew.writeShort(SendOpcode.LOAD_FAMILY.getValue());
        mplew.writeInt(11);
        for (int i = 0; i < 11; i++) {
        mplew.write(i > 4 ? (i % 2) + 1 : i);
        mplew.writeInt(repCost[i] * 100);
        mplew.writeInt(1);
        mplew.writeMapleAsciiString(title[i]);
        mplew.writeMapleAsciiString(description[i]);
        }*/
        return mplew.getPacket();
    }

    public static MaplePacket sendFamilyMessage() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        //   mplew.writeShort(SendOpcode.FAMILY_MESSAGE.getValue());
        //  mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket getFamilyInfo(MapleFamilyEntry f) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.OPEN_FAMILY.getValue());
        mplew.writeInt(f.getReputation()); // cur rep left
        mplew.writeInt(f.getTotalReputation()); // tot rep left
        mplew.writeInt(f.getTodaysRep()); // todays rep
        mplew.writeShort(f.getJuniors()); // juniors added
        mplew.writeShort(f.getTotalJuniors()); // juniors allowed
        mplew.writeShort(0); //Unknown
        mplew.writeInt(f.getId()); // id?
        mplew.writeMapleAsciiString(f.getFamilyName());
        mplew.writeInt(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket showPedigree(int chrid, Map<Integer, MapleFamilyEntry> members) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_PEDIGREE.getValue());
        //Hmmm xD
        return mplew.getPacket();
    }

    public static MaplePacket updateAreaInfo(String mode, int quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0x0A); //0x0B in v95
        mplew.writeShort(quest);
        mplew.writeMapleAsciiString(mode);
        return mplew.getPacket();
    }

    public static MaplePacket questProgress(short id, String process) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(id);
        mplew.write(1);
        mplew.writeMapleAsciiString(process);
        return mplew.getPacket();
    }

    public static MaplePacket getItemMessage(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(7);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static MaplePacket addCard(boolean full, int cardid, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        //  mplew.writeShort(SendOpcode.MONSTERBOOK_ADD.getValue());
      /*  mplew.write(full ? 0 : 1);
        mplew.writeInt(cardid);
        mplew.writeInt(level);*/
        return mplew.getPacket();
    }

    public static MaplePacket showGainCard() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        //  mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        //  mplew.write(0x0D);
        return mplew.getPacket();
    }

    public static MaplePacket showForeginCardEffect(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        //   mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        //   mplew.writeInt(id);
        //  mplew.write(0x0D);
        return mplew.getPacket();
    }

    public static MaplePacket changeCover(int cardid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        //   mplew.writeShort(SendOpcode.MONSTER_BOOK_CHANGE_COVER.getValue());
        //  mplew.writeInt(cardid);
        return mplew.getPacket();
    }

    public static MaplePacket aranGodlyStats() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //  mplew.writeShort(SendOpcode.TEMPORARY_STATS.getValue());
        //  mplew.write(new byte[]{(byte) 0x1F, (byte) 0x0F, 0, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xFF, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0x78, (byte) 0x8C});
        return mplew.getPacket();
    }

    public static MaplePacket temporaryStats(List<Pair<MapleStat, Integer>> stats) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /*  mplew.writeShort(SendOpcode.TEMPORARY_STATS.getValue());
        int updateMask = 0;
        for (final Pair<MapleStat, Integer> statupdate : stats) {
        updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<MapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
        Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {
        
        @Override
        public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) {
        int val1 = o1.getLeft().getValue();
        int val2 = o2.getLeft().getValue();
        return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
        }
        });
        }
        mplew.writeInt(updateMask);
        Integer value;
        
        for (Pair<MapleStat, Integer> statupdate : mystats) {
        value = statupdate.getLeft().getValue();
        
        if (value >= 1) {
        if (value <= 0x200) { //level 0x10 - is this really short or some other? (FF 00)
        mplew.writeShort(statupdate.getRight().shortValue());
        } else {
        mplew.write(statupdate.getRight().byteValue());
        }
        }
        }*/
        return mplew.getPacket();
    }

    public static MaplePacket resetTemporaryStats() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //  mplew.writeShort(SendOpcode.TEMPORARY_STATS_RESET.getValue());
        return mplew.getPacket();
    }

    public static MaplePacket showIntro(String path) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x12);
        mplew.writeMapleAsciiString(path);
        return mplew.getPacket();
    }

    public static MaplePacket showInfo(String path) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x17);
        mplew.writeMapleAsciiString(path);
        mplew.writeInt(1);
        return mplew.getPacket();
    }

    /**
     * Sends a UI utility.
     * 0x01 - Equipment Inventory.
     * 0x02 - Stat Window.
     * 0x03 - Skill Window.
     * 0x05 - Keyboard Settings.
     * 0x06 - Quest window.
     * 0x09 - Monsterbook Window.
     * 0x0A - Char Info
     * 0x0B - Guild BBS
     * 0x12 - Monster Carnival Window
     * 0x16 - Party Search.
     * 0x17 - Item Creation Window.
     * 0x1A - My Ranking O.O
     * 0x1B - Family Window
     * 0x1C - Family Pedigree
     * 0x1D - GM Story Board /funny shet
     * 0x1E - Envelop saying you got mail from an admin. lmfao
     * 0x1F - Medal Window
     * 0x20 - Maple Event (???)
     * 0x21 - Invalid Pointer Crash
     *
     * @param ui
     * @return
     */
    public static MaplePacket openUI(byte ui) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        //	mplew.writeShort(SendOpcode.OPEN_UI.getValue());
        //	mplew.write(ui);
        return mplew.getPacket();
    }

    public static MaplePacket lockUI(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        //    mplew.writeShort(SendOpcode.LOCK_UI.getValue());
        //     mplew.write(enable ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket disableUI(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        // mplew.writeShort(SendOpcode.DIS.getValue());
        // mplew.write(enable ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket itemMegaphone(String msg, boolean whisper, int channel, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(8);
        mplew.writeMapleAsciiString(msg);
        mplew.write(channel - 1);
        mplew.write(whisper ? 1 : 0);
        if (item == null) {
            mplew.write(0);
        } else {
            mplew.write(item.getPosition());
            addItemInfo(mplew, item, true);
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeNPC(int oid) { //Make npc's invisible
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(0);
        mplew.writeInt(oid);
        return mplew.getPacket();
    }

    /**
     * Sends a report response
     *
     * Possible values for <code>mode</code>:<br>
     * 0: You have succesfully reported the user.<br>
     * 1: Unable to locate the user.<br>
     * 2: You may only report users 10 times a day.<br>
     * 3: You have been reported to the GM's by a user.<br>
     * 4: Your request did not go through for unknown reasons. Please try again later.<br>
     *
     * @param mode The mode
     * @return Report Reponse packet
     */
    public static MaplePacket reportResponse(byte mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.REPORT_RESPONSE.getValue());
        mplew.write(mode);
        return mplew.getPacket();
    }

    public static MaplePacket sendHammerData(int hammerUsed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
        mplew.write(0x39);
        mplew.writeInt(0);
        mplew.writeInt(hammerUsed);
        return mplew.getPacket();
    }

    public static MaplePacket sendHammerMessage() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
        mplew.write(0x3D);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket hammerItem(IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0); // could be from drop
        mplew.write(2); // always 2
        mplew.write(3); // quantity > 0 (?)
        mplew.write(1); // Inventory type
        mplew.writeShort(item.getPosition()); // item slot
        mplew.write(0);
        mplew.write(1);
        mplew.writeShort(item.getPosition()); // wtf repeat
        addItemInfo(mplew, item, true);
        return mplew.getPacket();
    }

    public static MaplePacket playPortalSound() {
        return showSpecialEffect(7);
    }

    public static MaplePacket showMonsterBookPickup() {
        return showSpecialEffect(7);
    }

    public static MaplePacket showEquipmentLevelUp() {
        return showSpecialEffect(15);
    }

    public static MaplePacket showItemLevelup() {
        return showSpecialEffect(15);
    }

    /**
     * 6 = Exp did not drop (Safety Charms)
     * 7 = Enter portal sound
     * 8 = Job change
     * 9 = Quest complete
     * 10 = damage O.O
     * 14 = Monster book pickup
     * 15 = Equipment levelup
     * 16 = Maker Skill Success
     * 19 = Exp card [500, 200, 50]
     * @param effect
     * @return 
     */
    public static MaplePacket showSpecialEffect(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effect);
        return mplew.getPacket();
    }

    public static MaplePacket showForeignEffect(int cid, int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect);
        return mplew.getPacket();
    }

    public static MaplePacket showHpHealed(int cid, int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(0x0A);
        mplew.write(amount);
        return mplew.getPacket();
    }

    public static MaplePacket showWheelsLeft(int left) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x15);
        mplew.write(left);
        return mplew.getPacket();
    }

    public static MaplePacket updateQuestFinish(short quest, int npc, short nextquest) { //Check
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue()); //0xF2 in v95
        mplew.write(0x0A);//0x0A in v95
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeShort(nextquest);
        return mplew.getPacket();
    }

    public static MaplePacket showMedalProgress(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static MaplePacket questError(short quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(0x10);
        mplew.writeShort(quest);
        return mplew.getPacket();
    }

    public static MaplePacket questFailure(byte type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(type);//0x0B = No meso, 0x0D = Worn by character, 0x0E = Not having the item ?
        return mplew.getPacket();
    }

    public static MaplePacket questExpire(short quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(0x0F);
        mplew.writeShort(quest);
        return mplew.getPacket();
    }

    public static MaplePacket getMultiMegaphone(String[] messages, int channel, boolean showEar) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(0x0A);
        if (messages[0] != null) {
            mplew.writeMapleAsciiString(messages[0]);
        }
        mplew.write(messages.length);
        for (int i = 1; i < messages.length; i++) {
            if (messages[i] != null) {
                mplew.writeMapleAsciiString(messages[i]);
            }
        }
        for (int i = 0; i < 10; i++) {
            mplew.write(channel - 1);
        }
        mplew.write(showEar ? 1 : 0);
        mplew.write(1);
        return mplew.getPacket();
    }

    /**
     * Gets a gm effect packet (ie. hide, banned, etc.)
     *
     * Possible values for <code>type</code>:<br>
     * 0x04: You have successfully blocked access.<br>
     * 0x05: The unblocking has been successful.<br>
     * 0x06 with Mode 0: You have successfully removed the name from the ranks.<br>
     * 0x06 with Mode 1: You have entered an invalid character name.<br>
     * 0x10: GM Hide, mode determines whether or not it is on.<br>
     * 0x1E: Mode 0: Failed to send warning Mode 1: Sent warning<br>
     * 0x13 with Mode 0: + mapid
     * 0x13 with Mode 1: + ch (FF = Unable to find merchant)
     *
     * @param type The type
     * @param mode The mode
     * @return The gm effect packet
     */
    public static MaplePacket getGMEffect(int type, byte mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GM_PACKET.getValue());
        mplew.write(type);
        mplew.write(mode);
        return mplew.getPacket();
    }

    public static MaplePacket findMerchantResponse(boolean map, int extra) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GM_PACKET.getValue());
        mplew.write(0x13);
        mplew.write(map ? 0 : 1); //00 = mapid, 01 = ch
        if (map) {
            mplew.writeInt(extra);
        } else {
            mplew.write(extra); //-1 = unable to find
        }
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket disableMinimap() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GM_PACKET.getValue());
        mplew.writeShort(0x1C);
        return mplew.getPacket();
    }

    public static MaplePacket sendFamilyInvite(int playerId, String inviter) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /////     mplew.writeShort(SendOpcode.FAMILY_INVITE.getValue());
        //     mplew.writeInt(playerId);
        //     mplew.writeMapleAsciiString(inviter);
        return mplew.getPacket();
    }

    public static MaplePacket showBoughtCashPackage(List<IItem> cashPackage, int accountId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        mplew.write(0x9D);
        mplew.write(cashPackage.size());

        for (IItem item : cashPackage) {
            addCashItemInformation(mplew, item, accountId);
        }

        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static MaplePacket showBoughtQuestItem(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        mplew.write(0x8D);
        mplew.writeInt(1);
        mplew.writeShort(1);
        mplew.write(0x0B);
        mplew.write(0);
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static MaplePacket updateSlot(IItem item) {//Just the same as merge... dst and src is the same...
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MODIFY_INVENTORY_ITEM.getValue());
        byte type = ItemConstants.getInventoryType(item.getItemId()).getType();
        mplew.write(new byte[]{0, 2, 3});
        mplew.write(type);
        mplew.writeShort(item.getPosition());
        mplew.write(0);
        mplew.write(type);
        mplew.writeShort(item.getPosition());
        addItemInfo(mplew, item, true);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    private static void getGuildInfo(MaplePacketLittleEndianWriter mplew, MapleGuild guild) {
        mplew.writeInt(guild.getId());
        mplew.writeMapleAsciiString(guild.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(guild.getRankTitle(i));
        }
        Collection<MapleGuildCharacter> members = guild.getMembers();
        mplew.write(members.size());
        for (MapleGuildCharacter mgc : members) {
            mplew.writeInt(mgc.getId());
        }
        for (MapleGuildCharacter mgc : members) {
            mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
            mplew.writeInt(mgc.getJobId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getGuildRank());
            mplew.writeInt(mgc.isOnline() ? 1 : 0);
            mplew.writeInt(guild.getSignature());
            mplew.writeInt(mgc.getAllianceRank());
        }
        mplew.writeInt(guild.getCapacity());
        mplew.writeShort(guild.getLogoBG());
        mplew.write(guild.getLogoBGColor());
        mplew.writeShort(guild.getLogo());
        mplew.write(guild.getLogoColor());
        mplew.writeMapleAsciiString(guild.getNotice());
        mplew.writeInt(guild.getGP());
        mplew.writeInt(guild.getAllianceId());
    }

    public static MaplePacket getAllianceInfo(MapleAlliance alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0xE9);
        mplew.write(0x0C);
        mplew.write(1);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        mplew.writeInt(2); // probably capacity
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeMapleAsciiString(alliance.getNotice());
        return mplew.getPacket();
    }

    public static MaplePacket makeNewAlliance(MapleAlliance alliance, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0F);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeInt(2); // probably capacity
        mplew.writeShort(0);
        for (Integer guildd : alliance.getGuilds()) {
            try {
                getGuildInfo(mplew, c.getChannelServer().getWorldInterface().getGuild(guildd, c.getPlayer().getMGC()));
            } catch (RemoteException re) {
                c.getChannelServer().reconnectWorld();
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket getGuildAlliances(MapleAlliance alliance, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0D);
        mplew.writeInt(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            try {
                getGuildInfo(mplew, c.getChannelServer().getWorldInterface().getGuild(guild, null));
            } catch (RemoteException re) {
                c.getChannelServer().reconnectWorld();
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket addGuildToAlliance(MapleAlliance alliance, int newGuild, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x12);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeInt(2);
        mplew.writeMapleAsciiString(alliance.getNotice());
        mplew.writeInt(newGuild);
        try {
            getGuildInfo(mplew, c.getChannelServer().getWorldInterface().getGuild(newGuild, null));
        } catch (RemoteException re) {
            c.getChannelServer().reconnectWorld();
        }
        return mplew.getPacket();
    }

    public static MaplePacket allianceMemberOnline(MapleCharacter mc, boolean online) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0E);
        mplew.writeInt(mc.getGuild().getAllianceId());
        mplew.writeInt(mc.getGuildId());
        mplew.writeInt(mc.getId());
        mplew.write(online ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket allianceNotice(int id, String notice) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1C);
        mplew.writeInt(id);
        mplew.writeMapleAsciiString(notice);
        return mplew.getPacket();
    }

    public static MaplePacket changeAllianceRankTitle(int alliance, String[] ranks) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1A);
        mplew.writeInt(alliance);
        for (int i = 0; i < 5; i++) {
            mplew.writeMapleAsciiString(ranks[i]);
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateAllianceJobLevel(MapleCharacter mc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x18);
        mplew.writeInt(mc.getGuild().getAllianceId());
        mplew.writeInt(mc.getGuildId());
        mplew.writeInt(mc.getId());
        mplew.writeInt(mc.getLevel());
        mplew.writeInt(mc.getJob().getId());
        return mplew.getPacket();
    }

    public static MaplePacket removeGuildFromAlliance(MapleAlliance alliance, int expelledGuild, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x10);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeInt(2);
        mplew.writeMapleAsciiString(alliance.getNotice());
        mplew.writeInt(expelledGuild);
        try {
            getGuildInfo(mplew, c.getChannelServer().getWorldInterface().getGuild(expelledGuild, null));
        } catch (RemoteException re) {
            c.getChannelServer().reconnectWorld();
        }
        mplew.write(0x01);
        return mplew.getPacket();
    }

    public static MaplePacket disbandAlliance(int alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1D);
        mplew.writeInt(alliance);
        return mplew.getPacket();
    }

    public static MaplePacket sendMesoLimit() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MESO_LIMIT.getValue()); //Players under level 15 can only trade 1m per day
        return mplew.getPacket();
    }

    public static MaplePacket sendEngagementRequest(String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //  mplew.writeShort(SendOpcode.RING_ACTION.getValue()); //<name> has requested engagement. Will you accept this proposal?
        //  mplew.write(0);
        //  mplew.writeMapleAsciiString(name); // name
        //   mplew.writeInt(10); // playerid
        return mplew.getPacket();
    }

    public static MaplePacket sendGroomWishlist() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //    mplew.writeShort(SendOpcode.RING_ACTION.getValue()); //<name> has requested engagement. Will you accept this proposal?
        // //   mplew.write(9);
        return mplew.getPacket();
    }

    public static MaplePacket sendBrideWishList(List<IItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        ///  mplew.writeShort(SendOpcode.WEDDING_ACTION.getValue());
        //  mplew.write(0x0A);
        ///  mplew.writeLong(-1); // ?
        //   mplew.writeInt(0); // ?
        ///   mplew.write(items.size());
        //   for (IItem item : items) {
        //      addItemInfo(mplew, item, true);
        // }
        return mplew.getPacket();
    }

    public static MaplePacket addItemToWeddingRegistry(MapleCharacter chr, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //   mplew.writeShort(SendOpcode.WEDDING_ACTION.getValue());
        //   mplew.write(0x0B);
        //     mplew.writeInt(0);
        //     for (int i = 0; i < 0; i++) // f4
        //         mplew.write(0);

        //     addItemInfo(mplew, item, true);
        return mplew.getPacket();
    }

    public static MaplePacket sendFamilyJoinResponse(boolean accepted, String added) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //   mplew.writeShort(SendOpcode.FAMILY_MESSAGE2.getValue());
        //   mplew.write(accepted ? 1 : 0);
        //   mplew.writeMapleAsciiString(added);
        return mplew.getPacket();
    }

    public static MaplePacket getSeniorMessage(String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //   mplew.writeShort(SendOpcode.FAMILY_SENIOR_MESSAGE.getValue());
        //   mplew.writeMapleAsciiString(name);
        //   mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket sendGainRep(int gain, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //    mplew.writeShort(SendOpcode.FAMILY_GAIN_REP.getValue());
        ////   mplew.writeInt(gain);
        //   mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket removeItemFromDuey(boolean remove, int Package) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DUEY.getValue());
        mplew.write(0x17);
        mplew.writeInt(Package);
        mplew.write(remove ? 3 : 4);
        return mplew.getPacket();
    }

    public static MaplePacket sendDueyMSG(byte operation) {
        return sendDuey(operation, null);
    }

    public static MaplePacket sendDuey(byte operation, List<DueyPackages> packages) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DUEY.getValue());
        mplew.write(operation);
        if (operation == 8) {
            mplew.write(0);
            mplew.write(packages.size());
            for (DueyPackages dp : packages) {
                mplew.writeInt(dp.getPackageId());
                mplew.writeAsciiString(dp.getSender());
                for (int i = dp.getSender().length(); i < 13; i++) {
                    mplew.write(0);
                }
                mplew.writeInt(dp.getMesos());
                mplew.writeLong(getQuestTimestamp(dp.sentTimeInMilliseconds()));
                mplew.writeLong(0); // Contains message o____o.
                for (int i = 0; i < 48; i++) {
                    mplew.writeInt(Randomizer.nextInt(Integer.MAX_VALUE));
                }
                mplew.writeInt(0);
                mplew.write(0);
                if (dp.getItem() != null) {
                    mplew.write(1);
                    addItemInfo(mplew, dp.getItem(), true);
                } else {
                    mplew.write(0);
                }
            }
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendDojoAnimation(byte firstByte, String animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BOSS_ENV.getValue());
        mplew.write(firstByte);
        mplew.writeMapleAsciiString(animation);
        return mplew.getPacket();
    }

    public static MaplePacket getDojoInfo(String info) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(10);
        mplew.write(new byte[]{(byte) 0xB7, 4});//QUEST ID f5
        mplew.writeMapleAsciiString(info);
        return mplew.getPacket();
    }

    public static MaplePacket getDojoInfoMessage(String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }

    /**
     * Gets a "block" packet (ie. the cash shop is unavailable, etc)
     *
     * Possible values for <code>type</code>:<br>
     * 1: The portal is closed for now.<br>
     * 2: You cannot go to that place.<br>
     * 3: Unable to approach due to the force of the ground.<br>
     * 4: You cannot teleport to or on this map.<br>
     * 5: Unable to approach due to the force of the ground.<br>
     * 6: This map can only be entered by party members.<br>
     * 7: The Cash Shop is currently not available. Stay tuned...<br>
     *
     * @param type The type
     * @return The "block" packet.
     */
    public static MaplePacket blockedMessage(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //  mplew.writeShort(SendOpcode.BLOCK_MESSAGE.getValue());
        //  mplew.write(type);
        return mplew.getPacket();
    }

    /**
     * Gets a "block" packet (ie. the cash shop is unavailable, etc)
     *
     * Possible values for <code>type</code>:<br>
     * 1: You cannot move that channel. Please try again later.<br>
     * 2: You cannot go into the cash shop. Please try again later.<br>
     * 3: The Item-Trading Shop is currently unavailable. Please try again later.<br>
     * 4: You cannot go into the trade shop, due to limitation of user count.<br>
     * 5: You do not meet the minimum level requirement to access the Trade Shop.<br>
     *
     * @param type The type
     * @return The "block" packet.
     */
    public static MaplePacket blockedMessage2(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //  mplew.writeShort(SendOpcode.BLOCK_MESSAGE2.getValue());
        //  mplew.write(type);
        return mplew.getPacket();
    }

    public static MaplePacket updateDojoStats(MapleCharacter chr, int belt) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(10);
        mplew.write(new byte[]{(byte) 0xB7, 4}); //?
        mplew.writeMapleAsciiString("pt=" + chr.getDojoPoints() + ";belt=" + belt + ";tuto=" + (chr.getFinishedDojoTutorial() ? "1" : "0"));
        return mplew.getPacket();
    }

    /**
     * Sends a "levelup"  packet to the guild or family.
     *
     * Possible values for <code>type</code>:<br>
     * 0: <Family> ? has reached Lv. ?.<br>
     * - The Reps you have received from ? will be reduced in half.
     * 1: <Family> ? has reached Lv. ?.<br>
     * 2: <Guild> ? has reached Lv. ?.<br>
     *
     * @param type The type
     * @return The "levelup" packet.
     */
    public static MaplePacket levelUpMessage(int type, int level, String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /*    mplew.writeShort(SendOpcode.LEVELUP_MSG.getValue());
        mplew.write(type);
        mplew.writeInt(level);
        mplew.writeMapleAsciiString(charname);
         */
        return mplew.getPacket();
    }

    /**
     * Sends a "married"  packet to the guild or family.
     *
     * Possible values for <code>type</code>:<br>
     * 0: <Guild ? is now married. Please congratulate them.<br>
     * 1: <Family ? is now married. Please congratulate them.<br>
     *
     * @param type The type
     * @return The "married" packet.
     */
    public static MaplePacket marriageMessage(int type, String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        // mplew.writeShort(SendOpcode.MARRIAGE_MSG.getValue());
        // mplew.write(type);
        // mplew.writeMapleAsciiString("> " + charname); //To fix the stupid packet lol

        return mplew.getPacket();
    }

    /**
     * Sends a "job advance"  packet to the guild or family.
     *
     * Possible values for <code>type</code>:<br>
     * 0: <Guild ? has advanced to a(an) ?.<br>
     * 1: <Family ? has advanced to a(an) ?.<br>
     *
     * @param type The type
     * @return The "job advance" packet.
     */
    public static MaplePacket jobMessage(int type, int job, String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //  mplew.writeShort(SendOpcode.JOB_MSG.getValue());
        //  mplew.write(type);
        //   mplew.writeInt(job); //Why fking int?
        //   mplew.writeMapleAsciiString("> " + charname); //To fix the stupid packet lol

        return mplew.getPacket();
    }

    /**
     *
     * @param type - (0:Light&Long 1:Heavy&Short)
     * @param delay - seconds
     * @return
     */
    public static MaplePacket trembleEffect(int type, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.BOSS_ENV.getValue());
        mplew.write(1);
        mplew.write(type);
        mplew.writeInt(delay);
        return mplew.getPacket();
    }

    public static MaplePacket getEnergy(int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ENERGY.getValue());
        mplew.writeMapleAsciiString("energy");
        mplew.writeMapleAsciiString(Integer.toString(level));
        return mplew.getPacket();
    }

    public static MaplePacket dojoWarpUp() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DOJO_WARP_UP.getValue());
        mplew.write(0);
        mplew.write(6);
        return mplew.getPacket();
    }

    public static MaplePacket itemExpired(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(2);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    private static String getRightPaddedStr(String in, char padchar, int length) {
        StringBuilder builder = new StringBuilder(in);
        for (int x = in.length(); x < length; x++) {
            builder.append(padchar);
        }
        return builder.toString();
    }

    public static MaplePacket MobDamageMobFriendly(MapleMonster mob, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.write(1); // direction ?
        mplew.writeInt(damage);
        int remainingHp = mob.getHp() - damage;
        if (remainingHp <= 0) {
            remainingHp = 0;
            mob.getMap().removeMapObject(mob);
        }
        mob.setHp(remainingHp);
        mplew.writeInt(remainingHp);
        mplew.writeInt(mob.getMaxHp());
        return mplew.getPacket();
    }

    public static MaplePacket shopErrorMessage(int error, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x0A);
        mplew.write(type);
        mplew.write(error);
        return mplew.getPacket();
    }

    private static void addRingInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(chr.getCrushRings().size());
        for (MapleRing ring : chr.getCrushRings()) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
            mplew.writeInt(ring.getRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getPartnerRingId());
            mplew.writeInt(0);
        }
        mplew.writeShort(chr.getFriendshipRings().size());
        for (MapleRing ring : chr.getFriendshipRings()) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
            mplew.writeInt(ring.getRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getPartnerRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getItemId());
        }
        mplew.writeShort(chr.getMarriageRing() != null ? 1 : 0);
        int marriageId = 30000;
        if (chr.getMarriageRing() != null) {
            mplew.writeInt(marriageId);
            mplew.writeInt(chr.getId());
            mplew.writeInt(chr.getMarriageRing().getPartnerChrId());
            mplew.writeShort(3);
            mplew.writeInt(chr.getMarriageRing().getRingId());
            mplew.writeInt(chr.getMarriageRing().getPartnerRingId());
            mplew.writeAsciiString(getRightPaddedStr(chr.getName(), '\0', 13));
            mplew.writeAsciiString(getRightPaddedStr(chr.getMarriageRing().getPartnerName(), '\0', 13));
        }
    }

    public static MaplePacket finishedSort(int inv) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.FINISH_SORT.getValue());
        mplew.write(0);
        mplew.write(inv);
        return mplew.getPacket();
    }

    public static MaplePacket finishedSort2(int inv) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.FINISH_SORT2.getValue());
        mplew.write(0);
        mplew.write(inv);
        return mplew.getPacket();
    }

    public static MaplePacket bunnyPacket() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeAsciiString("Protect the Moon Bunny!!!");
        return mplew.getPacket();
    }

    public static MaplePacket hpqMessage(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAP_EFFECT.getValue()); // not 100% sure
        mplew.write(0);
        mplew.writeInt(5120016);
        mplew.writeAsciiString(text);
        return mplew.getPacket();
    }

    public static MaplePacket showHPQMoon() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x83); // maybe?
        mplew.writeInt(-1);
        return mplew.getPacket();
    }

    public static MaplePacket showEventInstructions() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.GMEVENT_INSTRUCTIONS.getValue());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket leftKnockBack() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendOpcode.LEFT_KNOCK_BACK.getValue());
        return mplew.getPacket();
    }

    public static MaplePacket rollSnowBall(boolean entermap, int type, MapleSnowball ball0, MapleSnowball ball1) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.ROLL_SNOWBALL.getValue());
        if (entermap) {
            mplew.write0(21);
        } else {
            mplew.write(type);// 0 = move, 1 = roll, 2 is down disappear, 3 is up disappear
            mplew.writeInt(ball0.getSnowmanHP() / 75);
            mplew.writeInt(ball1.getSnowmanHP() / 75);
            mplew.writeShort(ball0.getPosition());//distance snowball down, 84 03 = max
            mplew.write(-1);
            mplew.writeShort(ball1.getPosition());//distance snowball up, 84 03 = max
            mplew.write(-1);
        }
        return mplew.getPacket();
    }

    public static MaplePacket hitSnowBall(int what, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.HIT_SNOWBALL.getValue());
        mplew.write(what);
        mplew.writeInt(damage);
        return mplew.getPacket();
    }

    /**
     * Sends a Snowball Message<br>
     *
     * Possible values for <code>message</code>:<br>
     * 1: ... Team's snowball has passed the stage 1.<br>
     * 2: ... Team's snowball has passed the stage 2.<br>
     * 3: ... Team's snowball has passed the stage 3.<br>
     * 4: ... Team is attacking the snowman, stopping the progress<br>
     * 5: ... Team is moving again<br>
     *
     * @param message
     **/
    public static MaplePacket snowballMessage(int team, int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.SNOWBALL_MESSAGE.getValue());
        mplew.write(team);// 0 is down, 1 is up
        mplew.writeInt(message);
        return mplew.getPacket();
    }

    public static MaplePacket coconutScore(int team1, int team2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        //  mplew.writeShort(SendOpcode.COCONUT_SCORE.getValue());
        ////  mplew.writeShort(team1);
        //   mplew.writeShort(team2);
        return mplew.getPacket();
    }

    public static MaplePacket hitCoconut(boolean spawn, int id, int type) {
        // // FF 00 00 00 00 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        //  mplew.writeShort(SendOpcode.HIT_COCONUT.getValue());
        //  if (spawn) {
        ////      mplew.write(new byte[]{0, (byte) 0x80, 0, 0, 0}); //00 80 00 00 00
        //} else {
        ///   mplew.writeInt(id);
        // mplew.write(type); // What action to do for the coconut.
        //}
        return mplew.getPacket();
    }

    public static MaplePacket customPacket(String packet) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString(packet));
        return mplew.getPacket();
    }

    public static MaplePacket spawnGuide(boolean spawn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        //  mplew.writeShort(SendOpcode.SPAWN_GUIDE.getValue());
        //   if (spawn) {
        //       mplew.write(1);
        //   } else {
        //       mplew.write(0);
        ///   }
        return mplew.getPacket();
    }

    public static MaplePacket talkGuide(String talk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        // mplew.writeShort(SendOpcode.TALK_GUIDE.getValue());
        // mplew.write(0);
        //  mplew.writeMapleAsciiString(talk);
        //  mplew.write(new byte[]{(byte)0xC8, 0, 0, 0, (byte) 0xA0, (byte) 0x0F, 0, 0});
        return mplew.getPacket();
    }

    public static MaplePacket guideHint(int hint) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        //   mplew.writeShort(SendOpcode.getValue());
        //   mplew.write(1);
        //   mplew.writeInt(hint);
        //   mplew.writeInt(7000);
        return mplew.getPacket();
    }

    public static void addCashItemInformation(MaplePacketLittleEndianWriter mplew, IItem item, int accountId) {
        addCashItemInformation(mplew, item, accountId, null);
    }

    public static void addCashItemInformation(MaplePacketLittleEndianWriter mplew, IItem item, int accountId, String giftMessage) {
        boolean isGift = giftMessage != null;
        mplew.writeLong(item.getPetId() > -1 ? item.getPetId() : item.getCashId());
        if (!isGift) {
            mplew.writeInt(accountId);
            mplew.writeInt(0);
        }
        mplew.writeInt(item.getItemId());
        if (!isGift) {
            mplew.writeInt(item.getSN());
            mplew.writeShort(item.getQuantity());
        }
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(item.getGiftFrom(), '\0', 13));
        if (isGift) {
            mplew.writeAsciiString(StringUtil.getRightPaddedStr(giftMessage, '\0', 73));
            return;
        }
        addExpirationTime(mplew, item.getExpiration());
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeShort(0);
    }

    public static MaplePacket showWishList(MapleCharacter mc, boolean update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        if (update) {
            mplew.write(0x55);
        } else {
            mplew.write(0x4F);
        }

        for (int sn : mc.getCashShop().getWishList()) {
            mplew.writeInt(sn);
        }

        for (int i = mc.getCashShop().getWishList().size(); i < 10; i++) {
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static MaplePacket showBoughtCashItem(IItem item, int accountId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        mplew.write(0x79); // v98
        addCashItemInformation(mplew, item, accountId);

        return mplew.getPacket();
    }

    /*
     * 00 = Due to an unknown error, failed
     * A4 = Due to an unknown error, failed + warpout
     * A5 = You don't have enough cash.
     * A6 = long as shet msg
     * A7 = You have exceeded the allotted limit of price for gifts.
     * A8 = You cannot send a gift to your own account. Log in on the char and purchase
     * A9 = Please confirm whether the character's name is correct.
     * AA = Gender restriction!
     * //Skipped a few
     * B0 = Wrong Coupon Code
     * B1 = Disconnect from CS because of 3 wrong coupon codes < lol
     * B2 = Expired Coupon
     * B3 = Coupon has been used already
     * B4 = Nexon internet cafes? lolfk
     *
     * BB = inv full
     * C2 = not enough mesos? Lol not even 1 mesos xD
     */
    public static MaplePacket showCashShopMessage(byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        mplew.write(0x68);
        mplew.write(message);

        return mplew.getPacket();
    }

    public static MaplePacket showCashInventory(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        mplew.write(0x4B);
        mplew.writeShort(c.getPlayer().getCashShop().getInventory().size());

        for (IItem item : c.getPlayer().getCashShop().getInventory()) {
            addCashItemInformation(mplew, item, c.getAccID());
        }

        mplew.writeShort(c.getPlayer().getStorage().getSlots());
        mplew.writeShort(c.getCharacterSlots());

        return mplew.getPacket();
    }

    public static MaplePacket showGifts(List<Pair<IItem, String>> gifts) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        mplew.write(0x4D);
        mplew.writeShort(gifts.size());

        for (Pair<IItem, String> gift : gifts) {
            addCashItemInformation(mplew, gift.getLeft(), 0, gift.getRight());
        }

        return mplew.getPacket();
    }

    public static MaplePacket showGiftSucceed(String to, CashItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        mplew.write(0x5E); //0x5D, Couldn't be sent
        mplew.writeMapleAsciiString(to);
        mplew.writeInt(item.getItemId());
        mplew.writeShort(item.getCount());
        mplew.writeInt(item.getPrice());

        return mplew.getPacket();
    }

    public static MaplePacket showBoughtInventorySlots(int type, short slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());;

        mplew.write(0x6E);
        mplew.write(type);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static MaplePacket showBoughtStorageSlots(short slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        mplew.write(0x70);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static MaplePacket showBoughtCharacterSlot(short slots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        mplew.write(0x64);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static MaplePacket takeFromCashInventory(IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        mplew.write(0x68);
        mplew.writeShort(item.getPosition());
        addItemInfo(mplew, item, true);

        return mplew.getPacket();
    }

    public static MaplePacket putIntoCashInventory(IItem item, int accountId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_OPERATION.getValue());

        mplew.write(0x6A);
        addCashItemInformation(mplew, item, accountId);

        return mplew.getPacket();
    }

    public static MaplePacket openCashShop(MapleClient c, boolean mts) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        MapleCharacter chr = c.getPlayer();
        mplew.writeShort(SendOpcode.CS_OPEN.getValue());
        addCharacterData(mplew, chr);
        //  mplew.writeInt(0);
        mplew.write(1);
        mplew.writeMapleAsciiString(c.getAccountName());
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 22 00 4A 26 9A 00 00 04 00 00 00 81 A0 98 00 00 04 00 00 00 4B 26 9A 00 00 04 00 00 00 48 26 9A 00 00 04 00 00 00 49 26 9A 00 00 04 00 00 00 AF 29 9A 00 00 04 00 00 00 AE 29 9A 00 00 04 00 00 00 47 26 9A 00 00 04 00 00 00 45 FE FD 02 00 04 00 00 00 46 FE FD 02 00 04 00 00 00 47 FE FD 02 00 04 00 00 00 48 27 9A 00 00 04 00 00 00 10 9F 98 00 00 04 00 00 00 11 9F 98 00 00 04 00 00 00 12 9F 98 00 00 04 00 00 00 B3 29 9A 00 00 04 00 00 00 B0 29 9A 00 00 04 00 00 00 55 FE FD 02 00 04 00 00 00 98 FE FD 02 00 04 00 00 00 54 FE FD 02 00 04 00 00 00 99 FE FD 02 00 04 00 00 00 B4 29 9A 00 00 04 00 00 00 47 27 9A 00 00 04 00 00 00 7A 87 93 03 00 04 00 00 00 78 87 93 03 00 04 00 00 00 61 9F 98 00 00 04 00 00 00 79 87 93 03 00 04 00 00 00 60 9F 98 00 00 04 00 00 00 77 87 93 03 00 04 00 00 00 A1 A1 98 00 00 04 00 00 00 A0 A1 98 00 00 04 00 00 00 A3 A1 98 00 00 04 00 00 00 A2 A1 98 00 00 04 00 00 00 A4 A1 98 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 93 FE FD 02 01 00 00 00 00 00 00 00 C4 FD FD 02 01 00 00 00 00 00 00 00 F9 96 98 00 01 00 00 00 00 00 00 00 E8 9E 98 00 01 00 00 00 00 00 00 00 1A 97 98 00 01 00 00 00 01 00 00 00 93 FE FD 02 01 00 00 00 01 00 00 00 C4 FD FD 02 01 00 00 00 01 00 00 00 F9 96 98 00 01 00 00 00 01 00 00 00 E8 9E 98 00 01 00 00 00 01 00 00 00 1A 97 98 00 02 00 00 00 00 00 00 00 93 FE FD 02 02 00 00 00 00 00 00 00 C4 FD FD 02 02 00 00 00 00 00 00 00 F9 96 98 00 02 00 00 00 00 00 00 00 E8 9E 98 00 02 00 00 00 00 00 00 00 1A 97 98 00 02 00 00 00 01 00 00 00 93 FE FD 02 02 00 00 00 01 00 00 00 C4 FD FD 02 02 00 00 00 01 00 00 00 F9 96 98 00 02 00 00 00 01 00 00 00 E8 9E 98 00 02 00 00 00 01 00 00 00 1A 97 98 00 03 00 00 00 00 00 00 00 93 FE FD 02 03 00 00 00 00 00 00 00 C4 FD FD 02 03 00 00 00 00 00 00 00 F9 96 98 00 03 00 00 00 00 00 00 00 E8 9E 98 00 03 00 00 00 00 00 00 00 1A 97 98 00 03 00 00 00 01 00 00 00 93 FE FD 02 03 00 00 00 01 00 00 00 C4 FD FD 02 03 00 00 00 01 00 00 00 F9 96 98 00 03 00 00 00 01 00 00 00 E8 9E 98 00 03 00 00 00 01 00 00 00 1A 97 98 00 04 00 00 00 00 00 00 00 93 FE FD 02 04 00 00 00 00 00 00 00 C4 FD FD 02 04 00 00 00 00 00 00 00 F9 96 98 00 04 00 00 00 00 00 00 00 E8 9E 98 00 04 00 00 00 00 00 00 00 1A 97 98 00 04 00 00 00 01 00 00 00 93 FE FD 02 04 00 00 00 01 00 00 00 C4 FD FD 02 04 00 00 00 01 00 00 00 F9 96 98 00 04 00 00 00 01 00 00 00 E8 9E 98 00 04 00 00 00 01 00 00 00 1A 97 98 00 05 00 00 00 00 00 00 00 93 FE FD 02 05 00 00 00 00 00 00 00 C4 FD FD 02 05 00 00 00 00 00 00 00 F9 96 98 00 05 00 00 00 00 00 00 00 E8 9E 98 00 05 00 00 00 00 00 00 00 1A 97 98 00 05 00 00 00 01 00 00 00 93 FE FD 02 05 00 00 00 01 00 00 00 C4 FD FD 02 05 00 00 00 01 00 00 00 F9 96 98 00 05 00 00 00 01 00 00 00 E8 9E 98 00 05 00 00 00 01 00 00 00 1A 97 98 00 06 00 00 00 00 00 00 00 93 FE FD 02 06 00 00 00 00 00 00 00 C4 FD FD 02 06 00 00 00 00 00 00 00 F9 96 98 00 06 00 00 00 00 00 00 00 E8 9E 98 00 06 00 00 00 00 00 00 00 1A 97 98 00 06 00 00 00 01 00 00 00 93 FE FD 02 06 00 00 00 01 00 00 00 C4 FD FD 02 06 00 00 00 01 00 00 00 F9 96 98 00 06 00 00 00 01 00 00 00 E8 9E 98 00 06 00 00 00 01 00 00 00 1A 97 98 00 07 00 00 00 00 00 00 00 93 FE FD 02 07 00 00 00 00 00 00 00 C4 FD FD 02 07 00 00 00 00 00 00 00 F9 96 98 00 07 00 00 00 00 00 00 00 E8 9E 98 00 07 00 00 00 00 00 00 00 1A 97 98 00 07 00 00 00 01 00 00 00 93 FE FD 02 07 00 00 00 01 00 00 00 C4 FD FD 02 07 00 00 00 01 00 00 00 F9 96 98 00 07 00 00 00 01 00 00 00 E8 9E 98 00 07 00 00 00 01 00 00 00 1A 97 98 00 08 00 00 00 00 00 00 00 93 FE FD 02 08 00 00 00 00 00 00 00 C4 FD FD 02 08 00 00 00 00 00 00 00 F9 96 98 00 08 00 00 00 00 00 00 00 E8 9E 98 00 08 00 00 00 00 00 00 00 1A 97 98 00 08 00 00 00 01 00 00 00 93 FE FD 02 08 00 00 00 01 00 00 00 C4 FD FD 02 08 00 00 00 01 00 00 00 F9 96 98 00 08 00 00 00 01 00 00 00 E8 9E 98 00 08 00 00 00 01 00 00 00 1A 97 98 00 00 00 00 00 00 00 00 A2 00 00 00"));
        /* mplew.writeInt(0);
        mplew.writeShort(0); // Modifier Size (Int, Int, Byte)
        mplew.writeShort(0);
        for (int i = 0; i < 14; i++) {
        mplew.writeLong(0);
        }
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        final int[] itemArray = new int[]{50200004, 50200069, 50200117, 50100008, 50000047};
        for (int i = 1; i <= 8; i++) {
        for (int j = 0; j < 2; j++) {
        for (int item : itemArray) {
        mplew.writeInt(i);
        mplew.writeInt(j);
        mplew.writeInt(item);
        }
        }
        }
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(75);*/
        return mplew.getPacket();
    }

    /*public static MaplePacket temporarySkills() {
    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
    mplew.writeShort(SendOpcode.TEMPORARY_STATS_RESET.getValue());
    return mplew.getPacket();
    }*/
    public static MaplePacket showCombo(int count) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.ARAN_COMBO_COUNTER.getValue());
        mplew.writeInt(count);
        return mplew.getPacket();
    }

    public static MaplePacket startCPQ(MapleCharacter chr, MonsterCarnivalParty enemy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_START.getValue());
        mplew.write(chr.getTeam()); //team
        mplew.writeShort(chr.getCP()); //Obtained CP - Used CP
        mplew.writeShort(chr.getObtainedCP()); //Total Obtained CP
        mplew.writeShort(chr.getCarnivalParty().getAvailableCP()); //Obtained CP - Used CP of the team
        mplew.writeShort(chr.getCarnivalParty().getTotalCP()); //Total Obtained CP of the team
        mplew.writeShort(enemy.getAvailableCP()); //Obtained CP - Used CP of the team
        mplew.writeShort(enemy.getTotalCP()); //Total Obtained CP of the team
        mplew.writeShort(0); //Probably useless nexon shit
        mplew.writeLong(0); //Probably useless nexon shit
        return mplew.getPacket();
    }

    public static MaplePacket updateCP(int cp, int tcp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_OBTAINED_CP.getValue());
        mplew.writeShort(cp); //Obtained CP - Used CP
        mplew.writeShort(tcp); //Total Obtained CP
        return mplew.getPacket();
    }

    public static MaplePacket updatePartyCP(MonsterCarnivalParty party) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_PARTY_CP.getValue());
        mplew.write(party.getTeam()); //Team where the points are given to.
        mplew.writeShort(party.getAvailableCP()); //Obtained CP - Used CP
        mplew.writeShort(party.getTotalCP()); //Total Obtained CP
        return mplew.getPacket();
    }

    public static MaplePacket CPQSummon(int tab, int number, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
        mplew.write(tab); //Tab
        mplew.writeShort(number); //Number of summon inside the tab
        mplew.writeMapleAsciiString(name); //Name of the player that summons
        return mplew.getPacket();
    }

    public static MaplePacket CPQDied(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
        mplew.write(chr.getTeam()); //Team
        mplew.writeMapleAsciiString(chr.getName()); //Name of the player that died
        mplew.write(chr.getAndRemoveCP()); //Lost CP
        return mplew.getPacket();
    }

    /**
     * Sends a CPQ Message<br>
     *
     * Possible values for <code>message</code>:<br>
     * 1: You don't have enough CP to continue.<br>
     * 2: You can no longer summon the Monster.<br>
     * 3: You can no longer summon the being.<br>
     * 4: This being is already summoned.<br>
     * 5: This request has failed due to an unknown error.<br>
     *
     * @param message Displays a message inside Carnival PQ
     **/
    public static MaplePacket CPQMessage(byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        // /   mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_MESSAGE.getValue());
        //    mplew.write(message); //Message
        return mplew.getPacket();
    }

    public static MaplePacket leaveCPQ(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //    mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_LEAVE.getValue());
        //    mplew.write(0); //Something
        //   mplew.write(chr.getTeam()); //Team
        //   mplew.writeMapleAsciiString(chr.getName()); //Player name
        return mplew.getPacket();
    }

    public static MaplePacket showInventoryFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        //  mplew.writeShort(SendOpcode.SOMETHING_WITH_INVENTORY.getValue());
        //   mplew.write0(6);
        return mplew.getPacket();
    }

    public static MaplePacket spawnDragon(MapleDragon dragon, boolean owner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_DRAGON.getValue());
        mplew.writeInt(dragon.getOwnerId());
        mplew.writeInt(dragon.getPosition().x);
        mplew.writeInt(dragon.getPosition().y);
        mplew.write(dragon.getStance());
        mplew.writeShort(0);
        mplew.writeShort(dragon.getOwnerJobId());
        return mplew.getPacket();
    }

    public static MaplePacket removeDragon(int ownerid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.REMOVE_DRAGON.getValue());
        mplew.writeInt(ownerid);
        return mplew.getPacket();
    }

    public static MaplePacket MapMessage(String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MAP_MESSAGE.getValue());
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }

    public static MaplePacket showPotentialffect(int id, boolean legendary) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(9);
        mplew.writeShort(SendOpcode.SHOW_POTENTIAL_EFFECT.getValue());
        mplew.writeInt(id);
        mplew.write(1);
        mplew.writeShort(legendary ? 1 : 0);
        mplew.writeInt(1);
        return mplew.getPacket();
    }

    public static MaplePacket showMagnifyingEffect(int itemid, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendOpcode.SHOW_MAGNIFYING_EFFECT.getValue());
        mplew.writeInt(itemid);
        mplew.writeShort(slot);
        return mplew.getPacket();
    }

    public static MaplePacket showCubeEffect(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendOpcode.SHOW_CUBE_EFFECT.getValue());
        mplew.writeInt(id);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket spawnMechanicSummon(MapleSummon summon) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(summon.getSkill());
        mplew.write(summon.getOwner().getLevel());
        switch (summon.getSkill()) {
            case Mechanic.HEALING_ROBOT:
                mplew.write(5);
                break;
            case Mechanic.SATELLITE1:
                mplew.write(10);
                break;
            case Mechanic.ROCK_N_SHOCK:
                mplew.write(20);
                break;
        }
        mplew.writeShort(summon.getPosition().x);
        mplew.writeShort(summon.getPosition().y);
        mplew.writeShort(4); // animation type
        mplew.write(0);
        if (summon.isStationary()) {
            mplew.write(0); // can attack boolean
        } else {
            mplew.write(1); // can attack boolean
        }
        mplew.write(summon.getMovementType().getValue());
        mplew.writeShort(1);
        if (summon.getSkill() == Mechanic.ROCK_N_SHOCK) {
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeMechanicSummon(MapleSummon summon) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId());
        if (summon.isStationary()) {
            mplew.write(5);
        } else {
            mplew.write(10);
        }
        return mplew.getPacket();
    }

//[FA 98 04 00] [01] [01]
    public static MaplePacket catchJaguar(int monsobid, byte success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CATCH_MOUNT.getValue());
        mplew.writeInt(monsobid);
        mplew.write(success);
        mplew.write(1); // direction?
        return mplew.getPacket();
    }

    public static MaplePacket spawnCapturedMonster(int mid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_JAGUAR.getValue());
        mplew.write(0x28); // jaguar color
        mplew.writeInt(mid);
        mplew.writeLong(0); // gets repeated
        mplew.writeLong(0); // gets repeated
        return mplew.getPacket();
    }

    public static MaplePacket addJaguarInfo(MaplePacketLittleEndianWriter mplew) {
        //[28] [00 00 00 00 00 00 00 00] [00 00 00 00 00 00 00 00] 00 00 00 00
        // 0x28 is ...dark purple
        mplew.write(0x28); // color?
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket updateJaguar() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.UPDATE_JAGUAR.getValue());
        addJaguarInfo(mplew); // probably buffs and such
        return mplew.getPacket();
    }
}