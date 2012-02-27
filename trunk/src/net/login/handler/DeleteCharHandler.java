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
package net.login.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import net.channel.handler.PlayerLoggedinHandler;
import java.sql.*;
import tools.*;
import tools.data.input.SeekableLittleEndianAccessor;

public final class DeleteCharHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String pic = slea.readMapleAsciiString();
        int cid = slea.readInt();
       // hasCharOnAccount()
     //   if (c.checkPic(pic)) {
        c.announce(MaplePacketCreator.deleteCharResponse(cid, 0));
        c.deleteCharacter(cid);
     //   } else {
      //  c.announce(MaplePacketCreator.deleteCharResponse(cid, 0x14));
      //  }
    }
    
    public boolean hasCharOnAccount(String name, String accountName){
        boolean hasChar = true;
        int accountid = 0;
        Connection con = DatabaseConnection.getConnection();
        try{
        PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
        ps.setString(1, accountName);
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            accountid = rs.getInt("id");
            ps.close();
            ps = con.prepareStatement("SELECT * FROM characters where accountid = ? && name = ?");
            ps.setInt(1, accountid);
            ps.setString(2, name);
            rs = ps.executeQuery();
            if(rs.next()){
                return true;
            } else {
                return false;
            }
        }
        rs.close();
        ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            return hasChar;
        }
    }
}
