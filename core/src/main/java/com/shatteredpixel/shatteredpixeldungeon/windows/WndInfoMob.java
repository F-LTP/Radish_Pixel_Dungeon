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

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.HealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

public class WndInfoMob extends Window {

	private static final int WIDTH_MIN    = 120;
	private static final int WIDTH_MAX    = 220;
	private static final int GAP	= 2;
	private static final int DICE_WIDTH_P = 150;
	private static final int DICE_WIDTH_L = 170;
	private static final int DICE_PAD = 3;
	private static final int DICE_PORTRAIT = 38;
	
	public WndInfoMob( Mob mob ) {
		if (DiceMageUI.active()) {
			layoutDiceMob(mob);
		} else {
			layoutDefault(mob);
		}
	}

	private void layoutDefault(Mob mob) {
		int width = WIDTH_MIN;
		Component titlebar = new MobTitle(mob);
		titlebar.setRect( 0, 0, width, 0 );
		add(titlebar);

		RenderedTextBlock text = PixelScene.renderTextBlock( 6 );
		text.text( mob.info(), width );
		text.setPos( titlebar.left(), titlebar.bottom() + 2*GAP );
		add( text );

		while (PixelScene.landscape()
				&& text.bottom() > (PixelScene.MIN_HEIGHT_L - 10)
				&& width < WIDTH_MAX){
			width += 20;
			titlebar.setRect(0, 0, width, 0);
			text.setPos( titlebar.left(), titlebar.bottom() + 2*GAP );
			text.maxWidth(width);
		}

		bringToFront(titlebar);
		resize( width, (int)text.bottom() + 2 );
	}

	private void layoutDiceMob(Mob mob) {
		int width = PixelScene.landscape() ? DICE_WIDTH_L : DICE_WIDTH_P;
		chrome.hardlight(DiceMageUI.DARK);

		int lineColor = mob.properties().contains(com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.BOSS)
				? DiceMageUI.RED : DiceMageUI.PURPLE;
		DiceMageUI.Frame portraitFrame = new DiceMageUI.Frame(DiceMageUI.BLACK, lineColor);
		portraitFrame.setRect(0, 0, DICE_PORTRAIT, DICE_PORTRAIT);
		add(portraitFrame);

		CharSprite image = mob.sprite();
		image.x = (DICE_PORTRAIT - image.width()) / 2f;
		image.y = (DICE_PORTRAIT - image.height()) / 2f;
		PixelScene.align(image);
		add(image);

		DiceMageUI.Frame titleFrame = new DiceMageUI.Frame(DiceMageUI.PANEL_ALT, lineColor);
		titleFrame.setRect(DICE_PORTRAIT + DICE_PAD, 0, width - DICE_PORTRAIT - DICE_PAD, DICE_PORTRAIT);
		add(titleFrame);

		RenderedTextBlock name = PixelScene.renderTextBlock("[MONSTER] " + Messages.titleCase(mob.name()), 8);
		name.hardlight(DiceMageUI.GOLD);
		name.maxWidth(width - DICE_PORTRAIT - DICE_PAD * 3);
		name.setPos(DICE_PORTRAIT + DICE_PAD * 2, DICE_PAD);
		add(name);

		DiceMageUI.HealthPips health = new DiceMageUI.HealthPips();
		int healthPips = DiceMageUI.pipCount(mob.HT);
		health.setRect(name.left(), name.bottom() + 1,
				DiceMageUI.pipWidth(healthPips), DiceMageUI.pipHeight(healthPips));
		health.level(mob);
		add(health);

		BuffIndicator buffs = new BuffIndicator(mob, false);
		buffs.setSize(width - DICE_PORTRAIT - DICE_PAD * 3, 8);
		buffs.setPos(name.left(), health.bottom() + 1);
		add(buffs);

		RenderedTextBlock info = PixelScene.renderTextBlock(mob.info(), 6);
		info.hardlight(DiceMageUI.CREAM);
		info.maxWidth(width - DICE_PAD * 4);
		info.setPos(DICE_PAD * 2, DICE_PORTRAIT + DICE_PAD * 4 + 8);

		RenderedTextBlock section = PixelScene.renderTextBlock("[INFO]", 8);
		section.hardlight(DiceMageUI.GOLD);
		section.setPos(DICE_PAD * 2, DICE_PORTRAIT + DICE_PAD * 2);
		add(section);

		DiceMageUI.Frame body = new DiceMageUI.Frame(DiceMageUI.PANEL, lineColor);
		body.setRect(0, DICE_PORTRAIT + DICE_PAD, width, info.height() + DICE_PAD * 6 + 8);
		add(body);
		bringToFront(section);
		add(info);

		resize(width, (int)(body.bottom() + DICE_PAD));
	}
	
	private static class MobTitle extends Component {

		private static final int GAP	= 2;
		
		private CharSprite image;
		private RenderedTextBlock name;
		private HealthBar health;
		private BuffIndicator buffs;
		
		public MobTitle( Mob mob ) {
			
			name = PixelScene.renderTextBlock( Messages.titleCase( mob.name() ), 9 );
			name.hardlight( TITLE_COLOR );
			add( name );
			
			image = mob.sprite();
			add( image );

			health = new HealthBar();
			health.level(mob);
			add( health );

			buffs = new BuffIndicator( mob, false );
			add( buffs );
		}
		
		@Override
		protected void layout() {
			
			image.x = 0;
			image.y = Math.max( 0, name.height() + health.height() - image.height() );

			float w = width - image.width() - GAP;
			int extraBuffSpace = 0;

			//Tries to make space for up to 11 visible buffs
			do {
				name.maxWidth((int)w - extraBuffSpace);
				buffs.setSize(w - name.width() - 8, 8);
				extraBuffSpace += 8;
			} while (extraBuffSpace <= 40 && !buffs.allBuffsVisible());

			name.setPos(x + image.width() + GAP,
					image.height() > name.height() ? y +(image.height() - name.height()) / 2 : y);

			health.setRect(image.width() + GAP, name.bottom() + GAP, w, health.height());

			buffs.setPos(name.right(), name.bottom() - BuffIndicator.SIZE_SMALL-2);

			height = Math.max(image.y + image.height(), health.bottom());
		}
	}
}
