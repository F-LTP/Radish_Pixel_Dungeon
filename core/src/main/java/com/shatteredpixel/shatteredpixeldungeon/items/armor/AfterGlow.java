package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

public class AfterGlow extends Armor{
    {
        image = ItemSpriteSheet.ARMOR_AFTERGLOW;
    }

    public AfterGlow() {
        super( 5 );
    }
    @Override
    protected ArmorBuff buff( ) {
        return new AfterGlow.Warmth();
    }
    @Override

    public int DRMax(int lvl){
        return 15 + 3 * lvl + augment.defenseFactor(lvl);
    }
    @Override
    public int DRMin(int lvl){
        return 2 * lvl;
    }
    public class Warmth extends ArmorBuff {
        public float storeHealth=0f;
        @Override
        public boolean act(){
            if (storeHealth>=1){
                int amount=(int)storeHealth;
                storeHealth-=amount;
                if (target instanceof Hero){
                    //英雄走统一治疗入口，「英雄回血」的各种联动才看得到这次回复；满血时也不会再飘一个 +0。
                    //它对英雄不会反过来触发余晖，不会循环
                    target.heal(amount);
                } else {
                    //其它穿戴者不能走 heal()：它会再给余晖攒一次回复，每回合都回个没完
                    int preHp=target.HP;
                    target.HP=Math.min(target.HT, target.HP+amount);
                    if (target.HP>preHp && target.sprite!=null) {
                        target.sprite.showStatus(CharSprite.POSITIVE, "+%dHP", target.HP-preHp);
                    }
                }
            }
            return super.act();
        }
        public void getWarmth(){
            storeHealth+=2+0.2f*buffedLvl();
        }
        private static final String STOREHEALTH       = "store_health";

        @Override
        public void storeInBundle( Bundle bundle ) {
            bundle.put(STOREHEALTH,storeHealth);
            super.storeInBundle(bundle);
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle(bundle);
            if (bundle.contains(STOREHEALTH))
                storeHealth = bundle.getFloat(STOREHEALTH);
            else
                storeHealth =0;
        }

    }
}
