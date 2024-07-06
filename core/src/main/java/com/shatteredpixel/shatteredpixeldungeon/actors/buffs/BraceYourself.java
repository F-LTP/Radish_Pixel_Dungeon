package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;


/*
    class BraceYourself
        detach if mv or lose preparation buff
    by: DoggingDog
 */
public class BraceYourself extends Buff{

    {
        type = buffType.POSITIVE;
    }

    public int pos = -1;

    @Override
    public boolean act() {
        if (pos == -1) pos = target.pos;
        if (pos != target.pos || Dungeon.hero.buff(Preparation.class)==null) {
            detach();
        } else {
            spend(TICK);
        }
        return true;
    }

    @Override
    public int icon() {
        return BuffIndicator.WEAPON;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(1.9f, 2.4f, 3.25f);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }

}
