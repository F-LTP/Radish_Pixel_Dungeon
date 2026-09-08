package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
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
	private boolean changing = false;

	private static final float MIN_INTERVAL = 180;
	private static final float MAX_INTERVAL = 220;

	private static final String TURNS = "turns";

	@Override
	public boolean act() {
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
		if (Dungeon.hero == null) return;
		JumbleChangeBuff b = Buff.affect(Dungeon.hero, JumbleChangeBuff.class);
		b.turnsRemaining = Random.NormalIntRange((int) MIN_INTERVAL, (int) MAX_INTERVAL);
	}

	/** 若变身倒计时 buff 尚未存在，则创建并重置倒计时；已存在则不动。 */
	public static void resetCountdownIfMissing() {
		if (Dungeon.hero == null) return;
		JumbleChangeBuff b = Dungeon.hero.buff(JumbleChangeBuff.class);
		if (b == null) {
			resetCountdown();
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
		super.storeInBundle(bundle);
		bundle.put(TURNS, turnsRemaining);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turnsRemaining = bundle.getFloat(TURNS);
		// Animation callbacks cannot survive saving; retry an expired transformation after loading.
		changing = false;
	}

	// ---- 变身流程 ----

	private void startChange() {
		Hero hero = (Hero) target;
		// 变身提示
		GLog.w(Messages.get(JumbleChangeBuff.class, "transform"));
		if (hero.sprite instanceof JumbleSprite) {
			JumbleSprite sprite = (JumbleSprite) hero.sprite;

			// 一口气连续播放：消失 → (逻辑变换) → 出现 → 结束。全程阻塞，结束时才恢复。
			// 用通用的"不消耗时间阻塞动画"机制：Hero.act() 期间被拦截，不移动/不攻击/不推进回合。
			// 逻辑变换的异常必须被捕获，否则动画回调链中断，finishChange 永不执行（倒计时卡 0、英雄永久阻塞）。
			hero.playAnimationNoTime(new Callback() {
				@Override
				public void call() {
					sprite.playChange(new Callback() {
						@Override
						public void call() {
							try {
								doTalentMetamorph(hero);
								doEquipmentTransmute(hero);
							} catch (Throwable t) {
								com.watabou.utils.DeviceCompat.log("JumbleChangeBuff", "startChange metamorph error: " + t);
								t.printStackTrace();
							}

							int newGroup = Random.Int(6);
							sprite.playAppear(newGroup, new Callback() {
								@Override
								public void call() {
									finishChange();
								}
							});
						}
					});
				}
			});
		} else {
			//没有杂散精灵时直接完成逻辑，不阻塞
			try {
				doTalentMetamorph(hero);
				doEquipmentTransmute(hero);
			} catch (Throwable t) {
				com.watabou.utils.DeviceCompat.log("JumbleChangeBuff", "startChange metamorph error: " + t);
				t.printStackTrace();
			}
			finishChange();
		}
	}

	private void finishChange() {
		Hero hero = (Hero) target;
		changing = false;
		resetCountdown();
		if (hero != null && hero.isAlive()) {
			if (hero.sprite instanceof JumbleSprite) {
				hero.sprite.idle();
			}
			// 恢复英雄行动（通用"不消耗时间阻塞动画"的收尾）
			hero.finishAnimationNoTime();
		}
	}

	// ---- 天赋替换：逐天赋换成随机角色的随机天赋 ----

	private void doTalentMetamorph(Hero hero) {
		// Only regular talent tiers metamorph. Armor ability talents (tier 4) stay unchanged.
		for (int tier = 0; tier < Math.min(3, hero.talents.size()); tier++) {
			LinkedHashMap<Talent, Integer> oldTier = hero.talents.get(tier);
			if (oldTier == null || oldTier.isEmpty()) continue;

			LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();

			for (Talent oldTalent : oldTier.keySet()) {
				int points = oldTier.get(oldTalent);
				//所有普通层天赋都参与蜕变；点数仅随天赋槽位保留，不影响替换流程。
				Talent replacement = randomTalent(hero, oldTalent, tier);
				newTier.put(replacement, points);

				if (replacement != oldTalent) {
					recordMetamorph(hero, oldTalent, replacement);
					// 与原版蜕变一致：新天赋的被动/效果需在此触发才会生效
					Talent.onTalentUpgraded(hero, replacement);
				}
			}
			hero.talents.set(tier, newTier);
		}
	}

	/** 与蜕变秘卷一致，只从其他职业的同阶天赋中选择替代项。 */
	private Talent randomTalent(Hero hero, Talent oldTalent, int tierIndex) {
		List<Talent> pool = new ArrayList<>();
		Set<Talent> alreadyUsed = new LinkedHashSet<>(hero.metamorphedTalents.values());
		Set<Talent> currentTier = hero.talents.get(tierIndex).keySet();
		HashMap<Talent, com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass> restricted = new HashMap<>();
		restricted.put(Talent.RUNIC_TRANSFERENCE, HeroClasses.WARRIOR);
		restricted.put(Talent.WAND_PRESERVATION, HeroClasses.MAGE);

		for (com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass cls : HeroClasses.ALL) {
			ArrayList<LinkedHashMap<Talent, Integer>> clsTalents = new ArrayList<>();
			Talent.initClassTalents(cls, clsTalents);
			if (tierIndex >= clsTalents.size()) continue;
			for (Talent talent : clsTalents.get(tierIndex).keySet()) {
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
		transmuteArtifact(hero);

		Ring ring = hero.belongings.ring();
		if (ring != null) {
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
		}

		KindOfWeapon weapon = hero.belongings.weapon();
		if (weapon != null) {
			Item result = ScrollOfTransmutation.changeItem(weapon);
			replaceEquipped(hero, weapon, result);
		}

		Armor armor = hero.belongings.armor();
		if (armor != null) {
			transmuteArmor(hero, armor);
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

		replaceEquipped(hero, old, replacement);
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
		//生成器可能默认生成诅咒神器；蜕变后必须严格继承原神器状态。
		replacement.cursed = old.cursed;
		replacement.cursedKnown = old.cursedKnown;

		replaceEquipped(hero, old, replacement);
	}

	private void replaceEquipped(Hero hero, Item oldItem, Item result) {
		if (result == null || result == oldItem) return;

		int slot = Dungeon.quickslot.getSlot(oldItem);

		//卸下旧物品（清除诅咒以允许卸下）
		oldItem.cursed = false;
		if (oldItem.isEquipped(hero) && oldItem instanceof EquipableItem) {
			((EquipableItem) oldItem).doUnequip(hero, false);
		} else {
			oldItem.detach(hero.belongings.backpack);
		}

		//收集新物品并装备
		if (result instanceof EquipableItem) {
			((EquipableItem) result).doEquip(hero);
		} else {
			if (!result.collect()) {
				Dungeon.level.drop(result, hero.pos).sprite.drop();
			}
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
	}
}
