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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.challenge.SnakeBiteChallengeManager;
import com.shatteredpixel.shatteredpixeldungeon.events.EventManager;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.MimicTooth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestArenaLevel;import com.shatteredpixel.shatteredpixeldungeon.levels.alterLevel.BloodPrisonLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.alterLevel.FireHallsLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.alterLevel.OldSewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.alterLevel.WarCityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.alterLevel.WorkCaveLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branch;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GnollKingBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.VineTrap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndResurrect;
import com.watabou.noosa.Game;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.watabou.utils.SparseArray;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

public class Dungeon {

	//enum of items which have limited spawns, records how many have spawned
	//could all be their own separate numbers, but this allows iterating, much nicer for bundling/initializing.
	public static enum LimitedDrops {
		//limited world drops
		STRENGTH_POTIONS,
		UPGRADE_SCROLLS,
		ARCANE_STYLI,
		ENCH_STONE,
		INT_STONE,
		TRINKET_CATA,
		LAB_ROOM, //actually a room, but logic is the same

		//Health potion sources
		//enemies
		SWARM_HP,
		NECRO_HP,
		BAT_HP,
		WARLOCK_HP,
		//Demon spawners are already limited in their spawnrate, no need to limit their health drops
		//alchemy
		COOKING_HP,
		BLANDFRUIT_SEED,

		//Other limited enemy drops
		SLIME_WEP,
		SKELE_WEP,
		THEIF_MISC,

		// Date : 2024/07/17
		// DoggingDog
		// for Grudge in RadishNewEnemy
		GRUDGE_WEP,

		CLUSTERED_SKELETON_WEP,
		BONE_CLAW,
		BONE_SPEAR,


		// Date : 2024/07/26
		// DoggingDog
		// for RoyalGuard in RadishNewEnemy
		ROYALGUARD_WEP,


		GUARD_ARM,
		SHAMAN_WAND,
		DM200_EQUIP,
		GOLEM_EQUIP,

		// 闪晶：每种怪物最多掉落6个
		FLASH_CRYSTAL_GNOLL,
		FLASH_CRYSTAL_KING,
		FLASH_CRYSTAL_DWARF,

		//containers
		VELVET_POUCH,
		SCROLL_HOLDER,
		POTION_BANDOLIER,
		MAGICAL_HOLSTER,

		//lore documents
		LORE_SEWERS,
		LORE_PRISON,
		LORE_CAVES,
		LORE_CITY,
		LORE_HALLS,
		LORE_STORS;

		public int count = 0;

		//for items which can only be dropped once, should directly access count otherwise.
		public boolean dropped(){
			return count != 0;
		}
		public void drop(){
			count = 1;
		}

		public static void reset(){
			for (LimitedDrops lim : values()){
				lim.count = 0;
			}
		}

		public static void store( Bundle bundle ){
			for (LimitedDrops lim : values()){
				bundle.put(lim.name(), lim.count);
			}
		}

		public static void restore( Bundle bundle ){
			for (LimitedDrops lim : values()){
				if (bundle.contains(lim.name())){
					lim.count = bundle.getInt(lim.name());
				} else {
					lim.count = 0;
				}
				
			}

			//pre-v2.2.0 saves
			if (Dungeon.version < 750
					&& Dungeon.isChallenged(Challenges.NO_SCROLLS)
					&& UPGRADE_SCROLLS.count > 0){
				//we now count SOU fully, and just don't drop every 2nd one
				UPGRADE_SCROLLS.count += UPGRADE_SCROLLS.count-1;
			}
		}

	}

	public static int challenges;
	public static int mobsToChampion;

	public static Hero hero;
	public static Level level;

	public static QuickSlot quickslot = new QuickSlot();
	
	public static int depth;
	// 当前分支的字符串标识（如 "main", "moss", "mining"）
	public static String branchId = Branches.MAIN;

	/** 循环楼梯每一条链当前轮到第几个（0=A,1=B,2=C） */
	public static final java.util.HashMap<String, Integer> gateChains = new java.util.HashMap<>();

	//keeps track of what levels the game should try to load instead of creating fresh
	public static HashSet<String> generatedLevels = new HashSet<>();
	public static boolean levelJustGenerated;

	// 测试工具（升降器）用：让下一次 newLevel() 直接使用指定的关卡类，
	// 用于在同一层里指定要打哪个 Boss 变体（如第10层的典狱长 / 天狗）。用后自动清空。
	public static Class<? extends Level> forcedLevelClass = null;

	public static int gold;
	public static int energy;
	
	public static HashSet<Integer> chapters;

	public static SparseArray<ArrayList<Item>> droppedItems;
	public static SparseArray<ArrayList<Item>> portedItems;


	//first variable is only assigned when game is started, second is updated every time game is saved
	public static int initialVersion;
	public static int version;

	public static boolean daily;
	public static boolean dailyReplay;
	public static String customSeedText = "";
	public static long seed;
	
	public static void init() {

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MobEnvelope.resetGlobal();

		// 初始化事件系统（自动扫描并注册订阅类）
		EventManager.init();
		
		// 初始化分支系统
		Branches.init();

		initialVersion = version = Game.versionCode;
		challenges = SPDSettings.challenges();
		SnakeBiteChallengeManager.initializeMessageTheme();
		mobsToChampion = -1;

		if (daily) {
			//Ensures that daily seeds are not in the range of user-enterable seeds
			seed = SPDSettings.lastDaily() + DungeonSeed.TOTAL_SEEDS;
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			customSeedText = format.format(new Date(SPDSettings.lastDaily()));
		} else if (!SPDSettings.customSeed().isEmpty()){
			customSeedText = SPDSettings.customSeed();
			seed = DungeonSeed.convertFromText(customSeedText);
		} else {
			customSeedText = "";
			seed = DungeonSeed.randomSeed();
		}

		Actor.clear();
		Actor.resetNextID();

		//offset seed slightly to avoid output patterns
		Random.pushGenerator( seed+1 );

			Scroll.initLabels();
			Potion.initColors();
			Ring.initGems();

			SpecialRoom.initForRun();
			SecretRoom.initForRun();

			Generator.fullReset();

		Random.resetGenerators();
		
		Statistics.reset();
		Notes.reset();

		quickslot.reset();
		QuickSlotButton.reset();
		Toolbar.swappedQuickslots = false;
		
		depth = 1;
		branchId = Branches.MAIN;
		gateChains.clear();
		generatedLevels.clear();
		levelJustGenerated = false;

		gold = 0;
		energy = 0;

		droppedItems = new SparseArray<>();
		portedItems = new SparseArray<>();

		LimitedDrops.reset();
		
		chapters = new HashSet<>();
		
		Ghost.Quest.reset();
		Wandmaker.Quest.reset();
		Blacksmith.Quest.reset();
		Imp.Quest.reset();

		hero = new Hero();
		hero.live();
		
		Badges.reset();
		
		GamesInProgress.selectedClass.initHero( hero );
	}

	public static boolean isChallenged( int mask ) {
		return (challenges & mask) != 0;
	}

	/**
	 * 检查楼层是否已生成
	 */
	public static boolean levelHasBeenGenerated(int depth, String branchId){
		return generatedLevels.contains(branchId + "_" + depth);
	}
	
	/**
	 * 生成楼层键
	 */
	public static String levelKey(String branchId, int depth) {
		return branchId + "_" + depth;
	}
	
	public static Level newLevel() {
		
		levelJustGenerated = true;
		Dungeon.level = null;
		Actor.clear();
		
		Level level;
		boolean randomMap;
		// rolls that shape level content but run before Level.create() must not consume
		// the shared ambient stream, otherwise exploring branches would shift later floors.
		Random.pushGenerator( Dungeon.seedCurDepth()+1 );
			randomMap = Random.Float()>=0.5f && !SPDSettings.origin_map();
		Random.popGenerator();
		
		// 使用 Branch 系统创建楼层
		Branch branch = Branches.get(branchId);
		if (branch != null) {
			level = branch.createLevel(depth);
			// 主线的特殊地图变体处理
			if (branchId.equals(Branches.MAIN) && randomMap && level != null) {
				Level variant = createMainBranchVariant(depth);
				if (variant != null) level = variant;
			}
			} else {
				throw new IllegalStateException("Unknown branch: " + branchId);
			}

		// 测试场：测试模式专用空白地图（不参与正常楼层生成记录）
		if (TestArenaLevel.requested) {
			TestArenaLevel.requested = false;
			level = new TestArenaLevel();
		}

			if (level == null) {
				throw new IllegalStateException("Invalid floor: " + branchId + ":" + depth);
		}
		
		// 第10层的 Boss 选择：典狱长与天狗随机替换出现
		if (branchId.equals(Branches.MAIN) && depth == 10 && level instanceof PrisonBossLevel) {
			Random.pushGenerator( Dungeon.seedCurDepth()+2 );
			boolean warden = Random.Float() < 0.5f;
			Random.popGenerator();
			if (warden) {
				level = new WardenBossLevel();
			}
		}

		// 第15层的 Boss 选择
		if (branchId.equals(Branches.MAIN) && depth == 15) {
			// deterministic per (branch, depth), independent of prior branch exploration
			Random.pushGenerator( Dungeon.seedCurDepth()+1 );
			level = GenerateCaveBossLevel();
			Random.popGenerator();
		}

		// 测试工具（升降器）可强制指定本层的关卡类，用于在同一层里选择要打哪个 Boss 变体。
		// 只影响「这一次生成」，用完立刻清空，不影响正常流程。
		if (forcedLevelClass != null) {
			level = Reflection.newInstance(forcedLevelClass);
			forcedLevelClass = null;
		}

		//dead end levels get cleared, don't count as generated
		if (!(level instanceof DeadEndLevel)){
			String key = levelKey(branchId, depth);
			if (!generatedLevels.contains(key)) {
				generatedLevels.add(key);
			}

			if (depth > Statistics.deepestFloor && branchId.equals(Branches.MAIN)) {
							Statistics.deepestFloor = depth;

							if (Statistics.qualifiedForNoKilling) {
								Statistics.completedWithNoKilling = true;
							} else {
								Statistics.completedWithNoKilling = false;
							}
						}
			
						// 苔藓分支深度追踪
						if (depth > Statistics.deepestMossFloor && branchId.equals(Branches.MOSS)) {
							Statistics.deepestMossFloor = depth;
						}
					}

		Statistics.qualifiedForBossRemainsBadge = false;
		
		level.create();
		
		if (branchId.equals(Branches.MAIN)) Statistics.qualifiedForNoKilling = !bossLevel();
		Statistics.qualifiedForBossChallengeBadge = false;
		
		return level;
	}
	
	/**
	 * 创建主线分支的地图变体
	 */
	private static Level createMainBranchVariant(int depth) {
		switch (depth) {
			case 2:
			case 3:
			case 4:
				return new OldSewerLevel();
			case 7:
			case 8:
			case 9:
				return new BloodPrisonLevel();
			case 12:
			case 13:
			case 14:
				return new WorkCaveLevel();
			case 17:
			case 18:
			case 19:
				return new WarCityLevel();
			case 22:
			case 23:
			case 24:
				return new FireHallsLevel();
			default:
				return null;
		}
	}

    public static Level GenerateCaveBossLevel()
	{
		level = new CavesBossLevel();

        float GNOLL_KING_BOSS_SPAWN_CHANCE = 0.42f;
        boolean shouldSpawnGnollKingBoss =
				( (Random.Float() <= GNOLL_KING_BOSS_SPAWN_CHANCE) && !Statistics.DM300MustBoss )
				|| ( Statistics.gnollMustBoss && !Statistics.DM300MustBoss);

		if (shouldSpawnGnollKingBoss) {
			level = new GnollKingBossLevel();
			Statistics.gnollMustBoss = true;
		} else {
			level = new CavesBossLevel();
			if (!Statistics.DM300MustBoss)
			{
				Statistics.DM300MustBoss = true;
			}
		}
		return level;
	}
	
	public static void resetLevel() {
		
		Actor.clear();
		
		level.reset();
		switchLevel( level, level.entrance() );
	}

	public static long seedCurDepth(){
		return seedForDepth(depth, branchId);
	}

	public static long seedForDepth(int depth, String branchId){
		int lookAhead = depth;
		// 使用 branchId 的 hashCode 来生成不同的种子
		lookAhead += 30 * Math.abs(branchId.hashCode() % 100);

		Random.pushGenerator( seed );

			for (int i = 0; i < lookAhead; i ++) {
				Random.Long(); //we don't care about these values, just need to go through them
			}
			long result = Random.Long();

		Random.popGenerator();
		return result;
	}
	
	public static boolean shopOnLevel() {
		return depth == 6 || depth == 11 || depth == 16;
	}

	public static boolean smwcLevel() {
		return depth == 1 ||depth == 6 || depth == 11 || depth == 16 || depth == 21;
	}
	
	//「当前所在的这一层是不是首领层」。支线的 depth 从 1 重新计数，不能套主线的 5/10/15…：
	//传奇重犯副本整层就是首领战，其余支线层都不是。
	//要问「主线第 N 层是不是首领层」的调用方用下面带参数的重载。
	public static boolean bossLevel() {
		if (!Branches.MAIN.equals( branchId )) {
			return Branches.CONVICT.equals( branchId );
		}
		return bossLevel( depth );
	}
	
	public static boolean bossLevel( int depth ) {
		return depth == 5 || depth == 10 || depth == 15 || depth == 20 || depth == 25;
	}

	//value used for scaling of damage values and other effects.
	//is usually the dungeon depth, but can be set to 26 when ascending
	public static int scalingDepth(){
		if (Dungeon.hero != null && Dungeon.hero.buff(AscensionChallenge.class) != null){
			return 26;
		} else {
			return depth;
		}
	}

	public static boolean interfloorTeleportAllowed(){
		if (depth==0 || !branchId.equals(Branches.MAIN)) return false;
		if (Dungeon.level.locked
				|| Dungeon.level instanceof MiningLevel
				|| (Dungeon.hero != null && Dungeon.hero.belongings.getItem(Amulet.class) != null)){
			return false;
		}
		return true;
	}
	
	public static void switchLevel( final Level level, int pos ) {

		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MobEnvelope.onLevelEnter(depth, branchId);

		//Position of -2 specifically means trying to place the hero the exit
		if (pos == -2){
			LevelTransition t = level.getTransition(LevelTransition.Type.REGULAR_EXIT);
			if (t != null) pos = t.cell();
		}

		//Place hero at the entrance if they are out of the map (often used for pox = -1)
		// or if they are in solid terrain (except in the mining level, where that happens normally)
		if (pos < 0 || pos >= level.length()
				|| (!(level instanceof MiningLevel) && !level.passable[pos] && !level.avoid[pos])){
			pos = level.getTransition(null).cell();
		}
		
		PathFinder.setMapSize(level.width(), level.height());
		
		Dungeon.level = level;
		com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoors.applyLevelFlags(level);

		hero.pos = pos;

		if (hero.buff(AscensionChallenge.class) != null){
			hero.buff(AscensionChallenge.class).onLevelSwitch();
		}

		Mob.restoreAllies( level, pos );

		Actor.init();

		level.addRespawner();
		
		for(Mob m : level.mobs){
			if (m.pos == hero.pos && !Char.hasProp(m, Char.Property.IMMOVABLE)){
				//displace mob
				for(int i : PathFinder.NEIGHBOURS8){
					if (Actor.findChar(m.pos+i) == null && level.passable[m.pos + i]){
						m.pos += i;
						break;
					}
				}
			}
		}
		
		Light light = hero.buff( Light.class );
		hero.viewDistance = light == null ? level.viewDistance : Math.max( Light.DISTANCE, level.viewDistance );
		
		hero.curAction = hero.lastAction = null;

		// 术士4-4 腐化怨灵：进入新层时生成怨灵
		// switchLevel 在读档、回到去过的楼层、复活时也会走到；只有这一层是刚生成的才算「新的一层」，
		// 否则每读一次档、每回一次旧楼层都会再生成一批怨灵
		if (levelJustGenerated) {
			Talent.onNewFloor(hero);
		}

		observe();
		try {
			saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
			/*This only catches IO errors. Yes, this means things can go wrong, and they can go wrong catastrophically.
			But when they do the user will get a nice 'report this issue' dialogue, and I can fix the bug.*/
		}
	}

	public static void dropToChasm( Item item ) {
		int depth = Dungeon.depth + 1;
		ArrayList<Item> dropped = Dungeon.droppedItems.get( depth );
		if (dropped == null) {
			Dungeon.droppedItems.put( depth, dropped = new ArrayList<>() );
		}
		dropped.add( item );
	}

	public static boolean posNeeded() {

		if (depth==0 || !branchId.equals(Branches.MAIN)) return false;

		//2 POS each floor set
		int posLeftThisSet = 2 - (LimitedDrops.STRENGTH_POTIONS.count - (depth / 5) * 2);
		if (posLeftThisSet <= 0) return false;

		int floorThisSet = (depth % 5);

		//pos drops every two floors, (numbers 1-2, and 3-4) with a 50% chance for the earlier one each time.
		int targetPOSLeft = 2 - floorThisSet/2;
		if (floorThisSet % 2 == 1 && Random.Int(2) == 0) targetPOSLeft --;

		if (targetPOSLeft < posLeftThisSet) return true;
		else return false;

	}
	
	public static boolean souNeeded() {

		if (depth==0 || !branchId.equals(Branches.MAIN)) return false;

		int souLeftThisSet;
		//3 SOU each floor set
		souLeftThisSet = 3 - (LimitedDrops.UPGRADE_SCROLLS.count - (depth / 5) * 3);
		if (souLeftThisSet <= 0) return false;

		int floorThisSet = (depth % 5);
		//chance is floors left / scrolls left
		return Random.Int(5 - floorThisSet) < souLeftThisSet;
	}
	
	public static boolean asNeeded() {

		if (depth==0) return false;

		//1 AS each floor set
		int asLeftThisSet = 1 - (LimitedDrops.ARCANE_STYLI.count - (depth / 5));
		if (asLeftThisSet <= 0) return false;

		int floorThisSet = (depth % 5);
		//chance is floors left / scrolls left
		return Random.Int(5 - floorThisSet) < asLeftThisSet;
	}

	public static boolean enchStoneNeeded(){
		//1 enchantment stone, spawns on chapter 2 or 3
		if (!LimitedDrops.ENCH_STONE.dropped()){
			int region = 1+depth/5;
			if (region > 1){
				int floorsVisited = depth - 5;
				if (floorsVisited > 4) floorsVisited--; //skip floor 10
				return Random.Int(9-floorsVisited) == 0; //1/8 chance each floor
			}
		}
		return false;
	}

	public static boolean intStoneNeeded(){
		//one stone on floors 1-3
		return depth < 5 && !LimitedDrops.INT_STONE.dropped() && Random.Int(4-depth) == 0;
	}

	public static boolean trinketCataNeeded(){
		if(depth == 0)return false;
		//one trinket catalyst on floors 1-3
		return depth < 5 && !LimitedDrops.TRINKET_CATA.dropped() && Random.Int(4-depth) == 0;
	}

	public static boolean labRoomNeeded(){
		//one laboratory each floor set, in floor 3 or 4, 1/2 chance each floor
		int region = 1+depth/5;
		if (region > LimitedDrops.LAB_ROOM.count){
			int floorThisRegion = depth%5;
			if (floorThisRegion >= 4 || (floorThisRegion == 3 && Random.Int(2) == 0)){
				return true;
			}
		}
		return false;
	}

	// 1/4
	// 3/4 * 1/3 = 3/12 = 1/4
	// 3/4 * 2/3 * 1/2 = 6/24 = 1/4
	// 1/4

	private static final String INIT_VER	= "init_ver";
	private static final String VERSION		= "version";
	private static final String SEED		= "seed";
	private static final String CUSTOM_SEED	= "custom_seed";
	private static final String DAILY	    = "daily";
	private static final String DAILY_REPLAY= "daily_replay";
	private static final String CHALLENGES	= "challenges";
	private static final String MOBS_TO_CHAMPION	= "mobs_to_champion";
	private static final String HERO		= "hero";
	private static final String DEPTH		= "depth";
	private static final String GATE_CHAINS	= "gate_chains";
	private static final String LEGACY_CYCLE_CHAINS	= "cycle_chains";
		private static final String BRANCH_ID		= "branch_id";
		private static final String GENERATED_LEVELS    = "generated_levels";
		private static final String GOLD		= "gold";
		private static final String ENERGY		= "energy";
		private static final String DROPPED     = "dropped%d";
		private static final String PORTED      = "ported%d";
		private static final String LEVEL		= "level";
		private static final String LIMDROPS    = "limited_drops";
		private static final String CHAPTERS	= "chapters";
		private static final String QUESTS		= "quests";
	private static final String BADGES		= "badges";

	public static class IncompatibleSaveException extends IOException {
		public IncompatibleSaveException(String message) {
			super(message);
		}
	}
	
		public static void saveGame( int save ) {
			try {
				Bundle bundle = new Bundle();

				bundle.put( INIT_VER, initialVersion );
				bundle.put( VERSION, version = Game.versionCode );
				bundle.put( SEED, seed );
				bundle.put( CUSTOM_SEED, customSeedText );
				bundle.put( DAILY, daily );
				bundle.put( DAILY_REPLAY, dailyReplay );
				bundle.put( CHALLENGES, challenges );
				bundle.put( MOBS_TO_CHAMPION, mobsToChampion );
				bundle.put( HERO, hero );
				bundle.put( DEPTH, depth );
				bundle.put( BRANCH_ID, branchId );
				String[] chains = new String[gateChains.size()];
				int ci = 0;
				for (java.util.Map.Entry<String,Integer> e : gateChains.entrySet()) {
					chains[ci++] = e.getKey() + "|" + e.getValue();
				}
				bundle.put( GATE_CHAINS, chains );

				bundle.put( GOLD, gold );
				bundle.put( ENERGY, energy );

				for (int d : droppedItems.keyArray()) {
					bundle.put(Messages.format(DROPPED, d), droppedItems.get(d));
				}

				for (int p : portedItems.keyArray()){
					bundle.put(Messages.format(PORTED, p), portedItems.get(p));
				}


				quickslot.storePlaceholders( bundle );

				Bundle limDrops = new Bundle();
				LimitedDrops.store( limDrops );
				bundle.put ( LIMDROPS, limDrops );
			
				int count = 0;
				int ids[] = new int[chapters.size()];
				for (Integer id : chapters) {
					ids[count++] = id;
				}
				bundle.put( CHAPTERS, ids );
			
				Bundle quests = new Bundle();
				Ghost		.Quest.storeInBundle( quests );
				Wandmaker	.Quest.storeInBundle( quests );
				Blacksmith	.Quest.storeInBundle( quests );
				Imp			.Quest.storeInBundle( quests );
				bundle.put( QUESTS, quests );
			
				SpecialRoom.storeRoomsInBundle( bundle );
				SecretRoom.storeRoomsInBundle( bundle );
			
				Statistics.storeInBundle( bundle );
				Notes.storeInBundle( bundle );
				Generator.storeInBundle( bundle );
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MobEnvelope.storeGlobal( bundle );

				// Save Snake Bite Manager state
				SnakeBiteChallengeManager.save(bundle);
				Messages.storeInBundle(bundle);

				// 保存已生成的楼层（String 格式）
				String[] levelKeys = generatedLevels.toArray(new String[0]);
				bundle.put( GENERATED_LEVELS, levelKeys);

				Scroll.save( bundle );
				Potion.save( bundle );
				Ring.save( bundle );

				Actor.storeNextID( bundle );
			
			Bundle badges = new Bundle();
			Badges.saveLocal( badges );
			bundle.put( BADGES, badges );
			
			FileUtils.bundleToFile( GamesInProgress.gameFile(save), bundle);
			
		} catch (IOException e) {
			GamesInProgress.setUnknown( save );
			ShatteredPixelDungeon.reportException(e);
		}
	}
	
	public static void saveLevel( int save ) throws IOException {
		Bundle bundle = new Bundle();
		bundle.put( LEVEL, level );
		
		FileUtils.bundleToFile(GamesInProgress.depthFile( save, depth, branchId ), bundle);
	}
	
	public static void saveAll() throws IOException {
		if (hero != null && (hero.isAlive() || WndResurrect.instance != null)) {
			
			Actor.fixTime();
			updateLevelExplored();
			saveGame( GamesInProgress.curSlot );
			saveLevel( GamesInProgress.curSlot );

			GamesInProgress.set( GamesInProgress.curSlot );

		}
	}
	
	public static void loadGame( int save ) throws IOException {
		EventManager.init();
		loadGame( save, true );
	}
	
	public static void loadGame( int save, boolean fullLoad ) throws IOException {
		
		Bundle bundle = FileUtils.bundleFromFile( GamesInProgress.gameFile( save ) );

		//pre-1.3.0 saves
		if (bundle.contains(INIT_VER)){
			initialVersion = bundle.getInt( INIT_VER );
		} else {
			initialVersion = bundle.getInt( VERSION );
		}

		version = bundle.getInt( VERSION );

		seed = bundle.contains( SEED ) ? bundle.getLong( SEED ) : DungeonSeed.randomSeed();
		customSeedText = bundle.getString( CUSTOM_SEED );
		daily = bundle.getBoolean( DAILY );
		dailyReplay = bundle.getBoolean( DAILY_REPLAY );

		Actor.clear();
		Actor.restoreNextID( bundle );

		quickslot.reset();
		QuickSlotButton.reset();
		Toolbar.swappedQuickslots = false;

		Dungeon.challenges = bundle.getInt( CHALLENGES );
		Dungeon.mobsToChampion = bundle.getInt( MOBS_TO_CHAMPION );

		// Restore Snake Bite Manager state
		SnakeBiteChallengeManager.restore(bundle);
		if (!Messages.restoreFromBundle(bundle)) {
			SnakeBiteChallengeManager.initializeMessageTheme();
		}

		Dungeon.level = null;
		Dungeon.depth = -1;
		
		Scroll.restore( bundle );
		Potion.restore( bundle );
		Ring.restore( bundle );

		quickslot.restorePlaceholders( bundle );
		
		if (fullLoad) {
			
			LimitedDrops.restore( bundle.getBundle(LIMDROPS) );

			chapters = new HashSet<>();
			int ids[] = bundle.getIntArray( CHAPTERS );
			if (ids != null) {
				for (int id : ids) {
					chapters.add( id );
				}
			}
			
			Bundle quests = bundle.getBundle( QUESTS );
			if (!quests.isNull()) {
				Ghost.Quest.restoreFromBundle( quests );
				Wandmaker.Quest.restoreFromBundle( quests );
				Blacksmith.Quest.restoreFromBundle( quests );
				Imp.Quest.restoreFromBundle( quests );
			} else {
				Ghost.Quest.reset();
				Wandmaker.Quest.reset();
				Blacksmith.Quest.reset();
				Imp.Quest.reset();
			}
			
			SpecialRoom.restoreRoomsFromBundle(bundle);
			SecretRoom.restoreRoomsFromBundle(bundle);
		}
		
		Bundle badges = bundle.getBundle(BADGES);
		if (!badges.isNull()) {
			Badges.loadLocal( badges );
		} else {
			Badges.reset();
		}
		
		Notes.restoreFromBundle( bundle );
		
		//动作按钮槽是全局的。槽里如果还留着上一个英雄对象的按钮（读档前那个状态实例），
		//新读出来的状态看到「槽已被占」就不会再放自己的按钮，读档后按钮点了没反应或者不出现。
		//所以恢复英雄之前先清空，由各状态在恢复时重新占位。
		//（从存档界面「继续」时窗口那边已经清过一次；这里保证所有读档入口都一样）
		ActionIndicator.clearAction();

		hero = null;
		hero = (Hero)bundle.get( HERO );
		
		depth = bundle.getInt( DEPTH );
		branchId = bundle.getString(BRANCH_ID);

		gateChains.clear();
		String[] chains = bundle.getStringArray(GATE_CHAINS);
		if (chains == null) {
			chains = bundle.getStringArray(LEGACY_CYCLE_CHAINS);
		}
		if (chains != null) {
			for (String s : chains) {
				String[] parts = s.split("\\|");
				if (parts.length == 2) gateChains.put(parts[0], Integer.parseInt(parts[1]));
			}
		}
		if (!bundle.contains(BRANCH_ID) || !Branches.exists(branchId)) {
			throw new IncompatibleSaveException("Invalid saved branch: " + branchId);
		}

		gold = bundle.getInt( GOLD );
		energy = bundle.getInt( ENERGY );

		Statistics.restoreFromBundle( bundle );
		Generator.restoreFromBundle( bundle );
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MobEnvelope.restoreGlobal( bundle );

		generatedLevels.clear();
			for (String key : bundle.getStringArray(GENERATED_LEVELS)){
				generatedLevels.add(key);
			}

		droppedItems = new SparseArray<>();
		portedItems = new SparseArray<>();
		for (int i=1; i <= 26; i++) {

			//dropped items
			ArrayList<Item> items = new ArrayList<>();
			if (bundle.contains(Messages.format( DROPPED, i )))
				for (Bundlable b : bundle.getCollection( Messages.format( DROPPED, i ) ) ) {
					items.add( (Item)b );
				}
			if (!items.isEmpty()) {
				droppedItems.put( i, items );
			}

			//ported items
			items = new ArrayList<>();
			if (bundle.contains(Messages.format( PORTED, i )))
				for (Bundlable b : bundle.getCollection( Messages.format( PORTED, i ) ) ) {
					items.add( (Item)b );
				}
			if (!items.isEmpty()) {
				portedItems.put( i, items );
			}
		}

	}
	
	public static Level loadLevel( int save ) throws IOException {
		
		levelJustGenerated = false;
		Dungeon.level = null;
		Actor.clear();

		// ensure the deck state of the branch being loaded is active (no-op on main)
		Generator.ensureBranchState(branchId);

		Bundle bundle = FileUtils.bundleFromFile( GamesInProgress.depthFile( save, depth, branchId ));

		Level level = (Level)bundle.get( LEVEL );

		if (level == null){
			throw new IOException();
		} else {
			return level;
		}
	}

	public static Level loadOrCreateLevel(int save, String targetBranch, int targetDepth) throws IOException {
		if (!Branches.exists(targetBranch)) {
			throw new IllegalArgumentException("Unknown branch: " + targetBranch);
		}
		Branch branch = Branches.get(targetBranch);
		if (targetDepth < 1 || targetDepth > branch.maxDepth) {
			throw new IllegalArgumentException("Invalid floor: " + targetBranch + ":" + targetDepth);
		}

		depth = targetDepth;
		branchId = targetBranch;
		// ensure the target branch's independent item-deck state is active
		// before any of its floors are generated (route-independent decks)
		Generator.ensureBranchState(targetBranch);
		return levelHasBeenGenerated(targetDepth, targetBranch) ? loadLevel(save) : newLevel();
	}
	
	public static void deleteGame( int save, boolean deleteLevels ) {

		if (deleteLevels) {
			String folder = GamesInProgress.gameFolder(save);
			for (String file : FileUtils.filesInDir(folder)){
				if (file.contains("depth")){
					FileUtils.deleteFile(folder + "/" + file);
				}
			}
		}

		FileUtils.overwriteFile(GamesInProgress.gameFile(save), 1);
		
		GamesInProgress.delete( save );
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		info.depth = bundle.getInt( DEPTH );
		info.version = bundle.getInt( VERSION );
		info.challenges = bundle.getInt( CHALLENGES );
		info.seed = bundle.getLong( SEED );
		info.customSeed = bundle.getString( CUSTOM_SEED );
		info.daily = bundle.getBoolean( DAILY );
		info.dailyReplay = bundle.getBoolean( DAILY_REPLAY );

		Hero.preview( info, bundle.getBundle( HERO ) );
		Statistics.preview( info, bundle );
	}
	
	public static void fail( Object cause ) {
		if (WndResurrect.instance == null) {
			updateLevelExplored();
			Statistics.gameWon = false;
			Rankings.INSTANCE.submit( false, cause );
		}
	}
	
	public static void win( Object cause ) {

		updateLevelExplored();
		Statistics.gameWon = true;

		hero.belongings.identify();

		Rankings.INSTANCE.submit( true, cause );
	}

	public static void updateLevelExplored(){
		if (branchId.equals(Branches.MAIN) && level instanceof RegularLevel && !Dungeon.bossLevel()){
			Statistics.floorsExplored.put( depth, level.isLevelExplored(depth));
		}
	}

	//default to recomputing based on max hero vision, in case vision just shrank/grew
	public static void observe(){
		int dist = Math.max(Dungeon.hero.viewDistance, 8);
		dist *= 1f + 0.25f*Dungeon.hero.pointsInTalent(Talent.FARSIGHT);

		if (Dungeon.hero.buff(MagicalSight.class) != null){
			dist = Math.max( dist, MagicalSight.DISTANCE );
		}

		observe( dist+1 );
	}
	
	public static void observe( int dist ) {

		if (level == null) {
			return;
		}
		
		level.updateFieldOfView(hero, level.heroFOV);
		com.shatteredpixel.shatteredpixeldungeon.challenges.Hongbao.highlightEnvelopes();

		int x = hero.pos % level.width();
		int y = hero.pos / level.width();
	
		//left, right, top, bottom
		int l = Math.max( 0, x - dist );
		int r = Math.min( x + dist, level.width() - 1 );
		int t = Math.max( 0, y - dist );
		int b = Math.min( y + dist, level.height() - 1 );
	
		int width = r - l + 1;
		int height = b - t + 1;
		
		int pos = l + t * level.width();
	
		for (int i = t; i <= b; i++) {
			BArray.or( level.visited, level.heroFOV, pos, width, level.visited );
			pos+=level.width();
		}

		//always visit adjacent tiles, even if they aren't seen
		for (int i : PathFinder.NEIGHBOURS9){
			level.visited[hero.pos+i] = true;
		}
	
		GameScene.updateFog(l, t, width, height);

		boolean stealthyMimics = MimicTooth.stealthyMimics();
		if (hero.buff(MindVision.class) != null){
			for (Mob m : level.mobs.toArray(new Mob[0])){
				if (stealthyMimics && m instanceof Mimic && m.alignment == Char.Alignment.NEUTRAL){
					continue;
				}

				BArray.or( level.visited, level.heroFOV, m.pos - 1 - level.width(), 3, level.visited );
				BArray.or( level.visited, level.heroFOV, m.pos - 1, 3, level.visited );
				BArray.or( level.visited, level.heroFOV, m.pos - 1 + level.width(), 3, level.visited );
				//updates adjacent cells too
				GameScene.updateFog(m.pos, 2);
			}
		}

		switch (Dungeon.hero.pointsInTalent(Talent.SOUL_NOWIFI)){
			case 1:
				for (Mob m : level.mobs.toArray(new Mob[0])){
					if (stealthyMimics && m instanceof Mimic && m.alignment == Char.Alignment.NEUTRAL || m.HP > m.HT / 4){
						continue;
					}

					BArray.or( level.visited, level.heroFOV, m.pos - 1 - level.width(), 3, level.visited );
					BArray.or( level.visited, level.heroFOV, m.pos - 1, 3, level.visited );
					BArray.or( level.visited, level.heroFOV, m.pos - 1 + level.width(), 3, level.visited );
					//updates adjacent cells too
					GameScene.updateFog(m.pos, 2);
				}
			break;
			case 2:
				for (Mob m : level.mobs.toArray(new Mob[0])){
					if (stealthyMimics && m instanceof Mimic && m.alignment == Char.Alignment.NEUTRAL || m.HP > m.HT * 0.4){
						continue;
					}

					BArray.or( level.visited, level.heroFOV, m.pos - 1 - level.width(), 3, level.visited );
					BArray.or( level.visited, level.heroFOV, m.pos - 1, 3, level.visited );
					BArray.or( level.visited, level.heroFOV, m.pos - 1 + level.width(), 3, level.visited );
					//updates adjacent cells too
					GameScene.updateFog(m.pos, 2);
				}
			break;
		}

		if (hero.buff(Awareness.class) != null){
			for (Heap h : level.heaps.valueList()){
				BArray.or( level.visited, level.heroFOV, h.pos - 1 - level.width(), 3, level.visited );
				BArray.or( level.visited, level.heroFOV, h.pos - 1, 3, level.visited );
				BArray.or( level.visited, level.heroFOV, h.pos - 1 + level.width(), 3, level.visited );
				GameScene.updateFog(h.pos, 2);
			}
		}

		for (TalismanOfForesight.CharAwareness c : hero.buffs(TalismanOfForesight.CharAwareness.class)){
			Char ch = (Char) Actor.findById(c.charID);
			if (ch == null || !ch.isAlive()) continue;
			BArray.or( level.visited, level.heroFOV, ch.pos - 1 - level.width(), 3, level.visited );
			BArray.or( level.visited, level.heroFOV, ch.pos - 1, 3, level.visited );
			BArray.or( level.visited, level.heroFOV, ch.pos - 1 + level.width(), 3, level.visited );
			GameScene.updateFog(ch.pos, 2);
		}

		for (TalismanOfForesight.HeapAwareness h : hero.buffs(TalismanOfForesight.HeapAwareness.class)){
			if (Dungeon.depth != h.depth || !Dungeon.branchId.equals(h.branchId)) continue;
			BArray.or( level.visited, level.heroFOV, h.pos - 1 - level.width(), 3, level.visited );
			BArray.or( level.visited, level.heroFOV, h.pos - 1, 3, level.visited );
			BArray.or( level.visited, level.heroFOV, h.pos - 1 + level.width(), 3, level.visited );
			GameScene.updateFog(h.pos, 2);
		}

		for (RevealedArea a : hero.buffs(RevealedArea.class)){
			if (Dungeon.depth != a.depth || !Dungeon.branchId.equals(a.branchId)) continue;
			BArray.or( level.visited, level.heroFOV, a.pos - 1 - level.width(), 3, level.visited );
			BArray.or( level.visited, level.heroFOV, a.pos - 1, 3, level.visited );
			BArray.or( level.visited, level.heroFOV, a.pos - 1 + level.width(), 3, level.visited );
			GameScene.updateFog(a.pos, 2);
		}

		for (Char ch : Actor.chars()){
			if (ch instanceof WandOfWarding.Ward
					|| ch instanceof WandOfRegrowth.Lotus
					|| ch instanceof SpiritHawk.HawkAlly){
				x = ch.pos % level.width();
				y = ch.pos / level.width();

				//left, right, top, bottom
				dist = ch.viewDistance+1;
				l = Math.max( 0, x - dist );
				r = Math.min( x + dist, level.width() - 1 );
				t = Math.max( 0, y - dist );
				b = Math.min( y + dist, level.height() - 1 );

				width = r - l + 1;
				height = b - t + 1;

				pos = l + t * level.width();

				for (int i = t; i <= b; i++) {
					BArray.or( level.visited, level.heroFOV, pos, width, level.visited );
					pos+=level.width();
				}
				GameScene.updateFog(ch.pos, dist);
			}
		}

		GameScene.afterObserve();
	}

	//we store this to avoid having to re-allocate the array with each pathfind
	private static boolean[] passable;

	private static void setupPassable(){
		if (passable == null || passable.length != Dungeon.level.length())
			passable = new boolean[Dungeon.level.length()];
		else
			BArray.setFalse(passable);
	}

	public static boolean[] findPassable(Char ch, boolean[] pass, boolean[] vis, boolean chars){
		return findPassable(ch, pass, vis, chars, chars);
	}

	public static boolean[] findPassable(Char ch, boolean[] pass, boolean[] vis, boolean chars, boolean considerLarge){
		setupPassable();
		if (ch.flying || ch.buff( Amok.class ) != null) {
			BArray.or( pass, Dungeon.level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, Dungeon.level.length() );
		}

		if (considerLarge && Char.hasProp(ch, Char.Property.LARGE)){
			BArray.and( passable, Dungeon.level.openSpace, passable );
		}

		ch.modifyPassable(passable);

		if (chars) {
			for (Char c : Actor.chars()) {
				if (vis[c.pos]) {
					passable[c.pos] = false;
				}
			}
		}

		return passable;
	}

	public static PathFinder.Path findPath(Char ch, int to, boolean[] pass, boolean[] vis, boolean chars) {

		return PathFinder.find( ch.pos, to, findPassable(ch, pass, vis, chars) );

	}
	
	public static int findStep(Char ch, int to, boolean[] pass, boolean[] visible, boolean chars ) {

		if (Dungeon.level.adjacent( ch.pos, to )) {
			return Actor.findChar( to ) == null && pass[to] ? to : -1;
		}

		return PathFinder.getStep( ch.pos, to, findPassable(ch, pass, visible, chars) );

	}
	
	public static int flee( Char ch, int from, boolean[] pass, boolean[] visible, boolean chars ) {
		boolean[] passable = findPassable(ch, pass, visible, false, true);
		passable[ch.pos] = true;

		//only consider other chars impassable if our retreat step may collide with them
		if (chars) {
			for (Char c : Actor.chars()) {
				if (c.pos == from || Dungeon.level.adjacent(c.pos, ch.pos)) {
					passable[c.pos] = false;
				}
			}
		}

		//chars affected by terror have a shorter lookahead and can't approach the fear source
		boolean canApproachFromPos = ch.buff(Terror.class) == null && ch.buff(Dread.class) == null;
		return PathFinder.getStepBack( ch.pos, from, canApproachFromPos ? 8 : 4, passable, canApproachFromPos );
		
	}

	/**
	 * [Plant TRAP]
	 * @param pos
	 */
	public static void plantVineTrap(int pos){
		VineTrap v=new VineTrap();
		v.pos=pos;
		level.plants.put(pos,v);
		GameScene.plantSeed( pos );
	}

}
