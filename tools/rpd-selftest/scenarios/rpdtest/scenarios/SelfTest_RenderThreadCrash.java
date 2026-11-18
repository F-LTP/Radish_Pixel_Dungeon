package rpdtest.scenarios;

import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;

import rpdtest.H;
import rpdtest.Scenario;

/**
 * 工具自测：渲染线程上、在普通一帧的 update 里抛出的未捕获异常（不是在玩家输入的那一下）。
 *
 * 往场景里加一个 update() 会抛异常的元素：下一帧渲染循环就会带着异常退出（真实游戏 = 闪退）。
 * 工具应该判 CRASH，并给出完整栈。
 *
 * EXPECTED-VERDICT: CRASH
 * EXPECTED-REASON: uncaught exception in render thread: java.lang.IllegalStateException: deliberate exception in a scene element
 * TIMEOUT-SECONDS: 90
 */
public class SelfTest_RenderThreadCrash implements Scenario {

	@Override
	public String describe() {
		return "工具自测：渲染线程某一帧的 update 里抛出的异常，应被判为 CRASH 并带完整栈";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.world( "add a scene element whose update() throws", () -> Game.scene().add( new Gizmo() {
			@Override
			public void update() {
				throw new IllegalStateException( "deliberate exception in a scene element's update() (rpd-selftest self-test)" );
			}
		} ) );
		h.frames( 30 );
		h.check( false, "不应该走到这里：渲染循环应该已经带着异常退出" );
	}
}
