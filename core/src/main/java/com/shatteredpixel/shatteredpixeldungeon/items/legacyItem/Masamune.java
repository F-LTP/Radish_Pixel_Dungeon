package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;

// 正宗
public class Masamune extends MeleeWeapon {
    {
        image = ItemSpriteSheet.KATANA;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;
        tier = 1;
    }

    @Override
    public int min(int lvl) {
        return 8+lvl*2;
    }

    @Override
    public int max(int lvl) {
        return 20+lvl*4;
    }


    @Override
    public int proc(Char attacker, Char defender, int damage ) {
            if (defender.properties().contains(Char.Property.DEMONIC) || defender.properties().contains(Char.Property.UNDEAD)) {
                defender.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10 + buffedLvl());
                Sample.INSTANCE.play(Assets.Sounds.BURNING);
                defender.damage(Math.round(damage * 0.35f), this);
                return (int) (damage * 1.35f);
            }
            return damage;
    }

    @Override
    public boolean doEquip( Hero hero ) {
        boolean ParentDoEquip = super.doEquip(hero);
        Buff.affect(hero, MasamuneBless.class);
        return ParentDoEquip;
    }


    public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
        boolean ParentDoUnEquip =  super.doUnequip(hero,collect,single);
        if(hero.buff(MasamuneBless.class) != null)
            Buff.detach(hero, MasamuneBless.class);
        return ParentDoUnEquip;
    }


    public static class MasamuneBless extends Buff {
        {
            type = buffType.POSITIVE;
            announced = true;
        }

        @Override
        public void detach() {
            super.detach();
            updateQuickslot();
        }

        @Override
        public String icon() {
            return BuffIndicator.BLESS;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0.2f, 0.6f, 1f);
        }

    }
}
