package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 圆球皮肤（SPHERE）专属的朝向角状态。
 * <p>以持久化 Buff 形式保存圆球当前的旋转角度（0-360），确保退出重进后角度不会重置。
 * 0 = 滚动动画第 0 帧（正确角度）。该 Buff 为中性、常驻状态，不随时间衰减。</p>
 */
public class SphereOrientation extends Buff {

	/** 当前朝向角度（0-360）。 */
	public float angle = 0f;

	private static final String ANGLE = "angle";

	@Override
	public boolean act() {
		// 常驻 Buff：每回合推进计时但不衰减、不消失
		spend( TICK );
		return true;
	}

	@Override
	public String icon() {
		// TODO: 暂无专属图标，临时复用 VERTIGO（旋转/眩晕）图标，后续替换为圆球朝向专用图标
		return BuffIndicator.VERTIGO;
	}

	@Override
	public String desc() {
		if (target instanceof Hero && ((Hero) target).isSphereSkin()){
			Hero h = (Hero) target;
			return Messages.get( this, "desc",
					Math.round( h.sphereAngle() ),
					h.sphereTurnsToCorrect() );
		}
		return Messages.get( this, "desc", 0, 0f );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ANGLE, angle );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		angle = bundle.getFloat( ANGLE );
	}
}
