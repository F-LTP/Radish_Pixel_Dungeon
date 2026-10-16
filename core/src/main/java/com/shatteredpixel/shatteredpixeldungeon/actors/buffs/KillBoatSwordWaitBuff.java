package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

public class KillBoatSwordWaitBuff extends Buff {

    {
        type = buffType.POSITIVE;
    }

    public int pos = -1;

    @Override
    public boolean act() {
        if (pos == -1) pos = target.pos;
        if (pos != target.pos) {
            detach();
        } else {
            spend(TICK);
        }
        return true;
    }

    @Override
    public String icon() {
        return BuffIndicator.ARMOR;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(1.9f, 2.4f, 3.25f);
    }

    @Override
    public String desc() {
        int weaponLevel = Dungeon.hero.belongings.weapon() != null ? Dungeon.hero.belongings.weapon().level() : 0;
        return Messages.get(this, "desc", 4 + weaponLevel, 8 + weaponLevel);
    }


}
