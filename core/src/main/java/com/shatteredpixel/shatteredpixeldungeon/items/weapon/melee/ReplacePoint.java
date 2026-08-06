package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class ReplacePoint extends MeleeWeapon {

    {
        tier = 4;
        image = ItemSpriteSheet.REPLACE_POINT;
        DLY = 0.4f;
    }

    @Override
    public int damageRoll(Char owner) {
        if (owner instanceof Hero) {
            Hero hero = (Hero)owner;
            int baseDamage = super.damageRoll(owner);
            int totalDamage = 0;
            PhysicalHitsTracker tracker = hero.buff(PhysicalHitsTracker.class);
            if(tracker != null){
                int physicalHits = tracker.getHits();
                int bonusDamage = physicalHits * (3 + Math.round(0.3f * level()));
                totalDamage = baseDamage + bonusDamage;
                tracker.updateNextDamage(baseDamage, physicalHits, bonusDamage, totalDamage);
            } else {
                PhysicalHitsTracker trackers = Buff.affect(hero, PhysicalHitsTracker.class,1f);
                int physicalHits = trackers.getHits();
                int bonusDamage = physicalHits * (3 + Math.round(0.3f * level()));
                totalDamage = baseDamage + bonusDamage;
                trackers.updateNextDamage(baseDamage, physicalHits, bonusDamage, totalDamage);
            }

            return totalDamage;
        }

        return super.damageRoll(owner);
    }

    public static class PhysicalHitsTracker extends FlavourBuff {

        {
            type = Buff.buffType.POSITIVE;
        }

        private int nextBaseDamage;
        private int nextPhysicalHits;
        private int nextBonusDamage;
        private int nextTotalDamage;

        public void updateNextDamage(int base, int hits, int bonus, int total) {
            nextBaseDamage = base;
            nextPhysicalHits = hits;
            nextBonusDamage = bonus;
            nextTotalDamage = total;
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc",
                    nextBaseDamage, nextPhysicalHits, nextBonusDamage, nextTotalDamage);
        }

        private int hits = 0;

        public int getHits() {
            return hits++;
        }

        public void reset() {
            hits = 0;
        }

        @Override
        public boolean act() {
            super.act();
            spend(TICK);
            return true;
        }

        @Override
        public String icon() {
            return BuffIndicator.CHALLENGE;
        }

        private static final String HITS             = "hits";
        private static final String NEXT_BASE_DAMAGE = "next_base_damage";
        private static final String NEXT_HITS        = "next_hits";
        private static final String NEXT_BONUS_DAMAGE = "next_bonus_damage";
        private static final String NEXT_TOTAL_DAMAGE = "next_total_damage";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(HITS, hits);
            bundle.put(NEXT_BASE_DAMAGE, nextBaseDamage);
            bundle.put(NEXT_HITS, nextPhysicalHits);
            bundle.put(NEXT_BONUS_DAMAGE, nextBonusDamage);
            bundle.put(NEXT_TOTAL_DAMAGE, nextTotalDamage);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            hits = bundle.getInt(HITS);
            nextBaseDamage = bundle.getInt(NEXT_BASE_DAMAGE);
            nextPhysicalHits = bundle.getInt(NEXT_HITS);
            nextBonusDamage = bundle.getInt(NEXT_BONUS_DAMAGE);
            nextTotalDamage = bundle.getInt(NEXT_TOTAL_DAMAGE);
        }
    }

    @Override
    public int min(int lvl) {
        return 2 + lvl;
    }

    @Override
    public int max(int lvl) {
        return 10 + lvl * 2;
    }
}
