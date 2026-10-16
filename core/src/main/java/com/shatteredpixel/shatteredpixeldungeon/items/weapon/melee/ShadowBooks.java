package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

public class ShadowBooks extends MeleeWeapon {

    public int chance;

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
            type = buffType.POSITIVE;
        }

        @Override
        public boolean act() {
            ShadowBooks w2 = (ShadowBooks) hero.belongings.weapon;
            if (w2 != null) {
            			w2.chance = (int) (1 + 0.2*w2.buffedLvl());
            		}
            spend(TICK);
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
        chance = bundle.getInt("chance");
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("chance", chance);
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
            desc = Messages.get(this, "desc", chance);
        } else {
            desc = Messages.get(this, "normal_desc",1);
        }

        return desc;
    }

}
