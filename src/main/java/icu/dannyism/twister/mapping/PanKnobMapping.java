package icu.dannyism.twister.mapping;

import com.bitwig.extension.controller.api.Track;
import icu.dannyism.bitwig.mappable.MappableTrackPan;
import icu.dannyism.bitwig.mappable.MappableTrackVolume;
import icu.dannyism.twister.MidiFighterTwister;

public class PanKnobMapping {

    public PanKnobMapping(MidiFighterTwister.Knob knob, Track track) {
        final MappableTrackPan mappableTrackPan = new MappableTrackPan(track);

        knob.mapTo(mappableTrackPan);
        knob.getButton().onDown(() -> knob.enableFineControl(true));
        knob.getButton().onUp(() -> knob.enableFineControl(false));
        knob.getButton().onDoublePress(mappableTrackPan::reset);
    }

}
