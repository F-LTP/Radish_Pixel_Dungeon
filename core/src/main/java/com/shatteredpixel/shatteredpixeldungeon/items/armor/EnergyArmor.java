package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class EnergyArmor extends Armor{

    public int energyLeft = 450;

    int chargeCount = 0;
    {
        image = ItemSpriteSheet.ARMOR_ENERGY1;
    }

    public EnergyArmor() {
        super( 3 );
    }

    public boolean update = false;

    public int Energy() {
        int energy;

        if (energyLeft<=0){
            energy = 15;
            update = true;
        } else {
            energy = 14;
        }

        return energy;
    }

    @Override
    public void level( int value ){
        int oldLvl = level();
        super.level(value);
        energyLeft += 200 *(level()-oldLvl);
        updateImage();
    }
    @Override
    public Item upgrade() {
        energyLeft +=200;
        updateImage();
        return super.upgrade();
    }
    @Override
    public Item degrade() {
        energyLeft -=200;
        updateImage();
        return super.degrade();
    }
    @Override
    protected ArmorBuff buff( ) {
        return new EnergyArmor.chargeShield();
    }
    @Override
    public int DRMax(int lvl){
        int ad=(1+ augment.defenseFactor(lvl))/2;
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return Math.max(2 + lvl + augment.defenseFactor(lvl),1+lvl/2);
        }
        return 3 + 2 * lvl + ad;
    }
    @Override
    public int DRMin(int lvl){
        int ad=(augment.defenseFactor(lvl)-1)/2;
        if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
            return 1 + lvl / 2 ;
        }

        return 3 + 2 * lvl +ad;
    }
    private void updateImage(){
        if (energyLeft<=0)
            image = ItemSpriteSheet.ARMOR_ENERGY2;
        else
            image = ItemSpriteSheet.ARMOR_ENERGY1;
        updateQuickslot();
    }
    private static final String ENERGY_LEFT       = "energy_left";
    private static final String CHARGE_COUNT       = "charge_count";

    @Override
    public void storeInBundle( Bundle bundle ) {
        bundle.put(ENERGY_LEFT,energyLeft);
        bundle.put(CHARGE_COUNT,chargeCount);
        super.storeInBundle(bundle);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(ENERGY_LEFT))
            energyLeft = bundle.getInt(ENERGY_LEFT);
        else
            energyLeft =450;
        updateImage();
        if (bundle.contains(CHARGE_COUNT))
            chargeCount = bundle.getInt(CHARGE_COUNT);
        else
            chargeCount =0;
    }
    public class chargeShield extends ArmorBuff {
        @Override
        public boolean act() {
            spend( TICK);
            if (((Hero)target).STR()>=EnergyArmor.this.STRReq() && EnergyArmor.this.energyLeft>0){
                chargeCount++;
                EnergyArmor.this.energyLeft--;
                if (EnergyArmor.this.energyLeft<=0)
                    EnergyArmor.this.updateImage();
            }
            if (chargeCount>=30){
                Buff.affect(target, myShield.class).incShield(12+buffedLvl());
                chargeCount-=30;
            }
            //GLog.h(String.valueOf(update));
            return true;
        }
        @Override
        public void detach(){
            Buff.detach(target, myShield.class);
            super.detach();
        }

    }
    public static class myShield extends Barrier{
    }
}
