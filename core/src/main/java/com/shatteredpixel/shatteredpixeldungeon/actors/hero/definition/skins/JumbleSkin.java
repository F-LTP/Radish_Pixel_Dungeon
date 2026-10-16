package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.JumbleSprite;

/**
 * 杂散皮肤 - 所有职业共享的可选皮肤变体。
 * <p>每组贴图与职业无关，杂散会随时间在 6 组形态间自动变身。</p>
 */
public class JumbleSkin extends SkinDefinition {

	public static final int INDEX = HeroClasses.JUMBLE;

	public JumbleSkin(HeroDefinition base) {
		super(base);
	}

	@Override public int skinIndex() { return INDEX; }

	@Override public String skinName() { return "JUMBLE"; }

	@Override public boolean customSprite() { return true; }

	@Override public String asset() { return Assets.Sprites.JUMBLE; }

	@Override public Class<? extends HeroSprite> spriteClass() { return JumbleSprite.class; }

	@Override public int frameW() { return 12; }
	@Override public int frameH() { return 16; }

	@Override public int[] idleFrames() { return new int[]{ 0, 0, 0, 1, 0, 0, 1, 1 }; }
}
