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
 * but WITHOUT ANY WARRANTY; even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.FungalSpinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Spinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.CorrosionParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SnakeSprite extends MobSprite {

	// Target position for zap attacks
	private int zapPos;

	// Crumple animation for Ghoul-like behavior (fake death)
	private Animation crumple;
	// Leap animation for Ripper-like behavior
	private Animation leap;
	// Charge animation for Goo/DM300/Eye etc. - attack forward/backward loop
	private Animation charge;
	// Pump attack animation for Goo
	private Animation pumpAttack;
	// Slam animation for DM300
	private Animation slam;

	// Particle emitters for Goo pumpUp
	private ArrayList<Emitter> pumpUpEmitters = new ArrayList<>();
	// Spray emitter for Goo (low HP)
	private Emitter spray;
	// Teleport particles for Golem
	private Emitter teleParticles;
	// Earth armor particles for GnollGeomancer/GnollGuard
	private Emitter earthArmor;
	// Supercharge sparks for DM300
	private Emitter superchargeSparks;
	// Elemental particles for YogFist variants
	private Emitter elementalParticles;

	public SnakeSprite() {
		super();

		texture( Assets.Sprites.SNAKE );

		TextureFilm frames = new TextureFilm( texture, 12, 11 );

		//many frames here as we want the rising/falling to be slow but the tongue to be fast
		idle = new Animation( 10, true );
		idle.frames( frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		                     1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 2, 1, 1);

		run = new Animation( 8, true );
		run.frames( frames, 4, 5, 6, 7 );

		attack = new Animation( 15, false );
		attack.frames( frames, 8, 9, 10, 9, 0);

		die = new Animation( 10, false );
		die.frames( frames, 11, 12, 13 );

		crumple = new Animation( 10, false );
		crumple.frames( frames, 11, 12, 13 );

		leap = run.clone();

		charge = new Animation( 15, true );
		charge.frames( frames, 8, 9, 10, 9, 8, 9, 10, 9 );

		pumpAttack = new Animation( 15, false );
		pumpAttack.frames( frames, 8, 9, 10, 9, 8, 9, 10, 9, 8, 0 );

		slam = attack.clone();
		zap = attack.clone();

		play(idle);

		spray = centerEmitter();
		if (spray != null) {
			spray.autoKill = false;
			spray.pour(GreenGooParticle.FACTORY, 0.04f);
			spray.on = false;
		}
	}

	@Override
	public void link(Char ch) {
		super.link(ch);
		// Goo: enable spray when low HP
		if (ch instanceof Goo && ch.HP*2 <= ch.HT) {
			spray(true);
		}
		if (ch instanceof DM300 && ((DM300) ch).isSupercharged()) {
			setupSuperchargeSparks(true);
		}
		// YogFist: create elemental particles
		if (ch instanceof YogFist) {
			elementalParticles = createFistEmitter();
		}
	}

	@Override
	public void update() {
		super.update();
		// Update particle emitters
		if (spray != null) {
			spray.pos(center());
			spray.visible = visible;
		}
		if (teleParticles != null) {
			teleParticles.pos(this);
			teleParticles.visible = visible;
		}
		if (earthArmor != null) {
			earthArmor.visible = visible;
		}
		if (superchargeSparks != null) {
			superchargeSparks.visible = visible;
		}
		if (elementalParticles != null) {
			elementalParticles.visible = visible;
		}
	}

	@Override
	public void die() {
		if (curAnim == crumple) {
			// Already in crumple state, don't replay death animation
			return;
		}
		super.die();
		// Turn off all emitters
		if (spray != null) spray.on = false;
		if (teleParticles != null) teleParticles.on = false;
		if (earthArmor != null) earthArmor.on = false;
		if (superchargeSparks != null) superchargeSparks.on = false;
		if (elementalParticles != null) elementalParticles.on = false;
	}

	@Override
	public void kill() {
		super.kill();
		if (spray != null) spray.killAndErase();
		if (teleParticles != null) teleParticles.killAndErase();
		if (earthArmor != null) earthArmor.killAndErase();
		if (superchargeSparks != null) superchargeSparks.killAndErase();
		if (elementalParticles != null) elementalParticles.killAndErase();
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
			if (ch instanceof Eye) {
				if (Actor.findChar(zapPos) != null) {
					parent.add(new Beam.DeathRay(center(), Actor.findChar(zapPos).sprite.center()));
				} else {
					parent.add(new Beam.DeathRay(center(), DungeonTilemap.raisedTileCenterToWorld(zapPos)));
				}
				((Eye)ch).deathGaze();
				ch.next();
			}
			else if (ch instanceof Spinner) {
				((Spinner)ch).shootWeb();
			}
			else if (ch instanceof FungalSpinner) {
				((FungalSpinner)ch).shootWeb();
			}
			// All other ranged mobs: use generic green magic missile
			else {
				MagicMissile.boltFromChar(parent, MagicMissile.FOLIAGE, this, zapPos, new Callback() {
					@Override
					public void call() {
						// Try onZapComplete method
						try {
							ch.getClass().getMethod("onZapComplete").invoke(ch);
						} catch (Exception e) {
							// Fallback to Callback interface
							if (ch instanceof Callback) {
								((Callback)ch).call();
							}
						}
					}
				});
				Sample.INSTANCE.play(Assets.Sounds.ZAP);
			}
		} else if (anim == pumpAttack) {
			triggerEmitters();
			idle();
			if (ch instanceof Goo) {
				((Goo)ch).onAttackComplete();
			}
		} else if (anim == slam) {
			idle();
			if (ch instanceof DM300) {
				((DM300)ch).onSlamComplete();
			}
		}
		super.onComplete( anim );
	}

	// Store target position for zap attacks
	@Override
	public void zap( int pos ) {
		zapPos = pos;
		attack( pos );
	}

	// ==================== Ghoul/Drake Compatibility ====================

	// Compatibility method for Drake's hideDrake() call
	public void hideDrake() {
		// Do nothing, snake doesn't hide
	}

	// Compatibility method for GhoulSprite's crumple() call
	// This plays a "fake death" animation, then stays at last frame
	public void crumple() {
		hideEmo();
		remove(State.PARALYSED);
		play(crumple);
	}

	// Compatibility method for CrystalGuardian's endCrumple()
	public void endCrumple() {
		if (curAnim == crumple) {
			idle();
		}
	}

	// ==================== Ripper Compatibility ====================

	// Compatibility method for RipperSprite's leapPrep() call
	public void leapPrep( int cell ) {
		turnTo( ch.pos, cell );
		// Snake doesn't have prep animation, just stay idle
	}

	// Compatibility method for RipperSprite's jump() call
	@Override
	public void jump( int from, int to, float height, float duration, Callback callback ) {
		super.jump( from, to, height, duration, callback );
		play( leap );
	}

	// ==================== Necromancer Compatibility ====================

	public void cancelSummoning() {
		// Do nothing, snake doesn't summon
	}

	public void finishSummoning() {
		idle();
	}

	// ==================== Charge Animation (Generic) ====================

	// Generic charge() for Necromancer, Eye, Deminion, Deviloon etc.
	public void charge() {
		play(charge);
	}

	// Charge with position for Eye, Deminion, Deviloon
	public void charge( int pos ) {
		turnTo(ch.pos, pos);
		play(charge);
		if (visible) Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
	}

	// ==================== Statue Compatibility ====================

	// Compatibility method for StatueSprite's setArmor() call (ArmoredStatue)
	public void setArmor( int tier ) {
		// Do nothing, snake doesn't have armor tier variations
	}

	// ==================== Goo Compatibility ====================

	// Goo pumpUp: play charge animation and create green particles in range
	public void pumpUp( int warnDist ) {
		if (warnDist == 0) {
			clearEmitters();
		} else {
			play(charge);
			Sample.INSTANCE.play( Assets.Sounds.CHARGEUP, 1f, warnDist == 1 ? 0.8f : 1f );
			// Create green particles in warning range
			if (ch.fieldOfView == null || ch.fieldOfView.length != Dungeon.level.length()) {
				ch.fieldOfView = new boolean[Dungeon.level.length()];
				Dungeon.level.updateFieldOfView( ch, ch.fieldOfView );
			}
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (ch.fieldOfView != null && ch.fieldOfView[i]
						&& Dungeon.level.distance(i, ch.pos) <= warnDist
						&& new Ballistica( ch.pos, i, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID).collisionPos == i
						&& new Ballistica( i, ch.pos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID).collisionPos == ch.pos) {
					Emitter e = CellEmitter.get(i);
					e.pour(GreenGooParticle.FACTORY, 0.04f);
					pumpUpEmitters.add(e);
				}
			}
		}
	}

	public void clearEmitters() {
		for (Emitter e : pumpUpEmitters) {
			e.on = false;
		}
		pumpUpEmitters.clear();
	}

	public void triggerEmitters() {
		for (Emitter e : pumpUpEmitters) {
			e.burst(ElmoParticle.FACTORY, 10);
		}
		Sample.INSTANCE.play( Assets.Sounds.BURNING );
		pumpUpEmitters.clear();
	}

	public void pumpAttack() {
		play(pumpAttack);
	}

	public void spray( boolean on ) {
		if (spray != null) spray.on = on;
	}

	// ==================== DM300 Compatibility ====================

	public void updateChargeState( boolean enraged ) {
		if (superchargeSparks != null) superchargeSparks.on = enraged;
		// Snake doesn't have different sprites for enraged state
		play(idle);
	}

	public void slam( int cell ) {
		turnTo( ch.pos, cell );
		play(slam);
		Sample.INSTANCE.play( Assets.Sounds.ROCKS );
		PixelScene.shake( 3, 0.7f );
	}

	private void setupSuperchargeSparks( boolean on ) {
		if (superchargeSparks == null) {
			superchargeSparks = emitter();
			superchargeSparks.autoKill = false;
			superchargeSparks.pour(SparkParticle.STATIC, 0.05f);
		}
		superchargeSparks.on = on;
	}

	// ==================== Golem Compatibility ====================

	public void teleParticles( boolean value ) {
		if (teleParticles == null) {
			teleParticles = emitter();
			teleParticles.autoKill = false;
			teleParticles.pour(ElmoParticle.FACTORY, 0.05f);
		}
		teleParticles.on = value;
	}

	// ==================== GnollGeomancer/GnollGuard Compatibility ====================

	public void setupArmor() {
		if (earthArmor == null) {
			earthArmor = emitter();
			earthArmor.fillTarget = false;
			earthArmor.y = height()/2f;
			earthArmor.x = (2*scale.x);
			earthArmor.width = width()-(4*scale.x);
			earthArmor.height = height() - (10*scale.y);
			earthArmor.pour(EarthParticle.SMALL, 0.15f);
		}
	}

	public void loseArmor() {
		if (earthArmor != null) {
			earthArmor.on = false;
			earthArmor = null;
		}
	}

	public void updateAnims() {
		// Snake doesn't have different animations for statue state
		play(idle);
	}

	// ==================== Other Compatibility ====================

	// Artillerist targeting
	public void targeting( int pos ) {
		turnTo(ch.pos, pos);
		// No special targeting animation for snake
	}

	// RatKing resetAnims (festival appearance)
	public void resetAnims() {
		// Snake doesn't have festival variations
		play(idle);
	}

	// ==================== YogFist Compatibility ====================

	// Create elemental particles based on YogFist type
	private Emitter createFistEmitter() {
		if (ch == null) return null;

		Emitter emitter = emitter();
		emitter.autoKill = false;

		if (ch instanceof YogFist.BurningFist) {
			emitter.pour(FlameParticle.FACTORY, 0.06f);
		} else if (ch instanceof YogFist.SoiledFist) {
			emitter.pour(LeafParticle.GENERAL, 0.06f);
		} else if (ch instanceof YogFist.RottingFist) {
			emitter.pour(Speck.factory(Speck.TOXIC), 0.25f);
		} else if (ch instanceof YogFist.RustedFist) {
			emitter.pour(CorrosionParticle.MISSILE, 0.06f);
		} else if (ch instanceof YogFist.BrightFist) {
			emitter.pour(SparkParticle.STATIC, 0.06f);
		} else if (ch instanceof YogFist.DarkFist) {
			emitter.pour(ShadowParticle.MISSILE, 0.06f);
		} else {
			// Default: no particles
			return null;
		}

		return emitter;
	}

	// ==================== Green Goo Particle (for Goo pumpUp) ====================

	public static class GreenGooParticle extends PixelParticle.Shrinking {



		public static final Emitter.Factory FACTORY = new Emitter.Factory() {
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((GreenGooParticle)emitter.recycle( GreenGooParticle.class )).reset( x, y );
			}
		};

		public GreenGooParticle() {
			super();
			// Green color instead of black
			color( 0x00FF00 );
			lifespan = 0.3f;
			acc.set( 0, +50 );
		}

		public void reset( float x, float y ) {
			revive();
			this.x = x;
			this.y = y;
			left = lifespan;
			size = 4;
			speed.polar( -Random.Float( PointF.PI ), Random.Float( 32, 48 ) );
		}

		@Override
		public void update() {
			super.update();
			float p = left / lifespan;
			am = p > 0.5f ? (1 - p) * 2f : 1;
		}
	}
}