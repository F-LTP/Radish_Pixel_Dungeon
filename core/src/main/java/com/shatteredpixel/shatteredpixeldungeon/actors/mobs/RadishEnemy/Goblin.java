package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.TheGreatDead;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GoblinSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class Goblin extends Mob {
    {
        spriteClass = GoblinSprite.class;

        HP = HT = 20;
        defenseSkill = 4;

        EXP = 5;
        maxLvl = 10;

        loot = Generator.Category.MIS_T2;
        lootChance = 0.1f;
    }

    private int StoneRemain = 2;
    private boolean canZap = true;
    @Override
    protected boolean canAttack( Char enemy ) {
        return new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos && (canZap || Dungeon.level.adjacent(pos,enemy.pos));
    }
    protected boolean doAttack(Char enemy ) {
        StoneRemain --;
        StoneRemain = Math.max(StoneRemain, 0);
        canZap = StoneRemain > 0;

        if (Dungeon.level.adjacent( pos, enemy.pos )) {

            return super.doAttack( enemy );

        } else{
            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                sprite.zap( enemy.pos );
                return false;
            } else {
                zap();
                return true;
            }
        }
    }

    private void zap() {
        spend(1f);
        Invisibility.dispel(this);
        if (hit(this, enemy, true)) {
            int dmg = Random.NormalIntRange(2, 4);
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
            dmg = defenseProc(enemy,dmg);
            dmg -= enemy.drRoll();
            enemy.damage(dmg, new ThrowingStone());

            if (!enemy.isAlive() && enemy == Dungeon.hero) {
                Dungeon.fail(getClass());
                GLog.n(Messages.get(this, "rock_punk_kill"));
            }
        } else {
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
            Item dropStone = new ThrowingStone();
            Item greatStone = new TheGreatDead();
            if(Dungeon.hero != null){
                if (Dungeon.hero.heroClass == HeroClass.WARRIOR && Random.Int(0,10)>8)
                    Dungeon.level.drop(greatStone,enemy.pos).sprite.drop();
                else
                    Dungeon.level.drop(dropStone,enemy.pos).sprite.drop();
            }
            else
                Dungeon.level.drop(dropStone,enemy.pos).sprite.drop();
        }
    }


    public void onZapComplete() {
        zap();
        next();
    }

    public int damageRoll() {
        return Random.NormalIntRange( 2, 5 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 14;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 3);
    }

    @Override
    public void die( Object cause ) {
        // Random drop
        if(Random.Float(0,10)<5) loot=Generator.Category.MIS_T3;
        super.die( cause );
    }

    @Override
    public String description() {
        String desc = super.description();
        desc += Messages.get(this, "stone_remain", StoneRemain );
        return desc;
    }
    @Override
    public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti) {
        boolean isAttack = super.attack(enemy, dmgMulti, dmgBonus, accMulti);
        if(!enemy.isAlive() && enemy==Dungeon.hero){
            Dungeon.fail(getClass());
            GLog.n('\n'+Messages.get(this, "rankings_desc"));
        }
        return isAttack;
    }
}
