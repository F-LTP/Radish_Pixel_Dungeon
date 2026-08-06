package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class PneumFistGloves extends MeleeWeapon {


    public Wand wand;

    public boolean active = false;

    private static final String AC_ACTIVE	= "active";
    private static final String AC_ON_ACTIVE	= "on_active";

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions(hero);
        if (!active && isIdentified() && !cursed){
            actions.add(AC_ACTIVE);
        } else if(active && isIdentified() && !cursed) {
            actions.add(AC_ON_ACTIVE);
        }
        return actions;
    }

    public String image() {
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

        }
    }

    {
        image = ItemSpriteSheet.PNEGLOVE_FIVE;
        tier = 3;
        DLY = 0.8f;
    }

    @Override
    public int min(int lvl) {
        int originDamage = 4+lvl;
        return active ? (int) (originDamage * ((0.1f * lvl) + 1.5f)) : originDamage;
    }


    @Override
    public int max(int lvl) {
        int originDamage = 15 + lvl * 3;
        return active ? (int) (originDamage * ((0.1f * lvl) + 1.5f)) : originDamage;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {

        if(Dungeon.energy>0 && active){
            Dungeon.energy--;
            hero.sprite.showStatusWithIcon( 0x44CCFF, Integer.toString(-1), FloatingText.ENERGY );
            int oppositeAdjacent = defender.pos + (defender.pos - attacker.pos);
            Ballistica trajectory = new Ballistica(defender.pos, oppositeAdjacent, Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(defender, trajectory, (int) (2+(0.5*level())), true, true, this);
        } else if(active) {
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
}

