package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility_neutral;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

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
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if (action.equals(AC_NONE)) return;
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
            if (cursed){
                Invisibility.dispel();
                Buff.affect(target,Disposed.class);
            }else if (target.invisible==0){
                Buff.detach(target,Disposed.class);
                charge+= Math.round(RingOfEnergy.artifactChargeMultiplier(target));
                if (charge>=chargeCap){
                    giveInvisibility();
                }
                updateQuickslot();
            }
            return true;
        }
    }
    public class Disposed extends Buff{
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
