/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2024-2026 Radish Pixel Dungeon Team
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

/**
 * 自定义死亡动画。由 Char.die() 在真正死亡时调用，
 * 实现内部自决：播放 shader / 粒子 / 替代贴图效果，并自行处理异步与收尾。
 */
public interface DeathAnimation {

	/**
	 * 接管尸体。
	 * @param sprite 死亡角色的精灵（调用方保证非 null 且有 parent）
	 * @param onFinished 尸体默认清理动作（killAndErase），实现可选择调用或自定收尾
	 */
	void play( CharSprite sprite, Runnable onFinished );
}
