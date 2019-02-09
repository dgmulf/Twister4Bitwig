package icu.dannyism.controller.mappable;

import icu.dannyism.controller.SynchronizedHardwareControl;

public interface ContinuousMappableParameter {
    void set(double value);
    void increment(double delta);
    default boolean subscribe(SynchronizedHardwareControl control) { return false; }
    default void unsubscribe(SynchronizedHardwareControl control) {}
}
