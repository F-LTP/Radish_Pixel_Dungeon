package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.WandOfScanningBeam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.CorrosionParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfBenediction;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Elastic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/*
    class :TacticalThrowTalent4Battlemage
       func: onThrow
         p: staff,attacker,defender,damage
            staff:    Magestaff;
            attacker: usually Hero;
            defender: usually target;
            damage:   involve the effect of talent
         description: act if Battlemage use missle weapon with War_throw talent
 */
public class TacticalThrowTalen4Battlemage {
    TacticalThrowTalen4Battlemage() {
    }

    ;

    public static void onThrow(MagesStaff staff, Char attacker, Char defender, int damage) {

        int lvOfTalent = Dungeon.hero.pointsInTalent(Talent.WAR_THROW);
        Wand wand = staff.wand;

        // if Mystical Charge exist
        if (attacker instanceof Hero && ((Hero) attacker).hasTalent(Talent.MYSTICAL_CHARGE)){
            Hero hero = (Hero) attacker;
            for (Buff b : hero.buffs()){
                if (b instanceof Artifact.ArtifactBuff && !((Artifact.ArtifactBuff) b).isCursed() ) {
                    ((Artifact.ArtifactBuff) b).charge(hero, hero.pointsInTalent(Talent.MYSTICAL_CHARGE)/2f);
                    SpellSprite.show( attacker, SpellSprite.CHARGE, 0, 1, 1 );
                }
            }
        }


        if (wand == null) {
            //null
        } else if (wand instanceof WandOfBlastWave) {
            Actor.addDelayed(new Actor() {
                {
                    actPriority = VFX_PRIO - 1; //act after pushing effects
                }

                @Override
                protected boolean act() {
                    Actor.remove(this);
                    if (defender.isAlive()) {
                        new BlastWaveOnThrow().proc(staff, attacker, defender, damage);
                    }
                    return true;
                }
            }, -1);
        } else if (wand instanceof WandOfCorruption) {
            int level = Math.max(0, wand.buffedLvl());
            float procChance = (level + 1f) / (level + 4f) * procChanceMultiplier(attacker);
            float powerMulti = Math.max(1f, procChance);
            if (Random.Float() < procChance) {
                Buff.prolong(defender, Amok.class, Math.round((4 + level * 2) * powerMulti));
            }
        } else if (wand instanceof WandOfFireblast) {
            new FireBlastOnThrow().proc(staff, attacker, defender, damage);
        } else if (wand instanceof WandOfCorrosion) {
            int level = Math.max(0, wand.buffedLvl());
            float procChance = (level + 1f) / (level + 3f) * procChanceMultiplier(attacker);
            if (Random.Float() < procChance) {
                float powerMulti = Math.max(1f, procChance);
                Buff.affect(defender, Ooze.class).set(Ooze.DURATION * powerMulti);
                CellEmitter.center(defender.pos).burst(CorrosionParticle.SPLASH, 5);
            }
        } else if (wand instanceof WandOfFrost) {
            Chill chill = defender.buff(Chill.class);

            if (chill != null) {

                //1/9 at 2 turns of chill, scaling to 9/9 at 10 turns
                float procChance = ((int) Math.floor(chill.cooldown()) - 1) / 9f;
                procChance *= procChanceMultiplier(attacker);

                if (Random.Float() < procChance) {

                    float powerMulti = Math.max(1f, procChance);

                    //need to delay this through an actor so that the freezing isn't broken by taking damage from the staff hit.
                    new FlavourBuff() {
                        {
                            actPriority = VFX_PRIO;
                        }

                        public boolean act() {
                            Buff.affect(target, Frost.class, Math.round(Frost.DURATION * powerMulti));
                            return super.act();
                        }
                    }.attachTo(defender);
                }
            }

        } else if (wand instanceof WandOfLightning) {
            new LightningOnThrow().proc(staff, attacker, defender, damage);

        } else if (wand instanceof WandOfLivingEarth) {
            //Todo :
            // I'm not willing to change the access of Buff(RockArmor) in LivingEarth Wand
            // the only way I came up with is adding new one.It require translation.
            WandOfLivingEarth.EarthGuardian guardian = null;
            for (Mob m : Dungeon.level.mobs) {
                if (m instanceof WandOfLivingEarth.EarthGuardian) {
                    guardian = (WandOfLivingEarth.EarthGuardian) m;
                    break;
                }
            }

            int armor = Math.round(damage * 0.33f * procChanceMultiplier(attacker));
            Buff ben = Dungeon.hero.buff(RingOfBenediction.Benediction.class);
            if (ben != null) {
                armor = Math.round(armor * RingOfBenediction.periodMultiplier(Dungeon.hero));
            }
            if (guardian != null) {
                guardian.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, 8 + wand.buffedLvl() / 2);
                guardian.setInfo(Dungeon.hero, wand.buffedLvl(), armor);
                int extra = armor - guardian.HT;
                if (extra > 0)
                    Buff.affect(guardian, Barrier.class).setShield(extra);
            } else {
                attacker.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, 8 + wand.buffedLvl() / 2);
                Buff.affect(attacker, RockArmor.class).addArmor(wand.buffedLvl(), armor);
            }

        } else if (wand instanceof WandOfPrismaticLight) {
            Buff.prolong(defender, Cripple.class, Math.round((1 + staff.buffedLvl()) * procChanceMultiplier(attacker)));

        } else if (wand instanceof WandOfWarding) {
            int level = Math.max(0, staff.buffedLvl());
            float procChance = (level + 1f) / (level + 5f) * procChanceMultiplier(attacker);
            if (Random.Float() < procChance) {

                float powerMulti = Math.max(1f, procChance);

                for (Char ch : Actor.chars()) {
                    if (ch instanceof WandOfWarding.Ward) {
                        ((WandOfWarding.Ward) ch).wandHeal(staff.buffedLvl(), powerMulti);
                        ch.sprite.emitter().burst(MagicMissile.WardParticle.UP, ((WandOfWarding.Ward) ch).tier);
                    }
                }
            }

        } else if (wand instanceof WandOfRegrowth) {
            //like pre-nerf vampiric enchantment, except with herbal healing buff, only in grass
            boolean grass = false;
            int terr = Dungeon.level.map[attacker.pos];
            if (terr == Terrain.GRASS || terr == Terrain.HIGH_GRASS || terr == Terrain.FURROWED_GRASS) {
                grass = true;
            }
            terr = Dungeon.level.map[defender.pos];
            if (terr == Terrain.GRASS || terr == Terrain.HIGH_GRASS || terr == Terrain.FURROWED_GRASS) {
                grass = true;
            }

            if (grass) {
                int level = Math.max(0, staff.buffedLvl());
                int healing = Math.round(damage * (level + 2f) / (level + 6f) / 2f);
                healing = Math.round(healing * procChanceMultiplier(attacker));
                Buff.affect(attacker, Sungrass.Health.class).boost(healing);
            }
        } else if (wand instanceof WandOfTransfusion) {
            if (defender.buff(Charm.class) != null && defender.buff(Charm.class).object == attacker.id()) {
                // Todo : It's troublesome to change the Wand Code so I ingore it temporarily
                //freeCharge = true;
                Buff.affect(attacker, Barrier.class).setShield(Math.round((2 * (5 + wand.buffedLvl())) * procChanceMultiplier(attacker)));
                //GLog.p( Messages.get(this, "charged") );
                attacker.sprite.emitter().burst(BloodParticle.BURST, 20);
            }

        } else if (wand instanceof WandOfScanningBeam) {
            // null
        } else if (wand instanceof WandOfMagicMissile) {
            SpellSprite.show(attacker, SpellSprite.CHARGE);
            for (Wand.Charger c : attacker.buffs(Wand.Charger.class)){
                if (!(c.wand() instanceof WandOfMagicMissile)){
                    c.gainCharge(0.5f * procChanceMultiplier(attacker));
                }
            }

        }
    }

    public static float procChanceMultiplier(Char attacker) {
        if (attacker.buff(Talent.EmpoweredStrikeTracker.class) != null) {
            return (1f + ((Hero) attacker).pointsInTalent(Talent.EMPOWERED_STRIKE) / 2f) * Dungeon.hero.pointsInTalent(Talent.WAR_THROW) * 0.5f;
        }
        return 1f * Dungeon.hero.pointsInTalent(Talent.WAR_THROW) * 0.5f;
    }

    private static class BlastWaveOnThrow extends Elastic {
        @Override
        protected float procChanceMultiplier(Char attacker) {
            return TacticalThrowTalen4Battlemage.procChanceMultiplier(attacker);
        }
    }

    private static class FireBlastOnThrow extends Blazing {
        @Override
        protected float procChanceMultiplier(Char attacker) {
            return TacticalThrowTalen4Battlemage.procChanceMultiplier(attacker);
        }
    }

    private static class LightningOnThrow extends Shocking {
        @Override
        protected float procChanceMultiplier(Char attacker) {
            return TacticalThrowTalen4Battlemage.procChanceMultiplier(attacker);
        }
    }


    public static class RockArmor extends Buff {

        {
            type = buffType.POSITIVE;
        }

        private int wandLevel;
        private int armor;

        private void addArmor(int wandLevel, int toAdd) {
            this.wandLevel = Math.max(this.wandLevel, wandLevel);
            armor += toAdd;
            armor = Math.min(armor, 2 * armorToGuardian());
        }

        private int armorToGuardian() {
            return 8 + wandLevel * 4;
        }

        public int absorb(int damage) {
            int block = damage - damage / 2;
            if (armor <= block) {
                detach();
                return damage - armor;
            } else {
                armor -= block;
                return damage - block;
            }
        }

        @Override
        public int icon() {
            return BuffIndicator.ARMOR;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.brightness(0.6f);
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (armorToGuardian() - armor) / (float) armorToGuardian());
        }

        @Override
        public String iconTextDisplay() {
            return Integer.toString(armor);
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", armor, armorToGuardian());
        }

        private static final String WAND_LEVEL = "wand_level";
        private static final String ARMOR = "armor";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(WAND_LEVEL, wandLevel);
            bundle.put(ARMOR, armor);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            wandLevel = bundle.getInt(WAND_LEVEL);
            armor = bundle.getInt(ARMOR);
        }
    }

}
