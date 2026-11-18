package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemAnimations;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.ChordHelper;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import static com.shatteredpixel.shatteredpixeldungeon.utils.ChordHelper.generateMajor7;

public class EchoplexHammer extends MeleeWeapon {

    {
        image = ItemSpriteSheet.CALLHAMR;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 0.8f;
        tier = 5;
    }

    @Override
    public ItemAnimations animations() {
        //回音锤脉动：声明式动画集，其余动画可声明后经 ItemSprite.playAnim 在恰当时机触发
        return new ItemAnimations()
                .of( ItemSpriteSheet.ECHOPLEX_HAMMER_ANIM, 16 )
                .add( ItemAnimations.IDLE, 0, 13, 10, true );
    }

    @Override
    public int STRReq(int lvl) {
        return (9 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    public int min(int lvl) {
        return 10 + lvl * 2;
    }
    @Override
    public int max(int lvl) {
        return 30 + lvl * 5;
    }

    // proc 发生在伤害结算之前，这时还不知道目标会不会死：护甲、护盾、抗性、首领的伤害上限都还没算。
    // 以前在这里拿减免前的伤害预判击杀并直接调 die()，会把本该活下来的目标（包括被敌人用这把锤子打的英雄）判死。
    // 现在 proc 只记下「谁用哪把锤子打了谁」，等这一击的伤害真正落到目标身上（AttackHitEvent）再看它死没死
    private static EchoplexHammer pendingHammer;
    private static Char pendingAttacker;
    private static Char pendingDefender;

    @Override
    public int proc(Char attacker, Char defender, int damage ) {
        damage = super.proc(attacker, defender, damage);
        if (!defender.isAlive()) {
            // 附魔在 proc 里就把目标打死了：攻击流程到此为止，不会再发命中事件
            pendingHammer = null;
            pendingAttacker = pendingDefender = null;
            onKill(this, attacker, defender);
        } else {
            pendingHammer = this;
            pendingAttacker = attacker;
            pendingDefender = defender;
        }
        return damage;
    }

    // 优先级排在斩杀(-300)、暗杀(-400)之后：那些效果补刀打死的也算这一击的击杀
    @SubscribeEvent(event = AttackHitEvent.class, priority = -500)
    public static void onAttackHit(AttackHitEvent event) {
        if (pendingHammer == null) return;

        EchoplexHammer hammer = pendingHammer;
        boolean sameHit = event.getAttacker() == pendingAttacker
                && event.getDefender() == pendingDefender
                && (event.getAttackerWeapon() == null || event.getAttackerWeapon() == hammer);
        pendingHammer = null;
        pendingAttacker = pendingDefender = null;

        if (sameHit && !event.getDefender().isAlive()) {
            onKill(hammer, event.getAttacker(), event.getDefender());
        }
    }

    private static void onKill( Weapon weapon, Char attacker, Char defender ){
        // 冲击波打的是「英雄视野里的敌方怪物」，只对英雄一方的攻击者有意义。
        // 敌人（石像、聪明怪）拿到这把锤子时不触发，否则震到的是它自己人
        if (attacker.alignment != Char.Alignment.ALLY) return;
        KillEffect(weapon, attacker, defender);
    }

    public static void KillEffect( Weapon weapon , Char attacker, Char defender ){
        float[] pitches = ChordHelper.generateMajor7();

        int pitchIndex = 0;
        boolean shouldEchoAgain = true;
        do {
            shouldEchoAgain = doEcho(weapon, attacker, defender);
            if (pitchIndex <= 3) {
                Sample.INSTANCE.play( Assets.Sounds.LARGE_BELL , 0.7f , pitches[pitchIndex++]);
            }
        } while (shouldEchoAgain);

    }

    public static boolean doEcho(Weapon weapon , Char attacker, Char defender){
        boolean killedAny = false;

        int radius = 10;
        WandOfBlastWave.BlastWave.blast(defender.pos, radius);

        Mob[] mobs = Dungeon.level.mobs.toArray(new Mob[0]);
        for (Mob mob : mobs) {
            // 已经死了的（被打中的那只、上一波震死后还留在列表里的）不再吃伤害、也不再算一次击杀，
            // 否则每一波都「又杀了一只」，回音停不下来
            if (!mob.isAlive()) continue;
            if (mob.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[mob.pos]) {
                // 死亡结算（经验、掉落、击杀数）由 damage() 自己完成；这里再调一次 die() 会全部结算两遍
                mob.damage(DamageInfo.of(10 + 2 * weapon.buffedLvl(), DamageType.MAGICAL, null, weapon));
                if (!mob.isAlive()) {
                    killedAny = true;
                }
            }
        }
        // 不可以删除
        Dungeon.observe();
        return killedAny;
    }

    @Override
    public String desc() {

        String desc;

        if(isIdentified()){
        			desc = Messages.get(this, "desc",10 + 2 * buffedLvl());
        		} else {
        			desc = Messages.get(this, "normal_desc",10);
        		}

        return desc;
    }

}
