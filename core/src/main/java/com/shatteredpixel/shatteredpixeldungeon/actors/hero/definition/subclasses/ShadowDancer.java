package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Bundle;

/**
 * 影舞者：盗贼的第三转职。
 *
 * 连续闪避敌人 3 次攻击后进入 3 回合隐身；解除隐身时冲刺穿过敌人并造成 125% 武器伤害。
 * 武器与投掷武器的攻击会打出第二段伤害（双重打击），并能强化斗篷（阴影之赐）。
 */
public class ShadowDancer extends HeroSubClass {

	public ShadowDancer() {
		super("SHADOW_DANCER", HeroIcon.SHADOW_DANCER);
	}

	@Override
	public Talent[] subclassT3() {
		return new Talent[]{ Talent.DOUBLE_STRIKE, Talent.SHADOWS_GIFT, Talent.SHADOW_RUSH };
	}

	@Override
	public Talent[] subclassT4() {
		return new Talent[]{ Talent.DEATHBLOW, Talent.LIGHT_CLOAK };
	}

	@Override
	public void onAttackProc( Hero hero, Char enemy, int damage, boolean hit, boolean wasEnemy ) {
		if (!hit || !wasEnemy || damage <= 0 || enemy == null) {
			return;
		}
		int pts = hero.pointsInTalent( Talent.DOUBLE_STRIKE );
		if (pts <= 0) {
			return;
		}
		Item w = hero.belongings.attackingWeapon();
		if (!(w instanceof KindOfWeapon)) {
			return;
		}
		// 双重打击：武器 / 投掷武器共结算两次伤害（每次 55%/65%）
		float pct = pts >= 2 ? 0.65f : 0.55f;
		int extra = Math.round( damage * (2f * pct - 1f) );
		if (extra > 0 && enemy.isAlive()) {
			enemy.damage( new DamageInfo( extra, DamageType.PHYSICAL, hero, w, this ) );
		}
	}

	@Override
	public void onDefenseProc( Hero hero, Char enemy, int damage ) {
		// 受到伤害，连击链重置
		ShadowDodgeCounter c = hero.buff( ShadowDodgeCounter.class );
		if (c != null) {
			c.reset();
		}
	}

	/** 影舞者：斗篷之下的闪避加成倍率（阴影之赐 +2/+3）。 */
	public static float stealthEvasionMultiplier( Char ch ) {
		if (!(ch instanceof Hero)) {
			return 1f;
		}
		Hero hero = (Hero) ch;
		if (hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.SHADOW_DANCER) {
			return 1f;
		}
		int pts = hero.pointsInTalent( Talent.SHADOWS_GIFT );
		if (pts < 2) {
			return 1f;
		}
		com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows cloak =
				hero.belongings.getItem( com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows.class );
		if (cloak == null) {
			return 1f;
		}
		float per = pts >= 3 ? 0.23f : 0.15f;
		return 1f + cloak.level() * per;
	}

	/** 闪避一次敌人攻击时由 Char 调用。 */
	public static void onDodged( Hero hero ) {
		if (hero == null || hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.SHADOW_DANCER) {
			return;
		}
		ShadowDodgeCounter c = Buff.affect( hero, ShadowDodgeCounter.class );
		c.count++;

		// 阴影之赐 +2/+3：斗篷之下闪避后的一次攻击视为伏击
		if (hero.pointsInTalent( Talent.SHADOWS_GIFT ) >= 2
				&& hero.buff( com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows.cloakStealth.class ) != null) {
			Buff.affect( hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation.class );
		}

		if (c.count >= 3) {
			c.reset();
			Buff.affect( hero, ShadowVeil.class, 3f );
		}
	}

	public static boolean rushing = false;

	/** 只要处于任意隐身（主动隐身/斗篷之下/闪避触发）即可突袭。 */
	public static boolean hasVeil( Hero hero ) {
		return hero != null && hero.invisible > 0;
	}

	/** 以攻击破除隐身：该次攻击替换为对目标的突袭（任意隐身来源均可）。 */
	public static void rushFromVeil( Hero hero, Char first ) {
		if (hero == null || first == null || !hasVeil( hero )) {
			return;
		}
		rushing = true;
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility.dispel( hero );
		rushing = false;
		if (Dungeon.level == null || Dungeon.level.heroFOV == null || !hero.isAlive()) {
			return;
		}

		int pts = hero.pointsInTalent( Talent.SHADOW_RUSH );
		int maxRush = pts >= 2 ? 3 : (pts >= 1 ? 2 : 1);
		boolean allowSame = pts >= 3;

		java.util.ArrayList<Char> used = new java.util.ArrayList<>();
		used.add( first );
		rush( hero, first );
		int range = 4;
		for (int i = 1; i < maxRush; i++) {
			Char t = nearestEnemy( hero, range, used, allowSame );
			if (t == null) {
				break;
			}
			used.add( t );
			rush( hero, t );
			range = Math.max( 1, range - 1 );
		}
	}

	/** 隐身结束（解除隐身）时冲刺，按影子闪袭层数可连续多次。 */
	public static void onVeilEnd( Hero hero ) {
		if (hero == null || hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.SHADOW_DANCER) {
			return;
		}
		if (Dungeon.level == null || Dungeon.level.heroFOV == null || !hero.isAlive()) {
			return;
		}

		// 非攻击破隐：弹出选择突袭目标点
		openSelector( hero );
	}

	/** 选择突袭方向：对所选位置附近的最近敌人发动突袭。 */
	private static com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector.Listener rushListener =
			new com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			Hero hero = Dungeon.hero;
			if (target == null || hero == null) {
				return;
			}
			rushing = true;
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility.dispel( hero );
			rushing = false;
			dashTo( hero, target );
			hero.spendAndNext( 1f );
		}

		@Override
		public String prompt() {
			return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get( HeroSubClass.class, "shadow_dancer_rush_prompt" );
		}
	};

	/** 打开突袭目标点选择。 */
	public static void openSelector( Hero hero ) {
		if (hero == null || Dungeon.level == null || Dungeon.level.heroFOV == null || !hero.isAlive()) {
			return;
		}
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.selectCell( rushListener );
	}

	/** 突袭：向指定目标点冲刺，途中最多穿过 1 名敌人并造成 125% 武器伤害。 */
	public static void dashTo( Hero hero, int dest ) {
		if (hero == null || Dungeon.level == null || Dungeon.level.heroFOV == null || !hero.isAlive()) {
			return;
		}
		int w = Dungeon.level.width();
		int ox = hero.pos % w, oy = hero.pos / w;
		int gx = dest % w, gy = dest / w;
		int stepx = Integer.signum( gx - ox );
		int stepy = Integer.signum( gy - oy );
		if (stepx == 0 && stepy == 0) {
			return;
		}
		float ang = (float) Math.toDegrees( Math.atan2( gy - oy, gx - ox ) );

		int maxDist = 5;
		int pos = hero.pos;
		int enemyPassed = 0;
		for (int i = 0; i < maxDist; i++) {
			int nx = pos % w + stepx;
			int ny = pos / w + stepy;
			if (nx < 0 || ny < 0 || nx >= w || ny >= Dungeon.level.height()) {
				break;
			}
			int next = ny * w + nx;
			if (!Dungeon.level.passable[next] || Dungeon.level.solid[next]) {
				break;
			}
			Char ch = Actor.findChar( next );
			if (ch != null) {
				if (ch.alignment == Char.Alignment.ENEMY && enemyPassed < 1) {
					int dmg = Math.round( hero.damageRoll() * 1.25f );
					ch.damage( new DamageInfo( dmg, DamageType.PHYSICAL, hero, hero.belongings.attackingWeapon(), hero ) );
					enemyPassed++;
				} else {
					break;
				}
			}
			pos = next;
			if (pos == dest) {
				break;
			}
		}

		com.shatteredpixel.shatteredpixeldungeon.effects.DashSlashEffect.showOn( hero, ang );
		if (pos != hero.pos) {
			hero.pos = pos;
			hero.sprite.place( pos );
			hero.sprite.visible = true;
			Dungeon.level.pressCell( pos );
			Dungeon.observe();
		}
	}

	private static Char nearestEnemy( Hero hero, int range, java.util.List<Char> used, boolean allowSame ) {
		Char target = null;
		float best = Float.MAX_VALUE;
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Char.Alignment.ENEMY && ch.isAlive()
					&& Dungeon.level.heroFOV[ch.pos]
					&& Dungeon.level.trueDistance( hero.pos, ch.pos ) <= range
					&& (allowSame || !used.contains( ch ))) {
				float d = Dungeon.level.trueDistance( hero.pos, ch.pos );
				if (d < best) {
					best = d;
					target = ch;
				}
			}
		}
		return target;
	}

	private static void rush( Hero hero, Char target ) {
		int dx = Dungeon.level.width();
		int ox = hero.pos % dx, oy = hero.pos / dx;
		int ex = target.pos % dx, ey = target.pos / dx;
		int stepx = Integer.signum( ex - ox );
		int stepy = Integer.signum( ey - oy );
		int beyond = Dungeon.level.map.length;
		if (Math.abs( ex - ox ) >= Math.abs( ey - oy )) {
			stepy = 0;
		} else {
			stepx = 0;
		}
		int cx = ex + stepx, cy = ey + stepy;
		if (cx >= 0 && cx < dx && cy >= 0 && cy < Dungeon.level.height()) {
			int cell = cy * dx + cx;
			if (Dungeon.level.passable[cell] && Actor.findChar( cell ) == null) {
				beyond = cell;
			}
		}

		// 冲刺斩击特效（沿冲刺方向）
		float ang = (float) Math.toDegrees( Math.atan2( ey - oy, ex - ox ) );
		com.shatteredpixel.shatteredpixeldungeon.effects.DashSlashEffect.showOn( target, ang );
		com.shatteredpixel.shatteredpixeldungeon.effects.DashSlashEffect.showOn( hero, ang );

		// 突袭伤害：通常为 125%；双重打击 +3 时改为造成两次 75%
		com.shatteredpixel.shatteredpixeldungeon.items.Item wp = hero.belongings.attackingWeapon();
		if (hero.pointsInTalent( Talent.DOUBLE_STRIKE ) >= 3) {
			int d = Math.round( hero.damageRoll() * 0.75f );
			target.damage( new DamageInfo( d, DamageType.PHYSICAL, hero, wp, hero ) );
			if (target.isAlive()) {
				target.damage( new DamageInfo( d, DamageType.PHYSICAL, hero, wp, hero ) );
			}
		} else {
			target.damage( new DamageInfo( Math.round( hero.damageRoll() * 1.25f ), DamageType.PHYSICAL, hero, wp, hero ) );
		}

		if (beyond < Dungeon.level.map.length) {
			hero.pos = beyond;
			hero.sprite.place( beyond );
			hero.sprite.visible = true;
			Dungeon.level.pressCell( beyond );
			Dungeon.observe();
		}
	}

	// 连击闪避计数
	public static class ShadowDodgeCounter extends Buff {

		{
			type = buffType.POSITIVE;
			announced = false;
		}

		public int count = 0;

		public void reset() {
			count = 0;
		}

		@Override
		public String icon() {
			return BuffIndicator.NONE;
		}

		private static final String COUNT = "count";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( COUNT, count );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			count = bundle.getInt( COUNT );
		}
	}

	// 由闪避触发的隐身；结束时冲刺
	public static class ShadowVeil extends Invisibility {

		@Override
		public void detach() {
			super.detach();
			if (!rushing && target instanceof Hero && target.isAlive()) {
				onVeilEnd( (Hero) target );
			}
		}
	}
}
