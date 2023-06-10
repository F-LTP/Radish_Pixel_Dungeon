package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Glasssword extends MeleeWeapon{
    private int ruin=0;
    private int ruinCap=8;
    private static ItemSprite.Glowing TEAL = new ItemSprite.Glowing( 0x88EEFF );
    private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing( 0x000000 );
    {
        image = ItemSpriteSheet.SHORTSWORD;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1.1f;

        tier = 2;
    }
    @Override
    public int min(int lvl){
        return 4+lvl;
    }
    @Override
    public int max(int lvl) {
        return  6*(tier+1) +        //16 base, up from 15
                lvl*(tier+1)-
                ruin;
    }
    @Override
    public int proc(Char attacker, Char defender, int damage){
        if (ruin<ruinCap && Random.Float()*100<10-buffedLvl()) {
            ruin++;
            GLog.w(Messages.get(this,"broken"));
            if(ruin==ruinCap)
                GLog.n(Messages.get(this,"ruin"));
        }
        return super.proc(attacker,defender,damage);
    }
    @Override
    public String statsInfo() {
        String d=Messages.get(this, "stats_desc");
        if (buffedLvl()>=10) d+=Messages.get(this,"stable");
        return d;
    }
    private static final String RUIN = "ruin";
    private static final String RUINCAP = "ruincap";
    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        ruin = bundle.getInt(RUIN);
        ruinCap = bundle.getInt(RUINCAP);
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put(RUIN, ruin);
        bundle.put(RUINCAP, ruinCap);
    }
    @Override
    public ItemSprite.Glowing glowing() {
        if (ruin<3)
        return TEAL;
        else if (ruin>3)return BLACK;
        else return null;
    }
}
