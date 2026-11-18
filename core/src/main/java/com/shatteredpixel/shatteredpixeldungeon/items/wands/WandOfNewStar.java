package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
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

    private int radius() {
        return 2 + (buffedLvl() / 3);
    }

    /** 这次施放以谁为圆心：点盟友时是那名盟友，点地面、敌人或其它角色时是施法者自己。 */
    private Char centerOf(Ballistica bolt) {
        Char targetChar = Actor.findChar(bolt.collisionPos);
        if (targetChar != null && targetChar.alignment == Char.Alignment.ALLY) {
            return targetChar;
        }
        return curUser;
    }

    /** 新星的范围。ConeAOE 要一条从圆心出发的射线来定朝向，这里朝地图更空的一侧取，保证射线不是空的。 */
    private ConeAOE starArea(Char centerChar) {
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

        return new ConeAOE(bolt, radius(), 360, Ballistica.STOP_TARGET);
    }

    // 效果在这里当场结算，特效全部在 fx() 里。原先伤害和护盾挂在一枚飞行特效的回调上，而回合在此之前就已经结束：
    // 身边的怪趁这段时间出手，攻击动画播到一半被新星打死，动画被死亡动画顶掉，再没有人替它调 next()，游戏卡死在它的回合上。
    @Override
    public void onZap(Ballistica bolt) {
        ConeAOE aoe = starArea(centerOf(bolt));
        int shield = buffedLvl() + 2;

        for (int pos : aoe.cells) {
            Char target = Actor.findChar(pos);
            if (target == null) continue;

            boolean isHiddenMimic = target instanceof Mimic
                    && target.alignment == Char.Alignment.NEUTRAL;
            if (isHiddenMimic || target.alignment == Char.Alignment.ENEMY) {
                wandProc(target, chargesPerCast());
                // 伤害只掷一次：每掷一次都会消耗一层法杖增伤之类的效果
                target.damage(DamageInfo.of(Math.max(1, damageRoll()), DamageType.HOLY, curUser, this));
                if (target.sprite != null) {
                    target.sprite.burst(0xFFFFFFFF, buffedLvl() / 2 + 2);
                }
            } else if (target.alignment == Char.Alignment.ALLY || target instanceof Hero) {
                Buff.affect(target, Barrier.class).setShield(shield);
            }
        }

        // WondrousResin 的额外效果由 Wand.zap() 统一处理（那里会在回调里调 wandUsed()）。
        // 这里再调一次 cursedZap 会重复施法，并且它的回调不释放 busy，导致施法后卡住。
    }

    @Override
    public void fx(Ballistica bolt, Callback callback) {
        Sample.INSTANCE.play(Assets.Sounds.ZAP);

        // callback 负责结算效果并结束回合，无论特效放没放出来都必须正好调用一次
        boolean launched = false;
        try {
            Char centerChar = centerOf(bolt);
            // 圆心角色的精灵可能不在场景里（例如刚生成或刚死亡的盟友），这时不放特效
            CharSprite origin = (centerChar.sprite != null && centerChar.sprite.parent != null)
                    ? centerChar.sprite : null;

            if (origin != null) {
                ConeAOE aoe = starArea(centerChar);
                int radius = radius();
                for (Ballistica ray : aoe.outerRays) {
                    if (ray.path.isEmpty()) continue;
                    ((MagicMissile) origin.parent.recycle(MagicMissile.class)).reset(
                            MagicMissile.STAR,
                            origin,
                            ray.path.get(Math.min(radius / 2, ray.path.size() - 1)),
                            null
                    );
                }

                Ballistica core = aoe.coreRay;
                if (!core.path.isEmpty()) {
                    ((MagicMissile) origin.parent.recycle(MagicMissile.class)).reset(
                            MagicMissile.STAR,
                            origin,
                            core.path.get(Math.min(radius / 2, core.path.size() - 1)),
                            callback
                    );
                    launched = true;
                }
            }
        } catch (Exception e) {
            ShatteredPixelDungeon.reportException(e);
        }

        if (!launched) {
            callback.call();
        }
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
        int radius = 5 + (buffedLvl() / 3) * 2;
        if (levelKnown)
            return Messages.get(this, "stats_desc", radius, radius, rollMin(buffedLvl()), rollMax(buffedLvl()), buffedLvl() + 2);
        else
            return Messages.get(this, "stats_desc", 5, 5, min(0), max(0), 2);
    }

    @Override
    public String upgradeStat1(int level) {
        int radius = 5 + (level / 3) * 2;
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

        //蓄能打击天赋会放大法杖近战附带效果的触发率，其它法杖都乘了这个倍率
        float finalChance = Math.min(100f, triggerChance * procChanceMultiplier(attacker));

        ArrayList<Wand> wands = hero.belongings.getAllItems(Wand.class);
        for (Wand w : wands.toArray(new Wand[0])) {
            wandTotalLevel += w.buffedLvl();
        }

        wandTotalLevel += staff.buffedLvl();
        if (Random.Int(100) < finalChance) {
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