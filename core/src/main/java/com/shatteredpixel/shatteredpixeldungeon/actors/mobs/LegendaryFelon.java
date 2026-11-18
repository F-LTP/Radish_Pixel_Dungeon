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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Prisoner;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FelonSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.DaughterWine;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

/**
 * 传奇重犯：囚犯的强化版（副本 Boss）。
 *
 * 属性与 {@link Prisoner} 相同，仅生命上限提升为 140。
 *
 * 技能（冷却 10 回合）：
 *  1. 指定玩家当前位置，蓄力 1 回合（地面出现预警）；
 *  2. 下回合坠落到该格，对格上目标造成 200% 攻击力伤害；
 *  3. 随后每回合向周围扩散一圈地裂波，共 3 圈；
 *     每圈对范围内目标造成 100% 攻击力伤害并施加 2 回合残废。
 *
 * 半血以下：镣铐被打烂了，蓄力那一回合还能朝目标挪动一步（不再只能原地挨打）。
 */
public class LegendaryFelon extends Prisoner {

	private int skillCd = 0;
	private int roarCd = 0;
	private int markedCell = -1;
	private int quakeRing = 0;
	private boolean leaping = false;

	protected AiState CHARGING = new Charging();
	protected AiState SLAMMING = new Slamming();
	protected AiState QUAKING = new Quaking();

	{
		spriteClass = FelonSprite.class;

		HP = HT = 140;

		// 小 boss：拥有独立血条（同天狗）
		properties.add( Property.BOSS );

		// 掉落由 die() 自定义
		loot = null;
		lootChance = 0f;

		// 使用带技能的狩猎状态
		HUNTING = new FelonHunting();
	}

	private static final String SKILL_CD    = "skill_cd";
	private static final String ROAR_CD     = "roar_cd";
	private static final String MARKED_CELL = "marked_cell";
	private static final String QUAKE_RING  = "quake_ring";
	private static final String SKILL_STATE = "skill_state";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( SKILL_CD, skillCd );
		bundle.put( ROAR_CD, roarCd );
		bundle.put( MARKED_CELL, markedCell );
		bundle.put( QUAKE_RING, quakeRing );
		// Mob 只会保存自带的五种状态，技能的三个状态要自己存，否则读档后会停在默认的沉睡
		if (state == CHARGING) {
			bundle.put( SKILL_STATE, Charging.TAG );
		} else if (state == SLAMMING) {
			bundle.put( SKILL_STATE, Slamming.TAG );
		} else if (state == QUAKING) {
			bundle.put( SKILL_STATE, Quaking.TAG );
		}
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		// 旧存档没有这些键，保持字段初始值
		if (bundle.contains( SKILL_CD )) skillCd = bundle.getInt( SKILL_CD );
		if (bundle.contains( ROAR_CD )) roarCd = bundle.getInt( ROAR_CD );
		if (bundle.contains( MARKED_CELL )) markedCell = bundle.getInt( MARKED_CELL );
		if (bundle.contains( QUAKE_RING )) quakeRing = bundle.getInt( QUAKE_RING );
		if (bundle.contains( SKILL_STATE )) {
			String skillState = bundle.getString( SKILL_STATE );
			if (skillState.equals( Charging.TAG )) {
				state = CHARGING;
			} else if (skillState.equals( Slamming.TAG )) {
				state = SLAMMING;
			} else if (skillState.equals( Quaking.TAG )) {
				state = QUAKING;
			}
		}
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
		if (roarCd > 0) {
			roarCd--;
		}
		// 独立血条（进入视野才显示，同原版 Boss）
		if (!BossHealthBar.isAssigned() && Dungeon.level != null && Dungeon.level.heroFOV[pos]) {
			BossHealthBar.assignBoss( this );
		}
		if (HP * 2 <= HT) {
			BossHealthBar.bleed( true );
		}
		// 作为 Boss：一旦进入战斗状态就封锁本层，打完才能离开（同史莱姆王）
		if (state != SLEEPING && Dungeon.level != null) {
			if (!Dungeon.level.locked) {
				Dungeon.level.seal();
				GLog.n( Messages.get( this, "seal" ) );
			}
			if (Dungeon.level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.ConvictLevel) {
				// 玩家还没走进竞技场时不锁门（否则会被关在外面卡死），进门那一刻才锁
				((com.shatteredpixel.shatteredpixeldungeon.levels.ConvictLevel) Dungeon.level).sealBossDoor();
			}
		}
		return super.act();
	}

	/**
	 * 战吼：对视野内的英雄施加恐惧，恐惧期间英雄无法使用近战攻击。
	 */
	private void roar( Char target ) {
		roarCd = 22;
		if (target != null && target.isAlive()) {
			Buff.prolong( target, Terror.class, 8f ).object = id();
			target.sprite.showStatus( CharSprite.NEGATIVE, Messages.get( this, "roar_status" ) );
		}
		GLog.w( Messages.get( this, "roar" ) );
		Camera.main.shake( 4, 0.4f );
	}

	@Override
	public void die( Object cause ) {
		// 掉落 800~1200 金币与 1~2 坛女儿红陈酿
		Dungeon.level.drop( new Gold().quantity( Random.NormalIntRange( 800, 1200 ) ), pos ).sprite.drop();
		int wine = Random.NormalIntRange( 1, 2 );
		if (wine > 0) {
			Dungeon.level.drop( new DaughterWine().quantity( wine ), pos ).sprite.drop();
		}
		if (Dungeon.level != null) {
			Dungeon.level.unseal();
			// 传奇重犯副本：击败后打开那扇上锁的门
			if (Dungeon.level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.ConvictLevel) {
				((com.shatteredpixel.shatteredpixeldungeon.levels.ConvictLevel) Dungeon.level).openBossDoor();
			}
		}
		super.die( cause );
	}

	// 狩猎时若技能就绪且能看到目标，则改为蓄力
	protected class FelonHunting extends Hunting {
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			// 技能锁定英雄，而不是被召唤物带偏
			Char target = null;
			if (Dungeon.hero != null && Dungeon.hero.isAlive()
					&& fieldOfView[Dungeon.hero.pos]) {
				target = Dungeon.hero;
			} else if (enemyInFOV && enemy != null && enemy.isAlive()) {
				target = enemy;
			}

			if (roarCd <= 0 && target != null
					&& Dungeon.level.distance( pos, target.pos ) <= 7
					&& target.buff( Terror.class ) == null) {
				roar( target );
				spend( TICK );
				return true;
			}

			if (skillCd <= 0 && target != null
					&& Dungeon.level.distance( pos, target.pos ) <= 8) {
				markedCell = target.pos;
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
			// 半血以下镣铐已经碎了：蓄力的这一回合还可以朝目标挪一步。
			// 这一回合的计时已由蓄力动作 spend(TICK) 占用，所以移动不再额外花时间；
			// 挪动后把预警重新锁到目标脚下，免得加强变成砸空。
			if (HP * 2 <= HT) {
				Char tgt = chargeTarget( enemyInFOV );
				if (tgt != null && !canAttack( tgt )) {
					int oldPos = pos;
					if (getCloser( tgt.pos )) {
						moveSprite( oldPos, pos );
						if (tgt.isAlive() && fieldOfView[tgt.pos]) {
							markedCell = tgt.pos;
						}
					}
				}
			}

			warn( markedCell );
			state = SLAMMING;
			spend( TICK );
			return true;
		}
	}

	// 蓄力/坠落始终优先盯着英雄，避免被召唤物带偏
	private Char chargeTarget( boolean enemyInFOV ) {
		if (Dungeon.hero != null && Dungeon.hero.isAlive()
				&& fieldOfView[Dungeon.hero.pos]) {
			return Dungeon.hero;
		}
		if (enemyInFOV && enemy != null && enemy.isAlive()) {
			return enemy;
		}
		return null;
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
					// 跃起动画期间其他行动者照常行动，落地前可能已经被毒、流血或队友打死：尸体不能再砸人、再挪位置
					if (isAlive()) impact( land );
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
			// 技能伤害不经过 Char.attack，砸死英雄要自己记录死因，否则这局不会进排行榜
			if (ch == Dungeon.hero && !ch.isAlive()) {
				Dungeon.fail( getClass() );
				GLog.n( Messages.capitalize( Messages.get( Char.class, "kill", name() ) ) );
			}
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
					} else if (ch == Dungeon.hero) {
						// 同坠落：地裂波震死英雄也要自己记录死因
						Dungeon.fail( getClass() );
						GLog.n( Messages.capitalize( Messages.get( Char.class, "kill", name() ) ) );
					}
				}
			}
		}
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		PixelScene.shake( 1f, 0.4f );
	}
}
