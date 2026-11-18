/*
 * Radish Pixel Dungeon
 *
 * 原型机（WardenPrototype）—— 典狱长 Boss 战里的 DM-100 / DM-175 未激活机体。
 *
 * 状态机：
 *   未激活（NEUTRAL，躺倒不动）
 *     ├─ 被典狱长靠近激活 → 变成典狱长的友军（ENEMY，攻击玩家）
 *     ├─ 被玩家用简易控制器激活 → 进入倒数，若干回合后变成玩家友军（ALLY）
 *     └─ 被电击符石命中 → 立刻变成玩家友军（ALLY）
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DMPrototypeSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class WardenPrototype extends Mob implements Callback {

	//100 = DM-100 原型机（弱），175 = DM-175 原型机（强）
	public int model = 100;

	private static final String MODEL      = "model";
	private static final String STATE      = "proto_state";
	private static final String PROGRESS   = "proto_progress";
	private static final String SHIELD_GRANTED = "shield_granted";

	//INACTIVE：未激活；W_ALLY：已归典狱长；H_ALLY：已归玩家
	public static final int INACTIVE = 0;
	public static final int W_ALLY   = 1;
	public static final int H_ALLY   = 2;

	private int protoState = INACTIVE;
	private int progress   = 0;   // 玩家侧激活还剩的回合

	//DM-175 的护盾一台机体只给一次，和普通 DM-175 的做法一致；
	//不记下来的话护盾被打掉或自然衰减完之后，下一回合又会补满
	private boolean shieldGranted = false;

	{
		spriteClass = DMPrototypeSprite.class;
		alignment = Alignment.NEUTRAL;

		properties.add(Property.INORGANIC);
		properties.add(Property.ELECTRIC);
	}

	public WardenPrototype() {
		this( 100 );
	}

	public WardenPrototype( int model ) {
		this.model = model;
		if (model == 175) {
			HP = HT = 34;
			EXP = 0;
			maxLvl = 18;
		} else {
			HP = HT = 22;
			EXP = 0;
			maxLvl = 15;
		}
	}

	/** 175 型是重型机：更硬、打得更疼，但数量少。 */
	private boolean heavy() {
		return model == 175;
	}

	/** 按机型二选一，避免到处写 `model == 175 ? a : b`。 */
	private int pick( int heavyValue, int lightValue ) {
		return heavy() ? heavyValue : lightValue;
	}

	@Override
	public int damageRoll() {
		return Char.combatRoll( pick( 4, 2 ), pick( 10, 7 ) );
	}

	@Override
	public int attackSkill( Char target ) {
		return pick( 16, 12 );
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Char.combatRoll( pick( 2, 1 ), pick( 6, 4 ) );
	}

	@Override
	public String name() {
		return Messages.get( this, heavy() ? "name_175" : "name_100" );
	}

	//查看窗口、格子内容窗口拿到的是没有 link() 的精灵，机型和是否激活要在这里交给它，
	//否则一律显示成构造时的默认贴图
	@Override
	public CharSprite sprite() {
		CharSprite sprite = super.sprite();
		sprite.linkVisuals( this );
		return sprite;
	}

	public boolean isInactive() {
		return protoState == INACTIVE && isAlive();
	}

	/** 是否已经被某一方激活；和 isInactive() 不同，机体被打坏之后这里的结果不变。 */
	public boolean isActivated() {
		return protoState != INACTIVE;
	}

	public boolean isWardenAlly() {
		return protoState == W_ALLY;
	}

	public boolean isHeroAlly() {
		return protoState == H_ALLY;
	}

	/** 典狱长开始激活需要的回合数 */
	public int activationTurns() {
		return model == 175 ? 4 : 2;
	}

	/** 玩家用简易控制器激活需要的回合数 */
	public int playerActivationTurns() {
		return model == 175 ? 6 : 3;
	}

	/** 典狱长把原型机激活成自己的友军 */
	public void activateByWarden() {
		if (protoState != INACTIVE) return;
		Buff.detach( this, Corruption.class );
		protoState = W_ALLY;
		alignment = Alignment.ENEMY;
		if (sprite != null) {
			sprite.showStatus( 0xFFCC22, Messages.get( this, "activated_warden" ) );
			activateSprite();
		}
	}

	/** 电击符石：立刻归玩家 */
	public void activateByHero() {
		if (protoState != INACTIVE) return;
		Buff.detach( this, Corruption.class );
		protoState = H_ALLY;
		progress = 0;
		alignment = Alignment.ALLY;
		if (sprite != null) {
			sprite.showStatus( 0x22CCFF, Messages.get( this, "activated_hero" ) );
			activateSprite();
		}
	}

	//蛇咬挑战会把怪物精灵整个换成蛇，那时 sprite 不是 DMPrototypeSprite，不能直接强转
	private void activateSprite() {
		if (sprite instanceof DMPrototypeSprite) {
			((DMPrototypeSprite) sprite).activate();
		}
	}

	/** 简易控制器：倒数若干回合后归玩家 */
	public void beginHeroActivation() {
		if (protoState != INACTIVE) return;
		progress = playerActivationTurns();
		GLog.w( Messages.get( this, "hacking_start" ) );
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		if (model == 100) {
			// DM100：可以近战，或者弹道能打到就可以远程闪电
			return super.canAttack(enemy)
					|| new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
		} else {
			// DM175：只能普通近战，复用父类判定（相邻才能攻击）
			return super.canAttack(enemy);
		}
	}



	@Override
	protected boolean doAttack( Char enemy ) {
		if (model != 100 || Dungeon.level.adjacent(pos, enemy.pos)
				|| new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {
			return super.doAttack(enemy);
		}
		spend(1f);
		Invisibility.dispel(this);
		if (hit(this, enemy, true)) {
			int dmg = Char.combatRoll(3, 10);
			enemy.damage(DamageInfo.of(dmg, DamageType.LIGHTNING, this, new DM100.LightningBolt()));
			if (enemy.sprite.visible) {
				enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
				enemy.sprite.flash();
			}
			//远程闪电不走 Char.attack()，打死英雄时的结算要自己补，和 DM-100 一样；
			//否则这一局不会进排行榜，日志里也没有死因
			if (enemy == Dungeon.hero && !enemy.isAlive()) {
				Badges.validateDeathFromEnemyMagic();
				Dungeon.fail( this );
				GLog.n( Messages.get( DM100.class, "zap_kill" ) );
			}
		} else {
			enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
		}
		//这一击打死典狱长时，清场会把自己也一并销毁：不能再去播放电动画等回调，
		//正在死亡的精灵不保证会把动画播完，next() 就没人叫了
		if (!isAlive()) {
			return true;
		}
		if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
			sprite.zap(enemy.pos);
			return false;
		}
		return true;
	}

	public void onZapComplete() {
		next();
	}

	@Override
	protected boolean act() {
		if (model == 175 && protoState != INACTIVE && !shieldGranted
				&& Dungeon.level.heroFOV[pos]) {
			Buff.affect(this, Barrier.class).setShield(60);
			shieldGranted = true;
		}

		if (protoState == INACTIVE) {
			if (progress > 0) {
				progress--;
				if (progress <= 0) {
					activateByHero();
				}
			}
			//未激活的机体不动、不攻击
			spend( TICK );
			return true;
		}

		return super.act();
	}

	@Override
	public void damage( DamageInfo info ) {
		// 受伤不会擅自决定阵营；阵营只由典狱长、控制器或电击符石改变。
		super.damage( info );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( MODEL, model );
		bundle.put( STATE, protoState );
		bundle.put( PROGRESS, progress );
		bundle.put( SHIELD_GRANTED, shieldGranted );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		model = bundle.getInt( MODEL );
		protoState = bundle.getInt( STATE );
		progress = bundle.getInt( PROGRESS );
		//旧档没有这个键：已经激活的机体在旧版本里早就拿过护盾，按已给过处理，
		//否则更新后每台都会再白拿一次
		shieldGranted = bundle.contains( SHIELD_GRANTED )
				? bundle.getBoolean( SHIELD_GRANTED )
				: protoState != INACTIVE;
		if (protoState == W_ALLY) alignment = Alignment.ENEMY;
		else if (protoState == H_ALLY) alignment = Alignment.ALLY;
		else alignment = Alignment.NEUTRAL;
	}

	@Override
	public void call() {
		next();
	}
}
