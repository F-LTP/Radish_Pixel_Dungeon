package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Random;

/**
 * 标记（特殊学派）：持续20回合，期间目标每次受到伤害时额外受到7-11点真伤。
 * 通过向攻击者构建的 DamageInfo 注入最终加算(FINAL_ADDITIVE)修正项实现。
 */
public class MarkDebuff extends FlavourBuff {

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    /** 本次伤害应附加的最终增伤值。 */
    public int bonusDamage() {
        return Random.IntRange(7, 11);
    }

    @Override
    public String icon() {
        return BuffIndicator.MARK;
    }

    @Override
    public String desc() {
        // 回合数按整数传：英文文本用的是 %d，传 float 进去格式化失败，
        // 说明会显示成没替换的原始格式串。取值和图标上的数字（(int) visualcooldown()）一致。
        return Messages.get(this, "desc", (int) visualcooldown());
    }
}
