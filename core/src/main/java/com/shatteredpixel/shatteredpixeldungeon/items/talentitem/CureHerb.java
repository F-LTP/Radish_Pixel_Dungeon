package com.shatteredpixel.shatteredpixeldungeon.items.talentitem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class CureHerb extends Item {
    private static final String AC_EAT = "EAT";
    {
        image = ItemSpriteSheet.LIQUID_METAL;

        defaultAction = AC_EAT;

        stackable = true;
    }
    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions( hero );
        actions.add ( AC_EAT );
        return actions;
    }
    @Override
    public void execute(Hero hero, String action) {

        if (action.equals(AC_EAT)) {
            hero.sprite.operate( hero.pos );
            hero.busy();
            hero.spend( Actor.TICK );
            detach(hero.belongings.backpack);
            hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
            hero.HP+=Math.min(20,hero.HT-hero.HP);
        }

        super.execute(hero, action);
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }
}
