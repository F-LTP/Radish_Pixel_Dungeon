package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility_neutral;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MoveCount;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class CloakOfConcealment extends Artifact{
    public static final String AC_NONE = "NONE";
    {
        image = ItemSpriteSheet.ARTIFACT_CONCEAL;
        chargeCap=17;
        charge =0;
        levelCap = 10;
        defaultAction=AC_NONE;
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new CloakOfConcealment.conceal();
    }

    @Override
    public void charge(Hero target, float amount) {
        if (cursed || target.buff(MagicImmune.class) != null) return;

        Buff.affect(target,Invisibility_neutral.class,1f);
    }

    @Override
    public Item upgrade() {
        super.upgrade();
        chargeCap= 17-level();
        return this;
    }
    @Override
    public String desc() {
        String desc = super.desc();

        if (isEquipped (Dungeon.hero)){
            if (cursed) {
                desc += "\n\n";
                desc += Messages.get(this, "desc_cursed");
            }
        }

        return desc;
    }
    private void giveInvisibility(){
        if (isEquipped(Dungeon.hero) && !cursed){
            Buff.affect(Dungeon.hero, Invisibility_neutral.class,Math.round(2+level()*0.1f));
            charge=0;
        }
    }
    public class conceal extends ArtifactBuff {
        public void gainExp( int amount ) {
            if (cursed || target.buff(MagicImmune.class) != null ) return;

            exp += amount;
            if (exp >= 35+level()*5 && level() < levelCap){
                exp -= 35+level()*5;
                GLog.p( Messages.get(this, "levelup") );
                upgrade();
            }

        }
        @Override
        public boolean act(){
            spend(TICK);
            if (target!=null) {
                if (cursed) {
                    Buff.affect(target, Disposed.class);
                    Invisibility.dispel();
                } else if (target.invisible == 0) {
                    Buff.detach(target, Disposed.class);
                    float toGain=RingOfEnergy.artifactChargeMultiplier(target);
                    if (target.buff(MoveCount.class)!=null)
                        toGain*=target.buff(MoveCount.class).chargeMultiplier(Dungeon.hero);
                    charge += Math.round(toGain);
                    if (charge >= chargeCap) {
                        giveInvisibility();
                    }
                    updateQuickslot();
                }
            }
            return true;
        }
        @Override
        public void detach(){
            if (target.buff(Invisibility_neutral.class)!=null)
                Buff.detach(target, Invisibility_neutral.class);
            super.detach();
        }
    }
    public static class Disposed extends Buff{
        {
            immunities.add(Invisibility.class);
            immunities.add(Invisibility_neutral.class);
        }

        @Override
        public boolean attachTo( Char target ) {
            if (super.attachTo( target )) {
                target.invisible=0;
                return true;
            } else {
                return false;
            }
        }

    }
    public static class anti_nru_invisibility extends FlavourBuff{
        {
            immunities.add(Invisibility_neutral.class);
        }

        public static final float DURATION	= 1f;
    }
}
