package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ConfusionGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.StenchGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.utils.LegacyItemArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

// DoggingDog on 20250522
// 薄暮
public class Sunless extends LegacyItemArmor {
    {
        image = ItemSpriteSheet.SUNLESS;
    }

    public Sunless() {
        super(1);
    }

    @Override
    public int DRMin(int lvl){
        return 2 + lvl;
    }

    @Override
    public int DRMax(int lvl){
        return 6 + Math.max(3 * lvl + augment.defenseFactor(3 * lvl), 3 * lvl);
    }

    @Override
    protected ArmorBuff buff( ) {
        if(Dungeon.hero != null)
            return new Sirris().set(buffedLvl());
        return new Sirris();
    }

    public class Sirris extends ArmorBuff {
        {
            type = buffType.POSITIVE;
            announced = true;
        }

        private int lvl = 0;
        private boolean inGas = false;

        @Override
        public String icon() {
            return BuffIndicator.BLESS;
        }

        @Override
        public boolean act() {
            for (Blob blob : Dungeon.level.blobs.values()) {
                if(blob instanceof ToxicGas || blob instanceof ConfusionGas || blob instanceof CorrosiveGas || blob instanceof ParalyticGas || blob instanceof StenchGas){
                    if (blob.volume > 0 && blob.cur[target.pos] > 0 && blob.tileDesc() != null) {
                        inGas = true;
                        spend( TICK );
                        return true;
                    }
                }
            }
            spend( TICK );
            inGas = false;
            return true;
        }

        public boolean isInGas(){
            return inGas;
        }

        public Sirris set(int level){
            this.lvl = level;
            return this;
        }

        public int absorbDamage(int dmg){
            if(inGas)
                return Math.max(0,(int) (dmg * (0.4f - lvl * 0.08f)));
            return dmg;
        }

        private static final String LVL	= "level";

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( LVL, lvl);
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            lvl = bundle.getInt( LVL );
        }
        @Override
        public String desc() {
            if(inGas)
                return Messages.get(this, "desc", (60+lvl*8)+"");
            return Messages.get(this, "desc", 0+"");
        }

    }
}
