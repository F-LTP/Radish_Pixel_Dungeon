package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class Soulstaker extends FlavourBuff {

    public static final float DURATION = 30f;

    private int prevTargetHp = 0;

    // 还剩多少回合。FlavourBuff 的做法是把持续时间直接记成「下一次 act 的时刻」，到点就结束；
    // 但这个状态要每回合把回复量结算成伤害，所以持续时间改记在这里，act 自己一回合走一拍。
    private float left = 0;

    // 读档恢复时 attachTo 也会被调用，用它区分「新挂上」和「从存档读回来」
    private boolean restored = false;

    private static final String PREV_HP	= "prev_hp";
    private static final String LEFT	= "left";
    private static final String PER_TURN	= "per_turn";

    {
        type = buffType.NEUTRAL;
        announced = true;
    }


    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( PREV_HP, prevTargetHp );
        bundle.put( LEFT, left );
        bundle.put( PER_TURN, true );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        restored = true;
        prevTargetHp = bundle.getInt(PREV_HP);
        if (bundle.contains(PER_TURN)){
            left = bundle.getFloat(LEFT);
        } else {
            // 改成逐回合结算之前存下的档：那时剩余时间记在 Actor 的 time 里（存档时刻为 0 起算），
            // left 一直停在 30。把剩余时间搬到 left，并让它从读档这一拍开始逐回合走，
            // 否则会先空等到原定的结束时刻，再多跑 30 回合。
            left = Math.max(1f, bundle.getFloat("time"));
            clearTime();
        }
    }

    @Override
    public boolean attachTo(Char target) {
        if (!restored) prevTargetHp = target.HP;
        restored = false;

        return super.attachTo(target);
    }

    // Buff.affect( target, Soulstaker.class, 持续时间 ) 是通过 spend 把持续时间交进来的。
    // 这里只把它记进 left，不推后下一次 act：否则第一次结算要等到持续时间走完才发生，
    // 30 回合里的回复就不是「每点都转成伤害」，而是只在结束那一刻按净回复量结算一次。
    @Override
    protected void spend(float time) {
        left += time;
    }

    @Override
    protected void postpone(float time) {
        left = Math.max(left, time);
    }

    @Override
    public boolean act() {
        int healed = Math.max(0, target.HP - prevTargetHp);
        prevTargetHp = target.HP;

        if (healed > 0 && target.fieldOfView != null) {
            // 这一下可能当场打死怪，怪死亡时会把自己从 level.mobs 里移走，所以遍历一份拷贝
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                if (mob.isAlive() && mob.alignment == Char.Alignment.ENEMY
                        && mob.pos >= 0 && mob.pos < target.fieldOfView.length
                        && target.fieldOfView[mob.pos]) {
                    mob.damage(DamageInfo.of(healed, DamageType.MAGICAL, target, this));
                }
            }
        }

        left -= TICK;
        if (left <= 0) {
            detach();
        } else {
            super.spend( TICK );
        }

        return true;
    }

    @Override
    public String icon() {
        return BuffIndicator.CORRUPT;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(0f, 0.75f, 1f);
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0, (DURATION - left) / DURATION);
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString((int) Math.ceil(left));
    }

    @Override
    public void detach() {
        // Sacrifice heal
        // 暗色契约在施放时扣了 等级*2 的血，这里是把它还回来（+1 还一半，+2 起全还）。
        // 没点这个天赋的人施放时没扣血，结束时也就没有可还的。
        if(target instanceof Hero && ((Hero)target).hasTalent(Talent.SACRIFICE)){
            if(((Hero)target).pointsInTalent(Talent.SACRIFICE) > 1){
                Buff.affect(target, Healing.class).setHeal(((Hero) target).lvl * 2,1,0);
            } else {
                Buff.affect(target, Healing.class).setHeal(((Hero) target).lvl,1,0);
            }
        }

        super.detach();
    }
}
