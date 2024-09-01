package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.OwoSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class Owo extends NPC{

    {
        spriteClass = OwoSprite.class;

        properties.add(Property.IMMOVABLE);
    }

    @Override
    public int defenseSkill( Char enemy ) {
        return INFINITE_EVASION;
    }

    @Override
    public void damage( int dmg, Object src ) {
    }

    @Override
    public boolean add( Buff buff ) {
        return false;
    }

    @Override
    public boolean reset() {
        return true;
    }
    @Override
    public boolean interact(Char c){
        sprite.turnTo(pos,c.pos);

        if (!(c instanceof Hero)){
            return true;
        }
        GLog.i(Messages.get(this,"msg"));
        sprite.operate(pos);
        return true;
    }

}
