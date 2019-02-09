package icu.dannyism.twister.mapping;

import icu.dannyism.bitwig.mappable.MappableBitwigParameter;
import icu.dannyism.twister.MidiFighterTwister;

public class RemoteControlKnobMapping {

    private final MidiFighterTwister.Knob knob;
    private boolean holdForFineControl = false;

    public RemoteControlKnobMapping(MidiFighterTwister.Knob knob, MappableBitwigParameter mappableUserControl) {
        this.knob = knob;
        knob.mapTo(mappableUserControl);
        knob.getButton().onDown(() -> {
            if (holdForFineControl)
                knob.enableFineControl(true);
            else
                knob.toggleFineControl();
        });
        knob.getButton().onUp(() -> {
            if (holdForFineControl)
                knob.enableFineControl(false);
        });
        knob.getButton().onDoublePress(mappableUserControl::reset);
    }

    public void enableHoldForFineControl(boolean enabled) {
        if (enabled)
            knob.enableFineControl(false);
        holdForFineControl = enabled;
    }

}
