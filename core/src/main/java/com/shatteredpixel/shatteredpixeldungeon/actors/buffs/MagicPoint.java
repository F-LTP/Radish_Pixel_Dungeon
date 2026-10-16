/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDiceMageSpells;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 骰子法师魔力点追踪Buff
 * 存储当前魔力点数，以及骰子法师法术的持久状态。
 */
public class MagicPoint extends Buff implements ActionIndicator.Action {

    {
        type = buffType.POSITIVE;
        revivePersists = true;
    }

    private float currentPoints = 0f;
    private boolean infiniteMana = false;
    private final HashSet<Class<? extends DiceMageSpell>> learnedSpells = new HashSet<>();
    private int healValue = 50;
    private int refreshValue = 40;
    private int surgeryUses = 0;
    private int activeSurgerySummon = -1;
    private Class<? extends Mob> lastKilledMob;
    // 无可用3阶天赋点时免费升学派的总次数（用于 talentPointsAvailable(3) 记账补偿）
    private int freeSchoolUpgrades = 0;

    // 每 REGEN_TURNS 回合自动获得 1 魔力点
    public static final int REGEN_TURNS = 20;
    private float turnsToRegen = 1f;
    // 每 CLEAR_TURNS 回合，魔力点高于 CLEAR_THRESHOLD 的部分被清空
    public static final int CLEAR_TURNS = 25;
    public static final int CLEAR_THRESHOLD = 3;
    private float turnsToClear = 1f;

    // 本局魔力药水炼金的"正确答案"（各元素一个）；null 表示尚未随机
    private Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion> correctPotion = null;
    private Class<? extends com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed> correctSeed = null;
    private Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll> correctScroll = null;

    // 法术冷却：Class -> 剩余回合数
    private final HashMap<Class<? extends DiceMageSpell>, Float> cooldowns = new HashMap<>();
    // 特殊学派本局的三个法术（按等级 1/2/3 存储），null 表示未生成
    private Class<? extends DiceMageSpell>[] specialSpells = null;

    private static final String POINTS = "points";
    private static final String HEAL_VALUE = "heal_value";    private static final String REFRESH_VALUE = "refresh_value";
    private static final String SURGERY_USES = "surgery_uses";
    private static final String LAST_KILLED_MOB = "last_killed_mob";
    private static final String COOLDOWNS = "spell_cooldowns";
    private static final String SPECIAL_SPELLS = "special_spells";
    private static final String ACTIVE_SURGERY_SUMMON = "active_surgery_summon";
    private static final String TURNS_TO_REGEN = "turns_to_regen";
    private static final String TURNS_TO_CLEAR = "turns_to_clear";
    private static final String CORRECT_POTION = "correct_potion";
    private static final String CORRECT_SEED = "correct_seed";
    private static final String CORRECT_SCROLL = "correct_scroll";
    private static final String INFINITE_MANA = "infinite_mana";
    private static final String LEARNED_SPELLS = "learned_spells";
    private static final String FREE_SCHOOL_UPGRADES = "free_school_upgrades";

    public static MagicPoint inst() {
        return Dungeon.hero != null ? Dungeon.hero.buff(MagicPoint.class) : null;
    }

    public float getPoints() {
        return currentPoints;
    }

    public int getIntPoints() {
        return (int) currentPoints;
    }

    /** 距离下一次清空（魔力点超上限部分被清除）还有几回合。 */
    public int turnsUntilClear() {
        return Math.max(0, (int) Math.ceil(turnsToClear));
    }

    /** 视野内怪物死亡时调用：获得 1 魔力点。 */
    public void gainKillPoint() {
        addPoints(1f);
    }

    /** 每点法杖充能可转化的魔力点数：1 + 0.33 * 法杖等级。 */
    public static float wandMpPerCharge(Wand w) {
        return 1f + 0.33f * w.buffedLvl();
    }

    /** 背包中所有法杖当前充能可转化的最大魔力点数。 */
    public float wandMpTotal() {
        if (Dungeon.hero == null) return 0f;
        float total = 0f;
        for (Item item : Dungeon.hero.belongings) {
            if (item instanceof Wand) {
                Wand w = (Wand) item;
                total += w.curCharges * wandMpPerCharge(w);
            }
        }
        return total;
    }

    /** 当前魔力点 + 法杖充能可转化魔力是否足够支付 cost。 */
    public boolean canAfford(int cost) {
        return infiniteMana || currentPoints + wandMpTotal() >= cost;
    }

    /** 背包中所有法杖（每把按顺序），用于弹窗列出将被消耗充能的法杖。 */
    public ArrayList<Wand> allWands() {
        ArrayList<Wand> result = new ArrayList<>();
        if (Dungeon.hero == null) return result;
        for (Item item : Dungeon.hero.belongings) {
            if (item instanceof Wand && ((Wand) item).curCharges > 0) {
                result.add((Wand) item);
            }
        }
        return result;
    }

    /**
     * 将法杖充能转化为魔力点，直到魔力点达到 target 或法杖耗尽。
     * 消耗时按背包顺序逐点扣减充能。
     */
    public void drainWandsFor(int target) {
        if (Dungeon.hero == null) return;
        for (Item item : Dungeon.hero.belongings) {
            if (currentPoints >= target) break;
            if (item instanceof Wand) {
                Wand w = (Wand) item;
                float perCharge = wandMpPerCharge(w);
                while (w.curCharges > 0 && currentPoints < target) {
                    w.curCharges--;
                    currentPoints += perCharge;
                }
                w.updateQuickslot();
            }
        }
        updateAction();
        BuffIndicator.refreshHero();
    }

    public void addPoints(float amount) {
        currentPoints += amount;
        updateAction();
        BuffIndicator.refreshHero();
    }

    /** 是否无限魔力点（测试工具用）。 */
    public boolean infiniteMana() {
        return infiniteMana;
    }

    public void setInfiniteMana(boolean value) {
        infiniteMana = value;
        updateAction();
        BuffIndicator.refreshHero();
    }

    /** 是否已学会某法术（无视天赋限制）。 */
    public boolean isLearned(Class<? extends DiceMageSpell> spellClass) {
        return learnedSpells.contains(spellClass);
    }

    public void learnSpell(Class<? extends DiceMageSpell> spellClass) {
        if (spellClass != null) {
            learnedSpells.add(spellClass);
            updateAction();
            BuffIndicator.refreshHero();
        }
    }

    public HashSet<Class<? extends DiceMageSpell>> learnedSpells() {
        return learnedSpells;
    }

    /** 本局魔力药水配方的三个正确答案。 */
    public Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion> correctPotion() {
        ensureMagicPotionRecipe();
        return correctPotion;
    }

    public Class<? extends com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed> correctSeed() {
        ensureMagicPotionRecipe();
        return correctSeed;
    }

    public Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll> correctScroll() {
        ensureMagicPotionRecipe();
        return correctScroll;
    }

    public boolean spendPoints(int amount) {
        if (infiniteMana) return true;
        if (currentPoints >= amount) {
            currentPoints -= amount;
            updateAction();
            BuffIndicator.refreshHero();
            return true;
        }
        return false;
    }

    public int healValue() {
        return healValue;
    }

    public int refreshValue() {
        return refreshValue;
    }

    public void decayHealValue() {
        healValue = Math.max(0, healValue - 3);
    }

    public void decayRefreshValue() {
        refreshValue = Math.max(0, refreshValue - 2);
    }

    public int surgeryCostIncrease(int talentPoints) {
        return talentPoints == 1 ? 3 : talentPoints == 2 ? 2 : 1;
    }

    public int surgeryCost(int talentPoints) {
        return 3 + surgeryUses * surgeryCostIncrease(talentPoints);
    }

    public void surgeryUsed() {
        surgeryUses++;
    }

    public void recordKill(Class<? extends Mob> mobClass) {
        lastKilledMob = mobClass;
    }

    public Class<? extends Mob> lastKilledMob() {
        return lastKilledMob;
    }

    public void clearLastKilledMob() {
        lastKilledMob = null;
    }

    public boolean offCooldown(Class<? extends DiceMageSpell> spellClass) {
        Float left = cooldowns.get(spellClass);
        return left == null || left <= 0f;
    }

    public void setCooldown(Class<? extends DiceMageSpell> spellClass, float turns) {
        if (turns <= 0) cooldowns.remove(spellClass);
        else cooldowns.put(spellClass, turns);
    }

    public float cooldownLeft(Class<? extends DiceMageSpell> spellClass) {
        Float left = cooldowns.get(spellClass);
        return left == null ? 0f : left;
    }

    public Class<? extends DiceMageSpell>[] specialSpells() {
        return specialSpells;
    }

    public void setSpecialSpells(Class<? extends DiceMageSpell>[] spells) {
        specialSpells = spells;
    }

    public int activeSurgerySummon() {
        return activeSurgerySummon;
    }

    public int freeSchoolUpgrades() {
        return freeSchoolUpgrades;
    }

    public void addFreeSchoolUpgrade() {
        freeSchoolUpgrades++;
    }

    public void setActiveSurgerySummon(int id) {
        activeSurgerySummon = id;
    }

    @Override
    public boolean act() {
        if (!cooldowns.isEmpty()) {
            cooldowns.entrySet().removeIf(e -> {
                float v = e.getValue() - 1f;
                if (v <= 0f) return true;
                e.setValue(v);
                return false;
            });
        }

        // 每 REGEN_TURNS 回合获得 1 魔力点
        turnsToRegen -= 1f;
        if (turnsToRegen <= 0f) {
            turnsToRegen += REGEN_TURNS;
            addPoints(1f);
        }

        // 每 CLEAR_TURNS 回合，魔力点高于 CLEAR_THRESHOLD 的部分被清空
        turnsToClear -= 1f;
        if (turnsToClear <= 0f) {
            turnsToClear += CLEAR_TURNS;
            if (currentPoints > CLEAR_THRESHOLD) {
                currentPoints = CLEAR_THRESHOLD;
                updateAction();
                BuffIndicator.refreshHero();
            }
        }

        updateAction();
        spend(TICK);
        return true;
    }

    /** 保证本局魔力药水的三个"正确答案"已随机生成。排除升级卷轴与力量药水。 */
    public void ensureMagicPotionRecipe() {
        if (correctPotion == null || correctSeed == null || correctScroll == null) {
            correctPotion = randomPotion();
            correctSeed = (Class<? extends com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed>)
                    com.watabou.utils.Random.element(com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.SEED.classes);
            correctScroll = randomScroll();
        }
    }

    /** 随机一个非力量药水的药剂作为答案。 */
    private Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion> randomPotion() {
        Class<?>[] pool = com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.POTION.classes;
        ArrayList<Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion>> filtered = new ArrayList<>();
        for (Class<?> c : pool) {
            if (c == com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength.class) continue;
            if (com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion.class.isAssignableFrom(c)) {
                filtered.add(c.asSubclass(com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion.class));
            }
        }
        return com.watabou.utils.Random.element(filtered);
    }

    /** 随机一个非升级卷轴的卷轴作为答案。 */
    private Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll> randomScroll() {
        Class<?>[] pool = com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.SCROLL.classes;
        ArrayList<Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll>> filtered = new ArrayList<>();
        for (Class<?> c : pool) {
            if (c == com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade.class) continue;
            if (com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll.class.isAssignableFrom(c)) {
                filtered.add(c.asSubclass(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll.class));
            }
        }
        return com.watabou.utils.Random.element(filtered);
    }

    /** 判断某个药剂是否为本局炼金配方正确药剂。 */
    public boolean isCorrectPotion(Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion> cls) {
        ensureMagicPotionRecipe();
        return cls != null && cls == correctPotion;
    }

    /** 判断某个种子是否为本局炼金配方正确种子。 */
    public boolean isCorrectSeed(Class<? extends com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed> cls) {
        ensureMagicPotionRecipe();
        return cls != null && cls == correctSeed;
    }

    /** 判断某个卷轴是否为本局炼金配方正确卷轴。 */
    public boolean isCorrectScroll(Class<? extends com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll> cls) {
        ensureMagicPotionRecipe();
        return cls != null && cls == correctScroll;
    }

    private void updateAction() {
        if (target instanceof Hero && ((Hero) target).subClass == HeroSubClasses.DICE_MAGE) {
            if (ActionIndicator.action == null || ActionIndicator.action == this) {
                ActionIndicator.setAction(this);
            }
        } else {
            ActionIndicator.clearAction(this);
        }
    }

    @Override
    public void detach() {
        ActionIndicator.clearAction(this);
        super.detach();
    }

    @Override
    public String icon() {
        return BuffIndicator.MAGIC_POINT;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(0.5f, 0f, 1f); // 紫色调
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString(getIntPoints());
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", getIntPoints(), turnsUntilClear());
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(POINTS, currentPoints);
        bundle.put(INFINITE_MANA, infiniteMana);
        bundle.put(HEAL_VALUE, healValue);
        bundle.put(REFRESH_VALUE, refreshValue);
        bundle.put(SURGERY_USES, surgeryUses);
        bundle.put(ACTIVE_SURGERY_SUMMON, activeSurgerySummon);
        bundle.put(FREE_SCHOOL_UPGRADES, freeSchoolUpgrades);
        bundle.put(TURNS_TO_REGEN, turnsToRegen);
        bundle.put(TURNS_TO_CLEAR, turnsToClear);
        if (correctPotion != null) bundle.put(CORRECT_POTION, correctPotion.getName());
        if (correctSeed != null) bundle.put(CORRECT_SEED, correctSeed.getName());
        if (correctScroll != null) bundle.put(CORRECT_SCROLL, correctScroll.getName());
        if (lastKilledMob != null) {
            bundle.put(LAST_KILLED_MOB, lastKilledMob.getName());
        }
        if (!cooldowns.isEmpty()) {
            Bundle cd = new Bundle();
            for (Class<? extends DiceMageSpell> c : cooldowns.keySet()) {
                cd.put(c.getName(), cooldowns.get(c));
            }
            bundle.put(COOLDOWNS, cd);
        }
        if (specialSpells != null) {
            String[] names = new String[specialSpells.length];
            for (int i = 0; i < specialSpells.length; i++) {
                names[i] = specialSpells[i] != null ? specialSpells[i].getName() : null;
            }
            bundle.put(SPECIAL_SPELLS, names);
        }
        if (!learnedSpells.isEmpty()) {
            String[] names = new String[learnedSpells.size()];
            int i = 0;
            for (Class<? extends DiceMageSpell> c : learnedSpells) names[i++] = c.getName();
            bundle.put(LEARNED_SPELLS, names);
        }
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        currentPoints = bundle.getFloat(POINTS);
        infiniteMana = bundle.getBoolean(INFINITE_MANA);
        healValue = bundle.contains(HEAL_VALUE) ? bundle.getInt(HEAL_VALUE) : 50;
        refreshValue = bundle.contains(REFRESH_VALUE) ? bundle.getInt(REFRESH_VALUE) : 40;
        surgeryUses = bundle.getInt(SURGERY_USES);
        activeSurgerySummon = bundle.getInt(ACTIVE_SURGERY_SUMMON);
        freeSchoolUpgrades = bundle.contains(FREE_SCHOOL_UPGRADES) ? bundle.getInt(FREE_SCHOOL_UPGRADES) : 0;
        turnsToRegen = bundle.contains(TURNS_TO_REGEN) ? bundle.getFloat(TURNS_TO_REGEN) : 1f;
        turnsToClear = bundle.contains(TURNS_TO_CLEAR) ? bundle.getFloat(TURNS_TO_CLEAR) : 1f;
        if (bundle.contains(CORRECT_POTION)) {
            try {
                Class<?> cls = Class.forName(bundle.getString(CORRECT_POTION));
                if (com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion.class.isAssignableFrom(cls)) {
                    correctPotion = cls.asSubclass(com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion.class);
                }
            } catch (ClassNotFoundException ignored) {}
        }
        if (bundle.contains(CORRECT_SEED)) {
            try {
                Class<?> cls = Class.forName(bundle.getString(CORRECT_SEED));
                if (com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed.class.isAssignableFrom(cls)) {
                    correctSeed = cls.asSubclass(com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed.class);
                }
            } catch (ClassNotFoundException ignored) {}
        }
        if (bundle.contains(CORRECT_SCROLL)) {
            try {
                Class<?> cls = Class.forName(bundle.getString(CORRECT_SCROLL));
                if (com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll.class.isAssignableFrom(cls)) {
                    correctScroll = cls.asSubclass(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll.class);
                }
            } catch (ClassNotFoundException ignored) {}
        }
        if (bundle.contains(LAST_KILLED_MOB)) {
            try {
                Class<?> cls = Class.forName(bundle.getString(LAST_KILLED_MOB));
                if (Mob.class.isAssignableFrom(cls)) {
                    lastKilledMob = cls.asSubclass(Mob.class);
                }
            } catch (ClassNotFoundException ignored) {
                lastKilledMob = null;
            }
        }
        if (bundle.contains(COOLDOWNS)) {
            cooldowns.clear();
            Bundle cd = bundle.getBundle(COOLDOWNS);
            for (String key : cd.getKeys()) {
                try {
                    Class<?> cls = Class.forName(key);
                    if (DiceMageSpell.class.isAssignableFrom(cls)) {
                        cooldowns.put(cls.asSubclass(DiceMageSpell.class), cd.getFloat(key));
                    }
                } catch (ClassNotFoundException ignored) {
                    // ignore unknown spell classes from older saves
                }
            }
        }
        if (bundle.contains(SPECIAL_SPELLS)) {
            String[] names = bundle.getStringArray(SPECIAL_SPELLS);
            if (names != null) {
                @SuppressWarnings("unchecked")
                Class<? extends DiceMageSpell>[] arr = new Class[names.length];
                for (int i = 0; i < names.length; i++) {
                    if (names[i] == null) continue;
                    try {
                        Class<?> cls = Class.forName(names[i]);
                        if (DiceMageSpell.class.isAssignableFrom(cls)) {
                            arr[i] = cls.asSubclass(DiceMageSpell.class);
                        }
                    } catch (ClassNotFoundException ignored) {
                        // ignore
                    }
                }
                specialSpells = arr;
            }
        }
        if (bundle.contains(LEARNED_SPELLS)) {
            String[] names = bundle.getStringArray(LEARNED_SPELLS);
            if (names != null) {
                for (String n : names) {
                    if (n == null) continue;
                    try {
                        Class<?> cls = Class.forName(n);
                        if (DiceMageSpell.class.isAssignableFrom(cls)) {
                            learnedSpells.add(cls.asSubclass(DiceMageSpell.class));
                        }
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
        }
        updateAction();
    }

    @Override
    public String actionName() {
        return Messages.get(this, "action_name");
    }

    @Override
    public String actionIcon() {
        return HeroIcon.DICE_MAGE;
    }

    @Override
    public int indicatorColor() {
        return 0x8844FF;
    }

    @Override
    public void doAction() {
        if (Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClasses.DICE_MAGE) {
            GameScene.show(new WndDiceMageSpells());
        }
    }
}
