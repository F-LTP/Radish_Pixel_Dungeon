package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.CycleTestLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 测试工具：直接进入循环门测试支线。
 */
public class CycleTestPortal extends TestItem {

	private static final String AC_ENTER = "ENTER";

	{
		image = ItemSpriteSheet.AMULET;
		defaultAction = AC_ENTER;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_ENTER);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (!AC_ENTER.equals(action)) {
			return;
		}
		if (Branches.TEST.equals(Dungeon.branchId)) {
			GLog.w(Messages.get(this, "already"));
			return;
		}

		try {
			CycleTestLevel.pendingReturnDepth = Dungeon.depth;
			Level level = Dungeon.loadOrCreateLevel(GamesInProgress.curSlot, Branches.TEST, 1);
			Dungeon.switchLevel(level, CycleTestLevel.ENTRANCE_CELL);
		} catch (IOException e) {
			GLog.w(Messages.get(this, "failed"));
		}
	}
}