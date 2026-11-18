package rpdtest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * 测试工具的日志：所有内容（工具自己的、游戏 Gdx.app.log 的、System.out / System.err 的）
 * 都写进同一个 UTF-8 日志文件，每行带「启动后秒数 / 帧号 / 线程名」。
 */
public final class Log {

	private Log() {}

	private static final long T0 = System.nanoTime();
	private static PrintStream file;
	private static PrintStream realOut;

	/** 渲染线程已经跑完的帧数（HarnessGame 每帧 +1）。 */
	public static volatile long frame = 0;

	public static synchronized void init( File logFile ) throws Exception {
		File parent = logFile.getParentFile();
		if (parent != null) parent.mkdirs();
		file = new PrintStream( new FileOutputStream( logFile, false ), true, "UTF-8" );
		realOut = System.out;
		// 游戏和第三方库直接写 System.out / System.err 的内容也收进日志
		System.setOut( new PrintStream( new LineSink( "stdout" ), true, "UTF-8" ) );
		System.setErr( new PrintStream( new LineSink( "stderr" ), true, "UTF-8" ) );
	}

	public static double seconds() {
		return (System.nanoTime() - T0) / 1e9;
	}

	private static String prefix() {
		return String.format( Locale.ROOT, "[%8.3fs f%06d %-12.12s] ", seconds(), frame, Thread.currentThread().getName() );
	}

	public static synchronized void raw( String text ) {
		if (file != null) {
			file.println( text );
		} else if (realOut != null) {
			realOut.println( text );
		} else {
			System.out.println( text );
		}
	}

	/** 普通一行。多行文本每行都带前缀，便于 grep。 */
	public static synchronized void line( String tag, String msg ) {
		String p = prefix() + tag + " ";
		if (msg == null) msg = "null";
		for (String l : msg.split( "\r?\n", -1 )) {
			raw( p + l );
		}
	}

	public static void info( String msg ) {
		line( "INFO ", msg );
	}

	public static void step( String msg ) {
		line( "STEP ", msg );
	}

	public static void warn( String msg ) {
		line( "WARN ", msg );
	}

	/** 关键行：run.ps1 在失败时会把带 "!!" 的行摘出来。 */
	public static void key( String msg ) {
		line( "!!   ", msg );
	}

	public static String stackOf( Throwable t ) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		t.printStackTrace( pw );
		pw.flush();
		return sw.toString().trim();
	}

	public static void stack( String title, Throwable t ) {
		key( title + "\n" + stackOf( t ) );
	}

	/** 所有线程的栈（卡回合 / 挂死时用）。 */
	public static String dumpAllThreads() {
		StringBuilder sb = new StringBuilder();
		try {
			ThreadInfo[] infos = ManagementFactory.getThreadMXBean().dumpAllThreads( true, true );
			for (ThreadInfo ti : infos) {
				sb.append( '"' ).append( ti.getThreadName() ).append( "\" id=" ).append( ti.getThreadId() )
						.append( " state=" ).append( ti.getThreadState() );
				if (ti.getLockName() != null) {
					sb.append( " waitingOn=" ).append( ti.getLockName() );
				}
				if (ti.getLockOwnerName() != null) {
					sb.append( " lockOwner=\"" ).append( ti.getLockOwnerName() ).append( '"' );
				}
				sb.append( '\n' );
				for (StackTraceElement e : ti.getStackTrace()) {
					sb.append( "\tat " ).append( e ).append( '\n' );
				}
				sb.append( '\n' );
			}
		} catch (Throwable t) {
			// 退回到不带锁信息的版本
			for (Map.Entry<Thread, StackTraceElement[]> en : Thread.getAllStackTraces().entrySet()) {
				sb.append( '"' ).append( en.getKey().getName() ).append( "\" state=" ).append( en.getKey().getState() ).append( '\n' );
				for (StackTraceElement e : en.getValue()) {
					sb.append( "\tat " ).append( e ).append( '\n' );
				}
				sb.append( '\n' );
			}
		}
		return sb.toString();
	}

	public static synchronized void flush() {
		if (file != null) file.flush();
	}

	/** 把按字节写进来的输出攒成整行，再走 line()。 */
	private static final class LineSink extends OutputStream {
		private final String tag;
		private final java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();

		LineSink( String tag ) {
			this.tag = tag;
		}

		@Override
		public synchronized void write( int b ) {
			if (b == '\n') {
				emit();
			} else if (b != '\r') {
				buf.write( b );
			}
		}

		@Override
		public synchronized void flush() {
			// 只在整行结束时输出，避免半行
		}

		private void emit() {
			String s = new String( buf.toByteArray(), StandardCharsets.UTF_8 );
			buf.reset();
			Result.onConsoleLine( tag, s );
			line( tag.equals( "stderr" ) ? "ERR  " : "OUT  ", s );
		}
	}
}
