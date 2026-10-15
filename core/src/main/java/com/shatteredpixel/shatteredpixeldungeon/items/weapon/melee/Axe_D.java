package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.events.HitCheckEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Axe_D extends MeleeWeapon{
    {
        image = ItemSpriteSheet.AXE_D;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;

        tier = 5;
    }

    @Override
    public int max(int lvl) {
        return  5*tier +
                lvl*(tier-1);
    }
    @Override
    public int min(int lvl) {
        return  3*tier +
                lvl*(tier-2);
    }

    @Override
    public int STRReq(int lvl) {
        return STRReq(tier+1, lvl)-1; //19 base strength req, up from 18
    }

    /** 目标已受伤时必定命中（由命中判定事件驱动，装备者可能是玩家或怪物）。 */
    @SubscribeEvent(event = HitCheckEvent.class, priority = 500)
    public static void onWoundedForceHit(HitCheckEvent event) {
        if (event.getResult() != HitCheckEvent.Result.NONE) return;
        Char defender = event.getDefender();
        if (defender != null && defender.HP < defender.HT && event.getAttackerWeapon() instanceof Axe_D) {
            event.forceHit();
        }
    }
}
