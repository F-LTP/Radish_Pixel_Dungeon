package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.PixelExplosionEffect;
import com.shatteredpixel.shatteredpixeldungeon.effects.SwordSlashEffect;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

import java.util.ArrayList;

/**
 * 测试模式特效工具台。
 *
 * 用于预览/开关本次接入的 CC0 特效素材：
 *  - effects/cc0/sword_slash.png（斩击，已接到巨剑/斩舰刀/豺狼蛮兵）
 *  - effects/cc0/pixel_explosion.png（爆炸，已接到爆破合剂）
 *  - effects/cc0/simplefx.png（16x16 素材表，逐帧浏览，便于后续挑选接入）
 *
 * 纯客户端预览，不影响存档。素材来源与许可见 effects/cc0/CREDITS.txt（均为 CC0）。
 */
public class EffectTester extends TestItem {

	private static final String AC_OPEN  = "open";
	private static final String AC_SLASH = "slash";
	private static final String AC_BOOM  = "boom";

	private static EffectWindow window;

	{
		image = ItemSpriteSheet.WAND_FROST;
		defaultAction = AC_OPEN;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_OPEN );
		actions.add( AC_SLASH );
		actions.add( AC_BOOM );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if (action.equals( AC_OPEN )) {
			if (window != null && window.exists && window.alive && window.parent != null) {
				Game.scene().bringToFront( window );
			} else {
				window = new EffectWindow();
				Game.scene().addToFront( window );
			}
		} else if (action.equals( AC_SLASH )) {
			if (hero.sprite != null && hero.sprite.parent != null) {
				SwordSlashEffect.preview( hero.sprite.parent, hero.sprite.center(), 0.6f, SwordSlashEffect.angleOffset );
			}
		} else if (action.equals( AC_BOOM )) {
			PixelExplosionEffect.previewAtCell( hero.pos );
		}
	}

	// ==================== 工具窗口 ====================

	private static class EffectWindow extends Window {

		private static final int W = 150;
		private static final int FRAME = 16;

		private final Image sheetPreview;
		private TextureFilm film;
		private int cols;
		private int total;
		private int frame;
		private boolean playing = true;
		private float timer;

		private final RedButton btnSlashToggle;
		private final RedButton btnBoomToggle;
		private final RedButton btnPlay;
		private final RenderedTextBlock info;

		EffectWindow() {
			super();

			float y = 4;
			float margin = 2;

			RenderedTextBlock title = PixelScene.renderTextBlock( "特效测试台", 9 );
			title.hardlight( Window.TITLE_COLOR );
			title.setPos( (W - title.width()) / 2f, y );
			add( title );
			y = title.bottom() + margin;

			RenderedTextBlock src = PixelScene.renderTextBlock(
					"素材: CC0 (OpenGameArt)，见 effects/cc0/CREDITS.txt", 6 );
			src.maxWidth( (int)(W - 2 * margin) );
			src.setPos( margin, y );
			add( src );
			y = src.bottom() + margin;

			RedButton b;

			b = new RedButton( "预览：斩击→最近敌人" ) {
				@Override protected void onClick() { previewSlash(); }
			};
			b.setRect( margin, y, W - 2 * margin, 14 );
			add( b );
			y = b.bottom() + margin;

			b = new RedButton( "预览：爆炸（玩家脚下）" ) {
				@Override protected void onClick() {
					if (Dungeon.hero != null) PixelExplosionEffect.previewAtCell( Dungeon.hero.pos );
				}
			};
			b.setRect( margin, y, W - 2 * margin, 14 );
			add( b );
			y = b.bottom() + margin;

			btnSlashToggle = new RedButton( "" ) {
				@Override protected void onClick() { SwordSlashEffect.enabled = !SwordSlashEffect.enabled; }
			};
			btnSlashToggle.setRect( margin, y, (W - 3 * margin) / 2f, 14 );
			add( btnSlashToggle );

			btnBoomToggle = new RedButton( "" ) {
				@Override protected void onClick() { PixelExplosionEffect.enabled = !PixelExplosionEffect.enabled; }
			};
			btnBoomToggle.setRect( margin + (W - 3 * margin) / 2f + margin, y, (W - 3 * margin) / 2f, 14 );
			add( btnBoomToggle );
			y = btnBoomToggle.bottom() + margin;

			// 朝向补偿角微调（素材基准朝向与攻击方向的偏差）
			b = new RedButton( "朝向 -15°" ) {
				@Override protected void onClick() { SwordSlashEffect.angleOffset -= 15f; previewSlash(); }
			};
			b.setRect( margin, y, (W - 3 * margin) / 2f, 14 );
			add( b );

			b = new RedButton( "朝向 +15°" ) {
				@Override protected void onClick() { SwordSlashEffect.angleOffset += 15f; previewSlash(); }
			};
			b.setRect( margin + (W - 3 * margin) / 2f + margin, y, (W - 3 * margin) / 2f, 14 );
			add( b );
			y = b.bottom() + margin;

			info = PixelScene.renderTextBlock( "", 6 );
			info.maxWidth( (int)(W - 2 * margin) );
			info.setPos( margin, y );
			add( info );
			y = info.bottom() + 2 * margin;

			RenderedTextBlock sheetTitle = PixelScene.renderTextBlock( "simplefx.png 逐帧浏览", 6 );
			sheetTitle.maxWidth( (int)(W - 2 * margin) );
			sheetTitle.setPos( margin, y );
			add( sheetTitle );
			y = sheetTitle.bottom() + margin;

			sheetPreview = new Image( Assets.Effects.SIMPLE_FX );
			film = new TextureFilm( Assets.Effects.SIMPLE_FX, FRAME, FRAME );
			if (sheetPreview.texture != null) {
				cols = Math.max( 1, sheetPreview.texture.width / FRAME );
				total = cols * Math.max( 1, sheetPreview.texture.height / FRAME );
			} else {
				cols = 1;
				total = 0;
			}
			sheetPreview.scale.set( 4f );
			sheetPreview.frame( film.get( 0 ) );
			sheetPreview.x = (W - sheetPreview.width * 4f) / 2f;
			sheetPreview.y = y;
			add( sheetPreview );
			y += FRAME * 4f + margin;

			b = new RedButton( "上一帧" ) {
				@Override protected void onClick() { step( -1 ); }
			};
			b.setRect( margin, y, (W - 4 * margin) / 3f, 14 );
			add( b );

			btnPlay = new RedButton( "暂停" ) {
				@Override protected void onClick() { playing = !playing; }
			};
			btnPlay.setRect( margin + (W - 4 * margin) / 3f + margin, y, (W - 4 * margin) / 3f, 14 );
			add( btnPlay );

			b = new RedButton( "下一帧" ) {
				@Override protected void onClick() { step( 1 ); }
			};
			b.setRect( margin + 2 * ((W - 4 * margin) / 3f + margin), y, (W - 4 * margin) / 3f, 14 );
			add( b );
			y = b.bottom() + margin;

			b = new RedButton( "关闭" ) {
				@Override protected void onClick() { hide(); }
			};
			b.setRect( margin, y, W - 2 * margin, 14 );
			add( b );
			y = b.bottom() + margin;

			resize( W, (int)y );
		}

		private void step( int delta ) {
			if (total <= 0) return;
			frame = (frame + delta) % total;
			if (frame < 0) frame += total;
			sheetPreview.frame( film.get( frame ) );
		}

		private void previewSlash() {
			Mob target = nearestVisibleMob();
			if (Dungeon.hero == null) return;
			if (target != null) {
				SwordSlashEffect.previewOn( Dungeon.hero, target );
			} else {
				// 没有可见敌人时，朝右侧播一个（仅用于看基准朝向）
				SwordSlashEffect.preview( Dungeon.hero.sprite.parent,
						Dungeon.hero.sprite.center(), 0.6f, SwordSlashEffect.angleOffset );
			}
		}

		private Mob nearestVisibleMob() {
			if (Dungeon.hero == null) return null;
			Mob best = null;
			int bestDist = Integer.MAX_VALUE;
			for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
				if (mob.alignment != com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY
						|| !mob.isAlive()
						|| !Dungeon.level.heroFOV[mob.pos]) {
					continue;
				}
				int d = Dungeon.level.distance( Dungeon.hero.pos, mob.pos );
				if (d < bestDist) {
					bestDist = d;
					best = mob;
				}
			}
			return best;
		}

		@Override
		public synchronized void update() {
			super.update();
			if (!exists) return;

			btnSlashToggle.text( "斩击: " + (SwordSlashEffect.enabled ? "开" : "关") );
			btnBoomToggle.text( "爆炸: " + (PixelExplosionEffect.enabled ? "开" : "关") );
			btnPlay.text( playing ? "暂停" : "播放" );
			info.text( "朝向补偿 " + (int) SwordSlashEffect.angleOffset + "°   帧 " + (frame + 1) + "/" + total
					+ "\n斩击=巨剑/斩舰刀/蛮兵   爆炸=爆破合剂" );

			if (playing && total > 0) {
				timer += Game.elapsed;
				if (timer >= 0.08f) {
					timer = 0;
					step( 1 );
				}
			}
		}

		@Override
		public void destroy() {
			super.destroy();
			if (window == this) window = null;
		}
	}
}
