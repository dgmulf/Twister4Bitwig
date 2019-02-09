package icu.dannyism.controller.mappable;

import icu.dannyism.controller.BidirectionalHardwareControl;

public interface DiscreteMappableParameter {
    void increment(int delta);
    default boolean subscribe(BidirectionalHardwareControl control) { return false; }
    default void unsubscribe(BidirectionalHardwareControl control) {}
}
