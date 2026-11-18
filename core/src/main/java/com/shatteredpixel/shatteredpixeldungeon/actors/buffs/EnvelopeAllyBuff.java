package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

/** 红包：随机一种怪物永久成为盟友。 */
public class EnvelopeAllyBuff extends AllyBuff {

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)) {
			if (target instanceof Mob) {
				((Mob) target).makeIntelligentAlly();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}
}
