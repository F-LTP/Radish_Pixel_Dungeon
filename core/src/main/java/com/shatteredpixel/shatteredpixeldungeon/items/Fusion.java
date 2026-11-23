package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FusionCharges;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 装备融合：把第二件装备的全部效果并进第一件，外观保留第一件。
 *
 * <p>不再限制类型（武器/护甲/法杖/戒指/神器/投掷物可以任意组合），
 * 被融合装备的：强化等级、数值、附魔/铭文、特殊被动都会并入主装备，
 * 并记录在主装备的「已融合装备」列表里（物品描述可见）。</p>
 */
public class Fusion {

	/** 什么算「装备」：可装备物品 + 法杖 + 各类武器。 */
	public static boolean isEquipment(Item it) {
		return it instanceof EquipableItem || it instanceof Wand || it instanceof KindOfWeapon;
	}

	public static boolean hasValidPair() {
		if (Dungeon.hero == null) return false;
		int count = 0;
		for (Item it : Dungeon.hero.belongings) {
			if (isEquipment(it)) count++;
			if (count >= 2) return true;
		}
		return false;
	}

	/** 任意两件装备都能融合（外观取第一件）。 */
	public static boolean canFuse(Item a, Item b) {
		if (a == null || b == null || a == b) return false;
		return isEquipment(a) && isEquipment(b);
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

	public static <T> T fusedItem(Item weapon, Class<T> type) {
		if (weapon == null) return null;
		if (type.isInstance(weapon)) return type.cast(weapon);
		if (weapon.fusedItems != null) {
			for (Item it : weapon.fusedItems) {
				if (type.isInstance(it)) return type.cast(it);
			}
		}
		return null;
	}

	/** 被融合装备的「数值强度」：武器取最小伤害，护甲取最小减伤，其它按 1 计。 */
	private static int powerOf(Item b) {
		if (b instanceof KindOfWeapon) return Math.max(1, ((KindOfWeapon) b).min());
		if (b instanceof Armor) return Math.max(1, ((Armor) b).DRMin());
		return 1;
	}

	/** 特殊装备的被动效果，融合后直接挂到英雄身上。 */
	private static void applyFusedPassives(Item b) {
		if (Dungeon.hero == null || b == null) return;
		if (b instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WhiteKingGodSword) {
			Buff.affect(Dungeon.hero,
					com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WhiteKingGodSword.OnlyOneEyeAttack.class);
		}
		if (b instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EnemyFlag) {
			Buff.affect(Dungeon.hero,
					com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EnemyFlag.MobWarningProject.class);
		}
		if (b instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Morello) {
			Buff.affect(Dungeon.hero,
					com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Morello.MorelloCabala.class)
					.updateMaxCharge(Dungeon.hero);
		}
		if (b instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Rlyeh) {
			Buff.affect(Dungeon.hero,
					com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Rlyeh.StateProject.class);
		}
		if (b instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ShadowBooks) {
			Buff.affect(Dungeon.hero,
					com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ShadowBooks.ShadowProject.class);
		}
		if (b instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Taijutsu) {
			Buff.affect(Dungeon.hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Calm.class);
			Buff.affect(Dungeon.hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CriticalAttack.class);
		}
	}

	public static boolean fuse(Item a, Item b) {
		if (!canFuse(a, b)) return false;

		if (!FusionCharges.consumeFusionCharge()) {
			GLog.w("没有融合机会");
			return false;
		}

		if (a.fusedItems == null) a.fusedItems = new java.util.ArrayList<>();
		a.fusedItems.add(b);

		// ---------- 1. 强化等级并入 ----------
		int donorLevel = Math.max(0, b.level());
		if (donorLevel > 0) {
			try {
				a.upgrade(donorLevel);
			} catch (Throwable ignored) {
			}
		}

		// ---------- 2. 数值并入（武器/护甲用 fusion 字段，跨类型也按强度折算） ----------
		int power = powerOf(b);
		if (a instanceof KindOfWeapon) {
			KindOfWeapon wa = (KindOfWeapon) a;
			wa.fusionMin += power;
			wa.fusionMax += power;
			if (wa instanceof Weapon && b instanceof Weapon) {
				Weapon w1 = (Weapon) wa, w2 = (Weapon) b;
				w1.RCH = Math.max(w1.RCH, w2.RCH);
				w1.ACC = Math.max(w1.ACC, w2.ACC);
				w1.DLY = Math.min(w1.DLY, w2.DLY);
			}
		}
		if (a instanceof Armor) {
			Armor aa = (Armor) a;
			aa.fusionDRMin += power;
			aa.fusionDRMax += power;
		}

		// ---------- 3. 附魔 / 铭文并入（主装备没有时继承） ----------
		if (a instanceof Weapon && b instanceof Weapon) {
			Weapon w1 = (Weapon) a, w2 = (Weapon) b;
			if (w1.enchantment == null && w2.enchantment != null) {
				try {
					w1.enchant(w2.enchantment);
				} catch (Throwable ignored) {
				}
			}
		}
		if (a instanceof Armor && b instanceof Armor) {
			Armor a1 = (Armor) a, a2 = (Armor) b;
			if (a1.glyph == null && a2.glyph != null) {
				a1.glyph = a2.glyph;
			}
		}

		// ---------- 4. 特殊被动 ----------
		applyFusedPassives(b);

		// ---------- 5. 消耗被融合的那件 ----------
		removeEquipped(b);
		try {
			b.detachAll(Dungeon.hero.belongings.backpack);
		} catch (Throwable ignored) {
		}

		GLog.p("融合完成：" + a.name() + " 吸收了 " + b.name() + " 的全部效果");
		return true;
	}
}
