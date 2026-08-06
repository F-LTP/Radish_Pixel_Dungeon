/*
 * Radish Pixel Dungeon
 * Corrupt Spirit - 术士4-4天赋：腐化怨灵
 */
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WraithSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CorruptSpirit extends Mob {

	{
		spriteClass = WraithSprite.class;

		HP = HT = 10 + Dungeon.depth * 2;
		EXP = 0;
		maxLvl = -2;

		flying = true;
		alignment = Alignment.ALLY;

		properties.add(Property.UNDEAD);
		properties.add(Property.INORGANIC);
	}

	private int level;
	private static final String LEVEL = "level";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEVEL, level);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		level = bundle.getInt(LEVEL);
		adjustStats(level);
	}

	@Override
	public int damageRoll() {
		return Char.combatRoll(2 + level / 2, 4 + level);
	}

	@Override
	public int attackSkill(Char target) {
		return 10 + level;
	}

	@Override
	public int defenseSkill(Char target) {
		return 5 + level;
	}

	public void adjustStats(int level) {
		this.level = level;
		defenseSkill = 5 + level;
		enemySeen = true;
	}

	@Override
	public float spawningWeight() {
		return 0f;
	}

	@Override
	public boolean reset() {
		state = WANDERING;
		return true;
	}

	@Override
	protected boolean act() {
		// 怨灵死亡时降低怪物闪避和精准
		if (!isAlive()) {
			onDeath();
		}
		return super.act();
	}

	@Override
	public void die(Object cause) {
		onDeath();
		super.die(cause);
	}

	private void onDeath() {
		if (Dungeon.hero != null && Dungeon.hero.hasTalent(Talent.CORRUPT_SPIRIT)) {
			int points = Dungeon.hero.pointsInTalent(Talent.CORRUPT_SPIRIT);
			int debuffAmount = (points == 1) ? 1 : (points == 4) ? 3 : 2;

			// 对同一层所有怪物降低闪避和精准
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (mob.alignment == Alignment.ENEMY && mob != this) {
					Buff.affect(mob, CorruptSpiritDebuff.class).setAmount(debuffAmount);
				}
			}
		}
	}

	@Override
	public String description() {
		return Messages.get(this, "desc");
	}

	/**
	 * 在指定位置附近生成腐化怨灵
	 */
	public static void spawnAround(int pos, int count) {
		ArrayList<Integer> spawnPositions = new ArrayList<>();

		for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
			int p = pos + PathFinder.NEIGHBOURS9[i];
			if (Dungeon.level.passable[p] && Actor.findChar(p) == null) {
				spawnPositions.add(p);
			}
		}

		int spawned = 0;
		for (int i = 0; i < spawnPositions.size() && spawned < count; i++) {
			CorruptSpirit spirit = new CorruptSpirit();
			int level = Dungeon.depth;
			spirit.adjustStats(level);
			spirit.HP = spirit.HT;
			spirit.pos = spawnPositions.get(i);
			Dungeon.level.occupyCell(spirit);
			Dungeon.level.mobs.add(spirit);
			Actor.add(spirit);
//			Actor.delay(spirit);
			spawned++;
		}
	}

	/**
	 * 腐化怨灵死亡时给予怪物的debuff
	 */
	public static class CorruptSpiritDebuff extends Buff {
		private int amount;

		public void setAmount(int amount) {
			this.amount = amount;
		}

		public int getAmount() {
			return amount;
		}

		@Override
		public String icon() {
			return BuffIndicator.CORRUPT_SPIRIT;
		}

		// 降低闪避和精准
		public int evasionDebuff() {
			return amount;
		}

		public int accuracyDebuff() {
			return amount;
		}
	}
}