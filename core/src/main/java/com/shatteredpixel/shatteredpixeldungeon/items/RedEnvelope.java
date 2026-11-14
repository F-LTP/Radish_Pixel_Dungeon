package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.challenges.Hongbao;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

/** 红包：拾取后立刻开出奖励，不进入背包。 */
public class RedEnvelope extends Item {

	{
		image = ItemSpriteSheet.RED_ENVELOPE;
		stackable = false;
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		//红包不进入背包，直接从地上取走
		Heap heap = Dungeon.level.heaps.get(pos);
		if (heap != null) {
			heap.pickUp(this);
		}
		Hongbao.rewardHero(hero);
		Sample.INSTANCE.play(Assets.Sounds.ITEM);
		// 红包开奖：烟花爆竹音效
		Sample.INSTANCE.play(Assets.Sounds.FIREWORKS, 0.9f);
		hero.spendAndNext(TIME_TO_PICK_UP);
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
