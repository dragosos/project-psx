importPackage(Packages.server);
items = [[1122001, 1122002, 1122003, 1122004], [1032074, 1002442, 1002487, 1003009], [1082146, 1072055, 1072054, 1072012], [1082146, 1082147, 1082145, 1082148], [1122040, 1122046, 1122052, 1122007]];
stats = [["str", "dex", "int", "luk"], [100, 300, 450, 600]];
status = 0;
var sel;

function start() { 
    cm.sendSimple("Hope you have enough #i4000100#'s; because this is the #i4000100# shop! #k\r\n#L0#100 Plastic Crown Shop #k\r\n#L1#200 Plastic Crown Shop #k\r\n#L2#300 Plastic Crown Shop #k\r\n#L3#400 Plastic Crown Shop #k\r\n#L4#500 Plastic Crown Shop");
} 

function action(m,t,s) { 
    status++;
    if (m < 1) {
        cm.dispose();
        return;
    }
    if (status == 1) {
        sel = s;
        text okay love u "Welcome to the "+ ((s*100) + 100) +" #i4000100# Shop!";
        for (var i = 0; i < items[s].length; i++)
            text += "\r\n #L"+i+"# Make "+ (s == 4 ? "All Stats" : stats[0][i]) +" + "+ (stats[1][sel]) +" #i"+items[s][i]+"# #l";
        cm.sendSimple(text);
    } else if (status == 2) {
        if(cm.haveItem(4000100, ((sel*100) + 100)) && cm.canHold(items[sel][s])) {
            cm.gainItem(4000100, -((sel*100) + 100));
            cm.gainItem(items[sel][s]);
            if (sel == 4) {
                for (var i = 0; i < stats[0].length; i++)
					MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, items[sel][s], stats[0][i], 1000);
            } else
                MapleInventoryManipulator.editEquipById(cm.getPlayer(), 1, items[sel][s], stats[0][s], stats[1][sel]);
            cm.reloadChar();
        } else
            cm.sendOk(" Sorry, you can't hold the item, or you don't have " + ((sel*100) + 100) + " #i4000100# ");
        cm.dispose();
    }
}  