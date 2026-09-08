package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

/**
 * 预知（法力学派 L2）：引导2回合后，等待20回合结束获得4点魔力。
 * 引导期间（前2回合）若英雄移动则中断。
 */
public class MagicProphecy extends Buff {

    private static final float CHANNEL_TURNS = 2f;
    private static final float TOTAL_TURNS = 20f;
    private static final float REWARD_MP = 4f;

    private int startPos;
    private float elapsed = 0f;

    @Override
    public boolean act() {
        if (target == null) {
            detach();
            return true;
        }
        elapsed += 1f;
        if (elapsed <= CHANNEL_TURNS && target.pos != startPos) {
            GLog.w(Messages.get(MagicProphecy.this, "interrupted"));
            detach();
            return true;
        }
        if (elapsed >= TOTAL_TURNS) {
            Buff.affect(target, MagicPoint.class).addPoints(REWARD_MP);
            GLog.p(Messages.get(MagicProphecy.this, "complete"));
            detach();
            return true;
        }
        spend(TICK);
        return true;
    }

    @Override
    public void fx(boolean on) {
        //no persistent icon
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("start_pos", startPos);
        bundle.put("elapsed", elapsed);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        startPos = bundle.getInt("start_pos");
        elapsed = bundle.getFloat("elapsed");
    }

    public static MagicProphecy apply(com.shatteredpixel.shatteredpixeldungeon.actors.Char c) {
        MagicProphecy p = Buff.affect(c, MagicProphecy.class);
        p.startPos = c.pos;
        p.elapsed = 0f;
        return p;
    }
}
