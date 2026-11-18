package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** 怪物红包突变：全局共享。 */
public class MobEnvelope extends Buff {

	public static final String RAGE = "rage";
	public static final String COMBO = "combo";
	public static final String IRON = "iron";
	public static final String THORNS = "thorns";
	public static final String HASTE = "haste";
	public static final String EVOLVE = "evolve";
	public static final String SHIELD = "shield";
	public static final String EXPLODE = "explode";

	private static final java.util.LinkedHashMap<String,Integer> GLOBAL = new java.util.LinkedHashMap<>();

	public static void resetGlobal() {
		GLOBAL.clear();
	}

	public void addRandom(Mob mob) {
		mob.HP = Math.min(mob.HT, mob.HP + Math.max(1, mob.HT / 5));
		Buff.affect(mob, Barrier.class).setShield(Math.max(1, mob.HT / 10));

		java.util.ArrayList<String> available = new java.util.ArrayList<>();
		for (String key : new String[]{RAGE, COMBO, IRON, THORNS, HASTE, EVOLVE, SHIELD, EXPLODE}) {
			if (GLOBAL.getOrDefault(key, 0) < cap(key)) available.add(key);
		}
		if (available.isEmpty()) return;
		String key = available.get(Random.Int(available.size()));
		GLOBAL.put(key, Math.min(cap(key), GLOBAL.getOrDefault(key, 0) + 1));
		if (EVOLVE.equals(key)) {
			mob.HT = Math.round(mob.HT * 1.2f);
			mob.HP = Math.min(mob.HT, Math.round(mob.HP * 1.2f));
		}
	}

	public static int cap(String key) {
		switch (key) {
			case HASTE: return 2;
			case EVOLVE: return 1;
			case EXPLODE: return 1;
			default: return 3;
		}
	}

	public static int stack(Char ch, String key) {
		return GLOBAL.getOrDefault(key, 0);
	}

	public static int stack(String key) {
		return GLOBAL.getOrDefault(key, 0);
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
		return globalDesc();
	}

	public static String globalDesc() {
		StringBuilder sb = new StringBuilder();
		for (java.util.Map.Entry<String,Integer> e : GLOBAL.entrySet()) {
			if (sb.length() > 0) sb.append('\n');
			int n = e.getValue();
			sb.append("- ").append(label(e.getKey())).append(" +").append(n).append(": ");
			switch (e.getKey()) {
				case RAGE: sb.append("造成伤害 +").append(10 * n).append("%"); break;
				case COMBO: sb.append("每次攻击 ").append(10 * n).append("% 概率追加半伤害攻击"); break;
				case IRON: sb.append("受到伤害 -").append(10 * n).append("%"); break;
				case THORNS: sb.append("反弹 ").append(30 * n).append("% 近战伤害"); break;
				case HASTE: sb.append("移动速度提高约 ").append(Math.round((Math.pow(1.2, n) - 1) * 100)).append("%"); break;
				case EVOLVE: sb.append("生命上限 +").append(20 * n).append("%"); break;
				case SHIELD: sb.append("每层 10% 最大生命护盾"); break;
				case EXPLODE: sb.append("死亡时造成 ").append(2 * n + 1).append(" 点范围火焰伤害"); break;
			}
		}
		return sb.length() == 0 ? "" : sb.toString();
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

	public static void storeGlobal(Bundle bundle) {
		bundle.put("mob_env_count", GLOBAL.size());
		int i = 0;
		for (java.util.Map.Entry<String,Integer> e : GLOBAL.entrySet()) {
			bundle.put("mob_env_key_" + i, e.getKey());
			bundle.put("mob_env_val_" + i, e.getValue());
			i++;
		}
	}

	public static void restoreGlobal(Bundle bundle) {
		GLOBAL.clear();
		int count = bundle.getInt("mob_env_count");
		for (int i = 0; i < count; i++) {
			if (bundle.contains("mob_env_key_" + i)) {
				GLOBAL.put(bundle.getString("mob_env_key_" + i), bundle.getInt("mob_env_val_" + i));
			}
		}
	}
}
