package rpdtest;

/** 「期望的正确行为」断言不成立，且场景无法继续（H.fail / H.checkFatal 抛出）。结论记为 FAIL。 */
public class CheckFailed extends RuntimeException {

	public CheckFailed( String message ) {
		super( message );
	}
}
