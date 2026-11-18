package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 点金附魔：对敌人造成伤害时获得金币。
 * 比例为 (等级+1)/(等级+3)，单次最多获得 50 金币。
 */
public class Midas extends Weapon.Enchantment {

    private static final ItemSprite.Glowing GOLD = new ItemSprite.Glowing( 0xFFCC00 );

    private static final int GOLD_CAP = 50;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (!(attacker instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)) {
            return damage;
        }
        if (damage <= 0 || defender.alignment != Char.Alignment.ENEMY) {
            return damage;
        }

        int level = Math.max( 0, weapon.buffedLvl() );
        int gold = Math.round( damage * ((level + 1f) / (level + 3f)) );
        gold = Math.min( gold, GOLD_CAP );
        if (gold <= 0) {
            return damage;
        }

        Dungeon.gold += gold;
        com.shatteredpixel.shatteredpixeldungeon.items.armor.CountessDress.onGoldChanged();
        Statistics.goldCollected += gold;
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return GOLD;
    }
}
