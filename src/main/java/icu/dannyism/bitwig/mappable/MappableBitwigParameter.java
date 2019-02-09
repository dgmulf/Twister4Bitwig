package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.Parameter;
import icu.dannyism.controller.mappable.AbstractContinuousMappableParameter;

public class MappableBitwigParameter extends AbstractContinuousMappableParameter {

    private final Parameter parameter;

    public MappableBitwigParameter(Parameter parameter) {
        this.parameter = parameter;
        parameter.value().addValueObserver((double value) -> {
            this.value = value;
            flushValue();
        });
    }

    @Override
    public void set(double value) {
        parameter.setImmediately(value);
    }

    @Override
    public void increment(double delta) {
        parameter.inc(delta);
    }

    public void reset() {
        parameter.reset();
        synchronizeAll();
    }

}
