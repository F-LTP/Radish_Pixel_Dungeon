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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WndInfoSubclass extends WndTitledMessage {

	public WndInfoSubclass(HeroClass cls, HeroSubClass subCls){
		super( new HeroIcon(subCls), Messages.titleCase(subCls.title()), subCls.desc());

		ArrayList<LinkedHashMap<Talent, Integer>> talentList = new ArrayList<>();
		Talent.initClassTalents(cls, talentList);
		Talent.initSubclassTalents(subCls, talentList);
		Talent.initT4Talents(cls,subCls,talentList);

		//骰子法师的第三层不是固定天赋而是各个学派，这里用一段文字说明。
		//原先靠「第三层为空」来判断，但 initSubclassTalents 清空之后又把学派填了进去，这个条件永远不成立，
		//结果显示成一排学派图标（连内部用的占位项也在里面）
		if (subCls == HeroSubClasses.DICE_MAGE || talentList.get(2).isEmpty()) {
			RenderedTextBlock explanation = PixelScene.renderTextBlock(Messages.get(this, "dice_mage_talents"), 6);
			explanation.maxWidth(width);
			explanation.setPos(0, height + 5);
			add(explanation);
			resize(width, (int) explanation.bottom());
		} else {
			TalentsPane.TalentTierPane talentPane = new TalentsPane.TalentTierPane(talentList.get(2), 3, TalentButton.Mode.INFO);
			talentPane.title.text( Messages.titleCase(Messages.get(WndHeroInfo.class, "talents")));
			talentPane.setRect(0, height + 5, width, talentPane.height());
			add(talentPane);
			resize(width, (int) talentPane.bottom());
		}

	}

}
