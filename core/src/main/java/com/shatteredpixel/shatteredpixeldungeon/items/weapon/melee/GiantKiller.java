package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.PreAttackEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.Set;

public class GiantKiller extends MeleeWeapon {

    {
        image = ItemSpriteSheet.GIANTKILL;
        tier = 5;
    }

    @Override
    public int min(int lvl) {
        return 4 + lvl;
    }
    @Override
    public int max(int lvl) {
        return 25 + lvl * 5;
    }

    public boolean isMustCrit;

    private static boolean mustCritAgainst(Char defender) {

        Set<Char.Property> properties = defender.properties();

        boolean isSpecialEnemy =
                   properties.contains(Char.Property.BOSS)
                || properties.contains(Char.Property.MINIBOSS)
                || properties.contains(Char.Property.ELITES);

        boolean hasChampionBuff = false;
        for (Buff b : defender.buffs(ChampionEnemy.class)) {
            if (b != null) {
                hasChampionBuff = true;
                break;
            }
        }

        return isSpecialEnemy || hasChampionBuff;
    }

    // 「这一击必定暴击」要在掷暴击之前定下来，所以挂在攻击开始的事件上。
    // 以前是在 proc 里才设标记，而 proc 发生在掷暴击之后，标记其实是给下一击用的：
    // 砍首领的第一刀不必暴，砍完首领再去砍小怪的第一刀反而必暴
    @SubscribeEvent(event = PreAttackEvent.class, priority = 0)
    public static void onPreAttack(PreAttackEvent event) {
        Char attacker = event.getAttacker();
        // 与掷暴击时取武器的方式保持一致：英雄看主手，其它角色看它拿着的武器
        Item weapon = attacker instanceof Hero
                ? ((Hero) attacker).belongings.weapon()
                : attacker.attackingWeapon();
        if (weapon instanceof GiantKiller) {
            ((GiantKiller) weapon).isMustCrit = mustCritAgainst(event.getDefender());
        }
    }




}
