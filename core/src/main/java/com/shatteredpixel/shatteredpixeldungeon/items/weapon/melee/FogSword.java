package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

public class FogSword extends MeleeWeapon {

    {
        image = ItemSpriteSheet.FOGSWORD;
        tier = 5;
    }

    @Override
    public int min(int lvl) {
        return 2 + lvl;
    }
    @Override
    public int max(int lvl) {
        return 20 + lvl * 4;
    }

    @Override
    public int STRReq(int lvl) {
        return (7 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    public int proc(Char attacker, Char defender, int damage ) {
        if(defender instanceof Mob){
            Mob ks =(Mob)defender;
            if (ks.onlyActDown){
                ks.onlyActDown = false;
                Buff.affect(attacker, ActBless.class, 2+level());
            }
        }
        else {
            Buff.affect(attacker, ActBless.class, 2+level());
        }
        return super.proc(attacker, defender, damage);
    }

    public static class ActBless extends FlavourBuff {

        public static final float DURATION	= 6f;

        {
            type = buffType.POSITIVE;
            announced = true;
        }

        @Override
        public int icon() {
            return BuffIndicator.HEX;
        }

        @Override
        public String desc() {
            float s = 0;
            if(hero.belongings.weapon() instanceof FogSword) {
                FogSword ks = (FogSword) hero.belongings.weapon;
                s = (20 + (float) (ks.level() * 4));
            }
            return Messages.get(this, "desc", s , dispTurns());
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0xccbb00);
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }
    }

}
