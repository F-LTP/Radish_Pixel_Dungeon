package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroGainExperienceEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class HuntingIntuitionTalent {
    public static class ExperienceCounter extends Buff {
        public int count = 0;

        @Override
        public boolean act() {
            spend(TICK);
            return true;
        }
    }

    @SubscribeEvent(event = HeroGainExperienceEvent.class, priority = 0)
    public static void onGainExperience(HeroGainExperienceEvent event) {
        Hero hero = event.getHero();

        if (hero.heroClass != HeroClass.MOONLIGHT) return;

        int points = hero.pointsInTalent(Talent.HUNTING_INTUITION);
        if (points <= 0) return;

        // 获得0经验时不计数（如幽灵等）
        if (event.getExp() <= 0) return;

        ExperienceCounter counter = hero.buff(ExperienceCounter.class);
        if (counter == null) {
            counter = Buff.affect(hero, ExperienceCounter.class);
        }

        counter.count++;

        int threshold = points >= 2 ? 15 : 20;
        if (counter.count >= threshold) {
            counter.count = 0;
            MysteryMeat meat = new MysteryMeat();
            Level level = Dungeon.level;
            if (level != null) {
                level.drop(meat, hero.pos).sprite.drop();
                GLog.p(Messages.get(HuntingIntuitionTalent.class,"loot"));
            }
        }
    }
}