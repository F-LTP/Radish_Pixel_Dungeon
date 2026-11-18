package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 瘴气（自然学派 L3）：像毒气瓶一样造成毒素伤害，并每回合为目标叠加2层中毒。
 */
public class MiasmaGas extends Blob implements Hero.Doom {

    // 瘴气对气体里的每个角色都生效，英雄站进去一样会被毒死。和原版毒气一样由气体自己结算英雄的死亡：
    // Hero.die 只对实现了 Doom 的死因调 onDeath，不实现的话存档照删、这一局却不进排行榜
    @Override
    public void onDeath() {
        Dungeon.fail(this);
        GLog.n(Messages.get(this, "ondeath"));
    }

    @Override
    protected void evolve() {
        super.evolve();

        int damage = 1 + Dungeon.scalingDepth() / 5;

        Char ch;
        int cell;

        for (int i = area.left; i < area.right; i++) {
            for (int j = area.top; j < area.bottom; j++) {
                cell = i + j * Dungeon.level.width();
                if (cur[cell] > 0 && (ch = Actor.findChar(cell)) != null) {
                    if (!ch.isImmuneTo(DamageType.TOXIC)) {
                        ch.damage(new DamageInfo(damage, DamageType.TOXIC, null, null, this));
                    }
                    if (ch.isAlive()) {
                        Buff.affect(ch, Poison.class).extend(2f);
                    }
                }
            }
        }
    }

    @Override
    public void use(BlobEmitter emitter) {
        super.use(emitter);
        emitter.pour(Speck.factory(Speck.TOXIC), 0.4f);
    }

    @Override
    public String tileDesc() {
        return Messages.get(this, "desc");
    }
}
