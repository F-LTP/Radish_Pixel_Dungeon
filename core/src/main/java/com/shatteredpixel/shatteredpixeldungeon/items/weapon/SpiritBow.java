/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpiritBow extends Weapon {
	
	public static final String AC_SHOOT		= "SHOOT";

	{
		image = ItemSpriteSheet.SPIRIT_BOW;
		
		defaultAction = AC_SHOOT;
		usesTargeting = true;

		unique = true;
		bones = false;
	}
	
	public boolean sniperSpecial = false;
	public float sniperSpecialBonusDamage = 0f;
	//public boolean specialAttack = false;
	/*
	@Override
	public float castDelay(Char user, int dst){
		if(specialAttack) return 0;
		return super.castDelay(user, dst);
	}

	 */

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.remove(AC_EQUIP);
		actions.add(AC_SHOOT);
		return actions;
	}
	
	@Override
	public void execute(Hero hero, String action) {
		
		super.execute(hero, action);
		
		if (action.equals(AC_SHOOT)) {
			
			curUser = hero;
			curItem = this;
			GameScene.selectCell( shooter );
			
		}
	}

	private static Class[] harmfulPlants = new Class[]{
			Blindweed.class, Firebloom.class, Icecap.class, Sorrowmoss.class,  Stormvine.class
	};

	@Override
	public int proc(Char attacker, Char defender, int damage) {

		if(hero.pointsInTalent(Talent.LAND_HEART) >= 4){
			if((Dungeon.level.map[defender.pos] == Terrain.EMBERS) || (Dungeon.level.map[defender.pos] == Terrain.GRASS)){
				if(Random.Float()<=0.15f){
					Plant plant = (Plant) Reflection.newInstance(Random.element(harmfulPlants));
					plant.pos = defender.pos;
					plant.activate( defender.isAlive() ? defender : null );
				}
			}
		}

		if (attacker.buff(NaturesPower.naturesPowerTracker.class) != null && !sniperSpecial){

			Actor.add(new Actor() {
				{
					actPriority = VFX_PRIO;
				}

				@Override
				protected boolean act() {

					if (Random.Int(12) < ((Hero)attacker).pointsInTalent(Talent.NATURES_WRATH)){
						Plant plant = (Plant) Reflection.newInstance(Random.element(harmfulPlants));
						plant.pos = defender.pos;
						plant.activate( defender.isAlive() ? defender : null );
					}

					if (!defender.isAlive()){
						NaturesPower.naturesPowerTracker tracker = attacker.buff(NaturesPower.naturesPowerTracker.class);
						if (tracker != null){
							tracker.extend(((Hero) attacker).pointsInTalent(Talent.WILD_MOMENTUM));
						}
					}

					Actor.remove(this);
					return true;
				}
			});

		}

		return super.proc(attacker, defender, damage);
	}

	@Override
	public String info() {
		String info = desc();
		
		info += "\n\n" + Messages.get( SpiritBow.class, "stats",
				Math.round(augment.damageFactor(min())),
				Math.round(augment.damageFactor(max())),
				STRReq());
		
		if (STRReq() > Dungeon.hero.STR()) {
			info += " " + Messages.get(Weapon.class, "too_heavy");
		} else if (Dungeon.hero.STR() > STRReq()){
			info += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero.STR() - STRReq());
		}
		
		switch (augment) {
			case SPEED:
				info += "\n\n" + Messages.get(Weapon.class, "faster");
				break;
			case DAMAGE:
				info += "\n\n" + Messages.get(Weapon.class, "stronger");
				break;
			case NONE:
		}
		
		if (enchantment != null && (cursedKnown || !enchantment.curse())){
			info += "\n\n" + Messages.get(Weapon.class, "enchanted", enchantment.name());
			info += " " + Messages.get(enchantment, "desc");
		}
		
		if (cursed && isEquipped( Dungeon.hero )) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
		} else if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed");
		} else if (!isIdentified() && cursedKnown){
			info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
		}
		
		info += "\n\n" + Messages.get(MissileWeapon.class, "distance");
		
		return info;
	}
	
	@Override
	public int STRReq(int lvl) {
		return STRReq(1, lvl); //tier 1
	}
	
	@Override
	public int min(int lvl) {
		int dmg = 1 + Dungeon.hero.lvl/5
				+ RingOfSharpshooting.levelDamageBonus(Dungeon.hero)
				+ (curseInfusionBonus ? 1 + Dungeon.hero.lvl/30 : 0);
		return Math.max(0, dmg);
	}
	
	@Override
	public int max(int lvl) {
		int dmg = 6 + (int)(Dungeon.hero.lvl/2.5f)
				+ 2*RingOfSharpshooting.levelDamageBonus(Dungeon.hero)
				+ (curseInfusionBonus ? 2 + Dungeon.hero.lvl/15 : 0);
		return Math.max(0, dmg);
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		return knockArrow().targetingPos(user, dst);
	}
	
	private int targetPos;
	
	@Override
	public int damageRoll(Char owner) {
		int damage = augment.damageFactor(super.damageRoll(owner));
		
		if (owner instanceof Hero) {
			int exStr = ((Hero)owner).STR() - STRReq();
			if (exStr > 0) {
				damage += Random.IntRange( 0, exStr );
			}
		}

		/*
		if(specialAttack){
			switch (hero.pointsInTalent(Talent.STORM_ATTACK)){
				case 1:
					damage *= 0.25f;
					break;
				case 2:
					damage *= 0.5f;
					break;
				case 3:
					damage *= 0.25f;
					break;
				case 4:
					damage *= 0.5f;
					break;
			}
		}
		*/
		if (sniperSpecial){
			damage = Math.round(damage * (1f + sniperSpecialBonusDamage));

			switch (augment){
				case NONE:
					damage = Math.round(damage * 0.667f);
					break;
				case SPEED:
					damage = Math.round(damage * 0.5f);
					break;
				case DAMAGE:
					//as distance increases so does damage, capping at 3x:
					//1.20x|1.35x|1.52x|1.71x|1.92x|2.16x|2.43x|2.74x|3.00x
					int distance = Dungeon.level.distance(owner.pos, targetPos) - 1;
					float multiplier = Math.min(3f, 1.2f * (float)Math.pow(1.125f, distance));
					damage = Math.round(damage * multiplier);
					break;
			}
		}

		if(hero.hasTalent(Talent.BOW_DULES) && hero.pointsInTalent(Talent.BOW_DULES)>=4) damage *= 1.35f;

		return damage;
	}
	
	@Override
	protected float baseDelay(Char owner) {
		if (sniperSpecial){
			switch (augment){
				case NONE: default:
					return 0f;
				case SPEED:
					return 1f;
				case DAMAGE:
					return 2f;
			}
		} else{
			return super.baseDelay(owner);
		}
	}

	@Override
	protected float speedMultiplier(Char owner) {
		float speed = super.speedMultiplier(owner);
		if (owner.buff(NaturesPower.naturesPowerTracker.class) != null){
			// +33% speed to +50% speed, depending on talent points
			speed += ((8 + ((Hero)owner).pointsInTalent(Talent.GROWING_POWER)) / 24f);
		}
		return speed;
	}

	@Override
	public int level() {
		int level = Dungeon.hero == null ? 0 : Dungeon.hero.lvl/5;
		if (curseInfusionBonus) level += 1 + level/6;
		return level;
	}

	@Override
	public int buffedLvl() {
		//level isn't affected by buffs/debuffs
		return level();
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	public SpiritArrow knockArrow(){
		return new SpiritArrow();
	}

	public ALTSpiritArrow altknockArrow(){
		return new ALTSpiritArrow();
	}

	public class ALTSpiritArrow extends MissileWeapon {

		{
			image = ItemSpriteSheet.SPIRIT_ALT_ARROW;

			hitSound = Assets.Sounds.HIT_ARROW;
		}

		@Override
		public Emitter emitter() {
			if (Dungeon.hero.buff(NaturesPower.naturesPowerTracker.class) != null && !sniperSpecial){
				Emitter e = new Emitter();
				e.pos(5, 5);
				e.fillTarget = false;
				e.pour(LeafParticle.GENERAL, 0.01f);
				return e;
			} else {
				return super.emitter();
			}
		}

		@Override
		public int damageRoll(Char owner) {
			return SpiritBow.this.damageRoll(owner);
		}
		@Override
		public int max(){
			int damgeMuilt = 1;
			if(hero.pointsInTalent(Talent.STORM_ATTACK) == 1){
				damgeMuilt = 4;
			} else if(hero.pointsInTalent(Talent.STORM_ATTACK) == 2){
				damgeMuilt = 2;
			}
			return (SpiritBow.this.max()/damgeMuilt);
		}
		@Override
		public int min(){
			int damgeMuilt = 1;
			if(hero.pointsInTalent(Talent.STORM_ATTACK) == 1){
				damgeMuilt = 4;
			} else if(hero.pointsInTalent(Talent.STORM_ATTACK) == 2){
				damgeMuilt = 2;
			}
			return (SpiritBow.this.min()/damgeMuilt);
		}
		@Override
		public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
			return SpiritBow.this.hasEnchant(type, owner);
		}

		@Override
		public int proc(Char attacker, Char defender, int damage) {
			return SpiritBow.this.proc(attacker, defender, damage);
		}

		@Override
		public float delayFactor(Char user) {
			return SpiritBow.this.delayFactor(user);
		}

		@Override
		public float accuracyFactor(Char owner, Char target) {
			if (sniperSpecial && SpiritBow.this.augment == Augment.DAMAGE){
				return Float.POSITIVE_INFINITY;
			} else {
				return super.accuracyFactor(owner, target);
			}
		}

		@Override
		public int STRReq(int lvl) {
			return SpiritBow.this.STRReq(lvl);
		}

		@Override
		protected void onThrow( int cell ) {
			Char enemy = Actor.findChar( cell );
			if (enemy == null || enemy == curUser) {
				parent = null;
				Splash.at( cell, 0xCC99FFFF, 1 );
			} else {
				if (!curUser.shoot( enemy, this )) {
					Splash.at(cell, 0xCC99FFFF, 1);
				}
				if (sniperSpecial && SpiritBow.this.augment != Augment.SPEED) sniperSpecial = false;
			}
		}

		@Override
		public void throwSound() {
			Sample.INSTANCE.play( Assets.Sounds.ATK_SPIRITBOW, 1, Random.Float(0.87f, 1.15f) );
		}

		int flurryCount = -1;
		Actor flurryActor = null;

		@Override
		public void cast(final Hero user, final int dst) {
			final int cell = throwPos( user, dst );
			SpiritBow.this.targetPos = cell;
			if (sniperSpecial && SpiritBow.this.augment == Augment.SPEED){
				if (flurryCount == -1) flurryCount = 3;

				final Char enemy = Actor.findChar( cell );

				if (enemy == null){
					user.spendAndNext(castDelay(user, dst));
					sniperSpecial = false;
					flurryCount = -1;

					if (flurryActor != null){
						flurryActor.next();
						flurryActor = null;
					}
					return;
				}
				QuickSlotButton.target(enemy);

				final boolean last = flurryCount == 1;

				user.busy();

				throwSound();

				((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
						reset(user.sprite,
								cell,
								this,
								new Callback() {
									@Override
									public void call() {
										if (enemy.isAlive()) {
											curUser = user;
											onThrow(cell);
										}

										if (last) {
											user.spendAndNext(castDelay(user, dst));
											sniperSpecial = false;
											flurryCount = -1;
										}

										if (flurryActor != null){
											flurryActor.next();
											flurryActor = null;
										}
									}
								});

				user.sprite.zap(cell, new Callback() {
					@Override
					public void call() {
						flurryCount--;
						if (flurryCount > 0){
							Actor.add(new Actor() {

								{
									actPriority = VFX_PRIO-1;
								}

								@Override
								protected boolean act() {
									flurryActor = this;
									int target = QuickSlotButton.autoAim(enemy, ALTSpiritArrow.this);
									if (target == -1) target = cell;
									cast(user, target);
									Actor.remove(this);
									return false;
								}
							});
							curUser.next();
						}
					}
				});

			} else {

				if (user.hasTalent(Talent.SEER_SHOT)
						&& user.buff(Talent.SeerShotCooldown.class) == null){
					int shotPos = throwPos(user, dst);
					if (Actor.findChar(shotPos) == null) {
						RevealedArea a = Buff.affect(user, RevealedArea.class, 5 * user.pointsInTalent(Talent.SEER_SHOT));
						a.depth = Dungeon.depth;
						a.pos = shotPos;
						Buff.affect(user, Talent.SeerShotCooldown.class, 20f);
					}
				}

				super.cast(user, dst);
			}
		}
	}
	
	public class SpiritArrow extends MissileWeapon {
		
		{
			image = ItemSpriteSheet.SPIRIT_ARROW;

			hitSound = Assets.Sounds.HIT_ARROW;
		}

		@Override
		public Emitter emitter() {
			if (Dungeon.hero.buff(NaturesPower.naturesPowerTracker.class) != null && !sniperSpecial){
				Emitter e = new Emitter();
				e.pos(5, 5);
				e.fillTarget = false;
				e.pour(LeafParticle.GENERAL, 0.01f);
				return e;
			} else {
				return super.emitter();
			}
		}

		@Override
		public int damageRoll(Char owner) {
			return SpiritBow.this.damageRoll(owner);
		}
		@Override
		public int max(){
			return SpiritBow.this.max();
		}
		@Override
		public int min(){
			return SpiritBow.this.min();
		}
		@Override
		public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
			return SpiritBow.this.hasEnchant(type, owner);
		}
		
		@Override
		public int proc(Char attacker, Char defender, int damage) {
			return SpiritBow.this.proc(attacker, defender, damage);
		}
		
		@Override
		public float delayFactor(Char user) {
			return SpiritBow.this.delayFactor(user);
		}
		
		@Override
		public float accuracyFactor(Char owner, Char target) {
			if (sniperSpecial && SpiritBow.this.augment == Augment.DAMAGE){
				return Float.POSITIVE_INFINITY;
			} else {
				return super.accuracyFactor(owner, target);
			}
		}
		
		@Override
		public int STRReq(int lvl) {
			return SpiritBow.this.STRReq(lvl);
		}

		@Override
		protected void onThrow( int cell ) {
			Char enemy = Actor.findChar( cell );
			if (enemy == null || enemy == curUser) {
				parent = null;
				Splash.at( cell, 0xCC99FFFF, 1 );
			} else {
				if (!curUser.shoot( enemy, this )) {
					Splash.at(cell, 0xCC99FFFF, 1);
				}
				if (sniperSpecial && SpiritBow.this.augment != Augment.SPEED) sniperSpecial = false;
			}
		}

		@Override
		public void throwSound() {
			Sample.INSTANCE.play( Assets.Sounds.ATK_SPIRITBOW, 1, Random.Float(0.87f, 1.15f) );
		}

		int flurryCount = -1;
		Actor flurryActor = null;

		@Override
		public void cast(final Hero user, final int dst) {
			final int cell = throwPos( user, dst );
			SpiritBow.this.targetPos = cell;
			if (sniperSpecial && SpiritBow.this.augment == Augment.SPEED){
				if (flurryCount == -1) flurryCount = 3;
				
				final Char enemy = Actor.findChar( cell );
				
				if (enemy == null){
					user.spendAndNext(castDelay(user, dst));
					sniperSpecial = false;
					flurryCount = -1;

					if (flurryActor != null){
						flurryActor.next();
						flurryActor = null;
					}
					return;
				}
				QuickSlotButton.target(enemy);
				
				final boolean last = flurryCount == 1;


				user.busy();
				
				throwSound();
				
				((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
						reset(user.sprite,
								cell,
								this,
								new Callback() {
									@Override
									public void call() {
										if (enemy.isAlive()) {
											curUser = user;
											onThrow(cell);
										}
										
										if (last) {
											user.spendAndNext(castDelay(user, dst));
											sniperSpecial = false;
											flurryCount = -1;
										}

										if (flurryActor != null){
											flurryActor.next();
											flurryActor = null;
										}
									}
								});
				
				user.sprite.zap(cell, new Callback() {
					@Override
					public void call() {
						flurryCount--;
						if (flurryCount > 0){
							Actor.add(new Actor() {

								{
									actPriority = VFX_PRIO-1;
								}

								@Override
								protected boolean act() {
									flurryActor = this;
									int target = QuickSlotButton.autoAim(enemy, SpiritArrow.this);
									if (target == -1) target = cell;
									cast(user, target);
									Actor.remove(this);
									return false;
								}
							});
							curUser.next();
						}
					}
				});
				
			} else {

				if (user.hasTalent(Talent.SEER_SHOT)
						&& user.buff(Talent.SeerShotCooldown.class) == null){
					int shotPos = throwPos(user, dst);
					if (Actor.findChar(shotPos) == null) {
						RevealedArea a = Buff.affect(user, RevealedArea.class, 5 * user.pointsInTalent(Talent.SEER_SHOT));
						a.depth = Dungeon.depth;
						a.pos = shotPos;
						Buff.affect(user, Talent.SeerShotCooldown.class, 20f);
					}
				}

				super.cast(user, dst);
			}
		}
	}
	
	private CellSelector.Listener shooter = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target != null) {
				if (hero.pointsInTalent(Talent.STORM_ATTACK) >= 3) {
					// Main arrow towards the primary target
					Ballistica mainBall = new Ballistica(curUser.pos, target, Ballistica.PROJECTILE);
					if (Char.findChar(mainBall.collisionPos) == null || Char.findChar(mainBall.collisionPos).alignment != Char.Alignment.ENEMY) {
						altknockArrow().cast(curUser, target);
					} else {
						// Proceed with primary target logic
						if (Char.findChar(mainBall.collisionPos) == Char.findChar(target)) {
							Collection<Mob> mobs = Dungeon.level.mobs;
							if (!mobs.isEmpty()) {
								// Filter mobs within hero's FOV
								List<Mob> visibleMobs = new ArrayList<>();
								for (Mob mob : mobs) {
									if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY) {
										visibleMobs.add(mob);
									}
								}
								if (!visibleMobs.isEmpty() && !(visibleMobs.size() == 1 && visibleMobs.get(0).equals(Char.findChar(target)))) {
									// Select the first secondary target (different from primary)
									Mob secondaryTarget1;
									int randomIndex1;
									do {
										randomIndex1 = (int) (Math.random() * visibleMobs.size());
										secondaryTarget1 = visibleMobs.get(randomIndex1);
									} while (secondaryTarget1.equals(Char.findChar(target)));

									// Launch the first secondary arrow
									Ballistica secondaryBall1 = new Ballistica(curUser.pos, secondaryTarget1.pos, Ballistica.PROJECTILE);
									if (secondaryBall1.collisionPos.equals(secondaryTarget1.pos)) {
										//specialAttack = true;
										altknockArrow().cast(curUser, secondaryTarget1.pos);
									}

									// Select the second secondary target (different from primary and first secondary)
									List<Mob> remainingMobs = new ArrayList<>(visibleMobs);
									remainingMobs.remove(secondaryTarget1); // Remove the first secondary target
									if (!remainingMobs.isEmpty()) {
										Mob secondaryTarget2 = remainingMobs.get((int) (Math.random() * remainingMobs.size()));

										// Launch the second secondary arrow
										Ballistica secondaryBall2 = new Ballistica(curUser.pos, secondaryTarget2.pos, Ballistica.PROJECTILE);
										if (secondaryBall2.collisionPos.equals(secondaryTarget2.pos)) {
											//specialAttack = true;
											altknockArrow().cast(curUser, secondaryTarget2.pos);
										}
									}
								}
							}
							// Always shoot the main arrow at the primary target
							altknockArrow().cast(curUser, target);
						}
					}
				} else if (hero.pointsInTalent(Talent.STORM_ATTACK) <= 2 && hero.pointsInTalent(Talent.STORM_ATTACK) >0) {
					//for ling code block
					Ballistica ballForFoe = new Ballistica(curUser.pos, target, Ballistica.PROJECTILE);
					//if we got the floor or a pal nothin happen
					if (Char.findChar(ballForFoe.collisionPos) == null || Char.findChar(ballForFoe.collisionPos).alignment != Char.Alignment.ENEMY) {
						altknockArrow().cast(curUser, target);
					} else {
						//if we got not the char we wanted, we act like as it was the TARGET ALL ALONG MUAHAHAHAH
						if (Char.findChar(ballForFoe.collisionPos) == Char.findChar(target)) {
							Collection<Mob> mobs = Dungeon.level.mobs;
							if (!mobs.isEmpty()) {
								// Filter mobs within hero's FOV
								List<Mob> visibleMobs = new ArrayList<>();
								for (Mob mob : mobs) {
									if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY) {
										visibleMobs.add(mob);
									}
								}
								if (!visibleMobs.isEmpty() && !(visibleMobs.size() == 1 && visibleMobs.get(0).equals(Char.findChar(target)))) {
									// Select a random mob from visibleMobs, who is not an ally and is not the initial target/defender mob
									Mob randomMob;
									int randomIndex;
									do {
										randomIndex = (int) (Math.random() * visibleMobs.size());
									} while (visibleMobs.get(randomIndex) == Char.findChar(target));
									randomMob = visibleMobs.get(randomIndex);
									Ballistica ballForRan = new Ballistica(curUser.pos, randomMob.pos, Ballistica.PROJECTILE);
									// ok, our first arrow has a line of sight, we got the main target
									//now we work with the second mob...
									if (ballForRan.collisionPos.equals(randomMob.pos)) {//if we got the second mob we got the second mob, hooray
										//specialAttack = true;
										altknockArrow().cast(curUser, randomMob.pos);
									} else if (Dungeon.level.map[ballForRan.collisionPos] == Terrain.SOLID) { //if we got a wall we got a wall, sad
										//specialAttack = true;
										altknockArrow().cast(curUser, randomMob.pos);
									}
									//if we still somehow got a third smbdy who is not our secondary target, attack the second one directly
									else if (Char.findChar(ballForRan.collisionPos) != null && Char.findChar(ballForRan.collisionPos) != Char.findChar(randomMob.pos)) {
										Hero user = curUser;
										((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
												reset(user.pos,
														randomMob.pos,
														new ALTSpiritArrow(),
                                                        () -> {
                                                            if (randomMob.isAlive()) {
                                                                curUser = user;
																//specialAttack = true;
                                                                (new ALTSpiritArrow()).onThrow(randomMob.pos);
                                                            }
                                                        });
									}

									//In other cases we just shoot there and see what happens.
									else altknockArrow().cast(curUser, randomMob.pos);
								}
							}
							altknockArrow().cast(curUser, target);
						} else onSelect(ballForFoe.collisionPos);
					}
				} else {
					knockArrow().cast(curUser, target);
				}
			}
		}
		@Override
		public String prompt() {
			return Messages.get(SpiritBow.class, "prompt");
		}


	};
}
