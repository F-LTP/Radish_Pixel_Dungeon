package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.HeavyCannon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Scythe;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.ArtilleristSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SnakeSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Artillerist extends Mob {
    {
        spriteClass = ArtilleristSprite.class;

        HP = HT = 70;
        defenseSkill = 22;


        EXP = 9;
        maxLvl = 20;

        properties.add(Property.UNDEAD);

        loot = Generator.Category.SCROLL;
        lootChance = 0.1f;
    }

    private boolean targeting = false;
    private boolean shot = true;

    private int targetingPos = -1;

    private int cellToFire = 0;

    @Override
    protected boolean canAttack( Char enemy ) {
        Ballistica ballistica = new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE);
        boolean isCanAttack = ballistica.collisionPos == enemy.pos;
        if(targetingPos != pos) shot = true;
        // 相邻、挑战装备的武器距离、精英（索敌 / 巨型）的额外攻击距离都由父类判定
        return isCanAttack || super.canAttack( enemy );
    }

    public int damageRoll() {
        return Random.NormalIntRange( 12, 18 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 26;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(1, 6);
    }

    @Override
    protected String heroKilledMessage() {
        return Messages.capitalize(Messages.get(Artillerist.class, "kill"));
    }
    public void onZapComplete(int cell) {
        zap(cell);
        next();
    }

    private void zap(int cell) {
        spend(1f);
        Invisibility.dispel(this);
        int dmg = Random.NormalIntRange(25, 35);
        dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
        CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 4);
        // 炮击是范围伤害，英雄不是当前目标时也可能被炸死，所以按英雄自身的存活变化来结算死亡
        boolean heroWasAlive = Dungeon.hero != null && Dungeon.hero.isAlive();
        if(Dungeon.hero != null){
            if(Dungeon.hero.pos == cell){
                Dungeon.hero.damage(DamageInfo.of(dmg, DamageType.PHYSICAL, null, new Bomb()));
            }
        }
        // 落点中心也可能站着怪（目标是英雄的随从，或炮手被腐化 / 混乱后瞄准的怪），和四邻一样结算
        Mob centerMob = Dungeon.level.findMob(cell);
        if(centerMob != null){
            centerMob.damage(DamageInfo.of(dmg, DamageType.PHYSICAL, this, new Bomb()));
        }
        for(int c: PathFinder.NEIGHBOURS4){
            CellEmitter.get(cell+c).burst(BlastParticle.FACTORY, 20);
            Mob mob = Dungeon.level.findMob(cell+c);
            if(mob != null){
                mob.damage(DamageInfo.of(dmg, DamageType.PHYSICAL, this, new Bomb()));
            }
            if(Dungeon.hero != null){
                if(Dungeon.hero.pos == cell + c){
                    Dungeon.hero.damage(DamageInfo.of(dmg, DamageType.PHYSICAL, null, new Bomb()));
                }
            }
        }


        if (heroWasAlive && !Dungeon.hero.isAlive()) {
            Dungeon.fail(getClass());
            GLog.n(Messages.get(this, "bomb_party_kill"));
        }
    }
    protected boolean doAttack(Char enemy ) {

        // 弹道被挡住还能进到这里，说明是靠父类的攻击距离够到的，和相邻一样走普通攻击
        if (Dungeon.level.adjacent( pos, enemy.pos )
                || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos != enemy.pos) {
            shot = true;
            targeting = false;

            return super.doAttack( enemy );

        }else if (shot){
            targeting = true;
            shot = false;
            targetingPos = pos;
            sprite.parent.add(new TargetedCell(enemy.pos, 0xFF0000));
            for(int c: PathFinder.NEIGHBOURS4){
                sprite.parent.add(new TargetedCell(enemy.pos + c, 0xFF0000));
            }
            cellToFire = enemy.pos;
            if (sprite instanceof ArtilleristSprite) {
                ((ArtilleristSprite)sprite).targeting(cellToFire);
            } else if (sprite instanceof SnakeSprite) {
                ((SnakeSprite)sprite).targeting(cellToFire);
            }
            spend( attackDelay());
            return true;
        }
        else{
            shot = true;
            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                if(targeting)
                    sprite.zap( cellToFire );
                targeting = false;
                return false;
            } else {
                if(targeting)
                    zap(cellToFire);
                targeting = false;
                return true;
            }
        }
    }

    private static final String TARGETING     = "targeting";
    private static final String SHOT          = "shot";
    private static final String TARGETING_POS = "targetingPos";
    private static final String CELL_TO_FIRE  = "cellToFire";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( TARGETING, targeting );
        bundle.put( SHOT, shot );
        bundle.put( TARGETING_POS, targetingPos );
        bundle.put( CELL_TO_FIRE, cellToFire );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        // 旧存档没有这些键，保持默认值，读档后重新瞄准
        if (bundle.contains( SHOT )) {
            targeting = bundle.getBoolean( TARGETING );
            shot = bundle.getBoolean( SHOT );
            targetingPos = bundle.getInt( TARGETING_POS );
            cellToFire = bundle.getInt( CELL_TO_FIRE );
        }
    }

    @Override
    public void die( Object cause ) {
        // 手持重炮 10% 掉落（随机生成的矮人重炮）
        if(Random.Float() < 0.1f){
            Dungeon.level.drop(new HeavyCannon().random(), pos).sprite.drop();
        }
        super.die( cause );
    }
}
