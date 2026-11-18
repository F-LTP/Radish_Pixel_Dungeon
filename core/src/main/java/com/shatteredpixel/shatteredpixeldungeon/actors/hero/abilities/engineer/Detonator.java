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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.AdrenalineBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.ArcaneBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.FlashBangBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.FrostBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.HolyBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.RegrowthBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.StenchBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.TimeBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.WoollyBomb;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
public class Detonator extends ArmorAbility {

	{
		baseChargeUse = 65;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public float chargeUse(Hero hero) {
		if (getDetonator() != null) return 0;
		else return super.chargeUse(hero);
	}

	@Override
	public String icon() {
		return HeroIcon.DETONATOR;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target == null) return;
		DetonationMob detonator = getDetonator();

		int dist = Dungeon.level.distance(hero.pos, target);
		if (dist == 0){
			//the hero chose himself, explode the detonator
			if (detonator != null) detonator.damage(new DamageInfo(1, DamageType.PHYSICAL, null, null, Detonator.class));
			else GLog.w(Messages.get(this, "invalid_pos"));

		} else if (dist <= 1 + hero.pointsInTalent(Talent.PORTABLE_DETONATOR)) {
			if (Dungeon.level.heroFOV[target] && Dungeon.level.passable[target] && Actor.findChar(target) == null){

				if ( armor.charge < chargeUse(hero)) GLog.w(Messages.get(ClassArmor.class, "low_charge"));
				else {
					//the hero chose a new place, set a new detonator
					if (detonator != null) {
						detonator.die(Detonator.class);
						GLog.h(Messages.get(this, "demolish"));
					}
					if (armor.charge < chargeUse(hero)){
						GLog.w(Messages.get(ClassArmor.class, "low_charge"));
						return;
					}
					armor.charge -= chargeUse(hero);

					detonator = new DetonationMob();
					detonator.pos = target;
					GameScene.add(detonator);
					Dungeon.level.occupyCell(detonator);

					hero.sprite.attack(target);
					hero.spendAndNext(1f);

					Item.updateQuickslot();
					Sample.INSTANCE.play(Assets.Sounds.BEACON);

				}
			} else GLog.w(Messages.get(this, "invalid_pos"));

		} else {
			//the hero chose a far place, demolish the detonator
			if (detonator == null) GLog.w(Messages.get(this, "too_far"));
			else {
				detonator.die(Detonator.class);
				hero.sprite.operate(target);
				GLog.h(Messages.get(this, "demolish"));
			}
		}
		hero.next();
	}

	private static DetonationMob getDetonator() {
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0]))
			if (m instanceof DetonationMob) return (DetonationMob) m;
		return null;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.STACKED_CHARGE, Talent.ALCHEMY_CRAFT, Talent.DECOY, Talent.PORTABLE_DETONATOR, Talent.HEROIC_ENERGY};
	}

	public static class DetonationMob extends Mob {

		{
			spriteClass = DetonatorSprite.class;

			alignment = Alignment.ALLY;
			HP = HT = 1;
			properties.add(Property.INORGANIC);
			properties.add(Property.IMMOVABLE);
			properties.add(Property.STATIC);

			actPriority = MOB_PRIO + 1;
			state = WANDERING;
		}
		int explosionProcess = -1;

		@Override
		protected boolean act() {
			if (explosionProcess >= 0) processExplode();
			else diactivate();
			return true;
		}

		@Override
		protected boolean getCloser(int target) {
			return false;
		}

		@Override
		protected boolean getFurther(int target) {
			return false;
		}

		@Override
		public void damage(DamageInfo info) {
			if (info.getSource() == Detonator.class) {
				super.damage(info);
				return;
			}
			if (explosionProcess < 0){
				explosionProcess = 0;
				timeToNow();
				if (Dungeon.hero.hasTalent(Talent.DECOY) && info.getSource() != DetonationMob.class){
					for (Mob m : Dungeon.level.mobs.toArray(new Mob[0]))
						if (m.alignment != Alignment.ALLY) m.beckon(pos);
					spendConstant(4 + 4 * Dungeon.hero.pointsInTalent(Talent.DECOY));

					CellEmitter.center( pos ).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
					Sample.INSTANCE.play( Assets.Sounds.ALERT );
				}
				((DetonatorSprite)sprite).ignite();
			}
		}

		@Override
		public boolean add(Buff buff) {
			return false;
		}

		@Override
		public void die(Object cause) {
			super.die(cause);
			if (cause == Detonator.class){
				ClassArmor armor = Dungeon.hero.belongings.getItem(ClassArmor.class);
				if (armor != null && explosionProcess < 0) {
					armor.charge = Math.min(100, armor.charge +
						Dungeon.hero.armorAbility.chargeUse(Dungeon.hero) * (4 + Dungeon.hero.pointsInTalent(Talent.PORTABLE_DETONATOR)) / 8);
					Item.updateQuickslot();
				}
			}
		}

		private void processExplode(){
			if (explosionProcess < 2 + Dungeon.hero.pointsInTalent(Talent.STACKED_CHARGE)){
				switch (explosionProcess){
					case 0: default:
						explode(pos);
						GLog.w(Messages.get(this, "explode"));
						break;
					case 1:
						for (int i : PathFinder.NEIGHBOURS4) explode(pos + i);
						break;
					case 2:
						for (int i = 0; i < PathFinder.CIRCLE4.length; i++)
							explode(pos + PathFinder.CIRCLE4[i] + PathFinder.CIRCLE4[(i + 1)%4]);
						break;
					case 3:
						for (int i : PathFinder.NEIGHBOURS4) explode(pos + 2 * i);
						break;
					case 4:
						for (int i = 0; i < PathFinder.CIRCLE4.length; i++)
							explode(pos + 2 * (PathFinder.CIRCLE4[i] + PathFinder.CIRCLE4[(i + 1)%4]));
						break;
					case 5:
						for (int i = 0; i < PathFinder.CIRCLE8.length; i++)
							explode(pos + PathFinder.CIRCLE8[i] + PathFinder.CIRCLE8[(i + 1)%8]);
						break;
				}
				explosionProcess ++;
				if (explosionProcess >= 2 + Dungeon.hero.pointsInTalent(Talent.STACKED_CHARGE))
					die(this);

				spend(1f);
				next();
			} else die(this);
		}

		private void explode(int cell){
			if (!Dungeon.level.insideMap(cell)) return;

			Bomb b = new Bomb();
			if (Random.Int(12) < Dungeon.hero.pointsInTalent(Talent.ALCHEMY_CRAFT)){
				Bomb[] bombs = new Bomb[]{
					new AdrenalineBomb(), new ArcaneBomb(), new FlashBangBomb(), new FrostBomb(), new HolyBomb(), new RegrowthBomb(), new StenchBomb(), new TimeBomb(), new WoollyBomb()
			};
				b = Random.oneOf(bombs);
			}
			if (Dungeon.hero.subClass == HeroSubClasses.GRENADIER){
				Dungeon.hero.subClass = HeroSubClasses.NONE;
				b.explode(cell);
				Dungeon.hero.subClass = HeroSubClasses.GRENADIER;

			} else b.explode(cell);
		}

		@Override
		public CharSprite sprite() {// changes the icon in the mob info window
			DetonatorSprite sprite = (DetonatorSprite) super.sprite();

			if (explosionProcess >= 0) sprite.ignite();
			else                       sprite.idle();

			return sprite;
		}

		private static final String PROCESS = "process";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(PROCESS, explosionProcess);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			explosionProcess = bundle.getInt(PROCESS);
		}
	}

	public static class DetonatorSprite extends MobSprite {

		private final Animation ignite;
		//private Emitter spark;

		public DetonatorSprite() {
			super();

			texture( Assets.Sprites.DETONATOR );

			TextureFilm film = new TextureFilm( texture, 5, 15 );

			idle = new Animation( 1, true );
			idle.frames( film, 0);

			ignite = new Animation( 1, true );
			ignite.frames( film, 1);

			run = idle.clone();

			die = new Animation( 1, false );
			die.frames( film, 2 );

			attack = new Animation( 1, false );
			attack.frames( film, 0 );

			idle();
			resetColor();
		}

		public void ignite(){
			play(ignite);
		}

		//no emotions for detonator
		public void showAlert(int color) {}
		public void showLost() {}
		public void showSleep() {}

		public void linkVisuals(Char ch) {
			super.linkVisuals(ch);
			if (((DetonationMob)ch).explosionProcess >= 0) {
				ignite();
			} else play(idle);

		}

		@Override
		public void move(int from, int to) {
			linkVisuals(ch);
			super.move(from, to);
			linkVisuals(ch);
		}

		@Override
		public void die() {
			Splash.at( center(), blood(), 10 );
			super.die();
		}
	}
}
