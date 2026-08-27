package com.shatteredpixel.shatteredpixeldungeon.damage;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.FatedDraw;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Torturer;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Radish;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

public class OrdinaryAttackDamage {

	private OrdinaryAttackDamage() {
	}

	public static DamageInfo build(Char attacker, Char defender, int baseDamage, boolean critical,
								   float criticalMultiplier, float damageMultiplier, float damageBonus) {
		DamageInfo info = new DamageInfo(baseDamage,
				ignoresArmor(attacker, defender) ? DamageType.PHYSICAL_NO_ARMOR : DamageType.PHYSICAL,
				attacker, sourceItem(attacker), attacker);
		info.setCritical(critical, criticalMultiplier);
		info.addDirectMultModifier(damageMultiplier, "attack multiplier");

		applyOutgoingModifiers(attacker, defender, info);
		applyPreFinalModifiers(attacker, defender, info);
		info.addPreFinalAddModifier(damageBonus, "attack bonus");
		applyFinalModifiers(attacker, defender, info);

		// 圆球皮肤：近战伤害最终乘以 1.2（远程/投掷不生效）
		if (attacker instanceof Hero && ((Hero) attacker).isSphereSkin()
				&& !(sourceItem(attacker) instanceof MissileWeapon)) {
			info.addFinalMultModifier(1.2f, "sphere melee");
		}

		return info;
	}

	public static int rollDefenseReduction(Char attacker, Char defender) {
		return rollDefenseReduction(attacker, defender, false);
	}

	public static int rollDefenseReduction(Char attacker, Char defender, boolean includeBarkskin) {
		int dr = Math.round(defender.drRoll() * AscensionChallenge.statModifier(defender));

		if (defender instanceof Hero) {
			FatedDraw.FatedDrawTracker trackerD = defender.buff(FatedDraw.FatedDrawTracker.class);
			if (trackerD != null && trackerD.remainingChecks > 0) {
				trackerD.consume("defense_block");
			}
		}

		if (attacker instanceof Hero) {
			Hero h = (Hero) attacker;
			if (h.belongings.weapon() instanceof MissileWeapon
					&& h.subClass == HeroSubClasses.SNIPER
					&& !Dungeon.level.adjacent(h.pos, defender.pos)) {
				dr = 0;
			}

			if (h.pointsInTalent(Talent.LAND_HEART) >= 3 && nearbyGrass(attacker.pos)) {
				dr = 0;
			}
		}

		if (includeBarkskin) {
			Barkskin bark = defender.buff(Barkskin.class);
			if (bark != null) {
				dr += Random.NormalIntRange(0, bark.level());
			}
		}

		return dr;
	}

	public static DamageRoll rollBaseDamage(Char attacker) {
		Preparation prep = attacker.buff(Preparation.class);
		float damage;
		if (prep != null) {
			damage = prep.damageRoll(attacker);
			if (attacker == hero) {
				if (hero.hasTalent(Talent.BOUNTY_HUNTER)) {
					Buff.affect(hero, Talent.BountyHunterTracker.class, 0.0f);
				}
				if (hero.hasTalent(Talent.POWER_RECYCLE)) {
					Buff.affect(attacker, Talent.PowerRecycleTracker.class, 0.0f);
				}
			}
		} else {
			Item weapon = attacker.attackingWeapon();
			if (attacker instanceof Mob && weapon instanceof KindOfWeapon) {
				damage = ((KindOfWeapon) weapon).damageRoll(attacker);
			} else {
				damage = attacker.damageRoll();
			}
			if (attacker instanceof Hero) {
				FatedDraw.FatedDrawTracker trackerA = attacker.buff(FatedDraw.FatedDrawTracker.class);
				if (trackerA != null && trackerA.remainingChecks > 0) {
					trackerA.consume("attack_damage");
				}
			}
			if (attacker == hero && hero.hasTalent(Talent.POWER_RECYCLE)
					&& hero.pointsInTalent(Talent.POWER_RECYCLE) == 4
					&& Random.Int(2) == 0) {
				Buff.affect(attacker, Talent.PowerRecycleTracker.class, 0.0f);
			}
		}
		return new DamageRoll(damage, prep);
	}

	public static CriticalRoll rollCritical(Char attacker, Char defender, float baseDamage) {
		boolean critical = false;
		boolean surprise = defender instanceof Mob && ((Mob) defender).surprisedBy(attacker);
		float chance = attacker.baseCritSkill();
		float multiplier = attacker.baseCritDamage();
		float damage = baseDamage;

		Item weapon = attacker instanceof Hero
				? ((Hero) attacker).belongings.weapon()
				: attacker.attackingWeapon();
		if (weapon instanceof LongStick) {
			chance += attacker.defenseSkill(attacker);
		} else if (weapon instanceof Bloodblade) {
			chance += ((Bloodblade) weapon).sac;
		} else if (weapon instanceof GiantKiller) {
			critical = ((GiantKiller) weapon).isMustCrit;
		} else if (weapon instanceof Seekingspear) {
			Seekingspear ss = (Seekingspear) weapon;
			multiplier += 0.3f + 0.05f * ss.buffedLvl();
			if (surprise) {
				chance += 25f;
			}
		} else if (weapon instanceof MissileWeapon) {
			Talent.HoldBreathTracker hb = attacker.buff(Talent.HoldBreathTracker.class);
			if (hb != null) {
				chance += hb.crit_b;
				multiplier += hb.cd_b;
			}
		}

		if (attacker == hero) {
			Radish.GlobalCritChance globalCritChance = hero.buff(Radish.GlobalCritChance.class);

			if (hero.hasTalent(Talent.DEATHBLOW)) {
				chance += 15f;
			}
			if (globalCritChance != null) {
				chance += globalCritChance.critChance;
			}
		}

		if (!(attacker.buff(Calm.class) != null || attacker.buff(CriticalAttack.class) != null)) {
			multiplier = Math.min(multiplier, attacker.critDamageCap());
		}
		if (attacker.buff(Scythe.scytheSac.class) != null) {
			chance += 10f;
			multiplier += 0.1f;
		}
		if (attacker instanceof Hero && hero.hasTalent(Talent.DEATHBLOW) && surprise
				&& hero.pointsInTalent(Talent.DEATHBLOW) >= 2) {
			multiplier += 0.25f;
			if (hero.pointsInTalent(Talent.DEATHBLOW) == 3) {
				damage *= 1.15f;
			}
		}

		if (attacker.buff(RingOfTenacity.Tenacity.class) != null) {
			chance = 0;
		}

		if (Random.Float() * 100 < chance || critical
				|| (attacker.rawCritDamage() >= 3 && attacker instanceof Hero && hero.buff(CriticalAttack.class) != null)) {
			critical = true;
		}

		return new CriticalRoll(damage, critical, multiplier);
	}

	public static void applyOutgoingModifiers(Char attacker, Char defender, DamageInfo info) {
		AscensionChallenge.modifyOutgoingAttackDamage(attacker, info);
		for (Buff buff : attacker.buffs()) {
			buff.modifyOutgoingAttackDamage(attacker, defender, info);
		}
	}

	public static void applyPreFinalModifiers(Char attacker, Char defender, DamageInfo info) {
		for (Buff buff : attacker.buffs()) {
			buff.modifyPreFinalOutgoingAttackDamage(attacker, defender, info);
		}
	}

	public static void applyFinalModifiers(Char attacker, Char defender, DamageInfo info) {
		for (Buff buff : attacker.buffs()) {
			buff.modifyFinalOutgoingAttackDamage(attacker, defender, info);
		}
		for (Buff buff : defender.buffs()) {
			buff.modifyIncomingAttackDamage(attacker, defender, info);
		}
	}

	public static void applyPlateArmor(Char defender, DamageInfo info) {
		Item armor = Char.defendingArmor(defender);
		if (armor instanceof PlateArmor) {
			int before = info.getDamage();
			info.addFinalAddModifier(((PlateArmor) armor).damageReduce(defender, before) - before, "plate armor");
		}
	}

	/**
	 * 攻击后处理：defenseProc → 弱点 → attackProc → 诗。
	 * 护甲 DR 已移入 DamagePipeline 的「应用护甲」阶段；
	 * 黏稠刻印（Viscosity）延迟伤害改在「应用护甲」之后结算，优先级在护甲之后。
	 */
	public static int foldPostProcessing(Char attacker, Char defender, DamageInfo info) {
		int effectiveDamage = defender.defenseProc(attacker, info.getDamage());

		if (defender.buff(Vulnerable.class) != null) {
			effectiveDamage *= 1.33f;
		}

		effectiveDamage = attacker.attackProc(defender, effectiveDamage);

		if (attacker == hero) {
			PoemBuff poem = hero.buff(PoemBuff.class);
			if (poem != null) {
				effectiveDamage = poem.applyFinalDamage(hero, defender, effectiveDamage);
			}
		}

		info.addFinalAddModifier(effectiveDamage - info.getDamage(), "attack post-processing");
		return effectiveDamage;
	}

	private static boolean ignoresArmor(Char attacker, Char defender) {
		return attacker == Dungeon.hero
				&& Dungeon.hero.subClass == HeroSubClasses.SNIPER
				&& !Dungeon.level.adjacent(Dungeon.hero.pos, defender.pos)
				&& Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon;
	}

	public static boolean ignoresDefenseRoll(Char attacker) {
		Item weapon = attacker.attackingWeapon();
		boolean srcIsWieldingCS = weapon instanceof CelestialSphere;
		return attacker instanceof Torturer || srcIsWieldingCS;
	}

	private static Item sourceItem(Char attacker) {
		return attacker.attackingWeapon();
	}

	private static boolean nearbyGrass(int pos) {
		Point c = Dungeon.level.cellToPoint(pos);
		for (int y = Math.max(0, c.y - 1); y <= Math.min(Dungeon.level.height() - 1, c.y + 1); y++) {
			int left = Math.max(0, c.x - 1);
			int right = Math.min(Dungeon.level.width() - 1, c.x + 1);
			for (int curr = left + y * Dungeon.level.width(); curr <= right + y * Dungeon.level.width(); curr++) {
				if (Dungeon.level.map[curr] == Terrain.FURROWED_GRASS || Dungeon.level.map[curr] == Terrain.HIGH_GRASS) {
					return true;
				}
			}
		}
		return false;
	}

	public static class DamageRoll {
		public final float damage;
		public final Preparation preparation;

		private DamageRoll(float damage, Preparation preparation) {
			this.damage = damage;
			this.preparation = preparation;
		}
	}

	public static class CriticalRoll {
		public final float damage;
		public final boolean critical;
		public final float multiplier;

		private CriticalRoll(float damage, boolean critical, float multiplier) {
			this.damage = damage;
			this.critical = critical;
			this.multiplier = multiplier;
		}
	}
}
