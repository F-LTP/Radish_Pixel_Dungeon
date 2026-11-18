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
        // 装备可能失败（手里是脱不下来的诅咒武器、村正狂热），确认装上了再挂 buff；
        // 否则旗子没拿在手上，「被敌人立即发现」的效果却一直跟着英雄
        if (super.doEquip(hero)) {
            Buff.affect(hero, MobWarningProject.class);
            return true;
        }
        return false;
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        // 同理，旗子被诅咒卸不下来时效果不能消失
        if (super.doUnequip(hero, collect, single)) {
            Buff.detach(hero, MobWarningProject.class);
            return true;
        }
        return false;
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
            // 旗子不在主手上了就自行移除：以前装备失败时留下的，
            // 或者武器被缴械陷阱、滑溜诅咒直接从手上拿走、没有经过正常卸下的
            if (!(target instanceof Hero) || com.shatteredpixel.shatteredpixeldungeon.items.Fusion.fusedItem(((Hero) target).belongings.weapon, EnemyFlag.class) == null) {
                detach();
                return true;
            }
            for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
                if (mob.alignment != Char.Alignment.ALLY && mob.state == mob.SLEEPING) {
                    if (mob.fieldOfView != null && mob.fieldOfView.length > hero.pos && mob.fieldOfView[hero.pos]) {
                        mob.state = mob.HUNTING;
                        mob.beckon(mob.pos);
                    }
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
