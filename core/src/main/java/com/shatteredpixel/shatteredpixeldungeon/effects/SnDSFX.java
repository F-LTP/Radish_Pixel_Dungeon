package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.ClusteredSkeleton;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Skeleton;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Slime;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * SnD combat sound effect cache with automatic variant discovery.
 * <p>
 * Sound files follow the pattern: {name}_{N}.{ext}
 * E.g., swing_0.wav, swing_1.wav, swing_2.wav
 * <p>
 * Usage:
 * load();                           // Call once during game init
 * get("swing");                     // Returns a random variant path
 * play("bite/biteBig");             // Plays a random biteBig variant
 * play("swing", 0.8f, 1.2f);        // Play with volume and pitch
 * <p>
 * Combat shortcuts (only active when Dice Mage):
 * hit(pitch);                       // Attack hit sound
 * miss();                           // Attack miss sound
 * death();                          // Death sound
 */
public class SnDSFX {

    private static final String BASE_PATH = "sounds/SnD/combat/";

    // Sound variant registry: name -> list of full paths
    private static final Map<String, String[]> variants = new HashMap<>();

    // All registered paths for Sample.load()
    private static final Set<String> allPaths = new HashSet<>();

    // Track if sounds have been loaded
    private static boolean loaded = false;

    // ========== Sound Definitions ==========
    // Format: define("name", "path/to/name", count)
    // The actual files are: path/to/name_0.ext, path/to/name_1.ext, ...

    // Static initialization - register all SnD combat sounds
    static {
        // Root combat sounds
        define("arrowFly", "arrowFly", 1);
        define("arrowWobble", "arrowWobble", 1);
        define("block", "block", 4);
        define("boost", "boost", 3);
        define("clang", "clang", 4);
        define("deboost", "deboost", 3);
        define("heal", "heal", 4);
        define("impact", "impact", 4);
        define("mystic", "mystic", 4);
        define("punch", "punch", 6);
        define("slam", "slam", 3);
        define("slash", "slash", 1);
        define("spike", "spike", 1);
        define("stealth", "stealth", 1);
        define("swing", "swing", 5);
        define("thwack", "thwack", 3);

        // bite/
        define("bite/biteBig", "bite/biteBig", 3);
        define("bite/biteHuge", "bite/biteHuge", 2);
        define("bite/biteReg", "bite/biteReg", 3);
        define("bite/biteSmall", "bite/biteSmall", 4);

        // death/
        define("death/deathAlien", "death/deathAlien", 2);
        define("death/deathBig", "death/deathBig", 4);
        define("death/deathCute", "death/deathCute", 3);
        define("death/deathDemon", "death/deathDemon", 2);
        define("death/deathDragon", "death/deathDragon", 1);
        define("death/deathExplosion", "death/deathExplosion", 1);
        define("death/deathHero", "death/deathHero", 4);
        define("death/deathHorse", "death/deathHorse", 2);
        define("death/deathOof", "death/deathOof", 1);
        define("death/deathPew", "death/deathPew", 4);
        define("death/deathReg", "death/deathReg", 4);
        define("death/deathScream", "death/deathScream", 3);
        define("death/deathSpawn", "death/deathSpawn", 1);
        define("death/deathSqueak", "death/deathSqueak", 3);
        define("death/deathWeird", "death/deathWeird", 2);

        // effect/
        define("effect/chip", "effect/chip", 4);
        define("effect/clink", "effect/clink", 1);
        define("effect/flap", "effect/flap", 2);
        define("effect/flee", "effect/flee", 1);
        define("effect/gong", "effect/gong", 3);
        define("effect/slime", "effect/slime", 1);
        define("effect/wail", "effect/wail", 4);

        // end/
        define("end/defeat", "end/defeat", 1);
        define("end/victory", "end/victory", 1);

        // poison/
        define("poison/poison", "poison/poison", 2);
        define("poison/poisonImpact", "poison/poisonImpact", 3);

        // regen/
        define("regen/regen", "regen/regen", 2);
        define("regen/regenActivate", "regen/regenActivate", 3);

        // slime/
        define("slime/slimeMoveBig", "slime/slimeMoveBig", 2);
        define("slime/slimeMoveHuge", "slime/slimeMoveHuge", 2);
        define("slime/slimeMoveSmall", "slime/slimeMoveSmall", 5);

        // specialSide/
        define("specialSide/bats", "specialSide/bats", 2);
        define("specialSide/fireBreath", "specialSide/fireBreath", 2);
        define("specialSide/onRoll", "specialSide/onRoll", 1);
        define("specialSide/poisonBreath", "specialSide/poisonBreath", 2);
        define("specialSide/resurrect", "specialSide/resurrect", 1);
        define("specialSide/smith", "specialSide/smith", 1);
        define("specialSide/song", "specialSide/song", 1);
        define("specialSide/tribolt", "specialSide/tribolt", 2);
        define("specialSide/undying", "specialSide/undying", 1);
        define("specialSide/whistle", "specialSide/whistle", 1);

        // specialSide/summon/
        define("specialSide/summon/bones", "specialSide/summon/bones", 3);
        define("specialSide/summon/generic", "specialSide/summon/generic", 1);
        define("specialSide/summon/imp", "specialSide/summon/imp", 1);
        define("specialSide/summon/wolf", "specialSide/summon/wolf", 1);

        // spell/
        define("spell/beam", "spell/beam", 3);
        define("spell/fire", "spell/fire", 5);
        define("spell/iceExplode", "spell/iceExplode", 3);
        define("spell/lightning", "spell/lightning", 3);
        define("spell/slice", "spell/slice", 5);

        // surr/
        define("surr/surr", "surr/surr", 1);
    }

    private static void define(String name, String basePath, int count) {
        define(name, basePath, count, ".wav");
    }

    private static void define(String name, String basePath, int count, String ext) {
        String[] paths = new String[count];
        for (int i = 0; i < count; i++) {
            paths[i] = BASE_PATH + basePath + "_" + i + ext;
            allPaths.add(paths[i]);
        }
        variants.put(name, paths);
    }

    /**
     * Load all SnD sounds into Sample cache.
     * Call this once during game initialization.
     */
    public static void load() {
        if (loaded) return;
        // Load new combat sounds
        Sample.INSTANCE.load(allPaths.toArray(new String[0]));
        loaded = true;
    }

    /**
     * Check if Dice Mage audio layer is active.
     */
    public static boolean active() {
        return Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClasses.DICE_MAGE;
    }

    // ========== Core API ==========

    /**
     * Get a random variant path for the given sound name.
     *
     * @param name Sound name without suffix (e.g., "swing" or "bite/biteBig")
     * @return Full path to a random variant, or null if not found
     */
    public static String get(String name) {
        String[] paths = variants.get(name);
        if (paths == null || paths.length == 0) return null;
        return paths[Random.Int(paths.length)];
    }

    /**
     * Play a random variant of the specified sound.
     *
     * @param name Sound name without suffix
     * @return true if sound was played, false if not found
     */
    public static boolean play(String name) {
            return play(name, 1.5f, 1f);
        }

    /**
     * Play a random variant of the specified sound with volume and pitch.
     *
     * @param name   Sound name without suffix
     * @param volume Volume (0-1)
     * @param pitch  Pitch multiplier
     * @return true if sound was played, false if not found
     */
    public static boolean play(String name, float volume, float pitch) {
        String sound = get(name);
        if (sound == null) {
            return false;
        }
        Sample.INSTANCE.play(sound, volume, pitch);
        return true;
    }

    public static void PlaySnDHitSoundVariant(String hitSound) {
        switch (hitSound) {
            case Assets.Sounds.HIT_SLASH:
                play("slash");
                break;
            case Assets.Sounds.HIT_ARROW:
                play("arrowWobble");
                break;
            case Assets.Sounds.HIT:
                play("punch");
                break;
            case Assets.Sounds.HIT_PARRY:
                play("block");
                break;
            case Assets.Sounds.HIT_CRUSH:
                play("impact");
                break;
            case Assets.Sounds.HIT_MAGIC:
                play("spell/lightning");
                break;
            case Assets.Sounds.HIT_STAB:
                play("thwack");
                break;
            case Assets.Sounds.HIT_STRONG:
                play("slam");
        }
    }

    public static void playSnDDeathSoundVariant(Char mob) {
        if (mob instanceof Rat) {
            play("death/deathPew");
            return;
        } else if (Char.hasProp(mob, Char.Property.DEMONIC)) {
            play("death/deathDemon");
            return;
        } else if (mob instanceof Skeleton || mob instanceof ClusteredSkeleton) {
            play("death/bones");
            return;
        } else if (Char.hasProp(mob, Char.Property.BOSS)) {
            play("death/deathBig");
            return;
        } else if (mob instanceof Slime) {
            play("death/deathSpawn");
            return;
        }
        switch (mob.HT / 25) {
            // 血上限每多20的怪 就选择1种死亡音效-
            case 0:
                play("death/deathCute");
                break;
            case 1:
                play("death/deathReg");
                break;
            case 2:
                play("death/deathSqueak");
                break;
            case 3:
                play("death/deathScream");
                break;
            case 4:
                play("death/deathBig");
                break;
            default:
                play("death/deathBig");
                break;
        }
    }

    /**
     * Check if a sound exists.
     */
    public static boolean exists(String name) {
        return variants.containsKey(name);
    }

    /**
     * May this be used in future? The probability is slim...
     */
    public static int variantCount(String name) {
        String[] paths = variants.get(name);
        return paths != null ? paths.length : 0;
    }

    /**
     * Get all registered sound names.
     */
    public static Iterable<String> soundNames() {
        return variants.keySet();
    }

    /**
     * Play cast sound for spells.
     * Only plays if Dice Mage is active.
     */
    public static void cast(String sound) {
        if (!active()) return;
        play(sound, 1f, 1f);
    }
}
