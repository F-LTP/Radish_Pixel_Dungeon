/*
 * Radish Pixel Dungeon
 * Copyright (C) 2026
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
import com.watabou.noosa.TextureFilm;

/**
 * 游商精灵。
 *
 * 素材来自桌面 ai 文件夹那张 256x512 的 NPC 图集，按行拆成 7 张独立贴图
 * （npc_peddler0~6.png，每张 176x16 = 11 帧 x 16x16）。
 *
 * 注意：图集里并不是每一格都画了内容。实测逐帧像素统计（透明像素视为空）：
 *   - variant 0/1/2/3/6：11 帧全部有内容，可以整排循环；
 *   - variant 4（熔）：只有第 1~5 帧有内容，第 0、6~10 帧是空白格；
 *   - variant 5（灰羽）：只有第 0、9、10 帧有内容，第 1~8 帧是空白格。
 * 若把整排 11 帧无脑塞进 idle 动画，variant 4 和 5 就会在播放时不停闪出空白。
 * 因此这里为每个 variant 声明各自的有效帧序列，空帧一律不进动画。
 *
 * 游商站着不动、也不会打人，所以走位/攻击/死亡都复用 idle 的有效帧。
 */
public class PeddlerSprite extends MobSprite {

	/** 贴图表：下标即 Peddler 的 variant。 */
	private static final String[] SHEETS = {
			Assets.Sprites.NPC_PEDDLER0,
			Assets.Sprites.NPC_PEDDLER1,
			Assets.Sprites.NPC_PEDDLER2,
			Assets.Sprites.NPC_PEDDLER3,
			Assets.Sprites.NPC_PEDDLER4,
			Assets.Sprites.NPC_PEDDLER5,
			Assets.Sprites.NPC_PEDDLER6,
	};

	/**
	 * 每个 variant 的有效帧序列（只列真正画了内容的格子，空帧全部剔除）。
	 * 新增变体时务必先用 {@code _tools} 里的逐帧检查确认哪些帧有像素。
	 */
	private static final int[][] VALID_FRAMES = {
			// variant 0：11 帧全满
			{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
			// variant 1：11 帧全满
			{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
			// variant 2：11 帧全满
			{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
			// variant 3：11 帧全满
			{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
			// variant 4（熔）：只有 1~5 有内容
			{ 1, 2, 3, 4, 5 },
			// variant 5（灰羽）：只有 0、9、10 有内容
			{ 0, 9, 10 },
			// variant 6：11 帧全满
			{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
	};

	public PeddlerSprite(int variant) {
		super();

		if (variant < 0 || variant >= SHEETS.length) {
			variant = 0;
		}
		texture(SHEETS[variant]);

		TextureFilm film = new TextureFilm(texture, 16, 16);

		int[] frames = VALID_FRAMES[variant];
		// 防御：万一某个变体的有效帧表被写空，退回第 0 帧，绝不让动画没有任何帧可播
		if (frames == null || frames.length == 0) {
			frames = new int[] { 0 };
		}

		// frames() 的签名是 frames(TextureFilm, Object...)：
		// 直接把 int[] 传进去会被当成「一个 Object 元素」而不是逐个帧号，必须逐帧装箱。
		Object[] frameObjects = new Object[frames.length];
		for (int i = 0; i < frames.length; i++) {
			frameObjects[i] = frames[i];
		}

		idle = new Animation(6, true);
		idle.frames(film, frameObjects);

		run = idle.clone();
		attack = idle.clone();
		die = idle.clone();

		idle();
	}

	/** 供 catalog 出错误时的兜底，避免一异常就整张图消失。 */
	public PeddlerSprite() {
		this(0);
	}
}
