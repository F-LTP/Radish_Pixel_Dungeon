package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class GiantKiller extends MeleeWeapon {

    {
        image = ItemSpriteSheet.GIANTKILL;
        tier = 5;
    }

    @Override
    public int min(int lvl) {
        return 4 + lvl;
    }
    @Override
    public int max(int lvl) {
        return 25 + lvl * 5;
    }

    public boolean isMustCrit;

    public int proc(Char attacker, Char defender, int damage ) {
        //see Char.java Notes MUST CRITS METHOD
        isMustCrit = defender.properties().contains(Char.Property.BOSS) || defender.properties().contains(Char.Property.MINIBOSS) || defender.properties().contains(Char.Property.ELITES) || defender.buff(ChampionEnemy.class) != null;
        return super.proc(attacker, defender, damage);
    }

}
