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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 骰子法师法术基类。
 */
public abstract class DiceMageSpell {

    public String name() {
        return Messages.get(getClass(), "name");
    }

    public String desc() {
        return Messages.get(getClass(), "desc");
    }

    public abstract int mpCost();

    public boolean canCast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;

        MagicPoint mp = hero.buff(MagicPoint.class);
        return mp != null && mp.getIntPoints() >= mpCost();
    }

    public void cast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return;

        if (!canCast()) {
            GLog.w(Messages.get(DiceMageSpell.class, "no_mp"));
            return;
        }

        onCast(hero);
    }

    protected boolean spendMagic(Hero hero) {
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp != null && mp.spendPoints(mpCost())) {
            return true;
        }
        GLog.w(Messages.get(DiceMageSpell.class, "no_mp"));
        return false;
    }

    protected abstract void onCast(Hero hero);

    protected void getTarget(CellSelector.Listener listener) {
        GameScene.selectCell(listener);
    }

    protected boolean isValidEnemy(Char target) {
        return target != null && target.alignment == Char.Alignment.ENEMY;
    }

    protected boolean isValidAlly(Char target) {
        return target != null && target.alignment == Char.Alignment.ALLY;
    }
}
