package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;

import rpdtest.H;

/**
 * 「未激活的原型机挨了致命伤害之后」的公共检查。不是场景，只是辅助类。
 *
 * 两种结果都算对（属于设计选择）：
 *  - 被打死：已从关卡的怪物列表移除，精灵在死亡动画播完后从地图上消失；
 *  - 没死（未激活时免疫伤害）：还在关卡里、精灵还在地图上。
 * 不接受的是中间状态：死了还留在怪物列表里、活着却不在列表里、死了但尸体图永远留在地图上。
 */
final class InactiveDeath {

	private InactiveDeath() {}

	static void checkOutcome( H h, WardenPrototype proto, String byWhat ) {
		boolean alive = h.get( proto::isAlive );
		boolean inLevel = h.get( () -> Dungeon.level.mobs.contains( proto ) );
		if (!alive) {
			h.note( "the inactive prototype was killed by " + byWhat );
			h.check( !inLevel, "被" + byWhat + "打死的未激活原型机已从关卡的怪物列表移除" );
			// 死亡动画 + 淡出按游戏时间走：最多等 1500 帧（1 倍速下也够）
			boolean gone = h.waitUntil( () -> proto.sprite == null || proto.sprite.parent == null || !proto.sprite.exists, 1500 );
			h.check( gone, "被打死的未激活原型机的精灵已经从地图上消失（没有留下不会消失的尸体图）" );
		} else {
			h.note( "the inactive prototype SURVIVED " + byWhat + " (HP " + h.get( () -> proto.HP ) + "/" + h.get( () -> proto.HT )
					+ ", now " + h.get( () -> WardenLab.describe( proto ) ) + "): in this build it apparently cannot be killed while inactive" );
			h.check( inLevel, "没被打死的未激活原型机还在关卡的怪物列表里" );
			h.check( h.get( () -> proto.sprite != null && proto.sprite.parent != null && proto.sprite.exists ), "没被打死的未激活原型机的精灵还在地图上" );
		}
	}
}
