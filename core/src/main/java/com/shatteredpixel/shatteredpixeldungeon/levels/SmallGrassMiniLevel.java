package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MossEntranceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MossExitRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.EntranceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.ExitRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SmallGrassMiniLevel extends SewerLevel {

    @Override
    public void playLevelMusic(){
        Music.INSTANCE.play(Assets.Music.SEWERS_TENSE, true);
    }

    @Override
    protected int standardRooms(boolean forceMax) {
        if (forceMax) return 2;
        return 2+ Random.chances(new float[]{1, 3, 1});
    }

    @Override
    protected int specialRooms(boolean forceMax) {
        if (forceMax) return 1;
        return 1+Random.chances(new float[]{1, 2});
    }

    public String tilesTex() {
        return Assets.Environment.TILES_MOSS;
    }

    public String waterTex() {
        return Assets.Environment.WATER_MOSS;
    }

    @Override
    protected ArrayList<Room> initRooms() {
        ArrayList<Room> initRooms = new ArrayList<>();

        // 苔藓分支第1层使用 MossEntranceRoom（从主线进入）
        // 第2层使用普通入口房间（从第1层进入）
        if (Dungeon.depth == 1) {
            initRooms.add(roomEntrance = new MossEntranceRoom());
        } else {
            initRooms.add(roomEntrance = EntranceRoom.createEntrance());
        }

        // 苔藓分支第2层使用 MossExitRoom（返回主线）
        if (Dungeon.depth == 2) {
            initRooms.add(roomExit = new MossExitRoom());
        } else {
            // 第1层正常生成出口
            initRooms.add(roomExit = ExitRoom.createExit());
        }

        // 标准房间
        int standards = standardRooms(false);
        for (int i = 0; i < standards; i++) {
            initRooms.add(StandardRoom.createRoom());
        }

        // 特殊房间
        int specials = specialRooms(false);
        SpecialRoom.initForFloor();
        for (int i = 0; i < specials; i++) {
            initRooms.add(SpecialRoom.createRoom());
        }

        return initRooms;
    }
}