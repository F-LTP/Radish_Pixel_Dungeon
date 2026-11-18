/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class GriffinEye extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ lightningBonus() };
	}


	public GriffinEye() {
		super(5, 3, 5, 18);
		image = "griffin_eye";
	}

	private int lightningBonus() {
		return 25 + 15 * level();
	}

	@Override
	protected void onMobDamaged(Hero hero, Mob target, DamageInfo info) {
		// 不限伤害来源：陷阱、盟友和怪物之间的闪电同样加深
		if (info.getType() == DamageType.LIGHTNING) {
			info.addFinalMultModifier(1f + lightningBonus() / 100f, "griffin eye");
		}
	}
}
