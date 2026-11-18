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

        // 和其它护甲技能一样先扣充能。这里原先漏了：技能说明写着消耗充能，实际一点不扣，
        // 充能够一次的量就能每回合连放（带治愈圣启时等于无限回血加全屏击退）
        armor.charge -= chargeUse(hero);
        armor.updateQuickslot();

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

        // 技能本身要吃掉一回合，并且立刻刷新一次视野/界面，否则玩家会觉得“点了没反应”
        Dungeon.observe();
        hero.spendAndNext( 1f );
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
                //击退方向的目标格是英雄关于怪物的对称点
                int w = Dungeon.level.width();
                int targetX = 2 * (mob.pos % w) - hero.pos % w;
                int targetY = 2 * (mob.pos / w) - hero.pos / w;
                Ballistica trajectory;
                if (targetX >= 0 && targetX < w && targetY >= 0) {
                    //超出地图下沿不用另外处理：弹道只把目标下标换算回坐标来定方向，这时换算结果仍然正确
                    trajectory = new Ballistica(mob.pos, targetX + targetY * w, Ballistica.MAGIC_BOLT);
                } else {
                    //横向出界的下标会绕到相邻的行，向上出界会变成负数，换算回坐标后方向是错的，怪物会被推向不相干的方向。
                    //这时改用「英雄 -> 怪物」连线的延长线，取它在地图内的最后一格当目标。
                    //这一格之外就是地图边界，所以弹道要在目标格停下；怪物自己就站在这一格时弹道长度为 0，不位移
                    Ballistica toMob = new Ballistica(hero.pos, mob.pos, Ballistica.STOP_TARGET);
                    trajectory = new Ballistica(mob.pos, toMob.path.get(toMob.path.size() - 1), Ballistica.PROJECTILE);
                }
                throwChar(mob, trajectory, strength, false, true, getClass());
            }
        }
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.HOLY_SHOCKWAVE,Talent.GODHOOD,Talent.AVATAR,Talent.HEROIC_ENERGY};
    }
}
