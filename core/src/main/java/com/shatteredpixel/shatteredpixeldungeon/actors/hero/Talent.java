/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PhysicalEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.events.EventManager;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroEatFoodEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.talentitem.HerbMaker;
import com.shatteredpixel.shatteredpixeldungeon.items.talentitem.SpellQueue;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Gloves;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;

public enum Talent {

	/**
	 * [WARRIOR TALENT]
 	 */
	//Warrior T1
	HEARTY_MEAL, ARMSMASTERS_INTUITION, PROVOKED_ANGER, IRON_WILL,
	//Warrior T2
	IRON_STOMACH, EMERGENCY_PROTECTION, RUNIC_TRANSFERENCE, LETHAL_MOMENTUM, IMPROVISED_PROJECTILES,
	//Warrior T3
	HOLD_FAST(3), STRONGMAN(3),
	//Warrior T4
	IRON_MUSCLE(4),MOVING_DEFENSE(4),
	HIGH_DIET(4), // 战士4-2重做：高端饮食
	//Berserker T3
	ENDLESS_RAGE(3), PAIN_SCAR(3), FANATICISM_MAGIC(3),
	//Berserker T4
	REVENGE_ROAR(4),THIRSTY_BLADE(4),
	//Gladiator T3
	KEEP_VIGILANCE(3), LETHAL_DEFENSE(3), VENT_NOPLACE(3),
	//Gladiator T4
	DEFENSIVE_STRIKE(4),DEVASTATE(4),
	WEAPON_MASTER(4), // 角斗士4-4重做：武器大师
	//Heroic Leap T4
	BODY_SLAM(4), IMPACT_WAVE(4), DOUBLE_JUMP(4),
	//Shockwave T4
	EXPANDING_WAVE(4), STRIKING_WAVE(4), SHOCK_FORCE(4),
	//Endure T4
	SUSTAINED_RETRIBUTION(4), SHRUG_IT_OFF(4), EVEN_THE_ODDS(4),

	/**
	 * [MAGE TALENT]
	 */
	//Mage T1
	EMPOWERING_MEAL, SCHOLARS_INTUITION, LINGERING_MAGIC, BACKUP_BARRIER,
	//Mage T2
	ENERGIZING_MEAL, ENERGIZING_UPGRADE, WAND_PRESERVATION, ARCANE_VISION, SHIELD_BATTERY,
	//Mage T3
	SPELL_QUEUE(3), ALLY_WARP(3),
	//TODO WARMAGE T4
	MAGIC_REFINING(4),MAGIC_TACTICS(4),MAGIC_STICK(4),MAGIC_WORKMAN(4),
	WAND_DODGE(4), // 战法4-3：老魔杖闪避
	//TODO MAGIC T4
	DESPERATE_POWER(4),GHOST_ROOT(4),
	CORRUPT_SPIRIT(4), // 术士4-4：腐化怨灵
	//Battlemage T3
	EMPOWERED_STRIKE(3), MYSTICAL_CHARGE(3), WAR_THROW(3),
	//Warlock T3
	SOUL_EATER(3), SOUL_SIPHON(3), NECROMANCERS_MINIONS(3),
	//Elemental Blast T4
	BLAST_RADIUS(4), ELEMENTAL_POWER(4), REACTIVE_BARRIER(4),
	//Wild Magic T4
	WILD_POWER(4), FIRE_EVERYTHING(4), CONSERVED_MAGIC(4),
	//Warp Beacon T4
	TELEFRAG(4), REMOTE_BEACON(4), LONGRANGE_WARP(4),

	/**
	 * [ROUGE TALENT]
	 */
	//Rogue T1
	CACHED_RATIONS, THIEFS_INTUITION, SUCKER_PUNCH, PROTECTIVE_SHADOWS,
	//Rogue T2
	MYSTICAL_MEAL, DUEL_DANCE, WIDE_SEARCH, SILENT_STEPS, ROGUES_INSTINCT,
	//Rogue T3
	DEATHBLOW(3), LIGHT_CLOAK(3),
	//Rogue T4
	HIDE_IN_CROWD(4),DARK_ARMOR(4),
	//Assassin T3
	ENHANCED_LETHALITY(3), ASSASSINS_REACH(3), BOUNTY_HUNTER(3),
	//Assassin T4
	BRACE_YOURSELF(4),POWER_RECYCLE(4),
	//Freerunner T3
	EVASIVE_ARMOR(3), PROJECTILE_MOMENTUM(3), SPEEDY_STEALTH(3),
	//Freerunner T4
	KINETIC_ENERGY(4),STORM_RUSH(4),
	//Smoke Bomb T4
	HASTY_RETREAT(4), BODY_REPLACEMENT(4), SHADOW_STEP(4),
	//Death Mark T4
	FEAR_THE_REAPER(4), DEATHLY_DURABILITY(4), DOUBLE_MARK(4),
	//Shadow Clone T4
	SHADOW_BLADE(4), CLONED_ARMOR(4), PERFECT_COPY(4),

	/**
	 * [HUNTRESS TALENT]
	 */
	//Huntress T1
	NATURES_BOUNTY, SURVIVALISTS_INTUITION, FOLLOWUP_STRIKE, UNDERESTIMATED,
	//Huntress T2
	INVIGORATING_MEAL, HERB_MIXTURE, REJUVENATING_STEPS, HEIGHTENED_SENSES, DURABLE_PROJECTILES,
	//Huntress T3
	HOLD_BREATH(3), SEER_SHOT(3),
	//Huntress T4
	BRISK_PACE(4),PHASE_FILLING(4),
	BOW_DULES(4),
	STORM_ATTACK(4),
	MEDART_SPECIALIST(4),LAND_HEART(4),
	COMMON_SHOT(4), // 狙击4-3重做：通识射击
	MORE_DARTS(4),GRASS_VISION(4), // 守望4-3/4-4
	//Sniper T3
	FARSIGHT(3), SHARED_ENCHANTMENT(3), SHARED_UPGRADES(3),
	//Warden T3
	DURABLE_TIPS(3), BARKSKIN(3), VINE_TRAP(3),
	//Spectral Blades T4
	FAN_OF_BLADES(4), PROJECTING_BLADES(4), SPIRIT_BLADES(4),
	//Natures Power T4
	GROWING_POWER(4), NATURES_WRATH(4), WILD_MOMENTUM(4),
	//Spirit Hawk T4
	EAGLE_EYE(4), GO_FOR_THE_EYES(4), SWIFT_SPIRIT(4),
	//universal T4
	HEROIC_ENERGY(4), //See icon() and title() for special logic for this one
	//Ratmogrify T4
	RATSISTANCE(4), RATLOMACY(4), RATFORCEMENTS(4),

	/**
	 * [DUELIST TALENT]
	 */
	//Duelist T1
	STRENGTHENING_MEAL, ADVENTURERS_INTUITION, PATIENT_STRIKE, AGGRESSIVE_BARRIER,
	//Duelist T2
	FOCUSED_MEAL, LIQUID_AGILITY, WEAPON_RECHARGING, LETHAL_HASTE, SWIFT_EQUIP,
	//Duelist T3
	PRECISE_ASSAULT(3), DEADLY_FOLLOWUP(3),
	//Champion T3
	VARIED_CHARGE(3), TWIN_UPGRADES(3), COMBINED_LETHALITY(3),
	//Monk T3
	UNENCUMBERED_SPIRIT(3), MONASTIC_VIGOR(3), COMBINED_ENERGY(3),
	//Challenge T4
	CLOSE_THE_GAP(4), INVIGORATING_VICTORY(4), ELIMINATION_MATCH(4),
	//Elemental Strike T4
	ELEMENTAL_REACH(4), STRIKING_FORCE(4), DIRECTED_POWER(4),
	//Duelist A3 T4
	FEIGNED_RETREAT(4), EXPOSE_WEAKNESS(4), COUNTER_ABILITY(4),

	/**
	 * [RECTOR TALENT]
	 */
	PRAYER_BEFORE_MEALS,MENTAL_TELEPATHY, RAIN_GRACE,DEVOTIONAL,

	BLESS_FOOD,SOUL_NOWIFI,LIGHT_STEP,GOD_BODY,NOHOPE_LANG,

	//T3牧师通用
	ACT_GODPROGRESS(3),SMART_BLESSING(3),

	//BATTLE RECTOR
	IRON_SUN(3),PHARCIS_BLESS(3),BEN_WORK(3),

	//RED MASTER
	FIRE_GLASS(3),LIGHT_WASH(3),SKY_TOWER(3),

	//DEAD DIFE
	BLACK_LOVE(3),DEAD_POWER(3),EXP_IMPOTION(3),

	// T4 rector universal
	// T4 牧师通用(星界沟通，生命坚壁)
	SUPERSTITION(4),VITAE_BOOST(4),

	// T4 战斗牧师(战斗兴奋,神赐之礼)
	// T4 BATTLE RECTOR
	ADRENAL_COMBAT(4),GIFT(4),


	// T4 红衣主教(圣化转变,圣光障壁)
	// T4 Cardinal
	SOUL_POSSESSION(4),BLOODY_VITAE(4),

	// T4 执行者(无情扫除,信仰收割)
	// T4 DEAD DIFE
	PRESS_ON(4),BRIEF_HARVEST(4),

	// T4 rector
	// last prayer
	// 高效回复	借力疾驰	  圣灵赐福
	EFFICIENT_HEALING(4), INERTIAL_CHARGE(4),BLESS_RETURN(4),

	// T4 rector
	// shadow hymn
	// 暗色契约	潜心苦读	双修牧师
	SACRIFICE(4),BLOCKING_READING(4),TAI_CHI_POISE(4),

	// T4 Rector
	// gods possession
	// 治愈圣启	无可侵犯	神灵之触
	HOLY_SHOCKWAVE(4),GODHOOD(4),AVATAR(4),

	/**
	 * [MOONLIGHT TALENT]
	 */
	//Moonlight T1
	// 猎杀直觉 砥砺锋芒 武器掌握 战争践踏
	HUNTING_INTUITION, SHARPENING_EDGE, WEAPON_MASTERY, WAR_TRAMPLE,
	//Moonlight T2
	// 利用一餐 强壮肉体 神圣泉水 三重保险 弹射起步
	MEAL_UTILIZATION, STRONG_BODY, HOLY_SPRING, TRIPLE_INSURANCE, CATAPULT_START,
	//Moonlight T3 (Universal)
	// 剑盾骑士 轮椅翻车
	SWORD_SHIELD_KNIGHT(3), WHEELCHAIR_CRASH(3),
	//Moonlight T4 (Universal)

	HEROIC_ENERGY_MOONLIGHT(4),
	LIGHT_ETERNITY(4), MOON_GLORY(4), // 月华4-1/4-2

	// Little Knight T3
	// 我不会输 濡湿附魔 左弓连射
	WONT_LOSE(3), WET_ENCHANT(3), LEFT_BOW_RAPID(3),
	SHIELD_POKE(4), KNIGHT_SPIRIT(4), // 小骑士4-3/4-4

	//Dice Mage T3
	LEARN_SOOTHE(3), LEARN_LIQUOR(3), LEARN_OPERATE(3), LEARN_MIASMA(3), LEARN_CRUSH(3), LEARN_BLAZE(3),
	SPELL_EMPOWER(4), EGG_BASKET(4), // 骰子法师4-3/4-4

	//Jutte Champion T3
	ONE_JUTTE(3), IRON_QUENCH(3), SURPRISE_JUTTE(3),

	//注定一抽 T4
	FATED_TWICE(4), LOOT_GROUND(4), TIME_PAUSE(4),

	//玩具背包 T4
	BETTER_ITEM(4), EXTRA_POCKET(4), ACCEPT_CHALLENGE(4),

	//薪王化身 T4
	HOLY_LANCE(4), SOUL_STREAM(4), FATAL_BLADE(4),

	ERROR;


	public static class MagicRootDropped extends CounterBuff{{revivePersists = true;}};

	public static class ImprovisedProjectileCooldown extends FlavourBuff{
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
	};

	public static class NoBeliefUsedCooldown extends FlavourBuff{
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.75f, 0f, 0f); }
	};

	public static class HideInCrowdCooldown extends FlavourBuff{
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0x5562F6); }
	};

	public static class SlowHealingDeadCooldown extends FlavourBuff{
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0f, 0.55f, 0f); }
	};

	public static class ThirstyBladeCooldown extends FlavourBuff{
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.8f, 0f, 0f); }
	};

	public static class Rain_Grace_Cooldown extends FlavourBuff{
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0f, 0.6f, 0f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
	};

	public static class LethalMomentumTracker extends FlavourBuff{};
	public static class StrikingWaveTracker extends FlavourBuff{};
	public static class WandPreservationCounter extends CounterBuff{{revivePersists = true;}};
	public static class EmpoweredStrikeTracker extends FlavourBuff{
		//blast wave on-hit doesn't resolve instantly, so we delay detaching for it
		public boolean delayedDetach = false;
	};
	public static class ProtectiveShadowsTracker extends Buff {
		float barrierInc = 0.5f;

		@Override
		public boolean act() {
			//barrier every 2/1 turns, to a max of 3/5
			if (((Hero)target).hasTalent(Talent.PROTECTIVE_SHADOWS) && target.invisible > 0){
				Barrier barrier = Buff.affect(target, Barrier.class);
				if (barrier.shielding() < 1 + 2*((Hero)target).pointsInTalent(Talent.PROTECTIVE_SHADOWS)) {
					barrierInc += 0.5f * ((Hero) target).pointsInTalent(Talent.PROTECTIVE_SHADOWS);
				}
				if (barrierInc >= 1){
					barrierInc = 0;
					barrier.incShield(1);
				} else {
					barrier.incShield(0); //resets barrier decay
				}
			} else {
				detach();
			}
			spend( TICK );
			return true;
		}

		private static final String BARRIER_INC = "barrier_inc";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( BARRIER_INC, barrierInc);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			barrierInc = bundle.getFloat( BARRIER_INC );
		}
	}
	public static class BountyHunterTracker extends FlavourBuff{};
	public static class RejuvenatingStepsCooldown extends FlavourBuff{
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0f, 0.35f, 0.15f); }
		public float iconFadePercent() { return GameMath.gate(0, visualcooldown() / (15 - 5*Dungeon.hero.pointsInTalent(REJUVENATING_STEPS)), 1); }
	};
	public static class RejuvenatingStepsFurrow extends CounterBuff{{revivePersists = true;}};
	public static class SeerShotCooldown extends FlavourBuff{
		public String icon() { return target.buff(RevealedArea.class) != null ? BuffIndicator.NONE : BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.7f, 0.4f, 0.7f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 20); }
	};
	public static class SpiritBladesTracker extends FlavourBuff{};
	public static class PatientStrikeTracker extends Buff {
		public int pos;
		{ type = Buff.buffType.POSITIVE; }
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.5f, 0f, 1f); }
		@Override
		public boolean act() {
			if (pos != target.pos) {
				detach();
			} else {
				spend(TICK);
			}
			return true;
		}
		private static final String POS = "pos";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(POS, pos);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			pos = bundle.getInt(POS);
		}
	};
	public static class AggressiveBarrierCooldown extends FlavourBuff{
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.35f, 0f, 0.7f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
	};

	public static class PowerRecycleTracker extends FlavourBuff{};

	public static class RestoredAgilityTracker extends FlavourBuff{};
	public static class LethalHasteCooldown extends FlavourBuff{
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.35f, 0f, 0.7f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 100); }
	};
	public static class SwiftEquipCooldown extends FlavourBuff{
		public boolean secondUse;
		public boolean hasSecondUse(){
			return secondUse && cooldown() > 14f;
		}

		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) {
			if (hasSecondUse()) icon.hardlight(0.85f, 0f, 1.0f);
			else                icon.hardlight(0.35f, 0f, 0.7f);
		}
		public float iconFadePercent() { return GameMath.gate(0, visualcooldown() / 20f, 1); }

		private static final String SECOND_USE = "second_use";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(SECOND_USE, secondUse);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			secondUse = bundle.getBoolean(SECOND_USE);
		}
	};
	public static class DeadlyFollowupTracker extends FlavourBuff{
		public int object;
		{ type = Buff.buffType.POSITIVE; }
		public String icon() { return BuffIndicator.INVERT_MARK; }
		public void tintIcon(Image icon) { icon.hardlight(0.5f, 0f, 1f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
		private static final String OBJECT    = "object";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(OBJECT, object);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			object = bundle.getInt(OBJECT);
		}
	}
	public static class PreciseAssaultTracker extends FlavourBuff{
		{ type = buffType.POSITIVE; }
		public String icon() { return BuffIndicator.INVERT_MARK; }
		public void tintIcon(Image icon) { icon.hardlight(1f, 1f, 0.0f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
	};
	public static class VariedChargeTracker extends Buff{
		public Class weapon;

		private static final String WEAPON    = "weapon";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(WEAPON, weapon);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			weapon = bundle.getClass(WEAPON);
		}
	}
	public static class CombinedLethalityAbilityTracker extends FlavourBuff{
		public MeleeWeapon weapon;
	};
	public static class CombinedLethalityTriggerTracker extends FlavourBuff{
		{ type = buffType.POSITIVE; }
		public String icon() { return BuffIndicator.CORRUPT; }
		public void tintIcon(Image icon) { icon.hardlight(0.6f, 0.15f, 0.6f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
	};
	public static class CombinedEnergyAbilityTracker extends FlavourBuff{
		public int energySpent = -1;
		public boolean wepAbilUsed = false;
	}
	public static class CounterAbilityTacker extends FlavourBuff{};

	public static class HIGHGRSS_SPEED extends FlavourBuff{
		public String icon() { return BuffIndicator.HASTE;}
		public void tintIcon(Image icon) { icon.hardlight(0xFfa500);
		}
	};
	int maxPoints;

	// tiers 1/2/3/4 start at levels 2/7/13/21
	public static int[] tierLevelThresholds = new int[]{0, 2, 7, 13, 21, 31};

	Talent(){
		this(2);
	}

	Talent( int maxPoints ){
		this.maxPoints = maxPoints;
	}

	public String icon(){
		if (this == HEROIC_ENERGY){
			if (Ratmogrify.useRatroicEnergy){
				return "heroic_energy_rat";
			}
			HeroClass cls = Dungeon.hero != null ? Dungeon.hero.heroClass : GamesInProgress.selectedClass;
			switch (cls){
				case WARRIOR: default:
					return "heroic_energy_warrior";
				case MAGE:
					return "heroic_energy_mage";
				case ROGUE:
					return "heroic_energy_rogue";
				case HUNTRESS:
					return "heroic_energy_huntress";
			}
		} else {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	public int maxPoints(){
		return maxPoints;
	}

	public String title(){
		if (this == HEROIC_ENERGY && Ratmogrify.useRatroicEnergy){
			return Messages.get(this, name() + ".rat_title");
		}
		return Messages.get(this, name() + ".title");
	}

	public final String desc(){
		return desc(false);
	}

	public String desc(boolean metamorphed){
		if (metamorphed){
			String metaDesc = Messages.get(this, name() + ".meta_desc");
			if (!metaDesc.equals(Messages.NO_TEXT_FOUND)){
				return Messages.get(this, name() + ".desc") + "\n\n" + metaDesc;
			}
		}
		return Messages.get(this, name() + ".desc");
	}

	public static void onTalentUpgraded( Hero hero, Talent talent ){

		// Superstition by DoggingDog on 20250817
		// 天赋：星界沟通
		if(talent == SUPERSTITION){
			Dungeon.hero.superstitionCounter = new Hero.SuperstitionCounter();
		}
		//

		if (talent == HERB_MIXTURE  &&hero.belongings.getItem(HerbMaker.class)==null){
			Dungeon.level.drop(new HerbMaker(),Dungeon.hero.pos);
		}

		if (talent == HOLD_BREATH){
			Buff.affect(hero, HoldBreathTracker.class);
		}
		if (talent == SPELL_QUEUE){
			if (hero.belongings.getItem(SpellQueue.class)==null && hero.buff(SpellQueue.imageListner.class)==null){
				Dungeon.level.drop(new SpellQueue(),Dungeon.hero.pos);
				Buff.affect(hero, SpellQueue.imageListner.class);
			}
			SpellQueue mySq= hero.belongings.getItem(SpellQueue.class);
			if (mySq!=null){
				mySq.updateImage();
			}
		}

		if (talent == ARMSMASTERS_INTUITION && hero.pointsInTalent(ARMSMASTERS_INTUITION) == 2){
			if (hero.belongings.weapon() != null && !ShardOfOblivion.passiveIDDisabled()){
				hero.belongings.weapon().identify();
			}
			if (hero.belongings.armor() != null&& !ShardOfOblivion.passiveIDDisabled()){
				hero.belongings.armor.identify();
			}
		}



		if (talent == THIEFS_INTUITION && hero.pointsInTalent(THIEFS_INTUITION) == 2){
			if (hero.belongings.ring instanceof Ring && !ShardOfOblivion.passiveIDDisabled()) hero.belongings.ring.identify();
			if (hero.belongings.misc instanceof Ring && !ShardOfOblivion.passiveIDDisabled()) hero.belongings.misc.identify();
			for (Item item : Dungeon.hero.belongings){
				if (item instanceof Ring){
					((Ring) item).setKnown();
				}
			}
		}
		if (talent == THIEFS_INTUITION && hero.pointsInTalent(THIEFS_INTUITION) == 1){
			if (hero.belongings.ring instanceof Ring && !ShardOfOblivion.passiveIDDisabled()) hero.belongings.ring.setKnown();
			if (hero.belongings.misc instanceof Ring && !ShardOfOblivion.passiveIDDisabled()) ((Ring) hero.belongings.misc).setKnown();
		}
		if (talent == ADVENTURERS_INTUITION && hero.pointsInTalent(ADVENTURERS_INTUITION) == 2 && !ShardOfOblivion.passiveIDDisabled()){
			if (hero.belongings.weapon() != null) hero.belongings.weapon().identify();
		}

		if (talent == PROTECTIVE_SHADOWS && hero.invisible > 0){
			Buff.affect(hero, Talent.ProtectiveShadowsTracker.class);
		}

		if (talent == LIGHT_CLOAK && hero.heroClass == HeroClass.ROGUE){
			for (Item item : Dungeon.hero.belongings.backpack){
				if (item instanceof CloakOfShadows){
					if (!hero.belongings.lostInventory() || item.keptThroughLostInventory()) {
						((CloakOfShadows) item).activate(Dungeon.hero);
					}
				}
			}
		}

		if (talent == HEIGHTENED_SENSES || talent == FARSIGHT){
			Dungeon.observe();
		}

		if (talent == UNENCUMBERED_SPIRIT && hero.pointsInTalent(talent) == 3){
			Item toGive = new ClothArmor().identify();
			if (!toGive.collect()){
				Dungeon.level.drop(toGive, hero.pos).sprite.drop();
			}
			toGive = new Gloves().identify();
			if (!toGive.collect()){
				Dungeon.level.drop(toGive, hero.pos).sprite.drop();
			}
		}
	}

	public static class CachedRationsDropped extends CounterBuff{{revivePersists = true;}};
	public static class NatureBerriesDropped extends CounterBuff{{revivePersists = true;}};

	public static void onFoodEaten( Hero hero, float foodVal, Item foodSource ){
		EventManager.emit(new HeroEatFoodEvent(hero, foodVal, foodSource));
		if (hero.hasTalent(HEARTY_MEAL)){
			//3/5 HP healed, when hero is below 25% health
			if (hero.HP <= hero.HT/4) {
				hero.HP = Math.min(hero.HP + 1 + 2 * hero.pointsInTalent(HEARTY_MEAL), hero.HT);
				hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1+hero.pointsInTalent(HEARTY_MEAL));
				//2/3 HP healed, when hero is below 50% health
			} else if (hero.HP <= hero.HT/2){
				hero.HP = Math.min(hero.HP + 1 + hero.pointsInTalent(HEARTY_MEAL), hero.HT);
				hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), hero.pointsInTalent(HEARTY_MEAL));
			}
		}

		//餐前祈祷
		if (hero.hasTalent(PRAYER_BEFORE_MEALS)){
			Belief belief = Dungeon.hero.buff(Belief.class);
			if(belief != null){
				belief.getBelief(hero.pointsInTalent(PRAYER_BEFORE_MEALS));
			}
		}

		if (hero.hasTalent(BLESS_FOOD)){
			switch (Dungeon.hero.pointsInTalent(BLESS_FOOD)){
				case 1:
					Buff.affect(hero, Bless.class, 12f);
					break;
				case 2:
					Buff.affect(hero, Bless.class, 20f);
					break;
			}
		}

		if (hero.hasTalent(IRON_STOMACH)){
			if (hero.cooldown() > 0) {
				Buff.affect(hero, WarriorFoodImmunity.class, hero.cooldown());
			}
		}
		if (hero.hasTalent(EMPOWERING_MEAL)){
			//2/3 bonus wand damage for next 3 zaps
			Buff.affect( hero, WandEmpower.class).set(1 + hero.pointsInTalent(EMPOWERING_MEAL), 3);
			ScrollOfRecharging.charge( hero );
		}
		if (hero.hasTalent(ENERGIZING_MEAL)){
			//5/8 turns of recharging
			Buff.prolong( hero, Recharging.class, 2 + 3*(hero.pointsInTalent(ENERGIZING_MEAL)) );
			ScrollOfRecharging.charge( hero );
			SpellSprite.show(hero, SpellSprite.CHARGE);
		}
		if (hero.hasTalent(MYSTICAL_MEAL)){
			//3/5 turns of recharging
			ArtifactRecharge buff = Buff.affect( hero, ArtifactRecharge.class);
			if (buff.left() < 1 + 2*(hero.pointsInTalent(MYSTICAL_MEAL))){
				Buff.affect( hero, ArtifactRecharge.class).set(1 + 2*(hero.pointsInTalent(MYSTICAL_MEAL))).ignoreHornOfPlenty = foodSource instanceof HornOfPlenty;
			}
			ScrollOfRecharging.charge( hero );
			SpellSprite.show(hero, SpellSprite.CHARGE, 0, 1, 1);
		}
		if (hero.hasTalent(INVIGORATING_MEAL)){
			//effectively 1/2 turns of haste
			Buff.prolong( hero, Haste.class, 0.67f+hero.pointsInTalent(INVIGORATING_MEAL));
		}
		if (hero.hasTalent(STRENGTHENING_MEAL)){
			//3 bonus physical damage for next 2/3 attacks
			Buff.affect( hero, PhysicalEmpower.class).set(3, 1 + hero.pointsInTalent(STRENGTHENING_MEAL));
		}
		if (hero.hasTalent(FOCUSED_MEAL)){
				// lvl/3 / lvl/2 bonus dmg on next hit for other classes
				Buff.affect( hero, PhysicalEmpower.class).set(Math.round(hero.lvl / (4f - hero.pointsInTalent(FOCUSED_MEAL))), 1);
		}
	}

	public static class WarriorFoodImmunity extends FlavourBuff{
		{ actPriority = HERO_PRIO+1; }
	}

	public static float itemIDSpeedFactor( Hero hero, Item item ){
		// 1.75x/2.5x speed with Huntress talent
		float factor = 1f + 0.75f*hero.pointsInTalent(SURVIVALISTS_INTUITION);
		// 2x/instant for Warrior (see onItemEquipped)
		if (item instanceof MeleeWeapon || item instanceof Armor){
			factor *= 1f + hero.pointsInTalent(ARMSMASTERS_INTUITION);
		}
		// 3x/instant for Mage (see Wand.wandUsed())
		if (item instanceof Wand){
			factor *= 1f + 2.0f*hero.pointsInTalent(SCHOLARS_INTUITION);
		}
		// 2x/instant for Rogue (see onItemEqupped), also id's type on equip/on pickup
		if (item instanceof Ring){
			factor *= 1f + hero.pointsInTalent(THIEFS_INTUITION);
		}
		return factor;
	}

	public static void onPotionUsed( Hero hero, int cell, float factor ){

		if (hero.hasTalent(LIQUID_AGILITY)){
			Buff.prolong(hero, RestoredAgilityTracker.class, hero.cooldown() + Math.max(0, factor-1));
		}
	}

	public static void onScrollUsed( Hero hero, int pos, float factor ){

	}

	public static void onUpgradeScrollUsed( Hero hero ){
	}

	public static class HoldBreathTracker extends Buff{
		{
			actPriority=HERO_PRIO+1;
			type=buffType.POSITIVE;
		}

		@Override
		public String icon() {
			return BuffIndicator.HOLD_BREATH;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", crit_b,new DecimalFormat("#.###").format(cd_b));
		}

		public int crit_b=0;
		public float cd_b=0f;
		public boolean canreduce=true;

		public void reduce(){
			if (!canreduce) return;
			crit_b=Math.max(0,crit_b-(1+((Hero)target).pointsInTalent(HOLD_BREATH)));
			cd_b=Math.max(0,cd_b-(1+((Hero)target).pointsInTalent(HOLD_BREATH))*0.005f);
			canreduce=false;
		}
		public void clear_cb(){
			crit_b=0;
			cd_b=0;
		}
		@Override
		public void storeInBundle( Bundle bundle ) {
			bundle.put("CRIT_B_HB",crit_b);
			bundle.put("CD_B_HB",cd_b);
			bundle.put("CAN_REDUCE",canreduce);
			super.storeInBundle(bundle);
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle(bundle);
			if (bundle.contains("CRIT_B_HB"))
				crit_b = bundle.getInt("CRIT_B_HB");
			else
				crit_b =0;
			if (bundle.contains("CD_B_HB"))
				cd_b = bundle.getFloat("CD_B_HB");
			else
				cd_b =0;
			if (bundle.contains("CAN_REDUCE"))
				canreduce=bundle.getBoolean("CAN_REDUCE");
			else
				canreduce=true;
		}
		@Override
		public boolean act(){
			if (!((Hero)target).hasTalent(Talent.HOLD_BREATH)) detach();
			canreduce=true;
			crit_b=Math.min(100,crit_b+(1+((Hero)target).pointsInTalent(HOLD_BREATH)));
			cd_b=Math.min(3,cd_b+(1+((Hero)target).pointsInTalent(HOLD_BREATH))*0.005f);
			spend(TICK);
			return true;
		}
	}

	public static void RectorGetIdentify(Hero hero,Item item){
		switch (hero.pointsInTalent(MENTAL_TELEPATHY)){
			case 1:
				if(!item.cursed){
					if((item instanceof Weapon || item instanceof Armor)){
						item.identify();
					}
				}
				break;
			case 2:
				if(!item.cursed){
					if((item instanceof Weapon && hero.belongings.weapon() == item || item instanceof Armor && hero.belongings.armor() == item)){
						item.identify();
					}
					if((item instanceof Ring && hero.belongings.ring() == item) ||(item instanceof Ring && hero.belongings.misc() == item) ){
						item.identify();
					}
				}
				break;
		}
	}

	public static void onItemEquipped( Hero hero, Item item ){
		boolean identify = false;

		if (hero.pointsInTalent(ARMSMASTERS_INTUITION) == 2 && (item instanceof Weapon || item instanceof Armor)){
			item.identify();
		}

		RectorGetIdentify(hero,item);

		if (hero.hasTalent(THIEFS_INTUITION) && item instanceof Ring){
			if (hero.pointsInTalent(THIEFS_INTUITION) == 2){
				identify = true;
			}
			((Ring) item).setKnown();
		}


		if (identify && !ShardOfOblivion.passiveIDDisabled()){
			item.identify();
		}
	}

	public static void onItemCollected( Hero hero, Item item ){
		if (hero.pointsInTalent(THIEFS_INTUITION) == 2){
			if (item instanceof Ring) ((Ring) item).setKnown();
		}
	}

	//note that IDing can happen in alchemy scene, so be careful with VFX here
	public static void onItemIdentified( Hero hero, Item item ){
		//currently no talents that trigger here, it wasn't a very popular trigger =(
	}

	public static int onAttackProc( Hero hero, Char enemy, int dmg ){
		if (hero.hasTalent(Talent.SUCKER_PUNCH)
				&& enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)
				&& enemy.buff(SuckerPunchTracker.class) == null){
			dmg += Random.IntRange(hero.pointsInTalent(Talent.SUCKER_PUNCH) , 2);
			Buff.affect(enemy, SuckerPunchTracker.class);
		}


		if (hero.hasTalent(Talent.LINGERING_MAGIC)
				&& hero.buff(LingeringMagicTracker.class) != null){
			dmg += Random.IntRange(hero.pointsInTalent(Talent.LINGERING_MAGIC) , 2);
			hero.buff(LingeringMagicTracker.class).detach();
		}

		if (hero.hasTalent(THIRSTY_BLADE) && hero.buff(ThirstyBladeCooldown.class) == null){
			int restoration = Math.round(dmg* hero.pointsInTalent(THIRSTY_BLADE)*0.02f);
			if (restoration > 0) {
				int preHp=hero.HP;
				hero.HP = Math.min(hero.HT, hero.HP + restoration);
				hero.sprite.showStatus(CharSprite.POSITIVE, "+%dHP", hero.HP-preHp);
				hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
				// 添加20回合冷却
				Buff.affect(hero, ThirstyBladeCooldown.class, 20f);
			}
		}

		//受衅怒火 2024-9-17
		if (hero.hasTalent(Talent.PROVOKED_ANGER)
				&& hero.buff(ProvokedAngerTracker.class) != null){
			dmg += 1 + hero.pointsInTalent(Talent.PROVOKED_ANGER);
			hero.buff(ProvokedAngerTracker.class).detach();
		}

		if (hero.hasTalent(Talent.FOLLOWUP_STRIKE) && enemy.isAlive() && enemy.alignment == Char.Alignment.ENEMY) {
			if (hero.belongings.attackingWeapon() instanceof MissileWeapon) {
				Buff.prolong(hero, FollowupStrikeTracker.class, 5f).object = enemy.id();
			} else if (hero.buff(FollowupStrikeTracker.class) != null
					&& hero.buff(FollowupStrikeTracker.class).object == enemy.id()){
				dmg += 1 + hero.pointsInTalent(FOLLOWUP_STRIKE);
				hero.buff(FollowupStrikeTracker.class).detach();
			}
		}

		if (hero.buff(Talent.SpiritBladesTracker.class) != null
				&& Random.Int(10) < 3*hero.pointsInTalent(Talent.SPIRIT_BLADES)){
			SpiritBow bow = hero.belongings.getItem(SpiritBow.class);
			if (bow != null) dmg = bow.proc( hero, enemy, dmg );
			hero.buff(Talent.SpiritBladesTracker.class).detach();
		}

		if (hero.hasTalent(PATIENT_STRIKE)){
			if (hero.buff(PatientStrikeTracker.class) != null
					&& !(hero.belongings.attackingWeapon() instanceof MissileWeapon)){
				hero.buff(PatientStrikeTracker.class).detach();
				dmg += Random.IntRange(hero.pointsInTalent(Talent.PATIENT_STRIKE), 2);
			}
		}

		if (hero.hasTalent(DEADLY_FOLLOWUP) && enemy.alignment == Char.Alignment.ENEMY) {
			if (hero.belongings.attackingWeapon() instanceof MissileWeapon) {
				if (!(hero.belongings.attackingWeapon() instanceof SpiritBow.SpiritArrow)) {
					Buff.prolong(hero, DeadlyFollowupTracker.class, 5f).object = enemy.id();
				}
			} else if (hero.buff(DeadlyFollowupTracker.class) != null
					&& hero.buff(DeadlyFollowupTracker.class).object == enemy.id()){
				dmg = Math.round(dmg * (1.0f + .08f*hero.pointsInTalent(DEADLY_FOLLOWUP)));
			}
		}

		// 狙击4-3 通识射击：近战攻击有概率给予狙击标记
		if (hero.hasTalent(COMMON_SHOT) && hero.subClass == HeroSubClass.SNIPER) {
			if (!(hero.belongings.attackingWeapon() instanceof MissileWeapon)) {
				int points = hero.pointsInTalent(COMMON_SHOT);
				// +1: 33%, +2: 66%, +3: 100%
				int chance = points * 33;
				if (Random.Int(100) < chance) {
					Buff.prolong(enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark.class, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark.DURATION).set(enemy.id(), Dungeon.hero.belongings.weapon() != null ? Dungeon.hero.belongings.weapon().buffedLvl() : 0);
				}
			}
		}

		return dmg;
	}

	public static void onArtifactUsed( Hero hero ){
		/*if (hero.hasTalent(DEATHBLOW)){
			Buff.prolong(hero, EnhancedRings.class, 3f*hero.pointsInTalent(DEATHBLOW));
		}*/
	}

	/**
	 * 术士4-4 腐化怨灵：进入新层时生成怨灵
	 */
	public static void onNewFloor(Hero hero) {
		if (hero.hasTalent(CORRUPT_SPIRIT)) {
			int points = hero.pointsInTalent(CORRUPT_SPIRIT);
			// +1/+2: 2只怨灵, +3/+4: 3只怨灵
			int count = (points >= 3) ? 3 : 2;
			com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CorruptSpirit.spawnAround(hero.pos, count);
		}
	}

	public static class ProvokedAngerTracker extends FlavourBuff{
		{ type = Buff.buffType.POSITIVE; }
		public String icon() { return BuffIndicator.WEAPON; }
		public void tintIcon(Image icon) { icon.hardlight(1.43f, 1.43f, 1.43f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
	}
	public static class LingeringMagicTracker extends FlavourBuff{
		{ type = Buff.buffType.POSITIVE; }
		public String icon() { return BuffIndicator.WEAPON; }
		public void tintIcon(Image icon) { icon.hardlight(1.43f, 1.43f, 0f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
	}

	public static class DuelDanceWandTracker extends FlavourBuff{
		public String icon() { return BuffIndicator.DUEL_DANCE;}
	};
	public static class DuelDanceMissileTracker extends FlavourBuff{
		public String icon() { return BuffIndicator.DUEL_DANCE; }
	};

	public static class SuckerPunchTracker extends Buff{};
	public static class FollowupStrikeTracker extends FlavourBuff{
		public int object;
		{ type = Buff.buffType.POSITIVE; }
		public String icon() { return BuffIndicator.INVERT_MARK; }
		public void tintIcon(Image icon) { icon.hardlight(0f, 0.75f, 1f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
		private static final String OBJECT    = "object";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(OBJECT, object);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			object = bundle.getInt(OBJECT);
		}
	};

	public static final int MAX_TALENT_TIERS = 4;

	public static void initClassTalents( Hero hero ){
		initClassTalents( hero.heroClass, hero.talents, hero.metamorphedTalents );
	}

	public static void initClassTalents( HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents){
		initClassTalents( cls, talents, new LinkedHashMap<>());
	}

	public static void initClassTalents( HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents, LinkedHashMap<Talent, Talent> replacements ){
		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 1
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, HEARTY_MEAL, ARMSMASTERS_INTUITION, PROVOKED_ANGER, IRON_WILL);
				break;
			case MAGE:
				Collections.addAll(tierTalents, EMPOWERING_MEAL, SCHOLARS_INTUITION, LINGERING_MAGIC, BACKUP_BARRIER);
				break;
			case ROGUE:
				Collections.addAll(tierTalents, CACHED_RATIONS, THIEFS_INTUITION, SUCKER_PUNCH, PROTECTIVE_SHADOWS);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, NATURES_BOUNTY, SURVIVALISTS_INTUITION, FOLLOWUP_STRIKE, UNDERESTIMATED);
				break;
			case RECTOR:
				Collections.addAll(tierTalents, PRAYER_BEFORE_MEALS,MENTAL_TELEPATHY,RAIN_GRACE,DEVOTIONAL);
				break;
			case MOONLIGHT:
				Collections.addAll(tierTalents, HUNTING_INTUITION, SHARPENING_EDGE, WEAPON_MASTERY, WAR_TRAMPLE);
				break;
		}
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(0).put(talent, 0);
		}
		tierTalents.clear();

		//tier 2
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, IRON_STOMACH, EMERGENCY_PROTECTION, RUNIC_TRANSFERENCE, LETHAL_MOMENTUM, IMPROVISED_PROJECTILES);
				break;
			case MAGE:
				Collections.addAll(tierTalents, ENERGIZING_MEAL, ENERGIZING_UPGRADE, WAND_PRESERVATION, ARCANE_VISION, SHIELD_BATTERY);
				break;
			case ROGUE:
				Collections.addAll(tierTalents, MYSTICAL_MEAL, DUEL_DANCE, WIDE_SEARCH, SILENT_STEPS, ROGUES_INSTINCT);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, INVIGORATING_MEAL, HERB_MIXTURE, REJUVENATING_STEPS, HEIGHTENED_SENSES, DURABLE_PROJECTILES);
				break;
			case RECTOR:
				Collections.addAll(tierTalents, BLESS_FOOD,SOUL_NOWIFI,LIGHT_STEP,GOD_BODY,NOHOPE_LANG);
				break;
			case MOONLIGHT:
				Collections.addAll(tierTalents, MEAL_UTILIZATION, STRONG_BODY, HOLY_SPRING, TRIPLE_INSURANCE, CATAPULT_START);
				break;
		}
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(1).put(talent, 0);
		}
		tierTalents.clear();

		//tier 3
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, HOLD_FAST, STRONGMAN);
				break;
			case MAGE:
				Collections.addAll(tierTalents, SPELL_QUEUE, ALLY_WARP);
				break;
			case ROGUE:
				Collections.addAll(tierTalents, DEATHBLOW, LIGHT_CLOAK);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, HOLD_BREATH, SEER_SHOT);
				break;
			case RECTOR:
				Collections.addAll(tierTalents, ACT_GODPROGRESS, SMART_BLESSING);
				break;
			case MOONLIGHT:
				Collections.addAll(tierTalents, SWORD_SHIELD_KNIGHT, WHEELCHAIR_CRASH);
				break;
		}
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(2).put(talent, 0);
		}
		tierTalents.clear();


	}


	public static void initSubclassTalents( Hero hero ){
		initSubclassTalents( hero.subClass, hero.talents );
	}

	public static void initSubclassTalents( HeroSubClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		if (cls == HeroSubClass.NONE) return;

		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 3
		switch (cls){
			case BERSERKER: default:
				Collections.addAll(tierTalents, ENDLESS_RAGE, PAIN_SCAR, FANATICISM_MAGIC);
				break;
			case GLADIATOR:
				Collections.addAll(tierTalents, KEEP_VIGILANCE, LETHAL_DEFENSE, VENT_NOPLACE);
				break;
			case BATTLEMAGE:
				Collections.addAll(tierTalents, EMPOWERED_STRIKE, MYSTICAL_CHARGE, WAR_THROW);
				break;
			case WARLOCK:
				Collections.addAll(tierTalents, SOUL_EATER, SOUL_SIPHON, NECROMANCERS_MINIONS);
				break;
			case ASSASSIN:
				Collections.addAll(tierTalents, ENHANCED_LETHALITY, ASSASSINS_REACH, BOUNTY_HUNTER);
				break;
			case FREERUNNER:
				Collections.addAll(tierTalents, EVASIVE_ARMOR, PROJECTILE_MOMENTUM, SPEEDY_STEALTH);
				break;
			case SNIPER:
				Collections.addAll(tierTalents, FARSIGHT, SHARED_ENCHANTMENT, SHARED_UPGRADES);
				break;
			case WARDEN:
				Collections.addAll(tierTalents, DURABLE_TIPS, BARKSKIN, VINE_TRAP);
				break;
			case BATTLEPREIST:
				Collections.addAll(tierTalents,IRON_SUN,PHARCIS_BLESS, BEN_WORK);
				break;
			case REDCARDINAL:
				Collections.addAll(tierTalents,FIRE_GLASS, LIGHT_WASH, SKY_TOWER);
				break;
			case DEAD_KNIGHT:
				Collections.addAll(tierTalents,BLACK_LOVE,DEAD_POWER,EXP_IMPOTION);
				break;
			case LITTLE_KNIGHT:
				Collections.addAll(tierTalents, WONT_LOSE, WET_ENCHANT, LEFT_BOW_RAPID);
				break;
			case DICE_MAGE:
				Collections.addAll(tierTalents, LEARN_SOOTHE, LEARN_LIQUOR, LEARN_OPERATE, LEARN_BLAZE, LEARN_CRUSH, LEARN_MIASMA);
				break;
			case JUTTE_CHAMPION:
				Collections.addAll(tierTalents, ONE_JUTTE, IRON_QUENCH, SURPRISE_JUTTE);
				break;
		}
		for (Talent talent : tierTalents){
			talents.get(2).put(talent, 0);
		}
		tierTalents.clear();
	}

	public static void initArmorTalents( Hero hero ){
		initArmorTalents( hero.armorAbility, hero.talents);
	}

	public static void initArmorTalents(ArmorAbility abil, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		if (abil == null) return;

		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		for (Talent t : abil.talents()){
			talents.get(3).put(t, 0);
		}
	}


	/**
	 * [IMP TALENT ACTIVE]
	 * @param hero
	 */
	public static void initT4Talents(Hero hero){
		initT4Talents(hero.heroClass,hero.subClass,hero.talents);
	}

	public static void initT4Talents(HeroClass cls,HeroSubClass subcls, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		if (subcls == HeroSubClass.NONE) return;
		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 4
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, IRON_MUSCLE, HIGH_DIET); // 4-2重做
				break;
			case ROGUE:
				Collections.addAll(tierTalents, HIDE_IN_CROWD,DARK_ARMOR);
				break;

			//TODO: Mage T4 Is Not Complete, Should Maybe the more time
			case MAGE:
				Collections.addAll(tierTalents, MAGIC_REFINING,MAGIC_TACTICS);
				break;

			case HUNTRESS:
				Collections.addAll(tierTalents, BRISK_PACE,PHASE_FILLING);
				break;
			case RECTOR:
				Collections.addAll(tierTalents,SUPERSTITION,VITAE_BOOST);
				break;
			case MOONLIGHT:
				Collections.addAll(tierTalents, LIGHT_ETERNITY, MOON_GLORY); // 4-1/4-2重做
				break;
		}
		//tier 4
		switch (subcls){
			case BERSERKER: default:
				Collections.addAll(tierTalents, REVENGE_ROAR, THIRSTY_BLADE); // THIRSTY_BLADE加冷却

				//GLog.p("5");
				break;
			case GLADIATOR:
				Collections.addAll(tierTalents, DEFENSIVE_STRIKE, WEAPON_MASTER); // 4-4重做
				break;
			case ASSASSIN:
				Collections.addAll(tierTalents, BRACE_YOURSELF,POWER_RECYCLE);
				break;

			case FREERUNNER:
				Collections.addAll(tierTalents, KINETIC_ENERGY,STORM_RUSH);
				break;

			case BATTLEMAGE:
				Collections.addAll(tierTalents,WAND_DODGE, MAGIC_WORKMAN); // 4-3重做
				break;
			case WARLOCK:
				Collections.addAll(tierTalents, DESPERATE_POWER, CORRUPT_SPIRIT); // 4-4重做
				break;

			case SNIPER:
				Collections.addAll(tierTalents, COMMON_SHOT, STORM_ATTACK); // 4-3重做
				break;

			case WARDEN:
				Collections.addAll(tierTalents, MORE_DARTS, GRASS_VISION); // 4-3/4-4重做
				break;

			case REDCARDINAL:
				Collections.addAll(tierTalents,SOUL_POSSESSION,BLOODY_VITAE);
				break;

			case BATTLEPREIST:
				Collections.addAll(tierTalents,ADRENAL_COMBAT,GIFT);
				break;

			case DEAD_KNIGHT:
				Collections.addAll(tierTalents,ERROR);
				break;

			case LITTLE_KNIGHT:
				Collections.addAll(tierTalents, SHIELD_POKE, KNIGHT_SPIRIT); // 4-3/4-4重做
				break;

			case DICE_MAGE:
				Collections.addAll(tierTalents, SPELL_EMPOWER, EGG_BASKET); // 4-3/4-4重做
				break;

			case JUTTE_CHAMPION:
				Collections.addAll(tierTalents,ERROR);
				break;
		}
		for (Talent talent : tierTalents){
			talents.get(3).put(talent, 0);
		}

		//GLog.p("6");
		tierTalents.clear();

	}

	private static final String TALENT_TIER = "talents_tier_";

	public static void storeTalentsInBundle( Bundle bundle, Hero hero ){
		for (int i = 0; i < MAX_TALENT_TIERS; i++){
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(i);
			Bundle tierBundle = new Bundle();

			for (Talent talent : tier.keySet()){
				if (tier.get(talent) > 0){
					tierBundle.put(talent.name(), tier.get(talent));
				}
				if (tierBundle.contains(talent.name())){
					tier.put(talent, Math.min(tierBundle.getInt(talent.name()), talent.maxPoints()));
				}
			}
			bundle.put(TALENT_TIER+(i+1), tierBundle);
		}

		Bundle replacementsBundle = new Bundle();
		for (Talent t : hero.metamorphedTalents.keySet()){
			replacementsBundle.put(t.name(), hero.metamorphedTalents.get(t));
		}
		bundle.put("replacements", replacementsBundle);
	}

	public static void restoreTalentsFromBundle( Bundle bundle, Hero hero ){
		if (bundle.contains("replacements")){
			Bundle replacements = bundle.getBundle("replacements");
			for (String key : replacements.getKeys()){
				hero.metamorphedTalents.put(Talent.valueOf(key), replacements.getEnum(key, Talent.class));
			}
		}

		if (hero.heroClass != null)     initClassTalents(hero);
		if (hero.subClass != null)      initSubclassTalents(hero);
		if (hero.armorAbility != null)  initArmorTalents(hero);
		if (hero.powerOfImp) initT4Talents(hero);

		for (int i = 0; i < MAX_TALENT_TIERS; i++){
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(i);
			Bundle tierBundle = bundle.contains(TALENT_TIER+(i+1)) ? bundle.getBundle(TALENT_TIER+(i+1)) : null;

			if (tierBundle != null){
				for (Talent talent : tier.keySet()){
					if (tierBundle.contains(talent.name())){
						tier.put(talent, Math.min(tierBundle.getInt(talent.name()), talent.maxPoints()));
					}
				}
			}
		}
	}
	private static final HashSet<String> removedTalents = new HashSet<>();
	static{
		//v2.4.0
		removedTalents.add("TEST_SUBJECT");
		removedTalents.add("TESTED_HYPOTHESIS");
		//v2.2.0
		removedTalents.add("EMPOWERING_SCROLLS");
	}

	private static final HashMap<String, String> renamedTalents = new HashMap<>();
	static{
		//v2.4.0
		renamedTalents.put("SECONDARY_CHARGE",          "VARIED_CHARGE");

		//v2.2.0
		renamedTalents.put("RESTORED_WILLPOWER",        "LIQUID_WILLPOWER");
		renamedTalents.put("ENERGIZING_UPGRADE",        "INSCRIBED_POWER");
		renamedTalents.put("MYSTICAL_UPGRADE",          "INSCRIBED_STEALTH");
		renamedTalents.put("RESTORED_NATURE",           "LIQUID_NATURE");
		renamedTalents.put("RESTORED_AGILITY",          "LIQUID_AGILITY");
		//v2.1.0
		renamedTalents.put("LIGHTWEIGHT_CHARGE",        "PRECISE_ASSAULT");
		//v2.0.0 BETA
		renamedTalents.put("LIGHTLY_ARMED",             "UNENCUMBERED_SPIRIT");
		//v2.0.0
		renamedTalents.put("ARMSMASTERS_INTUITION",     "VETERANS_INTUITION");
	}
}
