package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.Card;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDeck;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDrawer;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardTargeter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeldCardTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WndCardDrawer extends Window {

	private static final int MARGIN = 4;
	private static final int BUTTON_HEIGHT = 18;
	private static final int GAP = 3;

	private final Hero hero;
	private final int width;
	private final int inner;
	private float pos;

	public WndCardDrawer(Hero hero) {
		super();
		this.hero = hero;
		CardDeck deck = CardDeck.getOrCreate(hero);
		width = Math.min(160, PixelScene.uiCamera.width - chrome.marginHor());
		inner = width - MARGIN * 2;

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((width - title.width()) / 2f, MARGIN);
		add(title);
		pos = title.bottom() + MARGIN;

		CardDrawer.DrawCooldown cd = hero.buff(CardDrawer.DrawCooldown.class);
		int cooldown = cd == null ? 0 : (int) cd.cooldown() + 1;
		String status = Messages.get(this, "counts", deck.drawPile.size(), deck.discardPile.size());
		if (deck.freeDraws > 0) {
			status += "\n" + Messages.get(this, "free_draws", deck.freeDraws);
		} else if (cooldown > 0) {
			status += "\n" + Messages.get(this, "cooldown", cooldown);
		}
		if (deck.deletesLeft > 0) {
			status += "\n" + Messages.get(this, "deletes", deck.deletesLeft);
		}
		RenderedTextBlock info = PixelScene.renderTextBlock(status, 6);
		info.maxWidth(inner);
		info.setPos(MARGIN, pos);
		add(info);
		pos = info.bottom() + MARGIN;

		if (deck.held != null) {
			Card card = deck.held;
			RedButton play = new RedButton(Messages.get(this, "held", card.name())) {
				@Override
				protected void onClick() {
					hide();
					CardTargeter.play(hero, card);
				}
			};
			play.icon(new ItemSprite(card.type.image));
			play.leftJustify = true;
			play.setRect(MARGIN, pos, inner - BUTTON_HEIGHT - GAP, BUTTON_HEIGHT);
			add(play);

			IconButton cardInfo = new IconButton(Icons.get(Icons.INFO)) {
				@Override
				protected void onClick() {
					GameScene.show(new WndTitledMessage(
							new ItemSprite(card.type.image), card.name(), card.desc()));
				}
			};
			cardInfo.setRect(play.right() + GAP, pos, BUTTON_HEIGHT, BUTTON_HEIGHT);
			add(cardInfo);
			pos = play.bottom() + GAP;
		} else {
			RenderedTextBlock empty = PixelScene.renderTextBlock(Messages.get(this, "hand_empty"), 6);
			empty.maxWidth(inner);
			empty.setPos(MARGIN, pos);
			add(empty);
			pos = empty.bottom() + GAP;
		}

		RedButton draw = new RedButton(Messages.get(this, "draw")) {
			@Override
			protected void onClick() {
				if (CardDrawer.drawCard(hero)) {
					hide();
					hero.spendAndNext(1f);
					GameScene.show(new WndCardDrawer(hero));
				}
			}
		};
		draw.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
		draw.enable(deck.held == null && deck.totalSize() > 0 && (cooldown <= 0 || deck.freeDraws > 0));
		add(draw);
		pos = draw.bottom() + GAP;

		RedButton viewDraw = new RedButton(Messages.get(this, "view_draw")) {
			@Override
			protected void onClick() {
				openPile(deck.drawPile, Messages.get(WndCardDrawer.class, "draw_pile"));
			}
		};
		viewDraw.setRect(MARGIN, pos, inner / 2f - 1, BUTTON_HEIGHT);
		add(viewDraw);

		RedButton viewDiscard = new RedButton(Messages.get(this, "view_discard")) {
			@Override
			protected void onClick() {
				openPile(deck.discardPile, Messages.get(WndCardDrawer.class, "discard_pile"));
			}
		};
		viewDiscard.setRect(viewDraw.right() + 2, pos, inner / 2f - 1, BUTTON_HEIGHT);
		add(viewDiscard);
		pos = viewDraw.bottom() + GAP;

		RedButton delete = new RedButton(Messages.get(this, "delete", deck.deletesLeft)) {
			@Override
			protected void onClick() {
				if (deck.deletesLeft <= 0) return;
				ArrayList<Card> all = deck.allCards();
				GameScene.show(new WndCardSelect(
						Messages.get(WndCardDrawer.class, "delete_title"),
						Messages.get(WndCardDrawer.class, "delete_desc"),
						all, 1, false, selected -> {
							if (selected.isEmpty()) return;
							Card c = selected.get(0);
							if (deck.delete(c)) {
								deck.deletesLeft--;
								if (deck.held == null) HeldCardTracker.clear(hero);
								hide();
								GameScene.show(new WndCardDrawer(hero));
							}
						}));
			}
		};
		delete.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
		delete.enable(deck.deletesLeft > 0);
		add(delete);
		pos = delete.bottom() + GAP;

		if (hero.subClass == HeroSubClasses.ILLUSIONIST) {
			RedButton transform = new RedButton(Messages.get(this, "transform")) {
				@Override
				protected void onClick() {
					hide();
					CardDrawer.beginTransform(hero);
				}
			};
			transform.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
			add(transform);
			pos = transform.bottom() + GAP;

			RedButton compress = new RedButton(Messages.get(this, "compress")) {
				@Override
				protected void onClick() {
					if (CardDrawer.compress(hero)) {
						hide();
						hero.spendAndNext(1f);
						GameScene.show(new WndCardDrawer(hero));
					}
				}
			};
			compress.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
			add(compress);
			pos = compress.bottom() + GAP;
		}

		RedButton close = new RedButton(Messages.get(this, "close")) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		close.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
		add(close);
		pos = close.bottom() + MARGIN;

		resize(width, (int) pos);
	}

	private void openPile(ArrayList<Card> pile, String title) {
		ArrayList<Card> shown = new ArrayList<>(pile);
		Collections.sort(shown, Comparator.comparingInt(c -> c.type.ordinal()));
		GameScene.show(new WndCardSelect(title,
				Messages.get(this, "pile_desc", shown.size()),
				shown, 1, false, selected -> {
				}));
	}
}
