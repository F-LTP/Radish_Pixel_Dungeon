package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * audit-buffs-a：Hero.restoreFromBundle 末尾的 HeldCardTracker.sync 在没有手持卡牌时走 clear()，
 * clear() 无条件清空动作按钮槽，把别的状态在自己的 restoreFromBundle 里刚放回去的按钮清掉。
 * 这里用狂战士的怒气按钮当代表（任何带动作按钮的转职都一样）。
 */
public class Extra_AudBuffsA_ActionAfterReload implements Scenario {

	@Override
	public String describe() {
		return "狂战士怒气 >= 50% 时的动作按钮，存档读档后还在";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.world( "become a berserker with full rage", () -> {
			Dungeon.hero.subClass = HeroSubClasses.BERSERKER;
			Berserk b = Buff.affect( Dungeon.hero, Berserk.class );
			for (int i = 0; i < 4; i++) b.damage( Dungeon.hero.HT );
		} );
		h.require( h.get( () -> ActionIndicator.action instanceof Berserk ), "存档前动作按钮是狂战士的怒气按钮" );
		h.note( "rage before save: " + h.get( () -> Dungeon.hero.buff( Berserk.class ).getPower() ) );

		h.saveAndReload();

		String after = h.get( () -> ActionIndicator.action == null ? "null" : ActionIndicator.action.getClass().getSimpleName() );
		h.note( "rage after reload: " + h.get( () -> Dungeon.hero.buff( Berserk.class ) == null ? "no buff" : "" + Dungeon.hero.buff( Berserk.class ).getPower() ) );
		h.check( h.get( () -> Dungeon.hero.buff( Berserk.class ) != null && ActionIndicator.action == Dungeon.hero.buff( Berserk.class ) ),
				"读档后动作按钮仍然是怒气按钮（实际动作按钮：" + after + "）" );
	}
}
