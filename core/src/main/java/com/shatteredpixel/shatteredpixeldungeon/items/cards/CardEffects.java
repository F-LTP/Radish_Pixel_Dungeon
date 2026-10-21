package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CardConversionBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeldCardTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.PaperCutter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndCardSelect;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public final class CardEffects {

	private CardEffects() {
	}

	public static void play(Hero hero, Card card, Integer target) {
		if (hero == null || card == null || hero.cardDeck == null || hero.cardDeck.held != card) {
			return;
		}
		if (target != null && !CardTargeter.isValidTarget(hero, card, target)) {
			GLog.w(Messages.get(CardTargeter.class, "bad_target"));
			return;
		}

		int pos = target == null ? hero.pos : target;
		Char ch = Actor.findChar(pos);

		switch (card.type) {
			case BLANK: {
				boolean self = ch == hero;
				boolean free = false;
				if (self) {
					int shield = hero.pointsInTalent(Talent.BLANK_PROTECTION) == 2 ? 4
							: hero.pointsInTalent(Talent.BLANK_PROTECTION) == 1 ? 2 : 0;
					if (shield > 0) {
						Barrier barrier = Buff.affect(hero, Barrier.class);
						barrier.setShield(barrier.shielding() + shield);
						hero.sprite.showStatus(CharSprite.POSITIVE, "+" + shield);
					}
					free = true;
				} else if (ch != null) {
					int min = hero.pointsInTalent(Talent.BLANK_EMPOWERMENT) == 2 ? 4
							: hero.pointsInTalent(Talent.BLANK_EMPOWERMENT) == 1 ? 3 : 0;
					int max = hero.pointsInTalent(Talent.BLANK_EMPOWERMENT) == 2 ? 8
							: hero.pointsInTalent(Talent.BLANK_EMPOWERMENT) == 1 ? 5 : 0;
					if (max > 0) {
						int dmg = Random.NormalIntRange(min, max);
						int dist = Math.round(Dungeon.level.trueDistance(hero.pos, ch.pos));
						if (hero.hasTalent(Talent.BORROW_WIND_CUT)) {
							dmg += dist * (hero.pointsInTalent(Talent.BORROW_WIND_CUT) == 2
									? Random.NormalIntRange(3, 3) : Random.NormalIntRange(1, 3));
							free = true;
						}
						ch.damage(new DamageInfo(dmg, DamageType.PHYSICAL, hero, null, card));
					} else {
						GLog.w(Messages.get(CardEffects.class, "blank_no_talent"));
					}
				}
				if (hero.hasTalent(Talent.ADVANCED_FOOD)) {
					CardConversionBuff.gain(hero, 1);
				}
				finish(hero, card, false, free ? 0f : card.type.time);
				break;
			}
			case SMALL_RATION:
				eatCard(hero, 50);
				finish(hero, card, false, foodTime(hero, card));
				break;
			case RATION:
				eatCard(hero, 100);
				finish(hero, card, false, foodTime(hero, card));
				break;
			case IDENTIFY: {
				identifyRandom(hero, 1);
				if (card.power >= 2) {
					GameScene.show(new WndOptions(
							Messages.get(CardEffects.class, "destroy_title"),
							Messages.get(CardEffects.class, "destroy_desc"),
							Messages.get(CardEffects.class, "destroy_yes"),
							Messages.get(CardEffects.class, "destroy_no")) {
						@Override
						protected void onSelect(int index) {
							finish(hero, card, index == 0, card.type.time);
						}
					});
				} else {
					finish(hero, card, false, card.type.time);
				}
				break;
			}
			case RIGID: {
				if (ch == null) {
					GLog.w(Messages.get(CardTargeter.class, "bad_target"));
					return;
				}
				int turns = card.power >= 2 ? 8 : 5;
				Buff.affect(ch, Paralysis.class, turns);
				finish(hero, card, false, card.type.time);
				break;
			}
			case HORMONE: {
				int turns = card.power >= 2 ? 8 : 5;
				Buff.affect(hero, Adrenaline.class, turns);
				Buff.affect(hero, Haste.class, turns);
				finish(hero, card, false, card.type.time);
				break;
			}
			case SPIN: {
				int amount = card.power >= 3 ? 5 : card.power == 2 ? 4 : 3;
				hero.cardDeck.spin(amount);
				GLog.i(Messages.get(CardEffects.class, "spin_done", amount));
				finish(hero, card, false, card.type.time);
				break;
			}
			case GRAND_FINALE: {
				if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY) {
					GLog.w(Messages.get(CardTargeter.class, "bad_target"));
					return;
				}
				int dmg = Random.NormalIntRange(5, 10);
				dmg += 10 * hero.cardDeck.blankCount();
				if (card.power >= 3) {
					dmg += Math.round(weaponAverage(card) / 3f);
				}
				ch.damage(new DamageInfo(dmg, DamageType.PHYSICAL, hero, null, card));
				hero.cardDeck.spin(card.power >= 2 ? 3 : 2);
				finish(hero, card, false, card.type.time);
				break;
			}
			case PREPARED: {
				int look = card.power >= 3 ? 4 : card.power == 2 ? 3 : 2;
				int maxMove = card.power >= 3 ? 2 : 1;
				ArrayList<Card> top = new ArrayList<>();
				for (int i = hero.cardDeck.drawPile.size() - 1; i >= Math.max(0, hero.cardDeck.drawPile.size() - look); i--) {
					top.add(hero.cardDeck.drawPile.get(i));
				}
				if (top.isEmpty()) {
					finish(hero, card, false, card.type.time);
				} else {
					GameScene.show(new WndCardSelect(
							Messages.get(CardEffects.class, "prepared_title"),
							Messages.get(CardEffects.class, "prepared_desc", maxMove),
							top, maxMove, true, selected -> {
								for (Card c : selected) {
									if (hero.cardDeck.drawPile.remove(c)) {
										hero.cardDeck.addToBottom(c);
									}
								}
								finish(hero, card, false, card.type.time);
							}));
				}
				break;
			}
			case ATTACK: {
				if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY) {
					GLog.w(Messages.get(CardTargeter.class, "bad_target"));
					return;
				}
				int dmg = Random.NormalIntRange(1, 5) + Math.round(weaponAverage(card) / 3f);
				int refine = hero.pointsInTalent(Talent.REFINED_ATTACK_DEFENSE);
				if (refine > 0) {
					int count = hero.cardDeck.count(CardType.ATTACK);
					if (refine == 1) dmg += count;
					else if (refine == 2) dmg += count * Random.NormalIntRange(1, 2);
					else dmg += count * Random.NormalIntRange(2, 3);
				}
				int emp = hero.pointsInTalent(Talent.BLANK_EMPOWERMENT);
				if (emp == 1) dmg += Random.NormalIntRange(3, 5);
				else if (emp == 2) dmg += Random.NormalIntRange(4, 8);
				if (hero.hasTalent(Talent.BORROW_WIND_CUT)) {
					int dist = Math.round(Dungeon.level.trueDistance(hero.pos, ch.pos));
					dmg += dist * (hero.pointsInTalent(Talent.BORROW_WIND_CUT) == 2
							? 3 : Random.NormalIntRange(1, 3));
				}
				int enchantTalent = hero.pointsInTalent(Talent.CARD_ENCHANT);
				if (enchantTalent > 0) {
					if (card.enchantment == null) {
						card.enchantment = Weapon.Enchantment.random();
					}
					PaperCutter temp = new PaperCutter();
					temp.enchantment = card.enchantment;
					dmg = card.enchantment.proc(temp, hero, ch, dmg);
					dmg = Math.round(dmg * (1f + 0.5f * (enchantTalent - 1)));
				}
				ch.damage(new DamageInfo(dmg, DamageType.PHYSICAL, hero, null, card));
				com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDrawer.reduceCooldown(hero, 5);
				cardAssist(hero);
				finish(hero, card, false, card.type.time);
				break;
			}
			case DEFENSE: {
				int shield = 3 + Math.round(armorAverage(card));
				int prot = hero.pointsInTalent(Talent.BLANK_PROTECTION);
				if (prot == 1) shield += 2;
				else if (prot == 2) shield += 4;
				int refine = hero.pointsInTalent(Talent.REFINED_ATTACK_DEFENSE);
				if (refine > 0) {
					int count = hero.cardDeck.count(CardType.DEFENSE);
					shield += count * refine;
				}
				Barrier barrier = Buff.affect(hero, Barrier.class);
				barrier.setShield(barrier.shielding() + shield);
				hero.sprite.showStatus(CharSprite.POSITIVE, "+" + shield);
				int enchantTalent = hero.pointsInTalent(Talent.CARD_ENCHANT);
				if (enchantTalent > 0) {
					Buff.affect(hero, CardGlyphBoost.class, enchantTalent >= 3 ? 20f : 10f)
							.setStrength(enchantTalent == 1 ? 1.5f : 2f);
				}
				com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDrawer.reduceCooldown(hero, 5);
				cardAssist(hero);
				finish(hero, card, false, card.type.time);
				break;
			}
		}
	}

	private static void finish(Hero hero, Card card, boolean exhaust, float time) {
		if (hero.cardDeck == null || hero.cardDeck.held != card) return;
		hero.sprite.operate(hero.pos);
		if (time > 0) hero.busy();
		if (exhaust) {
			hero.cardDeck.held = null;
		} else {
			hero.cardDeck.playDone(card);
		}
		HeldCardTracker.clear(hero);
		ActionIndicator.refresh();
		Item.updateQuickslot();
		hero.spendAndNext(time);
	}

	private static float foodTime(Hero hero, Card card) {
		return hero.hasTalent(Talent.ADVANCED_FOOD) ? 1f : card.type.time;
	}

	private static void eatCard(Hero hero, float energy) {
		float val = energy;
		if (Dungeon.isChallenged(Challenges.NO_FOOD)) val /= 3f;
		Buff.affect(hero, Hunger.class).satisfy(val);
		hero.sprite.operate(hero.pos);
		SpellSprite.show(hero, SpellSprite.FOOD);
		GLog.i(Messages.get(CardEffects.class, "eaten", (int) energy));
	}

	private static void cardAssist(Hero hero) {
		int points = hero.pointsInTalent(Talent.CARD_ASSIST);
		if (points == 1) {
			Buff.affect(hero, Stamina.class, 1f);
		} else if (points == 2) {
			Buff.affect(hero, Adrenaline.class, 1f);
		} else if (points >= 3) {
			Buff.affect(hero, Stamina.class, 1f);
			Buff.affect(hero, Adrenaline.class, 1f);
		}
	}

	public static void onDeckCycled(Hero hero) {
		if (hero.subClass != HeroSubClasses.SPIN_MASTER || hero.cardDeck == null) return;
		int dmg = Math.max(1, hero.cardDeck.totalSize() / 2);
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[ch.pos]) {
				ch.damage(new DamageInfo(dmg, DamageType.PHYSICAL_NO_ARMOR, hero, null, hero.cardDeck));
			}
		}
		GLog.i(Messages.get(CardEffects.class, "cycle_damage", dmg));
	}

	public static void identifyRandom(Hero hero, int count) {
		for (int i = 0; i < count; i++) {
			Item item = findUnidentified(hero);
			if (item == null) {
				int roll = Random.Int(3);
				Generator.Category cat = roll == 0 ? Generator.Category.SCROLL
						: roll == 1 ? Generator.Category.POTION : Generator.Category.RING;
				item = Generator.random(cat);
				if (item != null) {
					item.identify();
					if (!item.collect(hero.belongings.backpack)) {
						Dungeon.level.drop(item, hero.pos).sprite.drop();
					}
				}
			} else {
				item.identify();
			}
		}
		GLog.i(Messages.get(CardEffects.class, "identified"));
	}

	private static Item findUnidentified(Hero hero) {
		for (Item item : hero.belongings) {
			if (!item.isIdentified() && (item instanceof Scroll || item instanceof Potion || item instanceof Ring)) {
				return item;
			}
		}
		return null;
	}

	private static float weaponAverage(Card card) {
		if (card.gearTier <= 0) return 0;
		int tier = Math.max(1, card.gearTier);
		int min = tier + card.gearLevel;
		int max = 5 * (tier + 1) + card.gearLevel * (tier + 1);
		return (min + max) / 2f;
	}

	private static float armorAverage(Card card) {
		if (card.gearTier <= 0) return 0;
		int drMax = card.gearTier * (2 + card.gearLevel);
		int drMin = Math.min(card.gearLevel, drMax);
		return (drMin + drMax) / 2f;
	}

	/** Used by the Illusionist armor card effect. Kept as a real buff so it is visible in the buff bar. */
	public static class CardGlyphBoost extends FlavourBuff {
		private float strength = 1.5f;

		{
			type = buffType.POSITIVE;
		}

		public CardGlyphBoost setStrength(float value) {
			strength = value;
			return this;
		}

		public float strength() {
			return strength;
		}

		@Override
		public String icon() {
			return BuffIndicator.ARMOR;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.54f, 0.23f, 0.72f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", Math.round(strength * 100));
		}

		private static final String STRENGTH = "strength";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(STRENGTH, strength);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			strength = bundle.getFloat(STRENGTH);
		}
	}
}
