package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Bundle;

/**
 * 魔武士：战士的第三转职。
 *
 * 物理攻击会附加一段与手持武器附魔相关的魔法伤害（剑类翻倍）；
 * 使用法杖/法力结晶会强化下一次攻击的附魔强度（最多叠 10 层）；
 * 可自由穿脱带诅咒的装备，并能把诅咒转化为随机附魔（1000 回合冷却）。
 */
public class MagicWarrior extends HeroSubClass {

	public MagicWarrior() {
		super("MAGIC_WARRIOR", HeroIcon.MAGIC_WARRIOR);
	}

	@Override
	public Talent[] subclassT3() {
		return new Talent[]{ Talent.SPECTRAL_WARD, Talent.MAGIC_INGENUITY, Talent.STORM_ASSAULT };
	}

	@Override
	public Talent[] subclassT4() {
		return new Talent[]{ Talent.REVENGE_ROAR, Talent.THIRSTY_BLADE };
	}

	@Override
	public void onAttackProc( Hero hero, Char enemy, int damage, boolean hit, boolean wasEnemy ) {
		Buff.affect( hero, StormAssaultTracker.class ).onHit();
		if (!hit || !wasEnemy || damage <= 0) {
			return;
		}

		Item w = hero.belongings.attackingWeapon();
		if (!(w instanceof Weapon)) {
			return;
		}

		// 附魔强度：与奥术之戒一致
		float power = RingOfArcana.enchantPowerMultiplier( hero );

		MagicWarriorCharge charge = hero.buff( MagicWarriorCharge.class );
		float mult = 1f + (charge == null ? 0f : 0.25f * charge.stacks());

		int bonus = Math.round( damage * 0.2f * power * mult );
		if (w instanceof Sword) {
			bonus *= 2;
		}

		if (bonus > 0) {
			enemy.damage( new DamageInfo( bonus, DamageType.MAGICAL, hero, (Item) w, this ) );
		}

		// 幻灵护身 +3：击杀敌人获得 2 点破损纹章护盾
		if (enemy != null && !enemy.isAlive() && hero.pointsInTalent( Talent.SPECTRAL_WARD ) >= 3) {
			Buff.affect( hero, Barrier.class ).incShield( 2 );
		}

		// 消耗叠加
		if (charge != null) {
			charge.detach();
		}
	}

	@Override
	public void onDefenseProc( Hero hero, Char enemy, int damage ) {
		Buff.affect( hero, StormAssaultTracker.class ).onHit();
	}

	// 骤雨攻势：叠层足够时提升附魔强度（接入 RingOfArcana.enchantPowerMultiplier）
	public static float stormAssaultMultiplier( Char ch ) {
		if (!(ch instanceof Hero)) {
			return 1f;
		}
		Hero hero = (Hero) ch;
		if (hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.MAGIC_WARRIOR) {
			return 1f;
		}
		int pts = hero.pointsInTalent( Talent.STORM_ASSAULT );
		if (pts <= 0) {
			return 1f;
		}
		StormAssaultTracker t = hero.buff( StormAssaultTracker.class );
		if (t == null || t.hits() < 5) {
			return 1f;
		}
		float[] bonus = { 0f, 0.7f, 1.35f, 2.0f };
		return 1f + bonus[ Math.min( 3, pts ) ];
	}

	public static class StormAssaultTracker extends Buff {

		{
			type = buffType.POSITIVE;
			announced = false;
		}

		private int hits = 0;

		public int hits() {
			return hits;
		}

		public void onHit() {
			hits++;
		}

		@Override
		public boolean act() {
			hits = Math.max( 0, hits - 1 );
			spend( TICK );
			return true;
		}

		@Override
		public String icon() {
			return BuffIndicator.NONE;
		}

		private static final String HITS = "hits";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( HITS, hits );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			hits = bundle.getInt( HITS );
		}
	}

	// 幻灵护身：破损纹章护盾上限/回复速度的提升倍率
	public static float spectralWardMultiplier( Hero hero ) {
		if (hero == null
				|| hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.MAGIC_WARRIOR) {
			return 1f;
		}
		int pts = hero.pointsInTalent( Talent.SPECTRAL_WARD );
		if (pts <= 0) {
			return 1f;
		}
		float bonus = pts >= 2 ? 0.70f : 0.35f;
		return 1f + bonus * RingOfArcana.enchantPowerMultiplier( hero );
	}

	// 魔术巧思：阅读卷轴/使用法力结晶时对视野内敌人造成伤害（无明确目标时 50%）
	public static void magicIngenuityDamage( Hero hero ) {
		int pts = hero.pointsInTalent( Talent.MAGIC_INGENUITY );
		if (pts <= 0) {
			return;
		}
		int lo = pts >= 3 ? 10 : (pts >= 2 ? 7 : 4);
		int hi = pts >= 3 ? 20 : (pts >= 2 ? 15 : 10);
		int dmg = Char.combatRoll( lo, hi );
		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (mob.alignment == Char.Alignment.ENEMY && mob.isAlive()
					&& Dungeon.level.heroFOV[mob.pos]) {
				mob.damage( new DamageInfo( Math.round( dmg * 0.5f ), DamageType.MAGICAL, hero, null, hero ) );
			}
		}
	}

	// 使用法杖/法力结晶时调用，为下一次攻击叠加一层附魔强化
	public static void gainCharge( Hero hero ) {
		if (hero == null || hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.MAGIC_WARRIOR) {
			return;
		}
		Buff.affect( hero, MagicWarriorCharge.class ).addStack();
	}

	// 使用法力结晶时：叠加附魔强化（伤害由 Talent.onScrollUsed 触发，避免重复结算）
	public static void onMagicCrystalUsed( Hero hero ) {
		gainCharge( hero );
	}

	// 魔武士可自由穿脱被诅咒的装备
	public static boolean canHandleCursed( Hero hero ) {
		return hero != null
				&& hero.subClass == com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.MAGIC_WARRIOR;
	}

	// 装备被诅咒的物品时，把诅咒转化为随机附魔（1000 回合冷却）
	public static void purgeCurseOnEquip( Hero hero, Item item ) {
		if (!canHandleCursed( hero ) || !(item instanceof EquipableItem)) {
			return;
		}
		EquipableItem eq = (EquipableItem) item;
		if (!eq.cursed || hero.buff( CursePurgeCooldown.class ) != null) {
			return;
		}
		eq.cursed = false;
		eq.cursedKnown = true;
		hero.sprite.emitter().burst( ShadowParticle.UP, 8 );
		if (eq instanceof Weapon) {
			((Weapon) eq).enchant();
		} else if (eq instanceof Armor) {
			((Armor) eq).inscribe();
		}
		Buff.affect( hero, CursePurgeCooldown.class, 1000f );
		GLog.p( Messages.get( HeroSubClass.class, "magicwarrior_curse_purged" ) );
	}

	public static class CursePurgeCooldown extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff {

		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		@Override
		public String icon() {
			return BuffIndicator.NONE;
		}
	}

	public static class MagicWarriorCharge extends Buff {

		{
			type = buffType.POSITIVE;
		}

		private int stacks = 0;

		public int stacks() {
			return stacks;
		}

		public void addStack() {
			stacks = Math.min( 10, stacks + 1 );
		}

		@Override
		public String icon() {
			return BuffIndicator.UPGRADE;
		}

		@Override
		public String desc() {
			return Messages.get( this, "desc", stacks );
		}

		private static final String STACKS = "stacks";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( STACKS, stacks );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			stacks = bundle.getInt( STACKS );
		}
	}
}
