package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.custom.buffs.SpellVulnerable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

/**
 * 颠茄：三阶长柄武器，攻击有概率使目标陷入“法术易伤”。
 */
public class Belladonna extends MeleeWeapon {

    {
        image = ItemSpriteSheet.BELLADONNA;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;

        tier = 3;
        RCH = 2;
    }

    @Override
    public int min(int lvl) {
        return 2 + lvl;
    }

    @Override
    public int max(int lvl) {
        return 14 + 3 * lvl;
    }

    /** 施加法术易伤的概率，最高 100%。 */
    public static float procChance(int lvl) {
        return Math.min(1f, 0.5f + 0.17f * lvl);
    }

    /** 法术易伤持续回合数。 */
    public static float procDuration(int lvl) {
        return 4f + 1.5f * lvl;
    }

    /** 施加法术易伤时的承伤倍率，每等级 ×1.05。 */
    public static float procFactor(int lvl) {
        return SpellVulnerable.DAMAGE_FACTOR * (float) Math.pow(1.05f, lvl);
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (Random.Float() < procChance(buffedLvl())) {
            SpellVulnerable sv = Buff.affect(defender, SpellVulnerable.class, procDuration(buffedLvl()));
            sv.setFactor(procFactor(buffedLvl()));
        }
        return super.proc(attacker, defender, damage);
    }

    @Override
    public String statsInfo() {
        if (Dungeon.hero != null) {
            return Messages.get(this, "stats_desc",
                    Math.round(procChance(buffedLvl()) * 100f),
                    Math.round((procFactor(buffedLvl()) - 1f) * 100f),
                    procDuration(buffedLvl()));
        }
        return Messages.get(this, "stats_desc", 50, 33, 4f);
    }
}
