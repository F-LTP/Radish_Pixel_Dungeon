package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 空间碎片：五阶武器。
 * 装备者免疫被动（非自身主动造成的）位移效果；
 * 主动坠入深渊时改为传送至本层随机地点，冷却 50 回合。
 */
public class SpaceFragment extends MeleeWeapon {

    /** 坠渊传送的冷却回合数。 */
    public static final float ABYSS_COOLDOWN = 50f;

    {
        image = ItemSpriteSheet.SPACE_FRAGMENT;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1f;

        tier = 5;
    }

    @Override
    public int min(int lvl) {
        return 10 + 2 * lvl;
    }

    @Override
    public int max(int lvl) {
        return 30 + 5 * lvl;
    }

    @Override
    public int STRReq(int lvl) {
        lvl = Math.max(0, lvl);
        // 基础力量需求 20，随升级按 1/3/6/10... 递减
        return 20 - (int) (Math.sqrt(8 * lvl + 1) - 1) / 2;
    }

    /** 是否由装备者持有（主手或副手）。 */
    public static boolean isWorn(Char ch) {
        if (!(ch instanceof Hero)) return false;
        Hero hero = (Hero) ch;
        return hero.belongings.weapon instanceof SpaceFragment
                || hero.belongings.secondWep instanceof SpaceFragment;
    }

    /** 是否免疫来自外部的位移。 */
    public static boolean blocksPassiveDisplacement(Char ch) {
        return isWorn(ch);
    }

    /**
     * 主动坠入深渊时尝试改为传送。已处理返回 true（调用方不应再执行坠渊）。
     */
    public static boolean tryAbyssTeleport(Hero hero) {
        if (!isWorn(hero)) return false;
        if (hero.buff(AbyssCooldown.class) != null) {
            GLog.w(Messages.get(SpaceFragment.class, "cooldown"));
            return false;
        }
        if (!ScrollOfTeleportation.teleportChar(hero)) {
            return false;
        }
        Buff.affect(hero, AbyssCooldown.class, ABYSS_COOLDOWN);
        Chasm.jumpConfirmed = false;
        GLog.i(Messages.get(SpaceFragment.class, "teleported"));
        return true;
    }

    /** 坠渊传送冷却。 */
    public static class AbyssCooldown extends FlavourBuff {
        {
            type = buffType.NEUTRAL;
        }
    }

    @Override
    public String statsInfo() {
        return Messages.get(this, "stats_desc");
    }
}
