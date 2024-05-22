package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class ShadowBooks extends MeleeWeapon {

    public float chance;

    //概率减半持续递归
    public float AloneChance = 1;


    public boolean aloneToChance(){
        return Random.NormalFloat(0,1)<=chance;
    }

    public boolean aloneDoubleChance(){
        return Random.NormalFloat(0,1)<=chance/2;
    }

    {
        image = ItemSpriteSheet.SHADOW_BOOK;
        tier = 4;
    }

    @Override
    public boolean doEquip( Hero hero ) {
        Buff.affect(hero, ShadowProject.class);
        return super.doEquip(hero);
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        Buff.detach(hero, ShadowProject.class);
        return super.doUnequip(hero, collect, single);
    }

    public static class ShadowProject extends Buff {

        {
            type=buffType.POSITIVE;
        }

        @Override
        public boolean act() {
            ShadowBooks w2 = (ShadowBooks) hero.belongings.weapon;
            if(w2 != null){
                w2.chance = (0.2f + 0.04f * w2.level())/w2.AloneChance;
            }
            spend( TICK );
            return true;
        }
    }


    @Override
    public int STRReq(int lvl) {
        return (7 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        chance = bundle.getFloat("chance");
        AloneChance = bundle.getFloat("lowchance");
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("chance", chance);
        bundle.put("lowchance",AloneChance);
    }

    @Override
    public int min(int lvl) {
        return 2 + lvl * 2;
    }
    @Override
    public int max(int lvl) {
        return 20 + lvl * 2;
    }

    @Override
    public String desc() {

        String desc;

        if(isIdentified()){
            desc = Messages.get(this, "desc", Math.round(Math.min(chance*100f,100)));
        } else {
            desc = Messages.get(this, "normal_desc",20);
        }

        return desc;
    }

}
