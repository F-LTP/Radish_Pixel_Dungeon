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
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class MagicRoot extends Item {
    {
        image = ItemSpriteSheet.REGROWTH_BOMB;

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
            Buff.affect(ch, Roots.class,2f);
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
