/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class WebBelt extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ poisonBonus() };
	}


	public WebBelt() {
		super(4, 2, 4, 16);
		image = "web_belt";
	}

	private int poisonBonus() {
		return 15 + 10 * level();
	}

	@Override
	protected void onMobDamaged(Hero hero, Mob target, DamageInfo info) {
		// 不限伤害来源：只要是毒素伤害就加深
		if (info.getType() == DamageType.POISON) {
			info.addFinalMultModifier(1f + poisonBonus() / 100f, "web belt");
		}
	}
}
