package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.Track;

public class MappableTrackVolume extends MappableBitwigParameter {

    public MappableTrackVolume(Track track) {
        super(track.volume());
        track.position().addValueObserver((int position) -> { synchronizeAll(); });
    }

}
