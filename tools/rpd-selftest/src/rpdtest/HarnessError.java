package rpdtest;

/**
 * 测试工具 / 场景自身的问题（前置条件不成立、世界搭建出错、输入没生效等）。
 * 结论记为 ERROR：它不是对游戏行为的判定，看到它应当先修场景或查工具。
 */
public class HarnessError extends RuntimeException {

	public HarnessError( String message ) {
		super( message );
	}

	public HarnessError( String message, Throwable cause ) {
		super( message, cause );
	}
}
