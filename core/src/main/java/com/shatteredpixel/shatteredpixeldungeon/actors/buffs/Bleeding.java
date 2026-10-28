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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Sacrificial;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class Bleeding extends Buff {

        {
                type = buffType.NEGATIVE;
                announced = true;
        }

        protected float level;

        //used in specific cases where the source of the bleed is important for death logic
        private Class source;

        // 流血伤害来源链：记录是谁/什么造成的流血（如「装甲石像的荆棘刻印」），随流血伤害传递
        private List<Object> causeChain = new ArrayList<>();

        public float level(){
                return level;
        }

        public static void finishAllBleedingDamage(Char target) {
                Bleeding bleeding = target.buff(Bleeding.class);
                if (bleeding == null) {
                        return;
                }

                float bleedLevel = bleeding.level();
                int totalBleedDmg = 0;

                while (bleedLevel > 0.5f) {
                        bleedLevel = Random.NormalFloat(bleedLevel / 2f, bleedLevel);
                        int bleedDmg = Math.round(bleedLevel);
                        totalBleedDmg += bleedDmg;
                }

                if (totalBleedDmg > 0) {
                        target.damage(new DamageInfo(totalBleedDmg, DamageType.BLEEDING, null, null, bleeding).setCauseChain(bleeding.getCauseChain()));
                }
                bleeding.detach();

        }

        private static final String LEVEL       = "level";
        private static final String SOURCE      = "source";

        @Override
        public void storeInBundle( Bundle bundle ) {
                super.storeInBundle( bundle );
                bundle.put( LEVEL, level );
                bundle.put( SOURCE, source );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
                super.restoreFromBundle( bundle );
                level = bundle.getFloat( LEVEL );
                source = bundle.getClass( SOURCE );
        }

        public void set( float level ) {
                set( level, null );
        }

        public void set( float level, Class source ){
                set( level, source, null );
        }

        /** set 并携带一条伤害来源链（如「装甲石像的荆棘刻印」）。 */
        public void set( float level, Class source, List<?> chain ){
                if (this.level < level) {
                        this.level = Math.max(this.level, level);
                        this.source = source;
                        if (chain != null) {
                                this.causeChain.clear();
                                for (Object c : chain) {
                                        if (c != null) this.causeChain.add(c);
                                }
                        }
                }
        }

        /** 流血伤害来源链（供 DamageInfo 注入与死亡追踪）。 */
        public List<Object> getCauseChain() {
                return new ArrayList<>(causeChain);
        }

        @Override
        public String icon() {
                return BuffIndicator.BLEEDING;
        }

        @Override
        public String iconTextDisplay() {
                return Integer.toString(Math.round(level));
        }

        @Override
        public boolean act() {
                if (target.isAlive()) {

                        level = Random.NormalFloat(level / 2f, level);
                        int dmg = Math.round(level);

                        if (dmg > 0) {

                                target.damage(new DamageInfo(dmg, DamageType.BLEEDING, null, null, this).setCauseChain(causeChain));
                                if (target.sprite.visible) {
                                        Splash.at( target.sprite.center(), -PointF.PI / 2, PointF.PI / 6,
                                                        target.sprite.blood(), Math.min( 10 * dmg / target.HT, 10 ) );
                                }

                                if (target == Dungeon.hero && !target.isAlive()) {
                                        if (source == Chasm.class){
                                                Badges.validateDeathFromFalling();
                                        } else if (source == Sacrificial.class){
                                                Badges.validateDeathFromFriendlyMagic();
                                        }
                                        Dungeon.fail( this );
                                        GLog.n( Messages.get(this, "ondeath") );
                                }

                                if (source == Sickle.HarvestBleedTracker.class && !target.isAlive()){
                                        MeleeWeapon.onAbilityKill(Dungeon.hero, target);
                                }

                                spend( TICK );
                        } else {
                                detach();
                        }

                } else {

                        detach();

                }

                return true;
        }

        @Override
        public String desc() {
                return Messages.get(this, "desc", Math.round(level));
        }
}