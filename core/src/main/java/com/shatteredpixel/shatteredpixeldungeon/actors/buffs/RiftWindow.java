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
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.DimensionalRipper;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Visual;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

/**
 * 空间裂隙窗口：次元撕裂者自己造成传送后，3 回合内可瞬移到被攻击的敌人身旁并追击一次。
 *
 * 按文档规格：按回合记账（act 里 spend(TICK)）、按钮带剩余回合数、
 * 落点用 appear（无视距离）、必中追击、出入场都有裂隙粒子、武器离手/换层/目标死亡即关窗。
 * 锁定的目标只记 actor id（同原版 SnipersMark 的做法），随 buff 一起存档。
 */
public class RiftWindow extends Buff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;
		announced = false;
	}

	public static final int WINDOW_TURNS = 3;

	// 只记目标的 actor id，不直接持有 Char：id 能进存档；换层、或目标被移出本层之后
	// findById 查不到，窗口自然失效，不会对着一个已经不在场上的角色传送和出手。
	// 字段也不能叫 target，那会遮住 Buff.target（持有本 buff 的英雄）。
	private int object = 0;
	private int turnsLeft = WINDOW_TURNS;
	// 开窗后的第一次结算只用来对齐回合，不计入 WINDOW_TURNS，原因见 act()
	private boolean fresh = false;

	// 开窗期间被这个窗口顶掉的按钮。动作按钮只有一个槽位，窗口关闭时要把它还回去：
	// 有些状态只在特定时机设置自己的按钮（工程师的脉冲只在开局和读档时设一次），
	// 不还的话那个按钮就一直不见了。只在内存里记，不进存档：读档时那些状态会先各自
	// 重新设置按钮，本窗口随后在 attachTo 里占槽，照样能记到。
	private ActionIndicator.Action displaced;

	/** 占用动作槽，并记下被顶掉的是谁。 */
	private void takeActionSlot(){
		ActionIndicator.Action current = ActionIndicator.action;
		if (current != null && current != this) displaced = current;
		ActionIndicator.setAction( this );
	}

	/** 打开（或刷新）窗口，锁定这次被攻击的敌人。 */
	public void open( Char enemy ){
		object = enemy.id();
		turnsLeft = WINDOW_TURNS;
		fresh = true;
		// 刷新已有窗口时把下一次结算拉回当前时刻，和新开的窗口一样从这一拍重新数
		timeToNow();
		takeActionSlot();
		BuffIndicator.refreshHero();
		GLog.p( Messages.get( DimensionalRipper.class, "window_open" ) );
	}

	/** 锁定的敌人：必须还在本层的 Actor 表里并且活着，否则返回 null。 */
	private Char lockedEnemy(){
		Actor actor = Actor.findById( object );
		if (actor instanceof Char && ((Char) actor).isAlive()){
			return (Char) actor;
		}
		return null;
	}

	private boolean stillValid(){
		return lockedEnemy() != null
				&& target instanceof Hero
				&& DimensionalRipper.equipped( (Hero) target ) != null;
	}

	@Override
	public boolean attachTo( Char target ) {
		if (super.attachTo( target )){
			// 读档恢复也会走到这里：窗口还开着就把按钮挂回去。
			// 这时英雄和关卡都还在读档途中，不能去读 Dungeon.hero / Dungeon.level。
			if (object != 0) takeActionSlot();
			return true;
		}
		return false;
	}

	@Override
	public String icon() {
		return BuffIndicator.LEVITATION;
	}

	@Override
	public String actionName() {
		return Messages.get( DimensionalRipper.class, "space_tear" );
	}

	/** 借用现成的传送信标帧，不必为按钮单独画图。 */
	@Override
	public String actionIcon() {
		return HeroIcon.WARP_BEACON;
	}

	/** 按钮右下角显示剩余回合数。 */
	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText( PixelScene.pixelFont );
		txt.text( Integer.toString( Math.max( 0, turnsLeft ) ) );
		txt.hardlight( CharSprite.POSITIVE );
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return 0xAA55FF;
	}

	@Override
	public void doAction() {
		final Char victim = lockedEnemy();
		if (victim == null || !stillValid()){
			detach();
			return;
		}
		final Hero hero = (Hero) target;
		int cell = freeCellNear( victim.pos );
		if (cell < 0){
			GLog.w( Messages.get( DimensionalRipper.class, "no_space" ) );
			return;
		}

		hero.busy();
		int from = hero.pos;
		ScrollOfTeleportation.appear( hero, cell );
		// 出入场粒子：appear 只管非英雄角色的起点光点，英雄传送两端都得自己补。
		DimensionalRipper.teleportFx( from, hero.pos );
		// appear 只挪位置和贴图，不刷新视野；和原版传送信标一样由调用方补上，
		// 否则这一拍用的还是落点之前的视野
		Dungeon.observe();
		GameScene.updateFog();
		hero.checkVisibleMobs();
		AttackIndicator.target( victim );

		// appear 会触发落点的地块效果：踩中暗藏陷阱时英雄可能被再次挪走，甚至倒下。
		// 和原版突进类技能一样，出手前复核一次还够不够得着，够不着就只结算这次行动，
		// 否则会隔着半张地图打出一记必中的近战。
		if (hero.isAlive() && victim.isAlive() && hero.canAttack( victim )){
			hero.sprite.attack( victim.pos, new Callback() {
				@Override
				public void call() {
					hero.attack( victim, 1f, 0f, Char.INFINITE_ACCURACY );
					// 英雄出手后一律解除隐形；这条路径不经过 Hero.onAttackComplete，要自己补上
					Invisibility.dispel();
					hero.spendAndNext( hero.attackDelay() );
				}
			});
		} else {
			hero.spendAndNext( hero.attackDelay() );
		}
		detach();
	}

	private int freeCellNear( int pos ){
		int w = Dungeon.level.width();
		int[] dirs = { -w-1, -w, -w+1, -1, 1, w-1, w, w+1 };
		for (int d : dirs){
			int c = pos + d;
			if (c >= 0 && c < Dungeon.level.passable.length
					&& Dungeon.level.passable[c] && Actor.findChar(c) == null){
				return c;
			}
		}
		return -1;
	}

	@Override
	public boolean act() {
		// 刚挂上的 buff 会在当前时刻立刻结算一次（Actor.add 把它排在 now），
		// 这一拍在英雄下一次行动之前。它如果也扣回合，写着 3 回合的窗口实际只有 2 回合可用，
		// 按钮上也永远看不到 3。
		if (fresh){
			fresh = false;
		} else {
			turnsLeft--;
		}
		if (!stillValid() || turnsLeft <= 0){
			detach();
			return true;
		}
		// 每回合重占动作槽，避免被其它动作按钮顶掉后按钮消失。
		takeActionSlot();
		spend( TICK );
		return true;
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction( this );
		// 槽位空出来了才还，而且只还给仍然挂在这个英雄身上的状态：
		// 窗口期间对方可能已经解除（比如连击断了），那就没有可还的了。
		if (ActionIndicator.action == null && displaced instanceof Buff
				&& target != null && target.buff( ((Buff) displaced).getClass() ) == displaced){
			ActionIndicator.setAction( displaced );
		}
		displaced = null;
	}

	private static final String OBJECT = "object";
	private static final String TURNS  = "turns_left";
	private static final String FRESH  = "fresh";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( OBJECT, object );
		bundle.put( TURNS, turnsLeft );
		bundle.put( FRESH, fresh );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		// 这几个键是后加的：此前存下的窗口没有它们，按「没有锁定目标」处理，读档后首次结算自行关闭
		object = bundle.contains( OBJECT ) ? bundle.getInt( OBJECT ) : 0;
		turnsLeft = bundle.contains( TURNS ) ? bundle.getInt( TURNS ) : 0;
		fresh = bundle.contains( FRESH ) && bundle.getBoolean( FRESH );
	}
}
