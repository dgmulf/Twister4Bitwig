package icu.dannyism.twister.mapping;

import com.bitwig.extension.controller.api.Track;
import icu.dannyism.twister.MidiFighterTwister;

public class ColoredVolumeKnobMapping extends VolumeKnobMapping {

    private static final int MUTE_COLOR = 87;
    private static final int SOLO_COLOR = 63;

    private boolean muteEnabled;
    private boolean soloEnabled;

    public ColoredVolumeKnobMapping(MidiFighterTwister.Knob knob, Track track) {
        super(knob, track);

        track.mute().addValueObserver((boolean mute) -> {
            muteEnabled = mute;
            updateColor();
        });
        track.solo().addValueObserver((boolean solo) -> {
            soloEnabled = solo;
            updateColor();
        });
    }

    private void updateColor() {
        if (soloEnabled)
            knob.setColor(SOLO_COLOR);
        else {
            if (muteEnabled)
                knob.setColor(MUTE_COLOR);
            else
                knob.setColor(0);
        }
    }

}
