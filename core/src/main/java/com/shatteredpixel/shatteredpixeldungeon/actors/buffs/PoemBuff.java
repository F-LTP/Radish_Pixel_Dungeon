package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 诗 Buff — 对名称和武器或护甲押韵的目标造成额外最终伤害。
 */
public class PoemBuff extends Buff {

	public static final float RHYME_DAMAGE_MULTIPLIER = 1.66f;
	private static final String RHYME_JSON = "poem_rhyme_table_zh.json";

	// 字符→韵母索引（char→rhyme_id）
	private static ObjectIntMap<Character> rhymeMap = null;
	// 韵母列表（rhyme_id→韵母名）
	private static String[] rhymes = null;

	{
		type = buffType.POSITIVE;
	}

	private static void tryLoadRhymeJson() {
		if (rhymeMap != null) return;
		rhymeMap = new ObjectIntMap<>();
		try {
			JsonValue root = new JsonReader().parse(Gdx.files.internal(RHYME_JSON));
			java.util.ArrayList<String> list = new java.util.ArrayList<>();
			for (JsonValue entry = root.child(); entry != null; entry = entry.next()) {
				String rhyme = entry.name();
				list.add(rhyme);
				int id = list.size() - 1;
				for (JsonValue ch = entry.child(); ch != null; ch = ch.next()) {
					String s = ch.asString();
					if (s != null && s.length() == 1) {
						rhymeMap.put(s.charAt(0), id);
					}
				}
			}
			rhymes = list.toArray(new String[0]);
		} catch (Exception e) {
			GLog.w("Failed to load rhyme table: " + e.getMessage());
		}
	}

	public int applyFinalDamage(Hero hero, Char enemy, int damage) {
		if (hero == null || enemy == null || damage <= 0) return damage;

		String targetName = enemy.name();
		String sourceName = rhymingEquipmentName(hero, targetName);
		if (sourceName == null) return damage;

		GLog.p(Messages.get(this, "rhymed", targetName, sourceName));
		return Math.round(damage * RHYME_DAMAGE_MULTIPLIER);
	}

	private String rhymingEquipmentName(Hero hero, String targetName) {
		KindOfWeapon weapon = hero.belongings.attackingWeapon();
		if (weapon != null && rhymes(targetName, weapon.name())) {
			return weapon.name();
		}

		Armor armor = hero.belongings.armor();
		if (armor != null && rhymes(targetName, armor.name())) {
			return armor.name();
		}

		return null;
	}

	private static boolean rhymes(String targetName, String equipmentName) {
		if (targetName == null || equipmentName == null) return false;

		int targetChinese = lastChineseChar(targetName);
		int equipChinese = lastChineseChar(equipmentName);
		if (targetChinese != -1 && equipChinese != -1) {
			String targetRhyme = chineseRhyme(targetChinese);
			String equipRhyme = chineseRhyme(equipChinese);
			if (targetRhyme == null || equipRhyme == null) {
				return targetChinese == equipChinese;
			}
			return targetRhyme.equals(equipRhyme);
		}

		int targetFirst = firstLetterOrDigit(targetName);
		int equipFirst = firstLetterOrDigit(equipmentName);
		return targetFirst != -1 && Character.toLowerCase(targetFirst) == Character.toLowerCase(equipFirst);
	}

	private static int lastChineseChar(String text) {
		for (int i = text.length() - 1; i >= 0; i--) {
			char c = text.charAt(i);
			if (isChinese(c)) return c;
		}
		return -1;
	}

	private static int firstLetterOrDigit(String text) {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (Character.isLetterOrDigit(c)) return c;
		}
		return -1;
	}

	private static boolean isChinese(char c) {
		return c >= '\u4E00' && c <= '\u9FFF';
	}

	private static String chineseRhyme(int c) {
		tryLoadRhymeJson();
		if (rhymeMap == null || rhymes == null) return null;
		int id = rhymeMap.get((char)c, -1);
		return id >= 0 ? rhymes[id] : null;
	}

	@Override
	public String icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((RHYME_DAMAGE_MULTIPLIER - 1) * 100));
	}
}
