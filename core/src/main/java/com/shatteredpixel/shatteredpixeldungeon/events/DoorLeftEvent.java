package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoor;

/** Emitted when a char steps off a special door cell. */
public class DoorLeftEvent extends GameEvent {

	private final Char ch;
	private final SpecialDoor door;
	private final int cell;

	public DoorLeftEvent(Char ch, SpecialDoor door, int cell) {
		this.ch = ch;
		this.door = door;
		this.cell = cell;
	}

	public Char getChar() { return ch; }

	public SpecialDoor getDoor() { return door; }

	public int getCell() { return cell; }
}
