package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GrudgeSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Random;

public class Grudge extends Mob {
    {
        spriteClass = GrudgeSprite.class;

        HP = HT = 10;
        defenseSkill = 18;

        baseSpeed = 1f;

        EXP = 7;
        maxLvl = 15;

        flying = true;

        loot = Random.oneOf(Generator.Category.WEP_T3,Generator.Category.WEP_T4,Generator.Category.WEP_T5);
        lootChance = 0.25f;

        properties.add(Property.UNDEAD);
    }
    public int damageRoll() {
        return Random.NormalIntRange( 4, 12 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 14;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 0);
    }

    @Override
    public void die( Object cause ) {
        if(enemy != null)
            Buff.prolong(enemy,Haunted.class, Haunted.DURATION);
        super.die( cause );
    }
    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(Grudge.class, "kill"));
    }
    @Override
    public Item createLoot() {
        Dungeon.LimitedDrops.GRUDGE_WEP.count++;
        loot = Random.oneOf(Generator.Category.WEP_T3,Generator.Category.WEP_T4,Generator.Category.WEP_T5);
        if(super.createLoot() instanceof Weapon)
            super.createLoot().getCurse(true);
        return super.createLoot();
    }
    @Override
    public float lootChance() {
        return super.lootChance() * ((float) 1 /(1+Dungeon.LimitedDrops.GRUDGE_WEP.count));
    }
    public static class Haunted extends FlavourBuff {

        public static final float DURATION = 30f;

        {
            type = buffType.NEGATIVE;
            announced = true;
        }

        @Override
        public String icon() {
            return BuffIndicator.TERROR;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }

        //固定降低 30% 命中和闪避，在 Char.hit 中与其它同类效果加法叠加
        @Override
        public float evasionAndAccuracyFactor() {
            return 0.7f;
        }
    }
}
