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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoBuff;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.GameMath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class BuffIndicator extends Component {

	//transparent icon
	public static final String NONE = "none";

	//FIXME this is becoming a mess, should do a big cleaning pass on all of these
	//and think about tinting options
	public static final String MIND_VISION = "mind_vision";
	public static final String LEVITATION = "levitation";
	public static final String FIRE = "fire";
	public static final String POISON = "poison";
	public static final String PARALYSIS = "paralysis";
	public static final String HUNGER = "hunger";
	public static final String STARVATION = "starvation";
	public static final String TIME = "time";
	public static final String OOZE = "ooze";
	public static final String AMOK = "amok";
	public static final String TERROR = "terror";
	public static final String ROOTS = "roots";
	public static final String INVISIBLE = "invisible";
	public static final String SHADOWS = "shadows";
	public static final String WEAKNESS = "weakness";
	public static final String FROST = "frost";
	public static final String BLINDNESS = "blindness";
	public static final String COMBO = "combo";
	public static final String FURY = "fury";
	public static final String HERB_HEALING = "herb_healing";
	public static final String ARMOR = "armor";
	public static final String HEART = "heart";
	public static final String LIGHT = "light";
	public static final String CRIPPLE = "cripple";
	public static final String BARKSKIN = "barkskin";
	public static final String IMMUNITY = "immunity";
	public static final String BLEEDING = "bleeding";
	public static final String MARK = "mark";
	public static final String DEFERRED = "deferred";
	public static final String DROWSY = "drowsy";
	public static final String MAGIC_SLEEP = "magic_sleep";
	public static final String THORNS = "thorns";
	public static final String FORESIGHT = "foresight";
	public static final String VERTIGO = "vertigo";
	public static final String RECHARGING = "recharging";
	public static final String LOCKED_FLOOR = "locked_floor";
	public static final String CORRUPT = "corrupt";
	public static final String BLESS = "bless";
	public static final String RAGE = "rage";
	public static final String SACRIFICE = "sacrifice";
	public static final String BERSERK = "berserk";
	public static final String HASTE = "haste";
	public static final String PREPARATION = "preparation";
	public static final String WELL_FED = "well_fed";
	public static final String HEALING = "healing";
	public static final String WEAPON = "weapon";
	public static final String VULNERABLE = "vulnerable";
	public static final String HEX = "hex";
	public static final String DEGRADE = "degrade";
	public static final String PINCUSHION = "pincushion";
	public static final String UPGRADE = "upgrade";
	public static final String MOMENTUM = "momentum";
	public static final String ANKH = "ankh";
	public static final String NOINV = "noinv";
	public static final String TARGETED = "targeted";
	public static final String IMBUE = "imbue";
	public static final String ENDURE = "endure";
	public static final String INVERT_MARK = "invert_mark";
	public static final String NATURE_POWER = "nature_power";
	public static final String AMULET = "amulet";
	public static final String DUEL_CLEAVE = "duel_cleave";
	public static final String DUEL_GUARD = "duel_guard";
	public static final String DUEL_SPIN = "duel_spin";
	public static final String DUEL_EVASIVE = "duel_evasive";
	public static final String DUEL_DANCE = "duel_dance";
	public static final String DUEL_BRAWL = "duel_brawl";
	public static final String DUEL_XBOW = "duel_xbow";
	public static final String CHALLENGE = "challenge";
	public static final String MONK_ENERGY = "monk_energy";
	public static final String DUEL_COMBO = "duel_combo";
	public static final String DAZE = "daze";

	public static final String HOLD_BREATH = "hold_breath";
	public static final String A_EVA = "a_eva";
	//	public static final String DUEL_DANCE = "duel_dance";
	public static final String SCYTHE_S = "scythe_s";

	public static final String FOG_ROAD = "fog_road";

	public static final String TAI_COLD = "tai_cold";

	public static final String TAI_CRIT = "tai_crit";

	public static final String WAND = "wand";

	public static final String BELIEF_LINK = "belief_link";
	public static final String BELIEF_DNOT = "belief_dnot";

	public static final String CORRUPT_SPIRIT = "corrupt_spirit"; // 术士4-4 腐化怨灵debuff

	public static final String MAGIC_POINT = "magic_point";

	public static final int SIZE_SMALL  = 7;
	public static final int SIZE_LARGE  = 16;

	private static BuffIndicator heroInstance;
	// 修改：将单个bossInstance改为数组，支持最多4个Boss
	private static BuffIndicator[] bossInstances = new BuffIndicator[4];

	private LinkedHashMap<Buff, BuffButton> buffButtons = new LinkedHashMap<>();
	public boolean needsRefresh;
	private Char ch;

	private boolean large = false;
	private boolean threeColumnGrid = false;

	public BuffIndicator( Char ch, boolean large ) {
		this(ch, large, false);
	}

	public BuffIndicator( Char ch, boolean large, boolean threeColumnGrid ) {
		super();

		this.ch = ch;
		this.large = large;
		this.threeColumnGrid = threeColumnGrid;
		if (ch == Dungeon.hero) {
			heroInstance = this;
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		if (this == heroInstance) {
			heroInstance = null;
		}
		// 修改：销毁时清理对应的boss实例引用
		for (int i = 0; i < bossInstances.length; i++) {
			if (bossInstances[i] == this) {
				bossInstances[i] = null;
				break;
			}
		}
	}

	@Override
	public synchronized void update() {
		super.update();
		if (needsRefresh){
			needsRefresh = false;
			layout();
		}
	}

	private boolean buffsHidden = false;

	@Override
	protected void layout() {

		ArrayList<Buff> newBuffs = new ArrayList<>();
		for (Buff buff : ch.buffs()) {
			if (!NONE.equals(buff.icon())) {
				newBuffs.add(buff);
			}
		}

		int size = large ? SIZE_LARGE : SIZE_SMALL;

		//remove any icons no longer present
		for (Buff buff : buffButtons.keySet().toArray(new Buff[0])){
			if (!newBuffs.contains(buff)){
				Image icon = buffButtons.get( buff ).icon;
				icon.originToCenter();
				icon.alpha(0.6f);
				add( icon );
				add( new AlphaTweener( icon, 0, 0.6f ) {
					@Override
					protected void updateValues( float progress ) {
						super.updateValues( progress );
						image.scale.set( 1 + 5 * progress );
					}

					@Override
					protected void onComplete() {
						image.killAndErase();
					}
				} );

				buffButtons.get( buff ).destroy();
				remove(buffButtons.get( buff ));
				buffButtons.remove( buff );
			}
		}

		//add new icons
		for (Buff buff : newBuffs) {
			if (!buffButtons.containsKey(buff)) {
				BuffButton icon = new BuffButton(buff, large);
				add(icon);
				buffButtons.put( buff, icon );
			}
		}

		//layout
		if (threeColumnGrid) {
			layoutThreeColumnGrid(size);
			return;
		}

		int pos = 0;
		float lastIconLeft = 0;
		for (BuffButton icon : buffButtons.values()){
			icon.updateIcon();
			//button areas are slightly oversized, especially on small buttons
			icon.setRect(x + pos * (size + 1), y, size + 1, size + (large ? 0 : 5));
			PixelScene.align(icon);
			pos++;

			icon.visible = icon.left() <= right();
			lastIconLeft = icon.left();
		}

		buffsHidden = false;
		//squish buff icons together if there isn't enough room
		float excessWidth = lastIconLeft - right();
		if (excessWidth > 0) {
			float leftAdjust = excessWidth/(buffButtons.size()-1);
			//can't squish by more than 50% on large and 62% on small
			if (large && leftAdjust >= size*0.48f) leftAdjust = size*0.5f;
			if (!large && leftAdjust >= size*0.62f) leftAdjust = size*0.65f;
			float cumulativeAdjust = leftAdjust * (buffButtons.size()-1);

			ArrayList<BuffButton> buttons = new ArrayList<>(buffButtons.values());
			Collections.reverse(buttons);
			for (BuffButton icon : buttons) {
				icon.setPos(icon.left() - cumulativeAdjust, icon.top());
				icon.visible = icon.left() <= right();
				if (!icon.visible) buffsHidden = true;
				PixelScene.align(icon);
				bringToFront(icon);
				icon.givePointerPriority();
				cumulativeAdjust -= leftAdjust;
			}
		}
	}

	private void layoutThreeColumnGrid(int size) {
		int count = buffButtons.size();
		if (count == 0) {
			buffsHidden = false;
			return;
		}

		float buttonWidth = size + 1f;
		float buttonHeight = size + (large ? 0f : 5f);
		float columnStep = Math.max(0f, (width - buttonWidth) / 2f);
		int rows = (count + 2) / 3;
		float normalRowStep = Math.max(0f, (height - buttonHeight) / 2f);
		float rowStep = rows <= 3
				? normalRowStep
				: Math.max(0f, (height - buttonHeight) / (rows - 1f));

		int index = 0;
		for (BuffButton icon : buffButtons.values()) {
			int column = index % 3;
			int row = index / 3;
			icon.updateIcon();
			icon.setRect(x + column * columnStep, y + row * rowStep,
					buttonWidth, buttonHeight);
			icon.visible = true;
			PixelScene.align(icon);
			bringToFront(icon);
			icon.givePointerPriority();
			index++;
		}
		buffsHidden = false;
	}

	public boolean allBuffsVisible(){
		return !buffsHidden;
	}

	private static class BuffButton extends IconButton {

		private Buff buff;

		private boolean large;

		public Image grey; //only for small
		public BitmapText text; //only for large

		public BuffButton( Buff buff, boolean large ){
			super( new BuffIcon(buff, large));
			this.buff = buff;
			this.large = large;

			bringToFront(grey);
			bringToFront(text);
		}

		@Override
		protected void createChildren() {
			super.createChildren();
			grey = new Image( TextureCache.createSolid(0xCC666666));
			add( grey );

			text = new BitmapText(PixelScene.pixelFont);
			add( text );
		}

		public void updateIcon(){
			((BuffIcon)icon).refresh(buff);
			//如果有文字显示（如剩余次数），则显示文字而非灰色进度条
			if (!buff.iconTextDisplay().isEmpty()) {
				text.visible = true;
				grey.visible = false;
				if (buff.type == Buff.buffType.POSITIVE)        text.hardlight(CharSprite.POSITIVE);
				else if (buff.type == Buff.buffType.NEGATIVE)   text.hardlight(CharSprite.NEGATIVE);
				text.alpha(0.7f);

				text.text(buff.iconTextDisplay());
				text.measure();
			} else {
				//round up to the nearest pixel if <50% faded, otherwise round down
				text.visible = false;
				grey.visible = true;
				float fadeHeight = GameMath.gate(0, buff.iconFadePercent(), 1) * icon.height();
				float zoom = (camera() != null) ? camera().zoom : 1;
				if (fadeHeight < icon.height() / 2f) {
					grey.scale.set(icon.width(), (float) Math.ceil(zoom * fadeHeight) / zoom);
				} else {
					grey.scale.set(icon.width(), (float) Math.floor(zoom * fadeHeight) / zoom);
				}
			}
		}

		@Override
		protected void layout() {
			super.layout();
			grey.x = icon.x = this.x + (large ? 0 : 1);
			grey.y = icon.y = this.y + (large ? 0 : 2);

			//小型图标使用更小的字体
			if (!large) {
				text.scale.set(PixelScene.align(0.5f));
			} else if (text.width > width()){
				text.scale.set(PixelScene.align(0.5f));
			} else {
				text.scale.set(1f);
			}
			text.x = this.x + width() - text.width() - 1;
			text.y = this.y + width() - text.baseLine() - 2;
		}

		@Override
		protected void onClick() {
			if (!NONE.equals(buff.icon())) GameScene.show(new WndInfoBuff(buff));
		}

		@Override
		protected void onPointerDown() {
			//don't affect buff color
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			//don't affect buff color
		}

		@Override
		protected String hoverText() {
			return Messages.titleCase(buff.name());
		}
	}

	public static void refreshHero() {
		if (heroInstance != null) {
			heroInstance.needsRefresh = true;
		}
	}

	/**
	 * 兼容原有方法：刷新第一个Boss的Buff指示器
	 */
	public static void refreshBoss(){
		// 刷新第一个Boss
		if (bossInstances[0] != null) {
			bossInstances[0].needsRefresh = true;
		}
	}

	/**
	 * 新增方法：刷新所有活跃的Boss Buff指示器
	 */
	public static void refreshAllBosses() {
		for (BuffIndicator instance : bossInstances) {
			if (instance != null) {
				instance.needsRefresh = true;
			}
		}
	}

	/**
	 * 兼容原有方法：设置第一个Boss的Buff实例
	 */
	public static void setBossInstance(BuffIndicator boss){
		bossInstances[0] = boss;
	}

	/**
	 * 新增方法：设置指定索引的Boss Buff实例（适配多Boss血条）
	 */
	public static void setBossInstance(int index, BuffIndicator boss) {
		if (index >= 0 && index < bossInstances.length) {
			bossInstances[index] = boss;
		}
	}

	/**
	 * 新增方法：获取指定索引的Boss Buff实例
	 */
	public static BuffIndicator getBossInstance(int index) {
		if (index >= 0 && index < bossInstances.length) {
			return bossInstances[index];
		}
		return null;
	}
}
