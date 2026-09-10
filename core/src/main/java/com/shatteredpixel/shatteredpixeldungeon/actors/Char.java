/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.moonlight.FatedDraw;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.Deminion;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishEnemy.RoyalGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.ImmortalShieldAffecter;
import com.shatteredpixel.shatteredpixeldungeon.damage.*;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.SnDSFX;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.events.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.LunarCorona;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Masamune;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Sunless;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Turtleir;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.*;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.FerretTuft;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.LightKing;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ThirteenLeafClover;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.YetWand.HolyLand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.*;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

public abstract class Char extends Actor {

    public int pos = 0;

    public CharSprite sprite;

    public boolean LockChainCripple = false;

    public int HT;
    public int HP;

    // DoggingDog on 20250501c
    public int VITAE;

    public int DefendProKill = 3;

    // change from budding
    protected float critSkill = 0;
    protected float critDamage = 1.5f;
    protected float critDamageCap = 3f;

    protected float baseSpeed = 1;
    protected PathFinder.Path path;

    public int paralysed = 0;
    public boolean rooted = false;
    public boolean flying = false;
    public int invisible = 0;


    ;

    protected float critSkill() {
		return critSkill + com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfConcentration.critBonus(this);
	}

    protected float critDamage() {
		return Math.min(critDamage + com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfConcentration.critDamgeBonus(this), critDamageCap);
    }

    public float baseCritSkill() {
        return critSkill();
    }

    public float baseCritDamage() {
        return critDamage();
    }

    public float critDamageCap() {
        return critDamageCap;
    }

    public float rawCritDamage() {
        return critDamage;
    }

    public float talentProc() {//for RUNIC_TRANSFERENCE
        return 1f;
    }

    //these are relative to the hero
    public enum Alignment {
        ENEMY,
        NEUTRAL,
        ALLY
    }

    public Alignment alignment;

    public int viewDistance = 8;

    public boolean[] fieldOfView = null;

    private LinkedHashSet<Buff> buffs = new LinkedHashSet<>();

    @Override
    protected boolean act() {
        if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()) {
            fieldOfView = new boolean[Dungeon.level.length()];
        }
        Dungeon.level.updateFieldOfView(this, fieldOfView);

        //throw any items that are on top of an immovable char
        if (properties().contains(Property.IMMOVABLE)) {
            throwItems();
        }
        return false;
    }

    protected void throwItems() {
        Heap heap = Dungeon.level.heaps.get(pos);
        if (heap != null && heap.type == Heap.Type.HEAP
                && !(heap.peek() instanceof Tengu.BombAbility.BombItem)
                && !(heap.peek() instanceof Tengu.ShockerAbility.ShockerItem)) {
            ArrayList<Integer> candidates = new ArrayList<>();
            for (int n : PathFinder.NEIGHBOURS8) {
                if (Dungeon.level.passable[pos + n]) {
                    candidates.add(pos + n);
                }
            }
            if (!candidates.isEmpty()) {
                Dungeon.level.drop(heap.pickUp(), Random.element(candidates)).sprite.drop(pos);
            }
        }
    }

    public String name() {
        return Messages.get(this, "name");
    }

    public boolean canInteract(Char c) {
        if (Dungeon.level.adjacent(pos, c.pos)) {
            return true;
        } else if (c instanceof Hero
                && alignment == Alignment.ALLY
                && !hasProp(this, Property.IMMOVABLE)
                && Dungeon.level.distance(pos, c.pos) <= 2 * hero.pointsInTalent(Talent.ALLY_WARP)) {
            return true;
        } else {
            return false;
        }
    }

    //swaps places by default
    public boolean interact(Char c) {

        //don't allow char to swap onto hazard unless they're flying
        //you can swap onto a hazard though, as you're not the one instigating the swap
        if (!Dungeon.level.passable[pos] && !c.flying) {
            return true;
        }

        //can't swap into a space without room
        if (properties().contains(Property.LARGE) && !Dungeon.level.openSpace[c.pos]
                || c.properties().contains(Property.LARGE) && !Dungeon.level.openSpace[pos]) {
            return true;
        }

        int curPos = pos;

        //warp instantly with allies in this case
        if (c == hero && hero.hasTalent(Talent.ALLY_WARP)) {
            PathFinder.buildDistanceMap(c.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
            if (PathFinder.distance[pos] == Integer.MAX_VALUE) {
                return true;
            }
            ScrollOfTeleportation.appear(this, c.pos);
            ScrollOfTeleportation.appear(c, curPos);
            Dungeon.observe();
            GameScene.updateFog();
            return true;
        }

        //can't swap places if one char has restricted movement
        if (rooted || c.rooted || buff(Vertigo.class) != null || c.buff(Vertigo.class) != null) {
            return true;
        }

        moveSprite(pos, c.pos);
        move(c.pos);
        float speedAdj = 1f;
        if (c.buff(CrabArmor.likeCrab.class) != null) {
            if (c.pos / Dungeon.level.width() == curPos / Dungeon.level.width()) speedAdj = 1.75f;
            else speedAdj = 5f / 6f;
        }
        c.sprite.move(c.pos, curPos);
        c.move(curPos);

        c.spend(1 / (c.speed() * speedAdj));

        if (c == hero) {
            if (hero.subClass == HeroSubClasses.FREERUNNER) {
                Buff.affect(hero, Momentum.class).gainStack();
            }

            hero.busy();
        }

        return true;
    }

    protected boolean moveSprite(int from, int to) {

        if (sprite.isVisible() && sprite.parent != null && (Dungeon.level.heroFOV[from] || Dungeon.level.heroFOV[to])) {
            sprite.move(from, to);
            return true;
        } else {
            sprite.turnTo(from, to);
            sprite.place(to);
            return true;
                    }
                }

                public void hitSound(float pitch) {
                    if (this == hero && SnDSFX.active()) {
                        SnDSFX.play("impact");
                        return;
                    }
                    Sample.INSTANCE.play(Assets.Sounds.HIT, 1, pitch);
                }

                public boolean blockSound(float pitch) {
                    return false;
                }

                protected static final String POS = "pos";
                protected static final String TAG_HP = "HP";
                protected static final String TAG_HT = "HT";
                protected static final String TAG_SHLD = "SHLD";
                protected static final String BUFFS = "buffs";

                protected static final String KILL_PREF = "kill_pref";

                /**
                 * CRIT BUNDLE
                 */
                protected static final String CRIT = "crit";
                protected static final String CRIT_D = "crit_d";

                //LSD
                protected static final String LOCK_CHAIN = "lock_chain";

                @Override
                public void storeInBundle(Bundle bundle) {

        super.storeInBundle(bundle);

        bundle.put(POS, pos);
        bundle.put(TAG_HP, HP);

        bundle.put(KILL_PREF, DefendProKill);

        bundle.put(TAG_HT, HT);
        bundle.put(BUFFS, buffs);

        bundle.put(LOCK_CHAIN, LockChainCripple);

        bundle.put(CRIT, critSkill);
        bundle.put(CRIT_D, critDamage);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {

        super.restoreFromBundle(bundle);

        DefendProKill = bundle.getInt(KILL_PREF);

        pos = bundle.getInt(POS);
        HP = bundle.getInt(TAG_HP);
        HT = bundle.getInt(TAG_HT);

		Buff.beginRestore();
		try {
			for (Bundlable b : bundle.getCollection(BUFFS)) {
				if (b != null) {
					((Buff) b).attachTo(this);
				}
			}
		} finally {
			Buff.endRestore();
		}

        LockChainCripple = bundle.getBoolean(LOCK_CHAIN);

        if (bundle.contains(CRIT)) {
            critSkill = bundle.getFloat(CRIT);
        }
        if (bundle.contains(CRIT_D)) {
            critDamage = bundle.getFloat(CRIT_D);
        }

    }

    final public boolean attack(Char enemy) {
        return attack(enemy, 1f, 0f, 1f);
    }

    public boolean attack(Char enemy, float dmgMulti, float dmgBonus, float accMulti) {

        if (enemy == null) return false;

        // 充能（特殊学派）：英雄攻击伤害提升30%
        if (this == hero && buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChargeBoost.class) != null) {
            dmgMulti *= 1.3f;
        }

        // Attack animations can finish after a level transition or scene reload.
        // Do not resolve a stale attack while the level's map/FOV is unavailable.
        if (Dungeon.level == null || Dungeon.level.heroFOV == null
                || pos < 0 || enemy.pos < 0
                || pos >= Dungeon.level.heroFOV.length
                || enemy.pos >= Dungeon.level.heroFOV.length) {
            return false;
        }

        boolean visibleFight = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[enemy.pos];

        if (enemy.isInvulnerable(getClass())) {

            if (visibleFight) {
                enemy.sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));

                Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1f, Random.Float(0.96f, 1.05f));
            }

            return false;

        } else if (hit(this, enemy, accMulti, false)) {
            if (enemy.buff(AfterImage.Blur.class) != null) {
                enemy.buff(AfterImage.Blur.class).gainDodge();
            }

			OrdinaryAttackDamage.DamageRoll damageRoll = OrdinaryAttackDamage.rollBaseDamage(this);
			OrdinaryAttackDamage.CriticalRoll criticalRoll = OrdinaryAttackDamage.rollCritical(this, enemy, damageRoll.damage);
			Preparation prep = damageRoll.preparation;
			DamageInfo attackDamage = OrdinaryAttackDamage.build(this, enemy, Math.round(criticalRoll.damage), criticalRoll.critical,
					criticalRoll.multiplier, dmgMulti, dmgBonus);
			OrdinaryAttackDamage.applyPlateArmor(enemy, attackDamage);

			// 发布近战攻击事件：反弹类效果（如 Rlyeh）可在命中后、结算前取消本次攻击
			AttackEvent attackEvent = new AttackEvent(
					this, enemy, attackDamage.getDamage(),
					attackingWeapon(), defendingWeapon(enemy), defendingArmor(enemy));
			EventManager.emit(attackEvent);
			if (attackEvent.isCancelled()) {
				if (visibleFight) {
					hitSound(Random.Float(0.87f, 1.15f));
				}
				return true;
			}

			int effectiveDamage = OrdinaryAttackDamage.foldPostProcessing(this, enemy, attackDamage);

            if (visibleFight) {
                if (effectiveDamage > 0 || !enemy.blockSound(Random.Float(0.96f, 1.05f))) {
                    hitSound(Random.Float(0.87f, 1.15f));
                }
            }

            // If the enemy is already dead, interrupt the attack.
            // This matters as defence procs can sometimes inflict self-damage, such as armor glyphs.
            if (!enemy.isAlive()) {
                return true;
            }

            //TODO 不会 交给狗哥）2025.2.1 19：45

            enemy.damage(attackDamage);

            if (buff(FireImbue.class) != null) buff(FireImbue.class).proc(enemy);
            if (buff(FrostImbue.class) != null) buff(FrostImbue.class).proc(enemy);

            // 圆球皮肤：近战命中时有 16% 概率使目标麻痹 2 回合
            if (this == hero && hero.isSphereSkin()
                    && !(((Hero) this).belongings.attackingWeapon() instanceof MissileWeapon)
                    && enemy.isAlive()
                    && Random.Float() < 0.16f) {
                Buff.affect(enemy, Paralysis.class, 2f);
            }

            if (this == hero && enemy.isAlive() && ArrowBuff.tryExecute(enemy)) {
                enemy.HP = 0;
                enemy.die(this);
            }

            if (enemy.isAlive() && enemy.alignment != alignment && prep != null && prep.canKO(enemy)) {
                enemy.HP = 0;
                if (!enemy.isAlive()) {
                    enemy.die(this);
                } else {
                    //helps with triggering any on-damage effects that need to activate
                    enemy.damage(DamageInfo.of(-1, DamageType.TRUE, this, this));
                    DeathMark.processFearTheReaper(enemy);
                }
                enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
            }

            enemy.sprite.bloodBurstA(sprite.center(), effectiveDamage);
            enemy.sprite.flash();

            if (!enemy.isAlive() && visibleFight) {
                if (enemy == hero) {

                    if (this == hero) {
                        return true;
                    }

                    if (this instanceof WandOfLivingEarth.EarthGuardian
                            || this instanceof MirrorImage || this instanceof PrismaticImage) {
                        Badges.validateDeathFromFriendlyMagic();
                    }
                    Dungeon.fail(getClass());
                    GLog.n(Messages.capitalize(Messages.get(Char.class, "kill", name())));

                } else if (this == hero) {
                    GLog.i(Messages.capitalize(Messages.get(Char.class, "defeat", enemy.name())));
                }
            }

            return true;

        } else {
			if (this instanceof Hero && ((Hero) this).belongings.attackingWeapon() instanceof Yamato) {
				((Yamato) ((Hero) this).belongings.attackingWeapon()).onMiss();
			}
            if (enemy.buff(CloakofGreyFeather.hexDodge.class) != null) {
                for (Char ch : Actor.chars()) {
                    if (ch.alignment != enemy.alignment && enemy.fieldOfView[ch.pos] && ch.alignment != Alignment.NEUTRAL) {
                        Buff.affect(ch, Hex.class, 2f + 0.75f * enemy.buff(CloakofGreyFeather.hexDodge.class).buffedLvl());
                    }
                }
            }
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());

            if (visibleFight) {
                //TODO enemy.defenseSound? currently miss plays for monks/crab even when they parry
                if (this == hero && SnDSFX.active()) {
                    SnDSFX.play("stealth");
                } else {
                    Sample.INSTANCE.play(Assets.Sounds.MISS);
                }
            }

            return false;

        }
    }

    public static int INFINITE_ACCURACY = 1_000_000;
    public static int INFINITE_EVASION = 1_000_000;

    final public static boolean hit(Char attacker, Char defender, boolean magic) {
        return hit(attacker, defender, magic ? 2f : 1f, magic);
    }

    public static boolean hit(Char attacker, Char defender, float accMulti, boolean magic) {
        float acuStat = attacker.attackSkill(defender);
        float defStat = defender.defenseSkill(attacker);

		if (attacker instanceof Mob) {
			Item mobWeapon = attacker.attackingWeapon();
			if (mobWeapon instanceof KindOfWeapon) {
				acuStat *= ((KindOfWeapon) mobWeapon).accuracyFactor(attacker, defender);
			}
			acuStat *= RingOfAccuracy.accuracyMultiplier(attacker);
		}
		if (defender instanceof Mob) {
			Armor mobArmor = defender.armor();
			if (mobArmor != null) defStat = mobArmor.evasionFactor(defender, defStat);
			defStat *= RingOfEvasion.evasionMultiplier(defender);
		}

		Item attackWeapon = attacker instanceof Hero
				? ((Hero) attacker).belongings.attackingWeapon()
				: attacker.attackingWeapon();
		AfterImage.absoluteEvasion attackEvasion = attacker.buff(AfterImage.absoluteEvasion.class);
		if (!magic && attackEvasion != null
				&& attacker.buff(AfterImage.AnotabsoluteEvasion.class) == null
				&& attackWeapon instanceof CircleSword) {
			attackEvasion.detach();
			return true;
		}

        if (defender instanceof Hero && ((Hero) defender).damageInterrupt) {
            ((Hero) defender).interrupt();
        }

        if (defender.buff(AfterImage.absoluteEvasion.class) != null && defender instanceof Wraith) {

            Buff.detach(defender, AfterImage.absoluteEvasion.class);
            return false;
        }

		boolean defenderWieldsCircleSword = defender.wieldsCircleSword();
		if (defender.buff(AfterImage.absoluteEvasion.class) != null
				&& !defenderWieldsCircleSword) {
            Buff.detach(defender, AfterImage.absoluteEvasion.class);
            return false;
        }

        if (defender.buff(AfterImage.AnotabsoluteEvasion.class) != null) {
            Buff.detach(defender, AfterImage.AnotabsoluteEvasion.class);
            return false;
        }

        if (defender.HP < defender.HT) {
            if (attackWeapon instanceof Axe_D) {
                return true;
            }
        }

        if (attackWeapon instanceof PneumFistGloves) {
            if (((PneumFistGloves) attackWeapon).active && Dungeon.energy > 0) {
                return true;
            }
        }


        //invisible chars always hit (for the hero this is surprise attacking)
        if (attacker.invisible > 0 && attacker.canSurpriseAttack()) {
            acuStat = INFINITE_ACCURACY;
        }

        // 怪物偷袭英雄：攻击者不在英雄当前视野内（如门后/黑暗中）时攻击必定命中。
        // 只必中，不改变防御/DR（与英雄偷袭怪物不同，没有防御归零逻辑）。
        if (defender instanceof Hero && ((Hero) defender).surprisedBy(attacker)) {
            acuStat = INFINITE_ACCURACY;
        }

        if (defender.buff(MonkEnergy.MonkAbility.Focus.FocusBuff.class) != null && !magic) {
            defStat = INFINITE_EVASION;
            defender.buff(MonkEnergy.MonkAbility.Focus.FocusBuff.class).detach();
            Buff.affect(defender, MonkEnergy.MonkAbility.Focus.FocusActivation.class, 0);
        }

        //if accuracy or evasion are large enough, treat them as infinite.
        //note that infinite evasion beats infinite accuracy
        if (defStat >= INFINITE_EVASION) {
            return false;
        } else if (acuStat >= INFINITE_ACCURACY) {
            return true;
        }

        float acuRoll;
        // 注定一抽：攻击者命中判定取最大值
        FatedDraw.FatedDrawTracker trackerA = attacker instanceof Hero ? ((Hero) attacker).buff(FatedDraw.FatedDrawTracker.class) : null;
        if (trackerA != null && trackerA.remainingChecks > 0) {
            acuRoll = acuStat; // 取最大值
            trackerA.consume("attack_hit");
        } else {
            acuRoll = Random.Float(acuStat);
        }

        //祝福之戒
        float bless_adj_a = 1.25f, bless_adj_d = 1.25f;
        if (hero.buff(RingOfBenediction.Benediction.class) != null) {
            if (attacker == hero)
                bless_adj_a *= RingOfBenediction.periodMultiplier(attacker);
            else if (defender == hero)
                bless_adj_d *= RingOfBenediction.periodMultiplier(attacker);
        }
        if (attacker.buff(Bless.class) != null) acuRoll *= bless_adj_a;
        //祝福之戒

        if (attacker.buff(Bless.class) != null) acuRoll *= 1.25f;
        // 命中修正取"加法叠加"：各效果的增减百分比直接相加，而不是连乘，
        // 避免多个减益叠乘后命中率掉得过狠。怨魂缠身等通过 FlavourBuff 的钩子参与。
        float accMod = 1f;
        if (attacker.buff(Hex.class) != null) accMod -= 0.2f;
        if (attacker.buff(Daze.class) != null) accMod -= 0.5f;
        for (ChampionEnemy buff : attacker.buffs(ChampionEnemy.class)) {
            accMod -= 1f - buff.evasionAndAccuracyFactor();
        }
        for (FlavourBuff buff : attacker.buffs(FlavourBuff.class)) {
            accMod -= 1f - buff.evasionAndAccuracyFactor();
        }
        acuRoll *= Math.max(0f, accMod);
        acuRoll *= AscensionChallenge.statModifier(attacker);

        float defRoll;
        // 注定一抽：防御者闪避判定取最大值
        FatedDraw.FatedDrawTracker trackerD = defender instanceof Hero ? ((Hero) defender).buff(FatedDraw.FatedDrawTracker.class) : null;
        if (trackerD != null && trackerD.remainingChecks > 0) {
            defRoll = defStat; // 取最大值
            trackerD.consume("defense_evasion");
        } else {
            defRoll = Random.Float(defStat);
        }
        if (defender.buff(Bless.class) != null) defRoll *= 1.25f;
        // 闪避修正同样取加法叠加，规则见上。
        float defMod = 1f;
        if (defender.buff(Hex.class) != null) defMod -= 0.2f;
        if (defender.buff(Daze.class) != null) defMod -= 0.5f;
        for (ChampionEnemy buff : defender.buffs(ChampionEnemy.class)) {
            defMod -= 1f - buff.evasionAndAccuracyFactor();
        }
        for (ChampionHero buff : defender.buffs(ChampionHero.class)) {
            defMod -= 1f - buff.evasionAndAccuracyFactor();
        }
        for (FlavourBuff buff : defender.buffs(FlavourBuff.class)) {
            defMod -= 1f - buff.evasionAndAccuracyFactor();
        }
        defRoll *= Math.max(0f, defMod);

        defRoll *= AscensionChallenge.statModifier(defender);
        defRoll *= FerretTuft.evasionMultiplier();

        return (acuRoll * accMulti) >= defRoll;
    }

    //used for damage and blocking calculations, normally just calls NormalIntRange
    // but may be affected by things that specifically impact combat number ranges
    public static int combatRoll(int min, int max) {
        // 注定一抽：伤害判定取最大值（需要在调用点处理）
        FatedDraw.FatedDrawTracker tracker = Dungeon.hero != null ? Dungeon.hero.buff(FatedDraw.FatedDrawTracker.class) : null;
        if (tracker != null && tracker.remainingChecks > 0) {
            // 不在这里消耗，由调用点决定是否消耗
            return max;
        }

        if (Random.Float() < ThirteenLeafClover.combatDistributionInverseChance()) {
            return ThirteenLeafClover.invCombatRoll(min, max);
        } else {
            return Random.NormalIntRange(min, max);
        }
    }

    public int attackSkill(Char target) {
        return 0;
    }

    public int defenseSkill(Char enemy) {
        return 0;
    }

    public String defenseVerb() {
        return Messages.get(this, "def_verb");
    }

    public int drRoll() {
        // 轮刃会放弃所有防御：手持轮刃时基础防御与护甲防御全部失效
        if (wieldsCircleSword()) return 0;
        int dr = 0;

        dr += combatRoll(0, Barkskin.currentLevel(this));

        // 护甲统一接入：Hero 在 Hero.drRoll 处理，其余持甲角色（ArmoredStatue/未来挑战怪）在此统一结算护甲 DR
        if (!(this instanceof Hero)) {
            Item armorItem = defendingArmor(this);
            if (armorItem != null) {
				int armorDr = combatRoll(((Armor) armorItem).DRMin(), ((Armor) armorItem).DRMax());
				dr += armorDr;
				if (com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Resonance.isResonanceActive(this)) {
					dr += Math.round(armorDr * 0.5f);
				}
            }
			Item weaponItem = defendingWeapon(this);
			if (weaponItem instanceof KindOfWeapon) {
				dr += combatRoll(0, ((KindOfWeapon) weaponItem).defenseFactor(this));
			}
        }

        return dr;
    }

    public int damageRoll() {
        return 1;
    }

    //TODO it would be nice to have a pre-armor and post-armor proc.
    // atm attack is always post-armor and defence is already pre-armor

    public int attackProc(Char enemy, int damage) {
        for (ChampionEnemy buff : buffs(ChampionEnemy.class)) {
            buff.onAttackProc(enemy);
        }

        if (getClass() == Hero.class) {
            LightKing lightKing = hero.belongings.getItem(LightKing.class);
            if (lightKing != null) {
                int lvl = lightKing.level();
                float[] thresholds = {0.9f, 0.85f, 0.8f, 0.75f};
                float[] damageModifiers = {1.25f, 1.33f, 1.41f, 1.50f};

                float hpPercentage = (float) HP / HT;
                int originalDamage = damage;

                if (hpPercentage >= thresholds[lvl]) {
                    float modifiedDamage = damage * damageModifiers[lvl];
                    int bonusDamage = Math.round(modifiedDamage - damage);
                    if (bonusDamage < 1) {
                        bonusDamage = 1;
                    }
                    damage = damage + bonusDamage;
                } else {
                    damage = Math.round(damage / damageModifiers[lvl]);
                    int reducedDamage = originalDamage - damage;
                }
            }
        }

        for (ChampionHero buff : buffs(ChampionHero.class)) {
            buff.onAttackProc(enemy);
        }

        return damage;
    }

    public int defenseProc(Char enemy, int damage) {

        Earthroot.Armor armor = buff(Earthroot.Armor.class);
        if (armor != null) {
            damage = armor.absorb(damage);
        }

        // 护甲统一接入：Hero 在 Hero.defenseProc 处理，其余持甲角色（ArmoredStatue/未来挑战怪）在此统一触发护甲 glyph
        if (!(this instanceof Hero)) {
            Item armorItem = defendingArmor(this);
            if (armorItem != null) {
                damage = ((Armor) armorItem).proc(enemy, this, damage);
            }
        }

        return damage;
    }

    // ========== 攻击事件辅助 ==========

    /** 该角色本次使用的攻击武器（Hero 用装备武器，Statue 用其 weapon 字段，RoyalGuard 用其 equipment 字段），无则为 null。 */
    public Item attackingWeapon() {
        if (this instanceof Hero) {
            return ((Hero) this).belongings.attackingWeapon();
        }
        if (this instanceof Statue) {
            return ((Statue) this).weapon;
        }
        if (this instanceof RoyalGuard) {
            return ((RoyalGuard) this).equipment;
        }
        return null;
    }

    /** 该角色当前是否手持轮刃（CircleSword），手持时放弃全部防御。 */
    public boolean wieldsCircleSword() {
        return defendingWeapon(this) instanceof CircleSword;
    }

    /** 防御方持有的武器（Hero 用装备武器，Statue 用其 weapon 字段，RoyalGuard 用其 equipment 字段），无则为 null。 */
    public static Item defendingWeapon(Char defender) {
        if (defender instanceof Hero) {
            return ((Hero) defender).belongings.weapon();
        }
        if (defender instanceof Statue) {
            return ((Statue) defender).weapon;
        }
        if (defender instanceof RoyalGuard) {
            return ((RoyalGuard) defender).equipment;
        }
		if (defender instanceof Mob) {
			return defender.attackingWeapon();
		}
        return null;
    }

    /** 该角色当前穿着的护甲（Hero 在 belongings，ArmoredStatue/未来挑战怪覆写），无则为 null。 */
    public Armor armor() {
        return null;
    }

    /** 防御方护甲（Hero 用装备护甲，其余用其 armor() 访问器），无则为 null。 */
    public static Item defendingArmor(Char defender) {
        if (defender instanceof Hero) {
            return ((Hero) defender).belongings.armor();
        }
        return defender.armor();
    }

    /** 该角色可用的法杖等级总和（CelestialSphere 等按持有者结算法杖加成）。普通角色无法杖为 0，Hero 累加装备法杖，法杖型远程怪可覆写为随机数值。 */
    public int wandLevel() {
        return 0;
    }

    public float speed() {
        float speed = baseSpeed;

        /** 祝福之戒 */
        float ben_mul = 1f;
        if (this == hero) {
            Buff ben = hero.buff(RingOfBenediction.Benediction.class);
            if (ben != null) {
                ben_mul *= RingOfBenediction.periodMultiplier(this);
            }
        }
        if (buff(Stamina.class) != null) speed *= 1.5f * ben_mul;
        /** 祝福之戒 */

        if (buff(HolyLand.DemonSlowSpeed.class) != null) {
            speed *= 0.5f;
        }
        if (buff(HolyLand.MobSlowSpeed.class) != null) {
            speed *= 0.77f;
        }

        if (buff(Cripple.class) != null) speed /= 2f;
        if (buff(Adrenaline.class) != null) speed *= 2f;
        if (buff(Haste.class) != null) speed *= 3f;
        if (buff(Dread.class) != null) speed *= 2f;
        if (buff(WheelchairRush.class) != null) speed *= 2f;
        if (buff(DarkCoat.myPace.class) != null) speed = Math.max(1f, speed);
        return speed;
    }

    //currently only used by invisible chars, or by the hero
    public boolean canSurpriseAttack() {
        return true;
    }

    // 取当前攻击目标：英雄/怪物通用（用于武器偷袭判定）
    public static Char enemyOf( Char ch ) {
        if (ch instanceof Hero) return ((Hero) ch).enemy();
        if (ch instanceof Mob)  return ((Mob) ch).getEnemy();
        return null;
    }

    // 对称的偷袭判定：英雄偷袭怪物 / 怪物偷袭英雄（视野外攻击）都算偷袭
    public static boolean isSurpriseAttack( Char attacker, Char defender ) {
        if (defender == null) return false;
        if (defender instanceof Mob && ((Mob) defender).surprisedBy(attacker)) return true;
        if (defender instanceof Hero && ((Hero) defender).surprisedBy(attacker)) return true;
        return false;
    }

    //used so that buffs(Shieldbuff.class) isn't called every time unnecessarily
    private int cachedShield = 0;
    public boolean needsShieldUpdate = true;

    public int shielding() {
        if (!needsShieldUpdate) {
            return cachedShield;
        }

        cachedShield = 0;
        for (ShieldBuff s : buffs(ShieldBuff.class)) {
            cachedShield += s.shielding();
        }
        needsShieldUpdate = false;
        return cachedShield;
    }

    public int getVitae() {
        int preVitae = 0;
        for (VitaeBuff s : buffs(VitaeBuff.class)) {
            preVitae += s.getVitae();
        }
        return preVitae;
    }

    public void GetMobExp(Mob alter) {
        int exp = hero.lvl <= alter.maxLvl ? alter.EXP : 0;
        if (hero.buff(AscensionChallenge.class) != null &&
                exp == 0 && alter.maxLvl > 0 && alter.EXP > 0 && hero.lvl < Hero.MAX_LEVEL) {
            exp = Math.round(10 * alter.spawningWeight());
        }
        if (exp > 0) {
            hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(exp), FloatingText.EXPERIENCE);
        }
        hero.earnExp(exp, getClass());
    }

    /**
     * 新的伤害方法：使用DamageInfo包装伤害信息
     * <p>
     * 暴击是DamageInfo的属性，而不是特殊的伤害类型。
     * 此方法将DamageInfo转换为旧格式调用现有逻辑，保持向后兼容。
     *
     * @param info 伤害信息对象
     */
    public void damage(DamageInfo info) {
        // 标记（特殊学派）：受标记目标受到伤害时附加最终增伤modifier
        if (info != null) {
            com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MarkDebuff mark =
                    buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MarkDebuff.class);
            if (mark != null) {
                info.addFinalAddModifier(mark.bonusDamage(), "mark");
            }
            DamagePipeline.apply(this, info);
        }
    }

    /**
     * 旧调用兼容入口已被移除：所有伤害必须显式构造 DamageInfo 并经 damage(DamageInfo) 进入管线。
     */

    /** 唯一权威的伤害应用实现，由 DamagePipeline 调用。 */
    public DamageResult applyDamage(DamageInfo info) {
        Object src = info.getSource();
        DamageType damageType = info.getType();
        if (damageType == null || damageType == DamageType.UNKNOWN) {
            damageType = DamageType.PHYSICAL;
        }
        int dmg = info.getDamage();

        // NPC overrides still receive the hit, while base NPC damage remains immune.
        if (properties.contains(Property.NPC) && !isDamageable()) {
            return new DamageResult(info.getBaseDamage(), dmg, 0, 0, 0, 0, true);
        }

        // 天球仪造成魔法伤害的代码移动到这里来，以便防止额外造成1次物理伤害
        boolean srcIsAHeroWieldingCS = src instanceof Hero && ((Hero) src).belongings.attackingWeapon() instanceof CelestialSphere;
        boolean srcIsCS = src instanceof CelestialSphere;
        boolean srcIsAStatueWieldingCS = src instanceof Statue && ((Statue) src).weapon instanceof CelestialSphere;
        if (srcIsCS || srcIsAHeroWieldingCS || srcIsAStatueWieldingCS) {
            src = new DM100.LightningBolt();
        }

        if (!isAlive() || dmg < 0) {
            return new DamageResult(info.getBaseDamage(), Math.max(0, dmg), 0, 0, 0, 0, false);
        }

        // 角色未经过减免计算的原始伤害事件（伤害减免计算前）
        {
            Char attacker = src instanceof Char ? (Char) src : null;
            EventManager.emit(new CharUnprocedDamageEvent(this, attacker, src, dmg, damageType));
        }

        // —— 应用护甲：直接加算（在承伤倍率乘算之前做平坦扣减）——
        int armorBlocked = 0;
        Char attackerChar = info.getAttacker() != null ? info.getAttacker() : (src instanceof Char ? (Char) src : null);
        if (attackerChar != null && damageType == DamageType.PHYSICAL && !OrdinaryAttackDamage.ignoresDefenseRoll(attackerChar)) {
            int dr = OrdinaryAttackDamage.rollDefenseReduction(attackerChar, this, true);
            if (dr > 0) {
                int before = dmg;
                dmg = Math.max(dmg - dr, 0);
                armorBlocked = before - dmg;
            }
        }

		Armor wornArmor = armor();
		if (!(this instanceof Hero) && src != null && wornArmor != null
				&& wornArmor.hasGlyph(AntiMagic.class, this)
				&& AntiMagic.RESISTS.contains(src.getClass())) {
			dmg = Math.max(0, dmg - AntiMagic.drRoll(this, wornArmor.procLvl()));
		}

        // —— 黏稠刻印：在护甲之后结算延迟伤害（优先级在护甲之后，护甲先阻挡再延迟剩余部分）——
        if (buff(Viscosity.ViscosityTracker.class) != null) {
            dmg = buff(Viscosity.ViscosityTracker.class).deferDamage(dmg);
            buff(Viscosity.ViscosityTracker.class).detach();
        }

        // DoggingDog on 20250710
        if (hero.buff(LunarCorona.Phase.class) != null) {
            LunarCorona.Phase buff = hero.buff(LunarCorona.Phase.class);
            if (buff.isWaxing()) {
                dmg *= 2;
            } else {
                dmg /= 2;
            }
        }

        // DoggingDog on 20250518
        if (hero.belongings.armor instanceof Turtleir && this instanceof Hero) {
            Turtleir.Mass_Energy buff = hero.buff(Turtleir.Mass_Energy.class);
            if (buff != null) {
                dmg = buff.absorbDamage(dmg);
            }
        }

        // DoggingDog on 20250523
        if (hero.belongings.armor instanceof Sunless && this instanceof Hero) {
            Sunless.Sirris buff = hero.buff(Sunless.Sirris.class);
            if (buff != null) {
                dmg = buff.absorbDamage(dmg);
            }
        }

        if (isInvulnerable(src.getClass())) {
            sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));
            return new DamageResult(info.getBaseDamage(), Math.max(0, dmg), 0, 0, 0, 0, true);
        }

        if (!(src instanceof LifeLink) && buff(LifeLink.class) != null) {
            HashSet<LifeLink> links = buffs(LifeLink.class);
            for (LifeLink link : links.toArray(new LifeLink[0])) {
                if (Actor.findById(link.object) == null) {
                    links.remove(link);
                    link.detach();
                }
            }
            dmg = (int) Math.ceil(dmg / (float) (links.size() + 1));
            for (LifeLink link : links) {
                Char ch = (Char) Actor.findById(link.object);
                if (ch != null) {
                    ch.damage(DamageInfo.of(dmg, damageType, this, link));
                    if (!ch.isAlive()) {
                        link.detach();
                    }
                }
            }
        }

        Terror t = buff(Terror.class);
        if (t != null) {
            t.recover();
        }
        Dread d = buff(Dread.class);
        if (d != null) {
            d.recover();
        }
        Charm c = buff(Charm.class);
        if (c != null) {
            c.recover(src);
        }
        if (this.buff(Frost.class) != null) {
            Buff.detach(this, Frost.class);
        }
        if (this.buff(MagicalSleep.class) != null) {
            Buff.detach(this, MagicalSleep.class);
        }
        if (this.buff(Doom.class) != null && !isImmune(Doom.class)) {
            dmg *= 1.67f;
        }

        if (alignment != Alignment.ALLY && this.buff(DeathMark.DeathMarkTracker.class) != null) {
            dmg *= 1.25f;
        }

        if (buff(Sickle.HarvestBleedTracker.class) != null) {
            buff(Sickle.HarvestBleedTracker.class).detach();

            if (!isImmune(Bleeding.class)) {
                Bleeding b = buff(Bleeding.class);
                if (b == null) {
                    b = new Bleeding();
                }
                b.announced = false;
                b.set(dmg, Sickle.HarvestBleedTracker.class);
                b.attachTo(this);
                sprite.showStatus(CharSprite.WARNING, Messages.titleCase(b.name()) + " " + (int) b.level());
                return new DamageResult(info.getBaseDamage(), Math.max(0, dmg), 0, 0, 0, 0, false);
            }
        }

        for (ChampionEnemy buff : buffs(ChampionEnemy.class)) {
            dmg = (int) Math.ceil(dmg * buff.damageTakenFactor());
        }

        for (ChampionHero buff : buffs(ChampionHero.class)) {
            dmg = (int) Math.ceil(dmg * buff.damageTakenFactor());
        }

        // 伤害类型抗性：按 DamageType 的新层 + 现有基于来源类的旧层（乘算）
        int resistanceBlocked = 0;
        boolean immuneHit = false;
        if (damageType == DamageType.MIXED && info.isMixed() && info.getMixed() != null) {
            // 混合伤害：按各成分分别判定免疫/抗性后加权
            int before = dmg;
            float remaining = 0f;
            MixedDamage md = info.getMixed();
            boolean allImmune = true;
            for (int i = 0; i < md.size(); i++) {
                DamageType compType = md.typeAt(i);
                float pct = md.percentAt(i);
                if (isImmuneTo(compType) || isImmune(src.getClass())) {
                    continue; // 该成分被完全抵挡
                }
                allImmune = false;
                remaining += pct * resistanceTo(compType) * resist(src.getClass());
            }
            if (allImmune) {
                resistanceBlocked = dmg;
                dmg = 0;
                immuneHit = true;
            } else {
                dmg = Math.round(dmg * remaining);
                resistanceBlocked = Math.max(0, before - dmg);
            }
        } else if (damageType != DamageType.TRUE) {
            if (isImmuneTo(damageType) || isImmune(src.getClass())) {
                resistanceBlocked = dmg;
                dmg = 0;
                immuneHit = true;
            } else {
                int before = dmg;
                dmg = Math.round(dmg * resistanceTo(damageType) * resist(src.getClass()));
                resistanceBlocked = Math.max(0, before - dmg);
            }
        }

        //TODO improve this when I have proper damage source logic
        if (damageType.isMagical() && buff(ArcaneArmor.class) != null) {
            dmg -= combatRoll(0, buff(ArcaneArmor.class).level());
            if (dmg < 0) dmg = 0;
        }

        if (buff(Paralysis.class) != null) {
            buff(Paralysis.class).processDamage(dmg);
        }

        int shielded = dmg;
        int shieldingBeforeHit = shielding();

        if (!damageType.ignoresShields()) {
            for (ShieldBuff s : buffs(ShieldBuff.class)) {
                dmg = s.absorbDamage(dmg);
                if (dmg == 0) break;
            }

            //受衅怒火 2024-9-17
            // 受衅怒火 TheCatist 2026-8-8 仅有护盾从正值降低到 0 的时候才能触发
            if (HP > 0 && shielded > 0 && shieldingBeforeHit > 0 && shielding() == 0
                    && this instanceof Hero && ((Hero) this).hasTalent(Talent.PROVOKED_ANGER)
                    && buff(Talent.ProvokedAngerTracker.class) == null) {
                Buff.affect(this, Talent.ProvokedAngerTracker.class, 5f);
            }
        }
        shielded -= dmg;


        // DoggingDog on 20250511
        for (VitaeBuff s : buffs(VitaeBuff.class)) {
            dmg = s.absorbDamage(dmg);
        }

        // DoggingDog on 20250818
        if (hero.pointsInTalent(Talent.VITAE_BOOST) >= 4 && hero != null && !(src instanceof Hunger)) {
            if (hero.buff(VitaeBuff.class) != null) {
                dmg = Math.max(0, dmg - 2);
            }
        }

        int hpBefore = HP;
        if (this.buff(ImmortalShieldAffecter.ImmortalShield.class) == null) {
            HP -= Math.max(dmg, 0);

            // DoggingDog on 20250818
            if (dmg >= 1 && hero.hasTalent(Talent.BLOODY_VITAE) && this instanceof Hero && hero != null) {
                if (src instanceof Mob)
                    Buff.affect(hero, VitaeBuff.class).setVitae(2 + hero.pointsInTalent(Talent.BLOODY_VITAE));
            }
            //

        }

        if (HP > 0 && buff(Grim.GrimTracker.class) != null) {

            float finalChance = buff(Grim.GrimTracker.class).maxChance;
            finalChance *= (float) Math.pow(((HT - HP) / (float) HT), 2);

            if (Random.Float() < finalChance) {
                int extraDmg = Math.round(HP * resist(Grim.class));
                dmg += extraDmg;
                HP -= extraDmg;

                sprite.emitter().burst(ShadowParticle.UP, 5);
                if (!isAlive() && buff(Grim.GrimTracker.class).qualifiesForBadge) {
                    Badges.validateGrimWeapon();
                }
            }
        }

        // 角色最终伤害事件（伤害减免计算后）：携带实际造成的伤害
        int dealt = Math.max(0, hpBefore - Math.max(0, HP));
        if (dealt > 0) {
            Char attacker = src instanceof Char ? (Char) src : null;
            EventManager.emit(new CharFinalDamageEvent(this, attacker, src, dealt, damageType));
        }

        if (HP < 0 && src instanceof Char && alignment == Alignment.ENEMY) {
            if (((Char) src).buff(Kinetic.KineticTracker.class) != null) {
                int dmgToAdd = -HP;
                dmgToAdd -= ((Char) src).buff(Kinetic.KineticTracker.class).conservedDamage;
                dmgToAdd = Math.round(dmgToAdd * RingOfArcana.enchantPowerMultiplier((Char) src) * RingOfBenediction.periodMultiplier((Char) src));
                if (dmgToAdd > 0) {
                    Buff.affect((Char) src, Kinetic.ConservedDamage.class).setBonus(dmgToAdd);
                }
                ((Char) src).buff(Kinetic.KineticTracker.class).detach();
            }
        }


        if (sprite != null && !immuneHit) {
            // 免疫伤害（元素/来源免疫被完全抵挡）时不弹出 "0" 伤害数字
            // 图标以 DamageType 为准（含暴击）；以下仅保留 DamageType 无法表达的特例
            if (info.isMixed() && info.getMixed() != null) {
                // 混合伤害：按成分占比排序，左侧显示多个图标
                sprite.showStatusWithIcons(CharSprite.NEGATIVE, Integer.toString(dmg + shielded), info.getFloatingTextIcons());
            } else {
                int icon = info.getFloatingTextIcon();
                if (damageType == DamageType.PHYSICAL) {
                    // 狙击手远程攻击无视护甲
                    if (src == hero
                            && hero.subClass == HeroSubClasses.SNIPER
                            && !Dungeon.level.adjacent(hero.pos, pos)
                            && hero.belongings.attackingWeapon() instanceof MissileWeapon) {
                        icon = FloatingText.PHYS_DMG_NO_BLOCK;
                    }
                    if (src instanceof WhiteKingGodSword.OnlyOneEyeAttack) icon = FloatingText.PHYS_DMG_NO_BLOCK;
                }
                // 抗魔法刻印抵抗的来源类（与 DamageType 不完全一一对应，保留）
                if (AntiMagic.RESISTS.contains(src.getClass())) icon = FloatingText.MAGIC_DMG;
                if (src instanceof Deminion.DeminionCritClass) icon = FloatingText.CRIT_NO_BLOCK;

                sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(dmg + shielded), icon);
            }
        }

        if (HP < 0) HP = 0;
        lastDamageType = damageType;
        lastAttacker = src instanceof Char ? (Char) src : info.getAttacker();
        lastDamageCauseChain = info.getCauseChain();

        if (!isAlive()) {
            die(src);
        } else if (HP == 0 && buff(DeathMark.DeathMarkTracker.class) != null) {
            DeathMark.processFearTheReaper(this);
        }

        int hpDamage = Math.max(0, hpBefore - Math.max(0, HP));
        return new DamageResult(info.getBaseDamage(), info.getDamage(), armorBlocked, resistanceBlocked, shielded, hpDamage, immuneHit);
    }

    /**
     * 恢复生命值的统一入口。
     * <p>
     * 所有治疗来源（自然恢复、持续治疗、食物、药水、露水等）都应调用此方法，
     * 而不是直接修改 {@link #HP}。此方法在英雄恢复生命时发布 {@link HeroHealEvent}。
     *
     * @param amount 恢复量（内部会限制不超过最大生命值，并忽略非正数）
     * @return 本次实际恢复的生命值
     */
    public int heal(int amount) {
        return heal(amount, true);
    }

    /**
     * 恢复生命值的统一入口。
     * <p>
     * 所有治疗来源（自然恢复、持续治疗、食物、药水、露水等）都应调用此方法，
     * 而不是直接修改 {@link #HP}。此方法在英雄恢复生命时发布 {@link HeroHealEvent}。
     *
     * @param amount 恢复量（内部会限制不超过最大生命值，并忽略非正数）
     * @param showText 是否显示治疗绿字（自然恢复等小额/高频来源可传入 false 屏蔽）
     * @return 本次实际恢复的生命值
     */
    public int heal(int amount, boolean showText) {
        if (amount <= 0 || !isAlive() || HP >= HT) return 0;
        int before = HP;
        HP = Math.min(HT, HP + amount);
        int healed = HP - before;
        if (healed > 0) {
			AfterGlow.Warmth warmth = buff(AfterGlow.Warmth.class);
			if (!(this instanceof Hero) && warmth != null) {
				warmth.getWarmth();
			}
            // 统一在此显示治疗绿字
            if (showText && sprite != null) {
                sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healed), FloatingText.HEALING);
            }
            if (this == Dungeon.hero) {
                EventManager.emit(new HeroHealEvent(healed));
            }
        }
        return healed;
    }

    /** NPCs are invulnerable by default; special NPCs can opt into combat damage. */
    protected boolean isDamageable() {
        return !properties.contains(Property.NPC);
    }

    /** 最近一次受到伤害的 DamageType，供 die() 死亡特效等使用（避免 fromSource 类名猜测）。 */
    public DamageType lastDamageType = DamageType.PHYSICAL;

    /** 最近一次受到伤害的攻击者（可为 null）。投射物来源（如 LIGHTNING 弹）丢失凶手身份时，用它恢复。 */
    public Char lastAttacker = null;

    /** 最近一次受到伤害的来源链（有序因果对象），供 die()/死亡信息追踪使用。 */
    public List<Object> lastDamageCauseChain = new ArrayList<>();

    public void destroy() {
        HP = 0;
        Actor.remove(this);

        for (Char ch : Actor.chars().toArray(new Char[0])) {
            if (ch.buff(Charm.class) != null && ch.buff(Charm.class).object == id()) {
                ch.buff(Charm.class).detach();
            }
            if (ch.buff(Dread.class) != null && ch.buff(Dread.class).object == id()) {
                ch.buff(Dread.class).detach();
            }
            if (ch.buff(Terror.class) != null && ch.buff(Terror.class).object == id()) {
                ch.buff(Terror.class).detach();
            }
            if (ch.buff(SnipersMark.class) != null && ch.buff(SnipersMark.class).object == id()) {
                ch.buff(SnipersMark.class).detach();
            }
            if (ch.buff(Talent.FollowupStrikeTracker.class) != null
                    && ch.buff(Talent.FollowupStrikeTracker.class).object == id()) {
                ch.buff(Talent.FollowupStrikeTracker.class).detach();
            }
            if (ch.buff(Talent.DeadlyFollowupTracker.class) != null
                    && ch.buff(Talent.DeadlyFollowupTracker.class).object == id()) {
                ch.buff(Talent.DeadlyFollowupTracker.class).detach();
            }
        }
    }

    public void die(Object src) {
        destroy();
        if (src != Chasm.class && sprite != null) {
            // 如果 sprite 有待处理的死亡标记（shader 即将创建），跳过死亡动画。
            // dieAfterShader() 已经在 ShaderEffect.apply() 中同步调用过（shaderEffect
            // 是在渲染线程延迟设置的，因此这里不能依赖 getShaderEffect() 判空）。
            if (sprite.isPendingDeathAfterShader()) {
                // shader 会接管，不需要播放死亡动画
            } else if (sprite.getShaderEffect() == null) {
                sprite.die();
            } else {
                sprite.dieAfterShader();
            }
        }
        if (SnDSFX.active()) {
            SnDSFX.playSnDDeathSoundVariant(this);
        }
    }

    //we cache this info to prevent having to call buff(...) in isAlive.
    //This is relevant because we call isAlive during drawing, which has both performance
    //and thread coordination implications
    public boolean deathMarked = false;

    public boolean isAlive() {
        return HP > 0 || deathMarked;
    }

    public boolean isActive() {
        return isAlive();
    }

    @Override
    protected void spendConstant(float time) {
        TimekeepersHourglass.timeFreeze freeze = buff(TimekeepersHourglass.timeFreeze.class);
        if (freeze != null) {
            freeze.processTime(time);
            return;
        }

        Swiftthistle.TimeBubble bubble = buff(Swiftthistle.TimeBubble.class);
        if (bubble != null) {
            bubble.processTime(time);
            return;
        }

        super.spendConstant(time);
    }

    @Override
    protected void spend(float time) {

        float timeScale = 1f;
        if (buff(Slow.class) != null) {
            timeScale *= 0.5f;
            //slowed and chilled do not stack
        } else if (buff(Chill.class) != null) {
            timeScale *= buff(Chill.class).speedFactor();
        }
        if (buff(Speed.class) != null) {
            timeScale *= 2.0f;
        }

        super.spend(time / timeScale);
    }

    public synchronized LinkedHashSet<Buff> buffs() {
        return new LinkedHashSet<>(buffs);
    }

    @SuppressWarnings("unchecked")
    //returns all buffs assignable from the given buff class
    public synchronized <T extends Buff> HashSet<T> buffs(Class<T> c) {
        HashSet<T> filtered = new HashSet<>();
        for (Buff b : buffs) {
            if (c.isInstance(b)) {
                filtered.add((T) b);
            }
        }
        return filtered;
    }

    @SuppressWarnings("unchecked")
    //returns an instance of the specific buff class, if it exists. Not just assignable
    public synchronized <T extends Buff> T buff(Class<T> c) {
        for (Buff b : buffs) {
            if (b.getClass() == c) {
                return (T) b;
            }
        }
        return null;
    }

    public synchronized boolean isCharmedBy(Char ch) {
        int chID = ch.id();
        for (Buff b : buffs) {
            if (b instanceof Charm && ((Charm) b).object == chID) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean add(Buff buff) {

        if (buff(PotionOfCleansing.Cleanse.class) != null) { //cleansing buff
            if (buff.type == Buff.buffType.NEGATIVE
                    && !(buff instanceof AllyBuff)
                    && !(buff instanceof LostInventory)) {
                return false;
            }
        }

        // Masamune buff func
        // date : 20250418
        // by DoggingDog
        if (buff(Masamune.MasamuneBless.class) != null && (buff instanceof Hex || buff instanceof Vertigo)) {
            return false;
        }

        if (sprite != null && buff(Challenge.SpectatorFreeze.class) != null) {
            return false; //can't add buffs while frozen and game is loaded
        }

        buffs.add(buff);
        if (Actor.chars().contains(this)) Actor.add(buff);

        if (sprite != null && buff.announced) {
            switch (buff.type) {
                case POSITIVE:
                    sprite.showStatus(CharSprite.POSITIVE, Messages.titleCase(buff.name()));
                    break;
                case NEGATIVE:
                    sprite.showStatus(CharSprite.WARNING, Messages.titleCase(buff.name()));
                    break;
                case NEUTRAL:
                default:
                    sprite.showStatus(CharSprite.NEUTRAL, Messages.titleCase(buff.name()));
                    break;
            }
        }

        return true;

    }

    public synchronized boolean remove(Buff buff) {

        buffs.remove(buff);
        Actor.remove(buff);

        return true;
    }

    public synchronized void remove(Class<? extends Buff> buffClass) {
        for (Buff buff : buffs(buffClass)) {
            remove(buff);
        }
    }

    @Override
    protected synchronized void onRemove() {
        for (Buff buff : buffs.toArray(new Buff[buffs.size()])) {
            buff.detach();
        }
    }

    public synchronized void updateSpriteState() {
        for (Buff buff : buffs) {
            buff.fx(true);
        }
    }

    public float stealth() {
        return 0;
    }

    public final void move(int step) {
        move(step, true);
    }

    //travelling may be false when a character is moving instantaneously, such as via teleportation
    public void move(int step, boolean travelling) {

        if (travelling && Dungeon.level.adjacent(step, pos) && buff(Vertigo.class) != null) {
            sprite.interruptMotion();
            int newPos = pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
            if (!(Dungeon.level.passable[newPos] || Dungeon.level.avoid[newPos])
                    || (properties().contains(Property.LARGE) && !Dungeon.level.openSpace[newPos])
                    || Actor.findChar(newPos) != null)
                return;
            else {
                sprite.move(pos, newPos);
                step = newPos;
            }
        }

        if (Dungeon.level.map[pos] == Terrain.OPEN_DOOR) {
            Door.leave(pos);
        }

        pos = step;

        if (this != hero) {
            sprite.visible = Dungeon.level.heroFOV[pos];
        }

        Dungeon.level.occupyCell(this);
    }

    public int distance(Char other) {
        return Dungeon.level.distance(pos, other.pos);
    }

    public boolean[] modifyPassable(boolean[] passable) {
        //do nothing by default, but some chars can pass over terrain that others can't
        return passable;
    }

    public void onMotionComplete() {
        //Does nothing by default
        //The main actor thread already accounts for motion,
        // so calling next() here isn't necessary (see Actor.process)
    }

    public void onAttackComplete() {
        next();
    }

    public void onOperateComplete() {
        next();
    }

    protected final HashSet<Class> resistances = new HashSet<>();

    //returns percent effectiveness after resistances
    //TODO currently resistances reduce effectiveness by a static 50%, and do not stack.
    public float resist(Class effect) {
        HashSet<Class> resists = new HashSet<>(resistances);
        for (Property p : properties()) {
            resists.addAll(p.resistances());
        }
        for (Buff b : buffs()) {
            resists.addAll(b.resistances());
        }

        float result = 1f;
        for (Class c : resists) {
            if (c.isAssignableFrom(effect)) {
                result *= 0.5f;
            }
        }

        if (this instanceof Hero && ((Hero) this).hasTalent(Talent.IRON_MUSCLE)) {
            int lvl = ((Hero) this).pointsInTalent(Talent.IRON_MUSCLE);
            if (Bleeding.class.isAssignableFrom(effect)) {
                result *= 0.5f;
            }
            if (Cripple.class.isAssignableFrom(effect) && lvl > 1) {
                result *= 0.5f;
            }
            if (Blindness.class.isAssignableFrom(effect) && lvl > 2) {
                result *= 0.5f;
            }
        }


        return result * RingOfElements.resist(this, effect);
    }

    protected final HashSet<Class> immunities = new HashSet<>();

    public boolean isImmune(Class effect) {
		Armor wornArmor = armor();
		if (effect == Burning.class && wornArmor != null
				&& wornArmor.hasGlyph(Brimstone.class, this)) {
			return true;
		}
        HashSet<Class> immunes = new HashSet<>(immunities);
        for (Property p : properties()) {
            immunes.addAll(p.immunities());
        }
        for (Buff b : buffs()) {
            immunes.addAll(b.immunities());
        }

        for (Class c : immunes) {
            if (c.isAssignableFrom(effect)) {
                return true;
            }
        }
        return false;
    }

    // ========== 按 DamageType 的伤害抗性层 ==========
    // 用于伤害管线。状态/时长抗性仍走上面的 resist(Class)/isImmune(Class)。

    /** 按伤害类型的百分比减免（0 表示无减免，0.5 表示减半，1 表示免疫）。 */
    protected final HashMap<DamageType, Float> typeResistances = new HashMap<>();

    /** 按伤害类型的免疫集合。 */
    protected final HashSet<DamageType> typeImmunities = new HashSet<>();

    /** 汇总本角色按伤害类型的抗性（含 Property 与 Buff 贡献）。 */
    public HashMap<DamageType, Float> typeResistances() {
        HashMap<DamageType, Float> out = new HashMap<>(typeResistances);
        for (Property p : properties()) {
            for (Map.Entry<DamageType, Float> e : p.typeResistances().entrySet()) {
                out.merge(e.getKey(), e.getValue(), Float::min);
            }
        }
        for (Buff b : buffs()) {
            for (Map.Entry<DamageType, Float> e : b.typeResistances().entrySet()) {
                out.merge(e.getKey(), e.getValue(), Float::min);
            }
        }
        return out;
    }

    /** 汇总本角色按伤害类型的免疫（含 Property 与 Buff 贡献）。 */
    public HashSet<DamageType> typeImmunities() {
        HashSet<DamageType> out = new HashSet<>(typeImmunities);
        for (Property p : properties()) {
            out.addAll(p.typeImmunities());
        }
        for (Buff b : buffs()) {
            out.addAll(b.typeImmunities());
        }
        return out;
    }

    /**
     * 按伤害类型返回减免系数（0..1）。TRUE 不经过本方法。
     */
    public float resistanceTo(DamageType type) {
        float result = 1f;
        for (Map.Entry<DamageType, Float> e : typeResistances().entrySet()) {
            if (e.getKey() == type) {
                result *= clampEffectiveness(e.getValue());
            }
        }
        return result;
    }

    /**
     * 按伤害类型判定免疫。TRUE 不经过本方法。
     */
    public boolean isImmuneTo(DamageType type) {
        return typeImmunities().contains(type);
    }

    private static float clampEffectiveness(float v) {
        if (v <= 0f) return 0f;
        return Math.min(1f, v);
    }

    //similar to isImmune, but only factors in damage.
    //Is used in AI decision-making
    public boolean isInvulnerable(Class effect) {
        return buff(Challenge.SpectatorFreeze.class) != null;
    }

    public HashSet<Property> properties = new HashSet<>();

    public HashSet<Property> properties() {
        HashSet<Property> props = new HashSet<>(properties);
        //TODO any more of these and we should make it a property of the buff, like with resistances/immunities
        if (buff(ChampionEnemy.Giant.class) != null) {
            props.add(Property.LARGE);
        }
        return props;
    }

    public enum Property {
        BOSS(new HashSet<Class>(Arrays.asList(Grim.class, GrimTrap.class, ScrollOfRetribution.class, ScrollOfPsionicBlast.class)),
                new HashSet<Class>(Arrays.asList(AllyBuff.class, Dread.class))),
        MINIBOSS(new HashSet<Class>(),
                new HashSet<Class>(Arrays.asList(AllyBuff.class, Dread.class))),
        BOSS_MINION,
        UNDEAD,
        NPC,
        DEMONIC,

        INORGANIC(new HashSet<Class>(),
                new HashSet<Class>(Arrays.asList(Bleeding.class, ToxicGas.class, Poison.class))),
        FIERY(new HashSet<Class>(),
                new HashSet<Class>(Arrays.asList(Burning.class, Blazing.class)),
                typeRes(DamageType.FIRE)),
        ICY(new HashSet<Class>(),
                new HashSet<Class>(Arrays.asList(Frost.class, Chill.class)),
                typeRes(DamageType.FROST)),
        ACIDIC(new HashSet<Class>(),
                new HashSet<Class>(Arrays.asList(Ooze.class)),
                typeRes(DamageType.CORROSIVE)),
        ELECTRIC(new HashSet<Class>(),
                new HashSet<Class>(),
                typeRes(DamageType.LIGHTNING)),
        LARGE,
        IMMOVABLE(new HashSet<Class>(),
                new HashSet<Class>(Arrays.asList(Vertigo.class))),
        //A character that acts in an unchanging manner. immune to AI state debuffs or stuns/slows
        STATIC(new HashSet<Class>(),
                new HashSet<Class>(Arrays.asList(AllyBuff.class, Dread.class, Terror.class, Amok.class, Charm.class, Sleep.class,
                        Paralysis.class, Frost.class, Chill.class, Slow.class, Speed.class))),
        ELITES,

        HEADLESS,
        EYES,
        GNOLL,
        JAIL_INHABITANT;

        private HashSet<Class> resistances;
        private HashSet<Class> immunities;
        private HashMap<DamageType, Float> typeResistances = new HashMap<>();
        private HashSet<DamageType> typeImmunities = new HashSet<>();

        Property() {
            this(new HashSet<Class>(), new HashSet<Class>());
        }

        Property(HashSet<Class> resistances, HashSet<Class> immunities) {
            this.resistances = resistances;
            this.immunities = immunities;
        }

        Property(HashSet<Class> resistances, HashSet<Class> immunities, HashMap<DamageType, Float> typeResistances) {
            this.resistances = resistances;
            this.immunities = immunities;
            this.typeResistances = typeResistances;
        }

        /** 构造一个按 DamageType 的 50% 减免表（元素抗性迁移用）。 */
        private static HashMap<DamageType, Float> typeRes(DamageType t) {
            HashMap<DamageType, Float> m = new HashMap<>();
            m.put(t, 0.5f);
            return m;
        }

        public HashSet<Class> resistances() {
            return new HashSet<>(resistances);
        }

        public HashSet<Class> immunities() {
            return new HashSet<>(immunities);
        }

        public HashMap<DamageType, Float> typeResistances() {
            return new HashMap<>(typeResistances);
        }

        public HashSet<DamageType> typeImmunities() {
            return new HashSet<>(typeImmunities);
        }

    }

    public static boolean hasProp(Char ch, Property p) {
        return (ch != null && ch.properties().contains(p));
    }
}
