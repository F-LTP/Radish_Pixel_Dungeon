package com.shatteredpixel.shatteredpixeldungeon.events;

/** A named signal published by a pressure plate, lever, or similar trigger cell. */
public class GateSignalEvent extends GameEvent {

	private final String signalId;
	private final int cell;

	public GateSignalEvent(String signalId, int cell) {
		this.signalId = signalId;
		this.cell = cell;
	}

	public String getSignalId() { return signalId; }

	public int getCell() { return cell; }
}
