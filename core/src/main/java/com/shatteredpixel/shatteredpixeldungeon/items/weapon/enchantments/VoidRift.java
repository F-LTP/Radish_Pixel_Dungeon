/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RiftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.Objects;

/**
 * 虚空附魔：撕扯空间，攻击后有机会把目标脚下的地块撕成深渊使其坠落。
 * 撕掉会断路的过道上只开一道转瞬即合的裂隙，目标照样坠落，但不留下深渊。
 */
public class VoidRift extends Weapon.Enchantment {

	private static final ItemSprite.Glowing VOID_GLOW = new ItemSprite.Glowing( 0x7A2BE2 );

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		if (damage <= 0 || !defender.isAlive() || defender.isImmune( VoidRift.class )){
			return damage;
		}
		int level = Math.max( 0, weapon.buffedLvl() );
		float chance = Math.min( 0.40f, 0.10f + 0.02f * level ) * procChanceMultiplier( attacker );
		if (Random.Float() < chance){
			VoidRiftTracker tracker = Buff.affect( defender, VoidRiftTracker.class );
			tracker.setup( attacker, weapon, this, level );
		}
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return VOID_GLOW;
	}

	/** 与死神附魔一样延后一拍结算，等这一击的真实伤害落地后再判定地块。 */
	public static class VoidRiftTracker extends Buff {

		{ actPriority = Actor.VFX_PRIO; }

		private Char attacker;
		private Weapon weapon;
		private VoidRift enchant;
		private int level;

		void setup( Char attacker, Weapon weapon, VoidRift enchant, int level ){
			this.attacker = attacker;
			this.weapon = weapon;
			this.enchant = enchant;
			this.level = level;
		}

		@Override
		public boolean act() {
			if (target != null && target.isAlive()) resolve();
			detach();
			return true;
		}

		private void resolve(){
			Level lvl = Dungeon.level;
			if (lvl == null) return;
			int cell = target.pos;
			if (cell < 0 || cell >= lvl.map.length) return;

			boolean boss = Char.hasProp( target, Char.Property.BOSS )
					|| Char.hasProp( target, Char.Property.MINIBOSS );
			// 只有怪物会被撕下去；英雄被怪物手里的虚空武器打中时走下面的伤害兜底
			if (target instanceof Mob && !boss && canRip( lvl, cell )){
				if (Passage.wouldCut( cell, lvl.width(), lvl.height(),
						lvl.map, lvl.passable, lvl.avoid, lvl.pit )){
					// 这一格是过道：地形不动，裂隙转瞬合拢，只把怪带走，不留下断路的坑。
					// 在普通地板上直接让怪坠落并不特殊，原版塌方陷阱（PitfallTrap）就是这么做的。
					CellEmitter.center( cell ).burst( RiftParticle.IMPLODE, 12 );
				} else {
					// 三参 set 会同步刷新该格的 pit 标记，之后踩上去也会正常坠落。
					Level.set( cell, Terrain.CHASM, lvl );
					GameScene.updateMap( cell );
					CellEmitter.get( cell ).burst( RiftParticle.SCATTER, 12 );
				}
				Chasm.mobFall( (Mob) target );
			} else {
				// 地块撕不开时改走高额真实伤害，不另写进游戏说明。
				int extra = Math.max( 1, Math.round( target.HT * (0.45f + 0.02f * level) ) );
				// 伤害来源记成附魔本身，和其它附魔的追加伤害一致：Mob.die 靠来源是不是
				// 武器 / 附魔来认「武器击杀」，记成这个追踪 buff 就不算了。
				// 这几个字段不入档，读档后恢复出来的追踪器拿不到附魔，才退回用自己。
				Object source = enchant != null ? enchant : this;
				target.damage( new DamageInfo( extra, DamageType.TRUE, attacker, weapon, source ) );

				// 追踪器在这次攻击结算完之后才出手，Char.attack 里「英雄被打死」的收尾已经错过了。
				// 延后生效的伤害要像流血、中毒那样自己补上，否则这一局不会提交排行榜，也没有死亡提示。
				if (target == Dungeon.hero && !target.isAlive()){
					Dungeon.fail( attacker != null ? attacker.getClass() : VoidRift.class );
					if (attacker != null){
						GLog.n( Messages.capitalize( Messages.get( Char.class, "kill", attacker.name() ) ) );
					}
				}
			}
		}

		/** 这一格能不能撕成深渊。撕出来的坑是永久地形，所以撕不开的情况都交给 resolve() 的伤害兜底。 */
		private boolean canRip( Level lvl, int cell ){
			// 和原版塌方陷阱（PitfallTrap）同一条规则：首领层、25 层以后、支线层不开坑。
			// 这些楼层要么没有能掉下去的下一层，要么留下的坑能让英雄跳下去绕过首领战。
			if (Dungeon.bossLevel() || Dungeon.depth > 25
					|| !Objects.equals( Dungeon.branchId, Branches.MAIN )){
				return false;
			}
			// 封锁中的楼层不允许离开（传送类道具同样被 locked 拦住），不能在地上留一个出口
			if (lvl.locked) return false;

			if (lvl.solid[cell] || lvl.avoid[cell]) return false;

			int terr = lvl.map[cell];
			if (terr == Terrain.CHASM
					|| terr == Terrain.ENTRANCE || terr == Terrain.ENTRANCE_SP || terr == Terrain.EXIT
					|| terr == Terrain.UNLOCKED_EXIT || terr == Terrain.LOCKED_EXIT
					|| terr == Terrain.DOOR || terr == Terrain.OPEN_DOOR || terr == Terrain.LOCKED_DOOR
					// 专门垫在自定义贴图下面的地板
					|| terr == Terrain.CUSTOM_DECO_EMPTY){
				return false;
			}

			// 楼层出入口不一定是楼梯地形，也可能不止一格，按过渡区判断才不会漏；撕掉就回不去了
			if (lvl.getTransition( cell ) != null) return false;

			// Level.set 只换地形，格子上的物品堆和植物会原样留在坑上，再也够不着
			if (lvl.heaps.get( cell ) != null || lvl.plants.get( cell ) != null) return false;

			// 被自定义贴图盖住的格子换了地形画面也不会变，会留下一个看不见的坑。
			// 判断方式同 Level.setCellToWater。
			int x = cell % lvl.width();
			int y = cell / lvl.width();
			for (CustomTilemap cust : lvl.customTiles){
				int cx = x - cust.tileX;
				int cy = y - cust.tileY;
				if (cx >= 0 && cy >= 0 && cx < cust.tileW && cy < cust.tileH
						&& cust.image( cx, cy ) != null){
					return false;
				}
			}

			return true;
		}
	}

	/**
	 * 判断「把某一格撕成深渊，会不会把路撕断」，只看被撕格周围一圈 8 格。
	 *
	 * 凡是经过被撕格的路，都是从圈上一格进来、再从圈上另一格出去的，所以只要圈上能走的格子
	 * 彼此还连着，原来的路就能沿着这一圈绕过去。这个判定偏严：绕一大圈其实能通的环形走廊
	 * 也会被当成过道，宁可少留一个坑。
	 *
	 * 这里全是不碰游戏全局状态的静态纯函数，参数只有基本类型和数组，可以脱离游戏单独穷举验证。
	 */
	static final class Passage {

		/** 墙、深渊、地图外：既不能走，也没有够不着的问题。 */
		static final int OTHER = 0;
		/** 现在就能正常走过去。只有这一类能充当绕行路线。 */
		static final int PASS = 1;
		/** 不能拿来绕行，但撕完之后必须还够得着：上锁的门、已显形的陷阱之类。 */
		static final int KEEP = 2;

		// 周围一圈，从正北起顺时针
		private static final int[] DX = {  0,  1, 1, 1, 0, -1, -1, -1 };
		private static final int[] DY = { -1, -1, 0, 1, 1,  1,  0, -1 };

		// NEAR[i] 的第 j 位：圈上第 i 格和第 j 格是否挨着。
		// 游戏里是八向移动，斜着走不看拐角两侧是不是墙，门也能斜着进出，所以斜向同样算挨着。
		private static final int[] NEAR = new int[8];
		static {
			for (int i = 0; i < 8; i++){
				for (int j = 0; j < 8; j++){
					if (i != j && Math.abs( DX[i] - DX[j] ) <= 1 && Math.abs( DY[i] - DY[j] ) <= 1){
						NEAR[i] |= 1 << j;
					}
				}
			}
		}

		private Passage(){}

		/** 撕掉 cell 会不会断路，或者把须保留可达的格子孤立起来。地图按行存成一维数组。 */
		static boolean wouldCut( int cell, int width, int height,
				int[] map, boolean[] passable, boolean[] avoid, boolean[] pit ){
			int[] around = ring( cell, width, height );
			int[] kinds = new int[8];
			for (int i = 0; i < 8; i++){
				int n = around[i];
				kinds[i] = n < 0 ? OTHER : classOf( map[n], passable[n], avoid[n], pit[n] );
			}
			return !removable( kinds );
		}

		/**
		 * 周围 8 格的下标，顺序同 DX / DY；进不去的格子记 -1。
		 * 必须按坐标算：直接拿下标加减，贴着左右边界时会绕到相邻那一行去。
		 */
		static int[] ring( int cell, int width, int height ){
			int x = cell % width;
			int y = cell / width;
			int[] result = new int[8];
			for (int i = 0; i < 8; i++){
				int nx = x + DX[i];
				int ny = y + DY[i];
				// 地图最外一圈和地图外同样处理：Level.buildFlagMaps 每次都会把最外一圈封死
				boolean inside = nx > 0 && ny > 0 && nx < width - 1 && ny < height - 1;
				result[i] = inside ? nx + ny * width : -1;
			}
			return result;
		}

		/** 一格属于哪一类。passable / avoid / pit 取关卡当前的标记，它们会被场上的效果改写。 */
		static int classOf( int terrain, boolean passable, boolean avoid, boolean pit ){
			if (passable) return PASS;
			// 已显形的陷阱、水井：寻路会绕开，但英雄可以主动踩上去，不能让它够不着
			if (avoid && !pit) return KEEP;
			switch (terrain){
				// 拿到钥匙才开的门和出口
				case Terrain.LOCKED_DOOR:
				case Terrain.CRYSTAL_DOOR:
				case Terrain.LOCKED_EXIT:
				// 还没被发现的门，后面可能是唯一的去路
				case Terrain.SECRET_DOOR:
				// 烧掉之后就是路：路障用来堵门，隐藏符石房用一整排书架隔开
				case Terrain.BARRICADE:
				case Terrain.BOOKSHELF:
				// 炼金锅要贴着才能用
				case Terrain.ALCHEMY:
					return KEEP;
			}
			// 地形本身能走、眼下却被封住的格子（魔法火焰房间的永恒之火）：火灭了就是路
			if ((Terrain.flags[terrain] & Terrain.PASSABLE) != 0) return KEEP;
			return OTHER;
		}

		/** kinds 是周围 8 格的类别，顺序同 ring。返回 true 表示撕掉中间这格不碍事。 */
		static boolean removable( int[] kinds ){
			int pass = 0, keep = 0;
			for (int i = 0; i < 8; i++){
				if (kinds[i] == PASS) pass |= 1 << i;
				else if (kinds[i] == KEEP) keep |= 1 << i;
			}

			// 须保留可达的格子原本都挨着中间这格；撕掉之后，圈上得另有一个能走的格子挨着它
			for (int i = 0; i < 8; i++){
				if ((keep & (1 << i)) != 0 && (NEAR[i] & pass) == 0) return false;
			}

			// 圈上能走的格子只靠彼此相连，必须连成一片：从其中一格出发，看能不能走遍
			int reached = pass & -pass;
			int frontier = reached;
			while (frontier != 0){
				int i = Integer.numberOfTrailingZeros( frontier );
				frontier &= frontier - 1;
				int more = NEAR[i] & pass & ~reached;
				reached |= more;
				frontier |= more;
			}
			return reached == pass;
		}
	}
}
