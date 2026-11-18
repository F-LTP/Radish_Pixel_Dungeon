package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.InfusedArmbandBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.InfusedArmband;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * audit-buffs-a：灌能臂缠的说明是「法杖造成伤害后，获得持续 10 回合的伤害加成」。
 * InfusedArmbandBuff.apply 用的是 Buff.affect( …, duration )，它是在剩余时间上再加 duration，
 * 每造成一次法杖伤害就多 10 回合，可以无限叠长。
 */
public class Extra_AudBuffsA_ArmbandDuration implements Scenario {

	@Override
	public String describe() {
		return "灌能臂缠：连续用法杖造成伤害，增伤的剩余时间是刷新到 10 回合，而不是每次再加 10 回合";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		int cell = Arena.c( 13, 10 );
		Rat rat = Arena.dummy( h, cell );
		WandOfMagicMissile wand = h.give( new WandOfMagicMissile() );
		h.world( "wear an infused armband", () -> {
			Dungeon.hero.STR = 30;
			Dungeon.hero.belongings.secondaryArmor = new InfusedArmband();
		} );

		h.zap( wand, cell );
		h.require( h.get( () -> Dungeon.hero.buff( InfusedArmbandBuff.class ) != null ), "第一次法杖伤害后获得灌能臂缠的增伤" );
		float first = h.get( () -> Dungeon.hero.buff( InfusedArmbandBuff.class ).cooldown() );

		h.zap( wand, cell );
		h.require( h.get( () -> Dungeon.hero.buff( InfusedArmbandBuff.class ) != null ), "第二次法杖伤害后增伤还在" );
		float second = h.get( () -> Dungeon.hero.buff( InfusedArmbandBuff.class ).cooldown() );

		h.note( "remaining after 1st zap: " + first + ", after 2nd zap: " + second + "; " + Arena.describe( h, rat ) );
		h.check( second <= 10.01f, "第二次法杖伤害后，增伤剩余时间不超过 10 回合（第一次后 " + first + "，第二次后 " + second + "）" );
	}
}
