package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

public class EnergyArmor extends Armor{

    public int energyLeft = 450;

    int chargeCount = 0;
    {
        image = ItemSpriteSheet.ARMOR_ENERGY1;
    }

    public EnergyArmor() {
        super( 3 );
    }

    /** 穿在身上时用英雄贴图表的第几行：有能量是 14，能量耗尽是 15。只取值，不改任何状态。 */
    public int Energy() {
        return energyLeft <= 0 ? 15 : 14;
    }

    @Override
    public void level( int value ){
        int oldLvl = level();
        super.level(value);
        energyLeft += 200 *(level()-oldLvl);
    }
    @Override
    public Item upgrade() {
        energyLeft +=200;
        return super.upgrade();
    }
    @Override
    public Item degrade() {
        energyLeft -=200;
        return super.degrade();
    }
    @Override
    protected ArmorBuff buff( ) {
        return new EnergyArmor.chargeShield();
    }
    @Override
    public int DRMax(int lvl){
        int ad=(1+ augment.defenseFactor(lvl))/2;
        return 3 + 2 * lvl + ad;
    }
    @Override
    public int DRMin(int lvl){
        int ad=(augment.defenseFactor(lvl)-1)/2;
        return 3 + 2 * lvl +ad;
    }


    @Override
    public String image(){
        if (buff != null && buff.target != null && buff.target.buff(myShield.class) != null){
            image = ItemSpriteSheet.ARMOR_ENERGY2;
        } else {
            image = ItemSpriteSheet.ARMOR_ENERGY1;
        }
        //这里只挑图标，不能要求界面刷新：界面每次重画物品都会来取图标，
        //在这里要求刷新的话，只要背包面板开着，就会每一帧都把所有物品格子重建一遍。
        //图标该换的时机（护盾出现、护盾消失）由下面的状态自己通知
        return super.image();
    }
    private static final String ENERGY_LEFT       = "energy_left";
    private static final String CHARGE_COUNT       = "charge_count";

    @Override
    public void storeInBundle( Bundle bundle ) {
        bundle.put(ENERGY_LEFT,energyLeft);
        bundle.put(CHARGE_COUNT,chargeCount);
        super.storeInBundle(bundle);
        image();
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(ENERGY_LEFT))
            energyLeft = bundle.getInt(ENERGY_LEFT);
        else
            energyLeft =450;
        if (bundle.contains(CHARGE_COUNT))
            chargeCount = bundle.getInt(CHARGE_COUNT);
        else
            chargeCount =0;
        image();
    }
    public class chargeShield extends ArmorBuff {
        @Override
        public boolean act() {
            spend( TICK);
            boolean strengthMet = !(target instanceof Hero)
                    || ((Hero) target).STR() >= EnergyArmor.this.STRReq();
            if (strengthMet && EnergyArmor.this.energyLeft > 0){
                chargeCount++;
                EnergyArmor.this.energyLeft--;
            }
            if (chargeCount>=30){
                Buff.affect(target, myShield.class).incShield(12+buffedLvl());
                chargeCount-=30;
                updateQuickslot();
            }
            return true;
        }
        @Override
        public void detach(){
            Buff.detach(target, myShield.class);
            super.detach();
        }

    }
    public static class myShield extends Barrier{
        @Override
        public void detach() {
            super.detach();
            //护盾没了，护甲图标要换回没有护盾的样子
            Item.updateQuickslot();
        }
    }
}
