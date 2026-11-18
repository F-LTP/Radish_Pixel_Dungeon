/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class ShiningGloves extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ fullHpBonus() };
	}


	public ShiningGloves() {
		super(4, 1, 2, 16);
		image = "shining_gloves";
	}

	private int fullHpBonus() {
		return 25 + 5 * level();
	}

	@Override
	protected void onMobDamaged(Hero hero, Mob target, DamageInfo info) {
		if (info.getAttacker() != hero) return;

		// 两个条件各自独立成立，同时满足时乘两次
		float mult = 1f + fullHpBonus() / 100f;
		if (hero.HP >= hero.HT) {
			info.addFinalMultModifier(mult, "shining gloves full hp hero");
		}
		// 「满血敌人」只算敌对目标：范围法杖扫到的满血盟友、中立单位不该被额外增伤
		if (target.alignment == Char.Alignment.ENEMY && target.HP >= target.HT) {
			info.addFinalMultModifier(mult, "shining gloves full hp enemy");
		}
	}
}
