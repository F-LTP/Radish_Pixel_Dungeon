package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.challenges.Hongbao;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * 怪物拾取红包后的属性强化记录。
 * 怪物只会获得强化，不会像英雄一样被红包削弱。
 */
public class MobEnvelope extends Buff {

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
	/** 立即回血，不进入持久属性列表。 */
	public static final String LIFE = "life";
	/** 立即放出一只当前层级可生成的怪物。 */
	public static final String MONSTER = "monster";

	/** 属性池数量：10 项旧属性 + 充能/攻击次数/吸血 + 立即回血 + 怪物。 */
	public static final int STAT_COUNT = 15;

	private int count = 0;
	private float dmg = 0f, def = 0f, acc = 0f, eva = 0f, spd = 0f, hp = 0f, atk = 0f, sight = 0f, ench = 0f;
	private float chg = 0f, apt = 0f, steal = 0f;
	private int rch = 0;

	public int count() {
		return count;
	}

	private float roll() {
		return Random.Float(0.01f, 0.05f);
	}

	/**
	 * 无法移动的怪物不拾取红包、也不承受红包效果。
	 * 直接在 attachTo 层拦截，旧存档里的残留 Buff 重新挂载时也会被丢弃。
	 */
	public static boolean affects(Char ch) {
		return ch != null && !Char.hasProp(ch, Char.Property.IMMOVABLE);
	}

	@Override
	public boolean attachTo(Char target) {
		if (!affects(target)) {
			return false;
		}
		return super.attachTo(target);
	}

	/** 记录拾取到一个红包（不增加属性）。 */
	public void recordPickup() {
		if (affects(target)) {
			count++;
		}
	}

	public void addRandom() {
		if (!affects(target)) {
			return;
		}
		switch (Random.Int(STAT_COUNT)) {
			case 0: dmg += roll(); break;
			case 1: def += roll(); break;
			case 2: acc += roll(); break;
			case 3: eva += roll(); break;
			case 4: spd += roll(); break;
			case 5: atk += roll(); break;
			case 6:
				float pct = roll();
				hp += pct;
				int gain = Math.max(1, Math.round(target.HT * pct));
				target.HT += gain;
				target.HP += gain;
				break;
			case 7: sight += roll(); break;
			case 8: ench += roll(); break;
			case 9: rch += Random.IntRange(1, 2); break;
			case 10: chg += roll(); break;
			case 11: apt += Random.Float(0.1f, 0.3f); break;
			case 12: steal += roll(); break;
			case 13:
				int heal = Math.max(1, Math.round(target.HT * roll()));
				target.HP = Math.min(target.HT, target.HP + heal);
				break;
			default:
				Hongbao.spawnMonster(target != null ? target.pos : 0);
				break;
		}
	}

	public float get(String stat) {
		switch (stat) {
			case DMG: return dmg;
			case DEF: return def;
			case ACC: return acc;
			case EVA: return eva;
			case SPD: return spd;
			case ATK: return atk;
			case HP: return hp;
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

	/** 取该属性带来的倍率（1 + 累计强化值），最低保留 20%。 */
	public static float mult(Char ch, String stat) {
		if (!affects(ch)) {
			return 1f;
		}
		MobEnvelope b = ch.buff(MobEnvelope.class);
		if (b == null) {
			return 1f;
		}
		return Math.max(0.2f, 1f + b.get(stat));
	}

	/** 怪物从红包获得的额外攻击距离（格）。 */
	public static int reachBonus(Char ch) {
		if (!affects(ch)) {
			return 0;
		}
		MobEnvelope b = ch.buff(MobEnvelope.class);
		return b == null ? 0 : b.reachBonus();
	}

	/** 每回合攻击次数倍率：1 + 额外次数；负数会拉长攻击间隔。 */
	public static float attackCountMult(Char ch) {
		if (!affects(ch)) {
			return 1f;
		}
		MobEnvelope b = ch.buff(MobEnvelope.class);
		return b == null ? 1f : Math.max(0.2f, 1f + b.apt);
	}

	/** 吸血比例。怪物只取正值。 */
	public static float steal(Char ch) {
		if (!affects(ch)) {
			return 0f;
		}
		MobEnvelope b = ch.buff(MobEnvelope.class);
		return b == null ? 0f : Math.max(0f, b.steal);
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
		return Messages.get(this, "desc", count,
				pct(dmg), pct(def), pct(acc), pct(eva), pct(spd), pct(hp), pct(atk),
				pct(sight), pct(ench), signedInt(rch), pct(chg),
				EnvelopeBoost.attackText(apt), pct(steal));
	}

	public static String pct(float v) {
		return Math.round(v * 100) + "%";
	}

	public static String signedInt(int v) {
		return (v >= 0 ? "+" : "") + v;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("count", count);
		bundle.put("dmg", dmg);
		bundle.put("def", def);
		bundle.put("acc", acc);
		bundle.put("eva", eva);
		bundle.put("spd", spd);
		bundle.put("hp", hp);
		bundle.put("atk", atk);
		bundle.put("sight", sight);
		bundle.put("ench", ench);
		bundle.put("rch", rch);
		bundle.put("chg", chg);
		bundle.put("apt", apt);
		bundle.put("steal", steal);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		count = bundle.getInt("count");
		dmg = bundle.getFloat("dmg");
		def = bundle.getFloat("def");
		acc = bundle.getFloat("acc");
		eva = bundle.getFloat("eva");
		spd = bundle.getFloat("spd");
		hp = bundle.getFloat("hp");
		atk = bundle.getFloat("atk");
		sight = bundle.getFloat("sight");
		ench = bundle.getFloat("ench");
		rch = bundle.getInt("rch");
		chg = bundle.getFloat("chg");
		apt = bundle.getFloat("apt");
		steal = bundle.getFloat("steal");
	}
}
