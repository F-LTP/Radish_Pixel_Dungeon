package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;

/** 多物品堆拾取选择窗口：列出堆中物品，选择后按原拾取路径拾取该物品。 */
public class WndHeapSelect extends Window {

	private static final int WIDTH = 130;
	private static final int MARGIN = 2;

	public WndHeapSelect(final Heap heap, final int cell) {
		super();

		int w = Math.min(WIDTH, PixelScene.uiCamera.width - chrome.marginHor());

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
		title.maxWidth(w - MARGIN * 2);
		title.setPos(MARGIN, MARGIN);
		add(title);

		ArrayList<RowList.Row> rows = new ArrayList<>();
		Item[] items = heap.items.toArray(new Item[0]);
		for (final Item item : items) {
			RowList.Row btn = new RowList.Row(item.title(), 9) {
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
			rows.add(btn);
		}

		//标题固定在上面，物品多到一屏放不下时下面的列表可以滚动
		int h = RowList.place(this, null, rows, title.bottom() + MARGIN, w, RowList.maxContentHeight(chrome));
		resize(w, h);
	}
}
