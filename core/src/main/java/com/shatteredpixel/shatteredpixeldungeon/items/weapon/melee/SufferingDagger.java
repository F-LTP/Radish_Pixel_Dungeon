package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class SufferingDagger extends MeleeWeapon{


    {
        image = ItemSpriteSheet.DAGGER_S;
        hitSound = Assets.Sounds.HIT_STAB;
        hitSoundPitch = 1.1f;

        tier = 3;

    }

    @Override
    public int max(int lvl) {
        return  4*(tier+1) +
                lvl*(tier+1);   //scaling unchanged
    }
    @Override
    public int min(int lvl) {
        return  8 +
                lvl;   //scaling unchanged
    }
    @Override
    public int damageRoll(Char owner) {
        Char enemy = Char.enemyOf(owner);
        if (Char.isSurpriseAttack(owner, enemy)) {
            //deals 75% toward max to max on surprise, instead of min to max.
            int diff = max() - min();
            int damage = augment.damageFactor(Random.NormalIntRange(
                    min() + Math.round(diff*0.75f),
                    max()));
            int exStr = owner instanceof Hero ? ((Hero) owner).STR() - STRReq() : 0;
            if (exStr > 0) {
                damage += Random.IntRange(0, exStr);
            }
            return damage;
        }
        return super.damageRoll(owner);
    }
}
