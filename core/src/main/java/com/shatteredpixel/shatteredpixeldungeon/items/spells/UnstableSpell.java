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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.watabou.noosa.Game;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UnstableSpell extends Spell {

	{
		image = ItemSpriteSheet.UNSTABLE_SPELL;
	}
	
	private static HashMap<Class<? extends Scroll>, Float> scrollChances = new HashMap<>();
	static{
		scrollChances.put( ScrollOfIdentify.class,      3f );
		scrollChances.put( ScrollOfRemoveCurse.class,   2f );
		scrollChances.put( ScrollOfMagicMapping.class,  2f );
		scrollChances.put( ScrollOfMirrorImage.class,   2f );
		scrollChances.put( ScrollOfRecharging.class,    2f );
		scrollChances.put( ScrollOfLullaby.class,       2f );
		scrollChances.put( ScrollOfRetribution.class,   2f );
		scrollChances.put( ScrollOfRage.class,          2f );
		scrollChances.put( ScrollOfTeleportation.class, 2f );
		scrollChances.put( ScrollOfTerror.class,        2f );
		scrollChances.put( ScrollOfTransmutation.class, 1f );
	}

	public static HashSet<Class<? extends Scroll>> nonCombatScrolls = new HashSet<>();
	static {
		nonCombatScrolls.add( ScrollOfIdentify.class );
		nonCombatScrolls.add( ScrollOfRemoveCurse.class );
		nonCombatScrolls.add( ScrollOfMagicMapping.class );
		nonCombatScrolls.add( ScrollOfRecharging.class );
		nonCombatScrolls.add( ScrollOfLullaby.class );
		nonCombatScrolls.add( ScrollOfTeleportation.class );
		nonCombatScrolls.add( ScrollOfTransmutation.class );
	}

	public static HashSet<Class<? extends Scroll>> combatScrolls = new HashSet<>();
	static {
		combatScrolls.add( ScrollOfMirrorImage.class );
		combatScrolls.add( ScrollOfRecharging.class );
		combatScrolls.add( ScrollOfLullaby.class );
		combatScrolls.add( ScrollOfRetribution.class );
		combatScrolls.add( ScrollOfRage.class );
		combatScrolls.add( ScrollOfTeleportation.class );
		combatScrolls.add( ScrollOfTerror.class );
	}
	
	@Override
	protected void onCast(Hero hero) {
		
		detach( curUser.belongings.backpack );
		updateQuickslot();
		
		Scroll s = Reflection.newInstance(Random.chances(scrollChances));

		boolean enemy = hero.visibleEnemies() != 0;

		//reroll the scroll once if there is an enemy and it is a non-combat scroll
		// or if there is no enemy and it is a combat scroll
		if (enemy && nonCombatScrolls.contains(s.getClass())){
			s = Reflection.newInstance(Random.chances(scrollChances));
		} else if (!enemy && combatScrolls.contains(s.getClass())){
			s = Reflection.newInstance(Random.chances(scrollChances));
		}

		if (Dungeon.hero.pointsInTalent(Talent.MAGIC_REFINING) >= 3){
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndThreeScroll());
				}
			});
		} else {
			s.anonymize();
			curItem = s;
			s.doRead();
		}

		if (Random.Float() < talentChance){
			Talent.onScrollUsed(curUser, curUser.pos, talentFactor);
		}
	}



	//lower values, as it's cheaper to make
	@Override
	public int value() {
		return 40 * quantity;
	}

	@Override
	public int energyVal() {
		return 8 * quantity;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			boolean scroll = false;
			boolean stone = false;

			for (Item i : ingredients){
				if (i instanceof Runestone){
					stone = true;
					//if it is a regular or exotic potion
				} else if (ExoticScroll.regToExo.containsKey(i.getClass())
						|| ExoticScroll.regToExo.containsValue(i.getClass())) {
					scroll = true;
				}
			}

			return scroll && stone;
		}
		
		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 1;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {

			for (Item i : ingredients){
				i.quantity(i.quantity()-1);
			}

			return sampleOutput(null);
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return new UnstableSpell();
		}
	}


	public static class WndThreeScroll extends Window {
		private static final int WIDTH		= 120;
		private static final int BTN_SIZE	= 32;
		private static final int BTN_GAP	= 6;
		private static final int GAP		= 6;

		public static Scroll S1;
		public static Scroll S2;
		public static Scroll S3;

		public static HashMap<Class<? extends Scroll>, Float> scrollT1_Chances = new HashMap<>();
		static{
			scrollT1_Chances.put( ScrollOfIdentify.class,      3f );
			scrollT1_Chances.put( ScrollOfRemoveCurse.class,   2f );
			scrollT1_Chances.put( ScrollOfMagicMapping.class,  2f );
			scrollT1_Chances.put( ScrollOfMirrorImage.class,   2f );
		}

		public static HashMap<Class<? extends Scroll>, Float> scrollT2_Chances = new HashMap<>();
		static{
			scrollT2_Chances.put( ScrollOfRecharging.class,    2f );
			scrollT2_Chances.put( ScrollOfLullaby.class,       2f );
			scrollT2_Chances.put( ScrollOfRetribution.class,   2f );
			scrollT2_Chances.put( ScrollOfRage.class,          2f );
		}

		public static HashMap<Class<? extends Scroll>, Float> scrollT3_Chances = new HashMap<>();
		static{
			scrollT3_Chances.put( ScrollOfTeleportation.class, 2f );
			scrollT3_Chances.put( ScrollOfTerror.class,        2f );
			scrollT3_Chances.put( ScrollOfTransmutation.class, 1f );
		}

		public WndThreeScroll() {

			S1 = Reflection.newInstance(Random.chances(scrollT1_Chances));
			S2 = Reflection.newInstance(Random.chances(scrollT2_Chances));
			S3 = Reflection.newInstance(Random.chances(scrollT3_Chances));

			IconTitle titlebar = new IconTitle();
			titlebar.setRect(-3, -5, WIDTH, 0);
			UnstableSpell unstableSpell = new UnstableSpell();
			titlebar.icon(new ItemSprite(unstableSpell.image()));
			titlebar.label(Messages.get(UnstableSpell.class,"names"));
			add( titlebar );
			RenderedTextBlock message =
					PixelScene.renderTextBlock( (Messages.get(ArcaneCatalyst.class,"descx",hero.name())), 6 );
			message.maxWidth(WIDTH);
			message.setPos(0, titlebar.bottom() + GAP);
			add( message );

			WndThreeScroll.RewardButton shop1 = new WndThreeScroll.RewardButton( S1 );
			shop1.setRect( (WIDTH - BTN_GAP) / 3f - BTN_SIZE, message.top() + message.height() + BTN_GAP, BTN_SIZE,
					BTN_SIZE );
			add( shop1 );

			WndThreeScroll.RewardButton shop2 = new WndThreeScroll.RewardButton( S2 );
			shop2.setRect( shop1.right() + BTN_GAP, shop1.top(), BTN_SIZE, BTN_SIZE );
			add(shop2);

			WndThreeScroll.RewardButton shop3 = new WndThreeScroll.RewardButton( S3 );
			shop3.setRect( shop2.right() + BTN_GAP, shop2.top(), BTN_SIZE, BTN_SIZE );
			add(shop3);

			resize(WIDTH, (int) shop3.bottom());
		}

		private class RewardWindow extends WndInfoItem {

			public RewardWindow( Item item ) {
				super(item);

				StyledButton btnConfirm = new StyledButton(Chrome.Type.RED_BUTTON,Messages.get(Scroll.class, "ac_read")){
					@Override
					protected void onClick() {
						if(item == S1){
							S1.anonymize();
							curItem = S1;
							S1.doRead();
						} else if(item == S2){
							S2.anonymize();
							curItem = S2;
							S2.doRead();
						} else {
							S3.anonymize();
							curItem = S3;
							S3.doRead();
						}
						hide();
						WndThreeScroll.RewardWindow.this.hide();
						WndThreeScroll.this.hide();
					}
				};
				btnConfirm.setRect(0, height+2, width, 16);
				add(btnConfirm);

				resize(width, (int)btnConfirm.bottom());
			}
		}

		@Override
		public void onBackPressed() {
			//do nothing, reader has to cancel
		}

		public class RewardButton extends Component {

			protected NinePatch bg;
			protected ItemSlot slot;

			public RewardButton( Item item ){
				bg = Chrome.get( Chrome.Type.RED_BUTTON);
				add( bg );

				slot = new ItemSlot( item ){
					@Override
					protected void onPointerDown() {
						bg.brightness( 1.2f );
						Sample.INSTANCE.play( Assets.Sounds.CLICK );
					}
					@Override
					protected void onPointerUp() {
						bg.resetColor();
					}
					@Override
					protected void onClick() {
						ShatteredPixelDungeon.scene().addToFront(new WndThreeScroll.RewardWindow(item));
					}
				};
				add(slot);
			}

			@Override
			protected void layout() {
				super.layout();

				bg.x = x;
				bg.y = y;
				bg.size( width, height );

				slot.setRect( x + 2, y + 2, width - 4, height - 4 );

			}
		}
	}

}
