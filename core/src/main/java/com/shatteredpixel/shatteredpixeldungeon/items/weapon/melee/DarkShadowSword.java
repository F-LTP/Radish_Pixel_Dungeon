package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class DarkShadowSword extends MeleeWeapon {

    {
        image = ItemSpriteSheet.DARK_SHADOW_SWORD;
        tier = 2;
        ACC = 1.2f;
    }

    @Override
    public float delayFactor(Char user) {
        float delay = super.delayFactor(user);
        boolean[] fov = user.fieldOfView != null ? user.fieldOfView : Dungeon.level.heroFOV;
        int visibleMobs = 0;
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
            if(fov[mob.pos] && mob.alignment == Char.Alignment.ENEMY){
                visibleMobs++;
            }
        }
        float speedBonus = (0.2f + 0.05f * level()) * visibleMobs;
        delay = 1 - speedBonus;
        return Math.max(0.25f, delay);
    }

    public String desc() {
        float delay;
        String s;
        int visibleMobs = 0;
        if(Dungeon.level != null){
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
                if(Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY){
                    visibleMobs++;
                }
            }
            float speedBonus = (0.2f + 0.05f * level()) * visibleMobs;
            delay = 1 - speedBonus;
            s = Messages.get(this, "desc", String.format("%.2f", Math.max(0.25f, delay)));
            return s;
        }
        return Messages.get(this, "desc", String.format("%.2f", Math.max(0.25f, 1)));
    }


    @Override
    public int min(int lvl) {
        return 2 + lvl;
    }
    @Override
    public int max(int lvl) {
        return 12 + lvl * 2;
    }

}
