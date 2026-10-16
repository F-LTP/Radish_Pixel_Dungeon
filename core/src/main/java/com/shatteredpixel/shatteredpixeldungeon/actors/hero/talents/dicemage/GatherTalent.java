package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.dicemage;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;

/**
 * 收集被动（法力学派 L1）已改为主动技能 GatherSpell，此监听器不再生效。
 */
public class GatherTalent {
    // 已停用：收集现为主动拉取技能，见 GatherSpell
    public static void onHeroAct(HeroActEvent event) {
    }
}
