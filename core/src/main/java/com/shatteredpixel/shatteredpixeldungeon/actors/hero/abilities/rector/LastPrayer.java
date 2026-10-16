package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rector;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AnkhInvulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

// DoggingDog on 20251011
// 护甲技能：终末圣祷
public class LastPrayer extends ArmorAbility {
    {
        baseChargeUse = 50;
    }

    @Override
    protected void activate(ClassArmor armor, Hero hero, Integer target) {

        // reg to store hp remain
        int HpRemain = hero.HP - 1;

        // set Hp to 1
        hero.HP = 1;

        // Jump
        if (target != null) {

            if (hero.rooted) {
                PixelScene.shake(1, 1f);
                return;
            }

            Ballistica route = new Ballistica(hero.pos, target, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
            int cell = route.collisionPos;

            //can't occupy the same cell as another char, so move back one.
            int backTrace = route.dist - 1;
            while (Actor.findChar(cell) != null && cell != hero.pos) {
                cell = route.path.get(backTrace);
                backTrace--;
            }

            armor.charge -= chargeUse(hero);
            armor.updateQuickslot();

            final int dest = cell;
            hero.busy();
            hero.sprite.jump(hero.pos, cell, new Callback() {
                @Override
                public void call() {
                    hero.move(dest);
                    Dungeon.level.occupyCell(hero);
                    Dungeon.observe();
                    GameScene.updateFog();

                    for (int i : PathFinder.NEIGHBOURS8) {
                        Char mob = Actor.findChar(hero.pos + i);
                        if (mob != null && mob != hero && mob.alignment != Char.Alignment.ALLY) {
                            if (mob.pos == hero.pos + i && hero.hasTalent(Talent.IMPACT_WAVE)) {
                                Ballistica trajectory = new Ballistica(mob.pos, mob.pos + i, Ballistica.MAGIC_BOLT);
                                int strength = 1 + hero.pointsInTalent(Talent.IMPACT_WAVE);
                                WandOfBlastWave.throwChar(mob, trajectory, strength, true, true, LastPrayer.this);
                            }
                        }
                    }

                    WandOfBlastWave.BlastWave.blast(dest);
                    PixelScene.shake(2, 0.5f);

                    Invisibility.dispel();
                    hero.spendAndNext(Actor.TICK);
                }
            });
        }

        // set Invulnerable
        Buff.prolong(hero, AnkhInvulnerability.class, 2f);

        // set heal
        int Lv = hero.pointsInTalent(Talent.EFFICIENT_HEALING);
        Buff.affect(hero, Healing.class).setHeal(30 + 5 + Lv * 5, ((float) (1/hero.HT)) * (1f + (Lv+1) * 0.25f), 0);

        // set haste
        if(hero.hasTalent(Talent.INERTIAL_CHARGE)){
            Buff.prolong(Dungeon.hero, Haste.class, 2f + Dungeon.hero.pointsInTalent(Talent.INERTIAL_CHARGE));
        }

        // set bless
        if(hero.hasTalent(Talent.BLESS_RETURN)){
            Buff.prolong( hero, Bless.class, (hero.pointsInTalent(Talent.BLESS_RETURN) * 0.25f + 0.5f) * (HpRemain - 1));
        }

    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    @Override
    public String icon() {
        return HeroIcon.END_BLESS;
    }

    @Override
    public Talent[] talents() {
        return new Talent[]{Talent.EFFICIENT_HEALING,Talent.INERTIAL_CHARGE,Talent.BLESS_RETURN,Talent.HEROIC_ENERGY};
    }
}
