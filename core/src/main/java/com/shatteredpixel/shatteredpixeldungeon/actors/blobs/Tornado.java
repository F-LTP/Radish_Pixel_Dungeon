/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.RectF;
import com.watabou.utils.SparseArray;

//龙卷风药剂生成的风眼：每回合把 9x9 范围内的敌人往中心拖一格。
public class Tornado extends Blob {

	@Override
	protected void evolve() {
		//this blob stays put at each of its centers and simply decays by 1 each turn
		int width = Dungeon.level.width();
		for (int cell = 0; cell < cur.length; cell++) {
			if (cur[cell] <= 0) {
				//off 里留着的是两回合前的值，这里不清零的话，结算后一交换，
				//已经散掉的龙卷风会隔一回合又冒出 1 点来继续拖怪（场上还有别的龙卷风时）
				off[cell] = 0;
				continue;
			}
			off[cell] = cur[cell] - 1;
			volume += off[cell];

			if (Dungeon.level.insideMap(cell)) {
				pullToward(cell, width);
			}
		}
	}

	private void pullToward(int center, int width) {
		int cx = center % width;
		int cy = center / width;

		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (mob.alignment != Char.Alignment.ENEMY
					|| !mob.isAlive()
					|| Char.hasProp(mob, Char.Property.IMMOVABLE)) {
				continue;
			}

			int mx = mob.pos % width;
			int my = mob.pos / width;
			if (Math.max(Math.abs(mx - cx), Math.abs(my - cy)) > 4) {
				continue;
			}

			int sx = Integer.signum(cx - mx);
			int sy = Integer.signum(cy - my);
			int[] candidates = {
					(mx + sx) + (my + sy) * width,
					(mx + sx) + my * width,
					mx + (my + sy) * width
			};

			for (int target : candidates) {
				if (target == mob.pos) {
					continue;
				}
				if (Dungeon.level.insideMap(target)
						&& Dungeon.level.passable[target]
						&& !Dungeon.level.pit[target]
						&& Actor.findChar(target) == null) {
					mob.sprite.move(mob.pos, target);
					mob.move(target, false);
					break;
				}
			}
		}
	}

	@Override
	public void use(BlobEmitter emitter) {
		super.use(emitter);
		//旋风是一个循环播放的贴图，不像别的气体粒子那样自己会消失，所以不能只在建发射器时放一次：
		//那样龙卷风散了贴图还留在原地，之后在别处再出现的龙卷风也没有贴图，
		//开局时风眼不在视野里的话则一直画不出来。
		//改为持续巡检：每个有风且看得见的格子上保证有一个旋风，旋风自己发现风散了就消失。
		//落点固定在格子正中，CYCLONE 靠它反推出是哪一格。
		emitter.bound = new RectF(0.5f, 0.5f, 0.5f, 0.5f);
		emitter.pour(CYCLONE, 0.2f);
	}

	//各格子上当前画着的旋风；只在渲染线程里读写
	private final SparseArray<CycloneParticle> cyclones = new SparseArray<>();

	private final Emitter.Factory CYCLONE = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			int cell = (int)(x / DungeonTilemap.SIZE) + (int)(y / DungeonTilemap.SIZE) * Dungeon.level.width();
			CycloneParticle p = cyclones.get(cell);
			//换层或重建场景后发射器是新的，旧场景里的旋风不算；被回收去画别的格子的也不算
			if (p != null && p.alive && p.parent == emitter && p.cell() == cell) {
				return;
			}
			p = (CycloneParticle)emitter.recycle( CycloneParticle.class );
			p.reset( Tornado.this, cell, x, y );
			cyclones.put(cell, p);
		}
	};

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
