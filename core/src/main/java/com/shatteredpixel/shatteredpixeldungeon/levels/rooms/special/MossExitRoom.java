/*
 * Radish Pixel Dungeon
 * 苔藓分支出口房间 - 返回主线传送门
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Point;

/**
 * 苔藓分支的出口房间
 * 包含一个返回主线第2层的传送门（不生成向下楼梯，因为苔藓分支只有2层）
 */
public class MossExitRoom extends StandardRoom {

    @Override
    public int minWidth() {
        return 5;
    }

    @Override
    public int minHeight() {
        return 5;
    }

    @Override
    public int maxWidth() {
        return 7;
    }

    @Override
    public int maxHeight() {
        return 7;
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public boolean canMerge(Level l, Room other, Point p, int mergeTerrain) {
        return false;
    }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY);

        for (Room.Door door : connected.values()) {
            door.set(Room.Door.Type.REGULAR);
        }

        Point c = center();
        int exitCell = level.pointToCell(c);

        LevelTransition transition = LevelTransition.branchDown(level, exitCell,
                "moss:main-3", Branches.MAIN, 3);
        level.transitions.add(transition);
        Painter.set(level, exitCell, Terrain.EXIT);

        // 添加视觉效果
        MossPortalVisual vis = new MossPortalVisual();
        vis.pos(c.x, c.y);
        level.customTiles.add(vis);
    }

    @Override
    public boolean connect(Room room) {
        if (room.isExit()) return false;
        return super.connect(room);
    }
        // 不能连接到另一个出口房间

    @Override
    public void onLevelLoad(Level level) {
        super.onLevelLoad(level);
        for (LevelTransition transition : level.transitions) {
            if (inside(transition.center())) {
                transition.linkId = "moss:main-3";
                transition.direction = LevelTransition.Direction.DOWN;
                transition.destBranch = Branches.MAIN;
                transition.destDepth = 3;
                transition.type = LevelTransition.Type.BRANCH_EXIT;
                Painter.set(level, transition.cell(), Terrain.EXIT);
                return;
            }
        }
    }

    /**
     * 传送门视觉效果
     */
    public static class MossPortalVisual extends CustomTilemap {

        {
            texture = Assets.Environment.MOSS_ENTER;
            tileW = tileH = 1;
        }

        final int TEX_WIDTH = 16;

        @Override
        public Tilemap create() {
            Tilemap v = super.create();
            v.map(mapSimpleImage(0, 0, TEX_WIDTH), 1);
            return v;
        }

        @Override
        public String name(int tileX, int tileY) {
            return Messages.get(this, "name");
        }

        @Override
        public String desc(int tileX, int tileY) {
            return Messages.get(this, "desc");
        }
    }
}
