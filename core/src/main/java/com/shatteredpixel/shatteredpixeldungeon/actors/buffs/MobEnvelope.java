package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.challenges.Hongbao;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * 怪物拾取红包后的全局强化记录。
 * 属性由本局所有怪物共享、永久保留，新生成怪物也会继承；装备仍是拾取者本地使用。
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
	public static final String STEAL = "steal";
	public static final String COMBO = "combo";

	/** 属性池数量：11 项旧属性 + 充能/吸血/连击 + 立即回血。 */
	public static final int STAT_COUNT = 14;

	private static final String G_DMG = "g_mobenv_dmg";
	private static final String G_DEF = "g_mobenv_def";
	private static final String G_ACC = "g_mobenv_acc";
	private static final String G_EVA = "g_mobenv_eva";
	private static final String G_SPD = "g_mobenv_spd";
	private static final String G_HP = "g_mobenv_hp";
	private static final String G_ATK = "g_mobenv_atk";
	private static final String G_SIGHT = "g_mobenv_sight";
	private static final String G_ENCH = "g_mobenv_ench";
	private static final String G_RCH = "g_mobenv_rch";
	private static final String G_CHG = "g_mobenv_chg";
	private static final String G_STEAL = "g_mobenv_steal";
	private static final String G_COMBO = "g_mobenv_combo";
	private static final String G_PICKUPS = "g_mobenv_pickups";

	private static float gDmg, gDef, gAcc, gEva, gSpd, gHp, gAtk, gSight, gEnch, gChg, gSteal;
	private static int gRch, gCombo, gPickups;

	private int count = 0;

	public int count() {
		return count;
	}

	private float roll() {
		return Random.Float(0.01f, 0.05f);
	}

	public static boolean affects(Char ch) {
		return ch != null
				&& ch.alignment == Char.Alignment.ENEMY
				&& !Char.hasProp(ch, Char.Property.IMMOVABLE);
	}

	@Override
	public boolean attachTo(Char target) {
		if (!affects(target)) {
			return false;
		}
		return super.attachTo(target);
	}

	/** 记录拾取到一个红包。 */
	public void recordPickup() {
		if (affects(target)) {
			count++;
			gPickups++;
		}
	}

	public void addRandom() {
		if (!affects(target)) {
			return;
		}
		switch (Random.Int(STAT_COUNT)) {
			case 0: gDmg += roll(); break;
			case 1: gDef += roll(); break;
			case 2: gAcc += roll(); break;
			case 3: gEva += roll(); break;
			case 4: gSpd += roll(); break;
			case 5: gAtk += roll(); break;
			case 6:
				float pct = roll();
				float oldMult = Math.max(0.2f, 1f + gHp);
				gHp += pct;
				float newMult = Math.max(0.2f, 1f + gHp);
				if (Dungeon.level != null) {
					for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
						if (mob.alignment == Char.Alignment.ENEMY && affects(mob)) {
							int before = mob.HT;
							mob.HT = Math.max(1, Math.round(mob.HT / oldMult * newMult));
							mob.HP = Math.min(mob.HT, mob.HP + Math.max(0, mob.HT - before));
						}
					}
				}
				break;
			case 7: gSight += roll(); break;
			case 8: gEnch += roll(); break;
			case 9: gRch += Random.IntRange(1, 2); break;
			case 10: gChg += roll(); break;
			case 11: gSteal += roll(); break;
			case 12: gCombo += 1; break;
			default:
				int heal = Math.max(1, Math.round(target.HT * roll()));
				target.HP = Math.min(target.HT, target.HP + heal);
				break;
		}
	}

	public float get(String stat) {
		switch (stat) {
			case DMG: return gDmg;
			case DEF: return gDef;
			case ACC: return gAcc;
			case EVA: return gEva;
			case SPD: return gSpd;
			case HP: return gHp;
			case ATK: return gAtk;
			case SIGHT: return gSight;
			case ENCH: return gEnch;
			case CHG: return gChg;
			case STEAL: return gSteal;
			case COMBO: return gCombo;
			default: return 0f;
		}
	}

	public int reachBonus() {
		return gRch;
	}

	public static float mult(Char ch, String stat) {
		if (!affects(ch)) {
			return 1f;
		}
		float value;
		switch (stat) {
			case DMG: value = gDmg; break;
			case DEF: value = gDef; break;
			case ACC: value = gAcc; break;
			case EVA: value = gEva; break;
			case SPD: value = gSpd; break;
			case HP: value = gHp; break;
			case ATK: value = gAtk; break;
			case SIGHT: value = gSight; break;
			case ENCH: value = gEnch; break;
			case CHG: value = gChg; break;
			case STEAL: value = gSteal; break;
			case COMBO: value = gCombo; break;
			default: value = 0f; break;
		}
		return Math.max(0.2f, 1f + value);
	}

	public static int reachBonus(Char ch) {
		return affects(ch) ? gRch : 0;
	}

	public static float steal(Char ch) {
		return affects(ch) ? Math.max(0f, gSteal) : 0f;
	}

	public static int combo(Char ch) {
		return affects(ch) ? gCombo : 0;
	}

	public static int globalPickups() {
		return gPickups;
	}

	public static boolean hasGlobalGains() {
		return gPickups > 0;
	}

	/** 新生成/重新加入的怪物继承已累计的生命上限强化。 */
	public static void applyGlobalHP(Mob mob) {
		if (!affects(mob)) {
			return;
		}
		mob.HT = Math.max(1, Math.round(mob.HT * Math.max(0.2f, 1f + gHp)));
		mob.HP = Math.min(mob.HT, mob.HP);
	}

	public static void resetGlobal() {
		gDmg = gDef = gAcc = gEva = gSpd = gHp = gAtk = gSight = gEnch = gChg = gSteal = 0f;
		gRch = gCombo = gPickups = 0;
	}

	public static void storeGlobal(Bundle bundle) {
		bundle.put(G_DMG, gDmg); bundle.put(G_DEF, gDef); bundle.put(G_ACC, gAcc);
		bundle.put(G_EVA, gEva); bundle.put(G_SPD, gSpd); bundle.put(G_HP, gHp);
		bundle.put(G_ATK, gAtk); bundle.put(G_SIGHT, gSight); bundle.put(G_ENCH, gEnch);
		bundle.put(G_RCH, gRch); bundle.put(G_CHG, gChg); bundle.put(G_STEAL, gSteal);
		bundle.put(G_COMBO, gCombo); bundle.put(G_PICKUPS, gPickups);
	}

	public static void restoreGlobal(Bundle bundle) {
		gDmg = bundle.getFloat(G_DMG); gDef = bundle.getFloat(G_DEF);
		gAcc = bundle.getFloat(G_ACC); gEva = bundle.getFloat(G_EVA);
		gSpd = bundle.getFloat(G_SPD); gHp = bundle.getFloat(G_HP);
		gAtk = bundle.getFloat(G_ATK); gSight = bundle.getFloat(G_SIGHT);
		gEnch = bundle.getFloat(G_ENCH); gRch = bundle.getInt(G_RCH);
		gChg = bundle.getFloat(G_CHG); gSteal = bundle.getFloat(G_STEAL);
		gCombo = bundle.getInt(G_COMBO); gPickups = bundle.getInt(G_PICKUPS);
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
		return Messages.get(this, "header", count) + "\n" + globalSummary();
	}

	public static String globalDesc() {
		return globalSummary();
	}

	private static String globalSummary() {
		StringBuilder sb = new StringBuilder();
		appendStat(sb, DMG, gDmg);
		appendStat(sb, DEF, gDef);
		appendStat(sb, ACC, gAcc);
		appendStat(sb, EVA, gEva);
		appendStat(sb, SPD, gSpd);
		appendStat(sb, HP, gHp);
		appendStat(sb, ATK, gAtk);
		appendStat(sb, SIGHT, gSight);
		appendStat(sb, ENCH, gEnch);
		appendInt(sb, RCH, gRch);
		appendStat(sb, CHG, gChg);
		appendStat(sb, STEAL, gSteal);
		appendInt(sb, COMBO, gCombo);
		return sb.length() == 0 ? Messages.get(MobEnvelope.class, "desc_empty") : sb.toString();
	}

	private static StringBuilder appendStat(StringBuilder sb, String key, float value) {
		if (Math.abs(value) < 0.0001f) {
			return sb;
		}
		if (sb.length() > 0) {
			sb.append(Messages.get(EnvelopeBoost.class, "sep"));
		}
		return sb.append(Messages.get(EnvelopeBoost.class, key)).append(' ').append(pct(value));
	}

	private static StringBuilder appendInt(StringBuilder sb, String key, int value) {
		if (value == 0) {
			return sb;
		}
		if (sb.length() > 0) {
			sb.append(Messages.get(EnvelopeBoost.class, "sep"));
		}
		return sb.append(Messages.get(EnvelopeBoost.class, key)).append(' ').append(signedInt(value));
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
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		count = bundle.getInt("count");
	}
}
