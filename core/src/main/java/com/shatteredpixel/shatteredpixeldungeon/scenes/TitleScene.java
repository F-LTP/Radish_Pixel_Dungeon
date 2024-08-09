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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.BannerSprites;
import com.shatteredpixel.shatteredpixeldungeon.effects.Fireball;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.services.news.News;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.update.RDChangesButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSettings;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.ColorMath;
import com.watabou.utils.DeviceCompat;

import java.util.Date;

public class TitleScene extends PixelScene {
	
	@Override
	public void create() {
		
		super.create();

		Music.INSTANCE.playTracks(
				new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
				new float[]{1, 1},
				false);

		uiCamera.visible = false;
		
		int w = Camera.main.width;
		int h = Camera.main.height;
		
		Archs archs = new Archs();
		archs.setSize( w, h );
		add( archs );
		
		Image title = BannerSprites.get( BannerSprites.Type.PIXEL_DUNGEON );
		add( title );

		float topRegion = Math.max(title.height - 6, h*0.45f);

		title.x = (w - title.width()) / 2f;
		title.y = 2 + (topRegion - title.height()) / 2f;

		align(title);

		placeTorch(title.x + 22, title.y + 46);
		placeTorch(title.x + title.width - 22, title.y + 46);

		Image signs = new Image( BannerSprites.get( BannerSprites.Type.PIXEL_DUNGEON_SIGNS ) ) {
			private float time = 0;
			@Override
			public void update() {
				super.update();
				am = Math.max(0f, (float)Math.sin( time += Game.elapsed ));
				if (time >= 1.5f*Math.PI) time = 0;
			}
			@Override
			public void draw() {
				Blending.setLightMode();
				super.draw();
				Blending.setNormalMode();
			}
		};
		signs.x = title.x + (title.width() - signs.width())/2f;
		signs.y = title.y;
		add( signs );

		final Chrome.Type GREY_TR = Chrome.Type.GREY_BUTTON_TR;
		
		StyledButton btnPlay = new StyledButton(GREY_TR, Messages.get(this, "enter")){
			@Override
			protected void onClick() {
				if (GamesInProgress.checkAll().size() == 0){
					GamesInProgress.selectedClass = null;
					GamesInProgress.curSlot = 1;
					ShatteredPixelDungeon.switchScene(HeroSelectScene.class);
				} else {
					ShatteredPixelDungeon.switchNoFade( StartScene.class );
				}
			}
			
			@Override
			protected boolean onLongClick() {
				//making it easier to start runs quickly while debugging
				if (DeviceCompat.isDebug()) {
					GamesInProgress.selectedClass = null;
					GamesInProgress.curSlot = 1;
					ShatteredPixelDungeon.switchScene(HeroSelectScene.class);
					return true;
				}
				return super.onLongClick();
			}
		};
		btnPlay.icon(Icons.get(Icons.ENTER));
		add(btnPlay);

		StyledButton btnSupport = new SupportButton(GREY_TR, Messages.get(this, "support"));
		add(btnSupport);

		StyledButton btnRankings = new StyledButton(GREY_TR,Messages.get(this, "rankings")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchNoFade( RankingsScene.class );
			}
		};
		btnRankings.icon(Icons.get(Icons.RANKINGS));
		add(btnRankings);
		Dungeon.daily = Dungeon.dailyReplay = false;

		StyledButton btnBadges = new StyledButton(GREY_TR, Messages.get(this, "badges")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchNoFade( BadgesScene.class );
			}
		};
		btnBadges.icon(Icons.get(Icons.BADGES));
		add(btnBadges);

		/*StyledButton btnNews = new NewsButton(GREY_TR, Messages.get(this, "news"));
		btnNews.icon(Icons.get(Icons.NEWS));
		add(btnNews);*/

		StyledButton btnChanges = new RDChangesButton(GREY_TR, Messages.get(this, "changes"));
		btnChanges.icon(new Image(Icons.get(Icons.CHANGES)));
		add(btnChanges);

		StyledButton btnSettings = new SettingsButton(GREY_TR, Messages.get(this, "settings"));
		add(btnSettings);

		StyledButton btnAbout = new StyledButton(GREY_TR, Messages.get(this, "about")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchScene( AboutScene.class );
			}
		};
		btnAbout.icon(Icons.get(Icons.SHPX));
		add(btnAbout);

		StyledButton btnSeedTest = new StyledButton(GREY_TR, Messages.get(this, "seed_find")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchScene( SeedFindScene.class );
			}
		};
		btnSeedTest.icon(Icons.get(Icons.SEED));
		add(btnSeedTest);

		StyledButton btnSeedAnalysis = new StyledButton(GREY_TR, Messages.get(this, "seed_analysis")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchScene( SeedAnalysisScene.class );
			}
		};
		btnSeedAnalysis.icon(Icons.get(Icons.MAGNIFY));
		add(btnSeedAnalysis);

		StyledButton seed = new JoinButton(landscape() ? Chrome.Type.GREY_BUTTON_TR : Chrome.Type.BLANK, Messages.get(this, "seed"));
		seed.icon(Icons.get(Icons.ENERGY));
		add(seed);

		final int BTN_HEIGHT = 20;
		int GAP = (int)(h - topRegion - (landscape() ? 3 : 6)*BTN_HEIGHT)/3;
		GAP /= landscape() ? 3 : 5;
		GAP = Math.max(GAP, 2);

		BitmapText version = new BitmapText( "v" + Game.version, pixelFont);
		version.measure();
		version.alpha( 0.4f);
		version.x = w - version.width() - 4;
		version.y = h - version.height() - 2;

		if (landscape()) {
			btnPlay.setRect(title.x-90, topRegion+GAP, ((title.width()+180)/2)-1, BTN_HEIGHT);
			align(btnPlay);
			btnSupport.setRect(btnPlay.right()+2, btnPlay.top(), btnPlay.width(), BTN_HEIGHT);
			btnRankings.setRect(btnPlay.left(), btnPlay.bottom()+ GAP, (btnPlay.width()*.5f)-1, BTN_HEIGHT);
			btnSeedTest.setRect(btnRankings.right()+2, btnPlay.bottom()+ GAP, (btnPlay.width()*.5f)-1, BTN_HEIGHT);
			btnSeedAnalysis.setRect(btnSeedTest.right()+2, btnPlay.bottom()+ GAP, (btnPlay.width()*.5f)-1, BTN_HEIGHT);
			btnBadges.setRect(btnRankings.left(), btnRankings.bottom()+GAP, (btnPlay.width()*.67f)-1, BTN_HEIGHT);
			//btnNews.setRect(btnRankings.right()+2, btnRankings.top(), btnRankings.width(), BTN_HEIGHT);
			btnChanges.setRect(btnBadges.right()+2, btnBadges.top(), btnBadges.width(), BTN_HEIGHT);
			btnSettings.setRect(btnSupport.centerX(), btnSupport.bottom()+ GAP, (btnPlay.width()*.5f)-1, BTN_HEIGHT);
			btnAbout.setRect(btnChanges.right()+2, btnSettings.bottom() + GAP, btnBadges.width(), BTN_HEIGHT);
			seed.setRect(10, 0,40,20);
		} else {
			seed.setRect(10, version.y-10,40,20);
			btnPlay.setRect(title.x-5, topRegion+GAP, title.width()+10, BTN_HEIGHT);
			align(btnPlay);
			btnSupport.setRect(btnPlay.left(), btnPlay.bottom()+ GAP, btnPlay.width(), BTN_HEIGHT);
			btnRankings.setRect(btnPlay.left(), btnSupport.bottom()+ GAP, btnPlay.width(), BTN_HEIGHT);
			btnBadges.setRect(btnRankings.left(), btnRankings.bottom()+ GAP, (btnPlay.width()/2)-1, BTN_HEIGHT);
			//btnNews.setRect(btnRankings.left(), btnRankings.bottom()+ GAP, btnRankings.width(), BTN_HEIGHT);
			btnChanges.setRect(btnBadges.right()+2, btnBadges.top(), btnBadges.width(), BTN_HEIGHT);
			btnSettings.setRect(btnBadges.left(), btnBadges.bottom() +GAP, (btnPlay.width()/2)-1, BTN_HEIGHT);
			btnAbout.setRect(btnSettings.right()+2, btnBadges.bottom() +GAP, (btnPlay.width()/2)-1, BTN_HEIGHT);
			btnSeedTest.setRect(btnSettings.left(), btnSettings.bottom() +GAP, (btnPlay.width()/2)-1, BTN_HEIGHT);
			btnSeedAnalysis.setRect(btnSeedTest.right()+2, btnSettings.bottom() +GAP, (btnPlay.width()/2)-1, BTN_HEIGHT);
		}

		if (DeviceCompat.isDesktop()) {
			ExitButton btnExit = new ExitButton();
			btnExit.setPos( w - btnExit.width(), 0 );
			add( btnExit );
		}

		fadeIn();
	}
	
	private void placeTorch( float x, float y ) {
		Fireball fb = new Fireball();
		fb.setPos( x, y );
		add( fb );
	}

	private static class NewsButton extends StyledButton {

		public NewsButton(Chrome.Type type, String label ){
			super(type, label);
			if (SPDSettings.news()) News.checkForNews();
		}

		int unreadCount = -1;

		@Override
		public void update() {
			super.update();

			if (unreadCount == -1 && News.articlesAvailable()){
				long lastRead = SPDSettings.newsLastRead();
				if (lastRead == 0){
					if (News.articles().get(0) != null) {
						SPDSettings.newsLastRead(News.articles().get(0).date.getTime());
					}
				} else {
					unreadCount = News.unreadArticles(new Date(SPDSettings.newsLastRead()));
					if (unreadCount > 0) {
						unreadCount = Math.min(unreadCount, 9);
						text(text() + "(" + unreadCount + ")");
					}
				}
			}

			if (unreadCount > 0){
				textColor(ColorMath.interpolate( 0xFFFFFF, Window.SHPX_COLOR, 0.5f + (float)Math.sin(Game.timeTotal*5)/2f));
			}
		}

		@Override
		protected void onClick() {
			super.onClick();
			ShatteredPixelDungeon.switchNoFade( NewsScene.class );
		}
	}

	//在线更新 2024-8-6
	public static class ChangesButton extends StyledButton {

		public ChangesButton( Chrome.Type type, String label ){
			super(type, label);
		}

		@Override
		protected void onClick() {
			ShatteredPixelDungeon.switchNoFade( ChangesScene.class );
		}

	}

	private static class SettingsButton extends StyledButton {

		public SettingsButton( Chrome.Type type, String label ){
			super(type, label);
			if (Messages.lang().status() == Languages.Status.INCOMPLETE){
				icon(Icons.get(Icons.LANGS));
				icon.hardlight(1.5f, 0, 0);
			} else {
				icon(Icons.get(Icons.PREFS));
			}
		}

		@Override
		public void update() {
			super.update();

			if (Messages.lang().status() == Languages.Status.INCOMPLETE){
				textColor(ColorMath.interpolate( 0xFFFFFF, CharSprite.NEGATIVE, 0.5f + (float)Math.sin(Game.timeTotal*5)/2f));
			}
		}

		@Override
		protected void onClick() {
			if (Messages.lang().status() == Languages.Status.INCOMPLETE){
				WndSettings.last_index = 4;
			}
			ShatteredPixelDungeon.scene().add(new WndSettings());
		}
	}



	private static class SupportButton extends StyledButton{

		public SupportButton( Chrome.Type type, String label ){
			super(type, label);
			icon(Icons.get(Icons.GOLD));
			textColor(Window.TITLE_COLOR);
		}

		@Override
		protected void onClick() {
			ShatteredPixelDungeon.switchNoFade(SupporterScene.class);
		}
	}

	private static class JoinButton extends StyledButton {

		public JoinButton( Chrome.Type type, String label ){
			super(type, label);
		}

		@Override
		protected void onClick() {
			ShatteredPixelDungeon.platform.openURI( "https://qm.qq.com/q/Uzy3eiKSC6" );
		}

	}

}
