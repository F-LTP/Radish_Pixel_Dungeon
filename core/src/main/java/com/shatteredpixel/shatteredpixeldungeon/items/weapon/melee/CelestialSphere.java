package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class CelestialSphere extends MeleeWeapon {

    {
        image = ItemSpriteSheet.SKYSPS;
        tier = 4;

        // See Weapon.java ReachFactor()
        RCH = 1;
    }

    @Override
    public int STRReq(int lvl) {
        return (7 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    public int min(int lvl) {
        int wandTotalLevel = 0;

        ArrayList<Wand> wands = hero.belongings.getAllItems(Wand.class);
        for (Wand w : wands.toArray(new Wand[0])){
            wandTotalLevel += w.level();
        }

        return 1 + lvl + wandTotalLevel;
    }
    @Override
    public int max(int lvl) {
        int wandTotalLevel = 0;
        ArrayList<Wand> wands = hero.belongings.getAllItems(Wand.class);
        for (Wand w : wands.toArray(new Wand[0])){
            wandTotalLevel += w.level();
        }

        return 4 + lvl * 3 + wandTotalLevel * 2;
    }

    // See Hero.java AttackProc()
//    public int proc(Char attacker, Char defender, int damage ) {
//        int magicDamage;
//        magicDamage = Random.NormalIntRange(min(level()),max(level()));
//        defender.damage(magicDamage,new DM100.LightningBolt());
//        return magicDamage;
//    }

}
