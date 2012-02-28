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
/* 
 * @Name         NIMAKIN 
 * @Author:      BubblesDev v62, 75, 83, 88, 89 (Moogra, XxOsirisxX) 
 * @NPC:         9900001 
 * @Purpose:     Levels people up, "starts" events. 
 * @Purpose:     Clears inventories, Bans people 
 */ 
importPackage(Packages.client); 

inventoryClearMap = 180000000; 
removeAll = false; 
var id; 

status = 0; 

banningArray = [ 
    ["Click here to get candy", "Candy doesn't come free even for a GM"], 
    ["Special (Do not click)", "Not reading \"Do not click\""], 
    ]; 

banningElement = -1; 
chosenInventory = -1; 

function start() { 
    if (cm.getMapId() == 109040000){ 
        cm.sendOk("#e[MapleStory Physical Fitness Test]\r\n\r\nHi there, thank you for participating in our event game.\r\nHere's a little introduction about the game.\r\n\r\n[MapleStory Physical Fitness Test]is a race through an obstacle course much like the Forest of Patience. You can win by overcoming various obstacles and reaching the final destination within the time limit.\r\nThe game consist of #rfour levels#k, and the time limit is #b15 MINUTES#k. During the [MapleStory Physical Fitness Test], you will not be able to use #bteleport or haste#k.\r\n\r\nIf you have any other question regarding the game, feel free to ask during the game."); 
        cm.dispose(); 
    //    } else if (cm.getMapId() == 180000000 && c.getPlayer().gmLevel() > 0) { 
    //        cm.getPlayer().levelUp(false); 
    //        cm.dispose(); 
    } else if (cm.getMapId() == inventoryClearMap && cm.getPlayer().isGM()) { 
        cm.sendYesNo("Hi I am KIN Version 2. 1.1 has been upgraded. Do you want to clear or remove some things from your inventory?"); 
    } else { 
        cm.dispose(); 
    //        cm.runDinamycEvent("Test"); // osiris can't spell dynamic 
    } 
} 

function action(mode, type, selection) { 
    if (mode != 1) { 
        if (mode == 0) { 
            if (cm.getPlayer().isGM()) { 
                cm.sendOk("Come back any time."); 
            } 
        } 
        cm.dispose(); 
        return; 
    } 
    status++; 
    if (cm.getPlayer().isGM()) { 
        if (status == 1) { 
            cm.sendSimple("Which inventory removal type do you want? \r\n#L1#Full Inventory Wipe#l \r\n#L2#Individual Item Selection#l"); 
        } else if (status == 2) { 
            if (selection == 1) { 
                removeAll = true; 
                cm.sendSimple("Which inventory to kill? #r#eWarning: ALL ITEMS WILL BE DESTROYED.\r\n#L1#Equip#l \r\n#L2#Use#l \r\n #L3#Setup#l \r\n #L4#Etc#l \r\n #L5#Cash#l"); 
            } else if (selection == 2) { 
                cm.sendSimple("Which inventory do you want to remove items from?.\r\n#L1#EQUIP#l \r\n#L2#USE#l \r\n #L3#SETUP#l \r\n #L4#Etc#l \r\n #L5#CASH#l"); 
            } else if (selection == 3) {  
                cm.dispose(); 
            } 
        } else if (status == 3) { 
            if (chosenInventory < 0) { 
                chosenInventory = selection; 
            } 
            var itemList = cm.getPlayer().getInventory(MapleInventoryType.CASH);//.getItemArrayList(); 
            var iter = itemList.iterator(); 
            if (itemList.size() == 0) { 
                cm.sendNext("Your inventory is empty! Nothing to remove here."); 
                cm.dispose(); 
                return; 
            } 
            var sb = ""; 
            while (iter.hasNext()) { 
                var item = iter.next(); 
                if (removeAll) { 
                    cm.gainItem(item.getItemId(), -item.getQuantity()); 
                } else { 
                    sb += "#L" + item.getItemId() + "##v" + item.getItemId() + "##l"; 
                } 
            } 
            if (removeAll) { 
                cm.sendOk("Okay your items have been removed!"); 
                cm.dispose(); 
            } else { 
                cm.sendSimple(sb); 
            } 
        } else if (status == 4) { 
            id = selection; 
            cm.sendYesNo("Do you want to remove #z" + id + "#? There is no turning back."); 
        } else if (status == 5) { 
            cm.gainItem(id, -1); 
            cm.sendYesNo("I have removed your #z" + id + "#. Do you want to remove more stuff?"); 
        } else if (status == 6) { 
            cm.sendNext("Okay I will redirect you to the previous screen..."); 
            status = 2; 
        } 
    } 
}  