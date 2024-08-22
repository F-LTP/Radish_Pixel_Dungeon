package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Calm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CriticalAttack;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Taijutsu extends MeleeWeapon {

    {
        image = ItemSpriteSheet.TAIKIG;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;

        tier = 5;
    }

    @Override
    public int min(int lvl){
        return 1;
    }

    @Override
    public int max(int lvl){
        return 30 + lvl * 7;
    }

    @Override
    public boolean doEquip(Hero hero) {
        Buff.affect(hero, Calm.class);
        Buff.affect(hero, CriticalAttack.class);
        return super.doEquip(hero);
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single){
        if(hero.buff(Calm.class) != null) hero.buff(Calm.class).detach();
        if(hero.buff(CriticalAttack.class) != null) hero.buff(CriticalAttack.class).detach();
        return super.doUnequip(hero, collect, single);
    }
}
