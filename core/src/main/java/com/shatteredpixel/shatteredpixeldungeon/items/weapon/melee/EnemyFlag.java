package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class EnemyFlag extends MeleeWeapon {

    {
        image = ItemSpriteSheet.IAMSB_FLAG;
        tier = 3;
        RCH = 2;
    }

    @Override
    public int min(int lvl) {
        return 8 + lvl;
    }

    @Override
    public boolean doEquip( Hero hero ) {
        Buff.affect(hero, MobWarningProject.class);
        return super.doEquip(hero);
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        Buff.detach(hero, MobWarningProject.class);
        return super.doUnequip(hero, collect, single);
    }

    @Override
    public int STRReq(int lvl) {
        return (9 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    public int max(int lvl) {
        return 20 + lvl * 5;
    }

    public static class MobWarningProject extends Buff {

        {
            type=buffType.POSITIVE;
        }

        @Override
        public boolean act() {
            for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
                if (mob.alignment != Char.Alignment.ALLY && Dungeon.level.heroFOV[mob.pos] && mob.state == mob.SLEEPING) {
                    mob.state = mob.HUNTING;
                    mob.beckon(mob.pos);
                }
            }
            spend( TICK );
            return true;
        }
    }

    public String statsInfo(){
        return Messages.get(this, "stats_desc");
    }

}
