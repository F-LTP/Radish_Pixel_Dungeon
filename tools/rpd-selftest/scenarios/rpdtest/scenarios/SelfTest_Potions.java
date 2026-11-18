package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自测：喝药水（H.drink）和扔药水（H.throwAt）。
 */
public class SelfTest_Potions implements Scenario {

	@Override
	public String describe() {
		return "工具自测：喝治疗药水回血；把液火药水扔到 4 格外的怪身上，怪着火";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );

		// ---- 喝
		h.setHeroHP( 10, 300 );
		PotionOfHealing heal = h.give( new PotionOfHealing() );
		h.check( h.drink( heal ) == H.Status.READY, "喝完治疗药水后游戏在等下一步输入" );
		h.rest( 3 );
		int hp = h.get( () -> Dungeon.hero.HP );
		h.check( hp > 10, "喝治疗药水后生命回升（10 -> " + hp + "）" );
		h.check( h.get( () -> Dungeon.hero.belongings.getItem( PotionOfHealing.class ) == null ), "药水喝掉了（背包里没有了）" );

		// ---- 扔
		Rat rat = Arena.dummy( h, Arena.c( 14, 10 ) );
		PotionOfLiquidFlame flame = h.give( new PotionOfLiquidFlame() );
		h.throwAt( flame, Arena.c( 14, 10 ) );
		h.rest( 1 );
		h.frames( 30 );
		boolean burning = h.get( () -> rat.buff( Burning.class ) != null );
		boolean fire = h.get( () -> Blob.volumeAt( Arena.c( 14, 10 ), Fire.class ) > 0 );
		int ratHp = h.get( () -> rat.HP );
		h.note( "after throwing liquid flame at (14,10): rat burning=" + burning + " fire on its cell=" + fire + " rat HP=" + ratHp + "/500" );
		h.check( burning || fire || ratHp < 500, "液火药水砸在怪身上：怪着火 / 脚下有火 / 掉血（至少一样）" );
		h.check( h.get( () -> Dungeon.hero.belongings.getItem( PotionOfLiquidFlame.class ) == null ), "药水扔出去了（背包里没有了）" );
	}
}
