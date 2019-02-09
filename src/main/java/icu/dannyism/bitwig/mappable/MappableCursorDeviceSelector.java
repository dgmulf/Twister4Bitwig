package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.CursorDevice;
import icu.dannyism.controller.mappable.AbstractDiscreteMappableParameter;

public class MappableCursorDeviceSelector extends AbstractDiscreteMappableParameter {

    private final CursorDevice cursorDevice;
    private int deviceIndex = 0;
    private int deviceCount = 0;
    private boolean deviceExists;

    public MappableCursorDeviceSelector(CursorDevice cursorDevice) {
        this.cursorDevice = cursorDevice;
        cursorDevice.position().addValueObserver((int position) -> {
            deviceIndex = position;
            flushValue();
        });
        cursorDevice.createSiblingsDeviceBank(1).itemCount().addValueObserver((int count) -> {
            deviceCount = count;
            flushValue();
        });
        cursorDevice.exists().addValueObserver((boolean exists) -> {
            deviceExists = exists;
            flushValue();
        });
    }

    @Override
    public void increment(int delta) {
        if (delta > 0)
            for (int i = 0; i < delta; i++)
                cursorDevice.selectNext();
        else if (delta < 0)
            for (int i = 0; i > delta; i--)
                cursorDevice.selectPrevious();
    }

    @Override
    protected void flushValue() {
        if (deviceExists) {
            if (deviceCount > 1)
                value = (double) deviceIndex / (double) (deviceCount - 1);
            else if (deviceCount == 1)
                value = 0.5;
            else
                value = -1.0;
        } else
            value = -1.0;

        super.flushValue();
    }

}
