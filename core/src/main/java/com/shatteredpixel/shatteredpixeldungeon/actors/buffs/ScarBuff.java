package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * 伤疤 Buff — 提升最大生命值 10 点
 * 这是一个标记 Buff，实际 HP 加成在 Hero.ht() 中检测。
 */
public class ScarBuff extends Buff {

	public static final int MAX_HP_BONUS = 10;

	{
		type = buffType.POSITIVE;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE; // TODO: 自定义图标
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", MAX_HP_BONUS);
	}
}
