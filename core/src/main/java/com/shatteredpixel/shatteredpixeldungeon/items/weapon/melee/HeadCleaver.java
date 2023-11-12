package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
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
            float p=0.1f+0.01f*buffedLvl();
            if (defender.properties().contains(Char.Property.BOSS))
                p=0.01f;
            if(Random.Float()<p){
                for (Buff b:defender.buffs()){
                    if (b instanceof ShieldBuff){
                        b.detach();
                    }
                }
                Buff.affect(defender,headCleaverTracker.class);
                defender.HP=1;
                defender.damage(1,new headCleaverTracker());
                defender.sprite.emitter().burst(BloodParticle.BURST, 50 );
                if (defender == Dungeon.hero && !defender.isAlive()) {
                    GLog.n(Messages.get(HeadCleaver.headCleaverTracker.class, "ondeath"));
                    Dungeon.fail(HeadCleaver.headCleaverTracker.class);
                }
            }
        }
        return super.proc(attacker,defender,damage);
    }
    public static class headCleaverTracker extends Buff{

    }
}
