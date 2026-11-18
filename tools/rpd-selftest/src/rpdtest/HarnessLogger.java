package rpdtest;

import com.badlogic.gdx.ApplicationLogger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 接管 Gdx.app.log / error：游戏日志（GLog 的每条消息都会以 tag "GAME" 走到这里）写进同一个日志文件。
 * 顺带记录：
 *  - 最近的游戏消息（场景可以断言「出现过 / 没出现过某条消息」）；
 *  - 缺失的文本键：调试版 + 桌面端下 Messages.get 找不到键时会 GLog "Ms:<键>"。
 */
public final class HarnessLogger implements ApplicationLogger {

	private static final List<String> gameLines = new ArrayList<>();
	private static final Set<String> missingTextKeys = new LinkedHashSet<>();

	@Override
	public void log( String tag, String message ) {
		record( tag, message );
		Log.line( "GDX  ", "[" + tag + "] " + message );
	}

	@Override
	public void log( String tag, String message, Throwable exception ) {
		record( tag, message );
		Log.line( "GDX  ", "[" + tag + "] " + message + "\n" + Log.stackOf( exception ) );
	}

	@Override
	public void error( String tag, String message ) {
		record( tag, message );
		Log.line( "GDXE ", "[" + tag + "] " + message );
	}

	@Override
	public void error( String tag, String message, Throwable exception ) {
		record( tag, message );
		Log.line( "GDXE ", "[" + tag + "] " + message + "\n" + Log.stackOf( exception ) );
	}

	@Override
	public void debug( String tag, String message ) {
		Log.line( "GDXD ", "[" + tag + "] " + message );
	}

	@Override
	public void debug( String tag, String message, Throwable exception ) {
		Log.line( "GDXD ", "[" + tag + "] " + message + "\n" + Log.stackOf( exception ) );
	}

	private static void record( String tag, String message ) {
		if (message == null) return;
		synchronized (gameLines) {
			if ("GAME".equals( tag )) {
				gameLines.add( message );
				int i = message.indexOf( "Ms:" );
				if (i >= 0) {
					missingTextKeys.add( message.substring( i + 3 ).trim() );
				}
			}
		}
	}

	/** 到目前为止的全部游戏消息（GLog）。 */
	public static List<String> gameLines() {
		synchronized (gameLines) {
			return new ArrayList<>( gameLines );
		}
	}

	public static int gameLineCount() {
		synchronized (gameLines) {
			return gameLines.size();
		}
	}

	public static List<String> missingTextKeys() {
		synchronized (gameLines) {
			return new ArrayList<>( missingTextKeys );
		}
	}
}
