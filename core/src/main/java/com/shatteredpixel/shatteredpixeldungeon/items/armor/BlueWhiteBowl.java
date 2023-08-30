package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class BlueWhiteBowl extends Armor{
    int cd =0;
    {
        image = ItemSpriteSheet.ARMOR_BOWL;

        bones = false;
    }

    public BlueWhiteBowl() {
        super( 1 );
    }
    @Override

    public int proc(Char attacker, Char defender, int damage ){
        if (cd<=0){
            for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
                if (mob.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[mob.pos]) {
                    Charm c = Buff.affect( mob, Charm.class, Charm.DURATION );
                    c.object=defender.id();
                    mob.sprite.centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 5);
                }
            }
            Sample.INSTANCE.play(Assets.Sounds.CHARMS);
            cd=200;
        }
        return super.proc(attacker,defender,damage);
    }
    private static final String CD_CHARM        = "cd_charm";

    @Override
    public void storeInBundle( Bundle bundle ) {
        bundle.put(CD_CHARM,cd);
        super.storeInBundle(bundle);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(CD_CHARM))
            cd = bundle.getInt(CD_CHARM);
        else
            cd =0;
    }
}
