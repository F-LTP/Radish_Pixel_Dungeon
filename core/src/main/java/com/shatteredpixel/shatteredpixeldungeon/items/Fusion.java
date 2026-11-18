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

	private static void removeEquipped(Item b) {
		if (Dungeon.hero == null) return;
		com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings bel = Dungeon.hero.belongings;
		if (bel.weapon == b) bel.weapon = null;
		if (bel.secondWep == b) bel.secondWep = null;
		if (bel.armor == b) bel.armor = null;
		if (bel.secondaryArmor == b) bel.secondaryArmor = null;
		if (bel.artifact == b) bel.artifact = null;
		if (bel.misc == b) bel.misc = null;
		if (bel.ring == b) bel.ring = null;
		if (bel.ring2 == b) bel.ring2 = null;
		if (bel.extraRings.remove(b)) { /* removed */ }
	}

	public static boolean fuse(Item a, Item b) {
		if (!canFuse(a, b)) return false;
		if (a.fusedItems == null) a.fusedItems = new java.util.ArrayList<>();
		a.fusedItems.add(b);

		if (a instanceof KindOfWeapon && b instanceof KindOfWeapon) {
			KindOfWeapon wa = (KindOfWeapon) a, wb = (KindOfWeapon) b;
			int add = Math.max(1, wb.min());
			wa.fusionMin += add;
			wa.fusionMax += add;
			if (wa instanceof Weapon && wb instanceof Weapon) {
				Weapon w1 = (Weapon) wa, w2 = (Weapon) wb;
				w1.RCH = Math.max(w1.RCH, w2.RCH);
				w1.ACC = Math.max(w1.ACC, w2.ACC);
				w1.DLY = Math.min(w1.DLY, w2.DLY);
			}
		} else if (a instanceof Armor && b instanceof Armor) {
			Armor aa = (Armor) a, ab = (Armor) b;
			int add = Math.max(1, ab.DRMin());
			aa.fusionDRMin += add;
			aa.fusionDRMax += add;
		}
		// 效果由各装备的 proc/activate 读取 fusedItems 合并；
		// 这里只把第二件挂到第一件上，外观保持第一件。

		if (!com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.consumeFusionCharge()) {
			GLog.w("没有融合机会");
			a.fusedItems.remove(b);
			return false;
		}
		removeEquipped(b);
		b.detachAll(Dungeon.hero.belongings.backpack);
		GLog.p("融合完成：" + a.name() + " 获得了 " + b.name() + " 的力量");
		return true;
	}
}
