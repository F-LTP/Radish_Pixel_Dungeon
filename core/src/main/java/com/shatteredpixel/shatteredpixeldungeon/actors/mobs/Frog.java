package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FrogSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class Frog extends Mob {

    public boolean attackwater = false;

    {
        spriteClass = FrogSprite.class;

        HP = HT = 12;
        defenseSkill = 3;

        maxLvl = 5;
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        damage = super.attackProc( enemy, damage );

        if(!attackwater){
          PoulWater poulWater = new PoulWater();
          if(enemy instanceof Hero){
              poulWater.collect();
              GLog.w(Messages.get(this,"water"));
          }
          attackwater = true;
        }

        return damage;
    }

    @Override
    public int damageRoll() {
        return Char.combatRoll( 1, 5 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 9;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Char.combatRoll(0, 2);
    }

    private static final String ATTACK = "attack";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ATTACK,attackwater);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        attackwater = bundle.getBoolean(ATTACK);
    }

    public class PoulWater extends Item {

        public int cooldown = 50;

        {
            stackable = false;
            image = ItemSpriteSheet.POUL_WATER;
        }

        @Override
        public String desc() {
            return Messages.get(this,"desc",cooldown);
        }

        @Override
        public ArrayList<String> actions(Hero hero) {
            return new ArrayList<>();
        }

        private static final String COOLDOWN = "cooldown";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(COOLDOWN,cooldown);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            cooldown = bundle.getInt(COOLDOWN);
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


}

