package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;

import rpdtest.H;
import rpdtest.Reflect;
import rpdtest.Scenario;

/**
 * audit-buffs-a：Berserk.startBerserking 里原版的 turnRecovery *= 2f - power 被改成了
 * turnRecovery *= (int) (2f - power)。怒气超过 100% 时 (int)(2 - power) 恒为 0，
 * 手动狂暴的 100 回合冷却直接变成 0。
 */
public class Extra_AudBuffsA_BerserkCooldown implements Scenario {

	@Override
	public String describe() {
		return "怒气 150% 时手动进入狂暴：冷却按超出的怒气缩短到 50 回合，而不是没有冷却";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.world( "berserker with 150% rage", () -> {
			Dungeon.hero.subClass = HeroSubClasses.BERSERKER;
			Berserk b = Buff.affect( Dungeon.hero, Berserk.class );
			Reflect.set( b, "power", 1.5f );
		} );
		Berserk berserk = h.get( () -> Dungeon.hero.buff( Berserk.class ) );
		h.require( h.get( berserk::canActivateBerserk ), "有破损纹章的护盾、怒气 >= 100%：可以手动进入狂暴" );

		// 与 WndBerserk 里「狂暴」按钮的回调相同
		h.input( "press the berserk button", berserk::toStartBerserking );
		h.frames( 5 );

		int turnRecovery = h.get( () -> Reflect.<Integer>get( berserk, "turnRecovery" ) );
		h.check( turnRecovery == 50, "怒气 150% 时手动狂暴的冷却 = 100 × (2 − 1.5) = 50 回合（实际 " + turnRecovery + "）" );
	}
}
