package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins.JumbleSkin;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfConcealment;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Wheelchair;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.JumbleSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.timing.VirtualTimer;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JumbleChangeBuff extends Buff {

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	// 距下次变身的剩余回合数
	private float turnsRemaining;

	// 是否正在变身中（防止重入）
	private volatile boolean changing = false;

	// 当前外观组是变身结果的一部分，必须独立于精灵动画持久化。
	private volatile int currentGroup = 0;
	private transient volatile int visualChangeToken = 0;

	/** Shared by transformation commit and Hero serialization. */
	public static final Object STATE_LOCK = new Object();

	private static final float MIN_INTERVAL = 180;
	private static final float MAX_INTERVAL = 220;
	// 两段各 10 帧、12 FPS，再留半秒余量；仅用于解锁，不参与游戏计时。
	private static final float VISUAL_TIMEOUT = 20f / 12f + 0.5f;

	private static final String TURNS = "turns";
	private static final String GROUP = "group";

	public int currentGroup() {
		return currentGroup;
	}

	@Override
	public boolean act() {
		synchronized (STATE_LOCK) {
			if (target == Dungeon.hero && isJumbleActive() && !changing) {
				turnsRemaining -= 1;
				if (turnsRemaining <= 0) {
					changing = true;
					Hero hero = (Hero) target;
					hero.interrupt();
					hero.spend(TICK);
					startChange();
				}
			}
		}
		spend( TICK );
		return true;
	}

	/** 当前英雄是否为杂散皮肤。 */
	public static boolean isJumbleActive() {
		return Dungeon.hero != null
				&& Dungeon.hero.heroClass.activeSkin() instanceof JumbleSkin;
	}

	/** 附加或重置变身倒计时（持久化 buff，恒存在）。 */
	public static void resetCountdown() {
		synchronized (STATE_LOCK) {
			if (Dungeon.hero == null) return;
			JumbleChangeBuff b = Buff.affect(Dungeon.hero, JumbleChangeBuff.class);
			b.turnsRemaining = Random.NormalIntRange((int) MIN_INTERVAL, (int) MAX_INTERVAL);
		}
	}

	/** 若变身倒计时 buff 尚未存在，则创建并重置倒计时；已存在则不动。 */
	public static void resetCountdownIfMissing() {
		synchronized (STATE_LOCK) {
			if (Dungeon.hero == null) return;
			JumbleChangeBuff b = Dungeon.hero.buff(JumbleChangeBuff.class);
			if (b == null) {
				resetCountdown();
			}
		}
	}

	@Override
	public void detach() {
		//不可被驱散或移除：变身倒计时 buff 全程持续
		//do nothing
	}

	@Override
	public String icon() {
		return BuffIndicator.TIME;
	}

	@Override
	public String toString() {
		return name();
	}

	@Override
	public float iconFadePercent() {
		//随倒计时临近逐渐淡出
		return Math.max(0f, 1f - (turnsRemaining / MAX_INTERVAL));
	}

	@Override
	public String desc() {
		return super.desc() + "\n\n" + Messages.get(this, "countdown", (int) turnsRemaining);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		synchronized (STATE_LOCK) {
			super.storeInBundle(bundle);
			bundle.put(TURNS, turnsRemaining);
			bundle.put(GROUP, currentGroup);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		synchronized (STATE_LOCK) {
			super.restoreFromBundle(bundle);
			turnsRemaining = bundle.getFloat(TURNS);
			currentGroup = ((bundle.getInt(GROUP) % 6) + 6) % 6;
			// 动画和回调不属于存档协议。读档后直接显示已提交的变身结果。
			changing = false;
			visualChangeToken++;
		}
	}

	// ---- 变身流程 ----

	private void startChange() {
		Hero hero = (Hero) target;
		// 变身提示
		GLog.w(Messages.get(JumbleChangeBuff.class, "transform"));

		// 先提交全部逻辑结果。动画只展示这次已经确定的变身，不再决定变身是否成功。
		final int token;
		synchronized (STATE_LOCK) {
			try {
				doTalentMetamorph(hero);
			} catch (Throwable t) {
				logMetamorphError(t);
			}
			try {
				doEquipmentTransmute(hero);
			} catch (Throwable t) {
				logMetamorphError(t);
			}
			currentGroup = Random.Int(6);
			resetCountdown();
			token = ++visualChangeToken;
		}

		if (hero.sprite instanceof JumbleSprite) {
			JumbleSprite sprite = (JumbleSprite) hero.sprite;

			// 阻塞仍保留为演出效果；超时只负责解锁，不改变已提交的结果。
			try {
				hero.playAnimationNoTime(new Callback() {
					@Override
					public void call() {
						try {
							sprite.playChange(new Callback() {
								@Override
								public void call() {
									try {
										sprite.playAppear(currentGroup, new Callback() {
											@Override
											public void call() {
												finishVisualChange(token);
											}
										});
									} catch (Throwable t) {
										logMetamorphError(t);
										finishVisualChange(token);
									}
								}
							});
						} catch (Throwable t) {
							logMetamorphError(t);
							finishVisualChange(token);
						}
					}
				});
				Game.runOnRenderThread(() -> {
					try {
						VirtualTimer.countTime(VISUAL_TIMEOUT, () -> finishVisualChange(token));
					} catch (Throwable t) {
						logMetamorphError(t);
						finishVisualChange(token);
					}
				});
			} catch (Throwable t) {
				logMetamorphError(t);
				finishVisualChange(token);
			}
		} else {
			finishVisualChange(token);
		}
	}

	private void logMetamorphError(Throwable t) {
		com.watabou.utils.DeviceCompat.log("JumbleChangeBuff", "metamorph error: " + t);
		t.printStackTrace();
	}

	private void finishVisualChange(int token) {
		Hero hero;
		synchronized (STATE_LOCK) {
			if (!changing || token != visualChangeToken) return;
			changing = false;
			hero = (Hero) target;
		}
		if (hero != null) {
			if (hero.sprite instanceof JumbleSprite) {
				JumbleSprite sprite = (JumbleSprite) hero.sprite;
				sprite.cancelAnimationCallback();
				sprite.setGroup(currentGroup);
				if (hero.isAlive()) {
					sprite.idle();
				}
			}
			if (hero.animationBusy) {
				hero.finishAnimationNoTime();
			}
		}
	}

	// ---- 天赋替换：逐天赋换成随机角色的随机天赋 ----

	private void doTalentMetamorph(Hero hero) {
		// Only regular talent tiers metamorph. Armor ability talents (tier 4) stay unchanged.
		for (int tier = 0; tier < Math.min(3, hero.talents.size()); tier++) {
			LinkedHashMap<Talent, Integer> oldTier = hero.talents.get(tier);
			if (oldTier == null || oldTier.isEmpty()) continue;

			LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();
			List<Talent> upgradedTalents = new ArrayList<>();

			for (Talent oldTalent : oldTier.keySet()) {
				int points = oldTier.get(oldTalent);
				try {
					//所有普通层天赋都参与蜕变；点数仅随天赋槽位保留，不影响替换流程。
					Talent replacement = randomTalent(hero, oldTalent, tier);
					if (replacement != oldTalent) {
						recordMetamorph(hero, oldTalent, replacement);
						upgradedTalents.add(replacement);
					}
					newTier.put(replacement, points);
				} catch (Throwable t) {
					// 单个槽位失败时保留原天赋；本次变身仍视为已消耗，绝不重试。
					newTier.put(oldTalent, points);
					logMetamorphError(t);
				}
			}
			hero.talents.set(tier, newTier);
			// 回调必须在新层写回后执行，否则 pointsInTalent(replacement) 会错误地返回 0。
			for (Talent talent : upgradedTalents) {
				try {
					Talent.onTalentUpgraded(hero, talent);
				} catch (Throwable t) {
					logMetamorphError(t);
				}
			}
		}
	}

	/** 与蜕变秘卷一致，只从其他职业的同阶天赋中选择替代项。 */
	private Talent randomTalent(Hero hero, Talent oldTalent, int tierIndex) {
		List<Talent> pool = new ArrayList<>();
		Set<Talent> alreadyUsed = new LinkedHashSet<>(hero.metamorphedTalents.values());
		Set<Talent> currentTier = hero.talents.get(tierIndex).keySet();
		HashMap<Talent, HeroClass> restricted = new HashMap<>();
		restricted.put(Talent.RUNIC_TRANSFERENCE, HeroClasses.WARRIOR);
		restricted.put(Talent.WAND_PRESERVATION, HeroClasses.MAGE);

		for (HeroClass cls : HeroClasses.ALL) {
			Talent[] tierTalents = classTalentsAtTier(cls, tierIndex);
			if (tierTalents == null) continue;
			for (Talent talent : tierTalents) {
				if (talent != oldTalent
						&& !currentTier.contains(talent)
						&& (!restricted.containsKey(talent) || restricted.get(talent) == hero.heroClass)) {
					pool.add(talent);
				}
			}
		}

		//去除已作为替换目标的天赋，避免重复
		pool.removeIf(alreadyUsed::contains);

		if (pool.isEmpty()) return oldTalent;

		return Random.element(pool);
	}

	/** 使用职业基础定义，避免其他职业当前选择的皮肤改变候选池层级。 */
	private Talent[] classTalentsAtTier(HeroClass heroClass, int tierIndex) {
		TalentSet talents = heroClass.talentSet();
		if (talents == null) return null;
		switch (tierIndex) {
			case 0: return talents.getTier1();
			case 1: return talents.getTier2();
			case 2: return talents.getTier3();
			default: return null;
		}
	}

	private void recordMetamorph(Hero hero, Talent oldTalent, Talent newTalent) {
		//复用蜕变密卷的数据结构简化逻辑（与 TalentButton 中 METAMORPH_REPLACE 一致）
		if (!hero.metamorphedTalents.containsValue(oldTalent)) {
			hero.metamorphedTalents.put(oldTalent, newTalent);
		} else {
			//oldTalent 已被作为目标，需简化 a->b->c 链
			for (Talent t2 : hero.metamorphedTalents.keySet()) {
				if (hero.metamorphedTalents.get(t2) == oldTalent) {
					hero.metamorphedTalents.put(t2, newTalent);
				}
			}
		}
	}

	// ---- 装备变换：神器/戒指/武器/护甲 → 随机同类 ----

	private void doEquipmentTransmute(Hero hero) {
		//神器：允许重复（直接从未使用过的全神器池随机，忽略唯一性）
		try {
			transmuteArtifact(hero);
		} catch (Throwable t) {
			logMetamorphError(t);
		}

		Ring ring = hero.belongings.ring();
		if (ring != null) {
			try {
				boolean ringTypeKnown = ring.isKnown();
				Item result = ScrollOfTransmutation.changeItem(ring);
				if (result instanceof Ring) {
					if (ringTypeKnown) {
						// Ring type knowledge is stored separately from level/curse knowledge.
						((Ring) result).setKnown();
					}
					result.cursed = ring.cursed;
					result.cursedKnown = ring.cursedKnown;
					result.levelKnown = ring.levelKnown;
				}
				replaceEquipped(hero, ring, result);
			} catch (Throwable t) {
				logMetamorphError(t);
			}
		}

		KindOfWeapon weapon = hero.belongings.weapon();
		if (weapon != null) {
			try {
				Item result = ScrollOfTransmutation.changeItem(weapon);
				replaceEquipped(hero, weapon, result);
			} catch (Throwable t) {
				logMetamorphError(t);
			}
		}

		Armor armor = hero.belongings.armor();
		if (armor != null) {
			try {
				transmuteArmor(hero, armor);
			} catch (Throwable t) {
				logMetamorphError(t);
			}
		}
	}

	/** 护甲变身为同 tier 的随机护甲（原版嬗变卷轴不处理护甲，故单独实现）。 */
	private void transmuteArmor(Hero hero, Armor old) {
		int tier = old.tier;
		if (tier < 1 || tier > 5) return;

		BrokenSeal seal = old.checkSeal();
		if (seal != null) {
			old.detachSeal(hero);
		}

		Generator.Category cat = Generator.armTiers[tier - 1];
		Armor replacement;
		do {
			replacement = (Armor) Generator.randomUsingDefaults(cat);
		} while (replacement.getClass() == old.getClass() && Generator.armTiers[tier - 1].classes.length > 1);

		//保留强化等级、诅咒与辨识状态
		replacement.level(0);
		int level = old.trueLevel();
		if (level > 0) replacement.upgrade(level);
		else if (level < 0) replacement.degrade(-level);

		replacement.levelKnown = old.levelKnown;
		replacement.cursedKnown = old.cursedKnown;
		replacement.cursed = old.cursed;
		replacement.inscribe(old.glyph);
		replacement.curseInfusionBonus = old.curseInfusionBonus;
		replacement.glyphHardened = old.glyphHardened;
		replacement.masteryPotionBonus = old.masteryPotionBonus;
		replacement.augment = old.augment;
		if (seal != null) {
			replacement.affixSeal(seal);
		}

		if (!replaceEquipped(hero, old, replacement) && seal != null) {
			// 替换失败时，撤销前面的纹章转移并恢复到已经重新装备的旧护甲。
			replacement.detachSeal(hero);
			old.affixSeal(seal);
		}
	}

	private void transmuteArtifact(Hero hero) {
		Artifact old = hero.belongings.artifact();
		if (old == null) return;

		if (hero.heroClass == HeroClasses.MOONLIGHT && old instanceof Wheelchair) {
			return;
		}

		if (hero.heroClass == HeroClasses.ROGUE && old instanceof CloakOfConcealment) {
			return;
		}

		//所有可生成的神器类（忽略唯一性，允许重复）
		Class<?>[] classes = Generator.Category.ARTIFACT.classes;
		List<Class<?>> candidates = new ArrayList<>();
		for (Class<?> c : classes) {
			if (Artifact.class.isAssignableFrom(c)) candidates.add(c);
		}
		if (candidates.isEmpty()) return;

		Artifact replacement;
		do {
			replacement = (Artifact) Reflection.newInstance(Random.element(candidates));
		} while (replacement.getClass() == old.getClass() && candidates.size() > 1);

		replacement.levelKnown = old.levelKnown;
		replacement.transferUpgrade(old.visiblyUpgraded());
		// 生成器可能默认生成诅咒神器；蜕变后必须严格继承原神器状态。
		replacement.cursed = old.cursed;
		replacement.cursedKnown = old.cursedKnown;

		replaceEquipped(hero, old, replacement);
	}

	private boolean replaceEquipped(Hero hero, Item oldItem, Item result) {
		if (result == null || result == oldItem) return false;

		int slot = Dungeon.quickslot.getSlot(oldItem);
		boolean oldCursed = oldItem.cursed;

		// 嬗变必须先摘下旧装备；临时清除诅咒以允许摘下，结果仍继承原诅咒状态。
		oldItem.cursed = false;
		if (oldItem.isEquipped(hero) && oldItem instanceof EquipableItem) {
			if (!((EquipableItem) oldItem).doUnequip(hero, false)) {
				oldItem.cursed = oldCursed;
				return false;
			}
		} else {
			if (oldItem.detach(hero.belongings.backpack) == null) {
				oldItem.cursed = oldCursed;
				return false;
			}
		}

		boolean replaced = false;
		try {
			if (result instanceof EquipableItem) {
				replaced = ((EquipableItem) result).doEquip(hero);
			} else {
				replaced = result.collect();
				if (!replaced) {
					Dungeon.level.drop(result, hero.pos).sprite.drop();
					replaced = true;
				}
			}
		} catch (Throwable t) {
			logMetamorphError(t);
		}

		if (!replaced) {
			rollbackEquipmentReplacement(hero, oldItem, oldCursed, result);
			return false;
		}
		//装备 API 会正常扣除装备/卸下时间；蜕变是即时效果，抵消本次冷却。
		hero.spend(-hero.cooldown());

		//恢复快捷栏
		if (slot != -1
				&& result.defaultAction() != null
				&& !Dungeon.quickslot.isNonePlaceholder(slot)
				&& hero.belongings.contains(result)) {
			Dungeon.quickslot.setSlot(slot, result);
		}
		return true;
	}

	private void rollbackEquipmentReplacement(Hero hero, Item oldItem, boolean oldCursed, Item result) {
		try {
			if (result instanceof EquipableItem && result.isEquipped(hero)) {
				result.cursed = false;
				((EquipableItem) result).doUnequip(hero, false);
			} else {
				result.detach(hero.belongings.backpack);
			}
		} catch (Throwable t) {
			logMetamorphError(t);
		}

		oldItem.cursed = oldCursed;
		try {
			if (oldItem instanceof EquipableItem && ((EquipableItem) oldItem).doEquip(hero)) return;
		} catch (Throwable t) {
			logMetamorphError(t);
		}

		// 即使重新装备也异常，至少保住原物品，不能让它从存档中消失。
		if (!oldItem.isEquipped(hero)
				&& !hero.belongings.contains(oldItem)
				&& !oldItem.collect(hero.belongings.backpack)) {
			Dungeon.level.drop(oldItem, hero.pos).sprite.drop();
		}
	}
}
