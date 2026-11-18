/*
 * Radish Pixel Dungeon
 *
 * 典狱长（Warden）—— 地牢第 10 层 Boss，与天狗（Tengu）替换出现。
 * 数值与机制细节见类内常量与注释，不再在这里复述需求清单。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.levels.WardenBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WardenSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class Warden extends Mob {

	{
		spriteClass = WardenSprite.class;

		HP = HT = 200;
		EXP = 20;
		maxLvl = 20;

		defenseSkill = 12;

		properties.add(Property.BOSS);
		properties.add(Property.INORGANIC);
		immunities.add(Sleep.class);
	}

	private static final String ENRAGED      = "enraged";
	private static final String NEXT_SPAWN   = "next_spawn";
	private static final String SPEED_BONUS  = "speed_bonus";
	private static final String ACTIVATING   = "activating_cell";
	private static final String ACTIVATING_TURNS = "activating_turns";
	private static final String AWAKE        = "awake";

	private boolean enraged      = false;
	private int     nextSpawnHp  = 30;   // 下一次补生成原型机的生命阈值
	private int     speedBonus   = 0;    // 失去 60 生命后 +1

	//旧版本的典狱长在关卡生成时就摆在场上，靠这个标记区分开没开战。
	//现在开战时才生成，一出场就是 true；只剩 WardenBossLevel 读旧版本存档时还要用它
	private boolean awake = true;

	//当前正在激活的原型机
	private WardenPrototype activating = null;
	private int activatingTurnsLeft = 0;
	//读档时只能先记下目标所在的格子：那时关卡还没装好，Dungeon.level 是 null，
	//要等到第一次行动才能按格子把机体找回来
	private int pendingActivatingCell = -1;

	@Override
	public int attackSkill( Char target ) {
		return 15;
	}

	@Override
	public int damageRoll() {
		return Char.combatRoll( 4, 12 );
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Char.combatRoll( 2, 5 );
	}

	@Override
	public String name() {
		return Messages.get( this, "name" );
	}

	@Override
	public String description() {
		return Messages.get( this, "desc" );
	}

	//查看窗口、格子内容窗口拿到的是没有 link() 的精灵，是不是红眼形态要在这里交给它
	@Override
	public CharSprite sprite() {
		CharSprite sprite = super.sprite();
		sprite.linkVisuals( this );
		return sprite;
	}

	@Override
	public void notice() {
		super.notice();
		//这里要用带参数的版本：无参版本只判断"场上有任意 Boss"，
		//多 Boss 场景下会把自己漏掉
		if (!BossHealthBar.isAssigned( this )) {
			BossHealthBar.assignBoss( this );
			GLog.p( Messages.get( this, "boss_appear" ) );
		}
	}

	@Override
	protected boolean act() {

		//注意：本类在 super.act()（→Mob.act→Char.act）之前就会调用 getCloser()，
		//而视野数组正是在 Char.act() 里才初始化的，所以必须先自己补一次，
		//否则第一回合走向原型机时 Dungeon.findPassable 会拿到 null 的 vis 直接崩。
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView( this, fieldOfView );

		if (pendingActivatingCell != -1) {
			Char target = Actor.findChar( pendingActivatingCell );
			if (target instanceof WardenPrototype) {
				activating = (WardenPrototype) target;
			} else {
				activatingTurnsLeft = 0;
			}
			pendingActivatingCell = -1;
		}

		//通用AI处理麻痹、恐惧及Amok目标切换，不能让开机策略抢占这些状态。
		if (paralysed > 0 || buff( Amok.class ) != null || buff( Terror.class ) != null || buff( Dread.class ) != null || buff( Sleep.class ) != null) {
			return super.act();
		}

		//正在激活某台原型机：每回合推进一次，期间不动
		if (activating != null) {
			WardenPrototype p = activating;
			if (p.isAlive() && p.isInactive() && Dungeon.level.adjacent( p.pos, pos )) {
				if (activatingTurnsLeft > 0) {
					activatingTurnsLeft--;
				}
				if (activatingTurnsLeft <= 0) {
					p.activateByWarden();
					GLog.w( Messages.get( this, "activated" ) );
					activating = null;
				}
				spend( TICK );
				return true;
			}
			//目标没了就地取消
			activating = null;
			activatingTurnsLeft = 0;
		}

		//旧版本在开战前把典狱长设成 PASSIVE，开战后也只有凑够 3 台友军才改回来，
		//于是没有机体可唤醒时它会一直站着挨打。现在出场就是 HUNTING，这里只为纠正旧存档
		if (state == PASSIVE) {
			state = HUNTING;
		}

		//拥有 3 个以上友军后才主动追击玩家
		if (activatedAllies() >= 3 && state != HUNTING && state != FLEEING) {
			state = HUNTING;
			enemySeen = false;
		}

		//少于 3 个友军时优先去激活原型机
		if (activatedAllies() < 3 && isAlive()) {
			WardenPrototype target = nearestInactivePrototype();
			if (target != null) {
				if (Dungeon.level.adjacent( target.pos, pos )) {
					startActivating( target );
					spend( TICK );
					return true;
				}
				int oldPos = pos;
				if (getCloser( target.pos )) {
					spend( 1 / speed() );
					return moveSprite( oldPos, pos );
				}
				//走不过去（去路被别的角色堵住、或者自己被定身）：这一回合交给通用 AI，
				//否则它会一直原地空等，连贴在身边的敌人都不打
			}
		}

		return super.act();
	}

	private void startActivating( WardenPrototype p ) {
		activating = p;
		activatingTurnsLeft = Math.max( 1, p.activationTurns() - speedBonus ) - 1;
		GLog.i( Messages.get( this, "activating" ) );
	}

	public int activatedAllies() {
		int count = 0;
		for (Mob m : Dungeon.level.mobs) {
			if (m instanceof WardenPrototype && m.alignment == Alignment.ENEMY && ((WardenPrototype) m).isWardenAlly()) {
				count++;
			}
		}
		return count;
	}

	private WardenPrototype nearestInactivePrototype() {
		WardenPrototype best = null;
		int bestDist = Integer.MAX_VALUE;
		for (Mob m : Dungeon.level.mobs) {
			if (m instanceof WardenPrototype && m.isAlive() && ((WardenPrototype) m).isInactive()) {
				int d = Dungeon.level.distance( pos, m.pos );
				if (d < bestDist) {
					bestDist = d;
					best = (WardenPrototype) m;
				}
			}
		}
		return best;
	}

	@Override
	public void damage( DamageInfo info ) {

		int preHP = HP;
		super.damage( info );
		int lost = preHP - HP;
		if (lost <= 0) return;

		//这一击已经把自己打死了：尸体不该再补生成原型机、也不再播形态切换的台词
		if (!isAlive()) return;

		//每失去 30 生命补生成一台 DM-100 原型机
		while ( lostTotal() >= nextSpawnHp ) {
			nextSpawnHp += 30;
			if (Dungeon.level instanceof WardenBossLevel) {
				((WardenBossLevel) Dungeon.level).spawnPrototype( 100 );
			}
		}

		//失去 60 生命后加速
		if (speedBonus == 0 && HT - HP >= 60) {
			speedBonus = 1;
			GLog.w( Messages.get( this, "faster" ) );
		}

		//半血切换红眼形态
		if (!enraged && HP * 2 <= HT) {
			enrage();
		}
	}

	private int lostTotal() {
		return HT - HP;
	}

	private void enrage() {
		enraged = true;
		if (sprite instanceof WardenSprite) {
			((WardenSprite) sprite).setEnraged( true );
		}
		yell( Messages.get( this, "enraged" ) );
	}

	@Override
	public void die( Object cause ) {
		//与天狗（Tengu）保持一致：Boss 被击杀要有横幅 + 击杀音效
		GameScene.bossSlain();
		super.die( cause );

		// 掉落：天狗的面具（与天狗一致的战利品）
		Dungeon.level.drop( new TengusMask(), pos ).sprite.drop();

		if (Dungeon.level instanceof WardenBossLevel) {
			((WardenBossLevel) Dungeon.level).onBossDeath();
		}
	}

	/** 半血后的红眼形态（供精灵读档时同步） */
	public boolean isEnraged() {
		return enraged;
	}

	/** 只给 WardenBossLevel 读旧版本存档用，含义见 awake 字段。 */
	public boolean awake() {
		return awake;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ENRAGED, enraged );
		bundle.put( NEXT_SPAWN, nextSpawnHp );
		bundle.put( SPEED_BONUS, speedBonus );
		bundle.put( AWAKE, awake );
		if (activating != null && activating.isAlive()) {
			bundle.put( ACTIVATING, activating.pos );
			bundle.put( ACTIVATING_TURNS, activatingTurnsLeft );
		} else if (pendingActivatingCell != -1) {
			//读档后还没轮到自己行动就又存档：目标还没找回来，原样写回去
			bundle.put( ACTIVATING, pendingActivatingCell );
			bundle.put( ACTIVATING_TURNS, activatingTurnsLeft );
		}
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		enraged = bundle.getBoolean( ENRAGED );
		//老档没有这个键时不能读成 0，否则下一次受伤会把历史阈值一次性全部补生成
		nextSpawnHp = bundle.contains( NEXT_SPAWN ) ? bundle.getInt( NEXT_SPAWN ) : 30;
		speedBonus = bundle.getInt( SPEED_BONUS );
		awake = bundle.getBoolean( AWAKE );
		//读档出来的是个新对象，血条要重新登记，否则要等它下一次 notice() 才出现，追击途中一般等不到。
		//旧版本存档里还没开战的典狱长会被关卡清掉，不登记
		if (awake) {
			BossHealthBar.assignBoss( this );
		}
		//红眼形态不在这里切：读档阶段 sprite 还没创建，交给 WardenSprite.linkVisuals()
		activating = null;
		activatingTurnsLeft = 0;
		pendingActivatingCell = -1;
		if (bundle.contains( ACTIVATING )) {
			pendingActivatingCell = bundle.getInt( ACTIVATING );
			activatingTurnsLeft = bundle.getInt( ACTIVATING_TURNS );
		}
	}
}
