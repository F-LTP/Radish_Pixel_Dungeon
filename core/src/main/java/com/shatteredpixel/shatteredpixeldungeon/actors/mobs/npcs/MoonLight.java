package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfShock;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BoatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MoonLightSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ThunderStormSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class MoonLight extends NPC{
    int hastalk=0;
    {
        spriteClass = MoonLightSprite.class;

        properties.add(Property.IMMOVABLE);
    }
    @Override
    public void storeInBundle(Bundle bundle){
        super.storeInBundle(bundle);
        bundle.put("ml_hastalk",hastalk);
    }
    @Override
    public void restoreFromBundle(Bundle bundle){
        super.restoreFromBundle(bundle);
        if (bundle.contains("ml_hastalk"))
            hastalk=bundle.getInt("ml_hastalk");
    }
    @Override
    public int defenseSkill( Char enemy ) {
        return INFINITE_EVASION;
    }

    @Override
    public void damage( int dmg, Object src ) {
    }

    @Override
    public void add( Buff buff ) {
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
        if (hastalk<2) {
            GLog.i(Messages.get(this, "msg" + hastalk));
            hastalk++;
        }
        else if (hastalk==2){
            GLog.p(Messages.get(this, "msg" + hastalk));
            hastalk++;
        }else
            GLog.i(Messages.get(this,"loop"));
        if (hastalk==3) {
            Generator.Category ca = Generator.Category.WEP_T1;
            MeleeWeapon w = (MeleeWeapon) Reflection.newInstance(ca.classes[Random.chances(ca.probs)]);
            w.cursed=false;
            w.level(0);
            Dungeon.level.drop(w, c.pos);
            hastalk++;
        }
        return true;
    }

}
