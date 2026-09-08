package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
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
                defender.sprite.emitter().burst(BloodParticle.BURST, 50 );
                if (defender == Dungeon.hero){
                    // 英雄目标：本次攻击的伤害结算完成后，由 onHeroCleave 判定处决/留 1 血
                    Buff.affect(defender, headCleaverTracker.class, headCleaverTracker.DURATION);
                } else {
                    // 非英雄目标保持即时结算
                    Buff.affect(defender, headCleaverTracker.class);
                    defender.HP=1;
                    defender.damage(new DamageInfo(1, DamageType.PHYSICAL_NO_ARMOR, attacker, this, new headCleaverTracker()));
                }
            }
        }
        return super.proc(attacker,defender,damage);
    }

    @SubscribeEvent(event = CharFinalDamageEvent.class, priority = 0)
    public static void onHeroCleave(CharFinalDamageEvent event) {
        if (Dungeon.hero == null) return;
        Char defender = event.getTarget();
        if (defender != Dungeon.hero) return;
        if (defender.buff(headCleaverTracker.class) == null) return;

        Buff.detach(defender, headCleaverTracker.class);

        if (defender.HP < defender.HT * 0.3f) {
            // 处决：HP 归零，由伤害管线正常触发死亡
            defender.HP = 0;
            GLog.n(Messages.get(HeadCleaver.headCleaverTracker.class, "ondeath"));
            Dungeon.fail(HeadCleaver.headCleaverTracker.class);
        } else {
            // 留 1 血
            defender.HP = 1;
        }
    }

    public static class headCleaverTracker extends FlavourBuff{
        public static final float DURATION = 1f;
    }
}
