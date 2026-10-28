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

package com.shatteredpixel.shatteredpixeldungeon.messages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.I18NBundle;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.challenge.SnakeBiteChallengeManager;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Bundle;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/*
	Simple wrapper class for libGDX I18NBundles.

	The core idea here is that each string resource's key is a combination of the class definition and a local value.
	An object or static method would usually call this with an object/class reference (usually its own) and a local key.
	This means that an object can just ask for "name" rather than, say, "items.weapon.enchantments.death.name"
 */
public class Messages {

    public static final String NO_TEXT_FOUND = "!!!NO TEXT FOUND!!!";
    // Snake Bite challenge: simple exclusion prefixes
    private static final String[] SNAKE_BITE_EXCLUDED_PREFIXES = {
            "items.keys.",
            "items.amulet."
    };
    // Keys that indicate a name-like query (includes potion colors, scroll runes, ring gems)
    private static final Set<String> NAME_KEYS = new HashSet<>(Arrays.asList(
            "name", "color",
            // Potion colors
            "turquoise", "crimson", "azure", "jade", "golden",
            "magenta", "charcoal", "ivory", "amber", "bistre", "indigo", "silver",
            // Scroll runes
            "kaunan", "sowilo", "laguz", "yngvi", "gyfu", "raido",
            "isaz", "mannaz", "naudiz", "berkanan", "odal", "tiwaz",
            // Ring gems
            "garnet", "ruby", "topaz", "emerald", "onyx", "opal",
            "tourmaline", "sapphire", "amethyst", "quartz", "agate", "diamond", "coral", "seed"
    ));
    // Keys that indicate a desc-like query
    private static final Set<String> DESC_KEYS = new HashSet<>(Arrays.asList(
            "desc", "unknown_desc", "stats_desc", "typical_stats_desc", "bmage_desc"
    ));
    //Words which should not be capitalized in title case, mostly prepositions which appear ingame
    //This list is not comprehensive!
    private static final HashSet<String> noCaps = new HashSet<>(
            Arrays.asList("a", "an", "and", "of", "by", "to", "the", "x", "for")
    );
    /**
     * Resource grabbing methods
     */
    public static String errorName;
    private static final String MESSAGE_THEME = "message_theme";
    private static String theme = "";
    // snake_bite 拥有最高优先级
    private static final String THEME_SNAKE_BITE = ".snake_bite";
    // 其余激活主题（按字母顺序），如 DiceMageUI 激活时的 dice_mage
    private static final String THEME_DICE_MAGE = ".dice_mage";
    private static ArrayList<I18NBundle> bundles;
    private static Languages lang;
    private static Locale locale;
    /**
     * Setup Methods
     */

    private static String[] prop_files = new String[]{
            Assets.Messages.ACTORS,
            Assets.Messages.ITEMS,
            Assets.Messages.JOURNAL,
            Assets.Messages.LEVELS,
            Assets.Messages.MISC,
            Assets.Messages.PLANTS,
            Assets.Messages.SCENES,
            Assets.Messages.UI,
            Assets.Messages.WINDOWS,

            Assets.Messages.CUSTOM,
            Assets.Messages.EXPANSION,
            Assets.Messages.TEXT
    };
    private static HashMap<String, DecimalFormat> formatters = new HashMap<>();

    static {
        setup(SPDSettings.language());
    }

    public static Languages lang() {
        return lang;
    }

    public static Locale locale() {
        return locale;
    }

    public static void useTextVariant(String suffix) {
        if (suffix == null || suffix.trim().isEmpty()) {
            theme = "";
        } else {
            theme = suffix.startsWith(".") ? suffix : "." + suffix;
        }
    }

    public static String theme() {
        return theme;
    }

    /**
     * 按优先级返回当前激活的主题后缀列表。
     * useTheme 设置的主题（snake_bite）拥有最高优先级，其余主题按字母顺序排列。
     */
    private static List<String> activeThemes() {
        List<String> result = new ArrayList<>();
        if (!theme.isEmpty()) result.add(theme);
        if (DiceMageUI.active()) result.add(THEME_DICE_MAGE);
        // 未来新增主题在此追加即可
        if (result.size() > 1) {
            Collections.sort(result.subList(1, result.size()));
        }
        return result;
    }

    public static void storeInBundle(Bundle bundle) {
        bundle.put(MESSAGE_THEME, theme);
    }

    public static boolean restoreFromBundle(Bundle bundle) {
        if (!bundle.contains(MESSAGE_THEME)) return false;
        useTextVariant(bundle.getString(MESSAGE_THEME));
        return true;
    }

    public static String findVariant(String key, Object... args) {
        String value = findVariantValue(key);
        if (value == null) value = getFromBundle(key.toLowerCase(Locale.CHINESE));
        if (value == null) return NO_TEXT_FOUND;
        return args.length > 0 ? format(value, args) : value;
    }

    public static void setup(Languages lang) {
        //seeing as missing keys are part of our process, this is faster than throwing an exception
        I18NBundle.setExceptionOnMissingKey(false);

        //store language and locale info for various string logic
        Messages.lang = lang;
        if (lang == Languages.ENGLISH) {
            locale = Locale.ENGLISH;
        } else {
            locale = new Locale(lang.code());
        }

        //strictly match the language code when fetching bundles however
        bundles = new ArrayList<>();
        Locale bundleLocal = new Locale(lang.code());
        for (String file : prop_files) {
            bundles.add(I18NBundle.createBundle(Gdx.files.internal(file), bundleLocal));
        }
    }

    public static String get(String key, Object... args) {
        return get(null, key, args);
    }

    public static String get(Object o, String k, Object... args) {
        return get(o.getClass(), k, args);
    }

    public static String get(Class c, String k, Object... args) {
        return get(c, k, null, args);
    }

    private static String get(Class c, String k, String baseName, Object... args) {
        String key;
        if (c != null) {
            key = c.getName();
            key = key.replace("com.shatteredpixel.shatteredpixeldungeon.", "");
            key += "." + k;
        } else
            key = k;
        
        String keyLower = key.toLowerCase(Locale.CHINESE);
        String localKey = k != null ? k.toLowerCase(Locale.CHINESE) : null;

        String variantValue = findVariantValue(key);
        if (variantValue != null) {
            return args.length > 0 ? format(variantValue, args) : variantValue;
        }

        if (keyLower.startsWith("items.") && localKey != null) {
            if (SnakeBiteChallengeManager.shouldReplaceItemText()) {
                String snakeItemKey = getSnakeBiteItemKey(key, localKey);
                if (snakeItemKey != null) {
                    String snakeValue = getFromBundle(snakeItemKey.toLowerCase(Locale.CHINESE));
                    if (snakeValue != null) {
                        if (args.length > 0) return format(snakeValue, args);
                        else return snakeValue;
                    }
                }
            }
        } else if (keyLower.startsWith("actors.mobs.") && localKey != null
                && SnakeBiteChallengeManager.shouldReplaceMobText()) {
            String snakeMobKey = getSnakeBiteMobKey(key, localKey);
            if (snakeMobKey != null) {
                String snakeValue = getFromBundle(snakeMobKey.toLowerCase(Locale.CHINESE));
                if (snakeValue != null) {
                    if (args.length > 0) return format(snakeValue, args);
                    else return snakeValue;
                }
            }
        }

        String value = getFromBundle(key.toLowerCase(Locale.CHINESE));
        if (value != null) {
            if (args.length > 0) return format(value, args);
            else return value;
        } else {
            //Use baseName so the missing string is clear what exactly needs replacing. Otherwise, it just says java.lang.Object.[key]
            if (baseName == null) {
                baseName = key;
                errorName = baseName;
                baseName = baseName.toLowerCase();
            }
            //this is so child classes can inherit properties from their parents.
            //in cases where text is commonly grabbed as a utility from classes that aren't mean to be instantiated
            //(e.g. flavourbuff.dispTurns()) using .class directly is probably smarter to prevent unnecessary recursive calls.

            if (c != null && c.getSuperclass() != null) {
                return get(c.getSuperclass(), k, baseName, args);
            } else {
                //本地调试+桌面
                if (DeviceCompat.isDebug() && DeviceCompat.isDesktop()) {
                    GLog.i("Ms:" + baseName);
                }
                return NO_TEXT_FOUND;
            }

        }
    }

    /**
     * 检查指定键是否有可用文本（非缺失）。用于皮肤等场景的"可选文本键"判断。
     */
    public static boolean isAvailable(Class c, String k) {
        String key = c.getName();
        key = key.replace("com.shatteredpixel.shatteredpixeldungeon.", "");
        key += "." + k;
        return getFromBundle(key.toLowerCase(Locale.CHINESE)) != null;
    }

    private static String findVariantValue(String key) {
        if (key == null) return null;
        String keyLower = key.toLowerCase(Locale.CHINESE);

        // 按优先级遍历激活主题：snake_bite 最高，其余按字母顺序
        for (String t : activeThemes()) {
            if (t.equals(THEME_SNAKE_BITE)) {
                // snake_bite 只替换它应覆盖的键
                boolean excluded = (keyLower.startsWith("items.") && !SnakeBiteChallengeManager.shouldReplaceItemText())
                        || (keyLower.startsWith("actors.mobs.") && !SnakeBiteChallengeManager.shouldReplaceMobText());
                if (excluded) continue;
            }
            String variant = getFromBundle((key + t).toLowerCase(Locale.CHINESE));
            if (variant != null) return variant;
        }
        return null;
    }

    /**
     * Get transformed key for Snake Bite challenge (items).
     * Returns the snake bite key, or null if no transformation needed.
     */
    private static String getSnakeBiteItemKey(String key, String localKey) {
        String keyLower = key.toLowerCase(Locale.CHINESE);

        // Check exclusions
        for (String excluded : SNAKE_BITE_EXCLUDED_PREFIXES) {
            if (keyLower.startsWith(excluded.toLowerCase())) {
                return null;
            }
        }
        if (keyLower.equals("items.heap.for_sale")) {
            return "items.heap.for_sale.snake_bite";
        }
        // Transform items (except excluded)
        if (keyLower.startsWith("items.") && localKey != null) {
            // Name-like keys
            if (NAME_KEYS.contains(localKey.toLowerCase())) {
                return "items.snake_bite.name";
            }
            // Desc-like keys
            if (DESC_KEYS.contains(localKey.toLowerCase()) || localKey.toLowerCase().endsWith("_desc")) {
                return "items.snake_bite.desc";
            }
            // Action keys
            if (localKey.toLowerCase().startsWith("ac_")) {
                return "items.snake_bite.ac_";
            }
            // All other item text returns empty
            return "items.snake_bite.empty";
        }
        return null;
    }

    public static String getSnakeBiteItemDescription() {
        String value = getFromBundle("items.snake_bite.desc");
        return value != null ? value : NO_TEXT_FOUND;
    }

    /**
     * Get transformed key for Snake Bite challenge (mobs).
     * Returns the snake bite key, or null if no transformation needed.
     */
    private static String getSnakeBiteMobKey(String key, String localKey) {
        String keyLower = key.toLowerCase(Locale.CHINESE);

        // Transform mobs
        if (keyLower.startsWith("actors.mobs.")) {
            if (localKey.equals("name") || localKey.equals("desc")) {
                return "actors.mobs.snake." + localKey;
            }
            // All other mob text returns origin text
            return null;
        }
        return null;
    }

    private static String getFromBundle(String key) {
        String result;
        for (I18NBundle b : bundles) {
            result = b.get(key);
            //if it isn't the return string for no key found, return it
            if (result.length() != key.length() + 6 || !result.contains(key)) {
                return result;
            }
        }
        return null;
    }

    /**
     * String Utility Methods
     */

    public static String format(String format, Object... args) {
        try {
            return String.format(Locale.ENGLISH, format, args);
        } catch (IllegalFormatException e) {
            ShatteredPixelDungeon.reportException(new Exception("formatting error for the string: " + format, e));
            return format;
        }
    }

    public static String decimalFormat(String format, double number) {
        if (!formatters.containsKey(format)) {
            formatters.put(format, new DecimalFormat(format, DecimalFormatSymbols.getInstance(Locale.ENGLISH)));
        }
        return formatters.get(format).format(number);
    }

    public static String capitalize(String str) {
        if (str.length() == 0) return str;
        else return str.substring(0, 1).toUpperCase(locale) + str.substring(1);
    }

    public static String titleCase(String str) {
        //English capitalizes every word except for a few exceptions
        if (lang == Languages.ENGLISH) {
            String result = "";
            //split by any unicode space character
            for (String word : str.split("(?<=\\p{Zs})")) {
                if (noCaps.contains(word.trim().toLowerCase(Locale.ENGLISH).replaceAll(":|[0-9]", ""))) {
                    result += word;
                } else {
                    result += capitalize(word);
                }
            }
            //first character is always capitalized.
            return capitalize(result);
        }

        //Otherwise, use sentence case
        return capitalize(str);
    }

    public static String upperCase(String str) {
        return str.toUpperCase(locale);
    }

    public static String lowerCase(String str) {
        return str.toLowerCase(locale);
    }
}
