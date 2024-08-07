package com.shatteredpixel.shatteredpixeldungeon.update;

public class UpdateImpl {

	private static UpdateService updateChecker = new RDUpdateServerice();

	public static UpdateService getUpdateService(){
		return updateChecker;
	}

	public static boolean supportsUpdates(){
		return true;
	}

}
