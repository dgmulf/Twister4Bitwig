package icu.dannyism.controller;

import java.util.HashSet;
import java.util.Set;

public class Button {

    private static final long LONG_PRESS_DURATION = 200;
    private static final long DOUBLE_PRESS_DURATION = 270;

    private boolean isDown = false;
    private long lastDownTime;
    private final Set<Runnable> buttonDownObservers = new HashSet<>();
    private final Set<Runnable> buttonUpObservers = new HashSet<>();
    private final Set<Runnable> doublePressObservers = new HashSet<>();
    private final Set<Runnable> shortPressObservers = new HashSet<>();
    private final Set<Runnable> longPressObservers = new HashSet<>();

    public void down() {
        long now = System.nanoTime();
        for (Runnable observer : buttonDownObservers)
            observer.run();
        if ((now - lastDownTime) / 1000000 < DOUBLE_PRESS_DURATION)
            for (Runnable observer : doublePressObservers)
                observer.run();
        lastDownTime = now;
        this.isDown = true;
    }

    public void up() {
        long pressDuration = (System.nanoTime() - lastDownTime) / 1000000;
        for (Runnable observer : buttonUpObservers)
            observer.run();
        if (pressDuration < LONG_PRESS_DURATION)
            for (Runnable observer : shortPressObservers)
                observer.run();
        else
            for (Runnable observer : longPressObservers)
                observer.run();
        this.isDown = false;
    }

    public void onDown(Runnable observer) {
        buttonDownObservers.add(observer);
    }

    public void onUp(Runnable observer) {
        buttonUpObservers.add(observer);
    }

    public void onDoublePress(Runnable observer) { doublePressObservers.add(observer); }

    public void onShortPress(Runnable observer) {
        shortPressObservers.add(observer);
    }

    public void onLongPress(Runnable observer) {
        longPressObservers.add(observer);
    }

    public boolean isDown() {
        return this.isDown;
    }

}
