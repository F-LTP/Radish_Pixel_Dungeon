package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import org.jetbrains.annotations.NotNull;

/**
 * Buff 挂接事件（可取消）。
 *
 * <p><b>发布时机</b>：{@link Buff#attachTo(Char)} 中 buff 已进入目标列表后发布；
 * 目标免疫或存档恢复期间（restoreDepth&gt;0）不发布。
 *
 * <p><b>取消语义</b>：cancel() 则附加失败——buff 从目标身上移除、目标头顶显示
 * "抵抗"字样、attachTo 返回 false。
 * RESTORE 为保留值，当前无代码使用。
 */
public class BuffAppliedEvent extends GameEvent {

	public enum Source {
		GAMEPLAY,
		RESTORE
	}

	private final Char target;
	private final Buff buff;
	private final Source source;

	public BuffAppliedEvent(@NotNull Char target, @NotNull Buff buff, @NotNull Source source) {
		this.target = target;
		this.buff = buff;
		this.source = source;
	}

	public @NotNull Char getTarget() { return target; }

	public @NotNull Buff getBuff() { return buff; }

	public @NotNull Source getSource() { return source; }
}
