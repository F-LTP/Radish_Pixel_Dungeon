package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;

/** Emitted after a buff is successfully attached to a creature. */
public class BuffAppliedEvent extends GameEvent {
    private final Char target;
    private final Buff buff;
    private final Source source;

    public BuffAppliedEvent(Char target, Buff buff, Source source) {
        this.target = target;
        this.buff = buff;
        this.source = source;
    }

    public Char getTarget() { return target; }
    public Buff getBuff() { return buff; }
    public Source getSource() { return source; }

    public enum Source {
        GAMEPLAY,
        RESTORE
    }
}
