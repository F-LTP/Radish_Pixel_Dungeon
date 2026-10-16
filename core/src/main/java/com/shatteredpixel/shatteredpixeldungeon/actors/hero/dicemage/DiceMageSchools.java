package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.AbyssSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.BurstSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.ChargeSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.HeatSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.InfinitySpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.LightBeamSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.MarkSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.ShineSpell;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * 学派辅助工具：特殊学派的随机法术分配、候选学派按权重抽取等。
 */
public class DiceMageSchools {

    /** 特殊学派候选池（每局从中随机3个）。深渊/无限权重为1/20。 */
    private static final Class<? extends DiceMageSpell>[] SPECIAL_POOL = new Class[]{
            LightBeamSpell.class, MarkSpell.class, ChargeSpell.class, HeatSpell.class, ShineSpell.class,
            AbyssSpell.class, InfinitySpell.class
    };
    private static final float[] SPECIAL_WEIGHTS = new float[]{1f, 1f, 1f, 1f, 1f, 0.05f, 0.05f};

    /** 若特殊学派本局尚未生成，则从候选池加权随机3个法术并保存。 */
    public static void ensureSpecialRolled(MagicPoint mp) {
        if (mp == null || mp.specialSpells() != null) return;
        @SuppressWarnings("unchecked")
        Class<? extends DiceMageSpell>[] picked = new Class[3];
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < SPECIAL_POOL.length; i++) indices.add(i);
        for (int slot = 0; slot < 3; slot++) {
            int idx = weightedPick(indices);
            picked[slot] = SPECIAL_POOL[idx];
            indices.remove((Integer) idx);
        }
        mp.setSpecialSpells(picked);
    }

    private static int weightedPick(List<Integer> indices) {
        float total = 0f;
        for (int i : indices) total += SPECIAL_WEIGHTS[i];
        float roll = Random.Float(total);
        float acc = 0f;
        for (int i : indices) {
            acc += SPECIAL_WEIGHTS[i];
            if (roll <= acc) return i;
        }
        return indices.get(indices.size() - 1);
    }

    /** 返回特殊学派某法术本局被分配到的等级位(1..3)，未分配返回0。 */
    public static int specialSlot(Class<? extends DiceMageSpell> spellClass) {
        MagicPoint mp = MagicPoint.inst();
        if (mp == null || mp.specialSpells() == null) return 0;
        Class<? extends DiceMageSpell>[] arr = mp.specialSpells();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null && arr[i] == spellClass) return i + 1;
        }
        return 0;
    }

    /** 返回某学派在指定等级位的法术实例；无该等级法术返回 null。 */
    public static DiceMageSpell spellForLevel(DiceMageSchool school, int level) {
        if (level < 1 || level > 3) return null;
        if (school == DiceMageSchool.SPECIAL) {
            MagicPoint mp = MagicPoint.inst();
            if (mp == null) return null;
            ensureSpecialRolled(mp);
            Class<? extends DiceMageSpell>[] arr = mp.specialSpells();
            if (arr == null || level > arr.length || arr[level - 1] == null) return null;
            try {
                return arr[level - 1].newInstance();
            } catch (Exception e) {
                return null;
            }
        }
        if (school.spells == null || level > school.spells.length || school.spells[level - 1] == null) return null;
        try {
            return school.spells[level - 1].newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    /** 根据学派天赋 Talent 反查对应学派；非学派返回 null。 */
    public static DiceMageSchool schoolOf(Talent talent) {
        for (DiceMageSchool s : DiceMageSchool.values()) {
            if (s.talent == talent) return s;
        }
        return null;
    }

    /**
     * 学派天赋贴图：显示"即将解锁"的法术（当前投入点+1）的 SND 贴图名；
     * 若已升满则展示满级（第3个）法术。找不到时返回 null，调用方回退到默认图标。
     */
    public static String nextSpellSndName(Talent schoolTalent, int currentPoints) {
        DiceMageSchool school = schoolOf(schoolTalent);
        if (school == null) return null;
        int level = Math.min(3, currentPoints + 1);
        DiceMageSpell spell = spellForLevel(school, level);
        return spell != null ? spell.sndImageName() : null;
    }

    /**
     * 按顺序返回所有法术类：固定"迸发" + 各固定学派（按学派、按等级）+ 特殊学派候选池。
     * 用于图鉴/列表展示全部法术。
     */
    public static List<Class<? extends DiceMageSpell>> allSpells() {
        List<Class<? extends DiceMageSpell>> list = new ArrayList<>();
        list.add(BurstSpell.class);
        for (DiceMageSchool school : DiceMageSchool.values()) {
            if (school == DiceMageSchool.SPECIAL) continue;
            if (school.spells != null) {
                for (Class<? extends DiceMageSpell> s : school.spells) {
                    if (s != null) list.add(s);
                }
            }
        }
        for (Class<? extends DiceMageSpell> s : SPECIAL_POOL) {
            list.add(s);
        }
        return list;
    }

    /** 某个学派是否还能投入天赋（未满3点）。 */
    public static boolean canInvest(DiceMageSchool school) {
        return school.talent.maxPoints() > 0
                && com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.pointsInTalent(school.talent) < 3;
    }
}
