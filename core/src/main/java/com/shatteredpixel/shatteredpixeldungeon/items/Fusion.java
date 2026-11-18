package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/** 红包融合：把第二件装备挂到第一件上，保留第一件外观。 */
public class Fusion {

	public static boolean hasValidPair() {
		if (Dungeon.hero == null) return false;
		int weapons = 0, armors = 0, wands = 0, rings = 0;
		for (Item it : Dungeon.hero.belongings) {
			if (it instanceof Weapon) weapons++;
			else if (it instanceof Armor) armors++;
			else if (it instanceof Wand) wands++;
			else if (it instanceof Ring) rings++;
		}
		return weapons >= 2 || armors >= 2 || wands >= 2 || rings >= 2;
	}

	public static boolean canFuse(Item a, Item b) {
		if (a == null || b == null || a == b) return false;
		if (a instanceof Weapon && b instanceof Weapon) return true;
		if (a instanceof Armor && b instanceof Armor) return true;
		if (a instanceof Wand && b instanceof Wand) return true;
		if (a instanceof Ring && b instanceof Ring) return true;
		return false;
	}

	public static boolean fuse(Item a, Item b) {
		if (!canFuse(a, b)) return false;
		if (a.fusedItems == null) a.fusedItems = new java.util.ArrayList<>();
		a.fusedItems.add(b);

		if (a instanceof Weapon && b instanceof Weapon) {
			Weapon wa = (Weapon) a, wb = (Weapon) b;
			wa.fusionMin += Math.min(wa.min(), wb.min());
			wa.fusionMax += Math.min(wa.max(), wb.max());
		} else if (a instanceof Armor && b instanceof Armor) {
			Armor aa = (Armor) a, ab = (Armor) b;
			aa.fusionDRMin += Math.min(aa.DRMin(), ab.DRMin());
			aa.fusionDRMax += Math.min(aa.DRMax(), ab.DRMax());
		}
		// 效果由各装备的 proc/activate 读取 fusedItems 合并；
		// 这里只把第二件挂到第一件上，外观保持第一件。

		if (!com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.consumeFusionCharge()) {
			GLog.w("没有融合机会");
			a.fusedItems.remove(b);
			return false;
		}
		b.detachAll(Dungeon.hero.belongings.backpack);
		GLog.p("融合完成：" + a.name() + " 获得了 " + b.name() + " 的力量");
		return true;
	}
}
