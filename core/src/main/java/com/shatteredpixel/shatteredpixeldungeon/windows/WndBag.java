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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RightClickMenu;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.UITheme;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PointF;

public class WndBag extends WndTabbed {
	
	//only one bag window can appear at a time
	public static Window INSTANCE;

	protected static final int COLS_P   = 6;

	protected static int SLOT_WIDTH_P   = 28;

	protected static int SLOT_HEIGHT_P	= 28;

	protected static final int SLOT_MARGIN	= 1;

	protected static final int TITLE_HEIGHT	= 14;
	
	private ItemSelector selector;

	private int nCols;
	private int nRows;

	private int slotWidth;
	private int slotHeight;

	protected int count;
	protected int col;
	protected int row;

	/** 物品格的可滚动容器（容量超过窗口时上下滑动）。 */
	private Component content;
	private ScrollPane scroll;
	private int scrollHeight;
	private boolean scrollSized;
	
	private static Bag lastBag;

	public WndBag( Bag bag ) {
		this(bag, null);
	}

	public WndBag( Bag bag, ItemSelector selector ) {
		
		super();
		
		if( INSTANCE != null ){
			INSTANCE.hide();
		}
		INSTANCE = this;
		
		this.selector = selector;
		
		lastBag = bag;

		// 标准布局：7 列 × 6 行（第一行是 6 个装备位），横屏与竖屏统一，跟桌面版一个样子。
		// 屏幕确实放不下时先缩小格子，缩到最小还不行才减列数。
		// 破碎原版观感：6 列 × 7 行；第一行 6 个装备位正好占满一行
		final int TARGET_COLS = 6;
		final int TARGET_ROWS = 7;

		int availW = (int)(PixelScene.uiCamera.width  * 0.94f - chrome.marginHor());
		int availH = (int)(PixelScene.uiCamera.height * 0.82f - chrome.marginTop() - chrome.marginBottom() - TITLE_HEIGHT);

		int slotByW = (availW - (TARGET_COLS - 1) * SLOT_MARGIN) / TARGET_COLS;
		int slotByH = (availH - (TARGET_ROWS - 1) * SLOT_MARGIN) / TARGET_ROWS;
		int slot = Math.min( Math.min( slotByW, slotByH ), 28 );
		slot = Math.max( slot, 16 );

		slotWidth = slotHeight = slot;

		nCols = Math.min( TARGET_COLS,
				Math.max( 4, (availW + SLOT_MARGIN) / (slot + SLOT_MARGIN) ) );

		// 行数按「装备 6 格 + 背包 30 格」算：7 列时正好 6 行铺满
		int capacityTotal = (bag != null ? bag.capacity() : 30) + 6;
		nRows = (int)Math.ceil(capacityTotal/(float)nCols);

		int windowWidth = slotWidth * nCols + SLOT_MARGIN * (nCols - 1);
		// 可见行数就是 6 行（放不下时按可用高度往下减），多出来的格子靠右侧滚动条看
		int maxVisibleRows = Math.max(3,
				Math.min(TARGET_ROWS, (availH + SLOT_MARGIN) / (slot + SLOT_MARGIN)));
		int visibleRows = Math.min(nRows, maxVisibleRows);
		int windowHeight = TITLE_HEIGHT + slotHeight * visibleRows + SLOT_MARGIN * (visibleRows - 1);

		placeTitle( bag, windowWidth );

		content = new Component();
		placeItems( bag );

		// 可见区域只放得下 visibleRows 行，内容按容量铺满，超出部分靠右侧滚动条
		int visibleHeight = slotHeight * visibleRows + SLOT_MARGIN * (visibleRows - 1);
		// 内容高度按“真正铺了几行”算，而不是按背包容量算：
		// 容量固定会让内容永远比视口高，缩略条就永远只有一小截、填不满。
		int contentRows = Math.max(visibleRows, row + 1);
		content.setSize(windowWidth, contentRows * (slotHeight + SLOT_MARGIN) - SLOT_MARGIN);
		scroll = new ScrollPane(content);
		scrollHeight = visibleHeight;
		add(scroll);
		// 立刻给视口设定尺寸，不等 update()（那时窗口可能还没挂到场景，camera() 为 null）
		scroll.setRect(0, TITLE_HEIGHT, windowWidth, visibleHeight);
		scrollSized = true;

		resize( windowWidth, windowHeight );

		int i = 1;
		for (Bag b : Dungeon.hero.belongings.getBags()) {
			if (b != null) {
				BagTab tab = new BagTab( b, i++ );
				add( tab );
				tab.select( b == bag );
			}
		}

		layoutTabs();
	}
	
	public static WndBag lastBag( ItemSelector selector ) {
		
		if (lastBag != null && Dungeon.hero.belongings.backpack.contains( lastBag )) {
			
			return new WndBag( lastBag, selector );
			
		} else {
			
			return new WndBag( Dungeon.hero.belongings.backpack, selector );
			
		}
	}

	public static WndBag getBag( ItemSelector selector ) {
		if (selector.preferredBag() == Belongings.Backpack.class){
			return new WndBag( Dungeon.hero.belongings.backpack, selector );

		} else if (selector.preferredBag() != null){
			Bag bag = Dungeon.hero.belongings.getItem( selector.preferredBag() );
			if (bag != null)    return new WndBag( bag, selector );
			//if a specific preferred bag isn't present, then the relevant items will be in backpack
			else                return new WndBag( Dungeon.hero.belongings.backpack, selector );
		}

		return lastBag( selector );
	}
	
	protected void placeTitle( Bag bag, int width ){

		float titleWidth;
		if (Dungeon.energy == 0) {
			ItemSprite gold = new ItemSprite(ItemSpriteSheet.GOLD, null);
			gold.x = width - gold.width();
			gold.y = (TITLE_HEIGHT - gold.height()) / 2f;
			PixelScene.align(gold);
			add(gold);

			BitmapText amt = new BitmapText(Integer.toString(Dungeon.gold), PixelScene.pixelFont);
			amt.hardlight(TITLE_COLOR);
			amt.measure();
			amt.x = width - gold.width() - amt.width() - 1;
			amt.y = (TITLE_HEIGHT - amt.baseLine()) / 2f - 1;
			PixelScene.align(amt);
			add(amt);

			titleWidth = amt.x;
		} else {

			Image gold = Icons.get(Icons.COIN_SML);
			gold.x = width - gold.width() - 0.5f;
			gold.y = 0;
			PixelScene.align(gold);
			add(gold);

			BitmapText amt = new BitmapText(Integer.toString(Dungeon.gold), PixelScene.pixelFont);
			amt.hardlight(TITLE_COLOR);
			amt.measure();
			amt.x = width - gold.width() - amt.width() - 2f;
			amt.y = 0;
			PixelScene.align(amt);
			add(amt);

			titleWidth = amt.x;

			Image energy = Icons.get(Icons.ENERGY_SML);
			energy.x = width - energy.width();
			energy.y = gold.height();
			PixelScene.align(energy);
			add(energy);

			amt = new BitmapText(Integer.toString(Dungeon.energy), PixelScene.pixelFont);
			amt.hardlight(0x44CCFF);
			amt.measure();
			amt.x = width - energy.width() - amt.width() - 1;
			amt.y = energy.y;
			PixelScene.align(amt);
			add(amt);

			titleWidth = Math.min(titleWidth, amt.x);
		}

		String title = selector != null ? selector.textPrompt() : null;
		RenderedTextBlock txtTitle = PixelScene.renderTextBlock(
				UITheme.isDiceMage() ? "[DICE BAG] " + (title != null ? Messages.titleCase(title) : Messages.titleCase( bag.name() ))
						: title != null ? Messages.titleCase(title) : Messages.titleCase( bag.name() ), 8 );
		txtTitle.hardlight( UITheme.isDiceMage() ? DiceMageUI.GOLD : TITLE_COLOR );
		txtTitle.maxWidth( (int)titleWidth - 2 );
		txtTitle.setPos(
				1,
				(TITLE_HEIGHT - txtTitle.height()) / 2f - 1
		);
		PixelScene.align(txtTitle);
		add( txtTitle );
	}
	
	@Override
	public void update() {
		super.update();
		// 兜底：万一还没设过尺寸（例如构造时被跳过），这里补一次
		if (!scrollSized && scroll != null) {
			scroll.setRect(0, TITLE_HEIGHT, width, scrollHeight);
			scrollSized = true;
		}
	}

	protected void placeItems( Bag container ) {
		
		// Equipped items
		Belongings stuff = Dungeon.hero.belongings;
		placeItem( stuff.weapon != null ? stuff.weapon : new Placeholder( ItemSpriteSheet.WEAPON_HOLDER ) );
		placeItem( stuff.armor != null ? stuff.armor : new Placeholder( ItemSpriteSheet.ARMOR_HOLDER ) );
		placeItem( stuff.secondaryArmor() != null ? stuff.secondaryArmor() : new Placeholder( "secondary_slot" ) );
		placeItem( stuff.artifact != null ? stuff.artifact : new Placeholder( ItemSpriteSheet.ARTIFACT_HOLDER ) );
		placeItem( stuff.misc != null ? stuff.misc : new Placeholder( ItemSpriteSheet.SOMETHING ) );
		placeItem( stuff.ring != null ? stuff.ring : new Placeholder( ItemSpriteSheet.RING_HOLDER ) );

		int equipped = 6;

		//the container itself if it's not the root backpack
		if (container != Dungeon.hero.belongings.backpack){
			placeItem(container);
			count--; //don't count this one, as it's not actually inside of itself
		} else if (stuff.secondWep != null) {
			//second weapon always goes to the front of view on main bag
			placeItem(stuff.secondWep);
			equipped++;
		}

		// Items in the bag, except other containers (they have tags at the bottom)
		for (Item item : container.items.toArray(new Item[0])) {
			if (!(item instanceof Bag)) {
				placeItem( item );
			} else {
				count++;
			}
		}
		
		// Free Space
		while ((count - equipped) < container.capacity()) {
			placeItem( null );
		}
	}

	protected void placeItem( final Item item ) {

		count++;

		int x = col * (slotWidth + SLOT_MARGIN);
		int y = row * (slotHeight + SLOT_MARGIN);

		InventorySlot slot = new InventorySlot( item ){
			{
				// InventorySlot 的 PointerArea 默认 BLOCK_WHEN_ACTIVE，会吞掉 DOWN 事件，
				// 手指按在格子上时 ScrollPane 收不到按下，整个列表就拖不动。
				// 这里只解开“阻断”，点击/长按/右键都照旧。
				hotArea.blockLevel = PointerArea.NEVER_BLOCK;
			}

			@Override
			protected void onClick() {
				if (lastBag != item && !lastBag.contains(item) && !item.isEquipped(Dungeon.hero)){

					hide();

				} else if (selector != null) {

					if (selector.hideAfterSelecting()){
						hide();
					}
					selector.onSelect( item );

				} else {

					Game.scene().addToFront(new WndUseItem( WndBag.this, item ) );

				}
			}

			@Override
			protected void onRightClick() {
				if (lastBag != item && !lastBag.contains(item) && !item.isEquipped(Dungeon.hero)){

					hide();

				} else if (selector != null) {

					if (selector.hideAfterSelecting()){
						hide();
					}
					selector.onSelect( item );

				} else {

					RightClickMenu r = new RightClickMenu(item){
						@Override
						public void onSelect(int index) {
							WndBag.this.hide();
						}
					};
					parent.addToFront(r);
					r.camera = camera();
					PointF mousePos = PointerEvent.currentHoverPos();
					mousePos = camera.screenToCamera((int)mousePos.x, (int)mousePos.y);
					r.setPos(mousePos.x-3, mousePos.y-3);

				}
			}

			@Override
			protected boolean onLongClick() {
				if (selector == null && item.defaultAction() != null) {
					hide();
					QuickSlotButton.set( item );
					return true;
				} else if (selector != null) {
					Game.scene().addToFront(new WndInfoItem(item));
					return true;
				} else {
					return false;
				}
			}
		};
		slot.setRect( x, y, slotWidth, slotHeight );
		content.add(slot);

		if (item == null || (selector != null && !selector.itemSelectable(item))){
			slot.enable(false);
		}

		if (++col >= nCols) {
			col = 0;
			row++;
		}

	}

	@Override
	public boolean onSignal(KeyEvent event) {
		if (event.pressed && KeyBindings.getActionForKey( event ) == SPDAction.INVENTORY) {
			onBackPressed();
			return true;
		} else {
			return super.onSignal(event);
		}
	}
	
	@Override
	public void onBackPressed() {
		if (selector != null) {
			selector.onSelect( null );
		}
		super.onBackPressed();
	}
	
	@Override
	protected void onClick( Tab tab ) {
		hide();
		Window w = new WndBag(((BagTab) tab).bag, selector);
		if (Game.scene() instanceof GameScene){
			GameScene.show(w);
		} else {
			Game.scene().addToFront(w);
		}
	}
	
	@Override
	public void hide() {
		super.hide();
		if (INSTANCE == this){
			INSTANCE = null;
		}
	}
	
	@Override
	protected int tabHeight() {
		return 20;
	}
	
	private Image icon( Bag bag ) {
		if (bag instanceof VelvetPouch) {
			return Icons.get( Icons.SEED_POUCH );
		} else if (bag instanceof ScrollHolder) {
			return Icons.get( Icons.SCROLL_HOLDER );
		} else if (bag instanceof MagicalHolster) {
			return Icons.get( Icons.WAND_HOLSTER );
		} else if (bag instanceof PotionBandolier) {
			return Icons.get( Icons.POTION_BANDOLIER );
		} else {
			return Icons.get( Icons.BACKPACK );
		}
	}
	
	private class BagTab extends IconTab {

		private Bag bag;
		private int index;
		
		public BagTab( Bag bag, int index ) {
			super( icon(bag) );
			
			this.bag = bag;
			this.index = index;
		}

		@Override
		public GameAction keyAction() {
			switch (index){
				case 1:
					return SPDAction.BAG_1;
				case 2:
					return SPDAction.BAG_2;
				case 3:
					return SPDAction.BAG_3;
				case 4:
					return SPDAction.BAG_4;
				case 5:
					return SPDAction.BAG_5;
				case 6:
					return SPDAction.BAG_6;
				case 7:
					return SPDAction.BAG_7;
				case 8:
					return SPDAction.BAG_8;
				default:
					// 超过 8 个的背包没有独立快捷键
					return null;
			}
		}

		@Override
		protected String hoverText() {
			return Messages.titleCase(bag.name());
		}
	}
	
	public static class Placeholder extends Item {

		public Placeholder(String image ) {
			this.image = image;
		}

		@Override
		public String name() {
			return null;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}
		
		@Override
		public boolean isEquipped( Hero hero ) {
			return true;
		}
	}

	public abstract static class ItemSelector {
		public abstract String textPrompt();
		public Class<?extends Bag> preferredBag(){
			return null; //defaults to last bag opened
		}
		public abstract boolean itemSelectable( Item item );
		public abstract void onSelect( Item item );
		public boolean hideAfterSelecting(){
			return true; //defaults to hiding the window when an item is picked
		}
	}
}
