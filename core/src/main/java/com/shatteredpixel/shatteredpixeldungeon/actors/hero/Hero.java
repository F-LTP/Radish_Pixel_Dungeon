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

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Bones;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SacrificialFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.AshKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight.WeaponMasteryTalent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Frog;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WhitePlasticChair;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.events.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Artillerist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.GnollZealot;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Mayfly;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.CheckedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.events.BeforeHeroMoveEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.EventManager;
import com.shatteredpixel.shatteredpixeldungeon.events.HeroMoveEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.AfterGlow;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.CloakofGreyFeather;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.CrabArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.DarkCoat;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.EnergyArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PrisonArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.RatArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CapeOfThorns;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfConcealment;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.journal.Guidebook;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.toys.TieredToyEffects;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.InversionBeta;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Showdarker;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDivineInspiration;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DarkGold;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.SmallWoodenCross;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfConcentration;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfElements;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.talentitem.SpellQueue;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Radish;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RiverCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Sprouted_Potato;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Seeking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.CircleSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Flail;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.FogSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.JutteChampionWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.KillBoatSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.LockChain;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.LongStick;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Rlyeh;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RoundShield;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scimitar;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Taijutsu;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.TippedDart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AlchemyScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.StatusPane;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndResurrect;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.Delayer;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class Hero extends Char {

	{
		actPriority = HERO_PRIO;

		alignment = Alignment.ALLY;
	}

	public static final int MAX_LEVEL = 30;

	public static final int STARTING_STR = 10;

	private static final float TIME_TO_REST		    = 1f;
	private static final float TIME_TO_SEARCH	    = 2f;
	private static final float HUNGER_FOR_SEARCH	= 6f;

	public HeroClass heroClass = HeroClass.ROGUE;
	public HeroSubClass subClass = HeroSubClass.NONE;
	public ArmorAbility armorAbility = null;
	public ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
	public LinkedHashMap<Talent, Talent> metamorphedTalents = new LinkedHashMap<>();

	private int attackSkill = 10;
	private int defenseSkill = 5;

	public boolean ready = false;

	public boolean rectorDeadKngithDeadMode = false;

	public boolean damageInterrupt = true;
	public HeroAction curAction = null;
	public HeroAction lastAction = null;

	private Char enemy;

	public boolean resting = false;

	public Belongings belongings;

	public int STR;

	public float awareness;

	public int lvl = 1;
	public int exp = 0;
	private int resistHealth = 0;
	private int originalHT = 20;

	public int HTBoost = 0;

	/**
	 * [CRIT BR POWER]
	 */
	public float csBoost=0;
	public boolean sniperSpecial = false;

	/**
	 * [IMP POWER]
	 */
	public boolean powerOfImp = false;

	private ArrayList<Mob> visibleEnemies;

	//This list is maintained so that some logic checks can be skipped
	// for enemies we know we aren't seeing normally, resulting in better performance
	public ArrayList<Mob> mindVisionEnemies = new ArrayList<>();

	// Talent: Superstition by DoggingDog on 20250817
	public static class SuperstitionCounter{
		int cnt;

		public SuperstitionCounter(){
			cnt = 0;
		}

		float briefRet(int exp){
			int m = 24 - 4 * hero.pointsInTalent(Talent.SUPERSTITION);
			cnt += exp;
			if(cnt >= m){
				cnt = m % cnt;
				return 1f;
			}
			return 0;
		}
	}
	SuperstitionCounter superstitionCounter = null;
	//

	public Hero() {
		super();

		HP = HT = 20;
		STR = STARTING_STR;

		critSkill=5f;

		belongings = new Belongings( this );

		visibleEnemies = new ArrayList<>();
	}

	public void updateHT( boolean boostHP ){
		int curHT = HT;

		// 基础成长系数
		int growthFactor = 5;
		int initialHP = 20;
		// 月华英雄成长调整（强壮肉体天赋）
		if (heroClass == HeroClass.MOONLIGHT) {
			initialHP = 18;
			growthFactor = 4; // 基础成长
			int strongBody = pointsInTalent(Talent.STRONG_BODY);
			if (strongBody != 0) initialHP = 20;
			if (strongBody >= 1) growthFactor = 5; // +1恢复至正常
			if (strongBody >= 2) growthFactor = 6; // +2额外成长
		}


		HT = initialHP + growthFactor*(lvl-1) + HTBoost;
		if (buff(ScarBuff.class) != null) {
			HT += ScarBuff.MAX_HP_BONUS;
		}
		if (buff(BarkskinToyBuff.class) != null) {
			HT -= BarkskinToyBuff.MAX_HP_PENALTY;
		}
		float multiplier = RingOfMight.HTMultiplier(this);
		HT = Math.round(multiplier * HT);


		if (buff(ElixirOfMight.HTBoost.class) != null){
			HT += buff(ElixirOfMight.HTBoost.class).boost();
		}

		if (buff(Sprouted_Potato.Potato_Poison.class) != null){
			HT -= buff(Sprouted_Potato.Potato_Poison.class).level();
		}

		if (boostHP){
			HP += Math.max(HT - curHT, 0);
		}

		// DoggingDog on 20250914
		if(belongings.misc instanceof InversionBeta || belongings.ring instanceof InversionBeta){
			HT = InversionBeta.betaHP(this);
		}
		HT = TieredToyEffects.adjustMaxHealth(HT);

		HP = Math.min(HP, HT);
	}


	public int STR() {
		int strBonus = 0;

		strBonus += RingOfMight.strengthBonus( this );

		AdrenalineSurge buff = buff(AdrenalineSurge.class);
		if (buff != null){
			strBonus += buff.boost();
		}



		return STR + strBonus;
	}

	@Override
	public float critSkill(){
		if (buff(RingOfTenacity.Tenacity.class)!=null) {return 0;}
		float critbonus=0;

		if(hero.belongings.weapon() instanceof Taijutsu && critDamage()>=3f){
			return 100f;
		}

		if(hero.belongings.weapon() instanceof LongStick){
			float Boost = 0;
			if(Dungeon.hero!=null){
				Boost += Dungeon.hero.defenseSkill(new Rat());
				Boost -= Dungeon.hero.lvl;
				Boost = Math.max(Boost,0);
				Boost = Math.min(100f,Boost);
			}
			critbonus += Boost;
		}

		// Radish 饰品提供的暴击率
		Radish.GlobalCritChance globalCritChance = buff(Radish.GlobalCritChance.class);
		if(globalCritChance != null){
			critbonus += globalCritChance.critChance;
		}

		return critSkill+critbonus;
	}
	public void updateCritSkill(){
		critSkill = 5 + 0.5f*(lvl-1) + csBoost;
		float bonus= RingOfConcentration.critBonus(this);
		critSkill+=bonus;
	}
	@Override
	public float critDamage(){
		float cdbouns=0;
		cdbouns += RingOfConcentration.critDamgeBonus(this);

		float calm_bouns = 0;
		if(buff(Calm.class)!=null){
			calm_bouns = 0.02f;
		}

		return Math.min(critDamage+cdbouns,critDamageCap) * ( 1 + calm_bouns * 100 * ((float) (HT-HP) /HT)) ;
	}

	public int critDamage_shown(){
		return (int)(100*critDamage());
	}

	private static final String CLASS       = "class";
	private static final String SUBCLASS    = "subClass";
	private static final String ABILITY     = "armorAbility";

	private static final String IMP_POWER     = "imp_power";

	private static final String ATTACK		= "attackSkill";
	private static final String DEFENSE		= "defenseSkill";
	private static final String STRENGTH	= "STR";
	private static final String LEVEL		= "lvl";
	private static final String EXPERIENCE	= "exp";
	private static final String HTBOOST     = "htboost";

	private static final String DEADKNGITH = "deadknight";

	@Override
	public void storeInBundle( Bundle bundle ) {

		super.storeInBundle( bundle );

		bundle.put( CLASS, heroClass );
		bundle.put( SUBCLASS, subClass );
		bundle.put( ABILITY, armorAbility );
		bundle.put( IMP_POWER, powerOfImp);
		Talent.storeTalentsInBundle( bundle, this );

		bundle.put( ATTACK, attackSkill );
		bundle.put( DEFENSE, defenseSkill );

		bundle.put( STRENGTH, STR );

		bundle.put( LEVEL, lvl );
		bundle.put( EXPERIENCE, exp );

		bundle.put( HTBOOST, HTBoost );

		bundle.put(DEADKNGITH, rectorDeadKngithDeadMode );

		belongings.storeInBundle( bundle );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {

		lvl = bundle.getInt( LEVEL );
		exp = bundle.getInt( EXPERIENCE );

		HTBoost = bundle.getInt(HTBOOST);

		rectorDeadKngithDeadMode = bundle.getBoolean(DEADKNGITH);

		super.restoreFromBundle( bundle );

		heroClass = bundle.getEnum( CLASS, HeroClass.class );
		subClass = bundle.getEnum( SUBCLASS, HeroSubClass.class );
		armorAbility = (ArmorAbility)bundle.get( ABILITY );
		powerOfImp = bundle.getBoolean(IMP_POWER);
		Talent.restoreTalentsFromBundle( bundle, this );

		attackSkill = bundle.getInt( ATTACK );
		defenseSkill = bundle.getInt( DEFENSE );

		STR = bundle.getInt( STRENGTH );

		belongings.restoreFromBundle( bundle );
	}

	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		info.level = bundle.getInt( LEVEL );
		info.str = bundle.getInt( STRENGTH );
		info.exp = bundle.getInt( EXPERIENCE );
		info.hp = bundle.getInt( Char.TAG_HP );
		info.ht = bundle.getInt( Char.TAG_HT );
		info.shld = bundle.getInt( Char.TAG_SHLD );
		info.heroClass = bundle.getEnum( CLASS, HeroClass.class );
		info.subClass = bundle.getEnum( SUBCLASS, HeroSubClass.class );
		Belongings.preview( info, bundle );
	}

	public boolean hasTalent( Talent talent ){
		return pointsInTalent(talent) > 0;
	}

	public int pointsInTalent( Talent talent ){
		for (LinkedHashMap<Talent, Integer> tier : talents){
			for (Talent f : tier.keySet()){
				if (f == talent) return tier.get(f);
			}
		}
		return 0;
	}

	public void upgradeTalent( Talent talent ){
		for (LinkedHashMap<Talent, Integer> tier : talents){
			for (Talent f : tier.keySet()){
				if (f == talent) tier.put(talent, tier.get(talent)+1);
			}
		}
		Talent.onTalentUpgraded(this, talent);
	}

	public int talentPointsSpent(int tier){
		int total = 0;
		for (int i : talents.get(tier-1).values()){
			total += i;
		}
		return total;
	}

	public int talentPointsAvailable(int tier){
		if (lvl < (Talent.tierLevelThresholds[tier] - 1)
				|| (tier == 3 && subClass == HeroSubClass.NONE)
				|| (tier == 4 && (armorAbility == null && !powerOfImp))) {
			return 0;
		} else if (lvl >= Talent.tierLevelThresholds[tier+1]){
			return Talent.tierLevelThresholds[tier+1] - Talent.tierLevelThresholds[tier] - talentPointsSpent(tier) + bonusTalentPoints(tier);
		} else {
			return 1 + lvl - Talent.tierLevelThresholds[tier] - talentPointsSpent(tier) + bonusTalentPoints(tier);
		}
	}

	public int bonusTalentPoints(int tier){
		int powerget=0;
		if (powerOfImp && tier ==4) powerget=2;
		if (lvl < (Talent.tierLevelThresholds[tier]-1)
				|| (tier == 3 && subClass == HeroSubClass.NONE)
				|| (tier == 4 && (armorAbility == null && !powerOfImp))) {
			return 0;
		} else if (buff(PotionOfDivineInspiration.DivineInspirationTracker.class) != null
				&& buff(PotionOfDivineInspiration.DivineInspirationTracker.class).isBoosted(tier)) {
			return 2+powerget;
		} else {
			return 0+powerget;
		}
	}

	public String className() {
		return subClass == null || subClass == HeroSubClass.NONE ? heroClass.title() : subClass.title();
	}

	@Override
	public String name(){
		return className();
	}

	@Override
	public void hitSound(float pitch) {
		if (RingOfForce.getBuffedBonus(this, RingOfForce.Force.class) > 0) {
			//pitch deepens by 2.5% (additive) per point of strength, down to 75%
			super.hitSound( pitch * GameMath.gate( 0.75f, 1.25f - 0.025f*STR(), 1f) );
		} else if (hero.belongings.weapon != null) {
			hero.belongings.weapon.hitSound(pitch);
		} else {
			super.hitSound(pitch * 1.1f);
		}
	}

	@Override
	public boolean blockSound(float pitch) {
		if ( belongings.weapon() != null && belongings.weapon().defenseFactor(this) >= 4 ){
			Sample.INSTANCE.play( Assets.Sounds.HIT_PARRY, 1, pitch);
			return true;
		}
		return super.blockSound(pitch);
	}

	public void live() {
		for (Buff b : buffs()){
			if (!b.revivePersists) b.detach();
		}
		if (hasTalent(Talent.HOLD_BREATH)) Buff.affect(this, Talent.HoldBreathTracker.class);
		if (hasTalent(Talent.SPELL_QUEUE)) Buff.affect(this, SpellQueue.imageListner.class);
		Buff.affect( this, Regeneration.class );
		Buff.affect( this, Hunger.class );



	}

	public int tier() {
		Armor armor = belongings.armor();
		if (armor instanceof ClassArmor){
			return 6;
		} else if (armor != null){
			return armor.tier;
		} else {
			return 0;
		}
	}

	public boolean shoot( Char enemy, MissileWeapon wep ) {

		this.enemy = enemy;
		boolean wasEnemy = enemy.alignment == Alignment.ENEMY
				|| (enemy instanceof Mimic && enemy.alignment == Alignment.NEUTRAL);

		//temporarily set the hero's weapon to the missile weapon being used
		//TODO improve this!
		belongings.thrownWeapon = wep;
		boolean hit = attack( enemy );
		Invisibility.dispel();
		belongings.thrownWeapon = null;

		if (hit && subClass == HeroSubClass.GLADIATOR && wasEnemy){
			Buff.affect( this, Combo.class ).hit(  );
		}

		Talent.HoldBreathTracker hb=buff(Talent.HoldBreathTracker.class);
		if (hb!=null){
			if (hit && enemy.alignment==Alignment.ENEMY){
				hb.clear_cb();
			}
			hb.reduce();
		}

		return hit;
	}

	@Override
	public int attackSkill( Char target ) {
		KindOfWeapon wep = belongings.attackingWeapon();

		float accuracy = 1;
		accuracy *= RingOfAccuracy.accuracyMultiplier( this );

		int killBoatSwordBonus = wep instanceof KillBoatSword ? 1 : 0;
		float talentPointBonus = 0.5f * pointsInTalent(Talent.STRONGMAN);
		if(attackDelay() > 1 && hasTalent(Talent.STRONGMAN)){
			accuracy += accuracy * (attackDelay() + killBoatSwordBonus - 1) * talentPointBonus;
		}

//		if (wep instanceof MissileWeapon){
//			if (Dungeon.level.adjacent( pos, target.pos )) {
//				accuracy *= (0.5f + 0.2f*pointsInTalent(Talent.POINT_BLANK));
//			} else {
//				accuracy *= 1.5f;
//			}
//		}

		if (buff(Scimitar.SwordDance.class) != null){
			accuracy *= 1.50f;
		}

		if (hero.buff(RingOfForce.Force.class) == null) {
			if(wep != null)
				return (int)(attackSkill * accuracy * wep.accuracyFactor( this, target ));
		} else {
			return (int)(attackSkill * accuracy);
		}

		return (int)(attackSkill * accuracy);
	}

	@Override
	public int defenseSkill( Char enemy ) {

		if (buff(Combo.ParryTracker.class) != null){
			if (canAttack(enemy) && !isCharmedBy(enemy)){
				Buff.affect(this, Combo.RiposteTracker.class).enemy = enemy;
			}
			return INFINITE_EVASION;
		}

		if (buff(RoundShield.GuardTracker.class) != null){
			return INFINITE_EVASION;
		}

		float evasion = defenseSkill;

		evasion *= RingOfEvasion.evasionMultiplier( this );

		if (buff(Talent.RestoredAgilityTracker.class) != null){
			if (pointsInTalent(Talent.LIQUID_AGILITY) == 1){
				evasion *= 4f;
			} else if (pointsInTalent(Talent.LIQUID_AGILITY) == 2){
				return INFINITE_EVASION;
			}
		}

		if (buff(Quarterstaff.DefensiveStance.class) != null){
			evasion *= 3;
		}

		if (paralysed > 0) {
			evasion /= 2;
		}

		if (belongings.armor() != null) {
			evasion = belongings.armor().evasionFactor(this, evasion);
		}

		return Math.round(evasion);
	}

	@Override
	public String defenseVerb() {
		Combo.ParryTracker parry = buff(Combo.ParryTracker.class);
		if (parry != null){
			parry.parry();
			return Messages.get(Monk.class, "parried");
		}

		if (hero.belongings.weapon() instanceof FogSword) {
			Buff.affect(hero, Invisibility.class,1f);
		}

		if (buff(RoundShield.GuardTracker.class) != null){
			buff(RoundShield.GuardTracker.class).hasBlocked = true;
			BuffIndicator.refreshHero();
			Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1, Random.Float(0.96f, 1.05f));
			return Messages.get(RoundShield.GuardTracker.class, "guarded");
		}

		return super.defenseVerb();
	}

	@Override
	public int drRoll() {
		int dr = super.drRoll();


		if (hasTalent(Talent.HOLD_FAST)){
			int drBouns = Random.NormalIntRange(0, 2* pointsInTalent(Talent.HOLD_FAST));
			if(buff(Chill.class) != null || buff(Frost.class) != null || buff(Slow.class) != null || buff(Roots.class) != null || buff(Paralysis.class) != null || buff(Cripple.class) != null){
				dr += drBouns * 3;
			}else{
				dr += drBouns;
			}
		}

		if (hasTalent(Talent.MOVING_DEFENSE) && pointsInTalent(Talent.MOVING_DEFENSE)>3)
			if (shielding()>0)
				dr+=Random.NormalIntRange(2,8);

		if(hero.belongings.weapon() instanceof CircleSword){
			dr = 0;
		} else if (belongings.armor() != null) {
			int armDr = Char.combatRoll( belongings.armor().DRMin(), belongings.armor().DRMax());

			RiverCrystal riverGlass = hero.belongings.getItem(RiverCrystal.class);
			if(riverGlass != null){
				int originalArmorDr = Char.combatRoll(belongings.armor().DRMin(), belongings.armor().DRMax());
				int secondRoll = Char.combatRoll(belongings.armor().DRMin(), belongings.armor().DRMax());
				int finalArmorDr = Math.min(originalArmorDr, secondRoll);
				if (STR() < belongings.armor().STRReq()){
					finalArmorDr -= 2*(belongings.armor().STRReq() - STR());
				}

				armDr = finalArmorDr;
            } else {
				if (STR() < belongings.armor().STRReq()){
					armDr -= 2*(belongings.armor().STRReq() - STR());
				}
            }
            if (armDr > 0) dr += armDr;
        }
		dr += ShieldToyBuff.getDRBonus();

		if (belongings.weapon() != null)  {
			int wepDr = Char.combatRoll( 0 , belongings.weapon().defenseFactor( this ) );
			if (STR() < ((Weapon)belongings.weapon()).STRReq()){
				wepDr -= 2*(((Weapon)belongings.weapon()).STRReq() - STR());
			}
			if (wepDr > 0) dr += wepDr;
		}

		// DoggingDog on 20250914
		if(belongings.ring instanceof InversionBeta || belongings.misc instanceof InversionBeta){
			dr = InversionBeta.betaDR(this);
		}

		return dr;
	}

	@Override
	public int damageRoll() {  //TODO FIX
		KindOfWeapon wep = belongings.weapon();
		int dmg;
		if (wep!=null){
			dmg = wep.damageRoll( this );
			if (!(wep instanceof MissileWeapon)) {
				dmg += RingOfForce.armedDamageBonus(this);
				if (hasTalent(Talent.DEVASTATE)){
					if (buff(Combo.class)!=null){
						int c=Math.min(buff(Combo.class).getComboCount(),10);
						dmg+=Random.NormalIntRange(0,pointsInTalent(Talent.DEVASTATE)*c);
					}
				}
			}
		} else {
			dmg = RingOfForce.damageRoll(this);
		}


		if(hero.pointsInTalent(Talent.IRON_SUN)>=1){
			int buffCnt = 0;
			for(Object i: buffs(Buff.class).toArray()) {
				if(!BuffIndicator.NONE.equals(((Buff) i).icon())){
					buffCnt+=3;
				}
			}
			dmg += buffCnt;
		}


		if( attackDelay() >1 && hasTalent(Talent.STRONGMAN)){
			int killBoatSwordBonus = hero.belongings.attackingWeapon() instanceof KillBoatSword ? 1 : 0;
			float pointBonus = 0.33f * pointsInTalent(Talent.STRONGMAN);
			dmg += (int) (dmg * (attackDelay() + killBoatSwordBonus - 1) * pointBonus);
		}

		// 武器掌握天赋伤害加成
		if (heroClass == HeroClass.MOONLIGHT && hasTalent(Talent.WEAPON_MASTERY)) {
			dmg += WeaponMasteryTalent.getBonusDamage(this);
		}

		dmg = Math.round(dmg
				* IronHeartBuff.getDamageMultiplier()
				* ClumsyShoesBuff.getDamageMultiplier()
				* WhetstoneBuff.getDamageMultiplier());

		if (dmg < 0) dmg = 0;

		return dmg;
	}

	@Override
	public float speed() {

		float speed = super.speed();

		speed *= RingOfHaste.speedMultiplier(this);

		for (ChampionHero buff : buffs(ChampionHero.class)){
			speed *= buff.speedFactor();
		}

		//索命弯刀：范围内有敌人时移速翻倍
		AshKing.FatalBladeForm fatalBlade = buff(AshKing.FatalBladeForm.class);
		if (fatalBlade != null && fatalBlade.hasEnemyNearby) {
			speed *= 2f;
		}

		if(Dungeon.hero.pointsInTalent(Talent.LAND_HEART)>=1) {
			if (hero.buff(Talent.HIGHGRSS_SPEED.class) != null){
				speed *= 1.5f;
			}
		}

		if(Dungeon.level.map[pos] == Terrain.HOLY_LAND && Dungeon.hero.pointsInTalent(Talent.SKY_TOWER)>=2){
			speed *= 1.25f;
		}

		if (belongings.armor() != null) {
			speed = belongings.armor().speedFactor(this, speed);
		}

		speed *= ClumsyShoesBuff.getSpeedMultiplier();

		Momentum momentum = buff(Momentum.class);
		if (momentum != null){
			((HeroSprite)sprite).sprint( momentum.freerunning() ? 1.5f : 1f );
			speed *= momentum.speedMultiplier();
		} else {
			((HeroSprite)sprite).sprint( 1f );
		}

		if (hasTalent(Talent.BRISK_PACE)){
			speed*=(1+0.025f*pointsInTalent(Talent.BRISK_PACE)*visibleEnemies());
		}

		NaturesPower.naturesPowerTracker natStrength = buff(NaturesPower.naturesPowerTracker.class);
		if (natStrength != null){
			speed *= (2f + 0.25f*pointsInTalent(Talent.GROWING_POWER));
		}

		speed = AscensionChallenge.modifyHeroSpeed(speed);

		//TS
		if (buff(DarkCoat.myPace.class)!=null) speed=Math.max(1f,speed);

		return speed;

	}

	@Override
	public boolean canSurpriseAttack(){
		KindOfWeapon w = belongings.attackingWeapon();
		if (!(w instanceof Weapon))             return true;

		if (STR() < ((Weapon)w).STRReq())       return false;
		if (w instanceof Flail)                 return false;

		return super.canSurpriseAttack();
	}

	public boolean canAttack(Char enemy){
		if (enemy == null || pos == enemy.pos || !Actor.chars().contains(enemy)) {
			return false;
		}

		for (ChampionHero buff : buffs(ChampionHero.class)){
			if (buff.canAttackWithExtraReach( enemy )){
				return true;
			}
		}

		//神佑长枪形态：攻击距离+1
		AshKing.HolyLanceForm holyLance = buff(AshKing.HolyLanceForm.class);
		if (holyLance != null && Dungeon.level.distance(pos, enemy.pos) <= 2) {
			return true;
		}

		//can always attack adjacent enemies
		if (Dungeon.level.adjacent(pos, enemy.pos)) {
			return true;
		}

		int polearmReach = PolearmBuff.getReachBonus();
		if (polearmReach > 0 && Dungeon.level.distance(pos, enemy.pos) <= 1 + polearmReach) {
			return true;
		}

		KindOfWeapon wep = Dungeon.hero.belongings.attackingWeapon();

		if (wep != null){
			// mod by DoggingDog on 2024-11-13, 4 Seeking enchantment
			if(enemy.buff(Seeking.SeekingBuff.class) != null){
				return true;
			}
			//
			return wep.canReach(this, enemy.pos);
		} else {
			return false;
		}
	}

	public float attackDelay() {
		if (buff(Talent.LethalMomentumTracker.class) != null){
			buff(Talent.LethalMomentumTracker.class).detach();
			return 0;
		}

		float delay = 1f;

		if (buff(Talent.LethalMomentumTracker.class) != null){
			buff(Talent.LethalMomentumTracker.class).detach();
			switch (pointsInTalent(Talent.LETHAL_MOMENTUM)){
				case 1: default:
					break;
				case 2:	delay=1.5f;
			}
		}

        //Normally putting furor speed on unarmed attacks would be unnecessary
        //But there's going to be that one guy who gets a furor+force ring combo
        //This is for that one guy, you shall get your fists of fury!
        float speed = RingOfFuror.attackSpeedMultiplier(this);

        if (hero.buff(SnipersMark.class) != null && hero.hasTalent(Talent.BOW_DULES)) {
            speed += 0.5f;
        }

        //ditto for furor + sword dance!
        if (buff(Scimitar.SwordDance.class) != null){
            speed += 0.6f;
        }

		// DoggingDog on 20250205
		if(belongings.attackingWeapon() != null)
			delay *= belongings.attackingWeapon().delayFactor( this );

		// DoggingDog on 20250520
		if(belongings.armor instanceof Showdarker){
			delay /= (2f + 0.25f * ((Showdarker) belongings.armor).buffedLvl());
		}

		if ( buff(Adrenaline.class) != null) delay /= 1.5f;

		delay *= PolearmBuff.getAttackDelayMultiplier();
		speed *= IronHeartBuff.getAttackSpeedMultiplier();

		// rector skill : gods possession with talent avatar
		// DoggingDog on 20260119
		if(hero != null){
			int t_lvl = hero.pointsInTalent(Talent.AVATAR);
			t_lvl = Math.max(0,t_lvl-1);
			speed *= (1f + ((float) t_lvl)/3);
		}

        return delay/speed;
    }

	@Override
	public void spend( float time ) {
		justMoved = false;
		super.spend(time);
	}

	@Override
	public void spendConstant(float time) {
		justMoved = false;
		super.spendConstant(time);
	}

	public void spendAndNextConstant(float time ) {
		busy();
		spendConstant( time );
		next();
	}

	public void spendAndNext( float time ) {
		busy();
		spend( time );
		next();
	}

	@Override
	public boolean act() {

		// 发布英雄回合事件
		EventManager.emit(new HeroActEvent(this));

		Radish radish = hero.belongings.getItem(Radish.class);
		Radish.GlobalCritChance globalCritChance = hero.buff(Radish.GlobalCritChance.class);
		if(radish != null && globalCritChance == null){
			Buff.affect(hero, Radish.GlobalCritChance.class);
		}
		//calls to dungeon.observe will also update hero's local FOV.
		fieldOfView = Dungeon.level.heroFOV;

		//粘液逻辑
		ArrayList<Frog.PoulWater> poulWaters = hero.belongings.getAllItems(Frog.PoulWater.class);
		if(poulWaters != null){
			for (Frog.PoulWater w : poulWaters.toArray(new Frog.PoulWater[0])) {
				if(w.cooldown >0){
					w.cooldown--;
				}
				if(w.cooldown <= 0){
					w.detach(hero.belongings.backpack);
				}
			}
		}


		SmallWoodenCross smallWoodenCross = hero.belongings.getItem(SmallWoodenCross.class);
		if (smallWoodenCross != null && Dungeon.smwcLevel()) {
			if(!Statistics.RectorGetHP){
				Buff.affect(hero, VitaeBuff.class).setVitae(6);
				Statistics.RectorGetHP = true;
				GLog.p(Messages.get(smallWoodenCross, "bless"));
			}
		}

		int mobcount = 0;
		if (hero.hasTalent(Talent.HIDE_IN_CROWD)){
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
				if(fieldOfView[mob.pos]){
					mobcount++;
				}
			}
			if(mobcount >= 6 - hero.pointsInTalent(Talent.HIDE_IN_CROWD)){
				if(buff(Talent.HideInCrowdCooldown.class) == null){
					Buff.affect(hero, Invisibility.class,7f);
					Buff.affect(hero, Talent.HideInCrowdCooldown.class, 50f);
				}
			}
		}

		if (hero.hasTalent(Talent.GOD_BODY)){
			for (Buff buff : hero.buffs()) {
				if (buff.type == Buff.buffType.NEGATIVE && buff.isAboutToEnd(-1f) && buff instanceof Corrosion) { // 使用阈值检测方法
					buff.detach();
				} else if (buff.type == Buff.buffType.NEGATIVE && buff.isAboutToEnd(Dungeon.hero.pointsInTalent(Talent.GOD_BODY) == 2 ? 1f : 0f)) { // 使用阈值检测方法
					buff.detach();
				}
			}
		}

		if (buff(Endure.EndureTracker.class) != null){
			buff(Endure.EndureTracker.class).endEnduring();
		}

		if (!ready) {
			//do a full observe (including fog update) if not resting.
			if (!resting || buff(MindVision.class) != null || buff(Awareness.class) != null || Dungeon.hero.hasTalent(Talent.SOUL_NOWIFI)) {
				Dungeon.observe();
			} else {
				//otherwise just directly re-calculate FOV
				Dungeon.level.updateFieldOfView(this, fieldOfView);
			}
		}

		checkVisibleMobs();
		BuffIndicator.refreshHero();
		BuffIndicator.refreshAllBosses();

		if (paralysed > 0) {

			curAction = null;

			spendAndNext( TICK );
			return false;
		}

		boolean actResult;
		if (curAction == null) {

			if (resting) {
				spendConstant( TIME_TO_REST );
				next();
			} else {
				ready();
			}

			//if we just loaded into a level and have a search buff, make sure to process them
			if(Actor.now() == 0){
				if (buff(Foresight.class) != null){
					search(false);
				} else if (buff(TalismanOfForesight.Foresight.class) != null){
					buff(TalismanOfForesight.Foresight.class).checkAwareness();
				}
			}

			actResult = false;

		} else {

			resting = false;

			ready = false;

			if (curAction instanceof HeroAction.Move) {
				actResult = actMove( (HeroAction.Move)curAction );

			} else if (curAction instanceof HeroAction.Interact) {
				actResult = actInteract( (HeroAction.Interact)curAction );

			} else if (curAction instanceof HeroAction.Buy) {
				actResult = actBuy( (HeroAction.Buy)curAction );

			}else if (curAction instanceof HeroAction.PickUp) {
				actResult = actPickUp( (HeroAction.PickUp)curAction );

			} else if (curAction instanceof HeroAction.OpenChest) {
				actResult = actOpenChest( (HeroAction.OpenChest)curAction );

			} else if (curAction instanceof HeroAction.Unlock) {
				actResult = actUnlock((HeroAction.Unlock) curAction);

			} else if (curAction instanceof HeroAction.Mine) {
				actResult = actMine( (HeroAction.Mine)curAction );

			}else if (curAction instanceof HeroAction.LvlTransition) {
				actResult = actTransition( (HeroAction.LvlTransition)curAction );

			} else if (curAction instanceof HeroAction.Attack) {
				actResult = actAttack( (HeroAction.Attack)curAction );

			} else if (curAction instanceof HeroAction.Alchemy) {
				actResult = actAlchemy( (HeroAction.Alchemy)curAction );

			} else {
				actResult = false;
			}
		}

		if(hasTalent(Talent.BARKSKIN) && Dungeon.level.map[pos] == Terrain.FURROWED_GRASS){
			Barkskin.conditionallyAppend(this, (lvl*pointsInTalent(Talent.BARKSKIN))/2, 1 );
		}

		return actResult;
	}

	public void busy() {
		ready = false;
	}

	public void ready() {
		if (sprite.looping()) sprite.idle();
		curAction = null;
		damageInterrupt = true;
		waitOrPickup = false;
		ready = true;
		canSelfTrample = true;

		AttackIndicator.updateState();

		GameScene.ready();
	}

	public void interrupt() {
		if (isAlive() && curAction != null &&
				((curAction instanceof HeroAction.Move && curAction.dst != pos) ||
						(curAction instanceof HeroAction.LvlTransition))) {
			lastAction = curAction;
		}
		curAction = null;
		GameScene.resetKeyHold();
		resting = false;
	}

	public void resume() {
		curAction = lastAction;
		lastAction = null;
		damageInterrupt = false;
		next();
	}

	private boolean canSelfTrample = false;
	public boolean canSelfTrample(){
		return canSelfTrample && !rooted && !flying &&
				//standing in high grass
				(Dungeon.level.map[pos] == Terrain.HIGH_GRASS ||
						//standing in furrowed grass and not huntress
						(heroClass != HeroClass.HUNTRESS && Dungeon.level.map[pos] == Terrain.FURROWED_GRASS) ||
						//standing on a plant
						Dungeon.level.plants.get(pos) != null);
	}

	private boolean actMove( HeroAction.Move action ) {

		if (getCloser( action.dst )) {
			canSelfTrample = false;
			return true;

			//Hero moves in place if there is grass to trample
		} else if (pos == action.dst && canSelfTrample()){
			canSelfTrample = false;
			Dungeon.level.pressCell(pos);
			spendAndNext( 1 / speed() );
			return false;
		} else {
			ready();
			return false;
		}
	}

	private boolean actInteract( HeroAction.Interact action ) {

		Char ch = action.ch;

		if (ch.isAlive() && ch.canInteract(this)) {

			ready();
			sprite.turnTo( pos, ch.pos );
			return ch.interact(this);

		} else {

			if (fieldOfView[ch.pos] && getCloser( ch.pos )) {

				return true;

			} else {
				ready();
				return false;
			}

		}
	}

	private boolean actBuy( HeroAction.Buy action ) {
		int dst = action.dst;
		if (pos == dst) {

			ready();

			Heap heap = Dungeon.level.heaps.get( dst );
			if (heap != null && heap.type == Type.FOR_SALE && heap.size() == 1) {
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndTradeItem( heap ) );
					}
				});
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actAlchemy( HeroAction.Alchemy action ) {
		int dst = action.dst;
		if (Dungeon.level.distance(dst, pos) <= 1) {

			ready();

			AlchemistsToolkit.kitEnergy kit = buff(AlchemistsToolkit.kitEnergy.class);
			if (kit != null && kit.isCursed()){
				GLog.w( Messages.get(AlchemistsToolkit.class, "cursed"));
				return false;
			}

			AlchemyScene.clearToolkit();
			ShatteredPixelDungeon.switchScene(AlchemyScene.class);
			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	//used to keep track if the wait/pickup action was used
	// so that the hero spends a turn even if the fail to pick up an item
	public boolean waitOrPickup = false;

	private boolean actPickUp( HeroAction.PickUp action ) {
		int dst = action.dst;
		if (pos == dst) {

			Heap heap = Dungeon.level.heaps.get( pos );
			if (heap != null) {
				Item item = heap.peek();
				if (item.doPickUp( this )) {
					heap.pickUp();

					if (item instanceof Dewdrop
							|| item instanceof TimekeepersHourglass.sandBag
							|| item instanceof DriedRose.Petal
							|| item instanceof Key
							|| item instanceof Guidebook) {
						//Do Nothing
					} else if (item instanceof DarkGold) {
						DarkGold existing = belongings.getItem(DarkGold.class);
						if (existing != null){
							if (existing.quantity() >= 40) {
								GLog.p(Messages.get(DarkGold.class, "you_now_have", existing.quantity()));
							} else {
								GLog.i(Messages.get(DarkGold.class, "you_now_have", existing.quantity()));
							}
						}
					} else {

						//TODO make all unique items important? or just POS / SOU?
						boolean important = item.unique && item.isIdentified() &&
								(item instanceof Scroll || item instanceof Potion);
						if (important) {
							GLog.p( Messages.capitalize(Messages.get(this, "you_now_have", item.name())) );
						} else {
							GLog.i( Messages.capitalize(Messages.get(this, "you_now_have", item.name())) );
						}
					}

					curAction = null;
				} else {

					if (waitOrPickup) {
						spendAndNextConstant(TIME_TO_REST);
					}

					//allow the hero to move between levels even if they can't collect the item
					if (Dungeon.level.getTransition(pos) != null){
						throwItems();
					} else {
						heap.sprite.drop();
					}

					if (item instanceof Dewdrop
							|| item instanceof TimekeepersHourglass.sandBag
							|| item instanceof DriedRose.Petal
							|| item instanceof Key) {
						//Do Nothing
					} else {
						GLog.newLine();
						GLog.n(Messages.capitalize(Messages.get(this, "you_cant_have", item.name())));
					}

					ready();
				}
			} else {
				ready();
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actOpenChest( HeroAction.OpenChest action ) {
		int dst = action.dst;
		if (Dungeon.level.adjacent( pos, dst ) || pos == dst) {
			path = null;

			Heap heap = Dungeon.level.heaps.get( dst );
			if (heap != null && (heap.type != Type.HEAP && heap.type != Type.FOR_SALE)) {

				if ((heap.type == Type.LOCKED_CHEST && Notes.keyCount(new GoldenKey(Dungeon.depth)) < 1)
						|| (heap.type == Type.CRYSTAL_CHEST && Notes.keyCount(new CrystalKey(Dungeon.depth)) < 1)){

					GLog.w( Messages.get(this, "locked_chest") );
					ready();
					return false;

				}

				switch (heap.type) {
					case TOMB:
						Sample.INSTANCE.play( Assets.Sounds.TOMB );
						PixelScene.shake( 1, 0.5f );
						break;
					case SKELETON:
					case REMAINS:
						break;
					default:
						Sample.INSTANCE.play( Assets.Sounds.UNLOCK );
				}

				sprite.operate( dst );

			} else {
				ready();
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actUnlock( HeroAction.Unlock action ) {
		int doorCell = action.dst;
		if (Dungeon.level.adjacent( pos, doorCell )) {
			path = null;

			boolean hasKey = false;
			int door = Dungeon.level.map[doorCell];

			if (door == Terrain.LOCKED_DOOR
					&& Notes.keyCount(new IronKey(Dungeon.depth)) > 0) {

				hasKey = true;

			} else if (door == Terrain.CRYSTAL_DOOR
					&& Notes.keyCount(new CrystalKey(Dungeon.depth)) > 0) {

				hasKey = true;

			} else if (door == Terrain.LOCKED_EXIT
					&& Notes.keyCount(new SkeletonKey(Dungeon.depth)) > 0) {

				hasKey = true;

			}

			if (hasKey) {

				sprite.operate( doorCell );

				Sample.INSTANCE.play( Assets.Sounds.UNLOCK );

			} else {
				GLog.w( Messages.get(this, "locked_door") );
				ready();
			}

			return false;

		} else if (getCloser( doorCell )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actMine(HeroAction.Mine action){
		if (Dungeon.level.adjacent(pos, action.dst)){
			path = null;
			if ((Dungeon.level.map[action.dst] == Terrain.WALL
					|| Dungeon.level.map[action.dst] == Terrain.WALL_DECO
					|| Dungeon.level.map[action.dst] == Terrain.MINE_CRYSTAL
					|| Dungeon.level.map[action.dst] == Terrain.MINE_BOULDER)
					&& Dungeon.level.insideMap(action.dst)){
				sprite.attack(action.dst, new Callback() {
					@Override
					public void call() {

						boolean crystalAdjacent = false;
						for (int i : PathFinder.NEIGHBOURS8) {
							if (Dungeon.level.map[action.dst + i] == Terrain.MINE_CRYSTAL){
								crystalAdjacent = true;
								break;
							}
						}

						//1 hunger spent total
						if (Dungeon.level.map[action.dst] == Terrain.WALL_DECO){
							DarkGold gold = new DarkGold();
							if (gold.doPickUp( Dungeon.hero )) {
								DarkGold existing = Dungeon.hero.belongings.getItem(DarkGold.class);
								if (existing != null && existing.quantity()%5 == 0){
									if (existing.quantity() >= 40) {
										GLog.p(Messages.get(DarkGold.class, "you_now_have", existing.quantity()));
									} else {
										GLog.i(Messages.get(DarkGold.class, "you_now_have", existing.quantity()));
									}
								}
								spend(-Actor.TICK); //picking up the gold doesn't spend a turn here
							} else {
								Dungeon.level.drop( gold, pos ).sprite.drop();
							}
							PixelScene.shake(0.5f, 0.5f);
							CellEmitter.center( action.dst ).burst( Speck.factory( Speck.STAR ), 7 );
							Sample.INSTANCE.play( Assets.Sounds.EVOKE );
							Level.set( action.dst, Terrain.EMPTY_DECO );

							//mining gold doesn't break crystals
							crystalAdjacent = false;

							//4 hunger spent total
						} else if (Dungeon.level.map[action.dst] == Terrain.WALL){
							buff(Hunger.class).affectHunger(-3);
							PixelScene.shake(0.5f, 0.5f);
							CellEmitter.get( action.dst ).burst( Speck.factory( Speck.ROCK ), 2 );
							Sample.INSTANCE.play( Assets.Sounds.MINE );
							Level.set( action.dst, Terrain.EMPTY_DECO );

							//1 hunger spent total
						} else if (Dungeon.level.map[action.dst] == Terrain.MINE_CRYSTAL){
							Splash.at(action.dst, 0xFFFFFF, 5);
							Sample.INSTANCE.play( Assets.Sounds.SHATTER );
							Level.set( action.dst, Terrain.EMPTY );

							//1 hunger spent total
						} else if (Dungeon.level.map[action.dst] == Terrain.MINE_BOULDER){
							Splash.at(action.dst, 0x555555, 5);
							Sample.INSTANCE.play( Assets.Sounds.MINE, 0.6f );
							Level.set( action.dst, Terrain.EMPTY_DECO );
						}

						for (int i : PathFinder.NEIGHBOURS9) {
							Dungeon.level.discoverable[action.dst + i] = true;
						}
						for (int i : PathFinder.NEIGHBOURS9) {
							GameScene.updateMap( action.dst+i );
						}

						if (crystalAdjacent){
							sprite.parent.add(new Delayer(0.2f){
								@Override
								protected void onComplete() {
									boolean broke = false;
									for (int i : PathFinder.NEIGHBOURS8) {
										if (Dungeon.level.map[action.dst+i] == Terrain.MINE_CRYSTAL){
											Splash.at(action.dst+i, 0xFFFFFF, 5);
											Level.set( action.dst+i, Terrain.EMPTY );
											broke = true;
										}
									}
									if (broke){
										Sample.INSTANCE.play( Assets.Sounds.SHATTER );
									}

									for (int i : PathFinder.NEIGHBOURS9) {
										GameScene.updateMap( action.dst+i );
									}
									spendAndNext(TICK);
									ready();
								}
							});
						} else {
							spendAndNext(TICK);
							ready();
						}

						Dungeon.observe();
					}
				});
			} else {
				ready();
			}
			return false;
		} else if (getCloser( action.dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actTransition(HeroAction.LvlTransition action ) {
		int stairs = action.dst;
		LevelTransition transition = Dungeon.level.getTransition(stairs);

		if (rooted) {
			PixelScene.shake(1, 1f);
			ready();
			return false;

		} else if (!Dungeon.level.locked && transition != null && transition.inside(pos)) {

			if (Dungeon.level.activateTransition(this, transition)){
				curAction = null;
			} else {
				ready();
			}

			return false;

		} else if (getCloser( stairs )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actAttack( HeroAction.Attack action ) {

		enemy = action.target;

		// 检查武器是否可以攻击（特殊武器如斩舰刀可能在此等待）
		if (belongings.weapon != null && !belongings.weapon.actAttack(hero, enemy, action)) {
			// 武器需要等待，已处理等待逻辑
			return false;
		}

		if(belongings.weapon instanceof LockChain) {
			LockChain lk = (LockChain) hero.belongings.weapon;
			if(Dungeon.level.distance( enemy.pos, pos ) <= 1){
				sprite.attack(enemy.pos);
			} else if(Dungeon.level.distance( enemy.pos,pos ) <= lk.RCH) {
				if(chain(enemy.pos)){
					chain(enemy.pos);
				} else {
					ready();
					GLog.w( Messages.get(LockChain.class, "cant_attack_2"));
				}
			} else {
				ready();
				GLog.w( Messages.get(LockChain.class, "cant_attack"));
			}
			return false;
		} else if (isCharmedBy( enemy )){
			GLog.w( Messages.get(Charm.class, "cant_attack"));
			ready();
			return false;
		}

		if (enemy.isAlive() && canAttack( enemy ) && enemy.invisible == 0) {


			sprite.attack( enemy.pos );

			// 攻击成功后，移除斩舰刀的等待 buff
			if (belongings.weapon instanceof KillBoatSword) {
				Buff buff = buff(KillBoatSwordWaitBuff.class);
				if (buff != null) {
					buff.detach();
				}
			}

			return false;

		} else {

			if (fieldOfView[enemy.pos] && getCloser( enemy.pos )) {

				return true;

			} else {
				ready();
				return false;
			}

		}
	}

	public Char enemy(){
		return enemy;
	}

	public void rest( boolean fullRest ) {
		spendAndNextConstant( TIME_TO_REST );
		if (hasTalent(Talent.HOLD_FAST)){
			Buff.affect(this, HoldFast.class);
		}

		// Talent : BraceYourSelf
		// by DoggingDog on 2024-09-21
		if(hasTalent(Talent.BRACE_YOURSELF) && buff(Preparation.class)!=null){
			Buff.affect(this, BraceYourself.class);
		}
		// end

		if (hasTalent(Talent.PATIENT_STRIKE)){
			Buff.affect(Dungeon.hero, Talent.PatientStrikeTracker.class).pos = Dungeon.hero.pos;
		}

		if(hero.belongings.weapon instanceof KillBoatSword && hero.buff(KillBoatSwordWaitBuff.class) == null) {
			Buff.affect(this, KillBoatSwordWaitBuff.class);
			hero.sprite.centerEmitter().burst(EnergyParticle.FACTORY, 15);
			Sample.INSTANCE.play(Assets.Sounds.KILL_BOAT_SWORD_SWING);

			hero.sprite.showStatus(CharSprite.WARNING, Messages.get(KillBoatSword.class, "ready"));
		};
		if (!fullRest) {
			if (sprite != null) {
				sprite.showStatus(CharSprite.DEFAULT, Messages.get(this, "wait"));
			}
		}
		resting = fullRest;
	}

	public void WandOfCorrectNoTime(float chargesPerCast,Char mob){
		if(Random.Float()<=chargesPerCast){
			int fixedDamage = 12 + Dungeon.depth;
			hero.sprite.parent.add(new Beam.DeathRay(hero.sprite.center(), DungeonTilemap.raisedTileCenterToWorld( mob.pos )));
			if (mob.properties().contains(Property.DEMONIC) || mob.properties().contains(Property.UNDEAD)) {
				fixedDamage = (int) (fixedDamage * 1.25f);
			}
			mob.damage(fixedDamage, this);
			mob.sprite.centerEmitter().burst(PurpleParticle.BURST, Random.IntRange(1, 2));
			mob.sprite.flash();
		}
	}

	@Override
	public int attackProc( final Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );

		if (buff(Talent.SpiritBladesTracker.class) != null
				&& Random.Int(10) < 3*pointsInTalent(Talent.SPIRIT_BLADES)){
			SpiritBow bow = belongings.getItem(SpiritBow.class);
			if (bow != null) damage = bow.proc( this, enemy, damage );
			buff(Talent.SpiritBladesTracker.class).detach();
		}

//		KindOfWeapon wept = belongings.weapon();
//		if (wept instanceof CelestialSphere) {
//			int magicDamage;
//			magicDamage = Random.NormalIntRange(wept.min(wept.level()),wept.max(wept.level()));
//			enemy.damage(magicDamage, new DM100.LightningBolt());
//			damage = wept.proc( this, enemy,0 );
//		}

		KindOfWeapon wep;
		wep = belongings.attackingWeapon();

		if (wep != null){
			damage = wep.proc( this, enemy, damage );
			if (wep instanceof JutteChampionWeapon) {
				((JutteChampionWeapon) wep).onSuccessfulHit(this, enemy);
			}
		}

		damage = Talent.onAttackProc( this, enemy, damage );

		//Roll 2 次 投掷武器等相关惩罚
		RiverCrystal riverGlass = hero.belongings.getItem(RiverCrystal.class);
		if(riverGlass != null){
			int dmg = 0;

			if(wep != null){
				int originalDamage = wep.damageRoll(this);
				int secondRoll = Char.combatRoll(belongings.weapon().min(), belongings.weapon().max());
                dmg = Math.min(originalDamage, secondRoll);

				if (!(wep instanceof MissileWeapon)) {
					dmg += RingOfForce.armedDamageBonus(this);
					if (hasTalent(Talent.DEVASTATE)){
						if (buff(Combo.class)!=null){
							int c=Math.min(buff(Combo.class).getComboCount(),10);
							dmg+=Random.NormalIntRange(0,pointsInTalent(Talent.DEVASTATE)*c);
						}
					}
				}

				damage = dmg;
			}
		}




		if (wep instanceof MeleeWeapon || wep == null) {
			switch (hero.pointsInTalent(Talent.PHARCIS_BLESS)){
				case 1:
					WandOfCorrectNoTime(0.13f,enemy);
					break;
				case 2:
					WandOfCorrectNoTime(0.26f,enemy);
					break;
				case 3:
					WandOfCorrectNoTime(0.40f,enemy);
					break;
			}
		}


		switch (subClass) {
			case SNIPER:

				if (!(sniperSpecial) && wep instanceof MissileWeapon && !(wep instanceof SpiritBow.SpiritArrow ||wep instanceof SpiritBow.ALTSpiritArrow) && enemy != this) {

					Actor.add(new Actor() {

						{
							actPriority = VFX_PRIO;
						}

						@Override
						protected boolean act() {
							if (enemy.isAlive()) {
								int bonusTurns = hasTalent(Talent.SHARED_UPGRADES) ? wep.buffedLvl() : 0;
								if( !hero.hasTalent(Talent.BOW_DULES) ){
									Buff.prolong(Hero.this, SnipersMark.class, SnipersMark.DURATION + bonusTurns).set(enemy.id(), bonusTurns);
								} else if(hero.buff(SnipersMark.class)!=null){
									//hero.buff(SnipersMark.class).setSec(enemy.id(), bonusTurns);
									Buff.prolong(Hero.this, SnipersMark.class, SnipersMark.DURATION + bonusTurns).setSec(enemy.id(), bonusTurns);
								}else{
									Buff.prolong(Hero.this, SnipersMark.class, SnipersMark.DURATION + bonusTurns).set(enemy.id(), bonusTurns);
								}
							}
							Actor.remove(this);
							return true;
						}
					});
				}else if(!(sniperSpecial) && (wep instanceof SpiritBow.SpiritArrow ||wep instanceof SpiritBow.ALTSpiritArrow)  && enemy != this){
					if(hasTalent(Talent.BOW_DULES) && pointsInTalent(Talent.BOW_DULES)>=2) {
						boolean ranged = enemy instanceof Tengu || enemy instanceof YogDzewa || enemy instanceof Mayfly || enemy instanceof DM100 || enemy instanceof Shaman || enemy instanceof GnollZealot || enemy instanceof Warlock || enemy instanceof Elemental.FireElemental || enemy instanceof Elemental.ChaosElemental || enemy instanceof Elemental.FrostElemental || enemy instanceof Elemental.ShockElemental || enemy instanceof Artillerist || enemy instanceof Scorpio || enemy instanceof Eye;
						//Mayfly, DM100, Shaman, GnollZealot, Warlock, Elemental.FireElemental , Elemental.NewbornFireElemental , Elemental.ChaosElemental, Elemental.FrostElemental, Elemental.ShockElemental, Artillerist, Acidic, Scorpio ,eye
						//Tengu, YogDzewa

                        if (enemy instanceof Mob && (((Mob) enemy).surprisedBy(this)) ||(pointsInTalent(Talent.BOW_DULES)>=3 && ranged)){

							Actor.add(new Actor() {

								{
									actPriority = VFX_PRIO;
								}

								@Override
								protected boolean act() {
									if (enemy.isAlive()) {
										int bonusTurns = hasTalent(Talent.SHARED_UPGRADES) ? wep.buffedLvl() : 0;
										if (!hero.hasTalent(Talent.BOW_DULES)) {
											Buff.prolong(Hero.this, SnipersMark.class, SnipersMark.DURATION + bonusTurns).set(enemy.id(), bonusTurns);
										} else if (hero.buff(SnipersMark.class) != null) {
											Buff.prolong(Hero.this, SnipersMark.class, SnipersMark.DURATION + bonusTurns).setSec(enemy.id(), bonusTurns);
										}
									}
									Actor.remove(this);
									return true;
								}
							});
						}
					}
				}
				break;
			default:
		}

		if (damage > 0 && subClass == HeroSubClass.BERSERKER){
			Berserk berserk = Buff.affect(this, Berserk.class);
			berserk.damage(damage);
		}

		damage = TieredToyEffects.attackProc(this, enemy, damage);

		return damage;
	}


	public void SlowHealDamage(int damage){
		if(hero.buff(Talent.SlowHealingDeadCooldown.class) == null){
			Buff.affect(hero, Healing.class).setHeal(damage, 0.05f, 0);
		}
	}

	@Override
	public int defenseProc( Char enemy, int damage ) {

		if (belongings.armor() != null) {
			damage = belongings.armor().proc( enemy, this, damage );
		}

		if (subClass == HeroSubClass.GLADIATOR && hasTalent(Talent.DEFENSIVE_STRIKE)){
			if (Random.Float()<0.25F*pointsInTalent(Talent.DEFENSIVE_STRIKE))
				Buff.affect( this, Combo.class ).hit();
		}

		WandOfLivingEarth.RockArmor rockArmor = buff(WandOfLivingEarth.RockArmor.class);
		if (rockArmor != null) {
			damage = rockArmor.absorb(damage);
		}

		return super.defenseProc( enemy, damage );
	}
	boolean isOnly = false;
	@Override
	public void damage( int dmg, Object src ) {
		if (buff(TimekeepersHourglass.timeStasis.class) != null)
			return;

		if (!(src instanceof Hunger || src instanceof Viscosity.DeferedDamage) && damageInterrupt) {
			interrupt();
			resting = false;
		}

		if (hero.pointsInTalent(Talent.MEDART_SPECIALIST) >= 4 ) {
			if(hero.belongings.thrownWeapon instanceof TippedDart){
				dmg = dmg*2;
			}
		}

		if (dmg >= 10) {
			switch (hero.pointsInTalent(Talent.BEN_WORK)) {
				case 1:
					SlowHealDamage((int) (dmg * 0.5f));
					if (hero.buff(Talent.SlowHealingDeadCooldown.class) == null)
						Buff.affect(hero, Talent.SlowHealingDeadCooldown.class, 50f);
					break;
				case 2:
					SlowHealDamage((int) (dmg * 0.7f));
					if (hero.buff(Talent.SlowHealingDeadCooldown.class) == null)
						Buff.affect(hero, Talent.SlowHealingDeadCooldown.class, 40f);
					break;
				case 3:
					SlowHealDamage((int) (dmg * 0.9f));
					if (hero.buff(Talent.SlowHealingDeadCooldown.class) == null)
						Buff.affect(hero, Talent.SlowHealingDeadCooldown.class, 30f);
					break;
			}
		}

		if (this.buff(Drowsy.class) != null){
			Buff.detach(this, Drowsy.class);
			GLog.w( Messages.get(this, "pain_resist") );
		}

		Endure.EndureTracker endure = buff(Endure.EndureTracker.class);
		if (!(src instanceof Char)){
			//reduce damage here if it isn't coming from a character (if it is we already reduced it)
			if (endure != null){
				dmg = Math.round(endure.adjustDamageTaken(dmg));
			}
			//the same also applies to challenge scroll damage reduction
			if (buff(ScrollOfChallenge.ChallengeArena.class) != null){
				dmg *= 0.67f;
			}
		}

		CapeOfThorns.Thorns thorns = buff( CapeOfThorns.Thorns.class );
		if (thorns != null) {
			dmg = thorns.proc(dmg, (src instanceof Char ? (Char)src : null),  this);
		}

		dmg = (int)Math.ceil(dmg * RingOfTenacity.damageMultiplier( this ));
		//TODO improve this when I have proper damage source logic
		if (belongings.armor() != null && belongings.armor().hasGlyph(AntiMagic.class, this)
				&& AntiMagic.RESISTS.contains(src.getClass())){
			dmg -= AntiMagic.drRoll(this, belongings.armor().procLvl());
		}

		if (buff(Talent.WarriorFoodImmunity.class) != null){
			if (pointsInTalent(Talent.IRON_STOMACH) == 1)       dmg = Math.round(dmg*0.25f);
			else if (pointsInTalent(Talent.IRON_STOMACH) == 2)  dmg = Math.round(dmg*0.00f);
		}
		if (hasTalent(Talent.IRON_MUSCLE) && pointsInTalent(Talent.IRON_MUSCLE)>3){
			if (HP*2<HT)  dmg = Math.round(dmg*0.75f);
		}


		if(hasTalent(Talent.PAIN_SCAR) && HP+ shielding() -dmg<=0){
			int point = pointsInTalent(Talent.PAIN_SCAR);
			float ber = 0;

			if(buff(Berserk.class)!=null)
				ber = buff(Berserk.class).getPower();

			Ankh ankh = null;
			for (Ankh i : belongings.getAllItems(Ankh.class)) {
				if (ankh == null || i.isBlessed()) {
					ankh = i;
				}
			}

			boolean canResist = false;

			switch (point){

				case 1:
					if(ber>=0.2f&&(originalHT-resistHealth)>20) {
						canResist = true;
					}
					break;

				case 2:

					if(ber>=0.15f&&(originalHT-resistHealth)>15) {
						canResist = true;
					}
					break;

				case 3:

					if(ber>=0.1f&&(originalHT-resistHealth)>10) {
						canResist = true;
					}
					break;
			}

			int RH = resistHealth;

			if(ankh != null && canResist && !isOnly){
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.ANKH),
								Messages.get(Talent.PAIN_SCAR,"title"),
								Messages.get(Talent.PAIN_SCAR,"desc"),
								Messages.get(Talent.PAIN_SCAR,"prompt"),
								Messages.get(Talent.PAIN_SCAR,"cancel")){
							@Override
							public void onBackPressed() {
								//阻止玩家逃课
							}
							@Override
							protected void onSelect(int index){
								super.onSelect(index);
								isOnly = true;
								if( index == 0 ){
									switch(pointsInTalent(Talent.PAIN_SCAR)){
										case 1:
											HT -= 20;
											isOnly = false;
											buff(Berserk.class).reducePower(0.2f);
											GLog.n(Messages.get(Talent.PAIN_SCAR,"resistDeath"));
											resistHealth +=20;
											return;
										case 2:
											HT -= 15;
											isOnly = false;
											buff(Berserk.class).reducePower(0.15f);
											GLog.n(Messages.get(Talent.PAIN_SCAR,"resistDeath"));
											resistHealth += 15;
											return;
										case 3:
											HT -= 10;
											isOnly = false;
											buff(Berserk.class).reducePower(0.1f);
											GLog.n(Messages.get(Talent.PAIN_SCAR,"resistDeath"));
											resistHealth += 10;
											return;
									}
								}else if(index == 1 ){
									isOnly = false;
									die(false);
								}
							}
						});
					}
				});

				return;
			} else if (canResist) {
				switch(pointsInTalent(Talent.PAIN_SCAR)){
					case 1:
						HT -= 20;
						buff(Berserk.class).reducePower(0.2f);
						GLog.n(Messages.get(Talent.PAIN_SCAR,"resistDeath"));
						resistHealth +=20;
						return;
					case 2:
						HT -= 15;
						buff(Berserk.class).reducePower(0.15f);
						GLog.n(Messages.get(Talent.PAIN_SCAR,"resistDeath"));
						resistHealth += 15;
						return;
					case 3:
						HT -= 10;
						buff(Berserk.class).reducePower(0.1f);
						GLog.n(Messages.get(Talent.PAIN_SCAR,"resistDeath"));
						resistHealth += 10;
						return;
				}
			}
			if(RH < resistHealth) return;

		}



		int preHP = HP + shielding();
		int preTrueHP = HP;

		// DoggingDog
		int minDmg = Dungeon.depth > 20 ? 15 :
				Dungeon.depth > 15 ? 10 :
						Dungeon.depth > 10 ? 6 :
								Dungeon.depth > 5 ? 3 : 1;
		dmg = (Dungeon.isChallenged(Challenges.BAD_POINT) && dmg <= minDmg && src instanceof Mob) ? minDmg : dmg;

		if (TieredToyEffects.preventDeath(this, dmg)) {
			dmg = Math.max(0, HP + shielding() - 1);
		}
		super.damage( dmg, src );
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TieredToyBuff toyState = buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TieredToyBuff.class);
		if (dmg > 0 && isAlive() && TieredToyEffects.has(com.shatteredpixel.shatteredpixeldungeon.items.toys.TieredToy.Spinach.class)
				&& toyState != null && !toyState.spinachUsed()) {
			toyState.useSpinach();
			TieredToyEffects.heal(this, 30);
		}
		int postHP = HP + shielding();
		int effectiveDamage = preHP - postHP;
		int trueDamage=preTrueHP-HP;
		if (effectiveDamage <= 0) return;
		if (trueDamage>0){
			if (this.hasTalent(Talent.EMERGENCY_PROTECTION) ){
				Class<?> srcClass = src.getClass();
				HashSet<Class> resists = new HashSet<>(RingOfElements.RESISTS);
				boolean flag = true;
				for (Class c : resists){
					if (c.isAssignableFrom(srcClass)){
						flag=false;
						break;
					}
				}
				if (flag) Buff.append(this, DeferredShield.class,1f).inc(2*pointsInTalent(Talent.EMERGENCY_PROTECTION));
			}
		}
		//flash red when hit for serious damage.
		float percentDMG = effectiveDamage / (float)preHP; //percent of current HP that was taken
		float percentHP = 1 - ((HT - postHP) / (float)HT); //percent health after damage was taken
		// The flash intensity increases primarily based on damage taken and secondarily on missing HP.
		float flashIntensity = 0.25f * (percentDMG * percentDMG) / percentHP;
		//if the intensity is very low don't flash at all
		if (flashIntensity >= 0.05f){
			flashIntensity = Math.min(1/3f, flashIntensity); //cap intensity at 1/3
			GameScene.flash( (int)(0xFF*flashIntensity) << 16 );
			if (isAlive()) {
				if (flashIntensity >= 1/6f) {
					Sample.INSTANCE.play(Assets.Sounds.HEALTH_CRITICAL, 1/3f + flashIntensity * 2f);
				} else {
					Sample.INSTANCE.play(Assets.Sounds.HEALTH_WARN, 1/3f + flashIntensity * 4f);
				}
			}
		}
	}

	public void checkVisibleMobs() {
		ArrayList<Mob> visible = new ArrayList<>();

		boolean newMob = false;

		Mob target = null;
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (fieldOfView[ m.pos ] && m.alignment == Alignment.ENEMY) {
				visible.add(m);
				if (!visibleEnemies.contains( m )) {
					newMob = true;
				}

				//only do a simple check for mind visioned enemies, better performance
				if ((!mindVisionEnemies.contains(m) && QuickSlotButton.autoAim(m) != -1)
						|| (mindVisionEnemies.contains(m) && new Ballistica( pos, m.pos, Ballistica.PROJECTILE ).collisionPos == m.pos)) {
					if (target == null) {
						target = m;
					} else if (distance(target) > distance(m)) {
						target = m;
					}
					if (m instanceof Snake && Dungeon.level.distance(m.pos, pos) <= 4
							&& !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_EXAMINING)){
						GLog.p(Messages.get(Guidebook.class, "hint"));
						GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_EXAMINING);
						//we set to read here to prevent this message popping up a bunch
						Document.ADVENTURERS_GUIDE.readPage(Document.GUIDE_EXAMINING);
					}
				}
			}
		}

		Char lastTarget = QuickSlotButton.lastTarget;
		if (target != null && (lastTarget == null ||
				!lastTarget.isAlive() || !lastTarget.isActive() ||
				lastTarget.alignment == Alignment.ALLY ||
				!fieldOfView[lastTarget.pos])){
			QuickSlotButton.target(target);
		}

		if (newMob) {
			if (resting){
				Dungeon.observe();
			}
			interrupt();
		}

		visibleEnemies = visible;
	}

	public int visibleEnemies() {
		return visibleEnemies.size();
	}

	public Mob visibleEnemy( int index ) {
		return visibleEnemies.get(index % visibleEnemies.size());
	}

	public ArrayList<Mob> getVisibleEnemies(){
		return new ArrayList<>(visibleEnemies);
	}

	private boolean walkingToVisibleTrapInFog = false;
	
	// 用于记录绕路确认状态
	private boolean detourConfirmed = false;
	private int detourTarget = -1;

	//FIXME this is a fairly crude way to track this, really it would be nice to have a short
	//history of hero actions
	public boolean justMoved = false;

	private boolean getCloser( final int target ) {

		if (target == pos)
			return false;

		if (rooted) {
			PixelScene.shake( 1, 1f );
			return false;
		}

		// 发布移动前事件，允许订阅者取消移动
		BeforeHeroMoveEvent beforeMoveEvent = new BeforeHeroMoveEvent(this, pos, target);
		EventManager.emit(beforeMoveEvent);
		if (beforeMoveEvent.isCancelled()) {
			return false;
		}

		int step = -1;

		if (Dungeon.level.adjacent( pos, target )) {
			if (subClass == HeroSubClass.FREERUNNER){
				Buff.affect(this, Momentum.class).gainStack();
				Buff.affect(this, MoveCount.class).gainStack();
			}
			path = null;

			if (Actor.findChar( target ) == null) {
				if (Dungeon.level.passable[target] || Dungeon.level.avoid[target]) {
					step = target;
				}
				if (walkingToVisibleTrapInFog
						&& Dungeon.level.traps.get(target) != null
						&& Dungeon.level.traps.get(target).visible){
					return false;
				}
			}

		} else {

			boolean newPath = false;
			if (path == null || path.isEmpty() || !Dungeon.level.adjacent(pos, path.getFirst()))
				newPath = true;
			else if (path.getLast() != target)
				newPath = true;
			else {
				if (!Dungeon.level.passable[path.get(0)] || Actor.findChar(path.get(0)) != null) {
					newPath = true;
				}
			}

			if (newPath) {

				int len = Dungeon.level.length();
				boolean[] p = Dungeon.level.passable;
				boolean[] v = Dungeon.level.visited;
				boolean[] m = Dungeon.level.mapped;
				boolean[] passable = new boolean[len];
				for (int i = 0; i < len; i++) {
					passable[i] = p[i] && (v[i] || m[i]);
				}

				PathFinder.Path newpath = Dungeon.findPath(this, target, passable, fieldOfView, true);
				
				// 检测绕路穿过迷雾的情况
				int directDist = Dungeon.level.distance(pos, target);
				if (newpath != null && directDist <= 8 && newpath.size() > directDist * 2) {
					// 存在绕远路，需要询问玩家
					if (!detourConfirmed || detourTarget != target) {
						final int finalTarget = target;
						final PathFinder.Path finalNewpath = newpath;
						Game.runOnRenderThread(new Callback() {
							@Override
							public void call() {
								GameScene.show(new WndOptions(
										Messages.get(Hero.class, "detour_title"),
										Messages.get(Hero.class, "detor_desc"),
										Messages.get(Hero.class, "detour_yes"),
										Messages.get(Hero.class, "detour_no")) {
									@Override
									protected void onSelect(int index) {
										if (index == 0) {
											// 玩家选择绕远路
											detourConfirmed = true;
											detourTarget = finalTarget;
											path = finalNewpath;
										} else {
											// 玩家取消，中断移动
											detourConfirmed = false;
											detourTarget = -1;
											interrupt();
										}
									}
								});
							}
						});
						return false;
					}
					// 已确认绕路
					path = newpath;
				} else if (newpath != null && path != null && newpath.size() > 2*path.size()){
					path = null;
				} else {
					path = newpath;
				}
			}

			if (path == null) return false;
			step = path.removeFirst();

		}

		if (step != -1) {

			float delay = 1 / speed();
			if (subClass == HeroSubClass.FREERUNNER){
				Buff.affect(this, Momentum.class).gainStack();
				Buff.affect(this, MoveCount.class).gainStack();
			}

			if (Dungeon.level.pit[step] && !Dungeon.level.solid[step]
					&& (!flying || buff(Levitation.class) != null && buff(Levitation.class).detachesWithinDelay(delay))){
				if (!Chasm.jumpConfirmed){
					Chasm.heroJump(this);
					interrupt();
				} else {
					flying = false;
					remove(buff(Levitation.class)); //directly remove to prevent cell pressing
					Chasm.heroFall(target);
				}
				canSelfTrample = false;
				return false;
			}

			if (hasTalent(Talent.MOVING_DEFENSE)){
				if (Random.Float()<0.33f*pointsInTalent(Talent.MOVING_DEFENSE)+0.01f){
					if (buff(Barrier.class)==null || buff(Barrier.class).shielding()<lvl)
						Buff.affect(this, Barrier.class).incShield(1);
				}
			}
			if (buff(Preparation.class)!=null){
				buff(Preparation.class).stay=false;
			}
			float speed = speed();
			float speedAdj=1f;
			if (buff(CrabArmor.likeCrab.class)!=null){
				if (pos/Dungeon.level.width()== step/Dungeon.level.width())	speedAdj=1.75f;
				else speedAdj=5f/6f;
			}
			if (speedAdj*speed>4f && pointsInTalent(Talent.STORM_RUSH)>3 ) Buff.affect(this, Levitation.class,1f);

			sprite.move(pos, step);
			move(step);

			spend( 1 / (speed * speedAdj ));
			justMoved = true;

			search(false);

			return true;

		} else {

			return false;

		}

	}

	public boolean handle( int cell ) {

		if (cell == -1) {
			return false;
		}

		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
			Dungeon.level.updateFieldOfView( this, fieldOfView );
		}

		Char ch = Actor.findChar( cell );
		Heap heap = Dungeon.level.heaps.get( cell );

		if (fieldOfView[cell] && ch instanceof WhitePlasticChair) {
			GameScene.examineObject(ch);
			return false;

		} else if (Dungeon.level.map[cell] == Terrain.ALCHEMY && cell != pos) {

			curAction = new HeroAction.Alchemy( cell );

		} else if (fieldOfView[cell] && ch instanceof Mob) {

			if (((Mob) ch).heroShouldInteract()) {
				curAction = new HeroAction.Interact( ch );
			} else {
				curAction = new HeroAction.Attack( ch );
			}

			//TODO perhaps only trigger this if hero is already adjacent? reducing mistaps
		} else if (Dungeon.level instanceof MiningLevel &&
				belongings.getItem(Pickaxe.class) != null &&
				(Dungeon.level.map[cell] == Terrain.WALL
						|| Dungeon.level.map[cell] == Terrain.WALL_DECO
						|| Dungeon.level.map[cell] == Terrain.MINE_CRYSTAL
						|| Dungeon.level.map[cell] == Terrain.MINE_BOULDER)){

			curAction = new HeroAction.Mine( cell );

		} else if (heap != null
				//moving to an item doesn't auto-pickup when enemies are near...
				&& (visibleEnemies.size() == 0 || cell == pos ||
				//...but only for standard heaps. Chests and similar open as normal.
				(heap.type != Type.HEAP && heap.type != Type.FOR_SALE))) {

			switch (heap.type) {
				case HEAP:
					curAction = new HeroAction.PickUp( cell );
					break;
				case FOR_SALE:
					curAction = heap.size() == 1 && heap.peek().value() > 0 ?
							new HeroAction.Buy( cell ) :
							new HeroAction.PickUp( cell );
					break;
				default:
					curAction = new HeroAction.OpenChest( cell );
			}

		} else if (Dungeon.level.map[cell] == Terrain.LOCKED_DOOR || Dungeon.level.map[cell] == Terrain.CRYSTAL_DOOR || Dungeon.level.map[cell] == Terrain.LOCKED_EXIT) {

			curAction = new HeroAction.Unlock( cell );

		} else if (Dungeon.level.getTransition(cell) != null
				//moving to a transition doesn't automatically trigger it when enemies are near
				&& (visibleEnemies.size() == 0 || cell == pos)
				&& !Dungeon.level.locked
				&& !Dungeon.level.plants.containsKey(cell)
				&& (Dungeon.depth < 26 || Dungeon.level.getTransition(cell).type == LevelTransition.Type.REGULAR_ENTRANCE) ) {

			curAction = new HeroAction.LvlTransition( cell );

		}  else {

			if (!Dungeon.level.visited[cell] && !Dungeon.level.mapped[cell]
					&& Dungeon.level.traps.get(cell) != null && Dungeon.level.traps.get(cell).visible) {
				walkingToVisibleTrapInFog = true;
			} else {
				walkingToVisibleTrapInFog = false;
			}

			curAction = new HeroAction.Move( cell );
			lastAction = null;

		}

		return true;
	}

	public void earnExp( int exp, Class source ) {

		//xp granted by ascension challenge is only for on-exp gain effects
		if (source != AscensionChallenge.class) {
			this.exp += exp;
		}

		EventManager.emit(new HeroGainExperienceEvent(this, exp, source));


		// Superstition by DoggingDog on 20250817
		// 天赋：星界沟通
		if(superstitionCounter != null){
			if(hero.heroClass == HeroClass.RECTOR){
				Belief belief = Dungeon.hero.buff(Belief.class);
				if(belief != null){
					belief.getBelief(superstitionCounter.briefRet(exp));
				}
			}
		}


		float percent = exp/(float)maxExp();

		EtherealChains.chainsRecharge chains = buff(EtherealChains.chainsRecharge.class);
		if (chains != null) chains.gainExp(percent);

		CloakOfConcealment.conceal concealment = buff(CloakOfConcealment.conceal.class);
		if (concealment!=null) concealment.gainExp(exp);

		HornOfPlenty.hornRecharge horn = buff(HornOfPlenty.hornRecharge.class);
		if (horn != null) horn.gainCharge(percent);

		AlchemistsToolkit.kitEnergy kit = buff(AlchemistsToolkit.kitEnergy.class);
		if (kit != null) kit.gainCharge(percent);

		MasterThievesArmband.Thievery armband = buff(MasterThievesArmband.Thievery.class);
		if (armband != null) armband.gainCharge(percent);

		Berserk berserk = buff(Berserk.class);
		if (berserk != null) berserk.recover(percent);

		if (source != PotionOfExperience.class) {
			for (Item i : belongings) {
				i.onHeroGainExp(percent, this);
			}
			if (buff(Talent.RejuvenatingStepsFurrow.class) != null){
				buff(Talent.RejuvenatingStepsFurrow.class).countDown(percent*200f);
				if (buff(Talent.RejuvenatingStepsFurrow.class).count() <= 0){
					buff(Talent.RejuvenatingStepsFurrow.class).detach();
				}
			}
			if (buff(ElementalStrike.ElementalStrikeFurrowCounter.class) != null){
				buff(ElementalStrike.ElementalStrikeFurrowCounter.class).countDown(percent*20f);
				if (buff(ElementalStrike.ElementalStrikeFurrowCounter.class).count() <= 0){
					buff(ElementalStrike.ElementalStrikeFurrowCounter.class).detach();
				}
			}
		}

		boolean levelUp = false;
		while (this.exp >= maxExp()) {
			this.exp -= maxExp();
			if (lvl < MAX_LEVEL) {
				lvl++;
				levelUp = true;

				if (buff(ElixirOfMight.HTBoost.class) != null){
					buff(ElixirOfMight.HTBoost.class).onLevelUp();
				}

				updateHT( true );
				attackSkill++;
				defenseSkill++;

			} else {
				Buff.prolong(this, Bless.class, Bless.DURATION);
				this.exp = 0;

				GLog.newLine();
				GLog.p( Messages.get(this, "level_cap"));
				// 这是为了防止30级点强壮肉体无法升级更新血上限的bug，但是真的会有这种情况吗...
				updateHT(false);
				Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
			}

		}

		if (levelUp) {

			if (sprite != null) {
				GLog.newLine();
				GLog.p( Messages.get(this, "new_level") );
				sprite.showStatus( CharSprite.POSITIVE, Messages.get(Hero.class, "level_up") );
				Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
				if (lvl < Talent.tierLevelThresholds[Talent.MAX_TALENT_TIERS+1]){
					GLog.newLine();
					GLog.p( Messages.get(this, "new_talent") );
					StatusPane.talentBlink = 10f;
					WndHero.lastIdx = 1;
				}
			}

			// 发布英雄升级事件
			EventManager.emit(new HeroLevelUpEvent(this, lvl - 1, lvl));

			Item.updateQuickslot();

			Badges.validateLevelReached();
		}
	}

	public int maxExp() {
		return maxExp( lvl );
	}

	public static int maxExp( int lvl ){
		return 5 + lvl * 5;
	}

	public boolean isStarving() {
		return Buff.affect(this, Hunger.class).isStarving();
	}

	@Override
	public boolean add( Buff buff ) {

		if (buff(TimekeepersHourglass.timeStasis.class) != null) {
			return false;
		}

		boolean added = super.add( buff );

		if (sprite != null && added) {
			String msg = buff.heroMessage();
			if (msg != null){
				GLog.w(msg);
			}

			if (buff instanceof Paralysis || buff instanceof Vertigo) {
				interrupt();
			}

		}

		BuffIndicator.refreshHero();

		return added;
	}

	@Override
	public boolean remove( Buff buff ) {
		if (super.remove( buff )) {
			BuffIndicator.refreshHero();
			return true;
		}
		return false;
	}

	@Override
	public float stealth() {
		float stealth = super.stealth();

		if (belongings.armor() != null){
			stealth = belongings.armor().stealthFactor(this, stealth);
		}

		//匿踪斗篷方法
		if (buff(CloakOfConcealment.Disposed.class)!=null)
			stealth-=4f;
		return stealth;
	}

	@Override
	public void die( Object cause ) {

		curAction = null;

		Ankh ankh = null;

		//look for ankhs in player inventory, prioritize ones which are blessed.
		for (Ankh i : belongings.getAllItems(Ankh.class)){
			if (ankh == null || i.isBlessed()) {
				ankh = i;
			}
		}

		if (ankh != null) {
			interrupt();

			if (ankh.isBlessed()) {
				this.HP = HT / 4;

				PotionOfHealing.cure(this);
				Buff.prolong(this, AnkhInvulnerability.class, AnkhInvulnerability.DURATION);

				SpellSprite.show(this, SpellSprite.ANKH);
				GameScene.flash(0x80FFFF40);
				Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
				GLog.w(Messages.get(this, "revive"));
				Statistics.ankhsUsed++;

				ankh.detach(belongings.backpack);

				for (Char ch : Actor.chars()) {
					if (ch instanceof DriedRose.GhostHero) {
						((DriedRose.GhostHero) ch).sayAnhk();
						return;
					}
				}
			} else {

				//this is hacky, basically we want to declare that a wndResurrect exists before
				//it actually gets created. This is important so that the game knows to not
				//delete the run or submit it to rankings, because a WndResurrect is about to exist
				//this is needed because the actual creation of the window is delayed here
				WndResurrect.instance = new Object();
				Ankh finalAnkh = ankh;
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndResurrect(finalAnkh) );
					}
				});

				if (cause instanceof Hero.Doom) {
					((Hero.Doom)cause).onDeath();
				}

				SacrificialFire.Marked sacMark = buff(SacrificialFire.Marked.class);
				if (sacMark != null){
					sacMark.detach();
				}

			}
			return;
		}

		Actor.fixTime();
		super.die( cause );
		reallyDie( cause );
	}

	public static void reallyDie( Object cause ) {

		int length = Dungeon.level.length();
		int[] map = Dungeon.level.map;
		boolean[] visited = Dungeon.level.visited;
		boolean[] discoverable = Dungeon.level.discoverable;

		for (int i=0; i < length; i++) {

			int terr = map[i];

			if (discoverable[i]) {

				visited[i] = true;
				if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {
					Dungeon.level.discover( i );
				}
			}
		}

		Bones.leave();

		Dungeon.observe();
		GameScene.updateFog();

		Dungeon.hero.belongings.identify();

		int pos = Dungeon.hero.pos;

		ArrayList<Integer> passable = new ArrayList<>();
		for (Integer ofs : PathFinder.NEIGHBOURS8) {
			int cell = pos + ofs;
			if ((Dungeon.level.passable[cell] || Dungeon.level.avoid[cell]) && Dungeon.level.heaps.get( cell ) == null) {
				passable.add( cell );
			}
		}
		Collections.shuffle( passable );

		ArrayList<Item> items = new ArrayList<>(Dungeon.hero.belongings.backpack.items);
		for (Integer cell : passable) {
			if (items.isEmpty()) {
				break;
			}

			Item item = Random.element( items );
			Dungeon.level.drop( item, cell ).sprite.drop( pos );
			items.remove( item );
		}

		for (Char c : Actor.chars()){
			if (c instanceof DriedRose.GhostHero){
				((DriedRose.GhostHero) c).sayHeroKilled();
			}
		}

		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.gameOver();
				Sample.INSTANCE.play( Assets.Sounds.DEATH );
			}
		});

		if (cause instanceof Hero.Doom) {
			((Hero.Doom)cause).onDeath();
		}

		Dungeon.deleteGame( GamesInProgress.curSlot, true );
	}

	//effectively cache this buff to prevent having to call buff(...) a bunch.
	//This is relevant because we call isAlive during drawing, which has both performance
	//and thread coordination implications if that method calls buff(...) frequently
	private Berserk berserk;

	@Override
	public boolean isAlive() {

		if (HP <= 0){
			if (berserk == null) berserk = buff(Berserk.class);
			return berserk != null && berserk.berserking();
		} else {
			berserk = null;
			return super.isAlive();
		}
	}

	@Override
	public void move(int step, boolean travelling) {
		int fromPos = pos; // 记录移动前的位置
		boolean wasHighGrass = Dungeon.level.map[step] == Terrain.HIGH_GRASS;

		super.move( step, travelling);

		// 发布移动完成事件（实际移动发生后）
		EventManager.emit(new HeroMoveEvent(this, fromPos, pos));

		if (!flying && travelling) {
			if (Dungeon.level.water[pos]) {
				Sample.INSTANCE.play( Assets.Sounds.WATER, 1, Random.Float( 0.8f, 1.25f ) );
			} else if (Dungeon.level.map[pos] == Terrain.EMPTY_SP) {
				Sample.INSTANCE.play( Assets.Sounds.STURDY, 1, Random.Float( 0.96f, 1.05f ) );
			} else if (Dungeon.level.map[pos] == Terrain.GRASS
					|| Dungeon.level.map[pos] == Terrain.EMBERS
					|| Dungeon.level.map[pos] == Terrain.FURROWED_GRASS){
				if (step == pos && wasHighGrass) {
					Sample.INSTANCE.play(Assets.Sounds.TRAMPLE, 1, Random.Float( 0.96f, 1.05f ) );
				} else {
					Sample.INSTANCE.play( Assets.Sounds.GRASS, 1, Random.Float( 0.96f, 1.05f ) );
				}
			} else {
				Sample.INSTANCE.play( Assets.Sounds.STEP, 1, Random.Float( 0.96f, 1.05f ) );
			}
		}
	}

	@Override
	public void onAttackComplete() {

		if (enemy == null){
			curAction = null;
			super.onAttackComplete();
			return;
		}

		AttackIndicator.target(enemy);
		boolean wasEnemy = enemy.alignment == Alignment.ENEMY
				|| (enemy instanceof Mimic && enemy.alignment == Alignment.NEUTRAL);

		boolean hit = attack( enemy );

		Invisibility.dispel();

		// CancelAttackBuff：攻击不消耗回合
		CancelAttackBuff cancelBuff = buff(CancelAttackBuff.class);
		if (cancelBuff != null) {
			Buff.detach(this, CancelAttackBuff.class);
		} else {
			// 出其不意天赋：十手伏击减少回合消耗
			boolean surpriseAttack = enemy instanceof Mob && ((Mob) enemy).surprisedBy(this);
			if (surpriseAttack
					&& subClass == HeroSubClass.JUTTE_CHAMPION
					&& hasTalent(Talent.SURPRISE_JUTTE)
					&& belongings.attackingWeapon() instanceof JutteChampionWeapon) {
				int points = pointsInTalent(Talent.SURPRISE_JUTTE);
				float delayMultiplier = (points == 2 ? 0.66f : 0.33f);
				spend(attackDelay() * delayMultiplier);
			} else {
				spend(attackDelay());
			}
		}

		if (hit && subClass == HeroSubClass.GLADIATOR && wasEnemy){
			Buff.affect( this, Combo.class ).hit( );
		}

		// DoggingDog on 20250818
		if(!enemy.isAlive() && hero.hasTalent(Talent.ADRENAL_COMBAT) && hero != null){
			Buff.affect(hero, Adrenaline.class,2f+hero.pointsInTalent(Talent.ADRENAL_COMBAT));
		}
		if (!enemy.isAlive() && wasEnemy) {
			TieredToyEffects.onKill(this);
		}
		//

		curAction = null;

		super.onAttackComplete();
	}

	@Override
	public void onMotionComplete() {
		GameScene.checkKeyHold();
	}

	@Override
	public void onOperateComplete() {

		if (curAction instanceof HeroAction.Unlock) {

			int doorCell = ((HeroAction.Unlock)curAction).dst;
			int door = Dungeon.level.map[doorCell];

			if (Dungeon.level.distance(pos, doorCell) <= 1) {
				boolean hasKey = true;
				if (door == Terrain.LOCKED_DOOR) {
					hasKey = Notes.remove(new IronKey(Dungeon.depth));
					if (hasKey) Level.set(doorCell, Terrain.DOOR);
				} else if (door == Terrain.CRYSTAL_DOOR) {
					hasKey = Notes.remove(new CrystalKey(Dungeon.depth));
					if (hasKey) {
						Level.set(doorCell, Terrain.EMPTY);
						Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
						CellEmitter.get( doorCell ).start( Speck.factory( Speck.DISCOVER ), 0.025f, 20 );
					}
				} else {
					hasKey = Notes.remove(new SkeletonKey(Dungeon.depth));
					if (hasKey) Level.set(doorCell, Terrain.UNLOCKED_EXIT);
				}

				if (hasKey) {
					GameScene.updateKeyDisplay();
					GameScene.updateMap(doorCell);
					spend(Key.TIME_TO_UNLOCK);
				}
			}

		} else if (curAction instanceof HeroAction.OpenChest) {

			Heap heap = Dungeon.level.heaps.get( ((HeroAction.OpenChest)curAction).dst );

			if (Dungeon.level.distance(pos, heap.pos) <= 1){
				boolean hasKey = true;
				if (heap.type == Type.SKELETON || heap.type == Type.REMAINS) {
					Sample.INSTANCE.play( Assets.Sounds.BONES );
				} else if (heap.type == Type.LOCKED_CHEST){
					hasKey = Notes.remove(new GoldenKey(Dungeon.depth));
				} else if (heap.type == Type.CRYSTAL_CHEST){
					hasKey = Notes.remove(new CrystalKey(Dungeon.depth));
				}

				if (hasKey) {
					GameScene.updateKeyDisplay();
					heap.open(this);
					spend(Key.TIME_TO_UNLOCK);
				}
			}

		}
		curAction = null;

		if (!ready) {
			super.onOperateComplete();
		}
	}

	@Override
	public boolean isImmune(Class effect) {
		if (effect == Burning.class
				&& belongings.armor() != null
				&& belongings.armor().hasGlyph(Brimstone.class, this)){
			return true;
		}

		if (hasTalent(Talent.STORM_RUSH) && pointsInTalent(Talent.STORM_RUSH)>2){
			if (effect == Cripple.class || effect == Chill.class || effect == Slow.class) return true;
		}

		return super.isImmune(effect);
	}

	@Override
	public boolean isInvulnerable(Class effect) {
		return super.isInvulnerable(effect) || buff(AnkhInvulnerability.class) != null;
	}

	public boolean search( boolean intentional ) {

		if (!isAlive()) return false;

		boolean smthFound = false;

		boolean circular = pointsInTalent(Talent.WIDE_SEARCH) == 1;
		int distance = heroClass == HeroClass.ROGUE ? 2 : 1;
		if (hasTalent(Talent.WIDE_SEARCH)) distance++;

		boolean foresight = buff(Foresight.class) != null;
		boolean foresightScan = foresight && !Dungeon.level.mapped[pos];

		if (foresightScan){
			Dungeon.level.mapped[pos] = true;
		}

		if (foresight) {
			distance = Foresight.DISTANCE;
			circular = true;
		}

		Point c = Dungeon.level.cellToPoint(pos);

		TalismanOfForesight.Foresight talisman = buff( TalismanOfForesight.Foresight.class );
		boolean cursed = talisman != null && talisman.isCursed();

		int[] rounding = ShadowCaster.rounding[distance];

		int left, right;
		int curr;
		for (int y = Math.max(0, c.y - distance); y <= Math.min(Dungeon.level.height()-1, c.y + distance); y++) {
			if (!circular){
				left = c.x - distance;
			} else if (rounding[Math.abs(c.y - y)] < Math.abs(c.y - y)) {
				left = c.x - rounding[Math.abs(c.y - y)];
			} else {
				left = distance;
				while (rounding[left] < rounding[Math.abs(c.y - y)]){
					left--;
				}
				left = c.x - left;
			}
			right = Math.min(Dungeon.level.width()-1, c.x + c.x - left);
			left = Math.max(0, left);
			for (curr = left + y * Dungeon.level.width(); curr <= right + y * Dungeon.level.width(); curr++){

				if ((foresight || fieldOfView[curr]) && curr != pos) {

					if ((foresight && (!Dungeon.level.mapped[curr] || foresightScan))){
						GameScene.effectOverFog(new CheckedCell(curr, foresightScan ? pos : curr));
					} else if (intentional) {
						GameScene.effectOverFog(new CheckedCell(curr, pos));
					}

					if (foresight){
						Dungeon.level.mapped[curr] = true;
					}

					if (Dungeon.level.secret[curr]){

						Trap trap = Dungeon.level.traps.get( curr );
						float chance;

						//searches aided by foresight always succeed, even if trap isn't searchable
						if (foresight){
							chance = 1f;

							//otherwise if the trap isn't searchable, searching always fails
						} else if (trap != null && !trap.canBeSearched){
							chance = 0f;

							//intentional searches always succeed against regular traps and doors
						} else if (intentional){
							chance = 1f;

							//unintentional searches always fail with a cursed talisman
						} else if (cursed) {
							chance = 0f;

							//unintentional trap detection scales from 40% at floor 0 to 30% at floor 25
						} else if (Dungeon.level.map[curr] == Terrain.SECRET_TRAP) {
							chance = 0.4f - (Dungeon.depth / 250f);

							//unintentional door detection scales from 20% at floor 0 to 0% at floor 20
						} else {
							chance = 0.2f - (Dungeon.depth / 100f);
						}

						//don't want to let the player search though hidden doors in tutorial
						if (SPDSettings.intro()){
							chance = 0;
						}

						if (Random.Float() < chance) {

							int oldValue = Dungeon.level.map[curr];

							GameScene.discoverTile( curr, oldValue );

							Dungeon.level.discover( curr );

							ScrollOfMagicMapping.discover( curr );

							if (fieldOfView[curr]) smthFound = true;

							if (talisman != null){
								if (oldValue == Terrain.SECRET_TRAP){
									talisman.charge(2);
								} else if (oldValue == Terrain.SECRET_DOOR){
									talisman.charge(10);
								}
							}
						}
					}
				}
			}
		}

		if (intentional) {
			sprite.showStatus( CharSprite.DEFAULT, Messages.get(this, "search") );
			sprite.operate( pos );
			if (!Dungeon.level.locked) {
				if (cursed) {
					GLog.n(Messages.get(this, "search_distracted"));
					Buff.affect(this, Hunger.class).affectHunger(TIME_TO_SEARCH - (2 * HUNGER_FOR_SEARCH));
				} else {
					Buff.affect(this, Hunger.class).affectHunger(TIME_TO_SEARCH - HUNGER_FOR_SEARCH);
				}
			}
			spendAndNext(TIME_TO_SEARCH);

		}

		if (smthFound) {
			GLog.w( Messages.get(this, "noticed_smth") );
			Sample.INSTANCE.play( Assets.Sounds.SECRET );
			interrupt();
		}

		if (foresight){
			GameScene.updateFog(pos, Foresight.DISTANCE+1);
		}

		if (talisman != null){
			talisman.checkAwareness();
		}

		return smthFound;
	}

	public void resurrect() {
		HP = HT;
		live();

		MagicalHolster holster = belongings.getItem(MagicalHolster.class);

		Buff.affect(this, LostInventory.class);

		//重生后验证 拉莱耶 合理性
		Weapon w2 = (Weapon) hero.belongings.weapon;
		if(w2 instanceof Rlyeh){
			if(w2.keptThoughLostInvent && hero.buff(LostInventory.class) != null){
				Buff.affect(this, Rlyeh.StateProject.class);
			}
		}

		Buff.affect(this, Invisibility.class, 3f);
		//lost inventory is dropped in interlevelscene

		//activate items that persist after lost inventory
		//FIXME this is very messy, maybe it would be better to just have one buff that
		// handled all items that recharge over time?
		for (Item i : belongings){
			if (i instanceof EquipableItem && i.isEquipped(this)){
				((EquipableItem) i).activate(this);
			} else if (i instanceof CloakOfShadows && i.keptThroughLostInventory() && hasTalent(Talent.LIGHT_CLOAK)){
				((CloakOfShadows) i).activate(this);
			} else if (i instanceof Wand && i.keptThroughLostInventory()){
				if (holster != null && holster.contains(i)){
					((Wand) i).charge(this, MagicalHolster.HOLSTER_SCALE_FACTOR);
				} else {
					((Wand) i).charge(this);
				}
			} else if (i instanceof MagesStaff && i.keptThroughLostInventory()){
				((MagesStaff) i).applyWandChargeBuff(this);
			}
		}

		updateHT(false);
	}

	@Override
	public void next() {
		if (isAlive())
			super.next();
	}

	public static interface Doom {
		public void onDeath();
	}

	public void MoveBoatSword(){
		KillBoatSword w2 = (KillBoatSword) hero.belongings.weapon;
		if(w2 !=null) w2.delayAttack = false;
	}

	/**
	 * 拉莱耶文本 自相残杀伤害 英雄<br>
	 * @param enemy 敌人<br>
	 * @param damage 伤害
	 */
	public void RlyehHeroDamage (Char enemy,int damage){
		if(hero.belongings.weapon() instanceof Rlyeh){
			Rlyeh w2 = (Rlyeh) hero.belongings.weapon;
			if(w2.HeroChance()){
				damage(hero.belongings.weapon.damageRoll(hero),new Rlyeh());
				//因为再处理无伤害会更麻烦，所以这里改成打多少回多少
				enemy.HP += Math.min(enemy.HT, damage);
				//一个神奇的特性会导致满血额外+1点血量，所以-1
				if(enemy.HP == enemy.HT){
					enemy.damage(1,new Rlyeh());
				}
				if(!isAlive()){
					Badges.validateDeathFromFriendlyMagic();
				}
			}
		} else {
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
				if (mob instanceof Statue) {
					if(((Statue) mob).weapon instanceof Rlyeh){
						Rlyeh w2 =(Rlyeh) ((Statue) mob).weapon;
						if(w2.HeroChance()){
							damage(damage, new Rlyeh());
							enemy.HP += Math.min(enemy.HT, damage);
							if(!enemy.isAlive() && enemy == hero){
								Badges.validateDeathFromEnemyMagic();
							}
							if(enemy.HP == enemy.HT){
								enemy.damage(1,new Rlyeh());
							}
						}
					}
				}
			}
		}
	}

	public int tier_for_image(){
		Armor armor = belongings.armor();
		if (armor == null ){
			return 0;
		} else if (armor instanceof ClassArmor){
			return 6;
		} else if (armor instanceof PrisonArmor){
			return 7;
		} else if (armor instanceof CrabArmor){
			return 8;
		} else if (armor instanceof DarkCoat){
			return 9;
		} else if (armor instanceof AfterGlow){
			return 10;
		} else if (armor instanceof CloakofGreyFeather){
			return 11;
		} else if (armor instanceof RatArmor){
			return 12;
		} else if (armor instanceof EnergyArmor){
			return ((EnergyArmor) armor).Energy();
		}
		else return armor.tier;
	}



	private boolean chain(int target){
		if (enemy.properties().contains(Property.IMMOVABLE))
			return false;

		Ballistica chain = new Ballistica(pos, target, Ballistica.PROJECTILE);

		if (chain.collisionPos != enemy.pos
				|| chain.path.size() < 2
				|| Dungeon.level.pit[chain.path.get(1)])
			return false;
		else {
			int newPos = -1;
			for (int i : chain.subPath(1, chain.dist)){
				if (!Dungeon.level.solid[i] && Actor.findChar(i) == null){
					newPos = i;
					break;
				}
			}

			if (newPos == -1){
				return false;
			} else {
				final int newPosFinal = newPos;

				if (sprite.visible || enemy.sprite.visible) {
					new Item().throwSound();
					Sample.INSTANCE.play(Assets.Sounds.CHAINS);
					sprite.parent.add(new Chains(sprite.center(),
							enemy.sprite.destinationCenter(),
							Effects.Type.CHAIN,
							new Callback() {
								public void call() {
									Actor.add(new Pushing(enemy, enemy.pos, newPosFinal, new Callback() {
										public void call() {
											pullEnemy(enemy, newPosFinal);
										}
									}));
									next();
								}
							}));
				} else {
					pullEnemy(enemy, newPos);
				}
			}
		}
		return true;
	}

	private void pullEnemy( Char enemy, int pullPos ){
		enemy.pos = pullPos;
		enemy.sprite.place(pullPos);
		Dungeon.level.occupyCell(enemy);
		Cripple.prolong(enemy, Cripple.class, 4f);
		if (enemy == Dungeon.hero) {
			Dungeon.hero.interrupt();
			Dungeon.observe();
			GameScene.updateFog();
		}
	}

	@Override
	public float talentProc(){
		if (hasTalent(Talent.RUNIC_TRANSFERENCE) && (pointsInTalent(Talent.RUNIC_TRANSFERENCE)>1)) return 1.25f;
		return super.talentProc();
	}

}
