package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.challenges.Hongbao;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * 红包给英雄的属性变化。
 *
 * 百分比属性累计存储（可正可负，负值即红包在削弱英雄），
 * 攻击距离按格数累计；生命（加血/扣血）与放出怪物是立即结算的效果。
 * 所有加成都会累计到同一个 Buff 上，因此多次拾取可以正常叠加。
 */
public class EnvelopeBoost extends Buff {

	public static final String DMG = "dmg";
	public static final String DEF = "def";
	public static final String ACC = "acc";
	public static final String EVA = "eva";
	public static final String SPD = "spd";
	public static final String HP = "hp";
	public static final String ATK = "atk";
	public static final String SIGHT = "sight";
	public static final String ENCH = "ench";
	public static final String RCH = "rch";
	public static final String CHG = "chg";
	public static final String STEAL = "steal";
	public static final String COMBO = "combo";
	public static final String MRES = "mres";
	/** 立即加血/扣血，不进入持久属性列表。 */
	/** 立即放出一只当前层级可生成的怪物。 */
	public static final String MONSTER = "monster";

	/** 红包不再出现减少属性的效果。 */
	public static final float NEGATIVE_CHANCE = 0f;

	/** 属性池数量：10 项旧属性 + 充能/吸血 + 连击 + 魔抗 + 怪物。 */
	public static final int STAT_COUNT = 15;

	private float dmg, def, acc, eva, spd, hp, atk, sight, ench, chg, steal, mres;
	private int rch, combo;

	public void roll() {
		boolean curse = Random.Float() < NEGATIVE_CHANCE;
		float amount = Random.Float(0.05f, 0.10f) * (curse ? -1f : 1f);

		String stat;
		String value;
		switch (Random.Int(STAT_COUNT)) {
			case 0: dmg += amount; stat = DMG; value = signedPct(amount); break;
			case 1: def += amount; stat = DEF; value = signedPct(amount); break;
			case 2: acc += amount; stat = ACC; value = signedPct(amount); break;
			case 3: eva += amount; stat = EVA; value = signedPct(amount); break;
			case 4: spd += amount; stat = SPD; value = signedPct(amount); break;
			case 5: atk += amount; stat = ATK; value = signedPct(amount); break;
			case 6:
				hp += amount;
				stat = HP;
				value = signedPct(amount);
				applyMaxHealthChange();
				break;
			case 7: sight += amount; stat = SIGHT; value = signedPct(amount); break;
			case 8: ench += amount; stat = ENCH; value = signedPct(amount); break;
			case 9:
				// 攻击距离按格数计算，不再用百分比
				int reachDelta = (curse ? -1 : 1) * Random.IntRange(1, 2);
				rch += reachDelta;
				stat = RCH;
				value = signedInt(reachDelta);
				break;
			case 10: chg += amount; stat = CHG; value = signedPct(amount); break;
			case 11: steal += amount; stat = STEAL; value = signedPct(amount); break;
			case 12:
				// 连击次数：每次红包最多 +1
				combo++;
				stat = COMBO;
				value = signedInt(1);
				break;
			case 13:
				mres += amount;
				stat = MRES;
				value = signedPct(amount);
				break;
			default:
				// 怪物：从当前层级可生成的怪物中随机放出一只
				Hongbao.spawnMonster(target != null ? target.pos : 0);
				GLog.w(Messages.get(this, "monster"));
				return;
		}

		// 提示只显示本次变化量；Buff 描述里才是累计值
		String msg = Messages.get(this, "gain", Messages.get(this, stat), value);
		if (curse) {
			GLog.w(msg);
		} else {
			GLog.p(msg);
		}
	}

	/**
	 * 生命上限发生变化后同步当前生命。
	 * 生命上限必须在 Hero.updateHT() 中计算，否则升级重算 HT 时会把红包加成覆盖掉；
	 * 这里刷新一次并补上/扣除对应的当前生命。
	 */
	private void applyMaxHealthChange() {
		if (target == null) {
			return;
		}
		if (target instanceof Hero) {
			Hero hero = (Hero) target;
			int beforeHT = hero.HT;
			int beforeHP = hero.HP;
			hero.updateHT(false);
			int delta = hero.HT - beforeHT;
			hero.HP = Math.max(1, Math.min(hero.HT, beforeHP + delta));
		}
	}

	public float get(String stat) {
		switch (stat) {
			case DMG: return dmg;
			case DEF: return def;
			case ACC: return acc;
			case EVA: return eva;
			case SPD: return spd;
			case HP: return hp;
			case ATK: return atk;
			case SIGHT: return sight;
			case ENCH: return ench;
			case CHG: return chg;
			case STEAL: return steal;
			case COMBO: return combo;
			case MRES: return mres;
			default: return 0f;
		}
	}

	public int reachBonus() {
		return rch;
	}

	/** 取该属性带来的倍率（1 + 累计变化值），最低保留 20%。 */
	public static float mult(Char ch, String stat) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		if (b == null) {
			return 1f;
		}
		return Math.max(0.2f, 1f + b.get(stat));
	}

	/** 红包提供的额外攻击距离（格）。 */
	public static int reachBonus(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0 : b.reachBonus();
	}

	/** 吸血比例（可为负，负值代表攻击时反噬）。 */
	public static float steal(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0f : b.steal;
	}

	/** 连击次数：每次攻击后追加的额外命中次数。 */
	public static int combo(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0 : b.combo;
	}

	/** 魔抗倍率：1 - 累计魔抗，最低保留 20% 伤害。 */
	public static float magicResist(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 1f : Math.max(0.2f, 1f - b.mres);
	}

	public static String pct(float v) {
		return Math.round(v * 100) + "%";
	}

	/** 带符号的百分比：+3% / -3%，避免出现 "+-3%"。 */
	public static String signedPct(float v) {
		return (v >= 0 ? "+" : "") + pct(v);
	}

	/** 带符号的整数：+1 / -1。 */
	public static String signedInt(int v) {
		return (v >= 0 ? "+" : "") + v;
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
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		StringBuilder sb = new StringBuilder();
		appendStat(sb, DMG, dmg);
		appendStat(sb, DEF, def);
		appendStat(sb, ACC, acc);
		appendStat(sb, EVA, eva);
		appendStat(sb, SPD, spd);
		appendStat(sb, HP, hp);
		appendStat(sb, ATK, atk);
		appendStat(sb, SIGHT, sight);
		appendStat(sb, ENCH, ench);
		appendInt(sb, RCH, rch);
		appendStat(sb, CHG, chg);
		appendStat(sb, STEAL, steal);
		appendInt(sb, COMBO, combo);
		appendStat(sb, MRES, mres);
		return sb.length() == 0 ? Messages.get(this, "desc_empty") : sb.toString();
	}

	private StringBuilder appendStat(StringBuilder sb, String key, float value) {
		if (Math.abs(value) < 0.0001f) {
			return sb;
		}
		if (sb.length() > 0) {
			sb.append('\n');
		}
		return sb.append("· ").append(Messages.get(this, key)).append(' ').append(signedPct(value));
	}

	private StringBuilder appendInt(StringBuilder sb, String key, int value) {
		if (value == 0) {
			return sb;
		}
		if (sb.length() > 0) {
			sb.append('\n');
		}
		return sb.append("· ").append(Messages.get(this, key)).append(' ').append(signedInt(value));
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("dmg", dmg); bundle.put("def", def); bundle.put("acc", acc);
		bundle.put("eva", eva); bundle.put("spd", spd); bundle.put("hp", hp);
		bundle.put("atk", atk); bundle.put("sight", sight); bundle.put("ench", ench);
		bundle.put("rch", rch); bundle.put("chg", chg); bundle.put("steal", steal);
		bundle.put("combo", combo); bundle.put("mres", mres);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		dmg = bundle.getFloat("dmg"); def = bundle.getFloat("def");
		acc = bundle.getFloat("acc"); eva = bundle.getFloat("eva");
		spd = bundle.getFloat("spd"); hp = bundle.getFloat("hp");
		atk = bundle.getFloat("atk"); sight = bundle.getFloat("sight");
		ench = bundle.getFloat("ench"); rch = bundle.getInt("rch");
		chg = bundle.getFloat("chg"); steal = bundle.getFloat("steal");
		combo = bundle.getInt("combo"); mres = bundle.getFloat("mres");
	}
}
