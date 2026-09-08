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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PhysicalEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;

public class Talent {

	protected final String name;
	protected final int maxPoints;

	protected Talent(String name){
		this(name, 2);
	}

	protected Talent(String name, int maxPoints){
		this.name = name;
		this.maxPoints = maxPoints;
	}

	public String name(){ return name; }
	public int maxPoints(){ return maxPoints; }

	public static final Talent HEARTY_MEAL = new Talent("HEARTY_MEAL");

	public static final Talent ARMSMASTERS_INTUITION = new Talent("ARMSMASTERS_INTUITION");

	public static final Talent PROVOKED_ANGER = new Talent("PROVOKED_ANGER");

	public static final Talent IRON_WILL = new Talent("IRON_WILL");

	public static final Talent IRON_STOMACH = new Talent("IRON_STOMACH");

	public static final Talent EMERGENCY_PROTECTION = new Talent("EMERGENCY_PROTECTION");

	public static final Talent RUNIC_TRANSFERENCE = new Talent("RUNIC_TRANSFERENCE");

	public static final Talent LETHAL_MOMENTUM = new Talent("LETHAL_MOMENTUM");

	public static final Talent IMPROVISED_PROJECTILES = new Talent("IMPROVISED_PROJECTILES");

	public static final Talent HOLD_FAST = new Talent("HOLD_FAST", 3);

	public static final Talent STRONGMAN = new Talent("STRONGMAN", 3);

	public static final Talent IRON_MUSCLE = new Talent("IRON_MUSCLE", 4);

	public static final Talent MOVING_DEFENSE = new Talent("MOVING_DEFENSE", 4);

	public static final Talent HIGH_DIET = new Talent("HIGH_DIET", 4);

	public static final Talent ENDLESS_RAGE = new Talent("ENDLESS_RAGE", 3);

	public static final Talent PAIN_SCAR = new Talent("PAIN_SCAR", 3);

	public static final Talent FANATICISM_MAGIC = new Talent("FANATICISM_MAGIC", 3);

	public static final Talent REVENGE_ROAR = new Talent("REVENGE_ROAR", 4);

	public static final Talent THIRSTY_BLADE = new Talent("THIRSTY_BLADE", 4);

	public static final Talent KEEP_VIGILANCE = new Talent("KEEP_VIGILANCE", 3);

	public static final Talent LETHAL_DEFENSE = new Talent("LETHAL_DEFENSE", 3);

	public static final Talent VENT_NOPLACE = new Talent("VENT_NOPLACE", 3);

	public static final Talent DEFENSIVE_STRIKE = new Talent("DEFENSIVE_STRIKE", 4);

	public static final Talent DEVASTATE = new Talent("DEVASTATE", 4);

	public static final Talent WEAPON_MASTER = new Talent("WEAPON_MASTER", 4);

	public static final Talent BODY_SLAM = new Talent("BODY_SLAM", 4);

	public static final Talent IMPACT_WAVE = new Talent("IMPACT_WAVE", 4);

	public static final Talent DOUBLE_JUMP = new Talent("DOUBLE_JUMP", 4);

	public static final Talent EXPANDING_WAVE = new Talent("EXPANDING_WAVE", 4);

	public static final Talent STRIKING_WAVE = new Talent("STRIKING_WAVE", 4);

	public static final Talent SHOCK_FORCE = new Talent("SHOCK_FORCE", 4);

	public static final Talent SUSTAINED_RETRIBUTION = new Talent("SUSTAINED_RETRIBUTION", 4);

	public static final Talent SHRUG_IT_OFF = new Talent("SHRUG_IT_OFF", 4);

	public static final Talent EVEN_THE_ODDS = new Talent("EVEN_THE_ODDS", 4);

	public static final Talent EMPOWERING_MEAL = new Talent("EMPOWERING_MEAL");

	public static final Talent SCHOLARS_INTUITION = new Talent("SCHOLARS_INTUITION");

	public static final Talent LINGERING_MAGIC = new Talent("LINGERING_MAGIC");

	public static final Talent BACKUP_BARRIER = new Talent("BACKUP_BARRIER");

	public static final Talent ENERGIZING_MEAL = new Talent("ENERGIZING_MEAL");

	public static final Talent ENERGIZING_UPGRADE = new Talent("ENERGIZING_UPGRADE");

	public static final Talent WAND_PRESERVATION = new Talent("WAND_PRESERVATION");

	public static final Talent ARCANE_VISION = new Talent("ARCANE_VISION");

	public static final Talent SHIELD_BATTERY = new Talent("SHIELD_BATTERY");

	public static final Talent SPELL_QUEUE = new Talent("SPELL_QUEUE", 3);

	public static final Talent ALLY_WARP = new Talent("ALLY_WARP", 3);

	public static final Talent MAGIC_REFINING = new Talent("MAGIC_REFINING", 4);

	public static final Talent MAGIC_TACTICS = new Talent("MAGIC_TACTICS", 4);

	public static final Talent MAGIC_STICK = new Talent("MAGIC_STICK", 4);

	public static final Talent MAGIC_WORKMAN = new Talent("MAGIC_WORKMAN", 4);

	public static final Talent WAND_DODGE = new Talent("WAND_DODGE", 4);

	public static final Talent DESPERATE_POWER = new Talent("DESPERATE_POWER", 4);

	public static final Talent GHOST_ROOT = new Talent("GHOST_ROOT", 4);

	public static final Talent CORRUPT_SPIRIT = new Talent("CORRUPT_SPIRIT", 4);

	public static final Talent EMPOWERED_STRIKE = new Talent("EMPOWERED_STRIKE", 3);

	public static final Talent MYSTICAL_CHARGE = new Talent("MYSTICAL_CHARGE", 3);

	public static final Talent WAR_THROW = new Talent("WAR_THROW", 3);

	public static final Talent SOUL_EATER = new Talent("SOUL_EATER", 3);

	public static final Talent SOUL_SIPHON = new Talent("SOUL_SIPHON", 3);

	public static final Talent NECROMANCERS_MINIONS = new Talent("NECROMANCERS_MINIONS", 3);

	public static final Talent BLAST_RADIUS = new Talent("BLAST_RADIUS", 4);

	public static final Talent ELEMENTAL_POWER = new Talent("ELEMENTAL_POWER", 4);

	public static final Talent REACTIVE_BARRIER = new Talent("REACTIVE_BARRIER", 4);

	public static final Talent WILD_POWER = new Talent("WILD_POWER", 4);

	public static final Talent FIRE_EVERYTHING = new Talent("FIRE_EVERYTHING", 4);

	public static final Talent CONSERVED_MAGIC = new Talent("CONSERVED_MAGIC", 4);

	public static final Talent TELEFRAG = new Talent("TELEFRAG", 4);

	public static final Talent REMOTE_BEACON = new Talent("REMOTE_BEACON", 4);

	public static final Talent LONGRANGE_WARP = new Talent("LONGRANGE_WARP", 4);

	public static final Talent CACHED_RATIONS = new Talent("CACHED_RATIONS");

	public static final Talent THIEFS_INTUITION = new Talent("THIEFS_INTUITION");

	public static final Talent SUCKER_PUNCH = new Talent("SUCKER_PUNCH");

	public static final Talent PROTECTIVE_SHADOWS = new Talent("PROTECTIVE_SHADOWS");

	public static final Talent MYSTICAL_MEAL = new Talent("MYSTICAL_MEAL");

	public static final Talent DUEL_DANCE = new Talent("DUEL_DANCE");

	public static final Talent WIDE_SEARCH = new Talent("WIDE_SEARCH");

	public static final Talent SILENT_STEPS = new Talent("SILENT_STEPS");

	public static final Talent ROGUES_INSTINCT = new Talent("ROGUES_INSTINCT");

	public static final Talent DEATHBLOW = new Talent("DEATHBLOW", 3);

	public static final Talent LIGHT_CLOAK = new Talent("LIGHT_CLOAK", 3);

	public static final Talent HIDE_IN_CROWD = new Talent("HIDE_IN_CROWD", 4);

	public static final Talent DARK_ARMOR = new Talent("DARK_ARMOR", 4);

	public static final Talent ENHANCED_LETHALITY = new Talent("ENHANCED_LETHALITY", 3);

	public static final Talent ASSASSINS_REACH = new Talent("ASSASSINS_REACH", 3);

	public static final Talent BOUNTY_HUNTER = new Talent("BOUNTY_HUNTER", 3);

	public static final Talent BRACE_YOURSELF = new Talent("BRACE_YOURSELF", 4);

	public static final Talent POWER_RECYCLE = new Talent("POWER_RECYCLE", 4);

	public static final Talent EVASIVE_ARMOR = new Talent("EVASIVE_ARMOR", 3);

	public static final Talent PROJECTILE_MOMENTUM = new Talent("PROJECTILE_MOMENTUM", 3);

	public static final Talent SPEEDY_STEALTH = new Talent("SPEEDY_STEALTH", 3);

	public static final Talent KINETIC_ENERGY = new Talent("KINETIC_ENERGY", 4);

	public static final Talent STORM_RUSH = new Talent("STORM_RUSH", 4);

	public static final Talent HASTY_RETREAT = new Talent("HASTY_RETREAT", 4);

	public static final Talent BODY_REPLACEMENT = new Talent("BODY_REPLACEMENT", 4);

	public static final Talent SHADOW_STEP = new Talent("SHADOW_STEP", 4);

	public static final Talent FEAR_THE_REAPER = new Talent("FEAR_THE_REAPER", 4);

	public static final Talent DEATHLY_DURABILITY = new Talent("DEATHLY_DURABILITY", 4);

	public static final Talent DOUBLE_MARK = new Talent("DOUBLE_MARK", 4);

	public static final Talent SHADOW_BLADE = new Talent("SHADOW_BLADE", 4);

	public static final Talent CLONED_ARMOR = new Talent("CLONED_ARMOR", 4);

	public static final Talent PERFECT_COPY = new Talent("PERFECT_COPY", 4);

	public static final Talent NATURES_BOUNTY = new Talent("NATURES_BOUNTY");

	public static final Talent SURVIVALISTS_INTUITION = new Talent("SURVIVALISTS_INTUITION");

	public static final Talent FOLLOWUP_STRIKE = new Talent("FOLLOWUP_STRIKE");

	public static final Talent UNDERESTIMATED = new Talent("UNDERESTIMATED");

	public static final Talent INVIGORATING_MEAL = new Talent("INVIGORATING_MEAL");

	public static final Talent HERB_MIXTURE = new Talent("HERB_MIXTURE");

	public static final Talent REJUVENATING_STEPS = new Talent("REJUVENATING_STEPS");

	public static final Talent HEIGHTENED_SENSES = new Talent("HEIGHTENED_SENSES");

	public static final Talent DURABLE_PROJECTILES = new Talent("DURABLE_PROJECTILES");

	public static final Talent HOLD_BREATH = new Talent("HOLD_BREATH", 3);

	public static final Talent SEER_SHOT = new Talent("SEER_SHOT", 3);

	public static final Talent BRISK_PACE = new Talent("BRISK_PACE", 4);

	public static final Talent PHASE_FILLING = new Talent("PHASE_FILLING", 4);

	public static final Talent BOW_DULES = new Talent("BOW_DULES", 4);

	public static final Talent STORM_ATTACK = new Talent("STORM_ATTACK", 4);

	public static final Talent MEDART_SPECIALIST = new Talent("MEDART_SPECIALIST", 4);

	public static final Talent LAND_HEART = new Talent("LAND_HEART", 4);

	public static final Talent COMMON_SHOT = new Talent("COMMON_SHOT", 4);

	public static final Talent MORE_DARTS = new Talent("MORE_DARTS", 4);

	public static final Talent GRASS_VISION = new Talent("GRASS_VISION", 4);

	public static final Talent FARSIGHT = new Talent("FARSIGHT", 3);

	public static final Talent SHARED_ENCHANTMENT = new Talent("SHARED_ENCHANTMENT", 3);

	public static final Talent SHARED_UPGRADES = new Talent("SHARED_UPGRADES", 3);

	public static final Talent DURABLE_TIPS = new Talent("DURABLE_TIPS", 3);

	public static final Talent BARKSKIN = new Talent("BARKSKIN", 3);

	public static final Talent VINE_TRAP = new Talent("VINE_TRAP", 3);

	public static final Talent FAN_OF_BLADES = new Talent("FAN_OF_BLADES", 4);

	public static final Talent PROJECTING_BLADES = new Talent("PROJECTING_BLADES", 4);

	public static final Talent SPIRIT_BLADES = new Talent("SPIRIT_BLADES", 4);

	public static final Talent GROWING_POWER = new Talent("GROWING_POWER", 4);

	public static final Talent NATURES_WRATH = new Talent("NATURES_WRATH", 4);

	public static final Talent WILD_MOMENTUM = new Talent("WILD_MOMENTUM", 4);

	public static final Talent EAGLE_EYE = new Talent("EAGLE_EYE", 4);

	public static final Talent GO_FOR_THE_EYES = new Talent("GO_FOR_THE_EYES", 4);

	public static final Talent SWIFT_SPIRIT = new Talent("SWIFT_SPIRIT", 4);

	public static final Talent HEROIC_ENERGY = new Talent("HEROIC_ENERGY", 4);

	public static final Talent RATSISTANCE = new Talent("RATSISTANCE", 4);

	public static final Talent RATLOMACY = new Talent("RATLOMACY", 4);

	public static final Talent RATFORCEMENTS = new Talent("RATFORCEMENTS", 4);

	public static final Talent STRENGTHENING_MEAL = new Talent("STRENGTHENING_MEAL");

	public static final Talent ADVENTURERS_INTUITION = new Talent("ADVENTURERS_INTUITION");

	public static final Talent PATIENT_STRIKE = new Talent("PATIENT_STRIKE");

	public static final Talent AGGRESSIVE_BARRIER = new Talent("AGGRESSIVE_BARRIER");

	public static final Talent FOCUSED_MEAL = new Talent("FOCUSED_MEAL");

	public static final Talent LIQUID_AGILITY = new Talent("LIQUID_AGILITY");

	public static final Talent WEAPON_RECHARGING = new Talent("WEAPON_RECHARGING");

	public static final Talent LETHAL_HASTE = new Talent("LETHAL_HASTE");

	public static final Talent SWIFT_EQUIP = new Talent("SWIFT_EQUIP");

	public static final Talent PRECISE_ASSAULT = new Talent("PRECISE_ASSAULT", 3);

	public static final Talent DEADLY_FOLLOWUP = new Talent("DEADLY_FOLLOWUP", 3);

	public static final Talent VARIED_CHARGE = new Talent("VARIED_CHARGE", 3);

	public static final Talent TWIN_UPGRADES = new Talent("TWIN_UPGRADES", 3);

	public static final Talent COMBINED_LETHALITY = new Talent("COMBINED_LETHALITY", 3);

	public static final Talent UNENCUMBERED_SPIRIT = new Talent("UNENCUMBERED_SPIRIT", 3);

	public static final Talent MONASTIC_VIGOR = new Talent("MONASTIC_VIGOR", 3);

	public static final Talent COMBINED_ENERGY = new Talent("COMBINED_ENERGY", 3);

	public static final Talent CLOSE_THE_GAP = new Talent("CLOSE_THE_GAP", 4);

	public static final Talent INVIGORATING_VICTORY = new Talent("INVIGORATING_VICTORY", 4);

	public static final Talent ELIMINATION_MATCH = new Talent("ELIMINATION_MATCH", 4);

	public static final Talent ELEMENTAL_REACH = new Talent("ELEMENTAL_REACH", 4);

	public static final Talent STRIKING_FORCE = new Talent("STRIKING_FORCE", 4);

	public static final Talent DIRECTED_POWER = new Talent("DIRECTED_POWER", 4);

	public static final Talent FEIGNED_RETREAT = new Talent("FEIGNED_RETREAT", 4);

	public static final Talent EXPOSE_WEAKNESS = new Talent("EXPOSE_WEAKNESS", 4);

	public static final Talent COUNTER_ABILITY = new Talent("COUNTER_ABILITY", 4);

	public static final Talent PRAYER_BEFORE_MEALS = new Talent("PRAYER_BEFORE_MEALS");

	public static final Talent MENTAL_TELEPATHY = new Talent("MENTAL_TELEPATHY");

	public static final Talent RAIN_GRACE = new Talent("RAIN_GRACE");

	public static final Talent DEVOTIONAL = new Talent("DEVOTIONAL");

	public static final Talent BLESS_FOOD = new Talent("BLESS_FOOD");

	public static final Talent SOUL_NOWIFI = new Talent("SOUL_NOWIFI");

	public static final Talent LIGHT_STEP = new Talent("LIGHT_STEP");

	public static final Talent GOD_BODY = new Talent("GOD_BODY");

	public static final Talent NOHOPE_LANG = new Talent("NOHOPE_LANG");

	public static final Talent ACT_GODPROGRESS = new Talent("ACT_GODPROGRESS", 3);

	public static final Talent SMART_BLESSING = new Talent("SMART_BLESSING", 3);

	public static final Talent IRON_SUN = new Talent("IRON_SUN", 3);

	public static final Talent PHARCIS_BLESS = new Talent("PHARCIS_BLESS", 3);

	public static final Talent BEN_WORK = new Talent("BEN_WORK", 3);

	public static final Talent FIRE_GLASS = new Talent("FIRE_GLASS", 3);

	public static final Talent LIGHT_WASH = new Talent("LIGHT_WASH", 3);

	public static final Talent SKY_TOWER = new Talent("SKY_TOWER", 3);

	public static final Talent BLACK_LOVE = new Talent("BLACK_LOVE", 3);

	public static final Talent DEAD_POWER = new Talent("DEAD_POWER", 3);

	public static final Talent EXP_IMPOTION = new Talent("EXP_IMPOTION", 3);

	public static final Talent SUPERSTITION = new Talent("SUPERSTITION", 4);

	public static final Talent VITAE_BOOST = new Talent("VITAE_BOOST", 4);

	public static final Talent ADRENAL_COMBAT = new Talent("ADRENAL_COMBAT", 4);

	public static final Talent GIFT = new Talent("GIFT", 4);

	public static final Talent SOUL_POSSESSION = new Talent("SOUL_POSSESSION", 4);

	public static final Talent BLOODY_VITAE = new Talent("BLOODY_VITAE", 4);

	public static final Talent PRESS_ON = new Talent("PRESS_ON", 4);

	public static final Talent BRIEF_HARVEST = new Talent("BRIEF_HARVEST", 4);

	public static final Talent EFFICIENT_HEALING = new Talent("EFFICIENT_HEALING", 4);

	public static final Talent INERTIAL_CHARGE = new Talent("INERTIAL_CHARGE", 4);

	public static final Talent BLESS_RETURN = new Talent("BLESS_RETURN", 4);

	public static final Talent SACRIFICE = new Talent("SACRIFICE", 4);

	public static final Talent BLOCKING_READING = new Talent("BLOCKING_READING", 4);

	public static final Talent TAI_CHI_POISE = new Talent("TAI_CHI_POISE", 4);

	public static final Talent HOLY_SHOCKWAVE = new Talent("HOLY_SHOCKWAVE", 4);

	public static final Talent GODHOOD = new Talent("GODHOOD", 4);

	public static final Talent AVATAR = new Talent("AVATAR", 4);

	public static final Talent HUNTING_INTUITION = new Talent("HUNTING_INTUITION");

	public static final Talent SHARPENING_EDGE = new Talent("SHARPENING_EDGE");

	public static final Talent WEAPON_MASTERY = new Talent("WEAPON_MASTERY");

	public static final Talent WAR_TRAMPLE = new Talent("WAR_TRAMPLE");

	public static final Talent MEAL_UTILIZATION = new Talent("MEAL_UTILIZATION");

	public static final Talent STRONG_BODY = new Talent("STRONG_BODY");

	public static final Talent HOLY_SPRING = new Talent("HOLY_SPRING");

	public static final Talent TRIPLE_INSURANCE = new Talent("TRIPLE_INSURANCE");

	public static final Talent CATAPULT_START = new Talent("CATAPULT_START");

	public static final Talent SWORD_SHIELD_KNIGHT = new Talent("SWORD_SHIELD_KNIGHT", 3);

	public static final Talent WHEELCHAIR_CRASH = new Talent("WHEELCHAIR_CRASH", 3);

	public static final Talent HEROIC_ENERGY_MOONLIGHT = new Talent("HEROIC_ENERGY_MOONLIGHT", 4);

	public static final Talent LIGHT_ETERNITY = new Talent("LIGHT_ETERNITY", 4);

	public static final Talent MOON_GLORY = new Talent("MOON_GLORY", 4);

	public static final Talent WONT_LOSE = new Talent("WONT_LOSE", 3);

	public static final Talent WET_ENCHANT = new Talent("WET_ENCHANT", 3);

	public static final Talent LEFT_BOW_RAPID = new Talent("LEFT_BOW_RAPID", 3);

	public static final Talent SHIELD_POKE = new Talent("SHIELD_POKE", 4);

	public static final Talent KNIGHT_SPIRIT = new Talent("KNIGHT_SPIRIT", 4);

	public static final Talent LEARN_CUT = new Talent("LEARN_CUT", 2) {
		@Override
		public String icon() {
			return "spell_empower";
		}
	};

	public static final Talent LEARN_HEAL = new Talent("LEARN_HEAL", 2) {
		@Override
		public String icon() {
			return "egg_basket";
		}
	};

	public static final Talent GATHER = new Talent("GATHER", 2) {
		@Override
		public String icon() {
			return "spell_queue";
		}
	};

	public static final Talent LEARN_SOOTHE = new Talent("LEARN_SOOTHE", 2);

	public static final Talent LEARN_LIQUOR = new Talent("LEARN_LIQUOR", 2);

	public static final Talent LEARN_OPERATE = new Talent("LEARN_OPERATE", 2);

	public static final Talent LEARN_MIASMA = new Talent("LEARN_MIASMA", 2);

	public static final Talent LEARN_CRUSH = new Talent("LEARN_CRUSH", 2);

	public static final Talent LEARN_BLAZE = new Talent("LEARN_BLAZE", 2);

	public static final Talent SCHOOL_FIRE = new Talent("SCHOOL_FIRE", 3) {
		@Override public String icon() { return "learn_blaze"; }
	};

	public static final Talent SCHOOL_BLADES = new Talent("SCHOOL_BLADES", 3) {
		@Override public String icon() { return "learn_crush"; }
	};

	public static final Talent SCHOOL_CONJURATION = new Talent("SCHOOL_CONJURATION", 3) {
		@Override public String icon() { return "spell_empower"; }
	};

	public static final Talent SCHOOL_MANA = new Talent("SCHOOL_MANA", 3) {
		@Override public String icon() { return "spell_queue"; }
	};

	public static final Talent SCHOOL_BLOOD = new Talent("SCHOOL_BLOOD", 3) {
		@Override public String icon() { return "learn_liquor"; }
	};

	public static final Talent SCHOOL_NATURE = new Talent("SCHOOL_NATURE", 3) {
		@Override public String icon() { return "learn_miasma"; }
	};

	public static final Talent SCHOOL_MEDICAL = new Talent("SCHOOL_MEDICAL", 3) {
		@Override public String icon() { return "learn_soothe"; }
	};

	public static final Talent SCHOOL_PHYSICAL = new Talent("SCHOOL_PHYSICAL", 3) {
		@Override public String icon() { return "learn_operate"; }
	};

	public static final Talent SCHOOL_EMERGENCY = new Talent("SCHOOL_EMERGENCY", 3) {
		@Override public String icon() { return "egg_basket"; }
	};

	public static final Talent SCHOOL_SPECIAL = new Talent("SCHOOL_SPECIAL", 3) {
		@Override public String icon() { return "fated_twice"; }
	};

	public static final Talent D3_SKIPPED = new Talent("D3_SKIPPED", 99) {
		@Override public String icon() { return "error"; }
	};

	public static final Talent SPELL_EMPOWER = new Talent("SPELL_EMPOWER", 4);

	public static final Talent EGG_BASKET = new Talent("EGG_BASKET", 4);

	public static final Talent ONE_JUTTE = new Talent("ONE_JUTTE", 3);

	public static final Talent IRON_QUENCH = new Talent("IRON_QUENCH", 3);

	public static final Talent SURPRISE_JUTTE = new Talent("SURPRISE_JUTTE", 3);

	public static final Talent FATED_TWICE = new Talent("FATED_TWICE", 4);

	public static final Talent LOOT_GROUND = new Talent("LOOT_GROUND", 4);

	public static final Talent TIME_PAUSE = new Talent("TIME_PAUSE", 4);

	public static final Talent BETTER_ITEM = new Talent("BETTER_ITEM", 4);

	public static final Talent EXTRA_POCKET = new Talent("EXTRA_POCKET", 4);

	public static final Talent ACCEPT_CHALLENGE = new Talent("ACCEPT_CHALLENGE", 4);

	public static final Talent HOLY_LANCE = new Talent("HOLY_LANCE", 4);

	public static final Talent SOUL_STREAM = new Talent("SOUL_STREAM", 4);

	public static final Talent FATAL_BLADE = new Talent("FATAL_BLADE", 4);

	public static final Talent ERROR = new Talent("ERROR");

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
	// tiers 1/2/3/4 start at levels 2/7/13/21
	public static int[] tierLevelThresholds = new int[]{0, 2, 7, 13, 21, 31};
	public String icon(){
		if (this == HEROIC_ENERGY){
			if (Ratmogrify.useRatroicEnergy){
				return "heroic_energy_rat";
			}
			HeroClass cls = Dungeon.hero != null ? Dungeon.hero.heroClass : GamesInProgress.selectedClass;
			if (cls == HeroClasses.MAGE) return "heroic_energy_mage";
			if (cls == HeroClasses.ROGUE) return "heroic_energy_rogue";
			if (cls == HeroClasses.HUNTRESS) return "heroic_energy_huntress";
			return "heroic_energy_warrior";
		} else {
			return name().toLowerCase(Locale.ROOT);
		}
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

		if (talent == LIGHT_CLOAK && hero.heroClass == HeroClasses.ROGUE){
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
		if (hero.hasTalent(COMMON_SHOT) && hero.subClass == HeroSubClasses.SNIPER) {
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

		//使用有效定义（皮肤变体可覆盖天赋；无皮肤时为基础职业）
		HeroDefinition def = cls.activeDefinition();
		if (def == null || def.talents() == null) return;
		TalentSet set = def.talents();

		initTier(talents.get(0), set.getTier1(), replacements);
		initTier(talents.get(1), set.getTier2(), replacements);
		initTier(talents.get(2), set.getTier3(), replacements);
	}

	private static void initTier(LinkedHashMap<Talent, Integer> tier, Talent[] src, LinkedHashMap<Talent, Talent> replacements){
		if (src == null) return;
		for (Talent talent : src){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			tier.put(talent, 0);
		}
	}


	public static void initSubclassTalents( Hero hero ){
		initSubclassTalents( hero.subClass, hero.talents );
	}

	public static void initSubclassTalents( HeroSubClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		if (cls == HeroSubClasses.NONE) return;

		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		// 骰子法师特殊：清空 T3 通用，放入魔法学派，并附加魔力点数
		if (cls == HeroSubClasses.DICE_MAGE){
			talents.get(2).clear();
			if (Dungeon.hero != null) Buff.affect(Dungeon.hero, MagicPoint.class);
		}

		initTier(talents.get(2), cls.subclassT3(), new LinkedHashMap<>());
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
		if (subcls == HeroSubClasses.NONE) return;
		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		LinkedHashMap<Talent, Integer> tier4 = talents.get(3);

		HeroDefinition def = cls.activeDefinition();
		if (def != null && def.talents() != null){
			initTier(tier4, def.talents().getTier4(), new LinkedHashMap<>());
		}

		initTier(tier4, subcls.subclassT4(), new LinkedHashMap<>());
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
			replacementsBundle.put(t.name(), hero.metamorphedTalents.get(t).name());
		}
		bundle.put("replacements", replacementsBundle);
	}

	public static void restoreTalentsFromBundle( Bundle bundle, Hero hero ){
		if (bundle.contains("replacements")){
			Bundle replacements = bundle.getBundle("replacements");
			for (String key : replacements.getKeys()){
				String val = replacements.getString(key);
				if (byName(key) != null && byName(val) != null){
					hero.metamorphedTalents.put(byName(key), byName(val));
				}
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

	public static boolean isDiceMageSpellTalent(Talent talent) {
		return talent == SCHOOL_FIRE || talent == SCHOOL_BLADES || talent == SCHOOL_CONJURATION || talent == SCHOOL_MANA
				|| talent == SCHOOL_BLOOD || talent == SCHOOL_NATURE || talent == SCHOOL_MEDICAL || talent == SCHOOL_PHYSICAL
				|| talent == SCHOOL_EMERGENCY || talent == SCHOOL_SPECIAL;
	}

	//按名称查找天赋（用于存档反序列化），自动处理改名映射
	public static Talent byName(String name){
		if (renamedTalents.containsKey(name)){
			name = renamedTalents.get(name);
		}
		return nameToTalent.get(name);
	}

	private static final HashMap<String, Talent> nameToTalent = new HashMap<>();
	static{
		for (Field f : Talent.class.getFields()){
			if (Modifier.isStatic(f.getModifiers()) && Talent.class.isAssignableFrom(f.getType())){
				try {
					Talent t = (Talent) f.get(null);
					nameToTalent.put(t.name(), t);
				} catch (Exception ignored){ }
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
