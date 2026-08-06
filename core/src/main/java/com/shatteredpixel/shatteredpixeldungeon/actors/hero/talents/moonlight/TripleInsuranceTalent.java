package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.events.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;

/**
 * 三重保险天赋
 * 进食、使用药水或卷轴后获得护盾（6点/9点）
 */
public class TripleInsuranceTalent {

    @SubscribeEvent(event = HeroEatFoodEvent.class, priority = 0)
    public static void onEatFood(HeroEatFoodEvent event) {
        applyShield(event.getHero());
    }

    @SubscribeEvent(event = ThrowPotionEvent.class, priority = 0)
    public static void onThrowPotion(ThrowPotionEvent event) {
        applyShield(event.getUser() != null ? event.getUser() : null);
    }

    @SubscribeEvent(event = DrinkPotionEvent.class, priority = 0)
    public static void onDrinkPotion(DrinkPotionEvent event) {
        applyShield(event.getUser() != null ? event.getUser() : null);
    }

    @SubscribeEvent(event = ReadScrollEvent.class, priority = 0)
    public static void onReadScroll(ReadScrollEvent event) {
        applyShield(event.getUser() != null ? event.getUser() : null);
    }

    private static void applyShield(Hero hero) {
        if (hero == null) return;
        if (hero.heroClass != HeroClass.MOONLIGHT) return;

        int points = hero.pointsInTalent(Talent.TRIPLE_INSURANCE);
        if (points <= 0) return;

        int shieldAmount = 3 * (points + 1);

        Barrier barrier = hero.buff(Barrier.class);
        if (barrier == null) {
            barrier = Buff.affect(hero, Barrier.class);
        }
        barrier.incShield(shieldAmount);
    }
}