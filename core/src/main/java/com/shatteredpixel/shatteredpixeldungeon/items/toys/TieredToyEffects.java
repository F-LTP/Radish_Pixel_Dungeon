package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TieredToyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.utils.Random;
import com.watabou.utils.Bundle;

public final class TieredToyEffects {

	private TieredToyEffects() {}

	public static boolean hasAnyTieredToy(Hero hero) {
		for (ItemArmorAttachable toy : ItemArmorAttachable.getAllAttachedToys()) {
			if (toy instanceof TieredToy) return true;
		}
		return false;
	}

	public static boolean has(Class<? extends TieredToy> type) {
		return ItemArmorAttachable.hasAttached(type);
	}

	public static int attackProc(Hero hero, Char enemy, int damage) {
		if (has(TieredToy.TwinDaggers.class) && hero.belongings.attackingWeapon() != null
				&& !(hero.belongings.attackingWeapon() instanceof MissileWeapon)) damage += Math.max(1, damage / 2);
		if (has(TieredToy.Shortsword.class)) damage += 4;
		if (has(TieredToy.Harpoon.class) && hero.belongings.attackingWeapon() instanceof MissileWeapon) Buff.prolong(enemy, Vulnerable.class, 3f);
		if (has(TieredToy.HissingRing.class) && Random.Int(6) == 0) Buff.affect(enemy, TieredToyPoison.class).set(10);
		TieredToyBuff state = hero.buff(TieredToyBuff.class);
		if (has(TieredToy.Nunchaku.class) && state != null) damage = Math.round(damage * state.chainMultiplier(enemy));
		if (has(TieredToy.ToothNecklace.class) && enemy.HP * 2 <= enemy.HT) damage = Math.round(damage * 1.5f);
		if (has(TieredToy.Doomblade.class)) damage *= 2;
		if (has(TieredToy.MiniCrossbow.class)
				&& hero.belongings.attackingWeapon() instanceof MissileWeapon
				&& enemy.HP == enemy.HT) damage *= 2;
		if (state != null && state.spongeTurns() > 0) damage = Math.round(damage * 1.5f);
		if (has(TieredToy.Hourglass.class) && state != null && Dungeon.depth > 0 && state.floorTurns() <= 10) {
			damage = Math.round(damage * 1.5f);
		}
		return damage;
	}

	public static void onKill(Hero hero) {
		if (has(TieredToy.Buckler.class)) {
			int wounded = 0;
			for (Mob mob : hero.getVisibleEnemies()) if (mob.isAlive() && mob.HP < mob.HT) wounded++;
			Buff.affect(hero, Barrier.class).incShield(3 + 3 * wounded);
		}
		if (has(TieredToy.Sponge.class)) hero.buff(TieredToyBuff.class).startSponge();
		if (has(TieredToy.Doomblade.class)) hero.damage(new DamageInfo(20, DamageType.TRUE, hero, null, TieredToy.Doomblade.class));
	}

	public static int adjustMaxHealth(int health) {
		if (has(TieredToy.BagOfHolding.class)) health += 10;
		TieredToy.CrackedPlate plate = ItemArmorAttachable.getAttachedToy(TieredToy.CrackedPlate.class);
		if (plate == null || !plate.active()) return health;
		return plate.lostMaxHealth() > 0
				? Math.max(1, health - plate.lostMaxHealth())
				: Math.max(1, Math.round(health * 0.4f));
	}

	public static boolean poisonImmune() {
		return has(TieredToy.Antivenom.class);
	}

	public static boolean preventDeath(Hero hero, int damage) {
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TieredToyBuff buff = hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TieredToyBuff.class);
		if (has(TieredToy.Determination.class) && buff != null && !buff.determinationUsed()
				&& damage >= hero.HP + hero.shielding()) {
			buff.useDetermination();
			return true;
		}
		return false;
	}

	public static int shieldGainMultiplier(int amount) {
		if (amount <= 0) return amount;
		float multiplier = 1f;
		Hero hero = Dungeon.hero;
		TieredToyBuff state = hero == null ? null : hero.buff(TieredToyBuff.class);
		if (state != null && has(TieredToy.Terrarium.class)) amount += state.growTerrariumShield();
		if (has(TieredToy.IronPendant.class)) amount = Math.max(amount + 2, Math.round(amount * 1.5f));
		if (hero != null && has(TieredToy.Ambrosia.class) && hero.HP * 2 <= hero.HT) multiplier *= 2f;
		if (state != null && state.spongeTurns() > 0) multiplier *= 1.5f;
		if (state != null && has(TieredToy.Hourglass.class) && state.floorTurns() <= 10) multiplier *= 1.5f;
		return Math.round(amount * multiplier);
	}

	public static void onAbilityUsed(Hero hero) {
		if (has(TieredToy.LifeBolt.class)) heal(hero, 10);
		if (has(TieredToy.BlindingBolt.class)) Buff.affect(hero, Barrier.class).incShield(20);
	}

	public static void heal(Hero hero, int amount) {
		if (amount <= 0) return;
		TieredToyBuff state = hero.buff(TieredToyBuff.class);
		if (has(TieredToy.Ambrosia.class) && hero.HP * 2 <= hero.HT) amount *= 2;
		if (state != null && state.spongeTurns() > 0) amount = Math.round(amount * 1.5f);
		if (state != null && has(TieredToy.Hourglass.class) && state.floorTurns() <= 10) amount = Math.round(amount * 1.5f);
		int missing = hero.HT - hero.HP;
		hero.heal(Math.min(missing, amount));
		int overflow = amount - missing;
		if (overflow > 0 && has(TieredToy.BloodChalice.class)) Buff.affect(hero, Barrier.class).incShield(overflow);
	}

	public static class TieredToyPoison extends Buff {
		private int turns;
		public TieredToyPoison set(int turns) { this.turns = turns; return this; }
		@Override public boolean attachTo(Char target) {
			return !(target == Dungeon.hero && poisonImmune()) && super.attachTo(target);
		}
		@Override public boolean act() {
			if (turns <= 0) { detach(); return true; }
			target.damage(new DamageInfo(2, DamageType.POISON, null, null, TieredToy.HissingRing.class));
			turns--;
			spend(TICK);
			return true;
		}
		@Override public void storeInBundle(Bundle bundle) { super.storeInBundle(bundle); bundle.put("turns", turns); }
		@Override public void restoreFromBundle(Bundle bundle) { super.restoreFromBundle(bundle); turns = bundle.getInt("turns"); }
	}
}
