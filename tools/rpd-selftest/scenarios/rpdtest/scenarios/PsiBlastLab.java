package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import rpdtest.H;
import rpdtest.HarnessError;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户报告 (e)：「与典狱长战斗，第二次读灵爆后崩溃，场上存在 2 个敌对 dm175，以及 1 个敌对的 dm100 和一个友方 dm175」。
 *
 * 公共流程 run()（各 E 场景只是参数不同）：
 *  1. 进典狱长关卡，真的走进战斗区开战（封层，11 台原型机出场）；
 *  2. 按报告摆场：2 台 DM-175 + 1 台 DM-100 由典狱长激活（敌对），1 台 DM-175 由玩家激活（友方），都挪到英雄身边；
 *  3. 过 1 回合（让 DM-175 拿到护盾），读第 1 张灵爆秘卷；
 *  4. 解除失明（直接解除，或原地等到失明结束），读第 2 张。
 * 任何未捕获异常由工具判为 CRASH 并给出完整栈。
 *
 * 断言只写「不管怎么修都应该成立」的事：不崩溃、读完后游戏状态正常、被炸死的怪干净地消失。
 * 未激活的原型机挨炸后是「被炸死」还是「免疫」属于设计选择，两种都接受，只记录实际发生了哪一种。
 */
final class PsiBlastLab {

	private PsiBlastLab() {}

	static final class Opt {
		/** 摆场时把剩下的未激活原型机全部拿走（场上只有报告里那 4 台 + 典狱长）。 */
		boolean removeInactive = true;
		/** DM-175 带护盾（false = 每次读之前把它们的护盾拿掉）。 */
		boolean keepShield = true;
		/** 典狱长在英雄视野内（false = 摆场时把它挪到视野外的角落）。 */
		boolean wardenInView = true;
		/** > 0：第二次读之前把典狱长的生命设成这个值（40 以下第二次会被炸死）。 */
		int wardenHpBeforeSecond = -1;
		/** 第二次读之前把英雄生命设成 1（第二次读的自伤会杀死英雄）。 */
		boolean heroDiesOnSecond = false;
		/** 两次读之间：true = 原地等到失明自然结束；false = 直接解除失明，紧接着读。 */
		boolean waitOutBlindness = false;

		/** 两次灵爆炸到过的所有怪（收尾检查用）。 */
		final List<Target> seen = new ArrayList<>();
	}

	static final class Target {
		Mob mob;
		String desc;
		/** 读之前是未激活的原型机。 */
		boolean inactive;
		/** 读之前没有护盾、不是 Boss、不是未激活原型机：按灵爆的规则必死。 */
		boolean mustDie;
	}

	/** 标准流程。 */
	static void run( H h, Opt o ) {
		ScrollOfPsionicBlast scroll = prepare( h );
		WardenLab.startFight( h );
		arrange( h, o );

		H.Status st = read( h, scroll, 1, o );
		if (st == H.Status.READY) {
			if (o.waitOutBlindness) {
				int waited = 0;
				while (waited < 40 && h.heroAlive() && h.get( () -> Dungeon.hero.buff( Blindness.class ) != null )) {
					h.rest( 1 );
					waited++;
				}
				h.note( "waited " + waited + " turn(s) for the blindness from the first blast to wear off" );
				h.require( h.heroAlive(), "等失明结束的过程中英雄没有死" );
			}
			read( h, scroll, 2, o );
		}
		finish( h, o );
	}

	/** 进典狱长关卡，背包里放 2 张灵爆秘卷，英雄 1000 血。英雄站在入口。 */
	static ScrollOfPsionicBlast prepare( H h ) {
		WardenLab.enter( h );
		final ScrollOfPsionicBlast scroll = h.give( (ScrollOfPsionicBlast) new ScrollOfPsionicBlast().quantity( 2 ) );
		h.require( h.get( scroll::quantity ) == 2, "背包里有 2 张灵爆秘卷" );
		h.setHeroHP( 1000, 1000 );
		return scroll;
	}

	/** 按用户报告摆场（要求已经开战、英雄站在 (6,11)），然后过 1 回合让 DM-175 拿到护盾。 */
	static void arrange( H h, Opt o ) {
		h.world( "arrange the reported field: 2 hostile DM-175 + 1 hostile DM-100 + 1 friendly DM-175", () -> {
			List<WardenPrototype> free175 = new ArrayList<>();
			List<WardenPrototype> free100 = new ArrayList<>();
			for (WardenPrototype p : WardenLab.inactivePrototypes()) {
				(p.model == 175 ? free175 : free100).add( p );
			}
			if (free175.size() < 3 || free100.size() < 1) {
				throw new HarnessError( "not enough inactive prototypes to arrange the field: DM-175 x" + free175.size() + ", DM-100 x" + free100.size() );
			}
			WardenPrototype[] cast = { free175.get( 0 ), free175.get( 1 ), free100.get( 0 ), free175.get( 2 ) };
			cast[0].activateByWarden();
			cast[1].activateByWarden();
			cast[2].activateByWarden();
			cast[3].activateByHero();

			int[] spots = {
					WardenLab.c( 4, 10 ), WardenLab.c( 8, 10 ), WardenLab.c( 4, 11 ), WardenLab.c( 8, 11 ), WardenLab.c( 5, 11 ),
					WardenLab.c( 7, 11 ), WardenLab.c( 3, 10 ), WardenLab.c( 9, 10 ), WardenLab.c( 3, 11 ), WardenLab.c( 9, 11 ) };
			int si = 0;
			for (WardenPrototype p : cast) {
				while (si < spots.length && Actor.findChar( spots[si] ) != null) si++;
				if (si >= spots.length) throw new HarnessError( "no free cell near the hero to place a prototype" );
				WardenLab.move( p, spots[si++] );
			}

			if (o.removeInactive) {
				for (WardenPrototype p : WardenLab.inactivePrototypes()) WardenLab.remove( p );
			}

			if (!o.wardenInView) {
				Warden w = WardenLab.warden();
				int[] far = { WardenLab.c( 1, 1 ), WardenLab.c( 11, 1 ), WardenLab.c( 2, 1 ), WardenLab.c( 10, 1 ) };
				boolean moved = false;
				for (int cell : far) {
					if (Actor.findChar( cell ) == null && Dungeon.level.passable[cell]) {
						WardenLab.move( w, cell );
						moved = true;
						break;
					}
				}
				if (!moved) throw new HarnessError( "no free far corner cell for the Warden" );
			}
		} );

		// 过 1 回合：DM-175 在自己的回合里拿到护盾（基线代码的护盾是在 act() 里给的）
		h.rest( 1 );
		h.require( h.heroAlive(), "摆场后英雄还活着" );
		if (!o.wardenInView) {
			h.require( !h.get( () -> Dungeon.level.heroFOV[WardenLab.warden().pos] ), "典狱长不在英雄视野内" );
		} else {
			h.require( h.get( () -> Dungeon.level.heroFOV[WardenLab.warden().pos] ), "典狱长在英雄视野内" );
		}
	}

	/** 读第 n 张灵爆秘卷（n = 1 或 2），并做这一次的断言。返回读完后的状态。 */
	static H.Status read( H h, ScrollOfPsionicBlast scroll, int n, Opt o ) {
		h.world( "prepare read #" + n, () -> {
			Hero hero = Dungeon.hero;
			Buff.detach( hero, Blindness.class );
			if (n == 2) {
				hero.HP = o.heroDiesOnSecond ? 1 : hero.HT;
				if (o.wardenHpBeforeSecond > 0 && WardenLab.warden() != null) {
					WardenLab.warden().HP = o.wardenHpBeforeSecond;
				}
			}
			if (!o.keepShield) {
				for (WardenPrototype p : WardenLab.prototypes( 175 )) Buff.detach( p, Barrier.class );
			}
		} );

		// 场上有未激活的原型机、但碰巧一台都不在英雄视野里（补生成的落点是随机的，视野只有 8 格）：
		// 挪一台到视野内，否则这一次灵爆炸不到任何未激活的机体，场景会「碰巧通过」而什么都没测到。
		final String moved = h.get( () -> {
			List<WardenPrototype> inactive = WardenLab.inactivePrototypes();
			if (inactive.isEmpty()) return "";
			for (WardenPrototype p : inactive) {
				if (Dungeon.level.heroFOV[p.pos]) return "";
			}
			return "needed";
		} );
		if (!moved.isEmpty()) {
			final String[] what = { null };
			h.world( "no inactive prototype is in view: bring one into the hero's view", () -> {
				WardenPrototype p = WardenLab.inactivePrototypes().get( 0 );
				int from = p.pos;
				//原型机正常只会出现在门前三行以外（y <= 8）；从离英雄近的一行往远处找一个看得见的空格
				int[] xs = { 6, 5, 7, 4, 8, 3, 9, 2, 10 };
				for (int y = 8; y >= 4; y--) {
					for (int x : xs) {
						int cell = WardenLab.c( x, y );
						if (Dungeon.level.heroFOV[cell] && Dungeon.level.passable[cell] && Actor.findChar( cell ) == null) {
							WardenLab.move( p, cell );
							what[0] = "DM-" + p.model + " inactive " + WardenLab.xy( from ) + " -> " + WardenLab.xy( cell );
							return;
						}
					}
				}
				throw new HarnessError( "no visible free cell to bring an inactive prototype into view" );
			} );
			h.note( "before read #" + n + ": inactive prototypes existed but none was in the hero's view (random spawn cells); moved one into view: " + what[0] );
		}

		// 读之前：视野内的怪（= 灵爆的目标）
		final List<Target> targets = h.get( () -> {
			List<Target> out = new ArrayList<>();
			for (Mob m : Dungeon.level.mobs) {
				if (!Dungeon.level.heroFOV[m.pos]) continue;
				Target t = new Target();
				t.mob = m;
				t.desc = WardenLab.describe( m );
				t.inactive = m instanceof WardenPrototype && ((WardenPrototype) m).isInactive();
				t.mustDie = !t.inactive && m.shielding() == 0 && !m.properties().contains( Char.Property.BOSS );
				out.add( t );
			}
			return out;
		} );
		o.seen.addAll( targets );
		int inactiveInView = 0;
		for (Target t : targets) {
			if (t.inactive) inactiveInView++;
		}
		h.note( "before read #" + n + ": " + targets.size() + " mob(s) in the hero's view, " + inactiveInView + " of them INACTIVE prototype(s); "
				+ h.get( WardenLab::field ) );

		H.Status st = h.read( scroll );
		h.frames( 60 );
		h.note( "after read #" + n + ": status=" + st + "; " + h.get( WardenLab::field ) );

		final String[] r = h.get( () -> {
			StringBuilder survivors = new StringBuilder();
			StringBuilder halfDead = new StringBuilder();
			int inactiveDied = 0, inactiveSurvived = 0;
			for (Target t : targets) {
				boolean alive = t.mob.isAlive();
				boolean inLevel = Dungeon.level.mobs.contains( t.mob );
				if (alive != inLevel) {
					halfDead.append( " [" ).append( t.desc ).append( " -> alive=" ).append( alive ).append( " inLevelMobs=" ).append( inLevel ).append( "]" );
				}
				if (t.mustDie && (alive || inLevel)) survivors.append( " [" ).append( t.desc ).append( "]" );
				if (t.inactive) {
					if (alive) inactiveSurvived++;
					else inactiveDied++;
				}
			}
			return new String[]{ survivors.toString(), halfDead.toString(), String.valueOf( inactiveDied ), String.valueOf( inactiveSurvived ) };
		} );
		if (inactiveInView > 0) {
			h.note( "read #" + n + ": of the " + inactiveInView + " inactive prototype(s) in view, " + r[2] + " died and " + r[3] + " survived the blast" );
		}
		h.check( r[0].isEmpty(), "第 " + n + " 次灵爆：视野内已激活、无护盾的非 Boss 怪全部被炸死" + (r[0].isEmpty() ? "" : "（没死的：" + r[0] + "）") );
		h.check( r[1].isEmpty(), "第 " + n + " 次灵爆后没有「半死不活」的怪（死了就已从关卡移除，活着就还在关卡里）" + (r[1].isEmpty() ? "" : "（实际：" + r[1] + "）") );

		boolean expectDead = n == 2 && o.heroDiesOnSecond;
		if (expectDead) {
			h.check( st == H.Status.DEAD && !h.heroAlive(), "第 2 次灵爆的自伤杀死了英雄（实际 status=" + st + "）" );
		} else {
			h.check( st == H.Status.READY && h.heroAlive(), "第 " + n + " 次读灵爆后英雄存活，游戏在等下一步输入（实际 status=" + st + "）" );
		}
		return st;
	}

	/** 收尾检查。 */
	static void finish( H h, Opt o ) {
		String stacked = h.get( WardenLab::stackedChars );
		h.check( stacked == null, "没有两个角色站在同一格上" + (stacked == null ? "" : "（实际重叠：" + stacked + "）") );
		h.check( h.get( () -> {
			for (Mob m : Dungeon.level.mobs) {
				if (!m.isAlive()) return false;
			}
			return true;
		} ), "关卡的怪物列表里没有残留的死怪" );

		if (o.wardenHpBeforeSecond > 0 && o.wardenHpBeforeSecond <= 60 && o.wardenInView) {
			h.check( h.get( () -> WardenLab.warden() == null ), "典狱长在第 2 次灵爆中被炸死" );
			h.check( h.get( () -> WardenLab.prototypes().isEmpty() ), "典狱长死后原型机全部清场" );
			h.check( !h.get( () -> Dungeon.level.locked ), "典狱长死后关卡解除封锁" );
			h.check( h.get( () -> Dungeon.level.map[WardenLab.c( 6, 2 )] ) == Terrain.EXIT, "典狱长死后出口 (6,2) 打开" );
		}

		if (h.heroAlive()) {
			h.world( "cure blindness so the hero can go on", () -> Buff.detach( Dungeon.hero, Blindness.class ) );
			H.Status st = h.rest( 2 );
			h.check( st == H.Status.READY, "两次灵爆之后游戏还能继续（又过了 2 个回合）" );
			h.note( "2 turns later: " + h.get( WardenLab::field ) );
		} else {
			h.note( "hero is dead; windows showing: " + h.windowNames() );
		}

		// 死亡动画（约 1 秒）+ 淡出（3 秒）都按游戏时间走：最多等 1500 帧（1 倍速下也够）
		h.waitUntil( () -> ghosts( o ).isEmpty(), 1500 );
		String ghosts = h.get( () -> ghosts( o ) );
		h.check( ghosts.isEmpty(), "被炸死的怪的精灵都已经从地图上消失（没有留下不会消失的尸体图）" + (ghosts.isEmpty() ? "" : "（还留着的：" + ghosts + "）") );
	}

	/** 已经死了、但精灵还挂在场景里的怪。只能在渲染线程调用。 */
	private static String ghosts( Opt o ) {
		StringBuilder sb = new StringBuilder();
		for (Target t : o.seen) {
			if (!t.mob.isAlive() && t.mob.sprite != null && t.mob.sprite.parent != null && t.mob.sprite.exists) {
				sb.append( " [" ).append( t.desc ).append( "]" );
			}
		}
		return sb.toString();
	}
}
