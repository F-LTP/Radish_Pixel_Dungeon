package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfBenediction;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * audit-buffs-a：Healing.setHeal 里写的是 amount *= (int) 倍率。
 * 倍率先被截成整数：1.2～1.99 变成 1（祝福之戒对治疗量没有效果），小于 1（诅咒的戒指）变成 0，
 * 治疗量直接清零，一瓶治疗药水只回 1 点血。
 */
public class Extra_AudBuffsA_CursedBenediction implements Scenario {

	@Override
	public String describe() {
		return "戴着被诅咒的祝福之戒喝治疗药水：治疗量被削弱，但不会变成只回 1 点";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.setHeroHP( 50, 300 );
		h.world( "wear a cursed -1 ring of benediction", () -> {
			RingOfBenediction ring = new RingOfBenediction();
			ring.level( -1 );
			ring.cursed = true;
			Dungeon.hero.belongings.ring = ring;
			ring.activate( Dungeon.hero );
		} );
		float mult = h.get( () -> RingOfBenediction.periodMultiplier( Dungeon.hero ) );
		h.require( mult > 0.3f && mult < 1f, "诅咒的祝福之戒的倍率在 0.3 和 1 之间（实际 " + mult + "）" );

		PotionOfHealing potion = h.give( new PotionOfHealing() );
		h.drink( potion );
		h.rest( 15 );

		int hp = h.get( () -> Dungeon.hero.HP );
		h.note( "multiplier " + mult + ", HP 50 -> " + hp + " / 300 (unmodified potion would heal " + (int) (0.8f * 300 + 14) + ")" );
		h.check( hp >= 50 + 100, "倍率 " + mult + " 时治疗药水应回复约 " + Math.round( (0.8f * 300 + 14) * mult ) + " 点生命（实际生命 50 → " + hp + "）" );
	}
}
