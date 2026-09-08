package com.shatteredpixel.shatteredpixeldungeon.events.processor;

/**
 * Subscriber info collected at compile time.
 */
public class SubscriberInfo {
    public final String className;
    public final String packageName;
    public final String simpleClassName;
    public final String methodName;
    public final String eventTypeName;
    public final int priority;

    public SubscriberInfo(String className, String packageName, String simpleClassName, String methodName, String eventTypeName, int priority) {
        this.className = className;
        this.packageName = packageName;
        this.simpleClassName = simpleClassName;
        this.methodName = methodName;
        this.eventTypeName = eventTypeName;
        this.priority = priority;
    }
}
