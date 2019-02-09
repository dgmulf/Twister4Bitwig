package icu.dannyism.controller.mappable;

import icu.dannyism.controller.SynchronizedHardwareControl;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractContinuousMappableParameter implements ContinuousMappableParameter {

    protected double value = 0.0;
    protected final Set<SynchronizedHardwareControl> linkedControls = new HashSet<>();

    @Override
    public boolean subscribe(SynchronizedHardwareControl control) {
        linkedControls.add(control);
        control.set(value);
        return true;
    }

    @Override
    public void unsubscribe(SynchronizedHardwareControl control) {
        linkedControls.remove(control);
    }

    protected void flushValue() {
        for (SynchronizedHardwareControl control : linkedControls)
            control.set(value);
    }

    protected void synchronizeAll() {
        for (SynchronizedHardwareControl control : linkedControls)
            control.synchronize();
    }
    
}
