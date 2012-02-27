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
package client;

public enum MapleJob {

    BEGINNER(0),
    WARRIOR(100),
    FIGHTER(110),
    CRUSADER(111),
    HERO(112),
    PAGE(120),
    WHITEKNIGHT(121),
    PALADIN(122),
    SPEARMAN(130),
    DRAGONKNIGHT(131),
    DARKKNIGHT(132),
    MAGICIAN(200),
    FP_WIZARD(210),
    FP_MAGE(211),
    FP_ARCHMAGE(212),
    IL_WIZARD(220),
    IL_MAGE(221),
    IL_ARCHMAGE(222),
    CLERIC(230),
    PRIEST(231),
    BISHOP(232),
    BOWMAN(300),
    HUNTER(310),
    RANGER(311),
    BOWMASTER(312),
    CROSSBOWMAN(320),
    SNIPER(321),
    MARKSMAN(322),
    THIEF(400),
    ASSASSIN(410),
    HERMIT(411),
    NIGHTLORD(412),
    BANDIT(420),
    CHIEFBANDIT(421),
    SHADOWER(422),
    BLADE_RECRUIT(430),
    BLADE_ACOLYTE(431),
    BLADE_SPECIALIST(432),
    BLADE_LORD(433),
    BLADE_MASTER(434),
    PIRATE(500),
    BRAWLER(510),
    MARAUDER(511),
    BUCCANEER(512),
    GUNSLINGER(520),
    OUTLAW(521),
    CORSAIR(522),
    MAPLELEAF_BRIGADIER(800),
    GM(900),
    SUPERGM(910),
    NOBLESSE(1000),
    DAWNWARRIOR1(1100),
    DAWNWARRIOR2(1110),
    DAWNWARRIOR3(1111),
    DAWNWARRIOR4(1112),
    BLAZEWIZARD1(1200),
    BLAZEWIZARD2(1210),
    BLAZEWIZARD3(1211),
    BLAZEWIZARD4(1212),
    WINDARCHER1(1300),
    WINDARCHER2(1310),
    WINDARCHER3(1311),
    WINDARCHER4(1312),
    NIGHTWALKER1(1400),
    NIGHTWALKER2(1410),
    NIGHTWALKER3(1411),
    NIGHTWALKER4(1412),
    THUNDERBREAKER1(1500),
    THUNDERBREAKER2(1510),
    THUNDERBREAKER3(1511),
    THUNDERBREAKER4(1512),
    LEGEND(2000),
    FARMER(2001),
    ARAN1(2100),
    ARAN2(2110),
    ARAN3(2111),
    ARAN4(2112),
    EVAN1(2200),
    EVAN2(2210),
    EVAN3(2211),
    EVAN4(2212),
    EVAN5(2213),
    EVAN6(2214),
    EVAN7(2215),
    EVAN8(2216),
    EVAN9(2217),
    EVAN10(2218),
    CITIZEN(3000),
    BATTLEMAGE1(3200),
    BATTLEMAGE2(3210),
    BATTLEMAGE3(3211),
    BATTLEMAGE4(3212),
    WILDHUNTER1(3300),
    WILDHUNTER2(3310),
    WILDHUNTER3(3311),
    WILDHUNTER4(3312),
    MECHANIC1(3500),
    MECHANIC2(3510),
    MECHANIC3(3511),
    MECHANIC4(3512),
    ADDITIONAL_SKILLS(9000);
    final int jobid;

    private MapleJob(int id) {
        jobid = id;
    }

    public int getId() {
        return jobid;
    }

    public boolean canBeMade(int id){
        MapleJob request = getById(id);
        if(request == null){
            return false;
        }  else {
            switch(id){
                case 0: // beginner
                case 1000: // noblesse
                case 2000: // aran
                case 3000: //mechanic
                case 2001: // farmer(evan)
                    return true;
                default:
                    return false; // anything else

            }
        }
    }

    public static MapleJob getById(int id) {
        for (MapleJob l : MapleJob.values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    public static MapleJob getBy5ByteEncoding(int encoded) {
        switch (encoded) {
            case 2:
                return WARRIOR;
            case 4:
                return MAGICIAN;
            case 8:
                return BOWMAN;
            case 16:
                return THIEF;
            case 32:
                return PIRATE;
            case 1024:
                return NOBLESSE;
            case 2048:
                return DAWNWARRIOR1;
            case 4096:
                return BLAZEWIZARD1;
            case 8192:
                return WINDARCHER1;
            case 16384:
                return NIGHTWALKER1;
            case 32768:
                return THUNDERBREAKER1;
            case 65536:
                return LEGEND;
            default:
                return BEGINNER;
        }
    }

    public boolean isA(MapleJob basejob) {
        return getId() >= basejob.getId() && getId() / 100 == basejob.getId() / 100;
    }

    public boolean isAnEvan() {
        return getId() == 2001 || getId() / 100 == 22;
    }

    public boolean isResistance(){
        return getId() >= 3000 && getId() <= 3512;
    }

    public static boolean isExtendSPJob(int jobId) {
        return jobId / 1000 == 3 || jobId / 100 == 22 || jobId == 2001;
    }

    public static boolean isExtendSPJob(MapleJob job) {
        return isExtendSPJob(job.getId());
    }

    public static String getJobNameById(int jobId) {
        String[] jobName;
        jobName = new String[70];
        jobName[100] = "Beginner";
        jobName[100] = "Warrior";
        jobName[110] = "Fighter";
        jobName[111] = "Crusader";
        jobName[112] = "Hero";
        jobName[120] = "Page";
        jobName[121] = "White Knight";
        jobName[122] = "Paladin";
        jobName[130] = "Spearman";
        jobName[131] = "Dragon Knight";
        jobName[132] = "Dark Knight";
        jobName[200] = "Magician";
        jobName[210] = "Fire/Poison Wizard";
        jobName[211] = "Fire/Poison Mage";
        jobName[212] = "Fire/Poison Archmage";
        jobName[220] = "Ice/Lightning Wizard";
        jobName[221] = "Ice/Lightning Mage";
        jobName[222] = "Ice/Lightning Archmage";
        jobName[230] = "Cleric";
        jobName[231] = "Priest";
        jobName[232] = "Bishop";
        jobName[300] = "Bowman";
        jobName[310] = "Hunter";
        jobName[311] = "Ranger";
        jobName[312] = "Bow Master";
        jobName[320] = "Crossbowman";
        jobName[321] = "Sniper";
        jobName[322] = "Crossbow Master";
        jobName[400] = "Thief";
        jobName[410] = "Assassin";
        jobName[411] = "Hermit";
        jobName[412] = "Night Lord";
        jobName[420] = "Bandit";
        jobName[421] = "Chieft Bandit";
        jobName[422] = "Shadower";
        jobName[500] = "Pirate";
        jobName[510] = "Brawler";
        jobName[511] = "Marauder";
        jobName[512] = "Buccaneer";
        jobName[520] = "Gunslinger";
        jobName[521] = "Outlaw";
        jobName[522] = "Corsair";
        jobName[800] = "Mapleleaf Brigadier";
        jobName[900] = "Game Master";
        jobName[910] = "Super Game Master";
        jobName[1000] = "Noblesse";
        jobName[1100] = "First Dawn Warrior";
        jobName[1110] = "Second Dawn Warrior";
        jobName[1111] = "Third Dawn Warrior";
        jobName[1112] = "Fourth Dawn Warrior";
        jobName[1200] = "First Blaze Wizard";
        jobName[1210] = "Second Blaze Wizard";
        jobName[1211] = "Third Blaze Wizard";
        jobName[1212] = "Fourth Blaze Wizard";
        jobName[1300] = "First Wind Archer";
        jobName[1310] = "Second Wind Archer";
        jobName[1311] = "Third Wind Archer";
        jobName[1312] = "Fourth Wind Archer";
        jobName[1400] = "First Night Walker";
        jobName[1410] = "Second Night Walker";
        jobName[1411] = "Third Night Walker";
        jobName[1412] = "Fourth Night Walker";
        jobName[1500] = "First Thunder Breaker";
        jobName[1510] = "Second Thunder Breaker";
        jobName[1511] = "Third Thunder Breaker";
        jobName[1512] = "Fourth Thunder Breaker";
        jobName[2000] = "Legend";
        jobName[2100] = "First Job Aran";
        jobName[2110] = "Second Job Aran";
        jobName[2111] = "Third Job Aran";
        jobName[2112] = "Fourth Job Aran";
        jobName[2200] = "Evan1";
        jobName[2210] = "Evan2";
        jobName[2211] = "Evan3";
        jobName[2212] = "Evan4";
        jobName[2213] = "Evan5";
        jobName[2214] = "Evan6";
        jobName[2215] = "Evan7";
        jobName[2216] = "Evan8";
        jobName[2217] = "Evan9";
        jobName[2218] = "Evan10";
        jobName[3000] = "Citizen";
        jobName[3200] = "Battle Mage1";
        jobName[3210] = "Battle Mage2";
        jobName[3211] = "Battle Mage3";
        jobName[3212] = "Battle Mage4";
        jobName[3300] = "Wild Hunter1";
        jobName[3310] = "Wild Hunter2";
        jobName[3311] = "Wild Hunter3";
        jobName[3312] = "Wild Hunter4";
        jobName[3500] = "Mechanic1";
        jobName[3510] = "Mechanic2";
        jobName[3511] = "Mechanic3";
        jobName[3512] = "Mechanic4";
        jobName[9000] = "Additional Skills";
        return jobName[jobId];
    }
}
