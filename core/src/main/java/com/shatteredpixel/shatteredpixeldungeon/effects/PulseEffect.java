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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;

public class PulseEffect extends Group {

	private static final float duration = 0.75f;

	private static final double A = 180 / Math.PI;

	private float spent = 0f;

	private final Image[] pulses;

	private final PointF from;
	private final PointF to;

	/*public PulseEffect(int from, int to){
		this(DungeonTilemap.tileCenterToWorld(from),
				DungeonTilemap.tileCenterToWorld(to));
	}*/

	public PulseEffect(PointF from, PointF to){
		super();

		this.from = from;
		this.to = to;

		float dx = to.x - from.x;
		float dy = to.y - from.y;

		int numPulses = Math.round( (float)(1 + Math.hypot(dx, dy) ) / 8f);

		pulses = new Image[numPulses];
		for (int i = 0; i < pulses.length; i++){
			pulses[i] = new Image(Effects.get(Effects.Type.LIGHTNING));
			pulses[i].angle = (float) (Math.atan2(dy, dx) * A) + 90f;
			pulses[i].origin.set( pulses[i].width()/ 2, pulses[i].height() );
			add(pulses[i]);
		}
	}

	@Override
	public void update() {
		if ((spent += Game.elapsed) > duration) {

			killAndErase();

		} else {
			float dx = to.x - from.x;
			float dy = to.y - from.y;
			for (int i = 0; i < pulses.length; i++) {
				float timeUsed = 1 - (spent / duration);

				pulses[i].scale.set( timeUsed, pulses[i].scale.y );

				pulses[i].center(new PointF(
						from.x + dx * (i / (float) pulses.length) + pulses[i].scale.x * pulses[i].width / 2f - pulses[i].width / 2f,
						from.y + dy * (i / (float) pulses.length) - pulses[i].height() / 2f
				));

			}
		}
	}
}