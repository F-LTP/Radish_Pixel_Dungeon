package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeldCardTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndCardDrawer;
import com.watabou.noosa.Image;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CardDrawer extends Item {

	public static final String AC_OPEN = "OPEN";
	public static final String AC_DRAW = "DRAW";
	public static final String AC_COMPRESS = "COMPRESS";

	{
		image = ItemSpriteSheet.CARD_DRAWER;
		unique = true;
		keptThoughLostInvent = true;
		// 快捷栏点击直接抽卡；查看牌堆请在背包中打开
		defaultAction = AC_DRAW;
		bones = false;
		// 抽卡器是固定独特物品，无需鉴定
		levelKnown = true;
		cursedKnown = true;
	}

	//等级对抽卡器没有任何作用，不让升级卷轴选中它，免得白白浪费一张
	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_OPEN);
		actions.add(AC_DRAW);
		if (hero.subClass == HeroSubClasses.ILLUSIONIST) {
			actions.add(AC_COMPRESS);
		}
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		switch (action) {
			case AC_OPEN:
			case AC_DRAW:
			case AC_COMPRESS:
				return Messages.get(this, "ac_" + action.toLowerCase());
			default:
				return super.actionName(action, hero);
		}
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		CardDeck deck = CardDeck.getOrCreate(hero);

		if (AC_OPEN.equals(action)) {
			GameScene.show(new WndCardDrawer(hero));
		} else if (AC_DRAW.equals(action)) {
			if (drawCard(hero)) {
				hero.spendAndNext(1f);
			}
		} else if (AC_COMPRESS.equals(action)) {
			if (hero.subClass != HeroSubClasses.ILLUSIONIST) return;
			if (compress(hero)) {
				hero.spendAndNext(1f);
			}
		}
	}

	public static boolean drawCard(Hero hero) {
		CardDeck deck = CardDeck.getOrCreate(hero);
		if (deck.held != null) {
			GLog.w(Messages.get(CardDrawer.class, "hand_full"));
			return false;
		}
		if (deck.totalSize() == 0) {
			GLog.w(Messages.get(CardDrawer.class, "empty_deck"));
			return false;
		}
		DrawCooldown cd = hero.buff(DrawCooldown.class);
		boolean freeDraw = deck.freeDraws > 0;
		if (cd != null && cd.cooldown() > 0 && !freeDraw) {
			GLog.w(Messages.get(CardDrawer.class, "cooling", (int) cd.cooldown() + 1));
			return false;
		}

		Card card = deck.draw();
		if (card == null) {
			GLog.w(Messages.get(CardDrawer.class, "empty_deck"));
			return false;
		}
		if (freeDraw) {
			// A free draw bypasses the current cooldown and does not start a new one.
			deck.freeDraws--;
			if (cd != null) cd.detach();
		} else {
			if (cd != null) cd.detach();
			Buff.affect(hero, DrawCooldown.class, 10f);
		}
		HeldCardTracker.sync(hero);
		ActionIndicator.refresh();
		GLog.i(Messages.get(CardDrawer.class, "drew", card.name()));
		return true;
	}

	/** 自动抽卡：冷却结束且没有手牌时触发，不消耗回合。 */
	public static boolean autoDraw(Hero hero) {
		if (hero == null || !hero.isAlive() || !hero.ready) return false;
		CardDeck deck = CardDeck.getOrCreate(hero);
		if (deck.held != null || deck.totalSize() == 0) return false;
		DrawCooldown cd = hero.buff(DrawCooldown.class);
		if (cd != null && cd.cooldown() > 0) return false;
		if (deck.freeDraws > 0) {
			deck.freeDraws--;
			if (cd != null) cd.detach();
		} else {
			if (cd != null) cd.detach();
			Buff.affect(hero, DrawCooldown.class, 10f);
		}
		Card card = deck.draw();
		if (card == null) return false;
		HeldCardTracker.sync(hero);
		ActionIndicator.refresh();
		GLog.i(Messages.get(CardDrawer.class, "drew", card.name()));
		return true;
	}

	public static void reduceCooldown(Hero hero, float amount) {
		DrawCooldown cd = hero.buff(DrawCooldown.class);
		if (cd != null) {
			cd.reduce(amount);
		}
	}

	/** 直接取消抽卡冷却。 */
	public static void cancelCooldown(Hero hero) {
		DrawCooldown cd = hero.buff(DrawCooldown.class);
		if (cd != null) cd.detach();
	}

	public static void beginTransform(Hero hero) {
		// 已移除：变换改为消耗手牌中的空白卡进行，不再直接从背包选装备
	}

	private static void transform(Hero hero, Item item) {
		// 保留空实现占位，实际变换由空白卡流程处理
	}

	public static boolean compress(Hero hero) {
		CardDeck deck = CardDeck.getOrCreate(hero);
		CompressCooldown cd = hero.buff(CompressCooldown.class);
		if (cd != null && cd.cooldown() > 0) {
			GLog.w(Messages.get(CardDrawer.class, "compress_cooling", (int) cd.cooldown() + 1));
			return false;
		}
		ArrayList<Card> blanks = new ArrayList<>();
		for (Card c : deck.allCards()) {
			if (c.type == CardType.BLANK) blanks.add(c);
		}
		if (blanks.size() < 2) {
			GLog.w(Messages.get(CardDrawer.class, "not_enough_blanks"));
			return false;
		}
		for (int i = 0; i < 2; i++) {
			Card c = blanks.remove(Random.Int(blanks.size()));
			deck.delete(c);
		}
		if (deck.held == null) {
			HeldCardTracker.clear(hero);
		}
		Card result = CardFactory.randomAttackOrDefense();
		deck.addToDiscard(result);
		Buff.affect(hero, CompressCooldown.class, 300f);
		GLog.i(Messages.get(CardDrawer.class, "compressed", result.name()));
		return true;
	}

	public static class DrawCooldown extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
		}

		@Override
		public String icon() {
			return BuffIndicator.TIME;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.54f, 0.23f, 0.72f);
		}

		public void reduce(float amount) {
			if (cooldown() <= amount) {
				detach();
			} else {
				spend(-amount);
			}
		}
	}

	public static class CompressCooldown extends FlavourBuff {
		{
			type = buffType.NEUTRAL;
		}

		@Override
		public String icon() {
			return BuffIndicator.TIME;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.54f, 0.23f, 0.72f);
		}
	}
}
