/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package tools;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import client.MapleClient;
import constants.ServerConstants;
import java.io.File;
import java.util.Date;
import net.channel.ChannelServer;

public class FileoutputUtil {

    // Logging output file
    public static final String Acc_Stuck = "Log_AccountStuck.rtf",
	    Login_Error = "Log_Login_Error.rtf",
	    Timer_Log = "Log_Timer_Except.rtf",
	    MapTimer_Log = "Log_MapTimer_Except.rtf",
	    GMCommand_Log = "Log_GMCommand.rtf",
	    IP_Log = "Log_AccountIP.rtf",
	    Horntail_Log = "Log_Horntail.rtf",
	    Pinkbean_Log = "Log_Pinkbean.rtf",
            System_Error = "sys_error.rtf",
            Saving_Error = "saveError.rtf",
            exceptions = "misc_exception.rtf",
            runtime_log = "runtime_log.rtf";
    // End
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void log(final String def, final String msg, MapleClient c) {
       /* final String file = buildLogPath(c, def);
        Date date = new Date();
	FileOutputStream out = null;
	try {
	    out = new FileOutputStream(file, true);
	    out.write(msg.getBytes());
            out.write(sdf.format(date).getBytes());
	} catch (IOException ess) {
	} finally {
	    try {
		if (out != null) {
		    out.close();
		}
	    } catch (IOException ignore) {
	    }
	}*/
    }
    
   // public static void logPacketError
  //  
    public static void log(final String def, final String msg, int accid) {
        /*final String file = buildLogPath(ChannelServer.getAccountFromAllServers(accid), def);
        Date date = new Date();
	FileOutputStream out = null;
	try {
	    out = new FileOutputStream(file, true);
            out.write(("(" + date.toString() + ") REV : " + ServerConstants.srcRevision + "\n\n").getBytes());
	    out.write(msg.getBytes());
	    out.write("\n------------------------\n".getBytes());
            out.write(sdf.format(date).getBytes());
	} catch (IOException ess) {
	} finally {
	    try {
		if (out != null) {
		    out.close();
		}
	    } catch (IOException ignore) {
	    }
	}*/
    }
    
    public static void logPacketErrors(String c_name, boolean unhandledrecv) {
       /* final String file = "/logs/HotList/" + c_name + "/";
        Date date = new Date();
	FileOutputStream out = null;
	try {
	    out = new FileOutputStream(file, true);
            out.write(("(" + date.toString() + ") REV : " + ServerConstants.srcRevision + "\n\n").getBytes());
	  //  out.write(msg.getBytes());
	    out.write("\n------------------------\n".getBytes());
            out.write(sdf.format(date).getBytes());
	} catch (IOException ess) {
	} finally {
	    try {
		if (out != null) {
		    out.close();
		}
	    } catch (IOException ignore) {
	    }
	}*/
    }

    
    private static String buildLogPath(MapleClient c, String defPath) {
        String builtPath = "";
        if(c == null){
            builtPath = "logs/system/";
        } else {
            if(c.getPlayer() == null){
                builtPath = "logs/accounts/" + c.getAccountName() + "_ACCOUNT/";
            } else {
                builtPath = "logs/accounts/players" + c.getAccountName() + "_ACCOUNT/" + c.getPlayer().getName() + "_CHARACTER" + "/";
            }
        }
        File dir_1 = new File(builtPath);
        try{
        dir_1.mkdirs();
        } catch(Exception e){} // already exists
        
        builtPath += defPath;
        return builtPath;
    }

    public static void outputFileError(final String file, final Throwable t) {
	FileOutputStream out = null;
	try {
	    out = new FileOutputStream(file, true);
	    out.write(getString(t).getBytes());
	    out.write("\n------------------------\n".getBytes());
	} catch (IOException ess) {
	} finally {
	    try {
		if (out != null) {
		    out.close();
		}
	    } catch (IOException ignore) {
	    }
	}
    }

    public static final String CurrentReadable_Time() {
	return sdf.format(Calendar.getInstance().getTime());
    }

    public static final String getString(final Throwable e) {
	String retValue = null;
	StringWriter sw = null;
	PrintWriter pw = null;
	try {
	    sw = new StringWriter();
	    pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    retValue = sw.toString();
	} finally {
	    try {
		if (pw != null) {
		    pw.close();
		}
		if (sw != null) {
		    sw.close();
		}
	    } catch (IOException ignore) {
	    }
	}
	return retValue;
    }
}
