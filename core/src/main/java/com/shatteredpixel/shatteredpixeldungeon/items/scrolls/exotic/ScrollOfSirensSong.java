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

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class ScrollOfSirensSong extends ExoticScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_SIREN;
	}
	
	@Override
	public void doRead() {
		if (!anonymous) curItem.collect(); //we detach it later
		GameScene.selectCell(targeter);
	}

	@Override
	public void MagicStone(boolean log,boolean original){
		//特殊卷轴 特殊处理
	}

	private CellSelector.Listener targeter = new CellSelector.Listener() {

		@Override
		public void onSelect(Integer cell) {
			if (cell == null && isKnown()){
				return;
			}

			Mob target = null;
			if (cell != null){
				Char ch = Actor.findChar(cell);
				if (ch != null && ch.alignment != Char.Alignment.ALLY && ch instanceof Mob){
					target = (Mob)ch;
				}
			}

			if (target == null && isKnown() && !anonymous){
				GLog.w(Messages.get(ScrollOfSirensSong.class, "cancel"));
				return;

			} else {

				detach(curUser.belongings.backpack);

				curUser.sprite.centerEmitter().start( Speck.factory( Speck.HEART ), 0.2f, 5 );
				Sample.INSTANCE.play( Assets.Sounds.CHARMS );
				Sample.INSTANCE.playDelayed( Assets.Sounds.LULLABY, 0.1f );

				for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
					if (Dungeon.level.heroFOV[mob.pos] && mob != target && mob.alignment != Char.Alignment.ALLY) {
						Buff.affect( mob, Charm.class, Charm.DURATION ).object = curUser.id();
						mob.sprite.centerEmitter().start( Speck.factory( Speck.HEART ), 0.2f, 5 );
					}
				}

				if (target != null){
					if (!target.isImmune(Enthralled.class)){
						AllyBuff.affectAndLoot(target, curUser, Enthralled.class);

					} else {
						Buff.affect( target, Charm.class, Charm.DURATION ).object = curUser.id();

					}
					target.sprite.centerEmitter().burst( Speck.factory( Speck.HEART ), 10 );

					/**MAGIC_REFINING +2 Talent Special Method*/
					Scroll recoveredScroll = new ScrollOfLullaby();
					if(Random.Int(4)==0 && Dungeon.hero.pointsInTalent(Talent.MAGIC_REFINING) >= 2){
						Dungeon.level.drop(recoveredScroll, curUser.pos);
						GLog.p(Messages.get(Scroll.class, "exscrollToscroll", recoveredScroll.name()));
					}

				} else {
					GLog.w(Messages.get(ScrollOfSirensSong.class, "no_target"));
				}

				identify();

				readAnimation(true);

			}
		}

		@Override
		public String prompt() {
			return Messages.get(ScrollOfSirensSong.class, "prompt");
		}

	};

	public static class Enthralled extends AllyBuff {

		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		@Override
		public void fx(boolean on) {
			if (on) target.sprite.add(CharSprite.State.HEARTS);
			else    target.sprite.remove(CharSprite.State.HEARTS);
		}

		@Override
		public int icon() {
			return BuffIndicator.HEART;
		}
	}
	
}
