package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GiantWormSprite;
import com.watabou.utils.Random;


public class GiantWorm extends Mob{

    {
        spriteClass = GiantWormSprite.class;

        HP = HT = 10;
        defenseSkill = 1;

        EXP = 1;
        maxLvl = 5;

    }



    public int damageRoll() {
        return Random.NormalIntRange( 1, 3 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 8;
    }

    @Override
    public int attackProc( Char enemy, int damage ) {

        damage = super.attackProc( enemy, damage );

        int healthSteal = 1;
        HP += healthSteal;
        if(HP>HT) Buff.affect(this,Barrier.class).setShield(damage);
        HP = Math.min(HP,HT);
        this.sprite.emitter().burst(Speck.factory(Speck.HEALING), healthSteal);
        this.sprite.showStatus(CharSprite.POSITIVE, "+%dHP", healthSteal);
        return damage;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 2);
    }

    @Override
    public void die( Object cause ) {
        super.die( cause );
    }


    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(GiantWorm.class, "rankings_desc"));
    }


    @Override
    protected boolean act() {
        Buff giantBuff = buff(ChampionEnemy.Giant.class);
        if(giantBuff != null){
           giantBuff.detach();
        }
        return super.act();
    }
}
