package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.DemonLordSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Random;

public class DemonLord extends Mob {
    {
        spriteClass = DemonLordSprite.class;

        HP = HT = 120;
        defenseSkill = 30;


        EXP = 14;
        maxLvl = 29;

        properties.add(Property.DEMONIC);

        loot = Random.oneOf(new PotionOfExperience(),new ScrollOfTransmutation(),new StoneOfEnchantment());

        lootChance = 0.3f;
    }

    private int buffCnt = 0;



    @Override
    protected boolean canAttack( Char enemy ) {
        // 相邻位置可以直接攻击
        if (Dungeon.level.adjacent(pos, enemy.pos)) {
            return true;
        }

        // ChampionEnemy 的 extra reach 可能提供更远的攻击距离
        for (ChampionEnemy buff : buffs(ChampionEnemy.class)) {
            if (buff.canAttackWithExtraReach(enemy)) {
                return true;
            }
        }

        // 基础攻击距离 2 格，需要检查弹道联通
        if (Dungeon.level.distance(pos, enemy.pos) <= 2) {
            Ballistica shot = new Ballistica(pos, enemy.pos, Ballistica.STOP_TARGET);
            return shot.collisionPos == enemy.pos;
        }

        return false;
    }

    private double damageBoost(int lv){
        //return lv * 1.15f;
        return Math.pow(1.15,lv)-1;
    }
    @Override
    public String description() {
        String desc = super.description();
        desc += "\n\n";
        desc += Messages.get(this, "damage_boost", (int)(damageBoost(buffCnt)*100) );
        return desc;
    }

    public int damageRoll() {
        buffCnt = 0;
        // accumulate without buff with no icon
        for(Object i:enemy.buffs(Buff.class).toArray()){
            if(!BuffIndicator.NONE.equals(((Buff) i).icon())) buffCnt+=1;
        }
        if(enemy.buff(Light.class)!=null) buffCnt-=1;
        buffCnt = Math.max(buffCnt, 0);
        return (int) (Random.NormalIntRange( 30, 35 ) * (1+damageBoost(buffCnt)));
    }

    @Override
    public int attackSkill( Char target ) {
        return 30;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 10);
    }

    @Override
    public void die( Object cause ) {
        super.die( cause );
    }
    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(DemonLord.class, "kill"));
    }
    @Override
    protected boolean act() {
        return super.act();
    }



    //CRIT METHOD
    protected float critSkill() {
        return critSkill;
    }
    protected float critDamage() {
        return Math.min(critDamage,critDamageCap);
    }

}
