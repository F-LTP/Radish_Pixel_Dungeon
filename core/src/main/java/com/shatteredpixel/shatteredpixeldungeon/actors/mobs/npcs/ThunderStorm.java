package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfShock;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.OwoSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ThunderStormSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class ThunderStorm extends NPC{
    int hastalk=0;
    {
        spriteClass = ThunderStormSprite.class;

        properties.add(Property.IMMOVABLE);
    }
@Override
public void storeInBundle(Bundle bundle){
        super.storeInBundle(bundle);
        bundle.put("ts_hastalk",hastalk);
}
@Override
public void restoreFromBundle(Bundle bundle){
        super.restoreFromBundle(bundle);
        if (bundle.contains("ts_hastalk"))
            hastalk=bundle.getInt("ts_hastalk");
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
        if (hastalk<4) {
            GLog.h(Messages.get(this, "msg" + hastalk));
            hastalk++;
        }
        else
            GLog.i(Messages.get(this,"loop"));
        if (hastalk==4) {
            Dungeon.level.drop(new StoneOfShock(), c.pos);
            hastalk++;
        }
        return true;
    }
}
