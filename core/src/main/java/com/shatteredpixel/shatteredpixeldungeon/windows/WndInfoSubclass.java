/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroInfo;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WndInfoSubclass extends WndTitledMessage {

	public WndInfoSubclass(HeroClass cls, HeroSubClass subCls){
		super( new HeroIcon(subCls), Messages.titleCase(subCls.title()), subCls.desc());

		ArrayList<LinkedHashMap<Talent, Integer>> talentList = new ArrayList<>();
		Talent.initClassTalents(cls, talentList);
		Talent.initSubclassTalents(subCls, talentList);
		Talent.initT4Talents(cls,subCls,talentList);

		TalentsPane.TalentTierPane T3talentPane = new TalentsPane.TalentTierPane(talentList.get(2), 3, TalentButton.Mode.INFO);
		//T3talentPane.title.text( Messages.titleCase(Messages.get(TalentsPane.class, "tier",3)));
		T3talentPane.setRect(0, height + 5, width, T3talentPane.height());
		add(T3talentPane);

		TalentsPane.TalentTierPane T4talentPane = new TalentsPane.TalentTierPane(talentList.get(3), 4, TalentButton.Mode.INFO);
		//T4talentPane.title.text( Messages.titleCase(Messages.get(TalentsPane.class, "tier",4)));
		T4talentPane.setRect(0, T3talentPane.bottom() + 5, width, T4talentPane.height());
		add(T4talentPane);

		resize(width, (int) T4talentPane.bottom());

	}

}
