package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.watabou.utils.Bundle;

/** Fixed-strength poison used by Hemlock; its strength does not decay. */
public class NonDecayingPoison extends Poison {
    private int strength;
    private static final String STRENGTH = "strength";

    @Override public void storeInBundle(Bundle b) { super.storeInBundle(b); b.put(STRENGTH, strength); }
    @Override public void restoreFromBundle(Bundle b) { super.restoreFromBundle(b); strength = b.getInt(STRENGTH); }

    public NonDecayingPoison set(int strength) {
        this.strength = Math.max(1, strength);
        return this;
    }

    @Override
    public boolean act() {
        if (!target.isAlive()) { detach(); return true; }
        target.damage(new DamageInfo(Math.max(1, strength / 2), DamageType.POISON, null, null, this));
        spend(TICK);
        return true;
    }

    @Override public String iconTextDisplay() { return ""; }
}
