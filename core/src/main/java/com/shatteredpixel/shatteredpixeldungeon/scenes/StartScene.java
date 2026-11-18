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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndGameInProgress;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;

import java.util.ArrayList;

public class StartScene extends PixelScene {
	
	private static final int SLOT_WIDTH = 120;
	private static final int SLOT_HEIGHT = 30;
	private static final int CLEAR_SAVES_HEIGHT = 18;
	private static final int CLEAR_SAVES_MARGIN = 6;
	
	@Override
	public void create() {
		super.create();
		
		Badges.loadGlobal();
		Journal.loadGlobal();
		
		uiCamera.visible = false;
		
		int w = Camera.main.width;
		int h = Camera.main.height;
		
		Archs archs = new Archs();
		archs.setSize( w, h );
		add( archs );
		
		ExitButton btnExit = new ExitButton();
		btnExit.setPos( w - btnExit.width(), 0 );
		add( btnExit );
		
		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 9);
		title.hardlight(Window.TITLE_COLOR);
		title.setPos(
				(w - title.width()) / 2f,
				(20 - title.height()) / 2f
		);
		align(title);
		add(title);
		
		ArrayList<GamesInProgress.Info> games = GamesInProgress.checkAll();
		
		int slotCount = Math.min(GamesInProgress.MAX_SLOTS, games.size()+1);
		int slotGap = 10 - slotCount;
		int slotsHeight = slotCount*SLOT_HEIGHT + (slotCount-1)* slotGap;

		//[改动] 有存档时要给下方“清档”按钮留出一行，避免列表把按钮挤到底边重叠；slotGap 加下限防止死循环
		int reservedSpace = games.isEmpty() ? 0 : CLEAR_SAVES_HEIGHT + CLEAR_SAVES_MARGIN;

		while (slotsHeight > (h-title.bottom()-2-reservedSpace) && slotGap > -SLOT_HEIGHT){
			slotGap--;
			slotsHeight -= slotCount-1;
		}
		
		//居中时也要把给清档按钮留的那一行扣掉：上面的循环是按「扣掉之后的高度」压缩列表的，
		//这里不扣的话留出来的空间上下各分一半，按钮仍然会压在最后一个槽位上
		float yPos = (h - reservedSpace - slotsHeight + title.bottom() + 2)/2f;
		
		for (GamesInProgress.Info game : games) {
			SaveSlotButton existingGame = new SaveSlotButton();
			existingGame.set(game.slot);
			existingGame.setRect((w - SLOT_WIDTH) / 2f, yPos, SLOT_WIDTH, SLOT_HEIGHT);
			yPos += SLOT_HEIGHT + slotGap;
			align(existingGame);
			add(existingGame);
			
		}
		
		if (games.size() < GamesInProgress.MAX_SLOTS){
			SaveSlotButton newGame = new SaveSlotButton();
			newGame.set(GamesInProgress.firstEmpty());
			newGame.setRect((w - SLOT_WIDTH) / 2f, yPos, SLOT_WIDTH, SLOT_HEIGHT);
			yPos += SLOT_HEIGHT + slotGap;
			align(newGame);
			add(newGame);
		}
		
		//[新增] 存档列表下方：一键清除全部存档（删完重建本界面，列表随之清空）
		if (!games.isEmpty()) {
			RedButton clearSaves = new RedButton( Messages.get(this, "clear_saves_short"), 8 ) {
				@Override
				protected void onClick() {
					confirmClearSaves();
				}

				@Override
				protected String hoverText() {
					return Messages.get(StartScene.class, "clear_saves_title");
				}
			};
			//yPos 此时是「最后一个槽位的底边 + slotGap」，列表被压缩时 slotGap 是负数，要先加回去
			clearSaves.setRect( (w - SLOT_WIDTH) / 2f,
					Math.min(yPos - Math.min(slotGap, 0) + CLEAR_SAVES_MARGIN, h - CLEAR_SAVES_HEIGHT - 2),
					SLOT_WIDTH, CLEAR_SAVES_HEIGHT );
			align(clearSaves);
			add(clearSaves);
		}
		GamesInProgress.curSlot = 0;
		
		fadeIn();
		
	}
	
	/**
	 * 一键清除全部存档。
	 * 先数一遍进行中的存档，再弹确认框；确认后逐个走 Dungeon.deleteGame(slot, true)，
	 * 与“放弃游戏”删档同一条路径（连同该槽位的 depth 层文件一起删），不会留下半截存档。
	 * 删完重建本界面，存档列表随之清空。
	 */
	private void confirmClearSaves() {
		int count = 0;
		for (int slot = 1; slot <= GamesInProgress.MAX_SLOTS; slot++) {
			if (GamesInProgress.gameExists(slot)) count++;
		}

		if (count == 0) {
			add(new WndMessage(Messages.get(this, "clear_saves_none")));
			return;
		}

		add(new WndOptions(Icons.get(Icons.WARNING),
				Messages.get(this, "clear_saves_title"),
				Messages.get(this, "clear_saves_desc", count),
				Messages.get(this, "clear_saves_confirm"),
				Messages.get(this, "clear_saves_cancel")) {
			@Override
			protected void onSelect(int index) {
				if (index != 0) return;

				for (int slot = 1; slot <= GamesInProgress.MAX_SLOTS; slot++) {
					if (!GamesInProgress.gameExists(slot)) continue;
					GamesInProgress.setUnknown(slot);
					Dungeon.deleteGame(slot, true);
				}
				ShatteredPixelDungeon.switchNoFade(StartScene.class);
			}
		});
	}

	@Override
	protected void onBackPressed() {
		ShatteredPixelDungeon.switchNoFade( TitleScene.class );
	}
	
	private static class SaveSlotButton extends Button {
		
		private NinePatch bg;
		
		private Image hero;
		private RenderedTextBlock name;
		
		private Image steps;
		private BitmapText depth;
		private Image classIcon;
		private BitmapText level;
		
		private int slot;
		private boolean newGame;
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			bg = Chrome.get(Chrome.Type.GEM);
			add( bg);
			
			name = PixelScene.renderTextBlock(9);
			add(name);
		}
		
		public void set( int slot ){
			this.slot = slot;
			GamesInProgress.Info info = GamesInProgress.check(slot);
			newGame = info == null;
			if (newGame){
				name.text( Messages.get(StartScene.class, "new"));
				
				if (hero != null){
					remove(hero);
					hero = null;
					remove(steps);
					steps = null;
					remove(depth);
					depth = null;
					remove(classIcon);
					classIcon = null;
					remove(level);
					level = null;
				}
			} else {
				
				if (info.skin > 0 && info.heroClass.skin(info.skin) != null){
					name.text(Messages.titleCase(info.heroClass.title(info.skin)));
				} else if (info.subClass != HeroSubClasses.NONE){
					name.text(Messages.titleCase(info.subClass.title()));
				} else {
					name.text(Messages.titleCase(info.heroClass.title(info.skin)));
				}
				
				if (hero == null){
					hero = HeroSprite.body(info.heroClass, info.armorTier, info.skin);
					add(hero);
					
					steps = new Image(Icons.get(Icons.STAIRS));
					add(steps);
					depth = new BitmapText(PixelScene.pixelFont);
					add(depth);
					
					classIcon = new Image(Icons.get(info.heroClass));
					add(classIcon);
					level = new BitmapText(PixelScene.pixelFont);
					add(level);
				} else {
					hero.copy(HeroSprite.body(info.heroClass, info.armorTier, info.skin));
					
					classIcon.copy(Icons.get(info.heroClass));
				}
				
				depth.text(Integer.toString(info.depth));
				depth.measure();
				
				level.text(Integer.toString(info.level));
				level.measure();
				
				if (info.challenges > 0){
					name.hardlight(Window.TITLE_COLOR);
					depth.hardlight(Window.TITLE_COLOR);
					level.hardlight(Window.TITLE_COLOR);
				} else {
					name.resetColor();
					depth.resetColor();
					level.resetColor();
				}

				if (info.daily){
					if (info.dailyReplay){
						steps.hardlight(1f, 0.5f, 2f);
					} else {
						steps.hardlight(0.5f, 1f, 2f);
					}
				} else if (!info.customSeed.isEmpty()){
					steps.hardlight(1f, 1.5f, 0.67f);
				}
				
			}
			
			layout();
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			bg.x = x;
			bg.y = y;
			bg.size( width, height );
			
			if (hero != null){
				hero.x = x+8;
				hero.y = y + (height - hero.height())/2f;
				align(hero);
				
				name.setPos(
						hero.x + hero.width() + 6,
						y + (height - name.height())/2f
				);
				align(name);
				
				classIcon.x = x + width - 24 + (16 - classIcon.width())/2f;
				classIcon.y = y + (height - classIcon.height())/2f;
				align(classIcon);
				
				level.x = classIcon.x + (classIcon.width() - level.width()) / 2f;
				level.y = classIcon.y + (classIcon.height() - level.height()) / 2f + 1;
				align(level);
				
				steps.x = x + width - 40 + (16 - steps.width())/2f;
				steps.y = y + (height - steps.height())/2f;
				align(steps);
				
				depth.x = steps.x + (steps.width() - depth.width()) / 2f;
				depth.y = steps.y + (steps.height() - depth.height()) / 2f + 1;
				align(depth);
				
			} else {
				name.setPos(
						x + (width - name.width())/2f,
						y + (height - name.height())/2f
				);
				align(name);
			}
			
			
		}
		
		@Override
		protected void onClick() {
			if (newGame) {
				GamesInProgress.selectedClass = null;
				GamesInProgress.curSlot = slot;
				ShatteredPixelDungeon.switchScene(HeroSelectScene.class);
			} else {
				ShatteredPixelDungeon.scene().add( new WndGameInProgress(slot));
			}
		}
	}
}
