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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.SNDItems;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.utils.Callback;

public class WndInfoTalent extends Window {

	private static final float GAP	= 2;

	private static final int WIDTH = 120;

	/** 学派天赋显示"即将解锁"法术的 SND 图标；非学派或找不到时返回 null。 */
	private static Image schoolIcon(Talent talent, int points) {
		if (!Talent.isDiceMageSpellTalent(talent)) return null;
		String sndName = DiceMageSchools.nextSpellSndName(talent, points);
		if (sndName == null) return null;
		Image img = SNDItems.get(sndName);
		if (img == null) return null;
		// 将 SND 贴图放大到与天赋图标(16px)相同的显示尺寸
		float s = 16f / img.width();
		img.scale.set(s);
		return img;
	}

	public WndInfoTalent(Talent talent, int points, TalentButtonCallback buttonCallback){
		super();

		IconTitle titlebar = new IconTitle();

		Image icon = schoolIcon(talent, points);
		titlebar.icon( icon != null ? icon : new TalentIcon( talent ) );
		String title = Messages.titleCase(talent.title());
		if (points > 0){
			title += " +" + points;
		}
		titlebar.label( title, Window.TITLE_COLOR );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		boolean metaDesc = (buttonCallback != null && buttonCallback.metamorphDesc()) ||
				(Dungeon.hero != null && Dungeon.hero.metamorphedTalents.containsValue(talent));

		RenderedTextBlock txtInfo = PixelScene.renderTextBlock(talent.desc(metaDesc), 6);
		txtInfo.maxWidth(WIDTH);
		txtInfo.setPos(titlebar.left(), titlebar.bottom() + 2*GAP);
		add( txtInfo );
		resize( WIDTH, (int)(txtInfo.bottom() + GAP) );

		if (buttonCallback != null) {
			RedButton button = new RedButton( buttonCallback.prompt() ) {
				@Override
				protected void onClick() {
					super.onClick();
					hide();
					buttonCallback.call();
				}
			};
			button.icon(Icons.get(Icons.TALENT));
			button.setRect(0, txtInfo.bottom() + 2*GAP, WIDTH, 18);
			add(button);

			//TODO 暂时禁用
			if(talent == Talent.GHOST_ROOT){
				button.enable(false);
				button.alpha(0.1f);
			}

			resize( WIDTH, (int)button.bottom()+1 );
		}

	}

	public static abstract class TalentButtonCallback implements Callback {

		public abstract String prompt();

		public boolean metamorphDesc(){
			return false;
		}

	}

}
