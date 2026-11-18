package rpdtest;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;

/**
 * 启动后的第一个场景：空场景。
 * 用它代替 WelcomeScene / TitleScene，避免标题界面的联网检查更新、公告、首次启动引导等。
 * 场景线程在它就绪后再从这里发起「开新局」。
 */
public class BootScene extends PixelScene {

	@Override
	public void create() {
		super.create();
		HarnessGame.bootReady = true;
		Log.info( "BootScene ready (game created, hidden window, audio disabled)" );
	}
}
