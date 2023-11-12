package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class Scythe extends MeleeWeapon{

    {
        image = ItemSpriteSheet.SCYTHE;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1f;
        defaultAction=AC_REAP;
        tier = 5;
    }

    public static final String AC_REAP	= "REAP";
    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_REAP );
        return actions;
    }
    private void doReap(Hero hero){
        PathFinder.buildDistanceMap(hero.pos,Dungeon.level.discoverable,reachFactor(hero));
        for (int i = 0; i < PathFinder.distance.length; i++) {
            if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                if (Dungeon.level.map[i] == Terrain.FURROWED_GRASS || Dungeon.level.map[i] == Terrain.HIGH_GRASS){
                        Level.set(i, Terrain.GRASS);
                        GameScene.updateMap(i);
                        CellEmitter.get(i).burst(LeafParticle.LEVEL_SPECIFIC, 4);
                }
                Char ch = Actor.findChar(i);
                if (ch != null && ch !=hero){
                    hero.attack(ch, 0.75f, 0, 1f);
                }
            }
        }
        hero.spendAndNext(2f);
        Invisibility.dispel();
    }
    @Override
    public void execute( Hero hero, String action ) {

        super.execute( hero, action );

        if (action.equals( AC_REAP )) {
            if (isEquipped(hero)) {
                doReap(hero);
            }else{
                GLog.w(Messages.get(this,"not_equipped"));
            }
        }
    }
    @Override
    public int max(int lvl) {
        return  5*tier +
                lvl*tier;
    }
    @Override
    public int min(int lvl) {
        return  2*tier-2 +
                lvl*2;
    }

    public static class scytheSac extends FlavourBuff{

        public static final float DURATION	= 5f;
        {
            type = buffType.POSITIVE;
        }
        //TODO icon

        @Override
        public int icon() {
            return BuffIndicator.SCYTHE_S;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (DURATION - visualcooldown()) / DURATION);
        }
    }
}
