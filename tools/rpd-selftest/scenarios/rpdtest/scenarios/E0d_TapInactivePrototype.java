package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * (e) 的旁证：什么伤害都不打，只是「点一下」相邻的未激活 DM-100 原型机。
 *
 * 未激活的原型机是中立阵营（Alignment.NEUTRAL），Mob.heroShouldInteract() 为 true，
 * 所以点击它走的是「互动」而不是「攻击」；Char.interact 的默认实现是和英雄互换位置，
 * 互换位置要播放原型机精灵的 run 动画。
 * 这个场景用来确认：未激活原型机的精灵除了 die 动画之外，run 动画是否也会出事。
 *
 * 期望：不崩溃；点完之后游戏还能继续，原型机还在场上，没有两个角色叠在同一格。
 * （点一下之后是互换位置、原地不动还是别的反应，属于设计选择，这里不判断。）
 */
public class E0d_TapInactivePrototype implements Scenario {

	@Override
	public String describe() {
		return "旁证：点击相邻的未激活 DM-100 原型机（互动 / 互换位置），不应崩溃";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		final int protoCell = Arena.c( 11, 10 );
		WardenPrototype proto = h.spawn( new WardenPrototype( 100 ), protoCell );
		h.require( h.get( proto::isInactive ), "原型机处于未激活状态" );
		h.require( h.get( () -> proto.heroShouldInteract() ), "点击未激活的原型机走的是「互动」而不是「攻击」（heroShouldInteract）" );
		final int heroCell = h.heroPos();

		boolean changed = h.tryTap( protoCell );
		h.waitUntil( () -> proto.sprite == null || !proto.sprite.isMoving, 300 );
		h.frames( 30 );
		h.note( "tapped the inactive prototype at " + h.cellStr( protoCell ) + ": anything happened=" + changed
				+ "; hero " + h.cellStr( heroCell ) + " -> " + h.cellStr( h.heroPos() )
				+ "; prototype " + h.cellStr( protoCell ) + " -> " + h.cellStr( h.get( () -> proto.pos ) )
				+ "; windows=" + h.windowNames() + "; " + h.get( () -> WardenLab.describe( proto ) ) );
		h.closeWindows();

		h.check( h.rest( 1 ) == H.Status.READY, "点击相邻的未激活原型机之后游戏还能继续（又过了 1 个回合）" );
		h.check( h.get( () -> proto.isAlive() && Dungeon.level.mobs.contains( proto ) && proto.sprite != null && proto.sprite.parent != null ),
				"被点击的未激活原型机还在场上（活着、在关卡的怪物列表里、精灵挂在场景里）" );
		h.check( h.get( () -> H.spriteOnCell( proto.sprite, proto.pos ) ),
				"原型机的精灵画在它所在的格子上（原型机在 " + h.cellStr( h.get( () -> proto.pos ) ) + "，精灵在 " + h.cellStr( h.get( () -> H.spriteCell( proto.sprite ) ) ) + "）" );
		String stacked = h.get( WardenLab::stackedChars );
		h.check( stacked == null, "没有两个角色站在同一格上" + (stacked == null ? "" : "（实际重叠：" + stacked + "）") );
	}
}
