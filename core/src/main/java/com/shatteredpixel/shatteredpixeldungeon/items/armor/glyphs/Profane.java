package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 邪秽刻印：亡灵/恶魔更难发现穿戴者（距离 2 格以内才会察觉），
 * 且穿戴者受到的亡灵/恶魔伤害降低 30%。
 */
public class Profane extends Armor.Glyph {

    private static final ItemSprite.Glowing PURPLE = new ItemSprite.Glowing( 0x6A0DAD );

    private static final float DAMAGE_REDUCTION = 0.30f;
    private static final int NOTICE_RANGE = 2;

    @Override
    public int proc( Armor armor, Char attacker, Char defender, int damage ) {
        if (attacker != null && isProfaneFoe( attacker )) {
            return Math.round( damage * (1f - DAMAGE_REDUCTION) );
        }
        return damage;
    }

    /** 亡灵/恶魔只有在 2 格内才能发现穿戴邪秽刻印的英雄。 */
    public static boolean hides( Mob mob, Hero hero ) {
        if (mob == null || hero == null || !isProfaneFoe( mob )) {
            return false;
        }
        Armor armor = hero.belongings.armor();
        if (armor == null || !armor.hasGlyph( Profane.class, hero )) {
            return false;
        }
        return Dungeon.level.distance( mob.pos, hero.pos ) > NOTICE_RANGE;
    }

    private static boolean isProfaneFoe( Char ch ) {
        return Char.hasProp( ch, Char.Property.UNDEAD ) || Char.hasProp( ch, Char.Property.DEMONIC );
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return PURPLE;
    }
}
