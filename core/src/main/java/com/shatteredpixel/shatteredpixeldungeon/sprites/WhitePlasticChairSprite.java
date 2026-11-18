package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class WhitePlasticChairSprite extends MobSprite {

	public WhitePlasticChairSprite() {
		texture(Assets.Sprites.WHITE_PLASTIC_CHAIR);

		TextureFilm frames = new TextureFilm(texture, 16, 16);
		idle = new Animation(1, true);
		idle.frames(frames, 0);
		run = idle.clone();
		attack = idle.clone();
		die = new Animation(1, false);
		die.frames(frames, 0);
		play(idle);
	}

	@Override
	public void turnTo(int from, int to) {
		// Furniture does not turn.
	}
}
