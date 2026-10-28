package com.shatteredpixel.shatteredpixeldungeon.items.potions;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 魔力药水：饮用后恢复魔力点。
 * 下等(1)=6，中等(2)=12，高等(3)=24。只能由骰子法师通过炼金炼制。
 */
public class MagicPotion extends Item {

    public static final String AC_DRINK = "DRINK";

    private static final float TIME_TO_DRINK = 1f;

    private int tier = 1;

    {
        stackable = true;
        defaultAction = AC_DRINK;
        sndImageName = "mana-potion";
    }

    public MagicPotion() {
    }

    public MagicPotion(int tier) {
        this.tier = tier;
    }

    public int tier() {
        return tier;
    }

    public int restoreAmount() {
        switch (tier) {
            case 3: return 24;
            case 2: return 12;
            default: return 6;
        }
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_DRINK);
        return actions;
    }

    @Override
    public void execute(final Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_DRINK)) {
            drink(hero);
        }
    }

    private void drink(Hero hero) {
        detach(hero.belongings.backpack);
        hero.spend(TIME_TO_DRINK);
        hero.busy();

        if (hero.subClass != HeroSubClasses.DICE_MAGE) {
            GLog.w(Messages.get(this, "only_dicemage"));
        } else {
            MagicPoint mp = Dungeon.hero.buff(MagicPoint.class);
            if (mp != null) {
                mp.addPoints(restoreAmount());
            }
            GLog.p(Messages.get(this, "restored", restoreAmount()));
        }

        Sample.INSTANCE.play(Assets.Sounds.DRINK);
        hero.sprite.operate(hero.pos);
    }

    @Override
    public ItemSprite.Glowing glowing() {
        switch (tier) {
            // 劣质：低饱和度和明度（暗淡灰紫色）
            case 1: return new ItemSprite.Glowing(0x555566, 1f);
            // 中等：正常，不加光效
            case 2: return null;
            // 优质：附魔的彩色光环
            default: return new ItemSprite.Glowing(0x66CCFF, 0.5f);
        }
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public String name() {
        return Messages.get(this, "name_" + tier);
    }

    @Override
    public String info() {
        return Messages.get(this, "desc_" + tier, restoreAmount());
    }

    private static final String TIER = "tier";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(TIER, tier);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        tier = bundle.getInt(TIER);
    }

    /**
     * 魔力药水炼金配方：一个药剂 + 一个种子 + 一个卷轴。
     * 每局随机各选一个"正确"要素；投入的材料与正确要素匹配的数量决定成品等级：
     * 3 个全对 = 高等(30)、2 个 = 中等(15)、1 个 = 下等(6)、0 个 = 随机合剂或秘药。
     */
    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

        @Override
        public boolean testIngredients(ArrayList<Item> ingredients) {
            if (Dungeon.hero == null || Dungeon.hero.subClass != HeroSubClasses.DICE_MAGE) return false;
            if (ingredients == null || ingredients.size() != 3) return false;
            boolean hasPotion = false, hasSeed = false, hasScroll = false;
            for (Item i : ingredients) {
                if (i == null || !i.isIdentified()) return false;
                if (i instanceof Potion) hasPotion = true;
                else if (i instanceof com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed) hasSeed = true;
                else if (i instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll) hasScroll = true;
                else return false;
            }
            return hasPotion && hasSeed && hasScroll;
        }

        @Override
        public int cost(ArrayList<Item> ingredients) {
            return 8;
        }

        @Override
        public Item brew(ArrayList<Item> ingredients) {
            if (!testIngredients(ingredients)) return null;

            MagicPoint mp = Dungeon.hero.buff(MagicPoint.class);
            if (mp == null) return null;
            mp.ensureMagicPotionRecipe();
            int matches = 0;
            for (Item i : ingredients) {
                if (i instanceof Potion && mp.isCorrectPotion(((Potion) i).getClass())) matches++;
                else if (i instanceof com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed
                        && mp.isCorrectSeed(((com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed) i).getClass())) matches++;
                else if (i instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll
                        && mp.isCorrectScroll(((com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll) i).getClass())) matches++;
            }

            // 消耗材料
            for (Item i : ingredients) {
                i.quantity(i.quantity() - 1);
            }

            if (matches == 0) {
                return randomBrewOrElixir();
            }
            return new MagicPotion(matches);
        }

        @Override
        public Item sampleOutput(ArrayList<Item> ingredients) {
            if (Dungeon.hero == null || Dungeon.hero.subClass != HeroSubClasses.DICE_MAGE) return null;
            if (ingredients == null || ingredients.size() != 3) return null;
            MagicPoint mp = Dungeon.hero.buff(MagicPoint.class);
            if (mp == null) return null;
            mp.ensureMagicPotionRecipe();
            int matches = 0;
            for (Item i : ingredients) {
                if (i instanceof Potion && mp.isCorrectPotion(((Potion) i).getClass())) matches++;
                else if (i instanceof com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed
                        && mp.isCorrectSeed(((com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed) i).getClass())) matches++;
                else if (i instanceof com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll
                        && mp.isCorrectScroll(((com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll) i).getClass())) matches++;
            }
            if (matches == 0) return randomBrewOrElixir();
            return new MagicPotion(matches);
        }

        private static Item randomBrewOrElixir() {
            ArrayList<Class<? extends Potion>> pool = new ArrayList<>();
            // 合剂（brews）
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.AquaBrew.class);
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.BlizzardBrew.class);
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.CausticBrew.class);
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.InfernalBrew.class);
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.ShockingBrew.class);
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.UnstableBrew.class);
            // 秘药（elixirs），排除龙血秘药
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfAquaticRejuvenation.class);
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfArcaneArmor.class);
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall.class);
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfHoneyedHealing.class);
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfIcyTouch.class);
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight.class);
            pool.add(com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfToxicEssence.class);
            // 肌肉记忆合剂(PotionOfMastery)已被排除（它是异变药剂，不属于此池）

            Class<? extends Potion> cls = com.watabou.utils.Random.element(pool);
            return com.watabou.utils.Reflection.newInstance(cls);
        }
    }
}
