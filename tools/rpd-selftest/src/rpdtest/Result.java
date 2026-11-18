package rpdtest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 场景结论与进程收尾。结论只有第一次有效；定下结论后写 result.txt / summary.txt 并直接终止进程
 * （Runtime.halt，不走游戏的正常退出流程，保证失败 / 卡死时也一定能退出）。
 */
public final class Result {

	private Result() {}

	public enum Verdict {
		/** 所有断言通过。 */
		PASS( 0 ),
		/** 「期望的正确行为」断言不成立（= 复现了 bug）。 */
		FAIL( 1 ),
		/** 未捕获异常：渲染线程或 Actor 线程（真实游戏里就是闪退）。 */
		CRASH( 2 ),
		/** 卡回合 / 渲染线程挂死 / 场景总超时。 */
		STUCK( 3 ),
		/** 场景前置条件不成立或测试工具自身出错：不是对游戏行为的结论。 */
		ERROR( 4 ),
		/** 安全护栏被触发：窗口变可见 / 抢到焦点 / 存档路径不在沙盒里。 */
		UNSAFE( 5 );

		public final int exitCode;

		Verdict( int exitCode ) {
			this.exitCode = exitCode;
		}
	}

	static volatile String scenarioName = "?";
	static volatile File runDir;

	private static final Object LOCK = new Object();
	private static volatile boolean finishing = false;

	private static final List<String> passedChecks = new ArrayList<>();
	private static final List<String> failedChecks = new ArrayList<>();
	private static final List<String> notes = new ArrayList<>();
	/** 游戏自己 catch 住并通过 Game.reportException 上报的异常（真实游戏里不会闪退，但值得看）。 */
	private static final List<String> reportedExceptions = new ArrayList<>();
	/** 被吞掉、只打印到 stderr 的异常栈的首行。 */
	private static final List<String> stderrExceptions = new ArrayList<>();

	private static final Pattern EXC_LINE = Pattern.compile( "^([\\w$]+\\.)+[\\w$]*(Exception|Error)\\b.*" );

	public static boolean finishing() {
		return finishing;
	}

	static void checkPassed( String what ) {
		synchronized (LOCK) {
			passedChecks.add( what );
		}
		Log.line( "CHECK", "ok   - " + what );
	}

	static void checkFailed( String what ) {
		synchronized (LOCK) {
			failedChecks.add( what );
		}
		Log.key( "CHECK FAILED - " + what );
	}

	static boolean hasFailedChecks() {
		synchronized (LOCK) {
			return !failedChecks.isEmpty();
		}
	}

	static int passedCheckCount() {
		synchronized (LOCK) {
			return passedChecks.size();
		}
	}

	static String firstFailedCheck() {
		synchronized (LOCK) {
			return failedChecks.isEmpty() ? null : failedChecks.get( 0 );
		}
	}

	/** 场景想写进结论摘要里的事实（实测数值等）。 */
	public static void note( String text ) {
		synchronized (LOCK) {
			notes.add( text );
		}
		Log.line( "NOTE ", text );
	}

	/** Game.reportException 走到这里（HarnessGame.logException）。 */
	static void reported( Throwable t ) {
		if (finishing) return;
		String s = Log.stackOf( t );
		synchronized (LOCK) {
			reportedExceptions.add( s );
		}
		Log.key( "REPORTED EXCEPTION (game caught it and called Game.reportException; no crash in the real game)\n" + s );
	}

	static void onConsoleLine( String stream, String line ) {
		if (finishing) return;
		if ("stderr".equals( stream ) && EXC_LINE.matcher( line ).matches()) {
			synchronized (LOCK) {
				stderrExceptions.add( line );
			}
		}
	}

	public static int reportedExceptionCount() {
		synchronized (LOCK) {
			return reportedExceptions.size();
		}
	}

	public static int stderrExceptionCount() {
		synchronized (LOCK) {
			return stderrExceptions.size();
		}
	}

	/** 未捕获异常：渲染线程 / Actor 线程 / 其它游戏线程。 */
	static void crash( String where, Throwable t ) {
		String stack = Log.stackOf( t );
		finish( Verdict.CRASH, "uncaught exception in " + where + ": " + firstLine( stack ),
				"UNCAUGHT EXCEPTION in " + where + "\n" + stack );
	}

	static String firstLine( String s ) {
		if (s == null) return "";
		int i = s.indexOf( '\n' );
		return (i < 0 ? s : s.substring( 0, i )).trim();
	}

	/**
	 * 定结论并终止进程。只有第一次调用生效；之后的调用（比如收尾过程中其它线程又抛异常）会被忽略并阻塞到进程结束。
	 *
	 * @param detail 要原样进 summary 的大段文字（异常栈 / 线程 dump），可为 null
	 */
	public static void finish( Verdict verdict, String reason, String detail ) {
		synchronized (LOCK) {
			if (finishing) {
				// 已经在收尾：其它线程不要再插手
				while (true) {
					try {
						LOCK.wait();
					} catch (InterruptedException ignored) {
					}
				}
			}
			finishing = true;
		}

		try {
			StringBuilder sb = new StringBuilder();
			sb.append( "VERDICT: " ).append( verdict ).append( '\n' );
			sb.append( "SCENARIO: " ).append( scenarioName ).append( '\n' );
			sb.append( "REASON: " ).append( oneLine( reason ) ).append( '\n' );
			sb.append( "CHECKS: " ).append( passedChecks.size() ).append( " passed, " ).append( failedChecks.size() ).append( " failed\n" );
			for (String f : failedChecks) {
				sb.append( "  FAILED: " ).append( oneLine( f ) ).append( '\n' );
			}
			for (String p : passedChecks) {
				sb.append( "  ok:     " ).append( oneLine( p ) ).append( '\n' );
			}
			for (String n : notes) {
				sb.append( "  note:   " ).append( oneLine( n ) ).append( '\n' );
			}
			sb.append( "REPORTED_EXCEPTIONS: " ).append( reportedExceptions.size() )
					.append( "  STDERR_EXCEPTION_LINES: " ).append( stderrExceptions.size() ).append( '\n' );
			sb.append( "ELAPSED_SECONDS: " ).append( String.format( java.util.Locale.ROOT, "%.1f", Log.seconds() ) )
					.append( "  FRAMES: " ).append( Log.frame ).append( '\n' );
			if (detail != null && !detail.isEmpty()) {
				sb.append( "---- detail ----\n" ).append( detail.trim() ).append( '\n' );
			}
			if (!reportedExceptions.isEmpty()) {
				sb.append( "---- reported exceptions (caught by the game, first " )
						.append( Math.min( 3, reportedExceptions.size() ) ).append( ") ----\n" );
				for (int i = 0; i < Math.min( 3, reportedExceptions.size() ); i++) {
					sb.append( reportedExceptions.get( i ) ).append( '\n' );
				}
			}
			if (!stderrExceptions.isEmpty()) {
				sb.append( "---- exception lines printed to stderr (swallowed by the game, first " )
						.append( Math.min( 5, stderrExceptions.size() ) ).append( ") ----\n" );
				for (int i = 0; i < Math.min( 5, stderrExceptions.size() ); i++) {
					sb.append( stderrExceptions.get( i ) ).append( '\n' );
				}
			}
			String summary = sb.toString();

			Log.raw( "" );
			Log.raw( "================ RESULT ================" );
			Log.raw( summary );
			Log.flush();

			if (runDir != null) {
				writeFile( new File( runDir, "summary.txt" ), summary );
				// result.txt 最后写：run.ps1 以它的存在作为「进程自己定了结论」的标志
				writeFile( new File( runDir, "result.txt" ),
						"verdict=" + verdict + "\n"
								+ "scenario=" + scenarioName + "\n"
								+ "reason=" + oneLine( reason ) + "\n"
								+ "checks_passed=" + passedChecks.size() + "\n"
								+ "checks_failed=" + failedChecks.size() + "\n"
								+ "reported_exceptions=" + reportedExceptions.size() + "\n"
								+ "stderr_exception_lines=" + stderrExceptions.size() + "\n"
								+ "elapsed_seconds=" + String.format( java.util.Locale.ROOT, "%.1f", Log.seconds() ) + "\n" );
			}
		} catch (Throwable t) {
			try {
				Log.raw( "rpdtest: failed to write result: " + t );
				Log.flush();
			} catch (Throwable ignored) {
			}
		} finally {
			Runtime.getRuntime().halt( verdict.exitCode );
		}
	}

	private static String oneLine( String s ) {
		if (s == null) return "";
		return s.replace( "\r", "" ).replace( "\n", " | " );
	}

	private static void writeFile( File f, String text ) throws Exception {
		try (Writer w = new OutputStreamWriter( new FileOutputStream( f, false ), StandardCharsets.UTF_8 )) {
			w.write( text );
		}
	}
}
