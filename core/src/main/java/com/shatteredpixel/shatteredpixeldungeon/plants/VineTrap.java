package com.shatteredpixel.shatteredpixeldungeon.plants;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class VineTrap extends Plant{
    {
        image = -1;
        seedClass=null;
    }
    @Override
    public void activate(Char ch) {
        if (ch != null){
            if (Dungeon.hero.hasTalent(Talent.VINE_TRAP)) {
                Buff.affect(ch, Weakness.class, 5f);
                if (Dungeon.hero.pointsInTalent(Talent.VINE_TRAP) > 1) {
                    Buff.affect(ch, Hex.class, 5f);
                    if (Dungeon.hero.pointsInTalent(Talent.VINE_TRAP) > 2) {
                        Buff.affect(ch, Vulnerable.class, 5f);
                    }
                }
            }
            //GLog.p("trap activated!");
        }
    }

    public static class PlantBuff extends FlavourBuff{
        int pos=-1;
        public void set(int t){
            pos=t;
        }
        @Override
        public void detach(){
            if (pos!=-1)
                Dungeon.plantVineTrap(pos);
            super.detach();
        }
        @Override
        public void storeInBundle(Bundle bundle){
            super.storeInBundle(bundle);
            bundle.put("plant_buff_pos",pos);
        }
        @Override
        public void restoreFromBundle(Bundle bundle){
            super.storeInBundle(bundle);
            if (bundle.contains("plant_buff_pos")) pos=bundle.getInt("plant_buff_pos");
        }
    }
}
