package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.Device;
import icu.dannyism.controller.mappable.AbstractDiscreteMappableParameter;

public class MappableRemoteControlsPageSelector extends AbstractDiscreteMappableParameter {

    private static final boolean CYCLE = false;

    private final CursorRemoteControlsPage remoteControlsPage;
    private int pageIndex = 0;
    private int pageCount = 0;
    private boolean deviceExists = false;

    public MappableRemoteControlsPageSelector(Device device, CursorRemoteControlsPage remoteControlsPage) {
        this.remoteControlsPage = remoteControlsPage;
        remoteControlsPage.selectedPageIndex().addValueObserver((int index) -> {
            pageIndex = index;
            flushValue();
        });
        remoteControlsPage.pageCount().addValueObserver((int count) -> {
            pageCount = count;
            flushValue();
        });
        device.exists().addValueObserver((boolean exists) -> {
            deviceExists = exists;
            flushValue();
        });
    }

    @Override
    public void increment(int delta) {
        if (delta > 0)
            for (int i = 0; i < delta; i++)
                remoteControlsPage.selectNextPage(CYCLE);
        else if (delta < 0)
            for (int i = 0; i > delta; i--)
                remoteControlsPage.selectPreviousPage(CYCLE);
    }

    @Override
    protected void flushValue() {
        if (deviceExists) {
            if (pageCount > 1)
                value = (double) pageIndex / (double) (pageCount - 1);
            else if (pageCount == 1)
                value = 0.5;
            else
                value = -1.0;
        } else
            value = -1.0;

        super.flushValue();
    }

}
