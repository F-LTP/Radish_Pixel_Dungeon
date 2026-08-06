package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class CircleSword extends MeleeWeapon {

    {
        image = ItemSpriteSheet.CIRCLE_SWORD;
        tier = 4;
    }


    @Override
    public int proc(Char attacker, Char defender, int damage) {
        new Bomb().explodeMobs(defender.pos, (int) (damage/4f+damage*(0.05f*level())));
        return super.proc(attacker, defender, damage);
    }

    @Override
    public String statsInfo(){
        if(Dungeon.hero != null){
            if (isEquipped(Dungeon.hero) && Dungeon.hero.belongings.armor() != null){
                return Messages.get(this, "stats_desc2",Dungeon.hero.belongings.armor().DRMin(),Dungeon.hero.belongings.armor().DRMax());
            }
        }
        return Messages.get(this, "stats_desc");
    }


    @Override
    public int min(int lvl) {
        if(Dungeon.hero != null && Dungeon.hero.belongings.armor() != null && isEquipped(Dungeon.hero)){
            return 8 + Dungeon.hero.belongings.armor().DRMin() + lvl;
        } else {
            return 8  + lvl;
        }
    }

    @Override
    public int max(int lvl) {
        if(Dungeon.hero != null && Dungeon.hero.belongings.armor() != null && isEquipped(Dungeon.hero)){
            return 20 + Dungeon.hero.belongings.armor().DRMax() + lvl * 5;
        } else {
            return 20 + lvl * 5;
        }
    }

}
