/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import client.Equip;
import client.MapleCharacter;
import client.MapleInventoryType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sharky
 */
public class MapleCampaign {

    protected final static String[] CampaignTitles = {"Collect Zombie's Gold Tooth!"
            , }; // cleaner this way
    protected final static String[][] CampaignAssignments = {{"So? You're ready for your first assignment? \r\nZombies have been rapidly producing, and they are a potential threat to our survival. I need you to eliminate some zombies for me, and thin down their population. This is where you prove your worth. Bring me back #b300 Zombie's Gold Teeth#k as proof of your accomplishment. \r\nAre you prepared to accept this assignment?", "You haven't thinned down the Zombie population enough yet. Gather #b300 of Zombie's Gold Teeth#k and come talk to me to complete your assignment."}
            , };
    protected final static String[] CompletionText = {"It's about time, those Zombie's were starting to outnumber our forces. But thanks to you, I think we've just about trumped them. Good job #h #!"
            , };

    public static String getCampaignTitle(short campaignProgress) {
        return CampaignTitles[campaignProgress / 10 - 1];
    }

    public static String getCampaignAssignment(short campaignProgress) {
        return CampaignAssignments[campaignProgress / 10 - 1][campaignProgress % 10];
    }

    public static boolean isAssignmentCompleted(MapleCharacter chr) {
        String title = getCampaignTitle(chr.getCampaignProgress());
        if (title != null) {
            if (title.equals("Collect Zombie's Gold Tooth!")) {
                if (chr.getItemQuantity(4000082, false) >= 300) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getCompletionText(MapleCharacter chr) {
        int[] reward = completeAssignment(chr);
        return CompletionText[chr.getCampaignProgress() / 10 - 1] + "\r\nHere, take this as a token of my apprecation. \r\n#i" + reward[0] + "# #b(+" + reward[1] + " stat)#k";
    }

    protected static int[] completeAssignment(MapleCharacter chr) {
        String completedAssignmentTitle = getCampaignTitle(chr.getCampaignProgress());
        Equip equip = null;
        if (completedAssignmentTitle != null) {
            if (completedAssignmentTitle.equals("Collect Zombie's Gold Tooth!")) {
                /*equip = (Equip) MapleItemInformationProvider.getInstance().getEquipById( item id );
                equip.setStr( stat mod );
                equip.setDex( stat mod );
                equip.setInt( stat mod );
                equip.setLuk( stat mod );
                chr.getInventory(MapleInventoryType.EQUIP).addItem(equip);*/
            }
        }
        return new int[] {equip.getItemId(), equip.getStr()};
    }
}
