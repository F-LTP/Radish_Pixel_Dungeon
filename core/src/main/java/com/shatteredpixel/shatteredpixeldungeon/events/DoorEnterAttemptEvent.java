package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SpecialDoor;

/** Emitted before a char enters a special door; subscribers may cancel. */
public class DoorEnterAttemptEvent extends GameEvent {

	private final Char ch;
	private final SpecialDoor door;
	private final int fromCell;
	private final int toCell;
	private String blockedMessage = "";

	public DoorEnterAttemptEvent(Char ch, SpecialDoor door, int fromCell, int toCell) {
		this.ch = ch;
		this.door = door;
		this.fromCell = fromCell;
		this.toCell = toCell;
	}

	public Char getChar() { return ch; }

	public SpecialDoor getDoor() { return door; }

	public int getFromCell() { return fromCell; }

	public int getToCell() { return toCell; }

	public String getBlockedMessage() { return blockedMessage; }

	public void block(String message) {
		this.blockedMessage = message;
		cancel();
	}
}
