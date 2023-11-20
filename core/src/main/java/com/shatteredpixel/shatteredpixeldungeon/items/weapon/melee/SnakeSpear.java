package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class SnakeSpear extends MeleeWeapon{
    {
        image = ItemSpriteSheet.SNAKE_SPEAR;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;

        tier = 3;
        RCH = 3;    //lots of extra reach
        DLY=1.2f;
    }

    @Override
    public int max(int lvl) {
        return  4*(tier+1) +    //12 base, down from 20
                lvl*(tier);     //+3 per level, down from +4
    }
    @Override
    public int damageRoll(Char owner) {
        int dmg=super.damageRoll(owner);
        int toget=4;
        if (owner instanceof Hero){
            if (((Hero) owner).belongings.armor!=null){
                toget--;
            }
            if (((Hero) owner).belongings.ring!=null){
                toget--;
            }
            if (((Hero) owner).belongings.misc!=null){
                toget--;
            }
            if (((Hero) owner).belongings.artifact!=null){
                toget--;
            }
        }else if (owner instanceof ArmoredStatue){
            if (((ArmoredStatue) owner).armor()!=null){
                toget--;
            }
        }else if (owner instanceof DriedRose.GhostHero){
            if (((DriedRose.GhostHero) owner).armor() !=null){
                toget--;
            }
        }
        dmg +=Random.IntRange(0,(3+buffedLvl())*toget);
        return dmg;
    }
    @Override
    public String statsInfo(){
        if (isEquipped(Dungeon.hero)){
            int toget=4;
            Hero owner=Dungeon.hero;
            if (owner.belongings.armor!=null){
                toget--;
            }
            if (owner.belongings.ring!=null){
                toget--;
            }
            if (owner.belongings.misc!=null){
                toget--;
            }
            if (owner.belongings.artifact!=null){
                toget--;
            }
            return Messages.get(this, "stats_desc2",(3+buffedLvl())*toget);
        }
        return Messages.get(this, "stats_desc");
    }
}
