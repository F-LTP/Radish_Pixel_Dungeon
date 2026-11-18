package com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

/**
 * 滑溜诅咒：攻击敌人时有 5% 概率使武器从手中滑落。
 * 掉落延迟到下一次行动结算，避免攻击动画中途更换武器。
 */
public class Slippery extends Weapon.Enchantment {

    private static final ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x333333 );

    private static final float DROP_CHANCE = 0.05f;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (!(attacker instanceof Hero)) {
            return damage;
        }
        Hero hero = (Hero) attacker;
        if (hero.belongings.weapon != weapon) {
            return damage;
        }
        if (hero.buff( SlipperyDrop.class ) != null) {
            return damage;
        }
        if (Random.Float() < DROP_CHANCE * procChanceMultiplier( attacker )) {
            Buff.affect( hero, SlipperyDrop.class );
        }
        return damage;
    }

    @Override
    public boolean curse() {
        return true;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return BLACK;
    }

    /** 下一次行动开始时把滑落的武器掉在脚下。 */
    public static class SlipperyDrop extends Buff {

        {
            type = buffType.NEGATIVE;
            announced = false;
        }

        @Override
        public boolean act() {
            if (target instanceof Hero) {
                Hero hero = (Hero) target;
                if (!hero.isAlive()) {
                    detach();
                    return true;
                }
                KindOfWeapon weapon = hero.belongings.weapon;
                if (weapon instanceof Weapon && ((Weapon) weapon).enchantment instanceof Slippery) {
                    hero.belongings.weapon = null;
                    Dungeon.quickslot.clearItem( weapon );
                    weapon.updateQuickslot();

                    Dungeon.level.drop( weapon, hero.pos ).sprite.drop( hero.pos );
                    GLog.w( Messages.get( Slippery.class, "slip" ) );
                    Sample.INSTANCE.play( Assets.Sounds.MISS );
                }
            }
            detach();
            return true;
        }

        @Override
        public String icon() {
            return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.NONE;
        }
    }
}
