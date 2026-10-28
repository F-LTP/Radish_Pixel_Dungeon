package com.shatteredpixel.shatteredpixeldungeon.events;

/**
 * Base class for all events.
 */
public abstract class GameEvent {
    private boolean cancelled = false;

    /**
     * Cancel the event.
     * High priority subscribers can cancel to prevent lower priority subscribers from executing.
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * Check if the event has been cancelled.
     * @return true if cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }
}