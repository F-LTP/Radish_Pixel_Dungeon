package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rector;

import static com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave.throwChar;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.GodsPossessionBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

import java.util.Collections;
import java.util.Set;

public class GodsPossesion extends ArmorAbility {

    @Override
    public String icon() {
        return HeroIcon.POSSESSION;
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {

        // avatar (talent)
        if(hero.hasTalent(Talent.AVATAR)){
            Buff.affect(hero, MagicalSight.class,30f);
        }
        // holy shockwave (talent)
        if(hero.hasTalent(Talent.HOLY_SHOCKWAVE)){
            int lvl = hero.pointsInTalent(Talent.HOLY_SHOCKWAVE);
            Buff.affect(hero, Healing.class).setHeal(5 + lvl*5,1f,0);
            holyBlastWave(hero,lvl+1);
        }
        // flush buff
        if(hero.buff(GodsPossessionBuff.class) != null)
            hero.buff(GodsPossessionBuff.class).detach();
        Buff.affect(hero, GodsPossessionBuff.class,GodsPossessionBuff.DURATION);
    }

    void holyBlastWave(Hero hero,int strength){
        // art effect
        WandOfBlastWave.BlastWave.blast(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.BLAST);
        Dungeon.observe();

        // momentum
        Set<Mob> mobs = Collections.synchronizedSet(Dungeon.level.mobs);
        for(Mob mob:mobs){
            if(hero.fieldOfView[mob.pos] && mob.alignment == Char.Alignment.ENEMY){
                int direction = mob.pos - hero.pos;
                Ballistica trajectory = new Ballistica(mob.pos, mob.pos + direction, Ballistica.MAGIC_BOLT);
                throwChar(mob, trajectory, strength, false, true, getClass());
                Dungeon.hero.spendAndNext(1f);
            }
        }
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.HOLY_SHOCKWAVE,Talent.GODHOOD,Talent.AVATAR,Talent.HEROIC_ENERGY};
    }
}
