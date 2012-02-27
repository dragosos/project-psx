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
package net.channel;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import client.MapleCharacter;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerStorage implements IPlayerStorage {
    private final Lock mutex = new ReentrantLock();
    private final Map<String, MapleCharacter> chars = new LinkedHashMap<String, MapleCharacter>();

    public void registerPlayer(MapleCharacter chr) {
        mutex.lock();
        try {
            chars.put(chr.getName().toLowerCase(), chr);
        } finally {
	    mutex.unlock();
	}
    }

    public void deregisterPlayer(MapleCharacter chr) {
        mutex.lock();
        try {
            chars.remove(chr.getName().toLowerCase());
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public MapleCharacter getCharacterByName(String name) {
        mutex.lock();
        try{
        return chars.get(name.toLowerCase());
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public MapleCharacter getCharacterById(int id) {
        MapleCharacter ret = null;
        mutex.lock();
        try{
        for(MapleCharacter chr : chars.values()){
            if(chr.getId() == id){
                ret = chr;
                break;
            }
        }
        } finally {
            mutex.unlock();
        }
        return ret;
    }

    @Override
    public Collection<MapleCharacter> getAllCharacters() {
        final Map<String, MapleCharacter> charz = new LinkedHashMap<String, MapleCharacter>();
        mutex.lock();
        try{
            for(MapleCharacter chr : chars.values()){
                charz.put(chr.getName(), chr);
            }
        } finally {
            mutex.unlock();
        }
        return charz.values();
    }

    public final void disconnectAll() {
	mutex.lock();
	try {
	    final Iterator<MapleCharacter> itr = chars.values().iterator();
	    MapleCharacter chr;
	    while (itr.hasNext()) {
		chr = itr.next();

		if (!chr.isGM()) {
		    chr.getClient().disconnect();
		    itr.remove();
		}
	    }
	} finally {
	    mutex.unlock();
	}
    }
    
    @Override
    public final String getCheatersList(){
        StringBuilder sb = new StringBuilder();
        sb.append("Here's the list of the current cheaters.. :\r\n"  );
        for(MapleCharacter chr : getAllCharacters()){
            synchronized(chr){
            if(!chr.getAutobanManager().points.isEmpty()){
                sb.append(chr.getName());
                sb.append("(");
                sb.append(sb.append(chr.getAutobanManager().calculateNumPoints()));
                sb.append(")");
                sb.append(" ~~ ");
            }
        }
        }
        return sb.toString();
    }

}
