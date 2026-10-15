package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.LawFragment;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.TorturerSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class Torturer extends Mob {
    {
        spriteClass = TorturerSprite.class;

        HP = HT = 40;
        defenseSkill = 5;

        baseSpeed = 1f;

        EXP = 5;
        maxLvl = 12;

        properties.add(Property.UNDEAD);
        properties.add(Property.JAIL_INHABITANT);

        loot = Gold.class;
        lootChance = 0.5f;
    }
    public int damageRoll() {
        return Random.NormalIntRange( 5, 5 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 12;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(2, 2);
    }

    @Override
    public void die( Object cause ) {
        // 律法残页 20% 掉落
        if(Random.Float() < 0.2f){
            Dungeon.level.drop(new LawFragment(), pos).sprite.drop();
        }
        super.die( cause );
    }
    @Override
    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(Torturer.class, "kill"));
    }
    @Override
    public Item createLoot() {
        return super.createLoot().quantity(Random.Int(100,200));
    }
    @Override
    protected boolean canAttack( Char enemy ) {
//        Ballistica chain = new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE);
//        int newPos = -1;
//        for (int i : chain.subPath(1, chain.dist)){
//            if (!Dungeon.level.solid[i] && Actor.findChar(i) == null){
//                newPos = i;
//                break;
//            }
//        }
//        if(chain.collisionPos == enemy.pos && Dungeon.level.distance(pos,enemy.pos)<=2){
//            Sample.INSTANCE.play(Assets.Sounds.CHAINS);
//            sprite.parent.add(new Chains(sprite.center(),
//                    enemy.sprite.destinationCenter(),
//                    Effects.Type.CHAIN,
//                    new Callback() {
//                        public void call() {
//                            next();
//                        }
//                    }));
//
//        }
//        return chain.collisionPos == enemy.pos && Dungeon.level.distance(pos,enemy.pos)<=2;
        return Dungeon.level.distance(pos,enemy.pos)<=2;
    }
}
