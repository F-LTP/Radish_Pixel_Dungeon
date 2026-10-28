/*
 * Radish Pixel Dungeon
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Prisoner;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FelonSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.DaughterWine;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

/**
 * 传奇重犯：囚犯的强化版（副本 Boss）。
 *
 * 属性与 {@link Prisoner} 相同，仅生命上限提升为 120。
 *
 * 技能（冷却 10 回合）：
 *  1. 指定玩家当前位置，蓄力 1 回合（地面出现预警）；
 *  2. 下回合坠落到该格，对格上目标造成 200% 攻击力伤害；
 *  3. 随后每回合向周围扩散一圈地裂波，共 3 圈；
 *     每圈对范围内目标造成 100% 攻击力伤害并施加 2 回合残废。
 */
public class LegendaryFelon extends Prisoner {

	private int skillCd = 0;
	private int markedCell = -1;
	private int quakeRing = 0;
	private boolean leaping = false;

	protected AiState CHARGING = new Charging();
	protected AiState SLAMMING = new Slamming();
	protected AiState QUAKING = new Quaking();

	{
		spriteClass = FelonSprite.class;

		HP = HT = 120;

		// 小 boss：拥有独立血条（同天狗）
		properties.add( Property.BOSS );

		// 掉落由 die() 自定义
		loot = null;
		lootChance = 0f;

		// 使用带技能的狩猎状态
		HUNTING = new FelonHunting();
	}

	@Override
	protected boolean act() {
		// 跃起坠落过程：暂停其他行为
		if (leaping) {
			spend( TICK );
			return true;
		}
		if (skillCd > 0) {
			skillCd--;
		}
		// 独立血条
		if (!BossHealthBar.isAssigned()) {
			BossHealthBar.assignBoss( this );
		}
		if (HP * 2 <= HT) {
			BossHealthBar.bleed( true );
		}
		return super.act();
	}

	@Override
	public void die( Object cause ) {
		// 掉落 800~1200 金币与 1~2 坛女儿红陈酿
		Dungeon.level.drop( new Gold().quantity( Random.NormalIntRange( 800, 1200 ) ), pos ).sprite.drop();
		int wine = Random.NormalIntRange( 1, 2 );
		if (wine > 0) {
			Dungeon.level.drop( new DaughterWine().quantity( wine ), pos ).sprite.drop();
		}
		super.die( cause );
	}

	// 狩猎时若技能就绪且能看到目标，则改为蓄力
	protected class FelonHunting extends Hunting {
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if (skillCd <= 0 && enemyInFOV && enemy != null && enemy.isAlive()
					&& Dungeon.level.distance( pos, enemy.pos ) <= 8) {
				markedCell = enemy.pos;
				state = CHARGING;
				// 立刻亮出预警
				warn( markedCell );
				spend( TICK );
				return true;
			}
			return super.act( enemyInFOV, justAlerted );
		}
	}

	// 蓄力 1 回合
	protected class Charging implements AiState {
		public static final String TAG = "CHARGING";
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			warn( markedCell );
			state = SLAMMING;
			spend( TICK );
			return true;
		}
	}

	// 坠落冲击
	protected class Slamming implements AiState {
		public static final String TAG = "SLAMMING";
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			doSlam();
			quakeRing = 1;
			state = QUAKING;
			spend( TICK );
			return true;
		}
	}

	// 地裂波扩散（3 圈）
	protected class Quaking implements AiState {
		public static final String TAG = "QUAKING";
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			doQuake( quakeRing );
			quakeRing++;
			if (quakeRing > 3) {
				quakeRing = 0;
				skillCd = 10;
				state = HUNTING;
			}
			spend( TICK );
			return true;
		}
	}

	private void warn( int cell ) {
		markCell( cell );
		if (cell >= 0 && cell < Dungeon.level.length() && Dungeon.level.heroFOV[cell]) {
			CellEmitter.get( cell ).burst( EarthParticle.FACTORY, 6 );
		}
	}

	// 与 DM300 落石同款的红色目标标记
	private void markCell( int cell ) {
		if (cell < 0 || cell >= Dungeon.level.length()) {
			return;
		}
		if (sprite != null && sprite.parent != null) {
			sprite.parent.add( new TargetedCell( cell, 0xFF0000 ) );
		}
	}

	private void doSlam() {
		if (markedCell < 0 || markedCell >= Dungeon.level.length()
				|| !Dungeon.level.passable[markedCell]) {
			markedCell = pos;
		}
		final int land = markedCell;

		if (sprite instanceof FelonSprite) {
			// 使用跃起坠落动画帧
			leaping = true;
			((FelonSprite) sprite).leap( pos, land, new Callback() {
				@Override
				public void call() {
					leaping = false;
					impact( land );
				}
			} );
		} else {
			impact( land );
		}
	}

	private void impact( int landCell ) {
		// 200% 攻击力伤害
		Char ch = Actor.findChar( landCell );
		if (ch != null && ch != this && ch.alignment != alignment) {
			int dmg = Math.round( damageRoll() * 2f );
			ch.damage( new DamageInfo( dmg, DamageType.PHYSICAL, this, null, this ) );
		}

		// 落地（目标格被占据时落到旁边）
		if (landCell != pos) {
			int dest = -1;
			if (Actor.findChar( landCell ) == null && Dungeon.level.passable[landCell]) {
				dest = landCell;
			} else {
				for (int i : PathFinder.NEIGHBOURS8) {
					int c = landCell + i;
					if (Dungeon.level.insideMap(c) && Dungeon.level.passable[c]
							&& !Dungeon.level.pit[c] && Actor.findChar( c ) == null) {
						dest = c;
						break;
					}
				}
			}
			if (dest != -1) {
				move( dest );
				if (sprite != null) sprite.place( pos );
			}
		}

		if (Dungeon.level.heroFOV[landCell]) {
			CellEmitter.get( landCell ).burst( EarthParticle.FACTORY, 20 );
		}
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		PixelScene.shake( 4, 0.8f );

		markedCell = landCell;
	}

	private void doQuake( int ring ) {
		int w = Dungeon.level.width();
		int cx = markedCell % w;
		int cy = markedCell / w;

		for (int x = cx - ring; x <= cx + ring; x++) {
			for (int y = cy - ring; y <= cy + ring; y++) {
				if (Math.max( Math.abs( x - cx ), Math.abs( y - cy ) ) != ring) {
					continue;
				}
				int cell = x + y * w;
				if (!Dungeon.level.insideMap(cell) || Dungeon.level.solid[cell]) {
					continue;
				}
				markCell( cell );
				if (Dungeon.level.heroFOV[cell]) {
					CellEmitter.get( cell ).burst( EarthParticle.FACTORY, 4 );
				}
				Char ch = Actor.findChar( cell );
				if (ch != null && ch != this && ch.alignment != alignment) {
					ch.damage( new DamageInfo( damageRoll(), DamageType.PHYSICAL, this, null, this ) );
					if (ch.isAlive()) {
						Buff.affect( ch, Cripple.class, 2f );
					}
				}
			}
		}
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		PixelScene.shake( 1f, 0.4f );
	}
}
