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

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.SnDBGM;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MoonLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Peddler;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PeddlerCatalog;
import com.shatteredpixel.shatteredpixeldungeon.effects.Ripple;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.SewerPainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.CombatRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.BlessScrollRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MossExitRoomInMain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SmallGrassEnterRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.AlarmTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ChillingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ConfusionTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlockTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GatewayTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.OozeTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ShockingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.SummoningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TeleportationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WornDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.SurfaceScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Callback;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SewerLevel extends RegularLevel {

	@Override
	public void create() {
		super.create();

		//主线第 1 层：放一扇通往测试支线的楼梯
		if (Dungeon.depth == 1 && Branches.MAIN.equals(Dungeon.branchId)
				&& testPortalCell < 0) {
			int chosen = -1;
			for (int tries = 0; tries < 200; tries++) {
				int c = com.watabou.utils.Random.Int(length());
				if (map[c] == Terrain.EMPTY && c != entrance() && c != exit()
						&& heaps.get(c) == null && traps.get(c) == null) {
					chosen = c;
					break;
				}
			}
			if (chosen >= 0) {
				map[chosen] = Terrain.EXIT;
				testPortalCell = chosen;
				com.shatteredpixel.shatteredpixeldungeon.levels.features.CycleStairVisual vis =
						new com.shatteredpixel.shatteredpixeldungeon.levels.features.CycleStairVisual(-1);
				vis.pos(chosen, this);
				customTiles.add(vis);
			}
		}
	}

	{
		color1 = 0x48763c;
		color2 = 0x59994a;
	}

	public static final String[] SEWER_TRACK_LIST
			= new String[]{Assets.Music.SEWERS_1, Assets.Music.SEWERS_2, Assets.Music.SEWERS_2,
			Assets.Music.SEWERS_1, Assets.Music.SEWERS_3, Assets.Music.SEWERS_3};
	public static final float[] SEWER_TRACK_CHANCES = new float[]{1f, 1f, 0.5f, 0.25f, 1f, 0.5f};

	public void playLevelMusic(){
        if (SnDBGM.playLevelMusic()) return;
		if (Ghost.Quest.active() || Statistics.amuletObtained){
			if (Statistics.amuletObtained && Dungeon.depth == 1){
				Music.INSTANCE.play(Assets.Music.THEME_FINALE, true);
			} else {
				Music.INSTANCE.play(Assets.Music.SEWERS_TENSE, true);
			}
		} else {
			Music.INSTANCE.playTracks(SEWER_TRACK_LIST, SEWER_TRACK_CHANCES, false);
		}
	}

	@Override
	protected ArrayList<Room> initRooms() {
		ArrayList<Room> initRooms = super.initRooms();
		if (Dungeon.depth == 1 && Dungeon.branchId.equals(Branches.MAIN)) {
			initRooms.add(new CombatRoom());
		}
		if(Dungeon.depth == 2){
			if(Dungeon.branchId.equals(Branches.MAIN)){
				initRooms.add(new SmallGrassEnterRoom());
			}
			if(Dungeon.branchId.equals(Branches.MOSS)){
				initRooms.add(new BlessScrollRoom());
			}
		}
		if (Dungeon.depth == 3 && Dungeon.branchId.equals(Branches.MAIN)) {
			initRooms.add(new MossExitRoomInMain());
		}
		return initRooms;
	}

	@Override
	protected int standardRooms(boolean forceMax) {
		if (forceMax) return 6;
		//4 to 6, average 5
		return 4+Random.chances(new float[]{1, 3, 1});
	}

	@Override
	protected int specialRooms(boolean forceMax) {
		if (forceMax) return 2;
		//1 to 2, average 1.8
		return 1+Random.chances(new float[]{1, 4});
	}
	
	@Override
	protected Painter painter() {
		return new SewerPainter()
				.setWater(feeling == Feeling.WATER ? 0.85f : 0.30f, 5)
				.setGrass(feeling == Feeling.GRASS ? 0.80f : 0.20f, 4)
				.setTraps(nTraps(), trapClasses(), trapChances());
	}
	
	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_SEWERS;
	}
	
	@Override
	public String waterTex() {
		return Assets.Environment.WATER_SEWERS;
	}
	
	@Override
	protected Class<?>[] trapClasses() {
		return Dungeon.depth == 1 ?
				new Class<?>[]{ WornDartTrap.class } :
				new Class<?>[]{
						ChillingTrap.class, ShockingTrap.class, ToxicTrap.class, WornDartTrap.class,
						AlarmTrap.class, OozeTrap.class,
						ConfusionTrap.class, FlockTrap.class, SummoningTrap.class, TeleportationTrap.class, GatewayTrap.class };
}

	@Override
	protected float[] trapChances() {
		return Dungeon.depth == 1 ?
				new float[]{1} :
				new float[]{
						4, 4, 4, 4,
						2, 2,
						1, 1, 1, 1, 1};
	}

	@Override
	protected void createMobs() {
		if(Dungeon.branchId.equals(Branches.MAIN)){
			Ghost.Quest.spawn( this, roomExit );
		}
		if(Dungeon.depth == 1 && Dungeon.branchId.equals(Branches.MAIN)){
			MoonLight npc18 = new MoonLight();
			npc18.pos = moonLightCell();
			mobs.add(npc18);
		}

		//一楼游商（仿暗黑地牢 BarterMan）：名字/台词/货单全在 PeddlerCatalog 里
		if(Dungeon.depth == 1 && Dungeon.branchId.equals(Branches.MAIN)){
			spawnPeddlers();
		}

		super.createMobs();
	}

	/**
	 * 月华站的格子：入口左边一格。那一格上已经有物品时换到入口旁边别的空格：
	 * 给新玩家的冒险者指南是随机丢在入口房间里的，可能正好落在这一格，而月华不可移动，被她压住的东西永远捡不到。
	 * 新手引导期间入口房间的门是隐藏的，捡起并读完指南才显形（引导期间也搜不出来），
	 * 所以指南被压住的那一局英雄出不了入口房间。
	 */
	private int moonLightCell() {
		int preferred = entrance() - 1;
		if (heaps.get( preferred ) == null && findMob( preferred ) == null) {
			return preferred;
		}
		//先上下左右，再斜向；不用随机数，免得改变这一层其它内容的生成
		int[][] order = { com.watabou.utils.PathFinder.NEIGHBOURS4, com.watabou.utils.PathFinder.NEIGHBOURS8 };
		for (int[] dirs : order) {
			for (int dir : dirs) {
				int cell = entrance() + dir;
				if (passable[cell] && !pit[cell] && heaps.get( cell ) == null && findMob( cell ) == null
						&& traps.get( cell ) == null && plants.get( cell ) == null) {
					return cell;
				}
			}
		}
		return preferred;
	}

	/** 游商之间至少隔开的格数（按真实地图距离，不是格子编号差）。 */
	private static final int PEDDLER_SPACING = 5;

	/**
	 * 把 7 位游商分散撒到一楼各处。
	 *
	 * 做法：先收集全图所有可以站人的空格（排除不可站立、坑洞、已有物品、已有单位、以及紧贴入口的格子），
	 * 打乱顺序后按 {@link #PEDDLER_SPACING} 格的最小真实距离贪心抽取；
	 * 一次没抽够就把最小距离放宽一格重来，直到放下足够人数。
	 * 这样他们散布在整个楼层，而不是在入口排成一队；实在放不下就少放几位，绝不覆盖已有单位或物品。
	 */
	private void spawnPeddlers() {
		int total = length();
		int start = entrance();
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int cell = 0; cell < total; cell++) {
			if (!passable[cell] || pit[cell]) {
				continue;
			}
			if (heaps.get(cell) != null) {
				continue;
			}
			//「已有单位」不在这里排除，放到下面抽取的时候查（见那里的说明）
			// 别刷在陷阱或植物上：游商是常驻 NPC，压住陷阱格会让人看不清那里有东西
			if (traps.get(cell) != null || plants.get(cell) != null) {
				continue;
			}
			// 别堵在入口格上
			if (cell == start) {
				continue;
			}
			// 只放在房间内部：room(cell) 为 null ⇒ 这格在走廊、门洞或通道上。
			// Room.inside() 已经把紧贴墙的那一圈排除了，所以不会贴着门站。
			if (room(cell) == null) {
				continue;
			}
			// 还要求上下左右四格都能走：避免站在一格宽的窄道/门口，被玩家堵住过不去。
			boolean open = true;
			for (int dir : com.watabou.utils.PathFinder.NEIGHBOURS4) {
				int n = cell + dir;
				if (n < 0 || n >= total || !passable[n] || pit[n]) {
					open = false;
					break;
				}
			}
			if (!open) {
				continue;
			}
			candidates.add(cell);
		}
		if (candidates.isEmpty()) {
			return;
		}

		ArrayList<Integer> chosen = new ArrayList<>();
		// 从宽到窄逐步放宽间距，保证尽可能放下全部游商
		for (int spacing = PEDDLER_SPACING; spacing >= 2 && chosen.size() < PeddlerCatalog.COUNT; spacing--) {
			chosen.clear();
			ArrayList<Integer> pool = new ArrayList<>(candidates);
			// 用 SPD 自带随机源做 Fisher-Yates 洗牌，保证每次开局的散布位置都不同
			for (int i = pool.size() - 1; i > 0; i--) {
				int j = com.watabou.utils.Random.Int(i + 1);
				int tmp = pool.get(i);
				pool.set(i, pool.get(j));
				pool.set(j, tmp);
			}
			for (int cell : pool) {
				if (chosen.size() >= PeddlerCatalog.COUNT) {
					break;
				}
				//已经站着别的单位（月华、房间里的食人鱼、水晶宝箱怪……）的格子不放。
				//原来在收集候选格时用 Actor.findChar 查，但建关时角色表是空的，那个判断永远不成立，
				//游商会和别的单位叠在同一格。要查这一层自己的怪物表；
				//并且放在这里查而不是从候选格里剔掉：洗牌用掉的随机数个数不变，没有撞车的种子生成结果和以前一样。
				if (findMob(cell) != null) {
					continue;
				}
				boolean ok = true;
				for (int other : chosen) {
					if (distance(cell, other) < spacing) {
						ok = false;
						break;
					}
				}
				if (ok) {
					chosen.add(cell);
				}
			}
		}

		for (int i = 0; i < chosen.size() && i < PeddlerCatalog.COUNT; i++) {
			Peddler p = new Peddler(i);
			p.pos = chosen.get(i);
			mobs.add(p);
		}
	}

	
	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		if (transition.type == LevelTransition.Type.SURFACE){
			if (hero.belongings.getItem( Amulet.class ) == null) {
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndMessage( Messages.get(hero, "leave") ) );
					}
				});
				return false;
			} else {
				Statistics.ascended = true;
				Game.switchScene( SurfaceScene.class );
				return true;
			}
		} else {
			return super.activateTransition(hero, transition);
		}
	}

	@Override
	public Group addVisuals() {
		super.addVisuals();
		addSewerVisuals(this, visuals);
		return visuals;
	}
	
	public static void addSewerVisuals( Level level, Group group ) {
		for (int i=0; i < level.length(); i++) {
			if (level.map[i] == Terrain.WALL_DECO) {
				group.add( new Sink( i ) );
			}
		}
	}
	
	@Override
	public String tileName( int tile ) {
		switch (tile) {
			case Terrain.WATER:
				return Messages.get(SewerLevel.class, "water_name");
			default:
				return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
			case Terrain.EMPTY_DECO:
				return Messages.get(SewerLevel.class, "empty_deco_desc");
			case Terrain.BOOKSHELF:
				return Messages.get(SewerLevel.class, "bookshelf_desc");
			default:
				return super.tileDesc( tile );
		}
	}
	
	private static class Sink extends Emitter {
		
		private int pos;
		private float rippleDelay = 0;
		
		private static final Emitter.Factory factory = new Factory() {
			
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				WaterParticle p = (WaterParticle)emitter.recycle( WaterParticle.class );
				p.reset( x, y );
			}
		};
		
		public Sink( int pos ) {
			super();
			
			this.pos = pos;
			
			PointF p = DungeonTilemap.tileCenterToWorld( pos );
			pos( p.x - 2, p.y + 3, 4, 0 );
			
			pour( factory, 0.1f );
		}
		
		@Override
		public void update() {
			if (visible = (pos < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[pos])) {
				
				super.update();
				
				if (!isFrozen() && (rippleDelay -= Game.elapsed) <= 0) {
					Ripple ripple = GameScene.ripple( pos + Dungeon.level.width() );
					if (ripple != null) {
						ripple.y -= DungeonTilemap.SIZE / 2;
						rippleDelay = Random.Float(0.4f, 0.6f);
					}
				}
			}
		}
	}
	
	public static final class WaterParticle extends PixelParticle {
		
		public WaterParticle() {
			super();
			
			acc.y = 50;
			am = 0.5f;
			
			color( ColorMath.random( 0xb6ccc2, 0x3b6653 ) );
			size( 2 );
		}
		
		public void reset( float x, float y ) {
			revive();
			
			this.x = x;
			this.y = y;
			
			speed.set( Random.Float( -2, +2 ), 0 );
			
			left = lifespan = 0.4f;
		}
	}
}
