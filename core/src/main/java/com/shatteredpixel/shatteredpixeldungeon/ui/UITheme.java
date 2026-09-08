package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;

/** Central access point for runtime UI skin selection. */
public final class UITheme {

    public static final int DICE_FILL = 0x09070B;
    public static final int DICE_LINE = 0xC45E16;
    public static final int DICE_PRESSED_LINE = 0xB59E09;

    private UITheme() {
    }

    public static boolean isDiceMage() {
        return Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClasses.DICE_MAGE;
    }

    public static RoundedFrame roundedFrame(int fillColor, int lineColor) {
        return new RoundedFrame(fillColor, lineColor);
    }
}
