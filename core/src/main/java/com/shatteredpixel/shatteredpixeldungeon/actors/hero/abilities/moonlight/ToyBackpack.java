/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChallengeToyEffects;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;


public class ToyBackpack extends ArmorAbility {

	public static final float BASE_CHARGE_COST = 35f;
	private static final float[] STANDARD_CHALLENGE_REWARD_TIERS = {42, 42, 7, 7, 2};

	{
		baseChargeUse = BASE_CHARGE_COST;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		requestGeneration(armor, hero);
	}

	public void requestGeneration(ClassArmor armor, Hero hero) {
		ArrayList<Armor.ToyRef> toys = Armor.ownedToys(hero);
		if (toys.size() >= totalToyCapacity(hero)) {
			GameScene.show(new WndDestroyToy(armor, hero, toys));
			return;
		}
		finishGeneration(armor, hero);
	}

	public static int totalToyCapacity(Hero hero) {
		switch (hero == null ? 0 : hero.pointsInTalent(Talent.EXTRA_POCKET)) {
			case 1:
				return 3;
			case 2:
				return 5;
			case 3:
				return 7;
			case 4:
				return 10;
			default:
				return 1;
		}
	}

	public static int attachedToyCapacity(Hero hero) {
		return hero != null && hero.pointsInTalent(Talent.EXTRA_POCKET) >= 3 ? 2 : 1;
	}

	private void finishGeneration(ClassArmor armor, Hero hero) {
		ItemArmorAttachable toy = armor.generateRandomToy();
		if (toy == null) return;
		com.shatteredpixel.shatteredpixeldungeon.items.toys.TieredToyEffects.onAbilityUsed(hero);

		float chargeCost = chargeUse(hero);
		armor.charge -= chargeCost;
		armor.updateQuickslot();
		Item.updateQuickslot();

		GLog.p(Messages.get(Armor.class, "toy_generated", toy.name()));
		if (!toy.collect(hero.belongings.backpack)) {
			if (toy instanceof com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal) {
				Dungeon.level.drop(toy, hero.pos).sprite.drop();
			} else {
				toy.vanishOnGround(false, hero.pos);
			}
		}

		hero.sprite.operate(hero.pos);
		hero.spendAndNext(Actor.TICK);
	}

	private class WndDestroyToy extends WndOptions {
		private final ClassArmor armor;
		private final Hero hero;
		private final ArrayList<Armor.ToyRef> toys;

		private WndDestroyToy(ClassArmor armor, Hero hero, ArrayList<Armor.ToyRef> toys) {
			super(Messages.get(ToyBackpack.class, "destroy_title"),
					Messages.get(ToyBackpack.class, "destroy_message", totalToyCapacity(hero)),
					toys.stream().map(ref -> ref.toy.name()).toArray(String[]::new));
			this.armor = armor;
			this.hero = hero;
			this.toys = toys;
		}

		@Override
		protected void onSelect(int index) {
			if (index < 0 || index >= toys.size()) return;
			toys.get(index).destroy(hero);
			requestGeneration(armor, hero);
		}
	}

	public static void checkForNewFloorChallenge() {
		if (!Dungeon.levelJustGenerated) return;
		Dungeon.levelJustGenerated = false;

		Hero hero = Dungeon.hero;
		if (hero == null || !hero.isAlive()
				|| !(hero.armorAbility instanceof ToyBackpack)
				|| !hero.hasTalent(Talent.ACCEPT_CHALLENGE)
				|| hero.belongings.armor == null) {
			return;
		}

		Challenge challenge = createChallenge(hero);
		if (challenge != null) GameScene.show(new WndToyChallenge(challenge));
	}

	private static Challenge createChallenge(Hero hero) {
		ArrayList<Class<? extends Mob>> currentRotation = Bestiary.getMobRotation(Dungeon.depth);
		if (currentRotation.isEmpty()) return null;

		Challenge challenge = new Challenge(hero);
		int talentLevel = hero.pointsInTalent(Talent.ACCEPT_CHALLENGE);
		challenge.duration = 25 * talentLevel;
		boolean canUseNextRegion = Branches.MAIN.equals(Dungeon.branchId) && Dungeon.depth <= 20;
		boolean useNextRegion = canUseNextRegion && Random.Float() < 0.15f * talentLevel;

		if (useNextRegion) {
			int nextRegionDepth = ((Dungeon.depth - 1) / 5 + 1) * 5 + 1;
			ArrayList<Class<? extends Mob>> nextRotation = Bestiary.getMobRotation(nextRegionDepth);
			addRandomMob(challenge.mobs, nextRotation);
			challenge.difficulty = 3;
			if (Random.Int(2) == 0) {
				addRandomMob(challenge.mobs, currentRotation);
				challenge.difficulty++;
			}
		} else {
			int count = Random.IntRange(1, 3);
			for (int i = 0; i < count; i++) addRandomMob(challenge.mobs, currentRotation);
			challenge.difficulty = count;
		}

		int rewardCount = Random.Float() < Math.min(0.85f, 0.10f + 0.15f * challenge.difficulty) ? 2 : 1;

		int rewardAttempts = 0;
		while (challenge.rewards.size() < rewardCount && rewardAttempts++ < 20) {
			int tier = Random.chances(STANDARD_CHALLENGE_REWARD_TIERS) + 1;
			ItemArmorAttachable reward = hero.belongings.armor.generateRandomToy(tier);
			if (reward == null) continue;
			boolean duplicate = false;
			for (ItemArmorAttachable existing : challenge.rewards) {
				if (existing.getClass() == reward.getClass()) {
					duplicate = true;
					break;
				}
			}
			if (!duplicate) challenge.rewards.add(reward);
		}
		return challenge.mobs.isEmpty() || challenge.rewards.isEmpty() ? null : challenge;
	}

	private static void addRandomMob(ArrayList<Mob> mobs, ArrayList<Class<? extends Mob>> rotation) {
		if (rotation == null || rotation.isEmpty()) return;
		Mob mob = Reflection.newInstance(Random.element(rotation));
		if (mob != null) mobs.add(mob);
	}

	private static void acceptChallenge(Challenge challenge) {
		Hero hero = challenge.hero;
		if (hero == null || !hero.isAlive()) return;

		spawnChallengeMobs(challenge.mobs, hero);
		for (ItemArmorAttachable reward : challenge.rewards) {
			ChallengeToyEffects.grant(hero, reward, challenge.duration);
			GLog.p(Messages.get(ToyBackpack.class, "challenge_reward_received",
					reward.name(), challenge.duration));
		}
	}

	private static void spawnChallengeMobs(ArrayList<Mob> mobs, Hero hero) {
		ArrayList<Integer> candidates = new ArrayList<>();
		int width = Dungeon.level.width();
		int heroX = hero.pos % width;
		int heroY = hero.pos / width;

		for (int radius = 1; radius <= 6; radius++) {
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dx = -radius; dx <= radius; dx++) {
					if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) continue;
					int x = heroX + dx;
					int y = heroY + dy;
					if (x < 0 || x >= width || y < 0 || y >= Dungeon.level.height()) continue;
					int cell = x + y * width;
					if (Actor.findChar(cell) == null
							&& (Dungeon.level.passable[cell] || Dungeon.level.avoid[cell])) {
						candidates.add(cell);
					}
				}
			}
		}
		Random.shuffle(candidates);

		for (Mob mob : mobs) {
			int selectedIndex = -1;
			for (int i = 0; i < candidates.size(); i++) {
				int cell = candidates.get(i);
				if (!Char.hasProp(mob, Char.Property.LARGE) || Dungeon.level.openSpace[cell]) {
					selectedIndex = i;
					break;
				}
			}
			if (selectedIndex < 0) continue;

			int cell = candidates.remove(selectedIndex);
			mob.pos = cell;
			mob.state = mob.HUNTING;
			GameScene.add(mob, 1f);
			Dungeon.level.occupyCell(mob);
			ScrollOfTeleportation.appear(mob, cell);
		}
	}

	private static class Challenge {
		private final Hero hero;
		private final ArrayList<Mob> mobs = new ArrayList<>();
		private final ArrayList<ItemArmorAttachable> rewards = new ArrayList<>();
		private int difficulty;
		private int duration;

		private Challenge(Hero hero) {
			this.hero = hero;
		}
	}

	private static class WndToyChallenge extends Window {
		private static final int WIDTH = 130;
		private static final int GAP = 2;
		private static final int ICON_SIZE = 18;
		private static final int BUTTON_HEIGHT = 18;

		private WndToyChallenge(final Challenge challenge) {
			RenderedTextBlock title = PixelScene.renderTextBlock(Talent.ACCEPT_CHALLENGE.title(), 9);
			title.hardlight(TITLE_COLOR);
			title.maxWidth(WIDTH);
			title.setPos(0, 0);
			add(title);

			StringBuilder mobNames = new StringBuilder();
			for (Mob mob : challenge.mobs) {
				if (mobNames.length() > 0) mobNames.append('\n');
				mobNames.append("- ").append(mob.name());
			}
			RenderedTextBlock message = PixelScene.renderTextBlock(
					Messages.get(ToyBackpack.class, "challenge_message", mobNames.toString()), 6);
			message.maxWidth(WIDTH);
			message.setPos(0, title.bottom() + GAP * 2);
			add(message);

			float pos = message.bottom() + GAP;
			for (ItemArmorAttachable reward : challenge.rewards) {
				ItemSprite icon = new ItemSprite(reward);
				icon.x = (ICON_SIZE - icon.width()) / 2f;
				icon.y = pos + (ICON_SIZE - icon.height()) / 2f;
				PixelScene.align(icon);
				add(icon);

				RenderedTextBlock name = PixelScene.renderTextBlock(
						Messages.get(ToyBackpack.class, "challenge_reward",
								reward.name(), challenge.duration), 6);
				name.maxWidth(WIDTH - ICON_SIZE - GAP);
				float rowHeight = Math.max(ICON_SIZE, name.height());
				icon.y = pos + (rowHeight - icon.height()) / 2f;
				PixelScene.align(icon);
				name.setPos(ICON_SIZE + GAP, pos + (rowHeight - name.height()) / 2f);
				add(name);
				pos += rowHeight + GAP;
			}

			RedButton accept = new RedButton(Messages.get(ToyBackpack.class, "challenge_accept")) {
				@Override
				protected void onClick() {
					hide();
					acceptChallenge(challenge);
				}
			};
			accept.setRect(0, pos + GAP, (WIDTH - GAP) / 2f, BUTTON_HEIGHT);
			add(accept);

			RedButton decline = new RedButton(Messages.get(ToyBackpack.class, "challenge_decline")) {
				@Override
				protected void onClick() {
					hide();
				}
			};
			decline.setRect(accept.right() + GAP, accept.top(), (WIDTH - GAP) / 2f, BUTTON_HEIGHT);
			add(decline);

			resize(WIDTH, (int) decline.bottom());
		}
	}

	@Override
	public String icon() {
		return HeroIcon.TOY_BACKPACK;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.BETTER_ITEM, Talent.EXTRA_POCKET, Talent.ACCEPT_CHALLENGE, Talent.HEROIC_ENERGY};
	}
}
