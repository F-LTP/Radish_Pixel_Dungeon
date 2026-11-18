package rpdtest;

import com.shatteredpixel.shatteredpixeldungeon.desktop.DesktopPlatformSupport;

/**
 * 桌面平台支持的「隐藏窗口」版本。
 *
 * 原版 DesktopPlatformSupport.updateSystemUI() 会按设置调用 Gdx.graphics.setFullscreenMode / setWindowedMode；
 * GLFW 切全屏时会带 SWP_SHOWWINDOW 把窗口显示出来（桌面端 fullscreen 设置默认是 true），
 * 所以这里必须整个屏蔽掉：窗口的大小、可见性永远不由游戏来改。
 */
public class HiddenPlatformSupport extends DesktopPlatformSupport {

	@Override
	public void updateSystemUI() {
		// 故意什么都不做
	}

	@Override
	public boolean supportsVibration() {
		return false;
	}

	@Override
	public void vibrate( int millis ) {
		// 不震动手柄
	}

	@Override
	public boolean openURI( String uri ) {
		Log.warn( "blocked openURI: " + uri );
		return false;
	}

	@Override
	public void setOnscreenKeyboardVisible( boolean value ) {
		// 无
	}
}
