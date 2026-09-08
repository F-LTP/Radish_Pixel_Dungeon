package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WandOfGnollKing extends DamageWand{

    {
        image = ItemSpriteSheet.WAND_GNOLL;
        usesTargeting = false;
    }

    private int firetarget;
    private MirrorGnoll firingone;

    public int min(int lvl){
        return 2+lvl;
    }

    public int max(int lvl){
        return 5+2*lvl;
    }

    public float gnollNumLimit() {
        return gnollNumLimit(buffedLvl());
    }

    public float gnollNumLimit(int lvl) {
        return 1 + lvl / 3f;
    }

    @Override
    public String statsDesc() {
        if (levelKnown)
            return Messages.get(this, "stats_desc", min(level()), max(level()), Messages.decimalFormat("#.##", gnollNumLimit(level())));
        else
            return Messages.get(this, "stats_desc", min(0), max(0), Messages.decimalFormat("#.##", 1));
    }

    @Override
    public int collisionProperties(int target) {
        if (cursed)                                 return super.collisionProperties(target);
        else if (Char.findChar(target)!=null || !Dungeon.level.heroFOV[target])     return Ballistica.PROJECTILE;
        else                                        return Ballistica.STOP_TARGET;
    }

    @Override
    public void execute(Hero hero, String action) {
        //cursed warding does use targeting as it's just doing regular cursed zaps
        usesTargeting = cursed && cursedKnown;
        super.execute(hero, action);
    }

    private boolean gnollAvailable = true;

    @Override
    public boolean tryToZap(Hero owner, int target) {
        int currentGnollNum = 0;
        for (Char ch : Actor.chars()) {
            if (ch instanceof MirrorGnoll)
                currentGnollNum++;
        }

        float maxGnollNum = 0;
        for (Buff buff : curUser.buffs()){
            if (buff instanceof Wand.Charger){
                if (((Charger) buff).wand() instanceof WandOfGnollKing){
                    maxGnollNum += ((WandOfGnollKing)((Charger) buff).wand()).gnollNumLimit();
                }
            }
        }

        gnollAvailable = (currentGnollNum < (int)maxGnollNum);

        Char ch = Actor.findChar(target);
        if (ch == null && !gnollAvailable) {
            GLog.w(Messages.get(this, "no_more_gnolls"));
            return false;
        }

        return super.tryToZap(owner, target);
    }

    @Override
    public void onZap(Ballistica bolt) {
        int target = bolt.collisionPos;
        Char ch = Actor.findChar(target);

        if (ch != null) {
            wandProc(ch, chargesPerCast());
            ch.damage(new DamageInfo(damageRoll(), DamageType.MAGICAL, curUser, this, this));
            Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.87f, 1.15f) );

            firetarget = bolt.collisionPos;
        } else if (gnollAvailable) {
            if (!Dungeon.level.passable[target]){
                GLog.w( Messages.get(this, "bad_location"));
            } else {
                spawnGnoll(target);
            }
        }

    }

    @Override
    public void fx(Ballistica bolt, Callback callback) {
        MagicMissile m = MagicMissile.boltFromChar(curUser.sprite.parent,
                MagicMissile.RAINBOW_CONE,
                curUser.sprite,
                bolt.collisionPos,
                callback);

        if (bolt.dist > 10){
            m.setSpeed(bolt.dist*20);
        }
        Sample.INSTANCE.play(Assets.Sounds.ZAP);
    }

    @Override
    public void wandUsed() {
        if (Char.findChar(firetarget)==null || Char.findChar(firetarget) instanceof MirrorGnoll || Char.findChar(firetarget) instanceof Hero) {
            super.wandUsed();
            return;
        }
        ArrayList<MirrorGnoll> gnolls2fire = new ArrayList<>();
        for (Char gnoll : Actor.chars()) {
            if (gnoll instanceof MirrorGnoll && new Ballistica( gnoll.pos, firetarget, Ballistica.PROJECTILE).collisionPos == firetarget)
                gnolls2fire.add((MirrorGnoll) gnoll);
        }
        if (gnolls2fire.isEmpty()) {
            super.wandUsed();
            return;
        }

        firingone = gnolls2fire.remove(0);

        Callback callback = new Callback() {
            @Override
            public void call() {
                Char enemy = Char.findChar(firetarget);
                if(enemy==null || !enemy.isAlive()) {
                    WandOfGnollKing.super.wandUsed();
                    return;
                }

                firingone.attack(enemy, 0.75f, 0, 1f);

                if (enemy.isAlive() && !gnolls2fire.isEmpty()) {
                    firingone = gnolls2fire.remove(0);
                    firingone.sprite.zap(firetarget, this);
                } else {
                    WandOfGnollKing.super.wandUsed();
                }
            }
        };

        firingone.sprite.zap(firetarget, callback);
    }

    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
        int gnoll2spawn = 0;
        float chance2spawn = procChanceMultiplier(attacker) * 0.5f;
        chance2spawn -= Random.NormalFloat(0,1);
        while (chance2spawn > 0) {
            gnoll2spawn++;
            chance2spawn -= Random.NormalFloat(0,1);
        }

        ArrayList<Integer> respawnPoints = new ArrayList<>();
        for (int i = 0; i < PathFinder.NEIGHBOURS9.length; i++) {
            int p = attacker.pos + PathFinder.NEIGHBOURS9[i];
            if (Actor.findChar( p ) == null && Dungeon.level.passable[p]) {
                respawnPoints.add( p );
            }
        }

        int currentGnollNum = 0;
        for (Char ch : Actor.chars()) {
            if (ch instanceof MirrorGnoll)
                currentGnollNum++;
        }

        float maxGnollNum = 0;
        for (Buff buff : attacker.buffs()){
            if (buff instanceof Wand.Charger){
                if (((Charger) buff).wand() instanceof WandOfGnollKing){
                    maxGnollNum += ((WandOfGnollKing)((Charger) buff).wand()).gnollNumLimit();
                }
            }
        }

        gnollAvailable = (currentGnollNum < (int)maxGnollNum);

        gnoll2spawn = Math.min(gnoll2spawn, (int)maxGnollNum-currentGnollNum);

        while (gnoll2spawn > 0 && !respawnPoints.isEmpty()) {
            int index = Random.index( respawnPoints );

            spawnGnoll(respawnPoints.get( index ));

            respawnPoints.remove( index );
            gnoll2spawn--;
        }
    }

    public void spawnGnoll(int target) {
        MirrorGnoll gnoll = new MirrorGnoll();
        gnoll.setLevel(buffedLvl());
        Buff.affect(gnoll, MirrorGnoll.MirrorInvis.class,Short.MAX_VALUE);
        gnoll.pos = target;
        GameScene.add(gnoll, 1f);
        Dungeon.level.occupyCell(gnoll);
        gnoll.sprite.emitter().burst(MagicMissile.EarthParticle.FACTORY, buffedLvl() / 2 + 1);
        Dungeon.level.pressCell(target);
    }

    public static class MirrorGnoll extends NPC {

        @Override
        protected boolean isDamageable() {
            return true;
        }

        {
            spriteClass = MirrorGnollSprite.class;

            HP = HT = 1;

            alignment = Alignment.ALLY;
            state = HUNTING;

            typeImmunities.add(DamageType.TOXIC);
            immunities.add(CorrosiveGas.class); typeImmunities.add(DamageType.CORROSIVE);
            immunities.add(Burning.class); typeImmunities.add(DamageType.BURNING_STATUS);
            immunities.add( AllyBuff.class );
        }

        private int level = 0;

        private static final String LEVEL	= "level";

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( LEVEL, level );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle(bundle);
            level = bundle.getInt(LEVEL);
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int lvl) {
            level = lvl;
        }

        @Override
        public String description() {
            return Messages.get(this, "desc", 2 * level, 5 + 4 * level);
        }

        @Override
        public int damageRoll() {
            return Random.NormalIntRange(2 * level, 5 + 4 * level);
        }

        @Override
        public int attackSkill( Char target ) {
            return 9 + Dungeon.scalingDepth();
        }

        @Override
        public int defenseSkill(Char enemy) {
            return 4 + Dungeon.scalingDepth();
        }

        @Override
        protected boolean canAttack(Char enemy) {
            if (Dungeon.level.adjacent( pos, enemy.pos )){
                return true;
            }

            if (Dungeon.level.distance( pos, enemy.pos ) <= 2) {
                boolean[] passable = BArray.not(Dungeon.level.solid, null);

                for (Char ch : Actor.chars()) {
                    //our own tile is always passable
                    passable[ch.pos] = ch == this;
                }

                PathFinder.buildDistanceMap(enemy.pos, passable, 2);

                if (PathFinder.distance[pos] <= 2) {
                    return true;
                }
            }

            return super.canAttack(enemy);
        }

        @Override
        public int attackProc( Char enemy, int damage ) {
            MirrorInvis buff = buff(MirrorInvis.class);
            if (buff != null){
                buff.detach();
            }

            return super.attackProc(enemy, damage);
        }

        public static class MirrorInvis extends Invisibility {

            {
                announced = false;
            }

            @Override
            public String icon() {
                return BuffIndicator.NONE;
            }
        }

    }

    public static class MirrorGnollSprite extends MobSprite {
        private int cellToAttack;
        private Callback spearCallback;

        public MirrorGnollSprite() {
            super();

            texture( Assets.Sprites.GNOLL_GUARD );

            TextureFilm frames = new TextureFilm( texture, 12, 16 );

            idle = new Animation( 2, true );
            idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );

            run = new Animation( 12, true );
            run.frames( frames, 4, 5, 6, 7 );

            attack = new Animation( 12, false );
            attack.frames( frames, 2, 3, 0 );

            die = new Animation( 12, false );
            die.frames( frames, 8, 9, 10 );

            zap = attack.clone();

            play( idle );
        }

        @Override
        public void zap( int cell, Callback callback ) {
            cellToAttack = cell;
            spearCallback = callback;

            super.zap(cell, null);
        }

        @Override
        public void onComplete( Animation anim ) {
            if (anim == zap) {
                idle();

                ((MissileSprite)parent.recycle( MissileSprite.class )).
                        reset( this, cellToAttack, new GnollSpear(), spearCallback );
            } else {
                super.onComplete( anim );
            }
        }

        public static class GnollSpear extends Item {
            {
                image = ItemSpriteSheet.THROWING_SPEAR;
            }
        }
    }
}
