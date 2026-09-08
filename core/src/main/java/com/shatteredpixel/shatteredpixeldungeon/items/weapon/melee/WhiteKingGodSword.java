package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;

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
        Buff.affect(hero, OnlyOneEyeAttack.class);
        return super.doEquip(hero);
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single){
        if(hero.buff(OnlyOneEyeAttack.class) != null)
            hero.buff(OnlyOneEyeAttack.class).detach();
        return super.doUnequip(hero, collect, single);
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
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ENEMY && !mob.eyeAttack) {
                    if (Dungeon.hero.belongings.weapon instanceof WhiteKingGodSword) {
                    			WhiteKingGodSword weapon = (WhiteKingGodSword) Dungeon.hero.belongings.weapon;
                    			int vault_damage = (int) (weapon.damageRoll(Dungeon.hero) * 0.6f);

                    			int damage = Math.round(vault_damage * (weapon.augment == Augment.DAMAGE ? 1.5f : (weapon.augment == Augment.SPEED ? 0.7f : 1f)) + 0.1f * weapon.buffedLvl());

                        Buff.affect(mob, Paralysis.class, 100f);
                        ((MissileSprite)target.sprite.parent.recycle( MissileSprite.class )).resetFromAbove(mob,mob.pos, new WKNOR(), new Callback() {
                            @Override
                            public void call() {
                                int enchantedDamage = weapon.proc(Dungeon.hero, mob, damage);
                                mob.damage(DamageInfo.of(enchantedDamage, DamageType.PHYSICAL, Dungeon.hero, this));
                                Buff.detach( mob, Paralysis.class);
                            }
                        });
                    }
                    mob.eyeAttack = true;
                }
            }
            spend(TICK);
            return true;
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
