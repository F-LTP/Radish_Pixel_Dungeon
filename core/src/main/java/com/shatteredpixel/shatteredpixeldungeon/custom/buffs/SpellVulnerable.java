package com.shatteredpixel.shatteredpixeldungeon.custom.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.IDamageModifier;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 法术易伤：使目标受到的所有魔法/元素伤害提高。
 * 由“颠茄”命中时施加。
 */
public class SpellVulnerable extends FlavourBuff implements IDamageModifier {

    public static final float DAMAGE_FACTOR = 1.33f;

    private float factor = DAMAGE_FACTOR;

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    @Override
    public String icon() {
        return BuffIndicator.SPELL_VULNERABLE;
    }

    @Override
    public float finalMultiply(int dmg, DamageInfo info) {
        return info.getType().isMagical() ? factor : 1f;
    }

    private static final String FACTOR = "factor";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(FACTOR, factor);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        factor = bundle.getFloat(FACTOR);
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", dispTurns());
    }
}
