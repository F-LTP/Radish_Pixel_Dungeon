package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.watabou.utils.Bundle;

public class VitaeBuff extends Buff{
    private int vitae;
    public int maxVitae;

    public int getVitae(){
        return vitae;
    }

    public void setVitae(int vt){
        this.vitae = vt;
        this.maxVitae = vt;

        // DoggingDog on 20250818
        if(Dungeon.hero.hasTalent(Talent.VITAE_BOOST)){
            switch (Dungeon.hero.pointsInTalent(Talent.VITAE_BOOST)){
                case 1:this.vitae+=1;break;
                case 2:this.vitae+=2;break;
                default:break;
            }
        }
        //
    }

    public int absorbDamage( int dmg ){
        if (vitae >= dmg){
            vitae -= dmg;
            dmg = 0;
        } else {
            vitae = 0;
        }
        if (vitae == 0){
            detach();
        }
        return dmg;
    }

    // DoggingDog on 20250818
    {
        if(Dungeon.hero.pointsInTalent(Talent.VITAE_BOOST)>=3){
            immunities.add(Weakness.class);
            immunities.add(Vulnerable.class);
            immunities.add(Hex.class);
            immunities.add(Degrade.class);
        }
    }

    private static final String VITAE = "vitae";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( VITAE, vitae);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        vitae = bundle.getInt( VITAE );
    }
}
