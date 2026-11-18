package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.levels.WardenBossLevel;

import rpdtest.H;
import rpdtest.HarnessError;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 典狱长关卡（WardenBossLevel，13 宽 x 18 高）上的常用搭建动作。不是场景，只是辅助类。
 *
 * 地形：战斗区 x1..11, y1..11；门 (6,12)；入口房 x4..8, y13..16；入口 (6,16)。
 * 基线代码里典狱长在关卡生成时就站在 (6,10)。
 *
 * 标了「只能在渲染线程」的方法要放在 h.get(...) / h.world(...) 里调用。
 */
final class WardenLab {

	private WardenLab() {}

	static final int W = 13;

	static int c( int x, int y ) {
		return x + y * W;
	}

	static final int ENTRANCE = c( 6, 16 );
	/** 门外一格（入口房里，紧贴着门）。 */
	static final int OUTSIDE = c( 6, 13 );
	static final int DOOR = c( 6, 12 );
	/** 进门后的第一格（战斗区里）。 */
	static final int INSIDE = c( 6, 11 );
	/** 基线代码里典狱长的初始位置。 */
	static final int BOSS_HOME = c( 6, 10 );

	/** 开新局并直接进第 10 层的典狱长关卡，英雄在入口 (6,16)。 */
	static void enter( H h ) {
		h.newGameInArena( HeroClasses.WARRIOR, Arena.SEED );
		h.gotoLevel( 10, WardenBossLevel.class );
		h.require( h.get( () -> Dungeon.level instanceof WardenBossLevel ), "第 10 层是典狱长关卡（WardenBossLevel）" );
		h.require( h.get( () -> Dungeon.level.width() ) == W, "典狱长关卡宽 13 格" );
		h.require( h.heroPos() == ENTRANCE, "英雄落在入口 (6,16)" );
	}

	/** 走进战斗区 (6,11)，并给 Boss 最多 3 个回合的反应时间（直到封层且原型机到场）。不做断言。 */
	static void walkIn( H h ) {
		h.walkTo( OUTSIDE );
		h.walkTo( DOOR );
		h.walkTo( INSIDE );
		for (int i = 0; i < 3 && !h.get( () -> Dungeon.level.locked && prototypes().size() >= 11 ); i++) {
			h.rest( 1 );
		}
	}

	/** 走进战斗区并确认 Boss 战已经开始（封层、典狱长与 11 台原型机在场）；否则以 ERROR 结束。 */
	static void startFight( H h ) {
		walkIn( h );
		h.require( h.get( () -> Dungeon.level.locked ), "英雄进入战斗区后关卡已封锁" );
		h.require( h.get( () -> warden() != null && warden().isAlive() ), "典狱长在场" );
		h.require( h.get( () -> prototypes().size() ) == 11, "11 台原型机在场" );
	}

	// ------------------------------------------------------------ 只能在渲染线程调用

	static Warden warden() {
		for (Mob m : Dungeon.level.mobs) {
			if (m instanceof Warden) return (Warden) m;
		}
		return null;
	}

	/** 本层所有原型机，按格子编号排序（顺序稳定）。 */
	static List<WardenPrototype> prototypes() {
		List<WardenPrototype> out = new ArrayList<>();
		for (Mob m : Dungeon.level.mobs) {
			if (m instanceof WardenPrototype) out.add( (WardenPrototype) m );
		}
		out.sort( Comparator.comparingInt( (WardenPrototype p) -> p.pos ) );
		return out;
	}

	static List<WardenPrototype> prototypes( int model ) {
		List<WardenPrototype> out = new ArrayList<>();
		for (WardenPrototype p : prototypes()) {
			if (p.model == model) out.add( p );
		}
		return out;
	}

	static List<WardenPrototype> inactivePrototypes() {
		List<WardenPrototype> out = new ArrayList<>();
		for (WardenPrototype p : prototypes()) {
			if (p.isInactive()) out.add( p );
		}
		return out;
	}

	/** 直接把一只怪挪到某格（世界操作）。 */
	static void move( Mob m, int cell ) {
		Char there = Actor.findChar( cell );
		if (there != null && there != m) {
			throw new HarnessError( "cannot move " + m.getClass().getSimpleName() + " to " + cell + ": occupied by " + there.getClass().getSimpleName() );
		}
		m.pos = cell;
		if (m.sprite != null) {
			m.sprite.interruptMotion();
			m.sprite.place( cell );
			m.sprite.visible = Dungeon.level.heroFOV[cell];
		}
	}

	/** 把一只怪从场上直接拿掉（不播死亡动画、不算击杀；做法同 WardenBossLevel.onBossDeath 清原型机）。 */
	static void remove( Mob m ) {
		m.destroy();
		if (m.sprite != null) m.sprite.killAndErase();
	}

	/** 格子编号 → "(x,y)"，按当前关卡的宽度（在测试场里也能用）。 */
	static String xy( int cell ) {
		int w = Dungeon.level == null ? W : Dungeon.level.width();
		return "(" + (cell % w) + "," + (cell / w) + ")";
	}

	static String stateName( Mob m ) {
		if (m.state == m.SLEEPING) return "SLEEPING";
		if (m.state == m.HUNTING) return "HUNTING";
		if (m.state == m.WANDERING) return "WANDERING";
		if (m.state == m.FLEEING) return "FLEEING";
		if (m.state == m.PASSIVE) return "PASSIVE";
		return String.valueOf( m.state );
	}

	static String describe( Mob m ) {
		StringBuilder sb = new StringBuilder();
		if (m instanceof WardenPrototype) {
			WardenPrototype p = (WardenPrototype) m;
			sb.append( "DM-" ).append( p.model ).append( p.isInactive() ? " inactive" : p.isWardenAlly() ? " WARDEN-ALLY" : p.isHeroAlly() ? " HERO-ALLY" : " ?" );
		} else {
			sb.append( m.getClass().getSimpleName() );
		}
		sb.append( ' ' ).append( xy( m.pos ) ).append( " HP=" ).append( m.HP ).append( '/' ).append( m.HT );
		if (m.shielding() > 0) sb.append( " shield=" ).append( m.shielding() );
		sb.append( ' ' ).append( m.alignment ).append( ' ' ).append( stateName( m ) );
		sb.append( Dungeon.level.heroFOV[m.pos] ? " inFOV" : " notInFOV" );
		if (!m.isAlive()) sb.append( " DEAD" );
		return sb.toString();
	}

	/** 场上所有怪的一览（多行）。 */
	static String field() {
		List<Mob> mobs = new ArrayList<>( Dungeon.level.mobs );
		mobs.sort( Comparator.comparingInt( (Mob m) -> m.pos ) );
		StringBuilder sb = new StringBuilder();
		sb.append( "hero " ).append( xy( Dungeon.hero.pos ) ).append( " HP=" ).append( Dungeon.hero.HP ).append( '/' ).append( Dungeon.hero.HT )
				.append( Dungeon.hero.isAlive() ? "" : " DEAD" ).append( "; level.locked=" ).append( Dungeon.level.locked )
				.append( "; mobs=" ).append( mobs.size() );
		for (Mob m : mobs) {
			sb.append( "\n        " ).append( describe( m ) );
		}
		return sb.toString();
	}

	/** 有没有两个角色站在同一格上；有的话返回描述，没有返回 null。 */
	static String stackedChars() {
		Map<Integer, List<Char>> byCell = new HashMap<>();
		for (Char ch : Actor.chars()) {
			byCell.computeIfAbsent( ch.pos, k -> new ArrayList<>() ).add( ch );
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, List<Char>> e : byCell.entrySet()) {
			if (e.getValue().size() > 1) {
				if (sb.length() > 0) sb.append( "; " );
				sb.append( xy( e.getKey() ) ).append( ": " );
				Map<String, Integer> names = new HashMap<>();
				for (Char ch : e.getValue()) names.merge( ch.getClass().getSimpleName(), 1, Integer::sum );
				sb.append( names );
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}
}
