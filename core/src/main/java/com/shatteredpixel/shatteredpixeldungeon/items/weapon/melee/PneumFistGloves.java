package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public class PneumFistGloves extends MeleeWeapon {


    public Wand wand;

    public boolean active = false;

    private static final String AC_ACTIVE	= "active";
    private static final String AC_ON_ACTIVE	= "on_active";

    private static final String AC_ZAP	= "on_zap";

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions(hero);
        if (!active && isIdentified() && !cursed){
            actions.add(AC_ACTIVE);
        } else if(active && isIdentified() && !cursed) {
            actions.add(AC_ON_ACTIVE);
            actions.add(AC_ZAP);
        }
        return actions;
    }

    public int image() {
        if (active){
            super.image = ItemSpriteSheet.PNEGLOVE_ACTIVE;
        } else {
            super.image = ItemSpriteSheet.PNEGLOVE_FIVE;
        }
        return image;
    }

    @Override
    public void execute(final Hero hero, String action) {
        super.execute(hero, action);

        //①
        if (action.equals( AC_ACTIVE )) {
            if(!active){
                active = true;
                GLog.w(Messages.get(this,"active"));
            }
        } else if (action.equals( AC_ON_ACTIVE )) {
            if(active){
                active = false;
                GLog.w(Messages.get(this,"on_active"));
            }

        } else if (action.equals(AC_ZAP)){
            curUser = hero;
            curItem = this;
            GameScene.selectCell( zapper );
        }
    }

    {
        image = ItemSpriteSheet.PNEGLOVE_FIVE;
        tier = 3;
        DLY = 0.8f;
    }

    @Override
    public String defaultAction() {
        if(active){
            return defaultAction = AC_ZAP;
        }
        return AC_THROW;
    }

    @Override
    public int min(int lvl) {
        int originDamage = 4+lvl;
        return active ? (int) (originDamage + ((0.1f * lvl) + 1.5f)) : originDamage;
    }


    @Override
    public int max(int lvl) {
        int originDamage = 15 + lvl * 3;
        return active ? (int) (originDamage + ((0.1f * lvl) + 1.5f)) : originDamage;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {

        if(Dungeon.energy>0 && active){
            Dungeon.energy--;
            hero.sprite.showStatusWithIcon( 0x44CCFF, Integer.toString(-1), FloatingText.ENERGY );
            int oppositeAdjacent = defender.pos + (defender.pos - attacker.pos);
            Ballistica trajectory = new Ballistica(defender.pos, oppositeAdjacent, Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(defender, trajectory, (int) (2+(0.5*level())), true, true, this);
        } else {
            attacker.sprite.showStatusWithIcon(CharSprite.WARNING, Messages.get(this,"low"), FloatingText.STRENGTH);
        }

        return super.proc(attacker, defender, damage);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        active = bundle.getBoolean("active_boolean");
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("active_boolean", active);
    }

    public void fxs(Ballistica beam, Callback callback) {
        curUser.sprite.parent.add(
                new Beam.DeathRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(beam.collisionPos)));
        callback.call();
    }

    protected int collisionProperties = Ballistica.MAGIC_BOLT;

    public int collisionProperties(int target){
        return collisionProperties;
    }


    protected static CellSelector.Listener zapper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {

            if (target != null) {

                final PneumFistGloves curWand;

                if (curItem instanceof PneumFistGloves) {
                    curWand = (PneumFistGloves) curItem;
                } else {
                    return;
                }
                final Ballistica shot = new Ballistica( curUser.pos, target, curWand.collisionProperties(target));
                int cell = shot.collisionPos;

                if(Dungeon.energy < 0){
                    GLog.w( Messages.get(PneumFistGloves.class, "donot_energy") );
                    return;
                } else if (target == curUser.pos || cell == curUser.pos) {
                    GLog.i( Messages.get(Wand.class, "self_target") );
                    return;
                }
                curUser.sprite.zap(cell);

                if (curWand.tryToZap(curUser)) {
                    curUser.busy();
                    curWand.fxs(shot, new Callback() {
                        public void call() {
                            curWand.onZap(shot);
                            curUser.spendAndNext( 1f );
                        }
                    });
                    curWand.cursedKnown = true;
                }
            }
        }

        @Override
        public String prompt() {
            return Messages.get(Wand.class, "prompt");
        }
    };

    public boolean tryToZap( Hero owner ){

        if (owner.buff(MagicImmune.class) != null){
            GLog.w( Messages.get(Wand.class, "no_magic") );
            return false;
        }

        return true;
    }


    public void onZap( Ballistica beam ) {
        Char ch = Actor.findChar(beam.collisionPos);

        if (ch != null){

            if (!(ch instanceof Mob)){
                return;
            }

            Mob enemy = (Mob) ch;

            int oppositeAdjacent = enemy.pos + (enemy.pos - hero.pos);
            Ballistica trajectory = new Ballistica(enemy.pos, oppositeAdjacent, Ballistica.WONT_STOP);

            if(Dungeon.energy>0){
                WandOfBlastWave.throwChar(enemy,trajectory, (int) (2+(0.5*level())), false, false, this);
                enemy.state = enemy.WANDERING;
                hero.belongings.weapon().proc( hero, enemy, hero.damageRoll() );
            }

        }
    }
}


