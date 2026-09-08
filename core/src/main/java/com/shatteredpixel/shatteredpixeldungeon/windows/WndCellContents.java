package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake;
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
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

/** 多对象格子检视窗口：列出地形与格内所有对象（怪物、物品、植物、陷阱），点击条目查看对应介绍。 */
public class WndCellContents extends Window {

	private static final float GAP = 2;
	private static final int WIDTH = 130;
	private static final int BUTTON_HEIGHT = 18;

	public WndCellContents(final int cell) {
		super();

		float pos = GAP;

		IconTitle titlebar = new IconTitle();
		titlebar.icon(WndInfoCell.cellImage(cell));
		titlebar.label(WndInfoCell.cellName(cell));
		titlebar.setRect(0, 0, WIDTH, 0);
		add(titlebar);
		pos = titlebar.bottom() + GAP;

		RenderedTextBlock info = PixelScene.renderTextBlock(6);
		info.text(tileDesc(cell), WIDTH - (int)(GAP * 2));
		info.setPos(GAP, pos);
		add(info);
		pos = info.bottom() + GAP;

		Char ch = Actor.findChar(cell);
		if (ch != null) {
			if (ch == Dungeon.hero) {
				final Hero hero = Dungeon.hero;
				pos = addRow(pos, Messages.titleCase(hero.className()), HeroSprite.avatar(hero), new Runnable() {
					@Override
					public void run() {
						GameScene.show(new WndHero());
					}
				});
			} else if (ch instanceof Mob) {
				final Mob mob = (Mob) ch;
				pos = addRow(pos, Messages.titleCase(mob.name()), mob.sprite(), new Runnable() {
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
			if (heap.type == Heap.Type.CHEST
					|| heap.type == Heap.Type.LOCKED_CHEST
					|| heap.type == Heap.Type.CRYSTAL_CHEST) {
				// Chest contents remain hidden until the chest is opened.
				pos = addRow(pos, heap.title(), new ItemSprite(heap), new Runnable() {
					@Override
					public void run() {
						GameScene.show(new WndInfoItem(heap));
					}
				});
			} else {
				for (final Item item : heap.items.toArray(new Item[0])) {
					pos = addRow(pos, item.title(), new ItemSprite(item), new Runnable() {
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
			pos = addRow(pos, Messages.titleCase(plant.name()), null, new Runnable() {
				@Override
				public void run() {
					GameScene.show(new WndInfoPlant(plant));
				}
			});
		}

		Trap trap = Dungeon.level.traps.get(cell);
		if (trap != null && trap.visible) {
			pos = addRow(pos, Messages.titleCase(trap.name()), null, new Runnable() {
				@Override
				public void run() {
					GameScene.show(new WndInfoTrap(trap));
				}
			});
		}

		resize(WIDTH, (int) (pos - GAP));
	}

	private float addRow(float pos, String label, Image icon, final Runnable onClick) {
		RedButton btn = new RedButton(label, 7) {
			@Override
			protected void onClick() {
				if (onClick != null) onClick.run();
			}
		};
		if (icon != null) btn.icon(icon);
		btn.leftJustify = true;
		btn.setRect(0, pos, WIDTH - GAP, BUTTON_HEIGHT);
		add(btn);
		return pos + BUTTON_HEIGHT + GAP;
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
