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

		StyledButton btnRankings = new StyledButton(GREY_TR,Messages.get(this, "rankings")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchNoFade( RankingsScene.class );
			}
		};
		btnRankings.icon(Icons.get(Icons.RANKINGS));
		add(btnRankings);
		Dungeon.daily = Dungeon.dailyReplay = false;

		StyledButton btnBadges = new StyledButton(GREY_TR, Messages.get(JournalScene.class, "title")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchNoFade(JournalScene.class);
			}
		};
		btnBadges.icon(Icons.get(Icons.CATALOG));
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

		StyledButton seed = new JoinButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "seed"));
		seed.icon(Icons.get(Icons.ENERGY));
		seed.icon().scale.set(align(0.5f));
		add(seed);

		final int BTN_HEIGHT = 20;
		final int seedWidth = Math.max(40, (int)Math.ceil(seed.reqWidth()) + 8);
		final int seedHeight = Math.max(20, (int)Math.ceil(seed.reqHeight()) + 4);
		int GAP = (int)(h - topRegion - (landscape() ? 3 : 6)*BTN_HEIGHT)/3;
		GAP /= landscape() ? 3 : 5;
		GAP = Math.max(GAP, 2);

		BitmapText version = new BitmapText( "v" + Game.version, pixelFont);
		version.measure();
		version.alpha( 0.4f);
		version.x = w - version.width() - 4;
		version.y = h - version.height() - 2;

		add( version );

		// 按钮区宽度固定成破碎原版的版心宽度并居中，不再跟着 logo 宽度走（高倍缩放下 logo 很宽，会把按钮撑出屏幕）
		float contentWidth = Math.min(w - 8,
				landscape() ? PixelScene.MIN_WIDTH_L - 6 : PixelScene.MIN_WIDTH_P - 2);
		contentWidth = Math.max(contentWidth, 80);
		float left = (w - contentWidth) / 2f;
		if (left < 4) left = 4;

		float halfW = (contentWidth - 2) / 2f;
		float quarterW = (contentWidth - 6) / 4f;

		// 行数固定：横屏 3 行、竖屏 6 行；空间不够时整体上移，保证最下面一行不会被屏幕截掉，
		// 并且要给右下角的版本号留出高度，否则最后一行按钮会压住它
		int rows = landscape() ? 3 : 6;
		int need = rows * BTN_HEIGHT + (rows - 1) * GAP;
		float bottomLimit = h - 4 - version.height() - 2;
		float btnTop = topRegion + GAP;
		if (btnTop + need > bottomLimit) {
			btnTop = Math.max(2, bottomLimit - need);
		}

		if (landscape()) {
			btnPlay.setRect(left, btnTop, halfW, BTN_HEIGHT);
			align(btnPlay);
			btnChanges.setRect(left + halfW + 2, btnTop, halfW, BTN_HEIGHT);

			btnRankings.setRect(left, btnPlay.bottom() + GAP, quarterW, BTN_HEIGHT);
			btnBadges.setRect(btnRankings.right() + 2, btnRankings.top(), quarterW, BTN_HEIGHT);
			btnSeedTest.setRect(btnBadges.right() + 2, btnRankings.top(), quarterW, BTN_HEIGHT);
			btnSeedAnalysis.setRect(btnSeedTest.right() + 2, btnRankings.top(), quarterW, BTN_HEIGHT);

			btnSettings.setRect(left, btnRankings.bottom() + GAP, quarterW, BTN_HEIGHT);
			btnAbout.setRect(btnSettings.right() + 2, btnSettings.top(), quarterW, BTN_HEIGHT);
			seed.setRect(left + 2*(quarterW + 2), btnSettings.top(), contentWidth - 2*(quarterW + 2), BTN_HEIGHT);
		} else {
			btnPlay.setRect(left, btnTop, contentWidth, BTN_HEIGHT);
			align(btnPlay);

			btnRankings.setRect(left, btnPlay.bottom() + GAP, contentWidth, BTN_HEIGHT);

			btnBadges.setRect(left, btnRankings.bottom() + GAP, halfW, BTN_HEIGHT);
			btnChanges.setRect(left + halfW + 2, btnBadges.top(), halfW, BTN_HEIGHT);

			btnSettings.setRect(left, btnBadges.bottom() + GAP, halfW, BTN_HEIGHT);
			btnAbout.setRect(left + halfW + 2, btnSettings.top(), halfW, BTN_HEIGHT);

			btnSeedTest.setRect(left, btnSettings.bottom() + GAP, halfW, BTN_HEIGHT);
			btnSeedAnalysis.setRect(left + halfW + 2, btnSeedTest.top(), halfW, BTN_HEIGHT);

			seed.setRect(left, btnSeedTest.bottom() + GAP, contentWidth, BTN_HEIGHT);
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
			if (Messages.lang().status() == Languages.Status.X_UNFINISH){
				icon(Icons.get(Icons.LANGS));
				icon.hardlight(1.5f, 0, 0);
			} else {
				icon(Icons.get(Icons.PREFS));
			}
		}

		@Override
		public void update() {
			super.update();

			if (Messages.lang().status() == Languages.Status.X_UNFINISH){
				textColor(ColorMath.interpolate( 0xFFFFFF, CharSprite.NEGATIVE, 0.5f + (float)Math.sin(Game.timeTotal*5)/2f));
			}
		}

		@Override
		protected void onClick() {
			if (Messages.lang().status() == Languages.Status.X_UNFINISH){
				WndSettings.last_index = 4;
			}
			ShatteredPixelDungeon.scene().add(new WndSettings());
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
