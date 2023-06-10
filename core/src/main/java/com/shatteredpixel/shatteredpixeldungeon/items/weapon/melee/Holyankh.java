package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Holyankh extends MeleeWeapon{
    private static ItemSprite.Glowing YELLOW = new ItemSprite.Glowing( 0xFFFF00 );
    {
        image = ItemSpriteSheet.ANKH;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 0.9f;

        tier = 4;
    }

    @Override
    public int max(int lvl) {
        return  4*(tier+2) +    //24 base, down from 25
                lvl*(tier);   //scaling unchanged
    }
    @Override
    public int proc(Char attacker, Char defender, int damage){
        boolean active=false;
        for (int i : PathFinder.NEIGHBOURS4){
            Char ch = Actor.findChar(defender.pos + i);
            if (ch!=null){
                if (ch.alignment != attacker.alignment) {
                    ch.damage(damageRoll(attacker), this);
                    ch.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 6 );
                    active=true;
                }
            }
        }
        if (active) Sample.INSTANCE.play( Assets.Sounds.ZAP );
        return super.proc(attacker,defender,damage);
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return YELLOW;
    }
}
