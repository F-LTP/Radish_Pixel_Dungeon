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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

import java.util.Collections;

public class WndImp extends Window{

	private static final int WIDTH      = 120;
	private static final int BUTTON_HEIGHT = 20;
	private static final int MARGIN        = 2;
	private Imp the_imp;
	private DwarfToken the_tokens;
	public WndImp (final Imp imp,final DwarfToken tokens){
		super();
		the_imp=imp;
		the_tokens=tokens;
		int width = WIDTH;

		float pos = 0;
		IconTitle tfTitle = new IconTitle(new ItemSprite( tokens.image(), null ), tokens.name());
		tfTitle.setRect(0, pos, width, 0);
		add(tfTitle);
		pos = tfTitle.bottom() + 2*MARGIN;


		layoutBody(pos, Messages.get(WndImp.class, "message"), Messages.get(WndImp.class, "reward"),Messages.get(WndImp.class, "power"));


	}
	/*public WndImp( final Imp imp, final DwarfToken tokens ) {

		super();

		IconTitle titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( tokens.image(), null ) );
		titlebar.label( Messages.titleCase( tokens.name() ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		RenderedTextBlock message = PixelScene.renderTextBlock( Messages.get(this, "message"), 6 );
		message.maxWidth(WIDTH);
		message.setPos(0, titlebar.bottom() + GAP);
		add( message );

		RedButton btnReward = new RedButton( Messages.get(this, "reward") ) {
			@Override
			protected void onClick() {
				takeReward( imp, tokens, Imp.Quest.reward );
			}
		};
		btnReward.setRect( 0, message.top() + message.height() + GAP, WIDTH, BTN_HEIGHT );
		add( btnReward );

		resize( WIDTH, (int)btnReward.bottom() );
	}*/
	private void layoutBody(float pos, String message, String... options){
		int width = WIDTH;

		RenderedTextBlock tfMesage = PixelScene.renderTextBlock( 6 );
		tfMesage.text(message, width);
		tfMesage.setPos( 0, pos );
		add( tfMesage );

		pos = tfMesage.bottom() + 2*MARGIN;

		for (int i=0; i < options.length; i++) {
			final int index = i;
			RedButton btn = new RedButton( options[i] ) {
				@Override
				protected void onClick() {
					hide();
					onSelect( index );
				}
			};
			if (hasIcon(i)) btn.icon(getIcon(i));
			btn.enable(enabled(i));
			add( btn );

			if (!hasInfo(i)) {
				btn.setRect(0, pos, width, BUTTON_HEIGHT);
			} else {
				btn.setRect(0, pos, width - BUTTON_HEIGHT, BUTTON_HEIGHT);
				IconButton info = new IconButton(Icons.get(Icons.INFO)){
					@Override
					protected void onClick() {
						onInfo( index );
					}
				};
				info.setRect(width-BUTTON_HEIGHT, pos, BUTTON_HEIGHT, BUTTON_HEIGHT);
				add(info);
			}

			pos += BUTTON_HEIGHT + MARGIN;
		}

		resize( width, (int)(pos - MARGIN) );
	}

	private float elapsed = 0f;

	@Override
	public synchronized void update() {
		super.update();
		elapsed += Game.elapsed;
	}

	@Override
	public void hide() {
		if (elapsed > 0.2f){
			super.hide();
		}
	}
	protected boolean enabled( int index ){
		switch (index) {
			case 0: default:	return true;
			case 1:return Dungeon.hero.armorAbility==null;
		}
	}

	protected void onSelect( int index ) {
		if (index == 0 && elapsed > 0.2f) {
			takeReward( the_imp, the_tokens, Imp.Quest.reward );
		}else if (index >0 && elapsed >0.2f){
			gainTalent(the_imp,the_tokens,Dungeon.hero);
		}
	}

	protected boolean hasInfo( int index ) {
		return false;
	}

	protected void onInfo( int index ) {}

	protected boolean hasIcon( int index ) {
		return false;
	}

	protected Image getIcon(int index ) {
		return null;
	}
	private void takeReward( Imp imp, DwarfToken tokens, Item reward ) {

		hide();

		tokens.detachAll( Dungeon.hero.belongings.backpack );
		if (reward == null) return;

		reward.identify(false);
		if (reward.doPickUp( Dungeon.hero )) {
			GLog.i( Messages.capitalize(Messages.get(Dungeon.hero, "you_now_have", reward.name())) );
		} else {
			Dungeon.level.drop( reward, imp.pos ).sprite.drop();
		}

		imp.flee();

		Imp.Quest.complete();
	}

	private void gainTalent(Imp imp, DwarfToken tokens, Hero hero) {
		if (hero.subClass== HeroSubClass.NONE){
			GLog.n( Messages.get(this, "deny"));
		}
		else {
			hero.powerOfImp=true;
			Buff.affect(hero, powerGainTracker.class);
			hero.spend(Actor.TICK);
			hero.busy();
			Talent.initT4Talents(hero);

			hero.sprite.operate(hero.pos);
			Sample.INSTANCE.playDelayed(Assets.Sounds.LEVELUP, 0.3f, 0.7f, 1.2f);
			Sample.INSTANCE.playDelayed(Assets.Sounds.LEVELUP, 0.6f, 0.7f, 1.2f);

			Emitter e = hero.sprite.centerEmitter();
			e.pos(e.x - 2, e.y - 6, 4, 4);
			e.start(Speck.factory(Speck.MASK), 0.05f, 20);

		}
		hide();

		tokens.detachAll( Dungeon.hero.belongings.backpack );
		imp.flee();

		Imp.Quest.complete();
	}
	public static class powerGainTracker extends Buff {

	}
}
