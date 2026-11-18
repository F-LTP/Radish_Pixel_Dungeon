/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/**
 * 次元撕裂者的裂隙粒子。
 *
 * 六种颜色取自武器图标那一格（去掉描边与棕色刀柄后恰好六色），按发射序号循环取色，
 * 不做插值——插值会混出贴图之外的中间色。三种发射形态都用叠加混合。
 */
public class RiftParticle extends PixelParticle {

	/** 白 → 淡紫 → 亮兰紫 → 中紫 → 深紫 → 靛紫。 */
	private static final int[] COLORS = {
			0xFFFFFF, 0xF1B8FC, 0xD887F0, 0xAE52DD, 0x6A42BA, 0x4638A8
	};

	private static final float LIFESPAN = 0.5f;

	private static Emitter.Factory factory( final int mode ){
		return new Emitter.Factory() {
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((RiftParticle) emitter.recycle( RiftParticle.class )).reset( x, y, index, mode );
			}
			@Override
			public boolean lightMode() { return true; }
		};
	}

	/** 命中：贴着目标向四周炸开一小蓬，射程近。 */
	public static final Emitter.Factory HIT = factory( 0 );
	/** 传送起点：向外炸散、更远更快。 */
	public static final Emitter.Factory SCATTER = factory( 1 );
	/** 传送终点：从四周围拢回格心，寿终那一帧正好抵达。 */
	public static final Emitter.Factory IMPLODE = factory( 2 );

	private int colorBase;
	private int mode;

	public RiftParticle() {
		super();
		lifespan = LIFESPAN;
	}

	private void reset( float x, float y, int index, int mode ) {
		revive();
		this.colorBase = index % COLORS.length;
		this.mode = mode;
		left = lifespan;

		if (mode == 0){
			this.x = x; this.y = y;
			speed.set( Random.Float( -8, 8 ), Random.Float( -8, 8 ) );
		} else if (mode == 1){
			this.x = x; this.y = y;
			speed.polar( Random.Float( PointF.PI2 ), Random.Float( 20, 36 ) );
		} else {
			float angle = Random.Float( PointF.PI2 );
			float dist = Random.Float( 10, 18 );
			this.x = x + (float)Math.cos( angle ) * dist;
			this.y = y + (float)Math.sin( angle ) * dist;
			// 速度 = 位移 ÷ 存活期，粒子正好在寿终那帧抵达落点。
			speed.set( x - this.x, y - this.y );
			speed.scale( 1f / lifespan );
		}
	}

	@Override
	public void update() {
		super.update();
		float am = left / lifespan;
		size( mode == 0 ? 3 - am * 2 : 5 - am * 4 );
		// 不插值：后半程整档下沉一色，读作「冷却」。
		int idx = Math.min( COLORS.length - 1, colorBase + (am < 0.5f ? 1 : 0) );
		color( COLORS[idx] );
	}
}
