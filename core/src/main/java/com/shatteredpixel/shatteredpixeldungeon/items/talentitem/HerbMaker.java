package com.shatteredpixel.shatteredpixeldungeon.items.talentitem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class HerbMaker extends Item {
    private static final String AC_MAKE = "MAKE";
    private static final String AC_NONE = "NONE";
    int current_seed = 0;
    {
        image = ItemSpriteSheet.HERB_MAKER;

        defaultAction = AC_MAKE;

        stackable = false;
    }
    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions( hero );
        if (hero.hasTalent(Talent.HERB_MIXTURE)) {
            actions.add(AC_MAKE);
            defaultAction = AC_MAKE;
        }
        else
            defaultAction=AC_NONE;
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {

        if (action.equals(AC_MAKE)) {
            GameScene.selectItem(itemSelector);
        }

        super.execute(hero, action);
    }
    private static final String CUR_SEED = "CUR_SEED";
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put( CUR_SEED, current_seed );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (bundle.contains( CUR_SEED ))
            current_seed=bundle.getInt(CUR_SEED);
        else
            current_seed=0;
    }
    @Override
    public String special(){
        return String.valueOf(current_seed);
    }
    protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(HerbMaker.class, "prompt");
        }

        @Override
        public Class<?extends Bag> preferredBag(){
            return VelvetPouch.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return item instanceof Plant.Seed;
        }

        @Override
        public void onSelect( Item item ) {
            if (item != null && item instanceof Plant.Seed && Dungeon.hero.hasTalent(Talent.HERB_MIXTURE)) {
                Hero hero = Dungeon.hero;
                hero.sprite.operate( hero.pos );
                hero.busy();
                hero.spend( Actor.TICK );
                item.detach(hero.belongings.backpack);
                current_seed++;
                if (current_seed>=5-hero.pointsInTalent(Talent.HERB_MIXTURE)){
                    CureHerb ch = new CureHerb();
                    if ( ch.doPickUp(hero)) {
                        hero.spend(-TIME_TO_PICK_UP);
                    } else {
                        Dungeon.level.drop(ch, hero.pos).sprite.drop();
                    }
                    current_seed-=5-hero.pointsInTalent(Talent.HERB_MIXTURE);
                }
            }
        }
    };

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }
    @Override
    public int value(){
        return 50;
    }
}
