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
    public int bee_charge=0;
    public boolean bee_charged=false;
    public static final String AC_SUMMON	= "SUMMON";
    {
        image = ItemSpriteSheet.BEECOMB;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 1f;

        tier = 3;
        defaultAction=AC_SUMMON;
        bee_charge=Random.IntRange(30,50);
    }
    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_SUMMON );
        return actions;
    }
    public void getCharge(){
        bee_charge+=buffedLvl()*2+6;
        bee_charge=Math.min(100,bee_charge);
        if (bee_charge == 100){
            GLog.p(Messages.get(Beecomb.class, "ready"));
        }
    }
    @Override
    public void execute( Hero hero, String action ) {

        super.execute( hero, action );

        if (action.equals( AC_SUMMON )) {
            if (!isEquipped( hero )){
                GLog.w(Messages.get(this,"not_equipped"));
            }
            else if (bee_charge<100){
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
                else {bee_charge=0;hero.spendAndNext(1f);updateQuickslot();}
            }
        }
    }
    @Override
    public int max(int lvl) {
        return  2+4*(tier+1) +    //18 base, down from 20
                lvl*(tier+1);   //scaling unchanged
    }
@Override
public String special(){
        return bee_charge+"%";
}
    private static final String BEE_CHARGE = "bee_charge";
    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(BEE_CHARGE))
            bee_charge = bundle.getInt(BEE_CHARGE);
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put(BEE_CHARGE, bee_charge);
    }
}
