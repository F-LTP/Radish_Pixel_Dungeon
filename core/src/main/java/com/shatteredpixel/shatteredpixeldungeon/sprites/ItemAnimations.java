package com.shatteredpixel.shatteredpixeldungeon.sprites;

import java.util.HashMap;

/**
 * 物品的声明式动画集：把一张条带/网格贴图按帧序号划分为多个命名动画。
 * 展示时默认播放 idle，其余动画由外部在恰当时机经 ItemSprite.playAnim 按名触发。
 * 贴图需在 sprites/items/manifest.txt 登记。
 */
public class ItemAnimations {

	public static final String IDLE = "idle";

	/** 一段动画：帧序号在贴图内先行后列、从0计数 */
	public static class Clip {
		public final String name;
		public final int start;
		public final int count;
		public final int fps;
		public final boolean looped;

		Clip( String name, int start, int count, int fps, boolean looped ){
			this.name = name;
			this.start = start;
			this.count = count;
			this.fps = fps;
			this.looped = looped;
		}
	}

	private String asset;
	private int cell = 16;
	private final HashMap<String, Clip> clips = new HashMap<>();

	/** 绑定贴图资产名（不含.png）与帧格边长；帧格为正方形 */
	public ItemAnimations of( String asset, int cell ){
		this.asset = asset;
		this.cell = cell;
		return this;
	}

	/** 声明一段动画；同名覆盖 */
	public ItemAnimations add( String name, int start, int count, int fps, boolean looped ){
		clips.put( name, new Clip( name, start, count, fps, looped ) );
		return this;
	}

	public boolean has( String name ){
		return clips.containsKey( name );
	}

	public Clip get( String name ){
		return clips.get( name );
	}

	public String asset(){
		return asset;
	}

	public int cell(){
		return cell;
	}
}
