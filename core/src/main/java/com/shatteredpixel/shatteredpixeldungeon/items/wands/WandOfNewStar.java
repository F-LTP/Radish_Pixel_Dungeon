package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

import java.util.ArrayList;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

public class WandOfNewStar extends DamageWand {

    {
        image = ItemSpriteSheet.WAND_NEWSTAR;
    }

    private void executeStarEffect(Char centerChar) {
        int radius = 1 + (buffedLvl() / 4);

        int x = centerChar.pos % Dungeon.level.width();
        int y = centerChar.pos / Dungeon.level.width();

        Ballistica bolt;
        if (Math.max(x, Dungeon.level.width() - x) >= Math.max(y, Dungeon.level.height() - y)) {
            if (x > Dungeon.level.width() / 2) {
                bolt = new Ballistica(centerChar.pos, centerChar.pos - 1, Ballistica.WONT_STOP);
            } else {
                bolt = new Ballistica(centerChar.pos, centerChar.pos + 1, Ballistica.WONT_STOP);
            }
        } else {
            if (y > Dungeon.level.height() / 2) {
                bolt = new Ballistica(centerChar.pos, centerChar.pos - Dungeon.level.width(), Ballistica.WONT_STOP);
            } else {
                bolt = new Ballistica(centerChar.pos, centerChar.pos + Dungeon.level.width(), Ballistica.WONT_STOP);
            }
        }

        ConeAOE aoe = new ConeAOE(bolt, radius, 360, Ballistica.STOP_TARGET);

        for (Ballistica ray : aoe.outerRays) {
            ((MagicMissile) centerChar.sprite.parent.recycle(MagicMissile.class)).reset(
                    MagicMissile.STAR,
                    centerChar.sprite,
                    ray.path.get(Math.min(radius / 2, ray.path.size() - 1)),
                    null
            );
        }

        ((MagicMissile) centerChar.sprite.parent.recycle(MagicMissile.class)).reset(
                MagicMissile.STAR,
                centerChar.sprite,
                bolt.path.get(Math.min(radius / 2, bolt.path.size() - 1)),
                () -> {
                    for (int pos : aoe.cells) {
                        Char target = Actor.findChar(pos);
                        if (target != null) {
                            int shield = buffedLvl() + 2;
                            boolean isHiddenMimic = target instanceof Mimic
                                    && target.alignment == Char.Alignment.NEUTRAL;
                            if (isHiddenMimic || target.alignment == Char.Alignment.ENEMY) {
                                target.damage(damageRoll() == 0 ? 1 : damageRoll(), DamageType.MAGICAL);
                                target.sprite.burst(0xFFFFFFFF, buffedLvl() / 2 + 2);
                            } else if (target.alignment == Char.Alignment.ALLY || target instanceof Hero) {
                                Buff.affect(target, Barrier.class).setShield(shield);
                            }
                        }
                    }
                });
    }

    @Override
    public void onZap(Ballistica bolt) {
        Char targetChar = Actor.findChar(bolt.collisionPos);

        // 判断施法中心
        Char centerChar;

        if (targetChar == null || targetChar.alignment == Char.Alignment.ENEMY) {
            // 目标是地面或敌人：以自己为中心
            centerChar = curUser;
        } else if (targetChar.alignment == Char.Alignment.ALLY) {
            // 目标是盟友：以盟友为中心
            centerChar = targetChar;
        } else {
            // 其他情况（如自己，但这里不会触发，因为 Wand.zapper 已处理）
            centerChar = curUser;
        }

        executeStarEffect(centerChar);
        Sample.INSTANCE.play(Assets.Sounds.ZAP);

        // WondrousResin 额外效果
        if (Random.Float() < WondrousResin.extraCurseEffectChance()) {
            WondrousResin.forcePositive = true;
            CursedWand.cursedZap(this, curUser, bolt, () -> {
                WondrousResin.forcePositive = false;
            });
        }
    }

    @Override
    public void fx(Ballistica bolt, Callback callback) {
        callback.call();
    }

    @Override
    public void staffFx(MagesStaff.StaffParticle particle) {
        particle.color(ColorMath.random(0xE44D3C, Window.RADISH));
        particle.am = 1f;
        particle.setLifespan(7f);
        particle.setSize(0.8f, 1.2f);
        float radius = 2f;
        int point = Random.Int(10);
        float angle;
        if (point % 2 == 0) {
            angle = point * 36f;
        } else {
            angle = point * 36f;
            radius *= 0.4f;
        }

        float rad = angle * (float) Math.PI / 180f;

        float offsetX = (float) Math.cos(rad) * radius;
        float offsetY = (float) Math.sin(rad) * radius;

        particle.x += offsetX;
        particle.y += offsetY;

        particle.shuffleXY(0.4f);

        particle.speed.x = -offsetY * 0.55f;
        particle.speed.y = offsetX * 0.55f;
    }

    @Override
    public String statsDesc() {
        int radius = 3 + (buffedLvl() / 4) * 2;
        if (levelKnown)
            return Messages.get(this, "stats_desc", radius, radius, min(), max(), buffedLvl() + 2);
        else
            return Messages.get(this, "stats_desc", 3, 3, min(0), max(0), 2);
    }

    @Override
    public String upgradeStat1(int level) {
        int radius = 3 + (level / 4) * 2;
        return Integer.toString(radius);
    }

    @Override
    public String upgradeStat2(int level) {
        return Integer.toString(level + 2);
    }

    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
        int triggerChance = 20;
        int wandTotalLevel = 0;
        if (buffedLvl() <= 12) {
            triggerChance += buffedLvl() * 2;
        } else {
            triggerChance += 24;
            triggerChance += (buffedLvl() - 12) / 2;
        }

        triggerChance = Math.min(triggerChance, 100);

        ArrayList<Wand> wands = hero.belongings.getAllItems(Wand.class);
        for (Wand w : wands.toArray(new Wand[0])) {
            wandTotalLevel += w.buffedLvl();
        }

        wandTotalLevel += staff.buffedLvl();
        if (Random.Int(100) < triggerChance) {
            if (hero.buff(Healing.StarHealing.class) == null) {
                Buff.affect(hero, Healing.StarHealing.class).setHeal(wandTotalLevel, 0, wandTotalLevel / 4);
            }
        }
    }

    @Override
    public int min(int lvl) {
        return 2 + lvl;
    }

    @Override
    public int max(int lvl) {
        return 5 + lvl * 4;
    }
}