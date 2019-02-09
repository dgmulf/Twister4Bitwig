package icu.dannyism.twister.mapping;

import com.bitwig.extension.controller.api.SettableBeatTimeValue;
import com.bitwig.extension.controller.api.Transport;
import icu.dannyism.controller.mappable.DiscreteMappableParameter;
import icu.dannyism.twister.MidiFighterTwister;

public class LoopEndKnobMapping {

    private static final double BEATS_PER_COURSE_STEP = 4.0;
    private static final double BEATS_PER_FINE_STEP = 1.0;

    private final SettableBeatTimeValue loopEnd;
    private boolean fineControl = false;
    private boolean parameterAdjusted = false;

    public LoopEndKnobMapping(MidiFighterTwister.Knob knob, Transport transport) {
        this.loopEnd = transport.getOutPosition();
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
                loopEnd.inc((double) delta * BEATS_PER_FINE_STEP);
            else
                loopEnd.inc((double) delta * BEATS_PER_COURSE_STEP);
            parameterAdjusted = true;
        }
    }

}
