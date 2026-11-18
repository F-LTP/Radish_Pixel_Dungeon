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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.HalfFood;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.CrackedSpyglass;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.gltextures.AtlasFrame;
import com.watabou.gltextures.RuntimeAtlas;
import com.watabou.gltextures.RuntimeAtlasRegistry;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Matrix;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;
import com.shatteredpixel.shatteredpixeldungeon.ui.UITheme;

import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;

public class ItemSprite extends MovieClip {

	private static final int MIN_VISIBLE_ALPHA = 16;

	private static final float DROP_INTERVAL = 0.4f;

	public Heap heap;
	private Item item;
	private static final RuntimeAtlas ATLAS = RuntimeAtlasRegistry.get( ItemSpriteSheet.ATLAS );

	// 声明式动画缓存：资产+片段名定位；图集重建（贴图身份变化）后自动失效重建
	private static final Map<String, CachedAnim> animsCache = new HashMap<>();
	private static class CachedAnim {
		final MovieClip.Animation anim;
		final SmartTexture texture;
		CachedAnim( MovieClip.Animation anim, SmartTexture texture ){
			this.anim = anim;
			this.texture = texture;
		}
	}

	private Glowing glowing;
	private boolean invertDarkSprite;
	//FIXME: a lot of this emitter functionality isn't very well implemented.
	//right now I want to ship 0.3.0, but should refactor in the future.
	protected Emitter emitter;
	private float phase;
	private boolean glowUp;

	private float dropInterval;

	//the amount the sprite is raised from flat when viewed in a raised perspective
	protected float perspectiveRaise    = 5 / 16f; //5 pixels

	//the width and height of the shadow are a percentage of sprite size
	//offset is the number of pixels the shadow is moved down or up (handy for some animations)
	protected boolean renderShadow  = false;
	protected float shadowWidth     = 1f;
	protected float shadowHeight    = 0.25f;
	protected float shadowOffset    = 0.5f;

	public ItemSprite() {
		this( ItemSpriteSheet.SOMETHING, null );
	}

	public ItemSprite( Heap heap ){
		this();
		view( heap );
	}

	public ItemSprite( Item item ) {
		this();
		view( item );
	}

	public ItemSprite( String image ){
		this( image, null );
	}

	public ItemSprite( String image, Glowing glowing ) {
		super();
		view(image, glowing);
	}

	public void link() {
		link(heap);
	}

	public void link( Heap heap ) {
		this.heap = heap;
		view(heap);
		renderShadow = true;
		visible = heap.seen;
		place(heap.pos);
	}

	@Override
	public void revive() {
		super.revive();

		speed.set( 0 );
		acc.set( 0 );
		dropInterval = 0;

		heap = null;
		item = null;
		if (emitter != null) {
			emitter.killAndErase();
			emitter = null;
		}
	}

	@Override
	public void copy(Image other) {
		super.copy(other);

		ItemSprite src = other instanceof ItemSprite ? (ItemSprite) other : null;
		if (src != null && src.glowing != null){
			glow(src.glowing);
		}
		if (src != null) {
			invertDarkSprite = src.invertDarkSprite;
			applyDarkSpriteColor();
			//目录页等copy出的图标跟随源sprite的序列帧动画
			if (src.curAnim != null) play(src.curAnim);
			else if (curAnim != null) curAnim = null;
		}

	}

	public void visible(boolean value){
		this.visible = value;
		if (emitter != null && !visible){
			emitter.killAndErase();
			emitter = null;
		}
	}

	public PointF worldToCamera( int cell ) {
		final int csize = DungeonTilemap.SIZE;

		return new PointF(
				PixelScene.align(Camera.main, ((cell % Dungeon.level.width()) + 0.5f) * csize - width() * 0.5f),
				PixelScene.align(Camera.main, ((cell / Dungeon.level.width()) + 1.0f) * csize - height() - csize * perspectiveRaise)
		);
	}

	public void place( int p ) {
		if (Dungeon.level != null) {
			point(worldToCamera(p));
			shadowOffset = 0.5f;
		}
	}

	public void drop() {

		if (heap.isEmpty()) {
			return;
		} else if (heap.size() == 1){
			// normally this would happen for any heap, however this is not applied to heaps greater than 1 in size
			// in order to preserve an amusing visual bug/feature that used to trigger for heaps with size > 1
			// where as long as the player continually taps, the heap sails up into the air.
			place(heap.pos);
		}

		dropInterval = DROP_INTERVAL;

		speed.set( 0, -100 );
		acc.set(0, -speed.y / DROP_INTERVAL * 2);

		if (heap != null && heap.seen && heap.peek() instanceof Gold) {
			CellEmitter.center( heap.pos ).burst( Speck.factory( Speck.COIN ), 5 );
			Sample.INSTANCE.play( Assets.Sounds.GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );
		}
	}

	public void drop( int from ) {

		if (heap.pos == from) {
			drop();
		} else {

			float px = x;
			float py = y;
			drop();

			place(from);

			speed.offset((px - x) / DROP_INTERVAL, (py - y) / DROP_INTERVAL);
		}
	}

	public ItemSprite view( Item item ){
		this.item = item;
		if (item.sndImageName != null) {
			com.watabou.gltextures.SmartTexture tex = com.shatteredpixel.shatteredpixeldungeon.ui.SNDItems.texture();
			com.watabou.utils.RectF sndFrame = com.shatteredpixel.shatteredpixeldungeon.ui.SNDItems.frame(item.sndImageName);
			if (tex != null && sndFrame != null) {
				if (this.emitter != null) this.emitter.killAndErase();
				emitter = null;
				this.texture = tex;
				frame(sndFrame);
				//SND贴图不经图集，清除可能残留的序列帧动画
				curAnim = null;
				glow(item.glowing());
				applyDarkSpriteColor();
				com.watabou.noosa.particles.Emitter emitter = item.emitter();
				if (emitter != null && parent != null) {
					emitter.pos(this);
					parent.add(emitter);
					this.emitter = emitter;
				}
				return this;
			}
		}
		ItemAnimations anims = item.animations();
		//image()被覆写时（如Snake Bite挑战换图）优先走静态贴图，避免动画泄漏真实物品
		if (anims != null && anims.has( ItemAnimations.IDLE )
				&& item.image().equals( item.image )){
			glow(item.glowing());
			playAnim( ItemAnimations.IDLE );
		} else {
			//静态贴图：清除遗留动画，防止旧帧覆盖
			view(item.image(), item.glowing());
		}
		if (item instanceof HalfFood) {
			AtlasFrame atlasFrame = ATLAS.trimmedFrame(item.image(), MIN_VISIBLE_ALPHA);
			float middle = (atlasFrame.uv.top + atlasFrame.uv.bottom) / 2f;
			texture = atlasFrame.texture;
			frame(new com.watabou.utils.RectF(
					atlasFrame.uv.left, middle, atlasFrame.uv.right, atlasFrame.uv.bottom));
		}
		Emitter emitter = item.emitter();
		if (emitter != null && parent != null) {
			emitter.pos( this );
			parent.add( emitter );
			this.emitter = emitter;
		}
		applyDarkSpriteColor();
		return this;
	}

	public ItemSprite view( Heap heap ){
		alpha( heap.hidden ? CrackedSpyglass.hiddenAlpha() : 1f);
		if (heap.size() <= 0 || heap.items == null){
			return view( ItemSpriteSheet.SOMETHING, null );
		}

		switch (heap.type) {
			case HEAP: case FOR_SALE:
				return view( heap.peek() );
			case CHEST:
				return view( ItemSpriteSheet.CHEST, null );
			case LOCKED_CHEST:
				return view( ItemSpriteSheet.LOCKED_CHEST, null );
			case CRYSTAL_CHEST:
				return view( ItemSpriteSheet.CRYSTAL_CHEST, null );
			case TOMB:
				return view( ItemSpriteSheet.TOMB, null );
			case SKELETON:
				return view( ItemSpriteSheet.BONES, null );
			case REMAINS:
				return view( ItemSpriteSheet.REMAINS, null );
			default:
				return view( ItemSpriteSheet.SOMETHING, null );
		}
	}

	public ItemSprite view( String image, Glowing glowing ) {
		if (this.emitter != null) this.emitter.killAndErase();
		emitter = null;
		frame( image );
		glow( glowing );
		applyDarkSpriteColor();
		return this;
	}

	/** Makes nearly-black, opaque item art visible on the Dice Mage dark panels. */
	private void applyDarkSpriteColor() {
		invertDarkSprite = false;
		if (UITheme.isDiceMage() && texture instanceof SmartTexture) {
			SmartTexture tex = (SmartTexture) texture;
			int left = Math.max(0, (int) (frame.left * tex.width));
			int right = Math.min(tex.width, (int) Math.ceil(frame.right * tex.width));
			int top = Math.max(0, (int) (frame.top * tex.height));
			int bottom = Math.min(tex.height, (int) Math.ceil(frame.bottom * tex.height));
			boolean hasOpaque = false;
			boolean dark = true;
			for (int py = top; py < bottom && dark; py++) {
				for (int px = left; px < right; px++) {
					int color = tex.getPixel(px, py);
					int alpha = (color >>> 24) & 0xFF;
					if (alpha < MIN_VISIBLE_ALPHA) continue;
					hasOpaque = true;
					int red = (color >>> 16) & 0xFF;
					int green = (color >>> 8) & 0xFF;
					int blue = color & 0xFF;
					if (red > 24 || green > 24 || blue > 24) {
						dark = false;
						break;
					}
				}
			}
			invertDarkSprite = hasOpaque && dark;
		}
		if (invertDarkSprite && glowing == null) {
			rm = gm = bm = -1f;
			ra = ga = ba = 1f;
		}
	}

	public void frame( String image ){
		//静态贴图：清除遗留动画，防止旧帧覆盖
		if (curAnim != null) curAnim = null;

		AtlasFrame atlasFrame = ATLAS.trimmedFrame( image, MIN_VISIBLE_ALPHA );
		texture = atlasFrame.texture;
		frame( atlasFrame.uv );

		float height = atlasFrame.height;
		//adds extra raise to very short items, so they are visible
		if (height < 8f){
			perspectiveRaise =  (5 + 8 - height) / 16f;
		}
	}

	/** 按名播放当前物品声明的动画；无声明/声明非法则忽略。重复触发的循环动画不会从头重播 */
	public void playAnim( String name ){
		ItemAnimations anims = item == null ? null : item.animations();
		ItemAnimations.Clip clip = anims == null ? null : anims.get( name );
		if (clip == null) return;

		AtlasFrame whole = ATLAS.frame( anims.asset() );
		MovieClip.Animation anim = buildAnim( whole, anims, clip );
		if (anim == null){
			//声明失效（资产缺失/帧序越界）：退回静态贴图
			view(item.image(), glowing);
			return;
		}
		if (curAnim == anim) return;

		texture = whole.texture;
		play( anim );
	}

	/** 从资产整帧内按网格坐标切出片段帧；帧序越界或网格不符返回null */
	private MovieClip.Animation buildAnim( AtlasFrame whole, ItemAnimations anims, ItemAnimations.Clip clip ){
		String cacheKey = anims.asset() + ":" + clip.name;
		CachedAnim cached = animsCache.get( cacheKey );
		if (cached != null && cached.texture == whole.texture) return cached.anim;

		int cell = anims.cell();
		if (cell <= 0 || whole.width % cell != 0 || whole.height % cell != 0) return null;

		int cols = whole.width / cell;
		RectF[] frames = new RectF[clip.count];
		int left = Math.round( whole.uv.left * whole.texture.width );
		int top = Math.round( whole.uv.top * whole.texture.height );
		for (int i = 0; i < clip.count; i++){
			int idx = clip.start + i;
			int cy = idx / cols;
			if (cy * cell >= whole.height) return null; //帧序号越界：声明与贴图不符
			int cx = idx % cols;
			frames[i] = whole.texture.uvRect(
					left + cx * cell, top + cy * cell,
					left + (cx + 1) * cell, top + (cy + 1) * cell );
		}

		MovieClip.Animation anim = new MovieClip.Animation( clip.fps, clip.looped ).frames( frames ).named( clip.name );
		animsCache.put( cacheKey, new CachedAnim( anim, whole.texture ) );
		return anim;
	}

	public synchronized void glow( Glowing glowing ){
		this.glowing = glowing;
		if (glowing == null) resetColor();
	}

	@Override
	public void kill() {
		super.kill();
		if (emitter != null) {
			emitter.on = false;
			emitter.autoKill = true;
		}
		emitter = null;
	}

	private float[] shadowMatrix = new float[16];

	@Override
	protected void updateMatrix() {
		super.updateMatrix();
		Matrix.copy(matrix, shadowMatrix);
		Matrix.translate(shadowMatrix,
				(width() * (1f - shadowWidth)) / 2f,
				(height() * (1f - shadowHeight)) + shadowOffset);
		Matrix.scale(shadowMatrix, shadowWidth, shadowHeight);
	}

	@Override
	public void draw() {
		if (texture == null || (!dirty && buffer == null))
			return;

		if (renderShadow) {
			if (dirty) {
				((Buffer)verticesBuffer).position(0);
				verticesBuffer.put(vertices);
				if (buffer == null)
					buffer = new Vertexbuffer(verticesBuffer);
				else
					buffer.updateVertices(verticesBuffer);
				dirty = false;
			}

			NoosaScript script = script();

			texture.bind();

			script.camera(camera());

			updateMatrix();

			script.uModel.valueM4(shadowMatrix);
			script.lighting(
					0, 0, 0, am * .6f,
					0, 0, 0, aa * .6f);

			script.drawQuad(buffer);
		}

		super.draw();

	}

	@Override
	public synchronized void update() {
		super.update();

		//一次性动画播完自动回到 idle（若有声明）
		if (finished && curAnim != null && !curAnim.looped && item != null){
			ItemAnimations anims = item.animations();
			if (anims != null
					&& anims.has( ItemAnimations.IDLE )
					&& !ItemAnimations.IDLE.equals( curAnim.name )){
				playAnim( ItemAnimations.IDLE );
			}
		}

		visible = (heap == null || heap.seen);

		if (emitter != null){
			emitter.visible = visible;
		}

		if (dropInterval > 0){
			shadowOffset -= speed.y * Game.elapsed * 0.8f;

			if ((dropInterval -= Game.elapsed) <= 0){

				speed.set(0);
				acc.set(0);
				shadowOffset = 0.25f;
				place(heap.pos);

				if (visible) {

					if (Dungeon.level.water[heap.pos]) {
						GameScene.ripple(heap.pos);
					}

					if (Dungeon.level.water[heap.pos]) {
						Sample.INSTANCE.play( Assets.Sounds.WATER, 0.8f, Random.Float( 1f, 1.45f ) );
					} else if (Dungeon.level.map[heap.pos] == Terrain.EMPTY_SP) {
						Sample.INSTANCE.play( Assets.Sounds.STURDY, 0.8f, Random.Float( 1.16f, 1.25f ) );
					} else if (Dungeon.level.map[heap.pos] == Terrain.GRASS
							|| Dungeon.level.map[heap.pos] == Terrain.EMBERS
							|| Dungeon.level.map[heap.pos] == Terrain.FURROWED_GRASS){
						Sample.INSTANCE.play( Assets.Sounds.GRASS, 0.8f, Random.Float( 1.16f, 1.25f ) );
					} else if (Dungeon.level.map[heap.pos] == Terrain.HIGH_GRASS) {
						Sample.INSTANCE.play( Assets.Sounds.STEP, 0.8f, Random.Float( 1.16f, 1.25f ) );
					} else {
						Sample.INSTANCE.play( Assets.Sounds.STEP, 0.8f, Random.Float( 1.16f, 1.25f ));
					}
				}
			}
		}

		if (visible && glowing != null) {
			if (glowUp && (phase += Game.elapsed) > glowing.period) {

				glowUp = false;
				phase = glowing.period;

			} else if (!glowUp && (phase -= Game.elapsed) < 0) {

				glowUp = true;
				phase = 0;

			}

			float value = phase / glowing.period * 0.6f;

			float base = 1 - value;
			if (invertDarkSprite) {
				rm = gm = bm = -base;
				ra = base + glowing.red * value;
				ga = base + glowing.green * value;
				ba = base + glowing.blue * value;
			} else {
				rm = gm = bm = base;
				ra = glowing.red * value;
				ga = glowing.green * value;
				ba = glowing.blue * value;
			}
		}
	}

	public static int pick( String name, int x, int y ) {
		return ATLAS.pixel( name, x, y );
	}

	public static AtlasFrame atlasFrame( String name ) {
		return ATLAS.trimmedFrame( name, MIN_VISIBLE_ALPHA );
	}

	public static class Glowing {

		public int color;
		public float red;
		public float green;
		public float blue;
		public float period;

		public Glowing( int color ) {
			this( color, 1f );
		}

		public Glowing( int color, float period ) {

			this.color = color;

			red = (color >> 16) / 255f;
			green = ((color >> 8) & 0xFF) / 255f;
			blue = (color & 0xFF) / 255f;

			this.period = period;
		}
	}
}
