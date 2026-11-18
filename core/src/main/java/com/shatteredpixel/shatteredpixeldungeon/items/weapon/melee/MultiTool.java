/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PhysicalEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.engineer.ForceField;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Transmuting;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class MultiTool extends MeleeWeapon {

    public static final String AC_DISARM = "disarm";

    public ConductiveLoot loot = null;

    {
        image = ItemSpriteSheet.MULTITOOL;
        hitSound = Assets.Sounds.HIT_STAB;
        hitSoundPitch = 1.1f;

        tier = 1;

        unique = true;
        bones = false;
    }

    @Override
    public int max(int lvl) {
        return  Math.round(2.5f*(tier+1)) +    //5 base, down from 10
                lvl*(tier);                  //+1 scaling, down from +2
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        Buff.affect(defender, Bleeding.class).set(Math.round((min() + 1) * Random.NormalFloat(1f, 1.5f)), attacker.getClass());

        return super.proc( attacker, defender, damage );
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions( hero );
        actions.add(AC_DISARM);
        return actions;
    }

    @Override
    public String defaultAction() {
        return AC_DISARM;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_DISARM)){
            if ((Dungeon.level.map[hero.pos] == Terrain.INACTIVE_TRAP ||
                    (Dungeon.level.map[hero.pos] == Terrain.PEDESTAL && hero.hasTalent(Talent.APART_ANYTHING))
                    && Dungeon.depth != 15)){

                Level.set(hero.pos, Terrain.EMPTY);
                Dungeon.level.traps.remove( hero.pos );
                int quantity = 1;

                for (int i : PathFinder.NEIGHBOURS4) {
                    if (Dungeon.level.map[hero.pos + i] == Terrain.INACTIVE_TRAP){
                        Level.set(hero.pos + i, Terrain.EMPTY);
                        Dungeon.level.traps.remove( hero.pos + i );
                        quantity ++;
                    }
                }
                GameScene.updateMap( hero.pos );

                if (hero.subClass != HeroSubClasses.CRAFTSMAN) quantity = Math.min(quantity, 2);
                Gold gold = new Gold(quantity * 5);

                if (!gold.collect()) Dungeon.level.drop(gold, hero.pos).sprite.drop();

                hero.sprite.operate( hero.pos );
                Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
                hero.spendAndNext(2f);

                if (hero.hasTalent(Talent.GENERAL_DISARM))
                    Buff.affect(hero, PhysicalEmpower.class).set(1 + hero.pointsInTalent(Talent.GENERAL_DISARM), 3);

            } else GLog.w(Messages.get(this, "no_trap"));
        }
    }

    @Override
    public boolean collect(Bag container){
        if (super.collect(container)){

            if (container.owner != null){
                if (loot != null) {
                    if (loot.target != null) loot.detach();
                    loot = null;
                }
                loot = new ConductiveLoot();
                loot.attachTo(container.owner);
            }
            return true;
        } else return false;
    }

    @Override
    public void onDetach( ) {
        if (loot != null) {
            loot.detach();
            loot = null;
        }
    }

    public class ConductiveLoot extends Buff {

        {
            revivePersists = true;
            actPriority = HERO_PRIO + 1;
        }

        @Override
        public boolean attachTo( Char target ) {
            if (super.attachTo( target )) {
                //if we're loading in and the hero has partially spent a turn, delay for 1 turn
                if (target instanceof Hero && Dungeon.hero == null && cooldown() == 0 && target.cooldown() > 0) {
                    spend(TICK);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean act() {
            spendConstant(1);
            return true;
        }

    }


}