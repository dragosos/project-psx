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
package constants;

public class ServerConstants {
    public static short VERSION = 97;
    public static String SERVERNAME = "Project PSX";
    // Rate Configuration
    public static short EXP_RATE = 5;
    public static short MESO_RATE = 10;
    public static final byte DROP_RATE = 3;
    public static final byte BOSS_DROP_RATE = 2;
    public static final byte QUEST_EXP_RATE = 4;
    public static final byte QUEST_MESO_RATE = 3;
    // Login Configuration
    public static final byte FLAG = 1;
    public static final int CHANNEL_NUMBER = 1;
    public static final int CHANNEL_LOAD = 50;
    public static final String EVENT_MESSAGE = "Welcome to Project PSX.";
    public static final long RANKING_INTERVAL = 3600000;
    public static final boolean ENABLE_PIN = false;
    public static final boolean ENABLE_PIC = false;
    // Channel Configuration
    public static String SERVER_MESSAGE = "";
    public static String RECOMMEND_MESSAGE = "";
    public static final String EVENTS = "";
    
    //Host info
    public static final String HOST = "127.0.0.1"; // Ip here
    
    // Random
    public static int[] BLOCKED_COMMANDS_MAPS = {970000004, 910010200, 200090300};
}