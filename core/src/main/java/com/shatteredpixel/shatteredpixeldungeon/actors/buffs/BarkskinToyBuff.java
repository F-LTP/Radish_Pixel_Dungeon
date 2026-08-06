package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 树肤 Buff — 降低最大生命值 10 点，获得常驻树肤效果
 */
public class BarkskinToyBuff extends Buff {

	public static final int MAX_HP_PENALTY = 10;
	public static final int BARKSKIN_LEVEL = 10;
	public static final int BARKSKIN_INTERVAL = 5;

	{
		type = buffType.POSITIVE;
	}

	@Override
	public boolean act() {
		// 持续刷新树肤效果
		if (target instanceof Hero) {
			Barkskin barkskin = target.buff(Barkskin.class);
			if (barkskin == null || barkskin.level() < BARKSKIN_LEVEL) {
				Barkskin.append(target, Barkskin.class).set(BARKSKIN_LEVEL, BARKSKIN_INTERVAL);
			}
		}
		spend(TICK);
		return true;
	}

	@Override
	public void detach() {
		// 移除时 detach 所有 barkskin
		for (Barkskin b : target.buffs(Barkskin.class)) {
			b.detach();
		}
		super.detach();
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", MAX_HP_PENALTY, BARKSKIN_LEVEL);
	}
}
