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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.engineer;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM200;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM201;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Golem;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;

import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
public class SummoningBeacon extends ArmorAbility {

	{
		baseChargeUse = 50;
	}

	static final HashMap<Class<? extends Mob>, Float> MACHINES = new HashMap<>();

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {

		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

		if (hero.hasTalent(Talent.VIOLENT_LEAP))
			for (int j : PathFinder.NEIGHBOURS8) {
				Char c = Actor.findChar(hero.pos + j);
				if (c != null && c.alignment != Char.Alignment.ALLY) {
					c.damage(new DamageInfo(Random.IntRange(5 + 5 * hero.pointsInTalent(Talent.VIOLENT_LEAP),
							5 + 10 * hero.pointsInTalent(Talent.VIOLENT_LEAP)), DamageType.PHYSICAL, hero));

					if (c.isAlive()){
						//trace a ballistica to our target (which will also extend past them)
						Ballistica trajectory = new Ballistica(hero.pos, c.pos, Ballistica.STOP_TARGET);
						//trim it to just be the part that goes past them
						trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
						//knock them back along that ballistica
						WandOfBlastWave.throwChar(c, trajectory, 1, false, true, hero);
					}

				}
			}

		MACHINES.clear();
		MACHINES.put(DM100.class, 10f - hero.pointsInTalent(Talent.POWERED_DEVICE));
		MACHINES.put(DM200.class, 10f);
		MACHINES.put(Golem.class, 10f + hero.pointsInTalent(Talent.POWERED_DEVICE));

		int machinesToSapwn = 2;
		float chance = hero.pointsInTalent(Talent.SKYNET) * 0.75f;
		while (Random.Float() < chance){
			machinesToSapwn ++;
			chance --;
		}
		for (int i = 0; i < machinesToSapwn; i++) {
			Mob machine = Reflection.newInstance(Random.chances(MACHINES));
			if (Random.Float() < 1/50f * RatSkull.exoticChanceMultiplier() && machine instanceof DM200)
				machine = new DM201();

			boolean haveSpace = false;
			ArrayList<Integer> spawnPoints = new ArrayList<>();
			for (int j : PathFinder.NEIGHBOURS8)
				if (Dungeon.level.passable[hero.pos + j] && Actor.findChar(hero.pos + j) == null) {
					if (!Char.hasProp(machine, Char.Property.LARGE) || Dungeon.level.openSpace[hero.pos + j])
						spawnPoints.add(hero.pos + j);
					haveSpace = true;
				}

			if (!haveSpace) {
				i = machinesToSapwn;
				continue;
			}
			if (spawnPoints.isEmpty()) {
				i --;
				continue;
			}
			int pos = Random.element(spawnPoints);

			machine.state = machine.HUNTING;
			Buff.affect(machine, AscensionChallenge.AscensionBuffBlocker.class);
			GameScene.add( machine );
			ScrollOfTeleportation.appear( machine, pos );
			Buff.affect(machine, ScrollOfSirensSong.Enthralled.class);
			if (hero.hasTalent(Talent.ASSAULT))
				Buff.affect(machine, Adrenaline.class, hero.pointsInTalent(Talent.ASSAULT) * 1f + 1.67f);
			//act priority problem
		}

		armor.charge -= chargeUse(hero);
		hero.spendAndNext(Actor.TICK);
		Item.updateQuickslot();

	}

	@Override
	public String icon(){
		return HeroIcon.SUMMON_BEACON;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.POWERED_DEVICE, Talent.SKYNET, Talent.VIOLENT_LEAP, Talent.ASSAULT, Talent.HEROIC_ENERGY};
	}
}