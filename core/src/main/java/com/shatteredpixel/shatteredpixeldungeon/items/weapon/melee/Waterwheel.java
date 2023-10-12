package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class Waterwheel extends MeleeWeapon{
    //private static ItemSprite.Glowing BLUE = new ItemSprite.Glowing( 0x0000FF );
    {
        image = ItemSpriteSheet.WATERWHEEL;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 1f;

        tier = 3;

    }
    @Override
    public int max(int lvl) {
        return  2+4*(tier+1) +    //18 base, down from 20
                lvl*(tier+1);   //scaling unchanged
    }
    @Override
    public int proc(Char attacker, Char defender, int damage){
        if (Dungeon.level.water[attacker.pos]){
            boolean active=false;
            if (Random.Float()*100<20+10*buffedLvl()){
                active=true;
                for (int i  : PathFinder.NEIGHBOURS8){
                    Char ch = Actor.findChar(attacker.pos + i);
                    if (ch != null){
                        if (ch.alignment != Char.Alignment.ALLY) ch.damage(3+buffedLvl(), this);
                        if (ch.pos == attacker.pos + i) {
                            Ballistica trajectory = new Ballistica(ch.pos, ch.pos + i, Ballistica.MAGIC_BOLT);
                            int strength = 1 ;
                            WandOfBlastWave.throwChar(ch, trajectory, strength, false, true, getClass());
                        }
                    }
                }
            }
            if (active) Sample.INSTANCE.play(Assets.Sounds.GAS, 1f, 0.75f);
        }
        return super.proc(attacker,defender,damage);
    }
}
