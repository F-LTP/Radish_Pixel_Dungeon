package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;


import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class SkyWalker extends Glyph {

    private static ItemSprite.Glowing SKYWALKER = new ItemSprite.Glowing( 0xFFFFFF );

    @Override
    public int proc(Armor armor, Char attacker, Char defender, int damage) {
        // no proc here, it works on Chasm.class
        return damage;
    }
    @Override
    public ItemSprite.Glowing glowing() {
        return SKYWALKER;
    }

}
