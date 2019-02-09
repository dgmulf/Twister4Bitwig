package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.Track;

public class MappableTrackPan extends MappableBitwigParameter {

    public MappableTrackPan(Track track) {
        super(track.pan());
        track.position().addValueObserver((int position) -> { synchronizeAll(); });
    }

}
