package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroActEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class WeaponMasteryTalent {

	public static class MasteryTracker extends Buff {
		{
			type = buffType.POSITIVE;
		}

		public int turnCount;
		private KindOfWeapon trackedWeapon;

		@Override
		public String icon() { return BuffIndicator.WEAPON; }

		@Override
		public void tintIcon(Image icon) {
			int stacks = getStacks();
			float brightness = stacks / 5f;
			icon.hardlight(1f, 0.5f + brightness * 0.5f, brightness);
		}

		@Override
		public String desc() {
			int stacks = getStacks();
			if (stacks >= 5) {
				return Messages.get(this, "desc_max", stacks);
			}
			int threshold = getThreshold();
			int turnsToNext = (stacks + 1) * threshold - turnCount;
			return Messages.get(this, "desc", stacks, turnsToNext, threshold);
		}

		private int getThreshold() {
			return ((Hero) target).pointsInTalent(Talent.WEAPON_MASTERY) == 1 ? 125 : 100;
		}

		public int getStacks() {
			return Math.min(turnCount / getThreshold(), 5);
		}

		public int getBonusDamage() {
			if (((Hero) target).belongings.weapon() == null) return 0;
			int stacks = getStacks();
			return stacks > 0 ? Random.Int(stacks + 1) : 0;
		}

		@Override
		public void modifyPreFinalOutgoingAttackDamage(Char attacker, Char defender, DamageInfo info) {
			if (!(attacker instanceof Hero)) return;

			Hero hero = (Hero) attacker;
			if (hero.heroClass != HeroClasses.MOONLIGHT || !hero.hasTalent(Talent.WEAPON_MASTERY)) return;
			if (!(hero.belongings.weapon instanceof MeleeWeapon)) return;
			if (info.getSourceItem() != hero.belongings.weapon) return;

			int bonus = getBonusDamage();
			if (bonus > 0) {
				if (turnCount >= 500) ((HeroSprite) hero.sprite).updateArmor();
				info.addPreFinalAddModifier(bonus, "weapon mastery", this);
			}
		}

		public void reset() { turnCount = 0; }

		public void setTrackedWeapon(KindOfWeapon weapon) {
			trackedWeapon = weapon;
		}

		public KindOfWeapon getTrackedWeapon() {
			return trackedWeapon;
		}

		@Override
		public boolean act() {
			turnCount++;
			spend(TICK);
			return true;
		}

		private static final String TURN_COUNT = "turn_count";
		private static final String TRACKED_WEAPON = "tracked_weapon";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TURN_COUNT, turnCount);
			bundle.put(TRACKED_WEAPON, trackedWeapon);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			turnCount = bundle.getInt(TURN_COUNT);
			trackedWeapon = (KindOfWeapon) bundle.get(TRACKED_WEAPON);
		}
	}

	@SubscribeEvent(event = HeroActEvent.class, priority = 0)
	public static void onHeroAct(HeroActEvent event) {
		Hero hero = event.getHero();

		if (hero.heroClass != HeroClasses.MOONLIGHT || !hero.hasTalent(Talent.WEAPON_MASTERY)) return;

		KindOfWeapon weapon = hero.belongings.weapon();

		// 初始化 Buff
		MasteryTracker tracker = Buff.affect(hero, MasteryTracker.class);
		if (tracker.getTrackedWeapon() == null) {
			tracker.trackedWeapon = weapon;
		}
		// 这里直接检测相等不行， 因为武器的equals方法没定义好,所以检测武器类型 倒也算是合理
		if (weapon != null && weapon.getClass() != tracker.getTrackedWeapon().getClass()) {
			tracker.reset();
			tracker.setTrackedWeapon(weapon);
			((HeroSprite) hero.sprite).updateArmor();
		}
	}
}
