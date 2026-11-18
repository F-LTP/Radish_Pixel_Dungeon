/*
 * 苔藓分支在主线第 3 层的出口房间：只能作为落点，不能从这里返回苔藓。
 *
 * InterlevelScene 需要靠 transition 的 linkId 定位落点，因此保留 transition，
 * 只把地形改成普通地面并标记 sealed。
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialStairs;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Point;

public class MossExitRoomInMain extends SpecialRoom {

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
    public boolean canMerge(Level level, Room other, Point point, int mergeTerrain) {
        return false;
    }

    @Override
    public void paint(Level level) {
        Painter.fill(level, this, Terrain.WALL);
        Painter.fill(level, this, 1, Terrain.EMPTY);

        for (Room.Door door : connected.values()) {
            door.set(Room.Door.Type.REGULAR);
        }

        Point center = center();
        int entranceCell = level.pointToCell(center);

        LevelTransition gate = LevelTransition.branchUp(level, entranceCell,
                "moss:main-3", Branches.MOSS, 2);
        level.transitions.add(gate);
        com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialStairs.add(level, gate).oneWay();
        Painter.set(level, entranceCell, Terrain.EMPTY);

        OneWayPortalVisual vis = new OneWayPortalVisual();
        vis.pos(center.x, center.y);
        level.customTiles.add(vis);
    }

    public static class OneWayPortalVisual extends CustomTilemap {

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

    @Override
    public void onLevelLoad(Level level) {
        super.onLevelLoad(level);

        for (LevelTransition transition : level.transitions) {
            if (inside(transition.center())) {
                transition.linkId = "moss:main-3";
                transition.direction = LevelTransition.Direction.UP;
                transition.destBranch = Branches.MOSS;
                transition.destDepth = 2;
                transition.type = LevelTransition.Type.BRANCH_ENTRANCE;
                SpecialStairs.add(level, transition).oneWay();
                Painter.set(level, transition.cell(), Terrain.EMPTY);
                return;
            }
        }
    }

    @Override
    public boolean connect(Room room) {
        if (room.isExit()) return false;
        return super.connect(room);
    }
}
