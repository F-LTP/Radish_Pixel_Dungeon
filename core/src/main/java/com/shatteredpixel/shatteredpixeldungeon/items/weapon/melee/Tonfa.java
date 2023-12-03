package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Tonfa extends MeleeWeapon{
    public static final String AC_ZAP = "ZAP";
    private int energy_charge =0;

    {
        image = ItemSpriteSheet.TONFA;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 1.2f;

        tier = 5;
        DLY = 0.5f;

        defaultAction = AC_ZAP;
    }

    private static final String ENE_CHARGE = "ene_charge";
    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(ENE_CHARGE))
            energy_charge = bundle.getInt(ENE_CHARGE);
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put(ENE_CHARGE, energy_charge);
    }
    @Override
    public int max(int lvl) {
        return  3*(tier-1) +
                lvl*(tier-2) ;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        energy_charge+=5;
        if (energy_charge>100) energy_charge=100;
        updateQuickslot();
        return super.proc(attacker, defender, damage);
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_ZAP);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {

        super.execute(hero, action);
        usesTargeting = false;

        if (Dungeon.hero.belongings.weapon == this) {
            if (action.equals(AC_ZAP) && energy_charge >= 20) {
                if (this.cursed != true) {
                    usesTargeting = true;
                    curItem=this;
                    cursedKnown = true;
                    GameScene.selectCell(zapper);
                } else {
                    cursedKnown = true;
                    energy_charge -= 20;
                }
            }
        }
    }

    @Override
    public String statsInfo() {
        if (!isIdentified())
            return Messages.get(this, "stats_desc",2, 8);
        return Messages.get(this, "stats_desc",2+buffedLvl(), 8+buffedLvl() * 2);
    }

    @Override
    public String special(){
        return energy_charge+"%";
    }

    protected static CellSelector.Listener zapper = new CellSelector.Listener() {

        @Override
        public void onSelect(Integer target) {

            if (target != null) {
                Tonfa ss= (Tonfa)curItem;
                    Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1f);

                    Ballistica shot = new Ballistica(curUser.pos, target, Ballistica.PROJECTILE);
                    int cell = shot.collisionPos;

                    if (target == curUser.pos || cell == curUser.pos) {
                        GLog.i(Messages.get(Tonfa.class, "self_target"));
                        return;
                    }

                    curUser.sprite.zap(cell);

                    //attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
                    if (Actor.findChar(target) != null)
                        QuickSlotButton.target(Actor.findChar(target));
                    else
                        QuickSlotButton.target(Actor.findChar(cell));
                    ss.fx(shot, new Callback() {
                            public void call() {
                                ss.onZap(shot);
                            }
                        });
            }
        }

        @Override
        public String prompt() {
            return Messages.get(Tonfa.class, "prompt");
        }
    };

    protected void fx( Ballistica bolt, Callback callback ) {
        MagicMissile.boltFromChar( curUser.sprite.parent,
                MagicMissile.FORCE,
                curUser.sprite,
                bolt.collisionPos,
                callback);
    }


    protected void onZap( Ballistica bolt ) {
        Char ch = Actor.findChar( bolt.collisionPos );
        if (ch != null) {
            int dmg = Random.Int(2+buffedLvl(), 8+buffedLvl() * 2);
            ch.damage(dmg, this);
            ch.damage(dmg, this);
            ch.sprite.burst(0xFFFFFFFF, buffedLvl() / 2 + 2);

        } else {
            Dungeon.level.pressCell(bolt.collisionPos);
        }
        energy_charge -=20;
        updateQuickslot();
    }

}
