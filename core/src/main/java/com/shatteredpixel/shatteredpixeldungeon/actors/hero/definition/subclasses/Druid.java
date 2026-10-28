package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
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
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Bundle;

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
		return (hero.lvl + Dungeon.depth) * 10f;
	}

	public static void gainEnergy( Hero hero, float amount ) {
		try {
			if (hero == null || hero.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses.DRUID) {
				return;
			}
			Buff.affect( hero, DruidEnergy.class ).gain( amount );
		} catch (Throwable t) {
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w( "Druid energy gain error: " + t );
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
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w( "Druid energy spend error: " + t );
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
		boar.HP = boar.HT = Math.round( (40 + hero.lvl * 2) * boar.power );
		return spawnAnimal( hero, boar, animalCost( hero, 12f ) );
	}

	private static float animalCost( Hero hero, float base ) {
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
		int amount = 3;
		float cost = animalCost( hero, 16f );
		if (!spendEnergy( hero, cost )) {
			return false;
		}
		enforceAnimalLimit( hero, DruidWolf.class );
		for (int i = 0; i < amount; i++) {
			DruidWolf wolf = new DruidWolf();
			wolf.lvl = hero.lvl;
			wolf.power = animalPower( hero );
			wolf.HP = wolf.HT = Math.round( (30 + hero.lvl / 2f) * wolf.power );
			place( wolf, hero );
		}
		return true;
	}

	public static boolean summonBear( Hero hero ) {
		DruidBear bear = new DruidBear();
		bear.lvl = hero.lvl;
		bear.power = animalPower( hero );
		bear.HP = bear.HT = Math.round( (80 + hero.lvl * 3) * bear.power );
		return spawnAnimal( hero, bear, animalCost( hero, 20f ) );
	}

	private static boolean spawnAnimal( Hero hero, com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m, float cost ) {
		if (!spendEnergy( hero, cost )) {
			return false;
		}
		enforceAnimalLimit( hero, m.getClass() );
		if (!place( m, hero )) {
			gainEnergy( hero, cost );
			return false;
		}
		return true;
	}

	private static void enforceAnimalLimit( Hero hero, Class<?> newType ) {
		int allowed = hero.pointsInTalent( Talent.ANIMAL_COMPANION ) >= 3 ? 3
				: hero.pointsInTalent( Talent.ANIMAL_COMPANION ) >= 2 ? 2 : 1;
		java.util.ArrayList<com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob> animals = new java.util.ArrayList<>();
		for (com.shatteredpixel.shatteredpixeldungeon.actors.Char ch : com.shatteredpixel.shatteredpixeldungeon.actors.Actor.chars()) {
			if (ch instanceof DruidBoar || ch instanceof DruidWolf || ch instanceof DruidBear) {
				animals.add( (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) ch );
			}
		}
		// 重新召唤同类时替换旧的同类召唤物
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob a : animals) {
			if (a.getClass() == newType) {
				a.die( null );
			}
		}
		// 统计其它种类，若达到上限则清空
		java.util.HashSet<Class<?>> types = new java.util.HashSet<>();
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob a : animals) {
			if (a.getClass() != newType) {
				types.add( a.getClass() );
			}
		}
		if (types.size() >= allowed) {
			for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob a : animals) {
				if (a.getClass() != newType) {
					a.die( null );
				}
			}
		}
	}

	private static void enforcePlantLimit() {
		for (com.shatteredpixel.shatteredpixeldungeon.actors.Char ch : com.shatteredpixel.shatteredpixeldungeon.actors.Actor.chars()) {
			if (ch instanceof DruidOakSage || ch instanceof DruidVines || ch instanceof DruidSunflower) {
				((com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) ch).die( null );
			}
		}
	}

	private static boolean place( com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m, Hero hero ) {
		int cell = -1;
		for (int n : com.watabou.utils.PathFinder.NEIGHBOURS8) {
			int c = hero.pos + n;
			if (c >= 0 && c < Dungeon.level.length()
					&& Dungeon.level.passable[c] && !Dungeon.level.solid[c]
					&& com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar( c ) == null) {
				cell = c;
				break;
			}
		}
		if (cell == -1) {
			return false;
		}
		m.pos = cell;
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add( m );
		com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( cell ).burst(
				com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory( com.shatteredpixel.shatteredpixeldungeon.effects.Speck.STAR ), 10 );
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
		int pts = hero.pointsInTalent( Talent.NATURE_BOND );
		float hpMul = pts >= 1 ? 1.5f : 1f;
		vines.HP = vines.HT = Math.max( 1, Math.round( (20 + 4 * vines.grassTiles) * hpMul ) );
		return spawnPlant( hero, vines, 10f );
	}

	public static boolean summonSunflower( Hero hero ) {
		DruidSunflower flower = new DruidSunflower();
		flower.grassTiles = countGrass( hero.pos, 2 );
		int pts = hero.pointsInTalent( Talent.NATURE_BOND );
		float hpMul = pts >= 2 ? 1.5f : 1f;
		flower.HP = flower.HT = Math.round( (100 + Dungeon.depth * 2) * hpMul );
		flower.power = pts >= 2 ? 1.5f : 1f;
		return spawnPlant( hero, flower, 30f );
	}

	private static boolean spawnPlant( Hero hero, com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m, float cost ) {
		if (!spendEnergy( hero, cost )) {
			return false;
		}
		enforcePlantLimit();
		if (!place( m, hero )) {
			gainEnergy( hero, cost );
			return false;
		}
		return true;
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
		for (int pos : lineCells( hero.pos, cell )) {
			if (Dungeon.level.heroFOV == null || pos >= Dungeon.level.heroFOV.length) {
				continue;
			}
			Char ch = com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar( pos );
			if (ch != null && ch.alignment == Char.Alignment.ENEMY && ch.isAlive()) {
				ch.damage( new com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo( dmg, com.shatteredpixel.shatteredpixeldungeon.damage.DamageType.MAGICAL, hero, null, hero ) );
				Buff.affect( ch, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class );
				if (hero.pointsInTalent( Talent.ELEMENTAL_MASTERY ) >= 1) {
					knockBack( ch, hero.pos );
				}
			}
			int t = Dungeon.level.map[pos];
			if (t == Terrain.GRASS || t == Terrain.HIGH_GRASS) {
				Dungeon.level.set( pos, Terrain.EMBERS );
			}
			com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( pos ).burst(
					com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory( com.shatteredpixel.shatteredpixeldungeon.effects.Speck.INFERNO ), 4 );
		}
		com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter.get( cell ).burst(
				com.shatteredpixel.shatteredpixeldungeon.effects.Speck.factory( com.shatteredpixel.shatteredpixeldungeon.effects.Speck.INFERNO ), 12 );
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
				Buff.affect( mob, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class );
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
		return true;
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

	private static void knockBack( Char ch, int from ) {
		int best = ch.pos;
		for (int n : com.watabou.utils.PathFinder.NEIGHBOURS8) {
			int c = ch.pos + n;
			if (c >= 0 && c < Dungeon.level.length()
					&& Dungeon.level.passable[c] && !Dungeon.level.solid[c]
					&& com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar( c ) == null
					&& Dungeon.level.distance( c, from ) > Dungeon.level.distance( best, from )) {
				best = c;
			}
		}
		if (best != ch.pos) {
			ch.pos = best;
			ch.sprite.place( best );
		}
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

		@Override
		public boolean act() {
			if (cell < 0 || turns <= 0 || Dungeon.level == null) {
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

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( CELL, cell );
			bundle.put( TURNS, turns );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			cell = bundle.getInt( CELL );
			turns = bundle.getInt( TURNS );
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

		@Override
		public boolean act() {
			if (cell < 0 || turns <= 0 || Dungeon.level == null) {
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
						Buff.affect( mob, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class );
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

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( CELL, cell );
			bundle.put( TURNS, turns );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			cell = bundle.getInt( CELL );
			turns = bundle.getInt( TURNS );
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

	public static class DruidEnergy extends Buff
			implements com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator.Action {

		{
			type = buffType.POSITIVE;
			announced = false;
		}

		public float energy = 0f;

		public float max() {
			Hero hero = Dungeon.hero;
			return ((hero == null ? 1 : hero.lvl) + Dungeon.depth) * 10f;
		}

		public void gain( float amount ) {
			energy = Math.min( max(), energy + amount );
			com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator.setAction( this );
		}

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo( target )) {
				com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator.setAction( this );
				return true;
			}
			return false;
		}

		public boolean useEnergy( float amount ) {
			if (energy < amount) {
				return false;
			}
			energy -= amount;
			com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator.refresh();
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
					new com.shatteredpixel.shatteredpixeldungeon.windows.WndDruidSkills( this ) );
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
			com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator.setAction( this );
		}
	}
}
