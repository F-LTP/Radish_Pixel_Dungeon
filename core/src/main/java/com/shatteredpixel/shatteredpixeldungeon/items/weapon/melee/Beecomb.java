package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bee;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Beecomb extends MeleeWeapon{
    private static ItemSprite.Glowing YELLOW = new ItemSprite.Glowing( 0xFFFF00 );
    public boolean bee_charged=false;
    public static final String AC_SUMMON	= "SUMMON";
    {
        image = ItemSpriteSheet.BEECOMB;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 1f;

        tier = 3;
        defaultAction=AC_SUMMON;
    }
    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_SUMMON );
        return actions;
    }
    @Override
    public void execute( Hero hero, String action ) {

        super.execute( hero, action );

        if (action.equals( AC_SUMMON )) {
            if (!isEquipped( hero )){
                GLog.w(Messages.get(this,"not_equipped"));
            }
            else if (!bee_charged){
                GLog.w(Messages.get(this,"not_ready"));
            }
            else {
                ArrayList<Integer> respawnPoints = new ArrayList<>();
                for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
                    int p = hero.pos + PathFinder.NEIGHBOURS8[i];
                    if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
                        respawnPoints.add( p );
                    }
                }
                int spawned = 0,to_sum=1;
                while (to_sum > 0 && respawnPoints.size() > 0) {
                    int index = Random.index( respawnPoints );

                    Bee b = new Bee();
                    b.spawn( Dungeon.scalingDepth() );
                    b.setPotInfo(-1, null);
                    b.HP = b.HT;
                    b.alignment = Char.Alignment.ALLY;
                    GameScene.add( b );
                    ScrollOfTeleportation.appear( b, respawnPoints.get( index ) );
                    respawnPoints.remove( index );
                    to_sum--;
                    spawned++;
                }
                if (spawned==0) {
                    GLog.w(Messages.get(this,"no_bee"));
                }
                else {bee_charged=false;hero.spend(1f);}
            }
        }
    }
    @Override
    public int max(int lvl) {
        return  4*(tier+1) +    //16 base, down from 20
                lvl*(tier+1);   //scaling unchanged
    }
    @Override
    public ItemSprite.Glowing glowing() {
        if (bee_charged)
        return YELLOW;
        else return null;
    }

    private static final String BEE_CHARGED = "bee_charged";
    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        bee_charged = bundle.getBoolean(BEE_CHARGED);
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put(BEE_CHARGED, bee_charged);
    }
}
