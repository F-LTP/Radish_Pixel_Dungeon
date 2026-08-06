package com.shatteredpixel.shatteredpixeldungeon.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Note that the annotation is only retained until compilation
 * The function annotated is not modified so debugging is still usable
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
    Class<? extends GameEvent> event();

    int priority() default 0;
}