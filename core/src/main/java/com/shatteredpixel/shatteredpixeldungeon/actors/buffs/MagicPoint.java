/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDiceMageSpells;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

/**
 * 骰子法师魔力点追踪Buff
 * 存储当前魔力点数，以及骰子法师法术的持久状态。
 */
public class MagicPoint extends Buff implements ActionIndicator.Action {

    {
        type = buffType.POSITIVE;
        revivePersists = true;
    }

    private float currentPoints = 0f;
    private int healValue = 50;
    private int surgeryUses = 0;
    private Class<? extends Mob> lastKilledMob;

    private static final String POINTS = "points";
    private static final String HEAL_VALUE = "heal_value";
    private static final String SURGERY_USES = "surgery_uses";
    private static final String LAST_KILLED_MOB = "last_killed_mob";

    public float getPoints() {
        return currentPoints;
    }

    public int getIntPoints() {
        return (int) currentPoints;
    }

    public void addPoints(float amount) {
        currentPoints += amount;
        updateAction();
        BuffIndicator.refreshHero();
    }

    public boolean spendPoints(int amount) {
        if (currentPoints >= amount) {
            currentPoints -= amount;
            updateAction();
            BuffIndicator.refreshHero();
            return true;
        }
        return false;
    }

    public int healValue() {
        return healValue;
    }

    public void decreaseHealValue() {
        healValue = Math.max(0, healValue - 5);
    }

    public int surgeryCostIncrease(int talentPoints) {
        return talentPoints == 1 ? 3 : talentPoints == 2 ? 2 : 1;
    }

    public int surgeryCost(int talentPoints) {
        return 3 + surgeryUses * surgeryCostIncrease(talentPoints);
    }

    public void surgeryUsed() {
        surgeryUses++;
    }

    public void recordKill(Class<? extends Mob> mobClass) {
        lastKilledMob = mobClass;
    }

    public Class<? extends Mob> lastKilledMob() {
        return lastKilledMob;
    }

    public void clearLastKilledMob() {
        lastKilledMob = null;
    }

    @Override
    public boolean act() {
        updateAction();
        spend(TICK);
        return true;
    }

    private void updateAction() {
        if (target instanceof Hero && ((Hero) target).subClass == HeroSubClass.DICE_MAGE) {
            if (ActionIndicator.action == null || ActionIndicator.action == this) {
                ActionIndicator.setAction(this);
            }
        } else {
            ActionIndicator.clearAction(this);
        }
    }

    @Override
    public void detach() {
        ActionIndicator.clearAction(this);
        super.detach();
    }

    @Override
    public String icon() {
        return BuffIndicator.MAGIC_POINT;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(0.5f, 0f, 1f); // 紫色调
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString(getIntPoints());
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", getIntPoints(), healValue);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(POINTS, currentPoints);
        bundle.put(HEAL_VALUE, healValue);
        bundle.put(SURGERY_USES, surgeryUses);
        if (lastKilledMob != null) {
            bundle.put(LAST_KILLED_MOB, lastKilledMob.getName());
        }
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        currentPoints = bundle.getFloat(POINTS);
        healValue = bundle.contains(HEAL_VALUE) ? bundle.getInt(HEAL_VALUE) : 50;
        surgeryUses = bundle.getInt(SURGERY_USES);
        if (bundle.contains(LAST_KILLED_MOB)) {
            try {
                Class<?> cls = Class.forName(bundle.getString(LAST_KILLED_MOB));
                if (Mob.class.isAssignableFrom(cls)) {
                    lastKilledMob = cls.asSubclass(Mob.class);
                }
            } catch (ClassNotFoundException ignored) {
                lastKilledMob = null;
            }
        }
        updateAction();
    }

    @Override
    public String actionName() {
        return Messages.get(this, "action_name");
    }

    @Override
    public String actionIcon() {
        return HeroIcon.DICE_MAGE;
    }

    @Override
    public int indicatorColor() {
        return 0x8844FF;
    }

    @Override
    public void doAction() {
        if (Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClass.DICE_MAGE) {
            GameScene.show(new WndDiceMageSpells());
        }
    }
}
