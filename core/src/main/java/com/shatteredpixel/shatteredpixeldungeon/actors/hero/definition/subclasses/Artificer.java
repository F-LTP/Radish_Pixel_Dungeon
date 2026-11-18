package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 奇械师：法师的第三转职。
 *
 * 炼金配方能量消耗降低，并获得投掷武器/药水包等新配方；
 * 投掷武器会继承上一次使用的法杖特性。
 */
public class Artificer extends HeroSubClass {

	public Artificer() {
		super("ARTIFICER", HeroIcon.ARTIFICER);
	}

	@Override
	public Talent[] subclassT3() {
		return new Talent[]{ Talent.POTION_DISTILLATION, Talent.CIRCUIT_CHAIN, Talent.READY_MODIFICATION };
	}

	@Override
	public Talent[] subclassT4() {
		return new Talent[]{ Talent.WAND_DODGE, Talent.MAGIC_WORKMAN };
	}

	/** 线路串联：武器攻击距离加成（+1/+1/+2）。 */
	public static int attackReachBonus( Char owner ) {
		if (!(owner instanceof Hero)) {
			return 0;
		}
		Hero hero = (Hero) owner;
		if (hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.ARTIFICER) {
			return 0;
		}
		int pts = hero.pointsInTalent( Talent.CIRCUIT_CHAIN );
		if (pts <= 0) {
			return 0;
		}
		return pts >= 3 ? 2 : 1;
	}

	@Override
	public void onAttackProc( Hero hero, Char enemy, int damage, boolean hit, boolean wasEnemy ) {
		if (!hit || !wasEnemy || enemy == null) {
			return;
		}
		int pts = hero.pointsInTalent( Talent.CIRCUIT_CHAIN );
		if (pts <= 0) {
			return;
		}
		float cost = pts >= 2 ? 2f : 3f;

		ArrayList<MissileWeapon> missiles = hero.belongings.getAllItems( MissileWeapon.class );
		ArrayList<MissileWeapon> usable = new ArrayList<>();
		for (MissileWeapon m : missiles) {
			if (m != null && m.durabilityLeft() > 0) {
				usable.add( m );
			}
		}
		if (usable.isEmpty()) {
			return;
		}
		MissileWeapon m = Random.element( usable );
		m.repair( -Math.min( cost, m.durabilityLeft() ) );

		// 投掷武器继承上一次使用法杖的特性：附加魔法伤害
		com.shatteredpixel.shatteredpixeldungeon.items.Item w = hero.belongings.attackingWeapon();
		if (w instanceof MissileWeapon) {
			LastWand lw = hero.buff( LastWand.class );
			if (lw != null && !lw.wandClass.isEmpty()) {
				enemy.damage( new com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo(
						Math.max( 2, 2 + lw.wandLevel ),
						com.shatteredpixel.shatteredpixeldungeon.damage.DamageType.MAGICAL,
						hero, w, this ) );
			}
		}
	}

	// ===== 投掷武器继承上一次法杖的特性 =====

	public static int alchemyDiscount( Hero hero, int cost ) {
		if (hero != null && hero.subClass == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.ARTIFICER) {
			return Math.max( 0, cost - 2 );
		}
		return cost;
	}

	public static void recordWand( Hero hero, com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand ) {
		if (hero == null || wand == null
				|| hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.ARTIFICER) {
			return;
		}
		LastWand lw = Buff.affect( hero, LastWand.class );
		lw.wandClass = wand.getClass().getName();
		lw.wandLevel = wand.level();
	}

	public static class LastWand extends Buff {

		{
			type = buffType.POSITIVE;
			announced = false;
		}

		public String wandClass = "";
		public int wandLevel = 0;

		@Override
		public String icon() {
			return BuffIndicator.NONE;
		}

		private static final String WAND_CLASS = "wand_class";
		private static final String WAND_LEVEL = "wand_level";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( WAND_CLASS, wandClass );
			bundle.put( WAND_LEVEL, wandLevel );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			wandClass = bundle.getString( WAND_CLASS );
			wandLevel = bundle.getInt( WAND_LEVEL );
		}
	}

	// ===== 即用改装：把武器/护甲改装给友方单位 =====

	public static boolean canReadyModify( Hero hero, Char ally, Item gear ) {
		return hero.subClass == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.ARTIFICER
				&& hero.pointsInTalent( Talent.READY_MODIFICATION ) > 0
				&& ally instanceof Mob && ally.isAlive() && ally.alignment == Char.Alignment.ALLY
				&& (gear instanceof Weapon || gear instanceof Armor);
	}

	public static boolean readyModify( Hero hero, Char ally, Item gear ) {
		if (!canReadyModify( hero, ally, gear )) {
			return false;
		}
		int tier;
		if (gear instanceof Armor) {
			tier = ((Armor) gear).tier;
		} else if (gear instanceof MeleeWeapon) {
			tier = ((MeleeWeapon) gear).tier;
		} else if (gear instanceof MissileWeapon) {
			tier = ((MissileWeapon) gear).tier;
		} else {
			tier = 1;
		}
		int quality = Math.max( 0, tier + gear.level() );
		if (gear.isEquipped( hero )) {
			if (!((EquipableItem) gear).doUnequip( hero, false, false )) {
				return false;
			}
		} else {
			gear = gear.detach( hero.belongings.backpack );
			if (gear == null) {
				return false;
			}
		}

		Mob mob = (Mob) ally;
		Item previous;
		if (gear instanceof KindOfWeapon) {
			previous = mob.mobEquipment.weapon;
			mob.mobEquipment.weapon = (KindOfWeapon) gear;
		} else {
			previous = mob.mobEquipment.armor;
			mob.mobEquipment.armor = (Armor) gear;
		}
		if (previous != null) {
			com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.drop( previous, ally.pos ).sprite.drop();
		}
		((EquipableItem) gear).activate( ally );
		int points = hero.pointsInTalent( Talent.READY_MODIFICATION );
		ReadyMod mod = Buff.affect( ally, ReadyMod.class );
		mod.apply( quality * (2 + 2 * points), quality * 2 );
		if (gear instanceof KindOfWeapon) {
			mod.gaveWeapon = true;
		} else {
			mod.gaveArmor = true;
		}
		return true;
	}

	public static class ReadyMod extends Buff {

		public ReadyMod() {
			type = buffType.POSITIVE;
			announced = false;
		}

		public int bonusHp = 0;
		public int bonusDef = 0;
		private boolean gaveWeapon = false;
		private boolean gaveArmor = false;

		// 怪物的闪避值不进存档，读档后回到基础值，而这里记着的加成还在：
		// 读档挂回怪物身上时要把加成补上，否则加成凭空消失，之后解除改装时还会从基础值里再扣一次
		private boolean restored = false;

		@Override
		public boolean attachTo( Char target ) {
			if (!super.attachTo( target )) {
				return false;
			}
			if (restored) {
				restored = false;
				if (target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) {
					((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) target).defenseSkill += bonusDef;
				}
			}
			return true;
		}

		public void apply( int hp, int def ) {
			unapply();
			bonusHp = hp;
			bonusDef = def;
			if (target != null) {
				target.HT += hp;
				target.HP += hp;
				if (target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) {
					((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) target).defenseSkill += def;
				}
			}
		}

		private void unapply() {
			if (target != null && (bonusHp != 0 || bonusDef != 0)) {
				target.HT -= bonusHp;
				target.HP = Math.min( target.HP, target.HT );
				if (target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) {
					((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) target).defenseSkill -= bonusDef;
				}
			}
		}

		// 友方死亡时 Mob.die 只给敌人掉装备；这里按说明让改装上去的每件有一半概率留在原地
		@Override
		public void detach() {
			if (target instanceof Mob && !target.isAlive() && target.alignment == Char.Alignment.ALLY
					&& com.shatteredpixel.shatteredpixeldungeon.Dungeon.level != null) {
				Mob mob = (Mob) target;
				if (gaveWeapon && mob.mobEquipment.weapon != null) {
					if (Random.Int( 2 ) == 0) {
						com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.drop( mob.mobEquipment.weapon, mob.pos ).sprite.drop();
					}
					mob.mobEquipment.weapon = null;
				}
				if (gaveArmor && mob.mobEquipment.armor != null) {
					if (Random.Int( 2 ) == 0) {
						com.shatteredpixel.shatteredpixeldungeon.Dungeon.level.drop( mob.mobEquipment.armor, mob.pos ).sprite.drop();
					}
					mob.mobEquipment.armor = null;
				}
			}
			unapply();
			super.detach();
		}

		@Override
		public String icon() {
			return BuffIndicator.UPGRADE;
		}

		private static final String HP = "bonus_hp";
		private static final String DEF = "bonus_def";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( HP, bonusHp );
			bundle.put( DEF, bonusDef );
			bundle.put( "gave_weapon", gaveWeapon );
			bundle.put( "gave_armor", gaveArmor );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			bonusHp = bundle.getInt( HP );
			bonusDef = bundle.getInt( DEF );
			gaveWeapon = bundle.getBoolean( "gave_weapon" );
			gaveArmor = bundle.getBoolean( "gave_armor" );
			restored = true;
		}
	}

	// ===== 药剂蒸馏冷却 =====

	public static boolean canDistill( Hero hero ) {
		return hero != null
				&& hero.subClass == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.ARTIFICER
				&& hero.pointsInTalent( Talent.POTION_DISTILLATION ) > 0
				&& hero.buff( DistillCooldown.class ) == null;
	}

	public static void startDistillCooldown( Hero hero ) {
		int pts = hero.pointsInTalent( Talent.POTION_DISTILLATION );
		float cd = pts >= 3 ? 150f : (pts >= 2 ? 225f : 300f);
		Buff.affect( hero, DistillCooldown.class, cd );
	}

	public static class DistillCooldown extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff {

		{
			type = buffType.NEUTRAL;
			announced = false;
			revivePersists = true;
		}

		@Override
		public String icon() {
			return BuffIndicator.NONE;
		}
	}
}

