package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

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

import java.util.ArrayList;

/**
 * 测试模式特效工具台：预览/开关 CC0 斩击、像素爆炸特效，并微调斩击朝向。
 * （simplefx 素材表已在 assets 中保留，需要时可再加逐帧浏览。）
 */
public class EffectTester extends TestItem {

	private static final String AC_OPEN = "open";

	private static EffectWindow window;

	{
		image = ItemSpriteSheet.WAND_FROST;
		defaultAction = AC_OPEN;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_OPEN );
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
		}
	}

	private static class EffectWindow extends Window {

		private static final int W = 150;

		private final RedButton btnSlash;
		private final RedButton btnBoom;
		private final RenderedTextBlock info;

		EffectWindow() {
			super();

			float y = 4;
			float m = 2;

			RenderedTextBlock title = PixelScene.renderTextBlock( "特效测试台", 9 );
			title.hardlight( Window.TITLE_COLOR );
			title.setPos( (W - title.width()) / 2f, y );
			add( title );
			y = title.bottom() + m;

			RedButton b;

			b = new RedButton( "预览：斩击→最近敌人" ) {
				@Override protected void onClick() { previewSlash(); }
			};
			b.setRect( m, y, W - 2 * m, 16 );
			add( b );
			y = b.bottom() + m;

			b = new RedButton( "预览：爆炸（脚下）" ) {
				@Override protected void onClick() {
					if (Dungeon.hero != null) PixelExplosionEffect.previewAtCell( Dungeon.hero.pos );
				}
			};
			b.setRect( m, y, W - 2 * m, 16 );
			add( b );
			y = b.bottom() + m;

			btnSlash = new RedButton( "" ) {
				@Override protected void onClick() { SwordSlashEffect.enabled = !SwordSlashEffect.enabled; }
			};
			btnSlash.setRect( m, y, (W - 3 * m) / 2f, 16 );
			add( btnSlash );

			btnBoom = new RedButton( "" ) {
				@Override protected void onClick() { PixelExplosionEffect.enabled = !PixelExplosionEffect.enabled; }
			};
			btnBoom.setRect( m + (W - 3 * m) / 2f + m, y, (W - 3 * m) / 2f, 16 );
			add( btnBoom );
			y = btnBoom.bottom() + m;

			b = new RedButton( "朝向 -15°" ) {
				@Override protected void onClick() { SwordSlashEffect.angleOffset -= 15f; previewSlash(); }
			};
			b.setRect( m, y, (W - 3 * m) / 2f, 16 );
			add( b );

			b = new RedButton( "朝向 +15°" ) {
				@Override protected void onClick() { SwordSlashEffect.angleOffset += 15f; previewSlash(); }
			};
			b.setRect( m + (W - 3 * m) / 2f + m, y, (W - 3 * m) / 2f, 16 );
			add( b );
			y = b.bottom() + m;

			b = new RedButton( "朝向归零" ) {
				@Override protected void onClick() { SwordSlashEffect.angleOffset = 0; previewSlash(); }
			};
			b.setRect( m, y, W - 2 * m, 16 );
			add( b );
			y = b.bottom() + m;

			info = PixelScene.renderTextBlock( "", 6 );
			info.maxWidth( (int)(W - 2 * m) );
			info.setPos( m, y );
			add( info );
			y = info.bottom() + m;

			b = new RedButton( "关闭" ) {
				@Override protected void onClick() { hide(); }
			};
			b.setRect( m, y, W - 2 * m, 16 );
			add( b );

			resize( W, (int)(b.bottom() + m) );
		}

		private void previewSlash() {
			if (Dungeon.hero == null || Dungeon.hero.sprite == null) return;
			Mob target = nearestVisibleMob();
			if (target != null) {
				SwordSlashEffect.previewOn( Dungeon.hero, target );
			} else {
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
						|| !mob.isAlive() || !Dungeon.level.heroFOV[mob.pos]) {
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
			btnSlash.text( "斩击: " + (SwordSlashEffect.enabled ? "开" : "关") );
			btnBoom.text( "爆炸: " + (PixelExplosionEffect.enabled ? "开" : "关") );
			info.text( "朝向补偿 " + (int) SwordSlashEffect.angleOffset + "°  (范围 -180~180)" );
		}

		@Override
		public void destroy() {
			super.destroy();
			if (window == this) window = null;
		}
	}
}
