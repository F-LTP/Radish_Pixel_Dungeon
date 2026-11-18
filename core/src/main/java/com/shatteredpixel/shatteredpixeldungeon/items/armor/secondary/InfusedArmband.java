/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.InfusedArmbandBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;

public class InfusedArmband extends SecondaryArmor {

	private static final float BOOST_DURATION = 10f;

	@Override
	public Object[] effectArgs() {
		return new Object[]{ damageBoost(), barrierAmount() };
	}


	public InfusedArmband() {
		super(2, 0, 2, 11);
		image = "infused_armband";
	}

	private int damageBoost() {
		return 1 + level() / 2;
	}

	private int barrierAmount() {
		return 2 + level();
	}

	@Override
	protected void onMobDamaged(Hero hero, Mob target, DamageInfo info) {
		if (info.getAttacker() != hero) return;

		// 多数法杖把自己同时填在 sourceItem 和 source 上；解离、新星等走四参重载，只落在 source
		if (info.getSourceItem() instanceof Wand || info.getSource() instanceof Wand) {
			InfusedArmbandBuff.apply(hero, damageBoost(), BOOST_DURATION);
			Buff.affect(hero, Barrier.class).setShield(barrierAmount());
		}
	}
}
