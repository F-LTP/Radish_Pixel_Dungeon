package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.RoyalGuardSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class RoyalGuard extends Mob {
    {
        spriteClass = RoyalGuardSprite.class;

        HP = HT = 80;
        defenseSkill = 30;


        EXP = 10;
        maxLvl = 21;

        properties.add(Property.UNDEAD);

        loot = Generator.Category.WEP_T4;
        lootChance = 0.2f;
    }
    public Weapon equipment() {
        return (Weapon) mobEquipment.weapon;
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        if (mobEquipment.weapon == null) {
            mobEquipment.weapon = (Weapon) Generator.randomUsingDefaults(Random.oneOf(Generator.Category.WEP_T4, Generator.Category.WEP_T5));
        }
    }

    public RoyalGuard(){
        super();
        mobEquipment.weapon = (Weapon) Generator.randomUsingDefaults(Random.oneOf(Generator.Category.WEP_T4, Generator.Category.WEP_T5));
    }

    @Override
    public String description() {
        String desc = super.description();
        desc += "\n\n";
        desc += Messages.get(this, "equipment", equipment().name() );
        return desc;
    }
    @Override
    public Item createLoot() {
        Dungeon.LimitedDrops.ROYALGUARD_WEP.count++;
        int Ug = Random.Int(0,100)<50?0:(Random.Int(0,50)<40?1:2);
        if(equipment() instanceof MeleeWeapon)
            loot = equipment().upgrade(Ug);
        return super.createLoot();
    }
    @Override
    public float lootChance() {
        return (float) (super.lootChance() * ((float) 1 /(Math.pow(2, Dungeon.LimitedDrops.ROYALGUARD_WEP.count))));
    }

    @Override
    public int attackSkill( Char target ) {
        int accuracy = 30;
        if(equipment() instanceof MeleeWeapon){
            accuracy *= equipment().accuracyFactor( this, target );
        }
        return accuracy;
    }

    @Override
    public int drRoll() {
        if (wieldsCircleSword()) return 0;
        return Random.NormalIntRange(1, 8) + equipment().defenseFactor(this);
    }

    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(RoyalGuard.class, "kill", equipment().name()));
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        damage = super.attackProc( enemy, damage );
        if (!enemy.isAlive() && enemy == Dungeon.hero){
            Dungeon.fail(getClass());
            GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name())) );
        }
        return damage;
    }


}
