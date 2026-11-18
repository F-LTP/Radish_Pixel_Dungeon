package rpdtest;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Preferences;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.watabou.input.ControllerHandler;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import com.watabou.utils.GameSettings;

import java.io.File;
import java.util.Locale;

/**
 * 测试进程入口（代替游戏自带的 DesktopLauncher）。
 *
 * 与 DesktopLauncher 的区别（都是为了「不打扰正在用电脑的人」）：
 *  - 窗口从创建起就是隐藏的，永不显示、永不最大化 / 全屏，摆在屏幕外，无边框；
 *  - 音频整体禁用（libGDX 的 MockAudio，不打开任何音频设备）；
 *  - 存档 / 设置 / user.home / 临时目录全部指向本次运行的沙盒目录；
 *  - 未捕获异常不弹 tinyfd 对话框，而是写日志并以非零退出码结束；
 *  - 不初始化手柄。
 * 游戏逻辑本身用的是目标 worktree 编译出来的原样 class。
 *
 * 系统属性（由 Java 运行器或旧 PowerShell 入口传入）：
 *   rpd.scenario     场景类名（rpdtest.scenarios 包下）
 *   rpd.rundir       本次运行的目录（日志、结论、沙盒都在里面）
 *   rpd.versionName  / rpd.versionCode   目标 worktree 的版本信息
 *   rpd.timescale    地牢内的加速倍率（默认 8）
 *   rpd.fps          帧率上限（默认 120）
 *   rpd.timeout      整个进程的总超时，秒（默认 240）
 *   rpd.width / rpd.height   隐藏窗口的尺寸（默认 480x800，竖屏手机比例）
 *   rpd.skipdraw     true = 不执行绘制（默认 false）
 *   rpd.parentPid    测试运行器的进程号；它消失后本进程自行退出
 */
public final class Launcher {

	private Launcher() {}

	static volatile Thread renderThread;

	private static String prop( String key, String def ) {
		String v = System.getProperty( key );
		return (v == null || v.isEmpty()) ? def : v;
	}

	public static void main( String[] args ) {
		final String scenario = prop( "rpd.scenario", null );
		final String runDirProp = prop( "rpd.rundir", null );
		if (scenario == null || runDirProp == null) {
			System.err.println( "rpd-selftest: -Drpd.scenario and -Drpd.rundir are required (use SuiteRunner)" );
			Runtime.getRuntime().halt( Result.Verdict.ERROR.exitCode );
			return;
		}
		final File runDir = new File( runDirProp ).getAbsoluteFile();
		final File sandbox = new File( runDir, "sandbox" );
		final File home = new File( sandbox, "home" );
		final File save = new File( sandbox, "save" );
		runDir.mkdirs();
		home.mkdirs();
		save.mkdirs();

		try {
			Log.init( new File( runDir, "game.log" ) );
		} catch (Exception e) {
			System.err.println( "rpd-selftest: cannot open the log file in " + runDir + ": " + e );
			Runtime.getRuntime().halt( Result.Verdict.ERROR.exitCode );
			return;
		}
		Result.scenarioName = scenario;
		Result.runDir = runDir;
		renderThread = Thread.currentThread();

		// 任何线程（包括游戏的 Actor 线程）的未捕获异常：记完整栈，CRASH，退出。不弹窗。
		Thread.setDefaultUncaughtExceptionHandler( ( t, e ) -> Result.crash( "thread \"" + t.getName() + "\"", e ) );

		final float timeScale = Float.parseFloat( prop( "rpd.timescale", "8" ) );
		final int fps = Integer.parseInt( prop( "rpd.fps", "120" ) );
		final long timeoutSec = Long.parseLong( prop( "rpd.timeout", "240" ) );
		final int width = Integer.parseInt( prop( "rpd.width", "480" ) );
		final int height = Integer.parseInt( prop( "rpd.height", "800" ) );
		final String versionName = prop( "rpd.versionName", "0.0.0" );
		final int versionCode = Integer.parseInt( prop( "rpd.versionCode", "1" ) );

		Log.info( "rpd-selftest launcher: scenario=" + scenario + " rundir=" + runDir );
		Log.info( "java " + System.getProperty( "java.version" ) + " maxHeap=" + (Runtime.getRuntime().maxMemory() >> 20) + "M"
				+ " timescale=" + timeScale + " fps=" + fps + " timeout=" + timeoutSec + "s window=" + width + "x" + height );

		try {
			checkSandbox( sandbox, home, save );
		} catch (Throwable t) {
			Result.finish( Result.Verdict.UNSAFE, "sandbox check failed: " + t.getMessage(), Log.stackOf( t ) );
		}

		startWatchdog( timeoutSec );

		try {
			// 调试版标记：与用户手机上的版本一致（INDEV），并且 InterlevelScene 没有淡入淡出
			Game.version = versionName + "-INDEV";
			Game.versionCode = versionCode;

			// 不碰手柄（gdx-controllers 会去枚举输入设备）
			Reflect.setStatic( ControllerHandler.class, "failedInit", Boolean.TRUE );

			final String savePath = save.getAbsolutePath().replace( '\\', '/' ) + "/";

			Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
			config.setTitle( "rpd-selftest (hidden)" );
			config.setInitialVisible( false );
			config.disableAudio( true );
			config.setWindowedMode( width, height );
			config.setWindowPosition( -20000, -20000 );
			config.setDecorated( false );
			config.setResizable( false );
			config.setAutoIconify( false );
			config.useVsync( false );
			config.setForegroundFPS( fps );
			config.setIdleFPS( fps );
			config.setWindowListener( new Lwjgl3WindowAdapter() {
				@Override
				public void focusGained() {
					Result.finish( Result.Verdict.UNSAFE, "the hidden game window gained input focus", Log.dumpAllThreads() );
				}

				@Override
				public void iconified( boolean isIconified ) {
					if (isIconified) Result.finish( Result.Verdict.UNSAFE, "the hidden game window was iconified", null );
				}

				@Override
				public void maximized( boolean isMaximized ) {
					if (isMaximized) Result.finish( Result.Verdict.UNSAFE, "the hidden game window was maximized", null );
				}
			} );

			// 设置与存档：全部落在沙盒里（绝对路径）
			config.setPreferencesConfig( savePath, Files.FileType.Absolute );
			SPDSettings.set( new Lwjgl3Preferences( new Lwjgl3FileHandle( savePath + SPDSettings.DEFAULT_PREFS_FILE, Files.FileType.Absolute ) ) );
			FileUtils.setDefaultFileProperties( Files.FileType.Absolute, savePath );
			HarnessGame.sandboxSaveDir = save;

			// 预置设置：不全屏（桌面端默认全屏！）、不放开场、跳过版本迁移、中文、手机界面
			GameSettings.put( SPDSettings.KEY_FULLSCREEN, false );
			SPDSettings.intro( false );
			SPDSettings.version( versionCode );
			SPDSettings.customSeed( "" );
			SPDSettings.challenges( 0 );
			SPDSettings.detourPrompt( false );
			SPDSettings.interfaceSize( 0 );
			SPDSettings.language( Languages.CHINESE );

			HarnessGame.timeScale = timeScale;
			HarnessGame.skipDraw = Boolean.parseBoolean( prop( "rpd.skipdraw", "false" ) );

			HarnessGame game = new HarnessGame( new HiddenPlatformSupport() );
			// 第一个场景不是标题画面，而是一个空场景：起来就等场景线程接管
			Game.switchScene( BootScene.class );

			Thread st = new Thread( () -> ScenarioRunner.run( scenario ), "rpd-scenario" );
			st.setDaemon( true );
			st.start();

			// 构造函数里就是渲染主循环；渲染线程上任何未捕获异常都会从这里抛出来
			new Lwjgl3Application( game, config );

			Result.finish( Result.Verdict.ERROR, "the application main loop ended without a verdict", null );

		} catch (Throwable t) {
			Result.crash( "render thread", t );
		}
	}

	/** 沙盒护栏：宁可不跑，也不能碰到真实存档 / 设置。 */
	private static void checkSandbox( File sandbox, File home, File save ) throws Exception {
		String sb = sandbox.getCanonicalPath();
		String userHome = new File( System.getProperty( "user.home" ) ).getCanonicalPath();
		if (!userHome.equals( home.getCanonicalPath() )) {
			throw new IllegalStateException( "user.home must be redirected into the run sandbox (-Duser.home=" + home
					+ "), but it is " + userHome );
		}
		String lower = sb.toLowerCase( Locale.ROOT );
		if (lower.contains( ".shatteredpixel" )) {
			throw new IllegalStateException( "sandbox path looks like a real save directory: " + sb );
		}
		String appData = System.getenv( "APPDATA" );
		if (appData != null && !appData.isEmpty()) {
			String ad = new File( appData ).getCanonicalPath().toLowerCase( Locale.ROOT );
			if (lower.startsWith( ad )) {
				throw new IllegalStateException( "sandbox must not be inside %APPDATA%: " + sb );
			}
		}
		if (!"true".equalsIgnoreCase( System.getProperty( "java.awt.headless" ) )) {
			throw new IllegalStateException( "-Djava.awt.headless=true is required (the game has Swing dialogs on desktop)" );
		}
		File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) ).getCanonicalFile();
		String tmp = tmpDir.getPath();
		if (!tmpDir.toPath().startsWith( sandbox.getCanonicalFile().toPath() )
				&& !tmp.contains( "rpd-selftest" )) {
			throw new IllegalStateException( "java.io.tmpdir must point into the sandbox or legacy rpd-selftest cache, but it is " + tmp );
		}
		Log.info( "sandbox ok: home=" + userHome + " save=" + save.getCanonicalPath() + " tmp=" + tmp );
	}

	private static void startWatchdog( final long timeoutSec ) {
		final long parentPid = Long.parseLong( prop( "rpd.parentPid", "-1" ) );
		Thread wd = new Thread( () -> {
			final long t0 = System.nanoTime();
			long lastFrame = -1;
			long lastFrameChange = t0;
			while (true) {
				try {
					Thread.sleep( 250 );
				} catch (InterruptedException ignored) {
				}
				long now = System.nanoTime();
				long f = Log.frame;
				if (f != lastFrame) {
					lastFrame = f;
					lastFrameChange = now;
				}
				double total = (now - t0) / 1e9;
				double frozen = (now - lastFrameChange) / 1e9;
				// 运行器被强杀时，由子进程自行检测并退出，避免残留隐藏进程
				if (parentPid > 0) {
					boolean alive = ProcessHandle.of( parentPid ).map( ProcessHandle::isAlive ).orElse( false );
					if (!alive) {
						Result.finish( Result.Verdict.ERROR,
								"the runner process (pid " + parentPid + ") is gone; exiting so that no hidden game process is left behind", null );
					}
				}
				if (total > timeoutSec) {
					Result.finish( Result.Verdict.STUCK,
							"total timeout: the scenario did not finish within " + timeoutSec + " s",
							"TOTAL TIMEOUT after " + timeoutSec + " s\n" + Log.dumpAllThreads() );
				}
				// 渲染线程 60 秒没出一帧（启动阶段的第一帧含资源加载，也算在内）
				if (frozen > 60) {
					Result.finish( Result.Verdict.STUCK,
							"render thread produced no frame for 60 s (frame " + f + ")",
							"RENDER THREAD HANG: no frame for 60 s\n" + Log.dumpAllThreads() );
				}
			}
		}, "rpd-watchdog" );
		wd.setDaemon( true );
		wd.start();
	}
}
