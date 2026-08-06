package com.shatteredpixel.shatteredpixeldungeon.items.wands.YetWand;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.HalomethaneFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

public class HolyLand extends Item {

    {
        image = ItemSpriteSheet.SPIRIT_ALT_ARROW;

        stackable = true;

        defaultAction = AC_THROW;
    }

    @Override
    protected void onThrow(int cell) {
        Dungeon.level.pressCell(cell);
        shatter(cell);
    }

    public void shatter(int cell) {

        if (Dungeon.level.heroFOV[cell]) {
            identify();

            Sample.INSTANCE.play( Assets.Sounds.SHATTER );
            Sample.INSTANCE.play( Assets.Sounds.BURNING );
        }

        for (int offset : pathfinder()){
            if (!Dungeon.level.solid[cell+offset]) {
                if(Dungeon.level.passable [cell+offset]) {
                    int targetCell = cell + offset;
                    int terrain = Dungeon.level.map[targetCell];
                    if (terrain != Terrain.EXIT && terrain != Terrain.ENTRANCE &&
                            terrain != Terrain.HIGH_GRASS && terrain != Terrain.WELL) {
                        Level.set(targetCell, Terrain.HOLY_LAND);
                        GameScene.updateMap(cell);
                        GameScene.updateMap(targetCell);
                        Dungeon.level.addVisuals();
                    }
                }
            }
        }
        Belief creaditSkills = hero.buff(Belief.class);
        if(creaditSkills != null) creaditSkills.DownBelief(8);
    }


    private int[] pathfinder() {
        switch (Dungeon.hero.pointsInTalent(Talent.SKY_TOWER)) {
            case 1:
                return PathFinder.CIRCLE5;
            case 2:
                return PathFinder.CIRCLE7x;
            case 3:
                return PathFinder.CIRCLE7;
            default:
                return PathFinder.CIRCLE5x;
        }
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    public int value() {
        return 0;
    }

    public static class DemonDmage extends Buff {

        public static int level = 0;
        private int interval = 1;

        @Override
        public boolean act() {
            if (target.isAlive()) {
                int dmgS = Dungeon.depth/5 == 0 ? 1 : Dungeon.depth/5;
                target.damage(dmgS,new DM100.LightningBolt());
                spend(interval);
                if (level <= 0) {
                    detach();
                }

            }

            return true;
        }

        public int level() {
            return level;
        }

        public void set( int value, int time ) {
            //decide whether to override, preferring high value + low interval
            if (Math.sqrt(interval)*level <= Math.sqrt(time)*value) {
                level = value;
                interval = time;
                spend(time - cooldown() - 1);
            }
        }
        private static final String LEVEL	    = "level";
        private static final String INTERVAL    = "interval";

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( INTERVAL, interval );
            bundle.put( LEVEL, level );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            interval = bundle.getInt( INTERVAL );
            level = bundle.getInt( LEVEL );
        }
    }

    public static class DemonSlowSpeed extends FlavourBuff {
        @Override
        public String icon() {
            return BuffIndicator.TIME;
        }
        public void detach() {
            super.detach();
            if(target.buffs(DemonDmage.class)!=null){
                Buff.detach(target, DemonDmage.class);
            }
        }
        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0xFF0000);
        }


        @Override
        public String desc(){
            return Messages.get(this, "desc",Dungeon.depth/5 == 0 ? 1 : Dungeon.depth/5);
        }
    };
    public static class MobSlowSpeed extends FlavourBuff {
        @Override
        public String icon() {
            return BuffIndicator.TIME;
        }

        public void detach() {
            super.detach();
            if(target.buffs(DemonDmage.class)!=null){
                Buff.detach(target, DemonDmage.class);
            }
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0xFF8400);
        }
        @Override
        public String desc(){
            if(Dungeon.hero.pointsInTalent(Talent.SKY_TOWER)>=3){
                return Messages.get(this, "damage_desc",Dungeon.depth/5 == 0 ? 1 : Dungeon.depth/5);
            }
            return Messages.get(this, "desc");
        }
    };
}
