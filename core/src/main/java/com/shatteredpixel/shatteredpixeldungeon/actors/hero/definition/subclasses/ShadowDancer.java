package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Bundle;

import java.util.HashSet;

/**
 * 影舞者：盗贼的第三转职。
 *
 * 连续闪避敌人 3 次攻击后进入 3 回合隐身。隐身时主动点击暗影斗篷解除斗篷隐身，可以选方向发动破隐突袭：
 * 冲刺至多 5 格，途中最多穿过 1 名敌人并造成 125% 武器伤害。突袭只有这一个入口。
 * 武器与投掷武器的攻击会打出第二段伤害（双重打击），并能强化斗篷（阴影之赐）。
 */
public class ShadowDancer extends HeroSubClass {

	public ShadowDancer() {
		super("SHADOW_DANCER", HeroIcon.SHADOW_DANCER);
	}

	/** 闪避敌人攻击时累计连击（由攻击未命中事件驱动）。 */
	@SubscribeEvent(event = AttackMissEvent.class, priority = 100)
	public static void onAttackMiss(AttackMissEvent event) {
		Char defender = event.getDefender();
		if (defender instanceof Hero) {
			onDodged((Hero) defender);
		}
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
		if (hero.subClass != HeroSubClasses.SHADOW_DANCER) {
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
		// 「斗篷之下」＝斗篷隐身生效期间，效果不应常驻
		if (hero.buff( com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows.cloakStealth.class ) == null) {
			return 1f;
		}
		float per = pts >= 3 ? 0.23f : 0.15f;
		return 1f + cloak.level() * per;
	}

	/** 闪避一次敌人攻击时由 Char 调用。 */
	public static void onDodged( Hero hero ) {
		if (hero == null || hero.subClass != HeroSubClasses.SHADOW_DANCER) {
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

	/** 破隐突袭的唯一入口：隐身时主动点击暗影斗篷、解除斗篷隐身之后由斗篷调用（渲染线程）。 */
	public static void onCloakBroken( Hero hero ) {
		if (hero == null || hero.subClass != HeroSubClasses.SHADOW_DANCER || !hero.isAlive() || hero.rooted
				|| Dungeon.level == null || Dungeon.level.heroFOV == null) {
			return;
		}
		GameScene.selectCell( new CellSelector.Listener() {
			@Override
			public void onSelect( Integer target ) {
				Hero h = Dungeon.hero;
				if (target == null || h == null || h.rooted || !h.isAlive()) {
					return;
				}
				Invisibility.dispel( h );
				dashTo( h, target );
				h.spendAndNext( 1f );
			}

			@Override
			public String prompt() {
				return Messages.get( HeroSubClass.class, "shadow_dancer_rush_prompt" );
			}
		} );
	}

	/**
	 * 突袭：朝目标点方向冲刺至多 5 格，途中最多穿过 1 名敌人并造成 125% 武器伤害。
	 * 影子闪袭：穿过敌人后沿同一方向再突袭一次，每次少 1 格，+1 至多 2 次、+2/+3 至多 3 次；+3 可以再穿过同一名敌人。
	 */
	public static void dashTo( Hero hero, int dest ) {
		if (hero == null || hero.rooted || Dungeon.level == null || Dungeon.level.heroFOV == null || !hero.isAlive()) {
			return;
		}
		int w = Dungeon.level.width();
		int stepx = Integer.signum( dest % w - hero.pos % w );
		int stepy = Integer.signum( dest / w - hero.pos / w );
		if (stepx == 0 && stepy == 0) {
			return;
		}
		float ang = (float) Math.toDegrees( Math.atan2( dest / w - hero.pos / w, dest % w - hero.pos % w ) );

		int pts = hero.pointsInTalent( Talent.SHADOW_RUSH );
		int maxRushes = pts >= 2 ? 3 : pts >= 1 ? 2 : 1;
		boolean allowSame = pts >= 3;
		HashSet<Char> passed = new HashSet<>();
		int dist = 5;
		for (int r = 0; r < maxRushes && dist > 0; r++) {
			Char hit = dashOnce( hero, stepx, stepy, dist, r == 0 ? dest : -1, ang, passed, allowSame );
			if (hit == null || !hero.isAlive()) {
				break;
			}
			passed.add( hit );
			dist--;
		}
	}

	/** 一次冲刺，返回穿过的敌人（没有穿过就是 null）。 */
	private static Char dashOnce( Hero hero, int stepx, int stepy, int maxDist, int dest, float ang,
								  HashSet<Char> passed, boolean allowSame ) {
		int w = Dungeon.level.width();
		int pos = hero.pos;
		// lastFree：冲刺途中最后一个没有别的角色站着的格子，冲刺只能停在这样的格子上
		int lastFree = hero.pos;
		boolean reachedDest = false;
		Char hitEnemy = null;
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
				if (ch.alignment == Char.Alignment.ENEMY && hitEnemy == null && (allowSame || !passed.contains( ch ))) {
					// 突袭伤害通常为 125%；双重打击 +3 时改为结算两次、每次 75%
					Item wp = hero.belongings.attackingWeapon();
					if (hero.pointsInTalent( Talent.DOUBLE_STRIKE ) >= 3) {
						int dmg = Math.round( hero.damageRoll() * 0.75f );
						ch.damage( new DamageInfo( dmg, DamageType.PHYSICAL, hero, wp, hero ) );
						if (ch.isAlive()) {
							ch.damage( new DamageInfo( dmg, DamageType.PHYSICAL, hero, wp, hero ) );
						}
					} else {
						int dmg = Math.round( hero.damageRoll() * 1.25f );
						ch.damage( new DamageInfo( dmg, DamageType.PHYSICAL, hero, wp, hero ) );
					}
					hitEnemy = ch;
				} else {
					break;
				}
			}
			com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( next ).burst(
					com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory(
							com.shatteredpixel.shatteredpixeldungeon.effects.Speck.STEAM ), 3 );
			pos = next;
			// 被穿过的敌人可能已经被这一击打死，所以重新查这一格现在还有没有人
			boolean occupied = Actor.findChar( pos ) != null;
			if (!occupied) {
				lastFree = pos;
			}
			if (pos == dest) {
				reachedDest = true;
			}
			// 到了所选的格子就停；但选的就是敌人所在的格子时不能停在它身上，继续穿过去，停在它身后第一个空格
			if (reachedDest && !occupied) {
				break;
			}
		}
		// 穿过敌人之后没有落脚点（墙、另一个角色、冲刺距离用完）时退回 lastFree，绝不能和别的角色站在同一格
		pos = lastFree;

		com.shatteredpixel.shatteredpixeldungeon.effects.DashSlashEffect.showOn( hero, ang );
		if (pos != hero.pos) {
			// 落位走角色自己的移动入口：离开门格时关门、触发关卡的落位处理（踩陷阱、踩草、首领层封门等），漂浮时不踩格子
			hero.move( pos, false );
			hero.sprite.place( hero.pos );
			hero.sprite.visible = true;
			Dungeon.observe();
		}
		return hitEnemy;
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

	// 由闪避触发的隐身
	public static class ShadowVeil extends Invisibility {
	}
}
