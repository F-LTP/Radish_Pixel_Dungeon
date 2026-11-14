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
	public static final String APT = "apt";
	public static final String STEAL = "steal";
	/** 立即加血/扣血，不进入持久属性列表。 */
	public static final String LIFE = "life";
	/** 立即放出一只当前层级可生成的怪物。 */
	public static final String MONSTER = "monster";

	/** 英雄开红包时，属性被削弱（负值）的概率。 */
	public static final float NEGATIVE_CHANCE = 0.35f;

	/** 属性池数量：10 项旧属性 + 充能/攻击次数/吸血 + 立即加血扣血 + 怪物。 */
	public static final int STAT_COUNT = 15;

	private float dmg, def, acc, eva, spd, hp, atk, sight, ench, chg, apt, steal;
	private int rch;

	public void roll() {
		boolean curse = Random.Float() < NEGATIVE_CHANCE;
		float amount = Random.Float(0.01f, 0.05f) * (curse ? -1f : 1f);

		String stat;
		String value;
		switch (Random.Int(STAT_COUNT)) {
			case 0: dmg += amount; stat = DMG; value = signedPct(dmg); break;
			case 1: def += amount; stat = DEF; value = signedPct(def); break;
			case 2: acc += amount; stat = ACC; value = signedPct(acc); break;
			case 3: eva += amount; stat = EVA; value = signedPct(eva); break;
			case 4: spd += amount; stat = SPD; value = signedPct(spd); break;
			case 5: atk += amount; stat = ATK; value = signedPct(atk); break;
			case 6:
				hp += amount;
				stat = HP;
				value = signedPct(hp);
				applyMaxHealthChange();
				break;
			case 7: sight += amount; stat = SIGHT; value = signedPct(sight); break;
			case 8: ench += amount; stat = ENCH; value = signedPct(ench); break;
			case 9:
				// 攻击距离按格数计算，不再用百分比
				rch += (curse ? -1 : 1) * Random.IntRange(1, 2);
				stat = RCH;
				value = signedInt(rch);
				break;
			case 10: chg += amount; stat = CHG; value = signedPct(chg); break;
			case 11:
				// 每回合攻击次数：正数表示每回合额外攻击次数；负数表示攻击间隔
				apt += Random.Float(0.1f, 0.3f) * (curse ? -1f : 1f);
				stat = APT;
				value = attackText(apt);
				break;
			case 12: steal += amount; stat = STEAL; value = signedPct(steal); break;
			case 13:
				// 加血扣血：立即改变当前生命，不改变生命上限
				int life = Math.max(1, Math.round(target == null ? 1 : target.HT * Math.abs(amount)));
				if (curse) {
					life = -life;
				}
				applyLifeChange(life);
				stat = LIFE;
				value = signedInt(life);
				break;
			default:
				// 怪物：从当前层级可生成的怪物中随机放出一只
				Hongbao.spawnMonster();
				GLog.w(Messages.get(this, "monster"));
				return;
		}

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

	private void applyLifeChange(int amount) {
		if (target == null) {
			return;
		}
		if (amount > 0) {
			target.HP = Math.min(target.HT, target.HP + amount);
		} else {
			target.HP = Math.max(1, target.HP + amount);
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
			case APT: return apt;
			case STEAL: return steal;
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

	/** 每回合攻击次数倍率：1 + 额外次数；负数会拉长攻击间隔。 */
	public static float attackCountMult(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		if (b == null) {
			return 1f;
		}
		return Math.max(0.2f, 1f + b.apt);
	}

	/** 吸血比例（可为负，负值代表攻击时反噬）。 */
	public static float steal(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0f : b.steal;
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

	/** 攻击次数文案：正数显示每回合额外次数，负数显示攻击一次需要的回合数。 */
	public static String attackText(float apt) {
		if (apt >= 0f) {
			return Messages.get(EnvelopeBoost.class, "apt_more",
					Messages.decimalFormat("#.##", apt));
		}
		float turns = 1f / Math.max(0.2f, 1f + apt);
		return Messages.get(EnvelopeBoost.class, "apt_less",
				Messages.decimalFormat("#.##", turns));
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
		return Messages.get(this, "desc",
				signedPct(dmg), signedPct(def), signedPct(acc), signedPct(eva), signedPct(spd),
				signedPct(hp), signedPct(atk), signedPct(sight), signedPct(ench), signedInt(rch),
				signedPct(chg), attackText(apt), signedPct(steal));
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("dmg", dmg); bundle.put("def", def); bundle.put("acc", acc);
		bundle.put("eva", eva); bundle.put("spd", spd); bundle.put("hp", hp);
		bundle.put("atk", atk); bundle.put("sight", sight); bundle.put("ench", ench);
		bundle.put("rch", rch); bundle.put("chg", chg); bundle.put("apt", apt);
		bundle.put("steal", steal);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		dmg = bundle.getFloat("dmg"); def = bundle.getFloat("def");
		acc = bundle.getFloat("acc"); eva = bundle.getFloat("eva");
		spd = bundle.getFloat("spd"); hp = bundle.getFloat("hp");
		atk = bundle.getFloat("atk"); sight = bundle.getFloat("sight");
		ench = bundle.getFloat("ench"); rch = bundle.getInt("rch");
		chg = bundle.getFloat("chg"); apt = bundle.getFloat("apt");
		steal = bundle.getFloat("steal");
	}
}
