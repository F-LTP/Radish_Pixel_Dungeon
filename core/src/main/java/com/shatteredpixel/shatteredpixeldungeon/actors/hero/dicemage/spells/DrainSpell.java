package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/**
 * 汲取（血液学派 L3）：击杀1个具有血液的盟友（镜像不可，土石不可），恢复19-38生命。
 */
public class DrainSpell extends DiceMageSpell {

    @Override
    public Talent school() {
        return Talent.SCHOOL_BLOOD;
    }

    @Override
    public int level() {
        return 3;
    }

    @Override
    public int mpCost() {
        return 1;
    }
    @Override
    public String sndImageName() {
        return "leech";
    }



    private boolean hasBlood(Char ch) {
        if (ch == null || ch == Dungeon.hero) return false;
        if (ch instanceof MirrorImage) return false;
        if (Char.hasProp(ch, Char.Property.INORGANIC)) return false;
        if (Char.hasProp(ch, Char.Property.IMMOVABLE)) return false;
        return true;
    }

    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                Char target = Actor.findChar(cell);
                if (!isValidAlly(target) || !hasBlood(target)) {
                    GLog.w(Messages.get(DrainSpell.this, "invalid_target"));
                    return;
                }
                if (!spendMagic(hero)) return;

                int heal = Random.IntRange(19, 38);
                target.damage(new DamageInfo(target.HP, DamageType.TRUE, hero, null, DrainSpell.this));
                CellEmitter.center(target.pos).burst(BloodParticle.BURST, 12);
                int oldHP = hero.HP;
                hero.HP = Math.min(hero.HT, hero.HP + heal);
                hero.sprite.showStatus(CharSprite.POSITIVE, "+%d", hero.HP - oldHP);
                CellEmitter.center(hero.pos).start(Speck.factory(Speck.HEALING), 0.12f, 3);
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(DrainSpell.this, "prompt");
            }
        });
    }
}
