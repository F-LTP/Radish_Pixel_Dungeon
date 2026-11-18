package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.util.ArrayList;

/** 多对象格子检视窗口：列出地形与格内所有对象（怪物、物品、植物、陷阱），点击条目查看对应介绍。 */
public class WndCellContents extends Window {

	private static final float GAP = 2;
	private static final int WIDTH = 130;

	public WndCellContents(final int cell) {
		super();

		IconTitle titlebar = new IconTitle();
		titlebar.icon(WndInfoCell.cellImage(cell));
		titlebar.label(WndInfoCell.cellName(cell));
		titlebar.setRect(0, 0, WIDTH, 0);
		add(titlebar);

		RenderedTextBlock info = PixelScene.renderTextBlock(6);
		info.text(tileDesc(cell), WIDTH - (int)(GAP * 2));

		//说明文字和各行先收集起来，最后由 RowList.place 一起摆进窗口
		ArrayList<RowList.Row> rows = new ArrayList<>();

		Char ch = Actor.findChar(cell);
		if (ch != null) {
			if (ch == Dungeon.hero) {
				final Hero hero = Dungeon.hero;
				addRow(rows, Messages.titleCase(hero.className()), HeroSprite.avatar(hero), new Runnable() {
					@Override
					public void run() {
						GameScene.show(new WndHero());
					}
				});
			} else if (ch instanceof Mob && Dungeon.level.heroFOV[cell]) {
				//只列看得见的格子上的怪（GameScene.getObjectsAtCell 也是这个条件）：
				//检视一格记在地图上、现在看不见的格子时，不能告诉玩家那里此刻站着什么
				final Mob mob = (Mob) ch;
				addRow(rows, Messages.titleCase(mob.name()), mob.sprite(), new Runnable() {
					@Override
					public void run() {
						GameScene.show(new WndInfoMob(mob));
						if (mob instanceof Snake && !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_SURPRISE_ATKS)) {
							GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_SURPRISE_ATKS);
						}
					}
				});
			}
		}

		Heap heap = Dungeon.level.heaps.get(cell);
		if (heap != null && heap.seen) {
			if (heap.type != Heap.Type.HEAP && heap.type != Heap.Type.FOR_SALE) {
				//没打开的容器（各种宝箱、墓碑、骸骨、遗骸）：里面有什么要打开才知道，只列容器本身。
				//按「不是散放的物品堆」来判断而不是逐个列举容器类型，以后新增容器类型时默认也是不泄露的
				addRow(rows, heap.title(), new ItemSprite(heap), new Runnable() {
					@Override
					public void run() {
						GameScene.show(new WndInfoItem(heap));
					}
				});
			} else {
				//商品堆里要花钱的只有压在最底下的那一件（见 Heap.forSaleItem），它那一行带上价格；
				//叠在它上面的东西不要钱，照常只写名字。不是商品堆时 forSale 为 null，每一行都只写名字
				final Item forSale = heap.forSaleItem();
				for (final Item item : heap.items.toArray(new Item[0])) {
					String label = item == forSale
							? Messages.get(Heap.class, "for_sale", Shopkeeper.sellPrice(item), item.title())
							: item.title();
					addRow(rows, label, new ItemSprite(item), new Runnable() {
						@Override
						public void run() {
							GameScene.show(new WndInfoItem(item));
						}
					});
				}
			}
		}

		Plant plant = Dungeon.level.plants.get(cell);
		if (plant != null) {
			addRow(rows, Messages.titleCase(plant.name()), null, new Runnable() {
				@Override
				public void run() {
					GameScene.show(new WndInfoPlant(plant));
				}
			});
		}

		Trap trap = Dungeon.level.traps.get(cell);
		if (trap != null && trap.visible) {
			addRow(rows, Messages.titleCase(trap.name()), null, new Runnable() {
				@Override
				public void run() {
					GameScene.show(new WndInfoTrap(trap));
				}
			});
		}

		//标题固定在上面；一格上东西多到一屏放不下时，标题以下的部分可以滚动
		int h = RowList.place(this, info, rows, titlebar.bottom() + GAP, WIDTH, RowList.maxContentHeight(chrome));
		resize(WIDTH, h);
	}

	private void addRow(ArrayList<RowList.Row> rows, String label, Image icon, final Runnable onClick) {
		RowList.Row btn = new RowList.Row(label, 7) {
			@Override
			protected void onClick() {
				if (onClick != null) onClick.run();
			}
		};
		if (icon != null) btn.icon(icon);
		btn.leftJustify = true;
		rows.add(btn);
	}

	private String tileDesc(int cell) {
		String desc = Dungeon.level.tileDesc(Dungeon.level.map[cell]);
		if (Dungeon.level.heroFOV[cell]) {
			for (Blob blob : Dungeon.level.blobs.values()) {
				if (blob.volume > 0 && blob.cur[cell] > 0 && blob.tileDesc() != null) {
					if (desc.length() > 0) {
						desc += "\n\n";
					}
					desc += blob.tileDesc();
				}
			}
		}
		return desc;
	}
}
