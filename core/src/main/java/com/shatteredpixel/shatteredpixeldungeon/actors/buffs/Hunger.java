/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.SaltCube;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Sprouted_Potato;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class Hunger extends Buff implements Hero.Doom {

        public static final float HUNGRY        = 300f;
        public static final float STARVING      = 450f;

        // 战士4-2 高端饮食：饱食度上限加成
        public static final int[] HIGH_DIET_MAX = {550, 600, 650, 700};
        public static final float HIGH_DIET_THRESHOLD = 450f;
        public static final float[] HIGH_DIET_REGEN_BOOST = {0.15f, 0.24f, 0.33f, 0.45f};

        private float level;
        private float partialDamage;

        private static final String LEVEL                       = "level";
        private static final String PARTIALDAMAGE       = "partialDamage";

        @Override
        public void storeInBundle( Bundle bundle ) {
                super.storeInBundle(bundle);
                bundle.put( LEVEL, level );
                bundle.put( PARTIALDAMAGE, partialDamage );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
                super.restoreFromBundle( bundle );
                level = bundle.getFloat( LEVEL );
                partialDamage = bundle.getFloat(PARTIALDAMAGE);
        }

        @Override
        public boolean act() {

                if (Dungeon.level.locked
                                || target.buff(WellFed.class) != null
                                || SPDSettings.intro()
                                || target.buff(ScrollOfChallenge.ChallengeArena.class) != null || Dungeon.depth == 0){
                        spend(TICK);
                        return true;
                }

                if (target.isAlive() && target instanceof Hero) {

                        Hero hero = (Hero)target;

                        if (isStarving()) {

                                partialDamage += target.HT/1000f;

                                if (partialDamage > 1){
                                        if (((Hero) target).belongings!=null && ((Hero) target).belongings.getItem(Sprouted_Potato.class)!=null){
                                                Buff.affect(target, Sprouted_Potato.Potato_Poison.class).harden((int)partialDamage*Sprouted_Potato.hungerMultiplier());
                                        } else {
                                                target.damage(new DamageInfo((int)partialDamage, DamageType.HUNGER, null, null, this));
                                        }
                                        partialDamage -= (int)partialDamage;
                                }

                        } else {

                                float hungerDelay = 1f;
                                if (target.buff(Shadows.class) != null){
                                        hungerDelay *= 1.5f;
                                }

                                hungerDelay /= SaltCube.hungerGainMultiplier();

                                float newLevel = level + (1f/hungerDelay);
                                float maxHunger = getMaxHunger();
                                if (newLevel >= maxHunger) {

                                        GLog.n( Messages.get(this, "onstarving") );

                                        if (((Hero) target).belongings!=null && ((Hero) target).belongings.getItem(Sprouted_Potato.class)!=null){
                                                Buff.affect(target, Sprouted_Potato.Potato_Poison.class).harden(1*Sprouted_Potato.hungerMultiplier());
                                        } else {
                                                hero.damage(new DamageInfo(1, DamageType.HUNGER, null, null, this));
                                        }

                                        hero.interrupt();
                                        newLevel = maxHunger;

                                } else if (newLevel >= HUNGRY && level < HUNGRY) {

                                        GLog.w( Messages.get(this, "onhungry") );

                                        if (!Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_FOOD)){
                                                GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_FOOD);
                                        }

                                }
                                level = newLevel;

                        }

                        spend( TICK );

                } else {

                        diactivate();

                }

                return true;
        }

        public void satisfy( float energy ) {
                affectHunger( energy, false );
        }

        public void affectHunger(float energy ){
                affectHunger( energy, false );
        }

        public void affectHunger(float energy, boolean overrideLimits ) {

                if (energy < 0 && target.buff(WellFed.class) != null){
                        target.buff(WellFed.class).left += energy;
                        BuffIndicator.refreshHero();
                        return;
                }

                float oldLevel = level;

                level -= energy;
                float maxHunger = getMaxHunger();
                if (level < 0 && !overrideLimits) {
                        level = 0;
                } else if (level > maxHunger) {
                        float excess = level - maxHunger;
                        level = maxHunger;
                        partialDamage += excess * (target.HT/1000f);
                        if (partialDamage > 1f){
                                if (((Hero) target).belongings!=null && ((Hero) target).belongings.getItem(Sprouted_Potato.class)!=null){
                                        Buff.affect(target, Sprouted_Potato.Potato_Poison.class).harden((int)partialDamage*Sprouted_Potato.hungerMultiplier());
                                } else {
                                        target.damage(new DamageInfo((int)partialDamage, DamageType.HUNGER, null, null, this));
                                }
                                partialDamage -= (int)partialDamage;
                        }
                }

                if (oldLevel < HUNGRY && level >= HUNGRY){
                        GLog.w( Messages.get(this, "onhungry") );
                } else if (oldLevel < maxHunger && level >= maxHunger){
                        GLog.n( Messages.get(this, "onstarving") );
                        if (((Hero) target).belongings!=null && ((Hero) target).belongings.getItem(Sprouted_Potato.class)!=null){
                                Buff.affect(target, Sprouted_Potato.Potato_Poison.class).harden((int)1*Sprouted_Potato.hungerMultiplier());
                        } else  {
                                target.damage(new DamageInfo(1, DamageType.HUNGER, null, null, this));
                        }
                }

                BuffIndicator.refreshHero();
        }

        public boolean isStarving() {
                // 战士4-2 高端饮食：使用动态饱食度上限
                return level >= getMaxHunger();
        }

        /**
         * 获取动态饱食度上限（战士4-2 高端饮食）
         */
        public float getMaxHunger() {
                if (target instanceof Hero) {
                        Hero hero = (Hero) target;
                        if (hero.hasTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.HIGH_DIET)) {
                                int points = hero.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.HIGH_DIET);
                                if (points > 0 && points <= 4) {
                                        return HIGH_DIET_MAX[points - 1];
                                }
                        }
                }
                return STARVING;
        }

        /**
         * 检查是否处于高饱食度状态（战士4-2 高端饮食）
         */
        public boolean isHighSatiety() {
                return level < HIGH_DIET_THRESHOLD;
        }

        /**
         * 获取回血加成倍率（战士4-2 高端饮食）
         */
        public static float getHighDietRegenMultiplier(Hero hero) {
                if (hero.hasTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.HIGH_DIET)) {
                        Hunger hunger = hero.buff(Hunger.class);
                        if (hunger != null && hunger.level < HIGH_DIET_THRESHOLD) {
                                int points = hero.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.HIGH_DIET);
                                if (points > 0 && points <= 4) {
                                        return 1f + HIGH_DIET_REGEN_BOOST[points - 1];
                                }
                        }
                }
                return 1f;
        }

        public int hunger() {
                return (int)Math.ceil(level);
        }

        @Override
        public String icon() {
                if (level < HUNGRY) {
                        return BuffIndicator.NONE;
                } else if (level < STARVING) {
                        return BuffIndicator.HUNGER;
                } else {
                        return BuffIndicator.STARVATION;
                }
        }

        @Override
        public String name() {
                if (level < STARVING) {
                        return Messages.get(this, "hungry");
                } else {
                        return Messages.get(this, "starving");
                }
        }

        @Override
        public String desc() {
                String result;
                if (level < STARVING) {
                        result = Messages.get(this, "desc_intro_hungry");
                } else {
                        result = Messages.get(this, "desc_intro_starving");
                }

                result += Messages.get(this, "desc");

                return result;
        }

        @Override
        public void onDeath() {

                Badges.validateDeathFromHunger();

                Dungeon.fail( this );
                GLog.n( Messages.get(this, "ondeath") );
        }
}
