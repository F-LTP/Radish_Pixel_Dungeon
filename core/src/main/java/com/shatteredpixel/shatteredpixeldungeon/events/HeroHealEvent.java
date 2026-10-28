package com.shatteredpixel.shatteredpixeldungeon.events;

/**
 * 英雄恢复生命事件。
 * <p>
 * 在英雄生命值恢复时发布，覆盖自然恢复（Regeneration）、持续治疗（Healing）、
 * 日灼草治疗（Sungrass）等各类治疗来源。{@link #getHealAmount()} 为本次实际恢复的生命值。
 */
public class HeroHealEvent extends GameEvent {

    private final int healAmount;

    public HeroHealEvent(int healAmount) {
        this.healAmount = healAmount;
    }

    /** 本次实际恢复的生命值。 */
    public int getHealAmount() { return healAmount; }
}
