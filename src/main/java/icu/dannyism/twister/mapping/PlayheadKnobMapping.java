package icu.dannyism.twister.mapping;

import com.bitwig.extension.controller.api.Transport;
import icu.dannyism.controller.mappable.DiscreteMappableParameter;
import icu.dannyism.twister.AnimationState;
import icu.dannyism.twister.MidiFighterTwister;

public class PlayheadKnobMapping {

    private static final double BEATS_PER_COURSE_STEP = 4.0;
    private static final double BEATS_PER_FINE_STEP = 1.0;

    private final MidiFighterTwister.Knob knob;
    private final Transport transport;
    private boolean fineControl = false;
    private boolean parameterAdjusted = false;
    private boolean flashOnPlay;

    public PlayheadKnobMapping(MidiFighterTwister.Knob knob, Transport transport) {
        this.knob = knob;
        this.transport = transport;
        knob.mapTo(new MappablePlayheadPosition());
        knob.getButton().onDown(() -> {
            fineControl = true;
            parameterAdjusted = false;
        });
        knob.getButton().onUp(() -> {
            if (!parameterAdjusted)
                transport.togglePlay();
            fineControl = false;
        });
        transport.isPlaying().addValueObserver((boolean playing) -> {
            if (flashOnPlay)
                knob.setAnimationState(playing ? AnimationState.RGB_TOGGLE_EVERY_EIGHTH : AnimationState.RGB_NONE);
        });
    }

    public void enableFlashOnPlay(boolean enabled) {
        if (!enabled)
            knob.setAnimationState(AnimationState.RGB_NONE);
        flashOnPlay = enabled;
    }

    private class MappablePlayheadPosition implements DiscreteMappableParameter {
        @Override
        public void increment(int delta) {
            if (fineControl)
                transport.incPosition((double) delta * BEATS_PER_FINE_STEP, true);
            else
                transport.incPosition((double) delta * BEATS_PER_COURSE_STEP, true);
            parameterAdjusted = true;
        }
    }

}
