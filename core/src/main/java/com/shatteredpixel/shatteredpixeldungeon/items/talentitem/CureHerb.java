package com.shatteredpixel.shatteredpixeldungeon.items.talentitem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.AfterGlow;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class CureHerb extends Item {
    private static final String AC_EAT = "EAT";
    {
        image = ItemSpriteSheet.HERB;

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
            hero.HP+=Math.min(15,hero.HT-hero.HP);
            if (hero.buff(AfterGlow.Warmth.class)!=null){
                hero.buff(AfterGlow.Warmth.class).getWarmth();
            }
            Buff.affect(hero, Healing.class).setHeal(10, 0, 1);
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
