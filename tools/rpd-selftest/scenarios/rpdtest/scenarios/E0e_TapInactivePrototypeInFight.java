package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.levels.WardenBossLevel;
import com.watabou.utils.PathFinder;

import rpdtest.H;
import rpdtest.Scenario;

import java.util.List;

/**
 * E0d 的实战版：不在测试场，而是在真实的典狱长战里，点一下身边的一台未激活原型机。
 *
 * 流程：进典狱长关卡 → 走进战斗区开战（11 台未激活原型机出场）→ 把其中一台未激活的 DM-100
 * 直接挪到英雄身边的空格上（世界操作；真实游戏里就是英雄走到某台原型机旁边）→ 点它。
 * 挪完立刻点，中间没有任何角色行动。
 *
 * 期望：不崩溃；点完之后游戏还能继续，原型机还在场上，精灵画在它所在的格子上，没有两个角色叠在同一格。
 * （点一下之后是互换位置、原地不动还是别的反应，属于设计选择，这里不判断。）
 */
public class E0e_TapInactivePrototypeInFight implements Scenario {

	@Override
	public String describe() {
		return "典狱长战里点击身边一台未激活的原型机（互动 / 互换位置），不应崩溃";
	}

	@Override
	public void run( H h ) {
		WardenLab.enter( h );
		h.setHeroHP( 2000, 2000 );
		WardenLab.startFight( h );

		final int heroCell = h.heroPos();
		final int free = h.get( () -> {
			WardenBossLevel level = (WardenBossLevel) Dungeon.level;
			for (int d : PathFinder.NEIGHBOURS8) {
				int c = heroCell + d;
				if (c >= 0 && c < level.length() && level.passable[c] && level.heroInBossArena( c ) && Actor.findChar( c ) == null) {
					return c;
				}
			}
			return -1;
		} );
		h.require( free != -1, "英雄身边（战斗区里）有一个可通行的空格" );

		final WardenPrototype proto = h.get( () -> {
			List<WardenPrototype> inactive = WardenLab.inactivePrototypes();
			for (WardenPrototype p : inactive) {
				if (p.model == 100) return p;
			}
			return inactive.isEmpty() ? null : inactive.get( 0 );
		} );
		h.require( proto != null, "开战后场上有未激活的原型机" );
		final int from = h.get( () -> proto.pos );
		h.world( "move one inactive prototype next to the hero", () -> WardenLab.move( proto, free ) );
		h.require( h.get( () -> proto.isInactive() && proto.pos == free ), "一台未激活的原型机站在英雄身边" );
		h.require( h.get( () -> proto.heroShouldInteract() ), "点击未激活的原型机走的是「互动」而不是「攻击」（heroShouldInteract）" );
		h.note( "hero at " + h.cellStr( heroCell ) + "; moved " + h.get( () -> WardenLab.describe( proto ) ) + " from " + h.cellStr( from ) + " to the hero's side" );

		boolean changed = h.tryTap( free );
		h.waitUntil( () -> proto.sprite == null || !proto.sprite.isMoving, 300 );
		h.frames( 30 );
		h.note( "tapped it: anything happened=" + changed
				+ "; hero " + h.cellStr( heroCell ) + " -> " + h.cellStr( h.heroPos() )
				+ "; prototype " + h.cellStr( free ) + " -> " + h.cellStr( h.get( () -> proto.pos ) )
				+ "; windows=" + h.windowNames() + "; " + h.get( () -> WardenLab.describe( proto ) ) );
		h.closeWindows();

		h.check( h.rest( 1 ) == H.Status.READY, "点击身边的未激活原型机之后游戏还能继续（又过了 1 个回合）" );
		h.check( h.get( () -> proto.isAlive() && Dungeon.level.mobs.contains( proto ) && proto.sprite != null && proto.sprite.parent != null ),
				"被点击的原型机还在场上（活着、在关卡的怪物列表里、精灵挂在场景里）" );
		h.waitUntil( () -> proto.sprite == null || !proto.sprite.isMoving, 300 );
		h.check( h.get( () -> H.spriteOnCell( proto.sprite, proto.pos ) ),
				"原型机的精灵画在它所在的格子上（原型机在 " + h.cellStr( h.get( () -> proto.pos ) ) + "，精灵在 " + h.cellStr( h.get( () -> H.spriteCell( proto.sprite ) ) ) + "）" );
		String stacked = h.get( WardenLab::stackedChars );
		h.check( stacked == null, "没有两个角色站在同一格上" + (stacked == null ? "" : "（实际重叠：" + stacked + "）") );
	}
}
