package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WandererSprite;

/**
 * 流浪者皮肤 - 战士的可选皮肤变体。
 * <p>共享战士的转职（狂战士/角斗士）与天赋，仅替换外观精灵；若有需要可覆盖
 * {@code initClassLoadout} 提供不同的初始物品。</p>
 */
public class WandererSkin extends SkinDefinition {

	public static final int INDEX = HeroClasses.WARRIOR_SKIN_WANDERER;

	public WandererSkin(HeroDefinition base) {
		super(base);
	}

	@Override public int skinIndex() { return INDEX; }

	@Override public String skinName() { return "WANDERER"; }

	@Override public boolean customSprite() { return true; }

	@Override public String asset() { return Assets.Sprites.WANDERER; }

	@Override public Class<? extends HeroSprite> spriteClass() { return WandererSprite.class; }

	@Override public int frameW() { return 12; }
	@Override public int frameH() { return 16; }

	@Override public int[] idleFrames() { return new int[]{ 0, 1, 0, 2, 3, 4, 3, 2 }; }
}
