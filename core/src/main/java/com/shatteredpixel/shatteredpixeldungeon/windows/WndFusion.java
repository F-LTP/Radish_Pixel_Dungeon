package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Fusion;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

/** 两件同类别装备融合。 */
public class WndFusion extends WndBag {

	public WndFusion() {
		super(Dungeon.hero.belongings.backpack, new ItemSelector() {
			{
				if (com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FusionCharges.fusionCharges(Dungeon.hero) <= 0) {
					com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("没有融合机会，请先获得红包融合属性");
				}
			}
			{
				if (!Fusion.hasValidPair()) {
					com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("没有可融合的同类装备，融合机会保留");
				}
			}
			@Override
			public void onSelect(Item first) {
				if (first == null) return;
				GameScene.show(new WndBag(Dungeon.hero.belongings.backpack, new ItemSelector() {
					@Override
					public void onSelect(Item second) {
						if (second == null) return;
						if (!Fusion.canFuse(first, second)) {
							com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("只能融合同类别装备");
							return;
						}
						Fusion.fuse(first, second);
					}

					@Override
					public String textPrompt() { return "选择第二件装备"; }

					@Override
					public boolean itemSelectable(Item item) {
						return item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon
								|| item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor
								|| item instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand
								|| item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
					}

					@Override
					public Class<? extends Bag> preferredBag() { return null; }
				}));
			}

			@Override
			public String textPrompt() { return "选择第一件装备"; }

			@Override
			public boolean itemSelectable(Item item) {
				return item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon
						|| item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor
						|| item instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand
						|| item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
			}

			@Override
			public Class<? extends Bag> preferredBag() { return null; }
		});
	}
}
