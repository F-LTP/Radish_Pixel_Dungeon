package rpdtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import com.watabou.utils.PlatformSupport;

import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * 真实游戏主类（ShatteredPixelDungeon）的测试子类：游戏逻辑一行不改，只加护栏和钩子。
 *  - 每帧检查窗口没有变可见 / 没有焦点（变了立刻藏回去并以 UNSAFE 结束）；
 *  - 只在 GameScene 里加速（Game.timeScale），加载界面保持 1 倍速；
 *  - 游戏自己 catch 后上报的异常（Game.reportException）记进结论摘要；
 *  - 启动时校验存档目录确实落在沙盒里。
 */
public class HarnessGame extends ShatteredPixelDungeon {

	/** BootScene.create() 完成后置 true。 */
	public static volatile boolean bootReady = false;

	/** GameScene 里的加速倍率（动画、回合推进都按它加速）。 */
	public static volatile float timeScale = 1f;

	/** true 时跳过场景绘制（只跑逻辑）。默认 false：照常绘制，才能抓到绘制路径上的崩溃。 */
	public static volatile boolean skipDraw = false;

	static volatile File sandboxSaveDir;

	private long windowHandle = 0;

	public HarnessGame( PlatformSupport platform ) {
		super( platform );
	}

	@Override
	public void create() {
		windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
		// 双保险：就算哪条路径把窗口 show 出来，也不许它抢焦点
		GLFW.glfwSetWindowAttrib( windowHandle, GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE );
		checkWindowHidden( "create()" );
		checkSandbox();

		// 音频必须是 libGDX 的空实现（config.disableAudio）：不打开任何音频设备
		String audioClass = Gdx.audio == null ? "null" : Gdx.audio.getClass().getName();
		if (!audioClass.toLowerCase( java.util.Locale.ROOT ).contains( "mock" )) {
			Result.finish( Result.Verdict.UNSAFE, "audio is not disabled: Gdx.audio is " + audioClass, null );
		}

		// 游戏日志（Gdx.app.log / GLog）并进测试日志
		Gdx.app.setApplicationLogger( new HarnessLogger() );

		super.create();

		Log.info( "game created: version=" + Game.version + " versionCode=" + Game.versionCode
				+ " gl=" + Gdx.graphics.getGLVersion().getDebugVersionString().replace( '\n', ' ' ) );
		Log.info( "window " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight()
				+ " visible=" + GLFW.glfwGetWindowAttrib( windowHandle, GLFW.GLFW_VISIBLE )
				+ " focused=" + GLFW.glfwGetWindowAttrib( windowHandle, GLFW.GLFW_FOCUSED )
				+ " audio=" + Gdx.audio.getClass().getSimpleName() );
	}

	/** 存档 / 设置必须落在沙盒目录里，否则宁可不跑。 */
	private void checkSandbox() {
		try {
			File probe = FileUtils.getFileHandle( "rpdtest-probe" ).file().getCanonicalFile();
			File sandbox = sandboxSaveDir.getCanonicalFile();
			if (!probe.getPath().startsWith( sandbox.getPath() + File.separator )) {
				Result.finish( Result.Verdict.UNSAFE,
						"save path is not inside the sandbox: " + probe + " (sandbox " + sandbox + ")", null );
			}
			Log.info( "save files resolve to " + probe.getParent() + " (sandbox ok)" );
		} catch (Throwable t) {
			Result.finish( Result.Verdict.UNSAFE, "could not verify the sandbox save path: " + t, Log.stackOf( t ) );
		}
	}

	private void checkWindowHidden( String where ) {
		if (windowHandle == 0) return;
		boolean visible = GLFW.glfwGetWindowAttrib( windowHandle, GLFW.GLFW_VISIBLE ) == GLFW.GLFW_TRUE;
		boolean focused = GLFW.glfwGetWindowAttrib( windowHandle, GLFW.GLFW_FOCUSED ) == GLFW.GLFW_TRUE;
		boolean iconified = GLFW.glfwGetWindowAttrib( windowHandle, GLFW.GLFW_ICONIFIED ) == GLFW.GLFW_TRUE;
		if (visible || focused || iconified) {
			// 先藏回去，再报
			GLFW.glfwHideWindow( windowHandle );
			Result.finish( Result.Verdict.UNSAFE,
					"window became visible/focused/iconified at " + where + " (visible=" + visible
							+ " focused=" + focused + " iconified=" + iconified + "); it was hidden again immediately",
					Log.dumpAllThreads() );
		}
	}

	@Override
	public void render() {
		checkWindowHidden( "frame " + Log.frame );

		// 只在地牢里加速：InterlevelScene 用 Game.elapsed 给关卡生成计时（超过 10 秒会上报异常），不能加速
		if (scene instanceof GameScene && !Game.switchingScene()) {
			Game.timeScale = timeScale;
		}

		// 换层出错：非 IO 类异常游戏会在渲染线程重新抛出（= 闪退，由 Launcher 抓）；
		// IO 类异常游戏只弹一个错误窗口并把异常对象丢掉，这里趁它还在先记下来
		if (scene instanceof InterlevelScene) {
			noteInterlevelError();
		}

		File shot = shotRequest;
		if (shot != null) {
			shotRequest = null;
			renderToFile( shot );
		} else {
			super.render();
		}

		if (scene instanceof InterlevelScene) {
			noteInterlevelError();
		}

		Log.frame++;
	}

	// ------------------------------------------------------------------ 截图（H.screenshot）

	private static volatile File shotRequest = null;
	private static volatile String shotResult = null;

	/** 请求把下一帧存成 PNG。 */
	static void requestShot( File target ) {
		shotResult = null;
		shotRequest = target;
	}

	/** null = 还没轮到；"ok" = 已写好；其它 = 失败原因。 */
	static String shotResult() {
		return shotResult;
	}

	/**
	 * 把这一帧画进一个离屏帧缓冲再读回来。隐藏窗口的默认帧缓冲里的像素没有保证（像素所有权测试），
	 * 离屏缓冲则与窗口是否可见无关——窗口仍然从头到尾不显示。
	 * 这一帧照常执行游戏的 draw + update，只是画的目标换了（游戏自己不绑定帧缓冲）。
	 */
	private void renderToFile( File target ) {
		final int w = Gdx.graphics.getBackBufferWidth();
		final int h = Gdx.graphics.getBackBufferHeight();
		FrameBuffer fbo;
		try {
			fbo = new FrameBuffer( Pixmap.Format.RGBA8888, w, h, false );
		} catch (Throwable t) {
			shotResult = "could not create a " + w + "x" + h + " offscreen buffer: " + t;
			super.render();
			return;
		}
		fbo.begin();
		try {
			super.render();
			Pixmap raw = null;
			Pixmap out = null;
			try {
				raw = Pixmap.createFromFrameBuffer( 0, 0, w, h );
				out = new Pixmap( w, h, Pixmap.Format.RGBA8888 );
				ByteBuffer src = raw.getPixels();
				ByteBuffer dst = out.getPixels();
				byte[] row = new byte[w * 4];
				// glReadPixels 是自下而上的；顺手把 alpha 填满（混合留下的半透明像素在看图软件里会变成「透明」）
				for (int y = 0; y < h; y++) {
					src.position( (h - 1 - y) * w * 4 );
					src.get( row );
					for (int i = 3; i < row.length; i += 4) row[i] = (byte) 0xFF;
					dst.position( y * w * 4 );
					dst.put( row );
				}
				src.position( 0 );
				dst.position( 0 );
				File dir = target.getParentFile();
				if (dir != null) dir.mkdirs();
				PixmapIO.writePNG( Gdx.files.absolute( target.getAbsolutePath() ), out );
				shotResult = "ok";
			} catch (Throwable t) {
				shotResult = "could not read back / write the frame: " + t;
			} finally {
				if (raw != null) raw.dispose();
				if (out != null) out.dispose();
			}
		} finally {
			fbo.end();
			fbo.dispose();
		}
	}

	private static volatile Object lastInterlevelError = null;

	/** 最近一次换层时游戏记下的异常（没有则 null）。 */
	public static Throwable interlevelError() {
		return (Throwable) lastInterlevelError;
	}

	private void noteInterlevelError() {
		Object err;
		try {
			err = Reflect.getStatic( InterlevelScene.class, "error" );
		} catch (Throwable t) {
			return;
		}
		if (err != null && err != lastInterlevelError) {
			lastInterlevelError = err;
			Log.stack( "InterlevelScene recorded an error while moving between floors", (Throwable) err );
		}
	}

	@Override
	protected void draw() {
		if (!skipDraw) {
			super.draw();
		}
	}

	@Override
	protected void logException( Throwable tr ) {
		Result.reported( tr );
	}

	@Override
	public void finish() {
		// 游戏自己要求退出（比如某个菜单流程）：测试里不应该发生
		Result.finish( Result.Verdict.ERROR, "the game called Game.finish() (requested exit) during the scenario",
				Log.stackOf( new Throwable( "Game.finish() called from" ) ) );
	}
}
