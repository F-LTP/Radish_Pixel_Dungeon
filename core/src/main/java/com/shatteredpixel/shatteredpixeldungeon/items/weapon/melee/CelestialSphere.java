package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class CelestialSphere extends MeleeWeapon {

    {
        image = ItemSpriteSheet.SKYSPS;
        tier = 4;

        // See Weapon.java ReachFactor()
        RCH = 1;
    }

    @Override
    public int STRReq(int lvl) {
        return (7 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    	public int min(int lvl) {
    		return 3 + lvl;
    	}
    @Override
    	public int max(int lvl) {
    		return 12 + lvl * 3;
    	}

    @Override
    public int damageRoll(Char owner) {
        int damage = super.damageRoll(owner);
        // 法杖加成：按持有者 wandLevel() 结算——Hero 累加装备法杖，怪物无法杖为 0，法杖型远程怪可覆写为随机数值
        int wandTotalLevel = owner.wandLevel();
        if (wandTotalLevel > 0) {
            damage += Random.NormalIntRange(wandTotalLevel, wandTotalLevel * 2);
        }
        return damage;
    }

    /**
     * 天球仪打的是魔法来源的伤害：来源归一化为闪电判定（与减伤、图标共用同一份名单），
     * 这样既不会额外造成一次物理伤害，也不需要在承伤管线里对武器类型做特判。
     */
    @Override
    public void modifyDamageInfo(Char attacker, Char defender, DamageInfo info) {
        info.setSource(new DM100.LightningBolt());
    }

    @Override
    public boolean ignoresDefenseRoll() {
        return true;
    }
}
