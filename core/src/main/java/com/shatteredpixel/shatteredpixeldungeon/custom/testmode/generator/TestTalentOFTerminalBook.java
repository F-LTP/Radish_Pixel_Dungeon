package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;

import static com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.NONE;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestItem;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndImp;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndImpTalent;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoSubclass;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

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
            if (!hero.powerOfImp & hero.subClass != NONE ) {
                Game.runOnRenderThread(new Callback() {
                    @Override
                    public void call() {
                        Game.scene().addToFront(new WndImpTalent(TestTalentOFTerminalBook.this));
                    }
                });
            } else {
                GLog.w(Messages.get(this,"power_mode_bad"));
            }

        }
    }
}
