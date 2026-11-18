/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class CatalystBracer extends SecondaryArmor {

	@Override
	public Object[] effectArgs() {
		return new Object[]{ 1 + 0.2f * level() };
	}


	public CatalystBracer() {
		super(1, 0, 1, 10);
		image = "catalyst_bracer";
	}

	/** 使用药水或卷轴后回馈炼金能量。 */
	public static void onUse( Hero hero ) {
		if (hero == null) return;
		SecondaryArmor armor = hero.belongings.secondaryArmor();
		if (!(armor instanceof CatalystBracer)) return;
		if (hero.STR() < armor.STRReq()) return;

		float gain = 1f + 0.2f * armor.level();
		hero.alchemyEnergy += gain;
		GLog.i( Messages.get( CatalystBracer.class, "energy_gain", gain ) );
	}
}
