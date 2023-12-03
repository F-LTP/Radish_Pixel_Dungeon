package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class DirkOfB extends MeleeWeapon{

    {
        image = ItemSpriteSheet.DIRK_B;
        hitSound = Assets.Sounds.HIT_STAB;
        hitSoundPitch = 0.9f;

        tier = 4;
    }

    @Override
    public int max(int lvl) {
        return  3*(tier+1) +
                lvl*(tier);
    }
    @Override
    public int min(int lvl) {
        return  2*(tier+1) +
                lvl;
    }
    @Override
    public int damageRoll(Char owner) {
        if (owner instanceof Hero) {
            Hero hero = (Hero)owner;
            Char enemy = hero.enemy();
            if (enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)) {
                Buff.affect(enemy,DB_first.class);
            }
        }
        return super.damageRoll(owner);
    }

    @Override
    public int proc(Char attacker, Char defender, int damage){
        if (Random.Float()<0.15f+0.02f*buffedLvl()) Buff.affect(defender, Cripple.class,3);
        float chance_for_poison=0.3f+0.04f*buffedLvl();
        int time_for_poison=6+3*buffedLvl();
        if (defender.buff(DB_already_first.class)==null){
            if (defender.buff(DB_first.class)!=null) {
                chance_for_poison*=2;
                time_for_poison*=2;
                Buff.affect(defender, DB_already_first.class);
            }
        }
        if (Random.Float()<chance_for_poison) Buff.affect(defender, Poison.class).set(time_for_poison);
        return super.proc(attacker,defender,damage);
    }
    public static class DB_first extends Buff {

    }
    public static class DB_already_first extends Buff {

    }
}
