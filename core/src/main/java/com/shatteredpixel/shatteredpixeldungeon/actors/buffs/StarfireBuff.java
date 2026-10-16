package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.events.CharFinalDamageEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 星火（法力学派 L3）：持续3回合。期间视野内怪物受到非骰子法师的非法术伤害（物理等）时，
 * 每10点最终伤害为你提供1点法力值。
 * 通过订阅 {@link CharFinalDamageEvent} 统计伤害。
 */
public class StarfireBuff extends FlavourBuff {

    public static final float DURATION = 3f;

    private float accumulated = 0f;

    /** 由事件订阅者在最终伤害事件中调用：记录怪物受到的非法术伤害。 */
    public void onMonsterNonSpellDamage(int dmg) {
        if (dmg <= 0) return;
        accumulated += dmg;
        int mp = (int) (accumulated / 10);
        if (mp > 0) {
            accumulated -= mp * 10;
            if (target != null) {
                Buff.affect(target, MagicPoint.class).addPoints(mp);
            }
        }
    }

    @SubscribeEvent(event = CharFinalDamageEvent.class, priority = 0)
    public static void onCharDamaged(CharFinalDamageEvent event) {
        Hero hero = Dungeon.hero;
        if (hero == null || hero.subClass != HeroSubClasses.DICE_MAGE) return;
        StarfireBuff sf = hero.buff(StarfireBuff.class);
        if (sf == null) return;

        Char target = event.getTarget();
        if (target == null || target.alignment != Char.Alignment.ENEMY) return;
        if (Dungeon.level == null || Dungeon.level.heroFOV == null) return;
        if (target.pos < 0 || target.pos >= Dungeon.level.heroFOV.length) return;
        if (!Dungeon.level.heroFOV[target.pos]) return;

        // 仅统计非法术伤害（物理等）
        if (event.getType() == null || event.getType().isMagical()) return;

        // 排除骰子法师自己造成的伤害
        Object src = event.getSource();
        if (src == hero || src instanceof DiceMageSpell) return;

        sf.onMonsterNonSpellDamage(event.getDamage());
    }

    @Override
    public String icon() {
        return BuffIndicator.MAGIC_POINT;
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", cooldown(), (int) accumulated);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("accumulated", accumulated);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        accumulated = bundle.getFloat("accumulated");
    }
}
