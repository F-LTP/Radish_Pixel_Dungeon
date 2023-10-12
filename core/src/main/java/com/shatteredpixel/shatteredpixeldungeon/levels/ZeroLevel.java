/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import static com.shatteredpixel.shatteredpixeldungeon.items.Generator.randomArtifact;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Boat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MoonLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Owo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PW;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.SeaShore;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.ThunderStorm;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.List;

public class ZeroLevel extends Level {
    static final Class<?>[] zero_npc={
            Boat.class,
            Owo.class,
            ThunderStorm.class,
            SeaShore.class,
            PW.class,
            MoonLight.class
    };
    private static final int[] true_map = {
            0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,4,4,4,4,4,0,0,0,
            0,0,0,4,1,1,1,4,0,0,0,
            0,0,0,4,1,7,1,4,0,0,0,
            0,0,0,4,1,1,1,4,0,0,0,
            0,0,0,4,1,1,1,4,0,0,0,
            0,0,0,4,4,5,4,4,0,0,0,
            0,0,0,0,0,14,0,0,0,0,0,
            0,0,0,0,0,14,0,0,0,0,0,
            0,0,0,0,0,14,0,0,0,0,0,
            0,0,0,0,14,14,14,0,0,0,0,
            0,0,0,0,14,0,14,0,0,0,0,
            0,0,0,0,14,0,14,0,0,0,0,
            0,0,0,0,14,0,14,0,0,0,0,
            0,0,4,4,5,4,5,4,4,0,0,
            0,4,4,27,1,25,1,27,4,4,0,
            0,4,27,11,1,14,1,11,27,4,0,
            0,4,1,1,14,9,14,1,1,4,0,
            0,4,25,14,9,8,9,14,25,4,0,
            0,4,1,1,14,9,14,1,1,4,0,
            0,4,27,11,1,14,1,11,27,4,0,
            0,4,4,27,1,25,1,27,4,4,0,
            0,0,4,4,4,4,4,4,4,0,0,
            0,0,0,0,0,0,0,0,0,0,0
    };
    {
        color1 = 5459774;
        color2 = 12179041;
    }

    public ZeroLevel() {
        this.viewDistance = 12;
    }

    /*private int mapToTerrain(int var1) {
        if (var1 == 1 || var1 == 2 || var1 == 3) {
            return 29;
        }
        if (var1 != 4) {
            if (var1 == 16) {
                return 7;
            }
            if (var1 == 17) {
                return 8;
            }
            switch (var1) {
                case -047483644:
                    break;
                case -2147483584:
                case 64:
                case 190:
                    return 4;
                case 85:
                    return 11;
                case -2147483524:
                case 124:
                case 140:
                    return 27;
                case 69:
                case 25:
                    return 12;
                case 80:
                    return 5;
                case 96:
                    return 23;
                case 120:
                    return 20;
                case 123:
                    return 29;
                default:
                    return 1;
            }
        }
        return 14;
    }*/

    protected boolean build() {
        setSize(11, 24);
        int exit = (this.width * 18 + 5);
        int entrance = (this.width * 3) + 5;
        for (int var1 = 0; var1 < this.map.length; var1++) {
            this.map[var1] = true_map[var1];
        }

        //map[entrance] = Terrain.ENTRANCE;
        transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.SURFACE));
        transitions.add(new LevelTransition(this, exit, LevelTransition.Type.REGULAR_EXIT));
        //map[exit] =Terrain.EXIT;
        return true;
    }

    protected void createItems() {


    }

    public Mob createMob() {
        return null;
    }

    protected void createMobs() {
        final int NW=(this.width * 16 + 3);
        final int NE=(this.width * 16 + 7);
        final int SW=(this.width * 20 + 3);
        final int SE=(this.width * 20 + 7);
        Class<?>[] this_npc =zero_npc.clone();
        int chosen[]={-1,-1,-1,-1};
        int poses[]={NW,NE,SW,SE};
        int len=this_npc.length;
        for (int i=0;i<4;++i){
            int c= Random.Int(len);
            boolean flag= true;
            while (flag){
                for (int j=0;j<4 && flag;++j){
                    if (chosen[j]==c) flag = false;
                }
                if (!flag){
                    c++;
                    c%=len;
                }
                flag=!flag;
            }
            chosen[i]=c;
            NPC npcToAdd  = (NPC)Reflection.newInstance(this_npc[c]);
            npcToAdd.pos=poses[i];
            mobs.add(npcToAdd);
        }

    }

    public int randomRespawnCell() {
        return this.entrance - width();
    }

    public String tilesTex() {
        return Assets.Environment.TILES_SEWERS;
    }

    public String waterTex() {
        return Assets.Environment.WATER_PRISON;
    }

}