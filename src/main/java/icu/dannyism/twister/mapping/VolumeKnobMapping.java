package icu.dannyism.twister.mapping;

import com.bitwig.extension.controller.api.Track;
import icu.dannyism.bitwig.mappable.MappableTrackVolume;
import icu.dannyism.twister.MidiFighterTwister;

public class VolumeKnobMapping {

    final MidiFighterTwister.Knob knob;
    final Track track;
    private boolean parameterAdjusted = false;

    public VolumeKnobMapping(MidiFighterTwister.Knob knob, Track track) {
        this.knob = knob;
        this.track = track;

        knob.mapTo(new AdjustmentAwareMappableVolume(track));
        knob.getButton().onDown(new KnobDownTask());
        knob.getButton().onUp(new KnobUpTask());
        knob.getButton().onShortPress(new KnobShortPressTask());
        knob.getButton().onLongPress(new KnobLongPressTask());
    }

    private class AdjustmentAwareMappableVolume extends MappableTrackVolume {

        AdjustmentAwareMappableVolume(Track track) {
            super(track);
        }

        @Override
        public void increment(double delta) {
            super.increment(delta);
            parameterAdjusted = true;
        }

        @Override
        public void set(double value) {
            super.set(value);
            parameterAdjusted = true;
        }
    }

    private class KnobDownTask implements Runnable {
        @Override
        public void run() {
            parameterAdjusted = false;
            knob.enableFineControl(true);
        }
    }

    private class KnobUpTask implements Runnable {
        @Override
        public void run() {
            knob.enableFineControl(false);
        }
    }

    private class KnobShortPressTask implements Runnable {
        @Override
        public void run() {
            if (!parameterAdjusted)
                track.mute().toggle();
        }
    }

    private class KnobLongPressTask implements Runnable {
        @Override
        public void run() {
            if (!parameterAdjusted)
                track.solo().toggle();
        }
    }

}
