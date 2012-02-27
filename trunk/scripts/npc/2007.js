item = [4000235, 4000243, 4031457];
var status = 0;
var manage;
var dick;
var s;

function start() {
    var text = "Which Boss item do you want to exchange for 5,000,000 EXP?#r Must be level 120 to proceed.#b\r\n";
    for(var i = 0; i < item.length ; i++)
        text += "\r\n#L" + i + "##v" + item[i] + "##l";
    cm.sendSimple(text);
}

function action(mode, type, selection) {
	if (m < 1) {
		cm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 1) {
		s = selection;
		cm.sendGetNumber("How many of this item do you want to trade?", 0, 0, 2000000000);
		manage = 1;
	} else if (status == 2) {
		if (manage == 1) {
			dick = cm.getText();
			if (cm.getLevel() < 120) {
				cm.sendOk("You must be level 120 to complete the quest.");
				cm.dispose();
			}
			if (cm.haveItem(item[s], dick)) {
				cm.gainItem(item[s], -dick);
				cm.gainExp(5000000 * dick);
				cm.sendOk("You have received your EXP.");
			} else {
				cm.sendOk("You don't have the required Boss Item.");
			}
		}
	}
}