package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rector;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Soulstaker;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

// 暗影咒文
// 20260115 by DoggingDog
public class ShadowHymn extends ArmorAbility {
    {
        baseChargeUse = 35;
    }

    @Override
    public String icon() {
        return HeroIcon.SHADOW;
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {

        armor.charge -= chargeUse(hero);
        armor.updateQuickslot();

        // Sacrifice
        if(hero.hasTalent(Talent.SACRIFICE)){
            int hpNeed = Math.min(hero.HP-1,hero.lvl * 2);
            hero.damage(DamageInfo.of(hpNeed, DamageType.TRUE, null, ShadowHymn.class));
        }
        if(hero.pointsInTalent(Talent.SACRIFICE) > 2){
            int shield = hero.pointsInTalent(Talent.SACRIFICE)/2;
            Buff.affect(hero, Barrier.class).setShield(shield * hero.lvl * 2);
        }
        //

        Buff buff = hero.buff(Soulstaker.class);
        if(buff != null)
            buff.detach();
        Buff.affect(hero, Soulstaker.class, Soulstaker.DURATION);
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.SACRIFICE,Talent.BLOCKING_READING,Talent.TAI_CHI_POISE,Talent.HEROIC_ENERGY};
    }
}
