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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameSettings;
import com.watabou.utils.Point;

import java.util.Locale;

public class SPDSettings extends GameSettings {
	
	//Version info
	
	public static final String KEY_VERSION      = "version";
	
	public static void version( int value)  {
		put( KEY_VERSION, value );
	}
	
	public static int version() {
		return getInt( KEY_VERSION, 0 );
	}
	
	//Display
	
	public static final String KEY_FULLSCREEN	= "fullscreen";
	public static final String KEY_LANDSCAPE	= "landscape";
	public static final String KEY_POWER_SAVER 	= "power_saver";
	public static final String KEY_ZOOM			= "zoom";
	public static final String KEY_BRIGHTNESS	= "brightness";
	public static final String KEY_GRID 	    = "visual_grid";
	public static final String KEY_CAMERA_FOLLOW= "camera_follow";
	public static final String KEY_SCREEN_SHAKE = "screen_shake";
	
	public static void fullscreen( boolean value ) {
		put( KEY_FULLSCREEN, value );
		
		ShatteredPixelDungeon.updateSystemUI();
	}
	
	public static boolean fullscreen() {
		return getBoolean( KEY_FULLSCREEN, DeviceCompat.isDesktop() );
	}
	
	public static void landscape( boolean value ){
		put( KEY_LANDSCAPE, value );
		((ShatteredPixelDungeon)ShatteredPixelDungeon.instance).updateDisplaySize();
	}
	
	//can return null because we need to directly handle the case of landscape not being set
	// as there are different defaults for different devices
	public static Boolean landscape(){
		if (contains(KEY_LANDSCAPE)){
			return getBoolean(KEY_LANDSCAPE, false);
		} else {
			return null;
		}
	}
	
	public static void powerSaver( boolean value ){
		put( KEY_POWER_SAVER, value );
		((ShatteredPixelDungeon)ShatteredPixelDungeon.instance).updateDisplaySize();
	}
	
	public static boolean powerSaver(){
		return getBoolean( KEY_POWER_SAVER, false );
	}
	
	public static void zoom( int value ) {
		put( KEY_ZOOM, value );
	}
	
	public static int zoom() {
		return getInt( KEY_ZOOM, 0 );
	}
	
	public static void brightness( int value ) {
		put( KEY_BRIGHTNESS, value );
		GameScene.updateFog();
	}
	
	public static int brightness() {
		return getInt( KEY_BRIGHTNESS, 0, -1, 1 );
	}
	
	public static void visualGrid( int value ){
		put( KEY_GRID, value );
		GameScene.updateMap();
	}
	
	public static int visualGrid() {
		return getInt( KEY_GRID, 0, -1, 2 );
	}

	public static void cameraFollow( int value ){
		put( KEY_CAMERA_FOLLOW, value );
	}

	public static int cameraFollow() {
		return getInt( KEY_CAMERA_FOLLOW, 4, 1, 4 );
	}

	public static void screenShake( int value ){
		put( KEY_SCREEN_SHAKE, value );
	}

	public static int screenShake() {
		return getInt( KEY_SCREEN_SHAKE, 2, 0, 4 );
	}
	
	//Interface

	public static final String KEY_UI_SIZE 	    = "full_ui";
	public static final String KEY_SCALE		= "scale";
	public static final String KEY_QUICK_SWAP	= "quickslot_swapper";
	public static final String KEY_FLIPTOOLBAR	= "flipped_ui";
	public static final String KEY_FLIPTAGS 	= "flip_tags";
	public static final String KEY_BARMODE		= "toolbar_mode";
	public static final String KEY_SLOTWATERSKIN= "quickslot_waterskin";
	public static final String KEY_SYSTEMFONT	= "system_font";
	public static final String KEY_VIBRATION    = "vibration";

	public static final String KEY_ORIGINMAP    = "origin_map";

	public static final String KEY_SEEDDEPTH	= "seeddepth";

	//0 = mobile, 1 = mixed (large without inventory in main UI), 2 = large
	public static void interfaceSize( int value ){
		put( KEY_UI_SIZE, value );
	}

	public static int interfaceSize(){
		int size = getInt( KEY_UI_SIZE, DeviceCompat.isDesktop() ? 2 : 0 );
		if (size > 0){
			//force mobile UI if there is not enough space for full UI
			float wMin = Game.width / PixelScene.MIN_WIDTH_FULL;
			float hMin = Game.height / PixelScene.MIN_HEIGHT_FULL;
			if (Math.min(wMin, hMin) < 2*Game.density){
				size = 0;
			}
		}
		return size;
	}

	public static void scale( int value ) {
		put( KEY_SCALE, value );
	}

	public static int scale() {
		return getInt( KEY_SCALE, 0 );
	}


	public static void setKeySeeddepth( int value ) {
		put( KEY_SEEDDEPTH, value );
	}

	public static int getKeySeedDepth() {
		return getInt( KEY_SEEDDEPTH, 1 );
	}

	
	public static void quickSwapper(boolean value ){ put( KEY_QUICK_SWAP, value ); }
	
	public static boolean quickSwapper(){ return getBoolean( KEY_QUICK_SWAP, true); }
	
	public static void flipToolbar( boolean value) {
		put(KEY_FLIPTOOLBAR, value );
	}
	
	public static boolean flipToolbar(){ return getBoolean(KEY_FLIPTOOLBAR, false); }
	
	public static void flipTags( boolean value) {
		put(KEY_FLIPTAGS, value );
	}
	
	public static boolean flipTags(){ return getBoolean(KEY_FLIPTAGS, false); }
	
	public static void toolbarMode( String value ) {
		put( KEY_BARMODE, value );
	}
	
	public static String toolbarMode() {
		return getString(KEY_BARMODE, PixelScene.landscape() ? "GROUP" : "SPLIT");
	}

	public static void quickslotWaterskin( boolean value ){
		put( KEY_SLOTWATERSKIN, value);
	}

	public static boolean quickslotWaterskin(){
		return getBoolean( KEY_SLOTWATERSKIN, true );
	}

	public static void systemFont(boolean value){
		put(KEY_SYSTEMFONT, value);
	}

	public static boolean systemFont(){
		return getBoolean(KEY_SYSTEMFONT,
				(language() == Languages.KOREAN || language() == Languages.CHINESE || language() == Languages.JAPANESE));
	}

	public static void vibration(boolean value){
		put(KEY_VIBRATION, value);
	}

	public static boolean vibration(){
		return getBoolean(KEY_VIBRATION, true);
	}

	public static void origin_map(boolean value){
		put(KEY_ORIGINMAP, value);
	}

	public static boolean origin_map(){
		return getBoolean(KEY_ORIGINMAP, false);
	}

	//Game State
	
	public static final String KEY_LAST_CLASS	= "last_class";
	public static final String KEY_CHALLENGES	= "challenges";
	public static final String KEY_CUSTOM_SEED	= "custom_seed";
	public static final String KEY_LAST_DAILY	= "last_daily";
	public static final String KEY_INTRO		= "intro";

	public static final String KEY_UPDATEREADY = "updateready";
	
	public static void intro( boolean value ) {
		put( KEY_INTRO, value );
	}
	
	public static boolean intro() {
		return getBoolean( KEY_INTRO, true );
	}
	
	public static void lastClass( int value ) {
		put( KEY_LAST_CLASS, value );
	}
	
	public static int lastClass() {
		return getInt( KEY_LAST_CLASS, 0, 0, 3 );
	}
	
	public static void challenges( int value ) {
		put( KEY_CHALLENGES, value );
	}
	
	public static int challenges() {
		return getInt( KEY_CHALLENGES, 0, 0, Challenges.MAX_VALUE );
	}

	public static void customSeed( String value ){
		put( KEY_CUSTOM_SEED, value );
	}

	public static String customSeed() {
		return getString( KEY_CUSTOM_SEED, "", 20);
	}

	public static void lastDaily( long value ){
		put( KEY_LAST_DAILY, value );
	}

	public static long lastDaily() {
		return getLong( KEY_LAST_DAILY, 0);
	}


	//补偿
	public static void UpdateReady(boolean value ) {
		put( KEY_UPDATEREADY, value );
	}

	public static boolean UpdateReady() {
		return getBoolean( KEY_UPDATES, true );
	}

	public static void boatMeeted(boolean meet){put("boat_meeted",meet);}
	public static boolean boatMeeted(){
		return getBoolean("boat_meeted",false);
	}


	//Input

	public static final String KEY_CONTROLLER_SENS  = "controller_sens";
	public static final String KEY_MOVE_SENS        = "move_sens";

	public static void controllerPointerSensitivity( int value ){
		put( KEY_CONTROLLER_SENS, value );
	}

	public static int controllerPointerSensitivity(){
		return getInt(KEY_CONTROLLER_SENS, 5, 1, 10);
	}

	public static void movementHoldSensitivity( int value ){
		put( KEY_MOVE_SENS, value );
	}

	public static int movementHoldSensitivity(){
		return getInt(KEY_MOVE_SENS, 3, 0, 4);
	}

	//Connectivity

	public static final String KEY_NEWS     = "news";
	public static final String KEY_UPDATES	= "updates";
	public static final String KEY_BETAS	= "betas";
	public static final String KEY_WIFI     = "wifi";

	public static final String KEY_NEWS_LAST_READ = "news_last_read";

	public static void news(boolean value){
		put(KEY_NEWS, value);
	}

	public static boolean news(){
		return getBoolean(KEY_NEWS, true);
	}

	public static void updates(boolean value){
		put(KEY_UPDATES, value);
	}

	public static boolean updates(){
		return getBoolean(KEY_UPDATES, true);
	}

	public static void betas(boolean value){
		put(KEY_BETAS, value);
	}

	public static boolean betas(){
		return getBoolean(KEY_BETAS, Game.version.contains("BETA") || Game.version.contains("RC"));
	}

	public static void WiFi(boolean value){
		put(KEY_WIFI, value);
	}

	public static boolean WiFi(){
		return getBoolean(KEY_WIFI, true);
	}

	public static void newsLastRead(long lastRead){
		put(KEY_NEWS_LAST_READ, lastRead);
	}

	public static long newsLastRead(){
		return getLong(KEY_NEWS_LAST_READ, 0);
	}

	//Audio
	
	public static final String KEY_MUSIC		= "music";
	public static final String KEY_MUSIC_VOL    = "music_vol";
	public static final String KEY_SOUND_FX		= "soundfx";
	public static final String KEY_SFX_VOL      = "sfx_vol";
	public static final String KEY_IGNORE_SILENT= "ignore_silent";
	public static final String KEY_MUSIC_BG     = "music_bg";
	
	public static void music( boolean value ) {
		Music.INSTANCE.enable( value );
		put( KEY_MUSIC, value );
	}
	
	public static boolean music() {
		return getBoolean( KEY_MUSIC, true );
	}
	
	public static void musicVol( int value ){
		Music.INSTANCE.volume(value*value/100f);
		put( KEY_MUSIC_VOL, value );
	}
	
	public static int musicVol(){
		return getInt( KEY_MUSIC_VOL, 10, 0, 10 );
	}
	
	public static void soundFx( boolean value ) {
		Sample.INSTANCE.enable( value );
		put( KEY_SOUND_FX, value );
	}
	
	public static boolean soundFx() {
		return getBoolean( KEY_SOUND_FX, true );
	}
	
	public static void SFXVol( int value ) {
		Sample.INSTANCE.volume(value*value/100f);
		put( KEY_SFX_VOL, value );
	}
	
	public static int SFXVol() {
		return getInt( KEY_SFX_VOL, 10, 0, 10 );
	}

	public static void ignoreSilentMode( boolean value ){
		put( KEY_IGNORE_SILENT, value);
		Game.platform.setHonorSilentSwitch(!value);
	}

	public static boolean ignoreSilentMode(){
		return getBoolean( KEY_IGNORE_SILENT, false);
	}

	public static void playMusicInBackground( boolean value ){
		put( KEY_MUSIC_BG, value);
	}

	public static boolean playMusicInBackground(){
		return getBoolean( KEY_MUSIC_BG, true);
	}
	
	//Languages
	
	public static final String KEY_LANG         = "language";
	
	public static void language(Languages lang) {
		put( KEY_LANG, lang.code());
	}
	
	public static Languages language() {
		String code = getString(KEY_LANG, null);
		if (code == null){
			return Languages.matchLocale(Locale.getDefault());
		} else {
			return Languages.matchCode(code);
		}
	}

	//Window management (desktop only atm)
	
	public static final String KEY_WINDOW_WIDTH     = "window_width";
	public static final String KEY_WINDOW_HEIGHT    = "window_height";
	public static final String KEY_WINDOW_MAXIMIZED = "window_maximized";
	
	public static void windowResolution( Point p ){
		put(KEY_WINDOW_WIDTH, p.x);
		put(KEY_WINDOW_HEIGHT, p.y);
	}
	
	public static Point windowResolution(){
		return new Point(
				getInt( KEY_WINDOW_WIDTH, 800, 720, Integer.MAX_VALUE ),
				getInt( KEY_WINDOW_HEIGHT, 600, 400, Integer.MAX_VALUE )
		);
	}
	
	public static void windowMaximized( boolean value ){
		put( KEY_WINDOW_MAXIMIZED, value );
	}
	
	public static boolean windowMaximized(){
		return getBoolean( KEY_WINDOW_MAXIMIZED, false );
	}


	public static final String KEY_NORMAL = "window_maximized";

	public static void NORMAL_SKIN( boolean value ){
		put( KEY_NORMAL, value );
	}

	public static boolean NORMAL_SKIN(){
		return getBoolean( KEY_NORMAL, false );
	}


	public static final String KEY_UNLOCKITEM = "forever_unlock_item";

	public static final String KEY_CURRENTHEROSKIN = "current_hero_skin";

	// 1. 更新默认值，增加第 6 个英雄的数据 (0;0;0;0;0;0)
	public static String getSkin(){
		// 注意：这里建议不要以分号结尾，避免 split 的陷阱，或者配合 split(";", -1) 使用
		// 这里改为 6 个 0，对应 6 个英雄
		return getString( KEY_CURRENTHEROSKIN, "0;0;0;0;0;0");
	}

	public static int getHeroSkin(int hero){
		String[] itemArray = getSkin().split( ";", -1 ); // 使用 -1 防止末尾空字符串丢失


		if (hero < 0 || hero >= itemArray.length) {
			return 0;
		}

		try {
			return Integer.parseInt(itemArray[hero]);
		} catch (NumberFormatException e) {
			return 0; // 防止数据损坏导致解析失败
		}
	}

	// 3. 重写设置皮肤的逻辑，不再使用字符位置计算，而是使用数组操作
	public static void setHeroSkin(int hero, int skinIndex) {
		String[] currentSkins = getSkin().split(";", -1);

		// 动态扩容：如果当前数组长度不够（例如旧存档只有5个，现在要设置第6个），扩容数组
		if (hero >= currentSkins.length) {
			String[] newArray = new String[hero + 1];
			System.arraycopy(currentSkins, 0, newArray, 0, currentSkins.length);
			// 将新增的位置填充为默认值 "0"
			for (int i = currentSkins.length; i < newArray.length; i++) {
				newArray[i] = "0";
			}
			currentSkins = newArray;
		}

		// 更新指定英雄的皮肤
		currentSkins[hero] = String.valueOf(skinIndex);

		// 重新组合成字符串
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < currentSkins.length; i++) {
			sb.append(currentSkins[i]);
			if (i < currentSkins.length - 1) {
				sb.append(";");
			}
		}

		put(KEY_CURRENTHEROSKIN, sb.toString());
	}


	//TODO: 使用新接口替换物品解锁的旧方法
	/*
	 * @Breif 永久解锁物品，允许批量解锁，以","作为元素分隔符,";"作为物品分隔符
	 * 输入格式为String itemName1,boolean allowMulti1,int itemLimit1;String itemName2,boolean allowMulti2,int itemLimit2;...
	 * @Pramas String
	 * @NativeName: unlockItem
	 * @NativeFunction: void unlockItem(String)
	 */
	public static void unlockItem( String itemName ){
		String[] itemArray = itemName.split( ";" );
		StringBuilder items = new StringBuilder( unlockItem() );

		for( String item : itemArray) {
			String[] tempItem = item.split( "," );
			if( !isItemUnlock( tempItem[0] ) ){
				switch( tempItem.length ){
					case 1:
						items.append( item ).append( ",false,1;" );
						break;
					case 2:
						if( tempItem[1].matches( "\\d+" ) ){
							items.append( tempItem[0] ).append( ",false," ).append( tempItem[1] ).append( ";" );
						}else if( tempItem[1].equals( "true" ) || tempItem[1].equals( "false" ) ){
							items.append( item ).append( ",1;" );
						}else {
							continue;
						}
						break;
					case 3:
						items.append( item ).append( ";" );
						break;
				}
			}
		}

		put( KEY_UNLOCKITEM, items.toString() );
	}

	/*
	 * @Breif 永久解锁物品，第一个参数为itemName，即物品的名称；第二个参数为allowMulti，即是否允许多持该物品
	 * @Pramas String,boolean
	 * @NativeName: unlockItem
	 * @NativeFunction: void unlockItem(String,boolean)
	 */
	public static void unlockItem( String itemName, boolean allowMulti ){
		if( !isItemUnlock( itemName ) ){
			StringBuilder items = new StringBuilder( unlockItem() );
			items.append( itemName ).append( "," );
			items.append( allowMulti ).append( ",1;" );
			put( KEY_UNLOCKITEM, items.toString() );
		}
	}

	/*
	 * @Breif 永久解锁物品，第一个参数为itemName，即物品的名称；第二个参数为allowMulti，即是否允许多持该物品；第三个参数为limit，即持有该物品的上限
	 * @Pramas String,boolean,int
	 * @NativeName: unlockItem
	 * @NativeFunction: void unlockItem(String,boolean,int)
	 */
	public static void unlockItem( String itemName, boolean allowMulti, int limit ){
		if( !isItemUnlock( itemName ) ){
			StringBuilder items = new StringBuilder( unlockItem() );
			items.append( itemName ).append( "," );
			items.append( allowMulti ).append( "," );
			items.append( limit ).append( ";" );
			put( KEY_UNLOCKITEM, items.toString() );
		}
	}

	/*
	 * @Breif 获取已解锁的物品列表，以","作为元素分隔符,";"作为物品分隔符
	 * 输出格式为String itemName1,boolean allowMulti1,int itemLimit1;String itemName2,boolean allowMulti2,int itemLimit2;...
	 * @Pramas
	 * @NativeName: unlockItem
	 * @NativeFunction: String unlockItem()
	 */
	public static String unlockItem(){ return getString( KEY_UNLOCKITEM, ""); }

	/*
	 * @Breif 返回目标物品是否已经解锁
	 * @Pramas String
	 * @NativeName: isItemUnlock
	 * @NativeFunction: Boolean isItemUnlock(String)
	 */
	public static Boolean isItemUnlock( String itemName ){ return unlockItem().indexOf( itemName ) != -1; }

	/*
	 * @Breif 返回目标物品是否允许多持
	 * @Pramas String
	 * @NativeName: isUnlockItemAllowMulti
	 * @NativeFunction: Boolean isUnlockItemAllowMulti(String)
	 */
	public static Boolean isUnlockItemAllowMulti( String itemName ){
		if( !isItemUnlock( itemName ) ){
			return false;
		}

		String[] items = unlockItem().split( ";" );
		for( String item : items ){
			if( item.indexOf( itemName ) != -1 ){
				return Boolean.parseBoolean( item.split( "," )[1] );
			}
		}

		return false;
	}

	/*
	 * @Breif 返回目标物品的持有上限
	 * @Pramas int
	 * @NativeName: getUnlockItemLimit
	 * @NativeFunction: int getUnlockItemLimit(String)
	 */
	public static int getUnlockItemLimit( String itemName ){
		if( !isItemUnlock( itemName ) ){
			return -1;
		}

		String[] items = unlockItem().split( ";" );
		for( String item : items ){
			if( item.indexOf( itemName ) != -1 ){
				return Integer.parseInt( item.split( ",")[2] );
			}
		}

		return -1;
	}


	/*
	 * @Breif 将已解锁物品移除，允许同时移除多个物品
	 * @Pramas String
	 * @NativeName: removeUnlockItem
	 * @NativeFunction: void removeUnlockItem(String)
	 */
	public static void removeUnlockItem( String itemName ){
		String[] itemArray = itemName.split( ";" );
		StringBuilder items = new StringBuilder( unlockItem() );

		int index;
		for( String target : itemArray ) {
			if ( ( index = items.indexOf( target ) ) != -1 ) {
				items.delete( index, index + items.indexOf( ";", index ) + 1 );
			}
		}

		put( KEY_UNLOCKITEM, items.toString() );
	}

}
