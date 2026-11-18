package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Andromeda;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.BlackBelt;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.CatalystBracer;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ChampionBelt;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Greaves;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.GriffinEye;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.InfusedArmband;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Shelter;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ShiningGloves;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ThePure;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.TridentGauntlet;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.WebBelt;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

/**
 * 测试模式用：弹窗选一件次要护甲，直接放进背包。
 *
 * 名字全部取自各护甲自己的 {@link SecondaryArmor#name()}，不在代码里写死中文；
 * 动作按钮文字走父类的 {@code ac_<action>} 查表。
 */
public class SecondaryArmorSpawner extends TestGenerator {

	/** 12 件次要护甲，数组顺序就是弹窗里的排列顺序。 */
	private static SecondaryArmor[] allArmors() {
		return new SecondaryArmor[]{
				new ChampionBelt(),
				new BlackBelt(),
				new Andromeda(),
				new InfusedArmband(),
				new CatalystBracer(),
				new Shelter(),
				new GriffinEye(),
				new WebBelt(),
				new ThePure(),
				new TridentGauntlet(),
				new ShiningGloves(),
				new Greaves()
		};
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (!AC_GIVE.equals( action )) {
			return;
		}

		final SecondaryArmor[] armors = allArmors();
		String[] names = new String[armors.length];
		for (int i = 0; i < armors.length; i++) {
			names[i] = armors[i].name();
		}

		GameScene.show( new WndOptions( name(), Messages.get( this, "choose" ), names ) {
			@Override
			protected void onSelect( int index ) {
				armors[index].collect();
			}
		} );
	}
}
