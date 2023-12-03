package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class RottenLance extends MeleeWeapon{// idea is from relic pd
    private static final String AC_WEAPONSKILL="AC_WS";
    {
        image = ItemSpriteSheet.SPEAR;
        hitSound = Assets.Sounds.HIT_STAB;
        hitSoundPitch=1f;

        tier=2;
        RCH=2;
        defaultAction=AC_WEAPONSKILL;
    }
    @Override
    public int max(int lvl) {
        return  14 +
                lvl*2+
                (isRoot?11+2*lvl:0);
    }
    boolean isRoot=false;
    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if(action.equals(AC_WEAPONSKILL)){
            if(isRoot){
                if(hero.buff(Root.class)!=null){
                    hero.buff(Root.class).detach();
                }
                hero.sprite.operate( hero.pos );
                isRoot=false;
                hero.spendAndNext(Actor.TICK);
            }else {
                if((Dungeon.level.map[hero.pos] != Terrain.GRASS &&Dungeon.level.map[hero.pos] != Terrain.HIGH_GRASS && Dungeon.level.map[hero.pos] != Terrain.FURROWED_GRASS) || hero.buff(Levitation.class)!=null){
                    GLog.w(Messages.get(this,"need"));
                    return;
                }
                onRoot(hero);
                isRoot=true;
            }
            modeSwitch();
        }
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        if(isRoot){
            GLog.n(Messages.get(this,"root"));
            return false;
        }
        return super.doUnequip(hero, collect, single);
    }

    protected void onRoot(Hero hero){

        if (hero.buff(MagicImmune.class)!=null){
            GLog.i( Messages.get(MeleeWeapon.class, "magic_immune") );
            return;
        }
        hero.sprite.operate( hero.pos );
        Buff.affect(hero,Root.class);
    }

    @Override
    public int defenseFactor(Char owner) {
        if(owner.buff(Root.class)!=null){
            return 1+buffedLvl() ;
        }else {
            return super.defenseFactor(owner);
        }
    }
    public void modeSwitch() {
        if(isRoot){
            DLY=1.5f;
            RCH=3;
        }else {
            DLY=1f;
            RCH=2;
        }
    }
    @Override
    public String statsInfo() {
        if(isIdentified()){
            return  Messages.get(this, "stats_desc",1+buffedLvl());
        }else {
            return  Messages.get(this, "typical_stats_desc",1);
        }

    }
    public static class Root extends Buff{
        {
            announced = true;
        }
        {
            immunities.add( Levitation.class );
        }
        @Override
        public int icon() {
            return BuffIndicator.ARMOR;
        }

        @Override
        public boolean act() {
            if (target.buff(Burning.class)!=null){
                detach();
            }
            return super.act();
        }@Override
        public boolean attachTo( Char target ) {
            if (super.attachTo( target )) {
                target.rooted = true;
                if (target.buff(Levitation.class)!=null) Buff.detach(target, Levitation.class);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void detach() {
            target.rooted = false;
            super.detach();
        }
    }
    private static final String ISROOT	        = "isroot";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( ISROOT,isRoot );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        isRoot=bundle.getBoolean(ISROOT);
        modeSwitch();
    }
}