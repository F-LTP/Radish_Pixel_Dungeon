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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;


public class JutteChampionWeapon extends MeleeWeapon {

    public int tier = 1;

    private static final int BASE_STR = 10;
    private static final float ATTACK_DELAY = 0.75f;
    private static final float ACCURACY = 1.25f;
    private static final float BASE_MAX_DURABILITY = 50f;

    private float durability = BASE_MAX_DURABILITY;
    
    // Store talent bonus for durability (to fix "one jutte talent only adds empty durability")
    private float talentBonus = 0f;

    private static final int[][] TIER_DAMAGE = {
        {5, 20},   // tier 1
        {6, 24},   // tier 2
        {8, 32},   // tier 3
        {9, 36},   // tier 4
        {10, 40}   // tier 5
    };

    private static final int[][] TIER_BLOCK = {
        {2, 4},    // tier 1
        {2, 5},    // tier 2
        {3, 6},    // tier 3
        {3, 7},    // tier 4
        {4, 8}     // tier 5
    };

    {
        image = ItemSpriteSheet.JUTTE_CHAMPION_WEAPON;
        defaultAction = AC_THROW;
    }

    public JutteChampionWeapon() {
        this(1);
    }

    public JutteChampionWeapon(int tier) {
        this.tier = Math.max(1, Math.min(5, tier));
        updateDurabilityFromTalent();
    }

    @Override
    public int min(int lvl) {
        return TIER_DAMAGE[tier - 1][0] + lvl;
    }

    @Override
    public int max(int lvl) {
        int baseMax = TIER_DAMAGE[tier - 1][1];
        int levelBonus = lvl * (tier + 1);
        
        // Iron Quench talent: extra bonus based on upgrade level
        if (Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClasses.JUTTE_CHAMPION) {
            int points = Dungeon.hero.pointsInTalent(Talent.IRON_QUENCH);
            if (points > 0 && lvl > 0) {
                float extraBonus = lvl * 0.1f * points;
                levelBonus = Math.round(levelBonus * (1 + extraBonus));
            }
        }
        return baseMax + levelBonus;
    }

    @Override
    public int STRReq() {
        return BASE_STR;
    }
    
    @Override
    public int STRReq(int lvl) {
        // Fixed strength requirement of 10
        return BASE_STR;
    }

    @Override
    public float delayFactor(Char owner) {
        float delay = ATTACK_DELAY;
        // One Jutte talent: throw speed bonus
        if (owner instanceof Hero) {
            Hero hero = (Hero) owner;
            if (hero.subClass == HeroSubClasses.JUTTE_CHAMPION && hero.hasTalent(Talent.ONE_JUTTE)) {
                int points = hero.pointsInTalent(Talent.ONE_JUTTE);
                // Throw speed bonus: +1: 20%, +2: 35%, +3: 50%
                // Note: this affects overall attack speed, not just throw
            }
        }
        return delay;
    }

    @Override
    public float accuracyFactor(Char owner, Char target) {
        return ACCURACY;
    }

    @Override
    public int defenseFactor(Char owner) {
        int[] block = TIER_BLOCK[tier - 1];
        return Random.IntRange(block[0], block[1]);
    }

    /**
     * Get talent bonus for durability from One Jutte talent
     */
    public float getTalentBonus() {
        if (Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClasses.JUTTE_CHAMPION) {
            int points = Dungeon.hero.pointsInTalent(Talent.ONE_JUTTE);
            switch (points) {
                case 1: return 10f;
                case 2: return 17f;
                case 3: return 25f;
            }
        }
        return 0f;
    }

    /**
     * Get max durability (base + talent bonus)
     */
    public float getMaxDurability() {
        return BASE_MAX_DURABILITY + getTalentBonus();
    }

    /**
     * Update durability when talent upgrades - increases current durability to match new max
     * This fixes the bug where "one jutte talent only adds empty durability"
     */
    public void updateDurabilityFromTalent() {
        float newBonus = getTalentBonus();
        if (newBonus > talentBonus) {
            // Talent upgraded, add the difference to current durability
            float diff = newBonus - talentBonus;
            durability += diff;
            talentBonus = newBonus;
        }
        // Ensure durability doesn't exceed max
        durability = Math.min(durability, getMaxDurability());
    }

    public float getDurability() {
        return durability;
    }

    public boolean isBroken() {
        return durability <= 0;
    }

    public void consumeDurability(float amount) {
        if (isBroken()) return;

        durability -= amount;
        if (durability < 0) durability = 0;
        updateQuickslot();

        if (durability <= 0 && Dungeon.hero != null) {
            Hero hero = Dungeon.hero;
            Sample.INSTANCE.play(Assets.Sounds.JUTTE_BREAK);
            if (isEquipped(hero)) {
                doUnequip(hero, true, true);
            }
            detach(hero.belongings.backpack);

            if (level() > 0 && hero.hasTalent(Talent.IRON_QUENCH)) {
                ScrollOfUpgrade scroll = new ScrollOfUpgrade();
                if (!scroll.identify().collect()) {
                    Dungeon.level.drop(scroll, hero.pos).sprite.drop();
                }
                GLog.p(Messages.get(this, "return_scroll"));
            }
        }
    }

    @Override
    public int damageRoll(Char owner) {
        if (isBroken()) return 0;
        return super.damageRoll(owner);
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        return super.proc(attacker, defender, damage);
    }

    public void onSuccessfulHit(Hero hero, Char defender) {
        boolean surpriseAttack = defender instanceof Mob && ((Mob) defender).surprisedBy(hero);
        boolean freeSurpriseAttack = surpriseAttack
                && hero.subClass == HeroSubClasses.JUTTE_CHAMPION
                && hero.hasTalent(Talent.SURPRISE_JUTTE);

        if (!freeSurpriseAttack) {
            consumeDurability(1f);
        }
    }

    @Override
    public Item random() {
        return this;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public int value() {
        return 0;
    }

    @Override
    public String statsInfo() {
        String info = Messages.get(this, "stats",
                TIER_BLOCK[tier-1][0], TIER_BLOCK[tier-1][1],
                (int)durability, (int)getMaxDurability());
        if (isBroken()) {
            info += "\n" + Messages.get(this, "broken");
        }
        return info;
    }

    @Override
    protected void onThrow(int cell) {
        Char enemy = Actor.findChar(cell);
        if (enemy == null || enemy == curUser) {
            super.onThrow(cell);
            return;
        }

        if (isBroken()) {
            super.onThrow(cell);
            return;
        }

        Hero hero = (Hero) curUser;
        
        // Bug1 fix: Check ONE_JUTTE talent - without it, jutte should drop like normal item
        if (hero.subClass != HeroSubClasses.JUTTE_CHAMPION || !hero.hasTalent(Talent.ONE_JUTTE)) {
            super.onThrow(cell);
            return;
        }
        boolean wasEnemy = enemy.alignment == Char.Alignment.ENEMY
                || (enemy instanceof Mimic && enemy.alignment == Char.Alignment.NEUTRAL);

        hero.belongings.thrownWeapon = this;
        boolean hit = hero.attack(enemy);
        Invisibility.dispel();
        hero.belongings.thrownWeapon = null;

        if (hit && hero.subClass == HeroSubClasses.GLADIATOR && wasEnemy) {
            Buff.affect(hero, Combo.class).hit();
        }

        Dungeon.level.drop(this, cell).sprite.drop(cell);
    }
    
    // Serialization: store and restore durability
    private static final String DURABILITY = "durability";
    private static final String TIER_KEY = "tier";
    private static final String TALENT_BONUS_KEY = "talent_bonus";
    
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(DURABILITY, durability);
        bundle.put(TIER_KEY, tier);
        bundle.put(TALENT_BONUS_KEY, talentBonus);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        durability = bundle.getFloat(DURABILITY);
        tier = bundle.getInt(TIER_KEY);
        talentBonus = bundle.getFloat(TALENT_BONUS_KEY);
        
        // After restore, check if talent has been upgraded and update durability accordingly
        updateDurabilityFromTalent();
    }
}
