package com.shatteredpixel.shatteredpixeldungeon.events;

import com.watabou.utils.DeviceCompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event bus manager.
 * Handles event registration, unregistration, and dispatching.
 * Supports priority-based subscriber ordering.
 */
public class EventManager {

    // Event type -> subscriber list
    private static final Map<Class<? extends GameEvent>, List<Subscriber>> subscribers = new ConcurrentHashMap<>();

    private static boolean initialized = false;

    /**
     * Initialize the event system.
     * Loads the compile-time generated index class.
     * Should be called once during game initialization.
     */
    public static void init() {
        if (initialized) {
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            Class<?> indexClass = Class.forName("com.shatteredpixel.shatteredpixeldungeon.events.EventSubscriberIndex");
            Method registerMethod = indexClass.getMethod("registerAll");
            registerMethod.invoke(null);
            DeviceCompat.log("EventManager", "EventSubscriberIndex loaded successfully");
        } catch (Exception e) {
            // This should not happen: only if you modified the EventSubscriberIndex file manually...
            DeviceCompat.log("EventManager", "EventSubscriberIndex load failed with error: ");

            e.printStackTrace();
            DeviceCompat.log("EventManager", "Maybe the EventSubscriberIndex file is corrupted. You can compile the project again to regenerate the file. ");
        }

        initialized = true;

        long endTime = System.currentTimeMillis();
        DeviceCompat.log("EventManager",
            "Initialization complete - time: " + (endTime - startTime) + "ms, event types: " + subscribers.size());
    }

    /**
     * Register a subscriber (called by generated index class).
     * Directly uses known method info, no class scanning required.
     *
     * @param eventType       Event type
     * @param subscriberClass Subscriber class
     * @param methodName      Method name
     * @param priority        Priority (higher number = executed first)
     */
    public static void registerSubscriber(
            Class<? extends GameEvent> eventType,
            Class<?> subscriberClass,
            String methodName,
            int priority) {

        try {
            Method method = subscriberClass.getDeclaredMethod(methodName, eventType);
            method.setAccessible(true);

            Subscriber subscriber = new Subscriber(method, priority, subscriberClass);
            subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(subscriber);

            // Sort by priority (ascending order, emit() iterates backwards so high priority executes first)
            List<Subscriber> list = subscribers.get(eventType);
            list.sort(Comparator.comparingInt(Subscriber::getPriority));

        } catch (NoSuchMethodException e) {
            DeviceCompat.log("EventManager", "Subscriber method not found: " + subscriberClass.getName() + "." + methodName);
        }
    }

    /**
     * Unregister all subscriptions for a class.
     *
     * @param clazz Class to unregister
     */
    public static void unregister(Class<?> clazz) {
        for (List<Subscriber> subscriberList : subscribers.values()) {
            subscriberList.removeIf(subscriber -> subscriber.getOwnerClass().equals(clazz));
        }
    }

    /**
     * Emit an event.
     * Calls all subscriber methods in priority order (high priority first).
     *
     * @param event Event to emit
     */
    public static void emit(GameEvent event) {
        Class<? extends GameEvent> eventType = event.getClass();
        List<Subscriber> subscriberList = subscribers.get(eventType);

        if (subscriberList == null || subscriberList.isEmpty()) {
            return;
        }

        // Iterate backwards: list is sorted ascending, so high priority is at the end
        for (int i = subscriberList.size() - 1; i >= 0; i--) {
            Subscriber subscriber = subscriberList.get(i);

            if (event.isCancelled()) {
                break;
            }

            try {
                synchronized (event){
                    subscriber.getMethod().invoke(null, event);
                }
            } catch (Exception e) {
                DeviceCompat.log("EventManager", "Event handling error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Clear all subscriptions and reset initialization state.
     */
    public static void clear() {
        subscribers.clear();
        initialized = false;
    }
}