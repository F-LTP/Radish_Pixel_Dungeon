package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class CircleSword extends MeleeWeapon {

    {
        image = ItemSpriteSheet.CIRCLE_SWORD;
        tier = 4;
        RCH = 2;    //extra reach
    }


    @Override
    public int proc(Char attacker, Char defender, int damage) {
		int shieldDamage = 0;
		for (ShieldBuff shield : attacker.buffs(ShieldBuff.class)) {
			shieldDamage += shield.shielding();
			shield.detach();
		}
		damage += shieldDamage;
        new Bomb().explodeMobs(defender.pos, (int) (damage/4f+damage*(0.05f*level())));
        return super.proc(attacker, defender, damage);
    }

    @Override
    public int damageRoll(Char owner) {
        // 基础伤害 + 持有者护甲/树肤转化为攻击（玩家与怪物行为一致，且这些防御不再生效）
        int dmg = augment.damageFactor(Char.combatRoll(8 + buffedLvl(), 20 + buffedLvl() * 5));

        if (owner instanceof Hero) {
            int exStr = ((Hero) owner).STR() - STRReq();
            if (exStr > 0) {
                dmg += Char.combatRoll(0, exStr);
            }
        }

        Armor arm = (Armor) Char.defendingArmor(owner);
        if (arm != null) {
            dmg += Char.combatRoll(arm.DRMin(), arm.DRMax());
        }
        dmg += Char.combatRoll(0, Barkskin.currentLevel(owner));
        return dmg;
    }

    @Override
    public String statsInfo(){
        if(Dungeon.hero != null){
            if (isEquipped(Dungeon.hero) && Dungeon.hero.belongings.armor() != null){
                return Messages.get(this, "stats_desc2",Dungeon.hero.belongings.armor().DRMin(),Dungeon.hero.belongings.armor().DRMax());
            }
        }
        return Messages.get(this, "stats_desc");
    }


    @Override
    public int min(int lvl) {
        if(Dungeon.hero != null && Dungeon.hero.belongings.armor() != null && isEquipped(Dungeon.hero)){
            return 8 + Dungeon.hero.belongings.armor().DRMin() + lvl;
        } else {
            return 8  + lvl;
        }
    }

    @Override
    public int max(int lvl) {
        if(Dungeon.hero != null && Dungeon.hero.belongings.armor() != null && isEquipped(Dungeon.hero)){
            return 20 + Dungeon.hero.belongings.armor().DRMax() + lvl * 4;
        } else {
            return 20 + lvl * 5;
        }
    }

}
