package com.shatteredpixel.shatteredpixeldungeon.items.talentitem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class MagicRoot extends Item {
    {
        image = ItemSpriteSheet.MAGIC_ROOT;

        defaultAction = AC_THROW;
        usesTargeting = true;

        stackable = true;
        bones = true;
    }
    @Override
    protected void onThrow( int cell ) {

        Splash.at( cell, 0xCC99FFFF, 1 );
        Char ch=Actor.findChar(cell);
        if (ch != null && !(ch instanceof Hero) ){
            Buff.affect(ch, Roots.class,3f);
        }
        if (Dungeon.level.map[cell] == Terrain.HIGH_GRASS ||
                Dungeon.level.map[cell] == Terrain.FURROWED_GRASS ||
                Dungeon.level.map[cell] == Terrain.EMPTY ||
                Dungeon.level.map[cell] == Terrain.EMBERS ||
                Dungeon.level.map[cell] == Terrain.EMPTY_DECO) {
            Level.set(cell, Terrain.GRASS);
            GameScene.updateMap(cell);
        }
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }
    @Override
    public int value() {
        return 10 * quantity;
    }
}
