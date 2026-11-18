package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.utils.LegacyItemRing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Sprouted_Potato;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.CircleSword;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

// 传承·转化物β
// init by DoggingDog on 20250914
public class InversionBeta extends LegacyItemRing {
    {
        icon = ItemSpriteSheet.Icons.RING_ACCURACY;
    }

    public static int betaHP(Hero hero){
        int dr = 0;
        dr += hero.combatRoll( 0 , Barkskin.currentLevel(hero) );
        if (hero.hasTalent(Talent.HOLD_FAST)){
            int drBouns = Random.NormalIntRange(0, 2* hero.pointsInTalent(Talent.HOLD_FAST));
            if(hero.buff(Chill.class) != null || hero.buff(Frost.class) != null || hero.buff(Slow.class) != null || hero.buff(Roots.class) != null || hero.buff(Paralysis.class) != null || hero.buff(Cripple.class) != null){
                dr += drBouns * 3;
            }else{
                dr += drBouns;
            }
        }

        if (hero.hasTalent(Talent.MOVING_DEFENSE) && hero.pointsInTalent(Talent.MOVING_DEFENSE)>3)
            if (hero.shielding()>0)
                dr+=Random.NormalIntRange(2,8);

        if(hero.belongings.weapon() instanceof CircleSword){
            dr = 0;
        } else if (hero.belongings.armor() != null) {
            int armDr = Char.combatRoll( hero.belongings.armor().DRMin(), hero.belongings.armor().DRMax());
            if (hero.STR() < hero.belongings.armor().STRReq()){
                armDr -= 2*(hero.belongings.armor().STRReq() - hero.STR());
            }
            if (armDr > 0) dr += armDr;
        }
        if (hero.belongings.weapon() != null)  {
            int wepDr = Char.combatRoll( 0 , hero.belongings.weapon().defenseFactor( hero ) );
            if (hero.STR() < ((Weapon)hero.belongings.weapon()).STRReq()){
                wepDr -= 2*(((Weapon)hero.belongings.weapon()).STRReq() - hero.STR());
            }
            if (wepDr > 0) dr += wepDr;
        }

        return dr;
    }

    public static int betaDR(Hero hero){
        int HT = 0;
        HT = 20 + 5*(hero.lvl-1) + hero.HTBoost;
        float multiplier = RingOfMight.HTMultiplier(hero);
        HT = Math.round(multiplier * HT);


        if (hero.buff(ElixirOfMight.HTBoost.class) != null){
            HT += hero.buff(ElixirOfMight.HTBoost.class).boost();
        }

        if (hero.buff(Sprouted_Potato.Potato_Poison.class) != null){
            HT -= hero.buff(Sprouted_Potato.Potato_Poison.class).level();
        }

        return HT;
    }

    @Override
    protected RingBuff buff( ) {
        return new Inversion();
    }

    public class Inversion extends RingBuff {
        @Override
        public void detach() {
            super.detach();
            if(Dungeon.hero != null){
                Dungeon.hero.updateHT(false);
            }
        }

    }
}
