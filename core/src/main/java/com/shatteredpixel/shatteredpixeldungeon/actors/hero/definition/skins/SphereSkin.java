package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.utils.Random;
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

	/** 圆球皮肤：近战（非投掷）命中时，16% 概率使目标麻痹 2 回合。 */
	@SubscribeEvent(event = AttackHitEvent.class, priority = -200)
	public static void onAttackHit(AttackHitEvent event) {
		Char attacker = event.getAttacker();
		Char defender = event.getDefender();
		if (!(attacker instanceof Hero)) return;
		if (!((Hero) attacker).isSphereSkin()) return;
		if (event.getAttackerWeapon() instanceof MissileWeapon) return;
		if (defender == null || !defender.isAlive()) return;
		if (Random.Float() < 0.16f) {
			Buff.affect(defender, Paralysis.class, 2f);
		}
	}
}
