package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * 红包祝福：目前承载“法杖不消耗充能概率”。
 * 保留 EnvelopeBoost 类名用于兼容旧存档。
 */
public class EnvelopeBoost extends Buff {

	public static final float WAND_FREE_PER_PICKUP = 0.05f;
	public static final float WAND_FREE_CAP = 1.0f;

	private float wandFreeChance = 0f;
	private int comboHits = 0;
	private int reachBonus = 0;
	private float missileReturnChance = 0f;
	private float enchantPower = 0f;
	private boolean flamewalker = false;
	private boolean knockbackOnApproach = false;
	private String allyClass = null;
	private final java.util.ArrayList<String> freeScrolls = new java.util.ArrayList<>();
	private final java.util.HashMap<String,Float> freeScrollCooldowns = new java.util.HashMap<>();
	private final java.util.ArrayList<String> freePotions = new java.util.ArrayList<>();
	private final java.util.HashMap<String,Float> freePotionCooldowns = new java.util.HashMap<>();
	private int fovDamage = 0;
	private boolean fusionUnlocked = false;
	private int fusionCharges = 0;

	public void addWandFreeChance(float amount) {
		wandFreeChance = Math.min(WAND_FREE_CAP, wandFreeChance + amount);
	}

	public float wandFreeChance() {
		return wandFreeChance;
	}

	public void addComboHits(int amount) {
		comboHits += amount;
	}

	public int comboHits() {
		return comboHits;
	}

	public static int comboHits(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0 : b.comboHits;
	}

	public void addReachBonus(int amount) {
		reachBonus += amount;
	}

	public int reachBonus() {
		return reachBonus;
	}

	public static int reachBonus(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0 : b.reachBonus;
	}

	public void addMissileReturnChance(float amount) {
		missileReturnChance = Math.min(1.0f, missileReturnChance + amount);
	}

	public float missileReturnChance() {
		return missileReturnChance;
	}

	public static boolean rollMissileReturn(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b != null && b.missileReturnChance > 0f && Random.Float() < b.missileReturnChance;
	}

	public void addEnchantPower(float amount) {
		enchantPower += amount;
	}

	public float enchantPower() {
		return enchantPower;
	}

	public static float enchantPower(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0f : b.enchantPower;
	}

	public void grantFlamewalker() {
		flamewalker = true;
	}

	public boolean hasFlamewalker() {
		return flamewalker;
	}

	public static boolean hasFlamewalker(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b != null && b.flamewalker;
	}

	public void grantKnockbackOnApproach() {
		knockbackOnApproach = true;
	}

	public static boolean hasKnockbackOnApproach(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b != null && b.knockbackOnApproach;
	}

	public boolean hasAlly() {
		return allyClass != null;
	}

	public static boolean hasAlly(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b != null && b.allyClass != null;
	}

	public static void grantFreeScroll(Hero hero) {
		EnvelopeBoost b = Buff.affect(hero, EnvelopeBoost.class);
		java.util.ArrayList<Class<? extends Scroll>> pool = new java.util.ArrayList<>();
		for (Class<?> c : Generator.Category.SCROLL.classes) {
			if (c == ScrollOfUpgrade.class || !Scroll.class.isAssignableFrom(c)) continue;
			pool.add((Class<? extends Scroll>) c);
		}
		for (Class<? extends ExoticScroll> c : ExoticScroll.regToExo.values()) {
			if (c != null) pool.add(c);
		}
		for (String selected : b.freeScrolls) {
			pool.removeIf(c -> c.getName().equals(selected));
		}
		if (pool.isEmpty()) {
			GLog.w("红包开出：所有卷轴都已免耗，本次无新增");
			return;
		}
		Class<? extends Scroll> cls = pool.get(Random.Int(pool.size()));
		b.freeScrolls.add(cls.getName());
		GLog.p("红包开出：卷轴免耗 " + Messages.get(cls, "name"));
	}

	public void addFovDamage(int amount) {
		fovDamage += amount;
	}

	public int fovDamage() {
		return fovDamage;
	}

	public void grantFusion() {
		fusionUnlocked = true;
		fusionCharges++;
	}

	public static int fusionCharges(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0 : b.fusionCharges;
	}

	public static boolean consumeFusionCharge() {
		if (Dungeon.hero == null) return false;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		if (b == null || b.fusionCharges <= 0) return false;
		b.fusionCharges--;
		return true;
	}

	public static boolean hasFusion(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b != null && b.fusionUnlocked;
	}

	public static void grantFusion(Hero hero) {
		Buff.affect(hero, EnvelopeBoost.class).grantFusion();
		GLog.p("红包开出：装备融合机会 +1（当前 " + fusionCharges(Dungeon.hero) + "）");
	}

	public static void grantFovDamage(Hero hero) {
		EnvelopeBoost b = Buff.affect(hero, EnvelopeBoost.class);
		b.addFovDamage(1);
		GLog.p("红包开出：视野内每回合真实伤害 " + b.fovDamage);
	}

	public static void grantFreePotion(Hero hero) {
		EnvelopeBoost b = Buff.affect(hero, EnvelopeBoost.class);
		java.util.ArrayList<Class<? extends Potion>> pool = new java.util.ArrayList<>();
		for (Class<?> c : Generator.Category.POTION.classes) {
			if (c == PotionOfStrength.class || c == PotionOfExperience.class
					|| !Potion.class.isAssignableFrom(c)) continue;
			pool.add((Class<? extends Potion>) c);
		}
		for (Class<? extends ExoticPotion> c : ExoticPotion.regToExo.values()) {
			if (c != null) pool.add(c);
		}
		for (String selected : b.freePotions) {
			pool.removeIf(c -> c.getName().equals(selected));
		}
		if (pool.isEmpty()) {
			GLog.w("红包开出：所有药水都已免耗，本次无新增");
			return;
		}
		Class<? extends Potion> cls = pool.get(Random.Int(pool.size()));
		b.freePotions.add(cls.getName());
		GLog.p("红包开出：药水免耗 " + Messages.get(cls, "name"));
	}

	public static boolean tryStartFreePotion(Potion potion) {
		if (Dungeon.hero == null || potion == null) return false;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		if (b == null) return false;
		String name = potion.getClass().getName();
		if (!b.freePotions.contains(name)) return false;
		float ready = b.freePotionCooldowns.getOrDefault(name, 0f);
		return Actor.now() >= ready;
	}

	public static void markFreePotionUsed(Potion potion) {
		if (Dungeon.hero == null || potion == null) return;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		if (b == null) return;
		b.freePotionCooldowns.put(potion.getClass().getName(), Actor.now() + 50f);
	}

	public static boolean isFreeScroll(Scroll scroll) {
		if (scroll == null || Dungeon.hero == null) return false;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		return b != null && b.freeScrolls.contains(scroll.getClass().getName());
	}

	public static int freeScrollRemaining(Scroll scroll) {
		if (scroll == null || Dungeon.hero == null) return 0;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		if (b == null) return 0;
		float ready = b.freeScrollCooldowns.getOrDefault(scroll.getClass().getName(), 0f);
		return Math.max(0, (int) Math.ceil(ready - Actor.now()));
	}

	public static boolean isFreePotion(Potion potion) {
		if (potion == null || Dungeon.hero == null) return false;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		return b != null && b.freePotions.contains(potion.getClass().getName());
	}

	public static int freePotionRemaining(Potion potion) {
		if (potion == null || Dungeon.hero == null) return 0;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		if (b == null) return 0;
		float ready = b.freePotionCooldowns.getOrDefault(potion.getClass().getName(), 0f);
		return Math.max(0, (int) Math.ceil(ready - Actor.now()));
	}

	public static boolean tryStartFreeScroll(Scroll scroll) {
		if (Dungeon.hero == null || scroll == null) return false;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		if (b == null) return false;
		String name = scroll.getClass().getName();
		if (!b.freeScrolls.contains(name)) return false;
		float ready = b.freeScrollCooldowns.getOrDefault(name, 0f);
		return Actor.now() >= ready;
	}

	public static void markFreeScrollUsed(Scroll scroll) {
		if (Dungeon.hero == null || scroll == null) return;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		if (b == null) return;
		b.freeScrollCooldowns.put(scroll.getClass().getName(), Actor.now() + 50f);
	}

	public static boolean isAllyClass(String className) {
		if (Dungeon.hero == null) return false;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		return b != null && b.allyClass != null && b.allyClass.equals(className);
	}

	public static void grantAlly(Hero hero) {
		EnvelopeBoost b = Buff.affect(hero, EnvelopeBoost.class);
		if (b.allyClass != null) return;

		java.util.ArrayList<Class<? extends Mob>> candidates = new java.util.ArrayList<>();
		for (int depth = 1; depth <= 26; depth++) {
			for (Class<? extends Mob> cls : Bestiary.getMobRotation(depth)) {
				if (!candidates.contains(cls)) {
					try {
						Mob m = cls.newInstance();
						if (m.properties().contains(Char.Property.BOSS)
								|| m.properties().contains(Char.Property.IMMOVABLE)) {
							continue;
						}
						candidates.add(cls);
					} catch (Exception ignored) {
					}
				}
			}
		}
		if (candidates.isEmpty()) {
			candidates.add(Rat.class);
		}
		Class<? extends Mob> cls = candidates.get(Random.Int(candidates.size()));
		b.allyClass = cls.getName();
		GLog.p("红包开出：怪物盟友 " + Messages.get(cls, "name"));

		// 当前层同种怪物立即转化
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (m.getClass() == cls) {
				Buff.affect(m, EnvelopeAllyBuff.class);
			}
		}
		spawnAlly(Dungeon.level, hero.pos);
	}

	private static final java.util.ArrayList<Mob> allyMobs = new java.util.ArrayList<>();

	public static void clearCarriedAllies() {
		allyMobs.clear();
	}

	public static void spawnFloorAlly(Level level, int pos) {
		if (Dungeon.hero == null || level == null) return;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		if (b == null || b.allyClass == null) return;
		// 先把上一层的存活盟友直接搬过来，避免首次切换时 holdAllies 失效
		for (Mob ally : allyMobs.toArray(new Mob[0])) {
			if (!ally.isAlive() || level.mobs.contains(ally)) continue;
			int at = freeCellNear(level, pos);
			if (at != -1) {
				ally.pos = at;
				ally.state = ally.WANDERING;
				GameScene.add(ally);
			}
		}
		spawnAlly(level, pos);
	}

	private static int freeCellNear(Level level, int pos) {
		for (int off : com.watabou.utils.PathFinder.NEIGHBOURS8) {
			int c = pos + off;
			if (c >= 0 && c < level.length() && level.passable[c] && !level.pit[c]
					&& com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(c) == null) {
				return c;
			}
		}
		return -1;
	}

	private static void spawnAlly(Level level, int pos) {
		if (level == null || Dungeon.hero == null) return;
		EnvelopeBoost b = Dungeon.hero.buff(EnvelopeBoost.class);
		if (b == null || b.allyClass == null) return;
		try {
			Mob mob = (Mob) Class.forName(b.allyClass).newInstance();
			int at = -1;
			for (int off : com.watabou.utils.PathFinder.NEIGHBOURS8) {
				int c = pos + off;
				if (c >= 0 && c < level.length() && level.passable[c]
						&& !level.pit[c] && com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(c) == null) {
					at = c;
					break;
				}
			}
			if (at == -1) at = pos;
			mob.pos = at;
			mob.state = mob.WANDERING;
			GameScene.add(mob);
			Buff.affect(mob, EnvelopeAllyBuff.class);
			if (!allyMobs.contains(mob)) allyMobs.add(mob);
		} catch (Exception e) {
			GLog.w("红包盟友生成失败：" + b.allyClass);
		}
	}

	/** 走过地格留下火焰；任何地格都会尝试燃烧。 */
	public static void burnWalkedCell(int cell) {
		if (Dungeon.level == null || cell < 0 || cell >= Dungeon.level.length()) return;
		if (Blob.volumeAt(cell, Fire.class) == 0) {
			GameScene.add(Blob.seed(cell, 4, Fire.class));
		}
	}

	public static float wandFreeChance(Char ch) {
		EnvelopeBoost b = ch == null ? null : ch.buff(EnvelopeBoost.class);
		return b == null ? 0f : b.wandFreeChance;
	}

	public static boolean rollFreeWandCast(Char ch) {
		float chance = wandFreeChance(ch);
		return chance > 0f && Random.Float() < chance;
	}

	@Override
	public boolean act() {
		if (fovDamage > 0 && Dungeon.hero != null && Dungeon.level != null) {
			for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (m.isAlive() && m.alignment == Char.Alignment.ENEMY
						&& Dungeon.level.heroFOV[m.pos]
						&& m.fieldOfView != null && m.fieldOfView.length == Dungeon.level.length()
						&& m.fieldOfView[Dungeon.hero.pos]) {
					m.damage(new com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo(
							fovDamage, com.shatteredpixel.shatteredpixeldungeon.damage.DamageType.PHYSICAL_NO_ARMOR,
							null, null, this));
				}
			}
		}
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
		if (wandFreeChance > 0) append(sb, "法杖不消耗充能概率", Math.round(wandFreeChance * 100) + "%");
		if (comboHits > 0) append(sb, "连击次数", "+" + comboHits);
		if (reachBonus > 0) append(sb, "攻击距离", "+" + reachBonus);
		if (missileReturnChance > 0) append(sb, "投掷物返回概率", Math.round(missileReturnChance * 100) + "%");
		if (enchantPower > 0) append(sb, "附魔触发与效果", "+" + Math.round(enchantPower * 100) + "%");
		if (flamewalker) append(sb, "烈焰人", "已获得");
		if (knockbackOnApproach) append(sb, "怪物初次贴近击飞", "已获得");
		if (allyClass != null) append(sb, "怪物盟友", "已获得");
		if (!freeScrolls.isEmpty()) append(sb, "免耗卷轴", Integer.toString(freeScrolls.size()));
		if (!freePotions.isEmpty()) append(sb, "免耗药水", Integer.toString(freePotions.size()));
		if (fovDamage > 0) append(sb, "视野内真实伤害", Integer.toString(fovDamage));
		if (fusionUnlocked) append(sb, "装备融合", "机会 " + fusionCharges);
		return sb.length() == 0 ? "尚未获得红包属性" : sb.toString();
	}

	private void append(StringBuilder sb, String key, String value) {
		if (sb.length() > 0) sb.append('\n');
		sb.append("- ").append(key).append(": ").append(value);
	}

	private static final String WAND_FREE = "wand_free_chance";
	private static final String COMBO_HITS = "combo_hits";
	private static final String REACH_BONUS = "reach_bonus";
	private static final String MISSILE_RETURN = "missile_return_chance";
	private static final String ENCHANT_POWER = "enchant_power";
	private static final String FLAMEWALKER = "flamewalker";
	private static final String KNOCKBACK_ON_APPROACH = "knockback_on_approach";
	private static final String ALLY_CLASS = "ally_class";
	private static final String FREE_SCROLLS = "free_scrolls";
	private static final String FREE_SCROLL_COOLDOWNS = "free_scroll_cooldowns";
	private static final String FREE_POTIONS = "free_potions";
	private static final String FREE_POTION_COOLDOWNS = "free_potion_cooldowns";
	private static final String FOV_DAMAGE = "fov_damage";
	private static final String FUSION_UNLOCKED = "fusion_unlocked";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(WAND_FREE, wandFreeChance);
		bundle.put(COMBO_HITS, comboHits);
		bundle.put(REACH_BONUS, reachBonus);
		bundle.put(MISSILE_RETURN, missileReturnChance);
		bundle.put(ENCHANT_POWER, enchantPower);
		bundle.put(FLAMEWALKER, flamewalker);
		bundle.put(KNOCKBACK_ON_APPROACH, knockbackOnApproach);
		if (allyClass != null) bundle.put(ALLY_CLASS, allyClass);
		bundle.put(FREE_SCROLLS, freeScrolls.toArray(new String[0]));
		Bundle cool = new Bundle();
		for (java.util.Map.Entry<String,Float> e : freeScrollCooldowns.entrySet()) {
			cool.put(e.getKey(), e.getValue());
		}
		bundle.put(FREE_SCROLL_COOLDOWNS, cool);
		bundle.put(FREE_POTIONS, freePotions.toArray(new String[0]));
		Bundle pcool = new Bundle();
		for (java.util.Map.Entry<String,Float> e : freePotionCooldowns.entrySet()) {
			pcool.put(e.getKey(), e.getValue());
		}
		bundle.put(FREE_POTION_COOLDOWNS, pcool);
		bundle.put(FOV_DAMAGE, fovDamage);
		bundle.put(FUSION_UNLOCKED, fusionUnlocked);
		bundle.put("fusion_charges", fusionCharges);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		wandFreeChance = bundle.getFloat(WAND_FREE);
		comboHits = bundle.getInt(COMBO_HITS);
		reachBonus = bundle.getInt(REACH_BONUS);
		missileReturnChance = bundle.getFloat(MISSILE_RETURN);
		enchantPower = bundle.getFloat(ENCHANT_POWER);
		flamewalker = bundle.getBoolean(FLAMEWALKER);
		knockbackOnApproach = bundle.getBoolean(KNOCKBACK_ON_APPROACH);
		allyClass = bundle.contains(ALLY_CLASS) ? bundle.getString(ALLY_CLASS) : null;
		freeScrolls.clear();
		if (bundle.contains(FREE_SCROLLS)) {
			for (String n : bundle.getStringArray(FREE_SCROLLS)) freeScrolls.add(n);
		}
		freeScrollCooldowns.clear();
		if (bundle.contains(FREE_SCROLL_COOLDOWNS)) {
			Bundle cool = bundle.getBundle(FREE_SCROLL_COOLDOWNS);
			for (String k : cool.getKeys()) freeScrollCooldowns.put(k, cool.getFloat(k));
		}
		freePotions.clear();
		if (bundle.contains(FREE_POTIONS)) {
			for (String n : bundle.getStringArray(FREE_POTIONS)) freePotions.add(n);
		}
		freePotionCooldowns.clear();
		if (bundle.contains(FREE_POTION_COOLDOWNS)) {
			Bundle cool = bundle.getBundle(FREE_POTION_COOLDOWNS);
			for (String k : cool.getKeys()) freePotionCooldowns.put(k, cool.getFloat(k));
		}
		fovDamage = bundle.getInt(FOV_DAMAGE);
		fusionUnlocked = bundle.getBoolean(FUSION_UNLOCKED);
		fusionCharges = bundle.getInt("fusion_charges");
	}
}
