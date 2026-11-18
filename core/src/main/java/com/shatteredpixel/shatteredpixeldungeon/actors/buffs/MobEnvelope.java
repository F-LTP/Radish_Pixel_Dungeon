package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** 怪物捡红包后的战斗突变。 */
public class MobEnvelope extends Buff {

	public static final String RAGE = "rage";
	public static final String COMBO = "combo";
	public static final String IRON = "iron";
	public static final String THORNS = "thorns";
	public static final String HASTE = "haste";
	public static final String EVOLVE = "evolve";
	public static final String SHIELD = "shield";
	public static final String EXPLODE = "explode";

	private final java.util.HashMap<String,Integer> stacks = new java.util.HashMap<>();
	private boolean teleportUsed = false;

	public void addRandom(Mob mob) {
		// 即时收益
		mob.HP = Math.min(mob.HT, mob.HP + Math.max(1, mob.HT / 5));
		Buff.affect(mob, Barrier.class).setShield(Math.max(1, mob.HT / 10));

		String[] pool = {RAGE, COMBO, IRON, THORNS, HASTE, EVOLVE, SHIELD, EXPLODE};
		String key = pool[Random.Int(pool.length)];
		int cur = stacks.getOrDefault(key, 0);
		if (cur >= 3) {
			key = pool[Random.Int(pool.length)];
			cur = stacks.getOrDefault(key, 0);
		}
		stacks.put(key, Math.min(3, cur + 1));
		if (EVOLVE.equals(key)) {
			mob.HT = Math.round(mob.HT * 1.2f);
			mob.HP = Math.min(mob.HT, Math.round(mob.HP * 1.2f));
		}
	}

	public int stack(String key) {
		return stacks.getOrDefault(key, 0);
	}

	public static int stack(Char ch, String key) {
		MobEnvelope b = ch == null ? null : ch.buff(MobEnvelope.class);
		return b == null ? 0 : b.stack(key);
	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	@Override
	public String icon() {
		return BuffIndicator.HONGBAO;
	}

	@Override
	public String name() {
		return "红包突变";
	}

	@Override
	public String desc() {
		StringBuilder sb = new StringBuilder();
		for (java.util.Map.Entry<String,Integer> e : stacks.entrySet()) {
			if (sb.length() > 0) sb.append('\n');
			int n = e.getValue();
			sb.append("- ").append(label(e.getKey())).append(" +").append(n).append("：");
			switch (e.getKey()) {
				case RAGE: sb.append("造成的伤害 +").append(10 * n).append("%"); break;
				case COMBO: sb.append("每次攻击有 ").append(10 * n).append("% 概率追加一次半伤害攻击"); break;
				case IRON: sb.append("受到的伤害 -").append(10 * n).append("%"); break;
				case THORNS: sb.append("反弹 ").append(30 * n).append("% 近战伤害"); break;
				case HASTE: sb.append("移动速度提高约 ").append(Math.round((Math.pow(1.2, n) - 1) * 100)).append("%"); break;
				case EVOLVE: sb.append("生命上限 +").append(20 * n).append("%"); break;
				case SHIELD: sb.append("每层提供 10% 最大生命的红包护盾"); break;
				case EXPLODE: sb.append("死亡时造成 ").append(2 * n + 1).append(" 点范围火焰伤害"); break;
			}
		}
		return sb.length() == 0 ? "尚未获得突变" : sb.toString();
	}

	private static String label(String key) {
		switch (key) {
			case RAGE: return "狂暴";
			case COMBO: return "连击";
			case IRON: return "铁甲";
			case THORNS: return "荆棘";
			case HASTE: return "疾行";
			case EVOLVE: return "进化";
			case SHIELD: return "红包护盾";
			case EXPLODE: return "爆裂红包";
			default: return key;
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		for (java.util.Map.Entry<String,Integer> e : stacks.entrySet()) {
			bundle.put(e.getKey(), e.getValue());
		}
		bundle.put("teleportUsed", teleportUsed);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		stacks.clear();
		for (String k : new String[]{RAGE, COMBO, IRON, THORNS, HASTE, EVOLVE, SHIELD, EXPLODE}) {
			if (bundle.contains(k)) stacks.put(k, bundle.getInt(k));
		}
		teleportUsed = bundle.getBoolean("teleportUsed");
	}
}
