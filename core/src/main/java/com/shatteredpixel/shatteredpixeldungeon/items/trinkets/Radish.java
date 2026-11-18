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
        // 这里原先调的是 super.doThrow：点「放下」弹出来的是投掷选格，东西并没有放到脚下
        super.doDrop(hero);
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
            if(radish == null){
                // 萝卜不经过放下 / 投掷也会离开背包（被嬗变成别的饰物、在炼金釜里拆成能量……），
                // 这时没有人来摘这个状态，暴击率加成会一直留到下次读档
                detach();
                return true;
            }
            critChance = radish.critChance(radish.buffedLvl());
            spend(TICK);
            return true;
        }
    }

}
