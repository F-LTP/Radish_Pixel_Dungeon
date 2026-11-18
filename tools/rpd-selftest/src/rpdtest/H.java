package rpdtest;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Spell;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Scene;
import com.watabou.utils.PointF;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 场景 API。场景代码跑在「场景线程」里，通过本类驱动真实游戏：
 *
 *  - 玩家输入（tap / rest / use / selectCell ...）：在渲染线程发起，走和真实点击相同的入口
 *    （GameScene.handleCell、Item.execute、Hero.rest），回合由游戏自己的 Actor 线程推进；
 *    这些入口里抛出的异常按「渲染线程崩溃」处理（真实游戏里就是闪退）。
 *  - 世界操作（world / spawn / setTerrain / placeTrap / give ...）：直接改游戏状态，
 *    只在「英雄可行动、Actor 线程在等待」的安全时机、在渲染线程里执行。
 *  - 读状态：用 get(() -> ...) 在渲染线程里读（安全时机）。
 *  - 断言：check（期望行为，不成立 = FAIL）、require（前置条件，不成立 = ERROR）。
 */
public final class H {

	// ------------------------------------------------------------------ 配置

	/** 等「英雄可行动」的总上限（毫秒，真实时间）。包含关卡生成时间。 */
	public long readyTimeoutMs = 60000;
	/** 回合推进完全没有进展（游戏时间、当前 actor 都不变）超过这么久，判定为卡回合。 */
	public long stuckMs = 15000;
	/** 判定卡回合还要求渲染线程在此期间至少跑过这么多帧（排除机器整体卡顿）。 */
	public long stuckFrames = 300;
	/** 渲染线程多久没有执行投递给它的任务，判定为渲染线程挂死。 */
	public long renderCallTimeoutMs = 45000;

	public enum Status {
		/** 英雄可行动（游戏在等玩家输入）。 */
		READY,
		/** 英雄已死亡（不会再有回合）。 */
		DEAD
	}

	// ------------------------------------------------------------------ 反射句柄

	private static final Field F_ACTOR_CURRENT = Reflect.field( Actor.class, "current" );
	private static final Field F_ACTOR_THREAD = Reflect.field( GameScene.class, "actorThread" );
	private static final Field F_SCENE = Reflect.field( GameScene.class, "scene" );
	private static final Field F_CELL_SELECTOR = Reflect.field( GameScene.class, "cellSelector" );
	private static final Field F_DEFAULT_LISTENER = Reflect.field( GameScene.class, "defaultCellListener" );
	private static final Field F_MEMBERS = Reflect.field( Group.class, "members" );

	private static Object sget( Field f ) {
		try {
			return f.get( null );
		} catch (IllegalAccessException e) {
			throw new HarnessError( "reflection failed: " + f, e );
		}
	}

	H() {}

	// ------------------------------------------------------------------ 线程调度

	private volatile Thread renderThread;

	void noteRenderThread( Thread t ) {
		renderThread = t;
	}

	private <T> T onRender( String what, boolean playerInput, Callable<T> task ) {
		if (Result.finishing()) parkForever();
		if (Thread.currentThread() == renderThread) {
			throw new HarnessError( "H methods must be called from the scenario thread, not from inside a render-thread block (" + what + ")" );
		}
		final Object[] box = new Object[2];
		final CountDownLatch latch = new CountDownLatch( 1 );
		Gdx.app.postRunnable( () -> {
			try {
				box[0] = task.call();
			} catch (Throwable t) {
				box[1] = t;
			} finally {
				latch.countDown();
			}
		} );
		try {
			if (!latch.await( renderCallTimeoutMs, TimeUnit.MILLISECONDS )) {
				Result.finish( Result.Verdict.STUCK,
						"render thread did not run a posted task within " + renderCallTimeoutMs + " ms (render thread hang) while: " + what,
						"RENDER THREAD HANG while: " + what + "\n" + stuckReport( null ) );
			}
		} catch (InterruptedException e) {
			throw new HarnessError( "scenario thread interrupted", e );
		}
		if (box[1] != null) {
			Throwable t = (Throwable) box[1];
			if (t instanceof HarnessError) throw (HarnessError) t;
			if (t instanceof CheckFailed) throw (CheckFailed) t;
			if (playerInput) {
				// 真实游戏里这段代码由点击事件触发、跑在渲染线程上：异常会一路抛出渲染循环 = 闪退
				Result.crash( "render thread, during player input [" + what + "]", t );
			}
			throw new HarnessError( "exception in world/query block [" + what + "] (direct state manipulation by the harness, "
					+ "not a player action; may be a scenario bug or a game bug): " + t, t );
		}
		@SuppressWarnings("unchecked")
		T result = (T) box[0];
		return result;
	}

	private static void parkForever() {
		while (true) {
			try {
				Thread.sleep( 1000 );
			} catch (InterruptedException ignored) {
			}
		}
	}

	private static void sleep( long ms ) {
		try {
			Thread.sleep( ms );
		} catch (InterruptedException e) {
			throw new HarnessError( "scenario thread interrupted", e );
		}
	}

	// ------------------------------------------------------------------ 状态快照

	private static final class Snap {
		String sceneName;
		int sceneId;
		boolean gameScene, switching;
		boolean heroExists, heroAlive, heroReady, heroIdle;
		boolean selEnabled;
		Actor current;
		Thread.State actorState;
		float now;
		int depth, heroPos;
		List<String> windows = new ArrayList<>();

		boolean ready() {
			return gameScene && !switching && heroExists && heroAlive && heroReady && heroIdle && selEnabled
					&& current == Dungeon.hero && actorState == Thread.State.WAITING;
		}

		boolean dead() {
			return gameScene && !switching && heroExists && !heroAlive
					&& (actorState == null || actorState == Thread.State.WAITING || actorState == Thread.State.TERMINATED);
		}

		String brief() {
			return "scene=" + sceneName + (switching ? "(switching)" : "") + " depth=" + depth
					+ " hero=" + (heroExists ? ("pos " + heroPos + (heroAlive ? "" : " DEAD") + " ready=" + heroReady + " idle=" + heroIdle) : "none")
					+ " cellSelector.enabled=" + selEnabled
					+ " current=" + (current == null ? "null" : shortName( current.getClass() ))
					+ " actorThread=" + actorState
					+ String.format( Locale.ROOT, " now=%.2f", now )
					+ (windows.isEmpty() ? "" : " windows=" + windows);
		}
	}

	/** 类名去掉包名（匿名类的 getSimpleName() 是空串，所以不用它）。 */
	private static String shortName( Class<?> c ) {
		String n = c.getName();
		return n.substring( n.lastIndexOf( '.' ) + 1 );
	}

	/** 只能在渲染线程调用。 */
	private Snap snapNow() {
		Snap s = new Snap();
		Scene sc = Game.scene();
		s.sceneName = sc == null ? "null" : sc.getClass().getSimpleName();
		s.sceneId = System.identityHashCode( sc );
		s.switching = Game.switchingScene();
		s.gameScene = sc instanceof GameScene;
		Hero hero = Dungeon.hero;
		s.heroExists = hero != null;
		if (hero != null) {
			s.heroAlive = hero.isAlive();
			s.heroReady = hero.ready;
			s.heroIdle = hero.curAction == null;
			s.heroPos = hero.pos;
		}
		s.depth = Dungeon.depth;
		s.now = Actor.now();
		s.current = (Actor) sget( F_ACTOR_CURRENT );
		Thread at = (Thread) sget( F_ACTOR_THREAD );
		s.actorState = at == null ? null : at.getState();
		if (s.gameScene) {
			CellSelector cs = (CellSelector) sget( F_CELL_SELECTOR );
			s.selEnabled = cs != null && cs.enabled;
			for (Window w : windowsNow()) {
				s.windows.add( w.getClass().getName().replace( "com.shatteredpixel.shatteredpixeldungeon.", "" ) );
			}
		} else if (sc != null) {
			// 非地牢场景里的窗口（典型：换层失败时 InterlevelScene 弹的 WndError）
			for (Gizmo g : membersOf( sc )) {
				if (g instanceof Window) {
					s.windows.add( g.getClass().getName().replace( "com.shatteredpixel.shatteredpixeldungeon.", "" ) );
				}
			}
		}
		return s;
	}

	private Snap snap() {
		return onRender( "state snapshot", false, this::snapNow );
	}

	// ------------------------------------------------------------------ 等待

	/** 等到游戏在等玩家输入（或英雄死亡）。超时 / 无进展 → STUCK（输出当前 actor 与所有线程栈）。 */
	public Status waitReady() {
		return waitReady( readyTimeoutMs );
	}

	public Status waitReady( long timeoutMs ) {
		final long t0 = System.nanoTime();
		long lastProgressNanos = t0;
		long lastProgressFrame = Log.frame;
		float lastNow = Float.NaN;
		Actor lastCurrent = null;
		int lastScene = 0;
		boolean first = true;
		long deadSinceFrame = -1;

		while (true) {
			Snap s = snap();
			if (s.ready()) return Status.READY;
			// 死亡要稳定一小会儿才算数（死亡瞬间 Actor 线程可能只是暂时停在某个动画上）
			if (s.dead()) {
				if (deadSinceFrame < 0) deadSinceFrame = Log.frame;
				if (Log.frame - deadSinceFrame >= 20) return Status.DEAD;
			} else {
				deadSinceFrame = -1;
			}

			// 换层失败：游戏弹了错误窗口（IO 类异常；其它异常游戏会直接在渲染线程抛出 = CRASH）
			if (!s.gameScene && !s.windows.isEmpty()) {
				Throwable err = HarnessGame.interlevelError();
				Result.finish( Result.Verdict.FAIL,
						"level transition failed: the game is showing " + s.windows + " in " + s.sceneName
								+ (err == null ? "" : " (" + err + ")"),
						err == null ? null : "LEVEL TRANSITION ERROR (the game shows an error window and goes back to the title screen)\n" + Log.stackOf( err ) );
			}

			long nowNanos = System.nanoTime();
			boolean progressed = first || s.now != lastNow || s.current != lastCurrent || s.sceneId != lastScene
					|| !s.gameScene || s.switching;
			first = false;
			if (progressed) {
				lastProgressNanos = nowNanos;
				lastProgressFrame = Log.frame;
				lastNow = s.now;
				lastCurrent = s.current;
				lastScene = s.sceneId;
			}

			long idleMs = (nowNanos - lastProgressNanos) / 1_000_000L;
			long idleFrames = Log.frame - lastProgressFrame;
			long totalMs = (nowNanos - t0) / 1_000_000L;

			if (idleMs > stuckMs && idleFrames > stuckFrames) {
				String head = String.format( Locale.ROOT,
						"STUCK TURN: hero never became ready; no turn progress for %.1f s / %d frames (game time and current actor unchanged)",
						idleMs / 1000.0, idleFrames );
				Result.finish( Result.Verdict.STUCK,
						"stuck turn: current actor = " + describeActor( s.current ) + "; " + s.brief(),
						head + "\n" + stuckReport( s ) );
			}
			if (totalMs > timeoutMs) {
				String head = String.format( Locale.ROOT,
						"TIMEOUT: hero did not become ready within %.1f s (turns may still be advancing: livelock, endless paralysis, very slow level generation ...)",
						totalMs / 1000.0 );
				Result.finish( Result.Verdict.STUCK,
						"hero not ready after " + (totalMs / 1000) + " s: current actor = " + describeActor( s.current ) + "; " + s.brief(),
						head + "\n" + stuckReport( s ) );
			}
			sleep( 2 );
		}
	}

	/** 等若干帧（让动画 / 延迟回调跑完）。 */
	public void frames( int n ) {
		long target = Log.frame + n;
		long t0 = System.nanoTime();
		while (Log.frame < target) {
			if ((System.nanoTime() - t0) / 1_000_000L > renderCallTimeoutMs) {
				Result.finish( Result.Verdict.STUCK, "render thread stopped producing frames",
						"RENDER THREAD HANG (no new frames)\n" + stuckReport( null ) );
			}
			sleep( 2 );
		}
		// 再同步一次，确保这些帧之后的状态对场景线程可见
		snap();
	}

	/**
	 * 改变地牢里的加速倍率（1 = 真实速度；默认值来自 run.ps1 的 -TimeScale，8 倍）。
	 *
	 * 加速只是把每帧的「游戏内经过时间」放大，回合逻辑不受影响；但动画补间会在一两帧内播完，
	 * 「动画还没播完时又发生了别的事」这一类和时序有关的 bug（典型：走路补间把刚被传送走的精灵拖回原地）
	 * 在高倍速下可能偶尔不出现。断言精灵位置 / 动画状态的场景，请在开局后先调 h.timeScale( 1f )。
	 */
	public void timeScale( float scale ) {
		if (scale <= 0f) throw new HarnessError( "timeScale must be > 0" );
		Log.step( "time scale = " + scale );
		HarnessGame.timeScale = scale;
		frames( 2 );
	}

	/**
	 * 每隔几帧在渲染线程里看一次 cond，成立就返回 true；最多等 maxFrames 帧，到时还不成立返回 false。
	 * 用来等「不占回合」的事情结束（死亡动画淡出、特效播完……），不依赖加速倍率。
	 */
	public boolean waitUntil( Supplier<Boolean> cond, int maxFrames ) {
		long end = Log.frame + maxFrames;
		while (true) {
			if (Boolean.TRUE.equals( get( cond ) )) return true;
			if (Log.frame >= end) return false;
			frames( 5 );
		}
	}

	private String describeActor( Actor a ) {
		if (a == null) return "null";
		StringBuilder sb = new StringBuilder( a.getClass().getName().replace( "com.shatteredpixel.shatteredpixeldungeon.", "" ) );
		try {
			if (a instanceof Char) {
				Char c = (Char) a;
				sb.append( " pos=" ).append( c.pos ).append( " HP=" ).append( c.HP ).append( '/' ).append( c.HT )
						.append( " alignment=" ).append( c.alignment );
				if (c.sprite != null) {
					sb.append( " sprite=" ).append( c.sprite.getClass().getSimpleName() )
							.append( " sprite.isMoving=" ).append( c.sprite.isMoving );
				} else {
					sb.append( " sprite=null" );
				}
				if (c instanceof Hero) {
					Hero h = (Hero) c;
					sb.append( " ready=" ).append( h.ready ).append( " curAction=" )
							.append( h.curAction == null ? "null" : h.curAction.getClass().getSimpleName() )
							.append( " resting=" ).append( h.resting ).append( " paralysed=" ).append( h.paralysed );
				}
			} else if (a instanceof Buff) {
				Buff b = (Buff) a;
				sb.append( " target=" ).append( b.target == null ? "null" : b.target.getClass().getSimpleName() );
			}
			sb.append( String.format( Locale.ROOT, " time=%.2f", a.time ) );
		} catch (Throwable t) {
			sb.append( " (describe failed: " ).append( t ).append( ')' );
		}
		return sb.toString();
	}

	private String stuckReport( Snap s ) {
		StringBuilder sb = new StringBuilder();
		try {
			Actor cur = (Actor) sget( F_ACTOR_CURRENT );
			sb.append( "current actor: " ).append( describeActor( cur ) ).append( '\n' );
			sb.append( "Actor.getCurrentActorClass(): " ).append( Actor.getCurrentActorClass() ).append( '\n' );
			if (s != null) sb.append( "state: " ).append( s.brief() ).append( '\n' );
			Hero h = Dungeon.hero;
			if (h != null) sb.append( "hero: " ).append( describeActor( h ) ).append( '\n' );
			sb.append( "seed=" ).append( Dungeon.seed ).append( " depth=" ).append( Dungeon.depth )
					.append( " level=" ).append( Dungeon.level == null ? "null" : Dungeon.level.getClass().getSimpleName() ).append( '\n' );
			Thread at = (Thread) sget( F_ACTOR_THREAD );
			if (at != null) {
				sb.append( "actor thread \"" ).append( at.getName() ).append( "\" state=" ).append( at.getState() ).append( '\n' );
				for (StackTraceElement e : at.getStackTrace()) {
					sb.append( "\tat " ).append( e ).append( '\n' );
				}
			} else {
				sb.append( "actor thread: none\n" );
			}
		} catch (Throwable t) {
			sb.append( "(failed to describe game state: " ).append( t ).append( ")\n" );
		}
		sb.append( "---- all threads ----\n" ).append( Log.dumpAllThreads() );
		return sb.toString();
	}

	// ------------------------------------------------------------------ 渲染线程块

	/** 在渲染线程里读状态并返回（任何时候都可以调，但只有英雄可行动时游戏状态才是静止的）。 */
	public <T> T get( Supplier<T> q ) {
		return onRender( "query", false, q::get );
	}

	/**
	 * 世界操作：直接修改游戏状态（生成怪、改地形、给物品……）。
	 * 只允许在安全时机执行：英雄可行动、Actor 线程在等待。执行后自动刷新视野。
	 */
	public void world( String what, Runnable op ) {
		Log.step( "world: " + what );
		onRender( "world: " + what, false, () -> {
			Snap s = snapNow();
			if (!s.ready()) {
				throw new HarnessError( "world op [" + what + "] attempted while the game is not idle (must be: hero ready, actor thread waiting): " + s.brief() );
			}
			op.run();
			if (Dungeon.hero != null && Dungeon.hero.isAlive() && Game.scene() instanceof GameScene && !Game.switchingScene()) {
				Dungeon.observe();
				GameScene.updateFog();
				// 让英雄立刻「看到」新出现的敌人。游戏里这一步发生在英雄每次 act() 的开头：
				// 发现新敌人会打断当前动作。不在这里做的话，世界操作后的第一次点击会被这次打断吃掉。
				Dungeon.hero.checkVisibleMobs();
			}
			return null;
		} );
	}

	/** 玩家输入：在渲染线程里调用一个「和真实点击等价」的入口。要求游戏正在等输入。 */
	public void input( String what, Runnable action ) {
		Log.step( "input: " + what );
		onRender( what, true, () -> {
			Snap s = snapNow();
			if (!s.ready()) {
				// 这是场景的问题，不是游戏的问题：真实玩家此刻的点击会被游戏忽略
				throw new HarnessError( "player input [" + what + "] attempted while the game is not waiting for input: " + s.brief() );
			}
			action.run();
			return null;
		} );
	}

	// ------------------------------------------------------------------ 开局 / 换层

	private boolean gameStarted = false;

	/** 开新局，第 1 层是正常生成的下水道。 */
	public void newGame( HeroClass heroClass, String seed ) {
		startGame( heroClass, seed, false );
	}

	/** 开新局，第 1 层直接是测试场（TestArenaLevel：21x21 空房间，四周是墙，入口在正中）。 */
	public void newGameInArena( HeroClass heroClass, String seed ) {
		startGame( heroClass, seed, true );
	}

	private void startGame( HeroClass heroClass, String seed, boolean arena ) {
		if (gameStarted) throw new HarnessError( "newGame may only be called once per scenario (one game process per scenario)" );
		gameStarted = true;
		Log.step( "new game: class=" + heroClass + " seed=" + seed + (arena ? " level=TestArenaLevel" : " level=regular depth 1") );
		onRender( "new game", false, () -> {
			if (!HarnessGame.bootReady) throw new HarnessError( "game not booted yet" );
			Badges.loadGlobal();
			Journal.loadGlobal();
			// 各大区的开场故事标成已读：否则第一次进第 1 层会弹 WndStory 挡住输入
			for (int i = 0; i <= 5; i++) {
				try {
					Document.INTROS.readPage( i );
				} catch (Throwable ignored) {
				}
			}
			SPDSettings.customSeed( seed == null ? "" : seed );
			SPDSettings.challenges( 0 );
			GamesInProgress.selectedClass = heroClass;
			GamesInProgress.curSlot = 1;
			Dungeon.hero = null;
			Dungeon.daily = Dungeon.dailyReplay = false;
			ActionIndicator.action = null;
			// TestArenaLevel 只在 leaf 开发线上有（new_contents 系的分支没有）：用反射访问，工具本体在两条线上都能编译。
			// （做法来自 redo-engineer 工作包的私有副本，它已经在 nc/engineer 上用过。）
			try {
				Class<?> arenaClass = Class.forName( "com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestArenaLevel" );
				Reflect.setStatic( arenaClass, "requested", arena );
			} catch (ClassNotFoundException e) {
				if (arena) {
					throw new HarnessError( "this worktree has no custom.testmode.TestArenaLevel (the test arena): "
							+ "newGameInArena / Arena.start cannot be used here, use newGame()" );
				}
			}
			InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
			Game.switchScene( InterlevelScene.class );
			return null;
		} );
		Status st = waitReady();
		if (st != Status.READY) throw new HarnessError( "hero is dead right after starting a new game" );
		onRender( "install probe", false, () -> {
			Probe.install();
			return null;
		} );
		dismissStartupWindows();
		Log.step( "game started: " + get( this::describeWorld ) );
	}

	private void dismissStartupWindows() {
		List<String> names = windowNames();
		if (!names.isEmpty()) {
			Log.warn( "windows showing right after level entry, closing them: " + names );
			closeWindows();
		}
	}

	/**
	 * 直接进入第 depth 层（主线），并强制该层使用指定的关卡类（做法同测试模式的楼层跳转器：
	 * Dungeon.forcedLevelClass + InterlevelScene RETURN）。levelClass 为 null 表示按正常规则生成。
	 * 英雄落在该层入口。
	 */
	public void gotoLevel( int depth, Class<? extends Level> levelClass ) {
		Log.step( "goto depth " + depth + (levelClass == null ? "" : " forced level " + levelClass.getSimpleName()) );
		world( "jump to depth " + depth, () -> {
			Dungeon.generatedLevels.remove( Dungeon.levelKey( Branches.MAIN, depth ) );
			// Dungeon.forcedLevelClass 同样只在 leaf 开发线上有：没有这个字段时不强制，按正常规则生成，到了之后再核对
			if (Reflect.hasField( Dungeon.class, "forcedLevelClass" )) {
				Reflect.setStatic( Dungeon.class, "forcedLevelClass", levelClass );
			}
			InterlevelScene.mode = InterlevelScene.Mode.RETURN;
			InterlevelScene.returnDepth = depth;
			InterlevelScene.returnPos = -1;
			InterlevelScene.returnBranchId = Branches.MAIN;
			Game.switchScene( InterlevelScene.class );
		} );
		Status st = waitReady();
		if (st != Status.READY) throw new HarnessError( "hero is dead right after entering depth " + depth );
		if (levelClass != null) {
			Level arrived = get( () -> Dungeon.level );
			if (!levelClass.isInstance( arrived )) {
				throw new HarnessError( "depth " + depth + " is a " + (arrived == null ? "null" : arrived.getClass().getSimpleName())
						+ ", not the requested " + levelClass.getSimpleName()
						+ (Reflect.hasField( Dungeon.class, "forcedLevelClass" ) ? ""
						: " (this worktree has no Dungeon.forcedLevelClass, so the level class cannot be forced)") );
			}
		}
		dismissStartupWindows();
		Log.step( "arrived: " + get( this::describeWorld ) );
	}

	/** 存档并从存档重新载入当前局（走 InterlevelScene CONTINUE），用来验证存读档不丢状态。 */
	public void saveAndReload() {
		Log.step( "save and reload" );
		world( "save game and reload it", () -> {
			try {
				Dungeon.saveAll();
			} catch (java.io.IOException e) {
				throw new RuntimeException( "Dungeon.saveAll failed", e );
			}
			InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
			Game.switchScene( InterlevelScene.class );
		} );
		waitReady();
		dismissStartupWindows();
	}

	private String describeHero() {
		Hero h = Dungeon.hero;
		if (h == null) return "no hero";
		return String.format( Locale.ROOT, "t=%.2f depth=%d hero@%s HP=%d/%d%s", Actor.now(), Dungeon.depth, cellStr( h.pos ),
				h.HP, h.HT, h.isAlive() ? "" : " DEAD" );
	}

	private String describeWorld() {
		Level l = Dungeon.level;
		return "depth=" + Dungeon.depth + " level=" + (l == null ? "null" : l.getClass().getSimpleName() + " " + l.width() + "x" + l.height())
				+ " hero=" + Dungeon.hero.heroClass + " pos=" + cellStr( Dungeon.hero.pos ) + " HP=" + Dungeon.hero.HP + "/" + Dungeon.hero.HT
				+ " mobs=" + (l == null ? 0 : l.mobs.size()) + " seed=" + Dungeon.customSeedText;
	}

	// ------------------------------------------------------------------ 坐标

	/** (x, y) → 格子编号（按当前关卡宽度）。 */
	public int xy( int x, int y ) {
		return x + y * Dungeon.level.width();
	}

	public String cellStr( int cell ) {
		Level l = Dungeon.level;
		if (l == null || cell < 0) return "#" + cell;
		return "(" + (cell % l.width()) + "," + (cell / l.width()) + ")#" + cell;
	}

	// ------------------------------------------------------------------ 玩家动作

	private static final class Mark {
		float now;
		int sceneId, depth, heroPos, heroHP;
	}

	private Mark mark() {
		return onRender( "mark", false, () -> {
			Mark m = new Mark();
			m.now = Actor.now();
			m.sceneId = System.identityHashCode( Game.scene() );
			m.depth = Dungeon.depth;
			m.heroPos = Dungeon.hero.pos;
			m.heroHP = Dungeon.hero.HP;
			return m;
		} );
	}

	/** 自上次 mark 以来游戏里是否发生了「回合级」的事情。 */
	private boolean changedSince( Mark m ) {
		return onRender( "compare", false, () ->
				Actor.now() != m.now
						|| System.identityHashCode( Game.scene() ) != m.sceneId
						|| Dungeon.depth != m.depth
						|| Dungeon.hero.pos != m.heroPos
						|| !Dungeon.hero.isAlive() );
	}

	private Status afterInput( String what, Mark before, boolean mustHaveEffect ) {
		Status st = waitReady();
		Log.line( "STATE", "after [" + what + "]: " + st + " " + get( this::describeHero ) );
		if (mustHaveEffect && st == Status.READY && !changedSince( before )) {
			throw new HarnessError( "player input [" + what + "] had no effect (no game time passed, hero did not move). "
					+ "The game ignored it: wrong target? window in the way? " + snap().brief() );
		}
		return st;
	}

	/**
	 * 点一下地图上的某个格子（= 真实玩家的一次点击，走 GameScene.handleCell → CellSelector）。
	 * 默认监听器下：空地 = 走过去（可能连走多回合，直到到达或被打断）；敌人 = 攻击；
	 * 有物品选格提示时（法杖 / 投掷）= 选定目标。返回时游戏又在等输入（或英雄已死）。
	 */
	public Status tap( int cell ) {
		String what = "tap " + cellStr( cell );
		Mark m = mark();
		input( what, () -> {
			requireNoWindow( what );
			GameScene.handleCell( cell );
		} );
		return afterInput( what, m, true );
	}

	/** 同 tap，但点击没有任何效果时返回 false 而不是报错。 */
	public boolean tryTap( int cell ) {
		String what = "tap " + cellStr( cell );
		Mark m = mark();
		input( what, () -> {
			requireNoWindow( what );
			GameScene.handleCell( cell );
		} );
		waitReady();
		return changedSince( m );
	}

	/** 走到某格：反复点击直到到达（中途被打断会继续点）。到不了 → ERROR。 */
	public void walkTo( int cell ) {
		int stalls = 0;
		for (int i = 0; i < 200; i++) {
			int pos = get( () -> Dungeon.hero.pos );
			if (pos == cell) return;
			if (!get( () -> Dungeon.hero.isAlive() )) throw new HarnessError( "hero died while walking to " + cellStr( cell ) );
			boolean moved = tryTap( cell ) && get( () -> Dungeon.hero.pos ) != pos;
			stalls = moved ? 0 : stalls + 1;
			if (stalls >= 3) {
				throw new HarnessError( "walkTo " + cellStr( cell ) + ": hero is not getting closer (at " + cellStr( get( () -> Dungeon.hero.pos ) ) + ")" );
			}
		}
		throw new HarnessError( "walkTo " + cellStr( cell ) + ": gave up after 200 taps" );
	}

	/** 攻击某格上的敌人一次（等价于点击那个敌人）。 */
	public Status attack( int cell ) {
		return tap( cell );
	}

	/** 原地等待 n 回合（等价于按 n 次「等待」按钮）。英雄死亡时提前返回。 */
	public Status rest( int turns ) {
		Status st = Status.READY;
		for (int i = 0; i < turns; i++) {
			String what = "wait 1 turn (" + (i + 1) + "/" + turns + ")";
			Mark m = mark();
			input( what, () -> {
				requireNoWindow( what );
				// 与 Toolbar 的等待按钮相同
				if (!GameScene.cancel()) {
					Dungeon.hero.rest( false );
				} else {
					throw new HarnessError( "the wait button was consumed by GameScene.cancel() (a pending action / cell prompt was active)" );
				}
			} );
			st = afterInput( what, m, true );
			if (st == Status.DEAD) break;
		}
		return st;
	}

	/**
	 * 使用物品的某个动作（等价于在背包里点该物品再点动作按钮：Item.execute）。
	 * 不等待：需要选目标的动作随后调 selectCell；不需要目标的随后调 waitReady。
	 * 一步到位的写法见 read / drink / zap / throwAt / castAt。
	 */
	public void use( Item item, String action ) {
		String what = "use " + item.getClass().getSimpleName() + " action " + action;
		input( what, () -> {
			requireNoWindow( what );
			if (!Dungeon.hero.belongings.contains( item )) {
				throw new HarnessError( what + ": the item is not in the hero's inventory" );
			}
			item.execute( Dungeon.hero, action );
		} );
	}

	/** 给当前的选格提示（法杖 / 投掷 / 法术）选定目标格。当前没有选格提示 → ERROR。 */
	public Status selectCell( int cell ) {
		String what = "select target cell " + cellStr( cell );
		Mark m = mark();
		input( what, () -> {
			requireNoWindow( what );
			CellSelector cs = (CellSelector) sget( F_CELL_SELECTOR );
			if (cs.listener == null || cs.listener == sget( F_DEFAULT_LISTENER )) {
				throw new HarnessError( what + ": no targeting prompt is active (the item action did not ask for a target)" );
			}
			GameScene.handleCell( cell );
		} );
		return afterInput( what, m, true );
	}

	/** 不需要目标的物品动作，一步到位（读卷轴、喝药水等）。 */
	public Status useAndWait( Item item, String action ) {
		Mark m = mark();
		use( item, action );
		return afterInput( "use " + item.getClass().getSimpleName() + " action " + action, m, true );
	}

	/** 需要目标的物品动作，一步到位（法杖 ZAP、投掷 THROW、法术 CAST）。 */
	public Status useOn( Item item, String action, int cell ) {
		use( item, action );
		return selectCell( cell );
	}

	/** 用法杖射某格。 */
	public Status zap( Wand wand, int cell ) {
		return useOn( wand, Wand.AC_ZAP, cell );
	}

	/** 把物品投掷到某格。 */
	public Status throwAt( Item item, int cell ) {
		return useOn( item, Item.AC_THROW, cell );
	}

	/** 对某格施放定向法术（TargetedSpell，如水爆 AquaBlast）。 */
	public Status cast( Spell spell, int cell ) {
		return useOn( spell, Spell.AC_CAST, cell );
	}

	/** 读卷轴（不需要选目标的那类）。 */
	public Status read( Scroll scroll ) {
		return useAndWait( scroll, Scroll.AC_READ );
	}

	/**
	 * 喝药水（喝下去立刻生效的那类）。
	 * 已鉴定的有害药水（液火、毒气……）游戏会先弹确认窗，这个方法会因为「输入没有效果」而以 ERROR 结束；
	 * 那种情况请写成：h.use( potion, Potion.AC_DRINK ); h.chooseOption( 0 ); h.waitReady();
	 */
	public Status drink( Potion potion ) {
		return useAndWait( potion, Potion.AC_DRINK );
	}

	// ------------------------------------------------------------------ 窗口

	private List<Window> windowsNow() {
		List<Window> out = new ArrayList<>();
		Object scene = sget( F_SCENE );
		if (scene == null) return out;
		for (Gizmo g : membersOf( (Group) scene )) {
			if (g instanceof Window) out.add( (Window) g );
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	private static List<Gizmo> membersOf( Group g ) {
		try {
			List<Gizmo> out = new ArrayList<>();
			// Group 的 add / erase / update / draw 都是 synchronized(this)：Actor 线程也会往场景里加东西
			// （Boss 被击杀的横幅、闪电特效……），所以这里必须拿同一把锁再遍历，否则偶尔会 ConcurrentModificationException。
			synchronized (g) {
				List<Gizmo> m = (List<Gizmo>) F_MEMBERS.get( g );
				if (m != null) {
					for (Gizmo x : m) {
						if (x != null) out.add( x );
					}
				}
			}
			return out;
		} catch (IllegalAccessException e) {
			throw new HarnessError( "reflection failed", e );
		}
	}

	private void requireNoWindow( String what ) {
		List<Window> ws = windowsNow();
		if (!ws.isEmpty()) {
			throw new HarnessError( what + ": a window is in the way: " + ws.get( ws.size() - 1 ).getClass().getName()
					+ " (handle it with chooseOption / closeWindows first)" );
		}
	}

	/** 当前打开的窗口类名（简化后的）。 */
	public List<String> windowNames() {
		return onRender( "list windows", false, () -> {
			List<String> out = new ArrayList<>();
			if (Game.scene() instanceof GameScene) {
				for (Window w : windowsNow()) out.add( w.getClass().getName().replace( "com.shatteredpixel.shatteredpixeldungeon.", "" ) );
			}
			return out;
		} );
	}

	/** 当前打开的、指定类型的窗口（没有则 null）。只能把返回值再交给 get(...) 在渲染线程里读。 */
	public <W extends Window> W findWindow( Class<W> type ) {
		return onRender( "find window", false, () -> {
			if (!(Game.scene() instanceof GameScene)) return null;
			W found = null;
			for (Window w : windowsNow()) {
				if (type.isInstance( w )) found = type.cast( w );
			}
			return found;
		} );
	}

	/** 关掉所有窗口（等价于逐个按返回键）。 */
	public void closeWindows() {
		for (int round = 0; round < 10; round++) {
			boolean any = onRender( "close windows", true, () -> {
				List<Window> ws = windowsNow();
				for (Window w : ws) w.onBackPressed();
				return !ws.isEmpty();
			} );
			if (!any) return;
			frames( 30 );
			if (windowNames().isEmpty()) return;
		}
		throw new HarnessError( "windows would not close: " + windowNames() );
	}

	/** 在最上层的 WndOptions 里点第 index 个按钮（0 起）。没有 WndOptions → ERROR。不等待。 */
	public void chooseOption( int index ) {
		String what = "choose option " + index;
		Log.step( "input: " + what );
		onRender( what, true, () -> {
			WndOptions top = null;
			for (Window w : windowsNow()) {
				if (w instanceof WndOptions) top = (WndOptions) w;
			}
			if (top == null) throw new HarnessError( what + ": no WndOptions window is open" );
			// 与按钮的 onClick 相同：先 hide 再 onSelect
			top.hide();
			Reflect.call( top, "onSelect", new Class<?>[]{ int.class }, index );
			return null;
		} );
	}

	/** 用放大镜查看某格（Toolbar 的查看按钮 → GameScene.examineCell）。之后用 findWindow 取窗口。 */
	public void examine( int cell ) {
		String what = "examine " + cellStr( cell );
		input( what, () -> {
			requireNoWindow( what );
			GameScene.examineCell( cell );
		} );
		frames( 3 );
	}

	/** 窗口（或任何 Group）里所有的角色精灵，深度优先。只能在 get(...) / world(...) 里调用。 */
	public static List<CharSprite> charSpritesIn( Group g ) {
		List<CharSprite> out = new ArrayList<>();
		collectSprites( g, out, 0 );
		return out;
	}

	private static void collectSprites( Group g, List<CharSprite> out, int depth ) {
		if (depth > 12) return;
		for (Gizmo x : membersOf( g )) {
			if (x instanceof CharSprite) out.add( (CharSprite) x );
			if (x instanceof Group) collectSprites( (Group) x, out, depth + 1 );
		}
	}

	// ------------------------------------------------------------------ 截图

	private int shotCount = 0;

	/**
	 * 把下一帧的画面存成 PNG，返回文件的绝对路径（本场景运行目录下的 shots\NN-name.png），并在结论摘要里记一条 note。
	 *
	 * 画面是画进离屏缓冲再读回来的，窗口仍然不可见。尺寸 = 隐藏窗口的尺寸（默认 480x800，run.ps1 的 -Width / -Height 可改）。
	 * 只负责把画面存下来给人（或能看图的 agent）看；工具不做图像比对，画面对不对仍然要靠状态断言。
	 * 截的是「调用之后的下一帧」：想截某个动画播完后的样子，先 frames( n ) / waitUntil(…) 再截。
	 * 带 -SkipDraw 运行时没有画面可截：只记一条 note，返回 null（场景照常往下跑）。
	 */
	public String screenshot( String name ) {
		if (HarnessGame.skipDraw) {
			note( "screenshot '" + name + "' skipped: drawing is switched off (-SkipDraw)" );
			return null;
		}
		String safe = (name == null || name.isEmpty()) ? "shot" : name.replaceAll( "[^A-Za-z0-9._-]", "_" );
		java.io.File file = new java.io.File( new java.io.File( Result.runDir, "shots" ),
				String.format( Locale.ROOT, "%02d-%s.png", ++shotCount, safe ) );
		Log.step( "screenshot -> " + file );
		HarnessGame.requestShot( file );
		long t0 = System.nanoTime();
		String r;
		while ((r = HarnessGame.shotResult()) == null) {
			if (Result.finishing()) parkForever();
			if ((System.nanoTime() - t0) / 1_000_000L > renderCallTimeoutMs) {
				Result.finish( Result.Verdict.STUCK, "render thread stopped producing frames",
						"RENDER THREAD HANG (no frame for the screenshot)\n" + stuckReport( null ) );
			}
			sleep( 2 );
		}
		if (!"ok".equals( r )) throw new HarnessError( "screenshot( " + name + " ) failed: " + r );
		note( "screenshot: " + file.getAbsolutePath() );
		return file.getAbsolutePath();
	}

	// ------------------------------------------------------------------ 世界操作

	/** 在某格生成一只怪（已 new 好的实例，可以先设 HP / 状态 / 阵营）。返回同一个实例。 */
	public <M extends Mob> M spawn( M mob, int cell ) {
		world( "spawn " + mob.getClass().getSimpleName() + " at " + cellStr( cell ), () -> {
			if (Actor.findChar( cell ) != null) {
				throw new HarnessError( "cannot spawn at " + cellStr( cell ) + ": cell is occupied by " + Actor.findChar( cell ).getClass().getSimpleName() );
			}
			mob.pos = cell;
			GameScene.add( mob );
			if (mob.sprite != null) {
				mob.sprite.place( cell );
				mob.sprite.visible = Dungeon.level.heroFOV[cell];
			}
		} );
		return mob;
	}

	/** 改一格地形（Terrain.* 常量），并刷新地图显示。 */
	public void setTerrain( int cell, int terrain ) {
		world( "terrain " + cellStr( cell ) + " = " + terrain, () -> {
			Level.set( cell, terrain );
			GameScene.updateMap( cell );
		} );
	}

	/** 把矩形区域（含两端）整片改成某种地形。 */
	public void fillTerrain( int x0, int y0, int x1, int y1, int terrain ) {
		world( "terrain rect (" + x0 + "," + y0 + ")-(" + x1 + "," + y1 + ") = " + terrain, () -> {
			for (int y = y0; y <= y1; y++) {
				for (int x = x0; x <= x1; x++) {
					int cell = x + y * Dungeon.level.width();
					Level.set( cell, terrain );
					GameScene.updateMap( cell );
				}
			}
		} );
	}

	/** 在某格放一个可见的陷阱。 */
	public <T extends Trap> T placeTrap( T trap, int cell ) {
		world( "trap " + trap.getClass().getSimpleName() + " at " + cellStr( cell ), () -> {
			Level.set( cell, Terrain.TRAP );
			Dungeon.level.setTrap( trap, cell );
			trap.reveal();
			GameScene.updateMap( cell );
		} );
		return trap;
	}

	/**
	 * 给英雄一件物品（已 new 好的实例，可先设等级 / 数量 / 附魔）。自动鉴定。
	 * 返回背包里实际的那个实例（可堆叠物品会并进已有的那一叠）。
	 */
	@SuppressWarnings("unchecked")
	public <I extends Item> I give( I item ) {
		final Object[] box = new Object[1];
		world( "give " + item.getClass().getSimpleName() + " x" + item.quantity(), () -> {
			item.identify();
			if (!item.collect()) {
				throw new HarnessError( "could not put " + item.getClass().getSimpleName() + " into the backpack (full?)" );
			}
			Item inBag = Dungeon.hero.belongings.contains( item ) ? item : Dungeon.hero.belongings.getItem( item.getClass() );
			if (inBag == null) throw new HarnessError( "item vanished after collect(): " + item.getClass().getSimpleName() );
			box[0] = inBag;
		} );
		return (I) box[0];
	}

	/** 直接把英雄放到某格（不触发陷阱 / 地形效果；门格请用 walkTo 真走过去）。 */
	public void placeHero( int cell ) {
		world( "place hero at " + cellStr( cell ), () -> {
			if (Actor.findChar( cell ) != null && Actor.findChar( cell ) != Dungeon.hero) {
				throw new HarnessError( "cannot place hero at " + cellStr( cell ) + ": occupied" );
			}
			Hero hero = Dungeon.hero;
			hero.sprite.interruptMotion();
			hero.pos = cell;
			hero.sprite.place( cell );
			hero.sprite.visible = true;
		} );
	}

	/**
	 * 直接设置英雄的生命上限与当前生命。
	 * 上限的差额记在 Hero.HTBoost 里（游戏自己的「额外生命上限」字段，会进存档），
	 * 所以英雄升级、游戏重算生命上限（Hero.updateHT）时不会被冲掉。
	 * 目标代码里没有这个字段时退回成直接写 HT（那样的话升级会把上限算回去）。
	 */
	public void setHeroHP( int hp, int ht ) {
		world( "hero HP=" + hp + "/" + ht, () -> {
			Hero hero = Dungeon.hero;
			if (Reflect.hasField( Hero.class, "HTBoost" )) {
				Reflect.set( hero, "HTBoost", 0 );
				hero.updateHT( false );
				Reflect.set( hero, "HTBoost", ht - hero.HT );
				hero.updateHT( false );
			}
			if (hero.HT != ht) hero.HT = ht;
			hero.HP = hp;
		} );
	}

	/** 给角色加一个 buff（= Buff.affect）。返回 buff 实例，可以在 world(...) 里继续设置它。 */
	@SuppressWarnings("unchecked")
	public <B extends Buff> B affect( Char target, Class<B> buffClass ) {
		final Object[] box = new Object[1];
		world( "buff " + buffClass.getSimpleName() + " on " + target.getClass().getSimpleName(),
				() -> box[0] = Buff.affect( target, buffClass ) );
		return (B) box[0];
	}

	/** 给角色加一个限时 buff，持续 duration 回合（= Buff.affect(target, cls, duration)）。 */
	@SuppressWarnings("unchecked")
	public <B extends FlavourBuff> B affect( Char target, Class<B> buffClass, float duration ) {
		final Object[] box = new Object[1];
		world( "buff " + buffClass.getSimpleName() + " (" + duration + " turns) on " + target.getClass().getSimpleName(),
				() -> box[0] = Buff.affect( target, buffClass, duration ) );
		return (B) box[0];
	}

	/** 去掉角色身上某一类 buff（= Buff.detach）。 */
	public void detach( Char target, Class<? extends Buff> buffClass ) {
		world( "remove buff " + buffClass.getSimpleName() + " from " + target.getClass().getSimpleName(),
				() -> Buff.detach( target, buffClass ) );
	}

	// ------------------------------------------------------------------ 断言 / 记录

	/** 期望行为断言：不成立记为失败（场景继续跑，最后结论 FAIL）。返回 cond。 */
	public boolean check( boolean cond, String expectation ) {
		if (cond) Result.checkPassed( expectation );
		else Result.checkFailed( expectation );
		return cond;
	}

	/** 期望行为断言：不成立立刻以 FAIL 结束场景。 */
	public void checkFatal( boolean cond, String expectation ) {
		if (cond) {
			Result.checkPassed( expectation );
		} else {
			Result.checkFailed( expectation );
			throw new CheckFailed( expectation );
		}
	}

	/** 立刻以 FAIL 结束场景。 */
	public void fail( String what ) {
		Result.checkFailed( what );
		throw new CheckFailed( what );
	}

	/** 前置条件：不成立说明场景没搭对（或游戏接口变了），以 ERROR 结束，不算对游戏行为的结论。 */
	public void require( boolean cond, String precondition ) {
		if (!cond) throw new HarnessError( "scenario precondition not met: " + precondition );
		Log.line( "PRE  ", "ok   - " + precondition );
	}

	/** 写进结论摘要的事实（实测数值等）。 */
	public void note( String text ) {
		Result.note( text );
	}

	public void log( String text ) {
		Log.info( text );
	}

	// ------------------------------------------------------------------ 常用查询

	public boolean heroAlive() {
		return get( () -> Dungeon.hero != null && Dungeon.hero.isAlive() );
	}

	public int heroPos() {
		return get( () -> Dungeon.hero.pos );
	}

	public int depth() {
		return get( () -> Dungeon.depth );
	}

	/** 当前的游戏时间（回合）。换层后会被游戏重置基准，只适合在同一层里做差。 */
	public float now() {
		return get( Actor::now );
	}

	/** 英雄精灵是否正画在英雄所在的格子上（位置误差 < 1 像素）。只能在 get(...) 里调用。 */
	public static boolean spriteOnCell( CharSprite sprite, int cell ) {
		PointF want = sprite.worldToCamera( cell );
		return Math.abs( sprite.x - want.x ) < 1f && Math.abs( sprite.y - want.y ) < 1f;
	}

	/** 精灵当前画在哪一格（按精灵底边中点估算）。只能在 get(...) 里调用。 */
	public static int spriteCell( CharSprite sprite ) {
		int w = Dungeon.level.width();
		int cx = (int) Math.floor( (sprite.x + sprite.width() / 2f) / 16f );
		int cy = (int) Math.floor( (sprite.y + sprite.height() - 1f) / 16f );
		return cx + cy * w;
	}

	/** 到目前为止的游戏消息里是否出现过包含 text 的一条。 */
	public boolean gameLogContains( String text ) {
		for (String l : HarnessLogger.gameLines()) {
			if (l.contains( text )) return true;
		}
		return false;
	}
}
