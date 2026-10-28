package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.utils.LegacyItemArmor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

// DoggingDog on 20250520
// 宵暗
public class Showdarker extends LegacyItemArmor {
    {
        image = ItemSpriteSheet.SUNLESS;
    }

    public Showdarker() {
        super(1);
    }

    @Override
    public int DRMax(int lvl){
        return 6 + Math.max(3 * lvl + augment.defenseFactor(3 * lvl), 3 * lvl);
    }

    @Override
    public int DRMin(int lvl){
        return 3 + 2 * lvl;
    }

    @Override
    protected ArmorBuff buff( ) {
        return new Rumia();
    }

    @Override
    public float speedFactor(Char owner, float speed ){
        for(Mob mob: Dungeon.level.mobs){
            if(Dungeon.hero != null){
                if(Dungeon.hero.belongings.weapon.canReach(owner,mob.pos)){
                    return super.speedFactor(owner,speed) * (0.5f + buffedLvl() * 0.05f);
                }
            }
        }
        return super.speedFactor(owner,speed);
    }

    public class Rumia extends ArmorBuff {
        {
            type = buffType.POSITIVE;
            announced = true;
        }

        @Override
        public String icon() {
            return BuffIndicator.BLINDNESS;
        }

    }
}
