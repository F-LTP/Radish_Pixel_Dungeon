package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DruidBear;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DruidBoar;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DruidOakSage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DruidSunflower;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DruidVines;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DruidWolf;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 德鲁伊：女猎手的第三转职。
 *
 * 通过踩踏绿草或用灵能之弓击杀敌人收集自然能量，消耗能量召唤动物伙伴、植物与释放自然法术。
 */
public class Druid extends HeroSubClass {

	public Druid() {
		super("DRUID", HeroIcon.DRUID);
	}

	@Override
	public Talent[] subclassT3() {
		return new Talent[]{ Talent.ANIMAL_COMPANION, Talent.NATURE_BOND, Talent.ELEMENTAL_MASTERY };
	}

	@Override
	public Talent[] subclassT4() {
		return new Talent[]{ Talent.BRISK_PACE, Talent.PHASE_FILLING };
	}

	// ===== 自然能量 =====

	public static float energy( Hero hero ) {
		DruidEnergy e = hero.buff( DruidEnergy.class );
		return e == null ? 0f : e.energy;
	}

	public static float maxEnergy( Hero hero ) {
		// 设计稿：上限 = 当前等级 + 当前楼层 + 10
		return hero.lvl + Dungeon.depth + 10;
	}

	public static void gainEnergy( Hero hero, float amount ) {
		try {
			if (hero == null || hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.DRUID) {
				return;
			}
			Buff.affect( hero, DruidEnergy.class ).gain( amount );
		} catch (Throwable t) {
		}
	}

	public static boolean spendEnergy( Hero hero, float amount ) {
		try {
			if (hero == null || hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.DRUID) {
				return false;
			}
			DruidEnergy e = hero.buff( DruidEnergy.class );
			return e != null && e.useEnergy( amount );
		} catch (Throwable t) {
			return false;
		}
	}

	@Override
	public void onMove( Hero hero ) {
		// 自然能量的踩草回复改由 HeroTrampleGrassEvent 处理（onMove 在位移前触发，读不到落点）
	}

	@com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent(
			event = com.shatteredpixel.shatteredpixeldungeon.events.HeroTrampleGrassEvent.class, priority = 0)
	public static void onTrampleGrass( com.shatteredpixel.shatteredpixeldungeon.events.HeroTrampleGrassEvent event ) {
		Hero hero = event.getHero();
		if (hero == null
				|| hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.DRUID) {
			return;
		}
		// 只有踩到真正的绿草（高草）才获得能量，枯草不计
		int terr = event.getLevel().map[event.getCell()];
		if (terr != com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.HIGH_GRASS) {
			return;
		}
		gainEnergy( hero, 0.5f );
	}

	/** 击杀敌人时由 Hero.earnExp 调用。 */
	public static void onKill( Hero hero ) {
		gainEnergy( hero, 1f + 0.1f * Dungeon.depth );
	}

	// ===== 召唤动物 =====

	private static float animalPower( Hero hero ) {
		return hero.pointsInTalent( Talent.ANIMAL_COMPANION ) >= 1 ? 1.25f : 1f;
	}

	public static boolean summonBoar( Hero hero ) {
		DruidBoar boar = new DruidBoar();
		boar.lvl = hero.lvl;
		boar.power = animalPower( hero );
		boar.HP = boar.HT = Math.round( (30 + hero.lvl * 1.5f) * boar.power );
		return spawnAnimal( hero, boar, animalCost( hero, 12f ) );
	}

	public static float animalCost( Hero hero, float base ) {
		int pts = hero.pointsInTalent( Talent.ANIMAL_COMPANION );
		if (pts >= 3) {
			return base + 6f;
		}
		if (pts >= 2) {
			return base + 2f;
		}
		return base;
	}

	public static boolean summonWolves( Hero hero ) {
		DruidWolf[] pack = new DruidWolf[3];
		for (int i = 0; i < pack.length; i++) {
			DruidWolf wolf = new DruidWolf();
			wolf.lvl = hero.lvl;
			wolf.power = animalPower( hero );
			wolf.HP = wolf.HT = Math.round( (22 + hero.lvl / 2f) * wolf.power );
			pack[i] = wolf;
		}
		return summon( hero, animalCost( hero, 16f ), animalsToReplace( hero, DruidWolf.class ), pack );
	}

	public static boolean summonBear( Hero hero ) {
		DruidBear bear = new DruidBear();
		bear.lvl = hero.lvl;
		bear.power = animalPower( hero );
		bear.HP = bear.HT = Math.round( (60 + hero.lvl * 2.2f) * bear.power );
		return spawnAnimal( hero, bear, animalCost( hero, 20f ) );
	}

	private static boolean spawnAnimal( Hero hero, Mob m, float cost ) {
		return summon( hero, cost, animalsToReplace( hero, m.getClass() ), m );
	}

	// 这次召唤会顶替掉场上哪些动物：同种的全部；其它种类已经占满名额时，其它种类也全部顶替
	private static ArrayList<Mob> animalsToReplace( Hero hero, Class<?> newType ) {
		int allowed = hero.pointsInTalent( Talent.ANIMAL_COMPANION ) >= 3 ? 3
				: hero.pointsInTalent( Talent.ANIMAL_COMPANION ) >= 2 ? 2 : 1;
		ArrayList<Mob> animals = new ArrayList<>();
		for (Char ch : Actor.chars()) {
			if (ch instanceof DruidBoar || ch instanceof DruidWolf || ch instanceof DruidBear) {
				animals.add( (Mob) ch );
			}
		}
		java.util.HashSet<Class<?>> otherTypes = new java.util.HashSet<>();
		for (Mob a : animals) {
			if (a.getClass() != newType) {
				otherTypes.add( a.getClass() );
			}
		}
		ArrayList<Mob> replaced = new ArrayList<>();
		for (Mob a : animals) {
			if (a.getClass() == newType || otherTypes.size() >= allowed) {
				replaced.add( a );
			}
		}
		return replaced;
	}

	// 植物同一时间只留一株，新召唤的会顶替场上已有的任何一种
	private static ArrayList<Mob> plantsToReplace() {
		ArrayList<Mob> replaced = new ArrayList<>();
		for (Char ch : Actor.chars()) {
			if (ch instanceof DruidOakSage || ch instanceof DruidVines || ch instanceof DruidSunflower) {
				replaced.add( (Mob) ch );
			}
		}
		return replaced;
	}

	// 所有召唤共用的结算。顺序不能换：先确认能量够、周围有落点，然后才扣能量、顶替旧召唤物。
	// 反过来做的话，周围没有空位时旧召唤物已经白白消失，狼群还会照扣能量却一只都不出来。
	private static boolean summon( Hero hero, float cost, ArrayList<Mob> replaced, Mob... fresh ) {
		if (energy( hero ) < cost) {
			GLog.w( Messages.get( HeroSubClass.class, "druid_no_energy" ) );
			return false;
		}
		ArrayList<Integer> spots = findSpots( hero, replaced, fresh.length );
		if (spots.isEmpty()) {
			GLog.w( Messages.get( HeroSubClass.class, "druid_no_space" ) );
			return false;
		}
		if (!spendEnergy( hero, cost )) {
			return false;
		}
		for (Mob old : replaced) {
			old.die( null );
		}
		// 落点比要召唤的数量少时，能放几个放几个（只有狼群会遇到）
		for (int i = 0; i < fresh.length && i < spots.size(); i++) {
			int c = spots.get( i );
			fresh[i].pos = c;
			GameScene.add( fresh[i] );
			CellEmitter.get( c ).burst( Speck.factory( Speck.STAR ), 10 );
		}
		return true;
	}

	// 在英雄周围两圈内由近到远找落点，最多找 amount 个。
	// vacating 里的旧召唤物马上会被顶替，它们站着的格子也算空位。
	private static ArrayList<Integer> findSpots( Hero hero, ArrayList<Mob> vacating, int amount ) {
		ArrayList<Integer> spots = new ArrayList<>();
		int w = Dungeon.level.width();
		int hx = hero.pos % w, hy = hero.pos / w;
		for (int radius = 1; radius <= 2; radius++) {
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dx = -radius; dx <= radius; dx++) {
					if (Math.max( Math.abs( dx ), Math.abs( dy ) ) != radius) {
						continue;
					}
					int nx = hx + dx, ny = hy + dy;
					if (nx < 0 || ny < 0 || nx >= w || ny >= Dungeon.level.height()) {
						continue;
					}
					int c = ny * w + nx;
					if (spots.size() < amount
							&& Dungeon.level.passable[c] && !Dungeon.level.solid[c]
							&& isVacant( c, vacating )) {
						spots.add( c );
					}
				}
			}
		}
		return spots;
	}

	// 沿用原先的双重检查：既查回合循环里登记的角色，也查本层的怪物列表
	private static boolean isVacant( int cell, ArrayList<Mob> vacating ) {
		Char ch = Actor.findChar( cell );
		if (ch != null && !vacating.contains( ch )) {
			return false;
		}
		for (Mob m : Dungeon.level.mobs) {
			if (m.pos == cell && !vacating.contains( m )) {
				return false;
			}
		}
		return true;
	}

	// ===== 召唤植物 =====

	public static boolean summonOakSage( Hero hero ) {
		DruidOakSage sage = new DruidOakSage();
		int pts = hero.pointsInTalent( Talent.NATURE_BOND );
		sage.lifespan = 200 + pts * 50;
		return spawnPlant( hero, sage, 15f );
	}

	public static boolean summonVines( Hero hero ) {
		DruidVines vines = new DruidVines();
		vines.grassTiles = countGrass( hero.pos, 1 );
		vines.lvl = hero.lvl;
		int pts = hero.pointsInTalent( Talent.NATURE_BOND );
		float hpMul = pts >= 1 ? 1.5f : 1f;
		vines.HP = vines.HT = Math.max( 1, Math.round( (30 + hero.lvl * 2.2f) * hpMul ) );
		return spawnPlant( hero, vines, 10f );
	}

	public static boolean summonSunflower( Hero hero ) {
		DruidSunflower flower = new DruidSunflower();
		flower.grassTiles = countGrass( hero.pos, 2 );
		int pts = hero.pointsInTalent( Talent.NATURE_BOND );
		float hpMul = pts >= 2 ? 1.5f : 1f;
		flower.HP = flower.HT = Math.round( (80 + Dungeon.depth * 1.5f) * hpMul );
		flower.power = pts >= 2 ? 1.5f : 1f;
		return spawnPlant( hero, flower, 30f );
	}

	private static boolean spawnPlant( Hero hero, Mob m, float cost ) {
		return summon( hero, cost, plantsToReplace(), m );
	}

	private static int countGrass( int center, int radius ) {
		int w = Dungeon.level.width();
		int cx = center % w, cy = center / w;
		int count = 0;
		for (int y = cy - radius; y <= cy + radius; y++) {
			for (int x = cx - radius; x <= cx + radius; x++) {
				if (x < 0 || y < 0 || x >= w || y >= Dungeon.level.height()) {
					continue;
				}
				int c = y * w + x;
				int t = Dungeon.level.map[c];
				if (t == Terrain.GRASS || t == Terrain.HIGH_GRASS || t == Terrain.FURROWED_GRASS) {
					count++;
				}
			}
		}
		return count;
	}

	// ===== 自然法术 =====

	public static float spellCost( Hero hero, float base ) {
		int pts = hero.pointsInTalent( Talent.ELEMENTAL_MASTERY );
		float reduce = pts >= 3 ? 2f : (pts >= 1 ? 1f : 0f);
		return Math.max( 0f, base - reduce );
	}

	public static boolean castEnchantArrow( Hero hero ) {
		if (!spendEnergy( hero, spellCost( hero, 5f ) )) {
			GLog.w( Messages.get( HeroSubClass.class, "druid_no_energy" ) );
			return false;
		}
		Buff.affect( hero, EnchantArrow.class, 20f );
		return true;
	}

	public static boolean castFirestorm( Hero hero, int cell ) {
		if (!spendEnergy( hero, spellCost( hero, 10f ) )) {
			return false;
		}
		int dmg = 12 + hero.lvl;
		java.util.HashSet<Char> hit = new java.util.HashSet<>();
		for (int pos : lineCells( hero.pos, cell )) {
			if (pos < 0 || pos >= Dungeon.level.length()) {
				continue;
			}
			// 火焰沿路径扩散，命中路径两侧一格内的敌人
			for (int off : com.watabou.utils.PathFinder.NEIGHBOURS9) {
				int c = pos + off;
				if (c < 0 || c >= Dungeon.level.length()) {
					continue;
				}
				Char ch = com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar( c );
				if (ch != null && ch.alignment == Char.Alignment.ENEMY && ch.isAlive()
						&& hit.add( ch )) {
					ch.damage( new com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo(
							dmg, com.shatteredpixel.shatteredpixeldungeon.damage.DamageType.MAGICAL, hero, null, hero ) );
					ignite( ch );
					if (hero.pointsInTalent( Talent.ELEMENTAL_MASTERY ) >= 1 && ch.isAlive()) {
						knockBack( ch, hero.pos );
					}
				}
			}
			// 火焰要交给 GameScene.add 登记才会进回合循环；只 seed 的话本层第一次起火时当回合不烧，
			// 要等别处起火或读档才突然烧起来。地图最外圈不放火：Fire 在边界行上演算会数组越界。
			if (pos != hero.pos && Dungeon.level.insideMap( pos )) {
				com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(
						com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob.seed(
								pos, 1, com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire.class ) );
			}
			com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( pos ).burst(
					com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory(
							com.shatteredpixel.shatteredpixeldungeon.effects.Speck.INFERNO ), 4 );
		}
		com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( cell ).burst(
				com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory( com.shatteredpixel.shatteredpixeldungeon.effects.Speck.INFERNO ), 12 );
		com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( hero.pos ).burst(
				com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory( com.shatteredpixel.shatteredpixeldungeon.effects.Speck.INFERNO ), 6 );
		com.watabou.noosa.audio.Sample.INSTANCE.play( com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.BURNING );
		return true;
	}

	public static boolean castThundercloud( Hero hero, int cell ) {
		if (!spendEnergy( hero, spellCost( hero, 16f ) )) {
			return false;
		}
		int dmg = 16 + hero.lvl;
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : Dungeon.level.mobs.toArray( new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0] )) {
			if (mob.alignment == Char.Alignment.ENEMY && mob.isAlive()
					&& Dungeon.level.distance( cell, mob.pos ) <= 2) {
				mob.damage( new com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo( dmg, com.shatteredpixel.shatteredpixeldungeon.damage.DamageType.MAGICAL, hero, null, hero ) );
				com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( mob.pos ).burst(
						com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory( com.shatteredpixel.shatteredpixeldungeon.effects.Speck.STORM ), 6 );
			}
		}
		com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( cell ).burst(
				com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory( com.shatteredpixel.shatteredpixeldungeon.effects.Speck.STORM ), 12 );
		com.watabou.noosa.audio.Sample.INSTANCE.play( com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.ZAP );
		ThunderField f = Buff.affect( hero, ThunderField.class );
		f.cell = cell;
		f.turns = 10;
		f.depth = Dungeon.depth;
		f.branch = Dungeon.branchId;
		return true;
	}

	public static boolean castCataclysm( Hero hero, int cell ) {
		if (!spendEnergy( hero, spellCost( hero, 30f ) )) {
			return false;
		}
		int dmg = 30 + hero.lvl;
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob : Dungeon.level.mobs.toArray( new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0] )) {
			if (mob.alignment == Char.Alignment.ENEMY && mob.isAlive()
					&& Dungeon.level.distance( cell, mob.pos ) <= 3) {
				mob.damage( new com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo( dmg, com.shatteredpixel.shatteredpixeldungeon.damage.DamageType.MAGICAL, hero, null, hero ) );
				ignite( mob );
				com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( mob.pos ).burst(
						com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory( com.shatteredpixel.shatteredpixeldungeon.effects.Speck.INFERNO ), 6 );
			}
		}
		com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( cell ).burst(
				com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory( com.shatteredpixel.shatteredpixeldungeon.effects.Speck.ROCK ), 16 );
		com.watabou.noosa.audio.Sample.INSTANCE.play( com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.ROCKS );
		VolcanoField v = Buff.affect( hero, VolcanoField.class );
		v.cell = cell;
		v.turns = 50 + (hero.pointsInTalent( Talent.ELEMENTAL_MASTERY ) >= 3 ? 25 : 0);
		v.depth = Dungeon.depth;
		v.branch = Dungeon.branchId;
		return true;
	}

	// 雷场和火山是放在某一层地面上的，但它们的 buff 挂在英雄身上，会跟着英雄换层。
	// 所以施放时记下楼层与分支，英雄不在那一层时让场地消散；
	// 否则它会在新楼层的同一个格子编号上继续打怪，位置和原来毫无关系。
	private static boolean onCastFloor( int depth, String branch ) {
		return depth == Dungeon.depth && java.util.Objects.equals( branch, Dungeon.branchId );
	}

	// 只 Buff.affect 而不 reignite 的燃烧剩余时间是 0，烧一跳就熄灭；这里按原版火系效果的写法补上 reignite。
	// 已被这次伤害打死的目标不再点燃，免得给尸体挂上燃烧特效。
	private static void ignite( Char ch ) {
		if (ch.isAlive()) {
			Buff.affect( ch, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class ).reignite( ch );
		}
	}

	/** 从 from 到 to 的直线格（Bresenham）。 */
	private static java.util.ArrayList<Integer> lineCells( int from, int to ) {
		java.util.ArrayList<Integer> cells = new java.util.ArrayList<>();
		int w = Dungeon.level.width();
		int x0 = from % w, y0 = from / w, x1 = to % w, y1 = to / w;
		int dx = Math.abs( x1 - x0 ), dy = Math.abs( y1 - y0 );
		int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
		int err = dx - dy, x = x0, y = y0;
		while (true) {
			cells.add( y * w + x );
			if (x == x1 && y == y1) {
				break;
			}
			int e2 = 2 * err;
			if (e2 > -dy) {
				err -= dy;
				x += sx;
			}
			if (e2 < dx) {
				err += dx;
				y += sy;
			}
		}
		return cells;
	}

	// 击退统一走 WandOfBlastWave.throwChar：推动动画、落点压格（陷阱、门、草），以及首领减半、
	// 被定身或不可移动的单位不动、大体型不进窄道这些规则都在那里，不要在这里手写改坐标。
	private static void knockBack( Char ch, int from ) {
		// 先画一条从施法者穿过目标的弹道，再截取目标身后的那一段作为击退路线
		Ballistica trajectory = new Ballistica( from, ch.pos, Ballistica.STOP_TARGET );
		trajectory = new Ballistica( trajectory.collisionPos,
				trajectory.path.get( trajectory.path.size() - 1 ), Ballistica.PROJECTILE );
		// 天赋文本写明击退 2 格；文本没有提撞墙伤害，所以不结算碰撞伤害与麻痹
		WandOfBlastWave.throwChar( ch, trajectory, 2, true, false, Dungeon.hero );
	}

	/** 附魔箭：持续期间灵能弓附魔效率提升并转为法术伤害。 */
	// 选格法术入口（供技能面板/侧边栏使用）
	public static void promptTargeted( Hero hero, int which ) {
		float base = which == 0 ? 10f : which == 1 ? 16f : 30f;
		if (energy( hero ) < spellCost( hero, base )) {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
					com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get( HeroSubClass.class, "druid_no_energy" ) );
			return;
		}
		pendingTargeted = which;
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.selectCell( targetedListener );
	}

	private static int pendingTargeted = -1;

	private static com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector.Listener targetedListener =
			new com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			Hero hero = Dungeon.hero;
			int which = pendingTargeted;
			pendingTargeted = -1;
			if (target == null || hero == null) {
				return;
			}
			boolean ok = which == 0 ? castFirestorm( hero, target )
					: which == 1 ? castThundercloud( hero, target )
					: castCataclysm( hero, target );
			if (ok) {
				hero.sprite.operate( hero.pos );
				hero.spendAndNext( 1f );
			}
		}

		@Override
		public String prompt() {
			return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get( HeroSubClass.class, "druid_spell_prompt" );
		}
	};

	public static class ThunderField extends Buff {

		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		public int cell = -1;
		public int turns = 0;
		// 施放时所在的楼层与分支，-1 表示还没有记录（旧存档）
		public int depth = -1;
		public String branch = null;

		@Override
		public boolean act() {
			if (cell < 0 || turns <= 0 || Dungeon.level == null) {
				detach();
				return true;
			}
			// 旧存档里没有记录楼层，按读档后第一次生效时所在的楼层算
			if (depth == -1) {
				depth = Dungeon.depth;
				branch = Dungeon.branchId;
			}
			if (!onCastFloor( depth, branch )) {
				detach();
				return true;
			}
			turns--;
			Hero hero = Dungeon.hero;
			int lvl = hero != null ? hero.lvl : 1;
			int bolts = 5 + (hero != null && hero.pointsInTalent( Talent.ELEMENTAL_MASTERY ) >= 2 ? 3 : 0);
			int dmg = 16 + lvl;
			int hits = 0;
			for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob
					: Dungeon.level.mobs.toArray( new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0] )) {
				if (hits >= bolts) {
					break;
				}
				if (mob.alignment == Char.Alignment.ENEMY && mob.isAlive()
						&& Dungeon.level.distance( cell, mob.pos ) <= 2) {
					mob.damage( new com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo(
							dmg, com.shatteredpixel.shatteredpixeldungeon.damage.DamageType.MAGICAL, hero, null, this ) );
					hits++;
				}
			}
			spend( TICK );
			return true;
		}

		@Override
		public String icon() {
			return BuffIndicator.NONE;
		}

		private static final String CELL = "cell";
		private static final String TURNS = "turns";
		private static final String DEPTH = "depth";
		private static final String BRANCH = "branch";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( CELL, cell );
			bundle.put( TURNS, turns );
			if (depth != -1) {
				bundle.put( DEPTH, depth );
				bundle.put( BRANCH, branch );
			}
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			cell = bundle.getInt( CELL );
			turns = bundle.getInt( TURNS );
			// 旧存档没有这两个键：保持未记录的状态，等第一次生效时再绑定楼层
			if (bundle.contains( DEPTH )) {
				depth = bundle.getInt( DEPTH );
				branch = bundle.getString( BRANCH );
			}
		}
	}

	/** 火山口：每 2 回合喷发落石。 */
	public static class VolcanoField extends Buff {

		{
			type = buffType.NEUTRAL;
			announced = false;
		}

		public int cell = -1;
		public int turns = 0;
		// 施放时所在的楼层与分支，-1 表示还没有记录（旧存档）
		public int depth = -1;
		public String branch = null;

		@Override
		public boolean act() {
			if (cell < 0 || turns <= 0 || Dungeon.level == null) {
				detach();
				return true;
			}
			// 旧存档里没有记录楼层，按读档后第一次生效时所在的楼层算
			if (depth == -1) {
				depth = Dungeon.depth;
				branch = Dungeon.branchId;
			}
			if (!onCastFloor( depth, branch )) {
				detach();
				return true;
			}
			turns--;
			if (turns % 2 == 0) {
				Hero hero = Dungeon.hero;
				int lvl = hero != null ? hero.lvl : 1;
				int dmg = 30 + lvl;
				for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob
						: Dungeon.level.mobs.toArray( new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[0] )) {
					if (mob.alignment == Char.Alignment.ENEMY && mob.isAlive()
							&& Dungeon.level.distance( cell, mob.pos ) <= 3) {
						mob.damage( new com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo(
								dmg, com.shatteredpixel.shatteredpixeldungeon.damage.DamageType.MAGICAL, hero, null, this ) );
						ignite( mob );
					}
				}
			}
			spend( TICK );
			return true;
		}

		@Override
		public String icon() {
			return BuffIndicator.NONE;
		}

		private static final String CELL = "cell";
		private static final String TURNS = "turns";
		private static final String DEPTH = "depth";
		private static final String BRANCH = "branch";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( CELL, cell );
			bundle.put( TURNS, turns );
			if (depth != -1) {
				bundle.put( DEPTH, depth );
				bundle.put( BRANCH, branch );
			}
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			cell = bundle.getInt( CELL );
			turns = bundle.getInt( TURNS );
			// 旧存档没有这两个键：保持未记录的状态，等第一次生效时再绑定楼层
			if (bundle.contains( DEPTH )) {
				depth = bundle.getInt( DEPTH );
				branch = bundle.getString( BRANCH );
			}
		}
	}

	public static class EnchantArrow extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		@Override
		public String icon() {
			return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.UPGRADE;
		}
	}

	public static class DruidEnergy extends Buff implements ActionIndicator.Action {

		{
			type = buffType.POSITIVE;
			announced = false;
			// 复活时会清掉所有不带这个标记的 buff；自然能量是转职资源，
			// 和 MonkEnergy、MagicPoint 这些职业资源一样要留下，否则复活后能量归零、法术书按钮也跟着消失
			revivePersists = true;
		}

		public float energy = 0f;

		public float max() {
			Hero hero = Dungeon.hero;
			// 设计稿：上限 = 当前等级 + 当前楼层 + 10
			return (hero == null ? 1 : hero.lvl) + Dungeon.depth + 10;
		}

		public void gain( float amount ) {
			energy = Math.min( max(), energy + amount );
			claimAction();
		}

		// 动作按钮只有一个槽位，而法术书只能从这个按钮打开，所以槽位一空出来就要补占。
		// 只在槽位空着或本来就是自己时动手，不去抢别的按钮（例如次元撕裂者的三回合窗口）。
		private void claimAction() {
			if (ActionIndicator.action == null) {
				ActionIndicator.setAction( this );
			} else if (ActionIndicator.action == this) {
				ActionIndicator.refresh();
			}
		}

		// 读档时 Hero.restoreFromBundle 的末尾会把动作槽整个清空，在 restoreFromBundle 里占位留不住。
		// 贴图挂接（进入场景、刚获得这个 buff）时每个 buff 都会收到一次 fx(true)，借这个时机占位。
		@Override
		public void fx( boolean on ) {
			if (on) {
				claimAction();
			}
		}

		// 每回合检查一次：别的按钮用完把槽位清空后，下一回合法术书按钮自己回来
		@Override
		public boolean act() {
			claimAction();
			spend( TICK );
			return true;
		}

		@Override
		public void detach() {
			ActionIndicator.clearAction( this );
			super.detach();
		}

		public boolean useEnergy( float amount ) {
			if (energy < amount) {
				return false;
			}
			energy -= amount;
			ActionIndicator.refresh();
			return true;
		}

		@Override
		public String icon() {
			return BuffIndicator.NONE;
		}

		@Override
		public String actionName() {
			return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get( HeroSubClass.class, "druid" );
		}

		@Override
		public String actionIcon() {
			return com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon.DRUID_SKILL;
		}

		@Override
		public com.watabou.noosa.Visual secondaryVisual() {
			com.watabou.noosa.BitmapText txt = new com.watabou.noosa.BitmapText(
					com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.pixelFont );
			txt.text( Integer.toString( (int) energy ) );
			txt.hardlight( com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE );
			txt.measure();
			return txt;
		}

		@Override
		public int indicatorColor() {
			return 0x3FA34D;
		}

		@Override
		public void doAction() {
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
					new com.shatteredpixel.shatteredpixeldungeon.windows.WndDruidSkills( this, false ) );
		}

		private static final String ENERGY = "energy";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( ENERGY, energy );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			energy = bundle.getFloat( ENERGY );
			// 旧版本里这个 buff 没有自己的 act，默认实现会把它的行动时间设成「永不行动」并存进存档。
			// 这里清零让它重新进入回合循环，否则旧存档里每回合补占按钮的逻辑不会运行。
			if (time >= Float.MAX_VALUE) {
				clearTime();
			}
		}
	}
}
