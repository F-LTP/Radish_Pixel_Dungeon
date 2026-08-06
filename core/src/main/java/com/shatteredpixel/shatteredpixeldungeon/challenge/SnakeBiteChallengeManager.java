/*
 * Radish Pixel Dungeon
 * Snake Bite Manager - Runtime control of snake bite challenge features
 */

package com.shatteredpixel.shatteredpixeldungeon.challenge;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalSpire;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Pylon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss.GnollKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss.GnollShamanKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Drake;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SentryRoom;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SnakeSprite;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Snake Bite Manager - Controls runtime behavior of Snake Bite challenge.
 * Allows toggling of item/mob sprite/text replacement independently.
 * The base challenge bit (Challenges.SNAKE_BITE) controls whether the challenge is active.
 * The individual flags control whether the specific effects are applied.
 */
public class SnakeBiteChallengeManager {

	private static boolean itemSpriteEnabled = true;
	private static boolean itemTextEnabled = true;
	private static boolean mobSpriteEnabled = true;
	private static boolean mobTextEnabled = true;

	private static final String ITEM_SPRITE_ENABLED = "snakebite_item_sprite";
	private static final String ITEM_TEXT_ENABLED = "snakebite_item_text";
	private static final String MOB_SPRITE_ENABLED = "snakebite_mob_sprite";
	private static final String MOB_TEXT_ENABLED = "snakebite_mob_text";

	public static boolean isActive() {
		return Dungeon.isChallenged(Challenges.SNAKE_BITE);
	}

	public static boolean isItemSpriteEnabled() {
		return itemSpriteEnabled;
	}

	public static void setItemSpriteEnabled(boolean enabled) {
		itemSpriteEnabled = enabled;
	}

	public static boolean isItemTextEnabled() {
		return itemTextEnabled;
	}

	public static void setItemTextEnabled(boolean enabled) {
		itemTextEnabled = enabled;
	}

	public static boolean isMobSpriteEnabled() {
		return mobSpriteEnabled;
	}

	public static void setMobSpriteEnabled(boolean enabled) {
		mobSpriteEnabled = enabled;
	}

	public static boolean isMobTextEnabled() {
		return mobTextEnabled;
	}

	public static void setMobTextEnabled(boolean enabled) {
		mobTextEnabled = enabled;
	}

	public static void save(com.watabou.utils.Bundle bundle) {
		bundle.put(ITEM_SPRITE_ENABLED, itemSpriteEnabled);
		bundle.put(ITEM_TEXT_ENABLED, itemTextEnabled);
		bundle.put(MOB_SPRITE_ENABLED, mobSpriteEnabled);
		bundle.put(MOB_TEXT_ENABLED, mobTextEnabled);
	}

	/**
	 * Restore state from bundle.
	 */
	public static void restore(com.watabou.utils.Bundle bundle) {
		if (bundle.contains(ITEM_SPRITE_ENABLED)) {
			itemSpriteEnabled = bundle.getBoolean(ITEM_SPRITE_ENABLED);
		}
		if (bundle.contains(ITEM_TEXT_ENABLED)) {
			itemTextEnabled = bundle.getBoolean(ITEM_TEXT_ENABLED);
		}
		if (bundle.contains(MOB_SPRITE_ENABLED)) {
			mobSpriteEnabled = bundle.getBoolean(MOB_SPRITE_ENABLED);
		}
		if (bundle.contains(MOB_TEXT_ENABLED)) {
			mobTextEnabled = bundle.getBoolean(MOB_TEXT_ENABLED);
		}
	}

	public static boolean shouldReplaceItemSprite(Item item) {
		return isActive() && itemSpriteEnabled && !isItemClassBlacklisted(item.getClass());
	}

	public static boolean shouldReplaceItemText(Item item) {
		return isActive() && itemTextEnabled && !isItemClassBlacklisted(item.getClass());
	}

	public static boolean shouldReplaceItemText() {
		return isActive() && itemTextEnabled;
	}

	public static boolean shouldReplaceMobSprite(Mob mob) {
		return isActive() && mobSpriteEnabled && !isMobClassBlacklisted(mob.getClass());
	}

	public static boolean shouldReplaceMobText() {
		return isActive() && mobTextEnabled;
	}

	private static final Set<Class<? extends Item>> ITEM_BLACKLIST = new HashSet<>(Arrays.asList(
			Key.class,
			Amulet.class,
			TestItem.class
	));

	private static final Set<Class<? extends Mob>> MOB_BLACKLIST = new HashSet<>(Arrays.asList(
			CrystalSpire.class,
			Mimic.class,
			SentryRoom.Sentry.class,
			Pylon.class,
			Drake.class,
			SentryRoom.Sentry.class,
			GnollShamanKing.class,
			GnollKing.class,
			WandOfWarding.Ward.class,
			MirrorImage.class,
			PrismaticImage.class,
			CrystalSpire.class)
	);

	public static boolean isItemClassBlacklisted(Class<? extends Item> itemClass) {
		for (Class<? extends Item> item : ITEM_BLACKLIST) {
			if (item.isAssignableFrom(itemClass)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isMobClassBlacklisted(Class<? extends Mob> mobClass){
		for (Class<? extends Mob> mob : MOB_BLACKLIST) {
			if (mob.isAssignableFrom(mobClass)) {
				return true;
			}
		}
		return false;
	}
}
