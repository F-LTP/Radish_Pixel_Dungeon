package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.utils.LegacyItemWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

// 决斗对剑
public class DualDuelDaggers extends LegacyItemWeapon {
    {
        image = ItemSpriteSheet.DUAL_DUEL_DAGGERS;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;
        DLY = 0.8f;
    }

    private int lastDamage = 0;

    @Override
    public int min(int lvl) {
        return 8+lvl;
//        return 1;
    }

    @Override
    public int max(int lvl) {
        return 20+lvl*5;
//        return 1;
    }


    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if(damage == lastDamage && Dungeon.hero != null){
            ArtifactRecharge.chargeArtifacts(Dungeon.hero, 1000);
            Buff.affect(attacker, Recharging.class, 40f);
            PotionOfHealing.heal(attacker);
        }
        lastDamage = damage;
        return damage;
    }

    @Override
    public int STRReq(int lvl) {
        lvl = Math.max(0, lvl);
        return (8 + 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }
}
