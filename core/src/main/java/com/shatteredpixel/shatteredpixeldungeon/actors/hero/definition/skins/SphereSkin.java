package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SphereSprite;

/**
 * 圆球皮肤 - 月华 MOONLIGHT 的可选皮肤变体。
 * <p>共享月华的转职（小骑士/骰子法师/术打冠军）与天赋，仅替换外观精灵为圆球。</p>
 */
public class SphereSkin extends SkinDefinition {

	public static final int INDEX = HeroClasses.MOONLIGHT_SKIN_SPHERE;

	public SphereSkin(HeroDefinition base) {
		super(base);
	}

	@Override public int skinIndex() { return INDEX; }

	@Override public String skinName() { return "SPHERE"; }

	@Override public boolean customSprite() { return true; }

	@Override public String asset() { return Assets.Sprites.SPHERE; }

	@Override public Class<? extends HeroSprite> spriteClass() { return SphereSprite.class; }

	@Override public int frameW() { return 12; }
	@Override public int frameH() { return 16; }

	@Override public int[] idleFrames() { return new int[]{ 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4 }; }
}
