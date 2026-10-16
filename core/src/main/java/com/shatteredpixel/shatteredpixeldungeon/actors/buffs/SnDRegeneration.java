package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class SnDRegeneration extends Buff {

    {
        type = buffType.POSITIVE;
        actPriority = HERO_PRIO - 1;
    }

    private int healPerTurn;
    private int turnsLeft;

    public void set(int healPerTurn, int duration) {
        this.healPerTurn = healPerTurn;
        this.turnsLeft = duration;
    }

    @Override
    public boolean act() {
        if (target.HP < target.HT && healPerTurn > 0) {
            target.heal(healPerTurn);

            if (target.HP == target.HT && target instanceof Hero) {
                ((Hero) target).resting = false;
            }
        }

        turnsLeft--;
        if (turnsLeft <= 0) {
            if (target instanceof Hero) {
                ((Hero) target).resting = false;
            }
            detach();
        }

        spend(TICK);
        return true;
    }

    @Override
    public String icon() {
        return BuffIndicator.HEALING;
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString(turnsLeft);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", healPerTurn, turnsLeft);
    }

    private static final String HEAL = "heal";
    private static final String TURNS = "turns";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(HEAL, healPerTurn);
        bundle.put(TURNS, turnsLeft);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        healPerTurn = bundle.getInt(HEAL);
        turnsLeft = bundle.getInt(TURNS);
    }
}