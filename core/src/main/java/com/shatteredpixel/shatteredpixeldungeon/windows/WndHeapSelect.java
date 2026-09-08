package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

/** 多物品堆拾取选择窗口：列出堆中物品，选择后按原拾取路径拾取该物品。 */
public class WndHeapSelect extends Window {

	private static final int WIDTH = 130;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 18;

	public WndHeapSelect(final Heap heap, final int cell) {
		super();

		float pos = MARGIN;
		int w = Math.min(WIDTH, PixelScene.uiCamera.width - chrome.marginHor());

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
		title.maxWidth(w - MARGIN * 2);
		title.setPos(MARGIN, pos);
		add(title);
		pos = title.bottom() + MARGIN;

		Item[] items = heap.items.toArray(new Item[0]);
		for (final Item item : items) {
			RedButton btn = new RedButton(item.title()) {
				@Override
				protected void onClick() {
					Hero hero = Dungeon.hero;
					if (hero == null) return;
					hide();
					hero.pendingPickupItem = item;
					hero.curAction = new HeroAction.PickUp(cell);
					hero.next();
				}
			};
			btn.leftJustify = true;
			btn.icon(new ItemSprite(item));
			btn.setRect(0, pos, w - MARGIN, BUTTON_HEIGHT);
			add(btn);
			pos += BUTTON_HEIGHT + MARGIN;
		}

		resize(w, (int) (pos - MARGIN));
	}
}