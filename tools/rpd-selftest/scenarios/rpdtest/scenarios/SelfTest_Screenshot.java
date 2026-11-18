package rpdtest.scenarios;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;

import java.util.Arrays;
import java.util.HashSet;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自测：H.screenshot() 能把隐藏窗口里的画面存成 PNG（离屏渲染，窗口不显示）。
 * 只验证「存下来的是一张尺寸正确、有内容、会随游戏状态变化的图」；图里的东西对不对要靠人看。
 */
public class SelfTest_Screenshot implements Scenario {

	@Override
	public String describe() {
		return "工具自测：截图得到尺寸正确、非纯色的 PNG；英雄走开、放了一只怪之后再截一张，两张不一样";
	}

	/** { 宽, 高, 采样到的颜色数, 窗口宽, 窗口高 } */
	private static int[] inspect( String path ) {
		Pixmap pm = new Pixmap( Gdx.files.absolute( path ) );
		try {
			HashSet<Integer> colours = new HashSet<>();
			for (int y = 0; y < pm.getHeight(); y += 2) {
				for (int x = 0; x < pm.getWidth(); x += 2) {
					colours.add( pm.getPixel( x, y ) );
				}
			}
			return new int[]{ pm.getWidth(), pm.getHeight(), colours.size(),
					Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight() };
		} finally {
			pm.dispose();
		}
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.frames( 30 );

		String first = h.screenshot( "arena at start" );
		if (first == null) {
			// -SkipDraw：没有画面可截。约定是「记一条 note、返回 null、场景照常往下跑」
			h.check( h.rest( 1 ) == H.Status.READY, "带 -SkipDraw 运行：screenshot() 返回 null，场景照常继续" );
			return;
		}
		int[] a = h.get( () -> inspect( first ) );
		h.note( "first screenshot: " + a[0] + "x" + a[1] + " (window " + a[3] + "x" + a[4] + "), distinct colours sampled: " + a[2] );
		h.check( a[0] == a[3] && a[1] == a[4], "截图尺寸 = 隐藏窗口的尺寸（截图 " + a[0] + "x" + a[1] + "，窗口 " + a[3] + "x" + a[4] + "）" );
		h.check( a[2] >= 16, "截图不是纯色图（采样到 " + a[2] + " 种颜色）" );

		Arena.dummy( h, Arena.c( 12, 8 ) );
		h.walkTo( Arena.c( 13, 12 ) );
		h.frames( 30 );

		String second = h.screenshot( "after walking + a rat" );
		int[] b = h.get( () -> inspect( second ) );
		h.note( "second screenshot: " + b[0] + "x" + b[1] + ", distinct colours sampled: " + b[2] );
		h.check( b[2] >= 16, "第二张截图也不是纯色图（采样到 " + b[2] + " 种颜色）" );
		boolean differs = h.get( () -> !Arrays.equals(
				Gdx.files.absolute( first ).readBytes(), Gdx.files.absolute( second ).readBytes() ) );
		h.check( differs, "英雄走开、场上多了一只怪之后，第二张截图与第一张不同" );
		h.check( h.rest( 1 ) == H.Status.READY, "截图之后游戏照常进行（等 1 回合后又在等输入）" );
	}
}
