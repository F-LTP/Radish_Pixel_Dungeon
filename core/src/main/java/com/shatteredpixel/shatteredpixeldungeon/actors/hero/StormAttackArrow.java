package com.shatteredpixel.shatteredpixeldungeon.actors.hero;


import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class StormAttackArrow extends MissileWeapon {

    {
        image = ItemSpriteSheet.SPIRIT_ALT_ARROW;

        hitSound = Assets.Sounds.HIT_ARROW;
    }


    @Override
    public Emitter emitter() {
            return super.emitter();
    }

    @Override
    public int damageRoll(Char owner) {
        if(owner instanceof Hero){
            int lv = ((Hero) owner).pointsInTalent(Talent.STORM_ATTACK);
            SpiritBow bow = ((Hero) owner).belongings.getItem(SpiritBow.class);
            // 弓可能不在身上（丢在了地上；或未祝福复活后没选择保留它），这时投掷别的武器照样会触发追加箭矢。
            // 没有弓可算，按固定 1 点伤害结算，否则下面直接空指针、游戏崩溃
            if (bow == null) return 1;
            switch (lv){
                case 2: case 4:
                    return bow.damageRoll(owner)/4;
                default:
                    return 1;
            }
        } else {
            return 1;
        }
    }

    @Override
    public float delayFactor(Char user) {
        return 0;
    }

    @Override
    public float accuracyFactor(Char owner, Char target) {
        return Float.POSITIVE_INFINITY;
    }

    @Override
    public int STRReq(int lvl) {
        return 0;
    }

    @Override
    protected void onThrow( int cell ) {
        Char enemy = Actor.findChar( cell );
        if (enemy == null || enemy == curUser) {
            parent = null;
            Splash.at( cell, 0xCC99FFFF, 1 );
        } else {
            if (!curUser.shoot( enemy, this )) {
                Splash.at(cell, 0xCC99FFFF, 1);
            }
        }
    }

    @Override
    public void throwSound() {
        Sample.INSTANCE.play( Assets.Sounds.ATK_SPIRITBOW, 1, Random.Float(0.87f, 1.15f) );
    }

    int flurryCount = -1;
    Actor flurryActor = null;

    /**
     * 在「当前这次行动」结束之后再射出追加箭。
     * <p>
     * 追加箭不能直接 cast：主箭一落地就 spendAndNext 结束了英雄的回合，追加箭晚到的那一下
     * 会在渲染线程上、回合之外结算——能把正在出手的怪射死，那只怪的回合就再也结束不了。
     * 这里排一个一次性 Actor：主箭的回合一结束它立刻行动（优先级仅次于特效），
     * 射出追加箭并占住 Actor 线程，直到追加箭结算完才放手（和原版狙击手连射的写法一致）。
     */
    public void castAfterCurrentAction(final Hero user, final int dst) {
        Actor.add(new Actor() {
            {
                actPriority = VFX_PRIO - 1;
            }

            @Override
            protected boolean act() {
                Actor.remove(this);
                if (!user.isAlive() || user.sprite == null || user.sprite.parent == null) {
                    return true;
                }
                flurryActor = this;
                cast(user, dst);
                return false;
            }
        });
    }

    @Override
    public void cast(final Hero user, final int dst) {
        final int cell = throwPos(user, dst);

        // Check if the user has enough points in StormAttack talent
//        if (user.pointsInTalent(Talent.STORM_ATTACK) - 2 > 0) {
//            handleFlurryAttack(user, dst, cell);
//        } else {
//            handleSeerShotOrDefault(user, dst, cell);
//        }
        handleFlurryAttack(user,dst,cell);

        // Additional logic for 疾风骤雨 based on talent level
        //handleStormAttack(user, dst, cell);

    }

    private void handleStormAttack(final Hero user, final int dst, final int cell) {
        int stormLevel = user.pointsInTalent(Talent.STORM_ATTACK);

        // Add extra arrows based on talent level
        switch (stormLevel) {
            case 1:
                // +1: Extra 1 arrow with fixed damage 1
                shootExtraArrow(user, dst, 0, 0.25f);
                break;
            case 2:
                // +2: Extra 1 arrow with 25% damage
                shootExtraArrow(user, dst, 0, 0.25f);
                break;
            case 3:
                // +3: Extra 2 arrows with fixed damage 1
                shootExtraArrow(user, dst, 1, 1f);
                break;
            case 4:
                // +4: Extra 2 arrows with 25% damage
                shootExtraArrow(user, dst, 1, 1f);
                break;
            default:
                // No extra arrows if stormLevel is 0 or invalid
                break;
        }
    }

    private void shootExtraArrow(final Hero user, final int dst, int numArrows, float damageMultiplier) {
        for (int i = 0; i < numArrows; i++) {
            final int extraArrowCell = calculateExtraArrowCell(user, dst);
            QuickSlotButton.target(user);
            user.busy();
            throwSound();
            user.sprite.zap(extraArrowCell);

            // Create extra arrows with the specified damage
            ((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
                    reset(user.sprite, extraArrowCell, this, new Callback() {
                        @Override
                        public void call() {
                            // Apply damage based on multiplier (fixed or percentage)
                            int damage = (damageMultiplier == 1) ? 1 : (int)(user.damageRoll() * damageMultiplier);
                            applyExtraArrowDamage(extraArrowCell, damage);
                        }
                    });
        }
    }

    private void applyExtraArrowDamage(int cell, int damage) {
        Char target = Actor.findChar(cell);
        if (target != null) {
            target.damage(DamageInfo.of(damage, DamageType.LIGHTNING, null, this));
        }
    }

    private int calculateExtraArrowCell(final Hero user, final int dst) {
        // Assuming you want the extra arrows to shoot at the same target position or nearby
        return throwPos(user, dst); // Can be adjusted if needed to shoot at different positions
    }

    private void handleFlurryAttack(final Hero user, final int dst, final int cell) {
        if (flurryCount == -1) {
            flurryCount = 2; // Initialize flurryCount if not set
        }

        if(Dungeon.hero.pointsInTalent(Talent.STORM_ATTACK)<3){
            flurryCount = 1;
        }

        final Char enemy = Actor.findChar(cell);

        if (enemy == null) {
            handleMissOrNoEnemy(user, dst);
        } else {
            performFlurryAttack(user, dst, enemy, cell);
        }
    }

    private void handleMissOrNoEnemy(final Hero user, final int dst) {
        if (user.buff(Talent.LethalMomentumTracker.class) != null) {
            user.buff(Talent.LethalMomentumTracker.class).detach();
            user.next();
        } else {
            user.spendAndNext(castDelay(user, dst));
        }

        flurryCount = -1;

        if (flurryActor != null) {
            flurryActor.next();
            flurryActor = null;
        }
    }

    private void performFlurryAttack(final Hero user, final int dst, final Char enemy, final int cell) {
        QuickSlotButton.target(enemy);
        user.busy();
        throwSound();
        user.sprite.zap(cell);

        ((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
                reset(user.sprite, cell, this, new Callback() {
                    @Override
                    public void call() {
                        if (enemy.isAlive()) {
                            curUser = user;
                            onThrow(enemy.pos);
                        }

                        flurryCount--;

                        if (flurryCount > 0) {
                            scheduleNextFlurry(user, enemy, cell);
                            // 这一箭如果是一次性 Actor 射出的，下一箭已经排好，这里要放开它占着的 Actor 线程
                            if (flurryActor != null) {
                                flurryActor.next();
                                flurryActor = null;
                            }
                        } else {
                            finalizeFlurryAttack(user, dst);
                        }
                    }
                });
    }

    private void scheduleNextFlurry(final Hero user, final Char enemy, final int cell) {
        Actor.add(new Actor() {
            {
                actPriority = VFX_PRIO - 1;
            }

            @Override
            protected boolean act() {
                flurryActor = this;
                int target = QuickSlotButton.autoAim(enemy, StormAttackArrow.this);
                if (target == -1) target = cell;
                cast(user, target);
                Actor.remove(this);
                return false;
            }
        });
        curUser.next();
    }

    private void finalizeFlurryAttack(final Hero user, final int dst) {
        if (user.buff(Talent.LethalMomentumTracker.class) != null) {
            user.buff(Talent.LethalMomentumTracker.class).detach();
            user.next();
        } else {
            user.spendAndNext(castDelay(user, dst));
        }

        flurryCount = -1;

        if (flurryActor != null) {
            flurryActor.next();
            flurryActor = null;
        }
    }

    private void handleSeerShotOrDefault(final Hero user, final int dst, final int cell) {
        if (user.hasTalent(Talent.SEER_SHOT) && user.buff(Talent.SeerShotCooldown.class) == null) {
            int shotPos = throwPos(user, dst);
            if (Actor.findChar(shotPos) == null) {
                RevealedArea a = Buff.affect(user, RevealedArea.class, 5 * user.pointsInTalent(Talent.SEER_SHOT));
                a.depth = Dungeon.depth;
                a.branchId = Dungeon.branchId;
                a.pos = shotPos;
                Buff.affect(user, Talent.SeerShotCooldown.class, 20f);
            }
        }

        super.cast(user, dst);
    }

}

