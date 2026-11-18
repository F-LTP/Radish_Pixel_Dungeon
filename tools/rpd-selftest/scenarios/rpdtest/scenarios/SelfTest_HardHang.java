package rpdtest.scenarios;

import rpdtest.H;
import rpdtest.Reflect;
import rpdtest.Result;
import rpdtest.Scenario;

/**
 * 工具自测：最后一道保险——进程自己已经没法下结论时，由 run.ps1 从外面杀掉。
 *
 * 场景线程抢住「写结论」用的那把锁然后永远睡下去：进程内的看门狗到点后想判超时，但拿不到锁，
 * 进程就这么挂着（模拟 JVM 里的死锁）。run.ps1 应该在「超时 + 30 秒」后杀掉它，判 KILLED，不留下隐藏进程。
 *
 * EXPECTED-VERDICT: KILLED
 * TIMEOUT-SECONDS: 5
 */
public class SelfTest_HardHang implements Scenario {

	@Override
	public String describe() {
		return "工具自测：进程挂死、自己下不了结论时，run.ps1 应在超时后把它杀掉（KILLED）";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.log( "grabbing the result lock and sleeping forever; run.ps1 is expected to kill this process" );
		Object lock = Reflect.getStatic( Result.class, "LOCK" );
		synchronized (lock) {
			while (true) {
				try {
					Thread.sleep( 1000 );
				} catch (InterruptedException ignored) {
				}
			}
		}
	}
}
