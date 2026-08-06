/*
 * Radish Pixel Dungeon
 * 苔藓分支入口房间 - 从主线第2层进入
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
 * 苔藓分支的入口房间
 * 包含一个从主线第2层进入的楼梯（BRANCH_ENTRANCE 类型）
 */
public class MossEntranceRoom extends StandardRoom {

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
    public boolean isEntrance() {
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
        int entranceCell = level.pointToCell(c);

        // 创建从主线进入苔藓分支的楼梯（BRANCH_ENTRANCE）
        // 返回主线第2层
        LevelTransition transition = LevelTransition.branchUp(level, entranceCell,
                "moss:main-2", Branches.MAIN, 2);
        level.transitions.add(transition);
        Painter.set(level, entranceCell, Terrain.ENTRANCE);
    }

    @Override
    public void onLevelLoad(Level level) {
        super.onLevelLoad(level);
        level.customTiles.removeIf(tile -> tile instanceof MossPortalVisual
                && inside(new Point(tile.tileX, tile.tileY)));
        for (LevelTransition transition : level.transitions) {
            if (inside(transition.center())) {
                Level.set(transition.cell(), Terrain.ENTRANCE, level);
                return;
            }
        }
    }

    @Override
    public boolean canPlaceTrap(Point p) {
        return false;
    }

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
