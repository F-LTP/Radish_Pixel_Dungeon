package com.shatteredpixel.shatteredpixeldungeon.events.processor;

/**
 * Subscriber info collected at compile time.
 */
public class SubscriberInfo {
    public final String className;
    public final String methodName;
    public final String eventTypeName;
    public final int priority;

    public SubscriberInfo(String className, String methodName, String eventTypeName, int priority) {
        this.className = className;
        this.methodName = methodName;
        this.eventTypeName = eventTypeName;
        this.priority = priority;
    }
}