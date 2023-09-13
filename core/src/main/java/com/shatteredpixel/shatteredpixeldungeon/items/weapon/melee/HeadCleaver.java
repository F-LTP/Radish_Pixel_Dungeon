package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class HeadCleaver extends MeleeWeapon{
    {
        image = ItemSpriteSheet.HEADCLEAVER;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;

        tier = 4;
        DLY=1.3f;
    }
    @Override
    public int min(int lvl) {
        return  2 +
                lvl*2;
    }
    @Override
    public int max(int lvl) {
        return  30 +
                lvl*(tier+1);   //scaling unchanged
    }
    @Override
    public int proc(Char attacker, Char defender, int damage){
        if (!defender.properties().contains(Char.Property.HEADLESS)){
            if(Random.Float()<0.1+0.01*buffedLvl()){
                defender.damage( defender.HP, this );
                defender.sprite.emitter().burst(BloodParticle.BURST, 50 );
            }
        }
        return super.proc(attacker,defender,damage);
    }
}
