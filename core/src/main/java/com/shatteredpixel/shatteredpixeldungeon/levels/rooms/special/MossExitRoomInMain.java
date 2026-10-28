package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
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
        level.transitions.add(LevelTransition.branchUp(level, entranceCell,
                "moss:main-3", Branches.MOSS, 2));
        Painter.set(level, entranceCell, Terrain.ENTRANCE);
    }

    @Override
    public void onLevelLoad(Level level) {
        super.onLevelLoad(level);
        level.customTiles.removeIf(tile -> tile instanceof MossExitRoom.MossPortalVisual
                && inside(new Point(tile.tileX, tile.tileY)));
        for (LevelTransition transition : level.transitions) {
            if (inside(transition.center())) {
                Level.set(transition.cell(), Terrain.ENTRANCE, level);
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
