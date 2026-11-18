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
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.DM175_Sprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class DM175 extends Mob {

    private boolean onlyShield = false;

    {
        spriteClass = DM175_Sprite.class;

        HP = HT = 20;
        defenseSkill = 14;

        EXP = 7;
        maxLvl = 17;

        properties.add(Property.INORGANIC);
        properties.add(Property.HEADLESS);

        loot = Generator.Category.SCROLL;
        lootChance = 0.1f;
    }


    public int damageRoll() {
        return Random.NormalIntRange( 4, 15 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 16;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 6);
    }

    @Override
    public void die( Object cause ) {
        super.die( cause );
    }
    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(DM175.class, "kill"));
    }

    private static final String STRING = "first";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(STRING, onlyShield);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        onlyShield = bundle.getBoolean(STRING);
    }

    @Override
    protected boolean act() {
        if(!onlyShield  && Dungeon.level.heroFOV[pos]){
            Buff.affect(this, Barrier.class).setShield(60);
            onlyShield = true;
        }
        return super.act();
    }

}
