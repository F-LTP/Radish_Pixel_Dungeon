package com.shatteredpixel.shatteredpixeldungeon.events;

import java.lang.reflect.Method;

/**
 * Subscriber wrapper class.
 * Stores subscriber method metadata.
 */
class Subscriber {
    private final Method method;
    private final int priority;
    private final Class<?> ownerClass;

    public Subscriber(Method method, int priority, Class<?> ownerClass) {
        this.method = method;
        this.priority = priority;
        this.ownerClass = ownerClass;
    }

    public Method getMethod() {
        return method;
    }

    public int getPriority() {
        return priority;
    }

    public Class<?> getOwnerClass() {
        return ownerClass;
    }
}