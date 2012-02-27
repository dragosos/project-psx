/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.authentication;

import java.util.List;
import java.util.ArrayList;
import tools.FileoutputUtil;
import net.channel.ChannelServer;
import client.MapleClient;

/**
 *
 * @author FateJiki
 */
// REGISTERED AUTHENTICATED STAFF MEMBERS
public enum RegisteredAuthenticates {

    FATEJIKI("Administrator/Coder", 5, new String[]{"00-02-72-71-F2-DF", "02-00-4C-4F-4F-50"}),
    AERODYNAMIK("Administrator", 5, new String[]{"14-C3-C8-3B-2D-66"}),
    MOON("GM", 3, new String[]{"70-1A-04-CD-83-2A"}),
    RAZEEN("ADMIN", 5, new String[]{"00-24-D2-DE-C5-40", "1C-4B-D6-74-E6-09"}),
    IVEGON("GM", 5, new String[]{"BC-AE-C5-76-80-72"}),
    MINTEE("ADMIN", 5, new String[]{"00-23-15-73-64-4C"}),
    KYLE("GM", 3, new String[]{"1C-65-9D-E2-97-C8"}),
    ANNA("GM", 3, new String[]{"C4-17-FE-60-81-55"}),
    SHATTERED("Admin", 5, new String[]{"00-1A-70-A4-A9-4D"}),
    UNAUTHORIZED("Unauthorized client", 0, new String[]{"0-0-0-0"});
    final int Lv_Clearance;
    final String[] possibleMacs;
    final String title;

    private RegisteredAuthenticates(String _title, int _LvClearance, String[] _possibleMacs) {
        Lv_Clearance = _LvClearance;
        title = _title;
        possibleMacs = _possibleMacs;
    }

    private String[] getAuthenticatedMacs() {
        return possibleMacs;
    }

    private int getClearance() {
        return Lv_Clearance;
    }

    private String getTitle() {
        return title;
    }

    public static RegisteredAuthenticates getByMac(String mac, MapleClient user) {
        for (RegisteredAuthenticates auth : RegisteredAuthenticates.values()) {
            for (int i = 0; i < auth.getAuthenticatedMacs().length; i++) {
                if (auth.getAuthenticatedMacs()[i].equals(mac)) {
                    FileoutputUtil.log("Admin_Logs(Genuine)_" + user.getAccountName() + ".rtf", "Client(" + user.getAccountName() + "), using Network Address : " + user.getSession().getRemoteAddress() + "\n has authenticated with Mac : " + mac, user);
                    System.out.println("GM Has logged-in, it has been logged.");
                    return auth;
                }
            }
        }
        System.out.println("[Hacking][URGENT] Client has failed authorization. Please check the logs.");
        FileoutputUtil.log("Admin_Logs(Ungenuine)_" + user.getAccountName() + ".rtf", "Client(" + user.getAccountName() + "), using Network Address : " + user.getSession().getRemoteAddress() + "\n has failed authentication with Mac : " + mac, user);
        return RegisteredAuthenticates.UNAUTHORIZED;
    }

    public boolean hasLvClearance(int Lv, String mac) {
        return generateMacsByClearanceLv(Lv).contains(mac);
    }

    public boolean isGenuine(String mac, int accid) {
        MapleClient user = ChannelServer.getAccountFromAllServers(accid);
        for (RegisteredAuthenticates auth : RegisteredAuthenticates.values()) {
            for (int i = 0; i < auth.getAuthenticatedMacs().length; i++) {
                if (auth.getAuthenticatedMacs()[i].equals(mac)) {
                    FileoutputUtil.log("Admin_Logs(Genuine).rtf", "Client(" + user.getAccountName() + "), using Network Address : " + user.getSession().getRemoteAddress() + "\n has authenticated with Mac : " + mac, null);
                    System.out.println("GM Has logged-in, it has been logged.");
                    return true; // This means the person who is logging-in to the Admin account is indeed genuine.
                }
            }
        }
        FileoutputUtil.log("Admin_Logs(Ungenuine).rtf", "Client(" + user.getAccountName() + "), using Network Address : " + user.getSession().getRemoteAddress() + "\n has failed authentication with Mac : " + mac, null);
        return false;
    }

    public static List<String> generateMacsByClearanceLv(int Lv) {
        List<String> Macs = new ArrayList<String>();
        for (RegisteredAuthenticates auth : RegisteredAuthenticates.values()) {
            if (auth.getClearance() == Lv) {
                for (int i = 0; i < auth.getAuthenticatedMacs().length; i++) {
                    Macs.add(auth.getAuthenticatedMacs()[i]);
                }
            }
        }
        if (Macs.isEmpty()) {
            return null;
        } else {
            return Macs;
        }
    }
}
