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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TalentsPane extends ScrollPane {

	ArrayList<TalentTierPane> panes = new ArrayList<>();
	ArrayList<ColorBlock> separators = new ArrayList<>();

	ColorBlock sep;
	ColorBlock blocker;
	RenderedTextBlock blockText;

	public TalentsPane( TalentButton.Mode mode ) {
		this( mode, Dungeon.hero.talents );
	}

	public TalentsPane( TalentButton.Mode mode, ArrayList<LinkedHashMap<Talent, Integer>> talents ) {
		super(new Component());

		Ratmogrify.useRatroicEnergy = Dungeon.hero != null && Dungeon.hero.armorAbility instanceof Ratmogrify;

		int tiersAvailable = 1;

		if (mode == TalentButton.Mode.INFO){
			if (!Badges.isUnlocked(Badges.Badge.LEVEL_REACHED_1)){
				tiersAvailable = 1;
			} else if (!Badges.isUnlocked(Badges.Badge.LEVEL_REACHED_2) || !Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_2)){
				tiersAvailable = 2;
			} else if (!Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_4) ){
				tiersAvailable = 3;
			} else {
				tiersAvailable = Talent.MAX_TALENT_TIERS;
			}
		} else {
			while (tiersAvailable < Talent.MAX_TALENT_TIERS
					&& Dungeon.hero.lvl+1 >= Talent.tierLevelThresholds[tiersAvailable+1]){
				tiersAvailable++;
			}
			if (tiersAvailable > 2 && Dungeon.hero.subClass == HeroSubClasses.NONE){
				tiersAvailable = 2;
			} else if (tiersAvailable > 3 && (Dungeon.hero.armorAbility == null && !Dungeon.hero.powerOfImp)){
				tiersAvailable = 3;
			}
		}
		tiersAvailable = Math.min(tiersAvailable, talents.size());

		for (int i = 0; i < Math.min(tiersAvailable, talents.size()); i++){
			if (talents.get(i).isEmpty()) continue;

			TalentTierPane pane = new TalentTierPane(talents.get(i), i+1, mode);
			panes.add(pane);
			content.add(pane);

			ColorBlock sep = new ColorBlock(0, 1, 0xFF000000);
			separators.add(sep);
			content.add(sep);
		}

		sep = new ColorBlock(0, 1, 0xFF000000);
		content.add(sep);

		blocker = new ColorBlock(0, 0, 0xFF222222);
		content.add(blocker);

		if (tiersAvailable == 1) {
			blockText = PixelScene.renderTextBlock(Messages.get(this, "unlock_tier2"), 6);
			content.add(blockText);
		} else if (tiersAvailable == 2) {
			blockText = PixelScene.renderTextBlock(Messages.get(this, "unlock_tier3"), 6);
			content.add(blockText);
		} else if (tiersAvailable == 3) {
			blockText = PixelScene.renderTextBlock(Messages.get(this, "unlock_tier4"), 6);
			content.add(blockText);
		} else {
			blockText = null;
		}

		for (int i = panes.size()-1; i >= 0; i--){
			content.bringToFront(panes.get(i));
		}
	}

	@Override
	protected void layout() {
		super.layout();

		float top = 0;
		for (int i = 0; i < panes.size(); i++){
			top += 2;
			panes.get(i).setRect(x, top, width, 0);
			top = panes.get(i).bottom();

			separators.get(i).x = 0;
			separators.get(i).y = top + 2;
			separators.get(i).size(width, 1);

			top += 3;

		}

		float bottom;
		if (blockText != null) {
			bottom = Math.max(height, top + 20);

			blocker.x = 0;
			blocker.y = top;
			blocker.size(width, bottom - top);

			blockText.maxWidth((int) width);
			blockText.align(RenderedTextBlock.CENTER_ALIGN);
			blockText.setPos((width - blockText.width()) / 2f, blocker.y + (bottom - blocker.y - blockText.height()) / 2);
		} else {
			bottom = Math.max(height, top);

			blocker.visible = false;
		}

		content.setSize(width, bottom);
	}

	public static class TalentTierPane extends Component {

		private int tier;

		public RenderedTextBlock title;
		ArrayList<TalentButton> buttons;
		RenderedTextBlock emptyNote;

		ArrayList<Image> stars = new ArrayList<>();

		public TalentTierPane(LinkedHashMap<Talent, Integer> talents, int tier, TalentButton.Mode mode){
			super();

			this.tier = tier;

			title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(TalentsPane.class, "tier", tier)), 9);
			title.hardlight(Window.TITLE_COLOR);
			add(title);

			if (mode == TalentButton.Mode.UPGRADE) setupStars();

			// 骰子法师3T：隐藏未投入点数的学派与隐藏的跳过天赋
			boolean diceT3 = tier == 3 && Dungeon.hero != null
					&& Dungeon.hero.subClass == HeroSubClasses.DICE_MAGE;

			buttons = new ArrayList<>();
			boolean diceEmpty = diceT3;
			for (Talent talent : talents.keySet()){
				if (diceT3 && talent == Talent.D3_SKIPPED) continue;
				if (diceT3 && Talent.isDiceMageSpellTalent(talent) && talents.get(talent) == 0) continue;
				diceEmpty = false;
				TalentButton btn = new TalentButton(tier, talent, talents.get(talent), mode){
					@Override
					public void upgradeTalent() {
						super.upgradeTalent();
						if (parent != null) {
							setupStars();
							TalentTierPane.this.layout();
							//光辉灌注天赋
							if(talent == Talent.LIGHT_STEP){
								Dungeon.level.drop(new PotionOfExperience(),Dungeon.hero.pos);
							}
							// TheCatist 2026/7/30 月华剑盾骑士更新自身贴图代码
							if (talent == Talent.SWORD_SHIELD_KNIGHT) {
								((HeroSprite) Dungeon.hero.sprite).updateArmor();
							}
						}
					}
				};
				buttons.add(btn);
				add(btn);
			}

			if (diceEmpty){
				emptyNote = PixelScene.renderTextBlock(Messages.get(TalentsPane.class, "dice_empty"), 6);
				emptyNote.hardlight(DiceMageUI.CREAM);
				emptyNote.maxWidth(110);
				add(emptyNote);
			}
		}

		private void setupStars(){
			if (!stars.isEmpty()){
				for (Image im : stars){
					im.killAndErase();
				}
				stars.clear();
			}

			int totStars = Talent.tierLevelThresholds[tier+1] - Talent.tierLevelThresholds[tier] + Dungeon.hero.bonusTalentPoints(tier);
			int openStars = Dungeon.hero.talentPointsAvailable(tier);
			int usedStars = Dungeon.hero.talentPointsSpent(tier);
			for (int i = 0; i < totStars; i++){
				Image im = new Speck().image(Speck.STAR);
				stars.add(im);
				add(im);
				if (i >= openStars && i < (openStars + usedStars)){
					im.tint(0.75f, 0.75f, 0.75f, 0.9f);
				} else if (i >= (openStars + usedStars)){
					im.tint(0f, 0f, 0f, 0.9f);
				}
			}
		}

		@Override
		protected void layout() {
			super.layout();

			int regStars = Talent.tierLevelThresholds[tier+1] - Talent.tierLevelThresholds[tier];

			float titleWidth = title.width();
			titleWidth += 2 + Math.min(stars.size(), regStars)*6;
			title.setPos(x + (width - titleWidth)/2f, y);

			float left = title.right() + 2;

			float starTop = title.top();
			if (regStars < stars.size()) starTop -= 2;

			for (Image star : stars){
				star.x = left;
				star.y = starTop;
				PixelScene.align(star);
				left += 6;
				regStars--;
				if (regStars == 0){
					starTop += 6;
					left = title.right() + 2;
				}
			}

			if (emptyNote != null){
				emptyNote.setPos(x + (width - emptyNote.width())/2f, title.bottom() + 6);
				height = emptyNote.bottom() - y;
				return;
			}

			int columns = Math.min(6, buttons.size());
			float gap = (width - columns*TalentButton.WIDTH)/(columns+1);
			for (int i = 0; i < buttons.size(); i++) {
				int column = i % 6;
				int row = i / 6;
				TalentButton btn = buttons.get(i);
				left = x + gap + column * (TalentButton.WIDTH + gap);
				btn.setPos(left, title.bottom() + 4 + row * (TalentButton.HEIGHT + 2));
				PixelScene.align(btn);
			}

			height = buttons.isEmpty() ? title.height() : buttons.get(buttons.size()-1).bottom() - y;

		}

	}
}
