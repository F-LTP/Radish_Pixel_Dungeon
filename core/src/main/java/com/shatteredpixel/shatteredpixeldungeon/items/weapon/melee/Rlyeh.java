package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Rlyeh extends MeleeWeapon {

    public float chance;

    public boolean chance(){
        return Random.NormalFloat(0,1)<=chance;
    }

    public boolean HeroChance(){
        return Random.NormalFloat(0,1)<=chance/2f;
    }

    {
        image = ItemSpriteSheet.RLYEH_BOOK;
        tier = 2;
    }

    @Override
    public boolean doEquip( Hero hero ) {
        Buff.affect(hero,StateProject.class);
        return super.doEquip(hero);
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        Buff.detach(hero,StateProject.class);
        return super.doUnequip(hero, collect, single);
    }


    @Override
    public int STRReq(int lvl) {
        return (9 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    public int min(int lvl) {
        return 2 + lvl * 2;
    }
    @Override
    public int max(int lvl) {
        return 12 + lvl * 2;
    }

    @Override
    public String desc() {

        String desc;

        if(isIdentified()){
            desc = Messages.get(this, "desc",Math.min(15+3*level(),100));
        } else {
            desc = Messages.get(this, "normal_desc",15);
        }

        return desc;
    }


    public static class StateProject extends Buff {

        {
            type=buffType.POSITIVE;
        }

        @Override
        public boolean act() {
            Rlyeh w2 = null;
            if(hero.belongings.weapon instanceof Rlyeh)
                w2 = (Rlyeh) hero.belongings.weapon;
            if(w2 != null){
                w2.chance = 0.15f + 0.03f * w2.level();
            }
            spend( TICK );
            return true;
        }
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        chance = bundle.getFloat("chance");
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("chance", chance);
    }

}

