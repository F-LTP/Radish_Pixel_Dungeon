/*
 * Radish Pixel Dungeon
 *
 * 简易控制器：典狱长 Boss 战里玩家用来拉拢原型机的一次性道具。
 * 使用后选择目标格子，若上面是未激活的原型机且距离不超过 3 格，则开始破解：
 *   DM-100 需要 3 回合，DM-175 需要 6 回合，倒数结束后原型机归玩家所有。
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class SimpleController extends Item {

	public static final String AC_CONTROL = "CONTROL";

	{
		image = ItemSpriteSheet.CONTROLLER;
		stackable = false;
		unique = true;
		defaultAction = AC_CONTROL;
		usesTargeting = true;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_CONTROL );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_CONTROL )) {
			curUser = hero;
			usesTargeting = true;
			GameScene.selectCell( listener );
		}
	}

	private final CellSelector.Listener listener = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer cell ) {
			if (cell == null) {
				return;
			}
			Char ch = Actor.findChar( cell );
			if ( !(ch instanceof WardenPrototype) ) {
				GLog.w( Messages.get( SimpleController.class, "no_target" ) );
				return;
			}
			WardenPrototype p = (WardenPrototype) ch;
			if ( !p.isInactive() ) {
				GLog.w( Messages.get( SimpleController.class, "already_active" ) );
				return;
			}
			if (Dungeon.level.distance( curUser.pos, p.pos ) > 3) {
				GLog.w( Messages.get( SimpleController.class, "too_far" ) );
				return;
			}
			p.beginHeroActivation();
			//操控期间英雄被定在原地，破解完成前不能行动
			Paralysis.prolong( curUser, Paralysis.class, p.playerActivationTurns() );
			GLog.i( Messages.get( SimpleController.class, "hacking_lock" ) );
			//使用控制器要占掉一回合，和同类的投掷/使用道具保持一致
			curUser.spendAndNext( 1f );
			//控制器是长期道具，用过之后留在背包里
		}

		@Override
		public String prompt() {
			return Messages.get( SimpleController.class, "prompt" );
		}
	};
}
