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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PhysicalEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class ForceField extends ArmorAbility {

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {

		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);

		Buff.prolong(hero, Field.class, 15);
		armor.charge -= chargeUse(hero);
		hero.spendAndNext(Actor.TICK);
		Item.updateQuickslot();

	}

	@Override
	public String icon(){
		return HeroIcon.FORCE_FIELD;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.MECHANICAL_REFINEMENT, Talent.MOMENTUM_TRANSFORM, Talent.ACTIVE_DEFENSE, Talent.REPAIR_ABILITY, Talent.HEROIC_ENERGY};
	}

	public static class Field extends FlavourBuff {

		{
			announced = false;
			type = buffType.POSITIVE;
		}

		private boolean canDetach = false;

		private int min(){
			return 5 + 5 * Dungeon.hero.pointsInTalent(Talent.MECHANICAL_REFINEMENT);
		}

		private int max(){
			return 15 + 5 * Dungeon.hero.pointsInTalent(Talent.MECHANICAL_REFINEMENT);
		}

		@Override
		public boolean act() {
			diactivate();
			return super.act();
		}

		@Override
		public void fx(boolean on) {
			if (on) {
				target.sprite.add(CharSprite.State.SHIELDED);
			} else if (target.buff(Barrier.class) == null) {
				target.sprite.remove(CharSprite.State.SHIELDED);
			}
		}

		@Override
		public String icon() {
			return BuffIndicator.ARMOR;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0x3287CA);
		}

		@Override
		public String desc() {
			if (!canDetach) return Messages.get(this, "desc", min(), max(), dispTurns());
			return Messages.get(this, "desc_detach", min(), max());
		}

		//flavour buffs can all just rely on cooldown()
		protected String dispTurns() {
			if (!canDetach) return dispTurns(visualcooldown());
			return "";
		}

		@Override
		public String iconTextDisplay() {
			if (!canDetach) return Integer.toString((int)visualcooldown());
			return "";
		}

		@Override
		public void detach() {
			if (canDetach) {
				for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
					if (m.alignment != Char.Alignment.ALLY
							&& (new Ballistica(target.pos, m.pos, Ballistica.PROJECTILE).collisionPos == m.pos
							|| new Ballistica(m.pos, target.pos, Ballistica.PROJECTILE).collisionPos == m.pos)
							&& Dungeon.level.distance(target.pos, m.pos) <= 2){
						Buff.affect(m, Blindness.class, 5f);
						Buff.affect(m, Vertigo.class, 5f);
						if (Dungeon.hero.hasTalent(Talent.ACTIVE_DEFENSE)){
							//trace a ballistica to our target (which will also extend past them)
							Ballistica trajectory = new Ballistica(target.pos, m.pos, Ballistica.STOP_TARGET);
							//trim it to just be the part that goes past them
							trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
							//knock them back along that ballistica
							WandOfBlastWave.throwChar(m, trajectory, 1 + Dungeon.hero.pointsInTalent(Talent.ACTIVE_DEFENSE), true, false, target);
						}
					}
				}
				WandOfBlastWave.BlastWave.blast(target.pos, 2);
				Sample.INSTANCE.play(Assets.Sounds.MISS);

				// 修复能力：力场消散时修复英雄自身
				if (Dungeon.hero.hasTalent(Talent.REPAIR_ABILITY)) {
					int heal = 3 * Dungeon.hero.pointsInTalent(Talent.REPAIR_ABILITY);
					Dungeon.hero.HP = Math.min(Dungeon.hero.HT, Dungeon.hero.HP + heal);
					Bleeding bleeding = Dungeon.hero.buff(Bleeding.class);
					if (bleeding != null) bleeding.detach();
				}

				super.detach();
			}
			else canDetach = true;
		}

		public int hit(int dmg){
			int dr = Random.Int(min(), max());
			int dmgDec = Math.min(dmg, dr);
			dmg = Math.max(dmg - dr, 0);
			if (Dungeon.hero.hasTalent(Talent.MOMENTUM_TRANSFORM) && dmgDec > 0)
				Buff.affect(target, PhysicalEmpower.class)
						.set(dmgDec * Dungeon.hero.pointsInTalent(Talent.MOMENTUM_TRANSFORM) / 5, 1);
			if (canDetach) detach();
			return dmg;
		}

		private static final String CAN_DETACH = "can_detach";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(CAN_DETACH, canDetach);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			canDetach   = bundle.getBoolean(CAN_DETACH);
		}
	}
}