package icu.dannyism.controller.mappable;

import icu.dannyism.controller.BidirectionalHardwareControl;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractDiscreteMappableParameter implements DiscreteMappableParameter {

    protected double value = -1.0;
    protected final Set<BidirectionalHardwareControl> linkedControls = new HashSet<>();

    @Override
    public boolean subscribe(BidirectionalHardwareControl control) {
        linkedControls.add(control);
        control.set(value);
        return true;
    }

    @Override
    public void unsubscribe(BidirectionalHardwareControl control) {
        linkedControls.remove(control);
    }

    protected void flushValue() {
        for (BidirectionalHardwareControl control : linkedControls)
            control.set(value);
    }
}
