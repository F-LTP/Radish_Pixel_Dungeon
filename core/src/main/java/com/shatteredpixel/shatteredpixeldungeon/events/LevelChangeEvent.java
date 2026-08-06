package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

/**
 * 关卡变更事件
 * 当英雄进入新关卡时触发
 */
public class LevelChangeEvent extends GameEvent {
    private final Level previousLevel;
    private final Level newLevel;
    private final TransitionType transitionType;

    public LevelChangeEvent(Level previousLevel, Level newLevel, TransitionType transitionType) {
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.transitionType = transitionType;
    }

    public Level getPreviousLevel() {
        return previousLevel;
    }

    public Level getNewLevel() {
        return newLevel;
    }

    public TransitionType getTransitionType() {
        return transitionType;
    }

    public enum TransitionType {
        DESCEND,        // 下楼
        ASCEND,         // 上楼
        TELEPORT,       // 传送
        FALL,           // 掉落
        PORTAL          // 传送门
    }
}
