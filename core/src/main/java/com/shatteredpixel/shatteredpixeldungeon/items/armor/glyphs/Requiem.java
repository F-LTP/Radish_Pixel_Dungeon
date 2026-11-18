package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 安魂刻印：受到的亡灵/恶魔伤害降低 30%，受到怨灵的伤害降为 0。
 */
public class Requiem extends Armor.Glyph {

    private static final ItemSprite.Glowing PALE = new ItemSprite.Glowing( 0xBFD8E6 );

    private static final float UNDEAD_REDUCTION = 0.30f;

    @Override
    public int proc( Armor armor, Char attacker, Char defender, int damage ) {
        if (attacker == null) {
            return damage;
        }

        if (attacker instanceof Wraith) {
            return 0;
        }

        if (Char.hasProp( attacker, Char.Property.UNDEAD )
                || Char.hasProp( attacker, Char.Property.DEMONIC )) {
            return Math.round( damage * (1f - UNDEAD_REDUCTION) );
        }
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return PALE;
    }
}
