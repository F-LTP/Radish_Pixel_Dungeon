package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.watabou.utils.Bundle;

import java.util.HashSet;

public class VitaeBuff extends Buff{
    private int vitae;
    public int maxVitae;

    public int getVitae(){
        return vitae;
    }

    public void addVitae(int amount){
        int points = Dungeon.hero.pointsInTalent(Talent.VITAE_BOOST);
        int bonus = points <= 2 ? points : 0;
        vitae += amount + bonus;
        maxVitae = vitae;
    }

    /** Explicit assignment for tools and legacy callers. Gameplay grants use addVitae. */
    @Deprecated
    public void setVitae(int amount){
        vitae = 0;
        addVitae(amount);
    }

    /**
     * 维塔吸收：护盾之后的第二层生命池。
     * VITAE_BOOST 4 点的固定减伤在这里结算（饥愤伤害除外）；vitae 清空时本 buff 已 detach，
     * 与旧实现在 Char 里"buff 存在才减伤"的判断一致。
     */
    public int absorbDamage( int dmg, DamageInfo info ){
        if (vitae >= dmg){
            vitae -= dmg;
            dmg = 0;
        } else {
            dmg -= vitae;
            vitae = 0;
        }
        if (vitae == 0){
            detach();
            return dmg;
        }
        if (Dungeon.hero.pointsInTalent(Talent.VITAE_BOOST) >= 4
                && !(info != null && info.getSource() instanceof Hunger)) {
            dmg = Math.max(0, dmg - 2);
        }
        return dmg;
    }

    // DoggingDog on 20250818
    // 免疫在被查询时按持有者当前的天赋点数现算，不能放在初始化块里读 Dungeon.hero：
    // 读档时本 buff 是在英雄还没读完（Dungeon.hero 为 null）的时候被构造的，
    // 初始化块里一抛异常，整个 buff 就被存档读取丢掉了，临时生命读一次档就清零。
    @Override
    public HashSet<Class> immunities() {
        HashSet<Class> result = super.immunities();
        if (target instanceof Hero && ((Hero) target).pointsInTalent(Talent.VITAE_BOOST) >= 3){
            result.add(Weakness.class);
            result.add(Vulnerable.class);
            result.add(Hex.class);
            result.add(Degrade.class);
        }
        return result;
    }

    private static final String VITAE = "vitae";
    private static final String MAX_VITAE = "max_vitae";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( VITAE, vitae);
        bundle.put( MAX_VITAE, maxVitae);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        vitae = bundle.getInt( VITAE );
        // 状态栏的黄色条按 maxVitae 算比例，不存的话读档后是 0，条画不出来。
        // 这个键是后加的，之前的存档里没有，就拿当前剩余量当满值。
        maxVitae = bundle.contains( MAX_VITAE ) ? bundle.getInt( MAX_VITAE ) : vitae;
    }
}
