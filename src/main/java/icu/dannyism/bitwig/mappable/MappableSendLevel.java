package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.SendBank;
import com.bitwig.extension.controller.api.Track;

public class MappableSendLevel extends MappableBitwigParameter {

    public MappableSendLevel(Track track, SendBank sendBank, int index) {
        super(sendBank.getItemAt(index));
        track.position().addValueObserver((int position) -> synchronizeAll());
        sendBank.scrollPosition().addValueObserver((int position) -> synchronizeAll());
    }

}
