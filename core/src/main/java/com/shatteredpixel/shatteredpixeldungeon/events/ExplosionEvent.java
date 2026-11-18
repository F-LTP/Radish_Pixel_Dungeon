package com.shatteredpixel.shatteredpixeldungeon.events;

/** Emitted before an explosion destroys terrain or affects chars. */
public class ExplosionEvent extends GameEvent {

	private final int cell;

	public ExplosionEvent(int cell) {
		this.cell = cell;
	}

	public int getCell() { return cell; }
}
