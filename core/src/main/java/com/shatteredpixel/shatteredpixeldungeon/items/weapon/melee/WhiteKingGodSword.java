package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;

public class WhiteKingGodSword extends MeleeWeapon {

    {
        tier = 3;
        image = ItemSpriteSheet.WHITE_KING_GOD_SWORD;
    }

    @Override
    public int min(int lvl) {
        return 3 + lvl * 2;
    }

    @Override
    public boolean doEquip(Hero hero) {
        if(hero.belongings.weapon instanceof WhiteKingGodSword){
            GLog.n(Messages.get(this, "already_wielding"));
            return false;
        }
        // 装备可能失败（手里是脱不下来的诅咒武器、村正狂热），确认装上了再挂 buff。
        // 没装上却挂着 buff 时，视野里的敌人会被白白标记成「已经被圣剑打过」
        if (super.doEquip(hero)) {
            Buff.affect(hero, OnlyOneEyeAttack.class);
            return true;
        }
        return false;
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single){
        // 同理，圣剑被诅咒卸不下来时 buff 不能摘，否则剑还在手上、效果却没了
        if (super.doUnequip(hero, collect, single)) {
            if(hero.buff(OnlyOneEyeAttack.class) != null)
                hero.buff(OnlyOneEyeAttack.class).detach();
            return true;
        }
        return false;
    }

    @Override
    public int max(int lvl) {
        return 16 + lvl * 3;
    }

    public static class OnlyOneEyeAttack extends Buff {
        {
            type = buffType.POSITIVE;
        }

        @Override
        public boolean act() {
            Hero hero = (Hero) target;
            WhiteKingGodSword weapon = null;
            if (hero.belongings.weapon instanceof WhiteKingGodSword) {
                weapon = (WhiteKingGodSword) hero.belongings.weapon;
            } else if (hero.belongings.weapon != null
                    && hero.belongings.weapon.fusedItems != null) {
                for (com.shatteredpixel.shatteredpixeldungeon.items.Item it : hero.belongings.weapon.fusedItems) {
                    if (it instanceof WhiteKingGodSword) {
                        weapon = (WhiteKingGodSword) it;
                        break;
                    }
                }
            }
            if (weapon == null) {
                detach();
                return true;
            }
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                if (!mob.isAlive() || mob.alignment != Char.Alignment.ENEMY
                        || mob.eyeAttack || !Dungeon.level.heroFOV[mob.pos]) {
                    continue;
                }
                FallingSword strike = new FallingSword();
                int baseDamage = (int) (weapon.damageRoll(hero) * 0.6f);
                float augment = weapon.augment == Augment.DAMAGE ? 1.5f
                        : weapon.augment == Augment.SPEED ? 0.7f : 1f;
                strike.damage = Math.round(baseDamage * augment + 0.1f * weapon.buffedLvl());
                strike.weapon = weapon;
                if (strike.scheduleOn(mob)) {
                    mob.eyeAttack = true;
                }
            }
            spend(TICK);
            return true;
        }
    }

    public static class FallingSword extends Buff {
        {
            actPriority = VFX_PRIO;
        }

        private WhiteKingGodSword weapon;
        private int damage;

        private boolean scheduleOn(Char victim) {
            if (!attachTo(victim)) {
                return false;
            }
            postpone(TICK);
            return true;
        }

        @Override
        public boolean act() {
            detach();
            Hero hero = Dungeon.hero;
            if (hero != null && hero.isAlive() && target.isAlive() && weapon != null) {
                int enchantedDamage = weapon.proc(hero, target, damage);
                if (target.isAlive()) {
                    target.damage(new DamageInfo(enchantedDamage, DamageType.PHYSICAL_NO_ARMOR, hero, weapon, this)
                            .setFloatingTextIcon(FloatingText.PHYS_DMG_NO_BLOCK));
                }
            }
            return true;
        }

        @Override
        public void fx(boolean on) {
            if (on && target.sprite.parent != null) {
                ((MissileSprite) target.sprite.parent.recycle(MissileSprite.class))
                        .resetFromAbove(target, target.pos, new WKNOR(), null);
            }
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put("damage", damage);
            Bundle weaponState = new Bundle();
            weapon.storeInBundle(weaponState);
            weaponState.remove("quickslotpos");
            bundle.put("weapon", weaponState);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            damage = bundle.getInt("damage");
            weapon = new WhiteKingGodSword();
            weapon.restoreFromBundle(bundle.getBundle("weapon"));
        }
    }

    public String desc() {
    		String s;
    		if(Dungeon.hero != null){
    			int min = Math.round(((min() * 0.6f) * (augment == Augment.DAMAGE ? 1.5f : (augment == Augment.SPEED ? 0.7f : 1f))) + 0.1f * buffedLvl());
    			int max = Math.round(((max() * 0.6f) * (augment == Augment.DAMAGE ? 1.5f : (augment == Augment.SPEED ? 0.7f : 1f))) + 0.1f * buffedLvl());
    			s = Messages.get(this, "desc", min,max);
    		} else {
    			s = Messages.get(this, "desc", 0,0);
    		}
       
        return s;
    }

    public static class WKNOR extends WhiteKingGodSword{
        @Override
        public Emitter emitter() {
            Emitter e = new Emitter();
            e.pos(0, 10);
            e.fillTarget = false;
            e.pour(MagicMissile.MagicParticle.ATTRACTING, 0.05f);
            return e;
        }
    }

}
