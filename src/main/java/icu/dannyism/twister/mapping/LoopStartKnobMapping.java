package icu.dannyism.twister.mapping;

import com.bitwig.extension.controller.api.SettableBeatTimeValue;
import com.bitwig.extension.controller.api.Transport;
import icu.dannyism.controller.mappable.DiscreteMappableParameter;
import icu.dannyism.twister.AnimationState;
import icu.dannyism.twister.MidiFighterTwister;

public class LoopStartKnobMapping {

    private static final double BEATS_PER_COURSE_STEP = 4.0;
    private static final double BEATS_PER_FINE_STEP = 1.0;

    private final SettableBeatTimeValue loopStart;
    private boolean fineControl = false;
    private boolean parameterAdjusted = false;

    public LoopStartKnobMapping(MidiFighterTwister.Knob knob, Transport transport) {
        this.loopStart = transport.getInPosition();
        knob.mapTo(new MappableLoopStartPosition());
        knob.getButton().onDown(() -> {
            fineControl = true;
            parameterAdjusted = false;
        });
        knob.getButton().onUp(() -> {
            if (!parameterAdjusted)
                transport.isArrangerLoopEnabled().toggle();
            fineControl = false;
        });
    }

    private class MappableLoopStartPosition implements DiscreteMappableParameter {
        @Override
        public void increment(int delta) {
            if (fineControl)
                loopStart.inc((double) delta * BEATS_PER_FINE_STEP);
            else
                loopStart.inc((double) delta * BEATS_PER_COURSE_STEP);
            parameterAdjusted = true;
        }
    }

}
