package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.utils.LegacyItemArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

// 玄冥
// DoggingDog on 20250517
public class Turtleir extends LegacyItemArmor {
    {
        image = ItemSpriteSheet.TURTLEIR;
    }

    public Turtleir() {
        super(1);
    }

    @Override
    public int DRMax(int lvl){
        return 10 + Math.max(2 * lvl + augment.defenseFactor(5 * lvl), 5 * lvl);
    }

    @Override
    public int DRMin(int lvl){
        return 6 + 2 * lvl;
    }

    @Override
    protected ArmorBuff buff( ) {
        return new Turtleir.Mass_Energy();
    }

    @Override
    public int proc(Char attacker, Char defender, int damage ) {
        int dmg = Math.max(0,damage-Dungeon.hero.drRoll());
        dmg = ((Mass_Energy)buff).absorbDamage(dmg);
        if(dmg != 0 && buff instanceof Mass_Energy){
            ((Mass_Energy) buff).add(Mass_Energy.DURATION);
        }else if (dmg == 0 && ((Mass_Energy)buff).getLvl() != 0 && buff instanceof Mass_Energy){

            int pos = Dungeon.hero.pos;
            WandOfBlastWave.BlastWave.blast(pos);
            Sample.INSTANCE.play(Assets.Sounds.BLAST);
            Dungeon.observe();
            for (int i  : PathFinder.NEIGNBOURS24){
                Char ch = Actor.findChar(pos + i);
                if (ch != null && ch != Dungeon.hero){
                    ch.damage(DamageInfo.of(((Mass_Energy) buff).blastDamage(), DamageType.MAGICAL, attacker, this));
                }
            }
            ((Mass_Energy) buff).clr();
        }
        return super.proc(attacker,defender,damage);
    }

    @Override
    public float speedFactor( Char owner, float speed ){
        if(buff instanceof Mass_Energy){
            return super.speedFactor(owner,speed) * ((Mass_Energy) buff).slowRate();
        }
        return super.speedFactor(owner,speed);
    }


    public class Mass_Energy extends ArmorBuff {
        {
            type = buffType.POSITIVE;
            announced = true;
        }
        private static final String ENERGY_LEVEL = "Energy_Lvl";

        private int lvl = 0;
        public static final int DURATION = 1;

        @Override
        public String icon() {
            return BuffIndicator.ARMOR;
        }


        @Override
        public String desc() {
            return Messages.get(this, "desc", this.lvl);
        }

        @Override
        public String iconTextDisplay() {
            return lvl +"/8";
        }

        public float slowRate(){
            return 1f - lvl * 0.1f;
        }

        public int getLvl(){
            return lvl;
        }

        public int absorbDamage(int dmg){
            return Math.max(0,(int) (dmg * (1f - lvl * 0.15f)));
        }

        public void add(int Lv){
            if(this.lvl > 0){
                this.lvl += Lv;
                this.lvl = Math.min(8,this.lvl);
            }
            else{
                this.lvl = Lv;
            }
        }

        public void clr(){
            this.lvl = 0;
        }

        public int blastDamage(){
            return (Dungeon.depth/5 + 1) * lvl * 3;
        }

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( ENERGY_LEVEL, lvl );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle(bundle);
            lvl = bundle.getInt(ENERGY_LEVEL);
        }

    }

}
