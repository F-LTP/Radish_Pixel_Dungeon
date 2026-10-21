package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cardtrickster.CardBarrage;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cardtrickster.DeckRestore;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cardtrickster.WildCard;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDeck;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDrawer;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.PaperCutter;

public class CardTrickster extends HeroDefinition {

	@Override
	public HeroClass heroClass() {
		return HeroClasses.CARDTRICKSTER;
	}

	@Override
	public String spritesheet() {
		return Assets.Sprites.CARDTRICKSTER;
	}

	@Override
	public String splashArt() {
		return Assets.Splashes.CARDTRICKSTER;
	}

	@Override
	public String avatarSkin() {
		return Assets.Sprites.AVATARS_CARDTRICKSTER;
	}

	@Override
	public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.ILLUSIONIST, HeroSubClasses.SPIN_MASTER, HeroSubClasses.JOKER };
	}

	@Override
	public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new CardBarrage(), new DeckRestore(), new WildCard() };
	}

	@Override
	public TalentSet talents() {
		TalentSet set = new TalentSet();
		set.setTier1(new Talent[]{
				Talent.FOOD_CARD, Talent.IDENTIFY_CARD, Talent.BLANK_EMPOWERMENT, Talent.BLANK_PROTECTION });
		set.setTier2(new Talent[]{
				Talent.ADVANCED_FOOD, Talent.SCROLL_CONDITIONING, Talent.RIGID_CARD,
				Talent.HORMONE_CARD, Talent.BORROW_WIND_CUT });
		set.setTier3(new Talent[]{ Talent.STREAMLINE, Talent.BLANK_FILL });
		set.setTier4(new Talent[0]);
		return set;
	}

	@Override
	protected void initClassLoadout(Hero hero) {
		(hero.belongings.weapon = new PaperCutter()).identify();
		hero.cardDeck = CardDeck.starterDeck();
		CardDrawer drawer = new CardDrawer();
		drawer.collect();
		Dungeon.quickslot.setSlot(0, drawer);

		new ScrollOfRecharging().identify();
		new PotionOfHaste().identify();
	}
}
