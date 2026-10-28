package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.Set;

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

    @Override
    public int proc(Char attacker, Char defender, int damage) {

        Set<Char.Property> properties = defender.properties();

        boolean isSpecialEnemy =
                   properties.contains(Char.Property.BOSS)
                || properties.contains(Char.Property.MINIBOSS)
                || properties.contains(Char.Property.ELITES);

        boolean hasChampionBuff = false;
        for (Buff b : defender.buffs(ChampionEnemy.class)) {
            if (b != null) {
                hasChampionBuff = true;
                break;
            }
        }
        
        isMustCrit = isSpecialEnemy || hasChampionBuff;

        return super.proc(attacker, defender, damage);
    }




}
