package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class PneumFistGloves extends MeleeWeapon {

    public boolean active = false;

    private static final String AC_ACTIVE	= "active";
    private static final String AC_ON_ACTIVE	= "on_active";

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions(hero);
        if (!active){
            actions.add(AC_ACTIVE);
        } else {
            actions.add(AC_ON_ACTIVE);
        }
        return actions;
    }

    public int image() {
        if (active){
            super.image = ItemSpriteSheet.PNEGLOVE_ACTIVE;
        } else {
            super.image = ItemSpriteSheet.PNEGLOVE_FIVE;
        }
        return image;
    }

    @Override
    public void execute(final Hero hero, String action) {
        super.execute(hero, action);

        //①
        if (action.equals( AC_ACTIVE )) {
            if(!active){
                active = true;
                GLog.w(Messages.get(this,"active"));
            }
        }

        //②
        if (action.equals( AC_ON_ACTIVE )) {
            if(active){
                active = false;
                GLog.w(Messages.get(this,"on_active"));
            }

        }
    }

    {
        image = ItemSpriteSheet.PNEGLOVE_FIVE;
        tier = 3;
        DLY = 0.8f;
    }

    @Override
    public int min(int lvl) {
        return 4 + lvl;
    }
    @Override
    public int max(int lvl) {
        return 15 + lvl * 3;
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        active = bundle.getBoolean("active_boolean");
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("active_boolean", active);
    }

}
