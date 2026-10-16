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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroRegistry;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndKeyBindings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.noosa.Image;



/**
 * 职业类 - 通过注册中心获取具体定义。
 */
public class HeroClass {

	//职业实例与全量集合见 {@link HeroClasses}（Manager）

	private final String name;
	private HeroDefinition definition;

	HeroClass(String name) {
		this.name = name;
	}

	public void bindDefinition(HeroDefinition def) { this.definition = def; }

	public HeroDefinition definition() {
		if (definition == null){
			definition = HeroRegistry.get(this);
		}
		return definition;
	}

	public String name() { return name; }

	public int ordinal() {
		int i = 0;
		for (HeroClass cls : HeroClasses.ALL){
			if (cls == this) return i;
			i++;
		}
		return 0;
	}

	//代理方法
	public String spritesheet() { return definition().spritesheet(); }
	public String splashArt() { return definition().splashArt(); }
	public String GetSkinAssest() { return definition().avatarSkin(); }

	public boolean isUnlocked() {
		return definition().isUnlocked();
	}

	public HeroSubClass[] subClasses() { return definition().subClasses(); }

	public ArmorAbility[] armorAbilities() { return definition().armorAbilities(); }

	public void initHero( Hero hero ) {
		// A newly-created Hero defaults to rogue/skin 0, so activeDefinition()
		// would ignore the skin selected for a new rogue before initialization.
		SkinDefinition selectedSkin = skin(getGlobalSkin());
		(selectedSkin != null ? selectedSkin : definition()).initHero(hero);
	}

	public TalentSet talentSet() { return definition == null ? null : definition.talents(); }

	/** 当前有效定义：若选中了皮肤变体则返回该皮肤，否则为基础职业。 */
	public HeroDefinition activeDefinition() {
		SkinDefinition skin = skin(GetSkin());
		return skin != null ? skin : definition();
	}

	/** 当前选中的皮肤变体（无皮肤时返回 null）。 */
	public SkinDefinition activeSkin() {
		return skin(GetSkin());
	}

	/**
	 * 根据皮肤索引返回对应的皮肤变体定义。
	 * @return 皮肤变体；若该索引不属于任何皮肤（如基础职业或纯外观头像皮肤），返回 null。
	 */
	public SkinDefinition skin(int index) {
		if (index <= 0) return null;
		for (SkinDefinition s : definition().skins()){
			if (s.skinIndex() == index) return s;
		}
		return null;
	}

	public String title() {
		return activeDefinition().heroName();
	}

	public String desc(){
		return activeDefinition().heroDesc();
	}

	public String shortDesc(){
		return activeDefinition().heroShortDesc();
	}

	public String unlockMsg() {
		return activeDefinition().heroUnlockMsg();
	}

	/** 按指定皮肤索引取显示名称（无皮肤时为基础职业名），用于存档列表等无英雄上下文的界面。 */
	public String title(int skinIndex) {
		SkinDefinition s = skin(skinIndex);
		return s != null ? s.heroName() : definition().heroName();
	}

	/** 按指定皮肤索引取长描述。 */
	public String desc(int skinIndex) {
		SkinDefinition s = skin(skinIndex);
		return s != null ? s.heroDesc() : definition().heroDesc();
	}

	/** 按指定皮肤索引取短描述。 */
	public String shortDesc(int skinIndex) {
		SkinDefinition s = skin(skinIndex);
		return s != null ? s.heroShortDesc() : definition().heroShortDesc();
	}

	/** 按指定皮肤索引取解锁提示。 */
	public String unlockMsg(int skinIndex) {
		SkinDefinition s = skin(skinIndex);
		return s != null ? s.heroUnlockMsg() : definition().heroUnlockMsg();
	}

	private static boolean onlyMode = false;

	/** 创建当前皮肤/职业的精灵实例（游戏内使用，需已存在 {@link Dungeon#hero}）。 */
	public HeroSprite createSkinSprite() {
		try {
			return activeDefinition().spriteClass().getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			return new HeroSprite();
		}
	}

	/** 当前是否启用独立贴图皮肤（如盗贼的赌徒）。 */
	public boolean isGamblerSkin() {
		return activeDefinition().customSprite();
	}

	/**
	 *
	 * @param skinIndex 注意皮肤iNDEX与PNG索引有关
	 */
	public void SetSkin(int skinIndex){
		boolean isSkinUnlock = false;
		Image img = new Image(this.GetSkinAssest());
		int skinCount = img.texture.width/64;

		if(skinIndex==0){
			isSkinUnlock = true;
		}else {
			while ( skinIndex < skinCount ) {
				isSkinUnlock = SPDSettings.isItemUnlock("avatars_" + name().toLowerCase() + "_" + skinIndex);
				if(!isSkinUnlock){
					skinIndex++;
				}else {
					break;
				}
			}

			// 普通头像皮肤遍历完毕后，继续寻找更高索引的独立贴图皮肤变体（如杂散/赌徒/流浪者/圆球）
			if (skinIndex >= skinCount) {
				isSkinUnlock = false;
				for (SkinDefinition s : definition().skins()){
					if (s.customSprite() && s.skinIndex() >= skinIndex){
						skinIndex = s.skinIndex();
						isSkinUnlock = true;
						break;
					}
				}
			}
		}

		// 独立贴图皮肤变体（如盗贼的赌徒）恒解锁
		SkinDefinition skin = skin(skinIndex);
		if (skin != null && skin.customSprite()) {
			isSkinUnlock = true;
		}

		if(!isSkinUnlock){
			skinIndex=0;
			if(!onlyMode){
				ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndKeyBindings.class,"switch_skin")));
				onlyMode = true;
			}
		}

		// 游戏内修改时写入当前英雄（随存档持久化）；未开局（角色选择界面）时写入全局暂存
		if (Dungeon.hero != null && Dungeon.hero.heroClass == this){
			Dungeon.hero.skin = skinIndex;
		}
		SPDSettings.setHeroSkin(this.ordinal(),skinIndex);
	}

	/** 获取当前有效皮肤索引。游戏内返回本局英雄的皮肤，否则返回全局暂存值。 */
	public int GetSkin(){
		if (Dungeon.hero != null && Dungeon.hero.heroClass == this){
			return Dungeon.hero.skin;
		}
		return SPDSettings.getHeroSkin(this.ordinal());
	}

	/** 直接读取全局暂存的皮肤索引（不涉及当前英雄），用于新建角色时回填。 */
	public int getGlobalSkin() {
		return SPDSettings.getHeroSkin(this.ordinal());
	}

	//存档序列化（实例名）
	public String saveName(){ return name; }

}
