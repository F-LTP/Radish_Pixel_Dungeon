package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CompositeCrossbow extends MeleeWeapon{
    public static final String AC_SHOOT		= "SHOOT";
    private int curAmmo = 50;
    {
        image = ItemSpriteSheet.CROSSBOW_C;

        defaultAction = AC_SHOOT;
        tier = 4;
    }
    private static final String AMMO = "ammo";
    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(AMMO))
            curAmmo = bundle.getInt(AMMO);
        else
            curAmmo=50;
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put(AMMO, curAmmo);
    }
    @Override
    public String special() {
        if (curAmmo>0)
            return String.valueOf(curAmmo);
        else
            return null;
    }
    @Override
    public boolean specialColorChange() {
        return curAmmo<10;
    }
    @Override
    public int max(int lvl) {
        return  (curAmmo>0?15:20)+
                lvl*tier;
    }
    @Override
    public int min(int lvl) {
        return  (curAmmo>0?4:5) +
                lvl*(curAmmo>0?1:2);
    }
    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (curAmmo>0)
            actions.add(AC_SHOOT);
        return actions;
    }
    @Override
    public void execute(Hero hero, String action) {

        super.execute(hero, action);

        if (action.equals(AC_SHOOT)) {
            curUser = hero;
            if (!isEquipped(Dungeon.hero)) {
                GLog.w(Messages.get(this, "not_equipped"));
                usesTargeting = false;
            }
            else if (curAmmo > 0) {
                    usesTargeting = true;
                    GameScene.selectCell(shooter);
            } else {
                    GLog.w(Messages.get(this, "no_ammo"));
                usesTargeting = false;
                }
        }
    }
    @Override
    public String statsInfo(){
        if (curAmmo>0)
            return Messages.get(this, "stats_desc",combinedArrow().min(),combinedArrow().max(),curAmmo);
        else
            return Messages.get(this,"extra_desc");
    }
    @Override
    public int targetingPos(Hero user, int dst) {
        return combinedArrow().targetingPos(user, dst);
    }

    public CrossbowArrow combinedArrow(){
        return new CrossbowArrow();
    }

    public class CrossbowArrow extends MissileWeapon {

        {
            image = ItemSpriteSheet.SPIRIT_ARROW;

            hitSound = Assets.Sounds.HIT_ARROW;
        }

        @Override
        public int damageRoll(Char owner) {
            int dmg=Random.NormalIntRange(min(),max());
            if (owner instanceof Hero) {
                int exStr = ((Hero)owner).STR() - STRReq();
                if (exStr > 0) {
                    dmg += Random.IntRange( 0, exStr );
                }
            }
            return dmg;
        }
        @Override
        public int max(){
            return 15+5*CompositeCrossbow.this.buffedLvl();
        }
        @Override
        public int min(){
            return 10+3*CompositeCrossbow.this.buffedLvl();
        }
        @Override
        public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
            return CompositeCrossbow.this.hasEnchant(type, owner);
        }

        @Override
        public int proc(Char attacker, Char defender, int damage) {
            return CompositeCrossbow.this.proc(attacker, defender, damage);
        }

        @Override
        public float delayFactor(Char user) {
            return (1f/speedMultiplier(user));
        }

        @Override
        public int STRReq(int lvl) {
            return CompositeCrossbow.this.STRReq(lvl);
        }

        @Override
        protected void onThrow( int cell ) {
            Char enemy = Actor.findChar( cell );
            if (enemy == null || enemy == curUser) {
                parent = null;
                Splash.at( cell, 0xCC99FFFF, 1 );
            } else {
                if (!curUser.shoot( enemy, this )) {
                    Splash.at(cell, 0xCC99FFFF, 1);
                }
            }
        }

        @Override
        public void throwSound() {
            Sample.INSTANCE.play( Assets.Sounds.ATK_SPIRITBOW, 1, Random.Float(0.87f, 1.15f) );
        }

        @Override
        public void cast(final Hero user, final int dst) {
            super.cast(user, dst);
            curAmmo--;
            updateQuickslot();
        }
    }
    private CellSelector.Listener shooter = new CellSelector.Listener() {
        @Override
        public void onSelect( Integer target ) {
             if (target != null) {
                combinedArrow().cast(curUser, target);
             }
        }
        @Override
        public String prompt() {
            return Messages.get(CompositeCrossbow.class, "prompt");
        }
    };
}
