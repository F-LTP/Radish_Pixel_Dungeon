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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WingSword extends MeleeWeapon {

    public static final String AC_DIVE_ATTACK = "DIVE_ATTACK";

    {
        image = ItemSpriteSheet.WINGSWORD;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;
        defaultAction = AC_DIVE_ATTACK;
        tier = 3;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (Random.Float() < 0.4f + 0.04f * buffedLvl())
            Buff.affect(attacker, Levitation.class, 8 + buffedLvl());
        return super.proc(attacker, defender, damage);
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (hero.belongings.weapon() == this) {
            actions.add(AC_DIVE_ATTACK);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {

        super.execute(hero, action);

        if (action.equals(AC_DIVE_ATTACK)) {
            Levitation levitation = hero.buff(Levitation.class);
            if (levitation == null) {
                GLog.w(Messages.get(WingSword.class, "dive_no_levitation"));
                Sample.INSTANCE.play(Assets.Sounds.STEP);
            } else {
                int dmgBoost = augment.damageFactor(Math.round(2 * buffedLvl() + levitation.cooldown() / 2));

                GameScene.selectCell(new CellSelector.Listener() {
                    @Override
                    public void onSelect(Integer target) {
                        if (target == null) {
                            return;
                        }

                        Char enemy = Actor.findChar(target);
                        if (Dungeon.level.heroFOV[target]) {
                            if (enemy == null || enemy == hero || hero.isCharmedBy(enemy)) {
                                GLog.w(Messages.get(WingSword.class, "ability_no_target"));
                                return;
                            }
                        }

                        if (hero.rooted || Dungeon.level.distance(hero.pos, target) < 2
                                || Dungeon.level.distance(hero.pos, target) - 1 > reachFactor(hero)) {
                            GLog.w(Messages.get(WingSword.class, "ability_target_range"));
                            if (hero.rooted) PixelScene.shake(1, 1f);
                            return;
                        }

                        int lungeCell = -1;
                        for (int i : PathFinder.NEIGHBOURS8) {
                            if (Dungeon.level.distance(hero.pos + i, target) <= reachFactor(hero)
                                    && Actor.findChar(hero.pos + i) == null
                                    && (Dungeon.level.passable[hero.pos + i] || (Dungeon.level.avoid[hero.pos + i] && hero.flying))) {
                                if (lungeCell == -1 || Dungeon.level.trueDistance(hero.pos + i, target) < Dungeon.level.trueDistance(lungeCell, target)) {
                                    lungeCell = hero.pos + i;
                                }
                            }
                        }

                        if (lungeCell == -1) {
                            GLog.w(Messages.get(WingSword.class, "ability_target_range"));
                            return;
                        }

                        final int dest = lungeCell;

                        hero.busy();
                        Sample.INSTANCE.play(Assets.Sounds.MISS);
                        hero.sprite.jump(hero.pos, dest, 0, 0.1f, new Callback() {
                            @Override
                            public void call() {
                                if (Dungeon.level.map[hero.pos] == Terrain.OPEN_DOOR) {
                                    Door.leave(hero.pos);
                                }
                                hero.pos = dest;
                                Dungeon.level.occupyCell(hero);
                                Dungeon.observe();

                                if (enemy != null && hero.canAttack(enemy)) {
                                    hero.sprite.attack(enemy.pos, new Callback() {
                                        @Override
                                        public void call() {
                                            AttackIndicator.target(enemy);
                                            if (hero.attack(enemy, 1, dmgBoost, Char.INFINITE_ACCURACY)) {
                                                Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
                                            }
                                            Invisibility.dispel();
                                            hero.spendAndNext(hero.attackDelay());
                                            levitation.detach();
                                        }
                                    });
                                } else {
                                    GLog.w(Messages.get(WingSword.class, "ability_no_target"));
                                    hero.spendAndNext(1 / hero.speed());
                                    levitation.detach();
                                }
                            }
                        });
                    }

                    @Override
                    public String prompt() {
                        return Messages.get(WingSword.class, "select_enemy");
                    }
                });
            }
        }
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "select_enemy");
    }

    @Override
    public String status() {
        return null;
    }
}
