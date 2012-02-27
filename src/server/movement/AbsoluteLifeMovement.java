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
package server.movement;

import java.awt.Point;
import tools.data.output.LittleEndianWriter;

public class AbsoluteLifeMovement extends AbstractLifeMovement {
    private Point pixelsPerSecond;
    private int unk;
    private short foothold;

    public AbsoluteLifeMovement(int type, Point position, int duration, int newstate, int fh) {
        super(type, position, duration, newstate);
        foothold = (short)fh;
    }

    public Point getPixelsPerSecond() {
        return pixelsPerSecond;
    }

    public void setPixelsPerSecond(Point wobble) {
        this.pixelsPerSecond = wobble;
    }

    public int getUnk() {
        return unk;
    }
    
    public int getType(){
        return type;
    }

    public void setUnk(int unk) {
        this.unk = unk;
    }

    @Override
    // 18
    public void serialize(LittleEndianWriter lew) {
        lew.write(getType());
        lew.writeShort(getPosition().x);
        lew.writeShort(getPosition().y);
        lew.writeShort(pixelsPerSecond.x);
        lew.writeShort(pixelsPerSecond.y);
        lew.writeShort(foothold);
        lew.writeInt(unk);//new in v88
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}
