package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class PWSprite extends MobSprite{
    public PWSprite()
    {
        super();
        texture(Assets.Sprites.NEW_NPC);

        TextureFilm frames = new TextureFilm(texture, 16, 16);

        idle = new Animation(6, true);
        idle.frames(frames, 96,97,98,99,100,101,102,103,104,104,105,105,105,106,106,106);

        run = new Animation(20, true);
        run.frames(frames, 96);

        die = new Animation(20, false);
        die.frames(frames, 96, 108, 107);
        play(idle);
    }
}
