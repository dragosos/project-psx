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

import java.util.HashMap;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author Simon
 */
public class ExtendedSPTable {

    private ExtendedSPType SPType;
    private HashMap<Integer, Integer> SPTable;
    private int baseJob;

    public ExtendedSPTable(HashMap<Integer, Integer> SPTable, int jobID) {
        this.SPTable = SPTable;
        this.SPType = ExtendedSPType.getFromJobID(jobID);
        if (SPType == ExtendedSPType.EVAN) {
            baseJob = 2210;
        } else {
            baseJob = 3000;
        }
    }

    public ExtendedSPTable(int jobID) {
        SPTable = new HashMap<Integer, Integer>();
        for (int i = 1; i < 11; i++) {
            SPTable.put(i, 0);
        }

        this.SPType = ExtendedSPType.getFromJobID(jobID);

        if (SPType == ExtendedSPType.EVAN) {
            baseJob = 2210;
        } else {
            baseJob = 3000;
        }
    }

    public int getSPFromJobID(int jobID) {
        if (jobID == 2200) {
            return 2; // dont really care
        } else if (jobID >= 2210 && jobID <= 2218) {
            return SPTable.get((jobID - baseJob) + 2);
        } else if(jobID >= 3000 && jobID <= 3600){
            return 2;
        }
        return -1;
    }

    public int getSPFromSlotID(int slotID) {
        return SPTable.get(slotID);
    }

    public void updateSPFromSlotID(int slotID, int newSP) {
        SPTable.remove(slotID);
        SPTable.put(slotID, newSP);
    }

    public void updateSPFromJobID(int jobID, int newSP) {
        if (jobID == 2200) {
            SPTable.remove(1);
            SPTable.put(1, newSP);
        } else if (jobID >= 2210 && jobID <= 2218) {
            SPTable.remove((jobID - baseJob) + 2);
            SPTable.put((jobID - baseJob) + 2, newSP);
        }
    }

    public void addSPFromJobID(int jobID, int delta) {
        updateSPFromJobID(jobID, getSPFromJobID(jobID) + delta);
    }

    public void addSPFromSlotID(int slot, int delta) {
        updateSPFromSlotID(slot, getSPFromSlotID(slot) + delta);
    }

    private int getNonZeroSize() {
        int res = 0;
        for (Integer i : SPTable.values()) {
            if (i > 0) {
                res++;
            }
        }
        return res;
    }

    public void addSPData(MaplePacketLittleEndianWriter mplew) {
        mplew.write(getNonZeroSize());
        for (int i = 1; i < SPTable.size() + 1; i++) {
            if (SPTable.get(i) > 0) {
                mplew.write(i);
                mplew.write(SPTable.get(i));
            }
        }
    }
}
