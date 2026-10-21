package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.cards.Card;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/** Generic card list used for pile inspection, deletion and prepared-card selection. */
public class WndCardSelect extends Window {

	public interface Listener {
		void onSelect(ArrayList<Card> selected);
	}

	private static final int MARGIN = 4;
	private static final int BUTTON_HEIGHT = 18;
	private static final int ROW_HEIGHT = 20;
	private static final int MAX_LIST_HEIGHT = 110;

	private final ArrayList<Card> selected = new ArrayList<>();
	private final int maxSelect;
	private final boolean multi;
	private final Listener listener;
	private final ArrayList<CardButton> buttons = new ArrayList<>();

	public WndCardSelect(String title, String desc, ArrayList<Card> cards,
	                     int maxSelect, boolean multi, Listener listener) {
		super();
		this.maxSelect = Math.max(1, maxSelect);
		this.multi = multi;
		this.listener = listener;

		int width = Math.min(160, PixelScene.uiCamera.width - chrome.marginHor());
		int inner = width - MARGIN * 2;

		RenderedTextBlock heading = PixelScene.renderTextBlock(title, 9);
		heading.hardlight(TITLE_COLOR);
		heading.maxWidth(inner);
		heading.setPos(MARGIN, MARGIN);
		add(heading);

		float pos = heading.bottom() + MARGIN;
		RenderedTextBlock info = PixelScene.renderTextBlock(desc, 6);
		info.maxWidth(inner);
		info.setPos(MARGIN, pos);
		add(info);
		pos = info.bottom() + MARGIN;

		Component content = new Component();
		float y = 0;
		for (Card card : cards) {
			CardButton button = new CardButton(card);
			button.setRect(0, y, inner, BUTTON_HEIGHT);
			content.add(button);
			buttons.add(button);
			y += ROW_HEIGHT;
		}
		content.setSize(inner, Math.max(y, BUTTON_HEIGHT));

		int listHeight = (int) Math.min(MAX_LIST_HEIGHT, Math.max(BUTTON_HEIGHT, y));
		ScrollPane pane = new ScrollPane(content);
		pane.setRect(MARGIN, pos, inner, listHeight);
		add(pane);
		pos += listHeight + MARGIN;

		RedButton confirm = new RedButton(Messages.get(this, "confirm")) {
			@Override
			protected void onClick() {
				if (listener != null) listener.onSelect(new ArrayList<>(selected));
				hide();
			}
		};
		confirm.setRect(MARGIN, pos, inner / 2f - 1, BUTTON_HEIGHT);
		add(confirm);

		RedButton cancel = new RedButton(Messages.get(this, "cancel")) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		cancel.setRect(confirm.right() + 2, pos, inner / 2f - 1, BUTTON_HEIGHT);
		add(cancel);

		resize(width, (int) (cancel.bottom() + MARGIN));
	}

	private class CardButton extends RedButton {
		private final Card card;

		CardButton(Card card) {
			super(card.name() + (card.power > 1 ? " +" + (card.power - 1) : ""), 6);
			this.card = card;
		}

		@Override
		protected void onClick() {
			if (selected.remove(card)) {
				alpha(1f);
				return;
			}
			if (!multi) {
				selected.clear();
				for (CardButton b : buttons) b.alpha(1f);
			}
			if (selected.size() >= maxSelect) {
				if (!multi) selected.clear();
				else return;
			}
			selected.add(card);
			alpha(0.55f);
		}
	}
}
