package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.*;

public class MappableRemoteControl extends MappableBitwigParameter {

    public MappableRemoteControl(Track track, Device device, CursorRemoteControlsPage remoteControlsPage, int index)
    {
        super(remoteControlsPage.getParameter(index));

        track.position().addValueObserver((int position) -> { synchronizeAll(); });
        device.position().addValueObserver((int position) -> { synchronizeAll(); });
        remoteControlsPage.selectedPageIndex().addValueObserver((int position) -> { synchronizeAll(); });
    }

}
