package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Radish extends Trinket {

    {
        image = ItemSpriteSheet.RADISH;
    }

    @Override
    protected int upgradeEnergyCost() {
        return 10+5*level();
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc", critChance(buffedLvl()));
        } else {
            return Messages.get(this, "typical_stats_desc", critChance(0));
        }
    }

    public int critChance( int level ){
        switch (level){
            default:
            case 0:
                return 5;
            case 1:
                return 10;
            case 2:
                return 15;
            case 3:
                return 20;
        }
    }

    @Override
    public boolean doPickUp(Hero hero, int pos) {
        Buff.affect(hero, GlobalCritChance.class);
        return super.doPickUp(hero, pos);
    }

    public void doThrow( Hero hero ) {
        super.doThrow(hero);
        Buff.detach(hero, GlobalCritChance.class);
    }

    public void doDrop( Hero hero ) {
       super.doThrow(hero);
        Buff.detach(hero, GlobalCritChance.class);
    }

    public static class GlobalCritChance extends Buff {
        {
            type = buffType.POSITIVE;
        }

        @Override
        public String icon() {
            return BuffIndicator.NONE;
        }
        public int critChance = 0;

        @Override
        public boolean act() {
            Radish radish = Dungeon.hero.belongings.getItem(Radish.class);
            if(radish != null){
                critChance = radish.critChance(radish.buffedLvl());
            }
            spend(TICK);
            return true;
        }
    }

}
