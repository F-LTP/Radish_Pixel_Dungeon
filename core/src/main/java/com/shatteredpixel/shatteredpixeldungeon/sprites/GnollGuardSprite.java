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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss.GnollKing;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.ParalyticDart;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

public class GnollGuardSprite extends MobSprite {

	private Emitter earthArmor;

	public GnollGuardSprite() {
		super();

		texture(Assets.Sprites.GNOLL_GUARD );

		TextureFilm frames = new TextureFilm( texture, 12, 16 );

		idle = new Animation( 2, true );
		idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );

		run = new Animation( 12, true );
		run.frames( frames, 4, 5, 6, 7 );

		attack = new Animation( 12, false );
		attack.frames( frames, 2, 3, 0 );

		die = new Animation( 12, false );
		die.frames( frames, 8, 9, 10 );

		play( idle );
	}

	@Override
	public void link( Char ch ) {
		super.link( ch );

		if (ch instanceof GnollGuard && ((GnollGuard) ch).hasSapper()){
			setupArmor();
		}
	}

	public void setupArmor(){
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

	public void loseArmor(){
		if (earthArmor != null){
			earthArmor.on = false;
			earthArmor = null;
		}
	}

	@Override
	public void update() {
		super.update();

		if (earthArmor != null){
			earthArmor.visible = visible;
		}
	}

	@Override
	public void die() {
		super.die();
		if (earthArmor != null){
			earthArmor.on = false;
			earthArmor = null;
		}
	}

	@Override
	public void kill() {
		super.kill();
		if (earthArmor != null){
			earthArmor.on = false;
			earthArmor = null;
		}
	}

	public static class GnollGuardShadowSprite extends MobSprite {
		private Emitter earthArmor;
		private Animation cast;

		public GnollGuardShadowSprite() {
			super();

			texture(Assets.Sprites.GNOLL_GUARD );

			TextureFilm frames = new TextureFilm( texture, 12, 16 );

			idle = new Animation( 2, true );
			idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );

			run = new Animation( 12, true );
			run.frames( frames, 4, 5, 6, 7 );

			attack = new Animation( 12, false );
			attack.frames( frames, 2, 3, 0 );

			die = new Animation( 12, false );
			die.frames( frames, 8, 9, 10 );

			cast = attack.clone();

			play( idle );
		}

		@Override
		public void attack( int cell ) {
			if (!Dungeon.level.adjacent(cell, ch.pos) && ch instanceof GnollKing.GnollGuardShadow)  {
				if(((GnollKing.GnollGuardShadow) ch).reachAttack){
					((MissileSprite)parent.recycle( MissileSprite.class )).
							reset( this, cell, new ParalyticDart(), new Callback() {
								@Override
								public void call() {
									ch.onAttackComplete();
								}
							});
					play( cast );
					turnTo( ch.pos , cell );
					((GnollKing.GnollGuardShadow) ch).reachAttack = false;
				} else {
					super.attack( cell );
				}
			} else {
				super.attack( cell );
			}
		}

		@Override
		public void link( Char ch ) {
			super.link( ch );

			if (ch instanceof GnollKing.GnollGuardShadow && ((GnollKing.GnollGuardShadow) ch).hasSapper()){
				setupArmor();
			}
		}

		public void setupArmor(){
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

		public void loseArmor(){
			if (earthArmor != null){
				earthArmor.on = false;
				earthArmor = null;
			}
		}

		@Override
		public void update() {
			super.update();

			if (earthArmor != null){
				earthArmor.visible = visible;
			}
		}

		@Override
		public void die() {
			super.die();
			if (earthArmor != null){
				earthArmor.on = false;
				earthArmor = null;
			}
		}

		@Override
		public void kill() {
			super.kill();
			if (earthArmor != null){
				earthArmor.on = false;
				earthArmor = null;
			}
		}


	}


}
