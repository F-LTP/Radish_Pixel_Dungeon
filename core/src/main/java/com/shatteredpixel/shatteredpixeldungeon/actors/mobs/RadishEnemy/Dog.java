package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.DogLeg;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.DogSprite;
import com.watabou.utils.Random;

public class Dog extends Mob {
    {
        spriteClass = DogSprite.class;

        HP = HT = 15;
        defenseSkill = 4;

        baseSpeed = 1.5f;

        EXP = 4;
        maxLvl = 10;

        loot = new MysteryMeat();
        lootChance = 0.25f;

    }

    protected int loyalty = 0;


    public int damageRoll() {
        for(Mob mob:Dungeon.level.mobs){
            if(Dungeon.level.adjacent(pos,mob.pos) && mob!=this)
                loyalty++;
        }
        int DamageBoost = loyalty;
        loyalty = 0;
        return Random.NormalIntRange( 2, 5 )+DamageBoost;
    }

    @Override
    public int attackSkill( Char target ) {
        return 12;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 2);
    }

    @Override
    public void die( Object cause ) {
        // 狗腿 15% 掉落
        if(Random.Float() < 0.15f){
            Dungeon.level.drop(new DogLeg(), pos).sprite.drop();
        }
        super.die( cause );
    }
    @Override
    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(Dog.class, "rankings_desc"));
    }
}
