package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.watabou.noosa.audio.Music;

/**
 * Slice&Dice-inspired BGM layer for Dice Mage.
 * <p>
 * Imported Slice&Dice assets intentionally live under dedicated asset subfolders:
 * - sounds/snd/*
 * - music/snd/*
 * so they are not mixed with Radish/Shattered's original audio files.
 * <p>
 * Music tracks are organized by dungeon region (5 floors each):
 * - Sewers:  depth 1-5
 * - Prison:  depth 6-10
 * - Caves:   depth 11-15
 * - City:    depth 16-20
 * - Halls:   depth 21-25
 * <p>
 * For combat sound effects, see SnDSFX.java
 */
public class SnDBGM {

    // ========== SEWERS (depth 1-5) ==========
    private static final String[] SEWERS_TRACKS = {
            // 对应SnD地牢
            "music/snd/aleksander/Into The Depths.ogg",
            "music/snd/aleksander/Next Battle Awaits.ogg",
            "music/snd/aleksander/Dicing Opponents.ogg",
            "music/snd/louigi verona/Deadly Encounter.ogg",
            "music/snd/louigi verona/Spellcasters Galore.ogg",
            "music/snd/andrew goodwin/Black Castle.ogg",
            "music/snd/andrew goodwin/The Witches Castle.ogg",
            "music/snd/roho/Swiftsoles v3.ogg",
            "music/snd/ziggurath/Gemstones and Stratagems.ogg",
    };
    private static final float[] SEWERS_CHANCES;

    // ========== PRISON (depth 6-10) ==========
    private static final String[] PRISON_TRACKS = {
            // 对应SnD森林
            "music/snd/aleksander/Dicing Opponents.ogg",
            "music/snd/andrew goodwin/What The Smoke Conceals.ogg",
            "music/snd/cold sanctum/LINGERING DESOLATE GLOOM.ogg",
            "music/snd/louigi verona/Ancient Books of Magic.ogg",
            "music/snd/louigi verona/Bounty Hunters.ogg",
            "music/snd/louigi verona/Deadly Encounter.ogg",
            "music/snd/louigi verona/Spellcasters Galore.ogg",
            "music/snd/louigi verona/War Machines.ogg",
            "music/snd/roho/Swiftsoles v3.ogg",
            "music/snd/ziggurath/Steel Wins Battles.ogg",
            "music/snd/ziggurath/Veteran of 1000 Rolls.ogg"
    };
    private static final float[] PRISON_CHANCES;

    // ========== CAVES (depth 11-15) ==========
    private static final String[] CAVES_TRACKS = {
            // 对应Snd巢穴
            "music/snd/aleksander/Into The Depths.ogg",
            "music/snd/aleksander/Next Battle Awaits.ogg",
            "music/snd/cold sanctum/CONJURING SINISTER WIZARDRY.ogg",
            "music/snd/louigi verona/Dark Enchantments.ogg",
            "music/snd/andrew goodwin/Black Castle.ogg",
            "music/snd/andrew goodwin/Withering Thoughts.ogg",
            "music/snd/andrew goodwin/No Turning Back.ogg",
            "music/snd/andrew goodwin/The Witches Castle.ogg",
            "music/snd/ziggurath/Assassins_ Dirge.ogg",
    };
    private static final float[] CAVES_CHANCES;

    // ========== CITY (depth 16-20) ==========
    private static final String[] CITY_TRACKS = {
            // 对应SnD坟墓
            "music/snd/aleksander/Defense Ready.ogg",
            "music/snd/aleksander/Next Battle Awaits.ogg",
            "music/snd/louigi verona/Dark Enchantments.ogg",
            "music/snd/louigi verona/Bounty Hunters.ogg",
            "music/snd/andrew goodwin/What The Smoke Conceals.ogg",
            "music/snd/andrew goodwin/Withering Thoughts.ogg",
            "music/snd/andrew goodwin/No Turning Back.ogg",
            "music/snd/roho/Swiftsoles v3.ogg",
            "music/snd/ziggurath/Assassins_ Dirge.ogg",
            "music/snd/ziggurath/Gemstones and Stratagems.ogg",
            "music/snd/ziggurath/Steel Wins Battles.ogg",
            "music/snd/ziggurath/Veteran of 1000 Rolls.ogg"
    };
    private static final float[] CITY_CHANCES;

    // ========== HALLS (depth 21-25) ==========
    private static final String[] HALLS_TRACKS = {
            // 对应SnD深渊
            "music/snd/aleksander/Defense Ready.ogg",
            "music/snd/aleksander/Into The Depths.ogg",
            "music/snd/cold sanctum/CONJURING SINISTER WIZARDRY.ogg",
            "music/snd/louigi verona/Dark Enchantments.ogg",
            "music/snd/louigi verona/War Machines.ogg",
            "music/snd/andrew goodwin/Black Castle.ogg",
    };
    private static final float[] HALLS_CHANCES;

    // 静态初始化概率数组（默认所有曲目等概率）
    static {
        SEWERS_CHANCES = initChances(SEWERS_TRACKS);
        PRISON_CHANCES = initChances(PRISON_TRACKS);
        CAVES_CHANCES = initChances(CAVES_TRACKS);
        CITY_CHANCES = initChances(CITY_TRACKS);
        HALLS_CHANCES = initChances(HALLS_TRACKS);
    }

    private static float[] initChances(String[] tracks) {
        float[] chances = new float[tracks.length];
        for (int i = 0; i < chances.length; i++) {
            chances[i] = 1.0f;
        }
        return chances;
    }

    public static boolean active() {
        return Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClasses.DICE_MAGE;
    }

    /**
     * Load BGM and SFX for Dice Mage.
     */
    public static void load() {
        SnDSFX.load();
    }

    /**
     * 根据当前层数播放对应区域的背景音乐。
     *
     * @return true 如果 Dice Mage 激活并播放了音乐，否则false并且什么都不做
     */
    public static boolean playLevelMusic() {
        if (!active()) return false;

        int depth = Dungeon.depth;
        String[] tracks;
        float[] chances;

        if (depth >= 1 && depth <= 5) {
            tracks = SEWERS_TRACKS;
            chances = SEWERS_CHANCES;
        } else if (depth >= 6 && depth <= 10) {
            tracks = PRISON_TRACKS;
            chances = PRISON_CHANCES;
        } else if (depth >= 11 && depth <= 15) {
            tracks = CAVES_TRACKS;
            chances = CAVES_CHANCES;
        } else if (depth >= 16 && depth <= 20) {
            tracks = CITY_TRACKS;
            chances = CITY_CHANCES;
        } else if (depth >= 21 && depth <= 25) {
            tracks = HALLS_TRACKS;
            chances = HALLS_CHANCES;
        } else {
            tracks = SEWERS_TRACKS;
            chances = SEWERS_CHANCES;
        }

        if (tracks.length == 0) {
            return true;
        }

        Music.INSTANCE.playTracks(tracks, chances, true);
        Music.INSTANCE.volume(1.5f);
        return true;
    }
}