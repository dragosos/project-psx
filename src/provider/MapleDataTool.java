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
package provider;

import java.awt.Point;
import java.awt.image.BufferedImage;
import provider.wz.MapleDataType;
import client.SkillParse;

public class MapleDataTool {
    public static String getString(MapleData data) {
        return ((String) data.getData());
    }

    public static String getString(MapleData data, String def) {
        if (data == null || data.getData() == null) {
            return def;
        } else {
            return ((String) data.getData());
        }
    }

    public static String getString(String path, MapleData data) {
        return getString(data.getChildByPath(path));
    }

    public static String getString(String path, MapleData data, String def) {
        return getString(data.getChildByPath(path), def);
    }

    public static double getDouble(MapleData data) {
        return ((Double) data.getData()).doubleValue();
    }

    public static float getFloat(MapleData data) {
        return ((Float) data.getData()).floatValue();
    }

    public static int getInt(MapleData data) {
        return ((Integer) data.getData()).intValue();
    }

    public static int getInt(String path, MapleData data) {
        return getInt(data.getChildByPath(path));
    }

    public static int getIntConvert(MapleData data) {
        if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        } else {
            return getInt(data);
        }
    }
    
    public static int getIntConvert(MapleData data, int skillevel) {
        if (data.getType() == MapleDataType.STRING) {
            return SkillParse.parseSkillInfo(getString(data), skillevel);
        } else {
            return getInt(data, skillevel);
        }
    }

    public static int getIntConvert(String path, MapleData data) {
        MapleData d = data.getChildByPath(path);
        if (d.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(d));
        } else {
            return getInt(d);
        }
    }
    
    public static int getIntConvert(String path, MapleData data, int skilllevel) {
        try{
        MapleData d = data.getChildByPath(path);
        if (d.getType() == MapleDataType.STRING) {
            return SkillParse.parseSkillInfo(getString(d), skilllevel);
        } else {
            return getInt(d, skilllevel);
        }
        } catch (Exception e){
            return 0;
        }
    }

    public static int getInt(MapleData data, int def) {
        if (data == null || data.getData() == null) {
            return  def;
        } else if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        } else {
            return ((Integer) data.getData()).intValue();
        }
    }
    
    public static int getInt(MapleData data, int def, int skillLevel){
        if (data == null || data.getData() == null) {
            return  def;
        } else if (data.getType() == MapleDataType.STRING) {
         //   System.out.println("SkillLevel" + skillLevel);
            int val = SkillParse.parseSkillInfo(getString(data), skillLevel);
            return val;
        } else {
            return ((Integer) data.getData()).intValue();
        }
    }

    public static int getInt(String path, MapleData data, int def) {
        return getInt(data.getChildByPath(path), def);
    }
    
    public static int getInt(String path, MapleData data, int def, int level) {
        return getInt(data.getChildByPath(path), def, level);
    }

    
    public static int getIntConvert(String path, MapleData data, int def, int skillevel) {
        MapleData d = data.getChildByPath(path);
        if (d == null) {
            return def;
        }
        if (d.getType() == MapleDataType.STRING) {
            try {
                return SkillParse.parseSkillInfo(getString(d), skillevel);
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else {
            return getInt(d, def, skillevel);
        }
    }

    public static BufferedImage getImage(MapleData data) {
        return ((MapleCanvas) data.getData()).getImage();
    }

    public static Point getPoint(MapleData data) {
        return ((Point) data.getData());
    }

    public static Point getPoint(String path, MapleData data) {
        return getPoint(data.getChildByPath(path));
    }

    public static Point getPoint(String path, MapleData data, Point def) {
        final MapleData pointData = data.getChildByPath(path);
        if (pointData == null) {
            return def;
        }
        return getPoint(pointData);
    }

    public static String getFullDataPath(MapleData data) {
        String path = "";
        MapleDataEntity myData = data;
        while (myData != null) {
            path = myData.getName() + "/" + path;
            myData = myData.getParent();
        }
        return path.substring(0, path.length() - 1);
    }
}
