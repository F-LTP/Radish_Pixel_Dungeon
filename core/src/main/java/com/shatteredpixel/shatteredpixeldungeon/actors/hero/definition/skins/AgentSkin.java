package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AgentSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;

public class AgentSkin extends SkinDefinition {
	public AgentSkin(HeroDefinition base) { super(base); }
	@Override public int skinIndex() { return HeroClasses.ROGUE_SKIN_AGENT; }
	@Override public String skinName() { return "AGENT"; }
	@Override public boolean customSprite() { return true; }
	@Override public String asset() { return Assets.Sprites.AGENT; }
	@Override public Class<? extends HeroSprite> spriteClass() { return AgentSprite.class; }
	@Override public int frameW() { return 12; }
	@Override public int frameH() { return 16; }
	@Override public int[] idleFrames() { return new int[]{0, 1, 0, 2, 3, 4, 3, 2}; }
}
