package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.BladeRainSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.BlazeSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.BandageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.BurnSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.CutSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.DrainSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.HexSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.ExecuteSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.FlickSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.GatherSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.HackSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.HemlockSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.InfuseSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.LacerationSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.LightPokeSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.LightningSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.MendSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.MiasmaSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.ProphecySpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.ReapSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.RefreshSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.ScaldSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.ScorchSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.SootheSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.StarfireSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.SurgerySpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.VineSpell;

/**
 * 骰子法师学派。每个学派对应一个3点天赋。
 * 学派投入 N 点时只可使用该学派列表中的第 N 个法术（前序法术不可用）。
 */
public enum DiceMageSchool {

    FIRE(Talent.SCHOOL_FIRE, 1f, new Class[]{ScorchSpell.class, ScaldSpell.class, BlazeSpell.class}),
    BLADES(Talent.SCHOOL_BLADES, 1f, new Class[]{CutSpell.class, HackSpell.class, BladeRainSpell.class}),
    CONJURATION(Talent.SCHOOL_CONJURATION, 0.2f, new Class[]{ExecuteSpell.class, LightningSpell.class, HexSpell.class}),
    MANA(Talent.SCHOOL_MANA, 0.333f, new Class[]{GatherSpell.class, ProphecySpell.class, StarfireSpell.class}),
    BLOOD(Talent.SCHOOL_BLOOD, 1f, new Class[]{LacerationSpell.class, BurnSpell.class, DrainSpell.class}),
    NATURE(Talent.SCHOOL_NATURE, 1f, new Class[]{HemlockSpell.class, VineSpell.class, MiasmaSpell.class}),
    MEDICAL(Talent.SCHOOL_MEDICAL, 1f, new Class[]{BandageSpell.class, InfuseSpell.class, SootheSpell.class}),
    PHYSICAL(Talent.SCHOOL_PHYSICAL, 1f, new Class[]{LightPokeSpell.class, FlickSpell.class, ReapSpell.class}),
    EMERGENCY(Talent.SCHOOL_EMERGENCY, 1f, new Class[]{MendSpell.class, RefreshSpell.class, SurgerySpell.class}),
    SPECIAL(Talent.SCHOOL_SPECIAL, 0.1f, null); // 每局随机3个法术

    public final Talent talent;
    public final float weight;
    public final Class<? extends DiceMageSpell>[] spells;

    DiceMageSchool(Talent talent, float weight, Class<? extends DiceMageSpell>[] spells) {
        this.talent = talent;
        this.weight = weight;
        this.spells = spells;
    }
}
