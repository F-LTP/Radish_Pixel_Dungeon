package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * 无瑕附魔：命中时有概率驱散自身负面状态。
 * 概率与效果强度随当前生命比例提高，高生命时有概率一次驱散全部负面状态。
 */
public class Flawless extends Weapon.Enchantment {

    private static final ItemSprite.Glowing WHITE = new ItemSprite.Glowing( 0xFFFFFF );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (!(attacker instanceof Hero)) {
            return damage;
        }
        Hero hero = (Hero) attacker;

        float hpPercent = hero.HP / (float) hero.HT;
        //奥术之戒等附魔倍率也要作用于触发概率
        float chance = Math.min(1f, (0.25f + 0.50f * hpPercent) * procChanceMultiplier(attacker));
        if (Random.Float() >= chance) {
            return damage;
        }

        ArrayList<Buff> debuffs = new ArrayList<>();
        for (Buff buff : hero.buffs()) {
            //与原版净化合剂相同的排除项。「遗落行囊」被驱散的话，背包会当场全部回来，不用再回去捡遗物
            if (buff.type == Buff.buffType.NEGATIVE
                    && !(buff instanceof AllyBuff) && !(buff instanceof LostInventory)) {
                debuffs.add( buff );
            }
        }
        if (debuffs.isEmpty()) {
            return damage;
        }

        int cleanse = hpPercent >= 0.75f ? debuffs.size() : 1;
        for (int i = 0; i < cleanse && i < debuffs.size(); i++) {
            debuffs.get( i ).detach();
        }
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return WHITE;
    }
}
