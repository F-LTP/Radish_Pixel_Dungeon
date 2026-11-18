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

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AnkhInvulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeroDisguise;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WheelchairRush;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.AshKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.FatedDraw;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight.WeaponMasteryTalent;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.AfterGlow;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.CloakofGreyFeather;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.CrabArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.DarkCoat;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.EnergyArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PrisonArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.RatArmor;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.RectF;

public class HeroSprite extends CharSprite {
	
	private static final int FRAME_WIDTH	= 12;
	private static final int FRAME_HEIGHT	= 15;
	
	private static final int RUN_FRAMERATE	= 20;

	/** 标准英雄小人待机帧序（TextureFilm 索引）。 */
	public static final int[] IDLE_FRAMES = { 0, 0, 0, 1, 0, 0, 1, 1 };
	
	/** 每个英雄贴图各自缓存的「护甲行」胶片（不同高度的贴图不能共用 UV）。 */
	private static final java.util.HashMap<String, TextureFilm> tierFilms = new java.util.HashMap<>();

	/** 当前精灵使用的贴图资源路径，用于取对应高度的行胶片。 */
	private String sheetAsset = Assets.Sprites.ROGUE;

	protected Animation fly;
	protected Animation read;

	public HeroSprite() {
		super();
		
		sheetAsset = hero.heroClass.spritesheet();
		texture( sheetAsset );
		updateArmor();
		
		link( hero );

		if (ch.isAlive())
			idle();
		else
			die();
	}

	public void disguise(HeroClass cls){
		sheetAsset = cls.spritesheet();
		texture( sheetAsset );
		updateArmor();
	}

	public void updateArmor() {
		// 皮肤使用特制贴图，动画由皮肤自己的精灵类（如 GamblerSprite）负责，
		// 直接返回，不套用标准英雄的护甲行逻辑。
		if (hero.heroClass.activeSkin() != null) return;

		int t=0;
		Armor armor = hero.belongings.armor();
		
		// 月华角色特殊贴图逻辑（优先级从高到低）
		if (hero.heroClass == HeroClasses.MOONLIGHT) {
			WeaponMasteryTalent.MasteryTracker WeaponMasteryTracker = hero.buff(WeaponMasteryTalent.MasteryTracker.class);

			// 第7行：各种强力buff状态（HolyLanceForm/SoulStreamForm/FatalBladeForm）
			if (hero.buff(AshKing.HolyLanceForm.class) != null ||
				hero.buff(AshKing.SoulStreamForm.class) != null ||
				hero.buff(AshKing.FatalBladeForm.class) != null ||
				hero.buff(FatedDraw.FatedDrawTracker.class) != null ||
					hero.buff(AnkhInvulnerability.class) != null){
				t = 6;
			}
			// 第6行：轮椅加速状态
			else if (hero.buff(WheelchairRush.class) != null) {
				t = 5;
			}
			// 第5行：剑盾骑士天赋
			else if (hero.hasTalent(Talent.SWORD_SHIELD_KNIGHT)) {
				t = 4;
			}
			// 第3行：武器掌握天赋满层
			else if (WeaponMasteryTracker != null && WeaponMasteryTracker.turnCount>=500) {
				t = 2;
			}
			// 第2行：穿戴其他盔甲（非布甲）
			else if (armor != null && !(armor instanceof ClothArmor)) {
				t = 1;
			}
			// 第1行：光身子或布甲
			else {
				t = 0;
			}
		}
		// 其他角色使用原有逻辑
		else {
			if (armor instanceof ClassArmor){
				t= 6;
			} else if (armor != null) {
					if (armor instanceof PrisonArmor) t=7;
					else if (armor instanceof CrabArmor) t=8;
					else if (armor instanceof DarkCoat) t=9;
					else if (armor instanceof AfterGlow) t=10;
					else if (armor instanceof CloakofGreyFeather) t=11;
					else if (armor instanceof RatArmor) t=12;

					else if (armor instanceof EnergyArmor){
						t= ((EnergyArmor) armor).Energy();
					}
				else t = armor.tier;
			}
		}

		// 四件新护甲：与既有护甲一样只切换到角色贴图的对应行，不更换贴图。
		int outfitRow = ArmorOutfit.rowFor( armor );
		if (outfitRow >= 0 && ArmorOutfit.hasOutfit( hero.heroClass )) t = outfitRow;

		// 行号是按英雄本职业和护甲算的，而各职业贴图的行数并不一样（月华只有 17 行），实际显示的贴图不一定有这一行：
		// 伪装成行数更少的职业，或者本职业的贴图没画这件护甲的专属外观行，都会取到不存在的行，TextureFilm 直接空指针。
		// 每张英雄贴图都有 0~6 行，这时退回按护甲阶数取行。
		if (tiers(sheetAsset).get( t ) == null) t = hero.tier();

		TextureFilm film = new TextureFilm( tiers(sheetAsset), t, FRAME_WIDTH, FRAME_HEIGHT );

		idle = new Animation( 1, true );
		idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

		run = new Animation( RUN_FRAMERATE, true );
		run.frames( film, 2, 3, 4, 5, 6, 7 );

		die = new Animation( 20, false );
		// 标准素材的死亡帧是骷髅落地：第 12 帧是第 11 帧整体上移 1 像素的「弹起」，最后要回到第 11 帧才算落定。
		// 月华整张图和混灵那一行画的是一路倒下、消散的连续过程，第 12 帧就是终态，回到第 11 帧反而会倒放一帧。
		// 混灵的消散动画只画在有专属外观的贴图上，所以要看当前显示的贴图，不能只认行号。
		boolean endsOnLastFrame = Assets.Sprites.MOONLIGHT.equals( sheetAsset )
				|| (t == ArmorOutfit.ROW_SOULMIX && ArmorOutfit.sheetHasOutfit( sheetAsset ));
		die.frames( film, 8, 9, 10, 11, 12, endsOnLastFrame ? 12 : 11 );

		attack = new Animation( 15, false );
		attack.frames( film, 13, 14, 15, 0 );

		zap = attack.clone();

		operate = new Animation( 8, false );
		operate.frames( film, 16, 17, 16, 17 );

		fly = new Animation( 1, true );
		fly.frames( film, hero.heroClass == HeroClasses.MOONLIGHT ? 0 : 18 );

		read = new Animation( 20, false );
		read.frames( film, 19, 20, 20, 20, 20, 20, 20, 20, 20, 19 );

		if (hero.isAlive())
			idle();
		else
			die();
	}
	
	@Override
	public void place( int p ) {
		super.place( p );
		if (Game.scene() instanceof GameScene) Camera.main.panFollow(this, 5f);
	}

	@Override
	public void move( int from, int to ) {
		super.move( from, to );
		if (ch != null && ch.flying) {
			play( fly );
		}
		Camera.main.panFollow(this, 20f);
	}

	@Override
	public void idle() {
		super.idle();
		if (ch != null && ch.flying) {
			play( fly );
		}
	}

	@Override
	public void jump( int from, int to, float height, float duration,  Callback callback ) {
		super.jump( from, to, height, duration, callback );
		play( fly );
		Camera.main.panFollow(this, 20f);
	}

	public synchronized void read() {
		animCallback = new Callback() {
			@Override
			public void call() {
				idle();
				ch.onOperateComplete();
			}
		};
		play( read );
	}

	@Override
	public void bloodBurstA(PointF from, int damage) {
		//Does nothing.

		/*
		 * This is both for visual clarity, and also for content ratings regarding violence
		 * towards human characters. The heroes are the only human or human-like characters which
		 * participate in combat, so removing all blood associated with them is a simple way to
		 * reduce the violence rating of the game.
		 */
	}

	@Override
	public void update() {
		sleeping = ch.isAlive() && ((Hero)ch).resting;
		
		super.update();
	}
	
	public void sprint( float speed ) {
		run.delay = 1f / speed / RUN_FRAMERATE;
	}
	
	public static TextureFilm tiers() {
		return tiers( Assets.Sprites.ROGUE );
	}

	/**
	 * 取指定英雄贴图的「护甲行」胶片。
	 *
	 * <p>行胶片把整张图按 15px 高切成若干行；UV 归一化依赖贴图自身高度，
	 * 因此不同高度的图（如月华 256×256）必须各建一份，不能共用 ROGUE 的。</p>
	 */
	public static TextureFilm tiers( String asset ) {
		TextureFilm film = tierFilms.get( asset );
		if ( film == null ) {
			SmartTexture texture = TextureCache.get( asset );
			film = new TextureFilm( texture, texture.width, FRAME_HEIGHT );
			tierFilms.put( asset, film );
		}
		return film;
	}

	public static Image avatar( Hero hero ){
		HeroDisguise disguise = hero.buff( HeroDisguise.class );
		HeroClass cl = disguise != null ? disguise.getDisguise() : hero.heroClass;
		// 头像要与游戏内角色一致，所以取外观行而不是护甲阶数；
		// 但伪装后的贴图可能没有这一行（见 updateArmor），没有就退回按护甲阶数取行。
		int row = hero.tier_for_image();
		if (tiers( cl.spritesheet() ).get( row ) == null) row = hero.tier();
		return avatar( cl, row );
	}
	
	public static Image avatar( HeroClass cl, int armorTier ) {
		return avatar( cl, armorTier, cl.GetSkin() );
	}

	public static Image avatar( HeroClass cl, int armorTier, int skinIndex ) {
		
		SkinDefinition skin = cl.skin( skinIndex );
		// 自定义贴图皮肤：直接使用皮肤贴图的待机帧作为头像，不套用原版护甲行
		if (skin != null && skin.customSprite()){
			Image avatar = new Image( skin.asset() );
			TextureFilm film = new TextureFilm( avatar.texture, skin.frameW(), skin.frameH() );
			RectF frame = film.get( skin.idleFrames()[0] );
			avatar.frame( frame );
			return avatar;
		}

		TextureFilm rows = tiers( cl.spritesheet() );
		RectF patch = rows.get( armorTier );
		// 调用方只给职业和行号，行号可能超出这张贴图（状态栏、存档窗口的行号是按护甲算的，
		// 不看这个职业的贴图有没有画那一行）。这里拿不到英雄，退回每张图都有的第 0 行。
		if (patch == null) patch = rows.get( 0 );
		Image avatar = new Image( cl.spritesheet() );
		RectF frame = avatar.texture.uvRect( 1, 0, FRAME_WIDTH, FRAME_HEIGHT );
		frame.shift( patch.left, patch.top );
		avatar.frame( frame );
		
		return avatar;
	}

	/**
	 * 返回英雄小人待机贴图（含皮肤支持）。与 {@link #avatar(HeroClass, int)} 类似，
	 * 但保持原始帧尺寸（不裁剪到头像尺寸），用于存档列表等展示原始小人的位置。
	 */
	public static Image body( HeroClass cl, int armorTier ) {
		return body( cl, armorTier, cl.GetSkin() );
	}

	public static Image body( HeroClass cl, int armorTier, int skinIndex ) {
		SkinDefinition skin = cl.skin( skinIndex );
		if (skin != null && skin.customSprite()){
			Image body = new Image( skin.asset() );
			TextureFilm film = new TextureFilm( body.texture, skin.frameW(), skin.frameH() );
			RectF frame = film.get( skin.idleFrames()[0] );
			body.frame( frame );
			return body;
		}

		// 同 avatar()：行号超出贴图时取到的是贴图之外的区域，退回第 0 行
		if (tiers( cl.spritesheet() ).get( armorTier ) == null) armorTier = 0;

		return new Image( cl.spritesheet(), 0, 15 * armorTier, FRAME_WIDTH, FRAME_HEIGHT );
	}
}
