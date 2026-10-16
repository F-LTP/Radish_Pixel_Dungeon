package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

/** The uneaten half created by Moonlight's Meal Utilization talent. */
public class HalfFood extends Food {

	private static final String SOURCE = "source";
	private Food source;

	public HalfFood() {
		energy = 0;
	}

	public HalfFood(Food source) {
		this.source = source;
		energy = source.energy / 2f;
		image = source.image;
		bones = false;
	}

	@Override
	public String name() {
		return Messages.get(this, "name", source == null ? super.name() : source.name());
	}

	@Override
	public String desc() {
		return source == null ? super.desc() : source.desc();
	}

	@Override
	public boolean isSimilar(Item item) {
		return item instanceof HalfFood && source != null
				&& ((HalfFood) item).source != null && source.isSimilar(((HalfFood) item).source);
	}

	@Override
	public void execute(Hero hero, String action) {
		if (!AC_EAT.equals(action)) {
			super.execute(hero, action);
			return;
		}

		detach(hero.belongings.backpack);
		if (hero.pointsInTalent(Talent.MEAL_UTILIZATION) >= 2 && source != null) {
			float oldEnergy = source.energy;
			try {
				source.energy = energy;
				source.satisfyPortion(hero, energy);
				if (source instanceof Blandfruit) {
					Blandfruit fruit = (Blandfruit) source;
					if (fruit.potionAttrib != null) fruit.potionAttrib.apply(hero);
				}
			} finally {
				source.energy = oldEnergy;
			}
		} else {
			satisfyBase(hero, energy);
		}

		GLog.i(Messages.get(this, "eat_msg"));
		hero.sprite.operate(hero.pos);
		hero.busy();
		SpellSprite.show(hero, SpellSprite.FOOD);
		eatSFX();
		hero.spend(eatingTime());
		Talent.onFoodEaten(hero, energy, this);
		Statistics.foodEaten++;
		Badges.validateFoodEaten();
	}

	@Override
	protected float eatingTime() {
		return 1f;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (source != null) bundle.put(SOURCE, source);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		source = (Food) bundle.get(SOURCE);
		if (source != null) {
			energy = source.energy / 2f;
			image = source.image;
		}
	}
}
