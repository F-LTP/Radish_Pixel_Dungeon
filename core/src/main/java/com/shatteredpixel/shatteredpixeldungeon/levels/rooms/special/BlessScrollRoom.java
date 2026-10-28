package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfBlessGoTend;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.connection.ConnectionRoom;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class BlessScrollRoom extends ConnectionRoom {

    @Override
    public int minWidth() {
        return 7;
    }

    @Override
    public int minHeight() {
        return 7;
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
    public void paint(Level level) {

        Painter.fill( level, this, Terrain.WALL );
        Painter.fill( level, this, 1, Terrain.EMPTY );
        Painter.fillDiamond( level, this, 2, Terrain.WATER);

        Point c = center();
        level.drop(new ScrollOfBlessGoTend(), level.pointToCell(c)).type = Heap.Type.LOCKED_CHEST;

        int chestPos = (top + Random.Int(1,2)) * level.width() + left + Random.Int(1,2);
        level.drop(new Pasty(), chestPos);

        for (Door door : connected.values()) {
            door.set( Door.Type.REGULAR );
        }

        level.addItemToSpawn(new GoldenKey(2));
    }

    @Override
    public boolean connect(Room room) {
        //cannot connect to entrance, otherwise works normally
        if (room.isExit())  return false;
        else                return super.connect(room);
    }

    @Override
    public boolean canPlaceTrap(Point p) {
        return false;
    }
}


