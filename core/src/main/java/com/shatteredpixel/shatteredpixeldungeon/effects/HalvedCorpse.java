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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;

//将一个角色的精灵沿水平切口分成两半：
//下半段留在原位，上半段以指定初速度做抛物线运动，落回切口同等高度后静止，随后整体淡出。
public class HalvedCorpse extends Group {

	private static final float GRAVITY = 360f;      //px/s^2
	private static final float LINGER_TIME = 0.7f;  //上半段落地后的停留时间
	private static final float FADE_TIME = 1.2f;    //淡出时间

	private final Image upper;
	private final Image lower;

	//angleDeg：初速度方向，0 度为正右方，逆时针为正（90 度为正上），单位像素/秒
	public static DeathAnimation animation( float angleDeg, float speed ) {
		return animation( angleDeg, speed, 0.5f );
	}

	public static DeathAnimation animation( float angleDeg, float speed, float cutFrac ) {
		return ( sprite, onFinished ) -> {
			spawn( sprite, angleDeg, speed, cutFrac );
			if (onFinished != null) {
				onFinished.run();
			}
		};
	}

	public static void bisect( Char ch, float angleDeg, float speed ) {
		bisect( ch, angleDeg, speed, 0.5f );
	}

	public static void bisect( Char ch, float angleDeg, float speed, float cutFrac ) {
		if (ch != null && ch.sprite != null && ch.sprite.parent != null) {
			spawn( ch.sprite, angleDeg, speed, cutFrac );
		}
	}

	private static void spawn( CharSprite sprite, float angleDeg, float speed, float cutFrac ) {
		if (sprite == null || sprite.parent == null) {
			return;
		}

		//必须先取父级：killAndErase() 会把 parent 置空
		Group parent = sprite.parent;

		RectF srcFrame = sprite.frame();
		float sw = sprite.width;
		float sh = sprite.height;

		cutFrac = Math.max( 0.1f, Math.min( 0.9f, cutFrac ) );
		float cutY = sprite.y + sh * cutFrac;

		sprite.killAndErase();

		HalvedCorpse corpse = new HalvedCorpse( sprite, srcFrame, cutFrac );
		parent.add( corpse );
		parent.sendToBack( corpse );

		bloodAtCut( parent, sprite, sprite.x, cutY, sw );

		float rad = (float) Math.toRadians( angleDeg );
		float vx0 = speed * (float) Math.cos( rad );
		//屏幕坐标系 y 向下，向上飞行的初速度为负
		float vy0 = -speed * (float) Math.sin( rad );

		//解析求解上半段从初始位置落回切口高度的飞行时长
		float dist = cutY - sprite.y;
		float disc = vy0 * vy0 + 2 * GRAVITY * dist;
		float flightTime = disc > 0
				? ( -vy0 + (float) Math.sqrt( disc ) ) / GRAVITY
				: 0.5f;
		flightTime = Math.max( 0.15f, flightTime );

		//飞行中绕切口中心逐渐旋转，落地时达到最大角（随机方向，约 30~60 度），落地后不回正
		float maxAngle = Random.Float( 30f, 60f );
		if (Random.Int( 2 ) == 0) {
			maxAngle = -maxAngle;
		}

		corpse.parent.add( new UpperFlight( corpse.upper, flightTime,
				sprite.x, sprite.y, vx0, vy0, cutY, maxAngle, corpse ) );
	}

	private HalvedCorpse( CharSprite src, RectF srcFrame, float cutFrac ) {
		upper = new Image();
		upper.texture = src.texture;
		upper.frame( new RectF( srcFrame.left, srcFrame.top,
				srcFrame.right, srcFrame.top + srcFrame.height() * cutFrac ) );
		upper.flipHorizontal = src.flipHorizontal;
		upper.x = src.x;
		upper.y = src.y;
		//旋转轴心：切口中点（上半段的底边中点），使其像从切口处倾倒
		upper.origin.set( upper.width / 2f, upper.height );

		lower = new Image();
		lower.texture = src.texture;
		lower.frame( new RectF( srcFrame.left, srcFrame.top + srcFrame.height() * cutFrac,
				srcFrame.right, srcFrame.bottom ) );
		lower.flipHorizontal = src.flipHorizontal;
		//下半段固定在切口下方，位置不变
		lower.x = src.x;
		lower.y = src.y + upper.height();

		add( lower );
		add( upper );
	}

	//切口喷血 + 沿切口线持续冒 1-2px 血色粒子
	private static void bloodAtCut( Group parent, CharSprite sprite, float x, float cutY, float width ) {
		int color = sprite.blood();

		PointF cutCenter = new PointF( x + width / 2f, cutY );
		Splash.at( cutCenter, color, 14 );

		Emitter emitter = new Emitter();
		emitter.pos( x, cutY - 1f, width, 2f );
		emitter.start( new CutBloodFactory( color ), 0.04f, (int) (width / 2) );
		parent.add( emitter );
	}

	private static class CutBloodFactory extends Emitter.Factory {

		private final int color;

		CutBloodFactory( int color ) {
			this.color = color;
		}

		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			PixelParticle p = (PixelParticle) emitter.recycle( PixelParticle.Shrinking.class );
			p.reset( x, y, color, Random.Float( 1f, 2f ), Random.Float( 0.5f, 0.9f ) );
			p.speed.set( Random.Float( -12, 12 ), Random.Float( -8, 8 ) );
			p.acc.set( 0, 100 );
		}
	}

	//上半段的抛物线运动：x 匀速，y 匀加速，落到切口高度（targetY）后静止；
	//飞行全程线性旋转，落地时恰好达到 maxAngle，落地后保持该角度不再变化
	private static class UpperFlight extends Tweener {

		private final Image upper;
		private final float startX;
		private final float startY;
		private final float vx;
		private final float vy;
		private final float targetY;
		private final float maxAngle;
		private final HalvedCorpse corpse;

		UpperFlight( Image upper, float duration, float startX, float startY,
		             float vx, float vy, float targetY, float maxAngle, HalvedCorpse corpse ) {
			super( upper, duration );
			this.upper = upper;
			this.startX = startX;
			this.startY = startY;
			this.vx = vx;
			this.vy = vy;
			this.targetY = targetY;
			this.maxAngle = maxAngle;
			this.corpse = corpse;
		}

		@Override
		protected void updateValues( float progress ) {
			float t = progress * interval;
			upper.x = startX + vx * t;
			upper.y = startY + vy * t + 0.5f * GRAVITY * t * t;
			upper.angle = maxAngle * progress;
		}

		@Override
		protected void onComplete() {
			super.onComplete();
			corpse.parent.add( new Linger( corpse, LINGER_TIME ) );
		}
	}

	//上半段落地后整体停留一段时间再淡出销毁
	private static class Linger extends Tweener {

		private final HalvedCorpse corpse;

		Linger( HalvedCorpse corpse, float duration ) {
			super( corpse, duration );
			this.corpse = corpse;
		}

		@Override
		protected void updateValues( float progress ) {
			//nothing to animate
		}

		@Override
		protected void onComplete() {
			super.onComplete();
			for (Image img : new Image[]{ corpse.upper, corpse.lower }) {
				corpse.parent.add( new AlphaTweener( img, 0, FADE_TIME ) {
					@Override
					protected void onComplete() {
						img.killAndErase();
					}
				} );
			}
			corpse.killAndErase();
		}
	}
}
