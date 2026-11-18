package rpdtest;

import java.util.List;

/** 场景线程的主体：等游戏启动 → 跑场景 → 定结论。 */
final class ScenarioRunner {

	private ScenarioRunner() {}

	/** 场景包名：run.ps1 把 scenarios/ 下的类都编译到这个包里。 */
	static final String PACKAGE = "rpdtest.scenarios.";

	static void run( String name ) {
		try {
			runInner( name );
		} catch (Throwable t) {
			// 场景线程自己的意外（不是游戏线程）：工具 / 场景的问题
			Result.finish( Result.Verdict.ERROR, "harness scenario thread died: " + t, Log.stackOf( t ) );
		}
	}

	private static void runInner( String name ) {
		// 1. 等游戏起来（第一帧包含全部资源加载）
		long t0 = System.nanoTime();
		while (!HarnessGame.bootReady) {
			sleep( 5 );
			if ((System.nanoTime() - t0) / 1_000_000_000L > 90) {
				Result.finish( Result.Verdict.STUCK, "the game did not finish booting within 90 s",
						"BOOT TIMEOUT\n" + Log.dumpAllThreads() );
			}
		}
		Log.step( String.format( java.util.Locale.ROOT, "boot ok after %.1f s", Log.seconds() ) );

		// 2. 载入场景类
		Scenario scenario;
		try {
			Class<?> cls = Class.forName( PACKAGE + name );
			scenario = (Scenario) cls.getDeclaredConstructor().newInstance();
		} catch (Throwable t) {
			Result.finish( Result.Verdict.ERROR, "cannot load scenario class " + PACKAGE + name + ": " + t, Log.stackOf( t ) );
			return;
		}
		Log.step( "scenario " + name + ": " + scenario.describe() );

		// 3. 跑
		H h = new H();
		h.noteRenderThread( Launcher.renderThread );
		try {
			scenario.run( h );
			Log.step( "scenario body returned normally" );
		} catch (CheckFailed f) {
			Log.step( "scenario aborted by a failed check: " + f.getMessage() );
		} catch (HarnessError e) {
			Result.finish( Result.Verdict.ERROR, "scenario/harness error (not a verdict about the game): " + e.getMessage(),
					Log.stackOf( e ) );
		} catch (Throwable t) {
			Result.finish( Result.Verdict.ERROR, "scenario code threw " + t, Log.stackOf( t ) );
		}

		// 4. 再让游戏跑一小会儿：延迟到后续帧才爆的异常（坏掉的精灵、延迟回调）也要算进来
		try {
			h.frames( 90 );
		} catch (HarnessError e) {
			Log.warn( "settling frames failed: " + e.getMessage() );
		}

		// 5. 结论
		List<String> missing = HarnessLogger.missingTextKeys();
		if (!missing.isEmpty()) {
			Result.note( "missing text keys seen during the run (game shows \"Ms:<key>\" in INDEV builds): " + missing );
		}
		if (Result.hasFailedChecks()) {
			Result.finish( Result.Verdict.FAIL, Result.firstFailedCheck(), null );
		} else if (Result.passedCheckCount() == 0) {
			Result.finish( Result.Verdict.ERROR, "the scenario finished without executing a single check", null );
		} else {
			Result.finish( Result.Verdict.PASS, "all " + Result.passedCheckCount() + " checks passed", null );
		}
	}

	private static void sleep( long ms ) {
		try {
			Thread.sleep( ms );
		} catch (InterruptedException ignored) {
		}
	}
}
