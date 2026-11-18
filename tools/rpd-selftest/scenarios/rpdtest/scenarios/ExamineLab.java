package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.MovieClip;
import com.watabou.utils.RectF;

import rpdtest.H;
import rpdtest.Reflect;

import java.util.List;

/**
 * 「放大镜查看」的公共检查：查看窗口（WndInfoMob）里画的那只怪，应该和地图上那只怪长得一样
 * （同一张贴图，且当前帧属于地图精灵的待机动画）。不是场景，只是辅助类。
 */
final class ExamineLab {

	private ExamineLab() {}

	/** 用放大镜查看 mob，做断言，然后关窗。label 用在断言文字里。 */
	static void examineAndCheck( H h, Mob mob, String label ) {
		h.require( h.get( () -> mob.sprite != null && mob.sprite.parent != null ), label + "：地图上有它的精灵" );
		h.examine( h.get( () -> mob.pos ) );
		WndInfoMob wnd = h.findWindow( WndInfoMob.class );
		h.require( wnd != null, label + "：放大镜查看弹出了怪物信息窗口 WndInfoMob（实际窗口：" + h.windowNames() + "）" );
		// 存一张图给人看（窗口里画的是哪台机体，一眼就能看出来）；断言仍然只看状态
		h.frames( 10 );
		h.note( "下一张截图：" + label + " 的放大镜窗口" );
		h.screenshot( "examine-window" );

		String[] r = h.get( () -> {
			List<CharSprite> sprites = H.charSpritesIn( wnd );
			if (sprites.isEmpty()) return new String[]{ "(the window contains no character sprite)", "", "false" };
			CharSprite shown = sprites.get( 0 );
			CharSprite world = mob.sprite;
			MovieClip.Animation idle = (MovieClip.Animation) Reflect.get( world, "idle" );
			boolean frameOk = false;
			RectF sf = shown.frame();
			StringBuilder idleDesc = new StringBuilder();
			if (idle != null && idle.frames != null) {
				for (RectF f : idle.frames) {
					if (f == null) continue;
					if (same( f, sf )) frameOk = true;
					if (idleDesc.length() > 0) idleDesc.append( "," );
					idleDesc.append( frameIndex( world.texture, f ) );
				}
			}
			boolean sameTex = shown.texture == world.texture;
			return new String[]{
					describe( shown.texture, sf ),
					textureName( world.texture ) + " 待机帧 #" + idleDesc,
					String.valueOf( sameTex && frameOk ) };
		} );
		h.check( "true".equals( r[2] ), label + "：放大镜里的图 = 地图上这台机体的图（放大镜：" + r[0] + "；地图：" + r[1] + "）" );
		h.closeWindows();
	}

	private static boolean same( RectF a, RectF b ) {
		return Math.abs( a.left - b.left ) < 1e-4f && Math.abs( a.top - b.top ) < 1e-4f
				&& Math.abs( a.right - b.right ) < 1e-4f && Math.abs( a.bottom - b.bottom ) < 1e-4f;
	}

	private static String textureName( SmartTexture t ) {
		if (t == TextureCache.get( Assets.Sprites.DM_PROTO )) return "dm_proto.png（未激活原型机图，#0 是 DM-175、#1 是 DM-100）";
		if (t == TextureCache.get( Assets.Sprites.DM100 )) return "dm100.png（激活后的 DM-100）";
		if (t == TextureCache.get( Assets.Sprites.DM175 )) return "dm-175.png（激活后的 DM-175）";
		return "other texture " + t.width + "x" + t.height;
	}

	private static int frameIndex( SmartTexture t, RectF f ) {
		int wpx = Math.round( f.width() * t.width );
		int hpx = Math.round( f.height() * t.height );
		if (wpx <= 0 || hpx <= 0) return -1;
		int cols = Math.max( 1, t.width / wpx );
		int col = Math.round( f.left * t.width / wpx );
		int row = Math.round( f.top * t.height / hpx );
		return row * cols + col;
	}

	private static String describe( SmartTexture t, RectF f ) {
		return textureName( t ) + " 帧 #" + frameIndex( t, f );
	}
}
