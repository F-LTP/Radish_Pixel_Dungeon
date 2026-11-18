package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PointF;

public class DMPrototypeSprite extends MobSprite {

	private int model = 100;
	private boolean activated = false;

	public DMPrototypeSprite() {
		super();
		setup( 100, false );
	}

	//查看窗口、格子内容窗口、攻击指示器用的都是没有 link() 的精灵，机型和是否激活只能从这里拿到。
	//link() 也会先走到这里，所以摆位时用的已经是最终的帧尺寸。
	@Override
	public void linkVisuals( Char ch ) {
		super.linkVisuals( ch );
		if (ch instanceof WardenPrototype) {
			WardenPrototype p = (WardenPrototype) ch;
			if (p.model != model || p.isActivated() != activated) {
				setup( p.model, p.isActivated() );
			}
		}
	}

	/** 激活后换成对应型号怪物的原始贴图与动画 */
	public void activate() {
		setup( model, true );
		//帧尺寸变了，不重新摆位会相对格子偏出去；移动途中的落点由补间负责，不去打断
		if (ch != null && !isMoving) {
			place( ch.pos );
		}
	}

	//每次都把整套动画一起重建，任何一条都不能留空帧：
	//TextureFilm 对贴图里不存在的帧号返回 null，播到空帧会直接空指针。
	private synchronized void setup( int model, boolean activated ) {
		this.model = model;
		this.activated = activated;

		Object tx;
		if (!activated) {
			tx = Assets.Sprites.DM_PROTO;
			TextureFilm frames = new TextureFilm( tx, 16, 16 );
			//停机贴图整张只有两帧：0 是 DM-175，1 是 DM-100
			int frame = model == 175 ? 0 : 1;

			idle = new Animation( 1, true );
			idle.frames( frames, frame );

			run = idle.clone();

			attack = new Animation( 12, false );
			attack.frames( frames, frame );

			zap = attack.clone();

			die = new Animation( 20, false );
			die.frames( frames, frame );

		} else if (model == 175) {
			tx = Assets.Sprites.DM175;
			TextureFilm frames = new TextureFilm( tx, 18, 15 );

			idle = new Animation( 7, true );
			idle.frames( frames, 0, 1, 2, 3 );

			run = new Animation( 9, true );
			run.frames( frames, 4, 5, 6, 7 );

			attack = new Animation( 11, false );
			attack.frames( frames, 8, 9, 10, 11 );

			zap = attack.clone();

			die = new Animation( 9, false );
			die.frames( frames, 12, 13, 14, 15, 16, 17, 18, 19 );

		} else {
			tx = Assets.Sprites.DM100;
			//和 DM100Sprite 一样按 16x14 切：贴图最下面两行是空的，按 16 高切会让机体悬空两像素
			TextureFilm frames = new TextureFilm( tx, 16, 14 );

			idle = new Animation( 1, true );
			idle.frames( frames, 0, 1 );

			run = new Animation( 12, true );
			run.frames( frames, 6, 7, 8, 9 );

			attack = new Animation( 12, false );
			attack.frames( frames, 2, 3, 4, 0 );

			zap = new Animation( 8, false );
			zap.frames( frames, 5, 5, 1 );

			die = new Animation( 12, false );
			die.frames( frames, 10, 11, 12, 13, 14, 15 );
		}

		//换贴图会先把当前帧重置成整张图，紧接着播放才切回单帧：
		//激活发生在逻辑线程，这两步之间隔得越久，渲染线程越可能画出一整条贴图
		texture( tx );
		play( idle );
	}

	public void zap( int pos ) {
		Char enemy = Actor.findChar(pos);
		//shoot lightning from eye, not sprite center.
		PointF origin = center();
		if (flipHorizontal){
			origin.y -= 6*scale.y;
			origin.x -= 1*scale.x;
		} else {
			origin.y -= 8*scale.y;
			origin.x += 1*scale.x;
		}
		if (enemy != null) {
			parent.add(new Lightning(origin, enemy.sprite.destinationCenter(), (WardenPrototype) ch));
		} else {
			parent.add(new Lightning(origin, pos, (WardenPrototype) ch));
		}
		Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );

		super.zap( ch.pos );
		flash();
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
		}
		super.onComplete( anim );
	}

	@Override
	public int blood() {
		return 0xFFFFFF88;
	}
}
