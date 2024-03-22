package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.NONE;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestItem;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndImp;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

import java.util.ArrayList;

public class TestTalentOFTerminalBook extends TestItem {

    private static final String AC_READ	= "READ";
    {
        image = ItemSpriteSheet.TAL_MASTERY;
        unique= true;
        defaultAction = AC_READ;
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions(hero);
        if (hero.isAlive() && hero.subClass != NONE) actions.add(AC_READ);
        return actions;
    }

    @Override
    public void execute(final Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals( AC_READ )) {
            if ( !hero.powerOfImp & hero.subClass != NONE ) {
                hero.powerOfImp = true;
                Buff.affect(hero, WndImp.powerGainTracker.class);
                hero.spend(Actor.TICK);
                hero.busy();
                Talent.initT4Talents(hero);

                hero.sprite.operate(hero.pos);
                Sample.INSTANCE.playDelayed(Assets.Sounds.LEVELUP, 0.3f, 0.7f, 1.2f);
                Sample.INSTANCE.playDelayed(Assets.Sounds.LEVELUP, 0.6f, 0.7f, 1.2f);

                Emitter e = hero.sprite.centerEmitter();
                e.pos(e.x - 2, e.y - 6, 4, 4);
                e.start(Speck.factory(Speck.MASK), 0.05f, 20);
                GLog.p(Messages.get(this,"power_mode_on",hero.name()));
                hero.sprite.operate( hero.pos );
                detach( hero.belongings.backpack );
            } else {
                GLog.w(Messages.get(this,"power_mode_bad"));
            }

        }
    }
}
