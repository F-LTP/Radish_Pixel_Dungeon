/*
 * Radish Pixel Dungeon
 *
 * 典狱长 Boss 层：一个较长的长方形牢房，与天狗（PrisonBossLevel）在第 10 层随机二选一。
 * 原型机数量、控制器位置等见类内常量。
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.DOOR;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.LOCKED_DOOR;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.effects.SnDBGM;
import com.shatteredpixel.shatteredpixeldungeon.items.SimpleController;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfShock;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

import java.util.ArrayList;

public class WardenBossLevel extends Level {

	{
		color1 = 0x3f3f46;
		color2 = 0x6a6a75;
	}

	//严格按用户给的地形片段：整层 13 宽 × 18 高，四周留一圈墙。
	//片段坐标（left=0, top=0）：墙 y=12 两段、竖墙 x=3/x=9（y=13..16）、入口 (6,16)、门 (6,12)
	private static final int WIDTH  = 13;
	private static final int HEIGHT = 18;

	//下方初始房间：只挖 x=4..8, y=13..16（left/top 是闭区间、right/bottom 是开区间），
	//外圈 x=3、x=9、y=12、y=17 保留为墙，门只开在 (6,12) 这一格
	private static final Rect entranceRoom = new Rect( 4, 13, 9, 17 );
	//房间顶墙那格：这里放门，开门直接进战斗区
	private static final Point doorCell = new Point( 6, 12 );
	//战斗区（上方大长方形）：x=1..11, y=1..11
	private static final Rect arena = new Rect( 1, 1, 12, 12 );

	//两种“图画”随机：A=旧石地（零散碎石），B=破损地砖（裂痕 + 墙饰）
	private boolean variantB = false;

	private static final String BOSS_DEAD = "boss_dead";
	private static final String FIGHT_STARTED = "fight_started";

	//开战之前层里既没有典狱长也没有原型机：英雄第一次踏进战斗区才开战，见 occupyCell()
	private boolean fightStarted = false;
	private boolean bossDead = false;
	private int exitCell = -1;

	@Override
	public void playLevelMusic() {
		if (SnDBGM.playLevelMusic()) return;
		if (bossDead) {
			Music.INSTANCE.playTracks( PrisonLevel.PRISON_TRACK_LIST, PrisonLevel.PRISON_TRACK_CHANCES, false );
		} else if (fightStarted) {
			Music.INSTANCE.play( Assets.Music.PRISON_BOSS, true );
		} else {
			Music.INSTANCE.end();
		}
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_PRISON;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_PRISON;
	}

	@Override
	protected boolean build() {

		setSize( WIDTH, HEIGHT );

		Painter.fill( this, 0, 0, WIDTH, HEIGHT, Terrain.WALL );

		//下方初始房间：玩家从上一层下来先落在这里，看不到也打不到 Boss
		//注意：Painter.fill(level, rect, m, value) 的 m 是“四边内缩”，不是地形值！
		//之前写成 fill(arena, 1, EMPTY) 实际只挖了内缩一圈的 9x9，导致门(6,12)正上方(6,11)还是墙，
		//门后面是实心的，玩家怎么都进不去战斗区。这里改用显式 xywh 版本。
		Painter.fill( this, 1, 1, WIDTH - 2, doorCell.y - 1, Terrain.EMPTY );        //战斗区 x1..11, y1..11
		Painter.fill( this, 4, doorCell.y + 1, 5, 4, Terrain.EMPTY );                //入口房 x4..8, y13..16

		//房间顶墙上的门：普通门，不用钥匙，开门直接进战斗区
		Level.set( pointToCell( doorCell ), Terrain.DOOR, this );

		//点亮一些装饰，让长条牢房不那么空（两种图画二选一）
		variantB = Random.Int(2) == 1;
		if (variantB) {
			//B：破损地砖 —— 战斗区里散布裂痕地面，墙脚加墙饰
			for (int y = arena.top + 1; y < arena.bottom - 1; y++) {
				for (int x = arena.left + 1; x < arena.right - 1; x++) {
					int cell = pointToCell( new Point( x, y ) );
					if (map[cell] == Terrain.EMPTY && Random.Int(5) == 0) {
						Level.set( cell, Terrain.EMPTY_SP, this );
					}
				}
			}
			for (int i = 0; i < 4; i++) {
				int x = Random.IntRange( arena.left + 1, arena.right - 2 );
				//战斗区内部从 arena.top 就开始了，墙在更上面一行（y=0）
				int y = Math.max( 0, arena.top - 1 );
				int cell = pointToCell( new Point( x, y ) );
				if (map[cell] == Terrain.WALL) {
					Level.set( cell, Terrain.WALL_DECO, this );
				}
			}
		} else {
			//A：旧石地 —— 零散碎石装饰
			for (int i = 0; i < 6; i++) {
				int x = Random.IntRange( arena.left + 1, arena.right - 2 );
				int y = Random.IntRange( arena.top + 1, arena.bottom - 2 );
				int cell = pointToCell( new Point( x, y ) );
				if (map[cell] == Terrain.EMPTY) {
					//注意：build() 期间 Dungeon.level 还没指向本关，2 参 set() 会拿到 null 抛 NPE，
					//一律显式传 this 走 3 参版本
					Level.set( cell, Terrain.EMPTY_DECO, this );
				}
			}
		}

		//入口放在初始房间底部正中（与用户给的地形片段一致）
		int entrance = pointToCell( new Point( doorCell.x, entranceRoom.bottom - 1 ) );
		Level.set( entrance, Terrain.ENTRANCE, this );
		transitions.add( new LevelTransition( this, entrance, LevelTransition.Type.REGULAR_ENTRANCE ) );

		//出口在战斗区最上端（打完典狱长后从这里下去），击败典狱长前锁住
		exitCell = pointToCell( new Point( WIDTH / 2, arena.top + 1 ) );
		Level.set( exitCell, Terrain.LOCKED_EXIT, this );
		transitions.add( new LevelTransition( this, exitCell, LevelTransition.Type.REGULAR_EXIT ) );

		return true;
	}

	/** 与其它 Boss 层一致：关闭通用周期刷怪，站门口等待不该刷杂兵。 */
	@Override
	public Actor addRespawner() {
		return null;
	}

	@Override
	protected void createMobs() {
		//典狱长和原型机都不在这里放：一开始就摆在场上的话，站在门外就能隔着门打它，而那时并没有开战。
		//它们在英雄第一次踏进战斗区时才由 startFight() 生成
	}

	@Override
	public void occupyCell( Char ch ) {
		if (ch == Dungeon.hero && !bossDead && heroInBossArena( ch.pos )) {
			if (!fightStarted) {
				startFight();
			} else if (!locked) {
				//旧版本的存档可能停在「已经开战、但还没封层」
				seal();
			}
		}
		updateDoor();
		super.occupyCell( ch );
	}

	//和天狗一样：英雄踏进战斗区的那一刻才封层、锁门，典狱长和原型机这时才出场
	private void startFight() {
		int wardenCell = wardenSpawnCell();
		if (wardenCell == -1) {
			//整个战斗区都站满了：先不开战（也不封层），英雄再走一步时重试
			return;
		}

		fightStarted = true;
		seal();

		evictSheep( wardenCell );
		Warden warden = new Warden();
		warden.state = warden.HUNTING;
		warden.pos = wardenCell;
		GameScene.add( warden );
		warden.notice();

		//把跟在英雄身边的友军一起带进来，否则门一锁它们就被关在外面。
		//放在典狱长出场之后，友军的落点才会避开它那一格
		int door = pointToCell( doorCell );
		Mob.holdAllies( this, door );
		Mob.restoreAllies( this, Dungeon.hero.pos, door );

		for (int i = 0; i < 8; i++) spawnPrototype( 100 );
		for (int i = 0; i < 3; i++) spawnPrototype( 175 );

		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				playLevelMusic();
			}
		} );
	}

	@Override
	protected void createItems() {

		//控制器与补给都放在上方初始房间里，玩家进战斗区之前就能拿到
		SimpleController controller = new SimpleController();
		controller.identify();
		drop( controller, pointToCell( new Point( entranceRoom.left + 1, entranceRoom.top + 2 ) ) );

		int healing = pointToCell( new Point( entranceRoom.right - 2, entranceRoom.top + 2 ) );
		drop( new PotionOfHealing(), healing );

		//bottom 是开区间，-1 会落在房间外圈的墙上，这里退到内圈
		int shock = pointToCell( new Point( WIDTH / 2, entranceRoom.bottom - 2 ) );
		drop( new StoneOfShock(), shock );
	}

	//典狱长出场的位置：战斗区正中，离门口那一排 5 格，进门就看得见又不会贴脸。
	//正中站着别的角色时改用离正中最近的可用格
	private int wardenSpawnCell() {
		int center = pointToCell( new Point( WIDTH / 2, arena.top + arena.height() / 2 ) );
		int best = -1;
		for (int y = arena.top; y < arena.bottom; y++) {
			for (int x = arena.left; x < arena.right; x++) {
				int cell = x + y * width();
				if (canSpawnAt( cell ) && (best == -1 || distance( center, cell ) < distance( center, best ))) {
					best = cell;
				}
			}
		}
		return best;
	}

	//原型机的落点：战斗区里随机一格。门前三行平时留空，免得一进门机体就贴在脸上，
	//别处实在没有位置了才用
	private int prototypeSpawnCell() {
		ArrayList<Integer> candidates = new ArrayList<>();
		ArrayList<Integer> nearDoor = new ArrayList<>();
		for (int y = arena.top; y < arena.bottom; y++) {
			for (int x = arena.left; x < arena.right; x++) {
				int cell = x + y * width();
				if (canSpawnAt( cell )) {
					if (doorCell.y - y <= 3) {
						nearDoor.add( cell );
					} else {
						candidates.add( cell );
					}
				}
			}
		}
		if (candidates.isEmpty()) {
			candidates = nearDoor;
		}
		return candidates.isEmpty() ? -1 : Random.element( candidates );
	}

	//魔法羊占着的格子照样能用，生成前把羊顶掉（见 evictSheep）；其它角色占着的格子不能用
	private boolean canSpawnAt( int cell ) {
		if (!passable[cell]) {
			return false;
		}
		Char ch = Actor.findChar( cell );
		return ch == null || ch instanceof Sheep;
	}

	//和矮人国王召唤随从时一样，落点上的羊直接弄死，否则放一群羊就能把 Boss 和原型机堵在场外
	private void evictSheep( int cell ) {
		Char ch = Actor.findChar( cell );
		if (ch instanceof Sheep) {
			ch.die( null );
		}
	}

	/** 生成一台原型机（100 或 175），只落在战斗区里；战斗区站满时不生成，返回 null */
	public WardenPrototype spawnPrototype( int model ) {
		int cell = prototypeSpawnCell();
		if (cell == -1) {
			return null;
		}
		evictSheep( cell );
		WardenPrototype p = new WardenPrototype( model );
		p.pos = cell;
		GameScene.add( p );
		return p;
	}

	@Override
	public void seal() {
		super.seal();
		updateDoor();
	}

	@Override
	public void unseal() {
		super.unseal();
		updateDoor();
	}

	//门只在「开战期间、英雄和典狱长都在战斗区里」时锁着。
	//人在门外门却锁着（旧版本存档、测试传送）就进不去了，封层又让楼梯用不了，等于卡死；
	//典狱长追到门外时也不能锁，否则英雄先回到战斗区就把它关在了外面，同样卡死。
	//门格上站着角色时先不锁，免得把它嵌进实心格里，等它走开再补上
	private void updateDoor() {
		int door = pointToCell( doorCell );
		if (fightStarted && !bossDead && heroInBossArena( Dungeon.hero.pos ) && wardenInArena()) {
			if (map[door] != Terrain.LOCKED_DOOR && Actor.findChar( door ) == null) {
				Level.set( door, Terrain.LOCKED_DOOR, this );
				GameScene.updateMap( door );
			}
		} else if (map[door] == Terrain.LOCKED_DOOR) {
			Level.set( door, Terrain.DOOR, this );
			GameScene.updateMap( door );
		}
	}

	//开战的那一刻典狱长还没放进场，这时按在场内算，门照常锁上
	private boolean wardenInArena() {
		for (Mob mob : mobs) {
			if (mob instanceof Warden) {
				return heroInBossArena( mob.pos );
			}
		}
		return true;
	}

	//和天狗层一样只给入口旁边的格子：从上一层掉下来的英雄和物品、封层后复活的英雄
	//都落在初始房间里，而不是直接出现在战斗区
	@Override
	public int randomRespawnCell( Char ch ) {
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int i : PathFinder.NEIGHBOURS8) {
			int cell = entrance() + i;
			if (passable[cell]
					&& Actor.findChar( cell ) == null
					&& (!Char.hasProp( ch, Char.Property.LARGE ) || openSpace[cell])) {
				candidates.add( cell );
			}
		}
		if (candidates.isEmpty()) {
			return -1;
		}
		return Random.element( candidates );
	}

	public void onBossDeath() {
		bossDead = true;
		for (Mob mob : new ArrayList<>(mobs)) {
			if (mob instanceof WardenPrototype) {
				mob.alignment = Char.Alignment.NEUTRAL;
				mob.destroy();
				//精灵要走正常的死亡流程：直接 killAndErase() 会让它脱离场景却仍挂在角色身上，
				//范围效果里排在后面的结算、或者正是这台机体放电打死了典狱长时，再用到它就会空指针
				if (mob.sprite != null) {
					mob.sprite.die();
				}
			}
		}
		unseal();
		if (exitCell != -1) {
			Level.set( exitCell, Terrain.EXIT, this );
			GameScene.updateMap( exitCell );
		}
		GLog.w( Messages.get( this, "boss_dead" ) );

		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				Music.INSTANCE.fadeOut( 5f, new Callback() {
					@Override
					public void call() {
						Music.INSTANCE.end();
					}
				} );
			}
		} );
	}

	public boolean heroInBossArena(int cell) {
		return cell >= 0 && cell < length() && cell / width() < doorCell.y;
	}

	public boolean bossDead() {
		return bossDead;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( BOSS_DEAD, bossDead );
		bundle.put( "exit_cell", exitCell );
		bundle.put( FIGHT_STARTED, fightStarted );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		bossDead = bundle.getBoolean( BOSS_DEAD );
		//旧档/异常档里可能没有 exit_cell：Bundle.getInt 缺键会返回 0，直接用会把地图左上角挖成 EXIT。
		//只在键存在且落在合法范围时才采用，否则标记为 -1（onBossDeath 里有 -1 守卫）。
		if ( bundle.contains( "exit_cell" ) ) {
			int cell = bundle.getInt( "exit_cell" );
			exitCell = ( cell >= 0 && cell < length() ) ? cell : -1;
		} else {
			exitCell = -1;
		}
		//Boss 已死却没有出口记录（异常档）：按 build() 的规则补算，避免打完却无路可走
		if ( bossDead && exitCell == -1 ) {
			exitCell = pointToCell( new Point( WIDTH / 2, arena.top + 1 ) );
		}

		boolean wardenPresent = false;
		boolean wardenAwake = false;
		for (Mob mob : mobs) {
			if (mob instanceof Warden) {
				wardenPresent = true;
				wardenAwake |= ((Warden) mob).awake();
			}
		}

		//旧版本没有这个键：那时典狱长在关卡生成时就摆在场上，开没开战记在它自己身上
		boolean oldSave = !bundle.contains( FIGHT_STARTED );
		fightStarted = oldSave ? bossDead || wardenAwake : bundle.getBoolean( FIGHT_STARTED );

		//开战之前层里不该有典狱长和没激活的原型机，层也不该封着。有两种存档不满足：
		//旧版本在关卡生成时就把它们摆好了，不清掉的话进门时会再生成一套，场上出现两个典狱长；
		//以及已经开战、典狱长却没走死亡流程就不在场上了，不退回开战前的话层会一直封着、出口一直锁着。
		//已经被激活的原型机留着不动
		if (!bossDead && !(fightStarted && wardenPresent)) {
			fightStarted = false;
			locked = false;
			for (Mob mob : mobs.toArray( new Mob[0] )) {
				if (mob instanceof Warden
						|| (mob instanceof WardenPrototype && !((WardenPrototype) mob).isActivated())) {
					mobs.remove( mob );
				}
			}
		}

		//门只在开战期间锁着。旧版本可能把英雄关在了门外，这里又不能读英雄的位置来判断，
		//所以旧档一律先把门打开，英雄走第一步时 updateDoor() 会按实际位置重新锁上
		int door = pointToCell( doorCell );
		if (map[door] == Terrain.LOCKED_DOOR && (oldSave || !fightStarted || bossDead)) {
			Level.set( door, Terrain.DOOR, this );
		}
	}
}
