package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.dicemage;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.events.LevelChangeEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;

/**
 * 紧急学派辅助：前往新区域前，愈合/更新法术的设置值各自衰减3/2。
 */
public class EmergencyTalent {
    @SubscribeEvent(event = LevelChangeEvent.class, priority = 0)
    public static void onLevelChange(LevelChangeEvent event) {
        if (event.getTransitionType() != LevelChangeEvent.TransitionType.DESCEND
                && event.getTransitionType() != LevelChangeEvent.TransitionType.FALL) return;
        Hero hero = Dungeon.hero;
        if (hero == null || hero.subClass != HeroSubClasses.DICE_MAGE) return;
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp == null) return;
        if (hero.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SCHOOL_EMERGENCY) >= 1) {
            mp.decayHealValue();
        }
        if (hero.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SCHOOL_EMERGENCY) >= 2) {
            mp.decayRefreshValue();
        }
    }
}
