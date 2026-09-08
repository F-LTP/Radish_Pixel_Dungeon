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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.watabou.gltextures.AtlasFrame;
import com.watabou.gltextures.AtlasSource;
import com.watabou.gltextures.RuntimeAtlas;
import com.watabou.gltextures.RuntimeAtlasRegistry;
import com.watabou.noosa.Image;

public class ItemSpriteSheet {

	public static final AtlasSource ATLAS = new AtlasSource( "sprites/items", "something" );

	// TheCatist 2026/7/25 通过自动检测每个物品贴图的外接矩形的办法 移除了所有对assignItemRect的调用
	//SOMETHING is the default item sprite at position 0. May show up ingame if there are bugs.
	public static final String SOMETHING = "something";
	public static final String WEAPON_HOLDER = "weapon_holder";
	public static final String ARMOR_HOLDER = "armor_holder";
	public static final String MISSILE_HOLDER = "missile_holder";
	public static final String WAND_HOLDER = "wand_holder";
	public static final String RING_HOLDER = "ring_holder";
	public static final String ARTIFACT_HOLDER = "artifact_holder";
	public static final String TRINKET_HOLDER = "trinket_holder";
	public static final String FOOD_HOLDER = "food_holder";
	public static final String BOMB_HOLDER = "bomb_holder";
	public static final String POTION_HOLDER = "potion_holder";
	public static final String SEED_HOLDER = "seed_holder";
	public static final String SCROLL_HOLDER = "scroll_holder";
	public static final String STONE_HOLDER = "stone_holder";
	public static final String ELIXIR_HOLDER = "elixir_holder";
	public static final String SPELL_HOLDER = "spell_holder";
//	public static final String SNAKE_BITE = "snake_bite"; // Snake Bite challenge item icon (placeholder)

	public static final String MOB_HOLDER = "mob_holder";
	public static final String DOCUMENT_HOLDER = "document_holder";

	public static final String GOLD = "gold";
	public static final String ENERGY = "energy";

	public static final String DEWDROP = "dewdrop";
	public static final String PETAL = "petal";
	public static final String SANDBAG = "sandbag";
	public static final String SPIRIT_ARROW = "spirit_arrow";
	public static final String SPIRIT_ALT_ARROW = "spirit_alt_arrow";
	
	public static final String TENGU_BOMB = "tengu_bomb";
	public static final String TENGU_SHOCKER = "tengu_shocker";
	public static final String GEO_BOULDER = "geo_boulder";

	public static final String POUL_WATER = "poul_water";

	public static final String BONES = "bones";
	public static final String REMAINS = "remains";
	public static final String TOMB = "tomb";
	public static final String GRAVE = "grave";
	public static final String CHEST = "chest";
	public static final String LOCKED_CHEST = "locked_chest";
	public static final String CRYSTAL_CHEST = "crystal_chest";
	public static final String EBONY_CHEST = "ebony_chest";

	public static final String ANKH = "ankh";
	public static final String STYLUS = "stylus";
	public static final String SEAL = "seal";
	public static final String TORCH = "torch";
	public static final String BEACON = "beacon";
	public static final String HONEYPOT = "honeypot";
	public static final String SHATTPOT = "shattpot";
	public static final String IRON_KEY = "iron_key";
	public static final String GOLDEN_KEY = "golden_key";
	public static final String CRYSTAL_KEY = "crystal_key";
	public static final String SKELETON_KEY = "skeleton_key";
	public static final String MASK = "mask";
	public static final String CROWN = "crown";
	public static final String AMULET = "amulet";
	public static final String MASTERY = "mastery";
	public static final String KIT = "kit";
	public static final String SEAL_SHARD = "seal_shard";
	public static final String BROKEN_STAFF = "broken_staff";
	public static final String CLOAK_SCRAP = "cloak_scrap";
	public static final String BOW_FRAGMENT = "bow_fragment";
	public static final String BROKEN_HILT = "broken_hilt";
	public static final String TRINKET_CATA = "trinket_cata";
	public static final String TAL_MASTERY = "tal_mastery";

	public static final String BOMB = "bomb";
	public static final String DBL_BOMB = "dbl_bomb";
	public static final String FIRE_BOMB = "fire_bomb";
	public static final String FROST_BOMB = "frost_bomb";
	public static final String REGROWTH_BOMB = "regrowth_bomb";
	public static final String FLASHBANG = "flashbang";
	public static final String SHOCK_BOMB = "shock_bomb";
	public static final String HOLY_BOMB = "holy_bomb";
	public static final String WOOLY_BOMB = "wooly_bomb";
	public static final String NOISEMAKER = "noisemaker";
	public static final String ARCANE_BOMB = "arcane_bomb";
	public static final String SHRAPNEL_BOMB = "shrapnel_bomb";

	public static final String WORN_SHORTSWORD = "worn_shortsword";
	public static final String CUDGEL = "cudgel";
	public static final String GLOVES = "gloves";
	public static final String RAPIER = "rapier";
	public static final String DAGGER = "dagger";
	public static final String MAGES_STAFF = "mages_staff";
	public static final String HOLYANKH = "holyankh";

	public static final String SHORTSWORD = "shortsword";
	public static final String HAND_AXE = "hand_axe";
	public static final String SPEAR = "spear";
	public static final String QUARTERSTAFF = "quarterstaff";
	public static final String DIRK = "dirk";
	public static final String SICKLE = "sickle";


	//Radish Image WEP_TIER2
	public static final String KATAR = "katar";
	public static final String BLADESHIELD = "bladeshield";
	public static final String GLASSSWORD1 = "glasssword1";
	public static final String GLASSSWORD2 = "glasssword2";
	public static final String GLASSSWORD3 = "glasssword3";
	public static final String SILVER_STING = "silver_sting";
	public static final String ROTTEN_LANCE = "rotten_lance";
	public static final String REPAIRV2 = "repairv2";
	public static final String BONE_CLAW = "bone_claw";


	public static final String RLYEH_BOOK = "rlyeh_book";

	public static final String RUNE_SLADE = "rune_slade";

	public static final String SWORD = "sword";
	public static final String MACE = "mace";
	public static final String SCIMITAR = "scimitar";
	public static final String ROUND_SHIELD = "round_shield";
	public static final String SAI = "sai";
	public static final String WHIP = "whip";

	public static final String LONGSWORD = "longsword";
	public static final String BATTLE_AXE = "battle_axe";
	public static final String FLAIL = "flail";
	public static final String RUNIC_BLADE = "runic_blade";
	public static final String ASSASSINS_BLADE = "assassins_blade";
	public static final String CROSSBOW = "crossbow";
	public static final String KATANA = "katana";
	public static final String YAMATO = "yamato";

	//Radish Image WEP_TIER3

	public static final String BEECOMB = "beecomb";
	public static final String WATERWHEEL = "waterwheel";
	public static final String WINGSWORD = "wingsword";

	public static final String LOCK_CHAIN = "lock_chain";

	public static final String DAGGER_S = "dagger_s";
	public static final String SNAKESPEAR = "snakespear";

	public static final String PNEGLOVE_FIVE = "pneglove_five";
	public static final String PNEGLOVE_ACTIVE = "pneglove_active";


	public static final String IAMSB_FLAG = "iamsb_flag";
	public static final String LONG_STARK = "long_stark";

	public static final String GREATSWORD = "greatsword";
	public static final String WAR_HAMMER = "war_hammer";
	public static final String GLAIVE = "glaive";
	public static final String GREATAXE = "greataxe";
	public static final String GREATSHIELD = "greatshield";
	public static final String GAUNTLETS = "gauntlets";
	public static final String WAR_SCYTHE = "war_scythe";
	//Radish Image WEP_TIER4

	public static final String DARKSWORD = "darksword";
	public static final String CRBSC = "crbsc";
	public static final String SKYSPS = "skysps";
	public static final String BLOODBLADE1 = "bloodblade1";
	public static final String BLOODBLADE2 = "bloodblade2";
	public static final String BLOODBLADE3 = "bloodblade3";
	public static final String DIRK_B = "dirk_b";
	public static final String SEEKING = "seeking";
	public static final String HEADCLEAVER = "headcleaver";



	public static final String ENDDAY_KILL = "endday_kill";
	public static final String MORELLO_BOOK = "morello_book";
	public static final String SHADOW_BOOK = "shadow_book"; 	//8 free slots

	public static final String SPIRIT_BOW = "spirit_bow";
	
	public static final String THROWING_SPIKE = "throwing_spike";
	public static final String THROWING_KNIFE = "throwing_knife";
	public static final String THROWING_STONE = "throwing_stone";
	
	public static final String FISHING_SPEAR = "fishing_spear";
	public static final String SHURIKEN = "shuriken";
	public static final String THROWING_CLUB = "throwing_club";
	
	public static final String THROWING_SPEAR = "throwing_spear";
	public static final String BOLAS = "bolas";
	public static final String KUNAI = "kunai";
	
	public static final String JAVELIN = "javelin";
	public static final String TOMAHAWK = "tomahawk";
	public static final String BOOMERANG = "boomerang";
	
	public static final String TRIDENT = "trident";
	public static final String THROWING_HAMMER = "throwing_hammer";
	public static final String FORCE_CUBE = "force_cube";

	//Radish Image WEP_TIER5
	public static final String CROSSBOW_S = "crossbow_s";
	public static final String FOGSWORD = "fogsword";



	public static final String TAIKIG = "taikig";
	public static final String CALLHAMR = "callhamr";
	public static final String GIANTKILL = "giantkill";

	public static final String CUTTERHEAD = "cutterhead";
	public static final String KILL_BOAT = "kill_boat";
	public static final String AXE_D = "axe_d";
	public static final String TONFA = "tonfa";
	public static final String SCYTHE = "scythe";

	public static final String KNIGHT_GS = "knight_gs";

	public static final String DARTS = "darts";  //16 slots
	public static final String DEVILOON_ITEM = "deviloon_item";
	public static final String CHIBAYARI = "chibayari";
	public static final String DUAL_DUEL_DAGGERS = "dual_duel_daggers";
	public static final String ABERFORTH = "aberforth";
	public static final String DARK_SHADOW_SWORD = "dark_shadow_sword";
	public static final String WHITE_KING_GOD_SWORD = "white_king_god_sword";
	public static final String REPLACE_POINT = "replace_point";
	public static final String JUTTE_CHAMPION_WEAPON = "jutte_champion_weapon";
	public static final String TURTLEIR = "turtleir";
	public static final String SUNLESS = "sunless";
	public static final String STARLIGHT = "starlight";
	public static final String WASTELANDEW = "wastelandew";
	public static final String CIRCLE_SWORD = "circle_sword";
	public static final String DART = "dart";
	public static final String ROT_DART = "rot_dart";
	public static final String INCENDIARY_DART = "incendiary_dart";
	public static final String ADRENALINE_DART = "adrenaline_dart";
	public static final String HEALING_DART = "healing_dart";
	public static final String CHILLING_DART = "chilling_dart";
	public static final String SHOCKING_DART = "shocking_dart";
	public static final String POISON_DART = "poison_dart";
	public static final String CLEANSING_DART = "cleansing_dart";
	public static final String PARALYTIC_DART = "paralytic_dart";
	public static final String HOLY_DART = "holy_dart";
	public static final String DISPLACING_DART = "displacing_dart";
	public static final String BLINDING_DART = "blinding_dart";
	
	public static final String ARMOR_CLOTH = "armor_cloth";
	public static final String ARMOR_LEATHER = "armor_leather";
	public static final String ARMOR_MAIL = "armor_mail";
	public static final String ARMOR_SCALE = "armor_scale";
	public static final String ARMOR_PLATE = "armor_plate";
	public static final String ARMOR_WARRIOR = "armor_warrior";
	public static final String ARMOR_MAGE = "armor_mage";
	public static final String ARMOR_ROGUE = "armor_rogue";
	public static final String ARMOR_HUNTRESS = "armor_huntress";
	public static final String ARMOR_DUELIST = "armor_duelist";
	public static final String ARMOR_CRAB = "armor_crab";
	public static final String ARMOR_AFTERGLOW = "armor_afterglow";
	public static final String ARMOR_RAT = "armor_rat";
	public static final String ARMOR_GREYFEATHER = "armor_greyfeather";
	public static final String ARMOR_AFTERIMAGE = "armor_afterimage";
	public static final String ARMOR_PRISON = "armor_prison";
	public static final String ARMOR_ENERGY1 = "armor_energy1";
	public static final String ARMOR_ENERGY2 = "armor_energy2";
	public static final String ARMOR_SILVERSCALE = "armor_silverscale";
	public static final String ARMOR_BLACKCOAT = "armor_blackcoat";
	public static final String ARMOR_MOONLIGHT = "armor_moonlight";

	public static final String WAND_MAGIC_MISSILE = "wand_magic_missile";
	public static final String WAND_FIREBOLT = "wand_firebolt";
	public static final String WAND_FROST = "wand_frost";
	public static final String WAND_LIGHTNING = "wand_lightning";
	public static final String WAND_DISINTEGRATION = "wand_disintegration";
	public static final String WAND_PRISMATIC_LIGHT = "wand_prismatic_light";
	public static final String WAND_CORROSION = "wand_corrosion";
	public static final String WAND_LIVING_EARTH = "wand_living_earth";
	public static final String WAND_BLAST_WAVE = "wand_blast_wave";
	public static final String WAND_CORRUPTION = "wand_corruption";
	public static final String WAND_WARDING = "wand_warding";
	public static final String WAND_REGROWTH = "wand_regrowth";
	public static final String WAND_TRANSFUSION = "wand_transfusion";
	public static final String WAND_GNOLL = "wand_gnoll";
	public static final String WAND_BOMBWAVES = "wand_bombwaves";
	public static final String WAND_NEWSTAR = "wand_newstar";

	public static final String RING_GARNET = "ring_garnet";
	public static final String RING_RUBY = "ring_ruby";
	public static final String RING_TOPAZ = "ring_topaz";
	public static final String RING_EMERALD = "ring_emerald";
	public static final String RING_ONYX = "ring_onyx";
	public static final String RING_OPAL = "ring_opal";
	public static final String RING_TOURMALINE = "ring_tourmaline";
	public static final String RING_SAPPHIRE = "ring_sapphire";
	public static final String RING_AMETHYST = "ring_amethyst";
	public static final String RING_QUARTZ = "ring_quartz";
	public static final String RING_AGATE = "ring_agate";
	public static final String RING_DIAMOND = "ring_diamond";
	public static final String RING_GOLD = "ring_gold";
	public static final String RING_CORAL = "ring_coral";
	public static final String RING_PEARL = "ring_pearl";

	public static final String RING_SKYLUE = "ring_skylue";

	public static final String ARTIFACT_CLOAK = "artifact_cloak";
	public static final String ARTIFACT_ARMBAND = "artifact_armband";
	public static final String ARTIFACT_CAPE = "artifact_cape";
	public static final String ARTIFACT_TALISMAN = "artifact_talisman";
	public static final String ARTIFACT_HOURGLASS = "artifact_hourglass";
	public static final String ARTIFACT_TOOLKIT = "artifact_toolkit";
	public static final String ARTIFACT_SPELLBOOK = "artifact_spellbook";
	public static final String ARTIFACT_BEACON = "artifact_beacon";
	public static final String ARTIFACT_CHAINS = "artifact_chains";
	public static final String ARTIFACT_HORN1 = "artifact_horn1";
	public static final String ARTIFACT_HORN2 = "artifact_horn2";
	public static final String ARTIFACT_HORN3 = "artifact_horn3";
	public static final String ARTIFACT_HORN4 = "artifact_horn4";
	public static final String ARTIFACT_CHALICE1 = "artifact_chalice1";
	public static final String ARTIFACT_CHALICE2 = "artifact_chalice2";
	public static final String ARTIFACT_CHALICE3 = "artifact_chalice3";
	//ARTIFACTS Second Line
	public static final String ARTIFACT_SANDALS = "artifact_sandals";
	public static final String ARTIFACT_SHOES = "artifact_shoes";
	public static final String ARTIFACT_BOOTS = "artifact_boots";
	public static final String ARTIFACT_GREAVES = "artifact_greaves";
	public static final String ARTIFACT_ROSE1 = "artifact_rose1";
	public static final String ARTIFACT_ROSE2 = "artifact_rose2";
	public static final String ARTIFACT_ROSE3 = "artifact_rose3";

	public static final String ARTIFACT_CONCEAL = "artifact_conceal";
	public static final String ARTIFACT_ELTIE1 = "artifact_eltie1";
	public static final String ARTIFACT_ELTIE2 = "artifact_eltie2";
	public static final String ARTIFACT_ELTIE3 = "artifact_eltie3";
	public static final String ARTIFACT_ELTIE4 = "artifact_eltie4";
	public static final String ARTIFACT_ELTIE5 = "artifact_eltie5";
	public static final String ARTIFACT_ELTIE6 = "artifact_eltie6";
	public static final String ARTIFACT_ELTIE7 = "artifact_eltie7";

	public static final String MAGNETIC_CROWN = "magnetic_crown";

	public static final String BLESS_SCROLL = "bless_scroll";
	public static final String SNAKE_BITED_YENDOR = "snake_bited_yendor";
	public static final String SNAKE_BITE = "snake_bite";
	public static final String SNAKE_BITE_AMULET = "snake_bite_amulet";
	public static final String SNAKE_BITED_AMULET = SNAKE_BITE_AMULET;

	public static final String MAKESHIFT_SLINGSHOT = "makeshift_slingshot";
	public static final String DOG_LEG = "dog_leg";
	public static final String LAW_FRAGMENT = "law_fragment";
	public static final String SOUL_EMBER = "soul_ember";
	public static final String MECHANICAL_FRAGMENT = "mechanic_shard";
	public static final String BONE_PILE = "bone_pile";
	public static final String FLASH_CRYSTAL = "flash_crystal";
	public static final String HEAVY_CANNON = "heavy_cannon";
	public static final String BONE_SPEAR = "bone_spear";
	public static final String GRAPPLING_HOOK = "grappling_hook";

	public static final String ARTIFACT_WHEELCHAIR = "artifact_wheelchair";

	public static final String RAT_SKULL = "rat_skull";
	public static final String PARCHMENT_SCRAP = "parchment_scrap";
	public static final String PETRIFIED_SEED = "petrified_seed";
	public static final String EXOTIC_CRYSTALS = "exotic_crystals";
	public static final String MOSSY_CLUMP = "mossy_clump";
	public static final String SUNDIAL = "sundial";
	public static final String CLOVER = "clover";
	public static final String TRAP_MECHANISM = "trap_mechanism";
	public static final String MIMIC_TOOTH = "mimic_tooth";
	public static final String WONDROUS_RESIN = "wondrous_resin";
	public static final String EYE_OF_NEWT = "eye_of_newt";
	public static final String SALT_CUBE = "salt_cube";
	public static final String OBLIVION_SHARD = "oblivion_shard";
	public static final String CHAOTIC_CENSER = "chaotic_censer";

	public static final String RADISH = "radish";
	public static final String GOLD_RADISH = "gold_radish";
	public static final String FERRET_TUFT = "ferret_tuft";
	public static final String SPYGLASS = "spyglass";

	public static final String LIGHT_KING = "light_king";
	public static final String RIVER_GLASS = "river_glass";

	public static final String SCROLL_KAUNAN = "scroll_kaunan";
	public static final String SCROLL_SOWILO = "scroll_sowilo";
	public static final String SCROLL_LAGUZ = "scroll_laguz";
	public static final String SCROLL_YNGVI = "scroll_yngvi";
	public static final String SCROLL_GYFU = "scroll_gyfu";
	public static final String SCROLL_RAIDO = "scroll_raido";
	public static final String SCROLL_ISAZ = "scroll_isaz";
	public static final String SCROLL_MANNAZ = "scroll_mannaz";
	public static final String SCROLL_NAUDIZ = "scroll_naudiz";
	public static final String SCROLL_BERKANAN = "scroll_berkanan";
	public static final String SCROLL_ODAL = "scroll_odal";
	public static final String SCROLL_TIWAZ = "scroll_tiwaz";

	public static final String ARCANE_RESIN = "arcane_resin";

	public static final String EXOTIC_KAUNAN = "exotic_kaunan";
	public static final String EXOTIC_SOWILO = "exotic_sowilo";
	public static final String EXOTIC_LAGUZ = "exotic_laguz";
	public static final String EXOTIC_YNGVI = "exotic_yngvi";
	public static final String EXOTIC_GYFU = "exotic_gyfu";
	public static final String EXOTIC_RAIDO = "exotic_raido";
	public static final String EXOTIC_ISAZ = "exotic_isaz";
	public static final String EXOTIC_MANNAZ = "exotic_mannaz";
	public static final String EXOTIC_NAUDIZ = "exotic_naudiz";
	public static final String EXOTIC_BERKANAN = "exotic_berkanan";
	public static final String EXOTIC_ODAL = "exotic_odal";
	public static final String EXOTIC_TIWAZ = "exotic_tiwaz";

	public static final String SPELL_QUEUE_ON = "spell_queue_on";
	public static final String SPELL_QUEUE_OFF = "spell_queue_off";

	public static final String STONE_AGGRESSION = "stone_aggression";
	public static final String STONE_AUGMENTATION = "stone_augmentation";
	public static final String STONE_FEAR = "stone_fear";
	public static final String STONE_BLAST = "stone_blast";
	public static final String STONE_BLINK = "stone_blink";
	public static final String STONE_CLAIRVOYANCE = "stone_clairvoyance";
	public static final String STONE_SLEEP = "stone_sleep";
	public static final String STONE_DISARM = "stone_disarm";
	public static final String STONE_ENCHANT = "stone_enchant";
	public static final String STONE_FLOCK = "stone_flock";
	public static final String STONE_INTUITION = "stone_intuition";
	public static final String STONE_SHOCK = "stone_shock";

	public static final String POTION_CRIMSON = "potion_crimson";
	public static final String POTION_AMBER = "potion_amber";
	public static final String POTION_GOLDEN = "potion_golden";
	public static final String POTION_JADE = "potion_jade";
	public static final String POTION_TURQUOISE = "potion_turquoise";
	public static final String POTION_AZURE = "potion_azure";
	public static final String POTION_INDIGO = "potion_indigo";
	public static final String POTION_MAGENTA = "potion_magenta";
	public static final String POTION_BISTRE = "potion_bistre";
	public static final String POTION_CHARCOAL = "potion_charcoal";
	public static final String POTION_SILVER = "potion_silver";
	public static final String POTION_IVORY = "potion_ivory";

	public static final String LIQUID_METAL = "liquid_metal";
	public static final String POTION_CATALYST = "potion_catalyst";
	
	public static final String EXOTIC_CRIMSON = "exotic_crimson";
	public static final String EXOTIC_AMBER = "exotic_amber";
	public static final String EXOTIC_GOLDEN = "exotic_golden";
	public static final String EXOTIC_JADE = "exotic_jade";
	public static final String EXOTIC_TURQUOISE = "exotic_turquoise";
	public static final String EXOTIC_AZURE = "exotic_azure";
	public static final String EXOTIC_INDIGO = "exotic_indigo";
	public static final String EXOTIC_MAGENTA = "exotic_magenta";
	public static final String EXOTIC_BISTRE = "exotic_bistre";
	public static final String EXOTIC_CHARCOAL = "exotic_charcoal";
	public static final String EXOTIC_SILVER = "exotic_silver";
	public static final String EXOTIC_IVORY = "exotic_ivory";

	public static final String SEED_ROTBERRY = "seed_rotberry";
	public static final String SEED_FIREBLOOM = "seed_firebloom";
	public static final String SEED_SWIFTTHISTLE = "seed_swiftthistle";
	public static final String SEED_SUNGRASS = "seed_sungrass";
	public static final String SEED_ICECAP = "seed_icecap";
	public static final String SEED_STORMVINE = "seed_stormvine";
	public static final String SEED_SORROWMOSS = "seed_sorrowmoss";
	public static final String SEED_MAGEROYAL = "seed_mageroyal";
	public static final String SEED_EARTHROOT = "seed_earthroot";
	public static final String SEED_STARFLOWER = "seed_starflower";
	public static final String SEED_FADELEAF = "seed_fadeleaf";
	public static final String SEED_BLINDWEED = "seed_blindweed";
	public static final String BREW_INFERNAL = "brew_infernal";
	public static final String BREW_BLIZZARD = "brew_blizzard";
	public static final String BREW_SHOCKING = "brew_shocking";
	public static final String BREW_CAUSTIC = "brew_caustic";
	public static final String BREW_AQUA = "brew_aqua";
	public static final String BREW_UNSTABLE = "brew_unstable";

	public static final String MAGIC_ROOT = "magic_root";
	
	public static final String ELIXIR_HONEY = "elixir_honey";
	public static final String ELIXIR_AQUA = "elixir_aqua";
	public static final String ELIXIR_MIGHT = "elixir_might";
	public static final String ELIXIR_DRAGON = "elixir_dragon";
	public static final String ELIXIR_TOXIC = "elixir_toxic";
	public static final String ELIXIR_ICY = "elixir_icy";
	public static final String ELIXIR_ARCANE = "elixir_arcane";
	public static final String ELIXIR_FEATHER = "elixir_feather";
	public static final String WILD_ENERGY = "wild_energy";
	public static final String PHASE_SHIFT = "phase_shift";
	public static final String TELE_GRAB = "tele_grab";
	public static final String UNSTABLE_SPELL = "unstable_spell";

	public static final String CURSE_INFUSE = "curse_infuse";
	public static final String MAGIC_INFUSE = "magic_infuse";
	public static final String ALCHEMIZE = "alchemize";
	public static final String RECYCLE = "recycle";

	public static final String RECLAIM_TRAP = "reclaim_trap";
	public static final String RETURN_BEACON = "return_beacon";
	public static final String SUMMON_ELE = "summon_ele";

	//浮空
	public static final String FEATHER_FALL = "feather_fall";
	public static final String AQUA_BLAST = "aqua_blast";

	public static final String MEAT = "meat";
	public static final String STEAK = "steak";
	public static final String STEWED = "stewed";
	public static final String OVERPRICED = "overpriced";
	public static final String CARPACCIO = "carpaccio";
	public static final String RATION = "ration";
	public static final String PASTY = "pasty";
	public static final String MEAT_PIE = "meat_pie";
	public static final String BLANDFRUIT = "blandfruit";
	public static final String BLAND_CHUNKS = "bland_chunks";
	public static final String BERRY = "berry";
	public static final String PHANTOM_MEAT = "phantom_meat";
	public static final String SUPPLY_RATION = "supply_ration";
	public static final String STEAMED_FISH = "steamed_fish";
	public static final String FISH_LEFTOVER = "fish_leftover";
	public static final String CHOC_AMULET = "choc_amulet";
	public static final String EASTER_EGG = "easter_egg";
	public static final String RAINBOW_POTION = "rainbow_potion";
	public static final String SHATTERED_CAKE = "shattered_cake";
	public static final String PUMPKIN_PIE = "pumpkin_pie";
	public static final String VANILLA_CAKE = "vanilla_cake";
	public static final String CANDY_CANE = "candy_cane";
	public static final String SPARKLING_POTION = "sparkling_potion";
	public static final String DUST = "dust";
	public static final String CANDLE = "candle";
	public static final String EMBER = "ember";
	public static final String PICKAXE = "pickaxe";
	public static final String ORE = "ore";
	public static final String TOKEN = "token";
	public static final String BLOB = "blob";
	public static final String SHARD = "shard";

	public static final String SPOTOA = "spotoa";

	public static final String WATERSKIN = "waterskin";
	public static final String BACKPACK = "backpack";
	public static final String POUCH = "pouch";
	public static final String HOLDER = "holder";
	public static final String BANDOLIER = "bandolier";
	public static final String HOLSTER = "holster";
	public static final String VIAL = "vial";
	public static final String HERB_MAKER = "herb_maker";
	public static final String HERB = "herb";

	public static final String STONE_CRAD = "stone_crad";
	public static final String SEED_CARD = "seed_card";

	public static final String CORRECT = "correct";
	public static final String LIGHTIMUEE = "lightimuee";
	public static final String CLEAN = "clean";
	public static final String PRAYERS = "prayers";

	public static final String APOWER = "apower";
	public static final String BACKMESSAGE = "backmessage";
	public static final String DEADMODE = "deadmode";


	public static final String BLESS = "bless";
	public static final String HOLYFIRE = "holyfire";
	public static final String HOLYLAND = "holyland";

	public static final String GUIDE_PAGE = "guide_page";
	public static final String ALCH_PAGE = "alch_page";
	public static final String SEWER_PAGE = "sewer_page";
	public static final String PRISON_PAGE = "prison_page";
	public static final String CAVES_PAGE = "caves_page";
	public static final String CITY_PAGE = "city_page";
	public static final String HALLS_PAGE = "halls_page";
	public static final String LENGDS_PAGE = "lengds_page";

	//for smaller 8x8 icons that often accompany an item sprite
	public static class Icons {

		public static final int SIZE = 8;
		public static final AtlasSource ATLAS_SOURCE =
				new AtlasSource("sprites/item_icons", "ring_accuracy");
		private static final RuntimeAtlas ATLAS = RuntimeAtlasRegistry.get(ATLAS_SOURCE);
		public static final String RING_ACCURACY = "ring_accuracy";
		public static final String RING_ARCANA = "ring_arcana";
		public static final String RING_ELEMENTS = "ring_elements";
		public static final String RING_ENERGY = "ring_energy";
		public static final String RING_EVASION = "ring_evasion";
		public static final String RING_FORCE = "ring_force";
		public static final String RING_FUROR = "ring_furor";
		public static final String RING_HASTE = "ring_haste";
		public static final String RING_MIGHT = "ring_might";
		public static final String RING_SHARPSHOOT = "ring_sharpshoot";
		public static final String RING_TENACITY = "ring_tenacity";
		public static final String RING_WEALTH = "ring_wealth";
		public static final String RING_KING = "ring_king";
		public static final String RING_COMPRESSION = "ring_compression";
		public static final String RING_DESTRUCTION = "ring_destruction";

		public static final String SCROLL_UPGRADE = "scroll_upgrade";
		public static final String SCROLL_IDENTIFY = "scroll_identify";
		public static final String SCROLL_REMCURSE = "scroll_remcurse";
		public static final String SCROLL_MIRRORIMG = "scroll_mirrorimg";
		public static final String SCROLL_RECHARGE = "scroll_recharge";
		public static final String SCROLL_TELEPORT = "scroll_teleport";
		public static final String SCROLL_LULLABY = "scroll_lullaby";
		public static final String SCROLL_MAGICMAP = "scroll_magicmap";
		public static final String SCROLL_RAGE = "scroll_rage";
		public static final String SCROLL_RETRIB = "scroll_retrib";
		public static final String SCROLL_TERROR = "scroll_terror";
		public static final String SCROLL_TRANSMUTE = "scroll_transmute";
		public static final String SCROLL_ENCHANT = "scroll_enchant";
		public static final String SCROLL_DIVINATE = "scroll_divinate";
		public static final String SCROLL_ANTIMAGIC = "scroll_antimagic";
		public static final String SCROLL_PRISIMG = "scroll_prisimg";
		public static final String SCROLL_MYSTENRG = "scroll_mystenrg";
		public static final String SCROLL_PASSAGE = "scroll_passage";
		public static final String SCROLL_SIREN = "scroll_siren";
		public static final String SCROLL_FORESIGHT = "scroll_foresight";
		public static final String SCROLL_CHALLENGE = "scroll_challenge";
		public static final String SCROLL_PSIBLAST = "scroll_psiblast";
		public static final String SCROLL_DREAD = "scroll_dread";
		public static final String SCROLL_METAMORPH = "scroll_metamorph";

		public static final String POTION_STRENGTH = "potion_strength";
		public static final String POTION_HEALING = "potion_healing";
		public static final String POTION_MINDVIS = "potion_mindvis";
		public static final String POTION_FROST = "potion_frost";
		public static final String POTION_LIQFLAME = "potion_liqflame";
		public static final String POTION_TOXICGAS = "potion_toxicgas";
		public static final String POTION_HASTE = "potion_haste";
		public static final String POTION_INVIS = "potion_invis";
		public static final String POTION_LEVITATE = "potion_levitate";
		public static final String POTION_PARAGAS = "potion_paragas";
		public static final String POTION_PURITY = "potion_purity";
		public static final String POTION_EXP = "potion_exp";
		public static final String POTION_MASTERY = "potion_mastery";
		public static final String POTION_SHIELDING = "potion_shielding";
		public static final String POTION_MAGISIGHT = "potion_magisight";
		public static final String POTION_SNAPFREEZ = "potion_snapfreez";
		public static final String POTION_DRGBREATH = "potion_drgbreath";
		public static final String POTION_CORROGAS = "potion_corrogas";
		public static final String POTION_STAMINA = "potion_stamina";
		public static final String POTION_SHROUDFOG = "potion_shroudfog";
		public static final String POTION_STRMCLOUD = "potion_strmcloud";
		public static final String POTION_EARTHARMR = "potion_eartharmr";
		public static final String POTION_CLEANSE = "potion_cleanse";
		public static final String POTION_DIVINE = "potion_divine";

		public static AtlasFrame frame(String name) {
			return ATLAS.frame(name);
		}

		public static Image image(String name) {
			AtlasFrame frame = frame(name);
			Image image = new Image();
			image.texture = frame.texture;
			image.frame(frame.uv);
			return image;
		}
	}

}
