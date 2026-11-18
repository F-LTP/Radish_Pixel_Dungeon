package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.AquaBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.AquaBlast;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 顺带发现（不在用户的报告里）：背包里的水爆晶核（AquaBlast）存档读档之后会变成水爆魔药（AquaBrew）。
 *
 * 原因（代码走查）：ShatteredPixelDungeon 的构造函数里给 AquaBrew 注册了存档别名
 * "…items.spells.AquaBlast"（上游 v2.4.0 把 AquaBlast 改成 AquaBrew 时留下的旧存档迁移），
 * 而这个 fork 里 items/spells/AquaBlast.java 这个类又真实存在。Bundle 读档时先查别名，
 * 于是存下去的 AquaBlast 一律被读成 AquaBrew。
 *
 * 影响面很小：AquaBlast 的配方没有注册，目前只有测试模式的物品生成器能拿到它。
 */
public class Extra_AquaBlastAfterReload implements Scenario {

	@Override
	public String describe() {
		return "背包里的水爆晶核（AquaBlast）存档读档之后还是水爆晶核";
	}

	private static String bag( H h ) {
		return h.get( () -> {
			StringBuilder sb = new StringBuilder();
			for (Item it : Dungeon.hero.belongings) {
				if (it instanceof AquaBlast || it instanceof AquaBrew) {
					if (sb.length() > 0) sb.append( ", " );
					sb.append( it.getClass().getSimpleName() ).append( " x" ).append( it.quantity() );
				}
			}
			return sb.length() == 0 ? "(none)" : sb.toString();
		} );
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		AquaBlast spell = new AquaBlast();
		spell.quantity( 3 );
		h.give( spell );
		h.require( h.get( () -> Dungeon.hero.belongings.getItem( AquaBlast.class ) != null ), "存档前背包里有水爆晶核（AquaBlast）" );
		h.require( h.get( () -> Dungeon.hero.belongings.getItem( AquaBrew.class ) == null ), "存档前背包里没有水爆魔药（AquaBrew）" );
		h.note( "before saving, aqua items in the bag: " + bag( h ) );

		h.saveAndReload();

		h.note( "after reload, aqua items in the bag: " + bag( h ) );
		h.check( h.get( () -> Dungeon.hero.belongings.getItem( AquaBlast.class ) != null ), "读档后背包里仍然有水爆晶核（AquaBlast）；实际：" + bag( h ) );
		h.check( h.get( () -> Dungeon.hero.belongings.getItem( AquaBrew.class ) == null ), "读档后背包里没有凭空多出水爆魔药（AquaBrew）；实际：" + bag( h ) );
	}
}
