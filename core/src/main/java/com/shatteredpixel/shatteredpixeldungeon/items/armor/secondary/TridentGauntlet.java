/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class TridentGauntlet extends SecondaryArmor {

	private static final float PHYSICAL_MULT = 0.7f;

	@Override
	public Object[] effectArgs() {
		return new Object[]{ elementalBonus() };
	}


	public TridentGauntlet() {
		super(3, 1, 3, 14);
		image = "trident_gauntlet";
	}

	private int elementalBonus() {
		return 20 + 10 * level();
	}

	@Override
	protected void onMobDamaged(Hero hero, Mob target, DamageInfo info) {
		if (info.getAttacker() != hero) return;

		DamageType type = info.getType();
		if (type == DamageType.FIRE || type == DamageType.FROST || type == DamageType.LIGHTNING) {
			info.addFinalMultModifier(1f + elementalBonus() / 100f, "trident gauntlet");
		} else if (type == DamageType.PHYSICAL || type == DamageType.PHYSICAL_NO_ARMOR) {
			// 无视护甲的物理伤害也是物理伤害：狙击手的远程投掷走的就是这个类型
			info.addFinalMultModifier(PHYSICAL_MULT, "trident gauntlet phys");
		}
	}
}
