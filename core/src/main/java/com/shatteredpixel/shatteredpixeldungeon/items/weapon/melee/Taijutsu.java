package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
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
        // 装备可能失败（手里是脱不下来的诅咒武器、村正狂热），确认装上了再给 buff；
        // 否则大太刀还在背包里，英雄就永久拿到了它的暴击伤害加成，配什么武器都生效
        if (super.doEquip(hero)) {
            Buff.affect(hero, Calm.class);
            Buff.affect(hero, CriticalAttack.class);
            return true;
        }
        return false;
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single){
        // 同理，大太刀被诅咒卸不下来时 buff 不能丢
        if (super.doUnequip(hero, collect, single)) {
            if(hero.buff(Calm.class) != null) hero.buff(Calm.class).detach();
            if(hero.buff(CriticalAttack.class) != null) hero.buff(CriticalAttack.class).detach();
            return true;
        }
        return false;
    }
}
